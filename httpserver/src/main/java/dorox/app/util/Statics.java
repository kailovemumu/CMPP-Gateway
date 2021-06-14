package dorox.app.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
//import dorox.app.http.HttpDownPort;
import dorox.app.http.HttpDownPort;
import dorox.app.mq.event.ServerRequestEvent;
import dorox.app.mq.event.StatArrivedEvent;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;

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
	public static ConcurrentHashMap<String, HttpDownPort> DOWN_PORT_MAP =
			new ConcurrentHashMap<>();



}
