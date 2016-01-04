package com.logmonitor.filemonitor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.logmonitor.filemonitor.buffer.Buffer;
import com.logmonitor.filemonitor.buffer.BufferEvent;
import com.logmonitor.filemonitor.buffer.BufferEventHandler;
import com.logmonitor.filemonitor.buffer.ThreadSafeBuffer;
import com.logmonitor.filemonitor.config.Conf;
import com.logmonitor.filemonitor.scanner.FileListener;
import com.logmonitor.filemonitor.scanner.FileScanner;
import com.logmonitor.filemonitor.scanner.TimeFlusher;

public class ScannerTest {
	
	 public static void main(String[] args) throws Exception{
		Conf conf = new Conf(); 
		Conf.ConfItem confItem = new Conf.ConfItem("/Users/wanghaiyang/Desktop/logs");
		confItem.enableLogNameFilter();
		conf.addConfItem(confItem);
		conf.setFlushInterval(2);
		ThreadSafeBuffer buffer = new ThreadSafeBuffer(10);
		buffer.setEventHandler(new BufferEventHandler() {
			public void process(Buffer buffer, BufferEvent event) {
				System.out.println("(" + currentTime() + ")BufferEvent: " + event + ", Size: " + buffer.size());
				String logs = buffer.getAll();
				System.out.print(logs);
			}
		});
		TimeFlusher timeFlusher = new TimeFlusher(buffer, conf.getFlushInterval());
		FileListener fileListener = new FileListener(buffer);
		FileScanner fileScanner = new FileScanner(confItem,fileListener);
		fileScanner.startScan();
		timeFlusher.start();
		//testWrite();
		System.out.println("Finished");
	}
	 
	private static String currentTime() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss"); 
		return dateFormat.format(new Date());
	}
	 
	static class Writer implements Runnable {
		private static int index = 1;
		
		public void run() {
			String filePath = "/Users/wanghaiyang/Desktop/logs/web" + index++ + ".wf.log";
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(new File(filePath)));
				while (true) {
					writer.write("2016-01-02 19:02:50  [ main:64 ] - [ INFO ]  {rset-100002} ResultSet\n");
					//System.out.println("Write One.");
					writer.flush();
					try {
						TimeUnit.SECONDS.sleep(2);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public static void testWrite() {
		Runnable writer = new ScannerTest.Writer();
		ExecutorService executor = Executors.newCachedThreadPool();
		for (int i = 0 ; i < 10 ; i++) {
			executor.execute(writer);
		}
		executor.shutdown();
	}
	
	@Test
	public void testNothing() {
		
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
