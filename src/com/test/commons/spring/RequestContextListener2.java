package com.test.commons.spring;

import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextListener;

/**
 * Servlet listener that exposes the request to the current thread.<br>
 * 供未使用 org.springframework.web.servlet.DispatcherServlet 的場合使用.
 */
public class RequestContextListener2 extends RequestContextListener {
	@Override
    public void requestInitialized(ServletRequestEvent requestEvent) {
		super.requestInitialized(requestEvent);
		JspUtil.setHttpServletRequest((HttpServletRequest)requestEvent.getServletRequest());
	}
	
	//for accessing protected methods
    private static class JspUtil extends com.test.commons.util.JspUtil {
    	public static void setHttpServletRequest(HttpServletRequest request) {
    		com.test.commons.util.JspUtil.setHttpServletRequest(request);
    	}
    }
}
