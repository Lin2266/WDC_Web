package com.test.commons.util;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.test.commons.exception.JSONException;

import junit.framework.TestCase;

public class JSONObjectTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testJSONObject() {
        JSONObject json = new JSONObject();
        assertEquals("{}", json.toString());
    }
    
    public void testJSONObject2() {
    	String s = "{ a:0, b:'z', c:true, d:'test', e:{ aa:'testtest', bb:5 }, " +
    			"f:[1, true, '3'], g:[0,'zzz', { bbb:'kkk', ccc:9 }], y:null, null:'null key' }";
    	JSONObject json = new JSONObject(s);
    	System.out.println("testJSONObject2(): source: " + s + " => \n  json object toString(): " + json.toString());
    	
    	assertNotNull(json.getAsNumber("a"));
    	assertEquals(0, json.getAsNumber("a").intValue());
    	assertNotNull(json.getAsString("a"));
    	assertEquals("0", json.getAsString("a")); //JSON 裡的數字也可當字串處理
    	
    	assertNotNull(json.getAsString("b"));
    	assertEquals("z", json.getAsString("b")); //JavaScript 沒有 char 型態, 一律當字串處置
    	
    	assertNotNull(json.getAsBoolean("c"));
    	assertTrue(json.getAsBoolean("c"));
    	assertNotNull(json.getAsString("c"));
    	assertEquals("true", json.getAsString("c")); //JSON 裡的 boolean 也可當字串處理
    	
    	assertNotNull(json.getAsJSONObject("e"));
    	System.out.println("testJSONObject2(): JSONObject e: " + json.getAsJSONObject("e"));
    	System.out.println("testJSONObject2(): e as String: " + json.getAsString("e"));
    	
    	System.out.println("testJSONObject2(): get null value of y: " + json.get("y"));
    	System.out.println("testJSONObject2(): get null string value of y: " + json.getAsString("y"));
    	System.out.println("testJSONObject2(): get non-exist z value: " + json.get("z"));
    	System.out.println("testJSONObject2(): get non-exist z string value: " + json.getAsString("z"));
    	System.out.println("testJSONObject2(): get null key value: " + json.get(null));
    	System.out.println("testJSONObject2(): get null key string value: " + json.getAsString(null));
    }
    
    public void testJSONObject3() {
    	Double ct = 40.0;
    	Double pt = 1.0;
    	Double kwh = 8765.372;
    	Double kwhctpt = kwh * ct * pt;
    	JSONObject o = new JSONObject();
    	o.put("ct", ct).put("pt", pt).put("kwh", kwh).put("kwhctpt", kwhctpt);
    	System.out.println("testJSONObject3(): o=" + o);
    }
    
    public void testJSONObject4() {
    	try {
	    	//String s = null; //將引發 Exception, 空字串也不被視為 JSON object 或 array
    		//String s = "";
    		String s = "故意丟的 exception 訊息";
	    	JSONObject json = new JSONObject(s);
    	} catch(JSONException e) {
    		System.out.println("testJSONObject4(): empty string can't be source of JSONObject: " + ExceptionUtil.getRootMessage(e));
    		e.printStackTrace();
    	}
    }

    public void testGetAsJSONArray() {
    	JSONArray array = new JSONArray();
    	JSONObject json = new JSONObject();
    	json.put("array", array);
    	assertEquals(JSONArray.class, json.getAsJSONArray("array").getClass());
    }
    
    public void testGetAsJSONArray2() {
    	JSONArray array = new JSONArray();
    	array.add(true);
    	array.add("c");
    	array.add(3);
    	JSONObject o = new JSONObject();
    	o.put("test1", "value1");
    	array.add(o);
    	
    	JSONObject json = new JSONObject();
    	json.put("test1", "value1");
    	json.put("array", array);
    	
    	System.out.println("testGetAsJSONArray2(): " + json.getAsJSONArray("array"));
    	System.out.println("testGetAsJSONArray2(): " + json.getAsJSONArray("foo"));
    }
    
    public void testPutAll() {
    	System.out.println("testPutAll(): ");
    	JSONObject json = new JSONObject().put("a", 0).put("b", true).put("c", "test").put("xxx", "value1");
    	System.out.println("  json=" + json);
    	JSONObject json2 = new JSONObject().put("x", 999).put("y", false).put("z", "spam").put("xxx", "value2");
    	System.out.println("  json2=" + json2);
    	json.putAll(json2);
    	System.out.println("  => json=" + json);
    }
    
    public void testTuneTypeToJsonElement() {
    	JSONObject json = new JSONObject();
    	JsonElement j1 = JSONObject.tuneType(new JSONArray().add(true).add(321).add('z').add("sub3").add(new JSONObject().put("a2", 111).put("b2", true).put("c2", "測試")));
    	
    	JsonArray ar = new JsonArray();
    	JsonElement j2 = ar;
    	JsonObject jo = new JsonObject();
    	jo.addProperty("a2", 111);
    	jo.addProperty("c2", "測試");
    	jo.addProperty("b2", true);
    	
    	//順序不同的 array 即視為內容不同
    	ar.add(new JsonPrimitive(true));
    	ar.add(new JsonPrimitive(321));
    	ar.add(new JsonPrimitive('z'));
    	ar.add(new JsonPrimitive("sub3"));
    	ar.add(jo);
    	assertTrue(j1.equals(j2));
    }
    
    public void testEntrySet() {
    	System.out.println("testEntrySet():");
    	JSONObject json = new JSONObject().put("nil", (String)null).put("a", 0).put("b", true).put("c", "test")
    			.put("xxx", new JSONObject().put("a1", 123).put("b1", false).put("c1", "sub2"))
    			.put("yyy", new JSONArray().add(true).add(321).add('z').add("sub3").add(new JSONObject().put("a2", 111).put("b2", true).put("c2", "測試")));
    	Set<JSONObject.JSONEntry> entrySet = json.entrySet();
    	System.out.println("  json=" + json);
    	System.out.println("  => json.entrySet()=" + entrySet);
    
    	System.out.println("  iterated:");
    	for(Iterator<JSONObject.JSONEntry> i = entrySet.iterator(); i.hasNext(); ) {
    		JSONObject.JSONEntry entry = i.next();
    		System.out.println("    " + entry); //.getKey() + ": " + entry.getValue());
    	}
    	
    	//test Map.Entry.contains()
    	Map.Entry<String, JsonElement> entry = new AbstractMap.SimpleEntry<String, JsonElement>("nil", JSONObject.tuneType((String)null));
    	assertTrue(entrySet.contains(entry));
    	entry = new AbstractMap.SimpleEntry<String, JsonElement>("a", JSONObject.tuneType(0));
    	assertTrue(entrySet.contains(entry));
    	entry = new AbstractMap.SimpleEntry<String, JsonElement>("b", JSONObject.tuneType(true));
    	assertTrue(entrySet.contains(entry));
    	entry = new AbstractMap.SimpleEntry<String, JsonElement>("c", JSONObject.tuneType("test"));
    	assertTrue(entrySet.contains(entry));
    	entry = new AbstractMap.SimpleEntry<String, JsonElement>("c", JSONObject.tuneType("non-existed-value")); //放一個 value 不一致的 Entry 物件
    	assertTrue(!entrySet.contains(entry));
    	entry = new AbstractMap.SimpleEntry<String, JsonElement>("xxx", JSONObject.tuneType(new JSONObject().put("a1", 123).put("b1", false).put("c1", "sub2"))); //value 由 JSONObject 轉換而來
    	assertTrue(entrySet.contains(entry));
    	entry = new AbstractMap.SimpleEntry<String, JsonElement>("yyy", JSONObject.tuneType(new JSONArray().add(true).add(321).add('z').add("sub3").add(new JSONObject().put("a2", 111).put("b2", true).put("c2", "測試")))); //value 由 JSONArray 轉換而來
    	assertTrue(entrySet.contains(entry));
    }
}
