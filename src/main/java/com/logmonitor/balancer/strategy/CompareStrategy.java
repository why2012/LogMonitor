package com.logmonitor.balancer.strategy;

/**
 * Created by wanghaiyang on 16/3/13.
 */
public abstract class CompareStrategy implements Strategy {

    public int compare(StrategyVirtualNode strategyVirtualNode1, StrategyVirtualNode strategyVirtualNode2) {
        return (int)(strategyVirtualNode1.getWeight() - strategyVirtualNode2.getWeight());
    }
}
