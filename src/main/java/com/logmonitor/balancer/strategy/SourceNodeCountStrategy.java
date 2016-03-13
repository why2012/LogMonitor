package com.logmonitor.balancer.strategy;

/**
 * Created by wanghaiyang on 16/3/13.
 */
public class SourceNodeCountStrategy extends CompareStrategy {

    public float caculateWeight(StrategyVirtualNode strategyVirtualNode) {
        return strategyVirtualNode.getCurConsumeNodeOwnedSourceNum();
    }
}
