package com.logmonitor.balancer.strategy;

import java.util.Comparator;

/**
 * Created by wanghaiyang on 16/3/13.
 */
public interface Strategy extends Comparator<StrategyVirtualNode> {
    public float caculateWeight(StrategyVirtualNode strategyVirtualNode);
}
