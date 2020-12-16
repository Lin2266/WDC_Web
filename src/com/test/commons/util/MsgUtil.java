package com.test.commons.util;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.test.commons.acl.IAccount;

/**
 * 取用訊息文字轉換 property 檔的簡易工具.
 * 基底取用一個 classpath 中存在原始 property 檔, 但該檔又可依不同語系及區域再衍生出相對應的其他 property 檔, 如:
 * <pre><code> messages.properties (原始的)
 * messages_zh_TW.properties (繁體中文)
 * messages_zh_CN.properties (簡體中文)
 * messages_en_US.properties (美語)
 * messages_de_DE.properties (德語)
 * messages_fr_FR.properties (法語)
 * messages_ja_JP.properties (日語)
 * messages_ko_KR.properties (韓語)
 * messages_ru_RU.properties (俄語)
 * ...
 * (主檔名附加的「語系_區域」字串, 語系碼統一小寫,  區域碼統一大寫)
 * </code></pre>
 * 其他語系的檔裡找不到訊息者, 回頭自原始的 property 檔取, 再無者, 直接傳回原查詢的字串.<br>
 * 本工具為求方便, 未以 JDK ResourceBoundle 為基底, locale 參數也強制一次指定「語系_區域」再附加至 property 檔的主檔名後. 
 * property 檔的檔名及內容比照 JDK ResourceBoundle 所規定的, 但內容固定為一般的 UTF-8 文字, 不需編碼為 ASCII.
 */
public class MsgUtil {
	private static final Logger log = LoggerFactory.getLogger(MsgUtil.class);
	
	/** 預設訊息轉換 property 檔的主檔名 */
	public static final String DEFAULT_MESSAGE_PROPERTY_NAME = "/messages";
	/** 訊息檔副檔名 */
	public static final String FILE_EXT_NAME = ".properties";
	private static String _baseName;
	private static ConcurrentHashMap<String, Properties> _cache = new ConcurrentHashMap<String, Properties>();

	private MsgUtil() {}
	
	/**
	 * 設定 property 檔的主檔名(含在 classpath 中的路徑, 不含語系, 區域碼, 副檔名固定為 properties).
	 * 只能被設定一次, 且要在 getBaseName() 第一次呼叫前.
	 * 當實際訊息 property 主檔名不為 DEFAULT_MESSAGE_PROPERTY_NAME 時才需設定.
	 * @param baseName
	 * @see #DEFAULT_MESSAGE_PROPERTY_NAME
	 */
	public static void setBaseName(String baseName) {
		if(_baseName != null)
			throw new IllegalStateException("this method can only be called once, and must be set before getBaseName() been firstly called");
		_baseName = baseName;
	}
	
	/** 取 LOCALE_REGION 或 LOCALE 字串 */
	public static String retrieveLocaleString(Locale locale) {
		String language = locale.getLanguage();
		String region = locale.getCountry(); //JDK Locale class 內部記為大寫
		StringBuilder ret = new StringBuilder().append(language);
		if(!"".equals(region))
			ret.append("_").append(region);
		return ret.toString();
	}
	
	/**
	 * 取訊息檔內的訊息文字.
	 * @param messageKey 訊息 property 檔內的 key, 如果最終訊息檔內都無對應的訊息文字者, 將直接傳回 messageKey 字串
	 * @param locale 主要是取用該參數之 language 及 country 屬性, null 值者表示取預設訊息.
	 * @param args 與 messageKey 所對應的訊息字串中所含的 {0}, {1}, ...{n} 所對應的值, 無者則指定為 null
	 */
	public static String message(String messageKey, Locale locale, Object ... args) {
		if(messageKey == null)
			return null;
		
		String message = null;
		if(locale == null) {
			message = getBoundle(null, null).getProperty(messageKey);
		} else {
			String language = locale.getLanguage();
			message = getBoundle(retrieveLocaleString(locale), language).getProperty(messageKey);
		}
		
		if(message == null)
			message = messageKey;
		if(args == null || args.length == 0)
			return message;
		return replacePlaceHoder(message, args);
	}
	
	/**
	 * 取訊息檔內的訊息文字.
	 * @param messageKey 訊息 property 檔內的 key, 如果最終訊息檔內都無對應的訊息文字者, 將直接傳回 messageKey 字串
	 * @param locale 主要是取用該參數之 language 及 country 屬性, null 值者表示取預設訊息.
	 */
	public static String message(String messageKey, Locale locale) {
		return message(messageKey, locale, (Object[])null);
	}
	
	/**
	 * 為方便於本 framework 在 HttpServlet 環境而設的 method. 
	 * 先嘗試由 session 中的 IAccount 物件之 locale 屬性判斷, 
	 * 無該資訊者再自 HttpServletRequest 判斷 client 之 locale;
	 * 如果在非 web 執行環境下被呼叫, 則直接取用預設訊息檔的內容.
	 * @see IAccount#KEY_IN_HTTP_SESSION
	 */
	public static String message(String messageKey, Object ... args) {
		HttpServletRequest request = JspUtil.getCurrentBackgroundHttpServletRequest();
		HttpSession session = request.getSession(true);
		if(session != null) { //在 HttpServlet 環境中
			IAccount account = (IAccount)session.getAttribute(IAccount.KEY_IN_HTTP_SESSION);
			if(account != null && account.getLocale() != null)
				return message(messageKey, account.getLocale(), args); //優先採用使用者 profile 的已設定值
		}
		return message(messageKey, request.getLocale(), args); //不在 HttpServlet 環境中者, 傳 null 代表取預設訊息
	}
	
	/**
	 * 為方便於本 framework 在 HttpServlet 環境而設的 method.
	 * 先嘗試由 session 中的 IAccount 物件之 locale 屬性判斷, 
	 * 無該資訊者再自 HttpServletRequest 判斷 client 之 locale;
	 * 如果在非 web 執行環境下被呼叫, 則直接取用預設訊息檔的內容.
	 * 訊息內容不作處理, 原樣送出.
	 * @see IAccount#KEY_IN_HTTP_SESSION
	 */
	public static String message(String messageKey) {
		return message(messageKey, (Object[])null);
	}
	
	static String getBaseName() {
		if(_baseName == null)
			_baseName = DEFAULT_MESSAGE_PROPERTY_NAME;
		return _baseName;
	}
	
	static String getBoundleFilePath(String locale) {
		StringBuilder ret = new StringBuilder().append(getBaseName());
		if(locale != null)
			ret.append("_").append(locale);
		return ret.append(FILE_EXT_NAME).toString();
	}
	
	static boolean loadProperties(Properties prop, String path) {
		Reader in = null;
		
		try {
			InputStream in2 = MsgUtil.class.getResourceAsStream(path);
			if(in2 == null)
				return false;
			
			in = new InputStreamReader(in2, "UTF-8"); //讀入時, value 位置的 "\{", "\}" 的反斜線會被吃掉, 要寫 "\\{", "\\}" 才能在讀入後成為 "\{", "\}"
			prop.load(in);
			in.close();
			in = null;
			
			if(log.isDebugEnabled())
				log.debug("load boundle file: " + path);
			return true;
		} catch(UnsupportedEncodingException e) {
			throw new IllegalStateException("encoding of the property file '" + path + "' is not UTF-8", e);
		} catch(IOException e) {
			throw new IllegalStateException("read file '" + path + "' error: " + e.getMessage(), e);
		} finally {
			if(in != null) try { in.close(); } catch(Throwable t) {}
		}
	}
	
	static Properties getBoundle(String locale, String language) {
		String key = (locale == null) ? "" : locale;
		Properties boundle = _cache.get(key);
		if(boundle != null)
			return boundle;

		//default boundle
		if(locale == null) {
			boundle = new Properties();
			if(!loadProperties(boundle, getBoundleFilePath(null))) //預設訊息 property 檔一定要存在
				throw new IllegalStateException("default message file '" + getBoundleFilePath(null) + "' not exist");
			_cache.putIfAbsent(key, boundle);
			return boundle;
		}
		
		boundle = new Properties(getBoundle(null, null)); //即使無與 locale 對應的 property 檔, 還是會傳回預設的
		if(!loadProperties(boundle, getBoundleFilePath(locale))) //完整的 "語系_區域" 找不到訊息檔者, 嘗試找尋只帶 "語系" 檔名的訊息檔
			loadProperties(boundle, getBoundleFilePath(language));
		_cache.putIfAbsent(key, boundle);
		return boundle;
	}

	//把 s 字串中的 {0}, {1}, ... 與 args 對應的部分取代掉
	static String replacePlaceHoder(String s, Object ... args) {
		int start = s.indexOf('{');
		if(start == -1)
			return s;
		
		//
		//TODO: 以下每遇一個 args 值就得從頭掃描 s 字串一次, 但是程式簡單
		//
		String[] placements = toStringArray(args);
		StringBuilder s2 = new StringBuilder().append(s);
		for(int i = 0; i < placements.length; i++) {
			String placeholder = "{" + i + "}";
			for(int j = 0, k = start; (j = s2.indexOf(placeholder, k)) != -1; ) {
				if(j != 0 && s2.charAt(j - 1) == '\\') { //placeholder 前面帶 \ 者, 略過
					k = j + placeholder.length();
				} else {
					s2.replace(j, j + placeholder.length(), placements[i]);
					k = j + placements[i].length();
				}
			}
		}
		
		//最後把 "\{" => "{", "\}" => "}"
		for(int j = s2.indexOf("\\{"); j != -1; ) {
			s2.replace(j, j + 2, "{");
			j = s2.indexOf("\\{", j + 1);
		}
		for(int j = s2.indexOf("\\}"); j != -1; ) {
			s2.replace(j, j + 2, "}");
			j = s2.indexOf("\\}", j + 1);
		}
		return s2.toString();
	}
	
	static String[] toStringArray(Object[] os) {
		String[] ret = new String[os.length];
		for(int i = 0; i < os.length; i++)
			ret[i] = (os[i] == null) ? "" : os[i].toString();
		return ret;
	}
	
	static int indexOf(String[] ar, String s) {
		for(int i = 0; i < ar.length; i++) {
			if(s.equals(ar[i]))
				return i;
		}
		return -1;
	}
}
