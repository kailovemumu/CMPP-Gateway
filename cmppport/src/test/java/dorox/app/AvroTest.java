package dorox.app;


import com.twitter.bijection.Injection;
import com.twitter.bijection.avro.GenericAvroCodecs;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

public class AvroTest {

    public static String PortStatSchemaV1 = "{\"namespace\": \"org.dorox.avro\",\n" +
            " \"type\": \"record\",\n" +
            " \"name\": \"PortStat\",\n" +
            " \"fields\": [\n" +
            "     {\"name\": \"messageId\", \"type\": \"string\"},\n" +
            "     {\"name\": \"downName\",  \"type\": \"string\"},\n" +
            "     {\"name\": \"phone\", \"type\": \"string\"},\n" +
            "     {\"name\": \"msgId\", \"type\": \"string\"},\n" +
            "     {\"name\": \"seqId\", \"type\": \"string\"},\n" +
            "     {\"name\": \"stat\", \"type\": \"string\"},\n" +
            "     {\"name\": \"srcId\", \"type\": \"string\"},\n" +
            "     {\"name\": \"downRegionCode\", \"type\": \"string\"},\n" +
            "     {\"name\": \"makeTime\", \"type\": \"long\"},\n" +
            "     {\"name\": \"upName\",  \"type\": \"string\",  \"default\": \"\"}\n" +
            " ]\n" +
            "}";

    public static String PortStatSchemaV2 = "{\"namespace\": \"org.dorox.avro\",\n" +
            " \"type\": \"record\",\n" +
            " \"name\": \"PortStat\",\n" +
            " \"fields\": [\n" +
            "     {\"name\": \"messageId\", \"type\": \"string\"},\n" +
            "     {\"name\": \"downName\",  \"type\": \"string\"},\n" +
            "     {\"name\": \"phone\", \"type\": \"string\"},\n" +
            "     {\"name\": \"msgId\", \"type\": \"string\"},\n" +
            "     {\"name\": \"seqId\", \"type\": \"string\"},\n" +
            "     {\"name\": \"stat\", \"type\": \"string\"},\n" +
            "     {\"name\": \"srcId\", \"type\": \"string\"},\n" +
            "     {\"name\": \"downRegionCode\", \"type\": \"string\"},\n" +
            "     {\"name\": \"makeTime\", \"type\": \"long\"}\n" +
            " ]\n" +
            "}";



    public static void main(String[] args){

        Schema.Parser parserPortStatV1 = new Schema.Parser();
        Schema schemaPortStatV1 = parserPortStatV1.parse(PortStatSchemaV1);
        Injection<GenericRecord,byte[]>  portStatV1 = GenericAvroCodecs.toBinary(schemaPortStatV1);

        Schema.Parser parserPortStatV2 = new Schema.Parser();
        Schema schemaPortStatV2 = parserPortStatV2.parse(PortStatSchemaV1);
        Injection<GenericRecord,byte[]>  portStatV2 = GenericAvroCodecs.toBinary(schemaPortStatV2);


        GenericData.Record recordV1 = new GenericData.Record(schemaPortStatV1);
        recordV1.put("messageId","1");
        recordV1.put("downName","2");
        recordV1.put("upName","3");
        recordV1.put("phone","4");
        recordV1.put("msgId","5");
        recordV1.put("seqId","6");
        recordV1.put("stat","7");
        recordV1.put("srcId","8");
        recordV1.put("downRegionCode","9");
        recordV1.put("makeTime",10);
        byte[] record = portStatV1.apply(recordV1);

        GenericRecord recordV2 = portStatV2.invert(record).get();

        System.out.println(recordV2.get("upName"));
    }

}
