package dorox.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *	客户和通道的告警参数，各三个：时间周期、阈值、周期数
 *	告警策略：
 *	1，在一个时间周期内，成功率低于阈值，产生一条告警
 *	2，在周期数内，成功率连续下降，产生一条告警
 *	3，短信量在 100条内
 *
 *	方案：
 *	直接此处逻辑处理
 *  代替DayReportService
 *  downdayreport和updayreport合并
 *
 *	
 */

@Component
public class ChannelDayReportService {
    private static final Logger logger = LoggerFactory.getLogger(ChannelDayReportService.class);
	
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
	//报表更新周期5分钟
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
	
	private static Map<String, Long> currentChannelCount = new HashMap<>();
	private static Map<String, Long> currentChannelSuccessCount = new HashMap<>();

	@Scheduled(initialDelay = 1000, fixedDelay = REPORT_PERIOD * 60 * 1000)
	public void dayReport() {
		try {
			logger.info("dayReport on : dayReport");
			channelDayReport(0,true);
		}catch(Exception e) {
			logger.error("exception:{}",e);
		}
	}

	public void channelDayReport(int amount, boolean alarm) {
		Date date = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DAY_OF_MONTH, amount);
		final Date now = c.getTime();

		String tableName = DETAIL_TABLE_NAME + sdf.format(now);
		String sql = "SELECT DOWN_PORT_CODE,UP_PORT_CODE, sum(MSG_COUNT) TOTAL FROM `" + tableName + "` group by DOWN_PORT_CODE, UP_PORT_CODE";

		jdbcTemplate.query(sql, new Object[]{}, 
			(ResultSet rs, int rowNum)->{
				String downPortCode = rs.getString("DOWN_PORT_CODE");
				String upPortCode = rs.getString("UP_PORT_CODE");
				if(StringUtils.isEmpty(downPortCode) || "null".equals(downPortCode) || StringUtils.isEmpty(upPortCode) || "null".equals(upPortCode)){
					return null;
				}

				long total = rs.getLong("TOTAL");

				String successSql = "select IFNULL(sum(MSG_COUNT),0) success, IFNULL(sum(MSG_COUNT*DOWN_PRICE),0) totalPirce from " +
						tableName+" where DOWN_PORT_CODE='" + downPortCode + "' and UP_PORT_CODE='" + upPortCode + "' and stat='DELIVRD'";

				Map successMap = jdbcTemplate.queryForMap(successSql);
				BigDecimal successBig = (BigDecimal)successMap.get("success");
				BigDecimal totalPirceBig = (BigDecimal)successMap.get("totalPirce");
				long success = successBig.longValue();
				long totalPirce = totalPirceBig.longValue();
				//记录total值或success值，如果有改变，则更新记录。
				if(((currentChannelCount.get(downPortCode+upPortCode) == null ||
					currentChannelCount.get(downPortCode+upPortCode) != total) && total > 0) ||
					((currentChannelSuccessCount.get(downPortCode+upPortCode) == null ||
					currentChannelSuccessCount.get(downPortCode+upPortCode) != success) && success > 0)){

					String downCustomSql = "select down_custom_id from down_port where DOWN_NAME='" + downPortCode + "'";
					String downCustomId = jdbcTemplate.queryForObject(downCustomSql, String.class);

					//更新channel_day_report日表
//					删除旧数据，插入新数据。
					jdbcTemplate.update("delete from channel_day_report where IN_DATE = '" + sdf3.format(now) +
							"' and DOWN_PORT_CODE='" + downPortCode + "' and UP_PORT_CODE='"+upPortCode+"'");
					String dayReportInsert = "insert into channel_day_report "
							+ "(id,DOWN_CUSTOM_ID,DOWN_PORT_CODE,UP_PORT_CODE,IN_DATE,TOTAL_NUM,SUCCESS_NUM,FAILED_NUM,CREATE_BY,CREATE_DATE,UPDATE_BY,UPDATE_DATE,TOTAL_PRICE) values('"
							+ UUID.randomUUID().toString().replaceAll("-", "") + "','"	+ downCustomId + "','"
							+ downPortCode + "','" + upPortCode + "','"
							+ sdf2.format(now) + "'," + total + "," + success + "," + (total - success)
							+ ",1,'" + sdf2.format(now) + "',1,'" + sdf2.format(now) + "'," + totalPirce + ")";
					jdbcTemplate.update(dayReportInsert);

					//更新downPort表中的金额：current_true_money字段
					long downPortTotalPirce = jdbcTemplate.queryForObject(
			"select IFNULL(sum(TOTAL_PRICE),0) from channel_day_report where DOWN_PORT_CODE = '" +
					downPortCode+"'", Long.class);
//					统计账单
					long downPortTotalBill = jdbcTemplate.queryForObject(
			"select IFNULL(sum(bill_true_amount),0) from down_custom_bill where DOWN_PORT_CODE = '" +
					downPortCode+"'", Long.class);
					jdbcTemplate.update("update down_port set current_true_money = " +
			(downPortTotalBill - downPortTotalPirce)+ " where down_name = '" +
					downPortCode+"'");

					currentChannelCount.put(downPortCode, total);
					currentChannelSuccessCount.put(downPortCode, success);
					
					//成功率
					//总量中排除未知短信（没有返回值的会不考虑）
					String totalSql = "select IFNULL(sum(MSG_COUNT),0) from " +
							tableName+" where DOWN_PORT_CODE='" + downPortCode + 
							"' and UP_PORT_CODE='" + upPortCode + "' and stat!='接收成功'";
					long hasStatTotal = jdbcTemplate.queryForObject(totalSql, Long.class);
					double rate = Double.valueOf(success)/Double.valueOf(hasStatTotal);
					
					logger.info("downPortCode:{},success:{},total:{}", downPortCode, success, total);

//					一般都处理告警信息
					if (alarm == false) {
						return null;
					}

					//告警阈值要从down_Port表中获取
					Map<String, Object> downMap = jdbcTemplate.queryForMap(
							"select alarm, alarm_period_length from down_port where down_name = '" +
							downPortCode+"'");
					String downRateStr = String.valueOf(downMap.get("alarm"));
//					取出告警周期
					int periodLengthStr = Integer.valueOf(String.valueOf(downMap.get("alarm_period_length")));
					if(!StringUtils.isEmpty(downRateStr)){
						double downRate = Double.valueOf(downRateStr);
						if(downRate > 0){
//							总数大于最小告警量  或者  成功率低于配置的告警值
							if(total > MIN_ALARM_TOTAL_COUNT && (rate * 100) < downRate ){
								Long lastTime = LAST_ALARM_TIME.get(downPortCode+upPortCode);
//								记录上一次告警时间，并在这里判断是否有上一次的告警，防止一直发送告警信息。
								if(lastTime == null) {
									updateAlarmTable("通道", phones, downPortCode, upPortCode,
											rate, sdf2.format(now));
									
									lastTime = System.currentTimeMillis();
									LAST_ALARM_TIME.put(downPortCode+upPortCode, lastTime);
								}else if(System.currentTimeMillis()-lastTime > periodLengthStr*60*1000){
									updateAlarmTable("通道", phones, downPortCode, upPortCode,
											rate, sdf2.format(now));
									lastTime = System.currentTimeMillis();
									LAST_ALARM_TIME.put(downPortCode+upPortCode, lastTime);
								}
							}
						}
					}

					//告警阈值要从up_Port表中获取
					Map<String, Object> upMap = jdbcTemplate.queryForMap(
							"select alarm, alarm_period_length from up_port where up_name = '" +
									upPortCode+"'");
					String upRateStr = String.valueOf(upMap.get("alarm"));
					periodLengthStr = Integer.valueOf(String.valueOf(upMap.get("alarm_period_length")));
					if(!StringUtils.isEmpty(upRateStr)){
						double upRate = Double.valueOf(upRateStr);
						if(upRate > 0){
							if(total > MIN_ALARM_TOTAL_COUNT && (rate * 100) < upRate ){
								Long lastTime = LAST_ALARM_TIME.get(downPortCode+upPortCode);
								if(lastTime == null) {
									updateAlarmTable("通道", phones, downPortCode, upPortCode,
											rate, sdf2.format(now));

									lastTime = System.currentTimeMillis();
									LAST_ALARM_TIME.put(downPortCode+upPortCode, lastTime);
								}else if(System.currentTimeMillis()-lastTime > periodLengthStr*60*1000){
									updateAlarmTable("通道", phones, downPortCode, upPortCode,
											rate, sdf2.format(now));
									lastTime = System.currentTimeMillis();
									LAST_ALARM_TIME.put(downPortCode+upPortCode, lastTime);
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
						LinkedList<Double> downSampleList = DOWN_PORT_SAMPLE.get(downPortCode+upPortCode);
						if(downSampleList == null) {
							downSampleList = new LinkedList<Double>();
							DOWN_PORT_SAMPLE.put(downPortCode+upPortCode,downSampleList);
						}
						downSampleList.add(rate);
						//删除多余采样数据 如果sampleList 满了 清空 重新采集
						if(downSampleList.size() > SAMPLE_NUM) {
							downSampleList.clear();
						}

						//判断采样产生告警
//						logger.info("downPortCode:{},sample:{}", downPortCode, DOWN_PORT_SAMPLE);
//						判断是否是持续下降
						if(downSampleList.size() >= SAMPLE_NUM && isSorted(downSampleList)) {
							updateSampleAlarmTable("通道", phones, downPortCode, upPortCode,
									sdf2.format(now));
							DOWN_PORT_SAMPLE.remove(downPortCode+upPortCode);
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

	private void updateAlarmTable(String name, String phones, String downPortCode, String upPortCode,
			double rate, String dateFormat) {
		jdbcTemplate.update("insert into alarm_test_message " +
				"(id,up_port_code,phone,content,message_type,status,create_date,down_port_code,src_id) " +
				"values('"+UUID.randomUUID().toString().replaceAll("-", "") +
				"','" + alarmUpPortCode + 
				"','" + phones + 
				"','【告警信息】"+name+"：【" + downPortCode + "-" + upPortCode +"】 告警，发送成功率为:" +
				String.format("%.2f", rate) + "','1','1','" +
				dateFormat + "','123458','" + srcId + "')");
	}

	private void updateSampleAlarmTable(String name, String phones, String downPortCode, String upPortCode,
			String dateFormat) {
		jdbcTemplate.update("insert into alarm_test_message " +
				"(id,up_port_code,phone,content,message_type,status,create_date,down_port_code,src_id) " +
				"values('"+UUID.randomUUID().toString().replaceAll("-", "") +
				"','" + alarmUpPortCode + 
				"','" + phones + 
				"','【告警信息】"+name+"：【" + downPortCode+ "-" + upPortCode + "】 告警，发送成功率持续下降','1','1','" +
				dateFormat + "','123458','" + srcId + "')");
	}
}
