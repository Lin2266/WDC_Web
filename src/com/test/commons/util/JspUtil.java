package com.test.commons.util;

import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.apache.commons.beanutils.PropertyUtils;

import com.test.commons.acl.IAccount;
import com.test.commons.web.FlashScope;
import com.test.commons.web.internal.BackingBeanHelper;
import com.test.commons.web.internal.FlashScopeImpl;

/**
 * Utility class for JSP and Servlet.<br>
 * 本工具限定用在 View 層的 bean 中.<br>
 * <p>
 * depend on: Apache beanutils, Google gson, Spring framework
 */
public class JspUtil {
    private final static Logger log = LoggerFactory.getLogger(JspUtil.class);
    private static ApplicationContext _springContext;
    private static String _contextRealPath; //context-path 之實體路徑
    
    private static final ThreadLocal<HttpServletRequest> _request = new ThreadLocal<HttpServletRequest>();
    
    public final static String KEY_MSG_IN_REQUEST = "myMessageInRequest";
    
    /** 預設檔案上傳之最大單一檔案大小限制(100M) */
    public final static int FILE_UPLOAD_MAX_SIZE = 100 * 1024 * 1024; //100M (bytes)
    
    /** error page 的 URI (/error.jsp) */
    public final static String ERROR_PAGE_URI = "/error.jsp";
    
    protected JspUtil() {}
    
    /** 取使用者 server-side session 中的個人資訊物件 */
    @SuppressWarnings("unchecked")
    public static <T extends IAccount> T getAccountInSession() {
        return (T)getCurrentBackgroundHttpServletRequest().getSession().getAttribute(IAccount.KEY_IN_HTTP_SESSION);
    }
    
    /** 取當前 web ap 之 context path. */
    public static String getContextPath() {
        return getCurrentBackgroundHttpServletRequest().getContextPath();
    }
    
    public static HttpServletRequest getCurrentBackgroundHttpServletRequest() {
    	HttpServletRequest request = _request.get();
    	if(request == null)
    		throw new IllegalStateException("HttpServletRequest object not found by the framework. Maybe the util is not called in servlet context, or wrong configuration");
    	return request;
    }
    
    /**
     * 設定 context-path 之實體路徑, 且只可設定一次.
     * <br><b>注意: ServletContext().getRealPath(""); 不適用於 WebLogic 之類的 AP Server</b>
     * @param contextRealPath
     */
    protected static void setContextRealPath(String contextRealPath) {
        if(_contextRealPath != null)
            throw new RuntimeException("contextRealPath can be set only once");
        log.info("contextRealPath been set as: " + contextRealPath);
        _contextRealPath = contextRealPath;
    }
    
    /** 取當前 web ap 之 context path 的實體路徑(AP 初始化之初即需設值). */
    public static String getContextRealPath() {
        if(_contextRealPath == null)
            throw new RuntimeException("contextRealPath value should be set (by JspUtil.setContextRealPath()) previously");
        //return MyRequest.getSession().getServletContext().getRealPath("/"); //對 WebLogic 之類的 ap server 會失效
        return _contextRealPath;
    }
    
    //called by front-end servlet/filter
    protected static void setHttpServletRequest(HttpServletRequest request) {
        _request.set(request);
    }
    
    /**
     * 建立當前獨一無二的暫存目錄的實體路徑, 且保證每次呼叫本函數時會得到不同的路徑(在 AP tmp 目錄下).
     * <br>
     * 如果在非 web runtime 環境下呼叫本函數, 暫存目錄將指向 OS 系統暫存目錄下產生, 如: 
     * "/tmp/xxxx" 或 "/var/tmp/xxx" (在 Windows OS 下可能為 c:\WINNT\TEMP\xxx", 視 JVM system property "java.io.tmpdir" 之值而定).
     * @see #createUniqueTempRealPath()
     */
    public static File createUniqueTempRealDir() {
    	try {
	    	String tmpRootPath = (_contextRealPath != null) ? (_contextRealPath + "/tmp") : System.getProperty("java.io.tmpdir");
	    	if(tmpRootPath == null)
	    		tmpRootPath = "/tmp";
	    	final File tmpRootDir = new File(tmpRootPath);
	    	if(!tmpRootDir.isDirectory())
	    		tmpRootDir.mkdirs();
	    	
	    	return Files.createTempDirectory(tmpRootDir.toPath(), (String)null).toFile();
    	} catch(RuntimeException re) {
    		throw re;
    	} catch(Throwable t) {
    		throw new RuntimeException(t.getMessage(), t);
    	}
    }
    
    /**
     * 建立當前獨一無二的暫存目錄的實體路徑, 且保證每次呼叫本函數時會得到不同的路徑(在 AP tmp 目錄下).
     * <br>
     * 如果在非 web runtime 環境下呼叫本函數, 暫存目錄將指向 OS 系統暫存目錄下產生, 如: 
     * "/tmp/xxxx" 或 "/var/tmp/xxx" (在 Windows OS 下可能為 c:\WINNT\TEMP\xxx", 視 JVM system property "java.io.tmpdir" 之值而定).
     * @see #createUniqueTempRealDir()
     */
    public static String createUniqueTempRealPath() {
    	return createUniqueTempRealDir().getAbsolutePath();
    }

    /**
     * 針對位於 web ap 運行環境之下的實體絕對路徑, 將之轉成相對於 web context path 的虛擬路徑(不含 context path). 
     * 若輸入的實體路徑不在 web ap 運行位罝之下, 則傳回 null.
     * @param realPath OS 檔案系統上的實體路徑(絕對路徑), 限位於 web ap 運行時期的 context path 實體目錄之下的目錄或檔案.
     */
    public static String getRelativeURIByRealPath(String realPath) {
    	if(realPath == null)
    		return null;
    	final String contextRealPath = getContextRealPath();
    	if(!realPath.startsWith(contextRealPath))
    		return null;
    	String path = StrUtil.replaceAll(realPath.substring(contextRealPath.length()), "\\", "/");
    	if(path.length() > 0 && path.charAt(0) != '/')
    		path = "/" + path;
    	return path;
    }
    
    /** 
     * 取 flash scope 容器, 以便傳遞資料到下一網頁的 backing bean 或 JSP (透過 forward, redirect, form submit 或 direct link 等方式).
     * 例如: 欲在當前頁傳遞 aaa=xxx, bbb=yyy 二參數值至下一頁, 可在當前頁的 backing bean 中執行(注意不能在 JSP 中使用 JSTL 設值):<pre>
     * 	 FlashScope flashScope = JspUtil.flashScope();
     * 	 flashScope.put("aaa", xxx);
     *   flashScope.put("bbb", yyy);
     * </pre>
     * 至下一頁時, 可在下一頁的 backing bean 中取出:<pre>
     *   FlashScope flashScope = JspUtil.flashScope();
     *   Xxx xxx = flashScope.get("aaa");
     *   Yyy yyy = flashScope.get("bbb");
     * </pre>
     * 或者在下一頁 JSP 中以 EL 取值:<pre>
     *   &lt;html&gt;
     *       &lt;body&gt;
     *           ${flashScope.aaa} ,
     *           ${flashScope.bbb}
     * </pre>
     * 畫面上將顯示 xxx, yyy 二物件化成的字串.
     */
    public static FlashScope flashScope() {
    	return FlashScopeImpl.getCurrentInstance(getCurrentBackgroundHttpServletRequest());
    }
    
    /**
     * 取得當前 JSP 畫面的 URI, 含 context path, 不含 query string.
     * 如果 server 端藉 forward 導至其他 JSP 畫面者, 傳回導頁後的 JSP URI (目前只針對由 BackingBeanFilter 處理而導頁者).
     * @see com.test.commons.web.internal.BackingBeanHelper#KEY_CURRENT_JSP_URI
     */
    public static String getCurrentJspURI() {
    	String uri = null;
    	HttpServletRequest request = getCurrentBackgroundHttpServletRequest();
    	HttpSession session = request.getSession(false);
    	if(session == null || (uri = (String)session.getAttribute(BackingBeanHelper.KEY_CURRENT_JSP_URI)) == null) {
    		uri = request.getRequestURI();
    		if("".equals(uri)) //網址可能是 index.jsp 之類的 default page
    			return request.getContextPath() + request.getServletPath();
    		return uri;
    	}
    	return uri;
    }
    
    /** 設定欲置於 request 的訊息. */
    public static void setMessage(String message) {
    	HttpServletRequest request = getCurrentBackgroundHttpServletRequest();
        request.setAttribute(KEY_MSG_IN_REQUEST, message);
        request.getSession(true).removeAttribute(KEY_MSG_IN_REQUEST);
    }

    /** 設定欲置入 request 的訊息(含被捕捉的 Exception 訊息). */
    public static void setMessage(String message, Throwable t) {
    	HttpServletRequest request = getCurrentBackgroundHttpServletRequest();
        request.setAttribute(KEY_MSG_IN_REQUEST, message + ": " + t.toString());
        request.getSession(true).removeAttribute(KEY_MSG_IN_REQUEST);
    }

    /** 取得 request 中的由其他元件傳來的訊息, 如無者, 嘗試由 session 中取(只限由 session 取一次). */
    public static String getMessage() {
    	HttpServletRequest request = getCurrentBackgroundHttpServletRequest();
        String msg = (String)request.getAttribute(KEY_MSG_IN_REQUEST);
        if(msg == null) {
        	HttpSession session = request.getSession(true);
        	msg = (String)session.getAttribute(KEY_MSG_IN_REQUEST);
        	session.removeAttribute(KEY_MSG_IN_REQUEST);
        }
        return (msg != null) ? msg : "";
    }

    /** request 中是否有訊息. */
    public static boolean getHasMessage() {
    	String tmp;
    	HttpServletRequest request = getCurrentBackgroundHttpServletRequest();
    	return ((tmp = (String)request.getAttribute(KEY_MSG_IN_REQUEST)) != null && !"".equals(tmp)) ||
    			((tmp = (String)request.getSession(true).getAttribute(KEY_MSG_IN_REQUEST)) != null && !"".equals(tmp));
    }
    
    public static void internalCopyMssageInRequestToSession() {
    	HttpServletRequest request = getCurrentBackgroundHttpServletRequest();
    	String msg = (String)request.getAttribute(KEY_MSG_IN_REQUEST);
    	if(msg != null)
    		request.getSession(true).setAttribute(KEY_MSG_IN_REQUEST, msg);
    }

    /** 二 Java Bean 屬性的複製. 將 src bean 的屬性值複製到 dest bean 的屬性, 並且只進行淺層複製, 複雜屬性型態如 List 或自定義 type 等則無法複製. */
    public static void copyProperties(Object dest, Object src) {
        try {
            PropertyUtils.copyProperties(dest, src);
            
        } catch(RuntimeException re) {
    		throw re;
    	}  catch(Throwable t) {
            throw new RuntimeException(t.getMessage(), t);
        }
    }
    
    /** 取當前 JSP 所對應的 backing bean. 由當前 JSP 之 URI 推得在 spring context 中的 bean */
    public static Object getBackingBean(String beanId) {
        return getSpringContext().getBean(beanId);
    }
    
    /**
     * 提供內容, 讓前端網頁下載成為檔案 (content-type 為 "application/octet-stream").
     * 應在 response.getWriter() 或 response.getOutputStream() 之前執行.
     * @param response
     * @param file 供前端下載的資料來源
     * @param savedFilename 下載後要存的檔名(optional, 未指定者則沿用 file 的檔名)
     */
    public static void clientDownload(final HttpServletResponse response, final File file, final String savedFilename) {
    	forClientDownload(response, true, savedFilename, null, null, file, null, null);
    }
    
    /**
     * 提供內容, 讓前端網頁下載成為檔案.
     * 應在 response.getWriter() 或 response.getOutputStream() 之前執行.
     * @param response
     * @param file 供前端下載的資料來源
     * @param savedFilename 下載後要存的檔名(optional, 未指定者則沿用 file 的檔名)
     * @param mimeType (optional, 預設為 "application/octet-stream", 可設為與檔案類型對應的值, 如: "application/pdf", "application/vnd.ms-excel", "application/XprintReader" 等)
     */
    public static void clientDownload(final HttpServletResponse response, final File file, final String savedFilename, 
    		final String mimeType) {
    	forClientDownload(response, true, savedFilename, null, mimeType, file, null, null);
    }
    
    /**
     * 提供內容, 讓前端網頁下載成為檔案 (content-type 為 "application/octet-stream").
     * 應在 response.getWriter() 或 response.getOutputStream() 之前執行.
     * @param response
     * @param source 供前端下載的資料來源
     * @param savedFilename 下載後要存的檔名
     * @param fileLength 下載檔案大小(optional)
     */
    public static void clientDownload(final HttpServletResponse response, final InputStream source, final String savedFilename, final Integer fileLength) {
    	forClientDownload(response, true, savedFilename, fileLength, null, null, source, null);
    }
    
    /**
     * 提供內容, 讓前端網頁下載成為檔案.
     * 應在 response.getWriter() 或 response.getOutputStream() 之前執行.
     * @param response
     * @param source 供前端下載的資料來源
     * @param savedFilename 下載後要存的檔名
     * @param fileLength 下載檔案大小(optional)
     * @param mimeType (optional, 預設為 "application/octet-stream", 可設為與檔案類型對應的值, 如: "image/gif", "application/pdf", "application/vnd.ms-excel", "application/XprintReader" 等)
     */
    public static void clientDownload(final HttpServletResponse response, final InputStream source, final String savedFilename, final Integer fileLength, 
    		final String mimeType) {
    	forClientDownload(response, true, savedFilename, fileLength, mimeType, null, source, null);
    }
    
    /**
     * 提供內容, 讓前端網頁下載成為檔案 (content-type 為 "application/octet-stream").
     * 應在 response.getWriter() 或 response.getOutputStream() 之前執行.
     * @param response
     * @param handler 呼叫者自行輸出供前端下載的資料
     * @param savedFilename 下載後要存的檔名
     * @param fileLength 下載檔案大小(optional)
     */
    public static void clientDownload(final HttpServletResponse response, final OutputStreamHandler handler, final String savedFilename, final Integer fileLength) {
    	forClientDownload(response, true, savedFilename, fileLength, null, null, null, handler);
    }
    
    /**
     * 提供內容, 讓前端網頁下載成為檔案.
     * 應在 response.getWriter() 或 response.getOutputStream() 之前執行.
     * @param response
     * @param handler 呼叫者自行輸出供前端下載的資料
     * @param savedFilename 下載後要存的檔名
     * @param fileLength 下載檔案大小(optional)
     * @param mimeType (optional, 預設為 "application/octet-stream", 
     * 		可設為與檔案類型對應的值, 如: "image/gif", "application/pdf", "application/vnd.ms-excel", "application/XprintReader" 等)
     */
    public static void clientDownload(final HttpServletResponse response, final OutputStreamHandler handler, final String savedFilename, final Integer fileLength, 
    		final String mimeType) {
    	forClientDownload(response, true, savedFilename, fileLength, mimeType, null, null, handler);
    }
    
    /**
     * 提供內容, 讓前端網頁下載並以應用程式打開(由 mimeType 決定由何種程式開啟).
     * 應在 response.getWriter() 或 response.getOutputStream() 之前執行.
     * @param response
     * @param file 供前端下載的資料來源
     * @param filename 被傳送的檔案之檔名(optional, 未指定者則沿用 file 的檔名)
     * @param mimeType (optional, 但未指定者, 恐被瀏覽器當作 "下載並存檔" 處理)預設為 "application/octet-stream", 
     * 		可設為與檔案類型對應的值, 如: "image/gif", "application/pdf", "application/vnd.ms-excel", "application/XprintReader" 等
     */
    public static void clientDownloadAndOpen(final HttpServletResponse response, final File file, final String filename, 
    		final String mimeType) {
    	forClientDownload(response, false, filename, null, mimeType, file, null, null);
    }
    
    /**
     * 提供內容, 讓前端網頁下載並以應用程式打開(由 mimeType 決定何種程式).
     * 應在 response.getWriter() 或 response.getOutputStream() 之前執行.
     * @param response
     * @param source 供前端下載的資料來源
     * @param filename 被傳送的檔案之檔名
     * @param fileLength 傳送的檔案大小(optional)
     * @param mimeType (optional, 但未指定者, 恐被瀏覽器當作 "下載並存檔" 處理)預設為 "application/octet-stream", 
     * 		可設為與檔案類型對應的值, 如: "image/gif", "application/pdf", "application/vnd.ms-excel", "application/XprintReader" 等
     */
    public static void clientDownloadAndOpen(final HttpServletResponse response, final InputStream source, final String filename, final Integer fileLength, 
    		final String mimeType) {
    	forClientDownload(response, false, filename, fileLength, mimeType, null, source, null);
    }
    
    /**
     * 提供內容, 讓前端網頁下載並以應用程式打開(由 mimeType 決定何種程式).
     * 應在 response.getWriter() 或 response.getOutputStream() 之前執行.
     * @param response
     * @param handler 由呼叫者輸出供前端下載的資料
     * @param filename 被傳送的檔案之檔名
     * @param fileLength 傳送的檔案大小(optional)
     * @param mimeType (optional, 但未指定者, 恐被瀏覽器當作 "下載並存檔" 處理)預設為 "application/octet-stream", 
     * 		可設為與檔案類型對應的值, 如: "image/gif", "application/pdf", "application/vnd.ms-excel", "application/XprintReader" 等
     */
    public static void clientDownloadAndOpen(final HttpServletResponse response, final OutputStreamHandler handler, final String filename, final Integer fileLength, 
    		final String mimeType) {
    	forClientDownload(response, false, filename, fileLength, mimeType, null, null, handler);
    }
    
    /**
     * @param response
     * @param isAttach true("attachment", 資料應下載並存為檔案), false("inline", 資料以 response 網頁的一部分或整個頁面的型式傳遞)
     * @param downloadFilename 下載後要存的檔名(採用 dataForDownload 時為必要)
     * @param fileLength 下載檔案大小(optional)
     * @param contentType (optional, 預設為 "application/octet-stream", 可設為與檔案類型對應的值, 如: "image/gif", "application/vnd.ms-excel", "application/XprintReader" 等)
     * @param fileForDownload 供前端下載的資料來源 (fileForDownload 或 dataForDownload 擇一)
     * @param dataForDownload 供前端下載的資料來源 (fileForDownload 或 dataForDownload 擇一)
     * @param outputForDownload 提供 HttpServletResponse 的 outputStream 由呼叫者自行輸出供前端下載的資料 
     */
    private static void forClientDownload(final HttpServletResponse response, final boolean isAttach, final String downloadFilename, 
    		final Integer fileLength, final String contentType, 
    		final File fileForDownload, final InputStream dataForDownload, final OutputStreamHandler outputForDownload) {
    	try {
    		if(response == null)
    			throw new IllegalArgumentException("argument 'response' not specified");
    		final String inlineAttach = isAttach ? "attachment" : "inline"; //按 HTTP 規格預設為 inline
    		
    		if(fileForDownload != null) {
    			if(!fileForDownload.isFile())
    				throw new IllegalArgumentException("input file specified for downloading '" + fileForDownload.getName() + "' does not refer to a regular file");
    			final String filename = URLEncoder.encode(StrUtil.isEmpty(downloadFilename) ? fileForDownload.getName() : downloadFilename, "UTF-8");
    			
				response.setContentType(StrUtil.isEmpty(contentType) ? "application/octet-stream" : contentType); //(1)
    			response.setContentLength((int)fileForDownload.length()); //(2)
    			response.setHeader("Content-Disposition", inlineAttach + "; filename*=UTF-8''" + filename + "; filename=" + filename); //(3) 兼顧各家瀏覽器的 download URL 的作法
    			//filename*=UTF-8''檔名: 參考 RFC 5987, 及 https://stackoverflow.com/questions/93551/how-to-encode-the-filename-parameter-of-content-disposition-header-in-http
    			
    			if(fileForDownload.length() != 0) {
	    			final ServletOutputStream out = response.getOutputStream(); //要在所有 response.setXXX() 之後執行; 不能自行 close()
	    			FileUtil.dump(fileForDownload, out);
	    			out.flush(); //commit response!
    			}
    		} else if(dataForDownload != null || outputForDownload != null) {
    			if(StrUtil.isEmpty(downloadFilename))
    				throw new IllegalArgumentException("argument 'downloadFilename' not specified");
    			final String filename = URLEncoder.encode(downloadFilename, "UTF-8");
    			
				response.setContentType(StrUtil.isEmpty(contentType) ? "application/octet-stream" : contentType); //(1)
    			if(fileLength != null)
    				response.setContentLength(fileLength); //(2)
    			response.setHeader("Content-Disposition", inlineAttach + "; filename*=UTF-8''" + filename + "; filename=" + filename); //(3) 兼顧各家瀏覽器的 download URL 的作法
    			//filename*=UTF-8''檔名: 參考 RFC 5987, 及 https://stackoverflow.com/questions/93551/how-to-encode-the-filename-parameter-of-content-disposition-header-in-http
    			final ServletOutputStream out = response.getOutputStream(); //要在所有 response.setXXX() 之後執行; 不能自行 close()
    			
    			if(dataForDownload != null) {
        			FileUtil.dump(dataForDownload, out);
        		} else if(outputForDownload != null) {
        			outputForDownload.execute(out);
        		}
    			out.flush(); //commit response!
    		} else {
				throw new IllegalArgumentException("input file or data stream for downloading not specified");
			}
    	} catch(RuntimeException re) {
    		throw re;
    	} catch(Throwable t) {
    		throw new RuntimeException(t.getMessage(), t);
    	}
    }
    
    private static ApplicationContext getSpringContext() {
    	if(_springContext != null)
    		return _springContext;
    	final String errorMsg = "not called in this web framework's context";
    	final HttpServletRequest request = getCurrentBackgroundHttpServletRequest();
    	final ServletContext servletContext = request.getSession().getServletContext();
    	if(servletContext == null)
    		throw new IllegalStateException(errorMsg);
    	return (_springContext = WebApplicationContextUtils.getWebApplicationContext(servletContext));
    }
}
