package dorox.app;

import com.google.common.util.concurrent.RateLimiter;
import com.zx.sms.connect.manager.EndpointManager;
import dorox.app.manager.ManageCmppUpPort;
import dorox.app.thread.CmppDeliverRefresh;
import dorox.app.thread.DownSpCodeConfig;
import dorox.app.thread.ResendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
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
public class UpPortMain implements CommandLineRunner{
	private static final Logger logger = LoggerFactory.getLogger(UpPortMain.class);

	@Value("${up.region.code}")
	private String upRegionCode;
//	开启的线程数量
	@Value("${deliver.stat.refresh.thread.num}")
	private int freshThreadNum;

	@Autowired
	private RabbitTemplate rabbitTemplate;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	public static ScheduledExecutorService service = Executors.newScheduledThreadPool(9);
	public static ScheduledExecutorService singleThreadScheduledExecutor4Resend = Executors.newSingleThreadScheduledExecutor();
	public static ScheduledExecutorService singleThreadScheduledExecutor4Config = Executors.newSingleThreadScheduledExecutor();

	private EndpointManager manager = EndpointManager.INS;
//	流速的控制，每个通道的速率（从队列取消息的速度）
	public static ConcurrentHashMap<String, RateLimiter> CHANNEL_SPEED_MAP = new ConcurrentHashMap<>();

	@Override
	public void run(String... args) throws Exception {

		/*每分钟更新扩展位信息*/
		singleThreadScheduledExecutor4Config.scheduleAtFixedRate(new DownSpCodeConfig(jdbcTemplate), 0, 1 , TimeUnit.MINUTES);
		/*每分钟更新端口信息*/
		singleThreadScheduledExecutor4Config.scheduleAtFixedRate(new ManageCmppUpPort(jdbcTemplate, rabbitTemplate, upRegionCode, manager), 0, 1 , TimeUnit.MINUTES);
		/*netty发送失败，进入队列，10分钟重发*/
		singleThreadScheduledExecutor4Resend.scheduleAtFixedRate(new ResendMessage(), 0, 10 , TimeUnit.MINUTES);

		for(int i = 0; i< freshThreadNum; i++){
			/*刷新回执状态*/
			service.scheduleAtFixedRate(new CmppDeliverRefresh(rabbitTemplate), 0, 1 , TimeUnit.SECONDS);
		}
	}
}
