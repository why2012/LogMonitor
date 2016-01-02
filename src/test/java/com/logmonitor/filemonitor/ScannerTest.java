package com.logmonitor.filemonitor;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import org.junit.Test;

import com.logmonitor.filemonitor.buffer.Buffer;
import com.logmonitor.filemonitor.buffer.BufferEvent;
import com.logmonitor.filemonitor.buffer.BufferEventHandler;
import com.logmonitor.filemonitor.buffer.ThreadSafeBuffer;
import com.logmonitor.filemonitor.config.Conf;
import com.logmonitor.filemonitor.scanner.FileListener;
import com.logmonitor.filemonitor.scanner.FileScanner;

public class ScannerTest {
	
	 public static void main(String[] args) {
		Conf.ConfItem confItem = new Conf.ConfItem("/Users/wanghaiyang/Desktop/logs");
		confItem.enableLogNameFilter();
		ThreadSafeBuffer buffer = new ThreadSafeBuffer(10);
		buffer.setEventHandler(new BufferEventHandler() {
			public void process(Buffer buffer, BufferEvent event) {
				String logs = buffer.getAll();
				System.out.print(logs);
			}
		});
		FileListener fileListener = new FileListener(buffer);
		FileScanner fileScanner = new FileScanner(confItem,fileListener);
		fileScanner.startScan();
		System.out.println("Finished");
	}
	
	//@Test
	public void testLineNumberReader() {
		File file = new File("/Users/wanghaiyang/Desktop/logs/web.log");
		try {
			LineNumberReader reader = new LineNumberReader(new FileReader(file));
			FileFilter fileFilter = new FileFilter() {

				public boolean accept(File pathname) {
					if (pathname.getPath().matches(".*\\.log"))
						return true;
					else
						return false;
				}
				
			};
			int lineNumber = 0;
			while(fileFilter.accept(file) && reader.readLine() != null) {
				lineNumber++;
			}
			reader.close();
			System.out.println("lineNumber: " + lineNumber);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
}
