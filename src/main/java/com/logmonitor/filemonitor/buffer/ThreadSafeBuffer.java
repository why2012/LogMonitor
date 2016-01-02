package com.logmonitor.filemonitor.buffer;

import java.util.concurrent.Semaphore;


public class ThreadSafeBuffer implements Buffer {
	private StringBuffer buffer = new StringBuffer();
	private int maxSize = 1000;
	private int lines = 0;
	private BufferEventHandler bufferEventHandler = null;
	private boolean notifyOnFull = true;
	private boolean notifyOnce = true;
	private Semaphore semFull = null;
	private Semaphore semEmpty = null;
	
	public ThreadSafeBuffer() {
		this.initSemaphore();
	}
	
	public ThreadSafeBuffer(int maxSize) {
		this.maxSize = maxSize;
		this.initSemaphore();
	}
	
	private void initSemaphore() {
		semFull = new Semaphore(0);
		semEmpty = new Semaphore(this.maxSize);
	}
	
	public void setMaxSize(int size) {
		this.maxSize = size;
	}
	
	public int getMaxSize() {
		return this.maxSize;
	}
	
	public void setEventHandler(BufferEventHandler handler) {
		this.bufferEventHandler = handler;
	}
	
	public void removeEventHandler() {
		this.bufferEventHandler = null;
	}
	
	public void setNotifyOnFull(boolean notifyOnFull) {
		this.notifyOnFull = notifyOnFull;
	}
	
	public boolean getNotifyOnFull() {
		return this.notifyOnFull;
	}

	public void insert(String line) {
		if (isFull()) {
			if (this.notifyOnFull && this.bufferEventHandler != null && notifyOnce) {
				notifyOnce = false;
				this.bufferEventHandler.process(this, BufferEvent.Full);
				notifyOnce = true;
			}
		}
		
		this.semEmpty.acquireUninterruptibly();;
		buffer.append(line + "\n");
		lines++;
		this.semFull.release(1);
	}

	public String getAll() {
		int len = lines;
		this.semFull.acquireUninterruptibly(len);
		String all = buffer.toString();
		buffer.setLength(0);
		lines = 0;
		this.semEmpty.release(len);
		return all;
	}

	public void clearAll() {
		buffer.setLength(0);
	}

	public boolean isFull() {
		return lines == maxSize;
	}
	
	public boolean isEmpty() {
		return lines == 0;
	}
	
	public int size() {
		return lines;
	}
	
	public String toString() {
		return buffer.toString();
	}
}
