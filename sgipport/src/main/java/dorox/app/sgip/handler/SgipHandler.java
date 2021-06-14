package dorox.app.sgip.handler;

import com.zx.sms.codec.sgip12.msg.SgipSubmitRequestMessage;
import com.zx.sms.codec.sgip12.msg.SgipSubmitResponseMessage;
import com.zx.sms.common.util.ChannelUtil;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.connect.manager.EventLoopGroupFactory;
import com.zx.sms.connect.manager.ExitUnlimitCirclePolicy;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import com.zx.sms.session.cmpp.SessionState;
import dorox.app.sgip.SgipUpPort;
import dorox.app.util.CacheConfig;
import dorox.app.vo.ResponseVoSGIP;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;


public class SgipHandler extends AbstractBusinessHandler {

	private static final Logger logger = LoggerFactory.getLogger(SgipHandler.class);
	
	/*计算每秒发送速率*/
	private final int rate = 1;

	private SgipUpPort sgipUpPort;
	public SgipHandler(SgipUpPort sgipUpPort) {
		this.sgipUpPort=sgipUpPort;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void userEventTriggered(final ChannelHandlerContext ctx, Object evt) throws Exception {
		
		if (evt == SessionState.Connect) {

			/**
			 * 每25秒发送一个错误号码。做成长链接。
			 */
			EventLoopGroupFactory.INS.submitUnlimitCircleTask(new Callable<Boolean>() {

				@Override
				public Boolean call() throws Exception {

					SgipSubmitRequestMessage requestMessage = new SgipSubmitRequestMessage();
					requestMessage.setUsernumber("13419536069");
					requestMessage.setMsgContent("ceshi");
					requestMessage.setReportflag((short)1);

					requestMessage.setSpnumber(sgipUpPort.getUpSpCode());
					requestMessage.setCorpid(String.valueOf(sgipUpPort.getNodeId()));
					requestMessage.setServicetype(String.valueOf(sgipUpPort.getNodeId()));

					ChannelFuture channelFuture = ChannelUtil.asyncWriteToEntity(getEndpointEntity().getId(), requestMessage);
					if(channelFuture!=null){
						channelFuture.addListener((f)->{
							if(!f.isSuccess()){
								logger.info("err for write by netty1 for long connection");
							}
						});
					}else{
						logger.info("err for write by netty2 for long connection");
					}
					return true;
				}
			}, new ExitUnlimitCirclePolicy() {
				@Override
				public boolean notOver(Future future) {
					return EndpointManager.INS.getEndpointConnector(getEndpointEntity()) != null;
				}
			}, rate * 25000);
		}
		ctx.fireUserEventTriggered(evt);
	}

	@Override
	public String name() {
		return "SessionConnectedHandler-Gate";
	}

	@Override
	public SgipHandler clone() throws CloneNotSupportedException {
		SgipHandler ret = (SgipHandler) super.clone();
		return ret;
	}


	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {

		if(msg instanceof SgipSubmitResponseMessage) {
			SgipSubmitResponseMessage e = (SgipSubmitResponseMessage)msg;

			String messageId = ((SgipSubmitRequestMessage)e.getRequest()).getReserve();
	 		String seqNo = e.getSequenceNumber().toString();
	 		ResponseVoSGIP responseVoSGIP = new ResponseVoSGIP(e.getResult(), seqNo, messageId);

	 		String upPortCode = getEndpointEntity().getId();
	 		String key = upPortCode + seqNo;
 			CacheConfig.responseVoCacheSGIP.put(key, responseVoSGIP);
 			logger.info("responseVoCacheSGIP.put ({},{})", key, responseVoSGIP);
		}

	}

}
