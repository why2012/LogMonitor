package com.logmonitor.balancer.restapi;

import com.codahale.metrics.health.HealthCheck;

/**
 * Created by wanghaiyang on 16/3/16.
 */
public class DefaultHealthCheck extends HealthCheck {
    @Override
    protected Result check() throws Exception {
        return Result.healthy();
    }
}
