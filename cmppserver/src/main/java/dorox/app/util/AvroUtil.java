//package dorox.app.util;
//
//import com.twitter.bijection.Injection;
//import com.twitter.bijection.avro.GenericAvroCodecs;
//import dorox.app.mq.schema.AllSchema;
//import org.apache.avro.Schema;
//import org.apache.avro.generic.GenericRecord;
//
//public class AvroUtil extends AllSchema {
//    private static Injection<GenericRecord,byte[]> routeStat;
//    private static Injection<GenericRecord,byte[]> portStat;
//    private static Injection<GenericRecord,byte[]> portMo;
//
//    private static Injection<GenericRecord,byte[]> serverRequest;
//
//    static{
//        Schema.Parser parserRouteStat = new Schema.Parser();
//        Schema schemaRouteStat = parserRouteStat.parse(RouteStatSchema);
//        routeStat = GenericAvroCodecs.toBinary(schemaRouteStat);
//
//        Schema.Parser parserPortStat = new Schema.Parser();
//        Schema schemaPortStat = parserPortStat.parse(PortStatSchema);
//        portStat = GenericAvroCodecs.toBinary(schemaPortStat);
//
//        Schema.Parser parserPortMo = new Schema.Parser();
//        Schema schemaPortMo = parserPortMo.parse(PortMoSchema);
//        portMo = GenericAvroCodecs.toBinary(schemaPortMo);
//
//        Schema.Parser parser = new Schema.Parser();
//        Schema schema = parser.parse(ServerRequestSchema);
//        serverRequest = GenericAvroCodecs.toBinary(schema);
//    }
//
//    public static Injection<GenericRecord,byte[]> getGenericAvroCodecs4RouteStat() {
//        return routeStat;
//    }
//
//    public static Injection<GenericRecord,byte[]> getGenericAvroCodecs4PortStat() {
//        return portStat;
//    }
//
//    public static Injection<GenericRecord,byte[]> getGenericAvroCodecs4PortMo() {
//        return portMo;
//    }
//
//
//    public static Injection<GenericRecord,byte[]> getGenericAvroCodecs4ServerRequest() {
//        return serverRequest;
//    }
//}
