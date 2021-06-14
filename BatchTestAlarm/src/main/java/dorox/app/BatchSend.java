package dorox.app;

import dorox.app.mq.event.ServerRequestEvent;
import dorox.app.text.SmsTextMessage;
import dorox.app.util.MqUtil;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
//批量发送短信
public class BatchSend extends Thread{

    private static final Logger logger = LoggerFactory.getLogger(BatchSend.class);

    private JdbcTemplate jdbcTemplate;
    private RabbitTemplate rabbitTemplate;
    private Sid sid;
    private String messageIdPrefix;

    public BatchSend(JdbcTemplate jdbcTemplate, RabbitTemplate rabbitTemplate, Sid sid, String messageIdPrefix) {
        this.jdbcTemplate = jdbcTemplate;this.rabbitTemplate=rabbitTemplate; this.sid=sid;this.messageIdPrefix= messageIdPrefix;
    }

    private ConcurrentHashMap<String, Runnable> END_SEND_TAKS_THREADS = new ConcurrentHashMap<String, Runnable>();

    @Override
    public void run() {
        try {
            String sql = "select DISTINCT down_port_code from end_send_message where status = '1' group by down_port_code";
            List<Map<String, Object>> downPorts = jdbcTemplate.queryForList(sql);
            for(Map<String, Object> downPortCodeMap : downPorts){
                Object downPortObj = downPortCodeMap.get("down_port_code");
                if(downPortObj == null || "null".equals(String.valueOf(downPortObj))){
                    continue;
                }
                final String downPortCode = String.valueOf(downPortObj);
//                判断客户是否有对应的runnable 如果没有就 new 一个 runnable
                if(!END_SEND_TAKS_THREADS.containsKey(downPortCode)){
                    END_SEND_TAKS_THREADS.put(downPortCode, new Runnable() {

                        @Override
                        public void run() {
                            while(true){
                                try {
                                    logger.info("check batch for downport:{}", downPortCode);
                                    runsecend(downPortCode);
                                    Thread.sleep(60*1000);
                                } catch (Exception e) {
                                    logger.error("exception:{}", e);
                                }
                            }
                        }
                    });

                    new Thread(END_SEND_TAKS_THREADS.get(downPortCode)).start();
                }
            }
        } catch (Exception e) {
            logger.error("exception:{}", e);
        }

    }
    private void runsecend(String downPortCode) throws Exception{
        String sql = "select send.id, send.phone, send.content, dp.down_spcode, dp.region_code from end_send_message send left join down_port dp on dp.down_name = send.down_port_code where send.status = '1' and send.down_port_code='"+downPortCode+"'";

        jdbcTemplate.query(sql, new Object[]{}, new RowMapper<Object>() {
            @Override
            public Object mapRow(ResultSet rs, int arg1) throws SQLException {

                String id = rs.getString("id");
                String phones = rs.getString("phone");
                String content = rs.getString("content");
                String srcId = rs.getString("down_spcode");
                String regionCode = rs.getString("region_code");

                if(srcId==null) { srcId=""; }

                final List<String> msgIds = new ArrayList<>();
                for(int i = 0; i < new SmsTextMessage(content).getPdus().length; i++){
                    msgIds.add("0");
                }
                for(String phone : phones.split(",")){
//                    判断电话号码是否是空
                    if(!StringUtils.isBlank(phone)){
                        String messageId = sid.nextShort(messageIdPrefix);
                        //ServerRequestEvent丢入队列
                        MqUtil.sendMsg(new ServerRequestEvent(messageId, downPortCode, phone, content, srcId, msgIds, regionCode),
                                rabbitTemplate, "serverrequest.report");

                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                jdbcTemplate.update("update end_send_message set status = '2' where id='" + id + "'");//修改状态已发送

                return null;
            }
        });
    }
}
