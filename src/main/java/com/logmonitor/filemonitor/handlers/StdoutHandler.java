package com.logmonitor.filemonitor.handlers;

import java.util.concurrent.ConcurrentLinkedQueue;

public class StdoutHandler implements Handler {
	private boolean running = false;
	private Thread thread = null;
	private ConcurrentLinkedQueue<String> dataList = new ConcurrentLinkedQueue<String>();
	private MsgModifier msgModifier = null;

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
				System.out.println(tmpData);
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
	}

	public void setModifier(MsgModifier msgModifier) {
		this.msgModifier = msgModifier;
	}

	public MsgModifier getModifier() {
		return this.msgModifier;
	}

}
