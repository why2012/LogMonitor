package com.logmonitor.authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Authentication {
	
	private static Authentication auth = null;
	private static Lock lock = new ReentrantLock();
	private List<Project> projects = new ArrayList<Project>();
	
	private Authentication() {

	}
	
	public void addProject(Project project) {
		projects.add(project);
	}
	
	public void removeProject(Project project) {
		projects.remove(project);
	}
	
	public List<Project> getProjects() {
		return this.projects;
	}
	
	public static Authentication getInstance() {
		if (auth == null) {
			lock.lock();
			try {
				if (auth == null) {
					auth = new Authentication();
				}
			} finally {
				lock.unlock();
			}
		}
		return auth;
	}
}
