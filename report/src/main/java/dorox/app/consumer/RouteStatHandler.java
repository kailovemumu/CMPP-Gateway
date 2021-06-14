package dorox.app.consumer;

import com.rabbitmq.client.Channel;
import com.twitter.bijection.Injection;
import dorox.app.ReportMain;
import dorox.app.mq.event.RouteStatEvent;
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
public class RouteStatHandler {
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
            RouteStatEvent routeStatEvent = AvroUtil.eventFromRecord(routeStat, RouteStatEvent.class);
            if(routeStatEvent != null){
                ReportMain.REPORT_AGGREGATION_QUEUE.add(routeStatEvent);

                /*统计每秒请求*/
                LongAdder downAdder = ReportMain.DownStatCounter.get(routeStatEvent.getDownName());
                if(downAdder == null){
                    downAdder = new LongAdder();
                    ReportMain.DownStatCounter.put(routeStatEvent.getDownName(),downAdder);
                }
                downAdder.increment();

                /*统计每秒请求*/
                LongAdder upAdder = ReportMain.UpStatCounter.get(routeStatEvent.getUpName());
                if(upAdder == null){
                    upAdder = new LongAdder();
                    ReportMain.UpStatCounter.put(routeStatEvent.getUpName(),upAdder);
                }
                upAdder.increment();
            }
        }catch (Exception e){
            logger.error("exception:{}", e);
        }finally {
            //手工ACK
            channel.basicAck(deliveryTag, false);
        }
    }


}
