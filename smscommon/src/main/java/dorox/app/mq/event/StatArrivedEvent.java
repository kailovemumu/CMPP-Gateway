package dorox.app.mq.event;

public class StatArrivedEvent {

    private String messageId;
    private String msgId;
    private String stat;
    private long makeTime = System.currentTimeMillis();

    public StatArrivedEvent(String messageId, String msgId, String stat) {
        this.messageId = messageId;
        this.msgId = msgId;
        this.stat = stat;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getMsgId() {
        return msgId;
    }

    public String getStat() {
        return stat;
    }

    public long getMakeTime() {
        return makeTime;
    }

    @Override
    public String toString() {
        return "StatArrivedEvent{" +
                "messageId='" + messageId + '\'' +
                ", msgId='" + msgId + '\'' +
                ", stat='" + stat + '\'' +
                ", makeTime=" + makeTime +
                '}';
    }
}
