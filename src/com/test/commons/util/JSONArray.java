package com.test.commons.util;

import java.util.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.test.commons.exception.JSONException;

/**
 * 用來生成 JSON 陣列類型字串用的輔助容器, 成員限 JSONObject, JSONArray 物件, 
 * 或 null, Boolean, Number(含 Byte/Float/Integer/Long/Short/Decimal), String, 
 * Character(視同 String, 比照 JavaScript 無 char 型態).
 * <p>
 * depend on: google gson
 */
public class JSONArray {
    private JsonArray array; //成員僅限 JsonObject 物件
    
    public JSONArray() {
        this.array = new JsonArray();
    }
    
    /** 讀取 JSON 字串(限定 JavaScript array 型態, 不可為 null 或空字串)並解析化為 JSONArray 物件 */
    public JSONArray(String json) {
    	try {
    		this.array = new JsonParser().parse(json).getAsJsonArray();
    	} catch(JsonParseException je) {
    		throw new JSONException(je.getMessage(), new IllegalStateException(json)); //這裡的輸入字串極可能是呼叫者端所發生的 exception 訊息 -> 把輸入字串當成 exception 的根源
    	} catch(IllegalStateException ie) { //當輸入字串為 null 或 "" 時會走這裡
    		throw new JSONException(ie.getMessage(), new IllegalStateException(json)); //這裡的輸入字串極可能是呼叫者端所發生的 exception 訊息 -> 把輸入字串當成 exception 的根源
    	} catch(Throwable t) {
    		throw new JSONException(t.getMessage(), t);
    	}
    }
    
    JSONArray(JsonArray array) {
    	this.array = array;
    }
    
    JsonArray getArray() {
        return this.array;
    }
    
    /** 替 JSONArray 增加 JSONObject 成員, value 為 null 者, 轉為 Javascript 的 null 值. */
    public JSONArray add(JSONObject value) {
        getArray().add((value == null) ? JsonNull.INSTANCE : value.getContainedObject());
        return this;
    }
    
    /** 替 JSONArray 增加 JSONArray 成員, value 為 null 者, 轉為 Javascript 的 null 值. */
    public JSONArray add(JSONArray array) {
    	getArray().add((array == null) ? JsonNull.INSTANCE : array.getArray());
    	return this;
    }
    
    /** 替 JSONArray 增加 String 成員, value 為 null 者, 轉為 Javascript 的 null 值. */
    public JSONArray add(String value) {
    	JsonElement e = null;
    	if(value == null)
    		e = JsonNull.INSTANCE;
    	else 
    		e = new JsonPrimitive(value);
    	getArray().add(e);
    	return this;
    }
    
    /** 替 JSONArray 增加 Boolean 成員, value 為 null 者, 轉為 Javascript 的 null 值. */
    public JSONArray add(Boolean value) {
    	getArray().add((value == null) ? JsonNull.INSTANCE : new JsonPrimitive(value));
    	return this;
    }
    
    /** 替 JSONArray 增加 Number/Byte/Float/Integer/Long/Short/BigDecimal 成員, value 為 null 者, 轉為 Javascript 的 null 值. */
    public JSONArray add(Number value) {
    	getArray().add((value == null) ? JsonNull.INSTANCE : new JsonPrimitive(value));
    	return this;
    }
    
    /** 替 JSONArray 增加 String 成員(比照 JavaScript 並無 char 型態), value 為 null 者, 轉為 Javascript 的 null 值. */
    public JSONArray add(Character value) {
    	return add(String.valueOf(value));
    }
    
    /**
     * 取本物件成員.
     * @param i index, 由 0 起算
     * @return null 或 String 或 Number 或 Boolean 或 JSONObject 或 JSONArray 類型的物件
     */
    public Object get(int i) {
    	return JSONObject.tuneType(this.array.get(i));
    }
    
    /**
     * 取型態為 JSONObject 的成員
     * @param i 成員位置(自 0 起算)
     * @return 位置 i 的 JSONObject 型態成員, 該成員為 null 者則傳回 null
     */
    public JSONObject getAsJSONObject(int i) {
    	JsonElement e = this.array.get(i);
    	return (e == null) ? null : new JSONObject(e.getAsJsonObject());
    }
    
    /**
     * 取型態為 JSONArray 的成員
     * @param i 成員位置(自 0 起算)
     * @return 位置 i 的 JSONArray 型態成員, 該成員為 null 者則傳回 null
     */
    public JSONArray getAsJSONArray(int i) {
    	JsonElement e = this.array.get(i);
    	return (e == null) ? null : new JSONArray(e.getAsJsonArray());
    }
    
    /**
     * 取型態為 String 的成員
     * @param i 成員位置(自 0 起算)
     * @return 位置 i 的 String 型態成員, 該成員為 null 者則傳回 null
     */
    public String getAsString(int i) {
    	JsonElement e = this.array.get(i);
    	return (e == null) ? null : e.getAsString();
    }
    
    /**
     * 取型態為 Boolean 的成員
     * @param i 成員位置(自 0 起算)
     * @return 位置 i 的 Boolean 型態成員, 該成員為 null 者則傳回 null
     */
    public Boolean getAsBoolean(int i) {
    	JsonElement e = this.array.get(i);
    	return (e == null) ? null : e.getAsBoolean();
    }
    
    /**
     * 取型態為 Number 的成員
     * @param i 成員位置(自 0 起算)
     * @return 位置 i 的 Number 型態成員, 該成員為 null 者則傳回 null
     */
    public Number getAsNumber(int i) {
    	JsonElement e = this.array.get(i);
    	return (e == null) ? null : e.getAsNumber();
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////
    //addAsString()
    /////////////////////////////////////////////////////////////////////////////////////////////
    
    /** 替 JSONArray 增加 String 成員, value 為 null 者, 轉為 Javascript 空字串. */
    public JSONArray addAsString(String value) {
    	return add((value == null) ? "" : value);
    }
    
    /** 替 JSONArray 增加 Boolean 成員且一律轉為字串, value 為 null 者, 轉為 Javascript 空字串. */
    public JSONArray addAsString(Boolean value) {
		return add((value == null) ? "" : value.toString());
    }
    
    /** 替 JSONArray 增加 Number 成員且一律轉為字串, value 為 null 者, 轉為 Javascript 空字串. */
    public JSONArray addAsString(Number value) {
    	return add((value == null) ? "" : value.toString());
    }
    
    /** 替 JSONArray 增加 String 成員(比照 JavaScript 並無 char 型態), value 為 null 者, 轉為 Javascript 空字串. */
    public JSONArray addAsString(Character value) {
    	return add((value == null) ? "" : String.valueOf(value));
    }
    
    /** 將輸入的 JSONArray 的成員化為本物件的一部分, null 者則不動作 */
    public JSONArray addAll(JSONArray values) {
    	if(values != null && values.size() > 0)
    		getArray().addAll(values.getArray());
        return this;
    }
    
    /** 移除陣列中第 index 個元素(由 0 起算) */
    public JSONArray remove(final int index) {
    	getArray().remove(index);
    	return this; //原本應傳回被移除的元素, 但此處無法確定其實際型態(不傳回底層的 JsonElement (for gson) 物件), 故仍傳回本 JSONArray 物件本身
    }
    
    ///** 
    // * @deprecated 請改用 size()
    // * @see #size() 
    // */
    //@Deprecated
    //public int length() {
    //    return size();
    //}
    
    /** 本類 list 型態物件之第一層成員個數 */
    public int size() {
    	return getArray().size();
    }
    
    public Iterator<Object> iterator() {
        final Iterator<JsonElement> i = getArray().iterator();
        
        return new Iterator<Object>() {
            @Override
            public boolean hasNext() {
                return i.hasNext();
            }

            @Override
            public Object next() {
            	return JSONObject.tuneType((JsonElement)i.next());
            }

            @Override
            public void remove() {
                i.remove();
            }
        };
    }
    
    /** 轉出 JSON array 字串 */
    @Override
    public String toString() {
        return getArray().toString();
    }

    /**
     * 轉出 Java List 物件, 成員類型限定為 
     * Boolean, Number, String, Map&lt;String, Object&gt;, List&lt;String, Object&gt;
     * (Map, List 之值的型態亦限定為以上所列的型態).<br>
     * @return 對輸出的 list 作修改, 並不影響本 JSONArray 物件的內含資料.
     */
    public List<Object> toList() {
    	return _toList(this.array);
    }
    
    /**
     * 輔助轉出 JSON 字串的工具.
     * @param array 陣列物件中的值限定為 Boolean, Number/Byte/Float/Integer/Long/Short/BigDecimal, String, Character(視同 String), JSONObject, JSONArray,
     * 		也可以是 List(內含值的型態限制同前), Map(內含值的型態限制同前).
     */
    @SuppressWarnings("unchecked")
	public static JSONArray asJSONArray(Object ... array) {
    	if(array == null)
    		return null;
    	
    	JSONArray ar = new JSONArray();
    	for(Object o : array) {
    		if(o == null)
    			ar.add((String)null);
    		else if(o instanceof JSONObject)
    			ar.add((JSONObject)o);
    		else if(o instanceof JSONArray)
    			ar.add((JSONArray)o);
    		else if(o instanceof String)
    			ar.add((String)o);
    		else if(o instanceof Boolean)
    			ar.add((Boolean)o);
    		else if(o instanceof Number)
    			ar.add((Number)o);
    		else if(o instanceof Character) //JavaScript 無 char 型態
    			ar.add(String.valueOf(o));
    		else if(o instanceof List)
    			ar.add(asJSONArray(((List<Object>)o).toArray()));
    		else if(o instanceof Object[])
    			ar.add(asJSONArray((Object[])o));
    		else if(o instanceof Map) //!
    			ar.add(JSONObject.asJSONObject((Map<String, ?>)o));
    		else
    			ar.add(o.toString());
    	}
    	return ar;
    }
    
    //for primary type
    public static JSONArray asJSONArray(byte ... array) {
    	if(array == null)
    		return null;
    	JSONArray ret = new JSONArray();
    	for(byte e : array)
    		ret.add(e);
    	return ret;
    }
    
    //for primary type
    public static JSONArray asJSONArray(short ... array) {
    	if(array == null)
    		return null;
    	JSONArray ret = new JSONArray();
    	for(short e : array)
    		ret.add(e);
    	return ret;
    }
    
	//for primary type
    public static JSONArray asJSONArray(int ... array) {
    	if(array == null)
    		return null;
    	JSONArray ret = new JSONArray();
    	for(int e : array)
    		ret.add(e);
    	return ret;
    }
    
    //for primary type
    public static JSONArray asJSONArray(long ... array) {
    	if(array == null)
    		return null;
    	JSONArray ret = new JSONArray();
    	for(long e : array)
    		ret.add(e);
    	return ret;
    }
    
    //for primary type
    public static JSONArray asJSONArray(float ... array) {
    	if(array == null)
    		return null;
    	JSONArray ret = new JSONArray();
    	for(float e : array)
    		ret.add(e);
    	return ret;
    }
    
    //for primary type
    public static JSONArray asJSONArray(double ... array) {
    	if(array == null)
    		return null;
    	JSONArray ret = new JSONArray();
    	for(double e : array)
    		ret.add(e);
    	return ret;
    }
    
    //for primary type
    /** 比照 JavaScript, 轉成由字串組成的 JSONArray 物件(JavaScript 無 char 型態). */
    public static JSONArray asJSONArray(char ... array) {
    	if(array == null)
    		return null;
    	JSONArray ret = new JSONArray();
    	for(char e : array)
    		ret.add(String.valueOf(e));
    	return ret;
    }
    
    //for primary type
    public static JSONArray asJSONArray(boolean ... array) {
    	if(array == null)
    		return null;
    	JSONArray ret = new JSONArray();
    	for(boolean e : array)
    		ret.add(e);
    	return ret;
    }
    
    /**
     * 輔助轉出 JSON 字串的工具, 一律轉為字串值(但陣列中所含的 JSONObject, JSONArray 內部不予處理), null 值轉為空字串.
     * 陣列物件中的值限定為 Boolean, Number/Byte/Float/Integer/Long/Short, String, JSONObject, JSONArray,
     * 也可以是 List(內含值的型態限制同前), Map(內含值的型態限制同前).
     */
    @SuppressWarnings("unchecked")
	public static JSONArray asJSONArrayAsString(Object ... array) {
    	if(array == null)
    		return null;
    	
    	JSONArray ar = new JSONArray();
    	for(Object o : array) {
    		if(o == null)
    			ar.add("");
    		else if(o instanceof JSONObject)
    			ar.add((JSONObject)o);
    		else if(o instanceof JSONArray)
    			ar.add((JSONArray)o);
    		else if(o instanceof String)
    			ar.addAsString((String)o);
    		else if(o instanceof Boolean)
    			ar.addAsString((Boolean)o);
    		else if(o instanceof Number)
    			ar.addAsString((Number)o);
    		else if(o instanceof Character) //JavaScript 無 char 型態
    			ar.addAsString(String.valueOf(o));
    		else if(o instanceof List)
    			ar.add(asJSONArrayAsString(((List<Object>)o).toArray()));
    		else if(o instanceof Object[])
    			ar.add(asJSONArrayAsString((Object[])o));
    		else
    			ar.addAsString(o.toString());
    	}
    	return ar;
    }
    
    //for primary type
    public static JSONArray asJSONArrayAsString(byte ... array) {
    	if(array == null)
    		return null;
    	JSONArray ret = new JSONArray();
    	for(byte e : array)
    		ret.addAsString(e);
    	return ret;
    }
    
    //for primary type
    public static JSONArray asJSONArrayAsString(short ... array) {
    	if(array == null)
    		return null;
    	JSONArray ret = new JSONArray();
    	for(short e : array)
    		ret.addAsString(e);
    	return ret;
    }
    
    //for primary type
    public static JSONArray asJSONArrayAsString(int ... array) {
    	if(array == null)
    		return null;
    	JSONArray ret = new JSONArray();
    	for(int e : array)
    		ret.addAsString(e);
    	return ret;
    }
    
    //for primary type
    public static JSONArray asJSONArrayAsString(long ... array) {
    	if(array == null)
    		return null;
    	JSONArray ret = new JSONArray();
    	for(long e : array)
    		ret.addAsString(e);
    	return ret;
    }
    
    //for primary type
    public static JSONArray asJSONArrayAsString(float ... array) {
    	if(array == null)
    		return null;
    	JSONArray ret = new JSONArray();
    	for(float e : array)
    		ret.addAsString(e);
    	return ret;
    }
    
    //for primary type
    public static JSONArray asJSONArrayAsString(double ... array) {
    	if(array == null)
    		return null;
    	JSONArray ret = new JSONArray();
    	for(double e : array)
    		ret.addAsString(e);
    	return ret;
    }
    
    //for primary type
    /** 比照 JavaScript 一律視為 String 陣列(JavaScript 無 char 型態). */
    public static JSONArray asJSONArrayAsString(char ... array) {
    	if(array == null)
    		return null;
    	JSONArray ret = new JSONArray();
    	for(char e : array)
    		ret.add(String.valueOf(e));
    	return ret;
    }
    
    //for primary type
    public static JSONArray asJSONArrayAsString(boolean ... array) {
    	if(array == null)
    		return null;
    	JSONArray ret = new JSONArray();
    	for(boolean e : array)
    		ret.addAsString(e);
    	return ret;
    }

    //所有層的成員全轉成 Boolean, Number, String, Map<String, Object>, List<String, Object>
    static List<Object> _toList(JsonArray array) {
    	if(array == null)
    		return null;
    	
    	int size = array.size();
    	List<Object> list = new ArrayList<Object>(size);
    	for(int i = 0; i < size; i++) {
    		JsonElement e = array.get(i);
    		Object v = null;
    		
    		if(e.isJsonPrimitive()) {
    			JsonPrimitive p = (JsonPrimitive)e;
    			if(p.isString())
        			v = p.getAsString();
        		else if(p.isNumber())
        			v = p.getAsNumber();
        		else if(p.isBoolean())
        			v = p.getAsBoolean();
    		} else if(e.isJsonObject()) {
        		v = JSONObject._toMap((JsonObject)e);
    		} else if(e.isJsonArray()) {
        		v = _toList((JsonArray)e);
    		}
    		
    		list.add(v);
    	}
    	return list;
    }
}
