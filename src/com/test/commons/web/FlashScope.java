package com.test.commons.web;

import java.io.Serializable;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * 仿 JSF 2(仿 Ruby on Rails) 用於裝載由當前頁傳遞至下一頁的資料.
 * 於 session 放置本物件, 其 key 為 "flashScope", 故使用者可於當前頁 backing bean 中呼叫:<pre>
 *   JspUtil.flashScope().put("previousBeanName", xxx);
 * </pre>
 * 然後於下一頁之 JSP 以 EL 取值, 如:<pre>
 *   ${flashScope.previousBeanName.xxx}
 * </pre>
 */
@SuppressWarnings("serial")
public abstract class FlashScope implements Map<String, Object>, Serializable {
	
	/** flash scope 物件在 session 中的 key (比照 JSF 2) */
    public final static String KEY_FLASH_SCOPE_IN_SESSION = "flashScope";

    /** 把先前以 {@link putNow(String, Object)} 置入當前 flashScope 區的資料, 推進預備送往下一頁的 flashScope 區 */
    public abstract void keep(String key);

    /** 對當前 flashScope 區放置資料, 以便在當前頁可立即取用 */
    public abstract void putNow(String key, Object value);
    
    /** 在換頁時(含 redirect/page link/form submit)由 front-end controller 將上一頁所埋的資料, 轉成當前可讀取的狀態 */
	public abstract void doPrePhaseActions(HttpServletRequest request);
	
	/** 在換頁時(forward)由 front-end controller 將上一頁所埋的資料, 轉成當前可讀取的狀態. */
	public abstract void doPostPhaseActions(HttpServletRequest request);
}
