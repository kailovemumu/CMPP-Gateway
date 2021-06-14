package dorox.app.manager;

import dorox.app.http.HttpDownPort;
import dorox.app.util.Statics;
import org.n3r.idworker.Sid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ManageHttpDownPort extends Thread{
	private static final Logger logger = LoggerFactory.getLogger(ManageHttpDownPort.class);


    private String downRegionCode;
	private String messageIdPrefix;
	private Sid sid;
	private JdbcTemplate jdbcTemplate;

	public ManageHttpDownPort(String downRegionCode, String messageIdPrefix,Sid sid, JdbcTemplate jdbcTemplate ){
		this.jdbcTemplate=jdbcTemplate;
		this.downRegionCode=downRegionCode;
		this.sid=sid;
		this.messageIdPrefix=messageIdPrefix;
	}

	@Override
	public void run() {
		try {
			scanAllHttpDownPort();
		} catch (Exception e) {
			logger.error("exception : {}", e);
		}
	}

	public void scanAllHttpDownPort() {
		
		String sql = "SELECT dp.down_name, dp.down_password, dp.down_type, dp.push_result, dp.stat_url, dp.mo_url,"
				+ " dp.white_ip, dp.region_code, dp.status, dp.del_flag FROM down_port dp "
				+ "where dp.down_type='http' and dp.region_code='" + downRegionCode + "' ";
		List<HttpDownPort> httpDownPortList = jdbcTemplate.query(sql, new RowMapper<HttpDownPort>() {
			@Override
			public HttpDownPort mapRow(ResultSet rs, int arg1) throws SQLException {
				HttpDownPort httpDownPort = null;
				try {
					String downName = rs.getString("down_name");
					String downPassword = rs.getString("down_password");
					String downType = rs.getString("down_type");
					String whiteIp = rs.getString("white_ip");

					int pushResult = rs.getInt("push_result");
					String statUrl = rs.getString("stat_url");
					String moUrl = rs.getString("mo_url");

					int status = rs.getInt("status");
					int delFlag = rs.getInt("del_flag");

					/*and dp.`status` = 1 and dp.del_flag = 0*/
					if(status==1 && delFlag==0){
						httpDownPort = new HttpDownPort(jdbcTemplate, sid, messageIdPrefix);
						httpDownPort.downName(downName).downPassword(downPassword).downType(downType)
								.regionCode(downRegionCode).whiteIp(whiteIp).moUrl(moUrl).statUrl(statUrl);

						if(pushResult == 1) {
							httpDownPort.pushResult(true);
						}else {
							httpDownPort.pushResult(false);
						}

						Statics.DOWN_PORT_MAP.put(httpDownPort.getDownName(), httpDownPort);
					}else{
						Statics.DOWN_PORT_MAP.remove(downName);
					}
				} catch (Exception e) {
					logger.error("{}",e);
				}
				return httpDownPort;
			}
		});
		logger.info("http DOWN_PORT {}",Statics.DOWN_PORT_MAP);
	}
}
