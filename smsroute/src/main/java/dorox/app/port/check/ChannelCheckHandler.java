package dorox.app.port.check;

import java.util.Map;

public interface ChannelCheckHandler {

    CheckResult handler(Map<String, Object> map);

}
