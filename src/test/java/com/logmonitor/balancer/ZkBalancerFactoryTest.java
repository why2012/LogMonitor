package com.logmonitor.balancer;

import com.logmonitor.balancer.node.ConsumeNode;
import com.logmonitor.balancer.node.SourceNode;
import com.logmonitor.balancer.zkbalancer.ZkBalancer;
import com.logmonitor.balancer.zkbalancer.ZkBalancerForConsume;
import com.logmonitor.balancer.zkbalancer.ZkBalancerForSource;
import org.junit.Test;

/**
 * Created by wanghaiyang on 16/3/12.
 */
public class ZkBalancerFactoryTest {
    //@Test
    public void testFactorySourceRegister() throws Exception {
        ZkBalancerForSource zkBalancerForSource = getZkBalancerForSource();
        SourceNode sourceNode = new SourceNode("127.0.0.1", 1273, SourceNode.DIRECTION.RPOP);
        zkBalancerForSource.registerSource(sourceNode);
        System.out.println(sourceNode.getNodePath());
    }

    @Test
    public void testFactorySourceRemove() throws Exception {
        ZkBalancerForSource zkBalancerForSource = getZkBalancerForSource();
        SourceNode sourceNode = new SourceNode("127.0.0.1", 1273, SourceNode.DIRECTION.RPOP);
        SourceNode sourceNode1 = new SourceNode();
        sourceNode1.setNodePath("/logmonitor/source/nodes/PRODUCER|9c890300-1eef-4a95-8a6b-a8e9b707b297");
        sourceNode.setNodePath("/logmonitor/source/nodes/PRODUCER|824482d6-193e-4b6e-91c4-818d36b35d48");
        System.out.println(zkBalancerForSource.removeSource(sourceNode1));
        System.out.println(zkBalancerForSource.removeSource(sourceNode));
    }

    //@Test
    public void testFactoryConsumeRegister() throws Exception {
        ZkBalancerForConsume zkBalancerForConsume = getZkBalancerForConsume();
        ConsumeNode consumeNode = new ConsumeNode();
        zkBalancerForConsume.registerConsume(consumeNode);
        System.out.println(consumeNode.getNodePath());
    }

    //@Test
    public void testFactoryConsumeRemove() throws Exception {
        ZkBalancerForConsume zkBalancerForConsume = getZkBalancerForConsume();
        ConsumeNode consumeNode = new ConsumeNode();
        consumeNode.setNodePath("/logmonitor/consume/nodes/CONSUMER|06790807-44ce-4f7a-a8b3-7872490ffc26");
        System.out.println(zkBalancerForConsume.removeConsume(consumeNode));
    }

    //@Test
    public void testFactoryConsumeAnalyze() throws Exception {
        ZkBalancerForConsume zkBalancerForConsume = getZkBalancerForConsume();
        ConsumeNode consumeNode = new ConsumeNode();
        consumeNode.setNodePath("/logmonitor/consume/nodes/CONSUMER|06790807-44ce-4f7a-a8b3-7872490ffc26");
        zkBalancerForConsume.analyzeConsumeNode(consumeNode);
        System.out.println(consumeNode.getSourceNodeMap());
    }

    private static ZkBalancerForConsume getZkBalancerForConsume() throws Exception {
        Configuration configuration = new Configuration();
        configuration.addZkHost("127.0.0.1:2181");
        configuration.setZkMode(Configuration.ZkMode.CONSUME);
        ZkBalancerFactory zkBalancerFactory = ZkBalancerFactory.getInstance(configuration);
        ZkBalancerForConsume zkBalancerForConsume = (ZkBalancerForConsume)zkBalancerFactory.getZkBalancer();
        return zkBalancerForConsume;
    }

    private static ZkBalancerForSource getZkBalancerForSource() throws Exception {
        Configuration configuration = new Configuration();
        configuration.addZkHost("127.0.0.1:2181");
        ZkBalancerFactory zkBalancerFactory = ZkBalancerFactory.getInstance(configuration);
        ZkBalancerForSource zkBalancerForSource = (ZkBalancerForSource)zkBalancerFactory.getZkBalancer();
        return zkBalancerForSource;
    }
}
