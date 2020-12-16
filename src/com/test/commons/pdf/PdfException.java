package com.test.commons.pdf;

public class PdfException extends RuntimeException {
	public PdfException(String msg) {
        super(msg);
    }

    public PdfException(Throwable cause) {
        super("", cause);
    }

    public PdfException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
