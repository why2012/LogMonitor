package com.logmonitor.filemonitor.buffer;

public interface Buffer {
	public void insert(String line);
	public String getAll();
	public void clearAll();
	public boolean isFull();
}
