package dorox.app.thread;

import cn.hutool.core.collection.ConcurrentHashSet;
import dorox.app.mq.event.PortStatEvent;
import dorox.app.util.AvroUtil;
import dorox.app.util.CacheConfig;
import dorox.app.util.MqUtil;
import dorox.app.util.Statics;
import dorox.app.vo.DeliverVoCMPP;
import dorox.app.vo.ResponseVoCMPP;
import dorox.app.vo.SubmitVoCMPP;
import org.apache.avro.Schema;
import org.apache.avro.util.Utf8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 暂时每5分钟刷新cmpp中的request
 */

public class CmppDeliverRefresh extends Thread{
	private static final Logger logger = LoggerFactory.getLogger(CmppDeliverRefresh.class);
	private RabbitTemplate rabbitTemplate;

	public CmppDeliverRefresh(RabbitTemplate rabbitTemplate){
		this.rabbitTemplate = rabbitTemplate;
	}

	@Override
	public void run() {
		try {
			cmppCacheRefresh();
		} catch (Exception e) {
			logger.info("Exception {}", e);
		}
	}


	private void cmppCacheRefresh(){

		DeliverVoCMPP deliverVo;
		while(( deliverVo = Statics.DELIVER_QUEUE.poll())!=null){

			String upPort = deliverVo.getUpPortCode();
			String msgId = deliverVo.getMsgId();
			String key = upPort + msgId;
			ResponseVoCMPP resp = CacheConfig.responseVoCacheCMPP.getIfPresent(key);

			/*有响应消息*/
			if(resp!=null){
				/*如果响应成功*/
				String submitKey = resp.getMessageId();
//				发送的时候并未携带所有数据，这里可以取出 messageId 对应的 所有的数据
				SubmitVoCMPP submitVo = CacheConfig.submitVoCacheCMPP.getIfPresent(submitKey);
				if(submitVo != null){
					List<String> msgIds = submitVo.getMsgIds();
					String downMsgId = msgIds.remove(0);
					PortStatEvent event = new PortStatEvent(submitVo.getMessageId(),
								submitVo.getDownPortCode(), upPort, deliverVo.getPhone(), deliverVo.getStat(),
								downMsgId, resp.getSeqNo(), Statics.DOWN_REGION_CODE_MAP.get(submitVo.getDownPortCode()), submitVo.getSrcId());

					MqUtil.sendMsg(event, rabbitTemplate, event.getDownRegionCode() + ".report");
				}
			}else {
				logger.info("deliverVo not found response:{}", deliverVo);
			}
		}
	}



}
