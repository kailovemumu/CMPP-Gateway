package dorox.app.consumer;

import com.rabbitmq.client.Channel;
import com.twitter.bijection.Injection;
import dorox.app.ReportMain;
import dorox.app.mq.event.PortStatEvent;
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
public class PortStatHandler {
    private static final Logger logger = LoggerFactory.getLogger(PortStatHandler.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue(value = "${rabbitmq.portstat.queue}",
                            durable="${rabbitmq.queue.durable}"),
                    exchange = @Exchange(value = "${rabbitmq.portstat.topic}",
                            durable="${rabbitmq.exchange.durable}",
                            type= "${rabbitmq.exchange.type}",
                            ignoreDeclarationExceptions = "${rabbitmq.exchange.ignoreDeclarationExceptions}"),
                    key = "${rabbitmq.portstat.key}"
            )
    })
    @RabbitHandler
    public void handler(@Payload byte[] record, Channel channel,
                        @Headers Map<String, Object> headers) throws Exception {

        Long deliveryTag = (Long)headers.get(AmqpHeaders.DELIVERY_TAG);
        try{
            Injection<GenericRecord, byte[]> recordInjection = AvroUtil.getGenericAvroCodecs(PortStatEvent.class);
            GenericRecord portStat = recordInjection.invert(record).get();

            logger.info("portStat:{}", portStat);
            PortStatEvent portStatEvent = AvroUtil.eventFromRecord(portStat, PortStatEvent.class);
            if (portStatEvent != null) {
                ReportMain.REPORT_AGGREGATION_QUEUE.add(portStatEvent);

                /*统计每秒请求*/
                LongAdder downAdder = ReportMain.DownStatCounter.get(portStatEvent.getDownName());
                if(downAdder == null){
                    downAdder = new LongAdder();
                    ReportMain.DownStatCounter.put(portStatEvent.getDownName(),downAdder);
                }
                downAdder.increment();

                /*统计每秒请求*/
                LongAdder upAdder = ReportMain.UpStatCounter.get(portStatEvent.getUpName());
                if(upAdder == null){
                    upAdder = new LongAdder();
                    ReportMain.UpStatCounter.put(portStatEvent.getUpName(),upAdder);
                }
                upAdder.increment();

                if("DELIVRD".equals(portStatEvent.getStat())){

                    LongAdder downSuccessAdder = ReportMain.DownSuccessCounter.get(portStatEvent.getDownName());
                    if(downSuccessAdder == null){
                        downSuccessAdder = new LongAdder();
                        ReportMain.DownSuccessCounter.put(portStatEvent.getDownName(), downSuccessAdder);
                    }
                    downSuccessAdder.increment();

                    LongAdder upSuccessAdder = ReportMain.UpSuccessCounter.get(portStatEvent.getUpName());
                    if(upSuccessAdder == null){
                        upSuccessAdder = new LongAdder();
                        ReportMain.UpSuccessCounter.put(portStatEvent.getUpName(), upSuccessAdder);
                    }
                    upSuccessAdder.increment();
                }
            }
        }catch (Exception e){
            logger.error("exception:{}", e);
        }finally {
            //手工ACK
            channel.basicAck(deliveryTag, false);
        }
    }


}
