package com.logmonitor.balancer.ids;

import java.util.UUID;

/**
 * Created by wanghaiyang on 16/3/12.
 */
public abstract class Id {
    protected String uniqueId;

    public String getUniqueId() {
        return uniqueId;
    }

    protected static String getUUID() {
        return UUID.randomUUID().toString();
    }

    public String toString() {
        return uniqueId;
    }
}
