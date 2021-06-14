//package dorox.app;
//
//
//import dorox.app.thread.ServerRequestDelayQueueConsumer;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = {ReportApplication.class})
//public class DBTest {
//    private static final Logger logger = LoggerFactory.getLogger(DBTest.class);
//
//    @Autowired
//    private JdbcTemplate jdbcTemplate;
//
//    @Test
//    public void testInsert(){
//        String table = "detail_report_20210327";
//        List<Object[]> insertParam = new ArrayList<>();
//        Object[] objects = new Object[22];
//        objects[0] = "1";
//        objects[1] = "2";
//        objects[2] = "3";
//        insertParam.add(objects);
//
//        insertReport(insertParam, table);
//
//
//    }
//
//    private void updateReport(List<Object[]> insertParam, String table) {
//        String sql = "update "+ table +
//                " set MESSAGE_ID=?,DOWN_PORT_CODE=?,PHONE=?,CONTENT=?,SRC_ID=?,MSG_ID=?,DOWN_REQ_TIME=?,DOWN_REGION_CODE=?,UP_PORT_CODE=?,CARRIER=?,CITY_CODE=?,CITY=?,PROVINCE=?,PROVINCE_CODE=?,MSG_COUNT=?,UP_REGION_CODE=?,STAT=?,STAT_IN_TIME=?,SEQ_ID=?,MSG_RESP_ID=?,STAT_OUT_TIME=?,MSG_RESP_COUNT=?"
//                + " where MESSAGE_ID is null ";
//        try {
//            int[] ints = jdbcTemplate.batchUpdate(sql, insertParam);
//            logger.info("ints.length:{}", ints.length);
//        } catch (Exception e) {
//            for(Object[] obj : insertParam){
//                logger.info("obj[0]:{},obj[1]:{},obj[2]:{},obj[3]:{},obj[4]:{},obj[5]:{},obj[6]:{},obj[7]:{},obj[8]:{},obj[9]:{}," +
//                                "obj[10]:{},obj[11]:{},obj[12]:{},obj[13]:{},obj[14]:{},obj[15]:{},obj[16]:{},obj[17]:{},obj[18]:{},obj[19]:{}," +
//                                "obj[20]:{},obj[21]:{}",
//                        obj[0], obj[1], obj[2], obj[3], obj[4], obj[5], obj[6], obj[7], obj[8], obj[9],
//                        obj[10], obj[11], obj[12], obj[13], obj[14], obj[15], obj[16], obj[17], obj[18], obj[19],
//                        obj[20], obj[21]);
//            }
//            logger.info("sql:{}", sql);
//            logger.info("exception:{}", e);
//        }
//        insertParam.clear();
//    }
//
//    private void insertReport(List<Object[]> insertParam, String table) {
//        String sql = "insert into "+ table +
//                " (MESSAGE_ID,DOWN_PORT_CODE,PHONE,CONTENT,SRC_ID,MSG_ID,DOWN_REQ_TIME,DOWN_REGION_CODE,UP_PORT_CODE,CARRIER,CITY_CODE,CITY,PROVINCE,PROVINCE_CODE,MSG_COUNT,UP_REGION_CODE,STAT,STAT_IN_TIME,SEQ_ID,MSG_RESP_ID,STAT_OUT_TIME,MSG_RESP_COUNT)"
//                + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
//        try {
//            int[] ints = jdbcTemplate.batchUpdate(sql, insertParam);
//            logger.info("ints.length:{}", ints.length);
//        } catch (Exception e) {
//            for(Object[] obj : insertParam){
//                logger.info("obj[0]:{},obj[1]:{},obj[2]:{},obj[3]:{},obj[4]:{},obj[5]:{},obj[6]:{},obj[7]:{},obj[8]:{},obj[9]:{}," +
//                                "obj[10]:{},obj[11]:{},obj[12]:{},obj[13]:{},obj[14]:{},obj[15]:{},obj[16]:{},obj[17]:{},obj[18]:{},obj[19]:{}," +
//                                "obj[20]:{},obj[21]:{}",
//                        obj[0], obj[1], obj[2], obj[3], obj[4], obj[5], obj[6], obj[7], obj[8], obj[9],
//                        obj[10], obj[11], obj[12], obj[13], obj[14], obj[15], obj[16], obj[17], obj[18], obj[19],
//                        obj[20], obj[21]);
//            }
//            logger.info("sql:{}", sql);
//            logger.info("exception:{}", e);
//        }
//        insertParam.clear();
//    }
//}
