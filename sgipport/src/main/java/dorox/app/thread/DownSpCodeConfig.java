package dorox.app.thread;

import dorox.app.util.Statics;
import dorox.app.vo.SpVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;

/**
 * 上游查询扩展位对应哪个下游
 */
public class DownSpCodeConfig extends Thread{
	private static final Logger logger = LoggerFactory.getLogger(DownSpCodeConfig.class);
    private JdbcTemplate jdbcTemplate;
	
	public DownSpCodeConfig(JdbcTemplate jdbcTemplate){
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void run() {
		try {
			downSpCodeConfig();
			downRegionCodeConfig();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void downSpCodeConfig(){
		String sql = "SELECT down.down_name, up.up_spcode, down.down_spcode, down.down_vspcode, "
				+ "down.status status, down.del_flag del_flag, ch.status status1, ch.del_flag del_flag1 "
				+ "FROM down_port down "
				+ "LEFT JOIN channel ch on down.down_name = ch.down_port_code "
				+ "LEFT JOIN up_port up on ch.up_port_code=up.up_name "
				//+ "where down.status = 1 and down.del_flag = 0 and ch.status=1 and ch.del_flag = 0"
				;
		jdbcTemplate.query(sql, new Object[]{}, new RowMapper<Object>() {
			@Override
			public Object mapRow(ResultSet rs, int arg1) throws SQLException {
				
				String down_port_code = rs.getString("down_name");
				String down_spcode = rs.getString("down_spcode");
				String up_spcode = rs.getString("up_spcode");
				String down_vspcode = rs.getString("down_vspcode");
				if(StringUtils.isBlank(down_vspcode)){
					down_vspcode = "";
				}
				if(StringUtils.isBlank(up_spcode)){
					up_spcode = "";
				}
				if(StringUtils.isBlank(down_spcode)){
					down_spcode = "";
				}
				
				String status = rs.getString("status");
				String status1 = rs.getString("status1");
				String del_flag = rs.getString("del_flag");
				String del_flag1 = rs.getString("del_flag1");
				if("1".equals(status) && "1".equals(status1) && 
						"0".equals(del_flag) && "0".equals(del_flag1)){

					HashSet<SpVo> spCodeSet = Statics.SP_CODE_MAP.get(down_port_code);
					if(spCodeSet == null) {
						spCodeSet = new HashSet<SpVo>();
						Statics.SP_CODE_MAP.put(down_port_code, spCodeSet);
					}
					SpVo sp = new SpVo();
					sp.setSpCode(up_spcode + down_spcode);
					sp.setUpSpCode(up_spcode);
					sp.setVspCode(down_vspcode);
					spCodeSet.add(sp);
					
				}else{
					HashSet<SpVo> spVoList = Statics.SP_CODE_MAP.get(down_port_code);
					if(spVoList != null) {
						Iterator<SpVo> iterator = spVoList.iterator();
						while(iterator.hasNext()){
							SpVo sp = iterator.next();
							if((up_spcode + down_spcode).equals(sp.getSpCode())){
								iterator.remove();
							}
						}
					}
				}
				return null;
			}
		});
		logger.info("SP_CODE_MAP:{}", Statics.SP_CODE_MAP);
		
	}

	private void downRegionCodeConfig(){

		String sql = "SELECT down.down_name, down.region_code FROM down_port down ";
		jdbcTemplate.query(sql, new Object[]{}, new RowMapper<Object>() {
			@Override
			public Object mapRow(ResultSet rs, int arg1) throws SQLException {

				String down_port_code = rs.getString("down_name");
				String region_code = rs.getString("region_code");

				Statics.DOWN_REGION_CODE_MAP.put(down_port_code, region_code);
				return null;
			}
		});

		logger.info("DOWN_REGION_CODE_MAP:{}", Statics.DOWN_REGION_CODE_MAP);

	}
}
