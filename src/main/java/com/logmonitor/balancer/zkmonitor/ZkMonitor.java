package com.logmonitor.balancer.zkmonitor;

import com.logmonitor.balancer.Configuration;
import com.logmonitor.balancer.node.ConsumeNode;
import com.logmonitor.balancer.node.SourceNode;
import com.logmonitor.balancer.node.SourceNodeWrapper;
import com.logmonitor.balancer.strategy.Strategy;
import com.logmonitor.balancer.strategy.StrategyVirtualNode;
import com.logmonitor.balancer.zkbalancer.ZkBalancer;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wanghaiyang on 16/3/12.
 */
public class ZkMonitor implements PathChildrenCacheListener{
    //sorted by nodes' weight, stored consumeNode and its properties
    private List<StrategyVirtualNode> strategyVirtualNodeList = new ArrayList<StrategyVirtualNode>();
    //path -> SourceNodeWrapper, stored SourceNode and its owners
    private Map<String, SourceNodeWrapper> sourceNodeWrapperMap = new HashMap<String, SourceNodeWrapper>();
    //SourceNode cache, if no consume node when SourceNode added, then cache the SourceNode.
    private List<SourceNodeWrapper> sourceNodeWrapperCache = new ArrayList<SourceNodeWrapper>();
    //node weight caculator
    private Strategy strategy;
    //select first few consume nodes, allocate sourcenode to these consume node
    private int selectCount = 2;
    private ZkBalancer zkBalancer;
    private PathChildrenCache pathChildrenCache = null;

    public ZkMonitor(Strategy strategy) {
        this.strategy = strategy;
    }

    public void registZkBalancer(ZkBalancer zkBalancer) {
        try {
            this.zkBalancer = zkBalancer;
            scanNodesAndInit();
            if (pathChildrenCache != null) {
                pathChildrenCache.close();
            }
            pathChildrenCache = zkBalancer.getPathChildrenCache(Configuration.ZkMode.SOURCE, true);
            pathChildrenCache.start();
            pathChildrenCache.getListenable().addListener(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void scanNodesAndInit() {
        List<SourceNode> curSourceNodeList = zkBalancer.getAllSourceNodes();
        for (SourceNode sourceNode : curSourceNodeList) {
            SourceNodeWrapper sourceNodeWrapper = new SourceNodeWrapper(sourceNode);
            sourceNodeWrapperMap.put(sourceNode.getNodePath(), sourceNodeWrapper);
        }
        List<ConsumeNode> consumeNodeList = zkBalancer.getAllConsumeNodes();
        for (ConsumeNode consumeNode : consumeNodeList) {
            StrategyVirtualNode strategyVirtualNode = new StrategyVirtualNode(consumeNode);
            strategyVirtualNode.setSourceNodeNum(sourceNodeWrapperMap.size());
            strategyVirtualNode.setCurConsumeNodeOwnedSourceNum(consumeNode.getSourceNodeMap().size());
            for (Map.Entry<String, SourceNode> entry : consumeNode.getSourceNodeMap().entrySet()) {
                sourceNodeWrapperMap.get(entry.getKey()).addNode(strategyVirtualNode);
            }
            strategyVirtualNodeList.add(strategyVirtualNode);
        }
        for (StrategyVirtualNode strategyVirtualNode : strategyVirtualNodeList) {
            strategyVirtualNode.setConsumeNodeNum(strategyVirtualNodeList.size());
            strategyVirtualNode.setWeight(strategy.caculateWeight(strategyVirtualNode));
        }
        updateWeightList();
    }

    private synchronized void updateWeightList() {
        strategyVirtualNodeList.sort(strategy);
        System.out.println(this);
    }

    private void addSourceNodeWrapper(String sourceNodePath) {
        synchronized (sourceNodeWrapperMap) {
            SourceNode sourceNode = new SourceNode();
            zkBalancer.getByNodeObj(sourceNode, sourceNodePath);
            sourceNode.setNodePath(sourceNodePath);
            List<StrategyVirtualNode> strategyVirtualNodes = strategyVirtualNodeList.subList(0, Math.min(selectCount, strategyVirtualNodeList.size()));
            SourceNodeWrapper sourceNodeWrapper = new SourceNodeWrapper(sourceNode);
            if (strategyVirtualNodeList.size() <= 0) {
                //cache the SourceNode
                sourceNodeWrapperCache.add(sourceNodeWrapper);
                return;
            }
            sourceNodeWrapperMap.put(sourceNodePath, sourceNodeWrapper);
            for (StrategyVirtualNode strategyVirtualNode : strategyVirtualNodes) {
                sourceNodeWrapper.addNode(strategyVirtualNode);
                strategyVirtualNode.setConsumeNodeNum(strategyVirtualNodeList.size());
                strategyVirtualNode.setSourceNodeNum(sourceNodeWrapperMap.size());
                strategyVirtualNode.setCurConsumeNodeOwnedSourceNum(strategyVirtualNode.getCurConsumeNodeOwnedSourceNum() + 1);
                strategyVirtualNode.setWeight(strategy.caculateWeight(strategyVirtualNode));
                mountSourceNode(sourceNode, strategyVirtualNode.getConsumeNode());
            }
            updateWeightList();
        }
    }

    private void mountSourceNode(SourceNode sourceNode, ConsumeNode consumeNode) {
        String path = consumeNode.getNodePath() + "/" + sourceNode.getNodeName();
        zkBalancer.createNode(path);
        //set sourceNode allocated flag to true
        zkBalancer.setNodeData(sourceNode.getAllocPath(), new Boolean(true).toString().getBytes());
    }

    private void unmountSourceNode(SourceNode sourceNode, ConsumeNode consumeNode) {
        String path = consumeNode.getNodePath() + "/" + sourceNode.getNodeName();
        zkBalancer.deleteNode(path);
    }

    private void removeSourceNodeWrapper(String sourceNodePath) {
        synchronized (sourceNodeWrapperMap) {
            SourceNodeWrapper sourceNodeWrapper = sourceNodeWrapperMap.get(sourceNodePath);
            if (sourceNodeWrapper == null) {
                return;
            }
            sourceNodeWrapperMap.remove(sourceNodePath);
            for (StrategyVirtualNode strategyVirtualNode : sourceNodeWrapper.getStrategyVirtualNodeList()) {
                strategyVirtualNode.setConsumeNodeNum(strategyVirtualNodeList.size());
                strategyVirtualNode.setSourceNodeNum(sourceNodeWrapperMap.size());
                strategyVirtualNode.setCurConsumeNodeOwnedSourceNum(strategyVirtualNode.getCurConsumeNodeOwnedSourceNum() - 1);
                strategyVirtualNode.setWeight(strategy.caculateWeight(strategyVirtualNode));
                unmountSourceNode(sourceNodeWrapper.getSourceNode(), strategyVirtualNode.getConsumeNode());
            }
            updateWeightList();
        }
    }

    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
        PathChildrenCacheEvent.Type eventType = pathChildrenCacheEvent.getType();
        String nodePath = pathChildrenCacheEvent.getData().getPath();
        System.out.println(eventType);
        switch (eventType) {
            case CHILD_ADDED:
                if (sourceNodeWrapperMap.get(nodePath).nodesLength() == 0 ) {
                    addSourceNodeWrapper(nodePath);
                }
                break;
            case CHILD_REMOVED:
                removeSourceNodeWrapper(nodePath);
                break;
            default:
        }
    }

    public String toString() {
        String result = "[ZkMonitor]\n";
        result += "strategyVirtualNodeList: (" + strategyVirtualNodeList + ")\n";
        result += "sourceNodeWrapperMap: (" + sourceNodeWrapperMap + ")\n";
        result += "sourceNodeWrapperCache: (" + sourceNodeWrapperCache + ")\n";
        return result;
    }
}
