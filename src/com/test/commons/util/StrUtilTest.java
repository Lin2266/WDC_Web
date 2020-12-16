package com.test.commons.util;

import junit.framework.TestCase;

public class StrUtilTest extends TestCase {
    public static final String TEST_STRING = "Test for test";

    protected void setUp() throws Exception {
        super.setUp();
    }

    public final void testAlignLeft() {
        String s1 = "foo";
        String s2 = "測";
        assertEquals("foo   ", StrUtil.alignLeft(s1, 6, ' '));
        assertEquals("測   ", StrUtil.alignLeft(s2, 4, ' '));
        assertEquals("測試試試", StrUtil.alignLeft(s2, 4, '試'));
        assertEquals("fo", StrUtil.alignLeft(s1, 2, ' '));
        assertEquals("", StrUtil.alignLeft(s1, 0, ' '));
    }

    public final void testAlignRight() {
        String s1 = "bar";
        String s2 = "測";
        assertEquals("***bar", StrUtil.alignRight(s1, 6, '*'));
        assertEquals("****測", StrUtil.alignRight(s2, 5, '*'));
        assertEquals("ar", StrUtil.alignRight(s1, 2, '*'));
        assertEquals("", StrUtil.alignRight(s1, 0, '*'));
    }
    
    public final void testAlignRight2() {
    	double n1 = 123.5;
    	assertEquals("00123.5", StrUtil.alignRightNoCut(n1, 7, '0'));
    	
    	int n2 = 123;
    	assertEquals("00123", StrUtil.alignRightNoCut(n2, 5, '0'));
    }

    public final void testSlice() {
    	String s = null;
    	assertNull(StrUtil.slice(s, 1, 2));
    	
    	s = "";
    	assertEquals("", StrUtil.slice(s, 1, 2));
    	
    	s = "abcdef";
    	assertEquals("", StrUtil.slice(s, 6, 8));
    	assertEquals("", StrUtil.slice(s, 8, 6));
    	assertEquals("", StrUtil.slice(s, 6, 6));
    	assertEquals("cd", StrUtil.slice(s, 2, 4));
    	assertEquals("", StrUtil.slice(s, 4, 2));
    	assertEquals("", StrUtil.slice(s, 2, 2));
    	assertEquals("c", StrUtil.slice(s, 2, 3));
    	assertEquals("cd", StrUtil.slice(s, -4, 4));
    	assertEquals("cd", StrUtil.slice(s, -4, -2));
    	assertEquals("", StrUtil.slice(s, -3, -3));
    	assertEquals("ef", StrUtil.slice(s, 4, 8));
    	assertEquals("ab", StrUtil.slice(s, 0, -4));
    }
    
    public final void testSlice2() {
    	String s = null;
    	assertNull(StrUtil.slice(s, 1, 2));
    	
    	s = "";
    	assertEquals("", StrUtil.slice(s, 1, 2));
    	
    	s = "abcdef";
    	assertEquals("", StrUtil.slice(s, 6));
    	assertEquals("ef", StrUtil.slice(s, 4));
    	assertEquals("ef", StrUtil.slice(s, -2));
    	assertEquals(s, StrUtil.slice(s, 0));
    	assertEquals(s, StrUtil.slice(s, -6));
    }
    
    public void testStartWith() {
    	String s = "This is a book";
    	String head = "This ";
    	assertTrue(StrUtil.startWith(s, head));
    	
    	head = "THIS ";
    	assertFalse(StrUtil.startWith(s, head));
    }
    
    public void testStartWithIgnoreCase() {
    	String s = "This is a book";
    	String head = "This ";
    	assertTrue(StrUtil.startWithIgnoreCase(s, head));
    	
    	head = "THIS ";
    	assertTrue(StrUtil.startWithIgnoreCase(s, head));
    }
    
    public void testEndWith() {
    	String s = "This is a book";
    	String head = "a book";
    	assertTrue(StrUtil.endWith(s, head));
    	
    	head = "A book";
    	assertFalse(StrUtil.endWith(s, head));
    }
    
    public void testEndWithIgnoreCase() {
    	String s = "This is a book";
    	String head = "a book";
    	assertTrue(StrUtil.endWithIgnoreCase(s, head));
    	
    	head = "A book";
    	assertTrue(StrUtil.endWithIgnoreCase(s, head));
    }
    
    public void testFormatNumber() {
    	String i0 = null;
    	String i1 = "100";
    	String i2 = "1000";
    	String i3 = "100000";
    	String i4 = "1000000";
    	String i5 = ".55";
    	String i6 = "1.55";
    	String i7 = "100.55";
    	String i8 = "1000.55";
    	String i9 = "100000.55";
    	String i10 = "1000000.55";
    	String i11 = "-100";
    	String i12 = "-1000";
    	String i13 = "-100000";
    	String i14 = "-1000000";
    	String i15 = "-0.55";
    	String i16 = "-1.55";
    	String i17 = "-100.55";
    	String i18 = "-1000.55";
    	String i19 = "-100000.55";
    	String i20 = "-1000000.55";
    	assertEquals("", StrUtil.formatNumber(i0));
    	assertEquals("100", StrUtil.formatNumber(i1));
    	assertEquals("1,000", StrUtil.formatNumber(i2));
    	assertEquals("100,000", StrUtil.formatNumber(i3));
    	assertEquals("1,000,000", StrUtil.formatNumber(i4));
    	assertEquals("0.55", StrUtil.formatNumber(i5));
    	assertEquals("1.55", StrUtil.formatNumber(i6));
    	assertEquals("100.55", StrUtil.formatNumber(i7));
    	assertEquals("1,000.55", StrUtil.formatNumber(i8));
    	assertEquals("100,000.55", StrUtil.formatNumber(i9));
    	assertEquals("1,000,000.55", StrUtil.formatNumber(i10));
    	assertEquals("-100", StrUtil.formatNumber(i11));
    	assertEquals("-1,000", StrUtil.formatNumber(i12));
    	assertEquals("-100,000", StrUtil.formatNumber(i13));
    	assertEquals("-1,000,000", StrUtil.formatNumber(i14));
    	assertEquals("-0.55", StrUtil.formatNumber(i15));
    	assertEquals("-1.55", StrUtil.formatNumber(i16));
    	assertEquals("-100.55", StrUtil.formatNumber(i17));
    	assertEquals("-1,000.55", StrUtil.formatNumber(i18));
    	assertEquals("-100,000.55", StrUtil.formatNumber(i19));
    	assertEquals("-1,000,000.55", StrUtil.formatNumber(i20));
    }
    
    public final void testFormatNumber1() {
        double i1 = 1001;
        double i2 = 10000000;
        assertEquals("1,001", StrUtil.formatNumber(i1));
        assertEquals("10,000,000", StrUtil.formatNumber(i2));
    }

//    public final void testFormatNumber2() {
//        int i1 = 55;
//        int i2 = 12345;
//        assertEquals("0055", StrUtil.formatNumber(i1, 4));
//        assertEquals("012345", StrUtil.formatNumber(i2, 6));
//    }

    public final void testFormatNumber3() {
        double d1 = 4D;
        double d2 = 0.1;
        double d3 = 12345678.12;
        int d4 = 1234;
        assertEquals("12,345,678.120", StrUtil.formatNumber(d3, "###,###,###,##0.000"));
        assertEquals("4.0", StrUtil.formatNumber(d1, "###,##0.0"));
        assertEquals("0.10", StrUtil.formatNumber(d2, "###,##0.00"));
        assertEquals("12,345,678.120", StrUtil.formatNumber(d3, "###,###,###,##0.000"));
        assertEquals("1,234", StrUtil.formatNumber(d4, "###,###,###,##0"));
        assertEquals("1,234.00", StrUtil.formatNumber(d4, "###,###,###,##0.00"));
    }

    public final void testIndexOfString() {
        String[] ss = new String[] { "aaa", "中文", "bbb", "ccc", "ddd" };
        assertEquals(3, StrUtil.indexOfString(ss, "ccc"));
        assertEquals(1, StrUtil.indexOfString(ss, "中文"));
    }

    public final void testIndexOfStringIgnoreCase() {
        String[] ss = new String[] { "aaa", "中文", "bbb", "ccc", "ddd" };
        assertEquals(3, StrUtil.indexOfStringIgnoreCase(ss, "cCc"));
    }
    
    public void testIsInteger() {
    	String s1 = null;
    	String s2 = "123";
    	String s3 = "abc";
    	String s4 = "abc321xyz";
    	String s5 = "+123";
    	String s6 = "-123";
    	
    	assertTrue(!StrUtil.isInteger(s1));
    	assertTrue(!StrUtil.isInteger(s1, 2, 100));
    	assertTrue(StrUtil.isInteger(s2));
    	assertTrue(!StrUtil.isInteger(s3));
    	assertTrue(StrUtil.isInteger(s4, 3, 6));
    	assertTrue(!StrUtil.isInteger(s4, 3, 3));
    	assertTrue(!StrUtil.isInteger(s4, 2, 5));
    	assertTrue(!StrUtil.isInteger(s4, 10, 100));
    	assertTrue(StrUtil.isInteger(s5));
    	assertTrue(StrUtil.isInteger(s6));
    }
    
    public void testIsNumber() {
    	String s1 = null; //no
    	String s2 = "123"; //yes
    	String s3 = "abc"; //no
    	String s4 = "abc321xyz"; //no
    	String s5 = "+123"; //yes
    	String s6 = "-123"; //yes
    	String s7 = ".123"; //yes
    	String s8 = "123."; //yes
    	String s9 = "10.123"; //yes
    	String s10 = "+10.123"; //yes
    	String s11 = "-10.123"; //yes
    	String s12 = "10.12.3"; //no
    	String s13 = "1+23"; //no
    	String s14 = "0"; //yes
    	String s15 = "+"; //no
    	String s16 = "+1"; //yes
    	String s17 = ""; //no
    	String s18 = "."; //no
    	String s19 = "-.12"; //yes
    	String s20 = "123,456"; //no
    	String s21 = "13D"; //no (雖然 Java 可以順利轉成 Double)
    	
    	String f1 = "1.23E10"; //yes
    	String f2 = "1.23E+10"; //yes
    	String f3 = "1.23E-10"; //yes
    	String f4 = "1.23E10.1"; //no
    	String f5 = "1.23E"; //no
    	String f6 = "-1.23E10"; //yes
    	String f7 = "10.23E10"; //yes
    	String f8 = "-10.23E10"; //yes
    	String f9 = "1E10.1"; //no
    	String f10 = "1.23a4E10"; //no
    	String f11 = ".23E10"; //yes
    	String f12 = "1.23e10"; //yes
    	String f13 = "E"; //no
    	
    	assertTrue(!StrUtil.isNumber(s1));
    	assertTrue(!StrUtil.isNumber(s1, 2, 100));
    	assertTrue(StrUtil.isNumber(s2));
    	assertTrue(!StrUtil.isNumber(s3));
    	assertTrue(StrUtil.isNumber(s4, 3, 6));
    	assertTrue(!StrUtil.isNumber(s4, 3, 3));
    	assertTrue(!StrUtil.isNumber(s4, 2, 5));
    	assertTrue(!StrUtil.isNumber(s4, 10, 100));
    	assertTrue(StrUtil.isNumber(s5));
    	assertTrue(StrUtil.isNumber(s6));
    	assertTrue(StrUtil.isNumber(s7));
    	assertTrue(StrUtil.isNumber(s8));
    	assertTrue(StrUtil.isNumber(s9));
    	assertTrue(StrUtil.isNumber(s10));
    	assertTrue(StrUtil.isNumber(s11));
    	assertTrue(!StrUtil.isNumber(s12));
    	assertTrue(!StrUtil.isNumber(s13));
    	assertTrue(StrUtil.isNumber(s14));
    	assertTrue(!StrUtil.isNumber(s15));
    	assertTrue(StrUtil.isNumber(s16));
    	assertTrue(!StrUtil.isNumber(s17));
    	assertTrue(!StrUtil.isNumber(s18));
    	assertTrue(StrUtil.isNumber(s19));
    	assertTrue(!StrUtil.isNumber(s20));
    	assertTrue(!StrUtil.isNumber(s21));
    	
    	assertTrue(StrUtil.isNumber(f1));
    	assertTrue(StrUtil.isNumber(f2));
    	assertTrue(StrUtil.isNumber(f3));
    	assertTrue(!StrUtil.isNumber(f4));
    	assertTrue(!StrUtil.isNumber(f5));
    	assertTrue(StrUtil.isNumber(f6));
    	assertTrue(StrUtil.isNumber(f7));
    	assertTrue(StrUtil.isNumber(f8));
    	assertTrue(!StrUtil.isNumber(f9));
    	assertTrue(!StrUtil.isNumber(f10));
    	assertTrue(StrUtil.isNumber(f11));
    	assertTrue(StrUtil.isNumber(f12));
    	assertTrue(!StrUtil.isNumber(f13));
    }

    public final void testIsDBCS() {
        char a1 = 'a';
        char a2 = '評';
        char a3 = '评';
        assertTrue(!StrUtil.isDBCS(a1));
        assertTrue(StrUtil.isDBCS(a2));
        assertTrue(StrUtil.isDBCS(a3));
    }

    public final void testIsEmpty() {
        String s1 = "    ";
        String s2 = "           ";
        String s3 = "　　"; //兩個全形空白
        String s4 = String.valueOf((char)12288) + String.valueOf((char)12288); //兩個全形空白
        String s5 = " a ";
        assertTrue(StrUtil.isEmpty(s1));
        assertTrue(StrUtil.isEmpty(s2));
        assertTrue(StrUtil.isEmpty(s3));
        assertTrue(StrUtil.isEmpty(s4));
        assertTrue(!StrUtil.isEmpty(s5));
    }

//    public final void testIsTraditionalChinese() {
//        char a1 = '評';
//        char a2 = '评';
//        assertTrue(StrUtil.isTraditionalChinese(a1));
//        assertTrue(!StrUtil.isTraditionalChinese(a2));
//    }

    public final void testNum2BigCNum() {
        double d = 123.4;
        assertEquals("壹佰貳拾參點肆", StrUtil.num2BigCNum(d));
    }

    public final void testNum2CNum() {
        double d1 = 123.4;
        double d2 = 123.0;
        assertEquals("一百二十三點四", StrUtil.num2CNum(d1));
        assertEquals("一百二十三", StrUtil.num2CNum(d2));
    }

    public final void testParseIntWidthDefault() {
        String s1 = "123";
        String s2 = "1a3";
        assertEquals(123, StrUtil.parseIntWithDefault(s1, 0).intValue());
        //給一個錯的數字
        assertEquals(0, StrUtil.parseIntWithDefault(s2, 0).intValue());
    }

    public final void testParseLongWidthDefault() {
        String s1 = "1234567890";
        String s2 = "1a34567890";
        assertEquals(1234567890, StrUtil.parseLongWithDefault(s1, 0L).longValue());
        //給一個錯的數字
        assertEquals(0, StrUtil.parseLongWithDefault(s2, 0L).longValue());
    }

    public final void testRepeat() {
        assertEquals("a", StrUtil.repeat('a', 1));
        assertEquals("", StrUtil.repeat('a', 0));
        assertEquals("", StrUtil.repeat(null, 10));
        assertEquals("aaaaaaaaaa", StrUtil.repeat('a', 10));
    }
    
    public final void testRepeat1() {
    	assertEquals("a,", StrUtil.repeat("a,", 1));
    	assertEquals("", StrUtil.repeat("a,", 0));
    	assertEquals("", StrUtil.repeat(null, 10));
        assertEquals("a,a,a,a,a,a,a,a,a,a,", StrUtil.repeat("a,", 10));
    }

    public final void testReplaceAll() {
        String s = "spameggsfoobareggstestexperiment";
        assertEquals("spamAAAAfoobarAAAAtestexperiment", StrUtil.replaceAll(s, "eggs", "AAAA"));
    }
    
    public final void testReplaceAll2() {
        String s = "spameggsfoobareggstestexperiment";
        assertEquals("spameggsfoobareggstestexperiment", StrUtil.replaceAll(s, "ccc", "zzz"));
    }
    
    public final void testReplaceAll3() {
        String s = "spameggsfoobareggstestexperiment";
        assertEquals("zzzeggsfoobareggstestexperiment", StrUtil.replaceAll(s, "spam", "zzz"));
    }
    
    public final void testReplaceAll4() {
        String s = "spameggsfoobareggstestexperiment";
        assertEquals("spameggsfoobareggstestzzz", StrUtil.replaceAll(s, "experiment", "zzz"));
    }

    public final void testReverse() {
        String s = "abcdefg";
        assertEquals("gfedcba", StrUtil.reverse(s));
    }

    public final void testSortStrings() {
        String[] ss1 = new String[] { "a12345", "a23456", "b12345" };
        String[] ss2 = new String[] { "b12345", "a23456", "a12345" };
        StrUtil.sortStrings(ss2);
        for(int i = 0, j = ss1.length; i < j; i++)
            assertEquals(ss1[i], ss2[i]);
    }

    public final void testSortStrings2() {
        String[] ss1 = new String[] { "b12345", "a23456", "a12345" };
        String[] ss2 = new String[] { "b12345", "a23456", "a12345" };
        StrUtil.sortStrings(ss2, false);
        for(int i = 0, j = ss1.length; i < j; i++)
            assertEquals(ss1[i], ss2[i]);
    }

    public final void testSubstr() {
        String s = "abcdefg";
        assertEquals("def", StrUtil.substr(s, 3, 3));
        assertEquals("", StrUtil.substr(s, 8, 3)); //故意從超過範圍的地方開始取
    }

    public final void testToInt() {
        String s = "123";
        assertEquals(123, StrUtil.toInt(s));
    }

    public final void testToInt2() {
        String s1 = "123";
        String s2 = "123a";
        assertEquals(123, StrUtil.toInt(s1, 0));
        assertEquals(0, StrUtil.toInt(s2, 0));
    }

    public final void testTrim() {
        String s = "　\tabc 　 ";
        assertEquals("abc", StrUtil.trim(s));
    }

    public final void testTrimLeft() {
        String s = "　\tabc 　 ";
        assertEquals("abc 　 ", StrUtil.trimLeft(s));
    }

    public final void testTrimRight() {
        String s = "　\tabc 　 ";
        assertEquals("　\tabc", StrUtil.trimRight(s));
    }

    public final void testToHalfChar() {
        String s = "ａｂｃ１２３";
        assertEquals("abc123", StrUtil.toHalfChar(s));
        s = "Ｔｈｉｓ　〔中文＠〕：　ｉｓ　ａ　\n　ｔｅｓｔ";
        assertEquals("This [中文@]: is a \n test", StrUtil.toHalfChar(s));
    }

    public final void testToChineseFullChar() {
        String s = "abc123";
        assertEquals("ａｂｃ１２３", StrUtil.toChineseFullChar(s));
        s = "This [中文@]: is a \n test";
        assertEquals("Ｔｈｉｓ　〔中文＠〕：　ｉｓ　ａ　\n　ｔｅｓｔ", StrUtil.toChineseFullChar(s));
    }

    public final void testPrint() {
        String o1 = "this is a test";
        String o2 = null;
        Object o3 = TEST_STRING;
        assertEquals(o1, StrUtil.print(o1));
        assertEquals("", StrUtil.print(o2));
        assertEquals(TEST_STRING, StrUtil.print(o3));
    }
    
    public final void testPrintWithDefault() {
    	String s1 = "abc";
    	String s2 = "def";
    	String s3 = null;
    	String s4 = "xyz";
    	assertEquals("abc", StrUtil.printWithDefault(null, s1, s2, s3, s4));
    	
    	s1 = s2 = null;
    	assertEquals("xyz", StrUtil.printWithDefault(null, s1, s2, s3, s4));
    }

    public final void testEscape() {
        String s = "abc'def";
        assertEquals("abc\\'def", StrUtil.escape(s, '\'', '\\'));
        
        s = "abc\ndef";
        assertEquals("abc\\\ndef", StrUtil.escape(s, '\n', '\\'));
        
        s = "abc'def'ghi";
        assertEquals("abc\\'def\\'ghi", StrUtil.escape(s, '\'', '\\'));
    }
    
    public void testJoin() {
    	assertEquals("", StrUtil.join(","));
    	assertEquals("", StrUtil.join(",", new String[0]));
    	assertEquals("", StrUtil.join(",", new int[0]));
    	assertEquals("null", StrUtil.join(",", (String)null));
    	assertEquals("1", StrUtil.join(",", 1));
    	assertEquals("1", StrUtil.join(",", new int[] { 1 }));
    	assertEquals("true", StrUtil.join(",", true));
    	assertEquals("1", StrUtil.join(",", new Integer(1)));
    	assertEquals("true", StrUtil.join(",", new boolean[] { true }));
    	assertEquals("true", StrUtil.join(",", new Boolean[] { true }));
    	assertEquals("1,2,3,4,5", StrUtil.join(",", new int[] { 1, 2, 3, 4, 5 }));
    	assertEquals("1,2,3,4,5", StrUtil.join(",", 1, 2, 3, 4, 5));
    	assertEquals("(1),(2),(3),(4),(5)", StrUtil.join(",", 1, 2, 3, 4, 5, new StrUtil.ToStringHandler() {
			@Override public String toString(Object e) {
				return "(" + StrUtil.print(e) + ")";
			}
		}));
    	
        String[] s = { "1", "2", " 3", "4 ", " 5 ", "6", null };
        assertEquals("1,2, 3,4 , 5 ,6,null", StrUtil.join(",", s));
        assertEquals("1,2, 3,4 , 5 ,6,null", StrUtil.join(",", "1", "2", " 3", "4 ", " 5 ", "6", (String)null));
        assertEquals("1,2, 3,4 , 5 ,6,null", StrUtil.join(",", '1', 2, " 3", "4 ", " 5 ", "6", (String)null));
        
        int[] s2 = { 1, 2, 3, 4, 5, 6 };
        assertEquals("1,2,3,4,5,6", StrUtil.join(",", s2));
        assertEquals("1,2,3,4,5,6", StrUtil.join(",", 1, 2, 3, 4, 5, 6));
        assertEquals("1,2,3,4,5.0,6", StrUtil.join(",", 1, 2, 3, 4, 5.0f, 6)); //!
        
        float[] s3 = { 1.0F, 2.1F, 3.2F, 4.3F, 5.4F };
        assertEquals("1.0, 2.1, 3.2, 4.3, 5.4", StrUtil.join(", ", s3));
        assertEquals("1.0, 2.1, 3.2, 4.3, 5.4", StrUtil.join(", ", 1.0F, 2.1F, 3.2F, 4.3F, 5.4F));
        
        char[] s4 = { 'a', 'b', 'c', 'd', 'e' };
        assertEquals("abcde", StrUtil.join("", s4));
        assertEquals("abcde", StrUtil.join("", 'a', 'b', 'c', 'd', 'e'));
        
        Object[] s5 = { 'a', "b", 0, true, 1.2 };
        assertEquals("a,b,0,true,1.2", StrUtil.join(",", s5));
        assertEquals("a,b,0,true,1.2", StrUtil.join(",", 'a', "b", 0, true, 1.2));
        
        Object[] s6 = { 'a', "b", (String)null, 1, true, 1.2 };
        assertEquals("a,b,,1,true,1.2", StrUtil.join(",", s6, new StrUtil.ToStringHandler() {
			@Override public String toString(Object e) {
				return StrUtil.print(e);
			}
		}));
        assertEquals("a,b,,1,true,1.2", StrUtil.join(",", 'a', "b", (String)null, 1, true, 1.2, new StrUtil.ToStringHandler() {
			@Override public String toString(Object e) {
				return StrUtil.print(e);
			}
		}));
    }

    public void testIndexOf() {
    	final String src = "中文abc12345xyz測試mnop";
    	final String s1 = "測試";
    	final String s2 = "abc";
    	final String s3 = "XyZ";
    	
    	assertEquals(13, StrUtil.indexOf(src, s1));
    	assertEquals(2, StrUtil.indexOf(src, s2));
    	assertEquals(-1, StrUtil.indexOf(src, s3));
    	
    	assertEquals(13, StrUtil.indexOf(src, s1, 10));
    	assertEquals(-1, StrUtil.indexOf(src, s2, 3));
    	assertEquals(-1, StrUtil.indexOf(src, s3, 10));
    	
    	assertEquals(13, StrUtil.indexOfIgnoreCase(src, s1));
    	assertEquals(2, StrUtil.indexOfIgnoreCase(src, s2));
    	assertEquals(10, StrUtil.indexOfIgnoreCase(src, s3));
    	
    	assertEquals(13, StrUtil.indexOfIgnoreCase(src, s1, 10));
    	assertEquals(-1, StrUtil.indexOfIgnoreCase(src, s2, 3));
    	assertEquals(10, StrUtil.indexOfIgnoreCase(src, s3, 10));
    }
    
    public void testString() {
    	assertEquals("z", StrUtil.string("z"));
    	assertEquals("", StrUtil.string(null)); //被視為 Object[] s = null;
    	assertEquals("null", StrUtil.string(new Object[] { null }));
        assertEquals("123", StrUtil.string(123));
        assertEquals("zy", StrUtil.string("z", "y"));
        assertEquals("zy", StrUtil.string(new String[] { "z", "y" }));
        assertEquals("zynullkkk", StrUtil.string("z", 'y', null, "kkk"));
        assertEquals("zykkk0true1.5", StrUtil.string("z", 'y', "kkk", 0, true, 1.5));
    }
}
