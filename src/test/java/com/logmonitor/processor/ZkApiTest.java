package com.logmonitor.processor;

import com.logmonitor.processor.adapter.RedisAdapter;
import com.logmonitor.processor.coordinate.ZkApi;
import com.logmonitor.processor.coordinate.ZkResource;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * Created by wanghaiyang on 16/3/21.
 */
public class ZkApiTest {

    @Test
    public void testOnline() throws Exception {
        ZkResource zkResource = new ZkResource();
        zkResource.setZkNotification(new RedisAdapter());
        ZkApi zkApi = new ZkApi(zkResource, "http://127.0.0.1:8080");
        zkApi.online();
        new CountDownLatch(1).await();
    }
}
