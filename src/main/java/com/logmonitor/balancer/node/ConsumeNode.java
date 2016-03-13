package com.logmonitor.balancer.node;

import com.logmonitor.balancer.ids.SourceId;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by wanghaiyang on 16/3/12.
 */
public class ConsumeNode extends ZkNode {
    private Map<String, SourceNode> sourceNodeMap = new HashMap<String, SourceNode>();

    public void addSourceNode(String path, SourceNode sourceNode) {
        sourceNodeMap.put(path, sourceNode);
    }

    public void removeSourceNode(String path) {
        sourceNodeMap.remove(path);
    }

    public Set<Map.Entry<String, SourceNode>> getEntrySet() {
        return sourceNodeMap.entrySet();
    }

    public Map<String, SourceNode> getSourceNodeMap() {
        return sourceNodeMap;
    }

    @Override
    public String getNodeName() {
        return SourceId.prefix + nodeId;
    }
}
