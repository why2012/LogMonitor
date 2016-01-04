package com.logmonitor.filemonitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.logmonitor.filemonitor.buffer.Buffer;
import com.logmonitor.filemonitor.buffer.BufferEvent;
import com.logmonitor.filemonitor.buffer.BufferEventHandler;
import com.logmonitor.filemonitor.buffer.ThreadSafeBuffer;
import com.logmonitor.filemonitor.config.Conf;
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
	
	public FileMonitor(Conf conf) throws Exception{
		this.conf = conf;
		this.buffer = new ThreadSafeBuffer(conf.getMainBufferSize());
		this.bufferEventHandler = new BufferHandlerImplBufferEventHandler();
		buffer.setEventHandler(bufferEventHandler);
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
	}
	
	public void start() throws Exception {
		final int size = conf.getItems().size();
		this.timeFlusher.start();
		for (int i = 0 ; i < size ; i++) {
			fileScanners[i].startScan();
		}
	}
	
	public void stop() throws Exception {
		final int size = conf.getItems().size();
		this.timeFlusher.stop();
		for (int i = 0 ; i < size ; i++) {
			fileScanners[i].stopScan();
		}
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

	public void process(Buffer buffer, BufferEvent event) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss"); 
		String currentTime = dateFormat.format(new Date());
		System.out.println("(" + currentTime + ")BufferEvent: " + event + ", Size: " + buffer.size());
		String logs = buffer.getAll();
		System.out.print(logs);
	}
	
}