package com.logmonitor.balancer;

import com.logmonitor.balancer.strategy.SourceNodeCountStrategy;
import com.logmonitor.balancer.zkbalancer.ZkBalancer;
import com.logmonitor.balancer.zkmonitor.ZkMonitor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * Created by wanghaiyang on 16/3/13.
 */
public class ZkMonitorTest {

    //@Test
    public void testZkMonitor() throws Exception {
        Configuration configuration = new Configuration();
        configuration.addZkHost("127.0.0.1:2181");
        configuration.setZkMode(Configuration.ZkMode.NORMAL);
        ZkBalancerFactory zkBalancerFactory = ZkBalancerFactory.getInstance(configuration);
        final ZkBalancer zkBalancer = zkBalancerFactory.getZkBalancer();
        final ZkMonitor zkMonitor = new ZkMonitor(new SourceNodeCountStrategy());
        zkMonitor.registZkBalancer(zkBalancer);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                zkMonitor.stop();
                System.out.println("Quit.");
            }
        });

        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
    }
}
