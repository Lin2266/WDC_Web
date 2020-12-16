package com.test.commons.web.internal;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.test.commons.annotation.Rest;
import com.test.commons.exception.HttpServerException;
import com.test.commons.util.ExceptionUtil;
import com.test.commons.util.JspUtil;
import com.test.commons.util.MsgUtil;

/**
 * 負責處理非 JSP 畫面(由 BackingBeanFilter 負責) - 或者說非在 server 端構成 - 的各種畫面(例: 行動裝置上的畫面)的 backing bean.
 * <ul>
 *     <li>有限度支援 REST 模式:
 *         <ul>
 *             <li>本 servlet 使用的 URI 型式:<br>
 *                     <code>http://...../CONTEXT_PATH/.../demo002050.resource/RESOURCE_NAME</code>
 *             <li>本 servlet 儘量地把 server 端任何結果傳回 client 端, 不以 HTTP 狀態碼表達 server 端的異常
 *                 (本 servlet 之任何異常仍回應 HTTP status 500)
 *         </ul>
 *     <li>server 端不 forward/redirect page, 純粹只接收參數, 送出 plain-text/JSON/XML 字串
 *     <li>基於 REST 的 bean scope 固定為 singleton scope
 *     <li>BackingBean annotation 之 id, path 二者之組合不可重複
 *     <li>使用 Rest annotation 修飾的 method, annotation 之 (name, method) 二屬性組合不能重覆
 *     <li>字元編碼及 content-type 的議題:
 *         <ul>
 *             <li>預設 response content-type 為 text/plain, 但可利用本 servlet 之初始參數 RESPONSE_CONTENT_TYPE 改變之
 *             <li>不論是 GET/POST/PUT/DELETE/... request, request 參數之字元編碼一概受 ServletRequest.setCharacterEncoding() 的控制, 
 *                 以 URF-8 為預設, 但可利用 servlet 初始參數 REQUEST_CHARACTER_ENCODING 改變之
 *             <li>response 字元編碼預設為 UTF-8 (即 backing bean 之 action 的傳回值), 但也可利用本 servlet 之初始參數 RESPONSE_CHARACTER_ENCODING 改變之
 *             <li>URI 字元編碼按 HTML 4.0 標準, 預設為 ISO-8859-1, 必要時也能利用 servlet 初始參數 URI_CHARACTER_ENCODING 改變之
 *         </ul>
 * </u> 
 * @deprecated 網址可能含 pathInfo, 使得 dispatcher filter 須對應至網址 /*. 宜另獨立以 servlet 處理
 */
@Deprecated
public class SimpleResourceAdapter {
	private static final Logger log = LoggerFactory.getLogger(SimpleResourceAdapter.class);
	private ApplicationContext applicationContext;
	private String defaultContentType;
	private String uriCharEncoding;
	private String defaultResponseCharacterEncoding;
	private String requestCharacterEncoding;
	private long fileUploadMaxSizeBytes;
	
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public String getDefaultContentType() {
		return defaultContentType;
	}

	public void setDefaultContentType(String defaultContentType) {
		this.defaultContentType = defaultContentType;
	}

	public String getUriCharEncoding() {
		return uriCharEncoding;
	}

	public void setUriCharEncoding(String uriCharEncoding) {
		this.uriCharEncoding = uriCharEncoding;
	}

	public String getDefaultResponseCharacterEncoding() {
		return defaultResponseCharacterEncoding;
	}

	public void setDefaultResponseCharacterEncoding(String defaultResponseCharacterEncoding) {
		this.defaultResponseCharacterEncoding = defaultResponseCharacterEncoding;
	}

	public String getRequestCharacterEncoding() {
		return requestCharacterEncoding;
	}

	public void setRequestCharacterEncoding(String requestCharacterEncoding) {
		this.requestCharacterEncoding = requestCharacterEncoding;
	}

	public long getFileUploadMaxSizeBytes() {
		return fileUploadMaxSizeBytes;
	}

	public void setFileUploadMaxSizeBytes(long fileUploadMaxSizeBytes) {
		this.fileUploadMaxSizeBytes = fileUploadMaxSizeBytes;
	}

	//@param beanPath 不含 ".resource"
	//@param pathInfo URI 之 ".resource" 之後的字串(取第一個 word (不含 "/") 作為 endpoint name)
	public void execute(final HttpServletRequest request, final HttpServletResponse response, 
			final String beanPath, final String pathInfo) throws ServletException, IOException {
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
			
			if(log.isDebugEnabled()) {
				//例: http://.../demo2/.../demo002050.resource/xxx
                log.debug("[{}{} {} {}] {}{}?{}", 
                		request.getMethod(), paramHelper.isMultiPartRequest() ? " MULTI-PART" : "", 
        				request.getRemoteAddr(), MsgUtil.retrieveLocaleString(request.getLocale()), 
        				request.getContextPath(), request.getServletPath(), 
        				paramHelper.toQueryStringForDebugging(true));
            }
			
			//取 bean 物件
			final String endpointName = getEndpointName(pathInfo);
			final Object bean = this.applicationContext.getBean(beanPath); //已在 BeanParameterMapper 被限制 backing bean for REST 只能使用 singleton scope 了
			final BeanParameterMapper.ClassDescriptor classDesc = BeanParameterMapper.getBeanDescriptor(bean.getClass()); //TODO: pathInfo 扣除開頭 endpoint name 後的字串, 尚未利用來傳遞 resource 參數
			
			//尋找欲呼叫的 bean method
			Object ret = null; //action return value
			Rest actionAnnotation = null; //標注於 bean method 的 annotation
			for(int i = 0; i < classDesc.methodDescs.length; i++) {
				final BeanParameterMapper.ActionMethodDescriptor md = classDesc.methodDescs[i];
				if(md.invokeType == BeanParameterMapper.INVOKE_AS_REST) {
					final Rest ann = (Rest)md.actionAnnotation;
					if(ann.name().equals(endpointName) && (ann.method().equals(requestMethod) || ann.method().equals(Rest.METHOD_ANY))) {
						
						actionAnnotation = ann;
						final Map<String, String[]> parameterValues = md.usePayload ? 
								paramHelper.getParameterMapFromQueryString() : paramHelper.getParameterMap();
						
						//BeanParameterMapper.populate(bean, parameterValues, classDesc); //規定: backing bean 不透過 bean property 傳入 request parameter
						//不操作 bean properties => 不考慮多 request 同步問題
						ret = BeanParameterMapper.invokeAction(request, response, bean, classDesc, md, parameterValues, (OutputWrapper)null, BeanParameterMapper.INVOKE_AS_REST);
						break;
					}
				}
			}
			if(actionAnnotation == null)
				throw new HttpServerException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
						"resource: \"HTTP " + requestMethod + " " + beanPath + BackingBeanHelper.DEFAULT_SIMPLE_RESOURCE_URI_SUFIX + "/" + endpointName + "\" not found");
			
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
		} finally {
			//REST 元件理應是無狀態的: 已於 BeanParameterMapper 限定 backing bean for REST 須為 singleton scope
			final HttpSession session = request.getSession(false); //依據 Servlet SPEC, session 不能避免, 除非在本 servlet URL 再配合一 filter, 先把 HttpServletRequest 再包裝為 HttpServletRequestWrapper, 讓 request.getSession() 傳回 null
			if(session != null)
				session.invalidate();
		}
	}
	
	@Override
	public String toString() {
		return "SimpleResourceAdapter [applicationContext=" + applicationContext + ", defaultContentType=" + defaultContentType +
				", uriCharEncoding=" + uriCharEncoding + ", defaultResponseCharacterEncoding=" + defaultResponseCharacterEncoding +
				", requestCharacterEncoding=" + requestCharacterEncoding + ", fileUploadMaxSizeBytes=" + fileUploadMaxSizeBytes + "]";
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
	
	//以 pathInfo 之由 "/" 分隔的第一個 word 作為 resource endpoint name (之後的部分皆視為參數)
	private String getEndpointName(final String pathInfo) {
		if(pathInfo == null || pathInfo.length() < 2)
			return null;
		final int end = pathInfo.indexOf('/', 1); //pathInfo 以 "/" 開頭
		final String name = (end == -1) ? pathInfo.substring(1) : pathInfo.substring(1, end);
		return (name.length() == 0) ? null : name;
	}
}
