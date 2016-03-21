package com.logmonitor.balancer.restapi;

import com.logmonitor.balancer.ids.SourceId;
import com.logmonitor.balancer.zkbalancer.ZkBalancer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by wanghaiyang on 16/3/16.
 */
public class HeartBearScanner implements Runnable {
    private ZkBalancer zkBalancer = null;
    private Map<String, Long> pathAndTime = new ConcurrentHashMap<String, Long>();
    private int heartBeatScanInterval = 1000;//milli seconds
    private int heartExpiredInterval = 5000;//milli seconds
    private boolean running = false;
    private Thread thread;

    public HeartBearScanner(ZkBalancer zkBalancer) {
        this.zkBalancer = zkBalancer;
    }

    public void run() {
        while (running) {
            long curTime = System.currentTimeMillis();
            for (Map.Entry<String, Long> entry : pathAndTime.entrySet()) {
                long lastTime = entry.getValue();
                if (curTime - lastTime > heartExpiredInterval) {
                    String nodePath;
                    if (entry.getKey().startsWith(SourceId.prefix)) {
                        nodePath = zkBalancer.getZkSourceParentPath() + "/" + entry.getKey();
                    } else {
                        nodePath = zkBalancer.getZkConsumeParentPath() + "/" + entry.getKey();
                    }
                    if (zkBalancer.deleteNode(nodePath)) {
                        pathAndTime.remove(entry.getKey());
                    }
                }
            }
            try {
                TimeUnit.MILLISECONDS.sleep(heartBeatScanInterval);
            } catch (InterruptedException e) {
                //ignore
            }
        }
    }

    public void add(String path) {
        pathAndTime.put(path, System.currentTimeMillis());
    }

    public void add(String[] pathArr) {
        long time = System.currentTimeMillis();
        for (String path : pathArr) {
            pathAndTime.put(path, time);
        }
    }

    public boolean touch(String path) {
        if (pathAndTime.containsKey(path)) {
            pathAndTime.put(path, System.currentTimeMillis());
            return true;
        }
        return false;
    }

    public void remove(String path) {
        pathAndTime.remove(path);
    }

    public void start() {
        if (running) {
            return;
        }
        thread = new Thread(this);
        running = true;
        thread.start();
    }

    public void stop() {
        if (!running) {
            return;
        }
        running = false;
    }

    public int getHeartBeatScanInterval() {
        return heartBeatScanInterval;
    }

    public void setHeartBeatScanInterval(int heartBeatScanInterval) {
        this.heartBeatScanInterval = heartBeatScanInterval;
    }

    public int getHeartExpiredInterval() {
        return heartExpiredInterval;
    }

    public void setHeartExpiredInterval(int heartExpiredInterval) {
        this.heartExpiredInterval = heartExpiredInterval;
    }
}
