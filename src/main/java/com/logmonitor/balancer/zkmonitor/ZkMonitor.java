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

import java.io.IOException;
import java.util.*;

/**
 * Created by wanghaiyang on 16/3/12.
 */
public class ZkMonitor implements PathChildrenCacheListener{
    //sorted by nodes' weight, stored consumeNode and its properties
    private List<StrategyVirtualNode> strategyVirtualNodeList = new ArrayList<StrategyVirtualNode>();
    private Map<String, StrategyVirtualNode> stringStrategyVirtualNodeMap = new HashMap<String, StrategyVirtualNode>();
    //path -> SourceNodeWrapper, stored SourceNode and its owners
    private Map<String, SourceNodeWrapper> sourceNodeWrapperMap = new HashMap<String, SourceNodeWrapper>();
    //SourceNode cache, if no consume node when SourceNode added, then cache the SourceNode.
    private List<SourceNodeWrapper> sourceNodeWrapperCache = new ArrayList<SourceNodeWrapper>();
    //node weight caculator
    private Strategy strategy;
    //select first few consume nodes, allocate sourcenode to these consume node
    private int selectCount = 2;
    private ZkBalancer zkBalancer;
    private PathChildrenCache pathChildrenCacheSource = null;
    private PathChildrenCache pathChildrenCacheConsume = null;

    public ZkMonitor(Strategy strategy) {
        this.strategy = strategy;
    }

    public void registZkBalancer(ZkBalancer zkBalancer) {
        try {
            this.zkBalancer = zkBalancer;
            scanNodesAndInit();
            if (pathChildrenCacheSource != null) {
                pathChildrenCacheSource.close();
            }
            pathChildrenCacheSource = zkBalancer.getPathChildrenCache(Configuration.ZkMode.SOURCE, true);
            pathChildrenCacheSource.start();
            pathChildrenCacheSource.getListenable().addListener(this);

            if (pathChildrenCacheConsume != null) {
                pathChildrenCacheConsume.close();
            }
            pathChildrenCacheConsume = zkBalancer.getPathChildrenCache(Configuration.ZkMode.CONSUME, true);
            pathChildrenCacheConsume.start();
            pathChildrenCacheConsume.getListenable().addListener(new ConsumeNodeWatcher());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println(this);
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
                SourceNodeWrapper sourceNodeWrapper = sourceNodeWrapperMap.get(entry.getKey());
                if (sourceNodeWrapper != null) {
                    sourceNodeWrapper.addNode(strategyVirtualNode);
                }
            }
            strategyVirtualNodeList.add(strategyVirtualNode);
            stringStrategyVirtualNodeMap.put(consumeNode.getNodePath(), strategyVirtualNode);
        }
        for (StrategyVirtualNode strategyVirtualNode : strategyVirtualNodeList) {
            strategyVirtualNode.setConsumeNodeNum(strategyVirtualNodeList.size());
            strategyVirtualNode.setWeight(strategy.caculateWeight(strategyVirtualNode));
        }
        updateWeightList();
    }

    private synchronized void updateWeightList() {
        strategyVirtualNodeList.sort(strategy);
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
                sourceNodeWrapperMap.put(sourceNodePath, sourceNodeWrapper);
                sourceNodeWrapperCache.add(sourceNodeWrapper);
                return;
            }
            sourceNodeWrapperMap.put(sourceNodePath, sourceNodeWrapper);
            for (StrategyVirtualNode strategyVirtualNode : strategyVirtualNodes) {
                sourceNodeWrapper.addNode(strategyVirtualNode);
                strategyVirtualNode.setCurConsumeNodeOwnedSourceNum(strategyVirtualNode.getCurConsumeNodeOwnedSourceNum() + 1);
                strategyVirtualNode.getConsumeNode().addSourceNode(sourceNodePath, sourceNode);
                mountSourceNode(sourceNode, strategyVirtualNode.getConsumeNode());
            }

            for (StrategyVirtualNode strategyVirtualNode : strategyVirtualNodeList) {
                strategyVirtualNode.setConsumeNodeNum(strategyVirtualNodeList.size());
                strategyVirtualNode.setSourceNodeNum(sourceNodeWrapperMap.size());
                strategyVirtualNode.setWeight(strategy.caculateWeight(strategyVirtualNode));
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
                strategyVirtualNode.setCurConsumeNodeOwnedSourceNum(strategyVirtualNode.getCurConsumeNodeOwnedSourceNum() - 1);
                strategyVirtualNode.getConsumeNode().removeSourceNode(sourceNodePath);
                unmountSourceNode(sourceNodeWrapper.getSourceNode(), strategyVirtualNode.getConsumeNode());
            }

            for (StrategyVirtualNode strategyVirtualNode : strategyVirtualNodeList) {
                strategyVirtualNode.setConsumeNodeNum(strategyVirtualNodeList.size());
                strategyVirtualNode.setSourceNodeNum(sourceNodeWrapperMap.size());
                strategyVirtualNode.setWeight(strategy.caculateWeight(strategyVirtualNode));
            }
            updateWeightList();
        }
    }

    /**
     * without caching extra sourceNode
     * @param sourceNode
     * @param _strategyVirtualNode
     * @return op result
     */
    private boolean unmountSourceNodeFromConsumeNode(SourceNode sourceNode, StrategyVirtualNode _strategyVirtualNode) {
        ConsumeNode consumeNode = _strategyVirtualNode.getConsumeNode();
        if (!consumeNode.containsSourceNode(sourceNode.getNodePath())) {
            return false;
        }
        consumeNode.removeSourceNode(sourceNode.getNodePath());
        StrategyVirtualNode strategyVirtualNode = stringStrategyVirtualNodeMap.get(consumeNode.getNodePath());
        strategyVirtualNode.setCurConsumeNodeOwnedSourceNum(consumeNode.getSourceNodeMap().size());
        sourceNodeWrapperMap.get(sourceNode.getNodePath()).removeNode(strategyVirtualNode);
        unmountSourceNode(sourceNode, consumeNode);
        _strategyVirtualNode.setWeight(strategy.caculateWeight(strategyVirtualNode));
        updateWeightList();
        return true;
    }

    /**
     * if consumeNode already contains the sourceNode, op failed
     * @param sourceNode
     * @param _strategyVirtualNode
     * @return op result
     */
    private boolean mountSourceNodeOnConsumeNode(SourceNode sourceNode, StrategyVirtualNode _strategyVirtualNode) {
        ConsumeNode consumeNode = _strategyVirtualNode.getConsumeNode();
        if (consumeNode.containsSourceNode(sourceNode.getNodePath())) {
            return false;
        }
        consumeNode.addSourceNode(sourceNode.getNodePath(), sourceNode);
        StrategyVirtualNode strategyVirtualNode = stringStrategyVirtualNodeMap.get(consumeNode.getNodePath());
        strategyVirtualNode.setCurConsumeNodeOwnedSourceNum(consumeNode.getSourceNodeMap().size());
        sourceNodeWrapperMap.get(sourceNode.getNodePath()).addNode(strategyVirtualNode);
        mountSourceNode(sourceNode, consumeNode);
        _strategyVirtualNode.setWeight(strategy.caculateWeight(strategyVirtualNode));
        updateWeightList();
        return true;
    }

    private void addConusmeNode(String consumeNodePath) {
        synchronized (strategyVirtualNodeList) {
            ConsumeNode consumeNode = zkBalancer.getConsumeNode(consumeNodePath);
            StrategyVirtualNode strategyVirtualNode = new StrategyVirtualNode(consumeNode);
            strategyVirtualNode.setSourceNodeNum(sourceNodeWrapperMap.size());
            strategyVirtualNode.setCurConsumeNodeOwnedSourceNum(consumeNode.getSourceNodeMap().size());
            for (Map.Entry<String, SourceNode> entry : consumeNode.getSourceNodeMap().entrySet()) {
                SourceNodeWrapper sourceNodeWrapper = sourceNodeWrapperMap.get(entry.getKey());
                if (sourceNodeWrapper != null) {
                    sourceNodeWrapper.addNode(strategyVirtualNode);
                }
            }
            strategyVirtualNodeList.add(strategyVirtualNode);
            stringStrategyVirtualNodeMap.put(consumeNodePath, strategyVirtualNode);
            for (StrategyVirtualNode strategyVirtualNodeExit : strategyVirtualNodeList) {
                strategyVirtualNodeExit.setConsumeNodeNum(strategyVirtualNodeList.size());
                strategyVirtualNodeExit.setWeight(strategy.caculateWeight(strategyVirtualNodeExit));
            }
            updateWeightList();
            //重新分配SourceNode
            rebalanceSourceNode();
        }
    }

    private void removeConsumeNode(String consumeNodePath) {
        synchronized (strategyVirtualNodeList) {
            StrategyVirtualNode strategyVirtualNode = stringStrategyVirtualNodeMap.remove(consumeNodePath);
            if (strategyVirtualNode == null) {
                return;
            }
            strategyVirtualNodeList.remove(strategyVirtualNode);
            Map<String, SourceNode> sourceNodeMap = strategyVirtualNode.getConsumeNode().getSourceNodeMap();
            List<SourceNodeWrapper> willCachedSourceNodes = new ArrayList<SourceNodeWrapper>();
            for (String sourceNodePath : sourceNodeMap.keySet()) {
                SourceNodeWrapper sourceNodeWrapper = sourceNodeWrapperMap.get(sourceNodePath);
                sourceNodeWrapper.removeNode(strategyVirtualNode);
                if (sourceNodeWrapper.nodesLength() == 0) {
                    willCachedSourceNodes.add(sourceNodeWrapper);
                    sourceNodeWrapperMap.remove(sourceNodePath);
                    zkBalancer.setNodeData(sourceNodeWrapper.getSourceNode().getAllocPath(), new Boolean(false).toString().getBytes());
                }
            }
            for (StrategyVirtualNode strategyVirtualNodeExit : strategyVirtualNodeList) {
                strategyVirtualNodeExit.setConsumeNodeNum(strategyVirtualNodeList.size());
                strategyVirtualNodeExit.setWeight(strategy.caculateWeight(strategyVirtualNodeExit));
            }
            updateWeightList();
            //缓存并重新分配SourceNode
            cacheExtraSourceNode(strategyVirtualNode, willCachedSourceNodes);
            rebalanceSourceNode();
        }
    }

    private void cacheExtraSourceNode(StrategyVirtualNode strategyVirtualNode, List<SourceNodeWrapper> willCahcedSourceNode) {
        sourceNodeWrapperCache.addAll(willCahcedSourceNode);
    }

    private void rebalanceSourceNode() {
        StrategyVirtualNode strategyVirtualNodeMax = null;
        StrategyVirtualNode strategyVirtualNodeMin = null;
        int totalSourceNodePossessedNum = 0;
        int avgSourceNodePossessedNum = 1;
        int maxSourceNodePossessedNum = 0;
        int minSourceNodePossessedNum = Integer.MAX_VALUE;
        if (strategyVirtualNodeList.size() > 1) {
            for (StrategyVirtualNode strategyVirtualNode : strategyVirtualNodeList) {
                int curPossessedNum = strategyVirtualNode.getCurConsumeNodeOwnedSourceNum();
                totalSourceNodePossessedNum += curPossessedNum;
                if (curPossessedNum > maxSourceNodePossessedNum) {
                    maxSourceNodePossessedNum = curPossessedNum;
                    strategyVirtualNodeMax = strategyVirtualNode;
                } else if (curPossessedNum < minSourceNodePossessedNum) {
                    minSourceNodePossessedNum = curPossessedNum;
                    strategyVirtualNodeMin = strategyVirtualNode;
                }
            }
            int avg = totalSourceNodePossessedNum / (strategyVirtualNodeList.size() - 1);
            avgSourceNodePossessedNum = avg == 0 ? 1 : avg;
            if (strategyVirtualNodeMax != strategyVirtualNodeMin && maxSourceNodePossessedNum > 0) {
                int nodeNumGap = maxSourceNodePossessedNum - avgSourceNodePossessedNum;
                Iterator<Map.Entry<String, SourceNode>> iterator = strategyVirtualNodeMax.getConsumeNode().getEntrySet().iterator();
                while (nodeNumGap >= 0 && strategyVirtualNodeMin.getCurConsumeNodeOwnedSourceNum() < avgSourceNodePossessedNum) {
                    SourceNode sourceNode = iterator.next().getValue();
                    if (nodeNumGap > 0) {
                        unmountSourceNodeFromConsumeNode(sourceNode, strategyVirtualNodeMax);
                    }
                    mountSourceNodeOnConsumeNode(sourceNode, strategyVirtualNodeMin);
                    nodeNumGap--;
                }
            }
        }

        if (strategyVirtualNodeList.size() > 0) {
            for (SourceNodeWrapper sourceNodeWrapper : sourceNodeWrapperCache) {
                addSourceNodeWrapper(sourceNodeWrapper.getSourceNode().getNodePath());
            }
            sourceNodeWrapperCache.clear();
        }
    }

    /**
     * SourceNode Watcher
     */
    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
        PathChildrenCacheEvent.Type eventType = pathChildrenCacheEvent.getType();
        String nodePath = pathChildrenCacheEvent.getData().getPath();
        System.out.println("SourceNode: " + eventType);
        switch (eventType) {
            case CHILD_ADDED:
                SourceNodeWrapper sourceNodeWrapper = sourceNodeWrapperMap.get(nodePath);
                if (sourceNodeWrapper == null || sourceNodeWrapper.nodesLength() == 0) {
                    addSourceNodeWrapper(nodePath);
                    System.out.println(this);
                }
                break;
            case CHILD_REMOVED:
                removeSourceNodeWrapper(nodePath);
                System.out.println(this);
                break;
            default:
        }
    }

    /**
     * ConsumeNode Watcher
     */
    private class ConsumeNodeWatcher implements PathChildrenCacheListener {

        public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
            PathChildrenCacheEvent.Type eventType = pathChildrenCacheEvent.getType();
            String nodePath = pathChildrenCacheEvent.getData().getPath();
            System.out.println("ConsumeNode: " + eventType);
            switch (eventType) {
                case CHILD_ADDED:
                    if (stringStrategyVirtualNodeMap.get(nodePath) == null) {
                        addConusmeNode(nodePath);
                        System.out.println(ZkMonitor.this);
                    }
                    break;
                case CHILD_REMOVED:
                    removeConsumeNode(nodePath);
                    System.out.println(ZkMonitor.this);
                    break;
                default:
            }

        }
    }

    public void stop() {
        zkBalancer.close();
        try {
            pathChildrenCacheSource.close();
            pathChildrenCacheConsume.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
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
