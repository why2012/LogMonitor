package com.logmonitor.filemonitor.handlers;

import redis.clients.jedis.Jedis;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by wanghaiyang on 16/3/11.
 *
 *            msgQueue, lpush, rpop
 *     -------------------------------
 * --->               msg...           --->
 *     -------------------------------
 */
public class RedisHandler implements Handler {
    private boolean running = false;
    private Thread thread = null;
    private ConcurrentLinkedQueue<String> dataList = new ConcurrentLinkedQueue<String>();
    private MsgModifier msgModifier = null;
    private Jedis jedis = null;
    private String host;
    private int port;
    private long redisQueueMaxSize;
    private String redisQueueKeyName;

    public RedisHandler(String host, int port, long redisQueueMaxSize, String redisQueueKeyName) {
        this.host = host;
        this.port = port;
        this.redisQueueMaxSize = redisQueueMaxSize;
        this.redisQueueKeyName = redisQueueKeyName;

        jedis = new Jedis(host, port);
    }

    public void notify(String data) {
        this.dataList.add(data);
    }

    public void run() {
        while(running) {
            if (dataList.size() > 0) {
                String tmpData = dataList.poll();
                if (this.msgModifier != null) {
                    tmpData = this.msgModifier.append(tmpData);
                }
                String[] lines = tmpData.split("\n");
                for (String line : lines) {
                    long currentQueueLength = jedis.llen(redisQueueKeyName);
                    while (currentQueueLength >= redisQueueMaxSize) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            currentQueueLength = jedis.llen(redisQueueKeyName);
                            continue;
                        }
                    }
                    jedis.lpush(redisQueueKeyName, line);
                }
            }
        }
    }

    public void start() {
        if (running) {
            return;
        }
        this.thread = new Thread(this);
        running = true;
        this.thread.start();
    }

    public void stop() {
        if (!running) {
            return;
        }
        this.running = false;
        jedis.close();
    }

    public void setModifier(MsgModifier msgModifier) {
        this.msgModifier = msgModifier;
    }

    public MsgModifier getModifier() {
        return this.msgModifier;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getRedisQueueMaxSize() {
        return redisQueueMaxSize;
    }

    public void setRedisQueueMaxSize(long redisQueueMaxSize) {
        this.redisQueueMaxSize = redisQueueMaxSize;
    }

    public String getRedisQueueKeyName() {
        return redisQueueKeyName;
    }

    public void setRedisQueueKeyName(String redisQueueKeyName) {
        this.redisQueueKeyName = redisQueueKeyName;
    }
}
