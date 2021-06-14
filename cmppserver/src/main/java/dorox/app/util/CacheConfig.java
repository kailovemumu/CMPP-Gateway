//package dorox.app.util;
//
//import com.google.common.cache.Cache;
//import com.google.common.cache.CacheBuilder;
//import dorox.app.vo.StatVo;
//import dorox.app.vo.SubmitVo;
//
//import java.util.concurrent.TimeUnit;
//
//public class CacheConfig {
//
//	/**
//	 * 缓存down port这边的请求，messageId为key，up port返回状态时，用messageId从缓存中取对应数据。
//	 */
//	public static Cache<String, SubmitVo> submitVoCache = CacheBuilder.newBuilder()
//			.expireAfterWrite(3, TimeUnit.DAYS)
//			.build();
//
//
//}
