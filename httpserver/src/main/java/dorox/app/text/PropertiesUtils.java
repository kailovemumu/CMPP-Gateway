package dorox.app.text;

public class PropertiesUtils {

	public static String getDefaultTransportCharset() {
		String charset = "GBK";
		return charset==null?"UTF-8":charset;
	}

}
