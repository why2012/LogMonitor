package com.logmonitor.processor.adapter;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by wanghaiyang on 16/3/16.
 */
public class RedisAdapter implements Adapter, Runnable {
    private Map<String, Source> sourceMap = new ConcurrentHashMap<String, Source>();
    private Queue<String> msgQueue = new ConcurrentLinkedDeque<String>();

    public void sourceAdded(Source source) {
        sourceMap.put(source.sourceNodePath, source);
        System.out.println("Added: " + source);
    }

    public void sourceRemoved(Source source) {
        source = sourceMap.remove(source.sourceNodePath);
        System.out.println("Removed: " + source);
    }

    public void run() {

    }

    public String getMsg() {
        return null;
    }
}
