package com.test.commons.util.internal;

import junit.framework.TestCase;

public class JSObjectTest extends TestCase {
	//protected void setUp() throws Exception {
	//	super.setUp();
	//}
	
	public void test0() {
		System.out.println("test0()...");
		
		JSObject o = new JSObject();
		System.out.println(o.toString());
		assertTrue(true);
	}
	
	public void test1() {
		System.out.println("test1()...");
		
		JSObject o = new JSObject();
		o.put("key1", true)
			.put("key2", false)
			.put("key3", 123)
			.put("key4", 3.5)
			.put("key5", 'z')
			.put("key6", "test");
		System.out.println(o.toString());
		assertTrue(true);
	}
	
	public void test2() {
		System.out.println("test2()...");
		
		JSObject o = new JSObject(JSObject.KEY_WITH_QUOTE);
		o.put("key1", true)
			.put("key2", false)
			.put("key3", 123)
			.put("key4", 3.5)
			.put("key5", 'z')
			.put("key6", "test");
		System.out.println(o.toString());
		assertTrue(true);
	}
	
	public void test3() {
		System.out.println("test3()...");
		
		JSObject o = new JSObject(JSObject.KEY_WITH_QUOTE | JSObject.USE_SINGLE_QUOTE);
		o.put("key1", true)
			.put("key2", false)
			.put("key3", 123)
			.put("key4", 3.5)
			.put("key5", 'z')
			.put("key6", "test");
		System.out.println(o.toString());
		assertTrue(true);
	}
	
	public void testFunction1() {
		System.out.println("testFunction1()...");
		
		JSObject o = new JSObject();
		o.put("key1", true)
			.put("key2", false)
			.put("key3", 123)
			.put("key4", 3.5)
			.put("key5", 'z')
			.putFunction("key5", "alert('This is a test');")
			.put("key7", "test");
		System.out.println(o.toString());
		assertTrue(true);
	}
	
	public void testFunction2() {
		System.out.println("testFunction2()...");
		
		JSObject o = new JSObject(JSObject.USE_SINGLE_QUOTE | JSObject.KEY_WITH_QUOTE);
		o.put("key1", true)
			.put("key2", false)
			.put("key3", 123)
			.put("key4", 3.5)
			.put("key5", 'z')
			.putFunction("key5", "a", "ab", "abc", "alert('a=' + a + ', ab=' + ab + ', abc=' + abc);")
			.put("key7", "test");
		System.out.println(o.toString());
		assertTrue(true);
	}
	
	public void testJSObject() {
		System.out.println("testJSObject()...");
		
		JSObject o = new JSObject();
		o.put("key1", true)
			.put("key2", false)
			.put("key3", 123)
			.put("key4", 3.5)
			.put("key5", 'z')
			.put("key6", "test")
			.putAsObject("key7", "{abc:\"def\"}");
		
		JSObject o2 = new JSObject();
		o2.put("keyA", true)
			.put("keyB", 789)
			.put("keyC", 9.8)
			.put("keyD", o)
			.put("keyE", 'A')
			.put("keyF", "this is a test");
		
		System.out.println(o2.toString());
		assertTrue(true);
	}
}
