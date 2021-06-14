package dorox.app.sgip;

/**
 * * cmpp客户端口属性
 */
public class SgipUpPortProperty {

	private String upName;
	private String upPassword;
	private int upPort;
	private String upIp;
	private String upSpCode;
	private short channelNum;
	private String upType;
	private String upRegionCode;
	private long nodeId;
	private int sgipRecePort;


	public long getNodeId() {
		return nodeId;
	}

	public void setNodeId(long nodeId) {
		this.nodeId = nodeId;
	}

	public int getSgipRecePort() {
		return sgipRecePort;
	}

	public void setSgipRecePort(int sgipRecePort) {
		this.sgipRecePort = sgipRecePort;
	}

	public String getUpName() {
		return upName;
	}

	public void setUpName(String upName) {
		this.upName = upName;
	}

	public String getUpPassword() {
		return upPassword;
	}

	public void setUpPassword(String upPassword) {
		this.upPassword = upPassword;
	}

	public int getUpPort() {
		return upPort;
	}

	public void setUpPort(int upPort) {
		this.upPort = upPort;
	}

	public String getUpIp() {
		return upIp;
	}

	public void setUpIp(String upIp) {
		this.upIp = upIp;
	}

	public String getUpSpCode() {
		return upSpCode;
	}

	public void setUpSpCode(String upSpCode) {
		this.upSpCode = upSpCode;
	}

	public short getChannelNum() {
		return channelNum;
	}

	public void setChannelNum(short channelNum) {
		this.channelNum = channelNum;
	}

	public String getUpType() {
		return upType;
	}

	public void setUpType(String upType) {
		this.upType = upType;
	}

	public String getUpRegionCode() {
		return upRegionCode;
	}

	public void setUpRegionCode(String upRegionCode) {
		this.upRegionCode = upRegionCode;
	}
}
