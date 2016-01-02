package com.logmonitor.filemonitor.config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Conf {
	private List<ConfItem> itemList;
	
	public static class ConfItem {
		private String logPath = "";
		private String logNameFilter = ".log";
		private boolean useLogNameFilter = false;
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
	
	public void addConfItem(ConfItem item) {
		this.itemList.add(item);
	}
	
	public void removeConfItem(ConfItem item) {
		this.itemList.remove(item);
	}
	
	public List<ConfItem> getItems() {
		return this.itemList;
	}
}
