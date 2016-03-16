package com.logmonitor.balancer.zkbalancer;

import com.logmonitor.balancer.ids.SourceId;
import com.logmonitor.balancer.node.SourceNode;
import org.apache.curator.RetryPolicy;

/**
 * Created by wanghaiyang on 16/3/12.
 */
public class ZkBalancerForSource extends ZkBalancer {

    public ZkBalancerForSource(String connectString, int sessionTimeoutMs, int connectTimeoutMs, RetryPolicy retryPolicy) {
        super(connectString, sessionTimeoutMs, connectTimeoutMs, retryPolicy);
    }

    public boolean registerSource(SourceNode sourceNode) {
        SourceId sourceId = new SourceId();
        String path = parentPath + "/" + sourceId.prefix + sourceId.getUniqueId();
        boolean result = createByNodeObj(sourceNode, path);
        sourceNode.setNodePath(path);
        sourceNode.setNodeId(sourceId.getUniqueId());
        return result;
    }

    public boolean removeSource(SourceNode sourceNode) {
        return deleteNode(sourceNode.getNodePath());
    }
}
