package com.test.commons.util;

import java.io.Serializable;

/**
 * 用以存放 "名稱 : 值" 型式資料的唯讀容器.
 */
@SuppressWarnings("serial")
public class NameValuePair<N, V> implements Serializable {
	private N name;
	private V value;

	public NameValuePair(N name, V value) {
		this.name = name;
		this.value = value;
	}

	public N getName() {
		return name;
	}

	public V getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "NameValuePair [name=" + name + ", value=" + value + "]";
	}
}
