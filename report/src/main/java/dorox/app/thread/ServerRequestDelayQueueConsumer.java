package dorox.app.thread;

import dorox.app.ReportMain;
import dorox.app.delay.ServerRequestReportDelay;
import dorox.app.delay.UpdateEvent;
import dorox.app.mq.event.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/*负责批量插入数据*/
public class ServerRequestDelayQueueConsumer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ServerRequestDelayQueueConsumer.class);
    private BlockingQueue<ServerRequestReportDelay> queue;

    private JdbcTemplate jdbcTemplate;

    public ServerRequestDelayQueueConsumer(JdbcTemplate jdbcTemplate,BlockingQueue<ServerRequestReportDelay> queue) {
        this.jdbcTemplate = jdbcTemplate;
        this.queue=queue;
    }

    /**
     * 1，延时队列中取数据，1秒超时
     * 2，没有新数据，检查insert列表中的数据，批量更新到数据库
     * 3，有新数据，检查insert列表中的数据大于5000，先批量更新到数据库；新数据加入insert列表
     * 4，加入insert列表时，检查表名是否变化，如果变化则先批量更新到数据库
    * */
    @Override
    public void run() {

        List<Object[]> insertParam = new ArrayList<>();
        String tableNameTmp = null;
        while (true) {
            try {
                ServerRequestReportDelay serverRequestReportDelayEvent = queue.poll(1000, TimeUnit.MILLISECONDS);
                if(serverRequestReportDelayEvent != null){
                    logger.info("consumer take: {}", serverRequestReportDelayEvent);
                }
                /*延时队列中无数据 且 列表中有数据，则插入数据库*/
                if(serverRequestReportDelayEvent == null){
                    if(insertParam.size() > 0 ){
                        insertReport(insertParam,tableNameTmp);

                    }
                }else{/*延时队列中数据 且 列表中数据大于5000，批量插入数据库，清空列表*/
                    if(insertParam.size() >= 5000){
                        insertReport(insertParam,tableNameTmp);
                    }
                    /*新数据加入【insert列表】*/
                    ServerRequestEvent serverRequestEvent = serverRequestReportDelayEvent.getServerRequestEvent();
                    String messageId = serverRequestEvent.getMessageId();
                    String tableName = getTableName(messageId);

                    /*判断是否为隔天数据，不是:直接加入【insert列表】；是:就更新前一天列表，同时新数据加入【insert列表】*/
                    if(tableNameTmp == null || tableNameTmp.equals(tableName)){
                        addInsertParam(serverRequestEvent, insertParam);
                    }else{
                        insertReport(insertParam, tableNameTmp);
                        addInsertParam(serverRequestEvent, insertParam);
                    }
                    tableNameTmp = tableName;
                }
            } catch (Exception e) {
                logger.error("Exception: {}", e.toString());
            }
        }
    }

    private void insertReport(List<Object[]> insertParam, String table) {
        String sql = "insert into "+ table +
                " (MESSAGE_ID,DOWN_PORT_CODE,PHONE,CONTENT,SRC_ID,MSG_ID,DOWN_REQ_TIME,DOWN_REGION_CODE,UP_PORT_CODE,CARRIER,CITY_CODE,CITY,PROVINCE,PROVINCE_CODE,MSG_COUNT,UP_REGION_CODE,STAT,STAT_IN_TIME,SEQ_ID,MSG_RESP_ID,STAT_OUT_TIME,MSG_RESP_COUNT,OWN_STAT)"
                + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            int[] ints = jdbcTemplate.batchUpdate(sql, insertParam);
            if(ints.length>0) {
                logger.info("ints.length:{}", ints.length);
            }
        } catch (Exception e) {
            for(Object[] obj : insertParam){
                logger.info("obj[0]:{},obj[1]:{},obj[2]:{},obj[3]:{},obj[4]:{},obj[5]:{},obj[6]:{},obj[7]:{},obj[8]:{},obj[9]:{}," +
                                "obj[10]:{},obj[11]:{},obj[12]:{},obj[13]:{},obj[14]:{},obj[15]:{},obj[16]:{},obj[17]:{},obj[18]:{},obj[19]:{}," +
                                "obj[20]:{},obj[21]:{},obj[22]:{}",
                        obj[0], obj[1], obj[2], obj[3], obj[4], obj[5], obj[6], obj[7], obj[8], obj[9],
                        obj[10], obj[11], obj[12], obj[13], obj[14], obj[15], obj[16], obj[17], obj[18], obj[19],
                        obj[20], obj[21], obj[22]);
            }
            logger.info("sql:{}", sql);
            logger.info("exception:{}", e);
        }
        insertParam.clear();
    }

    /**报表字段要确定好*/
    private void addInsertParam(ServerRequestEvent serverRequestEvent, List<Object[]> insertParam) {

        String messageId = serverRequestEvent.getMessageId();
        String downName = serverRequestEvent.getDownName();
        String phone = serverRequestEvent.getPhone();
        String content = serverRequestEvent.getContent();
        String srcId = serverRequestEvent.getSrcId();
        List<String> msgIds = serverRequestEvent.getMsgIds();
        long downReqTime = serverRequestEvent.getMakeTime();
        String downRegionCode = serverRequestEvent.getDownRegionCode();

        Object[] obj = new Object[23];
        obj[0] = messageId;        obj[1] = downName;        obj[2] = phone;        obj[3] = content;        obj[4] = srcId;
        obj[5] = msgIds.toString();        obj[6] = getDateTimeOfTimestamp(downReqTime).format(sdf2);        obj[7] = downRegionCode;
        obj[16] = "接收成功";
        UpdateEvent updateEvent = ReportMain.UPDATE_REPORT_MAP.remove(messageId);
        logger.info("updateEvent:{}", updateEvent);

        if(updateEvent!= null && updateEvent.getRouteReportEvent()!=null){
            RouteReportEvent routeReportEvent = updateEvent.getRouteReportEvent();
            String upName = routeReportEvent.getUpName();
            int carrier=routeReportEvent.getCarrier();
            String cityCode = routeReportEvent.getCityCode();
            String city = routeReportEvent.getCity();
            String province = routeReportEvent.getProvince();
            String provinceCode = routeReportEvent.getProvinceCode();
            int msgCount = routeReportEvent.getMsgCount();
            String upRegionCode = routeReportEvent.getUpRegionCode();
            String upSrcId = routeReportEvent.getUpSrcId();
            obj[8] = upName; obj[9] = carrier;obj[10] = cityCode;obj[11] = city;
            obj[12] = province;obj[13] = provinceCode;obj[14] = msgCount;obj[15] = upRegionCode;
            obj[4] += "/" + upSrcId;
        }

        if(updateEvent != null && updateEvent.getRouteStatEventSet() != null && updateEvent.getRouteStatEventSet().size() > 0){
            Set<RouteStatEvent> routeStatEvent = updateEvent.getRouteStatEventSet();
            String stat = ""; long statInTime = 0;String ownStat = "";
            for(RouteStatEvent e : routeStatEvent) {
                if(StringUtils.isBlank(stat) || ("DELIVRD".equals(stat) && (!"DELIVRD".equals(e.getStat())))){
                    stat = e.getStat();
                }
                ownStat += e.getStat() + ",";
                statInTime = e.getMakeTime();
            }
            obj[16] = stat;
            obj[17] = getDateTimeOfTimestamp(statInTime).format(sdf2);
            obj[22] = ownStat;
        }

        if(updateEvent != null && updateEvent.getPortStatEventSet() != null && updateEvent.getPortStatEventSet().size() > 0){
            Set<PortStatEvent> portStatEvent = updateEvent.getPortStatEventSet();
            String stat = ""; long statInTime = 0;String seqIds = "";String ownStat = "";
            for(PortStatEvent e : portStatEvent) {
                if(StringUtils.isBlank(stat) || ("DELIVRD".equals(stat) && (!"DELIVRD".equals(e.getStat())))){
                    stat = e.getStat();
                }
                ownStat += e.getStat() + ",";
                seqIds += e.getSeqId() + ",";
                statInTime = e.getMakeTime();
            }
            obj[16] = stat;
            obj[17] = getDateTimeOfTimestamp(statInTime).format(sdf2);
            obj[18] = seqIds;
            obj[22] = ownStat;
        }

        if(updateEvent != null && updateEvent.getStatArrivedEventSet() != null && updateEvent.getStatArrivedEventSet().size() > 0){
            Set<StatArrivedEvent> statArrivedEvent = updateEvent.getStatArrivedEventSet();
            String msgRespIds = ""; long statOutTime = 0;
            for(StatArrivedEvent e : statArrivedEvent) {
                msgRespIds += e.getMsgId() + ",";
                statOutTime = e.getMakeTime();
            }
            obj[19] = msgRespIds;
            obj[20] = getDateTimeOfTimestamp(statOutTime).format(sdf2);
            obj[21] = statArrivedEvent.size();
        }

        insertParam.add(obj);
    }

    private static LocalDateTime getDateTimeOfTimestamp(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp),  ZoneId.systemDefault());
    }
    private static DateTimeFormatter sdf2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");
    private static final String DETAIL_TABLE_NAME = "detail_report_";

    private static String getTableName(String messageId) {
        return  DETAIL_TABLE_NAME + LocalDate.now().format(formatter) + messageId.substring(2,6);
    }

    public static void main(String[] args) {
        System.out.println(getTableName("DL03199D7DCC363C"));
    }
}