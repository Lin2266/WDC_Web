package com.test.commons.web;

import java.io.*;
import java.util.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.test.commons.annotation.Rest;
import com.test.commons.exception.HttpServerException;
import com.test.commons.util.ExceptionUtil;
import com.test.commons.util.JspUtil;
import com.test.commons.util.MsgUtil;
import com.test.commons.web.internal.BackingBeanHelper;
import com.test.commons.web.internal.BeanParameterMapper;
import com.test.commons.web.internal.OutputWrapper;
import com.test.commons.web.internal.RequestParameterHelper;
import com.test.commons.web.internal.RequestWrapper;

/**
 * 負責處理非 JSP 畫面(由 BackingBeanFilter 負責) - 或者說非在 server 端構成 - 的各種畫面(例: 行動裝置上的畫面)的 backing bean.
 * <ul>
 *     <li>有限度支援 REST 模式:
 *         <ul>
 *             <li>標準 REST resource 應是 stateless 的, 但 backing bean 則否(但也僅限於 request scope)
 *             <li>標準 REST 的 URI 要足以表示完整 resource, 但本 servlet 所套用的 URI 則只代表特定的功能或程式(以程式代碼區分網址)
 *             <li>標準 REST 可以在 URI path-info 內夾帶參數(當然也可以放在 query string 及 payload 內), 
 *                 但本 servlet 的 request 參數, 則只能放在 query string, 或 payload 內
 *             <li>標準 REST 要能以 URI path 表達完整的 resource, 如:<br>
 *                     <code>http://...../CONTEXT_PATH/resource/xxxservice/RESOURCE_NAME</code><br>
 *                 本 servlet 使用的 URI 型式:<br>
 *                     <code>http://...../CONTEXT_PATH/.../demo002050/RESOURCE_NAME</code>
 *             <li>本 servlet 儘可能地把 server 端任何結果傳回 client 端, 而不以 HTTP 狀態碼表達 server 端的異常
 *                 (本 servlet 都是回應 HTTP status 500)
 *         </ul>
 *     <li>
 *     <li>本 servlet 所對應的資源, 可和 JSP 畫面共用同一 backing bean.<br/> 
 *         但 servlet 在執行 backing bean 完畢後, 會將 bean 物件由 Spring context 中卸除; JSP 畫面用的 backing bean 則依 bean 的 scope 設定.<br/>
 *         以上敘述是基於如此假設: 在瀏覽器畫面上, 不會連至同 backing bean 的 REST 網址.
 *     <li>server 端不 forward/redirect page, 純粹只接收參數, 送出 plain-text/JSON/XML 字串
 *     <li>基於 REST 的 bean scope 固定為 request scope
 *     <li>BackingBean annotation 之 id, path 仍舊互相不能重複
 *     <li>使用 Rest annotation 修飾的 method, annotation 之 (name, method) 屬性組合不能重覆
 *     <li>字元編碼及 content-type 的議題:
 *         <ul>
 *             <li>預設 response content-type 為 text/plain, 但可利用本 servlet 之初始參數 RESPONSE_CONTENT_TYPE 改變之
 *             <li>不論是 GET/POST/PUT/DELETE/... request, request 參數之字元編碼一概受 ServletRequest.setCharacterEncoding() 的控制, 
 *                 以 URF-8 為預設, 但可利用 servlet 初始參數 REQUEST_CHARACTER_ENCODING 改變之
 *             <li>response 字元編碼預設為 UTF-8 (即 backing bean 之 action 的傳回值), 但也可利用本 servlet 之初始參數 RESPONSE_CHARACTER_ENCODING 改變之
 *             <li>URI 字元編碼按 HTML 4.0 標準, 預設為 ISO-8859-1, 必要時也能利用 servlet 初始參數 URI_CHARACTER_ENCODING 改變之
 *         </ul>
 * </u> 
 */
@SuppressWarnings("serial")
public class SimpleResourceBackingBeanServlet extends HttpServlet {
	private static final Logger log = LoggerFactory.getLogger(SimpleResourceBackingBeanServlet.class);
	
    /** 置於 URI query string 中, 用來指定 action name 的參數名(for 在以 query string 中指定 action method 的場合) */
	
    //在 web.xml 中的參數名
    public static final String RESPONSE_CONTENT_TYPE = "RESPONSE_CONTENT_TYPE";
    public static final String RESPONSE_CHARACTER_ENCODING = "RESPONSE_CHARACTER_ENCODING";

    private ApplicationContext applicationContext;
    private String defaultContentType;
    private String uriCharEncoding;
    private String defaultResponseCharacterEncoding;
    private String requestCharacterEncoding;
    private long fileUploadMaxSizeBytes;
    
	@Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        try {
        	String tmp;
        	this.applicationContext = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        	
        	this.defaultContentType = ((tmp = config.getInitParameter(RESPONSE_CONTENT_TYPE)) == null) ? BackingBeanHelper.DEFAULT_RESPONSE_CONTENT_TYPE : tmp;
            this.uriCharEncoding = ((tmp = config.getInitParameter(BackingBeanHelper.URI_CHARACTER_ENCODING)) == null) ? RequestParameterHelper.DEFAULT_APSERVER_URI_ENCODING : tmp;
        	this.defaultResponseCharacterEncoding = ((tmp = config.getInitParameter(RESPONSE_CHARACTER_ENCODING)) == null) ? BackingBeanHelper.DEFAULT_RESPONSE_CHARACTER_ENCODING : tmp;
        	this.requestCharacterEncoding = ((tmp = config.getInitParameter(BackingBeanHelper.REQUEST_CHARACTER_ENCODING)) == null) ? RequestParameterHelper.DEFAULT_REQUEST_CHARACTER_ENCODING : tmp;
        	this.fileUploadMaxSizeBytes = ((tmp = config.getInitParameter(BackingBeanHelper.FILE_UPLOAD_MAX_SIZE_BYTES)) == null) ? BackingBeanHelper.DEFAULT_FILE_UPLOAD_MAX_SIZE_BYTES : Long.parseLong(tmp);
        	
        	log.info("\nSimpleResourceBackingBeanServlet parameter:" +
        			"\n  URI character encoding = {}" +
        			"\n  response output character encoding = {}" +
					"\n  response content type = {}" +
					"\n  request character encoding = {}" +
					"\n  max file upload size = {} bytes",
					this.uriCharEncoding, this.defaultResponseCharacterEncoding, this.defaultContentType, this.requestCharacterEncoding, this.fileUploadMaxSizeBytes);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            throw new ServletException(e.getMessage(), e);
        }
	}
	
	@Override
	public void destroy() {
		this.applicationContext = null;
		this.defaultContentType = null;
		this.uriCharEncoding = null;
		this.defaultResponseCharacterEncoding = null;
		this.requestCharacterEncoding = null;
		this.fileUploadMaxSizeBytes = 0;
	}
	
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			//至 response.getWriter() 前才執行 response.setContentType()
			
			request.setCharacterEncoding(this.requestCharacterEncoding);
			final String requestMethod = request.getMethod();
			final RequestParameterHelper paramHelper = new RequestParameterHelper(request);
			paramHelper.setUriEncoding(this.uriCharEncoding);
			paramHelper.setParamCharEncoding(this.requestCharacterEncoding);
			if(paramHelper.isMultiPartRequest()) {
                paramHelper.setUploadFileMaxSize(this.fileUploadMaxSizeBytes);
                paramHelper.setUploadFileSaveDir(JspUtil.createUniqueTempRealPath());
            }
			
			//取 backing bean id 及 action name 用的路徑資訊. 以下類 URI 模式為例:
			// http://.../demo2/resource/.../demo002050/xxx (servlet url pattern = /resource/*  屬正統 REST 所用)
			final String servletPath = request.getServletPath(); //=> /resource
			final String pathInfo = request.getPathInfo(); //=> /.../demo002050/xxx (自 servletPath 以下, query string 之前, 開頭含 "/")
			final String[] beanPathAndActionName = findBeanPathAndActionNameFromUriPath(servletPath, pathInfo); //[ bean path, action name ]
			if(log.isDebugEnabled()) {
				final String queryString = paramHelper.toQueryStringForDebugging(true);
                log.debug("[{}{} {} {}] {}{}{}{}",
                		request.getMethod(), 
                		paramHelper.isMultiPartRequest() ? " MULTI-PART" : "", 
        				request.getRemoteAddr(), 
        				MsgUtil.retrieveLocaleString(request.getLocale()), 
        				request.getContextPath(), 
        				servletPath, 
        				pathInfo, 
        				(queryString.length() == 0) ? "" : ("?" + queryString));
            }
			
			//取 bean 物件
			final String beanPath = beanPathAndActionName[0];
			final String actionName = beanPathAndActionName[1];
			final Object bean = this.applicationContext.getBean(beanPath); //已在 BeanParameterMapper 被限制 backing bean for REST 只能使用 singleton scope 了
			final BeanParameterMapper.ClassDescriptor classDesc = BeanParameterMapper.getBeanDescriptor(bean.getClass());
			
			//尋找欲呼叫的 bean method
			Object ret = null; //action return value
			Rest actionAnnotation = null; //標注於 bean method 的 annotation
			for(int i = 0; i < classDesc.methodDescs.length; i++) {
				final BeanParameterMapper.ActionMethodDescriptor md = classDesc.methodDescs[i];
				if(md.invokeType == BeanParameterMapper.INVOKE_AS_REST) {
					final Rest ann = (Rest)md.actionAnnotation;
					if(ann.name().equals(actionName) && (ann.method().equals(requestMethod) || ann.method().equals(Rest.METHOD_ANY))) {
						
						actionAnnotation = ann;
						final Map<String, String[]> parameterValues = md.usePayload ? 
								paramHelper.getParameterMapFromQueryString() : paramHelper.getParameterMap();
						
						//BeanParameterMapper.populate(bean, parameterValues, classDesc); //規定: backing bean 不透過 bean property 傳入 request parameter
						//不操作 bean properties => 不考慮多 request 同步問題
						final RequestWrapper req2 = new RequestWrapper(request, true); //RESTful server side 永達無狀態 => 讓 request.getSession() 氶為 null
						ret = BeanParameterMapper.invokeAction(req2, response, bean, classDesc, md, parameterValues, (OutputWrapper)null, BeanParameterMapper.INVOKE_AS_REST);
						break;
					}
				}
			}
			if(actionAnnotation == null)
				throw new HttpServerException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
						"resource: \"HTTP " + requestMethod + " " + servletPath + ((pathInfo == null) ? "" : pathInfo) + "\" not found");
			
			//回應訊息. 如果開發者在 backing-bean 內自行 print 訊息至前端, 優先處理之
			if(ret != null)
				getWriter(response, actionAnnotation).print((ret instanceof String) ? (String)ret : ret.toString());
			
		//} catch(HttpServerException e) {
		//	log.error(e.getMessage(), e);
		//	response.sendError(e.getStatusCode(), e.getMessage()); //這樣會抛出 ap server 的 error page
		} catch(Throwable e) { //捕捉任何其他 exception
			//response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, handleException(e)); //這樣會抛出 ap server 的 error page
			log.error(e.getMessage(), e);
			String msg = ExceptionUtil.getRootException(e).getMessage();
			
			try {
				getWriter(response, null).print(msg);
			} catch(IllegalStateException ie) { //可能已被先呼叫 response.getOutputStream() 了
				getOutputStream(response, null).write(msg.getBytes(this.defaultResponseCharacterEncoding)); //仍有可能有編碼的問題
			}
		}
	}
	
	//backing-bean 執行完 action 後才呼叫這個較有意義
	private PrintWriter getWriter(HttpServletResponse response, Rest anno) throws IOException {
    	setResponseContentType(response, anno);
        return response.getWriter();
    }
    
    //backing-bean 執行完 action 後才呼叫這個較有意義
	private ServletOutputStream getOutputStream(HttpServletResponse response, Rest anno) throws IOException {
    	setResponseContentType(response, anno);
        return response.getOutputStream();
	}
    
	private void setResponseContentType(HttpServletResponse response, Rest anno) {
    	response.setCharacterEncoding(this.defaultResponseCharacterEncoding); //可能被稍後的 response.setContentType() 覆蓋
    	
    	String contentType = null;
    	if(anno == null || (contentType = anno.responseContentType()).length() == 0)
    		contentType = this.defaultContentType; //如果裡面含 encoding 設定, 可能被稍後的 response.setCharacterEncoding(encoding) 覆蓋
        response.setContentType(contentType); //response.getWriter() 或 response.getOutputStream() 後這個就無效了
    }

    //自網址路徑(不含 query string) 撈取 backing bean id, 及 action name(如果是正統 REST 風格的網址的話, 否則為 null)
	private String[] findBeanPathAndActionNameFromUriPath(String servletPath, String pathInfo) {
    	//pattern:  /**/*/xxx  不含 query string, 以最後的 xxx 為 action name
    	int i = pathInfo.lastIndexOf('/');
    	return new String[] { pathInfo.substring(0, i), pathInfo.substring(i + 1) };
    }
}
