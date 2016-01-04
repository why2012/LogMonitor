package com.logmonitor.filemonitor.scanner;

import java.util.concurrent.TimeUnit;

import com.logmonitor.filemonitor.buffer.Buffer;
import com.logmonitor.filemonitor.buffer.BufferEvent;

public class TimeFlusher implements Runnable {
	private int interval = 2;//second
	private Buffer buffer = null;
	private Thread flushThread = null;
	private boolean running = false;
	
	public TimeFlusher(Buffer buffer, int interval) {
		this.buffer = buffer;
		this.interval = interval;
	}

	public void run() {
		try {
			while(running) {
				TimeUnit.SECONDS.sleep(interval);
				if (this.buffer.size() > 0) {
					this.buffer.notifyEvent(BufferEvent.Flush);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public boolean start() {
		if (running) {
			return false;
		}
		this.flushThread = new Thread(this,"TimerFlusher");
		running = true;
		this.flushThread.start();
		return true;
	}
	
	public boolean stop() {
		if (!running) {
			return false;
		}
		running = false;
		return true;
	}
}
