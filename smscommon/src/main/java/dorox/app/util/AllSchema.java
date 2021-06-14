package dorox.app.util;

public class AllSchema {

    public static String ServerRequestSchema = "{\"namespace\": \"org.dorox.avro\",\n" +
            " \"type\": \"record\",\n" +
            " \"name\": \"ServerRequest\",\n" +
            " \"fields\": [\n" +
            "     {\"name\": \"messageId\", \"type\": \"string\"},\n" +
            "     {\"name\": \"downName\",  \"type\": \"string\"},\n" +
            "     {\"name\": \"phone\", \"type\": \"string\"},\n" +
            "     {\"name\": \"content\", \"type\": \"string\"},\n" +
            "     {\"name\": \"srcId\", \"type\": \"string\"},\n" +
            "     {\"name\": \"msgIds\", \"type\": {\"type\": \"array\", \"items\": \"string\"}},\n" +
            "     {\"name\": \"downRegionCode\", \"type\": \"string\"},\n" +
            "     {\"name\": \"makeTime\", \"type\": \"long\"}\n" +
            " ]\n" +
            "}";

    public static String RouteStatSchema = "{\"namespace\": \"org.dorox.avro\",\n" +
            " \"type\": \"record\",\n" +
            " \"name\": \"RouteStat\",\n" +
            " \"fields\": [\n" +
            "     {\"name\": \"messageId\", \"type\": \"string\"},\n" +
            "     {\"name\": \"downName\",  \"type\": \"string\"},\n" +
            "     {\"name\": \"upName\",  \"type\": \"string\"},\n" +
            "     {\"name\": \"phone\", \"type\": \"string\"},\n" +
            "     {\"name\": \"msgId\", \"type\": \"string\"},\n" +
            "     {\"name\": \"stat\", \"type\": \"string\"},\n" +
            "     {\"name\": \"srcId\", \"type\": \"string\"},\n" +
            "     {\"name\": \"downRegionCode\", \"type\": \"string\"},\n" +
            "     {\"name\": \"makeTime\", \"type\": \"long\"}\n" +
            " ]\n" +
            "}";

    public static String ServerRequestFixUpNameSchema =  "{\"namespace\": \"org.dorox.avro\",\n" +
            " \"type\": \"record\",\n" +
            " \"name\": \"ServerRequestFixUpName\",\n" +
            " \"fields\": [\n" +
            "     {\"name\": \"messageId\", \"type\": \"string\"},\n" +
            "     {\"name\": \"downName\",  \"type\": \"string\"},\n" +
            "     {\"name\": \"phone\", \"type\": \"string\"},\n" +
            "     {\"name\": \"content\", \"type\": \"string\"},\n" +
            "     {\"name\": \"srcId\", \"type\": \"string\"},\n" +
            "     {\"name\": \"upSrcId\", \"type\": \"string\"},\n" +
            "     {\"name\": \"msgIds\", \"type\": {\"type\": \"array\", \"items\": \"string\"}},\n" +
            "     {\"name\": \"downRegionCode\", \"type\": \"string\"},\n" +
            "     {\"name\": \"upName\",  \"type\": \"string\"},\n" +
            "     {\"name\": \"upRegionCode\", \"type\": \"string\"},\n" +
            "     {\"name\": \"makeTime\", \"type\": \"long\"}\n" +
            " ]\n" +
            "}";
    public static String RouteRequestSchema = "{\"namespace\": \"org.dorox.avro\",\n" +
            " \"type\": \"record\",\n" +
            " \"name\": \"RouteRequest\",\n" +
            " \"fields\": [\n" +
            "     {\"name\": \"messageId\", \"type\": \"string\"},\n" +
            "     {\"name\": \"downName\",  \"type\": \"string\"},\n" +
            "     {\"name\": \"phone\", \"type\": \"string\"},\n" +
            "     {\"name\": \"content\", \"type\": \"string\"},\n" +
            "     {\"name\": \"srcId\", \"type\": \"string\"},\n" +
            "     {\"name\": \"upSrcId\", \"type\": \"string\"},\n" +
            "     {\"name\": \"msgIds\", \"type\": {\"type\": \"array\", \"items\": \"string\"}},\n" +
            "     {\"name\": \"downRegionCode\", \"type\": \"string\"},\n" +
            "     {\"name\": \"upName\",  \"type\": \"string\"},\n" +
            "     {\"name\": \"upRegionCode\", \"type\": \"string\"},\n" +
            "     {\"name\": \"makeTime\", \"type\": \"long\"}\n" +
            " ]\n" +
            "}";

    public static String PortStatSchema = "{\"namespace\": \"org.dorox.avro\",\n" +
            " \"type\": \"record\",\n" +
            " \"name\": \"PortStat\",\n" +
            " \"fields\": [\n" +
            "     {\"name\": \"messageId\", \"type\": \"string\"},\n" +
            "     {\"name\": \"downName\",  \"type\": \"string\"},\n" +
            "     {\"name\": \"upName\",  \"type\": \"string\"},\n" +
            "     {\"name\": \"phone\", \"type\": \"string\"},\n" +
            "     {\"name\": \"msgId\", \"type\": \"string\"},\n" +
            "     {\"name\": \"seqId\", \"type\": \"string\"},\n" +
            "     {\"name\": \"stat\", \"type\": \"string\"},\n" +
            "     {\"name\": \"srcId\", \"type\": \"string\"},\n" +
            "     {\"name\": \"downRegionCode\", \"type\": \"string\"},\n" +
            "     {\"name\": \"makeTime\", \"type\": \"long\"}\n" +
            " ]\n" +
            "}";

    public static String PortMoSchema = "{\"namespace\": \"org.dorox.avro\",\n" +
            " \"type\": \"record\",\n" +
            " \"name\": \"PortMo\",\n" +
            " \"fields\": [\n" +
            "     {\"name\": \"downName\",  \"type\": \"string\"},\n" +
            "     {\"name\": \"phone\", \"type\": \"string\"},\n" +
            "     {\"name\": \"content\", \"type\": \"string\"},\n" +
            "     {\"name\": \"destId\", \"type\": \"string\"},\n" +
            "     {\"name\": \"downRegionCode\", \"type\": \"string\"},\n" +
            "     {\"name\": \"makeTime\", \"type\": \"long\"}\n" +
            " ]\n" +
            "}";

    public static String RouteReportSchema = "{\"namespace\": \"org.dorox.avro\",\n" +
            " \"type\": \"record\",\n" +
            " \"name\": \"RouteReport\",\n" +
            " \"fields\": [\n" +
            "     {\"name\": \"messageId\", \"type\": \"string\"},\n" +
            "     {\"name\": \"upName\",  \"type\": \"string\"},\n" +
            "     {\"name\": \"carrier\", \"type\": \"int\"},\n" +
            "     {\"name\": \"cityCode\", \"type\": \"string\"},\n" +
            "     {\"name\": \"city\", \"type\": \"string\"},\n" +
            "     {\"name\": \"provinceCode\", \"type\": \"string\"},\n" +
            "     {\"name\": \"province\", \"type\": \"string\"},\n" +
            "     {\"name\": \"msgCount\", \"type\": \"int\"},\n" +
            "     {\"name\": \"upRegionCode\", \"type\": \"string\"},\n" +
            "     {\"name\": \"upSrcId\", \"type\": \"string\"},\n" +
            "     {\"name\": \"makeTime\", \"type\": \"long\"}\n" +
            " ]\n" +
            "}";

    public static String StatArrivedSchema = "{\"namespace\": \"org.dorox.avro\",\n" +
            " \"type\": \"record\",\n" +
            " \"name\": \"StatArrived\",\n" +
            " \"fields\": [\n" +
            "     {\"name\": \"messageId\", \"type\": \"string\"},\n" +
            "     {\"name\": \"msgId\", \"type\": \"string\"},\n" +
            "     {\"name\": \"stat\", \"type\": \"string\"},\n" +
            "     {\"name\": \"makeTime\", \"type\": \"long\"}\n" +
            " ]\n" +
            "}";

}
