package com.test.commons.exception;

/**
 * 標示 java.sql.ResultSet 物件已無下一筆資料的 exception. 
 */
@SuppressWarnings("serial")
public class NonResultException extends RuntimeException {
	public NonResultException() {
		super();
	}
	
	public NonResultException(String message) {
		super(message);
	}
	
	public NonResultException(Throwable t) {
		super(t);
	}
	
	public NonResultException(String message, Throwable t) {
		super(message, t);
	}
}
