package com.zlyq.client.android.analytics.exception;

public class ZlyqEventException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public ZlyqEventException() {
		super();
	}
	
	public ZlyqEventException(String msg) {
		super(msg);
	}
	
	public ZlyqEventException(Throwable ex) {
		super(ex);
	}
	
	public ZlyqEventException(String msg, Throwable ex) {
		super(msg,ex);
	}

}
