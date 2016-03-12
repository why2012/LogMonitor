package com.logmonitor.balancer.node;

/**
 * Created by wanghaiyang on 16/3/12.
 */
public class ZkNode {
    protected String nodePath = null;
    protected String nodeId = null;

    public String getNodePath() {
        return nodePath;
    }

    public void setNodePath(String nodePath) {
        this.nodePath = nodePath;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
}
