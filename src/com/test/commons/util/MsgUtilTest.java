package com.test.commons.util;

import static org.junit.Assert.*;
import org.junit.Test;

public class MsgUtilTest {

	@Test
	public void testReplacePlaceHoder() {
		String s1 = "This {1} is {0} test, {2}";
		Object[] args1 = { 'a', "program", 2 };
		
		String ss1 = MsgUtil.replacePlaceHoder(s1, args1);
		System.out.println("s1=\"" + s1 + "\" => \"" + ss1 + "\"");
		assertEquals("This program is a test, 2", ss1);
		
		String s2 = "This {1} is {0} test, {2}: a placeholder \\{0}";
		Object[] args2 = { 'a', "program", 2 };
		String ss2 = MsgUtil.replacePlaceHoder(s2, args2);
		System.out.println("s2=\"" + s2 + "\" => \"" + ss2 + "\"");
		assertEquals("This program is a test, 2: a placeholder {0}", ss2);
		
		String ss2a = MsgUtil.replacePlaceHoder(s2, new Object[0]);
		System.out.println("s2=\"" + s2 + "\" => \"" + ss2a + "\"");
	}

}
