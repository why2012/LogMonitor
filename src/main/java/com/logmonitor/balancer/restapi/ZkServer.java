package com.logmonitor.balancer.restapi;

import com.logmonitor.balancer.Configuration;
import com.logmonitor.balancer.ZkBalancerFactory;
import com.logmonitor.balancer.strategy.SourceNodeCountStrategy;
import com.logmonitor.balancer.zkbalancer.ZkBalancer;
import com.logmonitor.balancer.zkbalancer.ZkBalancerForConsume;
import com.logmonitor.balancer.zkbalancer.ZkBalancerForSource;
import com.logmonitor.balancer.zkmonitor.ZkMonitor;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * Created by wanghaiyang on 16/3/16.
 */
public class ZkServer extends Application<ServerConfiguration> {
    private Configuration configuration = new Configuration();
    private HeartBearScanner heartBearScanner = null;
    private ZkBalancerForSource zkBalancerForSource = null;
    private ZkBalancerForConsume zkBalancerForConsume = null;
    private ZkMonitor zkMonitor = null;

    public static void main(String[] args) throws Exception {
        new ZkServer().start(args);
    }

    public void start(String[] args) throws Exception {
        new ZkServer().run(args);
    }

    @Override
    public String getName() {
        return "ZkServerRestfulApi";
    }

    @Override
    public void initialize(Bootstrap<ServerConfiguration> bootstrap) {

    }

    @Override
    public void run(ServerConfiguration serverConfiguration, Environment environment) throws Exception {
        configuration.addZkHost(serverConfiguration.getZookeeperHosts());
        zkBalancerForSource = getZkBalancerForSource();
        zkBalancerForConsume = getZkBalancerForConsume();
        heartBearScanner = new HeartBearScanner(zkBalancerForSource);
        heartBearScanner.setHeartBeatScanInterval(serverConfiguration.getHeartBeatScanInterval());
        heartBearScanner.setHeartExpiredInterval(serverConfiguration.getHeartExpiredInterval());
        heartBearScanner.start();

        zkMonitor = getZkMonitor();
        String[] pathArr = zkMonitor.getAllPath();
        heartBearScanner.add(pathArr);

        ApiMain apiMain = new ApiMain(zkBalancerForSource, zkBalancerForConsume, serverConfiguration.getZookeeperHosts(), heartBearScanner);
        DefaultHealthCheck defaultHealthCheck = new DefaultHealthCheck();
        environment.healthChecks().register("default", defaultHealthCheck);
        environment.jersey().register(apiMain);
    }

    private ZkBalancerForConsume getZkBalancerForConsume() throws Exception {
        configuration.setZkMode(Configuration.ZkMode.CONSUME);
        ZkBalancerFactory zkBalancerFactory = ZkBalancerFactory.getInstance(configuration);
        ZkBalancerForConsume zkBalancerForConsume = (ZkBalancerForConsume)zkBalancerFactory.getZkBalancer();
        return zkBalancerForConsume;
    }

    private ZkBalancerForSource getZkBalancerForSource() throws Exception {
        configuration.setZkMode(Configuration.ZkMode.SOURCE);
        ZkBalancerFactory zkBalancerFactory = ZkBalancerFactory.getInstance(configuration);
        ZkBalancerForSource zkBalancerForSource = (ZkBalancerForSource)zkBalancerFactory.getZkBalancer(false);
        return zkBalancerForSource;
    }

    private ZkMonitor getZkMonitor() {
        configuration.setZkMode(Configuration.ZkMode.NORMAL);
        ZkBalancerFactory zkBalancerFactory = ZkBalancerFactory.getInstance(configuration);
        final ZkBalancer zkBalancer = zkBalancerFactory.getZkBalancer();
        final ZkMonitor zkMonitor = new ZkMonitor(new SourceNodeCountStrategy());
        zkMonitor.registZkBalancer(zkBalancer);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                zkMonitor.stop();
                System.out.println("ZkMonitor Quit.");
            }
        });
        return zkMonitor;
    }
}
