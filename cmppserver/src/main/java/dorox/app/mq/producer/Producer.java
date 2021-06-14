//package dorox.app.mq.producer;
//
//import com.google.common.collect.Queues;
//import com.twitter.bijection.Injection;
//import com.twitter.bijection.avro.GenericAvroCodecs;
//import dorox.app.mq.event.ServerRequestEvent;
//
//import dorox.app.util.AllSchema;
//import dorox.app.util.AvroUtil;
//import dorox.app.util.Statics;
//import org.apache.avro.Schema;
//import org.apache.avro.generic.GenericData;
//import org.apache.avro.generic.GenericRecord;
//import org.apache.kafka.clients.producer.*;
//import org.apache.kafka.common.serialization.ByteArraySerializer;
//import org.apache.kafka.common.serialization.StringSerializer;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Properties;
//import java.util.concurrent.TimeUnit;
//
//public class Producer extends Thread {
//
//    private static final Logger logger = LoggerFactory.getLogger(Producer.class);
//
//    private final KafkaProducer<String, byte[]> producer;
//    private final String topic;
//
//    public Producer(final String topic, final String kafkaServer) {
//        Properties props = new Properties();
//        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
//        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
//        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
//
//        producer = new KafkaProducer<>(props);
//        this.topic = topic;
//
//    }
//
//    @Override
//    public void run() {
//
//        Schema.Parser parser = new Schema.Parser();
//        Schema schema = parser.parse(AllSchema.ServerRequestSchema);
//
//        Injection<GenericRecord, byte[]> recordInjection = AvroUtil.getGenericAvroCodecs4ServerRequest();
//
//        ServerRequestEvent serverRequestEvent = null;
//        try{
//
//            while (true) {
//                if((serverRequestEvent =Statics.SERVER_REQUEST_QUEUE.poll(100, TimeUnit.MILLISECONDS)) != null){
//
//                    long startTime = System.currentTimeMillis();
//                    GenericData.Record record = new GenericData.Record(schema);
//
//                    record.put("messageId", serverRequestEvent.getMessageId());
//                    record.put("downName", serverRequestEvent.getDownName());
//                    record.put("phone", serverRequestEvent.getPhone());
//                    record.put("content", serverRequestEvent.getContent());
//                    record.put("srcId", serverRequestEvent.getSrcId());
//                    record.put("srcId", serverRequestEvent.getSrcId());
//                    record.put("msgIds", serverRequestEvent.getMsgIds());
//                    record.put("makeTime", serverRequestEvent.getMakeTime());
//
//                    byte[] bytes = recordInjection.apply(record);
//
//                    producer.send(new ProducerRecord<String, byte[]>(topic, bytes), new DemoCallBack(startTime, serverRequestEvent));
//
//                }
//            }
//
//        }catch (InterruptedException e){
//            logger.info("e:{}", e);
//        }
//
//    }
//}
//
//class DemoCallBack implements Callback {
//
//    private final long startTime;
//    private final ServerRequestEvent serverRequestEvent;
//
//    public DemoCallBack(long startTime, ServerRequestEvent serverRequestEvent) {
//        this.startTime = startTime;
//        this.serverRequestEvent = serverRequestEvent;
//    }
//
//    /**
//     * A callback method the user can implement to provide asynchronous handling of request completion. This method will
//     * be called when the record sent to the server has been acknowledged. When exception is not null in the callback,
//     * metadata will contain the special -1 value for all fields except for topicPartition, which will be valid.
//     *
//     * @param metadata  The metadata for the record that was sent (i.e. the partition and offset). An empty metadata
//     *                  with -1 value for all fields except for topicPartition will be returned if an error occurred.
//     * @param exception The exception thrown during processing of this record. Null if no error occurred.
//     */
//    @Override
//    public void onCompletion(RecordMetadata metadata, Exception exception) {
//        long elapsedTime = System.currentTimeMillis() - startTime;
//        if (metadata != null) {
//            System.out.println(
//                    "message(" + serverRequestEvent + ") sent to partition(" + metadata.partition() +
//                            "), " +
//                            "offset(" + metadata.offset() + ") in " + elapsedTime + " ms");
//        } else {
//            exception.printStackTrace();
//        }
//    }
//}
