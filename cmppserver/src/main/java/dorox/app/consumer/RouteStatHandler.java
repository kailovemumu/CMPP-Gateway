package dorox.app.consumer;

import com.rabbitmq.client.Channel;
import com.twitter.bijection.Injection;
import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppReportRequestMessage;
import com.zx.sms.common.util.CachedMillisecondClock;
import com.zx.sms.common.util.ChannelUtil;
import com.zx.sms.common.util.MsgId;
import dorox.app.mq.event.RouteStatEvent;
import dorox.app.mq.event.StatArrivedEvent;
import dorox.app.util.AvroUtil;
import dorox.app.util.MqUtil;
import dorox.app.util.Statics;
import dorox.app.vo.ReSendMsg;
import io.netty.channel.ChannelFuture;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RouteStatHandler {
    //发送给通道层得到的响应消息。
    private static final Logger logger = LoggerFactory.getLogger(RouteStatHandler.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue(value = "${rabbitmq.routestat.queue}",
                            durable="${rabbitmq.queue.durable}"),
                    exchange = @Exchange(value = "${rabbitmq.routestat.topic}",
                            durable="${rabbitmq.exchange.durable}",
                            type= "${rabbitmq.exchange.type}",
                            ignoreDeclarationExceptions = "${rabbitmq.exchange.ignoreDeclarationExceptions}"),
                    key = "${rabbitmq.routestat.key}"
            )
    })
    @RabbitHandler
    public void handler(@Payload byte[] record, Channel channel,
                        @Headers Map<String, Object> headers) throws Exception {

        Long deliveryTag = (Long)headers.get(AmqpHeaders.DELIVERY_TAG);

        try{
            Injection<GenericRecord, byte[]> recordInjection = AvroUtil.getGenericAvroCodecs(RouteStatEvent.class);
            GenericRecord routeStat = recordInjection.invert(record).get();
            logger.info("routeStat:{}", routeStat);
            sendCmppDeliver(routeStat);
        }catch (Exception e){
            logger.error("exception:{}", e);
        }finally {
            //手工ACK
            channel.basicAck(deliveryTag, false);
        }
    }

    private void sendCmppDeliver(GenericRecord genericRecord) {
        CmppDeliverRequestMessage deliver = new CmppDeliverRequestMessage();
        deliver.setDestId(String.valueOf(genericRecord.get("srcId")));
        deliver.setSrcterminalId(String.valueOf(genericRecord.get("phone")));
        CmppReportRequestMessage report = new CmppReportRequestMessage();
        report.setDestterminalId(deliver.getSrcterminalId());
        report.setMsgId(new MsgId(String.valueOf(genericRecord.get("msgId"))));
        String t = DateFormatUtils.format(CachedMillisecondClock.INS.now(), "yyMMddHHmm");
        report.setSubmitTime(t);
        report.setDoneTime(t);
        report.setStat(String.valueOf(genericRecord.get("stat")));
        report.setSmscSequence(0);
        deliver.setReportRequestMessage(report);
        deliver.setReserved(String.valueOf(genericRecord.get("messageId")));
//        Statics.DOWN_PORT_MAP.get(String.valueOf(genericRecord.get("downName"))).NETTY_QUEUE.add(deliver);
        String downName = String.valueOf(genericRecord.get("downName"));
        ChannelFuture channelFuture = ChannelUtil.asyncWriteToEntity(downName , deliver);

        if(channelFuture!=null){
            channelFuture.addListener((f)->{
                if(f.isSuccess()){

                    StatArrivedEvent event = new StatArrivedEvent(
                            String.valueOf(genericRecord.get("messageId")),
                            String.valueOf(genericRecord.get("msgId")),
                            String.valueOf(genericRecord.get("stat")));

                    MqUtil.sendMsg(event, rabbitTemplate,"report");

                }else{
                    Statics.RESEND_QUEUE.add(new ReSendMsg(downName, deliver));
                    logger.error("err for write by netty {} - {}", downName, deliver);
                }
            });
        }else{
            Statics.RESEND_QUEUE.add(new ReSendMsg(downName, deliver));
            logger.error("err2 for write by netty {} - {}", downName, deliver);
        }
    }
}
