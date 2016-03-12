package com.logmonitor.balancer.node;

/**
 * Created by wanghaiyang on 16/3/12.
 */
public class SourceNode extends ZkNode{
    private String _sourceIp;
    private int _sourcePort;
    public enum DIRECTION {LPOP, RPOP};
    private DIRECTION _direction = DIRECTION.RPOP;
    private boolean _available = true;
    private boolean _allocated = false;

    public SourceNode() {
        this("0.0.0.0", -1, DIRECTION.RPOP);
    }

    public SourceNode(String sourceIp, int sourcePort) {
        this(sourceIp, sourcePort, DIRECTION.RPOP);
    }

    public SourceNode(String sourceIp, int sourcePort, DIRECTION direction) {
        this._sourceIp = sourceIp;
        this._sourcePort = sourcePort;
        this._direction = direction;
    }

    public DIRECTION getDirection() {
        return _direction;
    }

    public void setDirection(DIRECTION direction) {
        this._direction = direction;
    }

    public String getSourceIp() {
        return _sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this._sourceIp = sourceIp;
    }

    public int getSourcePort() {
        return _sourcePort;
    }

    public void setSourcePort(int sourcePort) {
        this._sourcePort = sourcePort;
    }

    public boolean isAvailable() {
        return _available;
    }

    public void setAvailable(boolean available) {
        this._available = available;
    }

    public boolean isAllocated() {
        return _allocated;
    }

    public void setAllocated(boolean allocated) {
        this._allocated = allocated;
    }

    public String getNodeProperty() {
        return _sourceIp + "," + _sourcePort + "," + _direction + "," + _available + "," + _allocated;
    }

    public String toString() {
        return getNodeProperty();
    }
}
