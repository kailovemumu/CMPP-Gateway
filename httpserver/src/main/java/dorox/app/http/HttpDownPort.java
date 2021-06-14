package dorox.app.http;

import dorox.app.manager.ManageHttpDownPort;
import dorox.app.util.Statics;
import org.n3r.idworker.Sid;
import org.springframework.jdbc.core.JdbcTemplate;


public class HttpDownPort extends HttpDownPortProperty {

    private Sid sid;
    private JdbcTemplate jdbcTemplate;
    private String messageIdPrefix;

	public HttpDownPort(JdbcTemplate jdbcTemplate, Sid sid, String messageIdPrefix) {
		this.jdbcTemplate=jdbcTemplate;
	    this.sid=sid;
	    this.messageIdPrefix = messageIdPrefix;
	}

	//CMPP业务属性配置
	public HttpDownPort downName(String downName) {
		setDownName(downName);
		return this;
	}

	public HttpDownPort downPassword(String downPassword) {
		setDownPassword(downPassword);
		return this;
	}

	public HttpDownPort whiteIp(String whiteIp) {
		setWhiteIp(whiteIp);
		return this;
	}

	public HttpDownPort moUrl(String moUrl) {
		setMoUrl(moUrl);
		return this;
	}

	public HttpDownPort statUrl(String statUrl) {
		setStatUrl(statUrl);
		return this;
	}

	public HttpDownPort pushResult(boolean pushResult) {
		setPushResult(pushResult);
		return this;
	}

	public HttpDownPort downType(String downType) {
		setDownType(downType);
		return this;
	}

	public HttpDownPort regionCode(String regionCode) {
		setRegionCode(regionCode);
		return this;
	}

	public void removeDownPort(String downName, String regionCode) {
		Statics.DOWN_PORT_MAP.remove(downName);
	}
	public boolean isWhiteIp(String clientIp) {
		return getWhiteIp()!=null &&
				(getWhiteIp().contains(clientIp) || getWhiteIp().contains("*"));
	}

}
