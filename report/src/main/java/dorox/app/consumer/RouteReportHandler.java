package dorox.app.consumer;

import com.rabbitmq.client.Channel;
import com.twitter.bijection.Injection;
import dorox.app.ReportMain;
import dorox.app.mq.event.RouteReportEvent;
import dorox.app.util.AvroUtil;
import org.apache.avro.generic.GenericRecord;
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
import java.util.concurrent.atomic.LongAdder;

@Component
public class RouteReportHandler {
    private static final Logger logger = LoggerFactory.getLogger(RouteReportHandler.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue(value = "${rabbitmq.routereport.queue}",
                            durable="${rabbitmq.queue.durable}"),
                    exchange = @Exchange(value = "${rabbitmq.routereport.topic}",
                            durable="${rabbitmq.exchange.durable}",
                            type= "${rabbitmq.exchange.type}",
                            ignoreDeclarationExceptions = "${rabbitmq.exchange.ignoreDeclarationExceptions}"),
                    key = "${rabbitmq.routereport.key}"
            )
    })
    @RabbitHandler
    public void handler(@Payload byte[] record, Channel channel,
                        @Headers Map<String, Object> headers) throws Exception {

        Long deliveryTag = (Long)headers.get(AmqpHeaders.DELIVERY_TAG);
        try{
            Injection<GenericRecord, byte[]> recordInjection = AvroUtil.getGenericAvroCodecs(RouteReportEvent.class);
            GenericRecord routeReport = recordInjection.invert(record).get();

            logger.info("routeReport:{}", routeReport);
            RouteReportEvent routeReportEvent = AvroUtil.eventFromRecord(routeReport, RouteReportEvent.class);
            if(routeReportEvent != null) {
                ReportMain.REPORT_AGGREGATION_QUEUE.add(routeReportEvent);

                /*统计每秒请求*/
                LongAdder adder = ReportMain.UpRequestCounter.get(routeReportEvent.getUpName());
                if(adder == null){
                    adder = new LongAdder();
                    ReportMain.UpRequestCounter.put(routeReportEvent.getUpName(),adder);
                }
                adder.add(routeReportEvent.getMsgCount());
            }
        }catch (Exception e){
            logger.error("exception:{}", e);
        }finally {
            //手工ACK
            channel.basicAck(deliveryTag, false);
        }
    }


}
