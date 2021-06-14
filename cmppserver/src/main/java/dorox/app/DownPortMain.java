package dorox.app;

import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.connect.manager.cmpp.CMPPServerEndpointEntity;
import dorox.app.manager.ManageCmppDownPort;
import dorox.app.thread.ResendMessage;
import org.n3r.idworker.Sid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *低负载时消息编码解码可控制在10ms以内。
 ***********************************************************************
 *下游端口流程
 *1，获取配置文件信息，包括下游账号，路由，连接数，流数
 *2，配置下游服务端口，打开链接，绑定handler，等待下游建立连接
 *3，下游请求信息，解析手机和内容，放入公共的路由map中，供对应的上游端口发送
 * @author apple
 */
//@Configuration
@Component
@Order(value=1)
public class DownPortMain implements CommandLineRunner{
	private static final Logger logger = LoggerFactory.getLogger(DownPortMain.class);

	@Value("${downcustom.port}")//服务端端口
	private int serverPort;
	@Value("${messageid.prefix}")
	private String messageIdPrefix;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	//spring 封装的rabbitMQ 组件
	@Autowired
	private RabbitTemplate rabbitTemplate;
	@Autowired
	private Sid sid;

	/**客户分区*/
	@Value("${region.code}")
	private String regionCode;

	//实现定时任务,创建变量，后面配置详细信息。
	public static ScheduledExecutorService service4DownPort = Executors.newSingleThreadScheduledExecutor();
	public static ScheduledExecutorService service4Resend = Executors.newSingleThreadScheduledExecutor();
	//@TODO  单例模式，管理所有端口，并负责所有端口的打开，关闭，以及端口信息保存，以及连接断线重连。
	private EndpointManager manager = EndpointManager.INS;
	//@TODO	管理器实例对象,创建服务监听端口
	private CMPPServerEndpointEntity server = new CMPPServerEndpointEntity();

	@Override
	public void run(String... args) throws Exception {

		initCmppServer();
		//初始化下游端口
		//每分钟执行一次
		service4DownPort.scheduleAtFixedRate(new ManageCmppDownPort(regionCode, messageIdPrefix, sid, jdbcTemplate,rabbitTemplate, manager, server),
				0, 1 , TimeUnit.MINUTES);
		//每秒钟执行一次 发送失败重发信息
		service4Resend.scheduleAtFixedRate(new ResendMessage(rabbitTemplate),0, 1 , TimeUnit.SECONDS);

	}

	private void initCmppServer() {
		server.setId("server");
		server.setPort(serverPort);
		server.setValid(true);
		server.setUseSSL(false);
//		打开端口
		manager.openEndpoint(server);
//		每秒检查一次所有连接，不足数目的就新建一个连接 ,客户端重连机制。
		manager.startConnectionCheckTask();
	}
}
