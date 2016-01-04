package com.logmonitor.filemonitor.config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Conf {
	private List<ConfItem> itemList;
	private int flushInterval = 2;//seconds
	private static DELIMITER delimiter = DELIMITER.N;//\n \r\n
	private int mainBufferSize = 10;//lines
	private String recoverPath = null;//日志文件读取状态保存地址
	private boolean enableRecover = false;
	
	public enum DELIMITER {
		N((byte)1) , NR((byte)2);
		private byte len = 0;
		DELIMITER(byte len) {this.len = len;}
		public byte len() {return len;};
	};
	
	public static class ConfItem {
		private String logPath = "";
		private String logNameFilter = ".*\\.log";
		private boolean useLogNameFilter = true;
		private long scanInterval = TimeUnit.SECONDS.toMillis(1);
		
		public ConfItem(String logPath) {
			this.logPath = logPath;
		}
		
		public ConfItem(String logPath,String logNameFilter) {
			this.logPath = logPath;
			this.logNameFilter = logNameFilter;
		}
		
		public void setLogPath(String logPath) {
			this.logPath = logPath;
		}
		
		public String getLogPath() {
			return this.logPath;
		}
		
		public void setLogNameFilter(String logNameFilter) {
			this.logNameFilter = logNameFilter;
		}
		
		public String getLogNameFilter() {
			return this.logNameFilter;
		}
		
		public void enableLogNameFilter() {
			this.useLogNameFilter = true;
		}
		
		public void disableLogNameFilter() {
			this.useLogNameFilter = false;
		}
		
		@Override
		public boolean equals(Object item) {
			return EqualsBuilder.reflectionEquals(this, item, "logPath", "logNameFilter");
		}
		
		@Override
		public int hashCode() {
			return HashCodeBuilder.reflectionHashCode(this, "logPath", "logNameFilter");
		}

		public boolean isUseLogNameFilter() {
			return useLogNameFilter;
		}

		public long getScanInterval() {
			return scanInterval;
		}
		
		public void setScanInterval(long seconds) {
			this.scanInterval = TimeUnit.SECONDS.toMillis(seconds);
		}

		public void setScanInterval(long seconds,long millis) {
			this.scanInterval = TimeUnit.SECONDS.toMillis(seconds) + millis;
		}
	}
	
	public Conf() {
		this.itemList = new ArrayList<ConfItem>();
	}
	
	public boolean validate() {
		return true;
	}
	
	public void addConfItem(ConfItem item) {
		this.itemList.add(item);
	}
	
	public void removeConfItem(ConfItem item) {
		this.itemList.remove(item);
	}
	
	public List<ConfItem> getItems() {
		return this.itemList;
	}

	public int getFlushInterval() {
		return flushInterval;
	}

	public void setFlushInterval(int flushInterval) {
		if (flushInterval <= 0) {
			flushInterval = 2;
		}
		this.flushInterval = flushInterval;
	}

	public static DELIMITER getDelimiter() {
		return delimiter;
	}

	public static void setDelimiter(DELIMITER delimiter) {
		Conf.delimiter = delimiter;
	}

	public int getMainBufferSize() {
		return mainBufferSize;
	}

	public void setMainBufferSize(int mainBufferSize) {
		if (mainBufferSize <= 0) {
			mainBufferSize = 10;
		}
		this.mainBufferSize = mainBufferSize;
	}

	public String getRecoverPath() {
		return recoverPath;
	}

	public void setRecoverPath(String recoverPath) {
		this.recoverPath = recoverPath.trim();
		if (!this.recoverPath.endsWith("/")) {
			this.recoverPath += "/";
		}
	}

	public boolean isEnableRecover() {
		return enableRecover;
	}

	public void setEnableRecover(boolean enableRecover) {
		this.enableRecover = enableRecover;
	}

}