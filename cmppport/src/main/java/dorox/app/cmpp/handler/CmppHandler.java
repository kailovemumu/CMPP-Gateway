package dorox.app.cmpp.handler;

import com.zx.sms.codec.cmpp.msg.*;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import dorox.app.cmpp.CmppUpPort;
import dorox.app.mq.event.PortMoEvent;
import dorox.app.util.CacheConfig;
import dorox.app.util.MqUtil;
import dorox.app.util.Statics;
import dorox.app.vo.DeliverVoCMPP;
import dorox.app.vo.ResponseVoCMPP;
import dorox.app.vo.SpVo;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.HashSet;
import java.util.Map.Entry;

//接收到  up  回执的状态
public class CmppHandler extends AbstractBusinessHandler {

	private static final Logger logger = LoggerFactory.getLogger(CmppHandler.class);

	private CmppUpPort cmppUpPort;
	private RabbitTemplate rabbitTemplate;
	
	//计算每秒发送速率
	private int rate = 1;

	public CmppHandler(CmppUpPort cmppUpPort,RabbitTemplate rabbitTemplate) {
		this.cmppUpPort=cmppUpPort;
		this.rabbitTemplate=rabbitTemplate;
	}

	@SuppressWarnings("unchecked")
//	@Override
//	public void userEventTriggered(final ChannelHandlerContext ctx, Object evt) throws Exception {
//
//		if (evt == SessionState.Connect) {
//			EventLoopGroupFactory.INS.submitUnlimitCircleTask(new Callable<Boolean>() {
//
//				@Override
//				public Boolean call() throws Exception {
//					Message message = null;
//					while((message = cmppUpPort.NETTY_QUEUE.poll()) != null){
//
//						ChannelFuture channelFuture = ChannelUtil.asyncWriteToEntity(getEndpointEntity().getId(), message);
////						ChannelFuture channelFuture =  ctx.writeAndFlush(message);
//						Message finalMessage = message;
//						if(channelFuture!=null){
//							channelFuture.addListener((f)->{
//								if(f.isSuccess()){
//
//								}else{
//									logger.error("err for write by netty1 {} - {}", getEndpointEntity().getId(), finalMessage);
//									if(finalMessage instanceof CmppSubmitRequestMessage){
//										Statics.RESEND_QUEUE.add(new ReSendMsg(getEndpointEntity().getId(), (CmppSubmitRequestMessage)finalMessage));
//									}
//								}
//							});
//						}else{
//							logger.error("err2 for write by netty2 {} - {}", getEndpointEntity().getId(), finalMessage);
//							if(finalMessage instanceof CmppSubmitRequestMessage){
//								Statics.RESEND_QUEUE.add(new ReSendMsg(getEndpointEntity().getId(), (CmppSubmitRequestMessage)finalMessage));
//							}
//						}
//					}
//					return true;
//				}
//			}, new ExitUnlimitCirclePolicy() {
//				@Override
//				public boolean notOver(Future future) {
//					return EndpointManager.INS.getEndpointConnector(getEndpointEntity()) != null && ctx.channel().isActive();
//				}
//			}, rate * 1000);
//
//		}
//		ctx.fireUserEventTriggered(evt);
//	}

	@Override
	public String name() {
		return "SessionConnectedHandler-Gate";
	}

	@Override
	public CmppHandler clone() throws CloneNotSupportedException {
		CmppHandler ret = (CmppHandler) super.clone();
		return ret;
	}


	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof CmppDeliverRequestMessage) {
		//上行和回执消息处理
			CmppDeliverRequestMessage e = (CmppDeliverRequestMessage) msg;
			/*短信回执处理*/
			if(e.getReportRequestMessage() != null){
				/*
				* 获取状态消息中的状态、号码、msgId
				*/
				String upPortCode = getEndpointEntity().getId();
				String msgId = e.getReportRequestMessage().getMsgId().toString();
				String phone = e.getReportRequestMessage().getDestterminalId();
				String stat = e.getReportRequestMessage().getStat();
				String srcId = e.getDestId();
				if(phone.startsWith("86")){	phone = phone.substring(2);	}

				DeliverVoCMPP deliverVo = new DeliverVoCMPP(stat, srcId, phone, msgId, upPortCode);
				//插入队列
				Statics.DELIVER_QUEUE.add(deliverVo);
				
			}else{//上行处理
				String destId = e.getDestId();
				String downPortCode = "";
				String tmp = "";
				String downPortCodeValue = "";
				String tmpValue = "";
//				遍历，取出接入码的集合，对比，找到目标客户
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
				String content = e.getMsgContent();
				String phone = e.getSrcterminalId();

				PortMoEvent event = new PortMoEvent(downPortCode,phone,content,destId,Statics.DOWN_REGION_CODE_MAP.get(downPortCode));
				MqUtil.sendMsg(event, rabbitTemplate, event.getDownRegionCode() + ".report");
			}
			
			//返回CmppDeliverResponseMessage	
			if(e.getFragments()!=null) {
				//长短信会带有片断
				for(CmppDeliverRequestMessage frag:e.getFragments()) {
					CmppDeliverResponseMessage responseMessage = new CmppDeliverResponseMessage(frag.getHeader().getSequenceId());
					responseMessage.setResult(0);
					responseMessage.setMsgId(frag.getMsgId());
//					cmppUpPort.NETTY_QUEUE.add(responseMessage);
//					ctx获取通道，执行 write 写入消息
					ctx.channel().write(responseMessage);
				}
			}
//获取SequenceId 给运营商 作出响应  运营商可通过此ID 匹配消息
			CmppDeliverResponseMessage responseMessage = new CmppDeliverResponseMessage(e.getHeader().getSequenceId());
			responseMessage.setResult(0);
//			@TODO 上行未做长短信 短短信 的处理。
			responseMessage.setMsgId(e.getMsgId());
//			cmppUpPort.NETTY_QUEUE.add(responseMessage);
//			flush 发送消息 在长短信都通过write 写入进 channel 后，使用 flush 发送
			ctx.channel().writeAndFlush(responseMessage);

		} else if (msg instanceof CmppDeliverResponseMessage) {
		} else if (msg instanceof CmppSubmitRequestMessage) {
		} else if (msg instanceof CmppSubmitResponseMessage) {
//			发送给运营商的消息，获得的响应
			//只处理插入缓存。只能按照request->response->deliver顺序查。反之!
			//即从seqNo->msgId
			String upPortCode = getEndpointEntity().getId();
			CmppSubmitResponseMessage e = (CmppSubmitResponseMessage) msg;
			String seqNo = String.valueOf(e.getSequenceNo());
			long result = e.getResult();
			String msgId = e.getMsgId().toString();
			String key = upPortCode + msgId;
			ResponseVoCMPP responseVo = new ResponseVoCMPP(msgId, result, seqNo, ((CmppSubmitRequestMessage)e.getRequest()).getReserve());
			
			CacheConfig.responseVoCacheCMPP.put(key, responseVo);

		} else if (msg instanceof CmppQueryRequestMessage) {
			CmppQueryRequestMessage e = (CmppQueryRequestMessage) msg;
			CmppQueryResponseMessage res = new CmppQueryResponseMessage(e.getHeader().getSequenceId());
//			cmppUpPort.NETTY_QUEUE.add(res);
			ctx.channel().writeAndFlush(res);
		} else {
			ctx.fireChannelRead(msg);
		}
	}
}
