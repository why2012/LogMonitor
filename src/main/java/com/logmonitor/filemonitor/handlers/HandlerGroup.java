package com.logmonitor.filemonitor.handlers;

import java.util.ArrayList;
import java.util.List;

import com.logmonitor.filemonitor.buffer.Buffer;
import com.logmonitor.filemonitor.config.Conf;

public class HandlerGroup {
	private Conf.ConfHandler confHandler = null;
	private Buffer buffer = null;
	private List<Handler> handlers = null;
	private String tmpData = "";
	
	public HandlerGroup(Conf conf, Buffer buffer) {
		this.buffer = buffer;
		this.confHandler = conf.getConfHandler();
		this.handlers = new ArrayList<Handler>();
		if (confHandler.isUseStdoutHandler()) {
			Handler handler = new StdoutHandler();
			this.handlers.add(handler);
		}
		
		if (confHandler.isUseNetHandler()) {
			Handler handler = new NetHandler(confHandler.getNetIp(), confHandler.getNetPort());
			this.handlers.add(handler);
		}
	}
	
	public void processData() {
		this.tmpData = buffer.getAll();
		for (Handler handler : handlers) {
			handler.notify(tmpData);
		}
	}
	
	public void startHandlers() {
		for (Handler handler : handlers) {
			handler.start();
		}
	}
	
	public void stopHandlers() {
		for (Handler handler : handlers) {
			handler.stop();
		}
	}
	
	public void addBufferDataHandler(Handler handler) {
		this.handlers.add(handler);
	}
	
	public void removeBufferDataHandler(Handler handler) {
		this.handlers.remove(handler);
	}
}
