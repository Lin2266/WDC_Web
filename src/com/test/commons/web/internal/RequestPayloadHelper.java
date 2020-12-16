package com.test.commons.web.internal;

import java.io.*;
import java.util.*;
import javax.servlet.ServletRequest;

import com.test.commons.exception.HttpHandleException;
import com.test.commons.util.FileUtil;
import com.test.commons.util.JspUtil;

public class RequestPayloadHelper {
	/** request 參數值預設欲使用的編碼. 如果在取參數值前未設定 ServletRequest.setCharacterEncoding() 者, 將採用此預設值 */
	public static final String DEFAULT_REQUEST_CHARACTER_ENCODING = RequestParameterHelper.DEFAULT_REQUEST_CHARACTER_ENCODING;
	
	/**
	 * 取 request body 內容(即 payload).<br> 
	 * 注意: request.getParameterMap() 後, 本 method 就取不到內容了, 反之亦然
	 * @param request 在沒有使用 HttpServletRequestWrapper 包裹的情況下, 本 method 取出 payload 後, request 物件將無法再次取得 request 資料
	 * @param charEncoding
	 * @see #DEFAULT_REQUEST_CHARACTER_ENCODING
	 */
    public static String getRequestPayload(ServletRequest request, String charEncoding) {
    	return RequestParameterHelper.getRequestPayload(request, charEncoding);
    }
    
    /**
	 * 取 request body 內容(即 payload), 
	 * chartset encoding 採用 request 之 characterEncoding 屬性所指定的編碼, 若無者則使用預設編碼(DEFAULT_REQUEST_CHARACTER_ENCODING).<br>
	 * 注意: request.getParameterMap() 後, 本 method 就取不到內容了, 反之亦然
	 * @param request 在沒有使用 HttpServletRequestWrapper 包裹的情況下, 本 method 取出 payload 後, request 物件將無法再次取得 request 資料
	 * @see #DEFAULT_REQUEST_CHARACTER_ENCODING
	 */
    public static String getRequestPayload(ServletRequest request) {
    	String charEncoding = request.getCharacterEncoding();
		if(charEncoding == null)
			charEncoding = DEFAULT_REQUEST_CHARACTER_ENCODING;
		return getRequestPayload(request, charEncoding);
    }

    /**
	 * 取 request body 內容(即 payload).<br/> 
	 * 注意: request.getParameterMap() 後, 本 method 就取不到內容了, 反之亦然
	 * @param request 在沒有使用 HttpServletRequestWrapper 包裹的情況下, 本 method 取出 payload 後, request 物件將無法再次取得 request 資料
	 */
    public static byte[] getRequestPayloadAsBytes(ServletRequest request) {
    	InputStream in = null;
    	
    	try {
    		final ByteArrayOutputStream ret = new ByteArrayOutputStream(); //不需 close
    		FileUtil.dump(in = request.getInputStream(), ret);
    		in.close();
    		in = null;
    		return ret.toByteArray();
    	} catch(Throwable t) {
    		throw new HttpHandleException(t.getMessage(), t);
    	} finally {
    		if(in != null) try { in.close(); } catch(Throwable t) {}
    	}
    }
    
    /**
	 * 取 request body 內容(即 payload)並存檔於當前 AP 之暫目錄下, 檔名由系統自訂.<br> 
	 * 注意: request.getParameterMap() 後, 本 method 就取不到內容了, 反之亦然
	 * @param request 在沒有使用 HttpServletRequestWrapper 包裹的情況下, 本 method 取出 payload 後, request 物件將無法再次取得 request 資料
	 */
    public static File getRequestPayloadAsFile(ServletRequest request) {
    	InputStream in = null;
    	
    	try {
    		final File tmpDir = JspUtil.createUniqueTempRealDir();
    		final File f = File.createTempFile("upload-", ".payload", tmpDir);
    		FileUtil.dump(in = request.getInputStream(), f);
    		in.close();
    		in = null;
    		return f;
    	} catch(Throwable t) {
    		throw new HttpHandleException(t.getMessage(), t);
    	} finally {
    		if(in != null) try { in.close(); } catch(Throwable t) {}
    	}
    }
    
    /**
     * 把 request payload 中的資料化為 name=value 之集合的型式.
     */
    public static Map<String, String[]> getParameterMap(final ServletRequest request, final String charEncoding) {
    	return RequestParameterHelper.ensureGetParameterMap(getParameterMap2(request, charEncoding));
    }

    /**
     * 把 request payload 中的資料化為 name=value 之集合的型式.<br>
     * 注意: 得出的內容, 已經過 URLDecoder 及字元編碼處理(按 request 之 characterEncoding 屬性值).
     */
    public static Map<String, String[]> getParameterMap(ServletRequest request) {
    	String charEncoding = request.getCharacterEncoding();
		if(charEncoding == null)
			charEncoding = DEFAULT_REQUEST_CHARACTER_ENCODING;
		return getParameterMap(request, charEncoding);
    }
    
    /**
     * 把 request payload 中的資料化為 name=value 之集合的型式.<br>
     * 注意: 得出的內容, 已經過 URLDecoder 及字元編碼處理(按 request 之 characterEncoding 屬性值).
     */
    public static Map<String, List<String>> getParameterMap2(ServletRequest request, String charEncoding) {
    	return RequestParameterHelper.queryStringToMap2(getRequestPayload(request, charEncoding), charEncoding);
    }

    /**
     * 把 request payload 中的資料化為 name=value 之集合的型式.<br>
     * 注意: 得出的內容, 已經過 URLDecoder 及字元編碼處理(按 request 之 characterEncoding 屬性值).
     */
    public static Map<String, List<String>> getParameterMap2(ServletRequest request) {
    	String charEncoding = request.getCharacterEncoding();
		if(charEncoding == null)
			charEncoding = DEFAULT_REQUEST_CHARACTER_ENCODING;
		return getParameterMap2(request, charEncoding);
    }
    
    static List<String> split(String s, String delimiters) {
    	return RequestParameterHelper.split(s, delimiters);
    }
}
