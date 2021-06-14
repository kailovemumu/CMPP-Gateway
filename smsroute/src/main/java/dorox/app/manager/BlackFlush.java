package dorox.app.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BlackFlush {

    @Autowired
	private JdbcTemplate jdbcTemplate;
	
	private static ConcurrentHashMap<String,Integer> BLACK_MAP = new ConcurrentHashMap<>();
	
	public Integer get(String phone) {
		return BLACK_MAP.get(phone);
	}
	public void put(String phone, int isBlack) {
		BLACK_MAP.put(phone, isBlack);
	}

	public void loadBlacks(){
		String sql = "SELECT phone FROM black_phone where status = 1";
		jdbcTemplate.query(sql, new Object[]{}, new RowMapper<Object>() {
			@Override
			public Object mapRow(ResultSet rs, int arg1) throws SQLException {
				
				String phone = rs.getString("phone");
//				key:phone value:1
				BLACK_MAP.put(phone,1);
				return null;
			}
		});
	}

}
