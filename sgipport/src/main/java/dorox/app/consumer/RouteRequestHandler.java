package dorox.app.consumer;

import com.rabbitmq.client.Channel;
import com.twitter.bijection.Injection;
import com.zx.sms.codec.sgip12.msg.SgipSubmitRequestMessage;
import com.zx.sms.common.util.ChannelUtil;
import com.zx.sms.common.util.SequenceNumber;
import dorox.app.UpPortMain;
import dorox.app.mq.event.RouteRequestEvent;
import dorox.app.sgip.SgipUpPort;
import dorox.app.util.AvroUtil;
import dorox.app.util.CacheConfig;
import dorox.app.util.Statics;
import dorox.app.vo.ReSendMsg;
import dorox.app.vo.SubmitVoSGIP;
import io.netty.channel.ChannelFuture;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class RouteRequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(RouteRequestHandler.class);

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue(value = "${rabbitmq.routerequest.queue}",
                            durable="${rabbitmq.queue.durable}"),
                    exchange = @Exchange(value = "${rabbitmq.routerequest.topic}",
                            durable="${rabbitmq.exchange.durable}",
                            type= "${rabbitmq.exchange.type}",
                            ignoreDeclarationExceptions = "${rabbitmq.exchange.ignoreDeclarationExceptions}"),
                    key = "${rabbitmq.routerequest.key}"
            )
    })
    @RabbitHandler
    public void handler(@Payload byte[] record, Channel channel,
                                  @Headers Map<String, Object> headers) throws Exception {

        Long deliveryTag = (Long)headers.get(AmqpHeaders.DELIVERY_TAG);
        try{
            Injection<GenericRecord, byte[]> recordInjection = AvroUtil.getGenericAvroCodecs(RouteRequestEvent.class);
            GenericRecord routeRequest = recordInjection.invert(record).get();
            logger.info("routeRequest:{}", routeRequest);
            sendCmppRequest(routeRequest);
        }catch (Exception e){
            logger.error("exception:{}", e);
        }finally {
            //手工ACK
            channel.basicAck(deliveryTag, false);
        }
    }

    private void sendCmppRequest(GenericRecord routeRequest) {

        String phone = String.valueOf(routeRequest.get("phone"));
        String srcId = String.valueOf(routeRequest.get("srcId"));
        String upSrcId = String.valueOf(routeRequest.get("upSrcId"));
        String messageId = String.valueOf(routeRequest.get("messageId"));
        String downName = String.valueOf(routeRequest.get("downName"));
        String upName = String.valueOf(routeRequest.get("upName"));
        List<Utf8> msgIds = (List<Utf8>)routeRequest.get("msgIds");
		String content = String.valueOf(routeRequest.get("content"));

        //流控
        if(UpPortMain.CHANNEL_SPEED_MAP.get(downName + upName) != null) {
            UpPortMain.CHANNEL_SPEED_MAP.get(downName + upName).acquire(msgIds.size());
        }

		SgipSubmitRequestMessage submitRequest = new SgipSubmitRequestMessage();
		submitRequest.setSpnumber(upSrcId);
		submitRequest.setUsernumber(phone);
		submitRequest.setMsgContent(content);
		submitRequest.setReportflag((short)1);
        submitRequest.setReserve(messageId);
		SgipUpPort sgipPort = Statics.UP_PORT_MAP.get(upName);
		if(sgipPort!=null){
            submitRequest.setCorpid(String.valueOf(sgipPort.getNodeId()));
//			submitRequest.setCorpid("44379");
			submitRequest.setServicetype(String.valueOf(sgipPort.getNodeId()));
		}

        ChannelFuture channelFuture = ChannelUtil.asyncWriteToEntity(upName, submitRequest);
        if(channelFuture!=null){
            channelFuture.addListener((f)->{
                if(f.isSuccess()){
                }else{
                    logger.error("err for write by netty1 {} - {}", upName, submitRequest);
                    Statics.RESEND_QUEUE.add(new ReSendMsg(downName, upName, submitRequest,msgIds.size()));
                }
            });
        }else{
            logger.error("err2 for write by netty2 {} - {}", upName, submitRequest);
            Statics.RESEND_QUEUE.add(new ReSendMsg(downName, upName, submitRequest,msgIds.size()));
        }

//		SequenceNumber sequenceNumber = submitRequest.getSequenceNumber();
//		sequenceNumber.setNodeIds(sgipPort.getNodeId());
//		String key = upName + sequenceNumber.toString();

        List<String> list = Collections.synchronizedList(new ArrayList<>());
        for(Utf8 utf8 : msgIds){
            list.add(utf8.toString());
        }

		SubmitVoSGIP submitVo = new SubmitVoSGIP(upName, downName, messageId, phone, srcId, list);
		CacheConfig.submitVoCacheSGIP.put(messageId, submitVo);
        logger.info("SgipsubmitVoCacheSGIP for key:{} and result:{}", messageId, submitVo);

    }
}
