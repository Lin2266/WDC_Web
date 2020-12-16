package com.test.commons.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 與日期時間處理相關的共用程式.
 * 本類別下, 以<b>西元年日期</b>為主的函數者, 輸入日期為字串者, 容許格式為下列之一:
 * <ul>
 * <li><code>yyyy MM dd hh mm ss(.SSS)</code>  //<i>西元年, 以非數字字元隔開, 後可接小數點接毫秒數 1 至 3 碼</i>
 * <li><code>yyyy MM dd</code>  //<i>西元年, 以非數字字元隔開</i>
 * <li><code>yyyyMMdd HH mm ss(.SSS)</code>  //<i>西元年, 年月日緊靠, 時分秒以非數字字元隔開, 後可接小數點接毫秒數 1 至 3 碼</i>
 * <li><code>yyyyMMddHHmmss(SSS)</code>  //<i>西元年, 全部緊靠, 14 碼或 17 碼, 可接毫秒數固定 3 碼</i>
 * <li><code>yyyyMMdd</code>  //<i>西元年, 年月日緊靠, 8 碼</i>
 * </ul>
 * <p>
 *
 * 如果是特別為<b>民國日期</b>輸入而設的函數, 其輸入的日期字串格式可為:
 * <ul>
 * <li><code>yyyMMdd HH mm ss(.SSS)</code>  //<i>民國年, 年月日緊靠, 時分秒以非數字字元隔開, 後可接小數點接毫秒數 1 至 3 碼</i>
 * <li><code>yyyMMddHHmmss(SSS)</code>  //<i>民國年, 全部緊靠, 13 碼或 16 碼, 可接毫秒數固定 3 碼</i>
 * <li><code>yyyMMDD</code>  //<i>民國年, 年月日緊靠, 7 碼</i>
 * <li><code>yyy MM dd HH mm ss(.SSS)</code>  //<i>民國年, 以非數字字元隔開, 後可接小數點接毫秒數 1 至 3 碼</i>
 * <li><code>yyy MM dd</code>  //<i>民國年, 以非數字字元隔開</i>
 * </ul>
 * <p>
 *
 * 如果是輸入參數字串屬<b>單純的時分秒</b>暫, 其格式可為:
 * <ul>
 * <li><code>HH mm ss(.SSS)</code>  //<i>以非數字字元隔開, 後可接小數點接毫秒數 1 至 3 碼</i>
 * <li><code>HHmmss(SSS)</code> (時分秒緊靠, 6 碼或 9 碼, , 可接毫秒數固定 3 碼)<br>
 * </ul>
 *
 * <b>注意</b>:
 * <ul>
 * <li>以上的格式均未計入負號. 如果年份為負, 則另外在年份前加上負號.
 * <li>在毫秒位數不可能超過三位數; 在時分秒有分隔字元的情況下, 毫秒數前的分隔字元固定為<b>小數點號</b>
 * </ul>
 *
 * <p>&nbsp;<p>
 * TODO: 本工具各 method 不擲出 exception 時, 發生問題只是傳回 null.
 *
 * @since 2005/12/28
 */
public class DateUtil {
    private static final GregorianCalendar gCal = new GregorianCalendar(); //不拿來取日期用
    private static final ThreadLocal<Matcher[]> _matchers = new ThreadLocal<Matcher[]>();

    /** 代表時間欄位: 年 */
    public static final int YEAR = 1; //Calendar.YEAR
    /** 代表時間欄位: 月 */
    public static final int MONTH = 2; //Calendar.MONTH
    /** 代表時間欄位: 日 */
    public static final int DATE = 5; //Calendar.DATE
    /** 代表時間欄位: 時(24小時制) */
    public static final int HOUR = 11; //Calendar.HOUR_OF_DAY
    /** 代表時間欄位: 分 */
    public static final int MINUTE = 12; //Calendar.MINUTE
    /** 代表時間欄位: 秒 */
    public static final int SECOND = 13; //Calendar.SECOND
    /** 代表時間欄位: 毫秒 */
    public static final int MILLISECOND = 14; //Calendar.MILLISECOND

    private DateUtil() {}

    private static final Matcher[] _getMatchers() {
        Matcher[] matchers = _matchers.get();
        if(matchers == null) {
            matchers = new Matcher[10]; //以下有幾個 _getXxxxxxMatcher() 就要配幾個 Matcher
            _matchers.set(matchers);
        }
        return matchers;
    }

    ////////////// _getXxxxxxMatcher() start

    //matchers[0], 民國年, 年月日間沒有分隔符號, 日時分秒間則可用任意非數字字元分隔, 秒後可能接小數點接毫秒數(1 至 3 碼): "YYYMMDD hh mm ss(.SSS)"
    private static final Matcher _getTwDateTimeCompactMatcher() {
        Matcher matcher = _getMatchers()[0];
        if(matcher == null) {
            matcher = Pattern.compile("^(-?\\d{3})(\\d{2})(\\d{2})(\\D+)(\\d+)(\\D+)(\\d+)(\\D+)(\\d+)(\\.\\d{0,3})?").matcher(""); //10組
            //年月日: 3 groups, 年月日時分秒: 9 groups
            _getMatchers()[0] = matcher;
        }
        return matcher;
    }

    //matchers[1], 民國年, 年月日時分秒間沒有分隔符號, 固定為 "YYYMMDDhhmmss(SSS)" (13 或 16 碼)的型式
    private static final Matcher _getTwDateTimeCompactMatcher2() {
        Matcher matcher = _getMatchers()[1];
        if(matcher == null) {
            matcher = Pattern.compile("^(-?\\d{3})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{3})?").matcher(""); //7組
            _getMatchers()[1] = matcher;
        }
        return matcher;
    }

    //matchers[2], 民國年, 年月日間沒有分隔符號, 固定為 "YYYMMDD" (7 碼)的型式
    private static final Matcher _getTwDateCompactMatcher() {
        Matcher matcher = _getMatchers()[2];
        if(matcher == null) {
            matcher = Pattern.compile("^(-?\\d{3})(\\d{2})(\\d{2})").matcher(""); //3組
            _getMatchers()[2] = matcher;
        }
        return matcher;
    }

    //matchers[3], 西元年, 年月日間沒有分隔符號， 日時分秒間則可用任意非數字字元分隔, 秒後可能接小數點接毫秒數(1 至 3 碼)： "YYYYMMDD hh mm ss(.SSS)"
    private static final Matcher _getAdDateTimeCompactMatcher() {
        Matcher matcher = _getMatchers()[3];
        if(matcher == null) {
            matcher = Pattern.compile("^(-?\\d{4})(\\d{2})(\\d{2})(\\D+)(\\d+)(\\D+)(\\d+)(\\D+)(\\d+)(\\.\\d{0,3})?").matcher(""); //10組
            _getMatchers()[3] = matcher;
        }
        return matcher;
    }

    //matchers[4], 西元年, 年月日時分秒間沒有分隔符號, 固定為 "YYYYMMDDhhmmss(SSS)" (14 或 17 碼)的型式
    private static final Matcher _getAdDateTimeCompactMatcher2() {
        Matcher matcher = _getMatchers()[4];
        if(matcher == null) {
            matcher = Pattern.compile("^(-?\\d{4})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{3})?").matcher(""); //7組
            _getMatchers()[4] = matcher;
        }
        return matcher;
    }

    //matchers[5], 西元年, 年月日間沒有分隔符號, 固定為 "YYYYMMDD" 的型式
    private static final Matcher _getAdDateCompactMatcher() {
        Matcher matcher = _getMatchers()[5];
        if(matcher == null) {
            matcher = Pattern.compile("^(-?\\d{4})(\\d{2})(\\d{2})").matcher(""); //3組
            _getMatchers()[5] = matcher;
        }
        return matcher;
    }

    //matchers[6], 年 月 日 時 分 秒(可能帶小數點連接毫秒數, 1 至 3 碼)
    private static final Matcher _getDateTimePatternMatcher() {
        Matcher matcher = _getMatchers()[6];
        if(matcher == null) {
            matcher = Pattern.compile("^(-?\\d+)(\\D+)(\\d+)(\\D+)(\\d+)(\\D+)(\\d+)(\\D+)(\\d+)(\\D+)(\\d+)(\\.\\d{0,3})?").matcher(""); //12組
            _getMatchers()[6] = matcher;
        }
        return matcher;
    }

    //matchers[7], 年 月 日
    private static final Matcher _getDatePatternMatcher() {
        Matcher matcher = _getMatchers()[7];
        if(matcher == null) {
            matcher = Pattern.compile("^(-?\\d+)(\\D+)(\\d+)(\\D+)(\\d+)").matcher(""); //5組
            _getMatchers()[7] = matcher;
        }
        return matcher;
    }

    //matchers[8], 時分秒(毫秒) HHmmss(SSS)(緊靠, 6 碼或 9 碼)
    private static final Matcher _getTimeCompactPatternMatcher() {
    	Matcher matcher = _getMatchers()[8];
    	if(matcher == null) {
    		matcher = Pattern.compile("(\\d{2})(\\d{2})(\\d{2})(\\d{3})?").matcher(""); //3+1 組(含毫秒)
    		_getMatchers()[8] = matcher;
    	}
    	return matcher;
    }

    //matchers[9], 時 分 秒(.毫秒) HH mm ss (.SSS) (有分隔字元, 可能帶小數點加毫秒數 1 至 3 碼)
    private static final Matcher _getTimePatternMatcher() {
    	Matcher matcher = _getMatchers()[9];
    	if(matcher == null) {
    		matcher = Pattern.compile("(\\d+)(\\D+)(\\d+)(\\D+)(\\d+)(\\.\\d{0,3})?").matcher(""); //5+1 組(含毫秒)
    		_getMatchers()[9] = matcher;
    	}
    	return matcher;
    }

    ////////////// _getXxxxxxMatcher() end

    /**
     * 將輸入的西元日期字串包裹為 java.util.Calendar 物件(使用 runtime 所在的時區).
     * @param s 日期字串. 如果輸入格式不合格, 則傳回 null.
     * @return java.util.Calendar 物件.
     */
    public static Calendar toCalendarObj(final String s) {
        try {
            return toCalendarObj(toArray(s));
        } catch(Exception e) {}
        return null;
    }

    /**
     * 將輸入的西元日期字串包裹為 java.util.Calendar 物件(使用 runtime 所在的時區).
     * @param ss 西元日期 [ 年, 月, 日, 時, 分, 秒, 毫秒(optional) ] 或 [ 年, 月, 日 ].<br>
     * 		如果年, 時, 分, 秒, 毫秒欄位為空, 則視為 0;<br>
     * 		如果月, 日欄位為空, 則視為 1
     * @return java.util.Calendar 物件.
     */
    public static Calendar toCalendarObj(final String ... ss) {
    	try {
    		if(ss == null || ss.length < 3)
    			return null;
    		final int[] ss2 = new int[(ss.length == 3) ? 3 : 7];
    		int ii = ss2.length;
    		if(ii > ss.length)
    			ii = ss.length;

    		for(int i = 0; i < ii; i++) {
    			if(StrUtil.isEmpty(ss[i])) {
    				ss2[i] = (i == 1 || i == 2) ? 1 : 0; //i=1:月欄位, i=2:日欄位
    			} else {
    				ss2[i] = Integer.parseInt(ss[i]);
    			}
    		}
    		return toCalendarObj(ss2);
    	} catch(Exception e) {}
        return null;
    }

    /**
     * 將輸入的西元日期欄位值包裹為 java.util.Calendar 物件(使用 runtime 所在的時區).
     * @param ss 西元日期 [ 年, 月, 日, 時, 分, 秒, 毫秒(optional) ] 或 [ 年, 月, 日 ] 各部位的數值. 如果輸入格式不合格, 則傳回 null.
     * @return java.util.Calendar 物件.
     */
    public static Calendar toCalendarObj(final int ... ss) {
    	try {
    		if(ss == null)
    			return null;
    		final Calendar cal = Calendar.getInstance();
            cal.set((ss.length > 0) ? ss[0] : 0, //西元年 
            		(ss.length > 1) ? (ss[1] - 1) : 0, //月 
    				(ss.length > 2) ? ss[2] : 1, //日
            		(ss.length > 3) ? ss[3] : 0, //時
    				(ss.length > 4) ? ss[4] : 0, //分
					(ss.length > 5) ? ss[5] : 0); //秒
            cal.set(Calendar.MILLISECOND, (ss.length > 6) ? ss[6] : 0);
            return cal;
    	} catch(Exception e) {}
        return null;
    }

    /**
     * 將輸入的西元日期字串包裹為 java.util.Date 物件(依照 runtime 所在的時區解析).
     * @param s 日期字串. 如果輸入格式不合格, 則傳回 null.
     * @return java.util.Date 物件.
     */
    public static Date toDate(final String s) {
        try {
            if(s == null)
                return null;
            final Calendar cal = toCalendarObj(s);
            return (cal == null) ? null : cal.getTime();
        } catch(Exception e) {}
        return null;
    }

    /**
     * 參數 s 值將參照 format 中出現的下列關鍵字所對應的數字字串, 各自轉成日期欄位值, 組成日期物件.
     * @param s 任意格式的日期字串.
     * @param format 與輸入值 s 對應的日期格式. 注意事項:
     *   <ul>
     *     <li>format 格式與輸入值 s 不符者(長度也得一樣), 傳回 null
     *     <li>format 中所缺少的日期欄位, 將化為 0 值
     *     <li>若 format 中的日期欄位有重複者, 只有第一個欄位有作用
     *     <li>若 format 為 null 或空字串者, 則視為本 DateUtil 可接受輸入的預設日期格式(見本 class 之說明)
     *   </ul>
     *   &nbsp;&nbsp; 其日期欄位格式字串如下:
     *   <ul>
     *     <li>年: yyyy 或 yyy 或 yy 或 y(西元年)
     *     <li>月: MM 或 M
     *     <li>日: dd 或 d
     *     <li>時: HH 或 H(24 小時制)
     *     <li>分: mm 或 m
     *     <li>秒: ss 或 s
     *     <li>毫秒: SSS 或 SS 或 S
     *   </ul>
     * @return java.util.Date 物件, 轉換失敗時傳回 null
     * @see DateUtil
     */
    public static Date toDate(final String s, final String format) {
    	try {
	    	if(isEmpty(s))
	    		return null;
	    	if(isEmpty(format))
	    		return toDate(s);
	    	if(s.length() != format.length())
	    		return null;

	    	final String[][] cols = {
	    			{ "yyyy", "yyy", "yy", "y" },
	    			{ "MM", "M" },
	    			{ "dd", "d" },
	    			{ "HH", "H" },
	    			{ "mm", "m" },
	    			{ "ss", "s" },
	    			{ "SSS", "SS", "S" }
	    	};
	    	final int[] values = { 0, 0, 0, 0, 0, 0, 0 }; //year, month, day, hour, minute, second, ms, 長度同 cols[]
	    	int n;

	    	for(int i = 0; i < cols.length; i++) {
	    		for(int j = 0; j < cols[i].length; j++) {
		    		if((n = format.indexOf(cols[i][j])) != -1) {
		    			values[i] = Integer.parseInt(s.substring(n, n + cols[i][j].length()));
		    			break; //第一個對應到的日期欄位值才算數
		    		}
		    	}
	    	}

	    	final Calendar cal = toCalendarObj(values);
            return (cal == null) ? null : cal.getTime();
    	} catch(Exception e) {}
        return null;
    }

    /**
     * 將輸入的西元日期字串包裹為 java.sql.Timestamp 物件(依照 runtime 所在的時區解析).
     * @param s 日期字串. 如果輸入格式不合格, 則傳回 null.
     * @return java.sql.Timestamp 物件.
     */
    public static Timestamp toTimestamp(final String s) {
    	try {
    		if(s == null)
                return null;
    		final Calendar cal = toCalendarObj(s);
            if(cal == null)
                return null;
            return new Timestamp(cal.getTimeInMillis());
    	} catch(Exception e) {}
        return null;
    }

    /**
     * 將 java.util.Date 物件轉為民國日期字串陣列(依 runtime 所在的時區轉成年月日).
     * @return 民國日期字串陣列(年月日)
     */
    public static String[] toTwDateArray(final Date date) {
        try {
        	final String[] ss = toArray(date);
            if(ss == null)
            	return null;

            int y = Integer.parseInt(ss[0]) - 1911;
            if(y <= 0)
            	y--;
            ss[0] = StrUtil.alignRight(String.valueOf(Math.abs(y)), 3, '0');
            if(y < 0)
            	ss[0] = "-" + ss[0];
            return new String[] { ss[0], ss[1], ss[2] };
        } catch(Exception e) {}
        return null;
    }

    /**
     * 將 java.util.Date 物件轉為民國日期時間字串陣列(依照 runtime 所在的時區轉出年月日時分秒值).
     * @return 民國日期字串陣列(年月日時分秒, 月份由 1 起算)
     */
    public static String[] toTwDateTimeArray(final Date date) {
        try {
        	final String[] ss = toArray(date);
            if(ss == null)
            	return null;

            int y = Integer.parseInt(ss[0]) - 1911;
            if(y <= 0)
            	y--;
            ss[0] = String.valueOf(Math.abs(y));
            if(y < 0)
            	ss[0] = "-" + ss[0];
            return ss;
        } catch(Exception e) {}
        return null;
    }

    /**
     * 將輸入的日期字串拆解為年, 月, 日 (或再加上 時, 分, 秒, 毫秒) 的字串陣列, 年可能為西元年或民國年.
     * @return 年, 月(自 1 起算), 日(, 時, 分, 秒, 毫秒) 的字串陣列. 如果輸入格式不合格, 則傳回 null.
     */
    public static String[] toArray(final String s) {
        try {
            if(s == null)
                return null;
            Matcher m;
            if(!(m = _getTwDateTimeCompactMatcher().reset(s)).matches())
                if(!(m = _getTwDateTimeCompactMatcher2().reset(s)).matches())
                    if(!(m = _getTwDateCompactMatcher().reset(s)).matches())
                        if(!(m = _getAdDateTimeCompactMatcher().reset(s)).matches())
                            if(!(m = _getAdDateTimeCompactMatcher2().reset(s)).matches())
                                if(!(m = _getAdDateCompactMatcher().reset(s)).matches())
                                    if(!(m = _getDateTimePatternMatcher().reset(s)).matches())
                                        if(!(m = _getDatePatternMatcher().reset(s)).matches())
                                            return null;

            final int groups = m.groupCount();
            final String[] ss = new String[(groups == 3 || groups == 5) ? 3 : 7];
            String tmp;
            if(groups == 3) { //年月日
                for(int i = 0, j = ss.length; i < j; i++)
                    ss[i] = _trimLeftZero(m.group(i + 1));
            } else if(groups == 5) { //年_月_日
                for(int i = 0, j = ss.length; i < j; i++)
                    ss[i] = _trimLeftZero(m.group(2 * (i + 1) - 1));
            } else if(groups == 7) { //年月日時分秒(毫秒)
                for(int i = 0; i < 6; i++)
                    ss[i] = _trimLeftZero(m.group(i + 1));
                //毫秒
                ss[6] = ((tmp = m.group(7)) == null) ? "0" : _trimLeftZero(tmp);
            } else if(groups == 10) { //年月日_時_分_秒(.毫秒)
                for(int i = 0; i < 3; i++)
                    ss[i] = _trimLeftZero(m.group(i + 1));
                for(int i = 3; i < 6; i++)
                    ss[i] = _trimLeftZero(m.group(2 * i - 1));
                //毫秒
                ss[6] = ((tmp = m.group(10)) == null || ".".equals(tmp)) ? "0" : _trimLeftZero(tmp.substring(1));
            } else { //groups == 12, 年_月_日_時_分_秒(.毫秒)
                for(int i = 0; i < 6; i++)
                    ss[i] = _trimLeftZero(m.group(2 * (i + 1) - 1));
                //毫秒
                ss[6] = ((tmp = m.group(12)) == null || ".".equals(tmp)) ? "0" : _trimLeftZero(tmp.substring(1));
            }

            return ss;
        } catch(Exception e) {}
        return null;
    }

    /**
     * 將輸入的日期轉為拆解為西元年, 月, 日, 時, 分, 秒, 毫秒 的字串陣列(依照 runtime 所在的時區轉出年月日時分秒之值).
     * @return 年, 月, 日, 時, 分, 秒, 毫秒 的字串陣列(月份由 1 開始計).
     */
    public static String[] toArray(final Date d) {
        try {
            if(d == null)
                return null;
            final Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            return new String[] {
                String.valueOf(cal.get(Calendar.YEAR)),
                String.valueOf(cal.get(Calendar.MONTH) + 1),
                String.valueOf(cal.get(Calendar.DAY_OF_MONTH)),
                String.valueOf(cal.get(Calendar.HOUR_OF_DAY)),
                String.valueOf(cal.get(Calendar.MINUTE)),
                String.valueOf(cal.get(Calendar.SECOND)),
                String.valueOf(cal.get(Calendar.MILLISECOND))
            };
        } catch(Exception e) {}
        return null;
    }

    /**
     * 將輸入的時分秒字串轉為拆解為時, 分, 秒(, 毫秒) 的字串陣列.
     * @return 時, 分, 秒(, 毫秒) 的字串陣列.
     */
    public static String[] toTimeArray(final String t) {
    	try {
    		if(t == null)
    			return null;
    		Matcher m;
    		if(!(m = _getTimeCompactPatternMatcher()).reset(t).matches()) //4 組
    			if(!(m = _getTimePatternMatcher()).reset(t).matches()) //6 組
    				return null;

    		final int groups = m.groupCount();
    		final String[] ss = new String[4];
    		String tmp;
    		if(groups == 4) { //時分秒(毫秒)
    			for(int i = 0; i < 3; i++)
                    ss[i] = _trimLeftZero(m.group(i + 1));
    			ss[3] = ((tmp = m.group(4)) == null) ? "0" : _trimLeftZero(tmp); //毫秒
    		} else { //groups = 6, 時_分_秒(.毫秒)
    			for(int i = 0; i < 3; i++)
                    ss[i] = _trimLeftZero(m.group(2 * (i + 1) - 1));
    			ss[3] = ((tmp = m.group(6)) == null || ".".equals(tmp)) ? "0" : _trimLeftZero(tmp.substring(1));
    		}

    		return ss;
    	} catch(Exception e) {}
    	return null;
    }

    /** 檢查西元日期格式是否合乎 "YYYY MM DD hh mm ss" 或 "YYYY MM DD" 的要求, 且日期值是否合理. */
    public static boolean isDateFormatValid(final String s) {
    	try {
        	return isEmpty(s) ? false : isDateValueValid(toArray(s));
    	} catch(Exception e) {}
    	return false;
    }

    /** 檢查西元日期 [ 年, 月, 日, 時(optional), 分(optional), 秒(optional), 毫秒(optional) ] 的值是否合理. */
    public static boolean isDateValueValid(final String ... ss) {
    	try {
	        if(ss == null || ss.length < 3)
	            return false;

	        final int[] ss2 = new int[(ss.length == 3) ? 3 : 7];
	        int ii = ss2.length;
	        if(ii > ss.length)
	        	ii = ss.length;

	        for(int i = 0; i < ii; i++) {
        		if(isEmpty(ss[i])) {
        			if(i < 3)
        				return false;
        			ss2[i] = 0; //時分秒毫秒欄可為 0
        		} else {
        			ss2[i] = Integer.parseInt(ss[i]);
        		}
	        }
	        return isDateValueValid(ss2);
    	} catch(Exception e) {}
    	return false;
    }

    /** 檢查西元日期 [ 年, 月, 日, 時(optional), 分(optional), 秒(optional), 毫秒(optional) ] 的值是否合理. */
    public static boolean isDateValueValid(final int ... ss) {
    	if(ss == null || ss.length < 3)
            return false;

        //檢查月
    	final int year = ss[0];
    	final int month = ss[1];
    	final int day = ss[2];
        if(month < 1 || month > 12)
            return false;

        //檢查日
        if(day < 1)
            return false;
        if(month == 2) {
            if(gCal.isLeapYear(year)) {
                if(day > 29)
                    return false;
            } else if(day > 28) {
                 return false;
            }
        }
        if(month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
            if(day > 31)
                return false;
        } else if(day > 30)
            return false;

        if(ss.length == 3) //如果沒有時分秒的話
            return true;

        //檢查時分秒毫秒
        if(ss.length > 3 && (ss[3] > 23 || ss[3] < 0))
            return false;
        if(ss.length > 4 && (ss[4] > 59 || ss[4] < 0))
        	return false;
        if(ss.length > 5 && (ss[5] > 59 || ss[5] < 0))
        	return false;
        if(ss.length > 6 && (ss[6] > 999 || ss[6] < 0))
            return false;

        return true;
    }

    /**
     * 將日期轉成如
     * "中華民國xx年xx月xx日xx時xx分xx秒(xxx毫秒)" 民國日期的型式, 數字為國字(依照 runtime 所在的時區轉出年月日時分秒之數值).
     */
    public static String toROCComplexForm(final Date date) {
        try {
            return toROCForm(toArray(date), true, false);
        } catch(Exception e) {}
        return null;
    }

    /**
     * 將西元日期格式為 "YYYY MM DD hh mm ss" 或 "YYYY MM DD" 的字串轉成如
     * "中華民國xx年xx月xx日xx時xx分xx秒(xxx毫秒)" 民國日期的型式, 數字為國字(依照 runtime 所在的時區轉出年月日時分秒數值).
     * @deprecated 不鼓勵直接操作字串, 應改用 {@link #toDate(String)} 及 {@link #toROCComplexForm(Date)}
     */
    @Deprecated
    public static String toROCComplexForm(final String s) {
        try {
            return toROCForm(toArray(s), true, false);
        } catch(Exception e) {}
        return null;
    }

    /**
     * 將西元日期字串陣列 [ 年, 月, 日, 時, 分, 秒 ] 或 [ 年, 月, 日 ] 轉成如
     * "中華民國xx年xx月xx日xx時xx分xx秒(xxx毫秒)" 的型式, 數字為國字.<br>
     * @deprecated 不鼓勵直接操作字串, 應改用 {@link #setDate(Date, Integer...)} 或 {@link #asDate(Integer, Integer, Integer, Integer...)}, 及 {@link #toROCComplexForm(Date)}
     */
    @Deprecated
    public static String toROCComplexForm(final String ... ss) {
        try {
            return toROCForm(ss, true, false);
        } catch(Exception e) {}
        return null;
    }

    /**
     * 將西元日期字串陣列 [ 年, 月, 日, 時, 分, 秒 ] 或 [ 年, 月, 日 ] 轉成如
     * "xx年xx月xx日xx時xx分xx秒(xxx毫秒)" 的型式, 數字為國字.<br>
     * @param prfixROC 輸出的日期字串前是否加上 "中華民國" 的字樣. (如果年份為負, 則一定加上 "民國前" 字樣, 0 年記為 "民國前一年" 或 "民國前壹年").
     * @param bigForm 數字是否使用大寫國字型式.
     */
    public static String toROCForm(final String[] ss, final boolean prfixROC, final boolean bigForm) {
        try {
            if(ss == null)
                return null;
            final StringBuilder buffer = new StringBuilder();
            int cYear = Integer.parseInt(ss[0]) - 1911;
            if(cYear == 1)
                buffer.append(prfixROC ? "中華民國元" : "元");
            else {
                if(cYear < 1) {
                    cYear *= -1;
                    cYear++;
                    buffer.append("民國前");
                } else if(prfixROC) {
                    buffer.append("中華民國");
                }
                buffer.append(bigForm ? StrUtil.num2BigCNum((double)cYear) : StrUtil.num2CNum((double)cYear));
            }
            buffer.append("年");
            final double month = Double.parseDouble(ss[1]);
            buffer.append(bigForm? StrUtil.num2BigCNum(month) : StrUtil.num2CNum(month)).append("月");
            final double day = Double.parseDouble(ss[2]);
            buffer.append(bigForm ? StrUtil.num2BigCNum(day) : StrUtil.num2CNum(day)).append("日");
            if(ss.length == 3)
                return buffer.toString();

            final double hour = Double.parseDouble(ss[3]);
            buffer.append(bigForm ? StrUtil.num2BigCNum(hour) : StrUtil.num2CNum(hour)).append("時");
            final double minute = Double.parseDouble(ss[4]);
            buffer.append(bigForm ? StrUtil.num2BigCNum(minute) : StrUtil.num2CNum(minute)).append("分");
            final double second = Double.parseDouble(ss[5]);
            buffer.append(bigForm ? StrUtil.num2BigCNum(second) : StrUtil.num2CNum(second)).append("秒");
            if(!"0".equals(ss[6])) {
            	double ms = Double.parseDouble(ss[6]);
                buffer.append(bigForm ? StrUtil.num2BigCNum(ms) : StrUtil.num2CNum(ms)).append("毫秒");
            }
            return buffer.toString();
        } catch(Exception e) {}
        return null;
    }

    /**
     * 將民國年日期(含或不含時分秒, 格式見 class 說明)轉成 java.util.Calendar 物件(使用 runtime 所在的時區).
     * @see DateUtil
     */
    public static Calendar twDateToCalendar(final String s) {
        try {
        	final String[] ss = toArray(s);
            if(ss == null)
            	return null;

            final int[] ss2 = new int[ss.length];
            for(int i = 0; i < ss.length; i++)
            	ss2[i] = Integer.parseInt(ss[i]);

            //化為西元年
            if(ss2[0] < 0)
            	ss2[0]++;
            ss2[0] += 1911;
            return toCalendarObj(ss2);
        } catch(Exception e) {}
        return null;
    }

    /**
     * 將民國年日期(含或不含時分秒)按指定的 format 格式, 轉成 java.util.Calendar 物件(使用 runtime 所在的時區).<br>
     * 詳細說明參見 {@link DateUtil#twDateToDate(String, String)}
     * @see #twDateToDate(String, String)
     */
    public static Calendar twDateToCalendar(final String s, final String format) {
    	try {
    		if(isEmpty(s))
	    		return null;
	    	if(isEmpty(format))
	    		return twDateToCalendar(s);
	    	if(s.length() != format.length())
	    		return null;

	    	final String[][] cols = {
	    			{ "yyy", "yy", "y" }, //民國年
	    			{ "MM", "M" },
	    			{ "dd", "d" },
	    			{ "HH", "H" },
	    			{ "mm", "m" },
	    			{ "ss", "s" },
	    			{ "SSS", "SS", "S" }
	    	};
	    	final int[] values = { 1, 1, 1, 0, 0, 0, 0 }; //year, month, day, hour, minute, second, ms, 長度同 cols[] (民國元年為 1, 0 稱為民國前一年, 與西元年表示習慣不同)
	    	int n;

	    	for(int i = 0; i < cols.length; i++) {
	    		for(int j = 0; j < cols[i].length; j++) {
		    		if((n = format.indexOf(cols[i][j])) != -1) {
		    			values[i] = Integer.parseInt(s.substring(n, n + cols[i][j].length()));
		    			break; //第一個對應到的日期欄位值才算數
		    		}
		    	}
	    	}
	    	
	    	//把年欄位值化為西元年
	    	if(values[0] < 0)
	    		values[0]++;
	    	values[0] += 1911;

	    	return toCalendarObj(values);
    	} catch(Exception e) {}
        return null;
    }

    /**
     * 將民國年日期(含或不含時分秒, 格式見 class 說明)轉成 java.util.Date 物件(依照 runtime 所在的時區解析).
     * @see DateUtil
     */
    public static Date twDateToDate(final String s) {
        try {
            if(s == null)
                return null;
            final Calendar cal = twDateToCalendar(s);
            return (cal == null) ? null : cal.getTime();
        } catch(Exception e) {}
        return null;
    }

    /**
     * 將民國年日期(含或不含時分秒)按指定的 format 格式, 轉成 java.util.Date 物件(依照 runtime 所在的時區解析).
     * @param s
     * @param format 與輸入值 s 對應的日期格式. 注意事項:
     *   <ul>
     *     <li>format 格式與輸入值不一樣者(長度也得一樣), 傳回 null
     *     <li>format 中缺年, 月, 日欄位者, 視為 1 (民國元年為 1, 0 稱為民國前一年, 與西元年表示習慣不同)
     *     <li>format 中缺時, 分, 秒, 毫秒欄位者, 視為 0
     *     <li>若 format 中的日期欄位有重複者, 只有第一個欄位有作用
     *     <li>若 format 為 null 或空字串者, 則視為本 DateUtil 可接受輸入的預設日期格式(見本 class 之說明)
     *   </ul>
     *   &nbsp;&nbsp; 其日期欄位格式字串如下:
     *   <ul>
     *     <li>年: yyy 或 yyy 或 yy 或 y(民國年)
     *     <li>月: MM 或 M
     *     <li>日: dd 或 d
     *     <li>時: HH 或 H(24 小時制)
     *     <li>分: mm 或 m
     *     <li>秒: ss 或 s
     *     <li>毫秒: SSS 或 SS 或 S
     *   </ul>
     */
    public static Date twDateToDate(final String s, final String format) {
    	try {
    		final Calendar cal = twDateToCalendar(s, format);
    		return (cal == null) ? null : cal.getTime();
    	} catch(Exception e) {}
        return null;
    }

    /**
     * 將民國年日期(含或不含時分秒, 格式見 class 說明)轉成 java.sql.Timestamp 物件(依照 runtime 所在的時區解析).
     * @see DateUtil
     */
    public static Timestamp twDateToTimestamp(final String s) {
        try {
            if(s == null)
                return null;
            final Calendar cal = twDateToCalendar(s);
            return (cal == null) ? null : new java.sql.Timestamp(cal.getTimeInMillis());
        } catch(Exception e) {}
        return null;
    }

    /**
     * 將民國年日期(含或不含時分秒)按指定的 format 格式, 轉成 java.sql.Timestamp 物件(依照 runtime 所在的時區解析).<br>
     * 詳細說明參見 {@link DateUtil#twDateToDate(String, String)}
     * @see #twDateToDate(String, String)
     */
    public static Timestamp twDateToTimestamp(final String s, final String format) {
    	try {
    		final Calendar cal = twDateToCalendar(s, format);
            return (cal == null) ? null : new java.sql.Timestamp(cal.getTimeInMillis());
        } catch(Exception e) {}
        return null;
    }

    /**
     * 將西元日期字串轉成民國日期字串陣列.
     * @param s 西元日期字串(格式見 class 說明)
     * @return 民國日期字串陣列(年月日或年月日時分秒)
     */
    public static String[] adDateToTwDateArray(final String s) {
        try {
        	final String[] ss = toArray(s);
            int y = Integer.parseInt(ss[0]) - 1911;
            if(y < 1)
                y--;
            ss[0] = String.valueOf(y);
            return ss;
        } catch(Exception e) {}
        return null;
    }

    /**
     * 將民國日期字串轉成西元日期字串陣列.
     * @param s 民國日期(格式見 class 說明)
     * @return 西元日期字串陣列(年月日或年月日時分秒)
     */
    public static String[] twDateToADDateArray(final String s) {
        try {
        	final String[] ss = toArray(s);
            if(ss == null)
            	return null;

            int y = Integer.parseInt(ss[0]);
            if(y < 0)
                y++;
            ss[0] = String.valueOf(y + 1911);
            return ss;
        } catch(Exception e) {}
        return null;
    }

    /**
     * 求取兩個西元日期的時間差之毫秒數(s2 - s1).
     * @param s1 被減的時間數. null 值者, 取 Long.MIN_VALUE
     * @param s2 null 值者. 取 Long.MIN_VALUE
     * @return 時間差(單位: 毫秒), 內部發生問題者, 傳回 Long.MIN_VALUE.
     */
    public static long diffDate(final String s1, final String s2) {
        try {
        	final Date d1 = toDate(s1);
        	if(d1 == null)
        		return Long.MIN_VALUE;
        	final Date d2 = toDate(s2);
        	if(d2 == null)
        		return Long.MIN_VALUE;
            return d2.getTime() - d1.getTime();
        } catch(Exception e) {}
        return Long.MIN_VALUE;
    }
    
    /**
     * 求取日期物件 d 加上 years 年後的日期.<br>
     * (輸入值 d 不受影響)
     * @param years 欲增加的年數(可負值)
     * @return 新的 java.util.Date 物件.
     */
    public static Date addYears(final Date d, final Integer years) {
    	try {
    		if(d == null || years == null || years == 0)
    			return d;
    		
    		final Calendar cal = Calendar.getInstance();
    		cal.setTime(d);
    		cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + years);
    		return cal.getTime();
    	} catch(Throwable t) {}
    	return null;
    }
    
    /**
     * 求取日期物件 d 加上 months 月後的日期.<br>
     * (輸入值 d 不受影響)
     * @param months 欲增加的月數(可負值)
     * @return 新的 java.util.Date 物件.
     */
    public static Date addMonths(final Date d, final Integer months) {
    	try {
    		if(d == null || months == null || months == 0)
    			return d;
    		
    		final Calendar cal = Calendar.getInstance();
    		cal.setTime(d);
    		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + months);
    		return cal.getTime();
    	} catch(Throwable t) {}
    	return null;
    }
    
    /**
     * 求取日期物件 d 加上 days 天後的日期.<br>
     * (輸入值 d 不受影響)
     * @param days 欲增加的天數(可負值)
     * @return 新的 java.util.Date 物件.
     * @see #dateAdd(Date, double) dateAdd(Date, double): 功能相似, 但本函數 addDays() 之天數限整數
     */
    public static Date addDays(final Date d, final Integer days) {
    	try {
    		if(d == null || days == null || days == 0)
    			return d;
    		
    		final Calendar cal = Calendar.getInstance();
    		cal.setTime(d);
    		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + days);
    		return cal.getTime();
    	} catch(Throwable t) {}
    	return null;
    }
    
    /**
     * 求取日期物件 d 加上 hours 個小時後的日期.<br>
     * (輸入值 d 不受影響)
     * @param hours 欲增加的小時數(可負值)
     * @return 新的 java.util.Date 物件.
     */
    public static Date addHours(final Date d, final Integer hours) {
    	try {
    		if(d == null || hours == null || hours == 0)
    			return d;
    		
    		final Calendar cal = Calendar.getInstance();
    		cal.setTime(d);
    		cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) + hours);
    		return cal.getTime();
    	} catch(Throwable t) {}
    	return null;
    }
    
    /**
     * 求取日期物件 d 加上 minutes 分鐘後的日期.<br>
     * (輸入值 d 不受影響)
     * @param minutes 欲增加的分鐘數(可負值)
     * @return 新的 java.util.Date 物件.
     */
    public static Date addMinutes(final Date d, final Integer minutes) {
    	try {
    		if(d == null || minutes == null || minutes == 0)
    			return d;
    		
    		final Calendar cal = Calendar.getInstance();
    		cal.setTime(d);
    		cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + minutes);
    		return cal.getTime();
    	} catch(Throwable t) {}
    	return null;
    }
    
    /**
     * 求取日期物件 d 加上 seconds 秒後的日期.<br>
     * (輸入值 d 不受影響)
     * @param seconds 欲增加的秒數(可負值)
     * @return 新的 java.util.Date 物件.
     */
    public static Date addSeconds(final Date d, final Integer seconds) {
    	try {
    		if(d == null || seconds == null || seconds == 0)
    			return d;
    		
    		final Calendar cal = Calendar.getInstance();
    		cal.setTime(d);
    		cal.set(Calendar.SECOND, cal.get(Calendar.SECOND) + seconds);
    		return cal.getTime();
    	} catch(Throwable t) {}
    	return null;
    }
    
    /**
     * 求取日期物件 d 加上 millis 毫秒後的日期.<br>
     * (輸入值 d 不受影響)
     * @param millis 欲增加的毫數(可負值)
     * @return 新的 java.util.Date 物件.
     */
    public static Date addMillis(final Date d, final Integer millis) {
    	try {
    		if(d == null || millis == null || millis == 0)
    			return d;
    		
    		final Calendar cal = Calendar.getInstance();
    		cal.setTime(d);
    		cal.set(Calendar.MILLISECOND, cal.get(Calendar.MILLISECOND) + millis);
    		return cal.getTime();
    	} catch(Throwable t) {}
    	return null;
    }

    /**
     * 求取日期物件 d 加上 days 天後的日期.<br>
     * (輸入值 d 不受影響)
     * @param days 欲增加的天數(單位: 天, 可為負數, 可能因含時分秒而帶小數)
     * @return 新的 java.util.Date 物件.
     * @see #addDays(Date, Integer) addDays(Date, Integer): 功能類似, 但本函數 dateAdd() 可涵蓋年月日時分秒毫秒等欄位的加減
     */
    public static Date dateAdd(final Date d, final double days) {
        try {
            if(d == null || days == 0D)
                return d;

            return new Date((long)(d.getTime() + days * 24 * 60 * 60 * 1000));
        } catch(Exception e) {}
        return null;
    }

    /**
     * <i>求取<b>西元日期</b>字串 s 加上 days 天後的日期.</i>
     * @return 格式為 "YYYY-MM-DD hh:mm:ss" 或 "YYYYMMDD hh:mm:ss" 或 "YYYY-MM-DD" 或 "YYYYMMDD" 或 "YYYYMMDDhhmmss" 的字串(與輸入的日期格式一致).
     * @see #dateAdd(Date, double)
     * @see #toDate(String)
     * @deprecated 不鼓勵直接操作字串. 應統一把輸入的日期字串轉為 java.util.Date 物件
     * 		(可使用 {@link #toDate(String)}, {@link #dateAdd(Date, double)}, {@link #addYears(Date, Integer)}, {@link #addMonths(Date, Integer)}, {@link #addDays(Date, Integer)}, 
     * 		{@link #addHours(Date, Integer)}, {@link #addMinutes(Date, Integer)}, {@link #addSeconds(Date, Integer)}, {@link #addMillis(Date, Integer)} ... 等), 
     * 		最後輸出為需要的字串格式(使用 {@link #format(Date, String)})
     */
	@Deprecated
    public static String dateAdd(final String s, final double days) {
        try {
        	Date d = toDate(s);
            if(d == null)
                return null;

            d = dateAdd(d, days);
            if((_getDateTimePatternMatcher().reset(s)).matches())
                return format(d, "yyyy-MM-dd HH:mm:ss");
            if((_getAdDateTimeCompactMatcher().reset(s)).matches())
                return format(d, "yyyyMMdd HH:mm:ss");
            if((_getAdDateTimeCompactMatcher2().reset(s)).matches())
                return format(d, "yyyyMMddHHmmss");
            if((_getDatePatternMatcher().reset(s)).matches())
                return format(d, "yyyy-MM-dd");
            else
                return format(d, "yyyyMMdd");
        } catch(Exception e) {}
        return null;
    }

    /**
     * 求取<b>民國日期</b>字串 s 加上 days 天後的日期.
     * @return 格式為 "YYY-MM-DD hh:mm:ss" 或 "YYY-MM-DD" 或 "YYYMMDD hh:mm:ss" 或 "YYYMMDD" 或 "YYYMMDDhhmmss" 的字串(與輸入的日期格式一致).
     * @see #formatTW(Date, String)
     * @see #twDateToDate(String)
     * @deprecated 不鼓勵直接操作字串. 應統一把輸入的日期字串轉為 java.util.Date 物件
     * 		(可使用 {@link #twDateToDate(String)}, {@link #dateAdd(Date, double)}, {@link #addYears(Date, Integer)}, {@link #addMonths(Date, Integer)}, {@link #addDays(Date, Integer)}, 
     * 		{@link #addHours(Date, Integer)}, {@link #addMinutes(Date, Integer)}, {@link #addSeconds(Date, Integer)}, {@link #addMillis(Date, Integer)} ... 等), 
     * 		最後輸出為需要的字串格式(使用 {@link #formatTW(Date, String)})
     */
	@Deprecated
    public static String twDateAdd(final String s, final double days) {
        try {
            if(s == null)
                return null;

            final Date d = twDateToDate(s);
            if(_getDateTimePatternMatcher().reset(s).matches())
            	return DateUtil.formatTW(dateAdd(d, days), "yyy-MM-dd HH:mm:ss");
            if(_getTwDateTimeCompactMatcher().reset(s).matches())
            	return DateUtil.formatTW(dateAdd(d, days), "yyyMMdd HH:mm:ss");
            if(_getTwDateTimeCompactMatcher2().reset(s).matches())
            	return DateUtil.formatTW(dateAdd(d, days), "yyyMMddHHmmss");
            if(_getDatePatternMatcher().reset(s).matches())
            	return DateUtil.formatTW(dateAdd(d, days), "yyy-MM-dd");
            return DateUtil.formatTW(dateAdd(d, days), "yyyMMdd");
        } catch(Exception e) {}
        return null;
    }

    /**
     * 將<b>西元日期</b>字串 s 轉成 format 所指定的格式(用於西元年, 轉換前後時區相同).
     * @param s 西元日期字串(可能含或不含時分秒欄位)
     * @param format 字串的規定(default: yyyy-MM-dd HH:mm:ss).<br>
     * 其具體如 JDK API doc 之 java.text.SimpleDateFormat 所述. 其中較常用的字符:
     * <ul>
     * <li>年: yyyy 或 yyy 或 yy 或 y(西元年)</li>
     * <li>月: MM 或 M</li>
     * <li>日: dd 或 d</li>
     * <li>時: HH 或 H(24 小時制)</li>
     * <li>分: mm 或 m</li>
     * <li>秒: ss 或 s</li>
     * <li>毫秒: SSS 或 SS 或 S</li>
     * </ul>
     * 例:
     * <pre>
     * <code>String s = DateUtil.format("2005-12-29 18:30:00", "yyyy/MM/dd - HH:mm:ss");
     * //得到 "2005/12/29 - 18:30:00";
     * </code></pre>
     * 注意: 欲獲得更大的彈性, 仍需直接使用 java.text.SimpleDateFormat 及 java.text.DateFormat .
     * @deprecated 不鼓勵直接操作字串, 應改用 {@link #toDate(String)} 及 {@link #format(Date, String)}
     */
	@Deprecated
    public static String format(final String s, final String format) {
        try {
            if(s == null || format == null)
                return null;
            final Date d = toDate(s);
            if(d == null)
                return null;
            return format(d, format);
        } catch(Exception e) {}
        return null;
    }

    /**
     * 將日期物件轉成 format 所指定的格式的字串(西元年, 依照 runtime 所使用的時區).<br>
     * @param d java.util.Date 物件.
     * @param format 格式字串(default: yyyy-MM-dd HH:mm:ss).<br>
     * 具體如 JDK API doc 之 java.text.SimpleDateFormat 所述. 其中較常用的字符:
     * <ul>
     * <li>年: yyyy 或 yyy 或 yy 或 y(西元年)</li>
     * <li>月: MM 或 M</li>
     * <li>日: dd 或 d</li>
     * <li>時: HH 或 H (24 小時制)</li>
     * <li>分: mm 或 m</li>
     * <li>秒: ss 或 s</li>
     * <li>毫秒: SSS 或 SS 或 S</li>
     * </ul>
     * 例:
     * <pre>
     * <code>//把當前時刻按指定的格式輸出
     * String s = DateUtil.format(new Date(), "yyyy/MM/dd - HH:mm:ss");
     * //得到類似這樣的格式 "2005/12/29 - 18:30:00";
     * </code></pre>
     */
    public static String format(final Date d, final String format) {
        try {
            if(d == null)
                return null;
            final String format2 = isEmpty(format) ? "yyyy-MM-dd HH:mm:ss" : format;
            return new SimpleDateFormat(format2).format(d);
        } catch(Exception e) {}
        return null;
    }

    /**
     * 將日期物件轉成 format 所指定的格式的字串(西元年, <span style="color:red">依照轉入值 c 所使用的時區</span>).<br>
     * @param c 日期物件
     * @param format 格式字串(default: yyyy-MM-dd HH:mm:ss).<br>
     * 具體如 JDK API doc 之 java.text.SimpleDateFormat 所述. 其中較常用的字符:
     * <ul>
     * <li>年: yyyy 或 yyy 或 yy 或 y(西元年)</li>
     * <li>月: MM 或 M</li>
     * <li>日: dd 或 d</li>
     * <li>時: HH 或 H (24 小時制)</li>
     * <li>分: mm 或 m</li>
     * <li>秒: ss 或 s</li>
     * <li>毫秒: SSS 或 SS 或 S</li>
     * </ul>
     */
    public static String format(final Calendar c, final String format) {
    	try {
	    	if(c == null)
	    		return null;
	    	final String format2 = isEmpty(format) ? "yyyy-MM-dd HH:mm:ss" : format;
	    	final SimpleDateFormat df = new SimpleDateFormat(format2);
	    	df.setTimeZone(c.getTimeZone());
	    	return df.format(c.getTime());
    	} catch(Exception e) {}
        return null;
    }

    /**
     * 將<b>民國日期</b>字串 s 轉成 format 所指定的格式(民國年, 轉換前後時區相同).
     * @param s 民國日期字串
     * @param format 輸出的日期格式(default: yyy-MM-dd HH:mm:ss), 可用的格式字符:
     * <ul>
     * <li>年: yyy 或 yy 或 yy 或 y (民國年)</li>
     * <li>月: MM 或 M</li>
     * <li>日: dd 或 d</li>
     * <li>時: HH 或 H (24 小時制)</li>
     * <li>分: mm 或 m</li>
     * <li>秒: ss 或 s</li>
     * <li>毫秒: SSS 或 SS 或 S</li>
     * </ul>
     * 例:
     * <pre>
     * <code>String s = DateUtil.formatTW("98-12-29 18:30:00", "yyy/MM/dd - HH:mm:ss");
     * //得到 s = "098/12/29 - 18:30:00";
     * </code></pre>
     * @return 民國日期 (負號另加)
     * @deprecated 不鼓勵直接操作字串, 應改用 {@link #twDateToDate(String)} 及 {@link #formatTW(Date, String)}
     */
    @Deprecated
    public static String formatTW(final String s, final String format) {
        try {
            return _dateTimeArrayToString(toArray(s), format);
        } catch(Exception e) {}
        return null;
    }

    /**
     * 將 java.util.Date 物件轉為民國日期時間字串, 並可指定格式(使用 runtime 所使用的時區).
     * @param date 輸入的日期物件
     * @param format 輸出的格式(default: yyyy-MM-dd HH:mm:ss).<br>
     * format 字串中的日期與時間分隔字元, 須為足以和下列的符號區別的字元.<br>
     * 可使用下的符號代表年月日時分秒, 每種符號出現的次數限一次以內; 且實際位數大於指定的位數者, 仍以實際位數為準:
     *     <ul>
     *     <li>年: yyy 或 yyy 或 yy 或 y</li>
     *     <li>月: MM 或 M</li>
     *     <li>日: dd 或 d</li>
     *     <li>時: HH 或 H (24 小時制)</li>
     *     <li>分: mm 或 m</li>
     *     <li>秒: ss 或 s</li>
     *     <li>毫秒: SSS 或 SS 或 S</li>
     *     </ul>
     * 例:
     * <pre>
     * <code>//把當前時刻轉成民國日期格式的字串
     * String s = DateUtil.formatTW(new Date(), "中華民國yyy年MM月dd日HH時mm分");
     * //得到類似如下的字串 "中華民國103年02月日13時30分";
     *
     * s = DateUtil.formatTW(new Date(), "中華民國yy年M月d日H時m分");
     * //得到類似如下的字串 "中華民國103年2月日13時30分";
     * </code></pre>
     * @return 民國日期 (負號另加)
     */
    public static String formatTW(final Date date, final String format) {
    	try {
            if(date == null || format == null)
                return null;
            return _dateTimeArrayToString(toTwDateTimeArray(date), format);
        } catch(Exception e) {}
        return null;
    }

    /**
     * 將時刻字串轉成需要的格式
     * @param time 時分秒
     * @param format 輸出格式字串<br>
     *     format 字串中的時間分隔字元, 可採用足以和下列的符號區別的字元.<br>
     *     format 字串, 可使用下的符號代表時分秒, 每種符號出現的次數限一次以內; 且實際位數大於指定的位數者, 仍以實際位數為準:
     *     <ul>
     *     <li>時: HH 或 H (24 小時制)</li>
     *     <li>分: mm 或 m</li>
     *     <li>秒: ss 或 s</li>
     *     <li>毫秒: SSS 或 SS 或 S</li>
     *     </ul>
     * @return 格式化後的時刻(不含年月日)
     */
    public static String formatTime(final String time, final String format) {
    	try {
    		final String[] tt = toTimeArray(time); //時,分,秒,毫秒
	    	if(tt == null)
	    		return null;
	    	final StringBuilder sb = _timeArrayToString(tt, 0, new StringBuilder().append(format));
	    	if(sb == null)
	    		return null;
	    	return sb.toString();
    	} catch(Exception e) {}
    	return null;
    }

    /**
     * 對輸入的日期 d 指定 [時(24小時制), 分, 秒, 毫秒] 時刻欄位. <b>注意</b>: 原輸入值 d 不受影響.
     * @param d
     * @param values 時刻之各欄位值, 依 [時, 分, 秒, 毫秒] 順序排列. 若其中欄位為 null 值者, 或較後的欄位未指定者, 則不更動對應的欄位值.<br>
     * @return 設值後的新的日期物件
     */
    public static Date setTime(final Date d, final Integer ... values) {
    	if(d == null)
    		return null;
    	final int len = (values == null || values.length == 0) ? 0 : Math.min(values.length, 4);
    	final Integer[] val = new Integer[3 + len];
    	if(len > 0)
    		System.arraycopy(values, 0, val, 3, len);
    	return setDate(d, val);
    }

    /**
     * 對輸入的日期 d 依順序指定 [西元年, 月, 日, 時(24小時制), 分, 秒, 毫秒] 目期欄位. <b>注意</b>: 原輸入值 d 不受影響.
     * @param d
     * @param values 日期各欄位值, 依 [西元年, 月, 日, 時, 分, 秒, 毫秒] 順序排列. 若其中欄位為 null 值者, 或較後的欄位未指定者, 則不更動對應的欄位值.<br>
     * 		(其中月由 一月=1 起算)
     * @return 設值後的新的日期物件
     */
    public static Date setDate(final Date d, final Integer ... values) {
    	try {
    		if(d == null)
                return null;
    		final Calendar c = Calendar.getInstance();
            c.setTime(d);
            
            if(values != null && values.length > 0) {
            	for(int i = 0, ii = Math.min(values.length, 7); i < ii; i++) {
            		if(values[i] == null)
            			continue;
            		
            		final int calColumn;
            		switch(i) {
            			case 0: calColumn = Calendar.YEAR; break;
            			case 1: calColumn = Calendar.MONTH; break;
            			case 2: calColumn = Calendar.DATE; break;
            			case 3: calColumn = Calendar.HOUR_OF_DAY; break;
            			case 4: calColumn = Calendar.MINUTE; break;
            			case 5: calColumn = Calendar.SECOND; break;
            			default: calColumn = Calendar.MILLISECOND;
            		}
            		
            		if(calColumn == Calendar.MONTH)
            			c.set(Calendar.MONTH, values[1] - 1);
            		else
            			c.set(calColumn, values[i]);
            	}
            }
            
            return c.getTime();
    	} catch(Exception e) {}
        return null;
    }

    /**
     * @deprecated (同 {@link #setDate(Date, Integer...)})
     */
    @Deprecated
    public static Date setDatetime(final Date d, final Integer ... values) {
    	return setDate(d, values);
    }
    
    /**
     * 依輸入的西元年月日(時分秒毫秒)產生日期物件. 年月日欄位中有 null 值者, 傳回 null.
     * @param year 西元年 (not null)
     * @param month 月(自 一月=1 起算) (not null)
     * @param dayOfMonth 日 (not null)
     * @param time 時(24小時制)分秒毫秒欄位值. 若其中有 null 值, 或較後的欄位未指定值, 對應的欄位均設值為 0
     * @return 設值後的新的日期物件
     */
    public static Date asDate(final Integer year, final Integer month, final Integer dayOfMonth, final Integer ... time) {
    	if(year == null || month == null || dayOfMonth == null) //年月日欄位缺一則不成日期
    		return null;
    	
    	final Calendar cal = Calendar.getInstance(); //年月日時分秒毫欄位一定設值
    	cal.set(Calendar.YEAR, year);
    	cal.set(Calendar.MONTH, month - 1);
    	cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		cal.set(Calendar.HOUR_OF_DAY, (time == null || time.length < 1 || time[0] == null) ? 0 : time[0]);
		cal.set(Calendar.MINUTE, (time == null || time.length < 2 || time[1] == null) ? 0 : time[1]);
		cal.set(Calendar.SECOND, (time == null || time.length < 3 || time[2] == null) ? 0 : time[2]);
		cal.set(Calendar.MILLISECOND, (time == null || time.length < 4 || time[3] == null) ? 0 : time[3]);
    	return cal.getTime();
    }

    /**
     * 完全捨去所指定的時間欄位(不含) 之後的欄位值(設為 0). 例:<div style="border:1px solid #666"><pre>
     * Date d = ...;
     * System.out.println(DateUtil.truncate(d, DateUtil.DATE)); //輸出的 Date 物件之時,分,秒,毫秒皆化為 0
     * System.out.println(DateUtil.truncate(d, DateUtil.MINUTE)); //輸出的 Date 物件之秒及毫秒皆化為 0
     * </pre></div>
     * @param d
     * @param field 欲保留的時間欄位 (可為 {@link #MONTH DateUtil.MONTH} 或 {@link #DATE DateUtil.DATE} 或 {@link #HOUR DateUtil.HOUR}, 
     * 		或 {@link #MINUTE DateUtil.MINUTE}, {@link #SECOND DateUtil.SECOND} 或 {@link #MILLISECOND DateUtil.MILLISECOND})
     * @return 削去指定欄位之後的欄位值後, 所得到的新 Date 物件(輸入的 Date 物件不受影響)
     */
    public static Date truncate(final Date d, final int field) {
    	try {
    		if(d == null)
    			return null;
    		final Calendar c = Calendar.getInstance();
    		c.setTime(d);
    		if(field < MILLISECOND) {
    			c.set(Calendar.MILLISECOND, 0);
	    		if(field < SECOND) {
	    			c.set(Calendar.SECOND, 0);
		    		if(field < MINUTE) {
		    			c.set(Calendar.MINUTE, 0);
			    		if(field < HOUR) {
			    			c.set(Calendar.HOUR_OF_DAY, 0);
				    		if(field < DATE) {
				    			c.set(Calendar.DATE, 0);
					    		if(field < MONTH) {
					    			c.set(Calendar.MONTH, 0);
					    			//if(field < YEAR) {
					    			//	c.set(Calendar.YEAR, 0);
					    			//}
					    		}
				    		}
			    		}
		    		}
	    		}
    		}
    		return c.getTime();
    	} catch(Exception e) {}
        return null;
    }

    /**
     * 取指定日期之年份.
     * @param d
     */
    public static Integer getYear(final Date d) {
        if(d == null)
            return null;
        final Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        return cal.get(Calendar.YEAR);
    }

    /**
     * 取指定日期之月份(1-12).
     * @param d
     */
    public static Integer getMonth(final Date d) {
        if(d == null)
            return null;
        final Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        return cal.get(Calendar.MONTH) + 1;
    }

    /**
     * 取指定日期之日(月中之日).
     * @param d
     */
    public static Integer getDay(final Date d) {
        if(d == null)
            return null;
        final Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        return cal.get(Calendar.DATE);
    }

    /**
     * 取指定日期之星期(1-7)
     * @param d
     * @return 星期數字, 星期日為 7. 若輸入值為 null 者, 則傳回 0;
     */
    public static Integer getWeek(final Date d) {
    	if(d == null)
    		return null;
    	final Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        switch(cal.get(Calendar.DAY_OF_WEEK)) {
        	case Calendar.SUNDAY: return 7;
        	case Calendar.MONDAY: return 1;
        	case Calendar.TUESDAY: return 2;
        	case Calendar.WEDNESDAY: return 3;
        	case Calendar.THURSDAY: return 4;
        	case Calendar.FRIDAY: return 5;
        	case Calendar.SATURDAY: return 6;
        	default: return 0;
        }
    }

    /**
     * 取指定日期之時(24 小時制).
     * @param d
     */
    public static Integer getHour(final Date d) {
        if(d == null)
            return null;
        final Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 取指定日期之分.
     * @param d
     */
    public static Integer getMinute(final Date d) {
        if(d == null)
            return null;
        final Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        return cal.get(Calendar.MINUTE);
    }

    /**
     * 取指定日期之秒數.
     * @param d
     */
    public static Integer getSecond(final Date d) {
        if(d == null)
            return null;
        final Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        return cal.get(Calendar.SECOND);
    }

    /**
     * 求取指定日期所在月份的天數.
     * @param aDate
     * @return 所在月份的天數
     */
    public static Integer getMaxDayOfMonth(final Date aDate) {
        if(aDate == null)
            return null;
        final Calendar cal = Calendar.getInstance();
        cal.setTime(aDate);
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * 將指定時刻化為同年之 1 日 0 時 0 分 0 秒, 用作以「年」為單位的時段之左邊界(include).
     * @param t 時刻
     */
    public static Date getDateLeftBoundForYear(final Date t) {
    	if(t == null)
            return null;
    	final Calendar cal = Calendar.getInstance();
        cal.setTime(t);
        cal.set(Calendar.MONTH, 0); //1 月
        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 將指定時刻化為隔年的 1 月 1 日 0 時 0 分 0 秒, 用作以「年」為單位的時段之右邊界(excluded).
     * @param t 時刻
     */
    public static Date getDateRightBoundForYear(final Date t) {
        if(t == null)
            return null;
        final Calendar cal = Calendar.getInstance();
        cal.setTime(t);
        cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + 1);
        cal.set(Calendar.MONTH, 0); //1 月
        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 將指定時刻化為該月 1 日 0 時 0 分 0 秒, 用作以「月」為單位的時段之左邊界(include).
     * @param t 時刻
     */
    public static Date getDateLeftBoundForMonth(final Date t) {
    	if(t == null)
            return null;
    	final Calendar cal = Calendar.getInstance();
        cal.setTime(t);
        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 將指定時刻化為隔月的 1 日 0 時 0 分 0 秒, 用作以「月」為單位的時段之右邊界(excluded).
     * @param t 時刻
     */
    public static Date getDateRightBoundForMonth(final Date t) {
        if(t == null)
            return null;
        final Calendar cal = Calendar.getInstance();
        cal.setTime(t);
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 1);
        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 將指定時刻化為該日 0 時 0 分 0 秒, 用作以「日」為單位的時段之左邊界(included)
     * @param t 時刻
     * @return
     */
    public static Date getDateLeftBoundForDay(final Date t) {
        if(t == null)
            return null;
        final Calendar cal = Calendar.getInstance();
        cal.setTime(t);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 將指定時刻化為隔天的 0 時 0 分 0 秒, 用作以「日」為單位的時段之右邊界(excluded).
     * @param t 時刻
     */
    public static Date getDateRightBoundForDay(final Date t) {
        if(t == null)
            return null;
        final Calendar cal = Calendar.getInstance();
        cal.setTime(t);
        cal.set(Calendar.DATE, cal.get(Calendar.DATE) + 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 將指定時刻化為該時之 0 分 0 秒, 用作以「時」為單位的時段之左邊界(included)
     * @param t 時刻
     */
    public static Date getDateLeftBoundForHour(final Date t) {
        if(t == null)
            return null;
        final Calendar cal = Calendar.getInstance();
        cal.setTime(t);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 將指定時刻化為下一小時的 0 分 0 秒, 用作以「時」為單位的時段之右邊界(excluded).
     * @param t 時刻
     */
    public static Date getDateRightBoundForHour(final Date t) {
        if(t == null)
            return null;
        final Calendar cal = Calendar.getInstance();
        cal.setTime(t);
        cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) + 1);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 將指定時刻化為該分鐘之 0 秒, 用作以「分」為單位的時段之左邊界(included)
     * @param t 時刻
     */
    public static Date getDateLeftBoundForMinute(final Date t) {
        if(t == null)
            return null;
        final Calendar cal = Calendar.getInstance();
        cal.setTime(t);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 將指定時刻化為下一分的 0 秒, 用作以「分」為單位的時段之右邊界(excluded).
     * @param t 時刻
     */
    public static Date getDateRightBoundForMinute(final Date t) {
        if(t == null)
            return null;
        final Calendar cal = Calendar.getInstance();
        cal.setTime(t);
        cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + 1);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
	 * 求得當前 Java runtime 可用的所有時區 ID.
	 */
	public static String[] availableTimeZoneIDs() {
		return TimeZone.getAvailableIDs();
	}

	/**
	 * 取程式執行時期所在環境下的時區全名.
	 * 如: "台灣標準時間", "Coordinated Universal Time", ...
	 */
	public static String localTimeZoneName() {
		return _localTimeZone().getDisplayName();
	}

	/**
	 * 取程式執行時期所在環境下的時區全名, 並轉譯為指定的 locale 文字(依 Java runtime 內建的 l10n 設定).
	 * 如(假設於台灣時區執行本程式):<ul>
	 * <li>TimeZoneUtil.localTimeZoneName(Locale.ENGLISH) =&gt; "China Standard Time"
	 * <li>TimeZoneUtil.localTimeZoneName(Locale.TRADITIONAL_CHINESE) =&gt; "台灣標準時間"
	 * </ul>
	 *
	 */
	public static String localTimeZoneName(final Locale locale) {
		final Locale locale2 = (locale == null) ? Locale.ENGLISH : locale;
		return _localTimeZone().getDisplayName(locale2);
	}

	/**
	 * 取程式執行時期所在環境下的時區簡寫名稱.
	 * 如: "TST"(台灣標準時間), "UTC", ...
	 */
	public static String localTimeZoneShortName() {
		return _localTimeZone().getDisplayName(false, TimeZone.SHORT);
	}

	/**
	 * 取程式執行時期所在環境下的時區 ID.
	 * 如: "Asia/Taipei", "UTC", ...
	 */
	public static String localTimeZoneID() {
		return _localTimeZone().getID();
	}

	/**
	 * 將指定的時刻轉為另一時區的表示方式.
	 * @param date 時刻
	 * @param timeZoneID 時區 (如: "Asia/Taipei", "UTC", ..., 限於 java.util.TimeZone.getAvailableIDs() 所列的, default "UTC" if null specified)
	 * @return 同一指定時刻在指定時區的表示
	 */
	public static Calendar dateWithTimeZone(final Date date, final String timeZoneID) {
		if(date == null)
			return null;
		final String timeZoneID2 = isEmpty(timeZoneID) ? "UTC" : timeZoneID;
		return _dateWithTimeZone(date.getTime(), timeZoneID2);
	}

	/**
	 * 將指定的時刻轉為另一時區的表示方式(原輸入的 date 物件不受影響).
	 * @param date 時刻
	 * @param timeZoneID 時區 (如: "Asia/Taipei", "UTC", ..., 限於 java.util.TimeZone.getAvailableIDs() 所列的, default "UTC" if null specified)
	 * @return 同一指定時刻在指定時區的表示(與輸入的 date 引數為不同物件)
	 */
	public static Calendar dateWithTimeZone(final Calendar date, final String timeZoneID) {
		if(date == null)
			return null;
		final String timeZoneID2 = isEmpty(timeZoneID) ? "UTC" : timeZoneID;
		return _dateWithTimeZone(date.getTimeInMillis(), timeZoneID2);
	}

	/**
	 * 將指定的時刻轉為另一時區的表示方式.
	 * @param date 時刻 (UTC milliseconds from the epoch)
	 * @param timeZoneID 時區 (如: "Asia/Taipei", "UTC", ... , 限於 java.util.TimeZone.getAvailableIDs() 所列的, default "UTC" if null specified)
	 * @return 同一指定時刻在指定時區的表示
	 * @see #availableTimeZoneIDs()
	 */
	public static Calendar dateWithTimeZone(final long date, final String timeZoneID) {
		final String timeZoneID2 = isEmpty(timeZoneID) ? "UTC" : timeZoneID;
		return _dateWithTimeZone(date, timeZoneID2);
	}

	/**
	 * 測指定的二日期之年是否相同
	 * @param date1
	 * @param date2
	 * @return 若二參數為 null 時, 仍傳回 false
	 */
	public static boolean isEqualToYear(final Date date1, final Date date2) {
		return equal(date1, date2, COMPARE_TO_YEAR);
	}
	
	/**
	 * 測指定的二日期之年月是否相同
	 * @param date1
	 * @param date2
	 * @return 若二參數為 null 時, 仍傳回 false
	 */
	public static boolean isEqualToMonth(final Date date1, final Date date2) {
		return equal(date1, date2, COMPARE_TO_MONTH);
	}
	
	/**
	 * 測指定的二日期之年月日是否相同
	 * @param date1
	 * @param date2
	 * @return 若二參數為 null 時, 仍傳回 false
	 */
	public static boolean isEqualToDate(final Date date1, final Date date2) {
		return equal(date1, date2, COMPARE_TO_DAY_OF_MONTH);
	}
	
	/**
	 * 測指定的二日期之年月日時是否相同
	 * @param date1
	 * @param date2
	 * @return 若二參數為 null 時, 仍傳回 false
	 */
	public static boolean isEqualToHour(final Date date1, final Date date2) {
		return equal(date1, date2, COMPARE_TO_HOUR);
	}
	
	/**
	 * 測指定的二日期之年月日時分是否相同
	 * @param date1
	 * @param date2
	 * @return 若二參數為 null 時, 仍傳回 false
	 */
	public static boolean isEqualToMinute(final Date date1, final Date date2) {
		return equal(date1, date2, COMPARE_TO_MINUTE);
	}
	
	/**
	 * 測指定的二日期之年月日時分秒是否相同
	 * @param date1
	 * @param date2
	 * @return 若二參數為 null 時, 仍傳回 false
	 */
	public static boolean isEqualToSecond(final Date date1, final Date date2) {
		return equal(date1, date2, COMPARE_TO_SECOND);
	}
	
	//測二日期是否相同, 比較直至 toCol 指定的欄位
	//toCol=0: 比較至年
	//toCol=1: 比較至月
	//toCol=2: 比較至日
	//toCol=3: 比較至時
	//toCol=4: 比較至分
	//toCol=5: 比較至秒
	//toCol=6: 比較至毫秒
	private static final int COMPARE_TO_YEAR = 0;
	private static final int COMPARE_TO_MONTH = 1;
	private static final int COMPARE_TO_DAY_OF_MONTH = 2;
	private static final int COMPARE_TO_HOUR = 3;
	private static final int COMPARE_TO_MINUTE = 4;
	private static final int COMPARE_TO_SECOND = 5;
	private static final int COMPARE_TO_MILLISECOND = 6;
	private static boolean equal(final Date date1, final Date date2, final int toCol) {
		if(date1 == null || date2 == null)
			return false;
		
		//比較至毫秒
		if(date1.getTime() == date2.getTime()) //java.util.Date 物件就精確至毫秒了
			return true;
		if(toCol == COMPARE_TO_MILLISECOND)
			return false;
		
		final Calendar d1 = Calendar.getInstance();
		d1.setTime(date1);
		final Calendar d2 = Calendar.getInstance();
		d2.setTime(date2);
		
		if(d1.get(Calendar.YEAR) != d2.get(Calendar.YEAR))
			return false;
		if(toCol == COMPARE_TO_YEAR) //比較至年
			return true;
		
		if(d1.get(Calendar.MONTH) != d2.get(Calendar.MONTH))
			return false;
		if(toCol == COMPARE_TO_MONTH) //比較至月
			return true;
		
		if(d1.get(Calendar.DAY_OF_MONTH) != d2.get(Calendar.DAY_OF_MONTH))
			return false;
		if(toCol == COMPARE_TO_DAY_OF_MONTH) //比較至日
			return true;
		
		if(d1.get(Calendar.HOUR_OF_DAY) != d2.get(Calendar.HOUR_OF_DAY))
			return false;
		if(toCol == COMPARE_TO_HOUR) //比較至時
			return true;
		
		if(d1.get(Calendar.MINUTE) != d2.get(Calendar.MINUTE))
			return false;
		if(toCol == COMPARE_TO_MINUTE) //比較至分
			return true;
		
		if(d1.get(Calendar.SECOND) != d2.get(Calendar.SECOND))
			return false;
		return true; //比較至秒 (比較至毫秒, 最一開始已經執行過了)
	}
	
	//@param date UTC milliseconds from the epoch
	static Calendar _dateWithTimeZone(final long date, final String timeZoneID) {
		final Calendar cal = new GregorianCalendar(TimeZone.getTimeZone(timeZoneID));
		cal.setTimeInMillis(date);
		return cal;
	}

	/** 取 runtime 的時區物件. */
    static TimeZone _localTimeZone() {
		return Calendar.getInstance().getTimeZone();
	}

    /**
     * 將民國或西元日期陣列([ 年, 月, 日, 時, 分, 秒, 毫秒 ] 或 [ 年, 月, 日 ])轉成 format 所指定的格式.<br>
     * 如果輸入的日期字串中不含時間, 則指定格式字串 format 也不應含時間格式的敘述.<p>
     * format 字串中的日期與時間分隔字元, 須採用足以和下列的符號區別的字元.<br>
     * format 字串, 可使用以下的符號代表年月日時分秒, 每種符號出現的次數限一次以內; 且實際位數大於指定的位數者, 仍以實際位數為準:
     * <ul>
     * <li>年: yyyy 或 yyy 或 yy 或 y (負號不包含在位數裡)
     * <li>月: MM 或 M
     * <li>日: dd 或 d
     * <li>時: HH 或 H (24 小時制)
     * <li>分: mm 或 m
     * <li>秒: ss 或 s
     * <li>毫秒: SSS 或 SS 或 S
     * </ul>
     */
    static String _dateTimeArrayToString(final String[] ss, final String format) {
        if(ss == null)
            return null;
        final String format2 = (format == null) ? "yyy-MM-dd HH:mm:ss" : format;
        final StringBuilder date = new StringBuilder().append(format2);
        int i, len; //begin index, 占位符號長度

        //年
        len = ((i = date.indexOf("yyyy")) != -1) ? 4 :
        	((i = date.indexOf("yyy")) != -1) ? 3 :
    		((i = date.indexOf("yy")) != -1) ? 2 :
			((i = date.indexOf("y")) != -1) ? 1 : 0;
        if(i != -1) {
            if(ss[0].startsWith("-")) { //帶負號的年份
            	String y = ss[0].substring(1);
            	date.replace(i,  i + len, "-" + ((len > y.length()) ? StrUtil.alignRight(y, len, '0') : y));
            } else {
            	date.replace(i, i + len, (len > ss[0].length()) ? StrUtil.alignRight(ss[0], len, '0') : ss[0]);
            }
        }

        //月
        len = ((i = date.indexOf("MM")) != -1) ? 2 :
        	((i = date.indexOf("M")) != -1) ? 1 : 0;
        if(i != -1)
            date.replace(i, i + len, (len > ss[1].length()) ? StrUtil.alignRight(ss[1], len, '0') : ss[1]);

        //日
        len = ((i = date.indexOf("dd")) != -1) ? 2 :
        	((i = date.indexOf("d")) != -1) ? 1 : 0;
        if(i != -1)
            date.replace(i, i + len, (len > ss[2].length()) ? StrUtil.alignRight(ss[2], len, '0') : ss[2]);

        if(ss.length == 3)
            return date.toString();

        _timeArrayToString(ss, 3, date); //從 ss[3] 開始, 為時分秒毫秒的部分
        return date.toString();
    }

    //只處理格式字串中的 HH mm ss SSS 的部分
    //@param ss 須保證含時、分、秒、毫秒四部
    //@param indexStart ss 中 HH 部分開始的 index
    private static StringBuilder _timeArrayToString(final String[] ss, final int indexStart, final StringBuilder format) {
    	if(ss == null || format == null)
            return null;
    	final StringBuilder time = format;
        int i, len; //begin index, 占位符號長度

        //時
        int idx = indexStart;
        len = ((i = time.indexOf("HH")) != -1) ? 2 :
        	((i = time.indexOf("H")) != -1) ? 1 : 0;
        if(i != -1)
        	time.replace(i, i + len, (len > ss[idx].length()) ? StrUtil.alignRight(ss[idx], len, '0') : ss[idx]);

        //分
        idx++;
        len = ((i = time.indexOf("mm")) != -1) ? 2 :
        	((i = time.indexOf("m")) != -1) ? 1 : 0;
        if(i != -1)
        	time.replace(i, i + len, (len > ss[idx].length()) ? StrUtil.alignRight(ss[idx], len, '0') : ss[idx]);

        //秒
        idx++;
        len = ((i = time.indexOf("ss")) != -1) ? 2 :
        	((i = time.indexOf("s")) != -1) ? 1 : 0;
        if(i != -1)
        	time.replace(i, i + len, (len > ss[idx].length()) ? StrUtil.alignRight(ss[idx], len, '0') : ss[idx]);

        //毫秒
        idx++;
        len = ((i = time.indexOf("SSS")) != -1) ? 3 :
        	((i = time.indexOf("SS")) != -1) ? 2 :
    		((i = time.indexOf("S")) != -1) ? 1 : 0;
        if(i != -1)
        	time.replace(i, i + len, (len > ss[idx].length()) ? StrUtil.alignRight(ss[idx], len, '0') : ss[idx]);

        return time;
    }

    //砍數字字串左方的 0, 至少傳回 "0"
    private static String _trimLeftZero(final String s) {
    	if(s == null)
    		return "0";
    	int len = s.length();
    	if(len == 0)
    		return "0";

    	String s2 = s;
    	if(s.charAt(0) == '-') { //負數
    		if(len == 1)
    			return "0";

    		s2 = s.substring(1); //不含負號
    		len = s2.length();

    		if(len == 0)
    			return "0";
    		if(len == 1)
    			return s;
    		s2 = StrUtil.trimLeft(s2, '0');
    		return (s2.length() == 0) ? "0" : "-" + s2;
    	}

    	if(len == 1)
    		return s;

    	s2 = StrUtil.trimLeft(s, '0');
    	return (s2.length() == 0) ? "0" : s2;
    }

    //判斷是否為 null 或空字串
    private static boolean isEmpty(final String s) {
    	return (s == null || s.length() == 0);
    }
}
