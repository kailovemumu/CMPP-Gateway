//package dorox.app;
//
//import com.google.common.util.concurrent.RateLimiter;
//import com.zx.sms.codec.cmpp.msg.*;
//import com.zx.sms.common.util.CachedMillisecondClock;
//import com.zx.sms.connect.manager.EndpointManager;
//import com.zx.sms.connect.manager.EventLoopGroupFactory;
//import com.zx.sms.connect.manager.ExitUnlimitCirclePolicy;
//import com.zx.sms.handler.api.AbstractBusinessHandler;
//import com.zx.sms.session.cmpp.SessionState;
//import io.netty.channel.ChannelFuture;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.util.concurrent.Future;
//import io.netty.util.concurrent.GenericFutureListener;
//import org.apache.commons.lang3.time.DateFormatUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.Callable;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.atomic.AtomicLong;
//
///*
// * 客户端和服务端的channel是一一对应的。
// * 比如有10个channel，每个channel都会绑定各自的handler。handler里的方法会调用10次。
// * */
//public class SendHandler extends AbstractBusinessHandler {
//
//	private static final Logger logger = LoggerFactory.getLogger(SendHandler.class);
////	private static String content = "[电费账单]尊敬的客户（客户号：1548915648），截至8月31日，您家电表知码2810，8月用电84度，电费46.58元，" +
////			"截至9月2号，可用余额18.56元。当月电费账单已生成，交电费就用网上国网app，新用户注册即送2元电费卷，点dwz.cn/zpdkfjie领取，还有更多精彩活动等您参与！";
////	private static String content = "【7FRESH】好物优惠！狮王洗衣液、春雨面膜买1赠1！奥妙洗衣凝珠、东阿阿胶糕等超多爆款直降！回T退订";
////	private static String content = "【ash官方旗舰店】 38女王节正式预热！300元优惠券每日10点1元秒杀！限量福袋预热期即可下单，正式活动都买不到！
////	更有全新产品复古运动鞋Spider系列，引领春夏新潮流！点我进店：c.tb.cn/c.00yyfa 回TD退订";
//	private static String content = "lt通道验证";
////	private static String content = "湖北电力年终超级福利！网上国网App交电费最高立减10元，即日起至12月25日，点 dwz.cn/4izNeIja 参与。";
////
//
////	private static int i = 0;
//	private int rate = 1;
//	public static AtomicLong cnt = new AtomicLong();
////	private AtomicLong cntSend = new AtomicLong();
//	private long lastNum = 0;
//
//
//	public static Map<String, String> amap = new ConcurrentHashMap<>();
//
//	private int limit = 0,times = 0;
//	private CountDownLatch countDownLatch;
//
//	public SendHandler(int limit, int times, CountDownLatch countDownLatch) {
//		this.limit=limit;this.times = times;this.countDownLatch = countDownLatch;
//	}
//
//	@SuppressWarnings("unchecked")
//	@Override
//	public void userEventTriggered(final ChannelHandlerContext ctx, Object evt) throws Exception {
//		logger.info("userEventTriggered.................");
//		if (evt == SessionState.Connect) {
//			EventLoopGroupFactory.INS.submitUnlimitCircleTask(new Callable<Boolean>() {
//
//				@Override
//				public Boolean call() throws Exception {
////						long nowcnt = cnt.get();
////						long nowcntSend = cntSend.get();
////						EndpointConnector conn = EndpointManager.INS.getEndpointConnector(getEndpointEntity());
////						if(nowcnt!=lastNum){
////							logger.info("channels : {},Totle Receive Msg Num:{},  Totle success Msg Num:{}, speed : {}/s   ",
////								conn == null ? 0 : conn.getConnectionNum(), nowcnt,nowcntSend, (nowcnt - lastNum) / rate);
////							logger.info("amap size : {}", amap.size());
////							logger.info("====================================");
////							logger.info("====================================");
////						}
////						lastNum = nowcnt;
//						return true;
//				}
//			}, new ExitUnlimitCirclePolicy() {
//				@Override
//				public boolean notOver(Future future) {
//					return EndpointManager.INS.getEndpointConnector(getEndpointEntity()) != null;
//				}
//			}, rate * 1000);
//
//			new StartSend(ctx).start();
//			new StartSend(ctx).start();
//		}
//		ctx.fireUserEventTriggered(evt);
//	}
//
//	public class StartSend2 extends Thread{
//		private ChannelHandlerContext ctx;
//		public StartSend2(ChannelHandlerContext ctx){
//			this.ctx=ctx;
//		}
//		@Override
//		public void run(){
//
//			RateLimiter limiter = RateLimiter.create(limit);
//			//while(true){
//			for(String phone : phoneList){
//				double waitTime = limiter.acquire(1);
//				CmppSubmitRequestMessage newMessage = new CmppSubmitRequestMessage();
//				newMessage.setDestterminalId(phone);
////				newMessage.setDestterminalId("13476176410");
////				newMessage.setLinkID("0000");
//				newMessage.setMsgContent(content);
//				newMessage.setRegisteredDelivery((short)1);
//
//
////				newMessage.setSrcId("106938970089123");
////				logger.info("[{}]CmppSubmitRequestMessage {}, waitTime:{}",
////						, sb.toString(), waitTime);
//				ctx.writeAndFlush(newMessage);
//				//logger.info("CmppSubmitRequestMessage :{}", newMessage);
//			}
//
//		}
//	}
//	public class StartSend extends Thread{
//		private ChannelHandlerContext ctx;
//		public StartSend(ChannelHandlerContext ctx){
//			this.ctx=ctx;
//		}
//		@Override
//		public void run(){
//
//			//RateLimiter limiter = RateLimiter.create(limit);
//			//while(true){
////			for(int a = 0;a < limit * times; a++){
////				double waitTime = limiter.acquire(1);
//				CmppSubmitRequestMessage newMessage = new CmppSubmitRequestMessage();
//				//电信
//				newMessage.setDestterminalId("13307163115");
////				newMessage.setDestterminalId("18171306612");
////				newMessage.setDestterminalId("13317186161");
////				newMessage.setDestterminalId("13886170483");
//
//
//				//移动
////				newMessage.setDestterminalId("13419536069");
////				newMessage.setDestterminalId("13476176410");
////				newMessage.setDestterminalId("18872276633");
////				newMessage.setDestterminalId("13607182255");
////				newMessage.setDestterminalId("13986137070");
////				newMessage.setDestterminalId("18271889505");
//
//				//联通
////				newMessage.setDestterminalId("18607192171");
////				newMessage.setDestterminalId("18571616424");
////				newMessage.setDestterminalId("18607181216");
////				newMessage.setDestterminalId("18672868923");
////				newMessage.setDestterminalId("15693100026");
////				newMessage.setDestterminalId("18607186846");
////				newMessage.setLinkID("0000");
//				newMessage.setMsgContent(content);
//				newMessage.setRegisteredDelivery((short)1);
//
//				newMessage.setServiceId("vac12shC");
//				newMessage.setSrcId("10692763112918995598");
////				logger.info("[{}]CmppSubmitRequestMessage {}, waitTime:{}",
////						, sb.toString(), waitTime);
//				ctx.writeAndFlush(newMessage);
//				//logger.info("CmppSubmitRequestMessage :{}", newMessage);
//
////			}
//
//		}
//	}
//	public class StartSend1 extends Thread{
//		private ChannelHandlerContext ctx;
//		public StartSend1(ChannelHandlerContext ctx){
//			this.ctx=ctx;
//		}
//		@Override
//		public void run(){
//			String[] haoduans = { "130", "131", "132", "133", "134", "135", "136", "137", "138", "139", "150",
//				"151", "152", "153", "154", "155", "156", "157", "158", "159", "180", "181", "182", "183", "184", "185",
//				"186", "187", "188", "189", "141", "145", "146", "147", "148", "149", "162", "165", "166", "167", "170",
//				"171", "172", "173", "174", "175", "176", "177", "178", "191", "198", "199" };
//
//			RateLimiter limiter = RateLimiter.create(limit);
////			while(true){
//			for(int a = 0;a < limit * times; a++){
//				StringBuilder sb = new StringBuilder();
//				sb.append(haoduans[(int) (Math.random() * 52)]);
//				for (int i = 0; i < 8; i++) { //②随机生成后8位
//					sb.append((int) (Math.random() * 10));
//				}
//				CmppSubmitRequestMessage newMessage = new CmppSubmitRequestMessage();
//				newMessage.setDestterminalId(sb.toString());
////				newMessage.setDestterminalId("13476176410");
////				newMessage.setLinkID("0000");
//				newMessage.setMsgContent(content);
//				newMessage.setRegisteredDelivery((short)1);
//
//				double waitTime = limiter.acquire(1);
////				newMessage.setSrcId(srcId);
////				logger.info("[{}]CmppSubmitRequestMessage {}, waitTime:{}",
////						, sb.toString(), waitTime);
//				ctx.writeAndFlush(newMessage);
//				//logger.info("CmppSubmitRequestMessage :{}", newMessage);
////				cntSend.incrementAndGet();
//				amap.put(sb.toString(), sb.toString());
//				cnt.getAndIncrement();
//			}
//		}
//	}
//	@Override
//	public String name() {
//		return "SessionConnectedHandler-Gate";
//	}
//
//	public SendHandler clone() throws CloneNotSupportedException {
//		SendHandler ret = (SendHandler) super.clone();
////		ret.cnt = new AtomicLong();
//		ret.lastNum = 0;
//		return ret;
//	}
//
//	public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
//
//		if (msg instanceof CmppDeliverRequestMessage) {
//			CmppDeliverRequestMessage e = (CmppDeliverRequestMessage) msg;
//			//logger.info("CmppDeliverRequestMessage : {}", e);
//			if(e.getFragments()!=null) {
//				//长短信会带有片断
//				for(CmppDeliverRequestMessage frag:e.getFragments()) {
//					CmppDeliverResponseMessage responseMessage = new CmppDeliverResponseMessage(frag.getHeader().getSequenceId());
//					responseMessage.setResult(0);
//					responseMessage.setMsgId(frag.getMsgId());
//					ctx.channel().write(responseMessage);
//				}
//			}
//
//			CmppDeliverResponseMessage responseMessage = new CmppDeliverResponseMessage(e.getHeader().getSequenceId());
//			responseMessage.setResult(0);
//			responseMessage.setMsgId(e.getMsgId());
//			ChannelFuture future = ctx.channel().writeAndFlush(responseMessage);
//			if (future != null)
//				future.addListener(new GenericFutureListener() {
//					@Override
//					public void operationComplete(Future future) throws Exception {
////						cnt.incrementAndGet();
//						if(e.getReportRequestMessage()!=null)
//							amap.remove(e.getReportRequestMessage().getDestterminalId());
//					}
//				});
//
//
//		} else if (msg instanceof CmppDeliverResponseMessage) {
//			CmppDeliverResponseMessage e = (CmppDeliverResponseMessage) msg;
//
//		} else if (msg instanceof CmppSubmitRequestMessage) {
//			//接收到 CmppSubmitRequestMessage 消息
//			CmppSubmitRequestMessage e = (CmppSubmitRequestMessage) msg;
//
//			final List<CmppDeliverRequestMessage> reportlist = new ArrayList<CmppDeliverRequestMessage>();
//
//			if(e.getFragments()!=null) {
//				//长短信会可能带有片断，每个片断都要回复一个response
//				for(CmppSubmitRequestMessage frag:e.getFragments()) {
//					CmppSubmitResponseMessage responseMessage = new CmppSubmitResponseMessage(frag.getHeader().getSequenceId());
//					responseMessage.setResult(0);
//					ctx.channel().write(responseMessage);
//
//					CmppDeliverRequestMessage deliver = new CmppDeliverRequestMessage();
//					deliver.setDestId(e.getSrcId());
//					deliver.setSrcterminalId(e.getDestterminalId()[0]);
//					CmppReportRequestMessage report = new CmppReportRequestMessage();
//					report.setDestterminalId(deliver.getSrcterminalId());
//					report.setMsgId(responseMessage.getMsgId());
//					String t = DateFormatUtils.format(CachedMillisecondClock.INS.now(), "yyMMddHHmm");
//					report.setSubmitTime(t);
//					report.setDoneTime(t);
//					report.setStat("DELIVRD");
//					report.setSmscSequence(0);
//					deliver.setReportRequestMessage(report);
//					reportlist.add(deliver);
//				}
//			}
//
//			final CmppSubmitResponseMessage resp = new CmppSubmitResponseMessage(e.getHeader().getSequenceId());
//			resp.setResult(0);
//
//			ctx.channel().writeAndFlush(resp);
//
//			//回复状态报告
//			if(e.getRegisteredDelivery()==1) {
//
//				final CmppDeliverRequestMessage deliver = new CmppDeliverRequestMessage();
//				deliver.setDestId(e.getSrcId());
//				deliver.setSrcterminalId(e.getDestterminalId()[0]);
//				CmppReportRequestMessage report = new CmppReportRequestMessage();
//				report.setDestterminalId(deliver.getSrcterminalId());
//				report.setMsgId(resp.getMsgId());
//				String t = DateFormatUtils.format(CachedMillisecondClock.INS.now(), "yyMMddHHmm");
//				report.setSubmitTime(t);
//				report.setDoneTime(t);
//				report.setStat("DELIVRD");
//				report.setSmscSequence(0);
//				deliver.setReportRequestMessage(report);
//				reportlist.add(deliver);
//
//				ctx.executor().submit(new Runnable() {
//					public void run() {
//						for(CmppDeliverRequestMessage t : reportlist)
//							ctx.channel().writeAndFlush(t);
//					}
//				});
//			}
//
//		} else if (msg instanceof CmppSubmitResponseMessage) {
//			CmppSubmitResponseMessage e = (CmppSubmitResponseMessage) msg;
//			//logger.info("CmppSubmitResponseMessage:{}", e);
//			countDownLatch.countDown();
//		} else if (msg instanceof CmppQueryRequestMessage) {
//			CmppQueryRequestMessage e = (CmppQueryRequestMessage) msg;
//			CmppQueryResponseMessage res = new CmppQueryResponseMessage(e.getHeader().getSequenceId());
//			ctx.channel().writeAndFlush(res);
//		} else {
//			ctx.fireChannelRead(msg);
//		}
//	}
//
//	public static List<String> phoneList = new ArrayList<String>();
////	static{
////
////		try {
////			@SuppressWarnings("resource")
////			BufferedReader br = new BufferedReader(new FileReader("d://phone.txt"));
////			String line = null;
////
////			while((line=br.readLine())!=null ){
////				if( line.length()==11)
////					phoneList.add(line);
////			}
////			logger.info("phone list size:{}", phoneList.size());
////		} catch (IOException e) {
////			e.printStackTrace();
////		}
////	}
//}
