//import com.twitter.bijection.Injection;
//import dorox.app.mq.consumer.MultithreadedKafkaConsumer;
//import dorox.app.mq.event.RouteRequestEvent;
//import dorox.app.mq.producer.Producer;
//import dorox.app.util.AvroUtil;
//import dorox.app.util.KafkaUtil;
//import org.apache.avro.generic.GenericRecord;
//import org.apache.avro.util.Utf8;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ArrayBlockingQueue;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.LinkedBlockingQueue;
//
//public class KafkaTest {
//    public static BlockingQueue<RouteRequestEvent> blockingQueue = new LinkedBlockingQueue();
//    public static String kafkaServer = "192.168.100.64:44447";
//    public static void main(String[] args) {
//
//        Producer producer = new Producer(RouteRequestEvent.class, kafkaServer, blockingQueue);
//        producer.start();
//
//        List<Utf8> lists = new ArrayList<>();
//        lists.add(new Utf8("0322161420002793533202"));
//        lists.add(new Utf8("0322161420002793533202"));
//        lists.add(new Utf8("0322161420002793533202"));
//        new Thread(()->{
//
//            //:{"messageId": "C10322BHFD4D3YNC", "downName": "111111", "phone": "15409964291", "content": "[电费账单]尊敬的客户（客户号：1548915648），截至8月31日，您家电表知码2810，8月用电84度，电费46.58元，截至9月2号，可用余额18.56元。当月电费账单已生成，交电费就
//            //用网上国网app，新用户注册即送2元电费卷，点dwz.cn/zpdkfjie领取，还有更多精彩活动等您参与！", "srcId": "", "upSrcId": "", "msgIds": ["0322161420002793533202", "0322161420002793533203", "0322161420002793533204"], "downRegionCode": "cmppserver1", "upName": "123456", "upRegionCode": "cmppclient1", "makeTime": 1616400861711}
//            blockingQueue.add(new RouteRequestEvent("C10322BHFD4D3YNC","111111", "123456","15409964291",
//                    "[电费账单]尊敬的客户（客户号：1548915648），截至8月31日，您家电表知码2810，8月用电84度，电费46.58元，截至9月2号，可用余额18.56元。当月电费账单已生成，交电费就 用网上国网app，新用户注册即送2元电费卷，点dwz.cn/zpdkfjie领取，还有更多精彩活动等您参与！",
//                    "","",lists,"cmppserver1","cmppclient1"));
//        }).start();
//
//        new MultithreadedKafkaConsumer("route", kafkaServer, "123456");
//
//        KafkaUtil.TOPIC_CONSUMER_HANDLER_MAP.put("cmppclient1", (record)->{
//
//            try{
//                Injection<GenericRecord, byte[]> recordInjection = AvroUtil.getGenericAvroCodecs(RouteRequestEvent.class);
//                GenericRecord routeRequest = recordInjection.invert(record.value()).get();
//                System.out.println("routeRequest:" + routeRequest);
//
//            }catch (Exception e){
//
//            }
//        });
//
//    }
//}
