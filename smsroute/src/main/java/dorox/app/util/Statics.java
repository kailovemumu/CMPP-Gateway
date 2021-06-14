package dorox.app.util;

import dorox.app.port.DownPort;

import java.util.concurrent.ConcurrentHashMap;

public class Statics {

    public static ConcurrentHashMap<String, DownPort> DOWN_PORT_MAP = new ConcurrentHashMap();
}
