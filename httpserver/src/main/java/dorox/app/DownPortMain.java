package dorox.app;

import com.twitter.bijection.Injection;
import dorox.app.http.HttpDownPort;
import dorox.app.manager.ManageHttpDownPort;
import dorox.app.mq.event.PortMoEvent;
import dorox.app.mq.event.PortStatEvent;
import dorox.app.mq.event.RouteStatEvent;
import dorox.app.mq.event.ServerRequestEvent;
import dorox.app.util.AvroUtil;
import dorox.app.util.CacheConfig;
import dorox.app.util.Statics;
import dorox.app.vo.SubmitVo;
import org.apache.avro.generic.GenericRecord;
import org.n3r.idworker.Sid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *低负载时消息编码解码可控制在10ms以内。
 ***********************************************************************
 *下游端口流程
 *1，获取配置文件信息，包括下游账号，路由，连接数，流数
 *2，配置下游服务端口，打开链接，绑定handler，等待下游建立连接
 *3，下游请求信息，解析手机和内容，放入公共的路由map中，供对应的上游端口发送
 * @author apple
 */
//@Configuration
@Component
@Order(value=1)
public class DownPortMain implements CommandLineRunner{
	private static final Logger logger = LoggerFactory.getLogger(DownPortMain.class);

	@Value("${messageid.prefix}")
	private String messageIdPrefix;
	@Autowired
	private JdbcTemplate jdbcTemplate;
    @Autowired
    private RedisTemplate redisTemplate;
	@Autowired
	private RabbitTemplate rabbitTemplate;
	@Autowired
	private Sid sid;

	/**客户分区*/
	@Value("${region.code}")
	private String regionCode;
    //线程池技术。
	public static ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

	@Override
	public void run(String... args) throws Exception {

		//初始化下游端口
        /**
         * command：执行线程
         * initialDelay：初始化延时
         * period：两次开始执行最小间隔时间
         * unit：计时单位
         * 每隔一分钟重新执行一次任务。
         */
		service.scheduleAtFixedRate(new ManageHttpDownPort(regionCode, messageIdPrefix, sid, jdbcTemplate),
				0, 1 , TimeUnit.MINUTES);


//		//kafka生产者
//		for(int i = 0; i < kafkaProducerNum; i++){
//			new Producer(ServerRequestEvent.class, Statics.SERVER_REQUEST_QUEUE,rabbitTemplate).start();
//		}

		//kafka消费者
//		for(int i = 0; i < kafkaConsumerNum; i++){
//			new MultithreadedKafkaConsumer(
//					KafkaUtil.getRouteStatTopic(regionCode),kafkaServer,"routestat");
//			new MultithreadedKafkaConsumer(
//					KafkaUtil.getPortStatTopic(regionCode),kafkaServer,"portstat");
//			new MultithreadedKafkaConsumer(
//					KafkaUtil.getPortMoTopic(regionCode),kafkaServer,"portmo");
//		}
	}

//	private void initTopicConsumerHandlerMap() {
//
//		//消费route模块发送的routestat消息，返回给客户回执，不需要缓存了
//		KafkaUtil.TOPIC_CONSUMER_HANDLER_MAP.put(KafkaUtil.getRouteStatTopic(regionCode),(record)->{
//			try{
//				Injection<GenericRecord, byte[]> recordInjection = AvroUtil.getGenericAvroCodecs(RouteStatEvent.class);
//				GenericRecord routeStat = recordInjection.invert(record.value()).get();
//				logger.info("routeStat:{}", routeStat);
//				sendHttpDeliver(routeStat);
//			}catch (Exception e){
//				logger.error("exception:{}", e);
//			}
//		});
//
//		//消费port模块发送的portstat消息
//		KafkaUtil.TOPIC_CONSUMER_HANDLER_MAP.put(KafkaUtil.getPortStatTopic(regionCode),(record)->{
//
//			try{
//				Injection<GenericRecord, byte[]> recordInjection = AvroUtil.getGenericAvroCodecs(PortStatEvent.class);
//				GenericRecord portStat = recordInjection.invert(record.value()).get();
//				logger.info("portStat:{}", portStat);
//				sendHttpDeliver(portStat);
//			}catch (Exception e){
//				logger.error("exception:{}", e);
//			}
//		});
//		//消费port模块发送的portmo消息
//		KafkaUtil.TOPIC_CONSUMER_HANDLER_MAP.put(KafkaUtil.getPortMoTopic(regionCode),(record)->{
//
//			try{
//				Injection<GenericRecord, byte[]> recordInjection = AvroUtil.getGenericAvroCodecs(PortMoEvent.class);
//				GenericRecord portMo = recordInjection.invert(record.value()).get();
//				logger.info("portMo:{}", portMo);
//				sendHttpMo(portMo);
//			}catch (Exception e){
//				logger.error("exception:{}", e);
//			}
//		});
//	}

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
