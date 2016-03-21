package com.logmonitor.balancer;

import org.omg.CORBA.PRIVATE_MEMBER;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wanghaiyang on 16/3/12.
 */
public class Configuration {
    private List<String> connectString = new ArrayList<String>();
    private int sessionTimeoutMs = 5000;
    private int connectionTimeoutMs = 3000;
    public enum ZkMode {SOURCE, CONSUME, NORMAL};
    public enum ZkCreateMode {PERSISTENT, EPHEMERAL};
    private ZkMode zkMode = ZkMode.SOURCE;
    private String zkTopNodeName = "logmonitor";
    private String zkSourceTopNodeName = "source/nodes";
    private String zkConsumeTopNodeName = "consume/nodes";
    //Ephemeral node can not have children
    private ZkCreateMode zkSourceNodeMode = ZkCreateMode.PERSISTENT;
    private ZkCreateMode zkConsumeNodeMode = ZkCreateMode.PERSISTENT;
    private ZkCreateMode zkDefaultNodeMode = ZkCreateMode.PERSISTENT;

    public void addZkHost(String host) {
        connectString.add(host);
    }

    public void addZkHost(String[] hosts) {
        for (String host : hosts) {
            connectString.add(host);
        }
    }

    public void clearZkHost() {
        connectString.clear();
    }

    public void removeHost(String host) {
        connectString.remove(host);
    }

    public String getZkHost() {
        StringBuilder hosts = new StringBuilder();
        for (String host : connectString) {
            hosts.append(host + ",");
        }
        if (hosts.length() > 0) {
            hosts.deleteCharAt(hosts.length() - 1);
        }
        return hosts.toString();
    }

    public int getSessionTimeoutMs() {
        return sessionTimeoutMs;
    }

    public void setSessionTimeoutMs(int sessionTimeoutMs) {
        this.sessionTimeoutMs = sessionTimeoutMs;
    }

    public int getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    public void setConnectionTimeoutMs(int connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }

    public ZkMode getZkMode() {
        return zkMode;
    }

    public void setZkMode(ZkMode zkMode) {
        this.zkMode = zkMode;
    }

    public String getZkTopNodeName() {
        return zkTopNodeName;
    }

    public void setZkTopNodeName(String zkTopNodeName) {
        this.zkTopNodeName = zkTopNodeName;
    }

    public String getZkSourceTopNodeName() {
        return zkSourceTopNodeName;
    }

    public void setZkSourceTopNodeName(String zkSourceTopNodeName) {
        this.zkSourceTopNodeName = zkSourceTopNodeName;
    }

    public String getZkConsumeTopNodeName() {
        return zkConsumeTopNodeName;
    }

    public void setZkConsumeTopNodeName(String zkConsumeTopNodeName) {
        this.zkConsumeTopNodeName = zkConsumeTopNodeName;
    }

    public ZkCreateMode getZkSourceNodeMode() {
        return zkSourceNodeMode;
    }

    public void setZkSourceNodeMode(ZkCreateMode zkSourceNodeMode) {
        this.zkSourceNodeMode = zkSourceNodeMode;
    }

    public ZkCreateMode getZkConsumeNodeMode() {
        return zkConsumeNodeMode;
    }

    public ZkCreateMode getZkDefaultNodeMode() {
        return zkDefaultNodeMode;
    }

    public void setZkDefaultNodeMode(ZkCreateMode zkDefaultNodeMode) {
        this.zkDefaultNodeMode = zkDefaultNodeMode;
    }

    public void setZkConsumeNodeMode(ZkCreateMode zkConsumeNodeMode) {
        this.zkConsumeNodeMode = zkConsumeNodeMode;
    }

    public String getZkSourceParentPath() {
        return "/" + zkTopNodeName + "/" + zkSourceTopNodeName;
    }

    public String getZkConsumeParentPath() {
        return "/" + zkTopNodeName + "/" + zkConsumeTopNodeName;
    }
}
