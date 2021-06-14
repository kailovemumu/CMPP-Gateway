package dorox.app.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import dorox.app.cmpp.CmppDownPort;
import dorox.app.vo.ReSendMsg;

import java.util.concurrent.*;

public class Statics {

	ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
			.setNameFormat("demo-pool-%d").build();
	ExecutorService singleThreadPool = new ThreadPoolExecutor(1, 1,
			0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());


	public static BlockingQueue<ReSendMsg> RESEND_QUEUE = new LinkedBlockingQueue<>();

	//下游配置表,
	public static ConcurrentHashMap<String, CmppDownPort> DOWN_PORT_MAP =
			new ConcurrentHashMap<>();


//	public static BlockingQueue<ServerRequestEvent> SERVER_REQUEST_QUEUE =
//			new LinkedBlockingQueue<>();
//	public static BlockingQueue<StatArrivedEvent> STAT_ARRIVED_QUEUE =
//			new LinkedBlockingQueue<>();


	

}
