package dorox.app.util.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.sift.AbstractDiscriminator;

public class LoggerNameBasedDiscriminator extends AbstractDiscriminator<ILoggingEvent> {
	 private static final String KEY = "loggerName";
	    private String defaultValue;

	    public String getDefaultValue() {
	        return defaultValue;
	    }

	    public void setDefaultValue(String defaultValue) {
	        this.defaultValue = defaultValue;
	    }

	    @Override
		public String getKey() {
	        return KEY;
	    }

	    public void setKey() {
	        throw new UnsupportedOperationException("Key not settable. Using " + KEY);
	    }

	    @Override
		public String getDiscriminatingValue(ILoggingEvent e) {
	        String loggerName = e.getLoggerName();

	        if (loggerName == null) {
				return defaultValue;
			}

	        return loggerName;
	    }
}