package com.test.commons.util.internal;

import junit.framework.TestCase;

public class JSArrayTest extends TestCase {
	//protected void setUp() throws Exception {
	//	super.setUp();
	//}

	public void test1() {
		JSArray ar = new JSArray();
		System.out.println(ar);
		assertTrue(true);
	}
	
	public void test2() {
		JSArray ar = new JSArray();
		ar.add(true)
			.add(false)
			.add('a')
			.add("test")
			.add(5)
			.addFunction("a", "b", "c", "return a + b + c;")
			.add(5.5);
		System.out.println(ar);
		assertTrue(true);
	}
	
	public void test3() {
		JSArray ar = new JSArray(JSArray.USE_SINGLE_QUOTE);
		ar.add(true)
			.add(false)
			.add('a')
			.add("test")
			.add(5)
			.addFunction("a", "b", "c", "return a + b + c;")
			.add(5.5);
		System.out.println(ar);
		assertTrue(true);
	}
	
	public void test4() {
		JSObject o = new JSObject();
		o.put("key1", true)
			.put("key2", false)
			.put("key3", 123)
			.put("key4", 3.5)
			.put("key5", 'z')
			.put("key6", "testtest");
		
		JSArray ar = new JSArray();
		ar.add(true)
			.add(false)
			.add('a')
			.add("test")
			.add(5)
			.add(o)
			.addFunction("a", "b", "c", "return a + b + c;")
			.add(5.5);
		System.out.println(ar);
		assertTrue(true);
	}
	
	public void test5() {
		JSArray ar = new JSArray();
		ar.add(true)
			.add(false)
			.add('a')
			.add("test")
			.add(5)
			.addFunction("a", "b", "c", "return a + b + c;")
			.add(5.5);
		
		JSArray ar2 = new JSArray();
		ar2.add(2)
			.add(false)
			.add('g')
			.add(ar)
			.add("this is a test");
		
		System.out.println(ar2);
		assertTrue(true);
	}
}
