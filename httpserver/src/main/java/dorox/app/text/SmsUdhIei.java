package dorox.app.text;

/**
 * Collection of known SMS UDH Identity Element Identifier.
 */
public final class SmsUdhIei {
    /** Concatenated short messages, 8-bit reference number. */
    public static final SmsUdhIei CONCATENATED_8BIT = new SmsUdhIei((byte)0x00, "CONCATENATED_8BIT");

    private final byte value;
    private final String name;

    private SmsUdhIei(byte value, String name) {
        this.value = value;
        this.name = name;
    }


    /**
     * Returns the UDH IEI value as specified in the GSM spec.
     * @return
     */
    public byte getValue() {
        return value;
    }

    @Override
    public String toString() {
        return name;
    }
    
    
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + value;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SmsUdhIei other = (SmsUdhIei) obj;
		if (value != other.value)
			return false;
		return true;
	}

}
