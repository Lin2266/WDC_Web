package com.test.commons.util;

import java.util.*;
import static org.junit.Assert.*;
import org.junit.Test;

public class DateUtilTest {

    @Test
    public void testToCalendarObj() throws Exception {
        String s1 = "2005-12-13 14:15:16";
        String s2 = "2006/01/12";
        String s3 = "20051213 14:05:16";
        String s4 = "20060112";
        String s5 = "20051213141516";
        Calendar c1 = DateUtil.toCalendarObj(s1);
        Calendar c2 = DateUtil.toCalendarObj(s2);
        Calendar c3 = DateUtil.toCalendarObj(s3);
        Calendar c4 = DateUtil.toCalendarObj(s4);
        Calendar c5 = DateUtil.toCalendarObj(s5);
        assertEquals(2005, c1.get(Calendar.YEAR));
        assertEquals(12, c1.get(Calendar.MONTH) + 1);
        assertEquals(13, c1.get(Calendar.DATE));
        assertEquals(14, c1.get(Calendar.HOUR_OF_DAY));
        assertEquals(15, c1.get(Calendar.MINUTE));
        assertEquals(16, c1.get(Calendar.SECOND));
        assertEquals(2006, c2.get(Calendar.YEAR));
        assertEquals(1, c2.get(Calendar.MONTH) + 1);
        assertEquals(12, c2.get(Calendar.DATE));
        assertEquals(2005, c3.get(Calendar.YEAR));
        assertEquals(12, c3.get(Calendar.MONTH) + 1);
        assertEquals(13, c3.get(Calendar.DATE));
        assertEquals(14, c3.get(Calendar.HOUR_OF_DAY));
        assertEquals(5, c3.get(Calendar.MINUTE));
        assertEquals(16, c3.get(Calendar.SECOND));
        assertEquals(2006, c4.get(Calendar.YEAR));
        assertEquals(1, c4.get(Calendar.MONTH) + 1);
        assertEquals(12, c4.get(Calendar.DATE));
        assertEquals(2005, c5.get(Calendar.YEAR));
        assertEquals(12, c5.get(Calendar.MONTH) + 1);
        assertEquals(13, c5.get(Calendar.DATE));
        assertEquals(14, c5.get(Calendar.HOUR_OF_DAY));
        assertEquals(15, c5.get(Calendar.MINUTE));
        assertEquals(16, c5.get(Calendar.SECOND));
    }

    @Test
    public void testToDate() throws Exception {
        String s = "2005-12-13 14:15:16";
        String s1 = "20051213 14:15:16";
        String s2 = "20051213141516";
        Calendar c = Calendar.getInstance();
        c.set(2005, 12 - 1, 13, 14, 15, 16);
        //相差不到 1 秒就視為相同
        assertTrue((DateUtil.toDate(s).getTime() - c.getTimeInMillis()) < 1000);
        assertTrue((DateUtil.toDate(s1).getTime() - c.getTimeInMillis()) < 1000);
        assertTrue((DateUtil.toDate(s2).getTime() - c.getTimeInMillis()) < 1000);
    }
    
    @Test
    public void testToDate2() throws Exception {
    	String s = "2005-12-13 14:15:16.123";
    	String format = "yyyy-MM-dd HH:mm:ss.SSS";
    	Date d = DateUtil.toDate(s, format);
    	
    	Calendar cal = Calendar.getInstance();
    	cal.setTime(d);
    	assertEquals(2005, cal.get(Calendar.YEAR));
    	assertEquals(12, cal.get(Calendar.MONTH) + 1);
    	assertEquals(13, cal.get(Calendar.DAY_OF_MONTH));
    	assertEquals(14, cal.get(Calendar.HOUR_OF_DAY));
    	assertEquals(15, cal.get(Calendar.MINUTE));
    	assertEquals(16, cal.get(Calendar.SECOND));
    	assertEquals(123, cal.get(Calendar.MILLISECOND));
    	
    	s = "2005-12-13 14:15:16.123";
    	format = "yyyy-MM-dd HH:mm:ss.SS"; //長度與日期字串不一樣
    	d = DateUtil.toDate(s, format);
    	assertNull(d);
    	
    	s = "2015-02-29 14:15:16.123"; //非閏年
    	format = "yyyy-MM-dd HH:mm:ss.SSS";
    	d = DateUtil.toDate(s, format);
    	//assertNull(d);
    	cal.setTime(d);
    	assertEquals(2015, cal.get(Calendar.YEAR));
    	assertEquals(3, cal.get(Calendar.MONTH) + 1); //由 Date 物件自動調整日期
    	assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
    	assertEquals(14, cal.get(Calendar.HOUR_OF_DAY));
    	assertEquals(15, cal.get(Calendar.MINUTE));
    	assertEquals(16, cal.get(Calendar.SECOND));
    	assertEquals(123, cal.get(Calendar.MILLISECOND));
    	
    	s = "2005/02/13 14:15";
    	format = "yyyy-MM-dd HH:mm";
    	d = DateUtil.toDate(s, format);
    	cal.setTime(d);
    	assertEquals(2005, cal.get(Calendar.YEAR));
    	assertEquals(2, cal.get(Calendar.MONTH) + 1);
    	assertEquals(13, cal.get(Calendar.DAY_OF_MONTH));
    	assertEquals(14, cal.get(Calendar.HOUR_OF_DAY));
    	assertEquals(15, cal.get(Calendar.MINUTE));
    	assertEquals(0, cal.get(Calendar.SECOND));
    	assertEquals(0, cal.get(Calendar.MILLISECOND));
    }

    @Test
    public void testToTwDateString() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.set(2006, 1, 23);
        //assertEquals("095-02-23", DateUtil.toTwDateString(cal.getTime()));
        assertEquals("095-02-23", DateUtil.formatTW(cal.getTime(), "yyy-MM-dd"));
    }

    @Test
    public void testToTwDateCompactString() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.set(2006, 1, 23);
        //assertEquals("0950223", DateUtil.toTwDateCompactString(cal.getTime()));
        assertEquals("0950223", DateUtil.formatTW(cal.getTime(), "yyyMMdd"));
    }

//    @Test
//    public void testToTwDateTimeString() throws Exception {
//        Calendar cal = Calendar.getInstance();
//        cal.set(2012, 1, 23, 1, 10, 20);
//        assertEquals("101-02-23 01:10:20", DateUtil.toTwDateTimeString(cal.getTime()));
//        cal.set(2006, 1, 23, 1, 10, 20);
//        assertEquals("095-02-23 01:10:20", DateUtil.toTwDateTimeString(cal.getTime()));
//        cal.set(1920, 1, 23, 1, 10, 20);
//        assertEquals("009-02-23 01:10:20", DateUtil.toTwDateTimeString(cal.getTime()));
//    }

//    @Test
//    public void testToTwDateTimeString2() throws Exception {
//        Calendar cal = Calendar.getInstance();
//        cal.set(2012, 1, 23, 1, 10, 20);
//        assertEquals("101-02-23 01:10:20", DateUtil.toTwDateTimeString(cal.getTime(), "yy-MM-dd HH:mm:ss"));
//        cal.set(2006, 1, 23, 1, 10, 20);
//        assertEquals("95-02-23 01:10:20", DateUtil.toTwDateTimeString(cal.getTime(), "yy-MM-dd HH:mm:ss"));
//        cal.set(1913, 1, 23, 1, 10, 20);
//        assertEquals("02-02-23 01:10:20", DateUtil.toTwDateTimeString(cal.getTime(), "yy-MM-dd HH:mm:ss"));
//    }

//    @Test
//    public void testToTwDateTimeCompactString() throws Exception {
//        Calendar cal = Calendar.getInstance();
//        cal.set(2006, 1, 23, 1, 10, 20);
//        assertEquals("0950223 01:10:20", DateUtil.toTwDateTimeCompactString(cal.getTime()));
//    }
    
//    @Test
//    public void testToTwDateTimeCompactString2() throws Exception {
//        Calendar cal = Calendar.getInstance();
//        cal.set(2006, 1, 23, 1, 10, 20);
//        assertEquals("0950223011020", DateUtil.toTwDateTimeCompactString2(cal.getTime()));
//    }

    @Test
    public void testToArray() throws Exception {
        String s1 = "2005/12/13\\14#15@16";
        String s2 = "2006/03/08";
        String s3 = "0941213\\14#15@16";
        String s4 = "0950308";
        String s5 = "20051213\\14#15@16";
        String s6 = "20060308";
        String s7 = "20051213141516";
        String s8 = "0941213141516";
        String[] ss1 = DateUtil.toArray(s1);
        String[] ss2 = DateUtil.toArray(s2);
        String[] ss3 = DateUtil.toArray(s3);
        String[] ss4 = DateUtil.toArray(s4);
        String[] ss5 = DateUtil.toArray(s5);
        String[] ss6 = DateUtil.toArray(s6);
        String[] ss7 = DateUtil.toArray(s7);
        String[] ss8 = DateUtil.toArray(s8);
        assertTrue(ss1[0].equals("2005") && ss1[1].equals("12") && ss1[2].equals("13") &&
                ss1[3].equals("14") && ss1[4].equals("15") && ss1[5].equals("16"));
        assertTrue(ss2[0].equals("2006") && ss2[1].equals("3") && ss2[2].equals("8"));
        assertTrue(ss3[0].equals("94") && ss3[1].equals("12") && ss3[2].equals("13") &&
                ss3[3].equals("14") && ss3[4].equals("15") && ss3[5].equals("16"));
        assertTrue(ss4[0].equals("95") && ss4[1].equals("3") && ss4[2].equals("8"));
        assertTrue(ss5[0].equals("2005") && ss5[1].equals("12") && ss5[2].equals("13") &&
                ss5[3].equals("14") && ss5[4].equals("15") && ss5[5].equals("16"));
        assertTrue(ss6[0].equals("2006") && ss6[1].equals("3") && ss6[2].equals("8"));
        assertTrue(ss7[0].equals("2005") && ss7[1].equals("12") && ss7[2].equals("13") &&
                ss7[3].equals("14") && ss7[4].equals("15") && ss7[5].equals("16"));
        assertTrue(ss8[0].equals("94") && ss8[1].equals("12") && ss8[2].equals("13") &&
                ss8[3].equals("14") && ss8[4].equals("15") && ss8[5].equals("16"));
    }

    @Test
    public void testToArray2() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.set(2006, 1, 23, 3, 12, 33);
        String[] ss = DateUtil.toArray(cal.getTime());
        assertEquals("2006", ss[0]);
        assertEquals("2", ss[1]);
        assertEquals("23", ss[2]);
        assertEquals("3", ss[3]);
        assertEquals("12", ss[4]);
        assertEquals("33", ss[5]);
    }
    
    @Test
    public void testToTimeArray() throws Exception {
    	String s1 = "141516";
        String s2 = "141516567";
        String s3 = "14:15:16";
        String s4 = "14:15:16.789";
        String s5 = "14  15   16";
        String s6 = "14  15   16.789";
        String[] ss1 = DateUtil.toTimeArray(s1);
        String[] ss2 = DateUtil.toTimeArray(s2);
        String[] ss3 = DateUtil.toTimeArray(s3);
        String[] ss4 = DateUtil.toTimeArray(s4);
        String[] ss5 = DateUtil.toTimeArray(s5);
        String[] ss6 = DateUtil.toTimeArray(s6);
        assertTrue("14".equals(ss1[0]) && "15".equals(ss1[1]) && "16".equals(ss1[2]));
        assertTrue("14".equals(ss2[0]) && "15".equals(ss2[1]) && "16".equals(ss2[2]) && "567".equals(ss2[3]));
        assertTrue("14".equals(ss3[0]) && "15".equals(ss3[1]) && "16".equals(ss3[2]));
        assertTrue("14".equals(ss4[0]) && "15".equals(ss4[1]) && "16".equals(ss4[2]) && "789".equals(ss4[3]));
        assertTrue("14".equals(ss5[0]) && "15".equals(ss5[1]) && "16".equals(ss5[2]));
        assertTrue("14".equals(ss6[0]) && "15".equals(ss6[1]) && "16".equals(ss6[2]) && "789".equals(ss6[3]));
    }

    @Test
    public void testToTwDateTimeArray() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.set(2006, 1, 23, 3, 12, 33);
        String[] ss = DateUtil.toTwDateTimeArray(cal.getTime());
        assertEquals("95", ss[0]);
        assertEquals("2", ss[1]);
        assertEquals("23", ss[2]);
        assertEquals("3", ss[3]);
        assertEquals("12", ss[4]);
        assertEquals("33", ss[5]);
    }

    @Test
    public void testIsDateFormatValid() throws Exception {
        String s1 = "2005-12-12 12:12:12";
        String s2 = "2005-2-12 12:12:12.40";
        String s3 = "2005-12-12";
        String s4 = "20060229";
        String s5 = "20050212 12:12:12.40";
        String s6 = "20050212121212";
        String s7 = "2005021212121240";
        String s8 = "20050212121212040";
        assertTrue(DateUtil.isDateFormatValid(s1));
        assertTrue(DateUtil.isDateFormatValid(s2));
        assertTrue(DateUtil.isDateFormatValid(s3));
        assertFalse(DateUtil.isDateFormatValid(s4));
        assertTrue(DateUtil.isDateFormatValid(s5));
        assertTrue(DateUtil.isDateFormatValid(s6));
        assertTrue(!DateUtil.isDateFormatValid(s7));
        assertTrue(DateUtil.isDateFormatValid(s8));
    }

    @Test
    public void testIsDateFormatValid2() throws Exception {
        String s1 = "2005 * 12 *12 12 ,12 + 12";
        String s2 = "2005 @ 12 !2";
        String s3 = "20051212 12 ,12 + 12";
        assertTrue(DateUtil.isDateFormatValid(s1));
        assertTrue(DateUtil.isDateFormatValid(s2));
        assertTrue(DateUtil.isDateFormatValid(s3));
    }

    @Test
    public void testIsDateFormatValid3() throws Exception {
        String s1 = "1996-02-29 12:12:12"; //潤年
        String s2 = "1900-02-29 12:12:12"; //非潤年
        String s3 = "19960229121212"; //潤年
        String s4 = "19000229121212"; //非潤年
        assertTrue(DateUtil.isDateFormatValid(s1));
        assertTrue(!DateUtil.isDateFormatValid(s2));
        assertTrue(DateUtil.isDateFormatValid(s3));
        assertTrue(!DateUtil.isDateFormatValid(s4));
    }

    @Test
    public void testIsDateFormatValid4() throws Exception {
        String s1 = "1996-02-29"; //潤年
        String s2 = "1900-02-29"; //非潤年
        String s3 = "19960229"; //潤年
        String s4 = "19000229"; //非潤年
        assertTrue(DateUtil.isDateFormatValid(s1));
        assertTrue(!DateUtil.isDateFormatValid(s2));
        assertTrue(DateUtil.isDateFormatValid(s3));
        assertTrue(!DateUtil.isDateFormatValid(s4));
    }

    @Test
    public void testIsDateValueValid() throws Exception {
        String[] ss1 = new String[] { "2005", "12", "12", "15", "15", "50", "999" };
        String[] ss2 = new String[] { "100", "12", "12", "15", "15", "50", "7" };
        String[] ss3 = new String[] { "2000", "2", "30", "15", "15", "50", "0" }; //潤年
        String[] ss4 = new String[] { "2005", "13", "12", "20", "15", "50", "0" };
        String[] ss5 = new String[] { "2005", "12", "32", "20", "15", "50", "0" };
        String[] ss6 = new String[] { "2005", "12", "12", "24", "15", "50", "0" };
        String[] ss7 = new String[] { "2005", "12", "12", "20", "61", "50", "0" };
        String[] ss8 = new String[] { "1998", "2", "29", "15", "15", "50", "0" }; //非潤年的 2 月
        assertTrue(DateUtil.isDateValueValid(ss1));
        assertTrue(DateUtil.isDateValueValid(ss2));
        assertTrue(!DateUtil.isDateValueValid(ss3));
        assertTrue(!DateUtil.isDateValueValid(ss4));
        assertTrue(!DateUtil.isDateValueValid(ss5));
        assertTrue(!DateUtil.isDateValueValid(ss6));
        assertTrue(!DateUtil.isDateValueValid(ss7));
        assertTrue(!DateUtil.isDateValueValid(ss8));
    }

    @Test
    public void testToROCComplexForm() throws Exception {
        String s1 = "2005/12/30 10:00:00.1";
        String s2 = "2005/12/30";
        String s3 = "1909/12/30";
        assertEquals("中華民國九十四年十二月三十日十時零分零秒一毫秒", DateUtil.toROCComplexForm(s1));
        assertEquals("中華民國九十四年十二月三十日", DateUtil.toROCComplexForm(s2));
        assertEquals("民國前三年十二月三十日", DateUtil.toROCComplexForm(s3));
    }

    @Test
    public void testToROCComplexForm2() throws Exception {
        String[] ss1 = new String[] { "2005", "12", "30", "10", "00", "00", "1" };
        String[] ss2 = new String[] { "2005", "12", "30" };
        String[] ss3 = new String[] { "1909", "12", "30" };
        assertEquals("中華民國九十四年十二月三十日十時零分零秒一毫秒", DateUtil.toROCComplexForm(ss1));
        assertEquals("中華民國九十四年十二月三十日", DateUtil.toROCComplexForm(ss2));
        assertEquals("民國前三年十二月三十日", DateUtil.toROCComplexForm(ss3));
    }

    @Test
    public void testToROCForm() throws Exception {
        String[] ss1 = new String[] { "2005", "12", "30", "10", "00", "00", "100" };
        String[] ss2 = new String[] { "2005", "12", "30" };
        String[] ss3 = new String[] { "1909", "12", "30" };
        assertEquals("玖拾肆年壹拾貳月參拾日壹拾時零分零秒壹佰毫秒", DateUtil.toROCForm(ss1, false, true));
        assertEquals("中華民國玖拾肆年壹拾貳月參拾日壹拾時零分零秒壹佰毫秒", DateUtil.toROCForm(ss1, true, true));
        assertEquals("中華民國玖拾肆年壹拾貳月參拾日", DateUtil.toROCForm(ss2, true, true));
        assertEquals("民國前參年壹拾貳月參拾日", DateUtil.toROCForm(ss3, true, true));
        assertEquals("民國前參年壹拾貳月參拾日", DateUtil.toROCForm(ss3, false, true));
    }

//    @Test
//    public void testTwDateStringToDate() throws Exception {
//        String s1 = "94/12/14";
//        String s2 = "95/1/5";
//        String s3 = "0941214";
//        String s4 = "0950105";
//        assertEquals("Wed Dec 14 00:00:00", DateUtil.twDateStringToDate(s1).toString().substring(0, 19));
//        assertEquals("Thu Jan 05 00:00:00", DateUtil.twDateStringToDate(s2).toString().substring(0, 19));
//        assertEquals("Wed Dec 14 00:00:00", DateUtil.twDateStringToDate(s3).toString().substring(0, 19));
//        assertEquals("Thu Jan 05 00:00:00", DateUtil.twDateStringToDate(s4).toString().substring(0, 19));
//    }

//    @Test
//    public void testTwDateStringToTimestamp() throws Exception {
//        String s1 = "94/01/12";
//        String s2 = "95/11/23";
//        String s3 = "0940112";
//        String s4 = "0951123";
//        assertEquals("2005-01-12 00:00:00", DateUtil.twDateStringToTimestamp(s1).toString().substring(0, 19));
//        assertEquals("2006-11-23 00:00:00", DateUtil.twDateStringToTimestamp(s2).toString().substring(0, 19));
//        assertEquals("2005-01-12 00:00:00", DateUtil.twDateStringToTimestamp(s3).toString().substring(0, 19));
//        assertEquals("2006-11-23 00:00:00", DateUtil.twDateStringToTimestamp(s4).toString().substring(0, 19));
//    }

//    @Test
//    public void testTwDateStringToDate2() throws Exception {
//        String s1 = "094-10-21 04:15:06";
//        String s2 = "095-03-05 14:15:16";
//        String s3 = "0941021 04:15:06";
//        String s4 = "0950305 14:15:16";
//        String s5 = "0950305141516";
//        assertEquals("Fri Oct 21 04:15:06", DateUtil.twDateStringToDate(s1).toString().substring(0, 19));
//        assertEquals("Sun Mar 05 14:15:16", DateUtil.twDateStringToDate(s2).toString().substring(0, 19));
//        assertEquals("Fri Oct 21 04:15:06", DateUtil.twDateStringToDate(s3).toString().substring(0, 19));
//        assertEquals("Sun Mar 05 14:15:16", DateUtil.twDateStringToDate(s4).toString().substring(0, 19));
//        assertEquals("Sun Mar 05 14:15:16", DateUtil.twDateStringToDate(s5).toString().substring(0, 19));
//    }

//    @Test
//    public void testTwDateStringToTimestamp2() throws Exception {
//        String s1 = "94/01/12 3 4 5";
//        String s2 = "95/11/23 21:03:05";
//        String s3 = "0940112 3 4 5";
//        String s4 = "0951123 21:03:05";
//        String s5 = "0951123210305";
//        assertEquals("2005-01-12 03:04:05", DateUtil.twDateStringToTimestamp(s1).toString().substring(0, 19));
//        assertEquals("2006-11-23 21:03:05", DateUtil.twDateStringToTimestamp(s2).toString().substring(0, 19));
//        assertEquals("2005-01-12 03:04:05", DateUtil.twDateStringToTimestamp(s3).toString().substring(0, 19));
//        assertEquals("2006-11-23 21:03:05", DateUtil.twDateStringToTimestamp(s4).toString().substring(0, 19));
//        assertEquals("2006-11-23 21:03:05", DateUtil.twDateStringToTimestamp(s5).toString().substring(0, 19));
//    }

    @Test
    public void testConvertADDateToTwDateArray() throws Exception {
        String s1 = "2005/05/28";
        String s2 = "1998/12/08 03:11:12";
        String s3 = "1910/05/28";
        String s4 = "20050528";
        String s5 = "19981208 03:11:12";
        String s6 = "19100528";
        String s7 = "19981208031112";
        String s8 = "19101208031112";
        String[] ss1 = DateUtil.adDateToTwDateArray(s1);
        String[] ss2 = DateUtil.adDateToTwDateArray(s2);
        String[] ss3 = DateUtil.adDateToTwDateArray(s3);
        String[] ss4 = DateUtil.adDateToTwDateArray(s4);
        String[] ss5 = DateUtil.adDateToTwDateArray(s5);
        String[] ss6 = DateUtil.adDateToTwDateArray(s6);
        String[] ss7 = DateUtil.adDateToTwDateArray(s7);
        String[] ss8 = DateUtil.adDateToTwDateArray(s8);
        assertTrue(ss1[0].equals("94") && ss1[1].equals("5") && ss1[2].equals("28"));
        assertTrue(ss2[0].equals("87") && ss2[1].equals("12") && ss2[2].equals("8") && ss2[3].equals("3") && ss2[4].equals("11") && ss2[5].equals("12"));
        assertTrue(ss3[0].equals("-2") && ss3[1].equals("5") && ss3[2].equals("28"));
        assertTrue(ss4[0].equals("94") && ss4[1].equals("5") && ss4[2].equals("28"));
        assertTrue(ss5[0].equals("87") && ss5[1].equals("12") && ss5[2].equals("8") && ss5[3].equals("3") && ss5[4].equals("11") && ss5[5].equals("12"));
        assertTrue(ss6[0].equals("-2") && ss6[1].equals("5") && ss6[2].equals("28"));
        assertTrue(ss7[0].equals("87") && ss7[1].equals("12") && ss7[2].equals("8") && ss7[3].equals("3") && ss7[4].equals("11") && ss7[5].equals("12"));
        assertTrue(ss8[0].equals("-2") && ss8[1].equals("12") && ss8[2].equals("8") && ss8[3].equals("3") && ss8[4].equals("11") && ss8[5].equals("12"));
    }
    
    @Test
    public void testTwDateToCalendar() {
    	String s = "095-12-08 03:11:12.123";
    	String format = "yyy-MM-dd HH:mm:ss.SSS";
    	Calendar d = DateUtil.twDateToCalendar(s, format);
    	assertEquals(2006, d.get(Calendar.YEAR));
    	assertEquals(12, d.get(Calendar.MONTH) + 1);
    	assertEquals(8, d.get(Calendar.DAY_OF_MONTH));
    	assertEquals(3, d.get(Calendar.HOUR_OF_DAY));
    	assertEquals(11, d.get(Calendar.MINUTE));
    	assertEquals(12, d.get(Calendar.SECOND));
    	assertEquals(123, d.get(Calendar.MILLISECOND));
    	
    	
    }
    
    @Test
    public void testConvertTwDateToADDateArray() throws Exception {
        String s1 = "-2/05/28";
        String s2 = "95/12/08 03:11:12";
        String s3 = "0951208 03:11:12";
        String s4 = "0951208031112";
        String[] ss1 = DateUtil.twDateToADDateArray(s1);
        String[] ss2 = DateUtil.twDateToADDateArray(s2);
        String[] ss3 = DateUtil.twDateToADDateArray(s3);
        String[] ss4 = DateUtil.twDateToADDateArray(s4);
        assertTrue(ss1[0].equals("1910") && ss1[1].equals("5") && ss1[2].equals("28"));
        assertTrue(ss2[0].equals("2006") && ss2[1].equals("12") && ss2[2].equals("8") && ss2[3].equals("3") && ss2[4].equals("11") && ss2[5].equals("12"));
        assertTrue(ss3[0].equals("2006") && ss3[1].equals("12") && ss3[2].equals("8") && ss3[3].equals("3") && ss3[4].equals("11") && ss3[5].equals("12"));
        assertTrue(ss4[0].equals("2006") && ss4[1].equals("12") && ss4[2].equals("8") && ss4[3].equals("3") && ss4[4].equals("11") && ss4[5].equals("12"));
    }

    @Test
    public void testAdDateToTwDate() throws Exception {
        String s1 = "2011/5/28";
        String s2 = "1998 12 08 03 11 12";
        String s3 = "1910,05,8";
        String s4 = "1998 12 08 03 11 0.2";
        String s5 = "1998 12 08 03 11 3.2";
        String s6 = "19981208 03 11 12";
        String s7 = "19100508";
        String s8 = "19981208031112";
        //assertEquals("100-05-28", DateUtil.adDateToTwDate(s1)); //deprecated
        assertEquals("100-05-28", DateUtil.formatTW(DateUtil.toDate(s1), "yyy-MM-dd"));
        //assertEquals("087-12-08 03:11:12", DateUtil.adDateToTwDate(s2)); //deprecated
        assertEquals("087-12-08 03:11:12", DateUtil.formatTW(DateUtil.toDate(s2), "yyy-MM-dd HH:mm:ss"));
        //assertEquals("-002-05-08", DateUtil.adDateToTwDate(s3)); //deprecated
        assertEquals("-002-05-08", DateUtil.formatTW(DateUtil.toDate(s3), "yyy-MM-dd"));
        //assertEquals("087-12-08 03:11:00.002", DateUtil.adDateToTwDate(s4)); //deprecated
        assertEquals("087-12-08 03:11:00.002", DateUtil.formatTW(DateUtil.toDate(s4), "yyy-MM-dd HH:mm:ss.SSS"));
        //assertEquals("087-12-08 03:11:03.002", DateUtil.adDateToTwDate(s5)); //deprecated
        assertEquals("087-12-08 03:11:03.002", DateUtil.formatTW(DateUtil.toDate(s5), "yyy-MM-dd HH:mm:ss.SSS"));
        //assertEquals("087-12-08 03:11:12", DateUtil.adDateToTwDate(s6)); //deprecated
        assertEquals("087-12-08 03:11:12", DateUtil.formatTW(DateUtil.toDate(s6), "yyy-MM-dd HH:mm:ss"));
        //assertEquals("-002-05-08", DateUtil.adDateToTwDate(s7)); //deprecated
        assertEquals("-002-05-08", DateUtil.formatTW(DateUtil.toDate(s7), "yyy-MM-dd"));
        //assertEquals("087-12-08 03:11:12", DateUtil.adDateToTwDate(s8)); //deprecated
        assertEquals("087-12-08 03:11:12", DateUtil.formatTW(DateUtil.toDate(s8), "yyy-MM-dd HH:mm:ss"));
        assertEquals("中華民國87年12月8日3時11分", DateUtil.formatTW(DateUtil.toDate(s8), "中華民國yy年M月d日H時m分"));
    }

    @Test
    public void testADDateToTwDate2() throws Exception {
        String s1 = "2011/5/28";
        String s2 = "1998 12 08 03 11 12";
        String s3 = "1910,05,8";
        String s4 = "1998 12 08 03 11 0.2";
        String s5 = "1998 12 08 03 11 3.2";
        String s6 = "19981208 03 11 12";
        String s7 = "19100508";
        String s8 = "19981208031112";
        //assertEquals("100/05/28", DateUtil.adDateToTwDate(s1, "yy/MM/dd")); //deprecated
        assertEquals("100/05/28", DateUtil.formatTW(DateUtil.toDate(s1), "yy/MM/dd"));
        //assertEquals("1000528", DateUtil.adDateToTwDate(s1, "yyyMMdd")); //deprecated
        assertEquals("1000528", DateUtil.formatTW(DateUtil.toDate(s1), "yyyMMdd"));
        //assertEquals("87-12-08 03:11:12", DateUtil.adDateToTwDate(s2, "yy-MM-dd HH:mm:ss")); //deprecated
        assertEquals("87-12-08 03:11:12", DateUtil.formatTW(DateUtil.toDate(s2), "yy-MM-dd HH:mm:ss"));
        //assertEquals("0871208 03:11:12", DateUtil.adDateToTwDate(s2, "yyyMMdd HH:mm:ss")); //deprecated
        assertEquals("0871208 03:11:12", DateUtil.formatTW(DateUtil.toDate(s2), "yyyMMdd HH:mm:ss"));
        //assertEquals("-02 05 08", DateUtil.adDateToTwDate(s3, "yy MM dd")); //deprecated
        assertEquals("-02 05 08", DateUtil.formatTW(DateUtil.toDate(s3), "yy MM dd"));
        //assertEquals("87-12-08 03:11:00", DateUtil.adDateToTwDate(s4, "yy-MM-dd HH:mm:ss")); //deprecated
        assertEquals("87-12-08 03:11:00", DateUtil.formatTW(DateUtil.toDate(s4), "yy-MM-dd HH:mm:ss"));
        //assertEquals("87-12-08 03:11:00.02", DateUtil.adDateToTwDate(s4, "yy-MM-dd HH:mm:ss.SS")); //deprecated
        assertEquals("87-12-08 03:11:00.02", DateUtil.formatTW(DateUtil.toDate(s4), "yy-MM-dd HH:mm:ss.SS"));
        //assertEquals("87-12-08 03:11:03", DateUtil.adDateToTwDate(s5, "yy-MM-dd HH:mm:ss")); //deprecated
        assertEquals("87-12-08 03:11:03", DateUtil.formatTW(DateUtil.toDate(s5), "yy-MM-dd HH:mm:ss"));
        //assertEquals("87-12-08 03:11:03.002", DateUtil.adDateToTwDate(s5, "yy-MM-dd HH:mm:ss.SSS")); //deprecated
        assertEquals("87-12-08 03:11:03.02", DateUtil.formatTW(DateUtil.toDate(s5), "yy-MM-dd HH:mm:ss.SS")); //毫秒位數完全照指定的格式, 不論對錯
        //assertEquals("87-12-08 03:11:12", DateUtil.adDateToTwDate(s6, "yy-MM-dd HH:mm:ss")); //deprecated
        assertEquals("87-12-08 03:11:12", DateUtil.formatTW(DateUtil.toDate(s6), "yy-MM-dd HH:mm:ss"));
        //assertEquals("-002-05-08", DateUtil.adDateToTwDate(s7, "yyy-MM-dd")); //deprecated
        assertEquals("-02-05-08", DateUtil.formatTW(DateUtil.toDate(s7), "yy-MM-dd")); //年的位數完全照指定的格式
        //assertEquals("87-12-08 03:11:12", DateUtil.adDateToTwDate(s8, "yy-MM-dd HH:mm:ss")); //deprecated
        assertEquals("87-12-08 03:11:12", DateUtil.formatTW(DateUtil.toDate(s8), "yy-MM-dd HH:mm:ss"));
        //assertEquals("0871208 03:11:12", DateUtil.adDateToTwDate(s8, "yyyMMdd HH:mm:ss")); //deprecated
        assertEquals("0871208 03:11:12", DateUtil.formatTW(DateUtil.toDate(s8), "yyyMMdd HH:mm:ss"));
    }

    @Test
    public void testTwDateToADDate() throws Exception {
        String s1 = "94/5/28";
        String s2 = "87 12 08 03 11 12";
        String s3 = "-1,05,8";
        String s4 = "0940528";
        String s5 = "1011208 03 11 12";
        String s6 = "1011208031112";
        //assertEquals("2005-05-28", DateUtil.twDateToADDate(s1)); //deprecated
        assertEquals("2005-05-28", DateUtil.format(DateUtil.twDateToDate(s1), "yyyy-MM-dd"));
        //assertEquals("1998-12-08 03:11:12", DateUtil.twDateToADDate(s2)); //deprecated
        assertEquals("1998-12-08 03:11:12", DateUtil.format(DateUtil.twDateToDate(s2), "yyyy-MM-dd HH:mm:ss"));
        //assertEquals("1911-05-08", DateUtil.twDateToADDate(s3)); //deprecated
        assertEquals("1911-05-08", DateUtil.format(DateUtil.twDateToDate(s3), "yyyy-MM-dd"));
        //assertEquals("2005-05-28", DateUtil.twDateToADDate(s4)); //deprecated
        assertEquals("2005-05-28", DateUtil.format(DateUtil.twDateToDate(s4), "yyyy-MM-dd"));
        //assertEquals("2012-12-08 03:11:12", DateUtil.twDateToADDate(s5)); //deprecated
        assertEquals("2012-12-08 03:11:12", DateUtil.format(DateUtil.twDateToDate(s5), "yyyy-MM-dd HH:mm:ss"));
        //assertEquals("2012-12-08 03:11:12", DateUtil.twDateToADDate(s6)); //deprecated
        assertEquals("2012-12-08 03:11:12", DateUtil.format(DateUtil.twDateToDate(s6), "yyyy-MM-dd HH:mm:ss"));
    }

    @Test
    public void testTwDateToADDate2() throws Exception {
        String s1 = "94/5/28";
        String s2 = "87 12 08 03 11 12";
        String s3 = "-1,05,8";
        String s4 = "0940528";
        String s5 = "1011208 03 11 12";
        String s6 = "87/12/08 03:11:2.5";
        String s7 = "1011208031112";
        //assertEquals("2005-05-28", DateUtil.twDateToADDate(s1, "yyyy-MM-dd")); //deprecated
        assertEquals("2005-05-28", DateUtil.format(DateUtil.twDateToDate(s1), "yyyy-MM-dd"));
        //assertEquals("1998/12/08  03:11:12", DateUtil.twDateToADDate(s2, "yyyy/MM/dd  HH:mm:ss")); //deprecated
        assertEquals("1998/12/08  03:11:12", DateUtil.format(DateUtil.twDateToDate(s2), "yyyy/MM/dd  HH:mm:ss"));
        //assertEquals("1911-05-08", DateUtil.twDateToADDate(s3, "yyyy-MM-dd")); //deprecated
        assertEquals("1911-05-08", DateUtil.format(DateUtil.twDateToDate(s3), "yyyy-MM-dd"));
        //assertEquals("2005-05-28", DateUtil.twDateToADDate(s4, "yyyy-MM-dd")); //deprecated
        assertEquals("2005-05-28", DateUtil.format(DateUtil.twDateToDate(s4), "yyyy-MM-dd"));
        //assertEquals("2012-12-08 03:11:12", DateUtil.twDateToADDate(s5, "yyyy-MM-dd HH:mm:ss")); //deprecated
        assertEquals("2012-12-08 03:11:12", DateUtil.format(DateUtil.twDateToDate(s5), "yyyy-MM-dd HH:mm:ss"));
        //assertEquals("1998/12/08  03:11:02", DateUtil.twDateToADDate(s6, "yyyy/MM/dd  HH:mm:ss")); //deprecated
        assertEquals("1998/12/08  03:11:02", DateUtil.format(DateUtil.twDateToDate(s6), "yyyy/MM/dd  HH:mm:ss"));
        //assertEquals("1998/12/08  03:11:02.005", DateUtil.twDateToADDate(s6, "yyyy/MM/dd  HH:mm:ss.SSS")); //deprecated
        assertEquals("1998/12/08  03:11:02.005", DateUtil.format(DateUtil.twDateToDate(s6), "yyyy/MM/dd  HH:mm:ss.SSS"));
        //assertEquals("2012-12-08 03:11:12", DateUtil.twDateToADDate(s7, "yyyy-MM-dd HH:mm:ss")); //deprecated
        assertEquals("2012-12-08 03:11:12", DateUtil.format(DateUtil.twDateToDate(s7), "yyyy-MM-dd HH:mm:ss"));
    }

    @Test
    public void testTwYearMonToADYearMon() throws Exception {
        String s1 = "09612";
        String s2 = "02503";
        String s3 = "00306";
        String s4 = "-00306";
        String s5 = "-00106";
        //assertEquals("200712", DateUtil.twYearMonToADYearMon(s1)); //deprecated
        assertEquals("200712", DateUtil.format(DateUtil.twDateToDate(s1 + "01"), "yyyyMM"));
        //assertEquals("193603", DateUtil.twYearMonToADYearMon(s2)); //deprecated
        assertEquals("193603", DateUtil.format(DateUtil.twDateToDate(s2 + "01"), "yyyyMM"));
        //assertEquals("191406", DateUtil.twYearMonToADYearMon(s3)); //deprecated
        assertEquals("191406", DateUtil.format(DateUtil.twDateToDate(s3 + "01"), "yyyyMM"));
        //assertEquals("190906", DateUtil.twYearMonToADYearMon(s4)); //deprecated
        assertEquals("190906", DateUtil.format(DateUtil.twDateToDate(s4 + "01"), "yyyyMM"));
        //assertEquals("191106", DateUtil.twYearMonToADYearMon(s5)); //deprecated
        assertEquals("191106", DateUtil.format(DateUtil.twDateToDate(s5 + "01"), "yyyyMM"));
    }

    @Test
    public void testADYearMonToTwYearMon() throws Exception {
        String s1 = "200712";
        String s2 = "193603";
        String s3 = "191406";
        String s4 = "190906";
        String s5 = "191106";
        //assertEquals("09612", DateUtil.adYearMonToTwYearMon(s1)); //deprecated
        assertEquals("09612", DateUtil.formatTW(DateUtil.toDate(s1 + "01"), "yyyMM"));
        //assertEquals("02503", DateUtil.adYearMonToTwYearMon(s2)); //deprecated
        assertEquals("02503", DateUtil.formatTW(DateUtil.toDate(s2 + "01"), "yyyMM"));
        //assertEquals("00306", DateUtil.adYearMonToTwYearMon(s3)); //deprecated
        assertEquals("00306", DateUtil.formatTW(DateUtil.toDate(s3 + "01"), "yyyMM"));
        //assertEquals("-00306", DateUtil.adYearMonToTwYearMon(s4)); //deprecated
        assertEquals("-00306", DateUtil.formatTW(DateUtil.toDate(s4 + "01"), "yyyMM"));
        //assertEquals("-00106", DateUtil.adYearMonToTwYearMon(s5)); //deprecated
        assertEquals("-00106", DateUtil.formatTW(DateUtil.toDate(s5 + "01"), "yyyMM"));
    }

    @Test
    public void testDiffDate() throws Exception {
        String s1 = "2005-12-01 12:00:00";
        String s2 = "2005-12-02 12:30:00";
        String s3 = "20051202 12:30:00";
        String s4 = "20051202123000";
        //相差不到一秒就算通過
        long d = Math.abs(((24 * 60 * 60 * 1000) + (30 * 60 * 1000)) - DateUtil.diffDate(s1, s2));
        assertTrue(d < 1000);
        d = Math.abs(((24 * 60 * 60 * 1000) + (30 * 60 * 1000)) - DateUtil.diffDate(s1, s3));
        assertTrue(d < 1000);
        d = Math.abs(((24 * 60 * 60 * 1000) + (30 * 60 * 1000)) - DateUtil.diffDate(s1, s4));
        assertTrue(d < 1000);
    }

    @Test
    public void testDateAdd() throws Exception {
        String s1 = "2005/12/30";
        String s2 = "20051230";
        String s3 = "2005/12/30 12:30:00";
        String s4 = "20051230 12:00:00";
        String s5 = "20051230123000";
        assertEquals("2006-01-01", DateUtil.dateAdd(s1, 2));
        assertEquals("2006-01-01", DateUtil.format(DateUtil.dateAdd(DateUtil.toDate(s1), 2), "yyyy-MM-dd"));
        assertEquals("20060101", DateUtil.dateAdd(s2, 2));
        assertEquals("20060101", DateUtil.format(DateUtil.dateAdd(DateUtil.toDate(s2), 2), "yyyyMMdd"));
        assertEquals("2006-01-01 12:30:00", DateUtil.dateAdd(s3, 2));
        assertEquals("2006-01-01 12:30:00", DateUtil.format(DateUtil.dateAdd(DateUtil.toDate(s3), 2), "yyyy-MM-dd HH:mm:ss"));
        assertEquals("20060101 12:00:00", DateUtil.dateAdd(s4, 2));
        assertEquals("20060101 12:00:00", DateUtil.format(DateUtil.dateAdd(DateUtil.toDate(s4), 2), "yyyyMMdd HH:mm:ss"));
        assertEquals("20060101123000", DateUtil.dateAdd(s5, 2));
        assertEquals("20060101123000", DateUtil.format(DateUtil.dateAdd(DateUtil.toDate(s5), 2), "yyyyMMddHHmmss"));
        assertEquals("20060101130000", DateUtil.dateAdd(s5, 2.02084)); //加 2 天又 30 分鐘
    }

    @Test
    public void testTwDateAdd() throws Exception {
        String s1 = "94/12/30";
        String s1a = "4/12/30";
        String s2 = "94/12/30 12:30:00";
        String s3 = "0941230";
        String s4 = "0941230 12:00:00";
        String s5 = "0890228 01:30:10"; //潤年
        String s6 = "0941230120000";
        assertEquals("095-01-01", DateUtil.twDateAdd(s1, 2));
        assertEquals("095-01-01", DateUtil.formatTW(DateUtil.dateAdd(DateUtil.twDateToDate(s1), 2), "yyy-MM-dd"));
        assertEquals("005-01-01", DateUtil.twDateAdd(s1a, 2));
        assertEquals("005-01-01", DateUtil.formatTW(DateUtil.dateAdd(DateUtil.twDateToDate(s1a), 2), "yyy-MM-dd"));
        assertEquals("095-01-01 12:30:00", DateUtil.twDateAdd(s2, 2));
        assertEquals("095-01-01 12:30:00", DateUtil.formatTW(DateUtil.dateAdd(DateUtil.twDateToDate(s2), 2), "yyy-MM-dd HH:mm:ss"));
        assertEquals("0950101", DateUtil.twDateAdd(s3, 2));
        assertEquals("0950101", DateUtil.formatTW(DateUtil.dateAdd(DateUtil.twDateToDate(s3), 2), "yyyMMdd"));
        assertEquals("0950101 12:00:00", DateUtil.twDateAdd(s4, 2));
        assertEquals("0950101 12:00:00", DateUtil.formatTW(DateUtil.dateAdd(DateUtil.twDateToDate(s4), 2), "yyyMMdd HH:mm:ss"));
        assertEquals("0890301 01:30:10", DateUtil.twDateAdd(s5, 2));
        assertEquals("0890301 01:30:10", DateUtil.formatTW(DateUtil.dateAdd(DateUtil.twDateToDate(s5), 2), "yyyMMdd HH:mm:ss"));
        assertEquals("0950101120000", DateUtil.twDateAdd(s6, 2));
        assertEquals("0950101120000", DateUtil.formatTW(DateUtil.dateAdd(DateUtil.twDateToDate(s6), 2), "yyyMMddHHmmss"));
    }

    @Test
    public void testFormat() throws Exception {
        String s1 = "2005 12 30";
        String s2 = "2005/12/30 - 12-00-00";
        String s3 = "20051230";
        String s4 = "2005/12/30 - 12-00-00";
        String s5 = "20051230120000";
        String s6 = "2005/12/30 12:00:00.099";
        String s7 = "20051230120000099";
        String s8 = "2005/12/30 12:00:00.99";
        assertEquals("2005-12-30", DateUtil.format(s1, "yyyy-MM-dd"));
        assertEquals("2005-12-30 12:00:00", DateUtil.format(s2, "yyyy-MM-dd HH:mm:ss"));
        assertEquals("2005-12-30", DateUtil.format(s3, "yyyy-MM-dd"));
        assertEquals("2005-12-30 12:00:00", DateUtil.format(s4, "yyyy-MM-dd HH:mm:ss"));
        assertEquals("2005-12-30 12:00:00", DateUtil.format(s5, "yyyy-MM-dd HH:mm:ss"));
        assertEquals("2005-12-30 12:00:00.099", DateUtil.format(s6, "yyyy-MM-dd HH:mm:ss.SSS"));
        assertEquals("2005-12-30 12:00:00.099", DateUtil.format(s7, "yyyy-MM-dd HH:mm:ss.SSS"));
        assertEquals("2005-12-30 12:00:00.099", DateUtil.format(s8, "yyyy-MM-dd HH:mm:ss.SSS"));
    }
    
    @Test
    public void testFormat2() throws Exception {
        String s1 = "2005-12-13 14:15:16";
        String s2 = "2006/01/12";
        Calendar c = Calendar.getInstance();

        c.set(2005, 12 - 1, 13, 14, 15, 16);
        assertEquals("2005-12-13 14:15:16", DateUtil.format(c.getTime(), "yyyy-MM-dd HH:mm:ss"));

        c.set(2006, 1 - 1, 12);
        assertEquals("20060112", DateUtil.format(c.getTime(), "yyyyMMdd"));
    }
    
    @Test
    public void testFormat3() throws Exception {
    	Calendar c = Calendar.getInstance();
    	System.out.println("testFormat3(): " + DateUtil.format(c.getTime(), "yyyy-MM-dd HH:mm:ss"));
    	c.setTimeZone(TimeZone.getTimeZone("UTC"));
    	System.out.println("testFormat3(): UTC => " + DateUtil.format(c, "yyyy-MM-dd HH:mm:ss"));
    }

    @Test
    public void testFormatTW() throws Exception {
        String s1 = "94 12 30";
        String s2 = "94/12/30 - 12-00-00";
        String s3 = "0941230";
        String s4 = "94/12/30 - 12-00-00";
        String s5 = "0941230120000";
        assertEquals("094-12-30", DateUtil.formatTW(s1, "yyy-MM-dd"));
        assertEquals("094-12-30 12:00:00", DateUtil.formatTW(s2, "yyy-MM-dd HH:mm:ss"));
        assertEquals("094-12-30", DateUtil.formatTW(s3, "yyy-MM-dd"));
        assertEquals("094-12-30 12:00:00", DateUtil.formatTW(s4, "yyy-MM-dd HH:mm:ss"));
        assertEquals("094-12-30 12:00:00", DateUtil.formatTW(s5, "yyy-MM-dd HH:mm:ss"));
        
        String s6 = "101 12 30";
        String s7 = "101/12/30 - 12-00-00";
        String s8 = "1011230";
        String s9 = "101/12/30 - 12-00-00";
        String s10 = "1011230120000";
        assertEquals("101-12-30", DateUtil.formatTW(s6, "yy-MM-dd"));
        assertEquals("101-12-30 12:00:00", DateUtil.formatTW(s7, "yy-MM-dd HH:mm:ss"));
        assertEquals("101-12-30", DateUtil.formatTW(s8, "yy-MM-dd"));
        assertEquals("101-12-30 12:00:00", DateUtil.formatTW(s9, "yy-MM-dd HH:mm:ss"));
        assertEquals("101-12-30 12:00:00", DateUtil.formatTW(s10, "yy-MM-dd HH:mm:ss"));
        
        String s11 = "-01 12 30";
        String s12 = "-01/12/30 - 12-00-00";
        String s13 = "-0011230";
        String s14 = "-01/12/30 - 12-00-00";
        String s15 = "-0011230120000";
        assertEquals("-001-12-30", DateUtil.formatTW(s11, "yyy-MM-dd"));
        assertEquals("-001-12-30 12:00:00", DateUtil.formatTW(s12, "yyy-MM-dd HH:mm:ss"));
        assertEquals("-01-12-30", DateUtil.formatTW(s13, "yy-MM-dd"));
        assertEquals("-01-12-30 12:00:00", DateUtil.formatTW(s14, "yy-MM-dd HH:mm:ss"));
        assertEquals("-01-12-30 12:00:00", DateUtil.formatTW(s15, "yy-MM-dd HH:mm:ss"));
    }
    
    @Test
    public void testFormatTime() throws Exception {
        String s1 = "12-34-56";
        String s2 = "12 34 56.789";
        String s3 = "12 34 56.78";
        
        assertEquals("12:34:56", DateUtil.formatTime(s1, "HH:mm:ss"));
        assertEquals("12:34:56.000", DateUtil.formatTime(s1, "HH:mm:ss.SSS"));
        assertEquals("123456", DateUtil.formatTime(s1, "HHmmss"));
        assertEquals("123456000", DateUtil.formatTime(s1, "HHmmssSSS"));
        assertEquals("12:34:56.789", DateUtil.formatTime(s2, "HH:mm:ss.SSS"));
        assertEquals("12:34:56.789", DateUtil.formatTime(s2, "HH:mm:ss.SS"));
        assertEquals("12:34:56.78", DateUtil.formatTime(s3, "HH:mm:ss.SS"));
        assertEquals("12:34:56.078", DateUtil.formatTime(s3, "HH:mm:ss.SSS"));
    }
    
    //測試與 testToTwDateTimeString() 同
    //@Test
    //public void testFormatTW2() throws Exception {
    //	
    //}
    
    @Test
    public void testGetMaxDayOfMonth() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, 10, 10); //20071110
        Date d1 = cal.getTime();
        cal.set(2007, 9, 10); //20071010
        Date d2 = cal.getTime();
        cal.set(2007, 1, 10); //20070210
        Date d3 = cal.getTime();
        cal.set(2000, 1, 10); //20000210, 閏年
        Date d4 = cal.getTime();

        assertEquals(30, DateUtil.getMaxDayOfMonth(d1).intValue());
        assertEquals(31, DateUtil.getMaxDayOfMonth(d2).intValue());
        assertEquals(28, DateUtil.getMaxDayOfMonth(d3).intValue());
        assertEquals(29, DateUtil.getMaxDayOfMonth(d4).intValue());
    }
    
    @Test
    public void testTruncate() throws Exception {
    	Calendar cal = Calendar.getInstance();
        cal.set(2007, 10, 10, 13, 23, 33);
        cal.set(Calendar.MILLISECOND, 100); //20071110 13:23:33.100
        Date d = cal.getTime();
        
        //精確至分
        Calendar d1 = Calendar.getInstance();
        d1.setTime(DateUtil.truncate(d, DateUtil.MINUTE));
        assertEquals(2007, d1.get(Calendar.YEAR));
        assertEquals(10, d1.get(Calendar.MONTH)); //11月
        assertEquals(10, d1.get(Calendar.DATE));
        assertEquals(13, d1.get(Calendar.HOUR_OF_DAY));
        assertEquals(23, d1.get(Calendar.MINUTE));
        assertEquals(0, d1.get(Calendar.SECOND));
        assertEquals(0, d1.get(Calendar.MILLISECOND));
        
        //精確至日
        Calendar d2 = Calendar.getInstance();
        d2.setTime(DateUtil.truncate(d, DateUtil.DATE));
        assertEquals(2007, d2.get(Calendar.YEAR));
        assertEquals(10, d2.get(Calendar.MONTH)); //11月
        assertEquals(10, d2.get(Calendar.DATE));
        assertEquals(0, d2.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, d2.get(Calendar.MINUTE));
        assertEquals(0, d2.get(Calendar.SECOND));
        assertEquals(0, d2.get(Calendar.MILLISECOND));
    }
    
    @Test
    public void testAvailableTimeZoneIDs() {
    	System.out.println("testAvailableTimeZoneIDs(): " + StrUtil.join(", ", DateUtil.availableTimeZoneIDs()));
    }
    
    @Test
	public void testLocalTimeZoneName() {
		System.out.println("testLocalTimeZoneName(): " + DateUtil.localTimeZoneName());
	}
    
    @Test
	public void testLocalTimeZoneName2() {
		Locale locale = Locale.ENGLISH;
		System.out.println("testLocalTimeZoneName2(): locale=Locale.ENGLISH => " + DateUtil.localTimeZoneName(locale));
		locale = Locale.TRADITIONAL_CHINESE;
		System.out.println("testLocalTimeZoneName2(): locale=Locale.TRADITIONAL_CHINESE => " + DateUtil.localTimeZoneName(locale));
	}
    
    @Test
	public void testLocalTimeZoneShortName() {
		System.out.println("testLocalTimeZoneShortName(): " + DateUtil.localTimeZoneShortName());
	}
    
    @Test
	public void testLocalTimeZoneID() {
		System.out.println("testLocalTimeZoneID(): " + DateUtil.localTimeZoneID());
	}
    
    @Test
	public void testDateWithTimeZone() {
		Calendar now2 = Calendar.getInstance();
		String nowTxt = valueOf(now2);
		Date now = now2.getTime();
		
		Calendar cal = DateUtil.dateWithTimeZone(now, "Asia/Taipei");
		System.out.println("testDateWithTimeZone(): " + nowTxt + " => (Asia/Taipei) " + valueOf(cal));
		cal = DateUtil.dateWithTimeZone(now, "Taipei"); //得到 UTC 時間
		System.out.println("testDateWithTimeZone(): " + nowTxt + " => (Taipei) " + valueOf(cal));
		cal = DateUtil.dateWithTimeZone(now, "UTC");
		System.out.println("testDateWithTimeZone(): " + nowTxt + " => (UTC) " + valueOf(cal));
	}
    
//    @Test
//    public void testTwDateStringToCalendar() {
//    	String s = "099-01-22 23:48:32";
//    	Calendar c = DateUtil.twDateStringToCalendar(s);
//    	assertEquals(2010, c.get(Calendar.YEAR));
//    	assertEquals(0, c.get(Calendar.MONTH));
//    	assertEquals(22, c.get(Calendar.DATE));
//    	assertEquals(23, c.get(Calendar.HOUR_OF_DAY));
//    	assertEquals(48, c.get(Calendar.MINUTE));
//    	assertEquals(32, c.get(Calendar.SECOND));
//    	
//    	
//    }
    
    @Test
    public void testGetDateRightBoundForMonth() {
    	Calendar cal = Calendar.getInstance();
    	cal.set(Calendar.YEAR, 2013);
    	cal.set(Calendar.MONTH, 11); //12 月
    	cal.set(Calendar.DATE, 31);
    	Date input = cal.getTime();
    	Date output = DateUtil.getDateRightBoundForMonth(input);
    	
    	assertEquals(2014, DateUtil.getYear(output).intValue());
    	assertEquals(1, DateUtil.getMonth(output).intValue());
    	assertEquals(1, DateUtil.getDay(output).intValue());
    	assertEquals(0, DateUtil.getHour(output).intValue());
    	assertEquals(0, DateUtil.getMinute(output).intValue());
    	assertEquals(0, DateUtil.getSecond(output).intValue());
    }
    
    @Test
    public void testGetDateRightBoundForDay() {
    	Calendar cal = Calendar.getInstance();
    	cal.set(Calendar.YEAR, 2014);
    	cal.set(Calendar.MONTH, 5); //6 月
    	cal.set(Calendar.DATE, 26);
    	cal.set(Calendar.HOUR_OF_DAY, 0);
    	cal.set(Calendar.MINUTE, 0);
    	cal.set(Calendar.SECOND, 0);
    	cal.set(Calendar.MILLISECOND, 0);
    	Date input = cal.getTime();
    	Date output = DateUtil.getDateRightBoundForDay(input);
    	
    	assertEquals(2014, DateUtil.getYear(output).intValue());
    	assertEquals(6, DateUtil.getMonth(output).intValue());
    	assertEquals(27, DateUtil.getDay(output).intValue());
    	assertEquals(0, DateUtil.getHour(output).intValue());
    	assertEquals(0, DateUtil.getMinute(output).intValue());
    	assertEquals(0, DateUtil.getSecond(output).intValue());
    }
    
    @Test
    public void testSetHourMinuteSecond() {
    	Calendar cal = Calendar.getInstance();
    	cal.set(Calendar.YEAR, 2014);
    	cal.set(Calendar.MONTH, 5); //6 月
    	cal.set(Calendar.DATE, 26);
    	cal.set(Calendar.HOUR_OF_DAY, 0);
    	cal.set(Calendar.MINUTE, 0);
    	cal.set(Calendar.SECOND, 0);
    	cal.set(Calendar.MILLISECOND, 300);
    	Date input = cal.getTime(); //2014-06-26 00:00:00.300
    	
    	Calendar output = Calendar.getInstance();
    	output.setTime(DateUtil.setTime(input, 5, 30, 30, 0)); //2014-06-26 05:30:30.000
    	assertEquals(2014, output.get(Calendar.YEAR));
    	assertEquals(5, output.get(Calendar.MONTH));
    	assertEquals(26, output.get(Calendar.DATE));
    	assertEquals(5, output.get(Calendar.HOUR_OF_DAY));
    	assertEquals(30, output.get(Calendar.MINUTE));
    	assertEquals(30, output.get(Calendar.SECOND));
    	assertEquals(0, output.get(Calendar.MILLISECOND));
    }
    
    @Test
    public void testSetTime() {
    	Calendar cal = Calendar.getInstance();
    	cal.set(Calendar.YEAR, 2014);
    	cal.set(Calendar.MONTH, 5); //6 月
    	cal.set(Calendar.DATE, 26);
    	cal.set(Calendar.HOUR_OF_DAY, 0);
    	cal.set(Calendar.MINUTE, 0);
    	cal.set(Calendar.SECOND, 0);
    	cal.set(Calendar.MILLISECOND, 0);
    	Date input = cal.getTime(); //2014-06-26 00:00:00.000
    	
    	Calendar output = Calendar.getInstance();
    	output.setTime(DateUtil.setTime(input, 5, 30, 30, 300)); //2014-06-26 05:30:30.300
    	assertEquals(2014, output.get(Calendar.YEAR)); //不變
    	assertEquals(5, output.get(Calendar.MONTH)); //不變
    	assertEquals(26, output.get(Calendar.DATE)); //不變
    	assertEquals(5, output.get(Calendar.HOUR_OF_DAY));
    	assertEquals(30, output.get(Calendar.MINUTE));
    	assertEquals(30, output.get(Calendar.SECOND));
    	assertEquals(300, output.get(Calendar.MILLISECOND));
    }
    
    @Test
    public void testSetDate() {
    	Date d = DateUtil.setDate(new Date(), 2015, 1, 1);
    	Date d1 = DateUtil.setDate(d, 2015, 1, 0); //理應變成前一天
    	Date d2 = DateUtil.setDate(d, 2015, 0, 1); //理應變成上個月的同一日
    	Date d3 = DateUtil.setDate(d, 2015, 4, 31); //理應 2015-04-31 -> 2015-05-01
    	
    	//證明 setDate(d, ...) 中的 d 並未受到更動
    	assertEquals(2015, DateUtil.getYear(d).intValue());
    	assertEquals(1, DateUtil.getMonth(d).intValue());
    	assertEquals(1, DateUtil.getDay(d).intValue());
    	
    	//看日期設為 0 是否代表為上個月的最後一天
    	assertEquals(2014, DateUtil.getYear(d1).intValue());
    	assertEquals(12, DateUtil.getMonth(d1).intValue());
    	assertEquals(31, DateUtil.getDay(d1).intValue());
    	
    	//看月份設為 0 是否代表為上個月的同一天
    	assertEquals(2014, DateUtil.getYear(d2).intValue());
    	assertEquals(12, DateUtil.getMonth(d2).intValue());
    	assertEquals(1, DateUtil.getDay(d2).intValue());
    	
    	//看是否 2015-04-31 -> 2015-05-01
    	assertEquals(2015, DateUtil.getYear(d3).intValue());
    	assertEquals(5, DateUtil.getMonth(d3).intValue());
    	assertEquals(1, DateUtil.getDay(d3).intValue());
    }
    
    @Test
    public void testSetDatetime() { //實為測試 DateUtil.setDate()
    	Calendar cal = Calendar.getInstance();
    	cal.set(Calendar.YEAR, 2014);
    	cal.set(Calendar.MONTH, 5); //6 月
    	cal.set(Calendar.DATE, 26);
    	cal.set(Calendar.HOUR_OF_DAY, 0);
    	cal.set(Calendar.MINUTE, 0);
    	cal.set(Calendar.SECOND, 0);
    	cal.set(Calendar.MILLISECOND, 0);
    	Date input = cal.getTime(); //2014-06-26 00:00:00.000
    	
    	Calendar output = Calendar.getInstance();
    	output.setTime(DateUtil.setDate(input, 2015, 7, 31, 5, 30, 30, 300)); //2015-07-31 05:30:30.300
    	assertEquals(2015, output.get(Calendar.YEAR));
    	assertEquals(6, output.get(Calendar.MONTH)); //7 月
    	assertEquals(31, output.get(Calendar.DATE));
    	assertEquals(5, output.get(Calendar.HOUR_OF_DAY));
    	assertEquals(30, output.get(Calendar.MINUTE));
    	assertEquals(30, output.get(Calendar.SECOND));
    	assertEquals(300, output.get(Calendar.MILLISECOND));
    }
    
    @Test
    public void testAsDate() {
    	final Calendar now = Calendar.getInstance();
    	
    	{ //只設年月, 漏掉日 => 應得到 null
	    	final Date d = DateUtil.asDate(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, null);
	    	assertNull(d);
    	}
    	
    	{//未設值秒, 毫秒欄位
    		final Date d = DateUtil.asDate(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH), 
    				now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));
    		final Calendar d1 = Calendar.getInstance();
	    	d1.setTime(d);
	    	assertEquals(now.get(Calendar.YEAR), d1.get(Calendar.YEAR));
	    	assertEquals(now.get(Calendar.MONTH), d1.get(Calendar.MONTH));
	    	assertEquals(now.get(Calendar.DAY_OF_MONTH), d1.get(Calendar.DAY_OF_MONTH));
	    	assertEquals(now.get(Calendar.HOUR_OF_DAY), d1.get(Calendar.HOUR_OF_DAY));
	    	assertEquals(now.get(Calendar.MINUTE), d1.get(Calendar.MINUTE));
	    	assertEquals(0, d1.get(Calendar.SECOND));
	    	assertEquals(0, d1.get(Calendar.MILLISECOND));
    	}
    }
    
    @Test
    public void testGetWeek() {
    	Calendar cal = Calendar.getInstance();
    	cal.set(Calendar.YEAR, 2015);
    	cal.set(Calendar.MONTH, 6); //7 月
    	cal.set(Calendar.DATE, 29);
    	cal.set(Calendar.HOUR_OF_DAY, 0);
    	cal.set(Calendar.MINUTE, 0);
    	cal.set(Calendar.SECOND, 0);
    	cal.set(Calendar.MILLISECOND, 0);
    	Date input = cal.getTime(); //2015-07-29 00:00:00.000
    	
    	assertEquals(3, DateUtil.getWeek(input).intValue());
    }
    
    @Test
    public void testIsEqualToYear() {
    	Calendar cal = Calendar.getInstance(); //2019+
    	Calendar cal2 = Calendar.getInstance();
    	cal2.set(Calendar.YEAR, 2018);
    	assertFalse(DateUtil.isEqualToYear(cal.getTime(), cal2.getTime()));
    	
    	cal = Calendar.getInstance();
    	cal2 = Calendar.getInstance();
    	cal.set(Calendar.DAY_OF_MONTH, 20); //日不同
    	cal2.set(Calendar.DAY_OF_MONTH, 21);
    	assertTrue(DateUtil.isEqualToYear(cal.getTime(), cal2.getTime()));
    	assertTrue(DateUtil.isEqualToMonth(cal.getTime(), cal2.getTime()));
    	assertFalse(DateUtil.isEqualToDate(cal.getTime(), cal2.getTime())); //日(含)以下應該都算不同
    	assertFalse(DateUtil.isEqualToHour(cal.getTime(), cal2.getTime()));
    	assertFalse(DateUtil.isEqualToMinute(cal.getTime(), cal2.getTime()));
    	assertFalse(DateUtil.isEqualToSecond(cal.getTime(), cal2.getTime()));
    }
    
    @Test
    public void testIsEqualsSecond() {
    	Calendar cal = Calendar.getInstance();
    	Calendar cal2 = Calendar.getInstance();
    	cal.set(Calendar.MILLISECOND, 2); //毫秒不同
    	cal2.set(Calendar.MILLISECOND, 3);
    	assertTrue(DateUtil.isEqualToSecond(cal.getTime(), cal2.getTime()));
    	assertFalse(cal.equals(cal2));
    }
    
    String valueOf(Calendar cal) {
		return StrUtil.alignRight(String.valueOf(cal.get(Calendar.YEAR)), 4, '0') + "-" + 
				StrUtil.alignRight(String.valueOf(cal.get(Calendar.MONTH) + 1), 2, '0') + "-" + 
				StrUtil.alignRight(String.valueOf(cal.get(Calendar.DATE)), 2, '0') + " " + 
				StrUtil.alignRight(String.valueOf(cal.get(Calendar.HOUR_OF_DAY)), 2, '0') + ":" + 
				StrUtil.alignRight(String.valueOf(cal.get(Calendar.MINUTE)), 2, '0') + ":" + 
				StrUtil.alignRight(String.valueOf(cal.get(Calendar.SECOND)), 2, '0');
	}
}
