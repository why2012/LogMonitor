package com.logmonitor.balancer.zkbalancer;

import com.logmonitor.balancer.Configuration;
import com.logmonitor.balancer.node.ConsumeNode;
import com.logmonitor.balancer.node.SourceNode;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.zookeeper.CreateMode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wanghaiyang on 16/3/12.
 */
public class ZkBalancer {
    protected CuratorFramework client;
    protected String parentPath = "/";
    protected CreateMode nodeMode = CreateMode.PERSISTENT;
    protected String zkSourceParentPath = "";
    protected String zkConsumeParentPath = "";

    public ZkBalancer(String connectString, int sessionTimeoutMs, int connectTimeoutMs, RetryPolicy retryPolicy) {
        client = CuratorFrameworkFactory.newClient(connectString, sessionTimeoutMs, connectTimeoutMs, retryPolicy);
        client.start();
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    public void setZkSourceParentPath(String zkSourceParentPath) {
        this.zkSourceParentPath = zkSourceParentPath;
    }

    public void setZkConsumeParentPath(String zkConsumeParentPath) {
        this.zkConsumeParentPath = zkConsumeParentPath;
    }

    public PathChildrenCache getPathChildrenCache(String path, boolean dataIsCompressed) {
        PathChildrenCache pathChildrenCache = new PathChildrenCache(client, path, dataIsCompressed);
        return pathChildrenCache;
    }

    public PathChildrenCache getPathChildrenCache(Configuration.ZkMode zkMode, boolean dataIsCompressed) {
        PathChildrenCache pathChildrenCache = null;
        switch (zkMode) {
            case SOURCE:
                pathChildrenCache = new PathChildrenCache(client, zkSourceParentPath, dataIsCompressed);
                break;
            case CONSUME:
                pathChildrenCache = new PathChildrenCache(client, zkConsumeParentPath, dataIsCompressed);
                break;
            default:
                throw new RuntimeException("Illegal ZkMode: " + zkMode);
        }
        return pathChildrenCache;
    }

    public void setNodeMode(Configuration.ZkCreateMode nodeMode) {
        switch (nodeMode) {
            case PERSISTENT:
                this.nodeMode = CreateMode.PERSISTENT;
                break;
            case EPHEMERAL:
                this.nodeMode = CreateMode.EPHEMERAL;
                break;
            default:
                throw new RuntimeException("Illegal Zk Create Mode.");
        }
    }

    public void createNode(String path, byte[] data) {
        try {
            client.create().creatingParentsIfNeeded().withMode(nodeMode)
                    .forPath(path,data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create node: " + path, e);
        }
    }

    public void createNode(String path) {
        try {
            client.create().creatingParentsIfNeeded().withMode(nodeMode)
                    .forPath(path);
        } catch (Exception e) {e.printStackTrace();
            throw new RuntimeException("Failed to create node: " + path, e);
        }
    }

    public void createByNodeObj(Object node, String path) {
        Field[] fields = node.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().startsWith("_")) {
                try {
                    field.setAccessible(true);
                    createNode(path + "/" + field.getName().substring(1), field.get(node).toString().getBytes());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void getByNodeObj(Object node, String path) {
        Field[] fields = node.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().startsWith("_")) {
                try {
                    field.setAccessible(true);
                    String nodePath = path + "/" + field.getName().substring(1);
                    byte[] data = client.getData().forPath(nodePath);
                    if (field.getType().equals(int.class)) {
                        field.setInt(node, Integer.valueOf(new String(data)));
                    } else if (field.getType().equals(boolean.class)) {
                        field.setBoolean(node, Boolean.valueOf(new String(data)));
                    } else if (field.getType().equals(String.class)) {
                        field.set(node, new String(data));
                    } else if (field.getType().equals(SourceNode.DIRECTION.class)) {
                        String tmp = new String(data);
                        if (tmp.equals(SourceNode.DIRECTION.LPOP)) {
                            field.set(node, SourceNode.DIRECTION.LPOP);
                        } else if (tmp.equals(SourceNode.DIRECTION.RPOP)) {
                            field.set(node, SourceNode.DIRECTION.RPOP);
                        }
                    } else {
                        field.set(node, data);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public List<ConsumeNode> getAllConsumeNodes() {
        List<ConsumeNode> consumeNodeList = new ArrayList<ConsumeNode>();
        try {
            List<String> pathList = client.getChildren().forPath(zkConsumeParentPath);
            for (String path : pathList) {
                ConsumeNode consumeNode = new ConsumeNode();
                consumeNode.setNodePath(zkConsumeParentPath + "/" + path);
                analyzeConsumeNode(consumeNode);
                consumeNodeList.add(consumeNode);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return consumeNodeList;
    }

    public ConsumeNode getConsumeNode(String path) {
        ConsumeNode consumeNode = new ConsumeNode();
        consumeNode.setNodePath(path);
        analyzeConsumeNode(consumeNode);
        return consumeNode;
    }

    public List<SourceNode> getAllSourceNodes() {
        List<SourceNode> sourceNodeList = new ArrayList<SourceNode>();
        try {
            List<String> pathList = client.getChildren().forPath(zkSourceParentPath);
            for (String path : pathList) {
                SourceNode sourceNode = new SourceNode();
                getByNodeObj(sourceNode,zkSourceParentPath + "/" + path);
                sourceNode.setNodePath(zkSourceParentPath + "/" + path);
                sourceNodeList.add(sourceNode);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return sourceNodeList;
    }

    public boolean deleteNode(String path) {
        try {
            client.delete().guaranteed().deletingChildrenIfNeeded().forPath(path);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean setNodeData(String path, byte[] data) {
        try {
            client.setData().forPath(path, data);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void close() {
        client.close();
    }

    public ConsumeNode analyzeConsumeNode(ConsumeNode consumeNode) {
        try {
            List<String> children = client.getChildren().forPath(consumeNode.getNodePath());
            for (String sourcePath : children) {
                sourcePath = zkSourceParentPath + "/" + sourcePath;
                SourceNode sourceNode = new SourceNode();
                getByNodeObj(sourceNode, sourcePath);
                sourceNode.setNodePath(sourcePath);
                consumeNode.addSourceNode(sourcePath, sourceNode);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return consumeNode;
    }
}
