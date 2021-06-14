package dorox.app.port.check;

import dorox.app.port.Channel;
import dorox.app.util.CacheConfig;
import org.apache.avro.generic.GenericRecord;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

@Component
public class SignCheck implements ChannelCheckHandler{

    @Override
    public CheckResult handler(Map<String, Object> map) {

        Channel channel = (Channel) map.get("channel");
        GenericRecord serverRequest = (GenericRecord) map.get("serverRequest");
        String content = String.valueOf(serverRequest.get("content"));

        CheckResult result = new CheckResult();

        boolean match = false;
        if(channel.getSign()!=null){
            for(String p : channel.getSign()){
                if(content.contains(p)){
                    /*匹配*/
                    match = true;
                    break;
                }
            }
        }

        if(match){
            result.setCode(CheckResult.SUCCESS);
        }else{
            result.setCode(CheckResult.FAILED);
            result.setMessage(CheckResult.ERRSIGN);
        }
        return result;
    }
}
