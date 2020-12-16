package com.test.commons.web.internal;

import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.test.commons.web.FlashScope;

public class FlashScopeImpl extends FlashScope {
	private Map<String, Object> current;
	private Map<String, Object> next;

	//以 getCurrentInstance(HttpServletRequest) 取得本 flashScope 物件
	private FlashScopeImpl() {}
	
	private Map<String, Object> getCurrent() {
		return (this.current == null) ? (this.current = new HashMap<String, Object>()) : this.current;
	}
	
	private Map<String, Object> getNext() {
		return (this.next == null) ? (this.next = new HashMap<String, Object>()) : this.next;
	}
	
	//以下實作 java.util.Map API

	/** 當前 flashScope 區資料筆數 */
	@Override
	public int size() {
		return getCurrent().size();
	}

	/** 當前 flashScope 區內容是否為空 */
	@Override
	public boolean isEmpty() {
		return getCurrent().isEmpty();
	}

	/** 當前 flashScope 區是否含使用者所指定的 key 資料 */
	@Override
	public boolean containsKey(Object key) {
		return getCurrent().containsKey(key);
	}

	/** 當前 flashScope 區是否含使用者所置入的值資料 */
	@Override
	public boolean containsValue(Object value) {
		return getCurrent().containsValue(value);
	}

	/** 取出當前 flashScope 區所存放的資料 */
	@Override
	public Object get(Object key) {
		return getCurrent().get(key);
	}

	/** 
	 * 放置將傳遞至下一頁的資料.
	 * @return put 資料之前原來的本物件所內含的 key 所對應到的值. 傳回 null 表示原本就沒此 key 或此 key 對應到 null 值 
	 */
	@Override
	public Object put(String key, Object value) {
		return getNext().put(key, value);
	}

	/** 移除當前 flashScope 區所內含的資料 */
	@Override
	public Object remove(Object key) {
		return getNext().remove(key);
	}

	/** 將資料置入欲送至下一頁的 flashScope 區 */
	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		getNext().putAll(m);
	}

	/** 清空欲送至下一頁的 flashScope 區 */
	@Override
	public void clear() {
		getNext().clear();
	}

	/** 取當前 flashScope 區所含的資料的 key */
	@Override
	public Set<String> keySet() {
		return getCurrent().keySet();
	}

	/** 取當前 flashScope 區所含的資料之值 */
	@Override
	public Collection<Object> values() {
		return getCurrent().values();
	}

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		return getCurrent().entrySet();
	}

	@Override
	public String toString() {
		return getCurrent().toString();
	}
	
	//以下實作 FlashScope 新增的 API
	
	@Override
	public void keep(String key) {
		getNext().put(key, getCurrent().get(key));
	}

	@Override
	public void putNow(String key, Object value) {
		getCurrent().put(key, value);
	}
	
	/** 在換頁時(含 redirect/page link/form submit)由 front-end controller 將上一頁所埋的資料, 轉成當前可讀取的狀態 */
	@Override
	public void doPrePhaseActions(HttpServletRequest request) {
		changeNextStateToCurrent();
	}
	
	/** 在換頁時(forward)由 front-end controller 將上一頁所埋的資料, 轉成當前可讀取的狀態 */
	@Override
	public void doPostPhaseActions(HttpServletRequest request) {
		changeNextStateToCurrent();
	}
	
	//以下本 class 獨有 API
	
	/** 取得 session 的當前 flashScope 物件 */
	public static FlashScope getCurrentInstance(HttpServletRequest request) {
		HttpSession session = request.getSession();
		FlashScope flash = (FlashScope)session.getAttribute(FlashScope.KEY_FLASH_SCOPE_IN_SESSION);
		if(flash == null)
			session.setAttribute(FlashScope.KEY_FLASH_SCOPE_IN_SESSION, flash = new FlashScopeImpl());
		return flash;
	}

	private void changeNextStateToCurrent() {
		if(this.current != null)
			this.current.clear();
		this.current = this.next;
		this.next = null;
	}
}
