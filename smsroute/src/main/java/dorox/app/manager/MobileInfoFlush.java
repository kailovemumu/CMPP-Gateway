package dorox.app.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Component
public class MobileInfoFlush  {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private PortabilityNumberFlush portabilityNumberFlush;

	public static final int YIDONG 	= 2;
	public static final int LIANTONG = 3;
	public static final int DIANXIN = 4;


	public static Map<String, Integer> MOBILE_SEGMENT = new HashMap<>();

	public static Map<String, MobileInfo> YIDONG_MOBILE_MAP = new HashMap<>();
	public static Map<String, MobileInfo> LIANTONG_MOBILE_MAP = new HashMap<>();
	public static Map<String, MobileInfo> DIANXIN_MOBILE_MAP = new HashMap<>();

	/**
	 * 通过手机号获取 运营商以及具体的地址
	 * @param phone
	 * @return mobileInfo 城市的具体信息
	 */
	public MobileInfo getCityInfo(String phone) {
		int real = 2;int city = 2;

		for(String segment : MOBILE_SEGMENT.keySet()){
			if(phone.startsWith(segment) ){
				city = MOBILE_SEGMENT.get(segment);
				real = MOBILE_SEGMENT.get(segment);
				break;
			}
		}
		if(portabilityNumberFlush.containsKey(phone)){
			real = portabilityNumberFlush.get(phone);
		}
		MobileInfo mobileInfo = null;
		if(city == 2){//移动
			if(YIDONG_MOBILE_MAP.containsKey(phone.subSequence(0, 7))){
				mobileInfo = YIDONG_MOBILE_MAP.get(phone.subSequence(0, 7));
				mobileInfo.setIsp(real);
			}else if(YIDONG_MOBILE_MAP.containsKey(phone.subSequence(0, 6))){
				mobileInfo = YIDONG_MOBILE_MAP.get(phone.subSequence(0, 6));
				mobileInfo.setIsp(real);
			}else if(YIDONG_MOBILE_MAP.containsKey(phone.subSequence(0, 8))){
				mobileInfo = YIDONG_MOBILE_MAP.get(phone.subSequence(0, 8));
				mobileInfo.setIsp(real);
			}else if(YIDONG_MOBILE_MAP.containsKey(phone.subSequence(0, 5)) ){
				mobileInfo = YIDONG_MOBILE_MAP.get(phone.subSequence(0, 5));
				mobileInfo.setIsp(real);
			}else if(YIDONG_MOBILE_MAP.containsKey(phone.subSequence(0, 4)) ){
				mobileInfo = YIDONG_MOBILE_MAP.get(phone.subSequence(0, 4));
				mobileInfo.setIsp(real);
			}else if(YIDONG_MOBILE_MAP.containsKey(phone.subSequence(0, 11)) ){
				mobileInfo = YIDONG_MOBILE_MAP.get(phone.subSequence(0, 11));
				mobileInfo.setIsp(real);
			}else if(YIDONG_MOBILE_MAP.containsKey(phone.subSequence(0, 1)) ){
				mobileInfo = YIDONG_MOBILE_MAP.get(phone.subSequence(0, 1));
				mobileInfo.setIsp(real);
			}
		}else if (city == 3){//联通
			if(LIANTONG_MOBILE_MAP.containsKey(phone.subSequence(0, 7)) ){
				mobileInfo = LIANTONG_MOBILE_MAP.get(phone.subSequence(0, 7));
				mobileInfo.setIsp(real);
			}else if(LIANTONG_MOBILE_MAP.containsKey(phone.subSequence(0, 6))){
				mobileInfo = LIANTONG_MOBILE_MAP.get(phone.subSequence(0, 6));
				mobileInfo.setIsp(real);
			}else if(LIANTONG_MOBILE_MAP.containsKey(phone.subSequence(0, 8))){
				mobileInfo = LIANTONG_MOBILE_MAP.get(phone.subSequence(0, 8));
				mobileInfo.setIsp(real);
			}else if(LIANTONG_MOBILE_MAP.containsKey(phone.subSequence(0, 5)) ){
				mobileInfo = LIANTONG_MOBILE_MAP.get(phone.subSequence(0, 5));
				mobileInfo.setIsp(real);
			}else if(LIANTONG_MOBILE_MAP.containsKey(phone.subSequence(0, 4)) ){
				mobileInfo = LIANTONG_MOBILE_MAP.get(phone.subSequence(0, 4));
				mobileInfo.setIsp(real);
			}else if(LIANTONG_MOBILE_MAP.containsKey(phone.subSequence(0, 11)) ){
				mobileInfo = LIANTONG_MOBILE_MAP.get(phone.subSequence(0, 11));
				mobileInfo.setIsp(real);
			}else if(LIANTONG_MOBILE_MAP.containsKey(phone.subSequence(0, 1)) ){
				mobileInfo = LIANTONG_MOBILE_MAP.get(phone.subSequence(0, 1));
				mobileInfo.setIsp(real);
			}
		}else{//电信
			if(DIANXIN_MOBILE_MAP.containsKey(phone.subSequence(0, 7)) ){
				mobileInfo = DIANXIN_MOBILE_MAP.get(phone.subSequence(0, 7));
				mobileInfo.setIsp(real);
			}else if(DIANXIN_MOBILE_MAP.containsKey(phone.subSequence(0, 6)) ){
				mobileInfo = DIANXIN_MOBILE_MAP.get(phone.subSequence(0, 6));
				mobileInfo.setIsp(real);
			}else if(DIANXIN_MOBILE_MAP.containsKey(phone.subSequence(0, 8)) ){
				mobileInfo = DIANXIN_MOBILE_MAP.get(phone.subSequence(0, 8));
				mobileInfo.setIsp(real);
			}else if(DIANXIN_MOBILE_MAP.containsKey(phone.subSequence(0, 5)) ){
				mobileInfo = DIANXIN_MOBILE_MAP.get(phone.subSequence(0, 5));
				mobileInfo.setIsp(real);
			}else if(DIANXIN_MOBILE_MAP.containsKey(phone.subSequence(0, 4)) ){
				mobileInfo = DIANXIN_MOBILE_MAP.get(phone.subSequence(0, 4));
				mobileInfo.setIsp(real);
			}else if(DIANXIN_MOBILE_MAP.containsKey(phone.subSequence(0, 11)) ){
				mobileInfo = DIANXIN_MOBILE_MAP.get(phone.subSequence(0, 11));
				mobileInfo.setIsp(real);
			}else if(DIANXIN_MOBILE_MAP.containsKey(phone.subSequence(0, 1)) ){
				mobileInfo = DIANXIN_MOBILE_MAP.get(phone.subSequence(0, 1));
				mobileInfo.setIsp(real);
			}
		}
		if(mobileInfo == null) {
			mobileInfo = new MobileInfo("", "0", "", "0", real);
		}
		return mobileInfo;
	}

	public void loadMobileInfos() {
		getMobileInfo();
		getMobileSegment();
	}

	/**
	 * 取出三个运营商的省 市信息。
	 */
	private void getMobileInfo() {
		String sql = "SELECT * FROM mobile_info ";
		jdbcTemplate.query(sql, new Object[] {}, new RowMapper<Object>() {
			@Override
			public Object mapRow(ResultSet rs, int arg1) throws SQLException {
				String phone = rs.getString("phone");
				String cityCode = rs.getString("city_code");
				String city = rs.getString("city");
				String provinceCode = rs.getString("province_code");
				String province = rs.getString("province");
				int isp = rs.getInt("isp");
				//Map<String, String> map = new HashMap<>();
				MobileInfo info = new MobileInfo(city,cityCode,province,provinceCode,isp);

				if (YIDONG ==(isp) ) {
					YIDONG_MOBILE_MAP.put(phone, info);
				} else if (LIANTONG ==(isp)) {
					LIANTONG_MOBILE_MAP.put(phone, info);
				} else {
					DIANXIN_MOBILE_MAP.put(phone, info);
				}
				return null;
			}
		});
	}

	/**
	 * 获取运营商和号段的map
	 */
	private void getMobileSegment() {
		String sql = "SELECT * FROM mobile_segment ";
		jdbcTemplate.query(sql, new Object[] {}, new RowMapper<Object>() {
			@Override
			public Object mapRow(ResultSet rs, int arg1) throws SQLException {
				int isp = rs.getInt("isp");
				String segment = rs.getString("segment");
				MOBILE_SEGMENT.put(segment, isp);
				return null;
			}
		});
	}
}