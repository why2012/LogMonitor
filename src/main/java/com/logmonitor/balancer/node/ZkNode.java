package com.logmonitor.balancer.node;

/**
 * Created by wanghaiyang on 16/3/12.
 */
public abstract class ZkNode {
    protected String nodePath = null;
    protected String nodeId = null;

    public String getNodePath() {
        return nodePath;
    }

    public void setNodePath(String nodePath) {
        this.nodePath = nodePath;
        this.nodeId = nodePath.split("\\|")[1];
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public abstract String getNodeName();
}
