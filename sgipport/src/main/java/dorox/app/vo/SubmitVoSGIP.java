package dorox.app.vo;

import java.util.List;

public class SubmitVoSGIP {

    private String upPortCode;
    private String downPortCode;
    private String messageId;
    private String phone;
    private String srcId;
    private List<String> msgIds;

    public SubmitVoSGIP(String upPortCode, String downPortCode,
                        String messageId, String phone, String srcId,List<String> msgIds) {
        this.upPortCode=upPortCode;this.downPortCode=downPortCode;
        this.setMessageId(messageId);
        this.phone=phone;
        this.srcId=srcId;
        this.msgIds=msgIds;
    }

    public String getUpPortCode() {
        return upPortCode;
    }

    public String getDownPortCode() {
        return downPortCode;
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

    public List<String> getMsgIds() {
        return msgIds;
    }

    @Override
    public String toString() {
        return "SubmitVoSGIP{" +
                "upPortCode='" + upPortCode + '\'' +
                ", downPortCode='" + downPortCode + '\'' +
                ", messageId='" + messageId + '\'' +
                ", phone='" + phone + '\'' +
                ", srcId='" + srcId + '\'' +
                ", msgIds=" + msgIds +
                '}';
    }
}
