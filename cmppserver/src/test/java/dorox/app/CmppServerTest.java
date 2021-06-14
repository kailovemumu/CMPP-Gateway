//package dorox.app;
//
//import com.google.common.util.concurrent.ThreadFactoryBuilder;
//import com.twitter.bijection.Injection;
//import com.twitter.bijection.avro.GenericAvroCodecs;
//import com.zx.sms.connect.manager.EndpointManager;
//import com.zx.sms.connect.manager.cmpp.CMPPClientEndpointEntity;
//import com.zx.sms.handler.api.BusinessHandlerInterface;
//import dorox.app.util.AllSchema;
//import dorox.app.util.KafkaUtil;
//import org.apache.avro.Schema;
//import org.apache.avro.generic.GenericRecord;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.apache.kafka.clients.consumer.ConsumerRecords;
//import org.apache.kafka.clients.consumer.KafkaConsumer;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.nio.charset.Charset;
//import java.time.Duration;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Properties;
//import java.util.concurrent.*;
//
//
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = {CmppServerApplication.class})
//public class CmppServerTest   {
//
//
//    @Value("${region.code}")
//    private String regionCode;
//
//    private CountDownLatch countDownLatch = new CountDownLatch(2) ;
//    @Before
//    public void setUp() throws Exception{
//        consumerKafka();
//
//        int channel = 1;
//        int limit = 50;
//        int times = 600;
//        String cmppAddr = "localhost";
//        String userName = "222222";
//        String password = "123456";
//        int cmppPort = 7890;
//        short version = 0x20;
//
//        final EndpointManager manager = EndpointManager.INS;
//
//        CMPPClientEndpointEntity client = new CMPPClientEndpointEntity();
//        client.setId("test123");
//        client.setHost(cmppAddr); //47.108.76.18 120.25.196.91
//        client.setPort(cmppPort);//44454 44445
//        client.setChartset(Charset.forName("utf-8"));
//        client.setGroupName("test");
//        client.setUserName(userName);//782365 547633
//        client.setPassword(password);//3uf6Fd Fg46fr
//
//        //client.setSpCode("1069039128");
//        client.setIdleTimeSec((short)30);
//        client.setMaxChannels((short)channel);
//        client.setVersion(version);
////		client.setRetryWaitTimeSec((short)30);
//        client.setUseSSL(false);
//        client.setReSendFailMsg(false);
////		client.setWriteLimit(500);
//        List<BusinessHandlerInterface> clienthandlers = new ArrayList<BusinessHandlerInterface>();
//        clienthandlers.add(new SendHandler(limit,times, countDownLatch));
//        client.setBusinessHandlerSet(clienthandlers);
//        manager.addEndpointEntity(client);
//
//        Thread.sleep(500);
//        for(int i = 0; i < client.getMaxChannels(); i++)
//            manager.openEndpoint(client);
//
//        manager.startConnectionCheckTask();
//
//        System.out.println("start.....");
//    }
//
//    private void consumerKafka() {
//        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
//                .setNameFormat("demo-pool-%d").build();
//        ExecutorService singleThreadPool = new ThreadPoolExecutor(1, 1,
//                0L, TimeUnit.MILLISECONDS,
//                new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
//
//        singleThreadPool.execute(()->{
//            Properties consumerProps = new Properties();
//            consumerProps.put("bootstrap.servers", "120.25.196.91:44447");
//
//            consumerProps.put("key.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
//            consumerProps.put("value.deserializer","org.apache.kafka.common.serialization.ByteArrayDeserializer");
//            consumerProps.put("group.id", "group");
//            KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<String, byte[]>(consumerProps);
//            consumer.subscribe(Collections.singleton(KafkaUtil.getServerRequestTopic()));
//
//            Schema.Parser parser = new Schema.Parser();
//            Schema schema = parser.parse(AllSchema.ServerRequestSchema);
//
//            Injection<GenericRecord, byte[]> recordInjection = GenericAvroCodecs.toBinary(schema);
//
//            while(true){
//                ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(10000));
//                for(ConsumerRecord<String, byte[]> record : records){
//                    GenericRecord request = recordInjection.invert(record.value()).get();
//                    System.out.println("*****************************************************");
//                    System.out.println(request.get("phone"));
//                    System.out.println(request.get("content"));
//                    System.out.println(request.get("srcId"));
//                    System.out.println(request.get("messageId"));
//                    System.out.println(request.get("downName"));
//                    System.out.println(request.get("msgIds"));
//                    System.out.println(request.get("downRegionCode"));
//                    //return;
//                }
//            }
//        });
//        singleThreadPool.shutdown();
//    }
//
//    @Test
//    public void test() throws Exception {
//
//
//    }
//
//    @After
//    public void after() throws Exception {
//
//        countDownLatch.await();
//
//        System.out.println(SendHandler.amap);
//        System.out.println(SendHandler.amap.size());
//        System.out.println(SendHandler.cnt.get());
//
//        EndpointManager.INS.close();
//    }
//}
