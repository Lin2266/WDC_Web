package com.test.commons.util.internal;

import java.io.*;
import java.util.*;

public class JSArray implements Serializable {
	/** 雙引號字元 */
	public static final char DOUBLE_QUOTE_CHAR = '"';
	/** 單引號字元 */
	public static final char SINGLE_QUOTE_CHAR = '\'';
	
	/** 選項: 使用雙引號(default) */
	public static final int USE_DOUBLE_QUOTE = 0;
	/** 選項: 使用單引號 */
	public static final int USE_SINGLE_QUOTE = 2;
	
	private List<Object> container;
	private char quote;
	
	public JSArray() {
		this.container = new ArrayList<Object>();
		this.quote = DOUBLE_QUOTE_CHAR;
	}
	
	public JSArray(int options) {
		this();
		this.quote = ((options & 2) == 0) ? DOUBLE_QUOTE_CHAR : SINGLE_QUOTE_CHAR;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//add()
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	/** 替 JSArray 增加 JSObject 成員, value 為 null 者, 轉為 Javascript 的 null 值. */
	public JSArray add(JSObject value) {
		this.container.add(value);
		return this;
	}
	
	/** 替 JSArray 增加 JSArray 成員, value 為 null 者, 轉為 Javascript 的 null 值. */
	public JSArray add(JSArray value) {
		this.container.add(value);
		return this;
	}
	
	/** 替 JSArray 增加 String 成員, value 為 null 者, 轉為 Javascript 的 null 值. */
	public JSArray add(String value) {
		this.container.add((value == null) ? (String)null : (this.quote + value + this.quote)); //除以 JSObject/JSArray 表示的 value 外, 其餘型態的 value 預轉為 String
		return this;
	}
	
	/** 替 JSONArray 增加 Boolean 成員, value 為 null 者, 轉為 Javascript 的 null 值. */
	public JSArray add(Boolean value) {
		this.container.add((value == null) ? (String)null : value.toString()); //除以 JSObject/JSArray 表示的 value 外, 其餘型態的 value 預轉為 String
		return this;
	}
	
	/** 替 JSArray 增加 Character 成員, value 為 null 者, 轉為 Javascript 的 null 值. */
	public JSArray add(Character value) {
		this.container.add((value == null) ? (String)null : (this.quote + value.toString() + this.quote)); //除以 JSObject/JSArray 表示的 value 外, 其餘型態的 value 預轉為 String
		return this;
	}
	
	/** 替 JSArray 增加 Number/Byte/Float/Integer/Long/Short 成員, value 為 null 者, 轉為 Javascript 的 null 值. */
	public JSArray add(Number value) {
		this.container.add((value == null) ? (String)null : value.toString()); //除以 JSObject/JSArray 表示的 value 外, 其餘型態的 value 預轉為 String
		return this;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//addAsString()
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	/** 替 JSArray 增加 String 成員, value 為 null 者, 轉為 Javascript 的 null 值. */
	public JSArray addAsString(String value) {
		this.container.add(this.quote + ((value == null) ? "" : value) + this.quote);
		return this;
	}
	
	/** 替 JSONArray 增加 Boolean 成員, value 為 null 者, 轉為 Javascript 的 null 值. */
	public JSArray addAsString(Boolean value) {
		this.container.add((value == null) ? "" : value.toString());
		return this;
	}
	
	/** 替 JSArray 增加 Character 成員, value 為 null 者, 轉為 Javascript 的 null 值. */
	public JSArray addAsString(Character value) {
		this.container.add(this.quote + ((value == null) ? "" : value.toString()) + this.quote);
		return this;
	}
	
	/** 替 JSArray 增加 Number/Byte/Float/Integer/Long/Short 成員, value 為 null 者, 轉為 Javascript 的 null 值. */
	public JSArray addAsString(Number value) {
		this.container.add((value == null) ? "" : value.toString());
		return this;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//addFunction()
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 加入 JavaScript function 物件
	 * @param funcArgsAndBody function 的參數, 及 function 本體內容(不含外圍的大括弧)
	 */
	public JSArray addFunction(String ... funcArgsAndBody) {
		String[] args = funcArgsAndBody;
		if(args == null) {
			this.container.add(null);
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
		
		this.container.add(sb.toString());
		return this;
	}
	
	/** 將輸入的 JSArray 的成員化為本物件的一部分, null 者則不動作  */
	public JSArray addAll(JSArray values) {
		if(values != null)
			this.container.addAll(values.getContainer());
		return this;
	}
	
	public int length() {
		return this.container.size();
	}
	
	/** 成員可能為 JSObject, JSArray, Boolean, String, Number */
	public Iterator<Object> iterator() {
		return this.container.iterator();
	}
	
	/**
	 * 化為 JavaScript 物件的表示字串
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder().append("[");
		for(int i = 0, ii = this.container.size(), iii = ii - 1; i < ii; i++) {
			Object value = this.container.get(i);
			String value2 = (value == null) ? (String)null :
				(value instanceof String) ? (String)value : value.toString();
			sb.append(value2);
			if(i != iii)
				sb.append(",");
		}
		sb.append("]");
		return sb.toString();
	}
	
	List<Object> getContainer() {
		return this.container;
	}
}
