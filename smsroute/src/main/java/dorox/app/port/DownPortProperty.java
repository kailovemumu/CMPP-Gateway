package dorox.app.port;

public class DownPortProperty {

    private String downName;
    private String downSpcode;
    private String downVspcode;
    private boolean isAdvance;
    private String regionCode;

    public String getDownSpcode() {
        return downSpcode;
    }

    public void setDownSpcode(String downSpcode) {
        this.downSpcode = downSpcode;
    }

    public String getDownVspcode() {
        return downVspcode;
    }

    public void setDownVspcode(String downVspcode) {
        this.downVspcode = downVspcode;
    }

    public boolean isAdvance() {
        return isAdvance;
    }

    public void setAdvance(boolean advance) {
        isAdvance = advance;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public String getDownName() {
        return downName;
    }

    public void setDownName(String downName) {
        this.downName = downName;
    }

    @Override
    public String toString() {
        return "DownPortProperty{" +
                "downName='" + downName + '\'' +
                ", downSpcode='" + downSpcode + '\'' +
                ", downVspcode='" + downVspcode + '\'' +
                ", isAdvance=" + isAdvance +
                ", regionCode='" + regionCode + '\'' +
                '}';
    }
}
