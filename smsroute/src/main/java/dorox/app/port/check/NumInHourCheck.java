package dorox.app.port.check;

import dorox.app.port.Channel;
import dorox.app.util.CacheConfig;
import org.apache.avro.generic.GenericRecord;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NumInHourCheck implements ChannelCheckHandler{

    @Override
    public CheckResult handler(Map<String, Object> map) {

        Channel channel = (Channel) map.get("channel");
        GenericRecord serverRequest = (GenericRecord) map.get("serverRequest");
        String downName = String.valueOf(serverRequest.get("downName"));
        String phone = String.valueOf(serverRequest.get("phone"));

        CheckResult result = new CheckResult();

        Integer count = CacheConfig.phoneControlInHoursCache. getIfPresent(
                downName+phone+channel.getUpPortCode()+System.currentTimeMillis()/1000/60/60);
        if(count==null){count=0;}
        if(count >= channel.getPhoneAlloInHours()) {
            result.setCode(CheckResult.FAILED);
            result.setMessage(CheckResult.OVERRATE);
        }else {
            CacheConfig.phoneControlInHoursCache.put(
                    downName+phone+channel.getUpPortCode()+System.currentTimeMillis()/1000/60/60, ++count);
            result.setCode(CheckResult.SUCCESS);
        }

        return result;
    }
}
