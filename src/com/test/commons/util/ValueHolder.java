package com.test.commons.util;

/**
 * 供需要 pass by reference 傳值的場合之用, 內容為可變動的.
 */
public class ValueHolder<T> {
	private T value;

	public ValueHolder() {}
	
	public ValueHolder(T value) {
		this.value = value;
	}
	
	public T get() {
		return this.value;
	}
	
	public void set(T value) {
		this.value = value;
	}
}
