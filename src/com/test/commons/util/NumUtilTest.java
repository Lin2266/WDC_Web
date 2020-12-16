package com.test.commons.util;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class NumUtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testRound1() {
		final double n = 123321.54567;
		assertEquals(123321.5, NumUtil.round(n, 1), 0);
		assertEquals(123321.55, NumUtil.round(n, 2), 0);
		assertEquals(123322, NumUtil.round(n, 0), 0);
		assertEquals(123300, NumUtil.round(n, -2), 0); //注意
	}
	
	@Test
	public void testRound2() {
		double n = -123321.54567;
		assertEquals(-123321.5, NumUtil.round(n, 1), 0);
		assertEquals(-123321.55, NumUtil.round(n, 2), 0);
		assertEquals(-123322, NumUtil.round(n, 0), 0);
		assertEquals(-123300, NumUtil.round(n, -2), 0); //注意
		
		n = -123321.56;
		assertEquals(-123321.56, NumUtil.round(n, 3), 0); //注意
	}

	@Test
	public void testRound3() {
		double n = 123321.34;
		assertEquals(123321.34, NumUtil.round(n, 3), 0);
		System.out.println("testRound3(): NumUtil.round(" + n + ", 3)=" + NumUtil.round(n, 3));
		
		n = -123321.34;
		assertEquals(-123321.34, NumUtil.round(n, 3), 0);
		System.out.println("testRound3(): NumUtil.round(" + n + ", 3)=" + NumUtil.round(n, 3));
		
		n = 123321.56;
		assertEquals(123321.56, NumUtil.round(n, 3), 0); //注意
		System.out.println("testRound3(): NumUtil.round(" + n + ", 3)=" + NumUtil.round(n, 3));
	}
	
	@Test
	public void testRoundDown1() {
		double n = 123321.54567;
		assertEquals(123321.5, NumUtil.roundDown(n, 1), 0);
		assertEquals(123321.54, NumUtil.roundDown(n, 2), 0);
		assertEquals(123321, NumUtil.roundDown(n, 0), 0);
		assertEquals(123300, NumUtil.roundDown(n, -2), 0); //注意
		
		n = 15678.8888;
		assertEquals(15600.0, NumUtil.roundDown(n, -2), 0); //注意
	}
	
	@Test
	public void testRoundDown2() {
		double n = -123321.54567;
		assertEquals(-123321.5, NumUtil.roundDown(n, 1), 0);
		assertEquals(-123321.54, NumUtil.roundDown(n, 2), 0);
		assertEquals(-123321, NumUtil.roundDown(n, 0), 0);
		assertEquals(-123300, NumUtil.roundDown(n, -2), 0); //注意
		
		n = -15678.8888;
		assertEquals(-15600.0, NumUtil.roundDown(n, -2), 0); //注意
	}
	
	@Test
	public void testRoundDown3() {
		double n = 123321.34;
		assertEquals(123321.339, NumUtil.roundDown(n, 3), 0); //注意! 注意! 注意!
		System.out.println("testTrunc3(): NumUtil.roundDown(" + n + ", 3)=" + NumUtil.roundDown(n, 3));
		
		n = 123321.34;
		assertEquals(123321.33, NumUtil.roundDown(n, 2), 0); //注意! 注意! 注意!
		System.out.println("testTrunc3(): NumUtil.roundDown(" + n + ", 2)=" + NumUtil.roundDown(n, 2));
		
		n = -123321.34;
		assertEquals(-123321.339, NumUtil.roundDown(n, 3), 0); //注意! 注意! 注意!
		System.out.println("testTrunc3(): NumUtil.roundDown(" + n + ", 3)=" + NumUtil.roundDown(n, 3));
		
		n = -123321.34;
		assertEquals(-123321.33, NumUtil.roundDown(n, 2), 0); //注意! 注意! 注意!
		System.out.println("testTrunc3(): NumUtil.roundDown(" + n + ", 2)=" + NumUtil.roundDown(n, 2));
		
		n = 123321.56;
		assertEquals(123321.559, NumUtil.roundDown(n, 3), 0); //注意! 注意! 注意!
		System.out.println("testTrunc3(): NumUtil.roundDown(" + n + ", 3)=" + NumUtil.roundDown(n, 3));
		
		n = 123321.56;
		assertEquals(123321.55, NumUtil.roundDown(n, 2), 0); //注意! 注意! 注意!
		System.out.println("testTrunc3(): NumUtil.roundDown(" + n + ", 2)=" + NumUtil.roundDown(n, 2));
		
		n = -123321.56;
		assertEquals(-123321.559, NumUtil.roundDown(n, 3), 0); //注意! 注意! 注意!
		System.out.println("testTrunc3(): NumUtil.roundDown(" + n + ", 3)=" + NumUtil.roundDown(n, 3));
		
		n = -123321.56;
		assertEquals(-123321.55, NumUtil.roundDown(n, 2), 0); //注意! 注意! 注意!
		System.out.println("testTrunc3(): NumUtil.roundDown(" + n + ", 2)=" + NumUtil.roundDown(n, 2));
	}
	
	@Test
	public void testRoundUp() {
		double n = 123321.6789;
		assertEquals(123322, NumUtil.roundUp(n, 0), 0F); //無條件進位至整數
		
		n = 123321.34567;
		assertEquals(123322, NumUtil.roundUp(n, 0), 0F); //無條件進位至整數
	}
	
	@Test
	public void testRoundUp1() {
		final double n = 123321.34567;
		assertEquals(123321.4, NumUtil.roundUp(n, 1), 0);
		assertEquals(123321.35, NumUtil.roundUp(n, 2), 0);
		assertEquals(123322, NumUtil.roundUp(n, 0), 0);
		assertEquals(123400, NumUtil.roundUp(n, -2), 0); //注意
	}
	
	@Test
	public void testRoundUp2() {
		final double n = -123321.34567;
		assertEquals(-123321.4, NumUtil.roundUp(n, 1), 0);
		assertEquals(-123321.35, NumUtil.roundUp(n, 2), 0);
		assertEquals(-123322, NumUtil.roundUp(n, 0), 0);
		assertEquals(-123400, NumUtil.roundUp(n, -2), 0); //注意
	}
	
	@Test
	public void testRoundUp3() {
		double n = 123321.34;
		assertEquals(123321.34, NumUtil.roundUp(n, 2), 0);
		
		n = 123321.34;
		assertEquals(123321.34, NumUtil.roundUp(n, 3), 0);
	}
	
	@Test
	public void testEquals1() {
		Number n1 = 1.1; //internal: double
		Number n2 = 1.1;
		assertTrue(NumUtil.equals(n1, n2));
		
		n2 = 1.10001;
		assertFalse(NumUtil.equals(n1, n2));
		
		n1 = 1.0; //internal: double
		n2 = 1; //internal: int
		assertFalse(NumUtil.equals(n1, n2));
	}
	
	@Test
	public void testEquals2() {
		Number n1 = 1L; //internal: long
		byte n2 = (byte)1; //byte
		assertTrue(NumUtil.equals(n1, n2));
	}
	
	@Test
	public void testEquals3() {
		Number n1 = 1.0; //internal: double
		double n2 = 1.0;
		assertTrue(NumUtil.equals(n1, n2));
		
		float n3 = 1.0F;
		assertTrue(NumUtil.equals(n1, n3));
	}
}
