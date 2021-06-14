package dorox.app.cmpp.handler;

import com.zx.sms.codec.cmpp.msg.*;
import com.zx.sms.common.util.ChannelUtil;
import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.connect.manager.EventLoopGroupFactory;
import com.zx.sms.connect.manager.ExitUnlimitCirclePolicy;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import com.zx.sms.session.cmpp.SessionState;
import dorox.app.cmpp.CmppDownPort;

import dorox.app.mq.event.StatArrivedEvent;
import dorox.app.util.MqUtil;
import dorox.app.util.Statics;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

//@Sharable
public class CmppMessageHandler extends AbstractBusinessHandler {
	private static final Logger logger = LoggerFactory.getLogger(CmppMessageHandler.class);
	private int rate = 1;


	private RabbitTemplate rabbitTemplate;
	private CmppDownPort cmppDownPort;

	public CmppMessageHandler(CmppDownPort cmppDownPort, RabbitTemplate rabbitTemplate) {
		this.cmppDownPort=cmppDownPort;this.rabbitTemplate=rabbitTemplate;
	}

	@Override
	public String name() {
		return "MessageReceiveHandler-smsBiz";
	}

//	@Override
//	@SuppressWarnings("unchecked")
//	public synchronized void userEventTriggered(final ChannelHandlerContext ctx, Object evt) throws Exception {
//		if (evt == SessionState.Connect) {
//
//			//从UPTODOWN_DELIVE队列中，读取状态消息。
//			String downPortCode = getEndpointEntity().getId();
//
//			//计数
//			EventLoopGroupFactory.INS.submitUnlimitCircleTask(new Callable<Boolean>() {
//
//				@Override
//				public Boolean call() throws Exception {
//
//					Message message = null;
//					while((message = Statics.DOWN_PORT_MAP.get(downPortCode).NETTY_QUEUE.poll()) != null){
//
//						ChannelFuture channelFuture = ChannelUtil.asyncWriteToEntity(downPortCode, message);
//						ChannelFuture channelFuture =  ctx.write(message);
//						Message finalMessage = message;
//						if(channelFuture!=null){
//							channelFuture.addListener((f)->{
//								if(f.isSuccess()){
//									if(finalMessage instanceof  CmppDeliverRequestMessage){
//										CmppDeliverRequestMessage deliver = (CmppDeliverRequestMessage)finalMessage;
//										String messageId = deliver.getReserved();
//										CmppReportRequestMessage report = deliver.getReportRequestMessage();
//										if(StringUtils.isNotBlank(messageId) && report != null){
//											StatArrivedEvent event = new StatArrivedEvent(
//													messageId, report.getMsgId().toString() , report.getStat());
//											MqUtil.sendMsg(event, rabbitTemplate, "report");
//
//										}
//									}
//								}else{
//									logger.error("err for write by netty {} - {}", downPortCode, finalMessage);
//								}
//							});
//						}else{
//							logger.error("err2 for write by netty {} - {}", downPortCode, finalMessage);
//						}
//					}
//					ctx.flush();
//					return true;
//				}
//			}, new ExitUnlimitCirclePolicy() {
//				@Override
//				public boolean notOver(Future future) {
//					return EndpointManager.INS.getEndpointConnector(getEndpointEntity()) != null && ctx.channel().isActive();
//				}
//			}, rate * 1000);
//		}
//		ctx.fireUserEventTriggered(evt);
//	}
	

	private void reponse(final ChannelHandlerContext ctx, Object msg) {
		//@TODO 这里是 CMPP接收到下游的请求，拆分里面的内容。还是对过来的请求作出响应，根据过来的类分别采取不同的策略
//		这里的判断类是为了区别什么呢？
		if (msg instanceof CmppSubmitRequestMessage) {
			//接收到下游发送的请求消息 CmppSubmitRequestMessage
			CmppSubmitRequestMessage e = (CmppSubmitRequestMessage) msg;	
			String downPortCode = getEndpointEntity().getId();
			//处理请求消息
			cmppDownPort.handler(e, ctx);
		} else if (msg instanceof CmppQueryRequestMessage) {
			CmppQueryRequestMessage e = (CmppQueryRequestMessage) msg;
			CmppQueryResponseMessage res = new CmppQueryResponseMessage(e.getHeader().getSequenceId());
			ctx.channel().writeAndFlush(res);
		} else if (msg instanceof CmppDeliverResponseMessage) {
		}
	}
	//@TODO 这里主要是用来鉴权，看客户的IP是否是属于 IP白名单
	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {

		InetSocketAddress insocket = (InetSocketAddress)ctx.channel().remoteAddress();
		String clientIp = insocket.getAddress().getHostAddress();
		
		String downPortCode = getEndpointEntity().getId();		
		if(cmppDownPort.isWhiteIp(clientIp)){
			reponse(ctx, msg);
		}else{
			logger.info("clientIp : {} is not for server port {}", clientIp, getEndpointEntity().getId());
			ctx.close();
		}
	}

	@Override
	public CmppMessageHandler clone() throws CloneNotSupportedException {
		CmppMessageHandler ret = (CmppMessageHandler) super.clone();
		return ret;
	}

}
