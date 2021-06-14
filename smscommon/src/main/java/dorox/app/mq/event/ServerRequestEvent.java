package dorox.app.mq.event;

import java.util.List;

public final class ServerRequestEvent {

    private String messageId;
    private String downName;
    private String phone;
    private String content;
    private String srcId;
    private List<String> msgIds;
    private String downRegionCode;
    private long makeTime = System.currentTimeMillis();

    public ServerRequestEvent(String messageId, String downName, String phone, String content, String srcId, List<String> msgIds, String regionCode) {
        this.messageId=messageId;
        this.downName=downName;
        this.phone=phone;
        this.content=content;
        this.srcId=srcId;
        this.msgIds=msgIds;
        this.downRegionCode=regionCode;
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

    public String getContent() {
        return content;
    }

    public String getSrcId() {
        return srcId;
    }

    public List<String> getMsgIds() {
        return msgIds;
    }

    public String getDownRegionCode() {
        return downRegionCode;
    }

    public long getMakeTime() {
        return makeTime;
    }

    @Override
    public String toString() {
        return "ServerRequestEvent{" +
                "messageId='" + messageId + '\'' +
                ", downName='" + downName + '\'' +
                ", phone='" + phone + '\'' +
                ", content='" + content + '\'' +
                ", srcId='" + srcId + '\'' +
                ", msgIds=" + msgIds +
                ", downRegionCode='" + downRegionCode + '\'' +
                ", makeTime=" + makeTime +
                '}';
    }
}
