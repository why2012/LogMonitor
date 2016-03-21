package com.logmonitor.processor.coordinate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logmonitor.balancer.restapi.ApiMain;
import com.logmonitor.balancer.restapi.Status;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Created by wanghaiyang on 16/3/21.
 */
public class ZkApi implements Runnable {
    private ZkResource zkResource;
    private String balancerHost;
    private Thread heartBeatThread;
    private boolean running = false;
    private int heartBeatInterval = 1000;//milli seconds
    private ObjectMapper objectMapper;
    private boolean online = false;

    public ZkApi(ZkResource zkResource, String balancerHost) {
        this.zkResource = zkResource;
        this.balancerHost = balancerHost;
        this.objectMapper = new ObjectMapper();
    }

    public void online() {
        try {
            if (!online) {
                URL url = new URL(balancerHost + ApiMain.CONSUME_ONLINE);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(url.openStream());
                String data = getData(bufferedInputStream);
                Status status = getStatus(data);
                String[] nodePathAndHost = status.getMsg().split(";");
                zkResource.setConsumeNodePath(nodePathAndHost[0]);
                zkResource.setZookeeperHost(nodePathAndHost[1]);
                zkResource.start();
                online = true;
                startHeartBeat();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void offline() {
        try {
            if (online) {
                URL url = new URL(balancerHost + ApiMain.CONSUME_OFFLINE + "/" + zkResource.getConsumeNodePath());
                BufferedInputStream bufferedInputStream = new BufferedInputStream(url.openStream());
                Status status = getStatus(getData(bufferedInputStream));
                online = false;
                stopHeartBeat();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendHeartBeatPak() {
        try {
            if (online) {
                URL url = new URL(balancerHost + ApiMain.HEART_BEAT + "/" + zkResource.getConsumeNodePath());
                BufferedInputStream bufferedInputStream = new BufferedInputStream(url.openStream());
                Status status = getStatus(getData(bufferedInputStream));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getData(BufferedInputStream bufferedInputStream) {
        byte[] buffer = new byte[100];
        StringBuilder stringBuilder = new StringBuilder();
        int len = 0;
        try {
            while ((len = bufferedInputStream.read(buffer)) > 0) {
                stringBuilder.append(new String(buffer, 0, len));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return stringBuilder.toString();
    }

    private Status getStatus(String json) {
        Status status = null;
        try {
            status = objectMapper.readValue(json, Status.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return status;
    }

    public void run() {
        while (running) {
            sendHeartBeatPak();
            try {
                TimeUnit.MILLISECONDS.sleep(heartBeatInterval);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void startHeartBeat() {
        if (running) {
            return;
        }
        heartBeatThread = new Thread(this);
        running = true;
        heartBeatThread.start();
    }

    private void stopHeartBeat() {
        running = false;
    }
}
