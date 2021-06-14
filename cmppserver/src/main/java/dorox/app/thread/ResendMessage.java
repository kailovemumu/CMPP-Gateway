package dorox.app.thread;

import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.common.util.ChannelUtil;
import dorox.app.mq.event.StatArrivedEvent;
import dorox.app.util.MqUtil;
import dorox.app.util.Statics;
import dorox.app.vo.ReSendMsg;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 ** 如果发送通道失败则10分钟候重发
 */
public class ResendMessage extends Thread{

	private static final Logger logger = LoggerFactory.getLogger(ResendMessage.class);

	private RabbitTemplate rabbitTemplate;
	public ResendMessage(RabbitTemplate rabbitTemplate){
		this.rabbitTemplate=rabbitTemplate;
	}
	@Override
	public void run() {
		try {
			resendMessage();
		} catch (Exception e) {
			logger.info("Exception {}", e);
		}
	}

	private void resendMessage() throws Exception{
//		发送失败的短信详细信息
		ReSendMsg reSendMsg;
//		poll() 和 remove() 都是从队列中取出一个元素，
//		但是 poll() 在获取元素失败的时候会返回空，但是 remove() 失败的时候会抛出异常.
//		从队列中取出元素，如果不为空则继续。
		while((reSendMsg= Statics.RESEND_QUEUE.poll())!=null){

			ChannelFuture channelFuture = ChannelUtil.asyncWriteToEntity(reSendMsg.getDownName(), reSendMsg.getMessage());
			ReSendMsg finalReSendMsg = reSendMsg;

			if(channelFuture!=null){
				channelFuture.addListener((f)->{
					if(!f.isSuccess()){
						logger.error("err for write by netty {}", finalReSendMsg);
						Statics.RESEND_QUEUE.add(finalReSendMsg);
					}else{
						if(finalReSendMsg.getMessage() instanceof CmppDeliverRequestMessage){
							CmppDeliverRequestMessage deliver = (CmppDeliverRequestMessage) finalReSendMsg.getMessage();
							StatArrivedEvent event = new StatArrivedEvent(
									deliver.getReserved(),
									deliver.getReportRequestMessage().getMsgId().toString(),
									deliver.getReportRequestMessage().getStat());
							MqUtil.sendMsg(event, rabbitTemplate, "report");
						}
					}
				});
			}else{
				logger.error("err2 for write by netty2 {}", finalReSendMsg);
				Statics.RESEND_QUEUE.add(finalReSendMsg);
			}
			logger.info("reSendMsg:{}", reSendMsg);

			//流控
			sleep(20);

		}
	}
}
