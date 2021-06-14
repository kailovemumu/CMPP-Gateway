package dorox.app.managerapi;

import dorox.app.manager.ManageDownPort;
import dorox.app.port.DownPort;
import dorox.app.util.Statics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.HashMap;
import java.util.Map;

@Controller
public class DownPortInMemController {
	
	@Autowired
	private ManageDownPort manageDownPort;

	/**
	 * 只更新客户信息，不更新通道信息
	 * */
	@RequestMapping(path="/updateDownPort")
	public @ResponseBody Map<String,Object> updateDownPort (
			String downName, String regionCode, String downSpcode, String downVspcode, int isAdvance ) {
		//新建端口
		manageDownPort.updateDownPort(downName, regionCode, downSpcode, downVspcode, isAdvance);
		Map<String,Object> res = new HashMap<String,Object>();
		res.put("result", "1");
		res.put("result_msg", "success");
		return res;
	}

	@RequestMapping(path="/removeDownPort")
	public @ResponseBody Map<String,Object> removeDownPort (String downName) {
		//新建端口
		manageDownPort.removeDownPort(downName);
		Map<String,Object> res = new HashMap<String,Object>();
		res.put("result", "1");
		res.put("result_msg", "success");
		return res;
	}

	@RequestMapping(path="/getDownPort") //
	public @ResponseBody Map<String, String> getDownPort (String downName) {
		DownPort o = Statics.DOWN_PORT_MAP.get(downName);
		Map<String, String> res = new HashMap<>();
		res.put("downport", o.toString());
		res.put("result", "1");
		res.put("result_msg", "success");
		return res;
	}

}
