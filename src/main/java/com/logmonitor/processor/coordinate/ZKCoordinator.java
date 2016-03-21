package com.logmonitor.processor.coordinate;

import com.logmonitor.processor.adapter.Adapter;

/**
 * Created by wanghaiyang on 16/3/16.
 */
public class ZKCoordinator {
    private String balancerHost;

    public ZKCoordinator(String balancerHost) {
        this.balancerHost = balancerHost;
    }

    public Adapter getAdapter() {
        return null;
    }

    public void online() {

    }

    public void offline() {

    }
}
