package org.n3r.idworker;

import cn.hutool.core.collection.ConcurrentHashSet;
import org.n3r.idworker.strategy.DefaultWorkerIdStrategy;
import org.n3r.idworker.utils.Utils;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class Sid {
    private static WorkerIdStrategy workerIdStrategy;
    private static IdWorker idWorker;

    static {
        configure(DefaultWorkerIdStrategy.instance);
    }


    public static synchronized void configure(WorkerIdStrategy custom) {
        if (workerIdStrategy != null) workerIdStrategy.release();
        workerIdStrategy = custom;
        idWorker = new IdWorker(workerIdStrategy.availableWorkerId()) {
            @Override
            public long getEpoch() {
                return Utils.midnightMillis();
            }
        };
    }

    /**
     * 一天最大毫秒86400000，最大占用27比特
     * 27+10+11=48位 最大值281474976710655(15字)，YK0XXHZ827(10字)
     * 6位(YYMMDD)+15位，共21位
     *
     * @return 固定21位数字字符串
     */

    public static String next() {
        long id = idWorker.nextId();
        System.out.println(id);
        String yyMMdd = new SimpleDateFormat("yyMMdd").format(new Date());
        return yyMMdd + String.format("%014d", id);
    }

    static SimpleDateFormat sdf = new SimpleDateFormat("MMdd");
    public String nextId() {
        long id = idWorker.nextId();

        String MMdd = sdf.format(new Date());
        return  String.format("%s%s%d","DL" ,MMdd, id);
    }

    /**
     * 返回固定16位的字母数字混编的字符串。
     */
    public String nextShort(String messageIdPrefix) {
        long id = idWorker.nextId();
        String yyMMdd = new SimpleDateFormat("MMdd").format(new Date());
        return messageIdPrefix + yyMMdd + Utils.padLeft(Utils.encode(id), 10, '0');
    }
    
    public static void main(String[] args) throws InterruptedException {
        Sid sid = new Sid();

        ConcurrentHashSet set = new ConcurrentHashSet();

        for(int j = 0; j < 10; j++){

            new Thread(()->{
                System.out.println( "begin :" + System.currentTimeMillis());
                for(int i = 0; i < 10000; i++) {
                    set.add(sid.nextShort("DL"));
                }
                System.out.println("end:" + System.currentTimeMillis());
            }).start();
        }


        Thread.sleep(2000);

        System.out.println(set.size());

	}
}
