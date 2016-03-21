package com.logmonitor.processor.coordinate;

import com.logmonitor.balancer.Configuration;
import com.logmonitor.balancer.ZkBalancerFactory;
import com.logmonitor.balancer.node.SourceNode;
import com.logmonitor.balancer.zkbalancer.ZkBalancerForConsume;
import com.logmonitor.processor.adapter.Source;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

/**
 * Created by wanghaiyang on 16/3/21.
 */
public class ZkResource implements PathChildrenCacheListener {
    private String consumeNodePath;
    private String zookeeperHost;
    private ZkBalancerForConsume zkBalancerForConsume;
    private PathChildrenCache pathChildrenCache;
    private ZkNotification zkNotification;
    private Configuration configuration = new Configuration();

    public ZkResource() {
    }

    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
        PathChildrenCacheEvent.Type eventType = pathChildrenCacheEvent.getType();
        String nodePath = pathChildrenCacheEvent.getData().getPath();
        String[] pathArr = nodePath.split("/");
        String sourceNodePath = configuration.getZkSourceParentPath() + "/" + pathArr[pathArr.length - 1];
        Source source = null;
        switch (eventType) {
            case CHILD_ADDED:
                SourceNode sourceNode = new SourceNode();
                zkBalancerForConsume.getByNodeObj(sourceNode, sourceNodePath);
                source = new Source(sourceNode.getSourceIp(), sourceNode.getSourcePort(), sourceNodePath);
                if (zkNotification != null) {
                    zkNotification.sourceAdded(source);
                }
                break;
            case CHILD_REMOVED:
                source = new Source(sourceNodePath);
                if (zkNotification != null) {
                    zkNotification.sourceRemoved(source);
                }
                break;
            default:
        }
    }

    public ZkNotification getZkNotification() {
        return zkNotification;
    }

    public void setZkNotification(ZkNotification zkNotification) {
        this.zkNotification = zkNotification;
    }

    public String getConsumeNodePath() {
        return consumeNodePath;
    }

    public void setConsumeNodePath(String consumeNodePath) {
        this.consumeNodePath = consumeNodePath;
    }

    public String getZookeeperHost() {
        return zookeeperHost;
    }

    public void setZookeeperHost(String zookeeperHost) {
        this.zookeeperHost = zookeeperHost;
    }

    public void start() {
        try {
            if (this.pathChildrenCache != null) {
                this.pathChildrenCache.close();
                this.pathChildrenCache = null;
            }
            if (this.zkBalancerForConsume != null) {
                this.zkBalancerForConsume.close();
                this.zkBalancerForConsume = null;
            }
            configuration.clearZkHost();
            configuration.addZkHost(this.zookeeperHost);
            this.zkBalancerForConsume = getZkBalancerForConsume();
            this.pathChildrenCache = this.zkBalancerForConsume.getPathChildrenCache(
                    configuration.getZkConsumeParentPath() + "/" + consumeNodePath, true);
            this.pathChildrenCache.start();
            this.pathChildrenCache.getListenable().addListener(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ZkBalancerForConsume getZkBalancerForConsume() throws Exception {
        configuration.setZkMode(Configuration.ZkMode.CONSUME);
        ZkBalancerFactory zkBalancerFactory = ZkBalancerFactory.getInstance(configuration);
        ZkBalancerForConsume zkBalancerForConsume = (ZkBalancerForConsume)zkBalancerFactory.getZkBalancer();
        return zkBalancerForConsume;
    }

    public String toString() {
        String result = "ZkResource[";
        result += "ConsumeNodePath: " + consumeNodePath + ", zookeeperHost: " + zookeeperHost;
        result += "]";
        return result;
    }
}
