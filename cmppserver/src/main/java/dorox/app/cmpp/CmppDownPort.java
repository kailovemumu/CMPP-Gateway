package dorox.app.cmpp;

import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitResponseMessage;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.connect.manager.cmpp.CMPPServerChildEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPServerEndpointEntity;
import com.zx.sms.handler.api.BusinessHandlerInterface;
import dorox.app.cmpp.handler.CmppMessageHandler;
import dorox.app.manager.ManageCmppDownPort;
import dorox.app.mq.event.ServerRequestEvent;
import dorox.app.util.MqUtil;
import dorox.app.util.Statics;
import dorox.app.vo.ReSendMsg;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import org.n3r.idworker.Sid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


public class CmppDownPort extends CmppDownPortProperty {

	private static final Logger logger = LoggerFactory.getLogger(CmppDownPort.class);
	//端口管理
	private ManageCmppDownPort manageCmppDownPort;

    private EndpointManager manager;
    private CMPPServerEndpointEntity server;
    private Sid sid;
    private JdbcTemplate jdbcTemplate;
    private RabbitTemplate rabbitTemplate;
    private String messageIdPrefix;

//	public ConcurrentLinkedQueue<Message> NETTY_QUEUE =
//			new ConcurrentLinkedQueue<>();

	public CmppDownPort(JdbcTemplate jdbcTemplate,RabbitTemplate rabbitTemplate,
		    EndpointManager manager,
		    CMPPServerEndpointEntity server,Sid sid,
		    String messageIdPrefix) {
		this.rabbitTemplate=rabbitTemplate;
		this.jdbcTemplate=jdbcTemplate;
		this.manager = manager;
	    this.server = server;
	    this.sid=sid;
	    this.messageIdPrefix = messageIdPrefix;

	}

	public CmppDownPort manageCmppDownPort(ManageCmppDownPort manageCmppDownPort){
		this.manageCmppDownPort=manageCmppDownPort;
		return this;
	}
	//CMPP业务属性配置
	public CmppDownPort downName(String downName) {
		setDownName(downName);
		return this;
	}

	public CmppDownPort downPassword(String downPassword) {
		setDownPassword(downPassword);
		return this;
	}

	public CmppDownPort version(String version) {
		setVersion(version);
		return this;
	}

	public CmppDownPort whiteIp(String whiteIp) {
		setWhiteIp(whiteIp);
		return this;
	}

	public CmppDownPort channelNum(short channelNum) {
		setChannelNum(channelNum);
		return this;
	}

	public CmppDownPort downType(String downType) {
		setDownType(downType);
		return this;
	}

	public CmppDownPort regionCode(String regionCode) {
		setRegionCode(regionCode);
		return this;
	}

	//启动端口
	public void start() {
		startCmppPort();
	}

	private void startCmppPort() {
		CMPPServerChildEndpointEntity child = new CMPPServerChildEndpointEntity();
		child.setId(getDownName());//每个子服务端的id使用下游的id
		child.setChartset(Charset.forName("utf-8"));
		child.setGroupName(getDownName());//每个子服务端的groupName使用下游的id
		child.setUserName(getDownName());
		child.setPassword(getDownPassword());
		child.setValid(true);
		child.setVersion((short)0x20);
		if("3".equals(getVersion())){
			child.setVersion((short)0x30);
		}

		child.setMaxChannels(getChannelNum());
		child.setRetryWaitTimeSec((short)30);
		child.setMaxRetryCnt((short)3);
		child.setReSendFailMsg(false);

		List<BusinessHandlerInterface> serverhandlers = new ArrayList<BusinessHandlerInterface>();
		serverhandlers.add(new CmppMessageHandler(this, rabbitTemplate));
		child.setBusinessHandlerSet(serverhandlers);
		server.addchild(child);
		manager.openEndpoint(child);

	}

	private void stopCmppPort() {
		manager.remove(getDownName());
		server.removechild(server.getChild(getDownName()));
		Statics.DOWN_PORT_MAP.remove(getDownName());
	}

	/**
	 * SubmitVo放入缓存中，收到回执消息后传给客户
	 * ServerRequestEvent放入队列中，通过kafka生产者发送消息
	 */
	public void handler(CmppSubmitRequestMessage e, ChannelHandlerContext ctx) {

		String phone = e.getDestterminalId()[0];
		if(phone.startsWith("86")){	phone = phone.substring(2);}
		String content = e.getMsgContent();

		String srcId = e.getSrcId();
		//生成rabbit的唯一消息id，也是短信的key
		String messageId = sid.nextShort(messageIdPrefix);

		//返回ReportRequestMessage
		final List<String> msgIds = new ArrayList<>();

		final CmppSubmitResponseMessage resp = new CmppSubmitResponseMessage(e.getHeader().getSequenceId());
		resp.setResult(0);
		//发送响应
		ChannelFuture channelFuture = ctx.channel().writeAndFlush(resp);
//		ChannelFuture channelFuture = ChannelUtil.asyncWriteToEntity(getDownName(), resp);
		if(channelFuture!=null){
			channelFuture.addListener((f)->{
				if(f.isSuccess()){
				}else{
					Statics.RESEND_QUEUE.add(new ReSendMsg(getDownName(), resp));
					logger.error("err for write by netty {} - {}", getDownName(), resp);
				}
			});
		}else{
			Statics.RESEND_QUEUE.add(new ReSendMsg(getDownName(), resp));
			logger.error("err2 for write by netty2 {} - {}", getDownName(), resp);
		}
		msgIds.add(resp.getMsgId().toString());

		if(e.getFragments() != null) {
			//长短信会可能带有片断，每个片断都要回复一个response
			for(CmppSubmitRequestMessage frag : e.getFragments()) {
				final CmppSubmitResponseMessage responseMessage = new CmppSubmitResponseMessage(frag.getHeader().getSequenceId());
				responseMessage.setResult(0);
//				channelFuture = ChannelUtil.asyncWriteToEntity(getDownName(), responseMessage);
				channelFuture = ctx.channel().writeAndFlush(responseMessage);
				if(channelFuture!=null){
					channelFuture.addListener((f)->{
						if(f.isSuccess()){
						}else{
							Statics.RESEND_QUEUE.add(new ReSendMsg(getDownName(), responseMessage));
							logger.error("err for write by netty {} - {}", getDownName(), responseMessage);
						}
					});
				}else{
					Statics.RESEND_QUEUE.add(new ReSendMsg(getDownName(), responseMessage));
					logger.error("err2 for write by netty2 {} - {}", getDownName(), responseMessage);
				}
				msgIds.add(responseMessage.getMsgId().toString());
			}
		}

		//ServerRequestEvent丢入队列
		ServerRequestEvent event = new ServerRequestEvent(
				messageId,getDownName(),phone,content,srcId,msgIds,getRegionCode());

		MqUtil.sendMsg(event, rabbitTemplate, "serverrequest.report");
	}

	//cmppReset:1开端口，2关端口，3重启端口，4不处理端口
	public void startCmppPort(int cmppReset) {
		if(1 == cmppReset) {
			startCmppPort();
		}else if(2 == cmppReset) {
			stopCmppPort();
		}else if(3 == cmppReset) {
			stopCmppPort();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			startCmppPort();
		}
	}

	public void removeDownPort(String downName, String regionCode) {
		Statics.DOWN_PORT_MAP.remove(downName);
	}
	public boolean isWhiteIp(String clientIp) {
		return getWhiteIp()!=null &&
				(getWhiteIp().contains(clientIp) || getWhiteIp().contains("*"));
	}

	public final static int CMPP_PORT_START = 1;
	public final static int CMPP_PORT_STOP = 2;
	public final static int CMPP_PORT_RESET = 3;
}
