package com.test.commons.exception;

/**
 * 用以表示 SQL 操作類所發生的 exception.
 */
@SuppressWarnings("serial")
public class SQLExecuteException extends RuntimeException {

	public SQLExecuteException() {
		super();
	}
	
	public SQLExecuteException(String reason) {
		super(reason);
	}
	
	public SQLExecuteException(String reason, Throwable cause) {
		super(reason, cause);
	}
	
	public SQLExecuteException(Throwable cause) {
		super(cause);
	}
}
