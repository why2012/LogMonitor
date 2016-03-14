package com.logmonitor.balancer;

import com.logmonitor.balancer.node.ConsumeNode;
import com.logmonitor.balancer.node.SourceNode;
import com.logmonitor.balancer.zkbalancer.ZkBalancer;
import com.logmonitor.balancer.zkbalancer.ZkBalancerForConsume;
import com.logmonitor.balancer.zkbalancer.ZkBalancerForSource;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by wanghaiyang on 16/3/12.
 */
public class ZkBalancerFactoryTest {
    private enum COMMAND {
        CREATE_SOURCE,
        CREATE_CONSUME,
        DELETE_SOURCE,
        DELETE_CONSUME,
        LS_SOURCE,
        LS_CONSUME
    };

    public static void main(String[] _args) throws Exception {
        ZkBalancerForSource zkBalancerForSource = getZkBalancerForSource();
        ZkBalancerForConsume zkBalancerForConsume = getZkBalancerForConsume();
        String commandLine = "";
        String args = "";
        Map<String, COMMAND> commandMap = new HashMap<String, COMMAND>();
        commandMap.put("create-source", COMMAND.CREATE_SOURCE);
        commandMap.put("delete-source", COMMAND.DELETE_SOURCE);
        commandMap.put("create-consume", COMMAND.CREATE_CONSUME);
        commandMap.put("delete-consume", COMMAND.DELETE_CONSUME);
        commandMap.put("ls-source", COMMAND.LS_SOURCE);
        commandMap.put("ls-consume", COMMAND.LS_CONSUME);
        Scanner scanner = new Scanner(System.in);
        while(true) {
            commandLine = scanner.nextLine();
            if (commandLine.equalsIgnoreCase("quit")) {
                break;
            }
            if (commandLine.contains(":")) {
                String[] arr = commandLine.split(":");
                if (arr.length > 1) {
                    args = arr[1].trim();
                    commandLine = commandLine.split(":")[0].trim();
                }
            }
            COMMAND command = commandMap.get(commandLine.toLowerCase());
            if (command == null) {
                System.err.println("No such command: " + commandLine);
                continue;
            }
            SourceNode sourceNode = null;
            ConsumeNode consumeNode = null;
            switch (command) {
                case CREATE_SOURCE:
                    sourceNode = new SourceNode("127.0.0.1", 1273, SourceNode.DIRECTION.RPOP);
                    zkBalancerForSource.registerSource(sourceNode);
                    System.out.println("[SourceNodePath]: " + sourceNode.getNodePath());
                    break;
                case CREATE_CONSUME:
                    consumeNode = new ConsumeNode();
                    zkBalancerForConsume.registerConsume(consumeNode);
                    System.out.println("[ConsumeNodePath]: " + consumeNode.getNodePath());
                    break;
                case DELETE_SOURCE:
                    sourceNode = new SourceNode();
                    sourceNode.setNodePath(args);
                    System.out.println(zkBalancerForSource.removeSource(sourceNode) + " -> " + args);
                    break;
                case DELETE_CONSUME:
                    consumeNode = new ConsumeNode();
                    consumeNode.setNodePath(args);
                    System.out.println(zkBalancerForConsume.removeConsume(consumeNode) + " -> " + args);
                    break;
                case LS_SOURCE:
                    List<SourceNode> sourceNodeList = zkBalancerForSource.getAllSourceNodes();
                    for (SourceNode sourceNode1 : sourceNodeList) {
                        System.out.println(sourceNode1.getNodePath());
                    }
                    break;
                case LS_CONSUME:
                    List<ConsumeNode> consumeNodeList = zkBalancerForConsume.getAllConsumeNodes();
                    for (ConsumeNode consumeNode1 : consumeNodeList) {
                        System.out.println(consumeNode1.getNodePath());
                    }
                    break;
                default:
                    System.err.println("No such command: " + commandLine);
            }
        }
        zkBalancerForConsume.close();
        zkBalancerForSource.close();
        scanner.close();
    }

    //@Test
    public void testFactorySourceRegister() throws Exception {
        ZkBalancerForSource zkBalancerForSource = getZkBalancerForSource();
        SourceNode sourceNode = new SourceNode("127.0.0.1", 1273, SourceNode.DIRECTION.RPOP);
        zkBalancerForSource.registerSource(sourceNode);
        System.out.println(sourceNode.getNodePath());
    }

    //@Test
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
        ZkBalancerForSource zkBalancerForSource = (ZkBalancerForSource)zkBalancerFactory.getZkBalancer(false);
        return zkBalancerForSource;
    }
}
