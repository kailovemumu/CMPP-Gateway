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
public class PortabilityNumberFlush {
//	private static final Logger logger = LoggerFactory.getLogger(PortabilityNumberFlush.class);
    @Autowired
	private JdbcTemplate jdbcTemplate;

	//携号转网
	private static Map<String,Integer> PORTABILITY_NUMBER_MAP = new ConcurrentHashMap<>();
	
	public Integer get(String phone) {
		return PORTABILITY_NUMBER_MAP.get(phone);
	}
	public void put(String phone, int carrier) {
		PORTABILITY_NUMBER_MAP.put(phone, carrier);
	}
	public void remove(String phone) {
		if(phone !=null)
			PORTABILITY_NUMBER_MAP.remove(phone);
	}

	/**
	 * 获取号码和运营商
	 */
	public void loadPortabilityNumber(){
		String sql = "SELECT phone, carrier FROM portability_number where status=1 and del_flag=0";
		jdbcTemplate.query(sql, new Object[]{}, new RowMapper<Object>() {
			@Override
			public Object mapRow(ResultSet rs, int arg1) throws SQLException {
				
				String phone = rs.getString("phone");
				int carrier = rs.getInt("carrier");
				PORTABILITY_NUMBER_MAP.put(phone,carrier);
				return null;
			}
		});
	}
	public boolean containsKey(String phone) {
		if(phone == null) {
			return false;
		}
		return PORTABILITY_NUMBER_MAP.containsKey(phone);
	}
}
