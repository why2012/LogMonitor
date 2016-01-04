package com.logmonitor.filemonitor.buffer;

public enum BufferEvent {
	Full("FULL"), Flush("FLUSH", 1);
	
	private String type = "NONE";
	private int notifyType = 0;//0:只允许系统调用，1:允许外部调用
	
	BufferEvent(String type, int notifyType) {
		this.type = type;
		this.notifyType = notifyType;
	}
	
	BufferEvent(String type) {
		this.type = type;
		this.notifyType = 0;
	}
	
	public boolean externalNotify() {
		return this.notifyType == 1;
	}
	
	@Override
	public String toString() {
		return type;
	}
}
