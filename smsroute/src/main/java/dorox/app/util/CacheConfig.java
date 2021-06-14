package dorox.app.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;


import java.util.concurrent.TimeUnit;

public class CacheConfig {

	//营销频控
	//当前通道每个号码每天发送量
	public static Cache<String, Integer> phoneControlInDaysCache = CacheBuilder.newBuilder()
			.expireAfterWrite(1, TimeUnit.DAYS)//。
			.build();
	//当前通道每个号码每小时发送量
	public static Cache<String, Integer> phoneControlInHoursCache = CacheBuilder.newBuilder()
			.expireAfterWrite(1, TimeUnit.HOURS)//。
			.build();
	//当前通道每个号码每分钟发送量
	public static Cache<String, Integer> phoneControlInMinutesCache = CacheBuilder.newBuilder()
			.expireAfterWrite(1, TimeUnit.MINUTES)//。
			.build();
	
//	public static void main(String[] args) throws Exception {
//
//		int max = 10;
//		new Thread(()->{
//
//			while(true) {
//
//				Integer count = CacheConfig.phoneControlInMinutesCache.
//						getIfPresent("aaa"+System.currentTimeMillis()/1000/20);
//				if(count ==null) {
//					count = 0;
//				}
//				if(count >= max) {
//					continue;
//				}else {
//					CacheConfig.phoneControlInMinutesCache.put("aaa"+System.currentTimeMillis()/1000/20, ++count);
//				}
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e) {
//				}
//			}
//		}).start();
//		
//		Map map = CacheConfig.phoneControlInMinutesCache.asMap();
//		while(true) {
//			System.out.println(map);
//			Thread.sleep(1000);
//		}
//	}
}
