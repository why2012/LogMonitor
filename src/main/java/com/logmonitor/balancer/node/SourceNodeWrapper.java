package com.logmonitor.balancer.node;

import com.logmonitor.balancer.strategy.StrategyVirtualNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wanghaiyang on 16/3/13.
 *
 * stored mapping between sourceNode and its owner, consumeNodes
 */
public class SourceNodeWrapper {
    private SourceNode sourceNode;
    private List<StrategyVirtualNode> strategyVirtualNodeList;

    public SourceNodeWrapper(SourceNode sourceNode) {
        this.sourceNode = sourceNode;
        strategyVirtualNodeList = new ArrayList<StrategyVirtualNode>();
    }

    public SourceNode getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(SourceNode sourceNode) {
        this.sourceNode = sourceNode;
    }

    public void addNode(StrategyVirtualNode consumeNode) {
        strategyVirtualNodeList.add(consumeNode);
    }

    public void removeNode(StrategyVirtualNode consumeNode) {
        strategyVirtualNodeList.remove(consumeNode);
    }

    public void clearNodes() {
        strategyVirtualNodeList.clear();
    }

    public int nodesLength() {
        return strategyVirtualNodeList.size();
    }

    public List<StrategyVirtualNode> getStrategyVirtualNodeList() {
        return strategyVirtualNodeList;
    }

    public String toString() {
        String result = "[SourceNodeWrapper]" + sourceNode.getNodePath() + "\n";
        result += strategyVirtualNodeList + "\n";
        return result;
    }
}
