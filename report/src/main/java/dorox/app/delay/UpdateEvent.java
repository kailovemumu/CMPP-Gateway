package dorox.app.delay;

import dorox.app.mq.event.*;

import java.util.Set;

public class UpdateEvent {

    private Set<RouteStatEvent> routeStatEventSet;
    private Set<PortStatEvent> portStatEventSet;
    private Set<StatArrivedEvent> statArrivedEventSet;
    private RouteReportEvent routeReportEvent;

    public Set<RouteStatEvent> getRouteStatEventSet() {
        return routeStatEventSet;
    }

    public void setRouteStatEventSet(Set<RouteStatEvent> routeStatEventSet) {
        this.routeStatEventSet = routeStatEventSet;
    }

    public Set<PortStatEvent> getPortStatEventSet() {
        return portStatEventSet;
    }

    public void setPortStatEventSet(Set<PortStatEvent> portStatEventSet) {
        this.portStatEventSet = portStatEventSet;
    }

    public Set<StatArrivedEvent> getStatArrivedEventSet() {
        return statArrivedEventSet;
    }

    public void setStatArrivedEventSet(Set<StatArrivedEvent> statArrivedEventSet) { this.statArrivedEventSet = statArrivedEventSet; }

    public RouteReportEvent getRouteReportEvent() {
        return routeReportEvent;
    }

    public void setRouteReportEvent(RouteReportEvent routeReportEvent) {
        this.routeReportEvent = routeReportEvent;
    }

    @Override
    public String toString() {
        return "UpdateEvent{" +
                "routeStatEventSet=" + routeStatEventSet +
                ", portStatEventSet=" + portStatEventSet +
                ", statArrivedEventSet=" + statArrivedEventSet +
                ", routeReportEvent=" + routeReportEvent +
                '}';
    }
}
