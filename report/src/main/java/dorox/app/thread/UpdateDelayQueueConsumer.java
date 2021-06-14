package dorox.app.thread;

import dorox.app.ReportMain;
import dorox.app.delay.UpdateEvent;
import dorox.app.delay.UpdateReportDelay;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/*负责批量更新数据*/
public class UpdateDelayQueueConsumer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(UpdateDelayQueueConsumer.class);
    private BlockingQueue<UpdateReportDelay> queue;

    private JdbcTemplate jdbcTemplate;

    public UpdateDelayQueueConsumer(JdbcTemplate jdbcTemplate, BlockingQueue<UpdateReportDelay> queue) {
        this.jdbcTemplate = jdbcTemplate;
        this.queue=queue;
    }


    /**
     * 1，延时队列中取数据，1秒超时
     * 2，没有新数据，检查【update列表】中的数据，批量更新到数据库
     * 3，有新数据，检查【update列表】中的数据大于500，先批量更新到数据库；新数据加入【update列表】
     * 4，加入【update列表】时，检查表名是否变化，如果变化则先批量更新到数据库
    * */
    @Override
    public void run() {

        List<Object[]> updateParam1 = new ArrayList<>();
        List<Object[]> updateParam2 = new ArrayList<>();
        String tableNameTmp = null;
        while (true) {
            try {
                UpdateReportDelay updateReportDelayEvent = queue.poll(1000, TimeUnit.MILLISECONDS);
                if(updateReportDelayEvent != null) {
                    logger.info("consumer take: {}", updateReportDelayEvent);
                }
                /*延时队列中无数据 且 列表中有数据，则更新数据库*/
                if(updateReportDelayEvent == null){
                    if(updateParam1.size() > 0 ){
                        updateReportDB1(updateParam1, tableNameTmp);
                    }
                    if(updateParam2.size() > 0 ){
                        updateReportDB2(updateParam2, tableNameTmp);
                    }
                }else{/*延时队列中数据 且 列表中数据大于500，批量更新数据库，清空列表*/
                    if(updateParam1.size() >= 500){
                        updateReportDB1(updateParam1, tableNameTmp);
                    }
                    if(updateParam2.size() >= 500){
                        updateReportDB2(updateParam2, tableNameTmp);
                    }
                    /*新数据加入【update列表】*/
                    String messageId = updateReportDelayEvent.getMessageId();
                    String tableName = getTableName(messageId);

                    /*判断是否为隔天数据，不是:直接加入【update列表】；是:就更新前一天列表，同时新数据加入【update列表】*/
                    if(tableNameTmp == null || tableNameTmp.equals(tableName)){
                        addUpdateParam(messageId, updateParam1, updateParam2);
                    }else{
                        updateReportDB1(updateParam1, tableNameTmp);
                        updateReportDB2(updateParam2, tableNameTmp);
                        addUpdateParam(messageId, updateParam1, updateParam2);
                    }
                    tableNameTmp = tableName;
                }
            } catch (Exception e) {
                logger.error("Exception: {}", e.toString());
            }
        }
    }

    private void updateReportDB1(List<Object[]> updateParam, String table) {
        String sql = "update "+ table +
                " set OWN_STAT=?, STAT=?, STAT_IN_TIME = ?, SEQ_ID = ?, MSG_RESP_ID = ?, STAT_OUT_TIME = ?, MSG_RESP_COUNT = ?  where MESSAGE_ID = ?";
        try {
            int[] ints = jdbcTemplate.batchUpdate(sql, updateParam);
            if(ints.length>0) {
                logger.info("ints.length:{}", ints.length);
            }
        } catch (Exception e) {
            for(Object[] obj : updateParam){
                logger.info("obj[0]:{},obj[1]:{},obj[2]:{},obj[3]:{},obj[4]:{},obj[5]:{},obj[6]:{},obj[7]:{}",
                        obj[0], obj[1], obj[2], obj[3], obj[4], obj[5], obj[6], obj[7]);
            }
            logger.info("sql:{}", sql);
            logger.info("exception:{}", e);
        }
        updateParam.clear();
    }
    private void updateReportDB2(List<Object[]> updateParam, String table) {
        String sql = "update "+ table + " set OWN_STAT=?, STAT=?, STAT_IN_TIME = ?, SEQ_ID = ?  where MESSAGE_ID = ?";
        try {
            int[] ints = jdbcTemplate.batchUpdate(sql, updateParam);
            if(ints.length>0) {
                logger.info("ints.length:{}", ints.length);
            }
        } catch (Exception e) {
            for(Object[] obj : updateParam){
                logger.info("obj[0]:{},obj[1]:{},obj[2]:{},obj[3]:{},obj[4]:{}", obj[0], obj[1], obj[2], obj[3], obj[4]);
            }
            logger.info("sql:{}", sql);
            logger.info("exception:{}", e);
        }
        updateParam.clear();
    }

    /**报表字段要确定好，哪几种情况需要更新报表，只考虑portstat和statarrived的更新，其他事件应该已经insert*/
    private void addUpdateParam(String messageId, List<Object[]> updateParam1, List<Object[]> updateParam2) {
        UpdateEvent updateEvent = ReportMain.UPDATE_REPORT_MAP.get(messageId);
        logger.info("updateEvent:{}", updateEvent);
        if(updateEvent != null){
            /*1，客户的回执响应和通道的回执消息都接收*/
            if(updateEvent.getPortStatEventSet() != null && updateEvent.getStatArrivedEventSet() != null) {
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
                Set<StatArrivedEvent> statArrivedEvent = updateEvent.getStatArrivedEventSet();
                String msgRespIds = ""; long statOutTime = 0;
                for(StatArrivedEvent e : statArrivedEvent) {
                    msgRespIds += e.getMsgId() + ",";
                    statOutTime = e.getMakeTime();
                }
                Object[] obj = new Object[8];
                obj[0] = ownStat; obj[1] = stat; obj[2] = getDateTimeOfTimestamp(statInTime).format(sdf2); obj[3] = seqIds; obj[4] = msgRespIds;
                obj[5] = getDateTimeOfTimestamp(statOutTime).format(sdf2); obj[6] = statArrivedEvent.size(); obj[7] = messageId;

                updateParam1.add(obj);
            /*2，收到通道回执，未接收到客户的回执响应*/
            }else if(updateEvent.getPortStatEventSet() != null && updateEvent.getStatArrivedEventSet() == null) {
                Set<PortStatEvent> portStatEvent = updateEvent.getPortStatEventSet();
                String stat = ""; String ownStat = "";long statInTime = 0;String seqIds = "";
                for(PortStatEvent e : portStatEvent) {
                    if(StringUtils.isBlank(stat) || ("DELIVRD".equals(stat) && (!"DELIVRD".equals(e.getStat())))){
                        stat = e.getStat();
                    }
                    ownStat += e.getStat() + ",";
                    seqIds += e.getSeqId() + ",";
                    statInTime = e.getMakeTime();
                }
                Object[] obj = new Object[5];
                obj[0] = ownStat;obj[1] = stat;obj[2] = getDateTimeOfTimestamp(statInTime).format(sdf2);obj[3] = seqIds;obj[4] = messageId;
                updateParam2.add(obj);
            /*3，记录其他情况*/
            }else{
                logger.info("messageId:{}， updateEvent：{}", messageId, updateEvent);
            }
        }
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
        ConcurrentHashMap<String, UpdateEvent> map =  new ConcurrentHashMap<>();

    }
}