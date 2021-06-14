package dorox.app.vo;

public class DeliverVoCMPP {

	private String upPortCode;
	private String stat;
	private String msgId;
	private String phone;
	private String srcId;

	public DeliverVoCMPP(String stat, String srcId, String phone,
			String msgId, String upPortCode) {
		this.setStat(stat);
		this.srcId=srcId;
		this.phone=phone;
		this.setMsgId(msgId);
		this.upPortCode=upPortCode;
	}

	public String getUpPortCode() {
		return upPortCode;
	}

	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getSrcId() {
		return srcId;
	}
	public void setSrcId(String srcId) {
		this.srcId = srcId;
	}

	public String getStat() {
		return stat;
	}

	public void setStat(String stat) {
		this.stat = stat;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	@Override
	public String toString() {
		return "DeliverVoCMPP [upPortCode=" + upPortCode + ", stat=" + stat + ", msgId=" + msgId + ", phone=" + phone
				+ ", srcId=" + srcId + "]";
	}
}
