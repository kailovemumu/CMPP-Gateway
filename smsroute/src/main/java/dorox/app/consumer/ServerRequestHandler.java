package dorox.app.consumer;

import com.rabbitmq.client.Channel;
import com.twitter.bijection.Injection;
import dorox.app.manager.MobileInfo;
import dorox.app.mq.event.RouteReportEvent;
import dorox.app.mq.event.RouteRequestEvent;
import dorox.app.mq.event.RouteStatEvent;
import dorox.app.mq.event.ServerRequestEvent;
import dorox.app.port.DownPort;
import dorox.app.port.check.ChannelCheckHandler;
import dorox.app.port.check.CheckResult;
import dorox.app.text.SmsTextMessage;
import dorox.app.util.AvroUtil;
import dorox.app.util.MqUtil;
import dorox.app.util.Statics;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ServerRequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(ServerRequestHandler.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("#{${dianli.srcid}}")
    private Map<String, Map<Integer, String>> dianliSrcIds;

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue(value = "${rabbitmq.serverrequest.queue}",
                            durable="${rabbitmq.queue.durable}"),
                    exchange = @Exchange(value = "${rabbitmq.serverrequest.topic}",
                            durable="${rabbitmq.exchange.durable}",
                            type= "${rabbitmq.exchange.type}",
                            ignoreDeclarationExceptions = "${rabbitmq.exchange.ignoreDeclarationExceptions}"),
                    key = "${rabbitmq.serverrequest.key}"
            )
    })
    @RabbitHandler
    public void handler(@Payload byte[] record, Channel channel,
                        @Headers Map<String, Object> headers) throws Exception {

        Long deliveryTag = (Long)headers.get(AmqpHeaders.DELIVERY_TAG);
        try{
            Injection<GenericRecord, byte[]> recordInjection = AvroUtil.getGenericAvroCodecs(ServerRequestEvent.class);
            GenericRecord serverRequest = recordInjection.invert(record).get();
            logger.info("serverRequest:{}", serverRequest);
            parseServerRequest(serverRequest);
        }catch (Exception e){
            logger.error("exception:{}", e);
        }finally {
            //手工ACK
            channel.basicAck(deliveryTag, false);
        }
    }

    /**
     * 1，根据downName获取
     * */
    private void parseServerRequest(GenericRecord serverRequest) {

        String downName = String.valueOf(serverRequest.get("downName"));
        String messageId = String.valueOf(serverRequest.get("messageId"));
        String phone = String.valueOf(serverRequest.get("phone"));
        String srcId = String.valueOf(serverRequest.get("srcId"));
        List<Utf8> msgIds = (List<Utf8>) serverRequest.get("msgIds");
        String content = String.valueOf(serverRequest.get("content"));
        long makeTime = (Long) serverRequest.get("makeTime");
        String downRegionCode = String.valueOf(serverRequest.get("downRegionCode"));

        int msgCount = msgIds.size();
        DownPort downPort = Statics.DOWN_PORT_MAP.get(downName);

        /*所有check的参数，都可以通过serverRequest获取*/
        Map<String, Object> param = new HashMap<>();
        param.put("serverRequest", serverRequest);
        //根据上面取出的信息找出发送消息对应的通道
        CheckResult checkResult = downPort.getChannelCheck().handler(param);
        //城市。区域信息。
        MobileInfo mobileInfo = ((MobileInfo)checkResult.getResult().get("mobileInfo"));
        if(CheckResult.FAILED.equals(checkResult.getCode())){
            sendErrMsg(messageId, "","", downRegionCode, mobileInfo, msgIds, downName, phone,
                    checkResult.getMessage(), srcId,"");
            return;
        }

        dorox.app.port.Channel channel = ((dorox.app.port.Channel)checkResult.getResult().get("channel"));
        param.put("channel", channel);

        /*解析扩展位*/
        /*电力不要扩展位*/
        /*对电力srcId特殊处理：srcid cmppport -》cmppserver -》 电力 */
        String upSrcId;
        if(dianliSrcIds.containsKey(downName)){
            srcId = dianliSrcIds.get(downName).get(mobileInfo.getIsp());
            content = "【湖北电力】"+content;
            upSrcId = srcId("", channel.getUpSpcode(), downPort.getDownSpcode(), downPort.getDownVspcode());
//            重新计算短信条数
            msgCount = new SmsTextMessage(content).getPdus().length;
        } else{
            upSrcId = srcId(srcId, channel.getUpSpcode(), downPort.getDownSpcode(), downPort.getDownVspcode());
        }
//        校验
        for(ChannelCheckHandler channelCheckHandler : channel.getCheckHandlers()){
            CheckResult result = channelCheckHandler.handler(param);
//            校验失败
            if(CheckResult.FAILED.equals(result.getCode())){
                sendErrMsg(messageId, channel.getUpPortCode(), channel.getUpRegionCode(), downRegionCode, mobileInfo, msgIds, downName, phone,
                        result.getMessage(), srcId, upSrcId);
                return;
            }
        }

        RouteRequestEvent routeRequestEvent = new RouteRequestEvent(messageId, downName, channel.getUpPortCode(), phone, content, srcId, upSrcId, msgIds,
                channel.getDownRegionCode(), channel.getUpRegionCode());
        MqUtil.sendMsg(routeRequestEvent, rabbitTemplate, channel.getUpRegionCode());

        RouteReportEvent routeReportEvent = new RouteReportEvent(messageId, channel.getUpPortCode(), channel.getUpRegionCode(), mobileInfo.getIsp(),
                mobileInfo.getCityCode(), mobileInfo.getCity(),
                mobileInfo.getProvinceCode(), mobileInfo.getProvince(), msgCount, upSrcId);
        MqUtil.sendMsg(routeReportEvent, rabbitTemplate, "report");

    }

    private void sendErrMsg(String messageId, String upPortCode, String upRegionCode, String downRegionCode, MobileInfo mobileInfo,
                            List<Utf8> msgIds, String downName, String phone, String stat, String srcId, String upSrcId) {
        /*生产RouteReport队列消息*/
//        报表
        RouteReportEvent routeReportEvent = new RouteReportEvent(messageId, upPortCode, upRegionCode, mobileInfo.getIsp(),
                mobileInfo.getCityCode(), mobileInfo.getCity(),
                mobileInfo.getProvinceCode(), mobileInfo.getProvince(), msgIds.size(), upSrcId);
        MqUtil.sendMsg(routeReportEvent, rabbitTemplate, "report");

        /*生产RouteStat队列消息*/
        for(Utf8 msgId : msgIds) {
            RouteStatEvent routeStatEvent = new RouteStatEvent(messageId, downName, upPortCode, phone, stat, msgId.toString(), downRegionCode, srcId);
            MqUtil.sendMsg(routeStatEvent, rabbitTemplate,downRegionCode + ".report");
        }
    }

    //客户可以传完整的接入码（上游接入码+平台扩展位+自定义），或者只传平台扩展位+自定义，或者只传自定义
    //拼成完整srcId,传到上游处理
    private String srcId(String srcId, String upSpCode, String downSpCode, String vspcode){

        if( StringUtils.isEmpty(srcId) ){
            srcId = "";
        }
        if( StringUtils.isEmpty(downSpCode)){
            downSpCode = "";
        }
        if( StringUtils.isEmpty(upSpCode)){
            upSpCode = "";
        }
        String res = "";

        //未分配虚拟接入号
        if(StringUtils.isEmpty(vspcode)){
            //如果下游传了完整接入号
            if(srcId.startsWith(upSpCode)){
                res = srcId;
            }else if(srcId.startsWith(downSpCode)){
                res = upSpCode + srcId;
            }else{
                res = upSpCode + downSpCode + srcId;
            }
            if(res.length()>20){res = res.substring(0,20);}
            return res;
        }else{
            //如果下游传了完整接入号,并且是给他的虚拟接入号
            if(srcId.startsWith(vspcode)){
                res = srcId.replace(vspcode, upSpCode);
            }else if(srcId.startsWith(downSpCode)){
                res = upSpCode + srcId;
            }else{
                res = upSpCode + downSpCode + srcId;
            }
            if(res.length()>20){res = res.substring(0,20);}
            return res;
        }
    }
}
