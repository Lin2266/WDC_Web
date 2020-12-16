package com.test.commons.util.internal;

import java.io.Serializable;
import java.util.*;

import com.test.commons.util.StrUtil;

/**
 * 封裝 JavaScript map.
 */
public class JSObject implements Serializable {
	/** 雙引號字元 */
	public static final char DOUBLE_QUOTE_CHAR = '"';
	/** 單引號字元 */
	public static final char SINGLE_QUOTE_CHAR = '\'';
	
	/** 選項: key 不以引號括起來(default) */
	public static final int KEY_WITHOUT_QUOTE = 0; //00, mask=01
	/** 選項: key 以引號括起來 */
	public static final int KEY_WITH_QUOTE = 1; //01, mask=01
	/** 選項: 使用雙引號(default) */
	public static final int USE_DOUBLE_QUOTE = 0; //00, mask=10
	/** 選項: 使用單引號 */
	public static final int USE_SINGLE_QUOTE = 2; //10, mask=10
	
	private Map<String, Object> container;
	
	private char quote;
	private boolean quoteKey;
	
	/**
	 * 預設: 使用雙引號作為引號字元, key 值不以引號括住.
	 */
	public JSObject() {
		this.container = new HashMap<String, Object>();
		this.quote = DOUBLE_QUOTE_CHAR;
		this.quoteKey = false;
	}
	
	/**
	 * @param options
	 */
	public JSObject(int options) {
		this();
		this.quote = ((options & 2) == 0) ? DOUBLE_QUOTE_CHAR : SINGLE_QUOTE_CHAR;
		this.quoteKey = ((options & 1) == 1);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//put()
	/////////////////////////////////////////////////////////////////////////////////////////////

	/** 替 JSObject 增加/修改 JSObject 屬性. value 為 null 者, 轉為 Javascript 的 null 值. */
	public JSObject put(String key, JSObject value) {
		this.container.put(key, value);
		return this;
	}
	
	/** 替 JSObject 增加/修改 JSArray 屬性. value 為 null 者, 轉為 Javascript 的 null 值. */
	public JSObject put(String key, JSArray value) {
		this.container.put(key, value);
		return this;
	}
	
	/** 替 JSObject 增加/修改 String 屬性. value 為 null 者, 轉為 Javascript 的 null 值. */
	public JSObject put(String key, String value) {
		this.container.put(key, (value == null) ? (String)null : (this.quote + escape(value) + this.quote)); //除以 JSObject/JSArray 表示的 value 外, 其餘型態的 value 預轉為 String
		return this;
	}
	
	/** 替 JSObject 增加/修改 Boolean 屬性. value 為 null 者, 轉為 Javascript 的 null 值. */
	public JSObject put(String key, Boolean value) {
		this.container.put(key, (value == null) ? (String)null : value.toString()); //除以 JSObject/JSArray 表示的 value 外, 其餘型態的 value 預轉為 String
		return this;
	}
	
	/** 替 JSObject 增加/修改 Character 屬性. value 為 null 者, 轉為 Javascript 的 null 值. */
	public JSObject put(String key, Character value) {
		this.container.put(key, (value == null) ? (String)null : (this.quote + escape(value.toString()) + this.quote)); //除以 JSObject/JSArray 表示的 value 外, 其餘型態的 value 預轉為 String
		return this;
	}
	
	/** 替 JSObject 增加/修改 Number/Byte/Float/Integer/Long/Short 屬性. value 為 null 者, 轉為 Javascript 的 null 值. */
	public JSObject put(String key, Number value) {
		this.container.put(key, (value == null) ? (String)null : value.toString()); //除以 JSObject/JSArray 表示的 value 外, 其餘型態的 value 預轉為 String
		return this;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//putAsString()
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	/** 替 JSObject 增加/修改 String 屬性, 一律轉為字串. value 為 null 者, 轉為 Javascript 空字串. */
	public JSObject putAsString(String key, String value) {
		this.container.put(key, this.quote + ((value == null) ? "" : escape(value)) + this.quote);
		return this;
	}
	
	/** 替 JSObject 增加/修改 Boolean 屬性, 一律轉為字串. value 為 null 者, 轉為 Javascript 空字串. */
	public JSObject putAsString(String key, Boolean value) {
		this.container.put(key, (value == null) ? "" : value.toString());
		return this;
	}
	
	/** 替 JSObject 增加/修改 Character 屬性, 一律轉為字串. value 為 null 者, 轉為 Javascript 空字串. */
	public JSObject putAsString(String key, Character value) {
		this.container.put(key, this.quote + ((value == null) ? "" : escape(value.toString())) + this.quote);
		return this;
	}
	
	/** 替 JSObject 增加/修改 Number/Byte/Float/Integer/Long/Short 屬性, 一律轉為字串. value 為 null 者, 轉為 Javascript 空字串. */
	public JSObject putAsString(String key, Number value) {
		this.container.put(key, (value == null) ? "" : value.toString());
		return this;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//putAsObject()
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	/** 替 JSObject 增加/修改 JavaScript Object 屬性. 輸入字串直接當成 JavaScript 的物件表示型式 */
	public JSObject putAsObject(String key, String value) {
		this.container.put(key, value);
		return this;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//putFunction()
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 加入 JavaScript function 物件
	 * @param functionName
	 * @param funcArgsAndBody function 的參數, 及 function 本體內容(不含外圍的大括弧)
	 */
	public JSObject putFunction(String functionName, String ... funcArgsAndBody) {
		String[] args = funcArgsAndBody;
		if(args == null) {
			this.container.put(functionName, null);
			return this;
		}
		
		StringBuilder sb = new StringBuilder().append("function(");
		
		//function 參數
		for(int i = 0, ii = args.length - 1, iii = ii - 1; i < ii; i++) {
			if(args[i] == null || "".equals(args[i]))
				throw new IllegalArgumentException("JavaScript argument can't be null or empty (index=" + i + ")");
			sb.append(args[i]);
			if(i != iii)
				sb.append(",");
		}
		
		//function 本體
		sb.append("){");
		if(args.length > 0) {
			String f = args[args.length - 1];
			if(f != null)
				sb.append(f);
		}
		sb.append("}");
		
		this.container.put(functionName, sb.toString());
		return this;
	}
	
	/**
	 * 化為 JavaScript 物件的表示字串
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder().append("{");
		for(String key : this.container.keySet()) {
			Object value = this.container.get(key);
			String value2 = (value == null) ? (String)null :
				(value instanceof String) ? (String)value : value.toString();
			sb.append(ky(key)).append(":").append(value2).append(",");
		}
		
		int lastIndex = sb.length() - 1;
		if(sb.charAt(lastIndex) == ',')
			sb.deleteCharAt(lastIndex);
		
		sb.append("}");
		return sb.toString();
	}

	//將特殊字元之前加上反斜線
	String escape(String value) {
		if(value == null)
			return null;
		if(this.quote == DOUBLE_QUOTE_CHAR)
			return StrUtil.escape(value, DOUBLE_QUOTE_CHAR, '\\');
		if(this.quote == SINGLE_QUOTE_CHAR)
			return StrUtil.escape(value, SINGLE_QUOTE_CHAR, '\\');
		return value;
	}
	
	String ky(String key) {
		if(key == null)
			return null;
		if(!this.quoteKey)
			return key;
		return this.quote + key + this.quote;
	}
}
