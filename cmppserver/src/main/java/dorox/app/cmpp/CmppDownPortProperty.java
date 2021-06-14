package dorox.app.cmpp;

/**
 * * cmpp客户端口属性
 */
public class CmppDownPortProperty {

	private String downName;
	private String downPassword;
	private String version;
	private short channelNum;
	private String downType;
	private String regionCode;
	private String whiteIp;
	
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
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}

	public short getChannelNum() {
		return channelNum;
	}
	public void setChannelNum(short channelNum) {
		this.channelNum = channelNum;
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
		return "CmppDownPortProperty [downName=" + downName + ", downPassword=" + downPassword + ", version=" + version
				+ ", channelNum=" + channelNum + ", downType=" + downType
				+ ", regionCode=" + regionCode + ", whiteIp=" + whiteIp + "]";
	}
	
}
