package dorox.app.sgip;

import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.connect.manager.sgip.SgipClientEndpointEntity;
import com.zx.sms.connect.manager.sgip.SgipServerChildEndpointEntity;
import com.zx.sms.connect.manager.sgip.SgipServerEndpointEntity;
import com.zx.sms.handler.api.BusinessHandlerInterface;
import dorox.app.manager.ManageSgipUpPort;
import dorox.app.sgip.handler.SgipHandler;
import dorox.app.sgip.handler.SgipStatReceiveHandler;
import dorox.app.util.Statics;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

public class SgipUpPort extends SgipUpPortProperty {

	//端口管理
	private ManageSgipUpPort manageSgipUpPort;

    private EndpointManager manager;

    private JdbcTemplate jdbcTemplate;
    private RabbitTemplate rabbitTemplate;

	public SgipUpPort(JdbcTemplate jdbcTemplate, RabbitTemplate rabbitTemplate, EndpointManager manager) {
		this.jdbcTemplate=jdbcTemplate;
		this.manager = manager;
		this.rabbitTemplate = rabbitTemplate;
	}

	public SgipUpPort manageCmppUpPort(ManageSgipUpPort manageSgipUpPort){
		this.manageSgipUpPort=manageSgipUpPort;
		return this;
	}

	public SgipUpPort upName(String upName) {
		setUpName(upName);
		return this;
	}

	public SgipUpPort upPassword(String upPassword) {
		setUpPassword(upPassword);
		return this;
	}

	public SgipUpPort upPort(int upPort) {
		setUpPort(upPort);
		return this;
	}

	public SgipUpPort upIp(String upIp) {
		setUpIp(upIp);
		return this;
	}

	public SgipUpPort upSpCode(String upSpCode) {
		setUpSpCode(upSpCode);
		return this;
	}

	public SgipUpPort channelNum(short channelNum) {
		setChannelNum(channelNum);
		return this;
	}

	public SgipUpPort upType(String upType) {
		setUpType(upType);
		return this;
	}

	public SgipUpPort upRegionCode(String upRegionCode) {
		setUpRegionCode(upRegionCode);
		return this;
	}
	public SgipUpPort nodeId(long nodeId) {
		setNodeId(nodeId);
		return this;
	}
	public SgipUpPort sgipRecePort(int sgipRecePort) {
		setSgipRecePort(sgipRecePort);
		return this;
	}

	/*启动端口*/
	private void startSgipPort() {
		SgipServerEndpointEntity server = new SgipServerEndpointEntity();
		server.setId("sgipserver" + getUpName());
		server.setPort(getSgipRecePort());
		server.setValid(true);
		//使用ssl加密数据流
		server.setUseSSL(false);
		manager.openEndpoint(server);


		SgipServerChildEndpointEntity child = new SgipServerChildEndpointEntity();
		child.setId("sgipchild" + getUpName());
		child.setLoginName(getUpName());
		child.setLoginPassowrd(getUpPassword());
//		if("zzxg".equals(getUpName())){
//			child.setLoginName("openet");
//			child.setLoginPassowrd("openet");
//		}

		child.setNodeId(getNodeId());
		child.setValid(true);
		child.setChannelType(EndpointEntity.ChannelType.DUPLEX);
		child.setMaxChannels((short)2);
		child.setRetryWaitTimeSec((short)30);
		child.setMaxRetryCnt((short)3);
		child.setReSendFailMsg(false);
		child.setIdleTimeSec((short)30);
//						child.setWriteLimit(200);
//						child.setReadLimit(200);
		child.setSupportLongmsg(EndpointEntity.SupportLongMessage.BOTH);
		List<BusinessHandlerInterface> serverhandlers = new ArrayList<BusinessHandlerInterface>();
		serverhandlers.add(new SgipStatReceiveHandler(rabbitTemplate));
		child.setBusinessHandlerSet(serverhandlers);
		server.addchild(child);
		manager.openEndpoint(child);

		//客户发送端
		SgipClientEndpointEntity client = new SgipClientEndpointEntity();
		client.setId(getUpName());
		client.setHost(getUpIp());
		client.setPort(getUpPort());

		client.setLoginName(getUpName());
		client.setLoginPassowrd(getUpPassword());
		client.setChannelType(EndpointEntity.ChannelType.DUPLEX);
		client.setNodeId(getNodeId());
		client.setMaxChannels(getChannelNum());

		client.setRetryWaitTimeSec((short)30);
		client.setMaxRetryCnt((short)0);
		client.setUseSSL(false);
		client.setReSendFailMsg(false);

		List<BusinessHandlerInterface> clienthandlers = new ArrayList<BusinessHandlerInterface>();
		clienthandlers.add(new SgipHandler(this));//
		client.setBusinessHandlerSet(clienthandlers);
		for(int i = 0; i < client.getMaxChannels(); i++){
			manager.openEndpoint(client);
		}
	}

	private void stopSgipPort() {
		manager.remove("sgipserver"+getUpName());
		manager.remove("sgipchild"+getUpName());
		manager.remove(getUpName());
		Statics.UP_PORT_MAP.remove(getUpName());
	}

	public final static int SGIP_PORT_START = 1;
	public final static int SGIP_PORT_STOP = 2;
	public final static int SGIP_PORT_RESET = 3;

	/*cmppReset:1开端口，2关端口，3重启端口，4不处理端口*/
	public void startSgipPort(int sgipReset) {
		if(SGIP_PORT_START == sgipReset) {
			startSgipPort();
		}else if(SGIP_PORT_STOP == sgipReset) {
			stopSgipPort();
		}else if(SGIP_PORT_RESET == sgipReset) {
			stopSgipPort();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			startSgipPort();
		}

	}

}
