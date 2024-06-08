package com.hzzx.protection;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : HuangZx
 * @date : 2024/6/7 13:16
 */
@Slf4j
public class CircuitBreaker {
    //三个状态 open halfopen  close
    /**
     * 闭->开
     * 在设定的时间窗口内失败次数达到阈值，由闭->开。
     * 开->半开
     * 在处于开的状态，对目标的调用做失败返回，进入开的时候，启动计时器，设定时间过后进入半开状态。
     * 半开->开
     * 进入半开状态，会启动一个计数器，记录连续成功的调用次数，超过阈值，进入闭状态。有一次失败则进入开状态，同时清零连续成功调用次数。进入开的同时启动进入半开状态的定时器。
     * 半开->闭
     * 进入半开状态，会启动一个计数器，记录连续成功的调用次数，超过阈值，进入闭状态，同时清零连续成功调用次数。
     */
    private CircuitBreakerConfig config;

    private volatile CircuitBreakerState state = CircuitBreakerState.CLOSED;

    //最近进入open状态的时间
    private volatile long lastOpenedTime;

    //closed状态下失败次数，采用的是窗口计数，不统计失败率了
    private LimitCounter failCount ;

    //half-open状态的连续成功次数,失败立即清零
    private AtomicInteger consecutiveSuccCount = new AtomicInteger(0);

    public CircuitBreaker(CircuitBreakerConfig circuitBreakerConfig){
        config = circuitBreakerConfig;
        failCount = new LimitCounter(config.getFailCountWindowInMs(),config.getFailThreshold());
    }

    /**
     * 状态判断
     * @return
     */
    public boolean isOpen(){
        return state == CircuitBreakerState.OPEN;
    }
    public boolean isHalfOpen(){
        return state == CircuitBreakerState.HALF_OPEN;
    }
    public boolean isClosed(){
        return state == CircuitBreakerState.CLOSED;
    }
    /**
     * 状态转移条件判断
     */
    /**
     * 是否应该从open -> halfopen
     */
    public boolean isOpenToHalfOpen(){
        return System.currentTimeMillis() - config.getOpen2HalfOpenTimeoutInMs() > lastOpenedTime;
    }
    /**
     *close->open
     */
    public boolean isCloseToOpen(){
        return failCount.thresholdReached();
    }

    /**
     *close->open
     */
    public boolean isHalfOpenToClose(){
        return consecutiveSuccCount.get()>=config.getConsecutiveSuccThreshold();
    }

    public boolean isHalfOpenToOpen(){
        //half状态只要失败自动调用open。
        return false;
    }

    /**
     * 状态转移
     */
    public void open(){
        this.lastOpenedTime = System.currentTimeMillis();
        state = CircuitBreakerState.OPEN;
        //todo:开启一个计时器,转入halfOpen
        log.debug("熔断器打开");
    }
    public void openHalf(){
        consecutiveSuccCount.set(0);
        state = CircuitBreakerState.HALF_OPEN;
        log.debug("进入半开状态");
    }
    public void close(){
        failCount.reset();
        state = CircuitBreakerState.CLOSED;
        log.debug("熔断器关闭");
    }

    //getter
    public void incrFailCount() {
        int count = failCount.incrAndGet();
    }

    public AtomicInteger getConsecutiveSuccCount() {
        return consecutiveSuccCount;
    }

    public CircuitBreakerState getState() {
        return state;
    }
}
