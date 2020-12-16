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
import com.test.commons.util.StrUtil;
import com.test.commons.util.ValueHolder;

/**
 * 處理被以 AJAX 方式引入的畫面.
 * 依 servlet path 之結尾字串(如 ".inc")來作為本 servlet 之 url pattern.
 * <p/>
 * 本 adapter 並未處理導頁之間 bean scope 相關的事務, 所以本 adapter 不適合搭配獨立頁面, 只適用於被引入的頁面.
 */
public class IncludePageAdapter {
	private static final Logger log = LoggerFactory.getLogger(IncludePageAdapter.class);
	private ApplicationContext applicationContext;
    private String actionParamName;
    private String uriCharEncoding;
    private String requestCharacterEncoding;
    private String forwardPrefixIndicator;
    
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
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

	public String getRequestCharacterEncoding() {
		return requestCharacterEncoding;
	}

	public void setRequestCharacterEncoding(String requestCharacterEncoding) {
		this.requestCharacterEncoding = requestCharacterEncoding;
	}

	public String getForwardPrefixIndicator() {
		return forwardPrefixIndicator;
	}

	public void setForwardPrefixIndicator(String forwardPrefixIndicator) {
		this.forwardPrefixIndicator = forwardPrefixIndicator;
	}

	//@param beanPath 不含 ".resource"
	public void execute(final HttpServletRequest request, final HttpServletResponse response, final String beanPath)
			throws ServletException, IOException {
        String dest2 = null; //下一畫面的 URI
        final Class<?>[] notEnhancedClasses = new Class<?>[1]; //找出 backing bean 未被增強的 class. backing bean class 可能因為裡面被 cglib 作 enhance 處理, 須取得未 enhance 前的 class (TODO: 恐怕也有可能是 JDK1.5 以後的動態 proxy class)

        try {
            request.setCharacterEncoding(this.requestCharacterEncoding);
            final boolean hasBackingBean = this.applicationContext.containsBean(beanPath);
            RequestParameterHelper paramHelper = null;
            
            if(log.isDebugEnabled()) {
            	paramHelper = new RequestParameterHelper(request);
            	paramHelper.setUriEncoding(this.uriCharEncoding);
				paramHelper.setParamCharEncoding(this.requestCharacterEncoding);
				String queryString = paramHelper.toQueryStringForDebugging(true);
				StringBuilder msg = new StringBuilder().append("[").append(request.getMethod())
						.append(" ").append(request.getRemoteAddr())
						.append(" ").append(MsgUtil.retrieveLocaleString(request.getLocale()));
				if(hasBackingBean)
					msg.append(" action=").append(StrUtil.print(paramHelper.getParameter(this.actionParamName)));
				else
					msg.append(" no-backing-bean");
				msg.append("] ").append(request.getContextPath()).append(request.getServletPath()); //即使網址未指定 index.jsp 也能顯示出來
				if(queryString != null && queryString.length() != 0)
					msg.append("?").append(queryString);
                log.debug(msg.toString());
            }
            
            //目的畫面有配置 backing bean 者, 將 request 參數結合至 bean 屬性, 再呼叫 action method
            if(hasBackingBean) {
            	if(paramHelper == null) {
            		paramHelper = new RequestParameterHelper(request);
            		paramHelper.setUriEncoding(this.uriCharEncoding);
    				paramHelper.setParamCharEncoding(this.requestCharacterEncoding);
            	}
            	
                //找出 backing bean
            	final ValueHolder<Object> beanHolder = new ValueHolder<Object>();
            	final ValueHolder<BeanParameterMapper.ClassDescriptor> classDescHolder = new ValueHolder<BeanParameterMapper.ClassDescriptor>();
            	getBackingBean(request, beanPath, beanHolder, classDescHolder);
            	final Object bean = beanHolder.get();
                final BeanParameterMapper.ClassDescriptor classDesc = classDescHolder.get(); 
                
                //準備將 request 參數值 populate 到 backing bean 的屬性
                final Map<String, String[]> parameterMap = paramHelper.getParameterMap();

                //執行 action
                final String actionName = paramHelper.getParameter(this.actionParamName); //取 backing bean 上欲執行的 meghod name
                if(actionName != null && actionName.length() != 0) {
                	final BeanParameterMapper.ActionMethodDescriptor methodDesc = getActionMethodDescriptor(classDesc, actionName);

                    //將 request populate 至 bean 屬性並執行 action
                    final Object ret = populateAndExecuteBackingBean(request, response, bean, classDesc, methodDesc, parameterMap);
                    
                    if(ret instanceof String) //非 null 代表要導向別的 JSP 畫面
                        dest2 = (String)ret;
                }
                
                //執行關於 bean scope 相關議題的後處理
                //postHandleScope(request, bean, classDesc);
            }
        } catch(Throwable t) {
            handleException(request, t);
        }
        
        //導向其他畫面(forward) (要把當前 bean 值帶入下一頁者, 應使用 flash scope)
        if(dest2 != null) {
            if(dest2.startsWith(this.forwardPrefixIndicator)) { //forward:
                dest2 = dest2.substring(this.forwardPrefixIndicator.length()).trim(); //去掉開頭標示 forward 的字串
                request.getRequestDispatcher(dest2).forward(request, response);
            	log.debug("Forward to {}", dest2);
            } else { //backing bean 的 return 字串有誤者
                String msg = "Backing Bean [" + notEnhancedClasses[0].getName() + "]: for forwarding to the next page, return string must be prefixed with either 'forward:'.";
                log.error(msg);
                JspUtil.setMessage(msg);
            }
        } else {
        	dest2 = beanPath + BackingBeanHelper.DEFAULT_JSP_URI_SUFIX;
        	request.getRequestDispatcher(dest2).forward(request, response);
        	log.debug("Forward to {}", dest2);
        }
	}

	@Override
	public String toString() {
		return "IncludePageAdapter [applicationContext=" + applicationContext + ", actionParamName=" + actionParamName + ", uriCharEncoding=" +
				uriCharEncoding + ", requestCharacterEncoding=" + requestCharacterEncoding + ", forwardPrefixIndicator=" + forwardPrefixIndicator +
				"]";
	}
	
	private BeanParameterMapper.ActionMethodDescriptor getActionMethodDescriptor(final BeanParameterMapper.ClassDescriptor classDesc, final String methodName) 
    		throws IntrospectionException, NoSuchMethodException {
    	for(int i = 0; i < classDesc.methodDescs.length; i++) {
    		final BeanParameterMapper.ActionMethodDescriptor md = classDesc.methodDescs[i];
    		if(md.methodName.equals(methodName) && md.invokeType == BeanParameterMapper.INVOKE_AS_ACTION) //當作一般 JSP 畫面傳回
    			return md;
    	}
		throw new NoSuchMethodException("no such method: @" + AjaxAction.class.getSimpleName() + " " + methodName + "() of class " + classDesc.nonEnhancedClass.getName());
    }
    
    private String handleException(HttpServletRequest request, Throwable e) {
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
			final ValueHolder<Object> outBean, final ValueHolder<BeanParameterMapper.ClassDescriptor> outClassDesc) 
			throws IntrospectionException, NoSuchMethodException, SecurityException {
    	final Object bean = this.applicationContext.getBean(beanPath); //依據 alias name 來取 bean, 如果 bean scope=session 者, 此時目的 URI 的 bean 應已在 session 中了
    	outBean.set(bean);
    	outClassDesc.set(BeanParameterMapper.getBeanDescriptor(bean.getClass()));
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
}
