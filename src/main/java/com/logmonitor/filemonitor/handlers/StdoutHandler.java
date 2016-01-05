package com.logmonitor.filemonitor.handlers;

import java.util.concurrent.ConcurrentLinkedQueue;

public class StdoutHandler implements Handler {
	private boolean running = false;
	private Thread thread = null;
	private ConcurrentLinkedQueue<String> dataList = new ConcurrentLinkedQueue<String>();

	public void notify(String data) {
		this.dataList.add(data);
	}

	public void run() {
		while(running) {
			if (dataList.size() > 0) {
				System.out.println(dataList.poll());
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

}
