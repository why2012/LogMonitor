package com.logmonitor.filemonitor.scanner;

import java.io.File;
import java.io.FileFilter;

public class LogFileFilter implements FileFilter {
	
	private String filterRule = "";
	
	public LogFileFilter(String filterRule) {
		this.filterRule = filterRule;
	}

	public boolean accept(File pathname) {
		if (pathname.getName().matches(this.filterRule)) {
			return true;
		} else {
			return false;
		}
	}

}
