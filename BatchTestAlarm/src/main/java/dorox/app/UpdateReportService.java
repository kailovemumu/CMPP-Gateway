package dorox.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 每天晚上更新4天前的发送量
 */
@Component
public class UpdateReportService {
    private static final Logger logger = LoggerFactory.getLogger(UpdateReportService.class);

	//日志路径
	@Value("${report.log.path}")
	private String reportLogPath;
	
	@Resource
    private JdbcTemplate jdbcTemplate;

    private static final String REPORT_FILE_NAME = "report.log.";
	private static SimpleDateFormat sdf = new SimpleDateFormat("MMdd");
	private static SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd");


	@Scheduled(cron = "0 0 23 * * ? ")
	public void updateReport() {
		try {
			logger.info("updateReport");

			Date date = new Date();
			Map<String, String> reportMap = new HashMap<>();
			Map<String, Set<String>> reportSet = new HashMap<>();
			//4天前日期
			String dateStr = getDayStr(-3, date, sdf3);
			//获取需要统计的通道数据
			jdbcTemplate.query("SELECT id, down_port_code, up_port_code FROM channel_day_report where in_date='"+dateStr+"'",
					(ResultSet rs, int rowNum)->{
						String id = rs.getString("id");
						String downName = rs.getString("down_port_code");
						String upName = rs.getString("up_port_code");
						reportMap.put(downName+upName, id);
						return null;
					}
			);

			//匹配文件
			List<File> files = getFiles(getDayStr(-3, date, sdf3),getDayStr(-2, date, sdf3),
					getDayStr(-1, date, sdf3),getDayStr(0, date, sdf3));

			//匹配messageId中的时间戳
			String dateMatchStr = getDayStr(-3, date, sdf);
			//解析文件
			//PortStatHandler - portStat:{"messageId": "H1041800K72FMRP0", "downName": "910034", "upName": "790937",
			// "phone": "18773877616", "msgId": "0", "seqId": "1823752723", "stat": "DELIVRD", "srcId": "",
			// "downRegionCode": "120httpserver", "makeTime": 1618675314936}
			for(File  file : files){
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line = null;
				while((line = br.readLine()) != null){
					if( checkLine(line, dateMatchStr)){
						String[] items = line.split("\", \"");

						String downName = null, upName = null, msgId = null, seqId = null;
						for(String item : items){
							if(item.startsWith("downName")){
								downName = item.substring("downName".length() + 4);
							}else if(item.startsWith("upName")){
								upName = item.substring("upName".length() + 4);
							}else if(item.startsWith("msgId")){
								msgId = item.substring("msgId".length() + 4);
							}else if(item.startsWith("seqId")){
								seqId = item.substring("seqId".length() + 4);
							}
						}

						Set<String> set = reportSet.get(downName+upName);
						if(set == null){
							set = new HashSet<>();
							reportSet.put(downName+upName, set);
						}
						set.add(msgId + seqId);
					}
				}
			}
			for(Map.Entry<String, String> en : reportMap.entrySet()){
				String key = en.getKey();
				String id = en.getValue();
				Set<String> sumSet = reportSet.get(key);
				int num = 0;
				if(sumSet!=null) {
					num=sumSet.size();
				}

				jdbcTemplate.update("update channel_day_report set real_success_num=" + num + " where id='" + id + "'");

			}
		}catch(Exception e) {
			logger.error("exception:{}",e);
		}
	}

	private boolean checkLine(String line, String dateMatchStr) {
		return line.contains("PortStatHandler") && line.contains("portStat") && line.contains("DELIVRD") &&
				dateMatchStr.equals(line.substring(line.indexOf("messageId")+"messageId".length()+6,line.indexOf("messageId")+"messageId".length()+10));
	}

	private List<File> getFiles(String dateStr3, String dateStr2, String dateStr1, String dateStr) {
		List<File> list = new ArrayList<>();
		File reportDir = new File(reportLogPath);
		for(File file : reportDir.listFiles()){
			if(file.getName().contains(REPORT_FILE_NAME) && (file.getName().contains(dateStr3) || file.getName().contains(dateStr2) ||
					file.getName().contains(dateStr1) || file.getName().contains(dateStr))){
				list.add(file);
			}
		}
		logger.info("file list:{}", list);
		return list;
	}

	private static String getDayStr(int shift, Date date, SimpleDateFormat sdf) {

		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DAY_OF_MONTH, shift);
		return sdf.format(c.getTime());
	}

	public static void main(String[] args) {
		String line = "2021-04-18 00:00:42.266 [org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#1-29] INFO  dorox.app.consumer.PortStatHandler - portStat:{\"messageId\": \"S1041800623WN7C0\", \"downName\": \"800012\", \"upName\": \"dlzz2_sy\", \"phone\": \"17671627840\", \"msgId\": \"0418000038001853143711\", \"seqId\": \"302704437904180000382059994120\", \"stat\": \"DELIVRD\", \"srcId\": \"106511000318\", \"downRegionCode\": \"120cmppserver\", \"makeTime\": 1618675242253}";
		String ss = line.substring(line.indexOf("messageId")+"messageId".length()+6,line.indexOf("messageId")+"messageId".length()+10);
		System.out.println(ss);

		String[] items = line.split("\", \"");
		for(String item : items){
			System.out.println(item);
		}
	}

}
