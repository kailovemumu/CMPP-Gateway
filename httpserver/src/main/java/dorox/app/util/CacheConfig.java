package dorox.app.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dorox.app.vo.SubmitVo;

import java.util.concurrent.TimeUnit;

public class CacheConfig {

	/**
	 * 保存短信发送结果,messageId
	 */
	public static Cache<String, SubmitVo> submitVoCache = CacheBuilder.newBuilder()
			.expireAfterWrite(3, TimeUnit.DAYS)
			.build();


}
