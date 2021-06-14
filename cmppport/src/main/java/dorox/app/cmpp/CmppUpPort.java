package dorox.app.cmpp;

import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.connect.manager.cmpp.CMPPClientEndpointEntity;
import com.zx.sms.handler.api.BusinessHandlerInterface;
import dorox.app.cmpp.handler.CmppHandler;
import dorox.app.manager.ManageCmppUpPort;
import dorox.app.util.Statics;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CmppUpPort extends CmppUpPortProperty {

	//端口管理
	private ManageCmppUpPort manageCmppUpPort;

    private EndpointManager manager;

    private JdbcTemplate jdbcTemplate;
	private RabbitTemplate rabbitTemplate;

//	public ConcurrentLinkedQueue<Message> NETTY_QUEUE =
//			new ConcurrentLinkedQueue<>();

	public CmppUpPort(JdbcTemplate jdbcTemplate, RabbitTemplate rabbitTemplate, EndpointManager manager) {
		this.jdbcTemplate=jdbcTemplate;
		this.manager = manager;
		this.rabbitTemplate=rabbitTemplate;
	}

	public CmppUpPort manageCmppUpPort(ManageCmppUpPort manageCmppUpPort){
		this.manageCmppUpPort=manageCmppUpPort;
		return this;
	}
	//CMPP业务属性配置
	public CmppUpPort upName(String upName) {
		setUpName(upName);
		return this;
	}

	public CmppUpPort upPassword(String upPassword) {
		setUpPassword(upPassword);
		return this;
	}

	public CmppUpPort upVersion(String upVersion) {
		setUpVersion(upVersion);
		return this;
	}

	public CmppUpPort upPort(int upPort) {
		setUpPort(upPort);
		return this;
	}

	public CmppUpPort upIp(String upIp) {
		setUpIp(upIp);
		return this;
	}

	public CmppUpPort upSpCode(String upSpCode) {
		setUpSpCode(upSpCode);
		return this;
	}

	public CmppUpPort channelNum(short channelNum) {
		setChannelNum(channelNum);
		return this;
	}

	public CmppUpPort upType(String upType) {
		setUpType(upType);
		return this;
	}

	public CmppUpPort upRegionCode(String upRegionCode) {
		setUpRegionCode(upRegionCode);
		return this;
	}


	/*启动端口*/
	private void startCmppPort() {

		CMPPClientEndpointEntity client = new CMPPClientEndpointEntity();
		client.setId(getUpName());
		client.setHost(getUpIp());
		client.setPort(getUpPort());
		client.setChartset(Charset.forName("utf-8"));
		client.setGroupName(getUpName());
		client.setUserName(getUpName());
		client.setPassword(getUpPassword());
		client.setSpCode(getUpSpCode());
		client.setMaxChannels(getChannelNum());
		client.setVersion((short)0x20);
		if("3".equals(getUpVersion())){
			client.setVersion((short)0x30);
		}
		client.setRetryWaitTimeSec((short)30);
		client.setMaxRetryCnt((short)0);
		client.setUseSSL(false);
		client.setReSendFailMsg(false);
		client.setIdleTimeSec((short)25);

		List<BusinessHandlerInterface> clienthandlers = new ArrayList<BusinessHandlerInterface>();
		clienthandlers.add(new CmppHandler(this, rabbitTemplate));
		client.setBusinessHandlerSet(clienthandlers);
		for(int i = 0; i < client.getMaxChannels(); i++){
			manager.openEndpoint(client);
		}

	}

	private void stopCmppPort() {
		manager.remove(getUpName());
		Statics.UP_PORT_MAP.remove(getUpName());
	}

	public final static int CMPP_PORT_START = 1;
	public final static int CMPP_PORT_STOP = 2;
	public final static int CMPP_PORT_RESET = 3;

	//cmppReset:1开端口，2关端口，3重启端口，4不处理端口
	public void startCmppPort(int cmppReset) {
		if(CMPP_PORT_START == cmppReset) {
			startCmppPort();
		}else if(CMPP_PORT_STOP == cmppReset) {
			stopCmppPort();
		}else if(CMPP_PORT_RESET == cmppReset) {
			stopCmppPort();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			startCmppPort();
		}

	}

}
