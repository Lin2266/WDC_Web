package com.test.commons.exception;

public class CsvHandleException extends RuntimeException {
	public CsvHandleException() {
		super();
	}
	
	public CsvHandleException(String message) {
		super(message);
	}
	
	public CsvHandleException(Throwable t) {
		super(t);
	}
	
	public CsvHandleException(String message, Throwable t) {
		super(message, t);
	}
}
