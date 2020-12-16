package com.test.commons.web.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

import com.test.commons.annotation.BackingBean;
import com.test.commons.util.JspUtil;

public class BackingBeanHelper {
	private final static Logger log = LoggerFactory.getLogger(BackingBeanHelper.class);
	
	/** 置於 URI 查詢字串中, 用來指定 action name 的參數名 */
    public static final String DEFAULT_ACTION_KEY = "_action";
    
    /** 預設 response 輸出的 content type */
    public static final String DEFAULT_RESPONSE_CONTENT_TYPE = "text/html;charset=UTF-8";
    
    /**
     * MIME media type for JSON text is <strong>application/json</strong>; the default encoding is <strong>UTF-8</strong>.<br>
     * But for JSONP with callback: <strong>application/javascript</strong>
     * 
     * @see 參考 https://stackoverflow.com/questions/477816/what-is-the-correct-json-content-type
     * @see <a href="http://www.ietf.org/rfc/rfc4627.txt">RFC 4627</a>
     * @see <a href="http://jibbering.com/blog/?p=514">Why you shouldn't use text/html for JSON</a>
     * @see <a href="http://www.entwicklungsgedanken.de/2008/06/06/problems-with-internet-explorer-and-applicationjson/">Internet Explorer sometimes has issues with application/json</a>
     * @see <a href="https://github.com/h5bp/server-configs-nginx/blob/master/mime.types">A rather complete list of Mimetypes and what to use them for</a>
     */
    public static final String DEFAULT_RESPONSE_JSON_TYPE = "application/json";
    
    public static final String DEFAULT_RESPONSE_TEXT_TYPE = "text/plain";
    
	/** 預設 response 輸出的 content encoding */
    public static final String DEFAULT_RESPONSE_CHARACTER_ENCODING = "UTF-8";
    
	/** 預設最大檔案上傳限制(單位 byte) */
    public static final long DEFAULT_FILE_UPLOAD_MAX_SIZE_BYTES = JspUtil.FILE_UPLOAD_MAX_SIZE;
    
	/** 記錄當前畫面頁的 backing bean name */
    public static final String KEY_CURRENT_BACKING_BEAN_NAME = "_current_backing_bean_name_";
    
    /** backing bean action 傳回的字串以此做開頭者, 代表要使用 forward */
    public static final String DEFAULT_FORWARD_PREFIX_INDICATOR = "forward:";
    
    /** backing bean action 傳回的字串以此做開頭者, 代表要使用 redirect */
    public static final String DEFAULT_REDIRECT_PREFIX_INDICATOR = "redirect:";
    
    /** 在記錄當前 JSP 畫面的實際 URI, forward 後也要能反映導頁後的 URI */
    public static final String KEY_CURRENT_JSP_URI = "_current_jsp_uri_";
    
    /** 預設用來代表 jsp request 的 URI 結尾字串 */
    public static final String DEFAULT_JSP_URI_SUFIX = ".jsp";
    
    /** 預設用來代表 ajax request 的 URI 結尾字串 */
    public static final String DEFAULT_AJAX_URI_SUFIX = ".ajax";
    
    /** 預設用來代表簡單 RESTful request 的 URI 結尾字串 */
    public static final String DEFAULT_SIMPLE_RESOURCE_URI_SUFIX = ".resource";
    
    /** 預設用來代表 ajax 方式引入子頁 request 的 URI 結尾字串 */
    public static final String DEFAULT_INCLUDE_PAGE_URI_SUFIX = ".inc";
    
    //以下是用於 web.xml 中的自訂參數 key
    
    /** 
     * WAR 設定檔參數名. 用在 web.xml 中 /web-app/.../init-param/param-name/ 設定用參數名.
     * @see #DEFAULT_ACTION_KEY
     */
    public static final String BACKING_BEAN_ACTION_KEY = "BACKING_BEAN_ACTION_KEY";
    
    /** 
     * WAR 設定檔參數名. 用在 web.xml 中 /web-app/.../init-param/param-name/ 設定用參數名.
     * @see #DEFAULT_FORWARD_PREFIX_INDICATOR
     */
    public static final String BACKING_BEAN_FORWARD_PREFIX_INDICATOR = "BACKING_BEAN_FORWARD_PREFIX_INDICATOR";
    
    /** 
     * WAR 設定檔參數名. 用在 web.xml 中 /web-app/.../init-param/param-name/ 設定用參數名.
     * @see #DEFAULT_REDIRECT_PREFIX_INDICATOR
     */
    public static final String BACKING_BEAN_REDIRECT_PREFIX_INDICATOR = "BACKING_BEAN_REDIRECT_PREFIX_INDICATOR";
    
    /** 
     * WAR 設定檔參數名. 用在 web.xml 中 /web-app/.../init-param/param-name/ 設定用參數名.
     * @see RequestParameterHelper#DEFAULT_APSERVER_URI_ENCODING
     */
    public static final String URI_CHARACTER_ENCODING = "URI_CHARACTER_ENCODING";
    
    /** 
     * WAR 設定檔參數名. 用在 web.xml 中 /web-app/.../init-param/param-name/ 設定用參數名.
     * @see RequestParameterHelper#DEFAULT_REQUEST_CHARACTER_ENCODING
     */
    public static final String REQUEST_CHARACTER_ENCODING = "REQUEST_CHARACTER_ENCODING";
    
    /** 用於 HttpServletResponse.setCharacterEncoding(), 但可能被稍後的 HttpServletResponse.setContentType() 之設定覆蓋 */
    public static final String RESPONSE_CONTENT_TYPE_KEY = "RESPONSE_CONTENT_TYPE";
    
    /** 用於 HttpServletResponse.setContentType() */
    public static final String RESPONSE_CHARACTER_ENCODING_KEY = "RESPONSE_CHARACTER_ENCODING";
    
    /** 
     * WAR 設定檔參數名. 用在 web.xml 中 /web-app/.../init-param/param-name/ 設定用參數名.
     * @see JspUtil#FILE_UPLOAD_MAX_SIZE
     */
    public static final String FILE_UPLOAD_MAX_SIZE_BYTES = "FILE_UPLOAD_MAX_SIZE_BYTES";
    
    /** 是否要檢查 Spring 的 Scope annotation. */
    private static Boolean _checkSpringScopeAnnotation = null;
}
