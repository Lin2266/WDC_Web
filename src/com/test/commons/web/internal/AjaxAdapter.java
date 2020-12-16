package com.test.commons.web.internal;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.test.commons.annotation.AjaxAction;
import com.test.commons.annotation.BackingBean;
import com.test.commons.util.JspUtil;
import com.test.commons.util.MsgUtil;
import com.test.commons.util.ValueHolder;

public class AjaxAdapter {
	private static final Logger log = LoggerFactory.getLogger(AjaxAdapter.class);
	
	private ApplicationContext applicationContext;
    private long fileUploadMaxSizeBytes;
    private String actionParamName;
    private String uriCharEncoding;
    private String defaultContentType;
    private String defaultResponseCharacterEncoding;
    private String requestCharacterEncoding;
    
    public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public long getFileUploadMaxSizeBytes() {
		return fileUploadMaxSizeBytes;
	}

	public void setFileUploadMaxSizeBytes(long fileUploadMaxSizeBytes) {
		this.fileUploadMaxSizeBytes = fileUploadMaxSizeBytes;
	}

	public String getActionParamName() {
		return actionParamName;
	}

	public void setActionParamName(String actionParamName) {
		this.actionParamName = actionParamName;
	}

	public String getUriCharEncoding() {
		return uriCharEncoding;
	}

	public void setUriCharEncoding(String uriCharEncoding) {
		this.uriCharEncoding = uriCharEncoding;
	}

	public String getDefaultContentType() {
		return defaultContentType;
	}

	public void setDefaultContentType(String defaultContentType) {
		this.defaultContentType = defaultContentType;
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

	//@param beanPath 不含 ".ajax"
	//@param pathInfo URI 之 ".ajax" 之後的字串(如果有的話)
	public void execute(final HttpServletRequest request, final HttpServletResponse response, final String beanPath) 
			throws ServletException, IOException {
		AjaxAction actionAnnotation = null;
    	
        try {
            //至 response.getWriter() 前才執行 response.setContentType()

            //包裹 request
        	request.setCharacterEncoding(this.requestCharacterEncoding);
            final RequestParameterHelper paramHelper = new RequestParameterHelper(request);
            paramHelper.setUriEncoding(this.uriCharEncoding);
            paramHelper.setParamCharEncoding(this.requestCharacterEncoding);
            if(paramHelper.isMultiPartRequest()) {
                paramHelper.setUploadFileMaxSize(this.fileUploadMaxSizeBytes);
                paramHelper.setUploadFileSaveDir(JspUtil.createUniqueTempRealPath());
            }
            
			//取 backing bean 上欲執行的 method name
			final String methodName = paramHelper.getParameter(this.actionParamName);
			if(methodName == null || methodName.length() == 0) {
				log.debug("No given action name yet.");
				return;
			}
            
            if(log.isDebugEnabled()) {
            	final String dest = request.getServletPath(); // "/.../xxx.ajax"
	            log.debug("[{}{} {} {} action={}] {}{}?{}",
	            		request.getMethod(), paramHelper.isMultiPartRequest() ? " MULTIPART" : "", request.getRemoteAddr(), MsgUtil.retrieveLocaleString(request.getLocale()), 
	    				methodName, request.getContextPath(), dest, paramHelper.toQueryStringForDebugging(true));
            }
            
            //目的畫面有配置 backing bean 者, 將 request 參數結合至 bean 屬性, 再呼叫 action method
            if(!this.applicationContext.containsBean(beanPath)) {
                String msg = "No backing bean registered in Spring as id=" + beanPath;
                log.error(msg);
                //response.setStatus(HttpServletResponse.SC_NOT_FOUND); //NOTE: 不設置 error status code, 讓錯誤訊息直接顯示於前端
                output(response, msg);
                return;
            }
            
            //取得 backing bean, nonEnhancedClasses, anns
            final ValueHolder<Object> beanHolder = new ValueHolder<Object>();
            final ValueHolder<BeanParameterMapper.ClassDescriptor> classDescHolder = new ValueHolder<BeanParameterMapper.ClassDescriptor>();
            getBackingBean(request, beanPath, beanHolder, classDescHolder);
            final Object bean = beanHolder.get();
            final BeanParameterMapper.ClassDescriptor classDesc = classDescHolder.get();
            
            //準備將 request 參數值 populate 到 backing bean 的屬性
            final Map<String, String[]> parameterMap = paramHelper.getParameterMap();

            //執行 action
            try {
            	final BeanParameterMapper.ActionMethodDescriptor methodDesc = getActionMethodDescriptor(classDesc, methodName);
                
                //取 action annotation
            	actionAnnotation = (AjaxAction)methodDesc.actionAnnotation;

                //populate request 參數並執行 action
                final Object ret = populateAndExecuteBackingBean(request, response, bean, classDesc, methodDesc, parameterMap);
                
                //執行關於 bean scope 相關議題的後處理
                //postHandleScope(request, bean, beanPath, classDesc);
                
                //回應訊息. 如果開發者在 backing-bean 內自行 print 訊息至前端, 優先處理之
                if(ret != null)
                    output(response, actionAnnotation, methodDesc.isReturnJSON, ret.toString());
            } catch(NoSuchMethodException ne) {
                log.error(ne.getMessage());
                //response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED); //NOTE: 不設置 error status code, 讓錯誤訊息直接顯示於前端
                output(response, ne.getMessage());
            }
            //log.debug("\nKEY_CURRENT_BACKING_BEAN_NAME: " + (String)request.getSession().getAttribute(BackingBeanHelper.KEY_CURRENT_BACKING_BEAN_NAME) +
            //		"\nKEY_VIEW_BEAN_NAME: " + (String)request.getSession().getAttribute(BackingBeanFilter.KEY_VIEW_BEAN_NAME) +
            //		"\nKEY_VIEW_BEAN_NAME_ALIAS: " + (String)request.getSession().getAttribute(BackingBeanFilter.KEY_VIEW_BEAN_NAME_ALIAS));
        } catch(Throwable e) { //捕捉任何其他 exception
            log.error(e.toString());
            //response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); //NOTE: 不設置 error status code, 讓錯誤訊息直接顯示於前端
            output(response, handleException(e));
        }
	}

	@Override
	public String toString() {
		return "AjaxAdapter [applicationContext=" + applicationContext + ", fileUploadMaxSizeBytes=" + fileUploadMaxSizeBytes + ", actionParamName=" +
				actionParamName + ", uriCharEncoding=" + uriCharEncoding + ", defaultContentType=" + defaultContentType +
				", defaultResponseCharacterEncoding=" + defaultResponseCharacterEncoding + ", requestCharacterEncoding=" + requestCharacterEncoding +
				"]";
	}

	private String getBeanPath(final String uri) {
    	if(uri.endsWith(BackingBeanHelper.DEFAULT_AJAX_URI_SUFIX))
    		return uri.substring(0, uri.length() - BackingBeanHelper.DEFAULT_AJAX_URI_SUFIX.length());
    	return uri;
    }
    
    private BeanParameterMapper.ActionMethodDescriptor getActionMethodDescriptor(final BeanParameterMapper.ClassDescriptor classDesc, final String methodName) 
    		throws IntrospectionException, NoSuchMethodException {
    	for(int i = 0; i < classDesc.methodDescs.length; i++) {
    		final BeanParameterMapper.ActionMethodDescriptor md = classDesc.methodDescs[i];
    		if(md.methodName.equals(methodName) && md.invokeType == BeanParameterMapper.INVOKE_AS_AJAX_ACTION)
    			return md;
    	}
		throw new NoSuchMethodException("no such method: @" + AjaxAction.class.getSimpleName() + " " + methodName + "() of class " + classDesc.nonEnhancedClass.getName());
    }
    
    private String handleException(final Throwable e) {
        //找出 exception 根源, 並將 SQL 訊息翻成中文訊息
        Throwable t = e, t2 = e;
        while((t = t.getCause()) != null)
            t2 = t;
        log.error("{}\n==> Root exception: {}\nStacktrace:\n", e, t2, e);

        String msg = t2.getMessage();
        if(msg == null || msg.length() == 0 || "null".equals(msg))
            msg = t2.toString();
        JspUtil.setMessage(msg);

        return msg;
    }
    
    private void getBackingBean(final HttpServletRequest request, final String beanPath, 
    		final ValueHolder<Object> outBean, final ValueHolder<BeanParameterMapper.ClassDescriptor> outClassDescs) 
			throws NoSuchMethodException, SecurityException, IntrospectionException {
    	final Object bean = this.applicationContext.getBean(beanPath); //依據 alias name 來取 bean, 如果 bean scope=session 者, 此時目的 URI 的 bean 應已在 session 中了
    	outBean.set(bean);
    	outClassDescs.set(BeanParameterMapper.getBeanDescriptor(bean.getClass()));
    }
    
    //將 request populate 至 bean 屬性並執行 action
    private Object populateAndExecuteBackingBean(final HttpServletRequest request, final HttpServletResponse response, 
    		final Object bean, final BeanParameterMapper.ClassDescriptor classDesc, 
    		final BeanParameterMapper.ActionMethodDescriptor methodDesc, final Map<String, String[]> parameterMap) 
    		throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, InstantiationException, SecurityException, IntrospectionException {
    	//if(parameterMap.size() != 0) //改統一透過 action method argument 傳遞, 不透過 bean 屬性
        //	BeanParameterMapper.populate(bean, parameterMap, classDesc);
    	return BeanParameterMapper.invokeAction(request, response, bean, classDesc, methodDesc, parameterMap, (OutputWrapper)null, BeanParameterMapper.INVOKE_AS_AJAX_ACTION);
    }

    //private void postHandleScope(final HttpServletRequest request, final Object bean, final String beanPath, 
    //		final BeanParameterMapper.ClassDescriptor classDesc) throws Exception {
    //}
    
	private void output(final HttpServletResponse response, final AjaxAction anno, final boolean isResponseJSON, 
			final String message) throws IOException {
		try {
			setResponseContentType(response, anno, isResponseJSON);
			response.getWriter().print(message);
		} catch(IllegalStateException ie) { //通常發生在 response.getWriter(), response.getOutputStream(), response.commit() 已呼叫過等情形
			log.error("original output message: '''{}''' before exception occurred ({})", message, ie);
		}
	}
	
	private void output(final HttpServletResponse response, final String message) throws IOException {
		try {
			response.setCharacterEncoding(this.defaultResponseCharacterEncoding);
			response.setContentType(BackingBeanHelper.DEFAULT_RESPONSE_TEXT_TYPE);
			response.getWriter().print(message);
		} catch(IllegalStateException ie) { //通常發生在 response.getWriter(), response.getOutputStream(), response.commit() 已呼叫過等情形
			log.error("original output message: '''{}''' before exception occurred ({})", message, ie);
		}
	}
    
    //private ServletOutputStream getOutputStream(final HttpServletResponse response, final AjaxAction anno) throws IOException {
    //	setResponseContentType(response, anno);
    //	return response.getOutputStream();
    //}
    
    private void setResponseContentType(final HttpServletResponse response, final AjaxAction anno, final boolean isResponseJSON) {
    	response.setCharacterEncoding(this.defaultResponseCharacterEncoding); //可能被稍後的 response.setContentType() 覆蓋

    	String contentType = null;
        if(anno == null || (contentType = anno.responseContentType()).length() == 0)
    		contentType = isResponseJSON ? BackingBeanHelper.DEFAULT_RESPONSE_JSON_TYPE : this.defaultContentType; //TODO: 如果 action method 傳回 JSON 型式的字串, 此處仍無法正確判別 content-type 為 "application/json"
        response.setContentType(contentType); //response.getWriter() 或 response.getOutputStream() 後這個就無效了
    }
}
