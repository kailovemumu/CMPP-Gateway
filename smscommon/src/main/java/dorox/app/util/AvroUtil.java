package dorox.app.util;

import com.twitter.bijection.Injection;
import com.twitter.bijection.avro.GenericAvroCodecs;

import dorox.app.mq.event.*;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AvroUtil extends AllSchema {
    private static Injection<GenericRecord,byte[]> serverRequest;
    private static Injection<GenericRecord,byte[]> serverRequestFixUpName;
    private static Injection<GenericRecord,byte[]> routeRequest;
    private static Injection<GenericRecord,byte[]> routeStat;
    private static Injection<GenericRecord,byte[]> portStat;
    private static Injection<GenericRecord,byte[]> portMo;
    private static Injection<GenericRecord,byte[]> routeReport;
    private static Injection<GenericRecord,byte[]> statArrived;

    private static Schema schemaRouteStat;
    private static Schema schemaPortStat;
    private static Schema schemaPortMo;
    private static Schema schemaServerRequest;
    private static Schema schemaServerRequestFixUpName;
    private static Schema schemaRouteReport;
    private static Schema schemaStatArrived;
    private static Schema schemaRouteRequest;

    private static Map<Class, String> EVENT_SCHEMA = new HashMap<>();
    private static Map<Class, Schema> EVENT_SCHEMA_PARSER = new HashMap<>();
    private static Map<Class, Injection<GenericRecord,byte[]>> EVENT_INJECTION = new HashMap<>();

    static{

        /*
         * 初始化<eventTypeName, injection>
         */
        Schema.Parser parserRouteStat = new Schema.Parser();
        schemaRouteStat = parserRouteStat.parse(RouteStatSchema);
        routeStat = GenericAvroCodecs.toBinary(schemaRouteStat);

        Schema.Parser parserRouteRequest = new Schema.Parser();
        schemaRouteRequest = parserRouteRequest.parse(RouteRequestSchema);
        routeRequest = GenericAvroCodecs.toBinary(schemaRouteRequest);

        Schema.Parser parserPortStat = new Schema.Parser();
        schemaPortStat = parserPortStat.parse(PortStatSchema);
        portStat = GenericAvroCodecs.toBinary(schemaPortStat);

        Schema.Parser parserPortMo = new Schema.Parser();
        schemaPortMo = parserPortMo.parse(PortMoSchema);
        portMo = GenericAvroCodecs.toBinary(schemaPortMo);

        Schema.Parser parserServerRequest = new Schema.Parser();
        schemaServerRequest = parserServerRequest.parse(ServerRequestSchema);
        serverRequest = GenericAvroCodecs.toBinary(schemaServerRequest);

        Schema.Parser parserServerRequestFixUpName = new Schema.Parser();
        schemaServerRequestFixUpName = parserServerRequestFixUpName.parse(ServerRequestFixUpNameSchema);
        serverRequestFixUpName = GenericAvroCodecs.toBinary(schemaServerRequestFixUpName);

        Schema.Parser parserRouteReport = new Schema.Parser();
        schemaRouteReport = parserRouteReport.parse(RouteReportSchema);
        routeReport = GenericAvroCodecs.toBinary(schemaRouteReport);

        Schema.Parser parserStatArrived = new Schema.Parser();
        schemaStatArrived = parserStatArrived.parse(StatArrivedSchema);
        statArrived = GenericAvroCodecs.toBinary(schemaStatArrived);

        EVENT_INJECTION.put(ServerRequestEvent.class, serverRequest);
        EVENT_INJECTION.put(ServerRequestFixUpNameEvent.class, serverRequestFixUpName);
        EVENT_INJECTION.put(RouteStatEvent.class, routeStat);
        EVENT_INJECTION.put(RouteRequestEvent.class, routeRequest);
        EVENT_INJECTION.put(PortStatEvent.class, portStat);
        EVENT_INJECTION.put(PortMoEvent.class, portMo);
        EVENT_INJECTION.put(RouteReportEvent.class, routeReport);
        EVENT_INJECTION.put(StatArrivedEvent.class, statArrived);

        /*
         * 初始化<eventTypeName, schema>
         */
        EVENT_SCHEMA.put(ServerRequestEvent.class, ServerRequestSchema);
        EVENT_SCHEMA.put(ServerRequestFixUpNameEvent.class, ServerRequestFixUpNameSchema);
        EVENT_SCHEMA.put(RouteStatEvent.class, RouteStatSchema);
        EVENT_SCHEMA.put(RouteRequestEvent.class, RouteRequestSchema);
        EVENT_SCHEMA.put(PortStatEvent.class, PortStatSchema);
        EVENT_SCHEMA.put(PortMoEvent.class, PortMoSchema);
        EVENT_SCHEMA.put(RouteReportEvent.class, RouteReportSchema);
        EVENT_SCHEMA.put(StatArrivedEvent.class, StatArrivedSchema);

        EVENT_SCHEMA_PARSER.put(ServerRequestEvent.class, schemaServerRequest);
        EVENT_SCHEMA_PARSER.put(ServerRequestFixUpNameEvent.class, schemaServerRequestFixUpName);
        EVENT_SCHEMA_PARSER.put(RouteStatEvent.class, schemaRouteStat);
        EVENT_SCHEMA_PARSER.put(RouteRequestEvent.class, schemaRouteRequest);
        EVENT_SCHEMA_PARSER.put(PortStatEvent.class, schemaPortStat);
        EVENT_SCHEMA_PARSER.put(PortMoEvent.class, schemaPortMo);
        EVENT_SCHEMA_PARSER.put(RouteReportEvent.class, schemaRouteReport);
        EVENT_SCHEMA_PARSER.put(StatArrivedEvent.class, schemaStatArrived);
    }

    public static Injection<GenericRecord,byte[]> getGenericAvroCodecs(Class eventClass) {
        return EVENT_INJECTION.get(eventClass);
    }

    /**
     *  将所有事件转为avro格式的通用方法
     */
    public static GenericData.Record recordFromEvent(Object event, Schema schema) {
        GenericData.Record record = new GenericData.Record(schema);
        if (event instanceof ServerRequestEvent){
            ServerRequestEvent serverRequestEvent = (ServerRequestEvent)event;
            record.put("messageId", serverRequestEvent.getMessageId());
            record.put("downName", serverRequestEvent.getDownName());
            record.put("phone", serverRequestEvent.getPhone());
            record.put("content", serverRequestEvent.getContent());
            record.put("srcId", serverRequestEvent.getSrcId());
            record.put("msgIds", serverRequestEvent.getMsgIds());
            record.put("downRegionCode", serverRequestEvent.getDownRegionCode());
            record.put("makeTime", serverRequestEvent.getMakeTime());
        } else if(event instanceof RouteStatEvent){
            RouteStatEvent routeStatEvent = (RouteStatEvent)event;
            record.put("messageId", routeStatEvent.getMessageId());
            record.put("downName", routeStatEvent.getDownName());
            record.put("upName", routeStatEvent.getUpName());
            record.put("phone", routeStatEvent.getPhone());
            record.put("msgId", routeStatEvent.getMsgId());
            record.put("stat", routeStatEvent.getStat());
            record.put("srcId", routeStatEvent.getMsgId());
            record.put("downRegionCode", routeStatEvent.getDownRegionCode());
            record.put("makeTime", routeStatEvent.getMakeTime());
        } else if(event instanceof RouteRequestEvent){
            RouteRequestEvent routeRequestEvent = (RouteRequestEvent) event;
            record.put("messageId", routeRequestEvent.getMessageId());
            record.put("downName", routeRequestEvent.getDownName());
            record.put("phone", routeRequestEvent.getPhone());
            record.put("content", routeRequestEvent.getContent());
            record.put("srcId", routeRequestEvent.getSrcId());
            record.put("upSrcId", routeRequestEvent.getUpSrcId());
            record.put("msgIds", routeRequestEvent.getMsgIds());
            record.put("downRegionCode", routeRequestEvent.getDownRegionCode());
            record.put("upName", routeRequestEvent.getUpName());
            record.put("upRegionCode", routeRequestEvent.getUpRegionCode());
            record.put("makeTime", routeRequestEvent.getMakeTime());

        } else if(event instanceof ServerRequestFixUpNameEvent){
            ServerRequestFixUpNameEvent serverRequestFixUpNameEvent = (ServerRequestFixUpNameEvent) event;
            record.put("messageId", serverRequestFixUpNameEvent.getMessageId());
            record.put("downName", serverRequestFixUpNameEvent.getDownName());
            record.put("phone", serverRequestFixUpNameEvent.getPhone());
            record.put("content", serverRequestFixUpNameEvent.getContent());
            record.put("srcId", serverRequestFixUpNameEvent.getSrcId());
            record.put("upSrcId", serverRequestFixUpNameEvent.getUpSrcId());
            record.put("msgIds", serverRequestFixUpNameEvent.getMsgIds());
            record.put("downRegionCode", serverRequestFixUpNameEvent.getDownRegionCode());
            record.put("upName", serverRequestFixUpNameEvent.getUpName());
            record.put("upRegionCode", serverRequestFixUpNameEvent.getUpRegionCode());
            record.put("makeTime", serverRequestFixUpNameEvent.getMakeTime());

        } else if(event instanceof PortStatEvent){
            PortStatEvent portStatEvent = (PortStatEvent) event;
            record.put("messageId", portStatEvent.getMessageId());
            record.put("downName", portStatEvent.getDownName());
            record.put("upName", portStatEvent.getUpName());
            record.put("phone", portStatEvent.getPhone());
            record.put("msgId", portStatEvent.getMsgId());
            record.put("seqId", portStatEvent.getSeqId());
            record.put("stat", portStatEvent.getStat());
            record.put("srcId", portStatEvent.getSrcId());
            record.put("downRegionCode", portStatEvent.getDownRegionCode());
            record.put("makeTime", portStatEvent.getMakeTime());

        } else if(event instanceof PortMoEvent){
            PortMoEvent portMoEvent = (PortMoEvent) event;
            record.put("downName", portMoEvent.getDownName());
            record.put("phone", portMoEvent.getPhone());
            record.put("content", portMoEvent.getContent());
            record.put("destId", portMoEvent.getDestId());
            record.put("downRegionCode", portMoEvent.getDownRegionCode());
            record.put("makeTime", portMoEvent.getMakeTime());

        } else if(event instanceof StatArrivedEvent){
            StatArrivedEvent statArrivedEvent = (StatArrivedEvent) event;
            record.put("messageId", statArrivedEvent.getMessageId());
            record.put("msgId", statArrivedEvent.getMsgId());
            record.put("stat", statArrivedEvent.getStat());
            record.put("makeTime", statArrivedEvent.getMakeTime());

        } else if(event instanceof RouteReportEvent){
            RouteReportEvent routeReportEvent = (RouteReportEvent) event;
            record.put("messageId", routeReportEvent.getMessageId());
            record.put("upName", routeReportEvent.getUpName());
            record.put("carrier", routeReportEvent.getCarrier());
            record.put("cityCode", routeReportEvent.getCityCode());
            record.put("city", routeReportEvent.getCity());
            record.put("provinceCode", routeReportEvent.getProvinceCode());
            record.put("province", routeReportEvent.getProvince());
            record.put("msgCount", routeReportEvent.getMsgCount());
            record.put("upRegionCode", routeReportEvent.getUpRegionCode());
            record.put("upSrcId", routeReportEvent.getUpSrcId());
            record.put("makeTime", routeReportEvent.getMakeTime());
        }
        return record;
    }

    public static <T> T eventFromRecord(GenericRecord record, Class<T> eventType) {
        if(eventType == ServerRequestEvent.class){
            ServerRequestEvent serverRequestEvent = new ServerRequestEvent(
                    String.valueOf(record.get("messageId")),
                    String.valueOf(record.get("downName")),
                    String.valueOf(record.get("phone")),
                    String.valueOf(record.get("content")),
                    String.valueOf(record.get("srcId")),
                    (List<String>) record.get("msgIds"),
                    String.valueOf(record.get("downRegionCode"))
            );
            return (T) serverRequestEvent;
        }else if(eventType == RouteStatEvent.class){
            RouteStatEvent routeStatEvent = new RouteStatEvent(
                    String.valueOf(record.get("messageId")),
                    String.valueOf(record.get("downName")),
                    String.valueOf(record.get("upName")),
                    String.valueOf(record.get("phone")),
                    String.valueOf(record.get("stat")),
                    String.valueOf(record.get("msgId")),
                    String.valueOf(record.get("downRegionCode")),
                    String.valueOf(record.get("srcId"))
            );
            return (T) routeStatEvent;
        }else if(eventType == ServerRequestFixUpNameEvent.class){
            ServerRequestFixUpNameEvent serverRequestFixUpNameEvent = new ServerRequestFixUpNameEvent(
                    String.valueOf(record.get("messageId")),
                    String.valueOf(record.get("downName")),
                    String.valueOf(record.get("upName")),
                    String.valueOf(record.get("phone")),
                    String.valueOf(record.get("content")),
                    String.valueOf(record.get("srcId")),
                    String.valueOf(record.get("upSrcId")),
                    (List<Utf8>) record.get("msgIds"),
                    String.valueOf(record.get("downRegionCode")),
                    String.valueOf(record.get("upRegionCode"))
            );
            return (T) serverRequestFixUpNameEvent;
        }else if(eventType == RouteRequestEvent.class){
            RouteRequestEvent routeRequestEvent = new RouteRequestEvent(
                    String.valueOf(record.get("messageId")),
                    String.valueOf(record.get("downName")),
                    String.valueOf(record.get("upName")),
                    String.valueOf(record.get("phone")),
                    String.valueOf(record.get("content")),
                    String.valueOf(record.get("srcId")),
                    String.valueOf(record.get("upSrcId")),
                    (List<Utf8>) record.get("msgIds"),
                    String.valueOf(record.get("downRegionCode")),
                    String.valueOf(record.get("upRegionCode"))
            );
            return (T) routeRequestEvent;
        }else if(eventType == PortStatEvent.class){
            PortStatEvent portStatEvent = new PortStatEvent(
                    String.valueOf(record.get("messageId")),
                    String.valueOf(record.get("downName")),
                    String.valueOf(record.get("upName")),
                    String.valueOf(record.get("phone")),
                    String.valueOf(record.get("stat")),
                    String.valueOf(record.get("msgId")),
                    String.valueOf(record.get("seqId")),
                    String.valueOf(record.get("downRegionCode")),
                    String.valueOf(record.get("srcId"))
            );
            return (T) portStatEvent;
        }else if(eventType == PortMoEvent.class){
            PortMoEvent portMoEvent = new PortMoEvent(
                    String.valueOf(record.get("downName")),
                    String.valueOf(record.get("phone")),
                    String.valueOf(record.get("content")),
                    String.valueOf(record.get("destId")),
                    String.valueOf(record.get("downRegionCode"))
            );
            return (T) portMoEvent;
        }else if(eventType == StatArrivedEvent.class){
            StatArrivedEvent statArrivedEvent = new StatArrivedEvent(
                    String.valueOf(record.get("messageId")),
                    String.valueOf(record.get("msgId")),
                    String.valueOf(record.get("stat"))
            );
            return (T) statArrivedEvent;
        }else if(eventType == RouteReportEvent.class){
            RouteReportEvent routeReportEvent = new RouteReportEvent(
                    String.valueOf(record.get("messageId")),
                    String.valueOf(record.get("upName")),
                    String.valueOf(record.get("upRegionCode")),
                    (int)record.get("carrier"),
                    String.valueOf(record.get("cityCode")),
                    String.valueOf(record.get("city")),
                    String.valueOf(record.get("provinceCode")),
                    String.valueOf(record.get("province")),
                    (int)record.get("msgCount"),
                    String.valueOf(record.get("upSrcId"))
            );
            return (T) routeReportEvent;
        }
        return null;
    }

    public static String getSchema(Class eventClass) {
        return EVENT_SCHEMA.get(eventClass);
    }

    public static <T> byte[] getBytes(T event) {
        Injection<GenericRecord, byte[]> recordInjection = AvroUtil.getGenericAvroCodecs(event.getClass());
        GenericData.Record record = AvroUtil.recordFromEvent(event, EVENT_SCHEMA_PARSER.get(event.getClass()));
        return recordInjection.apply(record);
    }
}
