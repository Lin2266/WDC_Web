package com.test.commons.exception;

public class FtpHandleException extends RuntimeException {
	public FtpHandleException() {
		super();
	}
	
	public FtpHandleException(String message) {
		super(message);
	}
	
	public FtpHandleException(Throwable t) {
		super(t);
	}
	
	public FtpHandleException(String message, Throwable t) {
		super(message, t);
	}
}
