package dorox.app.managerapi;

import dorox.app.manager.MobileInfo;
import dorox.app.manager.MobileInfoFlush;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class MobileInfoInMemController {

	/*
	 * *根据号段和运营商查区域
	 */
	@RequestMapping(path="/getMobileInfo") //
	public @ResponseBody Map<String,Object> getMobileInfo (
			@RequestParam(required = true) String phone,
			@RequestParam(required = true) int isp) {
		Map<String,Object> res = new HashMap<String,Object>();
		res.put("result", "1");
		res.put("result_msg", "success");
		
		if (MobileInfoFlush.YIDONG ==(isp) ) {
			res.put("cityInfo", MobileInfoFlush.YIDONG_MOBILE_MAP.get(phone));
		} else if (MobileInfoFlush.LIANTONG ==(isp)) {
			res.put("cityInfo", MobileInfoFlush.LIANTONG_MOBILE_MAP.get(phone));
		} else {
			res.put("cityInfo", MobileInfoFlush.DIANXIN_MOBILE_MAP.get(phone));
		}

		res.put("isp", isp);
		res.put("phone", phone);
		return res;
	}
	
	/*
	 * *提交号段区域
	 */
	@RequestMapping(path="/putMobileInfo") //
	public @ResponseBody Map<String,Object> putMobileInfo (
			@RequestParam(required = true) String phone,
			@RequestParam(required = true) String cityCode,
			@RequestParam(required = true) String city,
			@RequestParam(required = true) String provinceCode,
			@RequestParam(required = true) String province,
			@RequestParam(required = true) int isp) {
		
		MobileInfo mobileInfo = new MobileInfo(city, cityCode, province, provinceCode, isp);

		if (MobileInfoFlush.YIDONG ==(isp) ) {
			MobileInfoFlush.YIDONG_MOBILE_MAP.put(phone, mobileInfo);
		} else if (MobileInfoFlush.LIANTONG ==(isp)) {
			MobileInfoFlush.LIANTONG_MOBILE_MAP.put(phone, mobileInfo);
		} else {
			MobileInfoFlush.DIANXIN_MOBILE_MAP.put(phone, mobileInfo);
		}
		
		Map<String,Object> res = new HashMap<String,Object>();
		res.put("result", "1");
		res.put("result_msg", "success");
		return res;
	}

	@RequestMapping(path="/delMobileInfo") //
	public @ResponseBody Map<String,Object> delMobileInfo (
			@RequestParam(required = true) String phone,
			@RequestParam(required = true) int isp) {
		Map<String,Object> res = new HashMap<String,Object>();
		res.put("result", "1");
		res.put("result_msg", "success");
		
		if (MobileInfoFlush.YIDONG ==(isp) ) {
			MobileInfoFlush.YIDONG_MOBILE_MAP.remove(phone);
		} else if (MobileInfoFlush.LIANTONG ==(isp)) {
			MobileInfoFlush.LIANTONG_MOBILE_MAP.remove(phone);
		} else {
			MobileInfoFlush.DIANXIN_MOBILE_MAP.remove(phone);
		}

		res.put("isp", isp);
		res.put("phone", phone);
		return res;
	}
	
	/*
	 * *号段
	 */
	@RequestMapping(path="/getMobileSegment") //
	public @ResponseBody Map<String,Object> getMobileSegment (
			@RequestParam(required = true) String segment) {
		Map<String,Object> res = new HashMap<String,Object>();
		res.put("result", "1");
		res.put("result_msg", "success");

		if(MobileInfoFlush.MOBILE_SEGMENT.get(segment) == null) {
			res.put("carrier", -1);
		}else {
			int carrier = MobileInfoFlush.MOBILE_SEGMENT.get(segment);
			res.put("carrier", carrier);
		}
		
		res.put("segment", segment);
		return res;
	}
	
	/*
	 * *提交
	 */
	@RequestMapping(path="/putMobileSegment") //
	public @ResponseBody Map<String,Object> putMobileSegment (
			@RequestParam(required = true) String segment, 
			@RequestParam(required = true) int isp) {
		MobileInfoFlush.MOBILE_SEGMENT.put(segment, isp);
		Map<String,Object> res = new HashMap<String,Object>();
		res.put("result", "1");
		res.put("result_msg", "success");
		return res;
	}

	/*
	 * *删除
	 */
	@RequestMapping(path="/delMobileSegment") //
	public @ResponseBody Map<String,Object> delMobileSegment (
			@RequestParam(required = true) String segment) {
		MobileInfoFlush.MOBILE_SEGMENT.remove(segment);
		Map<String,Object> res = new HashMap<String,Object>();
		res.put("result", "1");
		res.put("result_msg", "success");
		return res;
	}
}
