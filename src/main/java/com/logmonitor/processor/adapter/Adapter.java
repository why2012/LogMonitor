package com.logmonitor.processor.adapter;

import com.logmonitor.processor.coordinate.ZkNotification;

/**
 * Created by wanghaiyang on 16/3/16.
 */
public interface Adapter extends ZkNotification {
    public String getMsg();
}
