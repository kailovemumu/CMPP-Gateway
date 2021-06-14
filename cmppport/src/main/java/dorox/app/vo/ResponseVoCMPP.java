package dorox.app.vo;

public class ResponseVoCMPP {

	private String msgId;
	private long result;
	private String seqNo;
	private String messageId;

	public ResponseVoCMPP(String msgId, long result,String seqNo,String messageId) {
		this.msgId = msgId;this.result = result; this.seqNo=seqNo;this.messageId=messageId;
	}

	public long getResult() {
		return result;
	}

	public void setResult(long result) {
		this.result = result;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public String getSeqNo() {
		return seqNo;
	}

	public void setSeqNo(String seqNo) {
		this.seqNo = seqNo;
	}


	public String getMessageId() {
		return messageId;
	}
}
