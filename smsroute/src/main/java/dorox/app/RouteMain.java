package dorox.app;

import dorox.app.manager.BlackFlush;
import dorox.app.manager.ManageConfiguration;
import dorox.app.manager.MobileInfoFlush;
import dorox.app.port.DownPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 1，初始化：读取加载所有配置信息，
 * 2，消费server的ServerRequest，
 * 3，根据配置信息，a:发送RouteStat到server，b:RouteRequest到port，c:RouteReport到report
 */
//@Configuration
@Component
@Order(value=1)
public class RouteMain implements CommandLineRunner{
	private static final Logger logger = LoggerFactory.getLogger(RouteMain.class);

	@Autowired
	private ManageConfiguration manageConfiguration;

	@Override
	public void run(String... args) throws Exception {

		//初始化加载配置信息
		manageConfiguration.start();

	}
}
