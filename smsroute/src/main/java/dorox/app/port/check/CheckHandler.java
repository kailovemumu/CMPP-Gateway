package dorox.app.port.check;

import java.util.Map;

public interface CheckHandler {

    CheckResult handler(Map<String, Object> map);

}
