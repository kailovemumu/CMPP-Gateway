package dorox.app.thread;

import com.zx.sms.common.util.ChannelUtil;
import dorox.app.UpPortMain;
import dorox.app.util.Statics;
import dorox.app.vo.ReSendMsg;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 ** 如果发送通道失败则10分钟候重发
 */
public class ResendMessage extends Thread{

	private static final Logger logger = LoggerFactory.getLogger(ResendMessage.class);

	@Override
	public void run() {
		try {
			resendMessage();
			logger.info("resendMessage");
		} catch (Exception e) {
			logger.info("Exception {}", e);
		}
	}

	private void resendMessage(){
		
		ReSendMsg reSendMsg;
		while((reSendMsg= Statics.RESEND_QUEUE.poll())!=null){
			
			try {
				//流控
				if(UpPortMain.CHANNEL_SPEED_MAP.get(reSendMsg.getDownName() + reSendMsg.getUpName()) != null) {
					UpPortMain.CHANNEL_SPEED_MAP.get(reSendMsg.getDownName() + reSendMsg.getUpName()).acquire(reSendMsg.getTimer()+1);
				}
				ChannelFuture channelFuture = ChannelUtil.asyncWriteToEntity(reSendMsg.getUpName(), reSendMsg.getSgipSubmitRequestMessage());
				ReSendMsg finalReSendMsg = reSendMsg;
				if(channelFuture!=null){
					channelFuture.addListener((f)->{
						if(!f.isSuccess()){
							logger.error("err for write by netty {} ", finalReSendMsg);
							Statics.RESEND_QUEUE.add( finalReSendMsg);
						}
					});
				}else{
					logger.error("err2 for write by netty {}", finalReSendMsg);
					Statics.RESEND_QUEUE.add( finalReSendMsg);
				}
				logger.info("reSendMsg:{}", reSendMsg);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}
	}
}
