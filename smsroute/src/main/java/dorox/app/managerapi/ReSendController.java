package dorox.app.managerapi;

import dorox.app.mq.event.RouteRequestEvent;
import dorox.app.util.MqUtil;
import org.apache.avro.util.Utf8;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class ReSendController {

	@Value("#{${dianli.srcid}}")
	private Map<String, Map<Integer, String>> dianliSrcIds;

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private RabbitTemplate rabbitTemplate;

	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
	static String table_prefix = "detail_report_";

	/*
	 * *
	 */
	@RequestMapping(path="/resend") //
	public @ResponseBody Map<String,Object> resend (
			@RequestParam() String messageIds) {
		Map<String,Object> res = new HashMap<String,Object>();
		res.put("result", "1");
		res.put("result_msg", "success");
		String[] messageIdArray = messageIds.split(",");
		for (String messageId: messageIdArray){

			String table = getTableFromMessageId(messageId);
			Map<String, Object> map = jdbcTemplate.queryForMap(
					"select DOWN_PORT_CODE, UP_PORT_CODE, DOWN_REGION_CODE, UP_REGION_CODE, MSG_ID, PHONE, CONTENT, SRC_ID from " +
					table + "where message_Id='" + messageId + "'");

			String downName = (String)map.get("DOWN_PORT_CODE");
			String upName = (String)map.get("UP_PORT_CODE");
			String downRegionCode = (String)map.get("DOWN_REGION_CODE");
			String upRegionCode = (String)map.get("UP_REGION_CODE");
			String msgId = StringUtils.strip((String)map.get("MSG_ID"),"[]");

			List<Utf8> msgIds = new ArrayList<>();
			for(String mId : msgId.split(",")){
				msgIds.add(new Utf8(mId));
			}

			String phone = (String)map.get("PHONE");
			String content = (String)map.get("CONTENT");

			if(dianliSrcIds.containsKey(downName)){
				content = "【湖北电力】"+content;
			}
			String srcIds = (String)map.get("SRC_ID");
			String[] srcIdArray = srcIds.split("/");
			String srcId = srcIdArray[0], upSrcId=srcIdArray[1];

			RouteRequestEvent routeRequestEvent = new RouteRequestEvent(messageId, downName,
					upName, phone, content, srcId, upSrcId, msgIds,
					downRegionCode, upRegionCode);
			MqUtil.sendMsg(routeRequestEvent, rabbitTemplate, upRegionCode);
		}
		return res;
	}

	private String getTableFromMessageId(String messageId) {
		String y = sdf.format(new Date());
		String md = messageId.substring(2,6);
		return table_prefix + y + md;
	}

}
