package dorox.app.consumer;

import com.rabbitmq.client.Channel;
import com.twitter.bijection.Injection;
import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.common.util.ChannelUtil;
import dorox.app.mq.event.PortMoEvent;
import dorox.app.util.AvroUtil;
import dorox.app.util.Statics;
import dorox.app.vo.ReSendMsg;
import io.netty.channel.ChannelFuture;
import org.apache.avro.generic.GenericRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PortMoHandler {
    private static final Logger logger = LoggerFactory.getLogger(PortMoHandler.class);

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue(value = "${rabbitmq.portmo.queue}",
                            durable="${rabbitmq.queue.durable}"),
                    exchange = @Exchange(value = "${rabbitmq.portmo.topic}",
                            durable="${rabbitmq.exchange.durable}",
                            type= "${rabbitmq.exchange.type}",
                            ignoreDeclarationExceptions = "${rabbitmq.exchange.ignoreDeclarationExceptions}"),
                    key = "${rabbitmq.portmo.key}"
            )
    })
    @RabbitHandler
    public void handler(@Payload byte[] record, Channel channel,
                        @Headers Map<String, Object> headers) throws Exception {

        Long deliveryTag = (Long)headers.get(AmqpHeaders.DELIVERY_TAG);

        try{
//            @TODO 这里是反序列化，将已经序列化传过来的队列中的数据反序列化成我们的对象，拆包
            Injection<GenericRecord, byte[]> recordInjection = AvroUtil.getGenericAvroCodecs(PortMoEvent.class);
            GenericRecord portMo = recordInjection.invert(record).get();
            logger.info("portMo:{}", portMo);
            sendCmppMo(portMo);
        }catch (Exception e){
            logger.error("exception:{}", e);
        }finally {
            //手工ACK
            channel.basicAck(deliveryTag, false);
        }
    }
//@TODO MQ消息，发送给SP
    private void sendCmppMo(GenericRecord portMo) {
        CmppDeliverRequestMessage deliver = new CmppDeliverRequestMessage();
        deliver.setDestId(String.valueOf(portMo.get("destId")));
        deliver.setSrcterminalId(String.valueOf(portMo.get("phone")));
        deliver.setMsgContent(String.valueOf(portMo.get("content")));

//        Statics.DOWN_PORT_MAP.get(String.valueOf(portMo.get("downName"))).NETTY_QUEUE.add(deliver);

        String downName =  String.valueOf(portMo.get("downName"));
        ChannelFuture channelFuture = ChannelUtil.asyncWriteToEntity(downName, deliver);

        if(channelFuture!=null){
            channelFuture.addListener((f)->{
                if(f.isSuccess()){
                    logger.info("sendCmppMo:[{}] success", portMo);
                }else{
                    Statics.RESEND_QUEUE.add(new ReSendMsg(downName, deliver));
                    logger.error("err for write by netty {} - {}", downName, deliver);
                }
            });
        }else{
            Statics.RESEND_QUEUE.add(new ReSendMsg(downName, deliver));
            logger.error("err2 for write by netty2 {} - {}", downName, deliver);
        }
    }
}
