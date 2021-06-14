package dorox.app.mq.event;

public class PortMoEvent {

    private String downName;
    private String phone;
    private String content;
    private String destId;
    private String downRegionCode;
    private long makeTime = System.currentTimeMillis();

    public PortMoEvent( String downName, String phone, String content, String destId, String downRegionCode) {
        this.downName = downName;
        this.phone = phone;
        this.content = content;
        this.destId = destId;
        this.downRegionCode = downRegionCode;
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

    public String getDestId() {
        return destId;
    }

    public long getMakeTime() {
        return makeTime;
    }

    public String getDownRegionCode() {
        return downRegionCode;
    }

    @Override
    public String toString() {
        return "PortMoEvent{" +
                "downName='" + downName + '\'' +
                ", phone='" + phone + '\'' +
                ", content='" + content + '\'' +
                ", destId='" + destId + '\'' +
                ", downRegionCode='" + downRegionCode + '\'' +
                ", makeTime=" + makeTime +
                '}';
    }
}
