package dorox.app.consumer;

import com.rabbitmq.client.Channel;
import com.twitter.bijection.Injection;
import dorox.app.http.HttpDownPort;
import dorox.app.mq.event.RouteStatEvent;
import dorox.app.util.AvroUtil;
import dorox.app.util.CacheConfig;
import dorox.app.util.Statics;
import dorox.app.vo.SubmitVo;
import org.apache.avro.generic.GenericRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RouteStatHandler {

    private static final Logger logger = LoggerFactory.getLogger(RouteStatHandler.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

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
            sendHttpDeliver(routeStat);
        }catch (Exception e){
            logger.error("exception:{}", e);
        }finally {
            //手工ACK
            channel.basicAck(deliveryTag, false);
        }
    }

    private void sendHttpDeliver(GenericRecord genericRecord) {

        String phone = String.valueOf(genericRecord.get("phone"));
        String srcId = String.valueOf(genericRecord.get("srcId"));
        String stat = String.valueOf(genericRecord.get("stat"));
        String msgId = String.valueOf(genericRecord.get("msgId"));
        String downName = String.valueOf(genericRecord.get("downName"));
        String messageId = String.valueOf(genericRecord.get("messageId"));
        HttpDownPort httpDownPort = Statics.DOWN_PORT_MAP.get(downName);

        //发送失败
        if(!"DELIVRD".equals(stat)){
            CacheConfig.submitVoCache.invalidate(messageId);
            //推送
            if(httpDownPort.isPushResult()){

            }else{
                Map<String, Object> tmp = new HashMap<String, Object>();
                tmp.put("taskId", messageId);
                tmp.put("phone", phone);
                tmp.put("result", stat);
                redisTemplate.opsForSet().add(downName + "_RESULT", tmp);
            }
        }else{
            SubmitVo submitVo = CacheConfig.submitVoCache.getIfPresent(messageId);
            //收到所有回执
            if(submitVo.getCount().decrementAndGet() == 0){
                CacheConfig.submitVoCache.invalidate(messageId);
                //推送
                if(httpDownPort.isPushResult()){

                }else{
                    Map<String, Object> tmp = new HashMap<String, Object>();
                    tmp.put("taskId", messageId);
                    tmp.put("phone", phone);
                    tmp.put("result", stat);
                    redisTemplate.opsForSet().add(downName + "_RESULT", tmp);
                }
            }
        }
    }
}
