package dorox.app.text;

import java.nio.charset.Charset;

public interface GlobalConstance {

    public static final Charset defaultTransportCharset = Charset.forName(PropertiesUtils.getDefaultTransportCharset());
    public static final SmsDcs defaultmsgfmt = SmsDcs.getGeneralDataCodingDcs(SmsAlphabet.ASCII, SmsMsgClass.CLASS_UNKNOWN);
}
