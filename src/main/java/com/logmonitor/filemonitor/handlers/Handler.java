package com.logmonitor.filemonitor.handlers;

/**
 * 
 * @author WHY
 * Buffer数据处理
 */
public interface Handler extends Runnable {
	//非阻塞
	public void notify(String data);
	public void start();
	public void stop();
}
