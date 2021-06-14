package dorox.app.vo;

import com.zx.sms.codec.sgip12.msg.SgipSubmitRequestMessage;

public class ReSendMsg {
    private String downName;
    private String upName;
    private SgipSubmitRequestMessage sgipSubmitRequestMessage;
    private int timer;

    public ReSendMsg(String downName, String upName, SgipSubmitRequestMessage sgipSubmitRequestMessage,int timer) {
        this.downName=downName;
        this.upName = upName;
        this.sgipSubmitRequestMessage = sgipSubmitRequestMessage;
        this.timer=timer;
    }

    public String getUpName() {
        return upName;
    }

    public SgipSubmitRequestMessage getSgipSubmitRequestMessage() {
        return sgipSubmitRequestMessage;
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
                ", sgipSubmitRequestMessage=" + sgipSubmitRequestMessage +
                ", timer=" + timer +
                '}';
    }
}