package dorox.app.api;

import dorox.app.http.HttpDownPort;
import dorox.app.mq.event.ServerRequestEvent;
import dorox.app.mq.event.ServerRequestFixUpNameEvent;
import dorox.app.text.SmsTextMessage;
import dorox.app.util.CacheConfig;
import dorox.app.util.MqUtil;
import dorox.app.util.Statics;
import dorox.app.vo.SubmitVo;
import org.apache.avro.util.Utf8;
import org.n3r.idworker.Sid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
public class HttpDownPortController {
    private static final Logger logger = LoggerFactory.getLogger(HttpDownPortController.class);

	@Value("${messageid.prefix}")
	private String messageIdPrefix;
	@Autowired
	private Sid sid;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private RabbitTemplate rabbitTemplate;

	
	@RequestMapping(path="/sendSM") //
	public @ResponseBody Map<String,Object> sendSM (@RequestParam String userId, 
			@RequestParam String timestamp, @RequestParam String sign, 
			@RequestParam String mobile,@RequestParam String content,
			@RequestParam(required=false) String srcId, HttpServletRequest request) {

		if(srcId == null){srcId="";}

		logger.info("userId:{}, mobile:{}, srcId:{}",userId, mobile, srcId);

		Map<String,Object> res = new HashMap<String,Object>();
		res.put("result", "-1");
		res.put("result_msg", "提交失败");
		
		HttpDownPort httpDownPort = Statics.DOWN_PORT_MAP.get(userId);

		if(httpDownPort == null){
			logger.info("no user : {}",userId);
			return res;
		}
		
		String clientIp = request.getRemoteAddr();
		if( ! httpDownPort.isWhiteIp(clientIp)){
			logger.error("clientIp:{}", clientIp);
			res.put("result_msg", "err ip");
			return res;	
		}
		
		String password = httpDownPort.getDownPassword();
		String mySign = DigestUtils.md5DigestAsHex((userId+password+timestamp).getBytes());
		if(!sign.equals(mySign)){
			logger.info("userId:{},password:{},timestamp:{},sign:{},but my sign is {}"
					, userId, password, timestamp,sign, mySign);
			res.put("result_msg", "sign异常");
			return res;
		}
		if(mobile.startsWith("86")) {
			mobile = mobile.substring(2);
		}

		try {
			if(mobile.length()==11){

				String messageId = sid.nextShort(messageIdPrefix);
				final List<String> msgIds = new ArrayList<>();
//				@TODO 这个方法是什么意思？ SmsTextMessage 开源项目
				for(int i = 0; i < new SmsTextMessage(content).getPdus().length; i++){
					msgIds.add("0");
				}
				//ServerRequestEvent丢入队列
				MqUtil.sendMsg(new ServerRequestEvent(messageId, userId, mobile, content, srcId, msgIds, httpDownPort.getRegionCode()),
						rabbitTemplate, "serverrequest.report");
				//缓存http请求
				CacheConfig.submitVoCache.put(messageId, new SubmitVo(mobile, new AtomicInteger(msgIds.size())));

				List<Map<String,String>> resList = new ArrayList<>();
				Map<String,String> resTmp = new HashMap<>();
				resTmp.put("taskId", messageId);
				resList.add(resTmp);

				res.put("result", "0");
				res.put("result_list", resList);
				res.put("result_size", resList.size());
				res.put("result_msg", "提交成功");
			}else if(mobile.contains(",")){
				String[] mobiles = mobile.split(",");
				List<Map<String,String>> resList = new ArrayList<>();

				for(String m : mobiles){
					String messageId = sid.nextShort(messageIdPrefix);
					final List<String> msgIds = new ArrayList<>();
					for(int i = 0; i < new SmsTextMessage(content).getPdus().length; i++){
						msgIds.add("0");
					}

					//ServerRequestEvent丢入队列
					MqUtil.sendMsg(new ServerRequestEvent(messageId, userId,
							m, content, srcId, msgIds, httpDownPort.getRegionCode()),  rabbitTemplate, "serverrequest.report");
					//缓存http请求 在 consumer 中通过 messageId msgIds 比对信息结果
					CacheConfig.submitVoCache.put(messageId, new SubmitVo(mobile, new AtomicInteger(msgIds.size())));

					Map<String,String> resTmp = new HashMap<>();
					resTmp.put("taskId", messageId);
					resList.add(resTmp);
				}
				res.put("result", "0");
				res.put("result_list", resList);
				res.put("result_size", resList.size());
				res.put("result_msg", "提交成功");
			}else{
				res.put("result_msg", "err content");
				return res;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		logger.info("result:{}", res);
		return res;
	}

	@RequestMapping(path="/statSM") //获取状态
	public @ResponseBody List<Map<String,String>> statSM (@RequestParam String userId, @RequestParam String timestamp, 
			@RequestParam String sign, HttpServletRequest request) {
		
		HttpDownPort downPort = Statics.DOWN_PORT_MAP.get(userId);

		if(downPort == null){
			logger.info("no user : {}",userId);
			return null;
		}

		String clientIp = request.getRemoteAddr();
		if( !downPort.isWhiteIp(clientIp)){
			logger.info("userId : {}, err clientIp : {}",userId,clientIp);
			return null;
		}
		
		String password = downPort.getDownPassword();

		String mySign = DigestUtils.md5DigestAsHex((userId+password+timestamp).getBytes());
		if(!sign.equals(mySign)){
			logger.info("userId:{},password:{},timestamp:{},sign:{},but my sign is {}"
					, userId, password, timestamp,sign, mySign);
			
			return null;
		}

		SetOperations<String,Object> set = redisTemplate.opsForSet();
		long size = set.size(userId+"_RESULT");
		if(size>0){
			size = size > 4000 ? 4000 : size;
		}

		List<Map<String,String>> res = new ArrayList<Map<String,String>>();
		
		for(int i = 0; i < size; i++){
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>)set.pop(userId+"_RESULT");
			Map<String,String> item = new HashMap<String,String>();
			item.put("taskId", (String)map.get("taskId"));
			item.put("phone", (String)map.get("phone"));
			item.put("result", (String)map.get("result"));
			res.add(item);
		}
		return res;
	}

	@RequestMapping(path="/moSM") //获取状态
	public @ResponseBody List<Map<String,String>> moSM (@RequestParam String userId, @RequestParam String timestamp, 
			@RequestParam String sign, HttpServletRequest request) {
		
		HttpDownPort downPort = Statics.DOWN_PORT_MAP.get(userId);

		if(downPort == null){
			logger.info("no user : {}",userId);
			return null;
		}

		String clientIp = request.getRemoteAddr();
		if( !downPort.isWhiteIp(clientIp)){
			logger.info("userId : {}, err clientIp : {}",userId,clientIp);
			return null;
		}
		
		String password = downPort.getDownPassword();

		String mySign = DigestUtils.md5DigestAsHex((userId+password+timestamp).getBytes());
		if(!sign.equals(mySign)){
			logger.info("userId:{},password:{},timestamp:{},sign:{},but my sign is {}"
					, userId, password, timestamp,sign, mySign);
			
			return null;
		}


		SetOperations<String,Object> set = redisTemplate.opsForSet();
		long size = set.size(userId+"_MO");
		if(size>0){
			size = size > 4000 ? 4000 : size;
		}

		List<Map<String,String>> res = new ArrayList<Map<String,String>>();
		
		for(int i = 0; i < size; i++){
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>)set.pop(userId+"_MO");
			Map<String,String> item = new HashMap<String,String>();
			item.put("phone", (String)map.get("phone"));
			item.put("content", (String)map.get("content"));
			item.put("srcId", (String)map.get("srcId"));
			res.add(item);
		}
		return res;
	}

	public static void main(String[] args) throws Exception{
		String s = "【网易云课堂】您已成功报名3天UI设计训练营，购课账号18696112182手机号，请打开网易云课堂，进入“我的学习”查看课表，打开手机系统通知及时查收更多上课提醒。";

		System.out.println(new SmsTextMessage(s).getPdus().length);
	}
}
