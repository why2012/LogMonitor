package com.logmonitor.balancer;

import com.logmonitor.balancer.zkbalancer.ZkBalancer;
import com.logmonitor.balancer.zkbalancer.ZkBalancerForConsume;
import com.logmonitor.balancer.zkbalancer.ZkBalancerForSource;
import org.apache.curator.RetryPolicy;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * Created by wanghaiyang on 16/3/12.
 */
public class ZkBalancerFactory {
    private static Configuration configuration = null;
    private static ZkBalancerFactory zkBalancerFactory = null;
    private static ThreadLocal<ZkBalancer> zkBalancerThreadLocal;
    private static RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

    private ZkBalancerFactory() {
        zkBalancerThreadLocal = new ThreadLocal<ZkBalancer>();
    }

    public static ZkBalancerFactory getInstance(Configuration configuration) {
        ZkBalancerFactory.configuration = configuration;
        if (zkBalancerFactory == null) {
            synchronized (ZkBalancerFactory.class) {
                if (zkBalancerFactory == null) {
                    zkBalancerFactory = new ZkBalancerFactory();
                }
            }
        }
        return zkBalancerFactory;
    }

    public ZkBalancer getZkBalancer() {
        ZkBalancer zkBalancer = zkBalancerThreadLocal.get();
        if (zkBalancer == null) {
            String parentPath = "/" + configuration.getZkTopNodeName() + "/";
            switch (configuration.getZkMode()) {
                case SOURCE:
                    zkBalancer = new ZkBalancerForSource(
                            configuration.getZkHost(),
                            configuration.getSessionTimeoutMs(),
                            configuration.getConnectionTimeoutMs(),
                            retryPolicy);
                    zkBalancer.setParentPath(parentPath + configuration.getZkSourceTopNodeName() + "/");
                    zkBalancer.setNodeMode(configuration.getZkSourceNodeMode());
                    break;
                case CONSUME:
                    zkBalancer = new ZkBalancerForConsume(
                            configuration.getZkHost(),
                            configuration.getSessionTimeoutMs(),
                            configuration.getConnectionTimeoutMs(),
                            retryPolicy);
                    zkBalancer.setParentPath(parentPath + configuration.getZkConsumeTopNodeName() + "/");
                    zkBalancer.setNodeMode(configuration.getZkConsumeNodeMode());
                    break;
                default:
                    throw new RuntimeException("Illegal Zk Mode: " + configuration.getZkMode());
            }
            zkBalancer.setZkSourceParentPath(configuration.getZkSourceParentPath());
            zkBalancer.setZkConsumeParentPath(configuration.getZkConsumeParentPath());
            zkBalancerThreadLocal.set(zkBalancer);
        }
        return zkBalancer;
    }
}
