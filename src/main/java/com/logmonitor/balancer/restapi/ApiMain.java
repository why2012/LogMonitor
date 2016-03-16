package com.logmonitor.balancer.restapi;

import com.codahale.metrics.annotation.Timed;
import com.logmonitor.balancer.node.ConsumeNode;
import com.logmonitor.balancer.node.SourceNode;
import com.logmonitor.balancer.zkbalancer.ZkBalancerForConsume;
import com.logmonitor.balancer.zkbalancer.ZkBalancerForSource;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by wanghaiyang on 16/3/16.
 */
@Path("/zk")
@Produces(MediaType.APPLICATION_JSON)
public class ApiMain {
    private String[] zookeeperHosts;
    private ZkBalancerForSource zkBalancerForSource = null;
    private ZkBalancerForConsume zkBalancerForConsume = null;
    private HeartBearScanner heartBearScanner = null;

    public ApiMain(ZkBalancerForSource zkBalancerForSource, ZkBalancerForConsume zkBalancerForConsume, String[] zookeeperHosts, HeartBearScanner heartBearScanner) {
        this.zkBalancerForSource = zkBalancerForSource;
        this.zkBalancerForConsume = zkBalancerForConsume;
        this.zookeeperHosts = zookeeperHosts;
        this.heartBearScanner = heartBearScanner;
    }

    /**
     *
     * @param host
     * @param port
     * @return Status:msg -> sourceNode path
     */
    @Path("/source-online/{host}/{port}")
    @GET
    @Timed
    public Status sourceOnLine(@PathParam("host") String host, @PathParam("port") int port) {
        Status status = new Status(Status.RESULT.OK);
        if (host == null || port == 0) {
            status.setStatus(Status.RESULT.FAILED);
            status.setMsg("host or port can not be null.");
            return status;
        }
        SourceNode sourceNode = new SourceNode(host, port);
        boolean result = zkBalancerForSource.registerSource(sourceNode);
        if (result) {
            status.setMsg(sourceNode.getNodeName());
            heartBearScanner.add(sourceNode.getNodeName());
        } else {
            status.setStatus(Status.RESULT.FAILED);
            status.setMsg("An error occured.");
        }
        return status;
    }

    @Path("/source-offline/{name}")
    @GET
    @Timed
    public Status sourceOffLine(@PathParam("name") String sourcePath) {
        Status status = new Status(Status.RESULT.OK);
        if (sourcePath == null) {
            status.setStatus(Status.RESULT.FAILED);
            status.setMsg("path can not be null.");
            return status;
        }
        SourceNode sourceNode = new SourceNode();
        sourceNode.setNodePath(zkBalancerForSource.getZkSourceParentPath() + "/" + sourcePath);
        boolean result = zkBalancerForSource.removeSource(sourceNode);
        return delete(result, status, sourcePath);
    }

    /**
     * @return Status:msg -> consumeNode path|zookeeper host
     */
    @Path("/consume-online")
    @GET
    @Timed
    public Status consumeOnLine() {
        Status status = new Status(Status.RESULT.OK);
        ConsumeNode consumeNode = new ConsumeNode();
        boolean result = zkBalancerForConsume.registerConsume(consumeNode);
        if (result) {
            status.setMsg(consumeNode.getNodeName() + "|" + zookeeperHosts[(int)Math.random() * zookeeperHosts.length]);
            heartBearScanner.add(consumeNode.getNodeName());
        } else {
            status.setStatus(Status.RESULT.FAILED);
            status.setMsg("An error occured.");
        }
        return status;
    }

    @Path("/consume-offline/{name}")
    @GET
    @Timed
    public Status consumeOffLine(@PathParam("name") String consumePath) {
        Status status = new Status(Status.RESULT.OK);
        if (consumePath == null) {
            status.setStatus(Status.RESULT.FAILED);
            status.setMsg("path can not be null.");
            return status;
        }
        ConsumeNode consumeNode = new ConsumeNode();
        consumeNode.setNodePath(zkBalancerForConsume.getZkConsumeParentPath() + "/" + consumePath);
        boolean result = zkBalancerForConsume.removeConsume(consumeNode);

        return delete(result, status, consumePath);
    }

    private Status delete(boolean result,Status status, String path) {
        if (!result) {
            status.setStatus(Status.RESULT.FAILED);
            status.setMsg("An error occured.");
        } else {
            status.setMsg("Delete [" + path + "] successfully.");
            heartBearScanner.remove(path);
        }
        return status;
    }

    @Path("/heart-beat/{name}")
    @GET
    @Timed
    public Status heartBeat(@PathParam("name") String nodePath) {
        Status status = new Status(Status.RESULT.OK);
        boolean result = heartBearScanner.touch(nodePath);
        if (!result) {
            status.setStatus(Status.RESULT.FAILED);
        }
        return status;
    }

    @Path("/test")
    @GET
    @Timed
    public Response testApi(@QueryParam("s") String param) {
        Status status = new Status((param != null && param.equalsIgnoreCase("ok")) ? Status.RESULT.OK : Status.RESULT.FAILED);
        return Response.ok(status).build();
    }
}
