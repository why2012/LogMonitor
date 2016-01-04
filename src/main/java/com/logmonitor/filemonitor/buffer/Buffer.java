package com.logmonitor.filemonitor.buffer;

public interface Buffer {
	public void insert(String line);
	public String getAll();
	public void clearAll();
	public boolean isFull();
	public boolean isEmpty();
	public int size();
	public void setEventHandler(BufferEventHandler handler);
	public void notifyEvent(BufferEvent event);
}
