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
 * 用來生成 JSON 物件字串用的輔助容器(相當於 Javascript 之關聯式陣列(associated array, map, plain JavaScript object, etc)).
 * 所有可存放的類別, 限於: null, Boolean, Number(含 Byte/Float/Integer/Long/Short/BigDecimal), String,
 * Character(視同 String, 比照 JavaScript 無 char 型態故),
 * 或 JSONObject, JSONArray.
 * <p>
 * depend on: google gson
 */
public class JSONObject {
    private JsonObject obj;
    
    public JSONObject() {
        this.obj = new JsonObject();
    }
    
    /** 讀取 JSON 字串(限定 JavaScript map 型態, 不可為 null 或空字串)並解析化為 JSONObject 物件 */
    public JSONObject(String json) {
    	try {
    		this.obj = new JsonParser().parse(json).getAsJsonObject();
    	} catch(JsonParseException je) {
    		throw new JSONException(je.getMessage(), new IllegalStateException(json)); //這裡的輸入字串極可能是呼叫者端所發生的 exception 訊息 -> 把輸入字串當成 exception 的根源
    	} catch(IllegalStateException ie) { //當輸入字串為 null 或 "" 時會走這裡
    		throw new JSONException(ie.getMessage(), new IllegalStateException(json)); //這裡的輸入字串極可能是呼叫者端所發生的 exception 訊息 -> 把輸入字串當成 exception 的根源
    	} catch(Throwable t) {
    		throw new JSONException(t.getMessage(), t);
    	}
    }

    JSONObject(JsonObject json) {
        this.obj = json;
    }
    
    /** 取得當前 JSONObject 物件第一層的 key 值(無順序性). */
    public Set<String> keySet() {
    	return getContainedObject().keySet();
    }
    
    /** 求本 JSONObject 物件內含的 key 的數量 */
    public int size() {
    	return getContainedObject().size();
    }
    
    /** 是否存在該屬性 */
    public boolean has(String key) {
    	return getContainedObject().has(key);
    }
 
    /**
     * 移除指定的屬性.
     * @param key
     * @return 當前 JSONObject 物件自身
     */
    public JSONObject remove(String key) {
    	getContainedObject().remove(key);
    	return this;
    }
    
    /**
     * 傳回本 JSONObject 物件的成員. 其排列順序按各成員加入時的順序而定(依照底層 gson 實作)
     * @return 本物件之成員
     */
    public Set<JSONEntry> entrySet() {
    	return new EntrySet(getContainedObject().entrySet());
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //get(), getAsXXX()
    /////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * 取出屬性值.
     * @param key 屬性名
     * @return 類型限定為 null/String/Number/Boolean/JSONObject/JSONArray
     */
    public Object get(String key) {
    	return tuneType(getContainedObject().get(key));
    }
    
    /**
     * 供便利於取出型態為 JSONObject 的指定屬性
     * @param key
     * @return key 對應的 JSONObject 物件; 無此 key 或該 key 對應至 null 值者, 傳回 null
     */
    public JSONObject getAsJSONObject(String key) {
    	JsonObject v = getContainedObject().getAsJsonObject(key);
    	return (v == null) ? null : new JSONObject(v);
    }
    
    /**
     * 供便利於取出型態為 JSONArray 的指定屬性
     * @param key
     * @return key 對應的 JSONArray 物件; 無此 key 或該 key 對應至 null 值者, 傳回 null
     */
    public JSONArray getAsJSONArray(String key) {
    	JsonArray v = getContainedObject().getAsJsonArray(key);
    	return (v == null) ? null : new JSONArray(v);
    }
    
    /**
     * 供便利於取出型態為 String 的指定屬性; 無此 key 或該 key 對應至 null 值者, 傳回 null.
     * @param key
     * @return key 對應的 String 物件; 但若<b>無此 key 或該 key 對應至 null 值者, 傳回 null</b>
     */
    public String getAsString(String key) {
    	JsonElement v = getContainedObject().get(key); //遇 key:null 的值會得到 JsonNull 物件, 遇不存在的 key 則傳回 null
    	if(v == null || v.isJsonNull())
    		return null;
    	if(v.isJsonObject() || v.isJsonArray())
    		return v.toString();
    	return v.getAsString();
    }
    
    /**
     * 供便利於取出型態為 Boolean 的指定屬性
     * @param key
     * @return key 對應的 Boolean 物件; 無此 key 或該 key 對應至 null 值者, 傳回 null
     */
    public Boolean getAsBoolean(String key) {
    	JsonElement v = getContainedObject().get(key); //遇 key:null 的值會得到 JsonNull 物件, 遇不存在的 key 則傳回 null
    	return (v == null || v.isJsonNull()) ? null : v.getAsBoolean();
    }
    
    /**
     * 供便利於取出型態為 Number 的指定屬性
     * @param key
     * @return key 對應的 Number 物件; 無此 key 或該 key 對應至 null 值者, 傳回 null
     */
    public Number getAsNumber(String key) {
    	JsonElement v = getContainedObject().get(key); //遇 key:null 的值會得到 JsonNull 物件, 遇不存在的 key 則傳回 null
    	return (v == null || v.isJsonNull()) ? null : v.getAsNumber();
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////
    //put(), putAll(), putAsXXX()
    /////////////////////////////////////////////////////////////////////////////////////////////
    
    /** 替 JSONObject 增加/修改 JSONObject 屬性. value 為 null 者, 轉為 Javascript 的 null 值. */
    public JSONObject put(String key, JSONObject value) {
    	return put(this, key, value);
    }
    
    /** 替 JSONObject 增加/修改 JSONArray 屬性. value 為 null 者, 轉為 Javascript 的 null 值. */
    public JSONObject put(String key, JSONArray value) {
    	return put(this, key, value);
    }
    
    /** 替 JSONObject 增加/修改 String 屬性. value 為 null 者, 轉為 Javascript 的 null 值. */
    public JSONObject put(String key, String value) {
    	return put(this, key, value);
    }
    
    /** 替 JSONObject 增加/修改 String 屬性(JavaScript 並無 char 型態). value 為 null 者, 轉為 Javascript 的 null 值. */
    public JSONObject put(String key, Character value) {
    	return put(this, key, value);
    }
    
    /** 替 JSONObject 增加/修改 Boolean 屬性. value 為 null 者, 轉為 Javascript 的 null 值. */
    public JSONObject put(String key, Boolean value) {
    	return put(this, key, value);
    }
    
    /** 替 JSONObject 增加/修改 Number/Byte/Float/Integer/Long/Short 屬性. value 為 null 者, 轉為 Javascript 的 null 值. */
    public JSONObject put(String key, Number value) {
    	return put(this, key, value);
    }

    /** 含入另一 JSONObject 物件的內容, key 相同者, 整組 key-value 將被覆蓋. */
    public JSONObject putAll(JSONObject json) {
    	if(json == null)
    		return this;
    	
    	final Set<Map.Entry<String, JsonElement>> entrySet2 = json.getContainedObject().entrySet();
    	for(Map.Entry<String, JsonElement> entry2 : entrySet2)
    		getContainedObject().add(entry2.getKey(), entry2.getValue());
    	return this;
    }
    
    /** 
     * 含入另一 Map 物件的內容, key 相同者, 整組 key-value 將被覆蓋.
     * 與執行 putAll(asJSONObject(m)) 等效.
     * @param m 可接受的型態, 見 asJSONObject(Map&lt;String, ?&gt;) 之說明
     * @see #asJSONObject(Map) 
     */
    public JSONObject putAll(Map<String, ?> m) {
    	return putAll(asJSONObject(m));
    }
    
    /** 供便利於替 JSONObject 增加/修改 String 屬性, 一律轉為字串. value 為 null 者, 轉為 Javascript 空字串. */
    public JSONObject putAsString(String key, String value) {
    	return put1(this, key, (value == null) ? "" : value);
    }

    /** 供便利於替 JSONObject 增加/修改 Boolean 屬性, 一律轉為字串. value 為 null 者, 轉為 Javascript 空字串. */
    public JSONObject putAsString(String key, Boolean value) {
    	return put1(this, key, (value == null) ? "" : value.toString());
    }
    
    /** 供便利於替 JSONObject 增加/修改 Number/Byte/Float/Integer/Long/Short/BigDecimal 屬性, 一律轉為字串. value 為 null 者, 轉為 Javascript 空字串. */
    public JSONObject putAsString(String key, Number value) {
    	return put1(this, key, (value == null) ? "" : value.toString());
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////
    //toXXX(), asXXX()
    /////////////////////////////////////////////////////////////////////////////////////////////
    
    /** 轉出 JSON map 字串 */
    @Override
    public String toString() {
        return getContainedObject().toString();
    }
    
    /**
     * 轉出 Java Map 物件, key 型態固定為 String, 值類型限定為 
     * Boolean, Number, String, Map&lt;String, Object&gt;, List&lt;String, Object&gt;
     * (Map, List 之值的型態亦限定為以上所列的型態)
     */
    public Map<String, Object> toMap() {
    	return _toMap(getContainedObject());
    }

    /**
     * 輔助轉出 JSON 字串的工具.
     * @param m 物件中的值限定為 Boolean, Number/Byte/Float/Integer/Long/Short/BigDecimal, String, Character(視同 String), JSONObject, JSONArray,
     * 		也可以是 List(內含值的型態限制同前), Map(內含值的型態限制同前).
     */
    @SuppressWarnings("unchecked")
	public static JSONObject asJSONObject(Map<String, ?> m) {
    	if(m == null)
    		return null;
    	
    	JSONObject jo = new JSONObject();
    	for(String key : m.keySet()) {
    		Object o = m.get(key);
    		if(o == null)
    			jo.put(key, (String)null);
    		else if(o instanceof JSONObject)
    			jo.put(key, (JSONObject)o);
    		else if(o instanceof JSONArray)
    			jo.put(key, (JSONArray)o);
    		else if(o instanceof String)
    			jo.put(key, (String)o);
    		else if(o instanceof Boolean)
    			jo.put(key, (Boolean)o);
    		else if(o instanceof Number)
    			jo.put(key, (Number)o);
    		else if(o instanceof Character) //JavaScript 無 char 型態
    			jo.put(key, String.valueOf(o));
    		else if(o instanceof List)
    			jo.put(key, JSONArray.asJSONArray(((List<Object>)o).toArray()));
    		else if(o instanceof Object[])
    			jo.put(key, JSONArray.asJSONArray((Object[])o));
    		else if(o instanceof Map) //!
    			jo.put(key, asJSONObject((Map<String, ?>)o));
    		else
    			jo.put(key, o.toString());
    	}
    	return jo;
    }
    
    /**
     * 輔助轉出 JSON 字串的工具, 一律轉為字串值(但 Map 中所含的 JSONObject, JSONArray 物件內部則保留不作轉換), null 值轉為空字串.
     * Map 物件中的值限定為 Boolean, Number/Byte/Float/Integer/Long/Short/BigDecimal, String, Character(視同 String), JSONObject, JSONArray,
     * 也可以是 List(內含值的型態限制同前), Map(內含值的型態限制同前).
     */
    @SuppressWarnings("unchecked")
	public static JSONObject asJSONObjectAsString(Map<String, ?> m) {
    	if(m == null)
    		return null;
    	
    	JSONObject jo = new JSONObject();
    	for(String key : m.keySet()) {
    		Object o = m.get(key);
    		if(o == null)
    			jo.put(key, "");
    		else if(o instanceof JSONObject)
    			jo.put(key, (JSONObject)o);
    		else if(o instanceof JSONArray)
    			jo.put(key, (JSONArray)o);
    		else if(o instanceof String)
    			jo.putAsString(key, (String)o);
    		else if(o instanceof Boolean)
    			jo.putAsString(key, (Boolean)o);
    		else if(o instanceof Number)
    			jo.putAsString(key, (Number)o);
    		else if(o instanceof Character) //JavaScript 無 char 型態
    			jo.putAsString(key, String.valueOf(o));
    		else if(o instanceof List)
    			jo.put(key, JSONArray.asJSONArrayAsString(((List<Object>)o).toArray()));
    		else if(o instanceof Object[])
    			jo.put(key, JSONArray.asJSONArrayAsString((Object[])o));
    		else if(o instanceof Map) //!
    			jo.put(key, asJSONObjectAsString((Map<String, ?>)o));
    		else
    			jo.putAsString(key, o.toString());
    	}
    	return jo;
    }
    
    JsonObject getContainedObject() {
    	return this.obj;
    }
    
    private static JSONObject put(JSONObject o, String key, JSONObject value) {
    	o.getContainedObject().add(key, (value == null) ? JsonNull.INSTANCE : value.getContainedObject());
    	return o;
    }
    
    private static JSONObject put(JSONObject o, String key, JSONArray value) {
    	o.getContainedObject().add(key, (value == null) ? JsonNull.INSTANCE : value.getArray());
    	return o;
    }
    
    private static JSONObject put(JSONObject o, String key, String value) {
    	if(value == null) {
    		o.getContainedObject().add(key, JsonNull.INSTANCE);
    		return o;
    	}
    	return put1(o, key, value);
    }
    
    private static JSONObject put(JSONObject o, String key, Character value) {
    	return put(o, key, (value == null) ? (String)null : String.valueOf(value));
    }
    
    private static JSONObject put(JSONObject o, String key, Boolean value) {
    	o.getContainedObject().addProperty(key, value);
    	return o;
    }
    
    private static JSONObject put(JSONObject o, String key, Number value) {
    	o.getContainedObject().addProperty(key, value);
    	return o;
    }
    
    private static JSONObject put1(JSONObject o, String key, String value) {
    	o.obj.add(key, new JsonPrimitive(value));
    	return o;
    }

    //所有層的成員全轉成 Boolean, Number/Byte/Float/Integer/Long/Short, String, Map<String, Object>, List<String, Object>
    static Map<String, Object> _toMap(JsonObject obj) {
    	if(obj == null)
    		return null;
    	
    	Map<String, Object> map = new HashMap<String, Object>();
    	for(Map.Entry<String,JsonElement> e : obj.entrySet()) {
    		String key = e.getKey();
    		JsonElement value = e.getValue();
    		Object v = null;
    		
    		if(value.isJsonPrimitive()) {
    			JsonPrimitive p = (JsonPrimitive)value;
        		if(p.isString())
        			v = p.getAsString();
        		else if(p.isNumber())
        			v = p.getAsNumber();
        		else if(p.isBoolean())
        			v = p.getAsBoolean();
    		} else if(value.isJsonObject()) {
        		v = _toMap((JsonObject)value);
    		} else if(value.isJsonArray()) {
        		v = JSONArray._toList((JsonArray)value);
    		}
    		
    		map.put(key, v);
    	}
    	return map;
    }
    
    //將被取出的 gson JsonElement 物件轉為 null 或 String 或 Number 或 Boolean 或 JSONObject 或 JSONArray 型態之物件
    static Object tuneType(JsonElement o) {
    	if(o == null)
    		return null;
    	if(o.isJsonObject())
    		return new JSONObject((JsonObject)o);
    	if(o.isJsonArray())
    		return new JSONArray((JsonArray)o);
    	//if(o.isJsonNull())
    	//	return null;
    	if(o.isJsonPrimitive()) {
    		JsonPrimitive p = (JsonPrimitive)o;
    		if(p.isString())
    			return p.getAsString();
    		if(p.isNumber())
    			return p.getAsNumber();
    		if(p.isBoolean())
    			return p.getAsBoolean();
    		return null;
    	}
        return null;
    }
    
    //把 null/String/Number/Boolean/JSONObject/JSONArray 型態的物件化為 gson 的 JsonElement 衍生物件, 其他型態則化為 JsonPrimitive(字串) 物件
    //以下沒有使用 instanceof 求類型而使用多型
    static JsonElement tuneType(JSONObject o) { return (o == null) ? JsonNull.INSTANCE : o.getContainedObject(); }
    static JsonElement tuneType(JSONArray o) { return (o == null) ? JsonNull.INSTANCE : o.getArray(); }
    static JsonElement tuneType(String o) { return (o == null) ? JsonNull.INSTANCE : new JsonPrimitive(o); }
    static JsonElement tuneType(Boolean o) { return (o == null) ? JsonNull.INSTANCE : new JsonPrimitive(o); }
    static JsonElement tuneType(Number o) { return (o == null) ? JsonNull.INSTANCE : new JsonPrimitive(o); }
    static JsonElement tuneType(Character o) { return (o == null) ? JsonNull.INSTANCE : new JsonPrimitive(String.valueOf(o)); } //JavaScript 無 char 型態
    static JsonElement tuneType(List<?> o) { return (o == null) ? JsonNull.INSTANCE : JSONArray.asJSONArray(o.toArray()).getArray(); }
    static JsonElement tuneType(Object[] o) { return (o == null) ? JsonNull.INSTANCE : JSONArray.asJSONArray(o).getArray(); }
    static JsonElement tuneType(Map<String, ?> o) { return (o == null) ? JsonNull.INSTANCE : asJSONObject(o).getContainedObject(); } //!
    static JsonElement tuneType(Object o) { return (o == null) ? JsonNull.INSTANCE : new JsonPrimitive(o.toString()); } //其他 type 只好一律轉成字串處理了
    
    public static final class JSONEntry implements Map.Entry<String, Object> {
    	private Map.Entry<String, JsonElement> entry;
    	
    	JSONEntry(Map.Entry<String, JsonElement> entry) {
    		this.entry = entry;
    	}
    
    	Map.Entry<String, JsonElement> getContainedEntry() {
    		return this.entry;
    	}
    		
		@Override 
		public String getKey() {
			return this.entry.getKey();
		}

		@Override 
		public Object getValue() {
			return tuneType(this.entry.getValue());
		}

		@Override 
		public Object setValue(Object value) {
			final Object oldValue = getValue();
			this.entry.setValue(tuneType(value));
			return oldValue;
		}
    	
		@Override 
		public final boolean equals(Object o) {
			return this.entry.equals(o);
		}
		
		@Override 
		public final int hashCode() {
			return this.entry.hashCode();
		}
		
		@Override 
		public final String toString() {
			return this.entry.toString();
		}
    }
    
    private static final class EntryIterator implements Iterator<JSONEntry> {
    	private Iterator<Map.Entry<String, JsonElement>> iter;

    	EntryIterator(Iterator<Map.Entry<String, JsonElement>> iter) {
    		this.iter = iter;
    	}
    	
		@Override
		public boolean hasNext() {
			return this.iter.hasNext();
		}

		@Override
		public JSONEntry next() {
			return new JSONEntry(this.iter.next());
		}
		
		@Override 
		public void remove() {
			this.iter.remove();
		}
    }
    
    private static final class EntrySet implements Set<JSONEntry> {
    	private Set<Map.Entry<String, JsonElement>> entrySet;
    	
    	EntrySet(Set<Map.Entry<String, JsonElement>> entrySet) {
    		this.entrySet = entrySet;
    	}
    	
		@Override 
		public int size() {
			return this.entrySet.size();
		}

		@Override
		public boolean isEmpty() {
			return this.entrySet.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			Object o1 = (o != null && o instanceof JSONEntry) ? ((JSONEntry)o).getContainedEntry() : o;
			return this.entrySet.contains(o1);
		}

		@Override
		public Iterator<JSONEntry> iterator() {
			return new EntryIterator(this.entrySet.iterator());
		}

		@Override
		public Object[] toArray() {
			return toArray(new JSONEntry[this.entrySet.size()]);
		}

		@SuppressWarnings({ "hiding", "unchecked" })
		@Override
		public <JSONEntry> JSONEntry[] toArray(JSONEntry[] a) {
			if(a == null)
				throw new NullPointerException("the specified array is null");
			final Iterator<JSONObject.JSONEntry> iter = iterator();
			for(int i = 0; i < a.length && iter.hasNext(); i++) {
				a[i] = (JSONEntry)iter.next();
			}
			return a;
		}

		@Override
		public boolean add(JSONEntry e) {
			return this.entrySet.add((e == null) ? null : e.getContainedEntry());
		}

		@Override
		public boolean remove(Object o) {
			final Object o2 = (o != null && o instanceof JSONEntry) ? ((JSONEntry)o).getContainedEntry() : o;
			return this.entrySet.remove(o2);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			if(c == null)
				throw new NullPointerException("the specified collection is null");
			for(Iterator<?> i = c.iterator(); i.hasNext(); ) {
				if(!contains(i.next()))
					return false;
			}
			return true;
		}

		@Override
		public boolean addAll(Collection<? extends JSONEntry> c) {
			if(c == null)
				throw new NullPointerException("the specified collection is null");
			boolean ret = true;
			for(Iterator<? extends JSONEntry> i = c.iterator(); i.hasNext(); ) {
				if(!add(i.next()))
					ret = false;
			}
			return ret;
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			if(c == null)
				throw new NullPointerException("the specified collection is null");
			final Object[] o = c.toArray();
			boolean ret = true;
			
			for(Iterator<JSONEntry> iter = iterator(); iter.hasNext(); ) {
				final JSONEntry e = iter.next();
				boolean retain = false;
				for(int i = 0; i < o.length; i++) {
					if(e.equals(o[i])) {
						retain = true;
						break;
					}
				}
				
				if(!retain)
					iter.remove();
			}
			return ret;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			if(c == null)
				throw new NullPointerException("the specified collection is null");
			boolean ret = true;
			for(Iterator<?> i = c.iterator(); i.hasNext(); ) {
				if(!remove(i.next()))
					ret = false;
			}
			return ret;
		}

		@Override
		public void clear() {
			this.entrySet.clear();
		}

		@Override
		public String toString() {
			return this.entrySet.toString();
		}
    }
}
