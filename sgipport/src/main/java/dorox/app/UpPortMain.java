package dorox.app;

import com.google.common.util.concurrent.RateLimiter;
import com.zx.sms.connect.manager.EndpointManager;
import dorox.app.manager.ManageSgipUpPort;
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
 */
@Component
@Order(value=1)
public class UpPortMain implements CommandLineRunner{
	private static final Logger logger = LoggerFactory.getLogger(UpPortMain.class);

	@Value("${up.region.code}")
	private String upRegionCode;

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private RabbitTemplate rabbitTemplate;

	public static ScheduledExecutorService singleThreadScheduledExecutor4Condig = Executors.newSingleThreadScheduledExecutor();
	public static ScheduledExecutorService singleThreadScheduledExecutor4Resend = Executors.newSingleThreadScheduledExecutor();

	private EndpointManager manager = EndpointManager.INS;

	public static ConcurrentHashMap<String, RateLimiter> CHANNEL_SPEED_MAP = new ConcurrentHashMap<>();

	@Override
	public void run(String... args) throws Exception {

		/*每分钟更新扩展位信息*/
		singleThreadScheduledExecutor4Condig.scheduleAtFixedRate(new DownSpCodeConfig(jdbcTemplate), 0, 1 , TimeUnit.MINUTES);
		/*每分钟更新端口信息*/
		singleThreadScheduledExecutor4Condig.scheduleAtFixedRate(new ManageSgipUpPort(jdbcTemplate, rabbitTemplate, upRegionCode, manager), 0, 1 , TimeUnit.MINUTES);
		/*netty发送失败，进入队列，10分钟重发*/
		singleThreadScheduledExecutor4Resend.scheduleAtFixedRate(new ResendMessage(), 0, 10 , TimeUnit.MINUTES);

	}

}
