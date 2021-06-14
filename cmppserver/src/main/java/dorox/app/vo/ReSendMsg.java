package dorox.app.vo;

import com.zx.sms.codec.cmpp.msg.Message;

public class ReSendMsg {
    private String downName;
    private Message message;

    public ReSendMsg(String downName,   Message message ) {
        this.downName = downName;
        this.message = message;
    }

    public String getDownName() {
        return downName;
    }

    public Message getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ReSendMsg{" + "downName='" + downName + ", message=" + message + "}";
    }
}
