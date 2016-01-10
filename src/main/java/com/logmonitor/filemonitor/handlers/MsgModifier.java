package com.logmonitor.filemonitor.handlers;

public class MsgModifier {
	private String keyCode = "";
	private String keyName = null;
	
	public String append(String msg) {
		if (keyName == null) {
			return msg + " [" + keyCode + "] ";
		} else {
			return msg + " [" + keyName + ":" + keyCode + "] ";
		}
	}

	public String getKeyCode() {
		return keyCode;
	}

	public void setKeyCode(String keyCode) {
		this.keyCode = keyCode;
	}

	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}
}
