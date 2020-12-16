package com.test.commons.pdf;

/**
 * @deprecated 由 com.tatung.commons.pdf2.XprintException 取代
 */
@Deprecated
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
