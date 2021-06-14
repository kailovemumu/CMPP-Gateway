package dorox.app.mq.event;

public class PortStatEvent {

    private String messageId;
    private String downName;
    private String upName;
    private String phone;
    private String stat;
    private String msgId;
    private String srcId;
    private String downRegionCode;
    private String seqId;
    private long makeTime = System.currentTimeMillis();

    public PortStatEvent(String messageId, String downName, String upName, String phone, String stat, String msgId, String seqId, String downRegionCode, String srcId) {
        this.messageId = messageId;
        this.downName = downName;
        this.upName = upName;
        this.phone = phone;
        this.stat = stat;
        this.msgId = msgId;
        this.downRegionCode = downRegionCode;
        this.seqId = seqId;
        this.srcId = srcId;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getDownName() {
        return downName;
    }

    public String getPhone() {
        return phone;
    }

    public String getStat() {
        return stat;
    }

    public String getMsgId() {
        return msgId;
    }

    public long getMakeTime() {
        return makeTime;
    }

    public String getDownRegionCode() {
        return downRegionCode;
    }

    public String getSeqId() {
        return seqId;
    }

    public String getSrcId() {
        return srcId;
    }

    public String getUpName() {
        return upName;
    }

    @Override
    public String toString() {
        return "PortStatEvent{" +
                "messageId='" + messageId + '\'' +
                ", downName='" + downName + '\'' +
                ", upName='" + upName + '\'' +
                ", phone='" + phone + '\'' +
                ", stat='" + stat + '\'' +
                ", msgId='" + msgId + '\'' +
                ", srcId='" + srcId + '\'' +
                ", downRegionCode='" + downRegionCode + '\'' +
                ", seqId='" + seqId + '\'' +
                ", makeTime=" + makeTime +
                '}';
    }
}
