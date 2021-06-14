package dorox.app.port.check;

import dorox.app.manager.BlackFlush;
import org.apache.avro.generic.GenericRecord;

import java.util.Map;
//黑名单校验
public class BlackCheck implements ChannelCheckHandler{

    private BlackFlush blackFlush;
    public BlackCheck(BlackFlush blackFlush){
        this.blackFlush=blackFlush;
    }

    @Override
    public CheckResult handler(Map<String, Object> map) {

        GenericRecord serverRequest = (GenericRecord) map.get("serverRequest");
        String phone = String.valueOf(serverRequest.get("phone"));

        CheckResult result = new CheckResult();
        /*黑名单*/
        if( blackFlush.get(phone) != null && blackFlush.get(phone)==1){
            result.setCode(CheckResult.FAILED);
            result.setMessage(CheckResult.BLACK);
        }else{
            result.setCode(CheckResult.SUCCESS);
        }
        return result;
    }
}
