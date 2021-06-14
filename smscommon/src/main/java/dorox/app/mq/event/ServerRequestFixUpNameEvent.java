package dorox.app.mq.event;

import org.apache.avro.util.Utf8;

import java.util.List;

public class ServerRequestFixUpNameEvent {

    private String messageId;
    private String downName;
    private String phone;
    private String content;
    private String srcId;
    private String upSrcId;
    private List<Utf8> msgIds;
    private String downRegionCode;
    private String upRegionCode;
    private String upName;
    private long makeTime = System.currentTimeMillis();

    public ServerRequestFixUpNameEvent(String messageId, String downName, String upName, String phone, String content, String srcId, String upSrcId, List<Utf8> msgIds,
                             String downRegionCode, String upRegionCode) {
        this.messageId=messageId;
        this.downName=downName;
        this.phone=phone;
        this.content=content;
        this.srcId=srcId;
        this.upSrcId=upSrcId;
        this.msgIds=msgIds;
        this.downRegionCode=downRegionCode;
        this.upRegionCode=upRegionCode;
        this.upName=upName;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getDownName() {
        return downName;
    }

    public String getUpName() {
        return upName;
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

    public List<Utf8> getMsgIds() {
        return msgIds;
    }

    public String getDownRegionCode() {
        return downRegionCode;
    }

    public String getUpRegionCode() {
        return upRegionCode;
    }

    public long getMakeTime() {
        return makeTime;
    }

    public String getUpSrcId() {
        return upSrcId;
    }

    @Override
    public String toString() {
        return "RouteRequestEvent{" +
                "messageId='" + messageId + '\'' +
                ", downName='" + downName + '\'' +
                ", phone='" + phone + '\'' +
                ", content='" + content + '\'' +
                ", srcId='" + srcId + '\'' +
                ", upSrcId='" + upSrcId + '\'' +
                ", msgIds=" + msgIds +
                ", downRegionCode='" + downRegionCode + '\'' +
                ", upRegionCode='" + upRegionCode + '\'' +
                ", upName='" + upName + '\'' +
                ", makeTime=" + makeTime +
                '}';
    }
}
