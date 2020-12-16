package com.test.commons.util;

import junit.framework.TestCase;

public class JSONArrayTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testJSONArray() {
        JSONArray jsons = new JSONArray();
        assertEquals("[]", jsons.toString());
    }
    
    public void testJSONArray2() {
    	String s = "[0, true, 'x', 'xyz', { aa:1, bb:true, cc:'y', dd:'abc' }, [ 7, true, 'z', 'mnp', { aaa:2, bbb:true, ccc:'g', ddd:'def' } ] ]";
    	JSONArray jsons = new JSONArray(s);
    	System.out.println("s=" + s + "\n ==> " + jsons.toString());
    	
    	assertEquals(0, jsons.getAsNumber(0).intValue());
    	assertEquals("0", jsons.getAsString(0)); //數字也可當字串處理
    	assertTrue(jsons.getAsBoolean(1));
    	assertEquals("true", jsons.getAsString(1)); //boolean 也可當字串處理
    	assertEquals("x", jsons.getAsString(2)); //JavaScript 沒有 char type, 一律化為 String 處理
    	assertEquals("xyz", jsons.getAsString(3));
    	
    	System.out.println("element [4]: " + jsons.getAsJSONObject(4));
    	System.out.println("element [5]: " + jsons.getAsJSONArray(5));
    	
    	s = "[]"; //null 將引發 NullPointerException, 空字串不被視為 JSON array
    	jsons = new JSONArray(s);
    	System.out.println(jsons.toString());
    	
    	s = "[{\"id\": \"Momo\", \"pi\": 123, \"checked\": true}, {\"id\": \"Awu\", \"pid\": 321, \"checked\": false}]";
    	jsons = new JSONArray(s);
    	System.out.println(jsons.toString());
    }

    public void testGetAsJSONArray1() {
    	JSONArray array = new JSONArray();
    	
    	JSONObject o = new JSONObject();
    	o.put("test", "value");
    	
    	array.add(o);
    	System.out.println("array=" + array);
    }
    
    public void testAsJSONArray() {
    	System.out.println("testAsJSONArray(): ");
    	
    	byte[] a1 = { (byte)11, (byte)22, (byte)33 };
    	JSONArray j1 = JSONArray.asJSONArray(a1);
    	System.out.println("byte array => " + j1);
    	
    	boolean[] a2 = { true, false, true };
    	JSONArray j2 = JSONArray.asJSONArray(a2);
    	System.out.println("boolean array => " + j2);
    	
    	char[] a3 = { 'a', 'b', 'c' };
    	JSONArray j3 = JSONArray.asJSONArray(a3);
    	System.out.println("char array => " + j3);
    }
    
    public void testAsJSONArrayAsString() {
    	System.out.println("testAsJSONArrayAsString(): ");
    	
    	byte[] a1 = { (byte)11, (byte)22, (byte)33 };
    	JSONArray j1 = JSONArray.asJSONArrayAsString(a1);
    	System.out.println("byte array => " + j1);
    }
}
