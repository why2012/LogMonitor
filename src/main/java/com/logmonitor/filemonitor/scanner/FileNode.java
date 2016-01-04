package com.logmonitor.filemonitor.scanner;

import java.io.File;
import java.io.LineNumberReader;
import java.io.Serializable;

public class FileNode implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2652684547318731203L;
	public File file = null;
	public transient LineNumberReader reader = null;
	public int nextIndex = 0;//应读行号
	public long curByte = 0;//已读字节
	
	public String toString() {
		String str = "FileNode(" + file + "," + nextIndex + "," + curByte + ")";
		return str;
	}
}
