package dorox.app.vo;

import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.codec.sgip12.msg.SgipSubmitRequestMessage;

public class ReSendMsg {
    private String downName;
    private String upName;
    private CmppSubmitRequestMessage cmppSubmitRequestMessage;
    private int timer;

    public ReSendMsg(String downName, String upName, CmppSubmitRequestMessage cmppSubmitRequestMessage, int timer) {
        this.downName = downName;
        this.upName = upName;
        this.cmppSubmitRequestMessage = cmppSubmitRequestMessage;
        this.timer=timer;
    }

    public String getUpName() {
        return upName;
    }

    public CmppSubmitRequestMessage getCmppSubmitRequestMessage() {
        return cmppSubmitRequestMessage;
    }

    public String getDownName() {
        return downName;
    }
    public int getTimer() {
        return timer;
    }

    @Override
    public String toString() {
        return "ReSendMsg{" +
                "downName='" + downName + '\'' +
                ", upName='" + upName + '\'' +
                ", cmppSubmitRequestMessage=" + cmppSubmitRequestMessage +
                ", timer=" + timer +
                '}';
    }
}
