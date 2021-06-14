package dorox.app.port.check;

import dorox.app.port.Channel;
import org.apache.avro.generic.GenericRecord;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

@Component
public class InterceptCheck implements ChannelCheckHandler{

    @Override
    public CheckResult handler(Map<String, Object> map) {

        Channel channel = (Channel) map.get("channel");
        GenericRecord serverRequest = (GenericRecord) map.get("serverRequest");
        String content = String.valueOf(serverRequest.get("content"));

        CheckResult result = new CheckResult();

        boolean interecpt = false;
        if(channel.getPatterns()!=null){
            for(Pattern p : channel.getPatterns()){
                if(p.matcher(content).find()){
                    /*拦截*/
                    interecpt = true;
                    break;
                }
            }
        }

        if(interecpt){
            result.setCode(CheckResult.FAILED);
            result.setMessage(CheckResult.INTERCEPT);
        }else{
            result.setCode(CheckResult.SUCCESS);
        }
        return result;
    }
}
