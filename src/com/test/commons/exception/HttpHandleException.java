package com.test.commons.exception;

public class HttpHandleException extends RuntimeException {
	public HttpHandleException() {
		super();
	}
	
	public HttpHandleException(String message) {
		super(message);
	}
	
	public HttpHandleException(Throwable t) {
		super(t);
	}
	
	public HttpHandleException(String message, Throwable t) {
		super(message, t);
	}
}
