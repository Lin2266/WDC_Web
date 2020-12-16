package com.test.commons.util;

import java.util.*;
import java.text.*;

/**
 * 字串處理工具.<br>
 * <b>注意</b>: 在 Java 環境中, 一個中文字仍視為一個字元(在任何 locale 環境下).
 */
public class StrUtil {
    private StrUtil() {}
    
    /**
     * 將字串向左靠.
     * 範例:<ul>
     * <li>StrUtil.alignLeft("ab", 5, '0') 會得到 "ab000"。
     * <li>StrUtil.alignLeft("我", 5, '你') 會得到 "我你你你你"。
     * </ul>
     * 但若指定的 len 比輸入字串短, 則輸入字串右側將被裁切, 如:<ul>
     * <li>StrUtil.alignLeft("abc", 2, '0') 得到 "ab".
     * <li>StrUtil.alignLeft("abc", 0, '0') 得到 "".
     * </ul>
     * @param s 輸入的字串。
     * @param len 新的字串長度.
     * @param ch 不滿字串長度的部分要填的字元.
     */
    public static String alignLeft(String s, int len, char ch) {
    	return alignLeft(s, len, ch, true);
    }
    
    /** 將輸入數字化為字串, 在 len 長度範圍內向左靠, 右側填補 ch 字元. 但若字串長度小於 len, 則字串右側將被裁切. */
    public static String alignLeft(Number input , int len, char ch) {
    	if(input == null)
    		return null;
    	return alignLeft(String.valueOf(input), len, ch, true);
    }
    
    /** 將字串 s 在 len 長度範圍內向左靠, 右側填以 ch 字串; 若 s 長度大於 len, 直接輸出原字串 s 而不裁切. */
    public static String alignLeftNoCut(String s, int len, char ch) {
    	return alignLeft(s, len, ch, false);
    }
    
    /** 將輸入數字化為字串, 並在 len 長度範圍內向左靠, 右側填以 ch 字串; 但若字串長度大於 len, 直接輸出完整字串而不裁切. */
    public static String alignLeftNoCut(Number input, int len, char ch) {
    	if(input == null)
    		return null;
    	return alignLeft(String.valueOf(input), len, ch, false);
    }
    
    static String alignLeft(String s, int len, char ch, boolean cut) {
    	if(s == null)
    		return null;
    	if(len <= 0)
    		return "";
        int l = s.length();
        if(l > len) {
        	if(cut)
        		return s.substring(0, len);
        	return s;
        } else if(l == len) {
            return s;
        } else {
            return new StringBuilder().append(s).append(repeat(ch, len - l)).toString();
        }
    }

    /**
     * 將字串往右靠, 以指定的字元往左補滿指定的長度.
     * 範例:<ul>
     * <li>StrUtil.alignRight("ab", 5, '0') 得到 "000ab".
     * <li>StrUtil.alignRight("我", 5, '0') 得到 "0000我".
     * </ul>
     * 但若指定的 len 比輸入字串短, 則輸入字串左側將被裁切, 如:<ul>
     * <li>StrUtil.alignRight("abc", 2, '0') 得到 "bc".
     * <li>StrUtil.alignRight("abc", 0, '0') 得到 "".
     * </ul>
     * @param s 輸入的字串.
     * @param len 新的字串長度.
     * @param ch 不滿字串長度的部分要填的字元.
     */
    public static String alignRight(String s, int len, char ch) {
    	return alignRight(s, len, ch, true);
    }
    
    /** 將輸入數字化為字串, 在 len 長度內向右靠, 右側填補 ch 字元. 但若字串長度大於 len 者, 字串左側將被裁切. */
    public static String alignRight(Number input, int len, char ch) {
    	if(input == null)
    		return null;
    	return alignRight(String.valueOf(input), len, ch, true);
    }
    
    /** 將字串 s 在 len 長度範圍內向右靠, 左側填以 ch 字串; 若 s 長度大於 len, 直接輸出原字串 s 而不裁切. */
    public static String alignRightNoCut(String s, int len, char ch) {
    	return alignRight(s, len, ch, false);
    }
    
    /**
     * 將輸入數字 input 轉成字並在 len 長度範圍內向右靠, 左側填以 ch 字串; 若其字串長度大於 len, 直接輸出原字串而不裁切.
     * 如:<pre><code>StrUtil.alignRightNoCut(123.5, 7, '0');
     * =&gt; "00123.5"
     * </code></pre>
     */
    public static String alignRightNoCut(Number input, int len, char ch) {
    	if(input == null)
    		return null;
    	return alignRight(String.valueOf(input), len, ch, false);
    }
    
    static String alignRight(String s, int len, char ch, boolean cut) {
    	if(s == null)
    		return null;
    	if(len <= 0)
    		return "";
        int l = s.length();
        if(l > len) {
        	if(cut)
        		return s.substring(l - len);
        	return s;
        }
        if(l == len)
            return s;
        return new StringBuilder().append(repeat(ch, len - l)).append(s).toString();
    }

    ///**
    // * 修正要傳遞給 JavaScript 的字串（供 JSP 使用）.
    // * @param s
    // * @return 修正後的字串.
    // */
    //@Deprecated
    //public static String fixForJavaScript(String s) {
    //    s = StrUtil.replaceAll(StrUtil.trim(s), "\n", " ");
    //    s = StrUtil.replaceAll(s, "\r", " ");
    //    s = StrUtil.replaceAll(s, "\\", "\\\\");
    //    s = StrUtil.replaceAll(s, "\"", "\\\"");
    //    return s;
    //}

    /**
     * 將數字之小數點前部位, 每三位間加一個逗號 ','.<br>
     * 範例：<code>
     *   formatNumber("100000.0") ==&gt; 100,000.0
     * </code>
     * @param number
     * @return 貨幣格式的字串, 若輸入值為 null 則傳回空字串.
     */
    public static String formatNumber(String number) {
    	try {
    		if(number == null || number.length() == 0)
        		return "";
    		return formatNumber(Double.parseDouble(number));
    	} catch(Throwable t) {
    		return "";
    	}
    }
    
    /** @see #formatNumber(double) */
    public static String formatNumber(Number number) {
    	if(number == null)
    		return "";
    	return formatNumber(number.doubleValue());
    }
    
    /**
     * 將數字之小數點前部位, 每三位間加一個逗號 ',', 小數部分最多留 6 位.<br>
     * 範例：<pre><code> String value = StrUtil.formatNumber(100000.0);
     * ==&gt; "100,000.0"
     * </code></pre>
     * @param number
     * @return 貨幣格式的字串.
     */
    public static String formatNumber(double number) {
        NumberFormat fmt = new DecimalFormat();
        fmt.setGroupingUsed(true);//預設就是true，此行在此可有可無;若此行設為false，則三位一撇取消
        fmt.setMaximumFractionDigits(6);//小數點後最多6位
        return fmt.format(number);
    }
    
    /**
     * 通用的格式化數字工具.<br>
     * 例1: 將輸入數字四捨五入至小數點下 2 位<pre><code>formatNumber(12345.556677, "0.00");<br>=&gt; "12345.56"</code></pre>
     * 例2: 將輸入數字四捨五入至小數點下 3 位, 且整數部位每 3 位插入區隔用逗號<pre><code>formatNumber(12345.556677, "###,##0.000");<br>=&gt; "12,345.557"</code></pre>
     * fmt 中的 0 代表: 若無值的話, 就填 0.
     * <p>
     * 註: 若只進行小數點的四捨五入, 且欲傳回 number 型態, 則使用 {@link NumUtil#round(Number, int)} 效率較好.
     * @param number 數字
     * @param fmt 欲輸出的格式(按 java.text.DecimalFormat 的格式字串, 詳見 {@link java.text.DecimalFormat}).
     * @return 若輸入值 number 為 null 則傳回空字串, 否則按要求的格式 fmt 輸出
     */
    public static String formatNumber(Number number, String fmt) {
    	if(number == null)
    		return "";
        NumberFormat nf = new java.text.DecimalFormat(fmt);
        return nf.format(number);
    }
    
    /***
     * 判斷字串 src 是否為 headString 開頭.
     * @param src
     * @param headString
     * @return
     */
    public static boolean startWith(final String src, final String headString) {
    	if(src == null || headString == null || src.length() < headString.length())
			return false;
    	if(headString.length() == 0)
    		return true;
		return src.regionMatches(false, 0, headString, 0, headString.length());
    }
    
    /***
     * 判斷字串 src 是否為 headString 開頭, 不分 "英文" 字母大小寫.
     * @param src
     * @param headString
     * @return
     */
    public static boolean startWithIgnoreCase(final String src, final String headString) {
    	if(src == null || headString == null || src.length() < headString.length())
			return false;
    	if(headString.length() == 0)
    		return true;
		return src.regionMatches(true, 0, headString, 0, headString.length());
    }
    
    /***
     * 判斷字串 src 是否為 tailString 結尾.
     * @param src
     * @param tailString
     * @return
     */
    public static boolean endWith(final String src, final String tailString) {
    	if(src == null || tailString == null || src.length() < tailString.length())
			return false;
    	if(tailString.length() == 0)
    		return true;
		return src.regionMatches(false, src.length() - tailString.length(), tailString, 0, tailString.length());
    }
    
    /***
     * 判斷字串 src 是否為 tailString 結尾, 不分 "英文" 字母大小寫.
     * @param src
     * @param tailString
     * @return
     */
    public static boolean endWithIgnoreCase(final String src, final String tailString) {
    	if(src == null || tailString == null || src.length() < tailString.length())
			return false;
    	if(tailString.length() == 0)
    		return true;
		return src.regionMatches(true, src.length() - tailString.length(), tailString, 0, tailString.length());
    }
    
    /**
	 * 在 src 字串中搜尋 str 第一次出現的位置.
	 * @param src
	 * @param str
	 * @return s 中第一次出現 str 的位置
	 */
	public static int indexOf(final String src, final String str) {
		return indexOf(src, str, 0);
	}
    
	/**
	 * 在 src 字串中搜尋 str 第一次出現的位置, 自 fromIndex 開始搜尋.
	 * @param src
	 * @param str
	 * @param fromIndex 在 s 中開始搜尋 str 的起始位置
	 * @return s 中自 fromIndex 後第一次出現 str 的位置
	 */
	public static int indexOf(final String src, final String str, final int fromIndex) {
		if(src == null || str == null || src.length() < str.length())
			return -1;
		return src.indexOf(str, fromIndex);
	}
	
	/**
	 * 在 src 字串中搜尋 str 第一次出現的位置(不分 "英文" 大小寫), 自 fromIndex 開始搜尋.
	 * @param src
	 * @param str
	 * @return s 中自 fromIndex 後第一次出現 str 的位置
	 */
	public static int indexOfIgnoreCase(final String src, final String str) {
		return indexOfIgnoreCase(src, str, 0);
	}
	
	/**
	 * 在 src 字串中搜尋 str 第一次出現的位置(不分 "英文" 大小寫), 自 fromIndex 開始搜尋.
	 * @param src
	 * @param str
	 * @param fromIndex 在 s 中開始搜尋 str 的起始位置
	 * @return s 中自 fromIndex 後第一次出現 str 的位置
	 */
	public static int indexOfIgnoreCase(final String src, final String str, final int fromIndex) {
		if(src == null || str == null || src.length() < str.length() || fromIndex >= src.length())
			return -1;
		if(str.length() == 0) //空字串算包含在內了
			return 0;
		
		final int start = (fromIndex < 0) ? 0 : fromIndex;
		final char firstCharL = Character.toLowerCase(str.charAt(0));
		final char firstCharU = Character.toUpperCase(str.charAt(0));
	
		//ref: https://stackoverflow.com/questions/86780/how-to-check-if-a-string-contains-another-string-in-a-case-insensitive-manner-in
		for(int i = start, ii = src.length() - str.length(); i <= ii; i++) {
			//quick check before calling the more expensive regionMatches() method
			final char ch = src.charAt(i);
			
			if(ch == firstCharL || ch == firstCharU) {
				if(src.regionMatches(true, i, str, 0, str.length()))
					return i;
			}
		}
		return -1;
	}

    /**
     * Locates a String in an array of Strings.
     * @return 傳回參數 string 在 strings 陣列中首次出現時的 index 值. strings 中不存在 string 者, 傳回 -1
     */
    public static int indexOfString(String[] strings, String string) {
    	if(strings == null || strings.length == 0)
    		return -1;
        for(int i = 0; i < strings.length; ++i)
            if(string.equals(strings[i]))
                return i;
        return -1;
    }

    /**
     * Locates a String in an array of Strings, ignoring case.
     * @return 傳回參數 string 在 strings 陣列中首次出現時的 index 值(英文大小寫無關). strings 中不存在 string 者, 傳回 -1
     */
    public static int indexOfStringIgnoreCase(String[] strings, String string) {
    	if(strings == null || strings.length == 0)
    		return -1;
        for(int i = 0; i < strings.length; ++i)
            if(string.equalsIgnoreCase(strings[i]))
                return i;
        return -1;
    }
    
    /**
     * 檢查字串是否由半形整數字組成(含正負號).
     * @param s
     * @see #isInteger(String, int, int)
     */
    public static boolean isInteger(String s) {
    	return (s == null) ? false : isInteger(s, 0, s.length());
    }
    
    /**
     * 檢查字串中指定區段中的部分是否為半形整數字(含正負號).
     * @param s
     * @param indexStart 判斷是否為整數字的起始 index
     * @param indexEnd 結束 index (不含)
     */
    public static boolean isInteger(String s, int indexStart, int indexEnd) {
    	if(s == null || s.length() == 0 ||
    			indexStart < 0 ||
    			indexEnd <= indexStart ||
    			indexEnd > s.length())
    		return false;
    	
    	//檢查第 1 個字元
    	char a = s.charAt(indexStart);
    	if((a < '0' || a > '9') && a != '+' && a != '-')
    		return false;
    	
    	//檢查第 2 及以後的字元
    	for(int i = indexStart + 1; i < indexEnd; i++) {
    		a = s.charAt(i);
    		if(a < '0' || a > '9')
    			return false;
    	}
    	return true;
    }
    
    /**
     * 檢查字串是否為合法的 10 進位數字(含正負號及小數點, 科學符號表示法亦可).<br>
     * 合法的數字字串例見 StrUtil.isNumber(String, int, int) 的說明
     * @param s
     * @see #isNumber(String, int, int)
     */
    public static boolean isNumber(String s) {
    	return (s == null) ? false : isNumber(s, 0, s.length());
    }
    
    /**
     * 檢查字串中指定區段中的部分是否為合法的 10 進位數字(含正負號及小數點, 科學符號表示法亦可).<br>
     * 下列可為合法數字字串:
     * <ul>
     * 	<li>"0"
     * 	<li>"123"
     * 	<li>"-123.450"
     * 	<li>"+123.450"
     * 	<li>".123"
     * 	<li>"-.123"
     * 	<li>".123E10" (以下是科學符號表示法)
     * 	<li>".123e10" ('E' 或 'e' 皆可用)
     * 	<li>"1.23E10"
     * 	<li>"12.34E10"
     * 	<li>"1.23E-10"
     * 	<li>"1.23E+10"
     * 	<li>"-1.23E10"
     * </ul>
     * 但下列則非合法數字字串:
     * <ul>
     * 	<li>null
     * 	<li>"" (空字串)
     * 	<li>"-" (只一個正負符號字元)
     * 	<li>"." (只一個小數點字元)
     * 	<li>"123,456" (逗號分隔字元不在支援之列)
     * 	<li>"1E10.1" (指數部位必為整數)
     * 	<li>"12D" (字串中混進非數字, '+', '-', '.' 的字元 -- 雖然本例的字串剛好可被 Java 順利轉成 Double)
     * </ul>
     * @param s
     * @param indexStart 判斷是否為數字的起始 index
     * @param indexEnd 結束 index (不含)
     */
    public static boolean isNumber(String s, int indexStart, int indexEnd) {
    	//檢查數字字串之起始, 結束的位置
    	if(s == null || s.length() == 0 ||
    			indexStart < 0 ||
    			indexEnd <= indexStart ||
    			indexEnd > s.length())
    		return false;
    	
    	//檢查第 1 個字元
    	char a = s.charAt(indexStart);
    	boolean pmPrefix = (a == '+' || a == '-'); //正或負號開頭
    	if(!pmPrefix && (a < '0' || a > '9') && a != '.') //只能 '+', '-', '.', 數字字元開頭
    		return false;
    	
    	int len = indexEnd - indexStart;
    	if(pmPrefix && len < 2) //只 1 個 '+' 或 '-' 字元不算是數字
    		return false;
    	
    	int dotPos = -1;
    	if(a == '.') {
    		if(len < 2) //只 1 個 '.' 字元不算是數字
    			return false;
    		dotPos = indexStart;
    	}
    	
    	int ePos = -1; //'E' 或 'e' 的位置
    	
    	//檢查第 2 個(及以後)的字元
    	for(int i = indexStart + 1, ii = indexEnd - 1; i < indexEnd; i++) {
    		a = s.charAt(i);
    		
    		if(a == '.') {
    			if(dotPos != -1) //小數點只能出現一次
    				return false;
    			if(ePos != -1 && i > ePos) //小數點在 'E/e' 之後時
    				return false;
    			dotPos = i;
    		} else if(a == 'E' || a == 'e') { //scientific notation, 後面可能接 '+' 或 '-', 再接數字, 或直接接數字
    			if(ePos != -1) //'E/e' 最多出現一次
    				return false;
    			if(i == ii) //'E/e' 排在最後一個字元時
    				return false;
    			ePos = i;
    		} else if(a == '+' || a == '-') { //'+' 或 '-', 前一字元為 'E/e', 其後必有數字
    			if(i == ii) //排在最後一個字元, 後面無法再接數字
    				return false;
    			if(i != (ePos + 1)) //緊接在 'E/e' 之後
    				return false;
    		} else if(a < '0' || a > '9') {
    			return false;
    		}
    	}
    	return true;
    }

    /**
     * 檢查某個字元是否為中/日/韓等國家的字元.
     * @param ch
     * @return
     */
    public static boolean isDBCS(char ch) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
        // JDK >= 1.4
        if(!Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals(block) &&
                !Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A.equals(block) &&
                !Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS.equals(block) &&
                !Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION.equals(block)) {
            return false;
        }
        return true;
    }

    /**
     * 判斷 s 的字串型式是否為空字串.
     * 若為 null 或者包含的字元都是空白字元(whitespace, tab, 全形空白等), 亦都視為空字串.
     * @param s
     */
    public static boolean isEmpty(final Object s) {
    	if(s == null)
    		return true;
    	final String s2 = s.toString();
    	if(s2.length() == 0)
    		return true;
    	for(int i = 0, ii = s2.length(); i < ii; i++) {
    		char a = s2.charAt(i);
    		if(a <= '\u0020' || a == 12288) //ASCII 空白字元, 全形空白
    			continue;
    		return false; //非空白字元
    	}
        return true;
    }
    
    /** s 的字串型式, 如為空字元或所含字元均為空白字元(whitespace, tab, 全形空白等), 則化為 null, 否則仍輸出原字串. */
    public static String nullifyEmpty(final Object s) {
    	final String ret = (s == null) ? null : s.toString();
    	return isEmpty(ret) ? null : ret;
    }

    ///**
    // * 檢查輸入字元是否為 Big5 繁體字.
    // * @param ch
    // * @deprecated 在　UTF-8 環境中, 已無意義.
    // */
    //@Deprecated
    //public static boolean isTraditionalChinese(char ch) {
    //    try {
    //        String s = String.valueOf(ch);
    //        return s.equals(new String(s.getBytes("MS950"), "MS950")); //TODO: 效率不好
    //    } catch(java.io.UnsupportedEncodingException e) {
    //        return false;
    //    }
    //}

    /**
     * 檢查輸入字元是否為全形字.<br>
     * <b>注意</b>: 只把 ASCII 字元及少數特殊字元視為半形字.<br>
     * 半形字: U+0000 ~ U+0080, U+00A2, U+00A3, U+00A5, U+00A6, U+00AC,
     * U+00AF, U+20A9, U+2985, U+2986, U+FFE8, U+FFEE, U+FFE8 ~ U+FFEE<br>
     * 其他劃為全形字.
     * <p>參考: <a href="http://zh.wikipedia.org/wiki/%E5%85%A8%E5%BD%A2">全形 - 維基百科</a>
     */
    public static boolean isWideChar(final char c) {
        if(c <= 0x80 || //ASCII
                //(c >= 0xFF61 && c <= 0xFF9F) || //JIS X 0201 (日文及韓文的半形字區因有可能挪為中文自造字區, 故仍歸為全形字區)
                //(c >= 0xFFA0 && c <= 0xFFDC) || //韓文
                c == 0x00A2 || c == 0x00A3 || c == 0x00A5 || c == 0x00A6 || c == 0x00AC || //其他
                c == 0x00AF || c == 0x20A9 ||c == 0x2985 || c == 0x2986 ||
                (c >= 0xFFE8 && c <= 0xFFEE))
            return false;
        return true;
    }

    /**
     * 將阿拉伯數字轉成"大寫"中文數字字串.
     * 範例：<code>
     *     num2BigCNum(123)      ==&gt; 壹佰貳拾參
     * </code>
     */
    public static String num2BigCNum(double dblArabic) {
        return num2ChineseNum(dblArabic, true);
    }
    
    /** @see #num2BigCNum(double) */
    public static String num2BigCNum(Number dblArabic) {
    	if(dblArabic == null)
    		return null;
    	return num2BigCNum(dblArabic.doubleValue());
    }

    /**
     * num2ChineseNum  將阿拉伯數字轉成中文數字字串.
     * @param dblArabic 輸入的數字.
     * @param isBig 中文數字是否大寫.
     * @author Michael Tsai.  Feb-19-2004.
     */
    private static String num2ChineseNum(double dblArabic, boolean isBig) {
        final String CHINESE_NUMERIC_1 = "零一二三四五六七八九十百千";
        final String CHINESE_NUMERIC_2 = "零壹貳參肆伍陸柒捌玖拾佰仟";
        String sChineseNumeric;
        String sArabic;
        String sIntArabic;
        int iPosOfDecimalPoint;
        int i;
        int iDigit;
        int iSection;
        String sSectionArabic;
        String sSection;
        boolean bInZero = true;
        boolean bMinus;
        StringBuilder sbResult = new StringBuilder();

        sChineseNumeric = CHINESE_NUMERIC_1;
        if(isBig)
            sChineseNumeric = CHINESE_NUMERIC_2;

        //將數字轉成阿拉伯數字字串
        DecimalFormat df = new DecimalFormat();
        df.setGroupingUsed(false);
        sArabic = df.format(dblArabic);
        df = null;

        //是否為負數？
        if(sArabic.startsWith("-")) {
            bMinus = true;
            sArabic = sArabic.substring(1);
        } else {
            bMinus = false;
        }

        //取得小數點的位置
        iPosOfDecimalPoint = sArabic.indexOf(".");

        //去除多餘的小數點，ex: "12.0" == "12".
        if((iPosOfDecimalPoint >= 0) && (Math.floor(dblArabic) == dblArabic)) {
            sArabic = sArabic.substring(0, iPosOfDecimalPoint);
            iPosOfDecimalPoint = -1;
        }

        //先處理整數的部分
        if(iPosOfDecimalPoint < 0)
            sIntArabic = StrUtil.reverse(sArabic);
        else
            sIntArabic = StrUtil.reverse(sArabic.substring(0, iPosOfDecimalPoint));

        //從個位數起以每四位數為一小節
        for(iSection = 0; iSection <= (sIntArabic.length() - 1) / 4; iSection++) {
            sSectionArabic = StrUtil.substr(sIntArabic, iSection * 4, 4);
            sSection = "";

            //以下的 i 控制: 個十百千位四個位數
            for(i = 0; i < sSectionArabic.length(); i++) {
                iDigit = sSectionArabic.charAt(i) - 48;
                if(iDigit == 0) {

                    //1. 避免 '零' 的重覆出現
                    //2. 個位數的 0 不必轉成 '零'
                    if(!bInZero && (i != 0)) {
                        sSection = "零" + sSection;
                    }
                    bInZero = true;
                } else {
                    switch(i) {
                    case 1: //十
                        sSection = sChineseNumeric.charAt(10) + sSection;
                        break;
                    case 2: //百
                        sSection = sChineseNumeric.charAt(11) + sSection;
                        break;
                    case 3: //千
                        sSection = sChineseNumeric.charAt(12) + sSection;
                        break;
                    }
                    sSection = sChineseNumeric.charAt(iDigit) + sSection;
                    bInZero = false;
                }
            }

            //加上該小節的位數
            if(sSection.length() == 0) {
                if((sbResult.length() > 0) && (sbResult.toString().indexOf("零") != 0)) {
                    sbResult.insert(0, "零");
                }
            } else {
                switch(iSection) {
                case 0:
                    sbResult = new StringBuilder().append(sSection);
                    break;
                case 1:
                    sbResult.insert(0, sSection + "萬");
                    break;
                case 2:
                    sbResult.insert(0, sSection + "億");
                    break;
                case 3:
                    sbResult.insert(0, sSection + "兆");
                    break;
                }
            }
        }

        //處理小數點右邊的部分
        if(iPosOfDecimalPoint > 0) {
            sbResult.append("點");
            for(i = iPosOfDecimalPoint + 1; i < sArabic.length(); i++) {
                iDigit = sArabic.charAt(i) - 48;
                sbResult.append(sChineseNumeric.charAt(iDigit));
            }
        }

        //其他例外狀況的處理
        if(sbResult.length() == 0)
            sbResult = new StringBuilder().append("零");
        else if(sbResult.toString().startsWith("一十"))
            sbResult = new StringBuilder().append(sbResult.substring(1));
        else if(sbResult.toString().startsWith("點"))
            sbResult.insert(0, "零");
        if(bMinus)
            sbResult.insert(0, "負");

        return sbResult.toString();
    }
     
    /**
     * num2CNum  將阿拉伯數字轉成中文數字字串.
     * 範例：<ul>
     * <li> num2CNum(10002.34) ==&gt; 一萬零二點三四
     * <li> num2CNum(123)      ==&gt; 一百二十三
     * <li> num2CNum(123.0)    ==&gt; 一百二十三（注意多餘的"點零"會自動去除）
     * </ul>
     * @param dblArabic
     */
    public static String num2CNum(double dblArabic) {
        return num2ChineseNum(dblArabic, false);
    }
    
    /** @see #num2CNum(double) */
    public static String num2CNum(Number dblArabic) {
    	if(dblArabic == null)
    		return null;
    	return num2CNum(dblArabic.doubleValue());
    }

    /**
     * Parse an integer string (use radix while parsing, default is 10), returning a default value on errors or when s is empty.
     */
    public static Integer parseIntWithDefault(final String s, final Integer defaultValue, final Integer radix) {
        try {
        	if(s == null || s.length() == 0)
        		return defaultValue;
            return (radix == null || radix == 10) ? Integer.parseInt(s) : Integer.parseInt(s, radix);
        } catch(Throwable t) {
            return defaultValue;
        }
    }
    
    /**
     * Parse an integer, returning a default value on errors.
     */
    public static Integer parseIntWithDefault(final String s, final Integer defaultValue) {
    	return parseIntWithDefault(s, defaultValue, null);
    }

    /**
     * Parse an integer string (use radix while parsing, default is 10), returning null when s is empty.
     */
    public static Integer parseInt(final String s, final Integer radix) {
    	if(s == null || s.length() == 0)
    		return null;
        return (radix == null || radix == 10) ? Integer.parseInt(s) : Integer.parseInt(s, radix);
    }
    
    /**
     * Parse an integer string, returning null when s is empty.
     */
    public static Integer parseInt(final String s) {
    	return parseInt(s, null);
    }
    
    /**
     * Parse a long integer string (use radix while parsing, default is 10), returning a default value on errors or when s is empty.
     */
    public static Long parseLongWithDefault(final String s, final Long defaultValue, final Integer radix) {
        try {
        	if(s == null || s.length() == 0)
        		return defaultValue;
            return (radix == null || radix == 10) ? Long.parseLong(s) : Long.parseLong(s, radix);
        } catch(Throwable t) {
            return defaultValue;
        }
    }

    /**
     * Parse a long integer string, returning a default value on errors or when s is empty.
     */
    public static Long parseLongWithDefault(final String s, final Long defaultValue) {
    	return parseLongWithDefault(s, defaultValue, null);
    }

    /**
     * Parse an long integer string (use radix while parsing, default is 10), returning null when s is empty.
     */
    public static Long parseLong(final String s, final Integer radix) {
    	if(s == null || s.length() == 0)
    		return null;
        return (radix == null || radix == 10) ? Long.parseLong(s) : Long.parseLong(s, radix);
    }
    
    /**
     * Parse an long integer string, returning null when s is empty.
     */
    public static Long parseLong(final String s) {
    	return parseLong(s, null);
    }
    
    /**
     * Parse a float number string, returning a default value on errors or when s is empty.
     */
    public static Float parseFloatWithDefault(final String s, final Float defaultValue) {
    	try {
    		if(s == null || s.length() == 0)
    			return defaultValue;
    		return Float.parseFloat(s);
    	} catch(Throwable t) {
    		return defaultValue;
    	}
    }
    
    /**
     * Parse a float number string, returning null when s is empty.
     */
    public static Float parseFloat(final String s) {
		if(s == null || s.length() == 0)
			return null;
		return Float.parseFloat(s);
    }
    
    /**
     * Parse a double number string, returning a default value on errors or when s is empty.
     */
    public static Double parseDoubleWithDefault(final String s, final Double defaultValue) {
    	try {
    		if(s == null || s.length() == 0)
    			return defaultValue;
    		return Double.parseDouble(s);
    	} catch(Throwable t) {
    		return defaultValue;
    	}
    }
    
    /**
     * Parse a double number string, returning null when s is empty.
     */
    public static Double parseDouble(final String s) {
		if(s == null || s.length() == 0)
			return null;
		return Double.parseDouble(s);
    }
    
    /**
     * 產生一個字串，該字串會重複指定的字元 n 次.
     * @param ch 要重複的字元
     * @param n 重複次數
     * @return n 為 0 者, 傳回空字串
     */
    public static String repeat(final char ch, final int n) {
    	if(n < 1)
    		return "";
        char[] ar = new char[n];
        Arrays.fill(ar, ch);
        return new String(ar);
    }
    
    /**
     * 產生一個字串，該字串會重複指定的字串 n 次.
     * @param s 要重複的字串
     * @param n 重複次數
     * @return s 為 null 或 n 為 0 者, 傳回空字串
     */
    public static String repeat(final String s, final int n) {
    	if(s == null || s.length() == 0 || n < 1)
    		return "";
    	if(n == 1)
    		return s;
    	
    	//from 陳皓(左耳朵耗子)
    	final StringBuilder s2 = new StringBuilder().append(s);
    	final StringBuilder ret = new StringBuilder(s.length() * n);
    	int n2 = n;
    	while(true) {
    		if((n2 & 1) == 1)
    			ret.append(s2);
    		n2 >>= 1;
    		if(n2 > 0)
    			s2.append(s2.toString());
    		else
    			break;
    	}
    	return ret.toString();
    }

    /**
     * 把字串中某個子字串以另一個字串取代.
     * @param s 輸入的字串
     * @param oldStr 舊的字串.
     * @param newStr 新的字串.
     * @return 替換後的字串. 無符合 oldStr 子字串者, 傳回原字串.
     */
    public static String replaceAll(String s, String oldStr, String newStr) {
    	if(s == null || s.length() == 0)
    		return s;
        int idx = s.indexOf(oldStr); //符合 oldStr 的起始 index
        if(idx == -1) //無符合 oldStr 子字串者, 傳回原字串
            return s;
        
        StringBuilder sb = new StringBuilder().append(s.substring(0, idx)).append(newStr);
        int i = idx + oldStr.length(); //開始搜尋之起始 index
        while(i < s.length()) {
            idx = s.indexOf(oldStr, i);
            
            if(idx == -1) {
                sb.append(s.substring(i));
                break;
            }
            sb.append(s.substring(i, idx)).append(newStr);
            i = idx + oldStr.length();
        }
        return sb.toString();
    }
    
    /**
     * 將字串反向排列, 例如: 傳入 '1234', 傳回 '4321'.
     * @param s
     */
    public static String reverse(String s) {
    	if(s == null || s.length() == 0)
    		return s;
        StringBuilder sb = new StringBuilder(s.length()).append(s);
        return sb.reverse().toString();
    }

    /**
     * 替字串陣排序(升冪, 按 unicode code point 的順序).
     */
    public static void sortStrings(String[] strings) {
    	if(strings == null || strings.length < 2)
    		return;
        Arrays.sort(strings, new Comparator<String>() {
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
        });
    }

    /**
     * 替字串陣排序.
     * @param ascending 是否升冪排列(按 unicode code point 的順序).
     */
    public static void sortStrings(String[] strings, boolean ascending) {
        if(ascending) {
            sortStrings(strings);
        } else {
        	if(strings == null || strings.length < 2)
        		return;
            Arrays.sort(strings, new Comparator<String>() {
                    public int compare(String o1, String o2) {
                        return o2.compareTo(o1);
                    }
            });
        }
    }

    /**
     * 取子字串，從 start 開始取，取 n 個.
     * @param s 輸入的字串.
     * @param start 起始位置. 如果為負值, 代表由字串尾起算, 如 -1 代表字串最後一個字元位置
     * @param length 字元數. 如果超界者, 取至實際的右邊界為止
     */
    public static String substr(final String s, final int start, final int length) {
    	return slice(s, start, start + length);
    }
    
    /**
     * 截取輸入字串 s 之片斷.
     * <ol>
     *   <li>原字串為 null 者, 傳回 null; 其他不為 null 的輸入字串, 皆會傳回非 null 值
     *   <li>beginIndex 必排在 endIndex 之前, 否則傳回空字串(當傳入非 null 字串時. 並注意負值代表由字串結尾起算, 如 -1 代表字串最後一個字元位置)
     *   <li>原字串部分在欲選取邊界之外者, 單取邊界內的片斷
     *   <li>原字串全部在欲選取邊界之外者, 傳回空字串
     * </ol>
     * @param s 原字串
     * @param beginIndex 字串片斷起始字元 index. 如果為負值, 代表由字串尾起算
     * @param endIndex 字串截取之終止 index (截取片斷之結尾字元所在 index + 1). 如果為負值, 代表由字串尾起算
     */
    public static String slice(final String s, final int beginIndex, final int endIndex) {
    	if(s == null || s.length() == 0)
    		return s;
    	int i = beginIndex, j = endIndex;
    	
    	//起始, 終止 index 同值(=> 同正或同負)
    	if(i == j)
    		return "";

    	//確保 index 皆由輸入字串首起算
    	if(i < 0)
    		i += s.length();
    	if(j < 0)
    		j += s.length();
    	
    	//起始 index 必小於終止 index
    	if(i >= j)
    		return "";

    	//原字串全部在欲選取邊界之外
    	if(i >= s.length() || j <= 0)
    		return "";
    	
    	//只在字串的範圍內截取
    	if(i < 0)
    		i = 0;
    	if(j > s.length())
    		j = s.length();
    	
    	if(i == 0 && j == s.length())
    		return s;
    	return s.substring(i, j);
    }
    
    /**
     * 截取輸入字串 s 之片斷, 自 beginIndex 開始直至原字串結尾.
     * <ol>
     *   <li>原字串為 null 者, 傳回 null; 其他不為 null 的輸入字串, 皆會傳回非 null 值
     *   <li>原字串部分在欲選取邊界之外者, 單取邊界內的片斷
     *   <li>原字串全部在欲選取邊界之外者, 傳回空字串
     * </ol>
     * @param s 原字串
     * @param beginIndex 字串片斷起始字元 index. 如果為負值, 代表由字串尾起算, 如 -1 代表字串最後一個字元位置
     */
    public static String slice(final String s, final int beginIndex) {
    	return slice(s, beginIndex, (s == null) ? 0 : s.length());
    }

    /**
     * 字串轉整數.
     * @param s 輸入的字串(視為 10 進位數字).
     * @return 字串的整數值.
     */
    public static int toInt(String s) {
        return Integer.parseInt(s);
    }

    /**
     * 字串轉整數，當轉換失敗時，傳回預設值，而不會丟出異常.
     * @param s 輸入的字串(視為 10 進位數字).
     * @param defaultValue 預設值.
     * @return 整數.
     */
    public static int toInt(String s, int defaultValue) {
        try {
        	if(s == null || s.length() == 0)
        		return defaultValue;
            return Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /** 視輸入字串為十進位數, 而轉為數字型態; 若輸入值為 null 或空字串者則傳回 null. */
    public static Integer asInt(String s) {
    	return (s == null ||s.length() == 0) ? null : Integer.valueOf(s);
    }
    
    /** 視輸入字串為十進位數, 而轉為數字型態; 若輸入值為 null 或空字串者則傳回 null. */
    public static Long asLong(String s) {
    	return (s == null ||s.length() == 0) ? null : Long.valueOf(s);
    }
    
    /** 視輸入字串為十進位數, 而轉為數字型態; 若輸入值為 null 或空字串者則傳回 null. */
    public static Short asShort(String s) {
    	return (s == null ||s.length() == 0) ? null : Short.valueOf(s);
    }
    
    /** 視輸入字串為十進位數, 而轉為數字型態; 若輸入值為 null 或空字串者則傳回 null. */
    public static Byte asByte(String s) {
    	return (s == null ||s.length() == 0) ? null : Byte.valueOf(s);
    }
    
    /** 視輸入字串為十進位數, 而轉為數字型態; 若輸入值為 null 或空字串者則傳回 null. */
    public static Float asFloat(String s) {
    	return (s == null ||s.length() == 0) ? null : Float.valueOf(s);
    }
    
    /** 視輸入字串為十進位數, 而轉為數字型態; 若輸入值為 null 或空字串者則傳回 null. */
    public static Double asDouble(String s) {
    	return (s == null ||s.length() == 0) ? null : Double.valueOf(s);
    }

    /**
     * 將字串頭尾的空白去掉, 首尾無空白字元者, 傳回原字串. 所謂的空白包括：
     * 空白字元(' '), 跳格符號(tab), 換行符號, 以及全形空白.
     * @param s
     * @return 去掉頭尾空白後的字串.
     */
    public static String trim(String s) {
        if(s == null || s.length() == 0)
            return s;
        return trimRight(trimLeft(s));
    }

    /**
     * 將字串前面的空白去掉, 開頭無空白字元者, 傳回原字串. 所謂的空白包括：
     * 空白字元(' '), 跳格符號(tab), 換行符號, 以及全形空白.
     */
    public static String trimLeft(String s) {
        if(s == null || s.length() == 0)
            return s;
        int i;
        for(i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if(ch != ' ' && ch != '\t' && ch != '\r' && ch != '\n' && ch != '　')
                break;
        }
        if(i == 0)
            return s;
        return s.substring(i);
    }
    
    /**
     * 將字串前面重覆的 c 字去掉, 開頭無該字元者, 傳回原字串.
     * @param s
     * @param c
     */
    public static String trimLeft(String s, char c) {
        if(s == null || s.length() == 0 || s.charAt(0) != c)
            return s;
        
        int i;
        for(i = 0; i < s.length(); i++) {
            if(s.charAt(i) != c)
                break;
        }
        if(i == 0)
            return s;
        return s.substring(i);
    }

    /**
     * 將字串後面的空白去掉, 尾端無空白字元者, 傳回原字串. 所謂的空白包括：
     * 空白字元(' '), 跳格符號(tab), 換行符號, 以及全形空白.
     * @param s
     * @return 去掉尾部空白的字串.
     */
    public static String trimRight(String s) {
        if(s == null || s.length() == 0)
            return s;
        
        int i;
        for(i = s.length() - 1; i >= 0; i--) {
            char ch = s.charAt(i);
            if(ch != ' ' && ch != '\t' && ch != '\r' && ch != '\n' && ch != '　')
                break;
        }
        if(i == (s.length() - 1))
            return s;
        return s.substring(0, i + 1);
    }
    
    /**
     * 將字串後面重覆的 c 字元去掉, 尾端無該字元者, 傳回原字串.
     * @param s
     * @param c
     */
    public static String trimRight(String s, char c) {
        if(s == null || s.length() == 0 || s.charAt(s.length() - 1) != c)
            return s;
        
        int i;
        for(i = s.length() - 1; i >= 0; i--) {
            if(s.charAt(i) != c)
                break;
        }
        if(i == (s.length() - 1))
            return s;
        return s.substring(0, i + 1);
    }

    /**
     * 將中文字串中之全形符號字元(如全形空白, 數字, 英文字, 標點符號等)轉成對應的半形字.
     */
    public static String toHalfChar(String s) {
        if(s == null || s.equals(""))
            return s;
        char[] ca = s.toCharArray();
        
        for(int i = 0; i < ca.length; i++) {
            switch(ca[i]) {
                case 12288:  ca[i] = 32; continue;       //全形空白轉成半形空白
                case '〔':                               // #12308;
                case '［':  ca[i] = '['; continue;
                case '〕':  ca[i] = ']'; continue;       // #12309;
                case '︿':  ca[i] = '^'; continue;       // #65087;
                case '╱':  ca[i] = '/'; continue;       // #9585;
                case '╲':  ca[i] = '\\'; continue;       // #9586;
                case '’':  ca[i] = '\''; continue;       // #8217;
                case '”':  ca[i] = '"'; continue;       // #8221;
                case 8764:  ca[i] = '~'; continue;
                case '。':  ca[i] = '.'; continue;
                case '｀':
                case '！':
                case '＠':
                case '＃':
                case '＄':
                case '％':
                case '＆':
                case '＊':
                case '（':
                case '）':
                case '＿':
                case '－':
                case '＋':
                case '＝':
                case '｛':
                case '｝':
                case '，':
                case '．':
                case '；':
                case '：':
                case '？':
                case '＜':
                case '＞':
                case '｜':
                case '１':  case '２':  case '３':  case '４':  case '５':  case '６':  case '７':  case '８':  case '９':  case '０':
                case 'ａ':  case 'ｂ':  case 'ｃ':  case 'ｄ':  case 'ｅ':  case 'ｆ':  case 'ｇ':  case 'ｈ':  case 'ｉ':  case 'ｊ':  case 'ｋ':  case 'ｌ':  case 'ｍ':  case 'ｎ':  case 'ｏ':  case 'ｐ':  case 'ｑ':  case 'ｒ':  case 'ｓ':  case 'ｔ':  case 'ｕ':  case 'ｖ':  case 'ｗ':  case 'ｘ':  case 'ｙ':  case 'ｚ':
                case 'Ａ':  case 'Ｂ':  case 'Ｃ':  case 'Ｄ':  case 'Ｅ':  case 'Ｆ':  case 'Ｇ':  case 'Ｈ':  case 'Ｉ':  case 'Ｊ':  case 'Ｋ':  case 'Ｌ':  case 'Ｍ':  case 'Ｎ':  case 'Ｏ':  case 'Ｐ':  case 'Ｑ':  case 'Ｒ':  case 'Ｓ':  case 'Ｔ':  case 'Ｕ':  case 'Ｖ':  case 'Ｗ':  case 'Ｘ':  case 'Ｙ':  case 'Ｚ':
                    ca[i] = (char)(ca[i] - 65248);
                    continue;
                default:
                   continue; //其他都不轉了
            }
        }
        return String.valueOf(ca);
    }
    
    /**
     * 半型轉全型(只針對 ASCII 半形字).
     */
    public static String toChineseFullChar(String s) {
        if(s == null || s.equals("")) {
            return "";
        }
        char[] ca = s.toCharArray();

        for(int i = 0; i < ca.length; i++) {
            if(ca[i] > 128)
                continue;

            switch(ca[i]) {
                case '\b':    //backspace BS
                case '\t':    //horizontal tab HT
                case '\n':    //linefeed LF
                case '\r':    //carriage return CR
                case '\f':    //form feed FF
                    continue;
                case 32:  ca[i] = 12288; continue; //半型空白轉成全型空白
                case '[':  ca[i] = '〔'; continue;       // #12308;
                case ']':  ca[i] = '〕'; continue;       // #12309;
                case '^':  ca[i] = '︿'; continue;       // #65087;
                case '/':  ca[i] = '╱'; continue;       // #9585;
                case '\\': ca[i] = '╲'; continue;       // #9586;
                case '\'': ca[i] = '’'; continue;       // #8217;
                case '"':  ca[i] = '”'; continue;       // #8221;
                case '~':  ca[i] = 8764; continue;
                case '`':
                case '!':
                case '@':
                case '#':
                case '$':
                case '%':
                case '&':
                case '*':
                case '(':
                case ')':
                case '_':
                case '-':
                case '+':
                case '=':
                case '{':
                case '}':
                case ',':
                case '.':
                case ';':
                case ':':
                case '?':
                case '<':
                case '>':
                case '|':
                    ca[i] = (char)(ca[i] + 65248);
                    continue;
                default:
                   if(Character.isLetterOrDigit(ca[i])) ca[i] = (char)(ca[i] + 65248); //數字或字母
                   continue; //其他都不轉了
            }
        }
        return String.valueOf(ca);
    }

    /**
     * 印出字串本身或物件內含字串, 任何內含的 null 值將轉為空字元.
     */
    public static String print(Object ... o) {
		if(o == null || o.length == 0)
			return "";
		if(o.length == 1)
			return (o[0] == null) ? "" : o[0].toString();
        return concatenate("", o, new ToStringHandler() {
				@Override public String toString(Object obj) {
					return (obj == null) ? "" : obj.toString();
				}
			});
    }
    
    /**
     * 印出 o 物件代表的字串, 若為 null 值則代以下一個參數值 defaultValue 物件代表的字串; 
     * 若再為 null 則再換下一參數值; 
     * 若最後後仍為 null 值, 則印出空字串.
     * @deprecated 為避免 null 值與空字串的混淆, 宜使用 {@link #selectNotNull(Object...)} 或 {@link #selectNotEmpty(Object...)} 配合 {@link #print(Object...)}
     */
    @Deprecated
    public static String printWithDefault(final Object ... values) {
    	final String ret = selectNotNull(values);
    	return (ret == null) ? "" : ret;
    }
    
    /**
     * 印出第一個 value 物件代表的字串, 若為 null 值則改取下一個參數值 value 物件代表的字串; 
     * 若再為 null 則再換下一 value; 
     * 若最後後仍為 null 值, 則傳回 null
     * @see #selectNotEmpty(Object...) 比較 selectNotEmpty(Object...)
     */
    public static String selectNotNull(final Object ... values) {
    	if(values == null || values.length == 0)
    		return null;
    	for(final Object v : values) {
    		if(v != null)
    			return v.toString();
    	}
    	return null;
    }
    
    /**
     * 印出第一個 value 物件代表的字串, 若為空值(可能 null 或為空字串或只含空白字元), 則改取下一個 value 物件代表的字串; 
     * 若再為空值則再換下一個 value; 
     * 若最後後仍為空值, 則傳回最後的參數值.
     * @see #selectNotNull(Object...) 比較 selectNotNull(Object...)
     */
    public static String selectNotEmpty(final Object ... values) {
    	if(values == null || values.length == 0)
    		return null;
    	for(final Object value : values) {
    		final String v = (value == null) ? null : value.toString();
    		if(!isEmpty(v))
    			return v.toString();
    	}
    	
    	final Object last = values[values.length - 1];
    	return (last == null) ? null : last.toString();
    }

    ///**
    // * 身分證號檢查. 符合規定傳回 true,反之傳回 false.
    // * @param idno 身分證號
    // * @return 合格為 true, 否則 false
    // */
    //public static boolean checkIdno(String idno) {
    //    idno = idno.toUpperCase();
    //    if(idno.length() != 10)
    //        return false;
    //
    //    char x = idno.charAt(0);
    //    int  y1 = 0;
    //    int  y2 = 0;
    //    switch(x) {
    //        case 'A': y1 = 1; y2 = 0; break;
    //        case 'B': y1 = 1; y2 = 1; break;
    //        case 'C': y1 = 1; y2 = 2; break;
    //        case 'D': y1 = 1; y2 = 2; break;
    //        case 'E': y1 = 1; y2 = 4; break;
    //        case 'F': y1 = 1; y2 = 5; break;
    //        case 'G': y1 = 1; y2 = 6; break;
    //        case 'H': y1 = 1; y2 = 7; break;
    //        case 'I': y1 = 3; y2 = 4; break;
    //        case 'J': y1 = 1; y2 = 8; break;
    //        case 'K': y1 = 1; y2 = 9; break;
    //        case 'L': y1 = 2; y2 = 0; break;
    //        case 'M': y1 = 2; y2 = 1; break;
    //        case 'N': y1 = 2; y2 = 2; break;
    //        case 'O': y1 = 3; y2 = 5; break;
    //        case 'P': y1 = 2; y2 = 3; break;
    //        case 'Q': y1 = 2; y2 = 4; break;
    //        case 'R': y1 = 2; y2 = 5; break;
    //        case 'S': y1 = 2; y2 = 6; break;
    //        case 'T': y1 = 2; y2 = 7; break;
    //        case 'U': y1 = 2; y2 = 8; break;
    //        case 'V': y1 = 2; y2 = 9; break;
    //        case 'W': y1 = 3; y2 = 2; break;
    //        case 'X': y1 = 3; y2 = 0; break;
    //        case 'Y': y1 = 3; y2 = 1; break;
    //        case 'Z': y1 = 3; y2 = 3; break;
    //        default: return false;
    //    }
    //
    //    int zz = (y1 * 1) +  (y2 * 9) +
    //             (8 * Integer.parseInt(idno.substring(1, 2))) +
    //             (7 * Integer.parseInt(idno.substring(2, 3))) +
    //             (6 * Integer.parseInt(idno.substring(3, 4))) +
    //             (5 * Integer.parseInt(idno.substring(4, 5))) +
    //             (4 * Integer.parseInt(idno.substring(5, 6))) +
    //             (3 * Integer.parseInt(idno.substring(6, 7))) +
    //             (2 * Integer.parseInt(idno.substring(7, 8))) +
    //             (1 * Integer.parseInt(idno.substring(8, 9)));
    //    zz = 10 - (zz % 10);
    //
    //    return ((zz % 10) == 0) ? (0 == Integer.parseInt(idno.substring(9, 10))) : (zz == Integer.parseInt(idno.substring(9, 10)));
    //}
    
    /**
     * 對 s 字串中對特定字元, 在其前插入逃逸字元. 如果 s 字串中無該特定字元者, 仍傳回原字串 s.<br>
     * 例如：<pre><code>
     * String s = "this is a 'book'.";
     * String s1 = StrUtil.escape(s, '\'', '\\'); //在每個 "'" 字元前插入反斜線 "\" 字元
     * </code></pre>
     * @param s
     * @param charToEscape 
     * @param escapeChar
     */
    public static String escape(String s, char charToEscape, char escapeChar) {
        if(s == null)
            return null;
        
        int m = s.indexOf(charToEscape);
        if(m == -1)
            return s;
        
        StringBuilder buff = new StringBuilder().append(s.substring(0, m)).append(escapeChar).append(charToEscape); //前段先處理掉
        m++;
        int len = s.length();
        for(int n = 0; m < len && (n = s.indexOf(charToEscape, m)) != -1; m = n + 1)
            buff.append(s.substring(m, n)).append(escapeChar).append(charToEscape);
        if(m < len)
            buff.append(s.substring(m));
        return buff.toString();
    }
    
    /**
     * 把輸入的陣列成員化為字串串接起來.
     * 相當於 Java StrinBuilder 之 append(String) 的用法.
     * @param s 欲串接成單一字串的陣列.
     *   <ul>
     *   <li>若其中含 null 值成員者, 該成員將化為 "null" 字串(同 StringBuffer/StringBuilder append() 的行為);<br>
     *   	 但若 s=null 者仍傳回空字串.
     *   <li>若陣列個數為 0 者, 傳回空字串.
     *   </ul>
     * @return 緊密接合後的新字串
     */
    public static <T> String string(T ... s) {
    	if(s == null || s.length == 0)
    		return "";
    	if(s.length == 1)
    		return String.valueOf(s[0]);
    	StringBuilder ret = new StringBuilder();
    	for(int i = 0; i < s.length; i++)
    		ret.append(s[i]);
    	return ret.toString();
    }
    
    /**
     * 把 list 中各元素之值組合成為一字串.
     * @param delimiter 分隔 list 各元素值的分隔字元
     * @param objs list 物件, objs=null 者仍傳回空字串; objs 成員中有 null 值者, 將顯示為 "null"
     */
    public static String join(final String delimiter, final Iterable<?> objs) {
        return concatenate(delimiter, objs, (ToStringHandler)null);
    }
    
    /**
     * 把陣列中各元素之值組合成為一字串.
     * @param delimiter 分隔 list 各元素值的分隔字元, null 值者視為空字串
     * @param objs 物件陣列, objs=null 者仍傳回空字串; objs 成員中有 null 值者, 將顯示為 "null"
     */
    public static <T> String join(final String delimiter, final T ... objs) {
    	if(objs == null || objs.length == 0)
    		return "";
    	final int last = objs.length - 1; //last element index of objs
    	if(objs[last] != null && objs[last] instanceof ToStringHandler) {
    		if(last == 0)
    			return "";
    		
    		if(last == 1) { //objs 可能為 [ Object[], <ToStringHandler> ]
    			if(objs[0] == null)
    				return "";
    			if(!objs[0].getClass().isArray())
    				return ((ToStringHandler)objs[1]).toString(objs[0]);
    			
    			final Object[] objs2 = (Object[])objs[0];
    			return concatenate(delimiter, objs2, 0, objs2.length, (ToStringHandler)objs[last]);
    		}
    		return concatenate(delimiter, objs, 0, last, (ToStringHandler)objs[last]);
    	}
        return concatenate(delimiter, objs, 0, objs.length, (ToStringHandler)null);
    }
    
    /**
     * 把 list 中各元素之值組合成為一字串.
     * @param delimiter 分隔 list 各元素值的分隔字元, null 值者視為空字串
     * @param objs list 物件, objs=null 者仍傳回空字串
     * @param handler 自訂輸入 objs 的成員如何轉為字串成員
     */
    public static String concatenate(final String delimiter, final Iterable<?> objs, ToStringHandler handler) {
        if(objs == null)
            return "";
        Iterator<?> iter = objs.iterator();
        if(!iter.hasNext())
            return "";
        final StringBuilder buffer = new StringBuilder();
        if(handler != null) {
            buffer.append(handler.toString(iter.next()));
            while(iter.hasNext()) {
            	if(delimiter != null)
            		buffer.append(delimiter);
        		buffer.append(handler.toString(iter.next()));
            }
        } else {
            buffer.append(iter.next());
            while(iter.hasNext()) {
            	if(delimiter != null)
            		buffer.append(delimiter);
            	buffer.append(iter.next());
            }
        }
        return buffer.toString();
    }
    
    /**
     * 把陣列中各元素之值組合成為一字串.
     * @param delimiter 分隔 list 各元素值的分隔字元, null 值者視為空字串
     * @param objs 物件陣列, objs=null 者仍傳回空字串; objs 成員中有 null 值者, 將顯示為 "null"
     * @param handler 自訂輸入 objs 的成員如何轉為字串成員
     */
    public static <T> String concatenate(final String delimiter, final T[] objs, ToStringHandler handler) {
    	return concatenate(delimiter, objs, 0, (objs == null) ? 0 : objs.length, handler);
    }
    
    static String concatenate(final String delimiter, final Object[] objs, final int beginIndex, final int endIndex, ToStringHandler handler) {
    	if(objs == null || objs.length == 0)
    		return "";
    	int start = (beginIndex < 0) ? 0 : beginIndex;
    	int end = (endIndex > objs.length) ? objs.length : endIndex;
        if(end <= start)
            return "";
        
        StringBuilder buffer = null;
        if(handler != null) {
        	buffer = new StringBuilder().append(handler.toString(objs[start]));
        	for(int i = start + 1; i < end; i++) {
        		if(delimiter != null)
        			buffer.append(delimiter);
        		buffer.append(handler.toString(objs[i]));
        	}
        } else {
        	buffer = new StringBuilder().append(objs[start]);
        	for(int i = start + 1; i < end; i++) {
        		if(delimiter != null)
        			buffer.append(delimiter);
        		buffer.append(objs[i]);
        	}
        }
        return buffer.toString();
    }
    
	//////////////////////////////////////////////////////////////////////////
	//for primary type array (如: Integer[] is instanceof Object[], 但 int[] not instanceof Object[])
	//Java 的 generic 機制不支援 primary type, 以下只好逐一列舉
	//////////////////////////////////////////////////////////////////////////

    /** 把陣列中各元素之值組合成為一字串. */
	public static String join(final String delimiter, final byte[] data) {
		if(data == null || data.length == 0)
			return "";

		StringBuilder buffer = null;
		buffer = new StringBuilder().append(data[0]);
		for(int i = 1, ii = data.length; i < ii; i++) {
			if(delimiter != null)
				buffer.append(delimiter);
			buffer.append(data[i]);
		}
		return buffer.toString();
	}

	/** 把陣列中各元素之值組合成為一字串. */
	public static String join(final String delimiter, final short[] data) {
		if(data == null || data.length == 0)
			return "";

		StringBuilder buffer = null;
		buffer = new StringBuilder().append(data[0]);
		for(int i = 1, ii = data.length; i < ii; i++) {
			if(delimiter != null)
				buffer.append(delimiter);
			buffer.append(data[i]);
		}
		return buffer.toString();
	}

	/** 把陣列中各元素之值組合成為一字串. */
	public static String join(final String delimiter, final int[] data) {
		if(data == null || data.length == 0)
			return "";

		StringBuilder buffer = null;
		buffer = new StringBuilder().append(data[0]);
		for(int i = 1, ii = data.length; i < ii; i++) {
			if(delimiter != null)
				buffer.append(delimiter);
			buffer.append(data[i]);
		}
		return buffer.toString();
	}

	/** 把陣列中各元素之值組合成為一字串. */
	public static String join(final String delimiter, final long[] data) {
		if(data == null || data.length == 0)
			return "";

		StringBuilder buffer = null;
		buffer = new StringBuilder().append(data[0]);
		for(int i = 1, ii = data.length; i < ii; i++) {
			if(delimiter != null)
				buffer.append(delimiter);
			buffer.append(data[i]);
		}
		return buffer.toString();
	}

	/** 把陣列中各元素之值組合成為一字串. */
	public static String join(final String delimiter, final float[] data) {
		if(data == null || data.length == 0)
			return "";

		StringBuilder buffer = null;
		buffer = new StringBuilder().append(data[0]);
		for(int i = 1, ii = data.length; i < ii; i++) {
			if(delimiter != null)
				buffer.append(delimiter);
			buffer.append(data[i]);
		}
		return buffer.toString();
	}

	/** 把陣列中各元素之值組合成為一字串. */
	public static String join(final String delimiter, final double[] data) {
		if(data == null || data.length == 0)
			return "";

		StringBuilder buffer = null;
		buffer = new StringBuilder().append(data[0]);
		for(int i = 1, ii = data.length; i < ii; i++) {
			if(delimiter != null)
				buffer.append(delimiter);
			buffer.append(data[i]);
		}
		return buffer.toString();
	}

	/** 把陣列中各元素之值組合成為一字串. */
	public static String join(final String delimiter, final char[] data) {
		if(data == null || data.length == 0)
			return "";
		
		StringBuilder buffer = null;
		buffer = new StringBuilder().append(data[0]);
		for(int i = 1, ii = data.length; i < ii; i++) {
			if(delimiter != null)
				buffer.append(delimiter);
			buffer.append(data[i]);
		}
		return buffer.toString();
	}

	/** 把陣列中各元素之值組合成為一字串. */
	public static String join(final String delimiter, final boolean[] data) {
		if(data == null || data.length == 0)
			return "";

		StringBuilder buffer = null;
		buffer = new StringBuilder().append(data[0]);
		for(int i = 1, ii = data.length; i < ii; i++) {
			if(delimiter != null)
				buffer.append(delimiter);
			buffer.append(data[i]);
		}
		return buffer.toString();
	}
	
    public static interface ToStringHandler {
        /**
         * 自行定義物件如何轉成字串
         * @param obj
         * @return 由 obj 所轉成的字串
         */
        String toString(Object obj);
    }
}
