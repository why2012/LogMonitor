package com.logmonitor.filemonitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.logmonitor.filemonitor.buffer.Buffer;
import com.logmonitor.filemonitor.buffer.BufferEvent;
import com.logmonitor.filemonitor.buffer.BufferEventHandler;
import com.logmonitor.filemonitor.buffer.ThreadSafeBuffer;
import com.logmonitor.filemonitor.config.Conf;
import com.logmonitor.filemonitor.handlers.HandlerGroup;
import com.logmonitor.filemonitor.scanner.FileListener;
import com.logmonitor.filemonitor.scanner.FileScanner;
import com.logmonitor.filemonitor.scanner.TimeFlusher;

public class FileMonitor {
	private Conf conf = null;
	private Buffer buffer = null;
	private BufferEventHandler bufferEventHandler = null;
	private TimeFlusher timeFlusher = null;
	private FileListener[] fileListeners = null;
	private FileScanner[] fileScanners = null;
	private HandlerGroup handlerGroup = null;
	private DataSaveHandler dataSaveHandler = null;
	
	public FileMonitor(Conf conf) throws Exception{
		this.conf = conf;
		this.buffer = new ThreadSafeBuffer(conf.getMainBufferSize());
		this.bufferEventHandler = new BufferHandlerImplBufferEventHandler();
		buffer.setEventHandler(bufferEventHandler);
		this.handlerGroup = new HandlerGroup(conf,buffer);
		((BufferHandlerImplBufferEventHandler)this.bufferEventHandler).setHandlerGroup(handlerGroup);
		this.timeFlusher = new TimeFlusher(buffer, conf.getFlushInterval());
		final int size = conf.getItems().size();
		fileListeners = new FileListener[size];
		fileScanners = new FileScanner[size];
		for (int i = 0 ; i < size ; i++) {
			FileListener flTmp = null;
			//从序列化文件恢复文件监控状态
			if (conf.isEnableRecover()) {
				File logFile = new File(conf.getItems().get(i).getLogPath());
				if (logFile.exists()) {
					flTmp = this.getFileListenerFromData(logFile.getName());
				}
			}
			if (flTmp == null) {
				fileListeners[i] = new FileListener(buffer);
			} else {
				flTmp.setBuffer(buffer);
				fileListeners[i] = flTmp;
			}
			fileScanners[i] = new FileScanner(conf.getItems().get(i),fileListeners[i]);
		}
		this.dataSaveHandler = new DataSaveHandler(this, conf.getDataSaveInterval());
	}
	
	public void start() throws Exception {
		final int size = conf.getItems().size();
		this.timeFlusher.start();
		for (int i = 0 ; i < size ; i++) {
			fileScanners[i].startScan();
		}
		this.handlerGroup.startHandlers();
		this.dataSaveHandler.start();
	}
	
	public void stop() throws Exception {
		final int size = conf.getItems().size();
		this.timeFlusher.stop();
		this.dataSaveHandler.stop();
		for (int i = 0 ; i < size ; i++) {
			fileScanners[i].stopScan();
		}
		this.handlerGroup.stopHandlers();
	}
	
	private FileListener getFileListenerFromData(String seriaFileName) throws Exception {
		ObjectInputStream inputStream = null;
		FileListener fileListener = null;
		String seriaPath = conf.getRecoverPath() + seriaFileName + ".obj";
		//DEBUG
		//System.out.println(seriaPath);
		if (seriaPath == null || seriaPath.equals("") || !(new File(seriaPath).exists())) {
			return null;
		}
		try {
			inputStream = new ObjectInputStream(new FileInputStream(seriaPath));
			fileListener = (FileListener)inputStream.readObject();
		} catch (Exception e) {
			throw e;
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
		return fileListener;
	}
	
	private void saveFileListener() throws Exception {
		ObjectOutputStream output = null;
		int index = 0;
		for (FileListener listener : this.fileListeners) {
			Conf.ConfItem confItem = this.fileScanners[index].getConfItem();
			File file = new File(confItem.getLogPath());
			String seriaPath = conf.getRecoverPath() + file.getName() + ".obj";
			output = new ObjectOutputStream(new FileOutputStream(seriaPath));
			output.writeObject(listener);
			output.close();
			index++;
		}
	}
	
	public void saveStateData() throws Exception {
		this.saveFileListener();
	}

}

class BufferHandlerImplBufferEventHandler implements BufferEventHandler {
	private HandlerGroup handlerGroup = null;
	
	public BufferHandlerImplBufferEventHandler(HandlerGroup handlerGroup) {
		this.handlerGroup = handlerGroup;
	}
	
	public BufferHandlerImplBufferEventHandler() {
		
	}
	
	public HandlerGroup getHandlerGroup() {
		return this.handlerGroup;
	}
	
	public void setHandlerGroup(HandlerGroup handlerGroup) {
		this.handlerGroup = handlerGroup;
	}
	
	/**
	 * 出现FULL事件通知Handler刷新缓存
	 */
	public void process(Buffer buffer, BufferEvent event) {
		//DEBUG
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss"); 
		String currentTime = dateFormat.format(new Date());
		System.out.println("(" + currentTime + ")BufferEvent: " + event + ", Size: " + buffer.size());
		
		this.handlerGroup.processData();
	}
	
}

class DataSaveHandler implements Runnable {
	private boolean running = false;
	private FileMonitor fileMonitor = null;
	private Thread thread = null;
	private int interval = 3;
	
	public DataSaveHandler(FileMonitor fileMonitor,int interval) {
		this.fileMonitor = fileMonitor;
		this.interval = interval;
	}
		
	public void run() {
		while(running) {
			try {
				TimeUnit.SECONDS.sleep(interval);
				fileMonitor.saveStateData();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void start() {
		if (running) {
			return;
		}
		running = true;
		thread = new Thread(this);
		thread.start();
	}
	
	public void stop() {
		if (!running) {
			return;
		}
		running = false;
	}
}
