package dorox.app.vo;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 仅仅用于客户回执，长短信的回执策略应该是通道模块处理。
 *
 */
public class SubmitVo {

	private String phone;
	private AtomicInteger count;


	public SubmitVo(String phone, AtomicInteger count) {
		this.count=count;
		this.phone=phone;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public AtomicInteger getCount() {
		return count;
	}

	public void setCount(AtomicInteger count) {
		this.count = count;
	}

}
