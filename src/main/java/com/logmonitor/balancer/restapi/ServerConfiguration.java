package com.logmonitor.balancer.restapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * Created by wanghaiyang on 16/3/16.
 */
public class ServerConfiguration extends Configuration {
    @NotEmpty
    private String[] zookeeperHosts = null;
    @Min(1)
    @Max(Integer.MAX_VALUE)
    private int heartBeatScanInterval = 100;//milli seconds
    @Min(1)
    @Max(Integer.MAX_VALUE)
    private int heartExpiredInterval = 1000;//milli seconds

    @JsonProperty
    public void setZookeeperHosts(String[] zookeeperHosts) {
        this.zookeeperHosts = zookeeperHosts;
    }

    @JsonProperty
    public String[] getZookeeperHosts() {
        return this.zookeeperHosts;
    }

    @JsonProperty
    public int getHeartBeatScanInterval() {
        return heartBeatScanInterval;
    }

    @JsonProperty
    public void setHeartBeatScanInterval(int heartBeatScanInterval) {
        this.heartBeatScanInterval = heartBeatScanInterval;
    }

    @JsonProperty
    public int getHeartExpiredInterval() {
        return heartExpiredInterval;
    }

    @JsonProperty
    public void setHeartExpiredInterval(int heartExpiredInterval) {
        this.heartExpiredInterval = heartExpiredInterval;
    }
}
