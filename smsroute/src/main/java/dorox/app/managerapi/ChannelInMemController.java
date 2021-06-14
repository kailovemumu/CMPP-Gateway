package dorox.app.managerapi;

import java.util.HashMap;
import java.util.Map;

import dorox.app.port.DownPort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import dorox.app.util.Statics;

@Controller
public class ChannelInMemController {
	
	/*
	 * *刷新路由,每次修改channel就刷新路由
	 */
	@RequestMapping(path="/routeFlush") //
	public @ResponseBody Map<String,Object> routeFlush (String downName, String regionCode) {
		DownPort downPort = (DownPort)Statics.DOWN_PORT_MAP.get(downName);
		downPort.loadDownPortChannel();
		Map<String,Object> res = new HashMap<String,Object>();

		res.put("result", "1");
		res.put("result_msg", "success");
		return res;
	}

}
