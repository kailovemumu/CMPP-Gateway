package dorox.app.port.check;

import dorox.app.port.Channel;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TimeControlCheck implements ChannelCheckHandler{

    @Override
    public CheckResult handler(Map<String, Object> map) {

        Channel channel = (Channel) map.get("channel");
        long curr = System.currentTimeMillis() % (24L*60*60*1000);

        CheckResult result = new CheckResult();

        /*时段控制*/
        if(curr > channel.getStartAllowTime() && curr < channel.getEndAllowTime()){
            result.setCode(CheckResult.SUCCESS);
        }else{
            result.setCode(CheckResult.FAILED);
            result.setMessage(CheckResult.INTERCEPT);
        }
        return result;
    }
}
