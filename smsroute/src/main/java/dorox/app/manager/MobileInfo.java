package dorox.app.manager;

import java.util.HashMap;
import java.util.Map;


/**
 * *号段区域信息
 */
public class MobileInfo {
	private String city;
	private String cityCode;
	private String province;
	private String provinceCode;
	private int isp;

	public MobileInfo(String city, String cityCode, String province, String provinceCode, int isp) {
		this.city = city;
		this.cityCode = cityCode;
		this.province = province;
		this.provinceCode = provinceCode;
		this.isp = isp;
	}


	public String getCity() {
		return city;
	}

	public String getCityCode() {
		return cityCode;
	}

	public String getProvince() {
		return province;
	}

	public String getProvinceCode() {
		return provinceCode;
	}

	public int getIsp() {
		return isp;
	}

	public void setIsp(int isp) {
		this.isp = isp;
	}
}
