package dorox.app.port.check;

import java.util.Map;

public class CheckResult {
    public final static String SUCCESS = "0";
    public final static String FAILED = "1";
    public final static String NOCHANNEL = "DR:002";//"NOCHANNEL";
    public final static String BLACK = "DR:003";//"BLACK";
    public final static String INTERCEPT = "DR:004";//"INTERCEPT";
    public final static String ERRSIGN = "DR:005";//"ERRSIGN";
    public final static String OVERRATE = "DR:006";//"OVERRATE";

    private String code;
    private String message;
    private Map<String, Object> result;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getResult() {
        return result;
    }

    public void setResult(Map<String, Object> result) {
        this.result = result;
    }

}
