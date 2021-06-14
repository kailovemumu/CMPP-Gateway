package dorox.app.consumer;

import com.rabbitmq.client.Channel;
import com.twitter.bijection.Injection;
import dorox.app.http.HttpDownPort;
import dorox.app.mq.event.PortMoEvent;
import dorox.app.util.AvroUtil;
import dorox.app.util.Statics;
import org.apache.avro.generic.GenericRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PortMoHandler {
    private static final Logger logger = LoggerFactory.getLogger(PortStatHandler.class);

    @Autowired
    private RedisTemplate redisTemplate;

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
            Injection<GenericRecord, byte[]> recordInjection = AvroUtil.getGenericAvroCodecs(PortMoEvent.class);
            GenericRecord portMo = recordInjection.invert(record).get();
            logger.info("portMo:{}", portMo);
            sendHttpMo(portMo);
        }catch (Exception e){
            logger.error("exception:{}", e);
        }finally {
            //手工ACK
            channel.basicAck(deliveryTag, false);
        }
    }

    private void sendHttpMo(GenericRecord portMo) {
        String srcId = String.valueOf(portMo.get("destId"));
        String phone = String.valueOf(portMo.get("phone"));
        String content = String.valueOf(portMo.get("content"));
        String downName = String.valueOf(portMo.get("downName"));

        HttpDownPort httpDownPort = Statics.DOWN_PORT_MAP.get(downName);
        if(httpDownPort.isPushResult()){

        }else{
            Map<String, Object> tmp = new HashMap<String, Object>();
            tmp.put("phone", phone);
            tmp.put("content", content);
            tmp.put("srcId", srcId);
            redisTemplate.opsForSet().add(downName + "_MO", tmp);
        }
    }
}
