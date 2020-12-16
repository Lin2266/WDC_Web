package com.test.commons.web.internal;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class ResponseWrapper extends HttpServletResponseWrapper {
	private boolean isRedirected;
	
	public ResponseWrapper(HttpServletResponse response) {
		super(response);
	}
	
	public boolean isRedirected() {
		return this.isRedirected;
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		this.isRedirected = true;
		super.sendRedirect(location);
	}
}
