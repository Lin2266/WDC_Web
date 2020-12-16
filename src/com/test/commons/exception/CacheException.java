package com.test.commons.exception;

public class CacheException extends RuntimeException {
	public CacheException() {
		super();
	}
	
	public CacheException(String message) {
		super(message);
	}
	
	public CacheException(Throwable t) {
		super(t);
	}
	
	public CacheException(String message, Throwable t) {
		super(message, t);
	}
}
