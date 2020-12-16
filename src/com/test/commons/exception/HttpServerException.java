package com.test.commons.exception;

public class HttpServerException extends RuntimeException {
	private int statusCode;
	
	public HttpServerException(int statusCode, String message) {
		super(message);
	}
	
	public HttpServerException(int statusCode, String message, Throwable t) {
		super(message, t);
	}
	
	public int getStatusCode() {
		return this.statusCode;
	}
}
