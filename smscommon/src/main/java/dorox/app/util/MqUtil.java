package dorox.app.util;

import dorox.app.mq.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class MqUtil {

    private static final Logger logger = LoggerFactory.getLogger(MqUtil.class);

    public static String getTopic(Object event) {
        if(event instanceof ServerRequestEvent){
            return getServerRequestTopic();
        }else if(event instanceof RouteRequestEvent){
            return getRouteRequestTopic();
        }else if(event instanceof RouteStatEvent){
            return getRouteStatTopic();
        }else if(event instanceof PortStatEvent){
            return getPortStatTopic();
        }else if(event instanceof PortMoEvent){
            return getPortMoTopic();
        }else if(event instanceof RouteReportEvent){
            return getRouteReportTopic();
        }else if(event instanceof StatArrivedEvent){
            return getStatArrivedTopic();
        }else if(event instanceof ServerRequestFixUpNameEvent){
            return getServerRequestFixUpNameTopic();
        }


        return null;
    }

    public static String getServerRequestTopic(){
        return "serverrequest";
    }

    public static String getRouteStatTopic(){
        return "routestat";
    }

    public static String getRouteRequestTopic(){ return "routerequest"; }

    public static String getPortStatTopic(){ return "portstat"; }

    public static String getPortMoTopic(){ return "portmo"; }

    public static String getRouteReportTopic(){ return "routereport";  }

    public static String getStatArrivedTopic(){ return "statarrived";  }

    public static String getServerRequestFixUpNameTopic(){return "serverrequestfixupname";}


    public static <T> void sendMsg(T event, RabbitTemplate rabbitTemplate, String key) {

        byte[] bytes = AvroUtil.getBytes(event);
        rabbitTemplate.convertAndSend(getTopic(event),key,bytes);
        logger.info("send msg:{}", event);
    }
}
