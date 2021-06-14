package dorox.app;

import cn.hutool.core.collection.ConcurrentHashSet;
import dorox.app.delay.ServerRequestReportDelay;
import dorox.app.delay.UpdateEvent;
import dorox.app.delay.UpdateReportDelay;
import dorox.app.mq.event.PortMoEvent;
import dorox.app.thread.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;

/**
 */
@Component
@Order(value=1)
public class ReportMain implements CommandLineRunner {
	private static final Logger logger = LoggerFactory.getLogger(ReportMain.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public static final int REPORT_SERVER_REQUEST_DALAY = 30*1000;
	public static final int REPORT_ROUTE_STAT_DALAY = 35*1000;
	public static final int REPORT_PORT_STAT_DALAY = 34*1000;
	public static final int REPORT_ROUTE_REPORT_DALAY = 33*1000;
	public static final int REPORT_STAT_ARRIVED_DALAY = 32*1000;

	/**
	 *  报表生产：
	 *
	 *  1，serverrequestevent放入延时队列， 30s后处理，根据messageId从map中取stat、arrived、report等数据，insert
	 *  2，routestatevent的messageId放入延时队列，30s后，如果map中有value，说明没有被取出，update
	 *  3，portstatevent的messageId放入延时队列，30s后，如果map中有value，说明没有被取出，update
	 *  4，routereportevent的messageId放入延时队列，30s后，如果map中有value，说明没有被取出，update
	 *  5，statarrivedevent的messageId放入延时队列，30s后，如果map中有value，说明没有被取出，update
	 *  6，portmo
	 *
	 * */

	/*存放延时队列，用于插入数据库*/
	public static DelayQueue<ServerRequestReportDelay> DB_SERVER_REQUEST_DELAY_QUEUE = new DelayQueue<>();

	/*存放延时队列，用于更新数据库*/
	public static DelayQueue<UpdateReportDelay> DB_UPDATE_DELAY_QUEUE = new DelayQueue<>();

	/*存放聚合数据*/
	public static ConcurrentHashMap<String, UpdateEvent> UPDATE_REPORT_MAP = new ConcurrentHashMap<>();

	/*为了线程安全，并行转串行，将消息集中到队列中，单线程做聚合处理，*/
	public static BlockingQueue<Object> REPORT_AGGREGATION_QUEUE = new LinkedBlockingQueue<>();

	public static BlockingQueue<PortMoEvent> DB_PORT_MO_QUEUE =	new LinkedBlockingQueue<>();

	public static ExecutorService dbService = Executors.newFixedThreadPool(4);
	public static ExecutorService singleThreadService4Aggregation = Executors.newSingleThreadExecutor();
	public static ScheduledExecutorService singleThreadService4DownPortCounter = Executors.newSingleThreadScheduledExecutor();
	public static ScheduledExecutorService singleThreadService4Mo = Executors.newSingleThreadScheduledExecutor();

	public static Map<String, LongAdder> DownRequestCounter = new ConcurrentHashMap<String, LongAdder>();
	public static Map<String, LongAdder> DownStatCounter = new ConcurrentHashMap<String, LongAdder>();
	public static Map<String, LongAdder> DownSuccessCounter = new ConcurrentHashMap<String, LongAdder>();

	public static Map<String, LongAdder> UpRequestCounter = new ConcurrentHashMap<String, LongAdder>();
	public static Map<String, LongAdder> UpStatCounter = new ConcurrentHashMap<String, LongAdder>();
	public static Map<String, LongAdder> UpSuccessCounter = new ConcurrentHashMap<String, LongAdder>();

	@Override
	public void run(String... args) throws Exception {
		/*聚合报表数据*/
		singleThreadService4Aggregation.execute(new ReportAggregation(REPORT_AGGREGATION_QUEUE));

		/*统计客户每秒的发送量、回执量、成功量*/
		singleThreadService4DownPortCounter.scheduleAtFixedRate(new DownPortAdder(jdbcTemplate), 0, 1 , TimeUnit.SECONDS);

		/*记录上行短信*/
		singleThreadService4Mo.scheduleAtFixedRate(new MoToDb(jdbcTemplate),0,1, TimeUnit.MINUTES);

		/*报表统计*/
		dbService.execute(new ServerRequestDelayQueueConsumer(jdbcTemplate, DB_SERVER_REQUEST_DELAY_QUEUE));
		dbService.execute(new ServerRequestDelayQueueConsumer(jdbcTemplate, DB_SERVER_REQUEST_DELAY_QUEUE));

		dbService.execute(new UpdateDelayQueueConsumer(jdbcTemplate, DB_UPDATE_DELAY_QUEUE));
		dbService.execute(new UpdateDelayQueueConsumer(jdbcTemplate, DB_UPDATE_DELAY_QUEUE));
	}

}
