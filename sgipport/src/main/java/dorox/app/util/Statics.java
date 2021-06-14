package dorox.app.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import dorox.app.sgip.SgipUpPort;
import dorox.app.mq.event.PortMoEvent;
import dorox.app.mq.event.PortStatEvent;
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

	//电力的客户接入码 三网
	public static ConcurrentHashMap<String, String> DIANLI_DOWN_SRCID = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<String, String> DIANLI_PHONE_CHANNEL = new ConcurrentHashMap<>();


	static {
		DIANLI_DOWN_SRCID.put("799864-2", "10692763112918595598");
		DIANLI_DOWN_SRCID.put("799864-3", "10630003595598");
		DIANLI_DOWN_SRCID.put("799864-4", "10659267043829595598");
		
		DIANLI_DOWN_SRCID.put("741854-2", "1069195598");
		DIANLI_DOWN_SRCID.put("741854-3", "1069295598");
		DIANLI_DOWN_SRCID.put("741854-4", "1069395598");


	}
	//下游配置表,
	public static ConcurrentHashMap<String, SgipUpPort> UP_PORT_MAP = new ConcurrentHashMap<>();


//	public static BlockingQueue<PortStatEvent> PORT_STAT_QUEUE = new LinkedBlockingQueue<>();
//	public static BlockingQueue<PortMoEvent> PORT_MO_QUEUE = new LinkedBlockingQueue<>();

	public static BlockingQueue<ReSendMsg> RESEND_QUEUE = new LinkedBlockingQueue<>();

	public static Map<String, HashSet<SpVo>> SP_CODE_MAP = new ConcurrentHashMap<>();


	public static Map<String,String> DOWN_REGION_CODE_MAP = new ConcurrentHashMap<>();
}
