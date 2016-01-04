package com.logmonitor.filemonitor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.junit.Test;

public class IOTest {
	
	@Test
	public void testIO() {
		String text = 
				"LINE01\r\n"
				+ "LINE02\n"
				+ "LINE03\n";
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(new ByteArrayInputStream(text.getBytes())));
		int byteNum = 0;
		String line = null;
		try {
			do {
				line = reader.readLine();
				if (line != null)
					byteNum += line.length() + 1;	
			} while(line != null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("\n".length() + " , " + text.length() + " , " + byteNum);
	}
}
