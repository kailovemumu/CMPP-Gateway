package dorox.app.text;

import java.io.Serializable;

/**
 * Collection of some known Sms port numbers.
 */
public final class SmsPort implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -5328952526114005361L;






    private final int port;
    private final String name;

    public SmsPort(int port, String name) {
        this.port = port;
        this.name = name;
    }



    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SmsPort smsPort = (SmsPort) o;

        return port == smsPort.port;

    }

    @Override
    public int hashCode() {
        return port;
    }
}
