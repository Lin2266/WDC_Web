package com.test.commons.exception;

public class ErrorConfigurationException extends RuntimeException {
	public ErrorConfigurationException() {
		super();
	}
	
	public ErrorConfigurationException(String message) {
		super(message);
	}
	
	public ErrorConfigurationException(Throwable t) {
		super(t);
	}
	
	public ErrorConfigurationException(String message, Throwable t) {
		super(message, t);
	}
}
