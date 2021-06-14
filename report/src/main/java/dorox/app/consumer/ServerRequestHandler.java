package dorox.app.consumer;

import com.rabbitmq.client.Channel;
import com.twitter.bijection.Injection;
import dorox.app.ReportMain;
import dorox.app.delay.ServerRequestReportDelay;
import dorox.app.mq.event.ServerRequestEvent;
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
public class ServerRequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(ServerRequestHandler.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue(value = "${rabbitmq.serverrequest.queue}",
                            durable="${rabbitmq.queue.durable}"),
                    exchange = @Exchange(value = "${rabbitmq.serverrequest.topic}",
                            durable="${rabbitmq.exchange.durable}",
                            type= "${rabbitmq.exchange.type}",
                            ignoreDeclarationExceptions = "${rabbitmq.exchange.ignoreDeclarationExceptions}"),
                    key = "${rabbitmq.serverrequest.key}"
            )
    })
    @RabbitHandler
    public void handler(@Payload byte[] record, Channel channel,
                        @Headers Map<String, Object> headers) throws Exception {

        Long deliveryTag = (Long)headers.get(AmqpHeaders.DELIVERY_TAG);
        try{
            Injection<GenericRecord, byte[]> recordInjection = AvroUtil.getGenericAvroCodecs(ServerRequestEvent.class);
            GenericRecord serverRequest = recordInjection.invert(record).get();

            logger.info("serverRequest:{}", serverRequest);
            ServerRequestEvent serverRequestEvent = AvroUtil.eventFromRecord(serverRequest, ServerRequestEvent.class);
            if(serverRequestEvent != null) {
                /*加入请求的延时队列*/
                ReportMain.DB_SERVER_REQUEST_DELAY_QUEUE.add(new ServerRequestReportDelay(serverRequestEvent, ReportMain.REPORT_SERVER_REQUEST_DALAY));

                /*统计每秒请求*/
                LongAdder adder = ReportMain.DownRequestCounter.get(serverRequestEvent.getDownName());
                if(adder == null){
                    adder = new LongAdder();
                    ReportMain.DownRequestCounter.put(serverRequestEvent.getDownName(),adder);
                }
                adder.add(serverRequestEvent.getMsgIds().size());
            }
        }catch (Exception e){
            logger.error("exception:{}", e);
        }finally {
            //手工ACK
            channel.basicAck(deliveryTag, false);
        }
    }


}
