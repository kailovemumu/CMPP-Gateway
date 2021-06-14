package dorox.app.thread;

import dorox.app.ReportMain;
import dorox.app.mq.event.PortMoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MoToDb implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(MoToDb.class);

	public static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private JdbcTemplate jdbcTemplate;

	public MoToDb(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void run() {
		try {
			moToDb();
		}catch (Exception e){
			logger.error("exception: {}", e);
		}
	}

	private void moToDb() {
		logger.info("mo message to db...");

		PortMoEvent portMoEvent;
		List<Object[]> splitUpNames = new ArrayList<>();
		while(( portMoEvent = ReportMain.DB_PORT_MO_QUEUE.poll())!=null){
			Object[] obj = new Object[5];
			obj[0] = portMoEvent.getDownName();
			obj[1] = portMoEvent.getDestId();
			obj[2] = portMoEvent.getContent();
			obj[3] = portMoEvent.getPhone();
			obj[4] = sdf2.format(new Date(portMoEvent.getMakeTime()));
			splitUpNames.add(obj);
		}
		if(splitUpNames.size()>0){
			try {
				jdbcTemplate.batchUpdate(
					"INSERT INTO mo_report(DOWN_PORT_CODE, SRC_ID, CONTENT, PHONE, MAKE_TIME ) VALUES (?,?,?,?,?)", splitUpNames);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
