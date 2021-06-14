package dorox.app;

import dorox.app.mq.event.ServerRequestEvent;
import dorox.app.mq.event.ServerRequestFixUpNameEvent;
import dorox.app.text.SmsTextMessage;
import dorox.app.util.MqUtil;
import org.apache.avro.util.Utf8;
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

/**
 * 发送测试短信和告警短信
 */
public class AlarmAndTest extends Thread{
	
	private static final Logger logger = LoggerFactory.getLogger(AlarmAndTest.class);
	
    private JdbcTemplate jdbcTemplate;
	private RabbitTemplate rabbitTemplate;
	private Sid sid;
	private String messageIdPrefix;
	
	public AlarmAndTest(JdbcTemplate jdbcTemplate, RabbitTemplate rabbitTemplate, Sid sid, String messageIdPrefix) {
		this.jdbcTemplate = jdbcTemplate;this.rabbitTemplate=rabbitTemplate; this.sid=sid;this.messageIdPrefix= messageIdPrefix;
	}

	@Override
	public void run() {
		//logger.info("take msg from table, and send msg...");
		alarmAndTest();
	}

	private void alarmAndTest(){
		String sql = "select atm.id, atm.down_port_code, atm.up_port_code, atm.phone, " +
				" atm.content, atm.src_id,  dp.region_code downRegionCode, up.region_code upRegionCode " +
				" from alarm_test_message atm " +
				" LEFT JOIN down_port dp on dp.down_name = atm.down_port_code " +
				" LEFT JOIN up_port up on up.up_name = atm.up_port_code " +
				" where atm.status = '1'";//待发送
		jdbcTemplate.query(sql, new Object[]{}, new RowMapper<Object>() {
			@Override
			public Object mapRow(ResultSet rs, int arg1) throws SQLException {
				
				String id = rs.getString("id");
				String upPortCode = rs.getString("up_port_code");
				String phones = rs.getString("phone");
				String content = rs.getString("content");
				String srcId = rs.getString("src_id");
				String downPortCode = rs.getString("down_port_code");//测试专用账号
				String downRegionCode = rs.getString("downRegionCode");//区域
				String upRegionCode = rs.getString("upRegionCode");//区域
				
				for(String phone : phones.split(",")){
					
					try {
						//runsecendByPost(downPortCode, upPortCode, downRegionCode, upRegionCode, phone, content, srcId);

						String messageId = sid.nextShort(messageIdPrefix);
						final List<String> msgIds = new ArrayList<>();
						final List<Utf8> msgIds1 = new ArrayList<>();
						for(int i = 0; i < new SmsTextMessage(content).getPdus().length; i++){
							msgIds.add("0");
							msgIds1.add(new Utf8("0"));
						}

						ServerRequestEvent serverRequestEvent = new ServerRequestEvent(
								messageId, downPortCode, phone, content, srcId,  msgIds,
								downRegionCode);
						MqUtil.sendMsg(serverRequestEvent, rabbitTemplate, "report");

						ServerRequestFixUpNameEvent serverRequestFixUpNameEvent = new ServerRequestFixUpNameEvent(
								messageId, downPortCode, upPortCode, phone, content, srcId, srcId, msgIds1,
								downRegionCode, upRegionCode);
						MqUtil.sendMsg(serverRequestFixUpNameEvent, rabbitTemplate, "serverrequestfixupname");

						Thread.sleep(50);
					} catch (Exception e) {
						
						e.printStackTrace();
					}
				}
				jdbcTemplate.update(
						"update alarm_test_message set status = '2' where id='" + id + "'");//修改状态已发送

				return null;
			}
		});
	}
}
