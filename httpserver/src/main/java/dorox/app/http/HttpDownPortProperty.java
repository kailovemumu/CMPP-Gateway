package dorox.app.http;

/**
 * * cmpp客户端口属性
 */
public class HttpDownPortProperty {

	private String downName;
	private String downPassword;
	private String downType;
	private String regionCode;
	private String whiteIp;
	private boolean pushResult;;
	private String statUrl;
	private String moUrl;
	
	public String getDownName() {
		return downName;
	}
	public void setDownName(String downName) {
		this.downName = downName;
	}
	public String getDownPassword() {
		return downPassword;
	}
	public void setDownPassword(String downPassword) {
		this.downPassword = downPassword;
	}

	public String getDownType() {
		return downType;
	}
	public void setDownType(String downType) {
		this.downType = downType;
	}
	public String getRegionCode() {
		return regionCode;
	}
	public void setRegionCode(String regionCode) {
		this.regionCode = regionCode;
	}
	public String getWhiteIp() {
		return whiteIp;
	}
	public void setWhiteIp(String whiteIp) {
		this.whiteIp = whiteIp;
	}

	@Override
	public String toString() {
		return "HttpDownPortProperty{" +
				"downName='" + downName + '\'' +
				", downPassword='" + downPassword + '\'' +
				", downType='" + downType + '\'' +
				", regionCode='" + regionCode + '\'' +
				", whiteIp='" + whiteIp + '\'' +
				", pushResult=" + pushResult +
				", statUrl='" + statUrl + '\'' +
				", moUrl='" + moUrl + '\'' +
				'}';
	}

	public boolean isPushResult() {
		return pushResult;
	}

	public void setPushResult(boolean pushResult) {
		this.pushResult = pushResult;
	}

	public String getStatUrl() {
		return statUrl;
	}

	public void setStatUrl(String statUrl) {
		this.statUrl = statUrl;
	}

	public String getMoUrl() {
		return moUrl;
	}

	public void setMoUrl(String moUrl) {
		this.moUrl = moUrl;
	}
}
