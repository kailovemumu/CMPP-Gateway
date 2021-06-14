package dorox.app.vo;

public class ResponseVoSGIP {

	private long result;
	private String seqNo;
	private String messageId;

	public ResponseVoSGIP( long result, String seqNo, String messageId) {
		 this.result = result; this.seqNo=seqNo;this.messageId=messageId;
	}


	@Override
	public String toString() {
		return "ResponseVoSGIP{" +
				"result=" + result +
				", seqNo='" + seqNo + '\'' +
				", messageId='" + messageId + '\'' +
				'}';
	}

	public String getMessageId() {
		return messageId;
	}
}
