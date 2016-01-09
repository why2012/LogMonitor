package com.logmonitor.filemonitor.scanner;

import java.io.Externalizable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import com.logmonitor.filemonitor.buffer.Buffer;

public class FileListener extends FileAlterationListenerAdaptor implements Externalizable {
	private static final long serialVersionUID = 6316167668511901932L;
	private Buffer buffer = null;
	private Map<File,FileNode> fileScanMap = null;
	private boolean running = false;
	private int sepLen = System.getProperty("line.separator").length();
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
	
	public FileListener(Buffer buffer) {
		this.buffer = buffer;
		this.fileScanMap = new HashMap<File,FileNode>();
	}
	
	public FileListener() {
		
	}
	
	public void setBuffer(Buffer buffer) {
		this.buffer = buffer;
	}
	
	public Buffer getBuffer() {
		return this.buffer;
	}
	
	@Override
	public void onFileCreate(File file) {
		super.onFileCreate(file);
		FileNode fileNode = new FileNode();
		fileNode.file = file;
		this.fileScanMap.put(file, fileNode);
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
		//DEBUG
		//System.out.println(STATE.DELETE + ": " + file.getPath());
		FileNode fileNode = this.fileScanMap.remove(file);
		try {
			fileNode.reader.close();
			fileNode.reader = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void processFileContent(File file, STATE state) {
		if (buffer == null) {
			return;
		}
		FileNode fileNode = this.fileScanMap.get(file);
		if (fileNode == null) {
			return;
		}
		//DEBUG
		//System.out.println(state + ": " + file.getPath());
		if (fileNode.reader == null) {
			try {
				fileNode.reader = new LineNumberReader(new FileReader(file));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		try {
			//recover file state
			if (fileNode.reader.getLineNumber() == 0 && fileNode.nextIndex != 0) {
				fileNode.reader.skip(fileNode.curByte);
			}
			String newLine = null;
			do {
				fileNode.reader.setLineNumber(fileNode.nextIndex);
				newLine = fileNode.reader.readLine();
				if (newLine != null) {
					//DEBUG
					//System.out.println("File:" + file + " , " + "Line: " + (fileNode.reader.getLineNumber() - 1) + " , Byte: " + fileNode.curByte + " , "+newLine);
					fileNode.nextIndex++;
					fileNode.curByte += newLine.length() + this.sepLen;
					buffer.insert(newLine);
				}
			} while(newLine != null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void initialScanningForAllFiles(File directory, FileFilter fileFilter) {
		File allFiles[] = directory.listFiles();
		if (allFiles == null) {
			return;
		}
		for (int i = 0; i < allFiles.length ; i++ ) {
			if (allFiles[i].isDirectory()) {
				//commons-io 不监控子文件夹
				//this.initialScanningForAllFiles(allFiles[i], fileFilter);
			} else if(allFiles[i].isFile() && !this.fileScanMap.containsKey(allFiles[i])) {
				FileNode fileNode = new FileNode();
				fileNode.file = allFiles[i];
				this.fileScanMap.put(allFiles[i], fileNode);
			}
		}
		
		Iterator<File> fileIterator = this.fileScanMap.keySet().iterator();
		while (fileIterator.hasNext()) {
			File file = fileIterator.next();
			if (!file.exists()) {
				fileIterator.remove();
			}
			if (fileFilter != null && !fileFilter.accept(file)) {
				fileIterator.remove();
			}
			//DEBUG
			//System.out.println("INIT-PROCESS(" + Thread.currentThread().getName() + "): " + file.getPath());
			this.processFileContent(file, STATE.INIT);
		}
	}
	
	public void executeStop(FileAlterationObserver observer) {
		//DEBUG
		//System.out.println("Stop.");
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

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(fileScanMap);
		//DEBUG
//		Set<File> keySet = this.fileScanMap.keySet();
//		Iterator<File> keyIterator = keySet.iterator();
//		while (keyIterator.hasNext()) {
//			File file = keyIterator.next();
//			FileNode fileNode = this.fileScanMap.get(file);
//			System.out.println("MAP {");
//			System.out.println(file + " ; " + fileNode);
//			System.out.println("}");
//		}
	}

	@SuppressWarnings("unchecked")
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		this.fileScanMap = (Map<File, FileNode>) in.readObject();
		//DEBUG
//		Set<File> keySet = this.fileScanMap.keySet();
//		Iterator<File> keyIterator = keySet.iterator();
//		while (keyIterator.hasNext()) {
//			File file = keyIterator.next();
//			FileNode fileNode = this.fileScanMap.get(file);
//			System.out.println("MAP {");
//			System.out.println(file + " ; " + fileNode);
//			System.out.println("}");
//		}
	}
}
