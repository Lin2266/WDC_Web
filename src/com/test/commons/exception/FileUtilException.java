package com.test.commons.exception;

public class FileUtilException extends RuntimeException {
	public FileUtilException() {
		super();
	}
	
	public FileUtilException(String msg) {
		super(msg);
	}
	
	public FileUtilException(Throwable t) {
		super(t);
	}
	
	public FileUtilException(String msg, Throwable t) {
		super(msg, t);
	}
}
