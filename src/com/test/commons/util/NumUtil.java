package com.test.commons.util;

import java.math.BigDecimal;

/**
 * 與數字相關的工具.
 */
public class NumUtil {
	
	/**
	 * 將輸入值 n 之小數部分四捨五入(round off)至 scale 位數.
	 * @param n 輸入值
	 * @param scale 精確度(小數點下的位數. 但若為負值者, 代表小數點前的 scale 個整數位數四捨五入為 0, 例:
	 * <ul>
	 *   <li><code>NumUtil.round(123.456, -2) -&gt; 100.0 (<span style="color:red;font-weight:bold;">!!!</span>)</code>
	 *   <li><code>NumUtil.round(-123.456, -2) -&gt; -100.0 (<span style="color:red;font-weight:bold;">!!!</span>)</code>
	 *   <li><code>NumUtil.round(123.345, 1) -&gt; 123.3</code>
	 *   <li><code>NumUtil.round(123.345, 0) -&gt; 123.0</code>
	 *   <li><code>NumUtil.round(-123.345, 0) -&gt; -123.0</code>
	 *   <li><code>NumUtil.round(123.345, 2) -&gt; 123.35</code>
	 *   <li><code>NumUtil.round(123.34, 3) -&gt; 123.34</code>
	 *   <li><code>NumUtil.round(123.56, 3) -&gt; 123.56</code>
	 *   <li><code>NumUtil.round(-123.56, 3) -&gt; -123.56</code>
	 *   <li><code>NumUtil.round(123.56, 0) -&gt; 124.0</code>
	 *   <li><code>NumUtil.round(-123.56, 0) -&gt; -124.0</code>
	 * </ul>
	 * @return 四捨五入後的值, 如果 scale 大於輸入值 n 的小數位數, 則傳回 n
	 * @see java.math.BigDecimal
	 */
	public static double round(final double n, final int scale) {
		return new BigDecimal(n).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	
	/**
	 * 將輸入值 n 之小數部分四捨五入(round off)至 scale 位數. 用例見 {@link #round(double, int)}
	 * @param n 輸入值
	 * @param scale 精確度(小數點下的位數)
	 * @return 四捨五入後的值. 如果 scale 大於輸入值 n 的小數位數, 則傳回與 n 同值的物件(但不與 n 為同一物件). 如果 n 為 null 則傳回 null
	 * @see #round(double, int)
	 */
	public static Number round(final Number n, final int scale) {
		if(n == null)
			return null;
		return new BigDecimal(n.doubleValue()).setScale(scale, BigDecimal.ROUND_HALF_UP);
	}
	
	/**
	 * 將輸入值 n 之小數部分捨去至 scale 位數. 用例:
	 * <ul>
	 *   <li><code>NumUtil.roundDown(123.56, 0) -&gt; 123.0</code>
	 *   <li><code>NumUtil.roundDown(-123.56, 0) -&gt; -123.0</code>
	 *   <li><code>NumUtil.roundDown(789.888, -2) -&gt; 7<span style="color:red;">00</span>.0</code>
	 *   <li><code>NumUtil.roundDown(-789.888, -2) -&gt; -7<span style="color:red;">00</span>.0</code>
	 *   <li><code>NumUtil.roundDown(123.345, 2) -&gt; 123.34</code>
	 *   <li><code>NumUtil.roundDown(123.34, 3) -&gt; 123.3<span style="color:red;font-weight:bold;">39</span></code>
	 *   <li><code>NumUtil.roundDown(123.34, 2) -&gt; 123.3<span style="color:red;font-weight:bold;">3</span></code>
	 *   <li><code>NumUtil.roundDown(123.56, 3) -&gt; 123.5<span style="color:red;font-weight:bold;">59</span></code>
	 *   <li><code>NumUtil.roundDown(123.56, 2) -&gt; 123.5<span style="color:red;font-weight:bold;">5</span></code>
	 *   <li><code>NumUtil.roundDown(-123.56, 3) -&gt; -123.5<span style="color:red;font-weight:bold;">59</span></code>
	 *   <li><code>NumUtil.roundDown(-123.56, 2) -&gt; -123.5<span style="color:red;font-weight:bold;">5</span></code>
	 * </ul>
	 * <div style="color:blue;">注意: 以上捨去位數多於或等於原數值位數導至的誤差, 乃本函數底層 java.math.BigDecimal 原有之現象, 暫不予更動之.</div>
	 * @param n 輸入值
	 * @param scale 精確度(小數點下的位數. 但若為負值者, 代表小數點前的 scale 個整數位數轉為 0, 例: <code>NumUtil.roundDown(167.4, -2) -&gt; 100.0</code>)
	 * @return 捨位後的值, 如果 scale 大於輸入值 n 的小數位數, 則傳回 n
	 * @see java.math.BigDecimal
	 */
	public static double roundDown(final double n, final int scale) {
		return new BigDecimal(n).setScale(scale, BigDecimal.ROUND_DOWN).doubleValue();
	}
	
	/**
	 * 將輸入值 n 之小數部分捨去至 scale 位數. 用例參考 {@link #roundDown(double, int)}
	 * @param n 輸入值
	 * @param scale 精確度(小數點下的位數. 但若為負值者, 代表小數點前的 scale 個整數位數轉為 0, 例: <code>NumUtil.roundDown(167.4, -2) -&gt; 100.0</code>)
	 * @return 捨位後的值. 如果 scale 大於輸入值 n 的小數位數, 則傳回與 n 同值的物件(但不與 n 為同一物件). 如果 n 為 null 則傳回 null
	 * @see #roundDown(double, int)
	 */
	public static Number roundDown(final Number n, final int scale) {
		if(n == null)
			return null;
		return new BigDecimal(n.doubleValue()).setScale(scale, BigDecimal.ROUND_DOWN);
	}
	
	/**
	 * 將輸入值 n 之小數部分完全進位至 scale 位數. 用例:
	 * <ul>
	 *   <li><code>NumUtil.roundUp(3123.456, -2) -&gt; 3200.0 (<span style="color:red;font-weight:bold;">!!!</span>)</code>
	 *   <li><code>NumUtil.roundUp(-3123.456, -2) -&gt; -3200.0 (<span style="color:red;font-weight:bold;">!!!</span>)</code>
	 *   <li><code>NumUtil.roundUp(123.345, 1) -&gt; 123.4</code>
	 *   <li><code>NumUtil.roundUp(-123.345, 1) -&gt; -123.4</code>
	 *   <li><code>NumUtil.roundUp(123.345, 0) -&gt; 124.0</code>
	 *   <li><code>NumUtil.roundUp(-123.345, 0) -&gt; -124.0</code>
	 *   <li><code>NumUtil.roundUp(123.56, 3) -&gt; 123.56</code>
	 *   <li><code>NumUtil.roundUp(-123.56, 3) -&gt; -123.56</code>
	 * </ul>
	 * @param n 輸入值
	 * @param scale 精確度(小數點下的位數. 但若為負值者, 代表進位至小數點前的 scale 個整數位數, 例: <code>NumUtil.roundUp(123.4, -2) -&gt; 200.0</code>)
	 * @return 進位後的值, 如果 scale 大於輸入值 n 的小數位數, 則傳回 n
	 * @see java.math.BigDecimal
	 */
	public static double roundUp(final double n, final int scale) {
		return new BigDecimal(n).setScale(scale, BigDecimal.ROUND_UP).doubleValue();
	}
	
	/**
	 * 將輸入值 n 之小數部分完全進位至 scale 位數. 用例參考 {@link #roundUp(double, int)}
	 * @param n 輸入值
	 * @param scale 精確度(小數點下的位數. 但若為負值者, 代表進位至小數點前的 scale 個整數位數, 例: <code>NumUtil.roundUp(123.4, -2) -&gt; 200.0</code>)
	 * @return 進位後的值. 如果 scale 大於輸入值 n 的小數位數, 則傳回與 n 同值的物件(但不與 n 為同一物件). 如果 n 為 null 則傳回 null
	 * @see #roundUp(double, int)
	 */
	public static Number roundUp(final Number n, final int scale) {
		if(n == null)
			return null;
		return new BigDecimal(n.doubleValue()).setScale(scale, BigDecimal.ROUND_UP);
	}
	
	/** 兩值是否相等(唯均不為 null 且內含值相等時為 true) */
	public static boolean equals(final Number n1, final Number n2) {
		return (n1 != null && n2 != null && n1.equals(n2));
	}
	
	/** 兩值是否相等(唯均不為 null 且內含值相等時為 true) */
	public static boolean equals(final Number n1, final byte n2) {
		return (n1 != null && n1.byteValue() == n2);
	}
	
	/** 兩值是否相等(唯均不為 null 且內含值相等時為 true) */
	public static boolean equals(final byte n1, final Number n2) {
		return (n2 != null && n2.byteValue() == n1);
	}
	
	/** 兩值是否相等(唯均不為 null 且內含值相等時為 true) */
	public static boolean equals(final Number n1, final short n2) {
		return (n1 != null && n1.shortValue() == n2);
	}
	
	/** 兩值是否相等(唯均不為 null 且內含值相等時為 true) */
	public static boolean equals(final short n1, final Number n2) {
		return (n2 != null && n2.shortValue() == n1);
	}
	
	/** 兩值是否相等(唯均不為 null 且內含值相等時為 true) */
	public static boolean equals(final Number n1, final int n2) {
		return (n1 != null && n1.intValue() == n2);
	}
	
	/** 兩值是否相等(唯均不為 null 且內含值相等時為 true) */
	public static boolean equals(final int n1, final Number n2) {
		return (n2 != null && n2.intValue() == n1);
	}
	
	/** 兩值是否相等(唯均不為 null 且內含值相等時為 true) */
	public static boolean equals(final Number n1, final long n2) {
		return (n1 != null && n1.longValue() == n2);
	}
	
	/** 兩值是否相等(唯均不為 null 且內含值相等時為 true) */
	public static boolean equals(final long n1, final Number n2) {
		return (n2 != null && n2.longValue() == n1);
	}
	
	/** 兩值是否相等(唯均不為 null 且內含值相等時為 true) */
	public static boolean equals(final Number n1, final float n2) {
		return (n1 != null && n1.floatValue() == n2);
	}
	
	/** 兩值是否相等(唯均不為 null 且內含值相等時為 true) */
	public static boolean equals(final float n1, final Number n2) {
		return (n2 != null && n2.floatValue() == n1);
	}
	
	/** 兩值是否相等(唯均不為 null 且內含值相等時為 true) */
	public static boolean equals(final Number n1, final double n2) {
		return (n1 != null && n1.doubleValue() == n2);
	}
	
	/** 兩值是否相等(唯均不為 null 且內含值相等時為 true) */
	public static boolean equals(final double n1, final Number n2) {
		return (n2 != null && n2.doubleValue() == n1);
	}
}
