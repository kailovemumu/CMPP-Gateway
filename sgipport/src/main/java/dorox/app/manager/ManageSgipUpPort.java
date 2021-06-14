package dorox.app.manager;

import com.google.common.util.concurrent.RateLimiter;
import com.zx.sms.connect.manager.EndpointManager;
import dorox.app.UpPortMain;
import dorox.app.sgip.SgipUpPort;
import dorox.app.util.Statics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ManageSgipUpPort extends Thread{
	private static final Logger logger = LoggerFactory.getLogger(ManageSgipUpPort.class);

    private String upRegionCode;

	private JdbcTemplate jdbcTemplate;
	private RabbitTemplate rabbitTemplate;
	private EndpointManager manager;

	public ManageSgipUpPort(JdbcTemplate jdbcTemplate, RabbitTemplate rabbitTemplate, String upRegionCode, EndpointManager manager){
		this.jdbcTemplate=jdbcTemplate;
		this.rabbitTemplate=rabbitTemplate;
		this.upRegionCode=upRegionCode;
		this.manager=manager;
		this.manager.startConnectionCheckTask();
	}

	@Override
	public void run() {
		try {
			scanAllCmppUpPort();
			channelSpeedFlush();//渠道速率
		} catch (Exception e) {
			logger.error("exception : {}", e);
		}
	}

	public void scanAllCmppUpPort() {

		String sql = "SELECT * FROM up_port where up_type = 'sgip' ";
		List<SgipUpPort> cmppUpPortList = jdbcTemplate.query(sql, new RowMapper<SgipUpPort>() {
			@Override
			public SgipUpPort mapRow(ResultSet rs, int arg1) throws SQLException {
				SgipUpPort sgipUpPort = null;
				try {

					String upName = rs.getString("up_name");
					String upPassword = rs.getString("up_password");

					int upPort = rs.getInt("up_port");
					String upIp = rs.getString("up_ip");
					String upSpCode = rs.getString("up_spcode");
					String upType = rs.getString("up_type");
					short channelNum = rs.getShort("channel_num");
					long nodeId = rs.getLong("node_id");
					int sgipRecePort = rs.getInt("sgip_rece_port");
					String upRegionCode2 = rs.getString("region_code");

					int status = rs.getInt("status");
					int delFlag = rs.getInt("del_flag");

					SgipUpPort existSgipUpPort = Statics.UP_PORT_MAP.get(upName);
					if(!upRegionCode2.equals(upRegionCode)){
						if(existSgipUpPort !=null){
							existSgipUpPort.startSgipPort(SgipUpPort.SGIP_PORT_STOP);
						}
						return null;
					}

					/*and status = 1 and del_flag = 0*/
					if(status==1 && delFlag==0){
						if(existSgipUpPort == null){
							sgipUpPort = new SgipUpPort(jdbcTemplate, rabbitTemplate, manager);
							sgipUpPort.upName(upName).upPassword(upPassword).upPort(upPort).upIp(upIp).upSpCode(upSpCode)
									.upType(upType).channelNum(channelNum)
									.nodeId(nodeId).sgipRecePort(sgipRecePort).upRegionCode(upRegionCode);

							Statics.UP_PORT_MAP.put(sgipUpPort.getUpName(), sgipUpPort);
							//启动端口
							sgipUpPort.startSgipPort(SgipUpPort.SGIP_PORT_START);
						}else{
							if((!existSgipUpPort.getUpName().equals(upName)) || (!existSgipUpPort.getUpPassword().equals(upPassword)) ||
									(existSgipUpPort.getChannelNum() != channelNum) ||
									(!existSgipUpPort.getUpIp().equals(upIp)) || (existSgipUpPort.getUpPort()!= upPort)||
									(existSgipUpPort.getNodeId() != nodeId) || (existSgipUpPort.getSgipRecePort()!= sgipRecePort)){

								existSgipUpPort.upName(upName).upPassword(upPassword).upPort(upPort).upIp(upIp).upSpCode(upSpCode)
										.upType(upType).channelNum(Short.valueOf(channelNum)).nodeId(nodeId).sgipRecePort(sgipRecePort)
										.upRegionCode(upRegionCode);
								existSgipUpPort.startSgipPort(SgipUpPort.SGIP_PORT_RESET);
							}
							existSgipUpPort.upSpCode(upSpCode).upType(upType);
						}

					}else {
						if(existSgipUpPort!=null) {
							existSgipUpPort.startSgipPort(SgipUpPort.SGIP_PORT_STOP);
						}
					}
				} catch (Exception e) {
					logger.error("{}",e);
				}
				return sgipUpPort;
			}
		});

		for(SgipUpPort cmppUpPort : cmppUpPortList) {
			if(cmppUpPort != null) {
				cmppUpPort.manageCmppUpPort(this);
			}
		}

		logger.info("cmpp DOWN_UP {}",Statics.UP_PORT_MAP);
	}

	private void channelSpeedFlush(){
		String sql = "select ch.down_port_code, ch.up_port_code,  ch.speed from channel ch left join up_port up on ch.up_port_code=up.up_name where ch.del_flag=0 and ch.status=1 and up.region_code='"+upRegionCode+"'";
		jdbcTemplate.query(sql, new Object[]{},
				(ResultSet rs, int arg1)->{
					String down_port_code = rs.getString("down_port_code");
					String up_port_code = rs.getString("up_port_code");
					String speed = rs.getString("speed");

					UpPortMain.CHANNEL_SPEED_MAP.put(down_port_code + up_port_code,
							RateLimiter.create(Double.valueOf(speed)));
					return null;
				}
		);
	}
}
