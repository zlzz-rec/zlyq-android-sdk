package com.zlyq.client.android.analytics.db.exception;

import com.zlyq.client.android.analytics.exception.ZlyqEventException;

public class ZlyqDbExceptionZlyq extends ZlyqEventException {
	private static final long serialVersionUID = 1L;
	
	public ZlyqDbExceptionZlyq() {}
	
	
	public ZlyqDbExceptionZlyq(String msg) {
		super(msg);
	}
	
	public ZlyqDbExceptionZlyq(Throwable ex) {
		super(ex);
	}
	
	public ZlyqDbExceptionZlyq(String msg, Throwable ex) {
		super(msg,ex);
	}
	
}
