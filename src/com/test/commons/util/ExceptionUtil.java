package com.test.commons.util;

/**
 * 錯誤訊息處理工具.
 * @author 洪瑀若, 謝岳佐
 * @since 2007/11/20
 * @version
 */
public class ExceptionUtil {
	private ExceptionUtil() {}

    /**
     * 找出 exception 根源.
     */
    public static Throwable getRootException(Throwable e) {
        Throwable t = e, t2 = e;
        while((t = t.getCause()) != null)
            t2 = t;

        return t2;
    }

    /**
     * 取得 root exception 之訊息.
     */
    public static String getRootMessage(Throwable e) {
        Throwable ee = getRootException(e);
        String msg = ee.getMessage();
        if(msg == null || "".equals(msg) || "null".equals(msg))
            msg = ee.toString();

        return msg;
    }
}
