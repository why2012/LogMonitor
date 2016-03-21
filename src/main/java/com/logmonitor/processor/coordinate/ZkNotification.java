package com.logmonitor.processor.coordinate;

import com.logmonitor.processor.adapter.Source;

/**
 * Created by wanghaiyang on 16/3/21.
 */
public interface ZkNotification {
    public void sourceAdded(Source source);
    public void sourceRemoved(Source source);
}
