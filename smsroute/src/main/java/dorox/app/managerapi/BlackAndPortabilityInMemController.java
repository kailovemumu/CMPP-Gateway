package dorox.app.managerapi;

import dorox.app.manager.BlackFlush;
import dorox.app.manager.MobileRouteFlush;
import dorox.app.manager.PortabilityNumberFlush;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class BlackAndPortabilityInMemController {

	@Autowired
	private BlackFlush blackFlush;
	@Autowired
	private PortabilityNumberFlush portabilityNumberFlush;
	@Autowired
	private MobileRouteFlush mobileRouteFlush;
	/*
	 * *根据号码查询是否黑名单
	 */
	@RequestMapping(path="/getBlack") //
	public @ResponseBody Map<String,Object> getBlack (
			@RequestParam(required = false) String phone) {
		Map<String,Object> res = new HashMap<String,Object>();
		res.put("result", "1");
		res.put("result_msg", "success");
		if(blackFlush.get(phone) == null) {
			res.put("isBlack", -1);
		}else {
			int isBlack = blackFlush.get(phone);
			res.put("isBlack", isBlack);
		}
		res.put("phone", phone);
		return res;
	}
	
	/*
	 * *提交黑名单
	 */
	@RequestMapping(path="/putBlack") //
	public @ResponseBody Map<String,Object> putBlack (
			@RequestParam(required = true) String phone, 
			@RequestParam(required = true) int isBlack) {
		blackFlush.put(phone, isBlack);
		Map<String,Object> res = new HashMap<String,Object>();
		res.put("result", "1");
		res.put("result_msg", "success");
		return res;
	}

	/*
	 * *根据号码查询是否携号转网
	 */
	@RequestMapping(path="/getPortability") //
	public @ResponseBody Map<String,Object> getPortability (
			@RequestParam(required = false) String phone) {
		Map<String,Object> res = new HashMap<String,Object>();
		res.put("result", "1");
		res.put("result_msg", "success");
		res.put("carrier", portabilityNumberFlush.get(phone));
		res.put("phone", phone);
		return res;
	}
	
	/*
	 * *提交携号转网
	 */
	@RequestMapping(path="/putPortability") //
	public @ResponseBody Map<String,Object> putPortability (
			@RequestParam(required = false) String phone, 
			@RequestParam(required = false) int carrier) {
		portabilityNumberFlush.put(phone, carrier);
		Map<String,Object> res = new HashMap<String,Object>();
		res.put("result", "1");
		res.put("result_msg", "success");
		return res;
	}
	
	/*
	 * *删除携号转网
	 */
	@RequestMapping(path="/delPortability") //
	public @ResponseBody Map<String,Object> delPortability (
			@RequestParam(required = false) String phone) {
		portabilityNumberFlush.remove(phone);
		Map<String,Object> res = new HashMap<String,Object>();
		res.put("result", "1");
		res.put("result_msg", "success");
		return res;
	}

	/*
	 * *根据号码查询是否携号转网
	 */
	@RequestMapping(path="/getMobileRoute") //
	public @ResponseBody Map<String,Object> getMobileRoute (
			@RequestParam() String phone) {
		Map<String,Object> res = new HashMap<String,Object>();
		res.put("result", "1");
		res.put("result_msg", "success");
		res.put("unName", mobileRouteFlush.get(phone));
		res.put("phone", phone);
		return res;
	}

	/*
	 * *提交携号转网
	 */
	@RequestMapping(path="/putMobileRoute") //
	public @ResponseBody Map<String,Object> putMobileRoute (
			@RequestParam() String phone,
			@RequestParam() String upName) {
		mobileRouteFlush.put(phone, upName);
		Map<String,Object> res = new HashMap<String,Object>();
		res.put("result", "1");
		res.put("result_msg", "success");
		return res;
	}

	/*
	 * *删除携号转网
	 */
	@RequestMapping(path="/delMobileRoute") //
	public @ResponseBody Map<String,Object> delMobileRoute (
			@RequestParam() String phone) {
		mobileRouteFlush.remove(phone);
		Map<String,Object> res = new HashMap<String,Object>();
		res.put("result", "1");
		res.put("result_msg", "success");
		return res;
	}
}
