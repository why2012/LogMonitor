package com.logmonitor.balancer.zkbalancer;

import com.logmonitor.balancer.ids.ConsumeId;
import com.logmonitor.balancer.node.ConsumeNode;
import com.logmonitor.balancer.node.SourceNode;
import org.apache.curator.RetryPolicy;

import java.util.List;

/**
 * Created by wanghaiyang on 16/3/12.
 */
public class ZkBalancerForConsume extends ZkBalancer {

    public ZkBalancerForConsume(String connectString, int sessionTimeoutMs, int connectTimeoutMs, RetryPolicy retryPolicy) {
        super(connectString, sessionTimeoutMs, connectTimeoutMs, retryPolicy);
    }

    public ConsumeId registerConsume(ConsumeNode consumeNode) {
        ConsumeId consumeId = new ConsumeId();
        String path = parentPath + consumeId.getUniqueId();
        createNode(path);
        consumeNode.setNodePath(path);
        consumeNode.setNodeId(consumeId.getUniqueId());
        return consumeId;
    }

    public boolean removeConsume(ConsumeNode consumeNode) {
        return deleteNode(consumeNode.getNodePath());
    }

    public ConsumeNode analyzeConsumeNode(ConsumeNode consumeNode) {
        try {
            List<String> children = client.getChildren().forPath(consumeNode.getNodePath());
            for (String sourcePath : children) {
                sourcePath = zkSourceParentPath + sourcePath;
                SourceNode sourceNode = new SourceNode();
                getByNodeObj(sourceNode, sourcePath);
                sourceNode.setNodePath(sourcePath);
                sourceNode.setNodeId(sourcePath.split("|")[1]);
                consumeNode.addSourceNode(sourcePath, sourceNode);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return consumeNode;
    }
}
