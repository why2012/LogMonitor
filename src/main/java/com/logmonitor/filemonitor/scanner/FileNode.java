package com.logmonitor.filemonitor.scanner;

import java.io.File;
import java.io.LineNumberReader;

public class FileNode {
	public File file = null;
	public LineNumberReader reader = null;
	public int curIndex = 0;
}
