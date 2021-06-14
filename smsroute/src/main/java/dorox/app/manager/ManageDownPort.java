package dorox.app.manager;

import dorox.app.port.DownPort;
import dorox.app.util.Statics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class ManageDownPort {
	private static final Logger logger = LoggerFactory.getLogger(ManageDownPort.class);

	@Autowired
    private JdbcTemplate jdbcTemplate;
	@Autowired
	private MobileInfoFlush mobileInfoFlush;
	@Autowired
	private MobileRouteFlush mobileRouteFlush;
	@Autowired
	private BlackFlush blackFlush;


	public void scanAllDownPort() {
		
		String sql = "SELECT dp.down_name,dp.down_spcode,dp.down_vspcode,dp.is_advance,dp.region_code "
				+ " FROM down_port dp where dp.status = 1 and dp.del_flag = 0";
		List<DownPort> downPortList = jdbcTemplate.query(sql, new RowMapper<DownPort>() {
			@Override
			public DownPort mapRow(ResultSet rs, int arg1) throws SQLException {
				DownPort downPort = null;
				try {
					String downName = rs.getString("down_name");
					String downSpcode = rs.getString("down_spcode");
					String downVspcode = rs.getString("down_vspcode");
					int is_advance = rs.getInt("is_advance");
					String regionCode = rs.getString("region_code");
					
					downPort = new DownPort(jdbcTemplate, mobileInfoFlush, mobileRouteFlush,blackFlush);
					downPort.downName(downName).downSpcode(downSpcode).downVspcode(downVspcode)
					.regionCode(regionCode).isAdvance(is_advance==1);

				} catch (Exception e) {
					logger.error("{}",e);
				}
				return downPort;
			}
		});

		for(DownPort downPort : downPortList) {
			downPort.manageDownPort(this);
			Statics.DOWN_PORT_MAP.put(downPort.getDownName(), downPort);
			downPort.loadDownPortChannel();
		}

		logger.info("DOWN_PORT {}",Statics.DOWN_PORT_MAP);
	}

	public void updateDownPort(String downName, String regionCode,
			String downSpcode, String downVspcode, int isAdvance) {
		DownPort downPort = Statics.DOWN_PORT_MAP.get(downName);
		if(downPort!=null){
			downPort.downSpcode(downSpcode).downVspcode(downVspcode)
					.isAdvance(isAdvance==1).regionCode(regionCode);
			downPort.loadDownPortChannel();
		}else{

			downPort = new DownPort(jdbcTemplate, mobileInfoFlush, mobileRouteFlush, blackFlush);
			downPort.downName(downName).downSpcode(downSpcode).downVspcode(downVspcode)
					.regionCode(regionCode).isAdvance(isAdvance==1);

			downPort.manageDownPort(this);
			Statics.DOWN_PORT_MAP.put(downPort.getDownName(), downPort);
			downPort.loadDownPortChannel();
		}

		logger.info("update DOWN_PORT {}", downPort);
	}

	public void removeDownPort(String downName) {
		DownPort downPort = Statics.DOWN_PORT_MAP.remove(downName);
		logger.info("remove DOWN_PORT {}", downPort);
	}
}
