package dorox.app.thread;

import dorox.app.ReportMain;
import dorox.app.count.CountTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.LongAdder;

/**
 * 批量插入
 */
public class DownPortAdder extends Thread{
	private static final Logger logger = LoggerFactory.getLogger(DownPortAdder.class);
    private JdbcTemplate jdbcTemplate;

	//private static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public DownPortAdder(JdbcTemplate jdbcTemplate){
		this.jdbcTemplate = jdbcTemplate;
	}

	private List<Object[]> insertParam = new ArrayList<>();

	private String sqlDown = "insert into down_minute_report (down_port_code,total_num,create_date, type) values(?,?,?,?)";
	private String sqlUp = "insert into up_minute_report (up_port_code,total_num,create_date, type) values(?,?,?,?)";

	@Override
	public void run() {
		long currentDate = System.currentTimeMillis();
		downPortAdder(currentDate);
		upPortAdder(currentDate);
	}

	private void upPortAdder(long currentDate){

		for(Entry<String, LongAdder> en : ReportMain.UpRequestCounter.entrySet()){
			String code = en.getKey();
			long num = en.getValue().sumThenReset();
			if(num > 0){
				Object[] obj = new Object[4];
				obj[0] = code; obj[1] = num; obj[2]=currentDate;obj[3] = CountTypeEnum.serverRequest.getType();
				insertParam.add(obj);
			}
		}
		for(Entry<String, LongAdder> en : ReportMain.UpStatCounter.entrySet()){
			String code = en.getKey();
			long num = en.getValue().sumThenReset();
			if(num > 0){
				Object[] obj = new Object[4];
				obj[0] = code; obj[1] = num; obj[2]=currentDate;obj[3] = CountTypeEnum.stat.getType();
				insertParam.add(obj);
			}
		}
		for(Entry<String, LongAdder> en : ReportMain.UpSuccessCounter.entrySet()){
			String code = en.getKey();
			long num = en.getValue().sumThenReset();
			if(num > 0){
				Object[] obj = new Object[4];
				obj[0] = code; obj[1] = num; obj[2]=currentDate;obj[3] = CountTypeEnum.success.getType();
				insertParam.add(obj);
			}
		}
		try {
			int[] ints = jdbcTemplate.batchUpdate(sqlUp, insertParam);
			if(ints.length>0) {
				logger.info("ints.length:{}", ints.length);
			}
		} catch (Exception e) {
			for(Object[] obj : insertParam){
				logger.info("obj[0]:{},obj[1]:{},obj[2]:{},obj[3]:{}", obj[0], obj[1], obj[2], obj[3]);
			}
			logger.info("sql:{}", sqlUp);
			logger.info("exception:{}", e);
		}
		insertParam.clear();
	}

	private void downPortAdder(long currentDate){

		for(Entry<String, LongAdder> en : ReportMain.DownRequestCounter.entrySet()){
			String code = en.getKey();
			long num = en.getValue().sumThenReset();
			if(num > 0){
				Object[] obj = new Object[4];
				obj[0] = code; obj[1] = num; obj[2]=currentDate;obj[3] = CountTypeEnum.serverRequest.getType();
				insertParam.add(obj);
			}
		}
		for(Entry<String, LongAdder> en : ReportMain.DownStatCounter.entrySet()){
			String code = en.getKey();
			long num = en.getValue().sumThenReset();
			if(num > 0){
				Object[] obj = new Object[4];
				obj[0] = code; obj[1] = num; obj[2]=currentDate;obj[3] = CountTypeEnum.stat.getType();
				insertParam.add(obj);
			}
		}
		for(Entry<String, LongAdder> en : ReportMain.DownSuccessCounter.entrySet()){
			String code = en.getKey();
			long num = en.getValue().sumThenReset();
			if(num > 0){
				Object[] obj = new Object[4];
				obj[0] = code; obj[1] = num; obj[2]=currentDate;obj[3] = CountTypeEnum.success.getType();
				insertParam.add(obj);
			}
		}
		try {
			int[] ints = jdbcTemplate.batchUpdate(sqlDown, insertParam);
			if(ints.length>0) {
				logger.info("ints.length:{}", ints.length);
			}
		} catch (Exception e) {
			for(Object[] obj : insertParam){
				logger.info("obj[0]:{},obj[1]:{},obj[2]:{},obj[3]:{}", obj[0], obj[1], obj[2], obj[3]);
			}
			logger.info("sql:{}", sqlDown);
			logger.info("exception:{}", e);
		}
		insertParam.clear();
	}
	
}
