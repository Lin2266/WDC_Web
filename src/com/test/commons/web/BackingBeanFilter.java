package com.test.commons.web;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.test.commons.annotation.BackingBean;
import com.test.commons.util.StrUtil;
import com.test.commons.web.internal.AjaxAdapter;
import com.test.commons.web.internal.BackingBeanHelper;
import com.test.commons.web.internal.IncludePageAdapter;
import com.test.commons.web.internal.JspAdapter;
import com.test.commons.web.internal.RequestParameterHelper;
import com.test.commons.web.internal.RequestWrapper;
import com.test.commons.web.internal.ResponseWrapper;
import com.test.commons.web.internal.SimpleResourceAdapter;

/**
 * <ol>
 * 	<li>此 filter 一定要排在眾 filter 的最後, 因為本 filter 有可能中止 filter chain 的動作.
 * 	<li>web.xml 中設定 filter path 要對應成 "/*", 以便 HttpServletRequest.getServletPath() 能捕捉到 URI 中 context path 及 query string 之間的任何字串
 * 	<li>每個 JSP 的 backing bean 預設為 singleton scope.
 * 	<li>如果 backing bean 的 field 或 getter/setter 被標注以 @Input 者, 則必須明確指定 bean scope 到底是 request 或 session.
 * </ol>
 */
public class BackingBeanFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(BackingBeanFilter.class);
    private static final String[] BYPASS_URI_PREFIX = {}; //由於歷史原因(有既有相對應的 servlet 在處理), 需加以排除處理的網址(不含 context path)
    
    //context configurations
    private ApplicationContext applicationContext; //Spring context
    //private SQLErrorCodeTranslator sqlErrorCodeTranslator; //將 SQL 錯誤代碼, 訊息轉成中文的元件
    private String actionParamName; //在 URL 中用來代表要呼叫 backing bean action 的參數
    private String forwardPrefixIndicator; //backing bean 欲 forward 時, 傳回值字串之前應加上的標示字串
    private String redirectPrefixIndicator; //backing bean 欲 redirect 時, 傳回值字串之前應加上的標示字串
    private String uriCharEncoding; //ap server 對網址所使用的字元編碼
    private String requestCharacterEncoding; //request parameter encoding
    private String defaultResponseCharacterEncoding; //Ajax response 的 encoding
    private String defaultContentType; //Ajax response 的 content type
    private long fileUploadMaxSizeBytes; //max file upload size (byte)
    
    //依 JSP 畫面, AJAX 處理, RESTful... 等用途而搭配對應的組件 (sorry, 和 "adapter pattern" 沒啥關係)
    private JspAdapter jspAdapter;
    private AjaxAdapter ajaxAdapter;
    private SimpleResourceAdapter simpleResourceAdapter;
    private IncludePageAdapter includePageAdapter;
    
    @Override
    public void destroy() {
    	this.applicationContext = null;
    	this.actionParamName = null;
    	this.forwardPrefixIndicator = null;
    	this.redirectPrefixIndicator = null;
    	this.uriCharEncoding = null;
    	this.requestCharacterEncoding = null;
    	this.defaultResponseCharacterEncoding = null;
    	this.defaultContentType = null;
    	this.fileUploadMaxSizeBytes = 0;
    	
    	this.jspAdapter = null;
    	this.ajaxAdapter = null;
    	this.simpleResourceAdapter = null;
    	this.includePageAdapter = null;
    }
    
    @Override
    public void init(FilterConfig config) throws ServletException {
		try {
			this.applicationContext = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext()); //Spring context
			String tmp;
			
			//this.sqlErrorCodeTranslator = new SQLErrorCodeTranslator();
			this.actionParamName = ((tmp = config.getInitParameter(BackingBeanHelper.BACKING_BEAN_ACTION_KEY)) == null) ? BackingBeanHelper.DEFAULT_ACTION_KEY : tmp;
			this.forwardPrefixIndicator = ((tmp = config.getInitParameter(BackingBeanHelper.BACKING_BEAN_FORWARD_PREFIX_INDICATOR)) == null) ? BackingBeanHelper.DEFAULT_FORWARD_PREFIX_INDICATOR : tmp;
			this.redirectPrefixIndicator = ((tmp = config.getInitParameter(BackingBeanHelper.BACKING_BEAN_REDIRECT_PREFIX_INDICATOR)) == null) ? BackingBeanHelper.DEFAULT_REDIRECT_PREFIX_INDICATOR : tmp;
			this.uriCharEncoding = ((tmp = config.getInitParameter(BackingBeanHelper.URI_CHARACTER_ENCODING)) == null) ? RequestParameterHelper.DEFAULT_APSERVER_URI_ENCODING : tmp;
			this.requestCharacterEncoding = ((tmp = config.getInitParameter(BackingBeanHelper.REQUEST_CHARACTER_ENCODING)) == null) ? RequestParameterHelper.DEFAULT_REQUEST_CHARACTER_ENCODING : tmp;
			this.defaultResponseCharacterEncoding = ((tmp = config.getInitParameter(BackingBeanHelper.RESPONSE_CHARACTER_ENCODING_KEY)) == null) ? BackingBeanHelper.DEFAULT_RESPONSE_CHARACTER_ENCODING : tmp;
			this.defaultContentType = ((tmp = config.getInitParameter(BackingBeanHelper.RESPONSE_CONTENT_TYPE_KEY)) == null) ? BackingBeanHelper.DEFAULT_RESPONSE_CONTENT_TYPE : tmp;
			this.fileUploadMaxSizeBytes = ((tmp = config.getInitParameter(BackingBeanHelper.FILE_UPLOAD_MAX_SIZE_BYTES)) == null) ? BackingBeanHelper.DEFAULT_FILE_UPLOAD_MAX_SIZE_BYTES : Long.parseLong(tmp);
		} catch(Exception e) {
			log.error(e.getMessage(), e);
			throw new ServletException(e.getMessage(), e);
		}
    }
    
    //以 servlet path 之第 1 個句號字元 "." 及其後的英文單字("/" 字元之前) 作為識別處理 adapter 物件的關鍵字, 如 ".jsp", ".ajax", ".resource", ".inc" 等
	@Override
    public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest)req; //for Servlet 環境 (非 Portlet 環境)
        final HttpServletResponse response = (HttpServletResponse)res;
        
		//即使網址未指定 index.jsp 也能顯示出來
		//不含 context path. filter path: /* => 型式: /.../xxx.jsp/... 或 /.../xxx.ajax/..., /.../xxx.resource/... 等
		final String path = request.getServletPath();
        
        if(bypass(path)) {
        	chain.doFilter(req, res);
        } else {
        	final BeanPathInfo beanPathInfo = getBeanPathInfo(path); //由 URI 中首次出現的 ".xxx" 定出數個區段
        	if(beanPathInfo == null) { //bypass 分不出區段的 request URI
        		chain.doFilter(req, res);
        	} else {
	            switch(beanPathInfo.indicator) {
	            	case BackingBeanHelper.DEFAULT_JSP_URI_SUFIX: { //要不要繼續 filter chain 下去由 JspAdapter 內部決定
	            		final RequestWrapper req2 = new RequestWrapper(request);
	            		final ResponseWrapper res2 = new ResponseWrapper(response);
	            		getJspAdapter().execute(req2, res2, chain, beanPathInfo.beanPath);
	            		if(!req2.isForwarded() && !res2.isRedirected()) //沒有直達目標 .jsp 者, chain 到此為止
	            			chain.doFilter(req, res);
	            		break;
	            	}
	            	case BackingBeanHelper.DEFAULT_AJAX_URI_SUFIX: { //(filter chain 到此為止)
	            		getAjaxAdapter().execute(request, response, beanPathInfo.beanPath);
	            		break;
	            	}
	            	case BackingBeanHelper.DEFAULT_SIMPLE_RESOURCE_URI_SUFIX: { //(filter chain 到此為止)
	            		//TODO: 只有此種網址帶 pathInfo, 會讓本 filter 不得不對應至網址 "/*" (過於泛濫), 宜另獨立以 servlet 處理
	            		final RequestWrapper req2 = new RequestWrapper(request, true); //RESTful server side 永達無狀態 => 讓 request.getSession() 氶為 null 
	            		getSimpleResourceAdapter().execute(req2, response, beanPathInfo.beanPath, beanPathInfo.pathInfo); 
	            		break;
	            	}
	            	case BackingBeanHelper.DEFAULT_INCLUDE_PAGE_URI_SUFIX: { //(filter chain 到此為止)
	            		getIncludePageAdapter().execute(request, response, beanPathInfo.beanPath);
	            		break;
	            	}
	            	default: { //bypass 其他不用轉接 adapter 的 request URI
	            		log.trace("bypass {}", path); 
	            		chain.doFilter(req, res);
	            	}
	            }
        	}
        }
    }
    
	//以 URI 中 ".xxx" 為分界等, 劃分三部分(分界符之前屬 beanPath, 之後屬 pathInfo), 找嘸分界符者傳回 null. 暫在此 *訂死規則*:
	//1. URI=/*.jsp (分界符 ".jsp", 無 pathInfo 部位), ex: /a.jsp
	//2. URI=/*.ajax (分界符 ".ajax", 無 pathInfo 部位), ex: /a.ajax
	//3. URI=/*.inc (分界符 ".inc", 無 pathInfo 部位), ex: /a.inc
	//4. URI=/*.resource/* (分界符 ".resource", 其後均屬 pathInfo(必有)), ex: /a.resource/employee 或 /a.resource/employee/number/3
	private BeanPathInfo getBeanPathInfo(final String uri) {
		if(uri == null)
			return null;

		String indicator; //part (1)分界符 (.jsp 或 .ajax 或 .resource 或 .inc 等)
		int start = -1; //indicator 字串起始 index
		int end = -1; //indicator 後接的字元 "/" 之 index
		
		//uri: 不含 context-path
		if((uri.endsWith(indicator = BackingBeanHelper.DEFAULT_JSP_URI_SUFIX) && (start = (uri.length() - indicator.length())) > 1) || //先比對 uri 樣式: /*.jsp, /*.ajax, /*.inc 
				(uri.endsWith(indicator = BackingBeanHelper.DEFAULT_AJAX_URI_SUFIX) && (start = (uri.length() - indicator.length())) > 1) || 
				(uri.endsWith(indicator = BackingBeanHelper.DEFAULT_INCLUDE_PAGE_URI_SUFIX) && (start = (uri.length() - indicator.length())) > 1)) {
			end = -1;
		} else if((start = uri.indexOf(indicator = BackingBeanHelper.DEFAULT_SIMPLE_RESOURCE_URI_SUFIX)) > 1) { //次比對 uri 樣式: /*.resource/*
			//確定當前找到的 indicator 後接 "/xxx" 字串(ex: /a.resource/employee 或 /a.resource/employee/number/3 )
			while((uri.length() - (end = start + indicator.length())) > 1) { //pathInfo 長度 >= 2 (ex: "/xxx")
				if(uri.charAt(end) == '/') //符合條件, 不用再往下找了
					break;
				
				if((start = uri.indexOf(indicator, end)) < 2) //往下找嘸符合條件的了, 不用再找了
					break;
			}
		}
		
		if(start < 2) //uri 開頭字元一定是 "/", indicator 開始位置一定是 start >= 2 (ex: /a.jsp)
			return null;
		
		final String beanPath = uri.substring(0, start); //part (2)
		final String pathInfo = (end == -1) ? null : uri.substring(end); //part (3)
		
		return new BeanPathInfo(indicator, beanPath, pathInfo);
	}

	//略過既定特殊網址型式
	private boolean bypass(final String uri) {
		if(uri == null || uri.length() == 0)
			return true;
		for(int i = 0; i < BYPASS_URI_PREFIX.length; i++) {
			if(uri.length() >= BYPASS_URI_PREFIX[i].length() && StrUtil.startWithIgnoreCase(uri, BYPASS_URI_PREFIX[i]))
				return true;
		}
		return false;
	}
	
	private JspAdapter getJspAdapter() {
		if(this.jspAdapter == null) {
			final JspAdapter adapter = new JspAdapter();
			adapter.setApplicationContext(this.applicationContext);
			adapter.setActionParamName(this.actionParamName);
			adapter.setForwardPrefixIndicator(this.forwardPrefixIndicator);
			adapter.setRedirectPrefixIndicator(this.redirectPrefixIndicator);
			adapter.setUriCharEncoding(this.uriCharEncoding);
			adapter.setRequestCharacterEncoding(this.requestCharacterEncoding);
			adapter.setFileUploadMaxSizeBytes(this.fileUploadMaxSizeBytes);
			log.info("prepared: {}", adapter);
			this.jspAdapter = adapter;
		}
		return this.jspAdapter;
	}
	
	private AjaxAdapter getAjaxAdapter() {
		if(this.ajaxAdapter == null) {
			final AjaxAdapter adapter = new AjaxAdapter();
			adapter.setApplicationContext(this.applicationContext);
			adapter.setActionParamName(this.actionParamName);
			adapter.setDefaultResponseCharacterEncoding(this.defaultResponseCharacterEncoding);
			adapter.setDefaultContentType(this.defaultContentType);
			adapter.setUriCharEncoding(this.uriCharEncoding);
			adapter.setRequestCharacterEncoding(this.requestCharacterEncoding);
			adapter.setFileUploadMaxSizeBytes(this.fileUploadMaxSizeBytes);
			log.info("prepared: {}", adapter);
			this.ajaxAdapter = adapter;
		}
		return this.ajaxAdapter;
	}
	
	private SimpleResourceAdapter getSimpleResourceAdapter() {
		if(this.simpleResourceAdapter == null) {
			final SimpleResourceAdapter adapter = new SimpleResourceAdapter();
			adapter.setApplicationContext(this.applicationContext);
			adapter.setDefaultContentType(this.defaultContentType);
			adapter.setUriCharEncoding(this.uriCharEncoding);
			adapter.setDefaultResponseCharacterEncoding(this.defaultResponseCharacterEncoding);
			adapter.setRequestCharacterEncoding(this.requestCharacterEncoding);
			adapter.setFileUploadMaxSizeBytes(this.fileUploadMaxSizeBytes);
			log.info("prepared: {}", adapter);
			this.simpleResourceAdapter = adapter;
		}
		return this.simpleResourceAdapter;
	}
	
	private IncludePageAdapter getIncludePageAdapter() {
		if(this.includePageAdapter == null) {
			final IncludePageAdapter adapter = new IncludePageAdapter();
			adapter.setApplicationContext(this.applicationContext);
			adapter.setActionParamName(this.actionParamName);
			adapter.setUriCharEncoding(this.uriCharEncoding);
			adapter.setRequestCharacterEncoding(this.requestCharacterEncoding);
			adapter.setForwardPrefixIndicator(this.forwardPrefixIndicator);
			log.info("prepared: {}", adapter);
			this.includePageAdapter = adapter;
		}
		return this.includePageAdapter;
	}
	
	private static class BeanPathInfo { //以 URI: /.../xxxxx.ajax/.../... 為例 (不含 context path)
		public final String indicator; //  => URI 之 ".ajax" 部分, 表明 URI 是屬於 JSP/AJAX/RESTful/include 等用途
		public final String beanPath; //  => URI 之 indicator 之前的 "/.../xxxxx" 部分
		public final String pathInfo; //  => URI 之 indicator 之後的 "/.../..." 部分
		
		public BeanPathInfo(String indicator, String beanPath, String pathInfo) {
			this.indicator = indicator;
			this.beanPath = beanPath;
			this.pathInfo = pathInfo;
		}
	}
}
