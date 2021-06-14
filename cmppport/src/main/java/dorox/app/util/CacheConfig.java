package dorox.app.util;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import dorox.app.vo.ResponseVoCMPP;
import dorox.app.vo.SubmitVoCMPP;
import dorox.app.vo.TestSubmitVoCMPP;
import org.apache.avro.util.Utf8;

public class CacheConfig {

	/**
	 * <seq:msgIds>
	 */
//	public static Cache<String, List<Utf8>> seqMsgIdsCacheCMPP = CacheBuilder.newBuilder()
//			.expireAfterWrite(3, TimeUnit.DAYS)//。
//			.build();

	/**
	 * key:seq_id
	 * 分析缓存过期：
	 * 
	 */
	public static Cache<String, SubmitVoCMPP> submitVoCacheCMPP = CacheBuilder.newBuilder()
			.expireAfterWrite(3, TimeUnit.DAYS)//。
			.build();
	
	/**
	 * key:msg_id
	 */
	public static Cache<String, ResponseVoCMPP> responseVoCacheCMPP = CacheBuilder.newBuilder()
			.expireAfterWrite(3, TimeUnit.DAYS)//1分钟超时。先入库。
			.build();

	/**
	 * key:msg_id,
	 */
//	public static Cache<String, DeliverVoCMPP> deliverVoCacheCMPP = CacheBuilder.newBuilder()
//			.expireAfterWrite(3, TimeUnit.DAYS)//1分钟超时。先入库。
//			.build();
	
	public static Cache<String, TestSubmitVoCMPP> TestSubmitVoCacheCMPP = CacheBuilder.newBuilder()
			.expireAfterWrite(3, TimeUnit.DAYS)//。
			.build();
	
//	/**
//	 * key:msg_id
//	 */
//	public static Cache<String, ResponseVoCMPP> TestResponseVoCacheCMPP = CacheBuilder.newBuilder()
//			.expireAfterWrite(3, TimeUnit.DAYS)//1分钟超时。先入库。
//			.build();
//
//	/**
//	 * key:msg_id,
//	 */
//	public static Cache<String, DeliverVoCMPP> TestDeliverVoCacheCMPP = CacheBuilder.newBuilder()
//			.expireAfterWrite(3, TimeUnit.DAYS)//1分钟超时。先入库。
//			.build();
	
}
