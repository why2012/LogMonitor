package com.logmonitor.balancer.ids;

/**
 * Created by wanghaiyang on 16/3/12.
 */
public class SourceId extends Id {
    private static final String prefix = "PRODUCER|";

    public SourceId() {
        uniqueId = prefix + getUUID();
    }
}