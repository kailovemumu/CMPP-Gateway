package dorox.app.port;

import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class SmsRoute {

	/**
	 * *存放端口的对应路由
	 * 1，key的组成：路由优先级策略：
	 * a，先找下游号码的运营商和城市编码对应的上游通道，b，再找下游号码的运营商对应的上游通道，c，再找下游对应的上游通道
	 * downPortCode_carrier_cityCode、downPortCode_carrier、downPortCode
	 * 2,   一个下游多个上游通道时，需要存放Map<Channel>,保存所有上游通道。
	 */
	private ConcurrentHashMap<String, Map<String,Channel>> downPortRouteInfo = 
			new ConcurrentHashMap<>();
	
	//存放权重
	//<down_port_code+carrier+city,WeightRandom>
	private ConcurrentHashMap<String, WeightRandom<String, Integer>> channelSpeedWeight =
			new ConcurrentHashMap<>();
	
	public ConcurrentHashMap<String, Map<String,Channel>> getDownPortRouteInfo() {
		return downPortRouteInfo;
	}
	public ConcurrentHashMap<String,WeightRandom<String, Integer>> getChannelSpeedWeight() {
		return channelSpeedWeight;
	}
	
	public void removeDownPortChannel() {
		downPortRouteInfo.clear();
		channelSpeedWeight.clear();
	}
	
	public void addChannel(String key, Channel channel) {
		Map<String,Channel> mapChannel = downPortRouteInfo.get(key);
		if(mapChannel==null) {
			mapChannel = new ConcurrentHashMap<String, Channel>();
			downPortRouteInfo.put(key,mapChannel);
		}
		mapChannel.put(channel.getChannelId(), channel);
		
	}

	public void delChannel() {
		
	}
	
	//根据客户编码，手机运营商，地域，获取通道
	public Channel getChannel(String mobile,int carrier,String cityCode,
			String provinceCode, String downPortCode) {

		String hubKey = null;
		if(downPortRouteInfo.containsKey(downPortCode+carrier+provinceCode)){
			hubKey = downPortCode+carrier+provinceCode;
		}else if(downPortRouteInfo.containsKey(downPortCode+carrier+"0")){
			hubKey = downPortCode+carrier+"0";
		}else if(downPortRouteInfo.containsKey(downPortCode+"0"+provinceCode)){
			hubKey = downPortCode+"0"+provinceCode;
		}else if(downPortRouteInfo.containsKey(downPortCode+"00")){
			hubKey = downPortCode+"00";
		}else{
		}
		if(hubKey == null){
			return null;
		}
		try {
			WeightRandom<String, Integer> random = channelSpeedWeight.get(hubKey);
			if(random !=null){
				return downPortRouteInfo.get(hubKey).get(random.random());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	
	}

	public void routeSpeedWeightConfig() {
		for(Entry<String, Map<String,Channel>> entry : downPortRouteInfo.entrySet()){
			String key = entry.getKey();//downPort+carrier+city
			Map<String,Channel> channelMaps = entry.getValue();
			flushSpeed(key, channelMaps);
		}
	}

	private void flushSpeed(String key, Map<String,Channel> channelMaps) {
		List<Pair<String, Integer>> list = new ArrayList<>();
		for(Entry<String, Channel> en : channelMaps.entrySet()){
			int speed = Integer.valueOf(en.getValue().getSpeed());
			if(speed > 0){
				list.add(new Pair<String, Integer>(en.getKey(), speed));
			}
		}
		WeightRandom<String, Integer> random = 
				new WeightRandom<String, Integer>(list);
		channelSpeedWeight.put(key, random);		
	}

	@Override
	public String toString() {
		return "SmsRoute{" +
				"downPortRouteInfo=" + downPortRouteInfo +
				", channelSpeedWeight=" + channelSpeedWeight +
				'}';
	}
}
