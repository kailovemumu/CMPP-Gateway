package dorox.app.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import dorox.app.cmpp.CmppUpPort;
import dorox.app.mq.event.PortMoEvent;
import dorox.app.mq.event.PortStatEvent;
import dorox.app.vo.DeliverVoCMPP;
import dorox.app.vo.ReSendMsg;
import dorox.app.vo.SpVo;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.*;

public class Statics {


    ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
			.setNameFormat("demo-pool-%d").build();
	ExecutorService singleThreadPool = new ThreadPoolExecutor(1, 1,
			0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());


	//下游配置表,
	public static ConcurrentHashMap<String, CmppUpPort> UP_PORT_MAP = new ConcurrentHashMap<>();


//	public static BlockingQueue<PortStatEvent> PORT_STAT_QUEUE = new LinkedBlockingQueue<>();
//	public static BlockingQueue<PortMoEvent> PORT_MO_QUEUE = new LinkedBlockingQueue<>();

	public static BlockingQueue<ReSendMsg> RESEND_QUEUE = new LinkedBlockingQueue<>();

	public static Map<String, HashSet<SpVo>> SP_CODE_MAP = new ConcurrentHashMap<>();

	public static ConcurrentLinkedQueue<DeliverVoCMPP> DELIVER_QUEUE =
			new ConcurrentLinkedQueue<>();

	public static Map<String,String> DOWN_REGION_CODE_MAP = new ConcurrentHashMap<>();
}
