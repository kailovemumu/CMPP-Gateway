package dorox.app.delay;

import com.google.common.primitives.Ints;
import dorox.app.mq.event.ServerRequestEvent;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class ServerRequestReportDelay implements Delayed {
    private ServerRequestEvent serverRequestEvent;
    private long startTime;

    @Override
    public long getDelay(TimeUnit unit) {
        long diff = startTime - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        return Ints.saturatedCast(
                this.startTime - ((ServerRequestReportDelay) o).startTime);
    }
    public ServerRequestReportDelay(ServerRequestEvent serverRequestEvent, long delayInMilliseconds) {
        this.serverRequestEvent = serverRequestEvent;
        this.startTime = System.currentTimeMillis() + delayInMilliseconds;
    }

    public ServerRequestEvent getServerRequestEvent() {
        return serverRequestEvent;
    }

    @Override
    public String toString() {
        return "ServerRequestReportDelay{" +
                "serverRequestEvent=" + serverRequestEvent +
                ", startTime=" + startTime +
                '}';
    }
}
