package com.logmonitor.filemonitor.scanner;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import com.logmonitor.filemonitor.buffer.ThreadSafeBuffer;

public class FileListener extends FileAlterationListenerAdaptor {
	private ThreadSafeBuffer buffer = null;
	private Map<File,FileNode> fileScanMap = null;
	private boolean running = false;
	private enum STATE {
		INIT("INIT"), CHANGE("CHANGE"), CREATE("CREATE"), DELETE("DELETE");
		
		private String type = "NONE";
		STATE(String type) {
			this.type = type;
		}
		
		public String toString() {
			return this.type;
		}
	};
	
	public FileListener(ThreadSafeBuffer buffer) {
		this.buffer = buffer;
		this.fileScanMap = new HashMap<File,FileNode>();
	}
	
	@Override
	public void onFileCreate(File file) {
		super.onFileCreate(file);
		this.fileScanMap.put(file, new FileNode());
		this.processFileContent(file, STATE.CREATE);
	}
	
	@Override
	public void onFileChange(File file) {
		super.onFileChange(file);
		
		this.processFileContent(file, STATE.CHANGE);
	}
	
	@Override
	public void onFileDelete(File file) {
		super.onFileDelete(file);
		FileNode fileNode = this.fileScanMap.remove(file);
		try {
			fileNode.reader.close();
			fileNode.reader = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void processFileContent(File file, STATE state) {
		FileNode fileNode = this.fileScanMap.get(file);
		if (fileNode == null) {
			return;
		}
//		if (state == STATE.CHANGE) {
//			try {
//				fileNode.reader.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			fileNode.reader = null;
//		}
		if (fileNode.reader == null) {
			try {
				fileNode.reader = new LineNumberReader(new FileReader(file));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		try {
			//recover file state
			if (fileNode.reader.getLineNumber() == 0 && fileNode.curIndex != 0) {
				for (int i = 0 ; i < fileNode.curIndex ; i++) {
					fileNode.reader.readLine();
				}
			}
			String newLine = null;
			do {
				fileNode.reader.setLineNumber(fileNode.curIndex);
				newLine = fileNode.reader.readLine();
				//DEBUG
				//System.out.println(fileNode.reader.getLineNumber()+" , "+newLine);
				if (newLine != null) {
					fileNode.curIndex++;
					buffer.insert(newLine);
				}
			} while(newLine != null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void initialScanningForAllFiles(File directory, FileFilter fileFilter) {
		File allFiles[] = directory.listFiles();
		for (int i = 0; i < allFiles.length ; i++ ) {
			if (allFiles[i].isDirectory()) {
				this.initialScanningForAllFiles(allFiles[i], fileFilter);
			} else if(allFiles[i].isFile() && !this.fileScanMap.containsKey(allFiles[i])) {
				if (fileFilter == null || ( fileFilter != null && fileFilter.accept(allFiles[i]))) {
					this.fileScanMap.put(allFiles[i], new FileNode());
				} else {
					continue;
				}
				//DEBUG
				//System.out.println(allFiles[i].getPath());
			}
		}
		
		Iterator<File> fileIterator = this.fileScanMap.keySet().iterator();
		while (fileIterator.hasNext()) {
			File file = fileIterator.next();
			this.processFileContent(file, STATE.INIT);
		}
	}
	
	public void executeStop(FileAlterationObserver observer) {
		System.out.println("Stop.");
		super.onStop(observer);
		Iterator<File> fileIterator = this.fileScanMap.keySet().iterator();
		while (fileIterator.hasNext()) {
			File file = fileIterator.next();
			try {
				this.fileScanMap.get(file).reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override  
    public void onStart(FileAlterationObserver observer) { 
		super.onStart(observer);
		if (!running) {
			running = true;
			this.initialScanningForAllFiles(observer.getDirectory(), observer.getFileFilter());
		}
	}
	
	@Override  
    public void onStop(FileAlterationObserver observer) {
		super.onStop(observer);
	}
}
