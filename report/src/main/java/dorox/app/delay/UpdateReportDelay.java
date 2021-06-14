package dorox.app.delay;

import com.google.common.primitives.Ints;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class UpdateReportDelay implements Delayed {
    private String messageId;
    private long startTime;

    @Override
    public long getDelay(TimeUnit unit) {
        long diff = startTime - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        return Ints.saturatedCast(
                this.startTime - ((UpdateReportDelay) o).startTime);
    }
    public UpdateReportDelay(String messageId, long delayInMilliseconds) {
        this.messageId = messageId;
        this.startTime = System.currentTimeMillis() + delayInMilliseconds;
    }

    public String getMessageId() {
        return messageId;
    }

    @Override
    public String toString() {
        return "UpdateReportDelay{" +
                "messageId='" + messageId + '\'' +
                ", startTime=" + startTime +
                '}';
    }
}
