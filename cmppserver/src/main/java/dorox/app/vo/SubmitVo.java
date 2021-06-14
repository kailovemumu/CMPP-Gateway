package dorox.app.vo;

import com.zx.sms.common.util.MsgId;

import java.util.List;

/**
 * 仅仅用于客户回执，长短信的回执策略应该是通道模块处理。
 *
 */
public class SubmitVo {

	private String msgId;
	private String phone;
	private String srcId;
	private String messageId;

	public SubmitVo(){}

	public SubmitVo(String messageId, String phone, String srcId, String msgId) {
		this.msgId=msgId;
		this.messageId=messageId;
		this.srcId=srcId;
		this.phone=phone;
	}


	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
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

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
}
