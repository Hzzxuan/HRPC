package com.hzzx.utils;

/**
 * @author : HuangZx
 * @date : 2024/6/1 21:20
 */

import com.hzzx.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.LongAdder;

/**
 * 雪花算法生成全局唯一ID
 * 机房号（数据中心） 5bit 32
 * 机器号 5bit 32
 * 时间戳（long 1970-1-1） 原本64位表示的时间，
 * 现在由41位构成
 * 同一个机房的同一个机器号的同一个时间可以因为并发量很大需要多个id
 * 序列号 12bit 1+41+5+5+12 = 64
 */
// 符号位（1）                 时间戳 （41）                   机房号 （5）     机器号（5）      序列号 （12）
//     0       01010101010101010101010101010101010101011      10101         10101       101011010101
@Slf4j
public class IdGenerator {
    // 起始时间戳
    public static final long START_STAMP = DateUtils.get("2024-5-28").getTime();
    //机房所占bit位
    public static final long DATA_CENTER_BIT = 5L;
    //机器号所占bit位
    public static final long MACHINE_BIT = 5L;
    //序列化12位
    public static final long SEQUENCE_BIT = 12L;
    //最大值
    public static final long DATA_CENTER_MAX = ~(-1L << DATA_CENTER_BIT);
    public static final long MACHINE_MAX = ~(-1L << MACHINE_BIT);
    public static final long SEQUENCE_MAX = ~(-1L << SEQUENCE_BIT);
    //组合时的移位
    public static final long TIMESTAMP_LEFT = DATA_CENTER_BIT + MACHINE_BIT +
            SEQUENCE_BIT;
    public static final long DATA_CENTER_LEFT = MACHINE_BIT + SEQUENCE_BIT;
    public static final long MACHINE_LEFT = SEQUENCE_BIT;

    private long dataCenterId;
    private long machineId;
    private long lastTimeStamp = -1L;

    private LongAdder sequenceId = new LongAdder();

    public IdGenerator(long dataCenterId, long machineId) {
// 判断传世的参数是否合法
        if(dataCenterId > DATA_CENTER_MAX || machineId > MACHINE_MAX){
            throw new IllegalArgumentException("你传入的数据中心编号或机器号不合法.");
        }
        this.dataCenterId = dataCenterId;
        this.machineId = machineId;
    }

    public long getId(){
        long stamp = System.currentTimeMillis()- START_STAMP;
        if(stamp < lastTimeStamp){
            log.error("系统时钟进行了回拨，请处理");
            throw new IllegalArgumentException();
        }
        if(lastTimeStamp == stamp){
            sequenceId.increment();
            if(sequenceId.sum() >= SEQUENCE_MAX){
                stamp = getNextTimeStamp();
                sequenceId.reset();
            }
        }else {
            sequenceId.reset();
        }
        lastTimeStamp = stamp;
        long sequence = sequenceId.longValue();
        return (stamp<<TIMESTAMP_LEFT)|(dataCenterId<<DATA_CENTER_LEFT)|(machineId<<MACHINE_LEFT)|(sequence)&(~(1L<<(Long.SIZE-1)));

    }

    private long getNextTimeStamp() {
        long now = System.currentTimeMillis()-START_STAMP;
        while (now == lastTimeStamp){
            now = getNextTimeStamp();
        }
        return now;
    }

    public static void main(String[] args) {
        //IdGenerator idGenerator = new IdGenerator(0,1);
        //for (int i = 0; i < 1000; i++) {
        //    new Thread(() -> System.out.println(idGenerator.getId())).start();
        //}
        Long num = 1796255650942980L;
        //String x = num.toByteString();
    }
}
