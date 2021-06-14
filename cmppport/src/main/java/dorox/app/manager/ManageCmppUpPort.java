package dorox.app.manager;

import com.google.common.util.concurrent.RateLimiter;
import com.zx.sms.connect.manager.EndpointManager;
import dorox.app.UpPortMain;
import dorox.app.cmpp.CmppUpPort;
import dorox.app.util.Statics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ManageCmppUpPort extends Thread{
	private static final Logger logger = LoggerFactory.getLogger(ManageCmppUpPort.class);

    private String upRegionCode;

	private JdbcTemplate jdbcTemplate;
	private RabbitTemplate rabbitTemplate;
	private EndpointManager manager;

	public ManageCmppUpPort(JdbcTemplate jdbcTemplate, RabbitTemplate rabbitTemplate, String upRegionCode, EndpointManager manager){
		this.jdbcTemplate=jdbcTemplate;
		this.upRegionCode=upRegionCode;
		this.manager=manager;
		this.manager.startConnectionCheckTask();
		this.rabbitTemplate=rabbitTemplate;
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

		String sql = "SELECT * FROM up_port where up_type = 'cmpp' ";
		List<CmppUpPort> cmppUpPortList = jdbcTemplate.query(sql, new RowMapper<CmppUpPort>() {
			@Override
			public CmppUpPort mapRow(ResultSet rs, int arg1) throws SQLException {
				CmppUpPort cmppUpPort = null;
				try {

					String upName = rs.getString("up_name");
					String upPassword = rs.getString("up_password");
					String upVersion = rs.getString("up_version");
					int upPort = rs.getInt("up_port");
					String upIp = rs.getString("up_ip");
					String upSpCode = rs.getString("up_spcode");
					String upType = rs.getString("up_type");
					short channelNum = rs.getShort("channel_num");

					String upRegionCode2 = rs.getString("region_code");

					int status = rs.getInt("status");
					int delFlag = rs.getInt("del_flag");

					CmppUpPort existCmppUpPort = Statics.UP_PORT_MAP.get(upName);
//					如果端口的分区信息做了修改，则该分区停止
					if(!upRegionCode2.equals(upRegionCode)){
						if(existCmppUpPort !=null){
							existCmppUpPort.startCmppPort(CmppUpPort.CMPP_PORT_STOP);
						}
						return null;
					}

					/*and status = 1 and del_flag = 0*/
					if(status==1 && delFlag==0){
//						端口未加载，则启动端口
						if(existCmppUpPort == null){
							cmppUpPort = new CmppUpPort(jdbcTemplate, rabbitTemplate, manager);
							cmppUpPort.upName(upName).upPassword(upPassword).upVersion(upVersion).upPort(upPort).upIp(upIp).upSpCode(upSpCode)
									.upType(upType).channelNum(Short.valueOf(channelNum)).upRegionCode(upRegionCode);

							Statics.UP_PORT_MAP.put(cmppUpPort.getUpName(), cmppUpPort);
							//启动端口
							cmppUpPort.startCmppPort(CmppUpPort.CMPP_PORT_START);
						}else{
//							端口已经加载，修改端口的配置信息，如果下面的配置有修改，则配置会重启
							if((!existCmppUpPort.getUpName().equals(upName)) || (!existCmppUpPort.getUpPassword().equals(upPassword)) ||
									(!existCmppUpPort.getUpVersion().equals(upVersion)) || (existCmppUpPort.getChannelNum() != channelNum) ||
									(!existCmppUpPort.getUpIp().equals(upIp)) || (existCmppUpPort.getUpPort()!= upPort)){
//								更新端口的配置信息
								existCmppUpPort.upName(upName).upPassword(upPassword).upVersion(upVersion).upPort(upPort).upIp(upIp).upSpCode(upSpCode)
										.upType(upType).channelNum(Short.valueOf(channelNum)).upRegionCode(upRegionCode);
//								重启端口
								existCmppUpPort.startCmppPort(CmppUpPort.CMPP_PORT_RESET);
							}
							existCmppUpPort.upSpCode(upSpCode).upType(upType);
						}

					}else {
						if(existCmppUpPort!=null) {
							existCmppUpPort.startCmppPort(CmppUpPort.CMPP_PORT_STOP);
						}
					}
				} catch (Exception e) {
					logger.error("{}",e);
				}
				return cmppUpPort;
			}
		});

		for(CmppUpPort cmppUpPort : cmppUpPortList) {
//			如果端口list内的端口不为空，则交由端口管理类进行管理
			if(cmppUpPort!=null) {
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
