//package dorox.app;
//
//import com.google.common.cache.Cache;
//import com.google.common.cache.CacheBuilder;
//import com.google.common.cache.RemovalListener;
//import com.google.common.cache.RemovalNotification;
//
//import java.util.concurrent.TimeUnit;
//
//public class GuavaCacheTest {
//
//    public static RemovalListener<String, String> removalListener = new RemovalListener<String, String>() {
//        @Override
//        public void onRemoval(RemovalNotification<String, String> removalNotification) {
//            System.out.println(removalNotification.getKey() + " : " + removalNotification.getValue() + "   has been removed");
//        }
//    };
//    public static Cache<String, String> cache = CacheBuilder.newBuilder()
//            .expireAfterWrite(3, TimeUnit.SECONDS)//
//            .removalListener(removalListener)
//            .build();
//
//
//    public static void main(String[] args) throws Exception {
//        cache.put("a", "b");
//        cache.asMap().remove("a", "b");
//        //cache.invalidate("a");
//        Thread.sleep(1000);
//        System.out.println(cache.getIfPresent("a"));
//        Thread.sleep(1000);
//
//        System.out.println(cache.getIfPresent("a"));
//        Thread.sleep(1000);
//        System.out.println(cache.getIfPresent("a"));
//        Thread.sleep(1000);
//        System.out.println(cache.getIfPresent("a"));
////        cache.cleanUp();
//
//    }
//}
