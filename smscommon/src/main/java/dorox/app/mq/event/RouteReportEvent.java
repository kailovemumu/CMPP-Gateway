package dorox.app.mq.event;

import java.util.List;

public class RouteReportEvent {

    private String messageId;
    private String upName;
    private int carrier;
    private String cityCode;
    private String city;
    private String province;
    private String provinceCode;
    private int msgCount;
    private String upRegionCode;
    private String upSrcId;
    private long makeTime = System.currentTimeMillis();

    public RouteReportEvent(String messageId, String upName, String upRegionCode, int carrier, String cityCode, String city,
                            String provinceCode, String province, int msgCount, String upSrcId) {
        this.messageId = messageId;
        this.upName = upName;
        this.upRegionCode=upRegionCode;
        this.carrier = carrier;
        this.cityCode = cityCode;
        this.city = city;
        this.province = province;
        this.provinceCode = provinceCode;
        this.msgCount = msgCount;
        this.upSrcId=upSrcId;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getUpName() {
        return upName;
    }

    public int getCarrier() {
        return carrier;
    }

    public String getCityCode() {
        return cityCode;
    }

    public String getCity() {
        return city;
    }

    public String getProvince() {
        return province;
    }

    public String getProvinceCode() {
        return provinceCode;
    }

    public int getMsgCount() {
        return msgCount;
    }

    public long getMakeTime() {
        return makeTime;
    }

    public String getUpRegionCode() {
        return upRegionCode;
    }

    public String getUpSrcId() { return upSrcId;  }

    @Override
    public String toString() {
        return "RouteReportEvent{" +
                "messageId='" + messageId + '\'' +
                ", upName='" + upName + '\'' +
                ", carrier=" + carrier +
                ", cityCode='" + cityCode + '\'' +
                ", city='" + city + '\'' +
                ", province='" + province + '\'' +
                ", provinceCode='" + provinceCode + '\'' +
                ", msgCount=" + msgCount +
                ", upRegionCode='" + upRegionCode + '\'' +
                ", upSrcId='" + upSrcId + '\'' +
                ", makeTime=" + makeTime +
                '}';
    }
}
