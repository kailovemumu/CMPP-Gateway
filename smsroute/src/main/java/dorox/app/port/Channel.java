package dorox.app.port;


import dorox.app.port.check.ChannelCheckHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Channel {

    private String downPortCode;
    private String upPortCode;
    private String carrier;
    private String cityCode;
    private String upSpcode;
    private String speed;
    private List<Pattern> patterns;
    private String channelId;
    private long startAllowTime;
    private long endAllowTime;
    private int isBlack;
    private String upType;
    private int isMarketIntercept;
    private String downRegionCode;
    private String upRegionCode;
    private int phoneAlloInDays;
    private int phoneAlloInHours;
    private int phoneAlloInMinutes;
    private List<String> sign;

    private List<ChannelCheckHandler> checkHandlers = new ArrayList<>();


    public List<String> getSign() {
        return sign;
    }
    public void setSign(List<String> sign) {
        this.sign = sign;
    }

    public List<ChannelCheckHandler> getCheckHandlers() {
        return checkHandlers;
    }

    public String getDownPortCode() {
        return this.downPortCode;
    }

    public void setDownPortCode(String downPortCode) {
        this.downPortCode = downPortCode;
    }

    public String getUpPortCode() {
        return this.upPortCode;
    }

    public void setUpPortCode(String upPortCode) {
        this.upPortCode = upPortCode;
    }

    public String getCarrier() {
        return this.carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getCityCode() {
        return this.cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getUpSpcode() {
        return this.upSpcode;
    }

    public void setUpSpcode(String upSpcode) {
        this.upSpcode = upSpcode;
    }

    public String getSpeed() {
        return this.speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public List<Pattern> getPatterns() {
        return this.patterns;
    }

    public void setPatterns(List<Pattern> patterns) {
        this.patterns = patterns;
    }

    public String getChannelId() {
        return this.channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public long getStartAllowTime() {
        return this.startAllowTime;
    }

    public void setStartAllowTime(long startAllowTime) {
        this.startAllowTime = startAllowTime;
    }

    public long getEndAllowTime() {
        return this.endAllowTime;
    }

    public void setEndAllowTime(long endAllowTime) {
        this.endAllowTime = endAllowTime;
    }

    public int getIsBlack() {
        return this.isBlack;
    }

    public void setIsBlack(int isBlack) {
        this.isBlack = isBlack;
    }

    public String getUpType() {
        return this.upType;
    }

    public void setUpType(String upType) {
        this.upType = upType;
    }

    public int getIsMarketIntercept() {
        return this.isMarketIntercept;
    }

    public void setIsMarketIntercept(int isMarketIntercept) {
        this.isMarketIntercept = isMarketIntercept;
    }

    public String getDownRegionCode() {
        return this.downRegionCode;
    }

    public void setDownRegionCode(String downRegionCode) {
        this.downRegionCode = downRegionCode;
    }

    public String getUpRegionCode() {
        return this.upRegionCode;
    }

    public void setUpRegionCode(String upRegionCode) {
        this.upRegionCode = upRegionCode;
    }

    public int getPhoneAlloInDays() {
        return this.phoneAlloInDays;
    }

    public void setPhoneAlloInDays(int phoneAlloInDays) {
        this.phoneAlloInDays = phoneAlloInDays;
    }

    public int getPhoneAlloInHours() {
        return this.phoneAlloInHours;
    }

    public void setPhoneAlloInHours(int phoneAlloInHours) {
        this.phoneAlloInHours = phoneAlloInHours;
    }

    public int getPhoneAlloInMinutes() {
        return this.phoneAlloInMinutes;
    }

    public void setPhoneAlloInMinutes(int phoneAlloInMinutes) {
        this.phoneAlloInMinutes = phoneAlloInMinutes;
    }

    public void addCheckHandlers(ChannelCheckHandler handler) {
        checkHandlers.add(handler);
    }




    @Override
    public String toString() {
        return "Channel{" +
                "downPortCode='" + downPortCode + '\'' +
                ", upPortCode='" + upPortCode + '\'' +
                ", carrier='" + carrier + '\'' +
                ", cityCode='" + cityCode + '\'' +
                ", upSpcode='" + upSpcode + '\'' +
                ", speed='" + speed + '\'' +
                ", patterns=" + patterns +
                ", channelId='" + channelId + '\'' +
                ", startAllowTime=" + startAllowTime +
                ", endAllowTime=" + endAllowTime +
                ", isBlack=" + isBlack +
                ", upType='" + upType + '\'' +
                ", isMarketIntercept=" + isMarketIntercept +
                ", downRegionCode='" + downRegionCode + '\'' +
                ", upRegionCode='" + upRegionCode + '\'' +
                ", phoneAlloInDays=" + phoneAlloInDays +
                ", phoneAlloInHours=" + phoneAlloInHours +
                ", phoneAlloInMinutes=" + phoneAlloInMinutes +
                ", sign='" + sign + '\'' +
                ", checkHandlers=" + checkHandlers +
                '}';
    }
}