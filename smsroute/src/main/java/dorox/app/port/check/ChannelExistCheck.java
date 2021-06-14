package dorox.app.port.check;

import dorox.app.manager.MobileInfo;
import dorox.app.manager.MobileInfoFlush;
import dorox.app.manager.MobileRouteFlush;
import dorox.app.port.Channel;
import dorox.app.port.DownPort;
import dorox.app.util.Statics;
import org.apache.avro.generic.GenericRecord;

import java.util.HashMap;
import java.util.Map;

//通道校验
public class ChannelExistCheck  implements CheckHandler{

    public ChannelExistCheck(MobileInfoFlush mobileInfoFlush,MobileRouteFlush mobileRouteFlush){
        this.mobileInfoFlush=mobileInfoFlush;
        this.mobileRouteFlush=mobileRouteFlush;
    }

    private MobileInfoFlush mobileInfoFlush;
    private MobileRouteFlush mobileRouteFlush;

    @Override
    public CheckResult handler(Map<String, Object> map) {

        GenericRecord serverRequest = (GenericRecord) map.get("serverRequest");

        String phone = String.valueOf(serverRequest.get("phone"));
        String downName = String.valueOf(serverRequest.get("downName"));
        DownPort downPort = Statics.DOWN_PORT_MAP.get(downName);
        MobileInfo mobileInfo = mobileInfoFlush.getCityInfo(phone);

        Channel channel = downPort.getChannel(phone, mobileInfo.getIsp(),
                mobileInfo.getCityCode(), mobileInfo.getProvinceCode(), downName);

//        if(mobileRouteFlush.containsKey(phone)){
//            channel.setUpPortCode(mobileRouteFlush.get(phone));
//        }

        CheckResult result = new CheckResult();

        if(channel == null){
            result.setCode(CheckResult.FAILED);
            result.setMessage(CheckResult.NOCHANNEL);

        }else{
            result.setCode(CheckResult.SUCCESS);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("channel", channel);
        res.put("mobileInfo", mobileInfo);
        result.setResult(res);
        return result;
    }
}
