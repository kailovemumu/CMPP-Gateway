package dorox.app.thread;

import dorox.app.ReportMain;
import dorox.app.delay.UpdateEvent;
import dorox.app.delay.UpdateReportDelay;
import dorox.app.mq.event.PortStatEvent;
import dorox.app.mq.event.RouteReportEvent;
import dorox.app.mq.event.RouteStatEvent;
import dorox.app.mq.event.StatArrivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class ReportAggregation implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ReportAggregation.class);
    private BlockingQueue<Object> queue;

    public ReportAggregation(BlockingQueue<Object> queue) {
        this.queue=queue;
    }

    /**
     * */
    @Override
    public void run() {

        while (true) {
            try {
                Object obj = queue.take();
                addUpdateReportMap(obj);
            } catch (Exception e) {
                logger.error("Exception: {}", e.toString());
            }
        }
    }

    /*线程不安全，需单线程执行，从一个*/
    public void addUpdateReportMap(Object t) {
        if( t instanceof  StatArrivedEvent) {
            StatArrivedEvent statArrivedEvent = (StatArrivedEvent) t;
            ReportMain.DB_UPDATE_DELAY_QUEUE.put(new UpdateReportDelay(statArrivedEvent.getMessageId(), ReportMain.REPORT_STAT_ARRIVED_DALAY));
            UpdateEvent updateEvent = ReportMain.UPDATE_REPORT_MAP.get(statArrivedEvent.getMessageId());
            Set<StatArrivedEvent> set;
            if (updateEvent == null) {
                updateEvent = new UpdateEvent();
                set = new HashSet<>();
                updateEvent.setStatArrivedEventSet(set);
                ReportMain.UPDATE_REPORT_MAP.put(statArrivedEvent.getMessageId(), updateEvent);
            } else if (updateEvent.getStatArrivedEventSet() == null) {
                set = new HashSet<>();
                updateEvent.setStatArrivedEventSet(set);
            } else {
                set = updateEvent.getStatArrivedEventSet();
            }
            set.add(statArrivedEvent);
        }else if (t instanceof  RouteStatEvent) {
            RouteStatEvent routeStatEvent = (RouteStatEvent) t;
            ReportMain.DB_UPDATE_DELAY_QUEUE.put(new UpdateReportDelay(routeStatEvent.getMessageId(), ReportMain.REPORT_ROUTE_STAT_DALAY));
            UpdateEvent updateEvent = ReportMain.UPDATE_REPORT_MAP.get(routeStatEvent.getMessageId());
            Set<RouteStatEvent> set;
            if (updateEvent == null) {
                updateEvent = new UpdateEvent();
                set = new HashSet<>();
                updateEvent.setRouteStatEventSet(set);
                ReportMain.UPDATE_REPORT_MAP.put(routeStatEvent.getMessageId(), updateEvent);
            } else if (updateEvent.getRouteStatEventSet() == null) {
                set = new HashSet<>();
                updateEvent.setRouteStatEventSet(set);
            } else {
                set = updateEvent.getRouteStatEventSet();
            }
            set.add(routeStatEvent);
        }else if(t instanceof  PortStatEvent){
            PortStatEvent portStatEvent = (PortStatEvent) t;
            ReportMain.DB_UPDATE_DELAY_QUEUE.put(new UpdateReportDelay(portStatEvent.getMessageId(), ReportMain.REPORT_PORT_STAT_DALAY));
            UpdateEvent updateEvent = ReportMain.UPDATE_REPORT_MAP.get(portStatEvent.getMessageId());
            Set<PortStatEvent> set;
            if(updateEvent == null) {
                updateEvent = new UpdateEvent();
                set = new HashSet<>();
                updateEvent.setPortStatEventSet(set);
                ReportMain.UPDATE_REPORT_MAP.put(portStatEvent.getMessageId(), updateEvent);
            } else if(updateEvent.getPortStatEventSet() == null){
                set = new HashSet<>();
                updateEvent.setPortStatEventSet(set);
            } else {
                set = updateEvent.getPortStatEventSet();
            }
            set.add(portStatEvent);
        }else if( t instanceof  RouteReportEvent){
            RouteReportEvent routeReportEvent = (RouteReportEvent)t;
            ReportMain.DB_UPDATE_DELAY_QUEUE.put(new UpdateReportDelay(routeReportEvent.getMessageId(), ReportMain.REPORT_ROUTE_REPORT_DALAY));
            UpdateEvent updateEvent = ReportMain.UPDATE_REPORT_MAP.get(routeReportEvent.getMessageId());
            if(updateEvent==null){
                updateEvent = new UpdateEvent();
                ReportMain.UPDATE_REPORT_MAP.put(routeReportEvent.getMessageId(), updateEvent);
            }
            updateEvent.setRouteReportEvent(routeReportEvent);
        }
    }



}