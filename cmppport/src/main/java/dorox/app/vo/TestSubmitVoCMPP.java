package dorox.app.vo;


public class TestSubmitVoCMPP {

	private String upPortCode;
	private String messageId;
	private String phone;
	private String srcId;
	private String content;
	private long makeTime;
	private String downPortCode;

	public TestSubmitVoCMPP(String downPortCode, String upPortCode, String messageId,
			String phone, String srcId, String content,long makeTime) {
		this.upPortCode = upPortCode;
		this.messageId = messageId;
		this.phone = phone;
		this.srcId = srcId;
		this.setMakeTime(makeTime);
		this.content = content;
		this.setDownPortCode(downPortCode);
	}

	public String getUpPortCode() {
		return upPortCode;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public long getMakeTime() {
		return makeTime;
	}

	public void setMakeTime(long makeTime) {
		this.makeTime = makeTime;
	}

	public String getDownPortCode() {
		return downPortCode;
	}

	public void setDownPortCode(String downPortCode) {
		this.downPortCode = downPortCode;
	}
}
