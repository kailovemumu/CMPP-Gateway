package dorox.app.consumer;

import com.rabbitmq.client.Channel;
import com.twitter.bijection.Injection;
import dorox.app.manager.MobileInfo;
import dorox.app.manager.MobileInfoFlush;
import dorox.app.mq.event.RouteReportEvent;
import dorox.app.mq.event.RouteRequestEvent;
import dorox.app.mq.event.ServerRequestFixUpNameEvent;
import dorox.app.util.AvroUtil;
import dorox.app.util.MqUtil;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ServerRequestFixUpNameHandler {
    private static final Logger logger = LoggerFactory.getLogger(ServerRequestFixUpNameHandler.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private MobileInfoFlush mobileInfoFlush;

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue(value = "${rabbitmq.serverrequestfixupname.queue}",
                            durable="${rabbitmq.queue.durable}"),
                    exchange = @Exchange(value = "${rabbitmq.serverrequestfixupname.topic}",
                            durable="${rabbitmq.exchange.durable}",
                            type= "${rabbitmq.exchange.type}",
                            ignoreDeclarationExceptions = "${rabbitmq.exchange.ignoreDeclarationExceptions}"),
                    key = "${rabbitmq.serverrequestfixupname.key}"
            )
    })
    @RabbitHandler
    public void handler(@Payload byte[] record, Channel channel,
                        @Headers Map<String, Object> headers) throws Exception {

        Long deliveryTag = (Long)headers.get(AmqpHeaders.DELIVERY_TAG);
        try{
            Injection<GenericRecord, byte[]> recordInjection = AvroUtil.getGenericAvroCodecs(ServerRequestFixUpNameEvent.class);
            GenericRecord serverRequest = recordInjection.invert(record).get();
            logger.info("serverRequestFixUpName:{}", serverRequest);
            parseServerRequest(serverRequest);
        }catch (Exception e){
            logger.error("exception:{}", e);
        }finally {
            //手工ACK
            channel.basicAck(deliveryTag, false);
        }
    }

    /**
     * 1，根据downName获取
     * */
    private void parseServerRequest(GenericRecord serverRequest) {

        String downName = String.valueOf(serverRequest.get("downName"));
        String upName = String.valueOf(serverRequest.get("upName"));
        String messageId = String.valueOf(serverRequest.get("messageId"));
        String phone = String.valueOf(serverRequest.get("phone"));
        String srcId = String.valueOf(serverRequest.get("srcId"));
        String upSrcId = String.valueOf(serverRequest.get("upSrcId"));
        List<Utf8> msgIds = (List<Utf8>) serverRequest.get("msgIds");
        String content = String.valueOf(serverRequest.get("content"));
        long makeTime = (Long) serverRequest.get("makeTime");
        String downRegionCode = String.valueOf(serverRequest.get("downRegionCode"));
        String upRegionCode = String.valueOf(serverRequest.get("upRegionCode"));

        MobileInfo mobileInfo = mobileInfoFlush.getCityInfo(phone);

        RouteRequestEvent routeRequestEvent = new RouteRequestEvent(messageId, downName, upName, phone, content, srcId, upSrcId, msgIds,
                downRegionCode, upRegionCode);
        MqUtil.sendMsg(routeRequestEvent, rabbitTemplate, upRegionCode);

        RouteReportEvent routeReportEvent = new RouteReportEvent(messageId, upName, upRegionCode, mobileInfo.getIsp(),
                mobileInfo.getCityCode(), mobileInfo.getCity(),
                mobileInfo.getProvinceCode(), mobileInfo.getProvince(), msgIds.size(), upSrcId);
        MqUtil.sendMsg(routeReportEvent, rabbitTemplate, "report");

    }
}
