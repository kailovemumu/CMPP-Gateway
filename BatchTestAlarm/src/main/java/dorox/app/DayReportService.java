package dorox.app;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 *	客户和通道的告警参数，各三个：时间周期、阈值、周期数
 *	告警策略：
 *	1，在一个时间周期内，成功率低于阈值，产生一条告警
 *	2，在周期数内，成功率连续下降，产生一条告警
 *	3，短信量在 100条内
 *
 *	方案：
 *	直接此处逻辑处理，
 *	
 */

@Component
public class DayReportService   {
    private static final Logger logger = LoggerFactory.getLogger(DayReportService.class);
	
	@Value("${downcustom.alarm_up_port_code}")//使用的通道
    private String alarmUpPortCode;
	@Value("${downcustom.alarm_up_port_code_srcId}")//接入码
    private String srcId;
	@Value("${downcustom.alarm_phones}")//发送号码
    private String phones;
	
	//取告警个数和多久取一个
	@Value("${downcustom.alarm_sample_num}")//采样15个
    private int SAMPLE_NUM;
	@Value("${downcustom.alarm_sample_time}")//每15s采样一次
    private int SAMPLE_TIME;
	
	//记录告警发送时间
	private static Map<String, Long> LAST_ALARM_TIME = new HashMap<>();
	//报表更新周期15分钟
	private static final int REPORT_PERIOD = 5;
	//告警产生，需要通道最小发送量
	private static final int MIN_ALARM_TOTAL_COUNT = 300;

	//记录最后采样时间
	private static Map<String, Long> LAST_SAMPLE_TIME = new HashMap<>();
	//记录每个端口最近几次的成功率采样，如果连续下降产生告警
	private static Map<String, LinkedList<Double>> DOWN_PORT_SAMPLE = new HashMap<>();
	
	@Resource
    private JdbcTemplate jdbcTemplate;

    private static final String DETAIL_TABLE_NAME = "detail_report_";
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	private static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd");
	
	private static Map<String, Long> currentDownCount = new HashMap<>();
	private static Map<String, Long> currentUpCount = new HashMap<>();
	private static Map<String, Long> currentDownSuccessCount = new HashMap<>();
	private static Map<String, Long> currentUpSuccessCount = new HashMap<>();

	@Scheduled(initialDelay = 1000, fixedDelay = REPORT_PERIOD * 60 * 1000)
	public void dayReport() {
		try {
			logger.info("dayReport on : dayReport");
			downDayReport(0,false);
			upDayReport(0,false);
			errDayReport();//统计失败表
		}catch(Exception e) {
			logger.error("exception:{}",e);
		}
	}

	private void errDayReport() {
		Date date = new Date();
		String tableName = DETAIL_TABLE_NAME + sdf.format(date);
		jdbcTemplate.update("delete from err_report");

		List<Object[]> paramList = new ArrayList<>();
		String sql = "select stat, sum(msg_count) num from "+tableName+" group by stat";
		jdbcTemplate.query(sql, new Object[]{}, new RowMapper<Object>() {
			@Override
			public Object mapRow(ResultSet rs, int arg1) throws SQLException {
				String stat = rs.getString("stat");
				int num = rs.getInt("num");
				Object[] obj = new Object[4];
				obj[2] = stat;
				obj[3] = num;
				paramList.add(obj);
				return null;
			}
		});

		sql = "select stat, sum(msg_count) num, down_port_code from "+tableName+" group by stat, down_port_code";
		jdbcTemplate.query(sql, new Object[]{}, new RowMapper<Object>() {
			@Override
			public Object mapRow(ResultSet rs, int arg1) throws SQLException {
				String downPortCode = rs.getString("down_port_code");
				String stat = rs.getString("stat");
				int num = rs.getInt("num");
				Object[] obj = new Object[4];
				obj[0] = downPortCode;
				obj[2] = stat;
				obj[3] = num;
				paramList.add(obj);
				return null;
			}
		});

		sql = "select stat, sum(msg_count) num, up_port_code from "+tableName+" group by stat, up_port_code";
		jdbcTemplate.query(sql, new Object[]{}, new RowMapper<Object>() {
			@Override
			public Object mapRow(ResultSet rs, int arg1) throws SQLException {
				String up_port_code = rs.getString("up_port_code");
				String stat = rs.getString("stat");
				int num = rs.getInt("num");
				Object[] obj = new Object[4];
				obj[1] = up_port_code;
				obj[2] = stat;
				obj[3] = num;
				paramList.add(obj);
				return null;
			}
		});
		sql = "select stat, sum(msg_count) num, up_port_code, down_port_code from "+tableName+" group by stat, up_port_code, down_port_code";
		jdbcTemplate.query(sql, new Object[]{}, new RowMapper<Object>() {
			@Override
			public Object mapRow(ResultSet rs, int arg1) throws SQLException {
				String downPortCode = rs.getString("down_port_code");
				String up_port_code = rs.getString("up_port_code");
				String stat = rs.getString("stat");
				int num = rs.getInt("num");
				Object[] obj = new Object[4];
				obj[0] = downPortCode;
				obj[1] = up_port_code;
				obj[2] = stat;
				obj[3] = num;
				paramList.add(obj);
				return null;
			}
		});

		if(paramList.size()>0){
			try {
				jdbcTemplate.batchUpdate(
						"INSERT INTO err_report(DOWN_PORT_CODE, UP_PORT_CODE, stat, num) VALUES (?,?,?,?)", paramList);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

//	@Scheduled(cron="0 30 22 * * ? ")
//	public void lastDayReport() {
//		try {
//			logger.info("dayReport on : lastDayReport");
//			downDayReport(-1,false);
//			upDayReport(-1,false);
//		}catch(Exception e) {
//			logger.error("exception:{}",e);
//		}
//	}

//	@Scheduled(cron="0 00 23 * * ? ")
//	public void last2DayReport() {
//		try {
//			logger.info("dayReport on : last2DayReport");
//			downDayReport(-2,false);
//			upDayReport(-2,false);
//		}catch(Exception e) {
//			logger.error("exception:{}",e);
//		}
//	}

//	@Scheduled(cron="0 30 23 * * ? ")
//	public void last3DayReport() {
//		try {
//			logger.info("dayReport on : last2DayReport");
//			downDayReport(-3,false);
//			upDayReport(-3, false);
//		}catch(Exception e) {
//			logger.error("exception:{}",e);
//		}
//	}

	public void downDayReport(int amount, boolean alarm) {
		Date date = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DAY_OF_MONTH, amount);
		final Date now = c.getTime();

		String tableName = DETAIL_TABLE_NAME + sdf.format(now);
		String sql = "SELECT DOWN_PORT_CODE, sum(MSG_COUNT) TOTAL FROM `" + tableName + "` group by DOWN_PORT_CODE";

		jdbcTemplate.query(sql, new Object[]{}, 
			(ResultSet rs, int rowNum)->{
				String downPortCode = rs.getString("DOWN_PORT_CODE");
				if(StringUtils.isEmpty(downPortCode) || "null".equals(downPortCode)){
					return null;
				}

				long total = rs.getLong("TOTAL");

				String successSql = "select IFNULL(sum(MSG_COUNT),0) from " + 
						tableName+" where DOWN_PORT_CODE='" + downPortCode + "' and stat='DELIVRD'";
				long success = jdbcTemplate.queryForObject(successSql, Long.class);
				//记录total值或success值，如果有改变，则更新记录。
				if(((currentDownCount.get(downPortCode) == null || 
					currentDownCount.get(downPortCode) != total) && total > 0) ||
					((currentDownSuccessCount.get(downPortCode) == null || 
					currentDownSuccessCount.get(downPortCode) != success) && success > 0)){

					String successPriceSql = "select IFNULL(sum(MSG_COUNT*DOWN_PRICE),0) from " + 
								tableName+" where DOWN_PORT_CODE='" + downPortCode + "' and stat='DELIVRD'";
					long totalPirce = jdbcTemplate.queryForObject(successPriceSql, Long.class);
						
					String downCustomSql = "select down_custom_id from down_port where DOWN_NAME='" + downPortCode + "'";
					String downCustomId = jdbcTemplate.queryForObject(downCustomSql, String.class);

					//更新down_day_report日表
					jdbcTemplate.update("delete from down_day_report where IN_DATE = '" + sdf3.format(now) + "' and DOWN_PORT_CODE='" + downPortCode + "'");
					String dayReportInsert = "insert into down_day_report "
							+ "(id,DOWN_CUSTOM_ID,DOWN_PORT_CODE,IN_DATE,TOTAL_NUM,SUCCESS_NUM,FAILED_NUM,CREATE_BY,CREATE_DATE,UPDATE_BY,UPDATE_DATE,TOTAL_PRICE) values('"
							+ UUID.randomUUID().toString().replaceAll("-", "") + "','"	+ downCustomId + "','"+downPortCode+"','"
							+ sdf2.format(now) + "'," + total + "," + success + "," + (total - success)
							+ ",1,'" + sdf2.format(now) + "',1,'" + sdf2.format(now) + "'," + totalPirce + ")";
					jdbcTemplate.update(dayReportInsert);

					//更新downPort表中的金额：current_true_money字段
					long downPortTotalPirce = jdbcTemplate.queryForObject(
			"select IFNULL(sum(TOTAL_PRICE),0) from down_day_report where DOWN_PORT_CODE = '" +
					downPortCode+"'", Long.class);
					long downPortTotalBill = jdbcTemplate.queryForObject(
			"select IFNULL(sum(bill_true_amount),0) from down_custom_bill where DOWN_PORT_CODE = '" +
					downPortCode+"'", Long.class);
					jdbcTemplate.update("update down_port set current_true_money = " +
			(downPortTotalBill - downPortTotalPirce)+ " where down_name = '" +
					downPortCode+"'");

					currentDownCount.put(downPortCode, total) ;
					currentDownSuccessCount.put(downPortCode, success) ;
					
					//成功率
					//总量中排除未知短信
					String totalSql = "select IFNULL(sum(MSG_COUNT),0) from " + 
							tableName+" where DOWN_PORT_CODE='" + downPortCode + 
							"' and stat!='接收成功'";
					long hasStatTotal = jdbcTemplate.queryForObject(totalSql, Long.class);
					double rate = Double.valueOf(success)/Double.valueOf(hasStatTotal);
					
					logger.info("downPortCode:{},success:{},total:{}", downPortCode, success, total);

					if (alarm == false) {
						return null;
					}

					//告警阈值要从down_Port表中获取
					Map<String, Object> downMap = jdbcTemplate.queryForMap(
							"select alarm, alarm_period_length from down_port where down_name = '" +
							downPortCode+"'");
					String downRateStr = String.valueOf(downMap.get("alarm"));
					int periodLengthStr = Integer.valueOf(String.valueOf(downMap.get("alarm_period_length")));
					if(!StringUtils.isEmpty(downRateStr)){
						double downRate = Double.valueOf(downRateStr);
						if(downRate > 0){
							if(total > MIN_ALARM_TOTAL_COUNT && (rate * 100) < downRate ){
								Long lastTime = LAST_ALARM_TIME.get(downPortCode);
								if(lastTime == null) {
									updateAlarmTable("客户", phones, downPortCode,
											rate, sdf2.format(now));
									
									lastTime = System.currentTimeMillis();
									LAST_ALARM_TIME.put(downPortCode, lastTime);
								}else if(System.currentTimeMillis()-lastTime > periodLengthStr*60*1000){
									updateAlarmTable("客户", phones, downPortCode,
											rate, sdf2.format(now));
									lastTime = System.currentTimeMillis();
									LAST_ALARM_TIME.put(downPortCode, lastTime);
								}
							}
						}
					}
					
					//采样处理
					//判断采样时间
					boolean isSample = false;
					Long lastSample = LAST_SAMPLE_TIME.get(downPortCode);
					if(lastSample == null) {
						isSample = true;
						lastSample = System.currentTimeMillis();
						LAST_SAMPLE_TIME.put(downPortCode, lastSample);
					}else if(System.currentTimeMillis()-lastSample > SAMPLE_TIME*1000) {
						isSample = true;
						lastSample = System.currentTimeMillis();
						LAST_SAMPLE_TIME.put(downPortCode, lastSample);
					}
					
					//采样
					if(isSample) {
						LinkedList<Double> downSampleList = DOWN_PORT_SAMPLE.get(downPortCode);
						if(downSampleList == null) {
							downSampleList = new LinkedList<Double>();
							DOWN_PORT_SAMPLE.put(downPortCode,downSampleList);
						}
						downSampleList.add(rate);
						//删除多余采样数据
						if(downSampleList.size() > SAMPLE_NUM) {
							downSampleList.clear();
						}

						//判断采样产生告警
//						logger.info("downPortCode:{},sample:{}", downPortCode, DOWN_PORT_SAMPLE);
						if(downSampleList.size() >= SAMPLE_NUM && isSorted(downSampleList)) {
							updateSampleAlarmTable("客户", phones, downPortCode,
									sdf2.format(now));
							DOWN_PORT_SAMPLE.remove(downPortCode);
						}
					}
				}
				return null;
			}
		);
	}
	public void upDayReport(int amount, boolean alarm) {
		
		Date date = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DAY_OF_MONTH, amount);
		final Date now = c.getTime();

		String tableName = DETAIL_TABLE_NAME + sdf.format(now);
		String sql = "SELECT UP_PORT_CODE, sum(MSG_COUNT) TOTAL FROM `" + tableName + "` group by UP_PORT_CODE";

		jdbcTemplate.query(sql, new Object[]{}, 
			(ResultSet rs, int rowNum)->{
				String upPortCode = rs.getString("UP_PORT_CODE");
				if(!StringUtils.isEmpty(upPortCode)){
					long total = rs.getLong("TOTAL");

					String successSql = "select IFNULL(sum(MSG_COUNT),0) from " +
							tableName+" where UP_PORT_CODE='" + upPortCode + "' and stat='DELIVRD'";
					long success = jdbcTemplate.queryForObject(successSql, Long.class);

					//记录total或success值，如果有改变，则更新记录。
					if(((currentUpCount.get(upPortCode) == null || 
						currentUpCount.get(upPortCode) != total) && total > 0) ||
						((currentUpSuccessCount.get(upPortCode) == null || 
						currentUpSuccessCount.get(upPortCode) != success) && success > 0)	){

						String successPriceSql = "select IFNULL(sum(MSG_COUNT*UP_PRICE),0) from " + 
								tableName+" where UP_PORT_CODE='" + upPortCode + "' and stat='DELIVRD'";
						long totalPirce = jdbcTemplate.queryForObject(successPriceSql, Long.class);
						
						String upCustomSql = "select up_custom_id from up_port where UP_NAME='" + upPortCode + "'";
						String upCustomId = jdbcTemplate.queryForObject(upCustomSql, String.class);

						//更新up_day_report日表
						jdbcTemplate.update("delete from up_day_report where IN_DATE = '" + sdf3.format(now) + "' and UP_PORT_CODE='" + upPortCode + "'");
						String dayReportInsert = "insert into up_day_report "
								+ "(id,UP_CUSTOM_ID,UP_PORT_CODE,IN_DATE,TOTAL_NUM,SUCCESS_NUM,FAILED_NUM,CREATE_BY,CREATE_DATE,UPDATE_BY,UPDATE_DATE,TOTAL_PRICE) values('"
								+ UUID.randomUUID().toString().replaceAll("-", "") + "','"	+ upCustomId + "','"+upPortCode+"','"
								+ sdf2.format(now) + "'," + total + "," + success + "," + (total - success)
								+ ",1,'" + sdf2.format(now) + "',1,'" + sdf2.format(now) + "'," + totalPirce + ")";
						jdbcTemplate.update(dayReportInsert);
						currentUpCount.put(upPortCode, total);
						currentUpSuccessCount.put(upPortCode, success);

						//成功率
						String totalSql = "select IFNULL(sum(MSG_COUNT),0) from " + 
								tableName+" where UP_PORT_CODE='" + upPortCode + 
								"' and stat!='接收成功'";
						long hasStatTotal = jdbcTemplate.queryForObject(totalSql, Long.class);
						double rate = Double.valueOf(success)/Double.valueOf(hasStatTotal);
						
//						double rate = Double.valueOf(success)/Double.valueOf(total);

						logger.info("upPortCode:{},success:{},total:{}", upPortCode, success, total);

						if (alarm == false) {
							return null;
						}
						
						//阈值要从up_Port表中获取
						Map<String, Object> upMap = jdbcTemplate.queryForMap(
								"select alarm, alarm_period_length from up_port where up_name = '" +
								upPortCode+"'");
						String upRateStr = String.valueOf(upMap.get("alarm"));
						int periodLengthStr = Integer.valueOf(String.valueOf(upMap.get("alarm_period_length")));
						if(!StringUtils.isEmpty(upRateStr)){
							double downRate = Double.valueOf(upRateStr);
							if(downRate > 0){
								if(total > MIN_ALARM_TOTAL_COUNT && (rate * 100) < downRate ){
									Long lastTime = LAST_ALARM_TIME.get(upPortCode);
									if(lastTime == null) {
										updateAlarmTable("通道", phones, upPortCode,
												rate, sdf2.format(now));
										lastTime = System.currentTimeMillis();
										LAST_ALARM_TIME.put(upPortCode, lastTime);
									}else if(System.currentTimeMillis()-lastTime > periodLengthStr*60*1000){
										updateAlarmTable("通道", phones, upPortCode,
												rate, sdf2.format(now));
										lastTime = System.currentTimeMillis();
										LAST_ALARM_TIME.put(upPortCode, lastTime);
									}
								}
							}
						}
						
						//采样处理
						//判断采样时间
						boolean isSample = false;
						Long lastSample = LAST_SAMPLE_TIME.get(upPortCode);
						if(lastSample == null) {
							isSample = true;
							lastSample = System.currentTimeMillis();
							LAST_SAMPLE_TIME.put(upPortCode, lastSample);
						}else if(System.currentTimeMillis()-lastSample > SAMPLE_TIME*1000) {
							isSample = true;
							lastSample = System.currentTimeMillis();
							LAST_SAMPLE_TIME.put(upPortCode, lastSample);
						}
						
						//采样
						if(isSample) {
							LinkedList<Double> downSampleList = DOWN_PORT_SAMPLE.get(upPortCode);
							if(downSampleList == null) {
								downSampleList = new LinkedList<Double>();
								DOWN_PORT_SAMPLE.put(upPortCode,downSampleList);
							}
							downSampleList.add(rate);
							//删除多余采样数据
							if(downSampleList.size() > SAMPLE_NUM) {
								downSampleList.clear();
							}

							//判断采样产生告警
//							logger.info("downPortCode:{},sample:{}", upPortCode, DOWN_PORT_SAMPLE);
							if(downSampleList.size() >= SAMPLE_NUM && isSorted(downSampleList)) {
								updateSampleAlarmTable("客户", phones, upPortCode,
										sdf2.format(now));
								DOWN_PORT_SAMPLE.remove(upPortCode);
							}
						}
					}
				}
				return null;
			}
		);
	}
	
	
	
	public static boolean isSorted(List<Double> listOfStrings) {

		Iterator<Double> iter = listOfStrings.iterator();
		Double current, previous = iter.next();
		while (iter.hasNext()) {
			current = iter.next();
			if (previous.compareTo(current) <= 0) {
				return false;
			}
			previous = current;
		}
		return true;
	}

	private void updateAlarmTable(String name, String phones, String downPortCode,
			double rate, String dateFormat) {
		jdbcTemplate.update("insert into alarm_test_message " +
				"(id,up_port_code,phone,content,message_type,status,create_date,down_port_code,src_id) " +
				"values('"+UUID.randomUUID().toString().replaceAll("-", "") +
				"','" + alarmUpPortCode + 
				"','" + phones + 
				"','【告警信息】"+name+"：【" + downPortCode + "】 告警，发送成功率为:" + 
				String.format("%.2f", rate) + "','1','1','" +
				dateFormat + "','123458','" + srcId + "')");
	}

	private void updateSampleAlarmTable(String name, String phones, String downPortCode,
			String dateFormat) {
		jdbcTemplate.update("insert into alarm_test_message " +
				"(id,up_port_code,phone,content,message_type,status,create_date,down_port_code,src_id) " +
				"values('"+UUID.randomUUID().toString().replaceAll("-", "") +
				"','" + alarmUpPortCode + 
				"','" + phones + 
				"','【告警信息】"+name+"：【" + downPortCode + "】 告警，发送成功率持续下降','1','1','" +
				dateFormat + "','123458','" + srcId + "')");
	}
}
