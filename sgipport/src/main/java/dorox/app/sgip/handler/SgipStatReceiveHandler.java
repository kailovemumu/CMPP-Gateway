package dorox.app.sgip.handler;

import com.zx.sms.codec.sgip12.msg.*;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import dorox.app.mq.event.PortMoEvent;
import dorox.app.mq.event.PortStatEvent;
import dorox.app.util.CacheConfig;
import dorox.app.util.MqUtil;
import dorox.app.util.Statics;
import dorox.app.vo.ResponseVoSGIP;
import dorox.app.vo.SpVo;
import dorox.app.vo.SubmitVoSGIP;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

public class SgipStatReceiveHandler extends AbstractBusinessHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(SgipStatReceiveHandler.class);



	private RabbitTemplate rabbitTemplate;
	public SgipStatReceiveHandler(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate=rabbitTemplate;
	}

	@Override
	public String name() {
		return "MessageReceiveHandler-smsBiz";
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {

		reponse(ctx, msg);

	}

	@Override
	public SgipStatReceiveHandler clone() throws CloneNotSupportedException {
		SgipStatReceiveHandler ret = (SgipStatReceiveHandler) super.clone();

		return ret;
	}	

	protected ChannelFuture reponse(final ChannelHandlerContext ctx, Object msg) {
		
		if(msg instanceof SgipDeliverRequestMessage){
			SgipDeliverRequestMessage deli = (SgipDeliverRequestMessage)msg;
			SgipDeliverResponseMessage resp = new SgipDeliverResponseMessage(deli.getHeader());
			resp.setResult((short)0);
			resp.setTimestamp(deli.getTimestamp());
			
			List<SgipDeliverRequestMessage> deliarr = deli.getFragments();
			if(deliarr!=null) {
				for(SgipDeliverRequestMessage item:deliarr) {
					SgipDeliverResponseMessage item_resp = new SgipDeliverResponseMessage(item.getHeader());
					item_resp.setResult((short)0);
					item_resp.setTimestamp(item.getTimestamp());
					ctx.writeAndFlush(item_resp);
				}
			}
			ctx.writeAndFlush(resp);

			//上行处理
			String content = deli.getMsgContent();
			String phone = deli.getUsernumber();
			if(phone.startsWith("86")) {
				phone= phone.substring(2);
			}
			String destId = deli.getSpnumber();
			String downPortCode = "";
			String tmp = "";
			String downPortCodeValue = "";
			String tmpValue = "";
			for(Entry<String,HashSet<SpVo>> entry : Statics.SP_CODE_MAP.entrySet()){
				if(entry.getValue() != null && entry.getValue().size() > 0) {
					HashSet<SpVo> spSet = entry.getValue();
					for(SpVo sp : spSet) {
						if(destId.startsWith(sp.getSpCode())) {
							tmp = entry.getKey();
							tmpValue = sp.getSpCode();
							if(tmpValue.length() > downPortCodeValue.length()){
								downPortCode = tmp;
								downPortCodeValue = tmpValue;
								if(StringUtils.isNotBlank(sp.getVspCode())) {
									destId = destId.replace(sp.getUpSpCode(), sp.getVspCode());
								}
							}
						}
					}
				}
			}

			MqUtil.sendMsg(new PortMoEvent(downPortCode,phone,content,destId,
					Statics.DOWN_REGION_CODE_MAP.get(downPortCode)),
					rabbitTemplate, Statics.DOWN_REGION_CODE_MAP.get(downPortCode) + ".report");

		}else if(msg instanceof SgipReportRequestMessage) {
			
			SgipReportRequestMessage report = (SgipReportRequestMessage)msg;
			if(report.getState() != 1){

				SgipReportResponseMessage resp = new SgipReportResponseMessage(report.getHeader());
				resp.setTimestamp(report.getTimestamp());
				resp.setResult((short)0);
				ChannelFuture future =  ctx.writeAndFlush(resp);
				
				//状态处理
				String upPortCode = getEndpointEntity().getId().substring("sgipchild".length());
				
				String phone = report.getUsernumber();
				if(phone.startsWith("86")){	phone = phone.substring(2);	}

				String key = upPortCode + report.getSequenceId().toString();
				ResponseVoSGIP responseVoSGIP = CacheConfig.responseVoCacheSGIP.getIfPresent(key);
				logger.info("responseVoCacheSGIP getIfPresent ({},{})",key, responseVoSGIP);
				if(responseVoSGIP != null){
					String messageId = responseVoSGIP.getMessageId();

					//根据通道中upPortCode,构造消息体投到指定上游通道。
					SubmitVoSGIP submitVo = CacheConfig.submitVoCacheSGIP.getIfPresent(messageId);
					logger.info("submitVoCacheSGIP getIfPresent ({},{})",key, submitVo);

					if(submitVo != null){
						/*0，发送成功；1，等待；2，失败*/
						short stat = report.getState();
						String statString = "DELIVRD";
						/*失败返回错误码*/
						if(stat == 2){
							statString = String.valueOf(report.getErrorcode());
						}

						List<String> msgIds = submitVo.getMsgIds();
						String downMsgId = msgIds.remove(0);

						PortStatEvent event = new PortStatEvent(submitVo.getMessageId(),
								submitVo.getDownPortCode(), upPortCode, phone, statString,
								downMsgId, report.getSequenceId().toString(), Statics.DOWN_REGION_CODE_MAP.get(submitVo.getDownPortCode()), submitVo.getSrcId());

						MqUtil.sendMsg(event, rabbitTemplate, event.getDownRegionCode() + ".report");

						//清缓存
						//CacheConfig.submitVoCacheSGIP.invalidate(key);
					}//不考虑取不到缓存的情况了，因为sgip不通过response关联短信状态。
				}


				return future;
			}
		}else {
			logger.info("other message:{}", msg);
		}
			
		return null;
	}

}
