package com.logmonitor.balancer.ids;

/**
 * Created by wanghaiyang on 16/3/12.
 */
public class ConsumeId extends Id {
    public static final String prefix = "CONSUMER|";

    public ConsumeId() {
        uniqueId = prefix + getUUID();
    }
}
