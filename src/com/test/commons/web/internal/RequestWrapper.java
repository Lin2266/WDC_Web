package com.test.commons.web.internal;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

public class RequestWrapper extends HttpServletRequestWrapper {
	private RequestDispatcherWrapper requestDispatcher;
	private boolean stateless; //true: 令 request.getSession() 永傳回 null (for server side 無狀態的場合, 如 RESTful request)
	
	public RequestWrapper(HttpServletRequest request) {
		super(request);
	}
	
	/**
	 * @param request
	 * @param stateless 設為 true 時, 令 request.getSession() 永傳回 null (for server side 無狀態的場合, 如 RESTful request)
	 */
	public RequestWrapper(HttpServletRequest request, boolean stateless) {
		super(request);
		this.stateless  = stateless;
	}
	
	public boolean isForwarded() {
		return (this.requestDispatcher != null && this.requestDispatcher.isForwarded());
	}

	@Override
	public HttpSession getSession() {
		return this.stateless ? null : super.getSession();
	}

	@Override
	public HttpSession getSession(boolean create) {
		return this.stateless ? null : super.getSession(create);
	}

	@Override
	synchronized public RequestDispatcher getRequestDispatcher(String path) {
		return (this.requestDispatcher != null) ? this.requestDispatcher :
			(this.requestDispatcher = new RequestDispatcherWrapper(super.getRequestDispatcher(path)));
	}
}
