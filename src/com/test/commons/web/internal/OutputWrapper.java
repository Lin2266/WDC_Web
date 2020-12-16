package com.test.commons.web.internal;

import java.io.OutputStream;

import com.test.commons.util.ValueHolder;

/**
 * 用來針對 backing bean action method 之部分特殊 parameter 型態而設的包裹物件.
 * (ex: for DownloaderServlet)
 * @see DownloaderServlet
 */
public class OutputWrapper {
	private OutputStream outputStream;
	private ValueHolder<String> stringHolder;
	private ValueHolder<Long> longHolder;

	public OutputStream getOutputStream() {
		return outputStream;
	}

	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public ValueHolder<String> getStringHolder() {
		return stringHolder;
	}

	public void setStringHolder(ValueHolder<String> stringHolder) {
		this.stringHolder = stringHolder;
	}

	public ValueHolder<Long> getLongHolder() {
		return longHolder;
	}

	public void setLongHolder(ValueHolder<Long> longHolder) {
		this.longHolder = longHolder;
	}
}
