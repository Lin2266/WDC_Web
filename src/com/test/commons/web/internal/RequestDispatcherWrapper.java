package com.test.commons.web.internal;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class RequestDispatcherWrapper implements RequestDispatcher {
	private RequestDispatcher dispatcher;
	private boolean forwarded;
	
	public RequestDispatcherWrapper(RequestDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	public boolean isForwarded() {
		return this.forwarded;
	}
	
	@Override
	public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
		this.forwarded = true;
		this.dispatcher.forward(request, response);
	}

	@Override
	public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
		this.dispatcher.include(request, response);
	}
}
