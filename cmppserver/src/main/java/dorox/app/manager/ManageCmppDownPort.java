package dorox.app.manager;

import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.connect.manager.cmpp.CMPPServerEndpointEntity;
import dorox.app.cmpp.CmppDownPort;

import dorox.app.util.Statics;
import org.n3r.idworker.Sid;
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

public class ManageCmppDownPort extends Thread{
	private static final Logger logger = LoggerFactory.getLogger(ManageCmppDownPort.class);


    private String downRegionCode;
	private String messageIdPrefix;
	private Sid sid;
	private JdbcTemplate jdbcTemplate;
	private RabbitTemplate rabbitTemplate;

	private EndpointManager manager ;
	private CMPPServerEndpointEntity server ;


	public ManageCmppDownPort(String downRegionCode, String messageIdPrefix,Sid sid,
							  JdbcTemplate jdbcTemplate, RabbitTemplate rabbitTemplate, EndpointManager manager,CMPPServerEndpointEntity server){
		this.jdbcTemplate=jdbcTemplate;
		this.downRegionCode=downRegionCode;
		this.manager=manager;
		this.server=server;
		this.sid=sid;
		this.messageIdPrefix=messageIdPrefix;
		this.rabbitTemplate=rabbitTemplate;
	}

	//构造CMPPDOWNPORT
	//加载路由信息		
	//加载白名单iP	
	@Override
	public void run() {
		try {
			scanAllCmppDownPort();
		} catch (Exception e) {
			logger.error("exception : {}", e);
		}
	}

	public void scanAllCmppDownPort() {
		
		String sql = "SELECT dp.down_name, dp.down_password ,dp.down_version ,dp.down_type ,"
				+ "dp.channel_num, dp.white_ip, dp.region_code, dp.status, dp.del_flag FROM down_port dp "
				+ "where dp.down_type='cmpp' and dp.region_code='" + downRegionCode + "' ";
		List<CmppDownPort> cmppDownPortList = jdbcTemplate.query(sql, new RowMapper<CmppDownPort>() {
			@Override
			public CmppDownPort mapRow(ResultSet rs, int arg1) throws SQLException {
				CmppDownPort cmppDownPort = null;
				try {
					String downName = rs.getString("down_name");
					String downPassword = rs.getString("down_password");
					String version = rs.getString("down_version");
					String downType = rs.getString("down_type");
					short channelNum = rs.getShort("channel_num");
					String whiteIp = rs.getString("white_ip");

					int status = rs.getInt("status");
					int delFlag = rs.getInt("del_flag");

					String downRegionCode2 = rs.getString("region_code");

					CmppDownPort existCmppDownPort = Statics.DOWN_PORT_MAP.get(downName);
					if(!downRegionCode2.equals(downRegionCode)){
						if(existCmppDownPort !=null){
							existCmppDownPort.startCmppPort(CmppDownPort.CMPP_PORT_STOP);
						}
						return null;
					}

					/*and dp.`status` = 1 and dp.del_flag = 0*/
					if(status==1 && delFlag==0){
						if(existCmppDownPort == null){
							cmppDownPort = new CmppDownPort(jdbcTemplate, rabbitTemplate, manager, server, sid, messageIdPrefix);
							cmppDownPort.downName(downName).downPassword(downPassword).version(version).downType(downType)
									.channelNum(channelNum).regionCode(downRegionCode).whiteIp(whiteIp);

							Statics.DOWN_PORT_MAP.put(cmppDownPort.getDownName(), cmppDownPort);
							//启动端口
							cmppDownPort.startCmppPort(CmppDownPort.CMPP_PORT_START);
						}else{
							if((!existCmppDownPort.getDownName().equals(downName)) || (!existCmppDownPort.getDownPassword().equals(downPassword)) ||
									(!existCmppDownPort.getVersion().equals(version)) || (existCmppDownPort.getChannelNum() != channelNum)){

								existCmppDownPort.downName(downName).downPassword(downPassword).version(version)
										.channelNum(Short.valueOf(channelNum)).regionCode(downRegionCode);
								existCmppDownPort.startCmppPort(CmppDownPort.CMPP_PORT_RESET);
							}
							if(! existCmppDownPort.getWhiteIp().equals(whiteIp)) {
								existCmppDownPort.whiteIp(whiteIp);
							}
						}
					}else{
						if(existCmppDownPort!=null) {
							existCmppDownPort.startCmppPort(CmppDownPort.CMPP_PORT_STOP);
						}
					}

				} catch (Exception e) {
					logger.error("{}",e);
				}
				return cmppDownPort;
			}
		});

		for(CmppDownPort cmppDownPort : cmppDownPortList) {
			if(cmppDownPort!=null) {
				cmppDownPort.manageCmppDownPort(this);
			}
		}

		logger.info("cmpp DOWN_PORT {}",Statics.DOWN_PORT_MAP);
	}


	public void loadCmppDownPort(String downName, String regionCode, String downPassword, 
			String whiteIp, String downVersion,
			String downType, String channelNum,
			int cmppReset) {
		CmppDownPort cmppDownPort = new CmppDownPort(
				jdbcTemplate, rabbitTemplate, manager, server, sid, messageIdPrefix);
		cmppDownPort.downName(downName).downPassword(downPassword).version(downVersion)
		.downType(downType).channelNum(Short.valueOf(channelNum)).regionCode(regionCode)
		.whiteIp(whiteIp).manageCmppDownPort(this);
		
		Statics.DOWN_PORT_MAP.put(cmppDownPort.getDownName(), cmppDownPort);
		//启动端口
		cmppDownPort.startCmppPort(cmppReset);
		
	}
}
