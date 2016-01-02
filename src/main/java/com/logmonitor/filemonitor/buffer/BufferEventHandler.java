package com.logmonitor.filemonitor.buffer;

public interface BufferEventHandler {
	
	public void process(Buffer buffer,BufferEvent event);
}
