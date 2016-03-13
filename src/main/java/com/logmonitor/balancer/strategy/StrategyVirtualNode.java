package com.logmonitor.balancer.strategy;

import com.logmonitor.balancer.node.ConsumeNode;
import com.logmonitor.balancer.zkmonitor.ZkMonitor;

/**
 * Created by wanghaiyang on 16/3/13.
 */
public class StrategyVirtualNode {
    private int sourceNodeNum = 0;
    private int consumeNodeNum = 0;
    private int curConsumeNodeOwnedSourceNum = 0;
    private ConsumeNode consumeNode = null;
    private float weight = 0;

    public StrategyVirtualNode(ConsumeNode consumeNode) {
        curConsumeNodeOwnedSourceNum = consumeNode.getSourceNodeMap().size();
        this.consumeNode = consumeNode;
    }

    public int getSourceNodeNum() {
        return sourceNodeNum;
    }

    public void setSourceNodeNum(int sourceNodeNum) {
        this.sourceNodeNum = sourceNodeNum;
    }

    public int getConsumeNodeNum() {
        return consumeNodeNum;
    }

    public void setConsumeNodeNum(int consumeNodeNum) {
        this.consumeNodeNum = consumeNodeNum;
    }

    public int getCurConsumeNodeOwnedSourceNum() {
        return curConsumeNodeOwnedSourceNum;
    }

    public void setCurConsumeNodeOwnedSourceNum(int curConsumeNodeOwnedSourceNum) {
        this.curConsumeNodeOwnedSourceNum = curConsumeNodeOwnedSourceNum;
    }

    public ConsumeNode getConsumeNode() {
        return consumeNode;
    }

    public void setConsumeNode(ConsumeNode consumeNode) {
        this.consumeNode = consumeNode;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public String toString() {
        String result = "[StrategyVirtualNode]" + consumeNode.getNodePath() + " | ";
        result += "(";
        result += "sourceNodeNum: " + sourceNodeNum + ", ";
        result += "consumeNodeNum: " + consumeNodeNum + ", ";
        result += "curConsumeNodeOwnedSourceNum: " + curConsumeNodeOwnedSourceNum + ", ";
        result += "weight: " + weight;
        result += ")\n";
        return result;
    }
}
