package com.logmonitor.processor.adapter;

/**
 * Created by wanghaiyang on 16/3/21.
 */
public class Source {
    public String ip = "0.0.0.0";
    public int port = -1;
    public String sourceNodePath;

    public Source(String sourceNodePath) {
        this.sourceNodePath = sourceNodePath;
    }

    public Source(String ip, int port, String sourceNodePath) {
        this.ip = ip;
        this.port = port;
        this.sourceNodePath = sourceNodePath;
    }

    public boolean available() {
        if (port <= 0 || ip.equals("0.0.0.0")) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object source) {
        if (!(source instanceof Source)) {
            return false;
        }
        if (this == source) {
            return true;
        }
        if (this.sourceNodePath.equals(((Source)source).sourceNodePath)) {
            return true;
        }
        return false;
    }

    public String toString() {
        return "Source[Ip: " + ip + ", Port: " + port + ", Path: " + sourceNodePath + "]";
    }
}
