package com.test.commons.exception;

public class UnsupportedTypeException extends RuntimeException {
	public UnsupportedTypeException() {
		super();
	}
	
	public UnsupportedTypeException(String msg) {
		super(msg);
	}
	
	public UnsupportedTypeException(Throwable t) {
		super(t);
	}
	
	public UnsupportedTypeException(String msg, Throwable t) {
		super(msg, t);
	}
}
