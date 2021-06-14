package dorox.app.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MobileRouteFlush {
//	private static final Logger logger = LoggerFactory.getLogger(MobileRouteFlush.class);
    @Autowired
	private JdbcTemplate jdbcTemplate;

	//携号转网
	private static Map<String,String> MOBILE_ROUTE_MAP = new ConcurrentHashMap<>();
	
	public String get(String phone) {
		return MOBILE_ROUTE_MAP.get(phone);
	}
	public void put(String phone, String upName) {
		MOBILE_ROUTE_MAP.put(phone, upName);
	}
	public void remove(String phone) {
		if(phone !=null)
			MOBILE_ROUTE_MAP.remove(phone);
	}

	/**
	 * 获取通道信息，通道，编码等。
	 */
	public void loadMobileRoute(){
		String sql = "SELECT phone, up_name FROM mobile_route where status=1";
		jdbcTemplate.query(sql, new Object[]{}, new RowMapper<Object>() {
			@Override
			public Object mapRow(ResultSet rs, int arg1) throws SQLException {
				
				String phone = rs.getString("phone");
				String upName = rs.getString("up_name");
				MOBILE_ROUTE_MAP.put(phone,upName);
				return null;
			}
		});
	}
	public boolean containsKey(String phone) {
		if(phone == null) {
			return false;
		}
		return MOBILE_ROUTE_MAP.containsKey(phone);
	}
}
