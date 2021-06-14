package dorox.app.port;

import dorox.app.manager.BlackFlush;
import dorox.app.manager.ManageDownPort;
import dorox.app.manager.MobileInfoFlush;
import dorox.app.manager.MobileRouteFlush;
import dorox.app.port.check.*;
import dorox.app.util.Statics;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class DownPort extends DownPortProperty {

	//端口管理
	private ManageDownPort manageDownPort;

    private JdbcTemplate jdbcTemplate;

    private BlackFlush blackFlush;

	private ChannelExistCheck channelExistCheck;

	public ChannelExistCheck getChannelCheck() {
		return channelExistCheck;
	}
	//路由
	private SmsRoute smsRoute;

	public DownPort(JdbcTemplate jdbcTemplate, MobileInfoFlush mobileInfoFlush,
					MobileRouteFlush mobileRouteFlush, BlackFlush blackFlush) {
		this.jdbcTemplate=jdbcTemplate;
		this.smsRoute = new SmsRoute();
		this.channelExistCheck = new ChannelExistCheck(mobileInfoFlush, mobileRouteFlush);
		this.blackFlush=blackFlush;
	}

	public DownPort manageDownPort(ManageDownPort manageDownPort){
		this.manageDownPort=manageDownPort;
		return this;
	}

	public DownPort downName(String downName) {
		setDownName(downName);
		return this;
	}

	public DownPort downSpcode(String downSpcode) {
		setDownSpcode(downSpcode);
		return this;
	}

	public DownPort downVspcode(String downVspcode) {
		setDownVspcode(downVspcode);
		return this;
	}

	public DownPort isAdvance(boolean isAdvance) {
		setAdvance(isAdvance);
		return this;
	}

	public DownPort regionCode(String regionCode) {
		setRegionCode(regionCode);
		return this;
	}

	public void loadDownPortChannel() {

		smsRoute.removeDownPortChannel();

		String channelSQL = "SELECT ch.allow, inw.intercept_word, ch.id, ch.down_port_code, ch.sign," +
				"ch.up_port_code, ch.carrier, ch.city_code, ch.speed, " +
				"ch.phone_allow_in_days, ch.phone_allow_in_hours, ch.phone_allow_in_minutes, " +
				"ch.is_market_intercept,ch.is_black,up.up_spcode,up.up_type,up.region_code " +
				"FROM channel ch " +
				"left join up_port up on ch.up_port_code = up.up_name " +
				"left join intercept_word inw on ch.intercept_word_id = inw.id and inw.status=1 " +
				"left join down_port down on ch.down_port_code = down.down_name " +
				"where ch.status = 1 and ch.del_flag = 0 and down.down_name='"+getDownName()+"'";

		jdbcTemplate.query(channelSQL, new Object[]{}, new RowMapper<Object>() {
			@Override
			public Object mapRow(ResultSet rs, int arg1) throws SQLException {

				String upPortCode = rs.getString("up_port_code");
				String carrier = rs.getString("carrier");
				String channelId = rs.getString("id");
				//这里实际应该是province_code，多个用逗号分隔
				String cityCode = rs.getString("city_code");
				String upSpcode = rs.getString("up_spcode");
				String upRegionCode = rs.getString("region_code");
				String speed = rs.getString("speed");
				String interceptWord = rs.getString("intercept_word");
				String allow = rs.getString("allow");
				String upType = rs.getString("up_type");
				String sign = rs.getString("sign");
				int isBlack = rs.getInt("is_black");
				int isMarketIntercept = rs.getInt("is_market_intercept");

				int phoneAlloInDays = rs.getInt("phone_allow_in_days");
				int phoneAlloInHours = rs.getInt("phone_allow_in_hours");
				int phoneAlloInMinutes = rs.getInt("phone_allow_in_minutes");

				if(cityCode != null){
					String citys[] = cityCode.split(",");
					for(String city : citys){
						String key = getDownName() + carrier + city;
						addChannel(key,getDownName(),upPortCode,carrier,
								channelId,upSpcode,getRegionCode(), upRegionCode,speed,interceptWord,
								allow,upType,isBlack,isMarketIntercept,
								phoneAlloInDays,phoneAlloInHours,phoneAlloInMinutes,sign);
					}
				}

				return null;
			}
		});

		smsRoute.routeSpeedWeightConfig();

	}

	//增加路由通道
	//增加通道权重
	public void addChannel(String key, String downName, String upPortCode, String carrier, String channelId,
						   String upSpcode, String downRegionCode, String upRegionCode, String speed, String interceptWord, String allow, String upType,
						   int isBlack, int isMarketIntercept,
						   int phoneAlloInDays,int phoneAlloInHours,int phoneAlloInMinutes,String sign) {

		Channel channel = makeChannel(downName, upPortCode, carrier, channelId,
				upSpcode, downRegionCode, upRegionCode, speed, interceptWord, allow, upType,
				isBlack, isMarketIntercept,phoneAlloInDays,phoneAlloInHours,phoneAlloInMinutes,sign);

		smsRoute.addChannel(key, channel);
	}

	private Channel makeChannel(String downName, String upPortCode, String carrier, String channelId, String upSpcode,
								String downRegionCode, String upRegionCode, String speed, String interceptWord, String allow, String upType, int isBlack,
								int isMarketIntercept,int phoneAlloInDays,int phoneAlloInHours,int phoneAlloInMinutes,String sign) {
		Channel channel = new Channel();
		/*0表示不限制*/
		channel.setCarrier(carrier);

		channel.setDownPortCode(downName);
		channel.setUpPortCode(upPortCode);
		channel.setUpSpcode(upSpcode);
		channel.setSpeed(speed);
		channel.setChannelId(channelId);
		channel.setUpType(upType);
		channel.setIsBlack(isBlack);
		channel.setIsMarketIntercept(isMarketIntercept);
		channel.setDownRegionCode(downRegionCode);
		channel.setUpRegionCode(upRegionCode);

		channel.setPhoneAlloInDays(phoneAlloInDays);
		channel.setPhoneAlloInHours(phoneAlloInHours);
		channel.setPhoneAlloInMinutes(phoneAlloInMinutes);

		if(org.apache.commons.lang3.StringUtils.isNotBlank(sign) && !"null".equals(sign)) {
			channel.setSign(Arrays.asList(sign.split("\\|")));
		}

		/*发送时间控制*/
		if(!StringUtils.isEmpty(allow)){
			String[] t = allow.split("-");
			String[] start = t[0].split(":");
			String[] end = t[1].split(":");
			long s1 = Long.valueOf(start[0]);
			long s2 = Long.valueOf(start[1]);
			long e1 = Long.valueOf(end[0]);
			long e2 = Long.valueOf(end[1]);
			channel.setStartAllowTime(s1*60*60*1000+s2*60*1000);
			channel.setEndAllowTime(e1*60*60*1000+e2*60*1000);
		}

		/*加拦截策略*/
		if(!StringUtils.isEmpty(interceptWord) ){
			List<Pattern> patterns = new ArrayList<>();
			if(!StringUtils.isEmpty(interceptWord)){
				String[] interceptWords = interceptWord.split("#");
				for(String word : interceptWords){
					if(!StringUtils.isEmpty(word)){
						patterns.add(Pattern.compile(word.trim()));
					}
				}
			}
			channel.setPatterns(patterns);
		}

		if(isBlack == 1){
			channel.addCheckHandlers(new BlackCheck(blackFlush));
		}
		if(channel.getPatterns() !=null && channel.getPatterns().size() > 0){
			channel.addCheckHandlers(new InterceptCheck());
		}
		if(channel.getStartAllowTime() > 0 && channel.getEndAllowTime() > 0){
			channel.addCheckHandlers(new TimeControlCheck());
		}
		if(channel.getPhoneAlloInDays() > 0){
			channel.addCheckHandlers(new NumInDayCheck());
		}
		if(channel.getPhoneAlloInHours() > 0){
			channel.addCheckHandlers(new NumInHourCheck());
		}
		if(channel.getPhoneAlloInMinutes() > 0){
			channel.addCheckHandlers(new NumInMinuteCheck());
		}
		if(channel.getSign() !=null && channel.getSign().size() > 0){
			channel.addCheckHandlers(new SignCheck());
		}

		return channel;
	}

	public Channel getChannel(String phone, int carrier, String cityCode, String provinceCode, String downName) {
		return smsRoute.getChannel(phone, carrier, cityCode, provinceCode, downName);
	}

	@Override
	public String toString() {
		return "DownPort{" +
				"manageDownPort=" + manageDownPort +
				", jdbcTemplate=" + jdbcTemplate +
				", channelExistCheck=" + channelExistCheck +
				", smsRoute=" + smsRoute +
				", DownPortProperty=" + super.toString() +
				'}';
	}

}
