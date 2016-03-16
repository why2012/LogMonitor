package com.logmonitor.balancer.restapi;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by wanghaiyang on 16/3/16.
 */
public class Status {
    public enum RESULT {OK, FAILED}
    private RESULT status = RESULT.OK;
    private String msg = "";

    public Status(RESULT status) {
        this.status = status;
    }

    public Status(RESULT status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public void setStatus(RESULT result) {
        status = result;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @JsonProperty
    public RESULT getStatus() {
        return status;
    }

    @JsonProperty
    public String getMsg() {
        return msg;
    }
}
