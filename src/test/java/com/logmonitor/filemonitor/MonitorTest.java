package com.logmonitor.filemonitor;

import java.util.concurrent.TimeUnit;

import com.logmonitor.filemonitor.config.Conf;

public class MonitorTest {
	
	@SuppressWarnings("unused")
	public static void main(String args[]) throws Exception {
		Conf conf = new Conf(); 
		conf.setRecoverPath("/Users/wanghaiyang/Desktop/logs/seria");
		conf.setEnableRecover(true);
		Conf.ConfItem confItem01 = new Conf.ConfItem("/Users/wanghaiyang/Desktop/logs");
		Conf.ConfItem confItem02 = new Conf.ConfItem("/Users/wanghaiyang/Desktop/logs/info");
		conf.addConfItem(confItem01);
		conf.addConfItem(confItem02);
		//conf.getConfHandler().setUseStdoutHandler(true);
		//conf.getConfHandler().setUseNetHandler(true);
		//conf.getConfHandler().setNetIp("127.0.0.1");
		//conf.getConfHandler().setNetPort(5656);
		conf.getConfHandler().setUseRedisHandler(true);
		final FileMonitor fileMonitor = new FileMonitor(conf);
		fileMonitor.start();
		if (false) {
			System.out.println("Start");
			TimeUnit.SECONDS.sleep(5);
			fileMonitor.stop();
			System.out.println("Exit");
		}
	}
}
