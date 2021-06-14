package dorox.app.vo;

import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;

public class SpVo {

	private String spCode;
	private String upSpCode;
	private String vspCode;
	
	public String getSpCode() {
		return spCode;
	}
	public void setSpCode(String spCode) {
		this.spCode = spCode;
	}
	public String getUpSpCode() {
		return upSpCode;
	}
	public void setUpSpCode(String upSpCode) {
		this.upSpCode = upSpCode;
	}
	public String getVspCode() {
		return vspCode;
	}
	public void setVspCode(String vspCode) {
		this.vspCode = vspCode;
	}
	
	@Override
	public String toString() {
		return "SpVo [spCode=" + spCode + ", upSpCode=" + upSpCode + ", vspCode=" + vspCode + "]";
	}
	
	@Override
	public int hashCode() {
		String val = "";
		if(StringUtils.isNotBlank(spCode)) {
			val += spCode;
		}
		if(StringUtils.isNotBlank(upSpCode)) {
			val += upSpCode;
		}
		if(StringUtils.isNotBlank(vspCode)) {
			val += vspCode;
		}
		System.out.println(val);
		return val.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof SpVo) {
			SpVo two = (SpVo)obj;

			String val1 = "";
			if(StringUtils.isNotBlank(spCode)) {
				val1 += spCode;
			}
			if(StringUtils.isNotBlank(upSpCode)) {
				val1 += upSpCode;
			}
			if(StringUtils.isNotBlank(vspCode)) {
				val1 += vspCode;
			}

			String val2 = "";
			if(StringUtils.isNotBlank(two.getSpCode())) {
				val2 += two.getSpCode();
			}
			if(StringUtils.isNotBlank(two.getUpSpCode())) {
				val2 += two.getUpSpCode();
			}
			if(StringUtils.isNotBlank(two.getVspCode())) {
				val2 += two.getVspCode();
			}
			return val1.equals(val2);
		}
		return false;
	}
	
	
	public static void main(String[] args) {
		HashSet<SpVo> set = new HashSet<>();
		SpVo vo =  new SpVo();
		vo.setSpCode("1234567");
		vo.setUpSpCode("123456");
		vo.setVspCode("");

		SpVo vo1 =  new SpVo();
		vo1.setSpCode("1234567");
		vo1.setUpSpCode("123456");
		vo.setVspCode(null);
		
		set.add(vo);
		set.add(vo1);
		System.out.println(set.size());
		
	}
}
