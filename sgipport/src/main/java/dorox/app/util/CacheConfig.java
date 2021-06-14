package dorox.app.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dorox.app.vo.ResponseVoSGIP;
import dorox.app.vo.SubmitVoSGIP;

import java.util.concurrent.TimeUnit;

public class CacheConfig {

	/**
	 * <seq:msgIds>
	 */
//	public static Cache<String, List<Utf8>> seqMsgIdsCacheSGIP = CacheBuilder.newBuilder()
//			.expireAfterWrite(3, TimeUnit.DAYS)//。
//			.build();

	public static Cache<String, SubmitVoSGIP> submitVoCacheSGIP = CacheBuilder.newBuilder()
			.expireAfterWrite(3, TimeUnit.DAYS)
			.build();
	public static Cache<String, ResponseVoSGIP> responseVoCacheSGIP = CacheBuilder.newBuilder()
			.expireAfterWrite(3, TimeUnit.DAYS)
			.build();
}
