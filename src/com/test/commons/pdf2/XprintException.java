package com.test.commons.pdf2;

public class XprintException extends RuntimeException {
	public XprintException(String msg) {
        super(msg);
    }

    public XprintException(Throwable cause) {
        super("", cause);
    }

    public XprintException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
