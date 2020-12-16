package com.test.commons.util;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.test.commons.exception.NonResultException;

/**
 * 輔助使用標準 JDBC 操作 SQL, 方便對 ResultSet, PreparedStatement 等物件操作的工具.
 * <br>
 * 注意: 以下所指的 "paramIndex" 變數, 圴依照 JDBC 的用法, 由 1 起算.
 * <p>
 * 用例 1: 使用標準 JDBC API 執行 SQL 並對 ResultSet 取值
 * <pre><code> //遇欄位型態為 integer, 其值為 null 的欄位
 * try (Connection conn = ds.getConnection()) {
 *     String sql = "...";
 *     try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
 *         pstmt.setXXX(1, ...);
 *         pstmt.setXXX(2, ...);
 *         ...
 *         try (ResultSet rs = pstmt.executeQuery()) {
 *             while(rs.next()) {
 *                 Integer n = rs.getInt(1); //若欄位值是 null 時, 此處會得到 0
 *                 if(rs.<b>wasNull</b>()) //得回頭檢測此欄位值是 null 還是 0
 *                     n = null;
 *     
 *                 //上面敘述對整數欄位取值, 如果改用 DBUtil 工具時
 *                 //Integer n = DBUtil.<b>intValue</b>(rs, 1); //若該欄位值為 null, 則此時 n=null    
 *             } //<i>出了這個區塊, JVM(JDK 1.7+) 會自動呼叫 rs.close()</i>
 *         } //<i>出了這個區塊, JVM(JDK 1.7+) 會自動呼叫 pstmt.close()</i>
 *     } //<i>出了這個區塊, JVM(JDK 1.7+) 會自動呼叫 conn.close()</i>
 * }
 * </code></pre>
 * 
 * 用例 2: 對 SQL 中的參數(佔位符號)設值
 * <pre><code> try (Connection conn = ds.getConnection()) {
 *     String sql = "select * from xxx where x=? and y=? and z &gt; ?";
 *     try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
 *         DBUtil.<b>set</b>(pstmt, 1L, "zzz", 123); //利用 DBUtil 一次替所有 SQL 參數設值
 *         //如果只使用原始 JDBC API 則要寫成如下
 *         //pstmt.setLong(1, 1L);
 *         //pstmt.setString(2, "zzz");
 *         //pstmt.setInt(3, 123);
 *         try (ResultSet rs = pstmt.executeQuery()) { //查詢
 *             while(rs.next()) { //取查詢結果
 *                 ...
 *             }
 *         }
 *     }
 * }
 * </code></pre>
 * 
 * 用例 3: 對 SQL 中的參數(佔位符號)設值, 且部分欄位將設為 null 值時
 * <pre><code> try (Connection conn = ds.getConnection()) {
 *     String sql = "update xxx set y=?, <b>z=?</b> where x=?";
 *     try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
 *         DBUtil.set(pstmt, "zzz", <b>null</b>, 1L); //利用 DBUtil 一次替所有 SQL 參數設值(有個 integer 欄位要設成 null)
 *         //如果只使用原始 JDBC API 則要寫成如下
 *         //pstmt.setString(1, "zzz");
 *         //pstmt.<b>setNull</b>(2, Types.INTEGER); //無法寫成 pstmt.setInt(2, null)
 *         //pstmt.setLong(3, 1L);
 *         pstmt.executeUpdate(); //SQL update
 *     }
 * }
 * </code></pre>
 * 
 * 用例 4: 對 PreparedStatement 參數設值並把查詢結果化為 list of JavaBean - <b>也許這會是最常用的使用方式</b>.<br>
 * (bean property 要以 JPA 之 &#64;Column　或 &#64;Column(name="...") 修飾, 以決定如何對應至 table 欄位)
 * <pre><code> try (Connection conn = ds.getConnection()) {
 *     String sql = "select * from employee where groupid=? and birthday &gt;= ?";
 *     try (PreparedStatement <b>pstmt</b> = conn.prepareStatement(sql)) {
 *         java.util.Date somday = ...; //某日
 *         
 *         for(String groupId : ...) { //多組條件. 同一 PreparedStatement 物件, 可重複使用, 多次設值-查詢
 *             //對 SQL 參數設值, 然後查詢, 並將每筆結果塞進 Employee bean, 多筆組成 list
 *             List&lt;Employee&gt; data = DBUtil.<b>query</b>(pstmt, Employee.class, groupId, somday);
 *             ...
 *         }
 *     }
 * }
 * </code></pre>
 * 
 * 用例 5: 除了自動把 table 欄位值對應至 JavaBean, 也可自行決定如何對應至任何物件.<br>
 * <pre><code> try (Connection conn = ds.getConnection()) {
 *     String sql = "select salary from employee where sex=? and birthday &gt;= ?";
 *     try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
 *         //查詢並且把結果化為 list of int, 
 *         List&lt;Integer&gt; data = DBUtil.<b>query</b>(pstmt, new Object[] { 0, somday }, new DBRowMapper&lt;Integer&gt;() {
 *             &#64;Override mapRow(ResultSet rs, int rowNum) throws SQLException {
 *                 return DBUtil.intValue(rs, "salary"); //取我們要的欄位值, 交給 DBUtil 自動組成 List&lt;Integer&gt;
 *             }
 *         });
 *         ...
 *     }
 * }
 * 
 * </code></pre>
 */
final public class DBUtil {
	private static final ThreadLocal<Boolean> _callPreparedStatementGetParameterMetaData = new ThreadLocal<Boolean>(); //是否要呼叫 PreparedStatement.getParameterMetaData() 取 SQL 敘述裡的 "?" 對應的 type
	//private static final Map<Class<?>, Integer> _javaTypeToSqlTypeMap = new HashMap<Class<?>, Integer>(32); //ref: org.springframework.jdbc.core.StatementCreatorUtils
	private static final int TYPE_UNKNOWN = Integer.MIN_VALUE; //ref: org.springframework.jdbc.core.StatementCreatorUtils, org.springframework.jdbc.core.SqlTypeValue, org.springframework.jdbc.support.JdbcUtils
	private DBUtil() {}
	
	//static { //ref: org.springframework.jdbc.core.StatementCreatorUtils
	//	_javaTypeToSqlTypeMap.put(boolean.class, Types.BOOLEAN);
	//	_javaTypeToSqlTypeMap.put(Boolean.class, Types.BOOLEAN);
	//	_javaTypeToSqlTypeMap.put(byte.class, Types.TINYINT);
	//	_javaTypeToSqlTypeMap.put(Byte.class, Types.TINYINT);
	//	_javaTypeToSqlTypeMap.put(short.class, Types.SMALLINT);
	//	_javaTypeToSqlTypeMap.put(Short.class, Types.SMALLINT);
	//	_javaTypeToSqlTypeMap.put(int.class, Types.INTEGER);
	//	_javaTypeToSqlTypeMap.put(Integer.class, Types.INTEGER);
	//	_javaTypeToSqlTypeMap.put(long.class, Types.BIGINT);
	//	_javaTypeToSqlTypeMap.put(Long.class, Types.BIGINT);
	//	_javaTypeToSqlTypeMap.put(BigInteger.class, Types.BIGINT);
	//	_javaTypeToSqlTypeMap.put(float.class, Types.FLOAT);
	//	_javaTypeToSqlTypeMap.put(Float.class, Types.FLOAT);
	//	_javaTypeToSqlTypeMap.put(double.class, Types.DOUBLE);
	//	_javaTypeToSqlTypeMap.put(Double.class, Types.DOUBLE);
	//	_javaTypeToSqlTypeMap.put(BigDecimal.class, Types.DECIMAL);
	//	_javaTypeToSqlTypeMap.put(java.sql.Date.class, Types.DATE);
	//	_javaTypeToSqlTypeMap.put(java.sql.Time.class, Types.TIME);
	//	_javaTypeToSqlTypeMap.put(java.sql.Timestamp.class, Types.TIMESTAMP);
	//	_javaTypeToSqlTypeMap.put(Blob.class, Types.BLOB);
	//	_javaTypeToSqlTypeMap.put(Clob.class, Types.CLOB);
	//}
	
	//以下參考 BeanRowMapper
	
	/** 將欄位值取出成為字串型式. */
	public static String stringValue(ResultSet rs, String columnName) {
		try {
			return rs.getString(columnName);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 將欄位值取出成為字串型式. */
	public static String stringValue(ResultSet rs, int paramIndex) {
		try {
			return rs.getString(paramIndex);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}

	/** 對 SQL VARCHAR 或 LONGVARCHAR 型態欄位設值 */
	public static void setString(PreparedStatement pstmt, int paramIndex, String value) {
		try {
			pstmt.setString(paramIndex, value);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 將欄位值取出成為 byte 型態, 如欄位值為 null 則傳回 null. */
	public static Byte byteValue(ResultSet rs, String columnName) {
		try {
			final byte v = rs.getByte(columnName);
			return (v == 0 && rs.wasNull()) ? null : v;
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 將欄位值取出成為 byte 型態, 如欄位值為 null 則傳回 null. */
	public static Byte byteValue(ResultSet rs, int paramIndex) {
		try {
			final byte v = rs.getByte(paramIndex);
			return (v == 0 && rs.wasNull()) ? null : v;
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}

	/** 對 SQL TINYINT 型態欄位設值 */
	public static void setByte(PreparedStatement pstmt, int paramIndex, Byte value) {
		try {
			if(value == null)
				pstmt.setNull(paramIndex, Types.TINYINT);
			else
				pstmt.setByte(paramIndex, value);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 將欄位值取出成為 short 型態, 如欄位值為 null 則傳回 null. */
	public static Short shortValue(ResultSet rs, String columnName) {
		try {
			final short v = rs.getShort(columnName);
			return (v == 0 && rs.wasNull()) ? null : v;
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 將欄位值取出成為 short 型態, 如欄位值為 null 則傳回 null. */
	public static Short shortValue(ResultSet rs, int paramIndex) {
		try {
			final short v = rs.getShort(paramIndex);
			return (v == 0 && rs.wasNull()) ? null : v;
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 對 SQL SMALLINT 型態欄位設值 */
	public static void setShort(PreparedStatement pstmt, int paramIndex, Short value) {
		try {
			if(value == null)
				pstmt.setNull(paramIndex, Types.SMALLINT);
			else
				pstmt.setShort(paramIndex, value);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 將欄位值取出成為 int 型態, 如欄位值為 null 則傳回 null. */
	public static Integer intValue(ResultSet rs, String columnName) {
		try {
			final int v = rs.getInt(columnName);
			return (v == 0 && rs.wasNull()) ? null : v;
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}

	/** 將欄位值取出成為 int 型態, 如欄位值為 null 則傳回 null. */
	public static Integer intValue(ResultSet rs, int paramIndex) {
		try {
			final int v = rs.getInt(paramIndex);
			return (v == 0 && rs.wasNull()) ? null : v;
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 對 SQL INTEGER 型態欄位設值 */
	public static void setInt(PreparedStatement pstmt, int paramIndex, Integer value) {
		try {
			if(value == null)
				pstmt.setNull(paramIndex, Types.INTEGER);
			else
				pstmt.setInt(paramIndex, value);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 將欄位值取出成為 long 型態, 如欄位值為 null 則傳回 null. */
	public static Long longValue(ResultSet rs, String columnName) {
		try {
			long v = rs.getLong(columnName);
			return (v == 0 && rs.wasNull()) ? null : v;
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 將欄位值取出成為 long 型態, 如欄位值為 null 則傳回 null. */
	public static Long longValue(ResultSet rs, int paramIndex) {
		try {
			final long v = rs.getLong(paramIndex);
			return (v == 0 && rs.wasNull()) ? null : v;
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 對 SQL BIGINT 型態欄位設值 */
	public static void setLong(PreparedStatement pstmt, int paramIndex, Long value) {
		try {
			if(value == null)
				pstmt.setNull(paramIndex, Types.BIGINT);
			else
				pstmt.setLong(paramIndex, value);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 將欄位值取出成為 float 型態, 如欄位值為 null 則傳回 null. */
	public static Float floatValue(ResultSet rs, String columnName) {
		try {
			float v = rs.getFloat(columnName);
			return (v == 0F && rs.wasNull()) ? null : v;
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 將欄位值取出成為 float 型態, 如欄位值為 null 則傳回 null. */
	public static Float floatValue(ResultSet rs, int paramIndex) {
		try {
			final float v = rs.getFloat(paramIndex);
			return (v == 0F && rs.wasNull()) ? null : v;
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 對 SQL REAL 型態欄位設值 */
	public static void setFloat(PreparedStatement pstmt, int paramIndex, Float value) {
		try {
			if(value == null)
				pstmt.setNull(paramIndex, Types.REAL);
			else
				pstmt.setFloat(paramIndex, value);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 將欄位值取出成為 double 型態, 如欄位值為 null 則傳回 null. */
	public static Double doubleValue(ResultSet rs, String columnName) {
		try {
			final double v = rs.getDouble(columnName);
			return (v == 0D && rs.wasNull()) ? null : v;
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 將欄位值取出成為 double 型態, 如欄位值為 null 則傳回 null. */
	public static Double doubleValue(ResultSet rs, int paramIndex) {
		try {
			final double v = rs.getDouble(paramIndex);
			return (v == 0D && rs.wasNull()) ? null : v;
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 對 SQL DOUBLE 型態欄位設值 */
	public static void setDouble(PreparedStatement pstmt, int paramIndex, Double value) {
		try {
			if(value == null)
				pstmt.setNull(paramIndex, Types.DOUBLE);
			else
				pstmt.setDouble(paramIndex, value);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 將欄位值取出成為 <code>java.sql.Timestamp</code> 型態. */
	public static java.sql.Timestamp timestampValue(ResultSet rs, String columnName) {
		try {
			return rs.getTimestamp(columnName);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 將欄位值取出成為 <code>java.sql.Timestamp</code> 型態. */
	public static java.sql.Timestamp timestampValue(ResultSet rs, int paramIndex) {
		try {
			return rs.getTimestamp(paramIndex);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}

	/** 對 SQL TIMESTAMP (timestamp/datetime) 型態欄位設值 */
	public static void setTimestamp(PreparedStatement pstmt, int paramIndex, java.sql.Timestamp value) {
		try {
			pstmt.setTimestamp(paramIndex, value);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 對 SQL TIMESTAMP (timestamp/datetime) 型態欄位設值 */
	public static void setTimestamp(PreparedStatement pstmt, int paramIndex, java.util.Date value) {
		final java.sql.Timestamp value2 = (value == null) ? (java.sql.Timestamp)null : new java.sql.Timestamp(value.getTime());
		setTimestamp(pstmt, paramIndex, value2);
	}
	
	/** 針對 SQL timestamp/datetime 型態欄位取值, 直接得出 <code>java.util.Date 型態</code>. */
	public static java.util.Date datetimeValue(ResultSet rs, String columnName) {
		return timestampValue(rs, columnName);
	}
	
	/** 針對 SQL timestamp/datetime 型態欄位取值, 直接得出 <code>java.util.Date 型態</code>. */
	public static java.util.Date datetimeValue(ResultSet rs, int paramIndex) {
		return timestampValue(rs, paramIndex);
	}
	
	/** 將欄位值取出成為 <code>java.sql.Date</code> 型態(無時刻). */
	public static java.sql.Date dateValue(ResultSet rs, String columnName) {
		try {
			return rs.getDate(columnName);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 將欄位值取出成為 <code>java.sql.Date</code> 型態(無時刻). */
	public static java.sql.Date dateValue(ResultSet rs, int paramIndex) {
		try {
			return rs.getDate(paramIndex);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 對 SQL DATE 型態(無時刻)欄位設值 */
	public static void setDate(PreparedStatement pstmt, int paramIndex, java.sql.Date value) {
		try {
			pstmt.setDate(paramIndex, value);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 將欄位值取出成為 <code>java.sql.Time</code> 型態(無日期). */
	public static java.sql.Time timeValue(ResultSet rs, String columnName) {
		try {
			return rs.getTime(columnName);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 將欄位值取出成為 <code>java.sql.Time</code> 型態(無日期). */
	public static java.sql.Time timeValue(ResultSet rs, int paramIndex) {
		try {
			return rs.getTime(paramIndex);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 對 SQL TIME 型態(無日期)欄位設值 */
	public static void setTime(PreparedStatement pstmt, int paramIndex, java.sql.Time value) {
		try {
			pstmt.setTime(paramIndex, value);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 將欄位原始內容倒出. */
	public static byte[] bytesValue(ResultSet rs, String columnName) {
		try {
			return rs.getBytes(columnName);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}

	/** 將欄位原始內容倒出. */
	public static byte[] bytesValue(ResultSet rs, int paramIndex) {
		try {
			return rs.getBytes(paramIndex);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 對 SQL VARBINARY 或 LONGVARBINARY 型態欄位設值. */
	public static void setBytes(PreparedStatement pstmt, int paramIndex, byte[] value) {
		try {
			pstmt.setBytes(paramIndex, value);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 
	 * 將欄位值取出成為 boolean 型態, 如欄位值為 null 則傳回 null.<br>
	 * 下列型態欄位傳回 false:
	 * <ul>
	 *   <li>CHAR 或 VARCHAR 型態欄位且其值為 "0"
	 *   <li>BIT, TINYINT, SMALLINT, INTEGER 或 BIGINT 且其值為 0
	 * </ul>
	 * 下列型態欄位傳回 true:
	 * <ul>
	 *   <li>CHAR 或 VARCHAR 型態欄位且其值為 "1"
	 *   <li>BIT, TINYINT, SMALLINT, INTEGER 或 BIGINT 且其值為 1
	 * </ul>
	 */
	public static Boolean booleanValue(ResultSet rs, String columnName) {
		try {
			final boolean v = rs.getBoolean(columnName);
			return (!v && rs.wasNull()) ? null : v;
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 
	 * 將欄位值取出成為 boolean 型態, 如欄位值為 null 則傳回 null.<br>
	 * 下列型態欄位傳回 false:
	 * <ul>
	 *   <li>CHAR 或 VARCHAR 型態欄位且其值為 "0"
	 *   <li>BIT, TINYINT, SMALLINT, INTEGER 或 BIGINT 且其值為 0
	 * </ul>
	 * 下列型態欄位傳回 true:
	 * <ul>
	 *   <li>CHAR 或 VARCHAR 型態欄位且其值為 "1"
	 *   <li>BIT, TINYINT, SMALLINT, INTEGER 或 BIGINT 且其值為 1
	 * </ul>
	 */
	public static Boolean booleanValue(ResultSet rs, int paramIndex) {
		try {
			final boolean v = rs.getBoolean(paramIndex);
			return (!v && rs.wasNull()) ? null : v;
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 對 SQL BOOLEAN 或 BIT 型態欄位設值 */
	public static void setBoolean(PreparedStatement pstmt, int paramIndex, Boolean value) {
		try {
			if(value != null)
				pstmt.setBoolean(paramIndex, value);
			else
				pstmt.setNull(paramIndex, Types.BOOLEAN);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 將欄位值取出成為 <code>java.math.BigDecimal</code> 型態. */
	public static BigDecimal bigDecimalValue(ResultSet rs, String columnName) {
		try {
			return rs.getBigDecimal(columnName);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 將欄位值取出成為 <code>java.math.BigDecimal</code> 型態. */
	public static BigDecimal bigDecimalValue(ResultSet rs, int paramIndex) {
		try {
			return rs.getBigDecimal(paramIndex);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 對 SQL NUMERIC 型態欄位設值 */
	public static void setBigDecimal(PreparedStatement pstmt, int paramIndex, BigDecimal value) {
		try {
			if(value == null)
				pstmt.setNull(paramIndex, Types.NUMERIC);
			else
				pstmt.setBigDecimal(paramIndex, value);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 將欄位值取出成為 <code>Blob</code> 型態. */
	public static Blob blobValue(ResultSet rs, String columnName) {
		try {
			return rs.getBlob(columnName);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 將欄位值取出成為 <code>Blob</code> 型態. */
	public static Blob blobValue(ResultSet rs, int paramIndex) {
		try {
			return rs.getBlob(paramIndex);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 對 SQL BLOB 型態欄位設值 */
	public static void setBlob(PreparedStatement pstmt, int paramIndex, Blob value) {
		try {
			pstmt.setBlob(paramIndex, value);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 將欄位值取出成為 <code>Clob</code> 型態. */
	public static Clob clobValue(ResultSet rs, String columnName) {
		try {
			return rs.getClob(columnName);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 將欄位值取出成為 <code>Clob</code> 型態. */
	public static Clob clobValue(ResultSet rs, int paramIndex) {
		try {
			return rs.getClob(paramIndex);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 對 SQL CLOB 型態欄位設值 */
	public static void setClob(PreparedStatement pstmt, int paramIndex, Clob value) {
		try {
			pstmt.setClob(paramIndex, value);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** @see java.sql.ResultSet#getObject(String) */
	public static Object objectValue(ResultSet rs, String columnName) {
		try {
			return rs.getObject(columnName);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** @see java.sql.ResultSet#getObject(int) */
	public static Object objectValue(ResultSet rs, int paramIndex) {
		try {
			return rs.getObject(paramIndex);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 對欄位設值, 由 JDBC driver 自行判斷如何對應至 SQL 資料型態. */
	public static void setObject(PreparedStatement pstmt, int paramIndex, Object value) {
		try {
			pstmt.setObject(paramIndex, value);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}

	/** 以 byte stream 方式將資料寫入欄位 (呼叫者自行關閉 value) */
	public static void setBinaryStream(PreparedStatement pstmt, int paramIndex, InputStream value) {
		try {
			pstmt.setBinaryStream(paramIndex, value);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/** 以 character stream 方式將資料寫入欄位 (呼叫者自行關閉 value) */
	public static void setCharacterStream(PreparedStatement pstmt, int paramIndex, Reader value) {
		try {
			pstmt.setCharacterStream(paramIndex, value);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/**
	 * 將欄位內容倒至檔案中(注意: 此功能無直接對應的 JDBC API)
	 * @param rs
	 * @param columnName
	 * @param file 放置欄位內容的檔案. 若為 null 者, 由系統決定暫存檔名及目錄.
	 * @return 即 file 參數所指的檔案
	 */
	public static File fileValue(final ResultSet rs, final String columnName, final File file) {
		InputStream in = null;
		
		try {
			final File f = (file != null) ? file : FileUtil.createTempFile("DBUtil-", "");
			in = rs.getBinaryStream(columnName);
			FileUtil.dump(in, f);
			in.close();
			in = null;
			return f;
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		} catch(RuntimeException re) {
			throw re;
		} catch(Throwable t) {
			throw new RuntimeException(t.getMessage(), t);
		} finally {
			if(in != null) try { in.close(); } catch(Throwable t) {}
		}
	}
	
	/**
	 * 將欄位內容倒至檔案中(注意: 此功能無直接對應的 JDBC API)
	 * @param rs
	 * @param paramIndex
	 * @param file 放置欄位內容的檔案. 若為 null 者, 由系統決定暫存檔名及目錄.
	 * @return 即 file 參數所指的檔案
	 */
	public static File fileValue(final ResultSet rs, final int paramIndex, final File file) {
		InputStream in = null;
		
		try {
			final File f = (file != null) ? file : FileUtil.createTempFile("DBUtil-", "");
			in = rs.getBinaryStream(paramIndex);
			FileUtil.dump(in, f);
			in.close();
			in = null;
			return f;
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		} catch(RuntimeException re) {
			throw re;
		} catch(Throwable t) {
			throw new RuntimeException(t.getMessage(), t);
		} finally {
			if(in != null) try { in.close(); } catch(Throwable t) {}
		}
	}
	
	/** 將欄位內容倒至暫存檔中. 位置可能在 web-app 自己的暂存目錄或 OS 的暫目錄下, 檔名由系統自訂 (注意: 此功能無直接對應的 JDBC API). */
	public static File fileValue(final ResultSet rs, final String columnName) {
		return fileValue(rs, columnName, (File)null);
	}
	
	/** 將欄位內容倒至暫存檔中. 位置可能在 web-app 自己的暂存目錄或 OS 的暫目錄下, 檔名由系統自訂 (注意: 此功能無直接對應的 JDBC API). */
	public static File fileValue(final ResultSet rs, final int paramIndex) {
		return fileValue(rs, paramIndex, (File)null);
	}
	
	/**
	 * 根據對 SQL 欄位所設之值的型態, 自動判斷對 PreparedStatement 物件設值的方式.
	 * <br>
	 * 注意: 若欲將欄位值設為 null, 且 SQL 裡只有一個參數佔位符號時, 應如下例設值:
	 * <pre><code> String sql = "update xxxx set xxx=? where yyy=1";
	 * ...
	 * 
	 * //DBUtil.set(pstmt, null);
	 * //上行敘述若不明確轉型, 則編譯時會發出警告(Java compiler 預設視為 DBUtil.set(pstmt, (Object[])null)), 
	 * //但本工具內部仍會將之轉成如下敘述, 視為對第 1 參數設 null 值.
	 * //為免日後個人的混餚, 仍應明確轉型.
	 * DBUtil.set(pstmt, (Object)null);
	 * </code></pre>
	 * <br>
	 * 
	 * 又若程式碼如:
	 * <pre><code> DBUtil.set(pstmt);</code></pre>
	 * 代表未指定 SQL 佔位符對應的值, 上行敘述什麼事也不做.
	 * 
	 * @param pstmt PreparedStatement 物件
	 * @param values 與 SQL 中的設值佔位符號(即 "?") 對應的值
	 */
	public static void set(final PreparedStatement pstmt, final Object ... values) {
		final Object[] v = (values == null) ? new Object[] { null } : //當輸入參數 values 為 null 時, 如: set(pstmt, null) 相當於 set(pstmt, (Object[])null) => values == null => 要幺成對第一參數設 null 值
			values; //當 values 不為 null, 如: set(pstmt) => values != null, values.length == 0 (此例代表未指定 SQL 參數值)
		for(int i = 0; i < v.length; i++)
			set(pstmt, i + 1, TYPE_UNKNOWN, v[i]);
	}
	
	/**
	 * 明確指定參數欄位的 SQL type, 並根據對應的設值型態, 自動判斷對 PreparedStatement 物件設值的方式.<br>
	 * @param pstmt PreparedStatement 物件
	 * @param values 與 SQL 中的設值佔位符號(即 "?") 對應的值
	 * @param sqlTypes 與 values 成員一一對應的 SQL type, 其值均為 java.sql.Types class 之成員.<br>
	 * 		&nbsp;&nbsp; 若為 null 者, 視為不指定欄位 SQL type 的設值方式 {@link #set(PreparedStatement, Object...) }
	 * @see java.sql.Types
	 */
	public static void set(final PreparedStatement pstmt, final Object[] values, final int[] sqlTypes) {
		if(values == null)
			throw new IllegalArgumentException("argument 'value' not specified");
		if(sqlTypes == null) {
			set(pstmt, values);
		} else {
			if(values.length != sqlTypes.length)
				throw new IllegalArgumentException("lengths of argument 'values' and 'sqlTypes' are not equal");
			for(int i = 0; i < values.length; i++)
				set(pstmt, i + 1, sqlTypes[i], values[i]);
		}
	}
	
	/**
	 * 對 PreparedStatement 的 SQL 參數設值, 然後執行查詢, 再把 ResultSet 每筆結果設值至 Java Bean(型態 T)中, 組成 list. 
	 * ResultSet 欄位值與 Bean 屬性如何對應, 由自定義的 DBRowMapper 實作物件決定.
	 * @param values 與 JDBC 所定義對 SQL 中的佔位符號 "?" 對應的值.
	 * @param rowMapper 決定每一筆查詢結果如何以 JavaBean 物件裝載. 常用的實作: {@link com.test.commons.spring.BeanRowMapper}
	 */
	public static <T> List<T> query(final PreparedStatement pstmt, final Object[] values, final DBRowMapper<T> rowMapper) {
		final List<T> ret = new ArrayList<T>();
		set(pstmt, values);
		
		try (ResultSet rs = pstmt.executeQuery()) {
			for(int i = 1; rs.next(); i++)
				ret.add(rowMapper.mapRow(rs, i));
			return ret;
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/**
	 * 對 PreparedStatement 的 SQL 參數設值, 然後執行查詢, 再把 ResultSet 每筆結果設值至 Java Bean(型態 T)中, 組成 list. 
	 * ResultSet 欄位值與 Bean 屬性如何對應, 由自定義的 DBRowMapper 實作物件決定.
	 * @param rowMapper 決定每一筆查詢結果如何以 JavaBean 物件裝載. 常用的實作: {@link com.test.commons.spring.BeanRowMapper}
	 * @param values 與 JDBC 所定義對 SQL 中的佔位符號 "?" 對應的值.
	 */
	public static <T> List<T> query(final PreparedStatement pstmt, final DBRowMapper<T> rowMapper, final Object ... values) {
		return query(pstmt, (values == null) ? new Object[0] : values, rowMapper);
	}
	
	/**
	 * 對 SQL 中的佔位符號設值, 然後執行查詢, 再把 ResultSet 每筆結果設值至 Java Bean(型態 T)中, 組成 list. 
	 * ResultSet 欄位值與 Bean 屬性如何對應, 由自定義的 DBRowMapper 實作物件決定.
	 * @param values 與 JDBC 所定義對 SQL 中的佔位符號 "?" 對應的值.
	 * @param rowMapper 決定每一筆查詢結果如何以 JavaBean 物件裝載. 常用的實作: {@link com.test.commons.spring.BeanRowMapper}
	 */
	public static <T> List<T> query(final Connection conn, final String sql, final Object[] values, final DBRowMapper<T> rowMapper) {
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			return query(pstmt, values, rowMapper);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/**
	 * 對 SQL 中的佔位符號設值, 然後執行查詢, 再把 ResultSet 每筆結果設值至 Java Bean(型態 T)中, 組成 list. 
	 * ResultSet 欄位值與 Bean 屬性如何對應, 由自定義的 DBRowMapper 實作物件決定.
	 * @param rowMapper 決定每一筆查詢結果如何以 JavaBean 物件裝載. 常用的實作: {@link com.test.commons.spring.BeanRowMapper}
	 * @param values 與 JDBC 所定義對 SQL 中的佔位符號 "?" 對應的值.
	 */
	public static <T> List<T> query(final Connection conn, final String sql, final DBRowMapper<T> rowMapper, final Object ... values) {
		return query(conn, sql, (values == null) ? new Object[0] : values, rowMapper);
	}
	
	/**
	 * 對 PreparedStatement 的 SQL 參數設值, 然後執行查詢, 再把 ResultSet 每筆結果的第一欄位值(型態 T)納入 list 中.
	 * @param type 與欲取值之欄位的 SQL 型態對應的 Java 型態. 可接受的型態: boolean/Boolean, String,
	 * 		byte/Byte, byte[], short/Short, int/Integer, long/Long, float/Float, double/Double, Number, BigDecimal,
	 * 		java.util.Date, java.sql.Timestamp, java.sql.Date, java.sql.Time,
	 * 		Blob, Clob
	 * @param values 與 JDBC 所定義對 SQL 中的佔位符號 "?" 對應的值.
	 */
	public static <T> List<T> queryForList(final PreparedStatement pstmt, final Class<T> type, final Object ... values) {
		final List<T> ret = new ArrayList<T>();
		set(pstmt, values);

		try (ResultSet rs = pstmt.executeQuery()) {
			while(rs.next())
				ret.add(retrieveFirstColumnValue(rs, type));
			return ret;
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/**
	 * 對 SQL 中的佔位符號設值, 然後執行查詢, 再把 ResultSet 每筆結果的第一欄位值(型態 T)納入 list 中. 
	 * @param type 與欲取值之欄位的 SQL 型態對應的 Java 型態. 可接受的型態: boolean/Boolean, String, 
	 * 		byte/Byte, byte[], short/Short, int/Integer, long/Long, float/Float, double/Double, Number, BigDecimal, 
	 * 		java.util.Date, java.sql.Timestamp, java.sql.Date, java.sql.Time, 
	 * 		Blob, Clob
	 * @param values 與 JDBC 所定義對 SQL 中的佔位符號 "?" 對應的值.
	 */
	public static <T> List<T> queryForList(final Connection conn, final String sql, final Class<T> type, final Object ... values) {
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			return queryForList(pstmt, type, values);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/**
	 * 對 PreparedStatement 的 SQL 參數設值, 然後執行查詢並預期得到剛好一筆, 再把該筆 ResultSet 設值至 Java Bean(型態 T)中而傳回.
	 * 如果查無值者, 傳回 null; 如果有多筆者, 只傳回第一筆.<br>
	 * ResultSet 欄位值與 Bean 屬性如何對應, 由自定義的 DBRowMapper 實作物件決定.
	 * @param values 與 JDBC 所定義對 SQL 中的佔位符號 "?" 對應的值
	 * @param rowMapper 決定唯一一筆查詢結果如何以 JavaBean 物件裝載. 常用的實作: {@link com.test.commons.spring.BeanRowMapper}
	 */
	public static <T> T queryForObject(final PreparedStatement pstmt, final Object[] values, final DBRowMapper<T> rowMapper) {
		return queryForObject((boolean[])null, pstmt, values, rowMapper);
	}
	
	/**
	 * 對 PreparedStatement 的 SQL 參數設值, 然後執行查詢並預期得到剛好一筆, 再把該筆 ResultSet 設值至 Java Bean(型態 T)中而傳回.
	 * 如果查無值者, 傳回 null; 如果有多筆者, 只傳回第一筆.<br>
	 * ResultSet 欄位值與 Bean 屬性如何對應, 由自定義的 DBRowMapper 實作物件決定.
	 * @param rowMapper 決定唯一一筆查詢結果如何以 JavaBean 物件裝載. 常用的實作: {@link com.test.commons.spring.BeanRowMapper}
	 * @param values 與 JDBC 所定義對 SQL 中的佔位符號 "?" 對應的值
	 */
	public static <T> T queryForObject(final PreparedStatement pstmt, final DBRowMapper<T> rowMapper, final Object ... values) {
		return queryForObject((boolean[])null, pstmt, (values == null) ? new Object[0] : values, rowMapper);
	}
	
	/**
	 * 對 PreparedStatement 的 SQL 參數設值, 然後執行查詢並預期得到剛好一筆且只有一欄位, 再把該筆 ResultSet 第一欄位值設至對應型態的物件中, 再傳回該物件.<br>
	 * 如果查無值者, 傳回 null(注意查到的欄位值如果為 null, 也是傳回 null); 如果有多筆者, 只傳回第一筆; 如果有多欄位者, 只取第一欄位值.
	 * @param type 與欲取值之欄位的 SQL 型態對應的 Java 型態. 可接受的型態: boolean/Boolean, String,
	 * 		byte/Byte, byte[], short/Short, int/Integer, long/Long, float/Float, double/Double, Number, BigDecimal,
	 * 		java.util.Date, java.sql.Timestamp, java.sql.Date, java.sql.Time,
	 * 		Blob, Clob
	 * @param values 與 JDBC 所定義對 SQL 中的佔位符號 "?" 對應的值.
	 */
	public static <T> T queryForObject(final PreparedStatement pstmt, final Class<T> type, final Object ... values) {
		return queryForObject((boolean[])null, pstmt, type, values);
	}
	
	/**
	 * 對 SQL 中的佔位符號設值, 然後執行查詢並預期得到剛好一筆, 再把該筆 ResultSet 設值至 Java Bean(型態 T)中而傳回.
	 * 如果查無值者, 傳回 null; 如果有多筆者, 只傳回第一筆.<br> 
	 * ResultSet 欄位值與 Bean 屬性如何對應, 由自定義的 DBRowMapper 實作物件決定.
	 * @param values 與 JDBC 所定義對 SQL 中的佔位符號 "?" 對應的值
	 * @param rowMapper 決定唯一一筆查詢結果如何以 JavaBean 物件裝載. 常用的實作: {@link com.test.commons.spring.BeanRowMapper}
	 */
	public static <T> T queryForObject(final Connection conn, final String sql, final Object[] values, final DBRowMapper<T> rowMapper) {
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			return queryForObject((boolean[])null, pstmt, values, rowMapper);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/**
	 * 對 SQL 中的佔位符號設值, 然後執行查詢並預期得到剛好一筆, 再把該筆 ResultSet 設值至 Java Bean(型態 T)中而傳回.
	 * 如果查無值者, 傳回 null; 如果有多筆者, 只傳回第一筆.<br> 
	 * ResultSet 欄位值與 Bean 屬性如何對應, 由自定義的 DBRowMapper 實作物件決定.
	 * @param rowMapper 決定唯一一筆查詢結果如何以 JavaBean 物件裝載. 常用的實作: {@link com.test.commons.spring.BeanRowMapper}
	 * @param values 與 JDBC 所定義對 SQL 中的佔位符號 "?" 對應的值.
	 */
	public static <T> T queryForObject(final Connection conn, final String sql, final DBRowMapper<T> rowMapper, final Object ... values) {
		return queryForObject(conn, sql, (values == null) ? new Object[0] : values, rowMapper);
	}
	
	/**
	 * 對 SQL 中的佔位符號設值, 然後執行查詢並預期得到剛好一筆且只有一欄位, 再把該筆 ResultSet 第一欄位值設至對應型態的物件中, 再傳回該物件.<br>
	 * 如果查無值者, 傳回 null(注意查到的欄位值如果為 null, 也是傳回 null); 如果有多筆者, 只傳回第一筆; 如果有多欄位者, 只取第一欄位值.
	 * @param type 與欲取值之欄位的 SQL 型態對應的 Java 型態. 可接受的型態: boolean/Boolean, String, 
	 * 		byte/Byte, byte[], short/Short, int/Integer, long/Long, float/Float, double/Double, Number, BigDecimal, 
	 * 		java.util.Date, java.sql.Timestamp, java.sql.Date, java.sql.Time, 
	 * 		Blob, Clob
	 * @param values 與 JDBC 所定義對 SQL 中的佔位符號 "?" 對應的值.
	 */
	public static <T> T queryForObject(final Connection conn, final String sql, final Class<T> type, final Object ... values) {
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			return queryForObject((boolean[])null, pstmt, type, values);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/**
	 * 對 PreparedStatement 的 SQL 參數設值, 然後執行查詢並預期得到剛好一筆, 再把該筆 ResultSet 設值至 Java Bean(型態 T)中而傳回.
	 * 如果查無值者, 拋出 {@link NonResultException}; 如果有多筆者, 只傳回第一筆.<br>
	 * ResultSet 欄位值與 Bean 屬性如何對應, 由自定義的 DBRowMapper 實作物件決定.
	 * @param values 與 JDBC 所定義對 SQL 中的佔位符號 "?" 對應的值
	 * @param rowMapper 決定唯一一筆查詢結果如何以 JavaBean 物件裝載. 常用的實作: {@link com.test.commons.spring.BeanRowMapper}
	 * @throws com.test.commons.exception.NonResultException 當查詢結果筆數為 0 時拋出
	 */
	public static <T> T queryForExactlyOneObject(final PreparedStatement pstmt, final Object[] values, final DBRowMapper<T> rowMapper) {
		final boolean[] hasRecord = { false };
		final T ret = queryForObject(hasRecord, pstmt, values, rowMapper);
		if(!hasRecord[0])
			throw new NonResultException("now more rows in the ResultSet object");
		return ret;
	}
	
	/**
	 * 對 PreparedStatement 的 SQL 參數設值, 然後執行查詢並預期得到剛好一筆, 再把該筆 ResultSet 設值至 Java Bean(型態 T)中而傳回.
	 * 如果查無值者, 拋出 {@link NonResultException}; 如果有多筆者, 只傳回第一筆.<br>
	 * ResultSet 欄位值與 Bean 屬性如何對應, 由自定義的 DBRowMapper 實作物件決定.
	 * @param rowMapper 決定唯一一筆查詢結果如何以 JavaBean 物件裝載. 常用的實作: {@link com.test.commons.spring.BeanRowMapper}
	 * @param values 與 JDBC 所定義對 SQL 中的佔位符號 "?" 對應的值
	 * @throws com.test.commons.exception.NonResultException 當查詢結果筆數為 0 時拋出
	 */
	public static <T> T queryForExactlyOneObject(final PreparedStatement pstmt, final DBRowMapper<T> rowMapper, final Object ... values) {
		return queryForExactlyOneObject(pstmt, (values == null) ? new Object[0] : values, rowMapper);
	}
	
	/**
	 * 對 PreparedStatement 的 SQL 參數設值, 然後執行查詢並預期得到剛好一筆且只有一欄位, 再把該筆 ResultSet 第一欄位值設至對應型態的物件中, 再傳回該物件.<br>
	 * 如果查無值者, 拋出 {@link NonResultException}; 如果有多筆者, 只傳回第一筆; 如果有多欄位者, 只取第一欄位值.
	 * @param type 與欲取值之欄位的 SQL 型態對應的 Java 型態. 可接受的型態: boolean/Boolean, String, 
	 * 		byte/Byte, byte[], short/Short, int/Integer, long/Long, float/Float, double/Double, Number, BigDecimal, 
	 * 		java.util.Date, java.sql.Timestamp, java.sql.Date, java.sql.Time, 
	 * 		Blob, Clob
	 * @param values 與 JDBC 所定義對 SQL 中的佔位符號 "?" 對應的值.
	 * @throws com.test.commons.exception.NonResultException 當查詢結果筆數為 0 時拋出
	 */
	public static <T> T queryForExactlyOneObject(final PreparedStatement pstmt, final Class<T> type, final Object ... values) {
		final boolean[] hasRecord = { false };
		final T ret = queryForObject(hasRecord, pstmt, type, values);
		if(!hasRecord[0])
			throw new NonResultException("now more rows in the ResultSet object");
		return ret;
	}
	
	/**
	 * 對 SQL 中的佔位符號設值, 然後執行查詢並預期得到剛好一筆, 再把該筆 ResultSet 設值至 Java Bean(型態 T)中而傳回.
	 * 如果查無值者, 拋出 {@link NonResultException}; 如果有多筆者, 只傳回第一筆.<br> 
	 * ResultSet 欄位值與 Bean 屬性如何對應, 由自定義的 DBRowMapper 實作物件決定.
	 * @param values 與 JDBC 所定義對 SQL 中的佔位符號 "?" 對應的值
	 * @param rowMapper 決定唯一一筆查詢結果如何以 JavaBean 物件裝載. 常用的實作: {@link com.test.commons.spring.BeanRowMapper}
	 * @throws com.test.commons.exception.NonResultException 當查詢結果筆數為 0 時拋出
	 */
	public static <T> T queryForExactlyOneObject(final Connection conn, final String sql, final Object[] values, final DBRowMapper<T> rowMapper) {
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			return queryForExactlyOneObject(pstmt, values, rowMapper);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/**
	 * 對 SQL 中的佔位符號設值, 然後執行查詢並預期得到剛好一筆, 再把該筆 ResultSet 設值至 Java Bean(型態 T)中而傳回.
	 * 如果查無值者, 拋出 {@link NonResultException}; 如果有多筆者, 只傳回第一筆.<br> 
	 * ResultSet 欄位值與 Bean 屬性如何對應, 由自定義的 DBRowMapper 實作物件決定.
	 * @param rowMapper 決定唯一一筆查詢結果如何以 JavaBean 物件裝載. 常用的實作: {@link com.test.commons.spring.BeanRowMapper}
	 * @param values 與 JDBC 所定義對 SQL 中的佔位符號 "?" 對應的值
	 * @throws com.test.commons.exception.NonResultException 當查詢結果筆數為 0 時拋出
	 */
	public static <T> T queryForExactlyOneObject(final Connection conn, final String sql, final DBRowMapper<T> rowMapper, final Object ... values) {
		return queryForExactlyOneObject(conn, sql, (values == null) ? new Object[0] : values, rowMapper);
	}
	
	/**
	 * 對 SQL 中的佔位符號設值, 然後執行查詢並預期得到剛好一筆且只有一欄位, 再把該筆 ResultSet 第一欄位值設至對應型態的物件中, 再傳回該物件.<br>
	 * 如果查無值者, 拋出 {@link NonResultException}; 如果有多筆者, 只傳回第一筆; 如果有多欄位者, 只取第一欄位值.
	 * @param type 與欲取值之欄位的 SQL 型態對應的 Java 型態. 可接受的型態: boolean/Boolean, String, 
	 * 		byte/Byte, byte[], short/Short, int/Integer, long/Long, float/Float, double/Double, Number, BigDecimal, 
	 * 		java.util.Date, java.sql.Timestamp, java.sql.Date, java.sql.Time, 
	 * 		Blob, Clob
	 * @param values 與 JDBC 所定義對 SQL 中的佔位符號 "?" 對應的值
	 * @throws com.test.commons.exception.NonResultException 當查詢結果筆數為 0 時拋出
	 */
	public static <T> T queryForExactlyOneObject(final Connection conn, final String sql, final Class<T> type, final Object ... values) {
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			return queryForExactlyOneObject(pstmt, type, values);
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/**
	 * 對 PreparedStatement 的 SQL 參數設值, 然後執行 update SQL, 傳回 update 的筆數.
	 * @param values values 與 JDBC 所定義對 SQL 中的佔位符號 "?" 對應的值.
	 */
	public static int update(final PreparedStatement pstmt, final Object ... values) {
		try {
			set(pstmt, values);
			return pstmt.executeUpdate();
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/**
	 * 對 SQL 中的佔位符號設值, 然後執行 update SQL, 傳回 update 的筆數.
	 * @param values 與 JDBC 所定義對 SQL 中的佔位符號 "?" 對應的值.
	 */
	public static int update(final Connection conn, final String sql, final Object ... values) {
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			set(pstmt, values);
			return pstmt.executeUpdate();
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	//ref: org.springframework.jdbc.core.StatementCreatorUtils
	//private static int javaTypeToSqlParameterType(Class<?> javaType) {
	//	if(javaType == null)
	//		return TYPE_UNKNOWN;
	//	
	//	final Integer sqlType = _javaTypeToSqlTypeMap.get(javaType);
	//	if(sqlType != null) {
	//		return sqlType;
	//	}
	//	if(Number.class.isAssignableFrom(javaType)) {
	//		return Types.NUMERIC;
	//	}
	//	if(isStringValue(javaType)) {
	//		return Types.VARCHAR;
	//	}
	//	if(isDateValue(javaType) || Calendar.class.isAssignableFrom(javaType)) {
	//		return Types.TIMESTAMP;
	//	}
	//	return TYPE_UNKNOWN;
	//}
	
	private static <T> T queryForObject(final boolean[] outHasRecord, final PreparedStatement pstmt, final Object[] values, final DBRowMapper<T> rowMapper) {
		set(pstmt, values);

		try (ResultSet rs = pstmt.executeQuery()) {
			if(rs.next()) {
				if(outHasRecord != null && outHasRecord.length > 0)
					outHasRecord[0] = true;
				return rowMapper.mapRow(rs, 1);
			}
			
			if(outHasRecord != null && outHasRecord.length > 0)
				outHasRecord[0] = false;
			return null;
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	/**
	 * 對 PreparedStatement 物件設值, 並執行查詢, 取第一筆, 第一個欄位之值
	 * @param outHasRecord 如果呼叫者有傳入此參數且長度不為 0, 則本 method 將藉此回應查詢結果 ResultSet 是否有值
	 * @param pstmt
	 * @param type 與欲取值之欄位的 SQL 型態對應的 Java 型態
	 * @param values 與 PreparedStatement 之 SQL 中的佔位符號對應的值
	 * @return 查詢結果之第一筆, 第一個欄位之值
	 */
	private static <T> T queryForObject(final boolean[] outHasRecord, final PreparedStatement pstmt, final Class<T> type,
			final Object ... values) {
		set(pstmt, values);

		try (ResultSet rs = pstmt.executeQuery()) {
			if(rs.next()) {
				if(outHasRecord != null && outHasRecord.length > 0)
					outHasRecord[0] = true;
				return retrieveFirstColumnValue(rs, type);
			}

			if(outHasRecord != null && outHasRecord.length > 0)
				outHasRecord[0] = false;
			return null;
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}

	/**
	 * 取 查詢結果 ResultSet 物件中, 當前 cursor 所在該筆之第一欄位的值
	 * @param rs
	 * @param type 與欲取值之欄位的 SQL 型態對應的 Java 型態
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static <T> T retrieveFirstColumnValue(final ResultSet rs, final Class<T> type) throws SQLException {
		//ref: BeanRowMapper.ColumnDescriptor
		if(boolean.class.equals(type)) {
			return (T)(rs.getBoolean(1) ? Boolean.TRUE : Boolean.FALSE);
		}
		if(Boolean.class.equals(type)) {
			Boolean ret = rs.getBoolean(1) ? Boolean.TRUE : Boolean.FALSE;
			return rs.wasNull() ? (T)null : (T)ret;
		}
		if(String.class.equals(type)) {
			return (T)rs.getString(1);
		}
		if(byte.class.equals(type) || Byte.class.equals(type)) {
			Byte ret = new Byte(rs.getByte(1));
			return rs.wasNull() ? (T)null : (T)ret;
		}
		if(byte[].class.equals(type)) {
			return (T)rs.getBytes(1);
		}
		if(short.class.equals(type) || Short.class.equals(type)) {
			Short ret = new Short(rs.getShort(1));
			return rs.wasNull() ? (T)null : (T)ret;
		}
		if(int.class.equals(type) || Integer.class.equals(type)) {
			Integer ret = new Integer(rs.getInt(1));
			return rs.wasNull() ? (T)null : (T)ret;
		}
		if(long.class.equals(type) || Long.class.equals(type)) {
			Long ret = new Long(rs.getLong(1));
			return rs.wasNull() ? (T)null : (T)ret;
		}
		if(float.class.equals(type) || Float.class.equals(type)) {
			Float ret = new Float(rs.getFloat(1));
			return rs.wasNull() ? (T)null : (T)ret;
		}
		if(double.class.equals(type) || Double.class.equals(type) || Number.class.equals(type)) {
			Double ret = new Double(rs.getDouble(1));
			return rs.wasNull() ? (T)null : (T)ret;
		}
		if(BigDecimal.class.equals(type)) {
			return (T)rs.getBigDecimal(1);
		}
		if(java.util.Date.class.equals(type) || java.sql.Timestamp.class.equals(type)) {
			return (T)rs.getTimestamp(1);
		}
		if(java.sql.Date.class.equals(type)) {
			return (T)rs.getDate(1);
		}
		if(java.sql.Time.class.equals(type)) {
			return (T)rs.getTime(1);
		}
		if(Blob.class.equals(type)) {
			return (T)rs.getBlob(1);
		}
		if(Clob.class.equals(type)) {
			return (T)rs.getClob(1);
		}
		return (T)rs.getObject(1); //最後只能依賴 JDBC driver 自己的 getObject()
	}
	
	/**
	 * 根據 SQL 參數及其值之型態, 自動判斷對 PreparedStatement 物件設參數值的方式.
	 * @param pstmt
	 * @param paramIndex SQL 中以 '?' 佔位符的參數 index (自 1 起算)
	 * @param sqlType java.sql.Types 之成員值 (SQL type)
	 * @param value 與指定的佔位符參數對應的值
	 * @see java.sql.Types
	 */
	private static void set(final PreparedStatement pstmt, final int paramIndex, final int sqlType, final Object value) {
		try {
			if(value == null) {
				setNull(pstmt, paramIndex, sqlType);
				return;
			}
			
			final Class<? extends Object> type = value.getClass();
			if(sqlType != TYPE_UNKNOWN && (sqlType != Types.OTHER || !"Oracle".equals(pstmt.getConnection().getMetaData().getDatabaseProductName()))) { //有明確指定 table 欄位 SQL type 者, 優先以之為基準
				//ref: org.springframework.jdbc.core.StatementCreatorUtils.setValue(PreparedStatement ps, int paramIndex, int sqlType, String typeName, Integer scale, Object inValue)
				
				if(sqlType == Types.VARCHAR || sqlType == Types.LONGVARCHAR ) {
					pstmt.setString(paramIndex, value.toString());
				} else if(sqlType == Types.NVARCHAR || sqlType == Types.LONGNVARCHAR) {
					pstmt.setNString(paramIndex, value.toString());
				} else if((sqlType == Types.CLOB || sqlType == Types.NCLOB) && isStringValue(type)) {
					final String strVal = value.toString();
					if(Reader.class.isAssignableFrom(type)) { //原 org.springframework.jdbc.core.StatementCreatorUtils.setValue() 所無
						if(sqlType == Types.NCLOB) {
							pstmt.setNCharacterStream(paramIndex, (Reader)value); //Java 1.6+, 呼叫者得自行 close Reader
						} else {
							pstmt.setCharacterStream(paramIndex, (Reader)value); //Java 1.6+, 呼叫者得自行 close Reader
						}
					} else {
						if(strVal.length() > 4000) {
							//Necessary for older Oracle drivers, in particular when running against an Oracle 10 database.
							//Should also work fine against other drivers/databases since it uses standard JDBC 4.0 API.
							if(sqlType == Types.NCLOB) {
								pstmt.setNClob(paramIndex, new StringReader(strVal), strVal.length());
							} else {
								pstmt.setClob(paramIndex, new StringReader(strVal), strVal.length());
							}
						} else {
							//Fallback: setString or setNString binding
							if(sqlType == Types.NCLOB) {
								pstmt.setNString(paramIndex, strVal);
							} else {
								pstmt.setString(paramIndex, strVal);
							}
						}
					}
				} else if(sqlType == Types.BLOB) {  //原 org.springframework.jdbc.core.StatementCreatorUtils.setValue() 未處理, 直接就歸 fall back 處置
					if(InputStream.class.isAssignableFrom(type)) {
						pstmt.setBinaryStream(paramIndex, (InputStream)value); //Java 1.6+, 呼叫者得自行 close InputStream
					} else { //Fall back
						pstmt.setObject(paramIndex, value, sqlType);
					}
				} else if(sqlType == Types.DECIMAL || sqlType == Types.NUMERIC) {
					if(BigDecimal.class.isAssignableFrom(type)) {
						pstmt.setBigDecimal(paramIndex, (BigDecimal)value);
					//} else if(scale != null) { //TODO: 暫未能顧及 scale
					//	pstmt.setObject(paramIndex, value, sqlType, scale);
					} else {
						pstmt.setObject(paramIndex, value, sqlType);
					}
				} else if(sqlType == Types.BOOLEAN) {
					if(Boolean.class.isAssignableFrom(type)) {
						pstmt.setBoolean(paramIndex, (Boolean)value);
					} else {
						pstmt.setObject(paramIndex, value, Types.BOOLEAN);
					}
				} else if(sqlType == Types.DATE) {
					if(java.util.Date.class.isAssignableFrom(type)) {
						if(java.sql.Date.class.isAssignableFrom(type)) {
							pstmt.setDate(paramIndex, (java.sql.Date)value);
						} else {
							pstmt.setDate(paramIndex, new java.sql.Date(((java.util.Date)value).getTime()));
						}
					} else if(Calendar.class.isAssignableFrom(type)) {
						final Calendar cal = (Calendar)value;
						pstmt.setDate(paramIndex, new java.sql.Date(cal.getTime().getTime()), cal);
					} else {
						pstmt.setObject(paramIndex, value, Types.DATE);
					}
				} else if(sqlType == Types.TIME) {
					if(java.util.Date.class.isAssignableFrom(type)) {
						if(java.sql.Time.class.isAssignableFrom(type)) {
							pstmt.setTime(paramIndex, (java.sql.Time)value);
						} else {
							pstmt.setTime(paramIndex, new java.sql.Time(((java.util.Date)value).getTime()));
						}
					} else if(Calendar.class.isAssignableFrom(type)) {
						final Calendar cal = (Calendar)value;
						pstmt.setTime(paramIndex, new java.sql.Time(cal.getTime().getTime()), cal);
					} else {
						pstmt.setObject(paramIndex, value, Types.TIME);
					}
				} else if(sqlType == Types.TIMESTAMP) {
					if(java.util.Date.class.isAssignableFrom(type)) {
						if(java.sql.Timestamp.class.isAssignableFrom(type)) {
							pstmt.setTimestamp(paramIndex, (java.sql.Timestamp)value);
						} else {
							pstmt.setTimestamp(paramIndex, new java.sql.Timestamp(((java.util.Date)value).getTime()));
						}
					} else if(Calendar.class.isAssignableFrom(type)) {
						final Calendar cal = (Calendar)value;
						pstmt.setTimestamp(paramIndex, new java.sql.Timestamp(cal.getTime().getTime()), cal);
					} else {
						pstmt.setObject(paramIndex, value, Types.TIMESTAMP);
					}
				//} else if(sqlType == TYPE_UNKNOWN || (sqlType == Types.OTHER && "Oracle".equals(pstmt.getConnection().getMetaData().getDatabaseProductName()))) { //歸以下 未指定 sqlType 的情況中處理
				} else { //fall back to generic setObject call with SQL type specified.
					pstmt.setObject(paramIndex, value, sqlType);
				}
			} else { //全憑設值 value 的型態推測 table 欄位型態
				if(Boolean.class.equals(type) || boolean.class.equals(type)) { //final class
					pstmt.setBoolean(paramIndex, (Boolean)value);
				} else if(Byte.class.equals(type) || byte.class.equals(type)) { //final class
					pstmt.setByte(paramIndex, (Byte)value);
				} else if(byte[].class.isAssignableFrom(type)) {
					pstmt.setBytes(paramIndex, (byte[])value);
				} else if(Short.class.equals(type) || short.class.equals(type)) { //final class
					pstmt.setShort(paramIndex, (Short)value);
				} else if(Integer.class.equals(type) || int.class.equals(type)) { //final class
					pstmt.setInt(paramIndex, (Integer)value);
				} else if(Long.class.equals(type) || long.class.equals(type)) { //final class
					pstmt.setLong(paramIndex, (Long)value);
				} else if(BigInteger.class.isAssignableFrom(type)) {
					pstmt.setLong(paramIndex, ((BigInteger)value).longValueExact()); //JDK 1.8+, 不讓值默默超過 long 的範圍
				} else if(Float.class.equals(type) || float.class.equals(type)) { //final class
					pstmt.setFloat(paramIndex, (Float)value);
				} else if(Double.class.equals(type) || double.class.equals(type)) { //final class
					pstmt.setDouble(paramIndex, (Double)value);
				} else if(BigDecimal.class.isAssignableFrom(type)) {
					pstmt.setBigDecimal(paramIndex, (BigDecimal)value);
				} else if(Number.class.isAssignableFrom(type)) { //NOTE: 要排在以上眾 Number 衍生類型 之後
					pstmt.setObject(paramIndex, value, Types.NUMERIC);
				} else if(isStringValue(type)) { //字串類型, 含 StringBuffer, StringBuilder, StringWriter
					pstmt.setString(paramIndex, value.toString());
				} else if(java.sql.Date.class.isAssignableFrom(type)) {
					pstmt.setDate(paramIndex, (java.sql.Date)value);
				} else if(java.sql.Time.class.isAssignableFrom(type)) {
					pstmt.setTime(paramIndex, (java.sql.Time)value);
				} else if(java.sql.Timestamp.class.isAssignableFrom(type)) {
					pstmt.setTimestamp(paramIndex, (java.sql.Timestamp)value);
				} else if(java.util.Date.class.isAssignableFrom(type)) { //NOTE: 要排在以上眾 java.util.Date 衍生類型之後
					pstmt.setTimestamp(paramIndex, new java.sql.Timestamp(((java.util.Date)value).getTime()));
				} else if(Calendar.class.isAssignableFrom(type)) {
					final Calendar cal = (Calendar)value;
					pstmt.setTimestamp(paramIndex, new java.sql.Timestamp(cal.getTime().getTime()), cal);
				} else if(Blob.class.isAssignableFrom(type)) {
					pstmt.setBlob(paramIndex, (Blob)value);
				} else if(NClob.class.isAssignableFrom(type)) {
					pstmt.setNClob(paramIndex, (NClob)value);
				} else if(Clob.class.isAssignableFrom(type)) { //NOTE: 要排在衍生的 NClob 之後
					pstmt.setClob(paramIndex, (Clob)value);
				} else if(InputStream.class.isAssignableFrom(type)) { //原 org.springframework.jdbc.core.StatementCreatorUtils.setValue() 所未顧及的型態
					pstmt.setBinaryStream(paramIndex, (InputStream)value); //Java 1.6+, 呼叫者得自行 close InputStream
				} else if(Reader.class.isAssignableFrom(type)) { //原 org.springframework.jdbc.core.StatementCreatorUtils.setValue() 所未顧及的型態
					pstmt.setCharacterStream(paramIndex, (Reader)value); //Java 1.6+, 呼叫者得自行 close Reader
				} else if(Character.class.equals(type) || char.class.equals(type)) { //一律視為 SQL VARCHAR
					pstmt.setString(paramIndex, ((Character)value).toString());
				} else if(char[].class.isAssignableFrom(type)) { //一律視為 SQL VARCHAR
					pstmt.setString(paramIndex, String.valueOf((char[])value));
				} else { //Fall back to generic setObject call without SQL type specified. (ref: org.springframework.jdbc.core.StatementCreatorUtils.setValue())
					pstmt.setObject(paramIndex, value);
				}
			}
		} catch(SQLException se) {
			throw new RuntimeException(se.getMessage(), se);
		}
	}
	
	//ref org.springframework.jdbc.core.StatementCreatorUtils.setNull(PreparedStatement ps, int paramIndex, int sqlType, @Nullable String typeName)
	private static void setNull(final PreparedStatement pstmt, final int paramIndex, final int sqlType) throws SQLException {
		if(sqlType == TYPE_UNKNOWN || sqlType == Types.OTHER) { //未明確指定 table 欄位型態者, 由 PreparedStatement 的 meta-data 推得
			boolean useSetObject = false;
			Integer sqlTypeToUse = null;
			
			if(isCallPreparedStatementGetParameterMetaData()) {
				try {
					sqlTypeToUse = pstmt.getParameterMetaData().getParameterType(paramIndex);
				} catch(Throwable t) {
					//JDBC 3.0 getParameterType call not supported - using fallback method instead
					//SQL Server 的 JDBC driver 往往 pstmt.getParameterMetaData() 出錯, 當 SQL where 含括弧時(即使是號稱符合 JDBC 4 規格的 driver)
					setCallPreparedStatementGetParameterMetaData(false);
				}
			}
			
			if(sqlTypeToUse == null) { //fallback
				//Proceed with database-specific checks
				sqlTypeToUse = Types.NULL;
				final DatabaseMetaData dbmd = pstmt.getConnection().getMetaData();
				final String jdbcDriverName = dbmd.getDriverName();
				final String databaseProductName = dbmd.getDatabaseProductName();
				if(databaseProductName.startsWith("Informix") ||
						(jdbcDriverName.startsWith("Microsoft") && jdbcDriverName.contains("SQL Server"))) {
						//"Microsoft SQL Server JDBC Driver 3.0" versus "Microsoft JDBC Driver 4.0 for SQL Server"
					useSetObject = true;
				} else if(databaseProductName.startsWith("DB2") ||
						jdbcDriverName.startsWith("jConnect") ||
						jdbcDriverName.startsWith("SQLServer")||
						jdbcDriverName.startsWith("Apache Derby")) {
					sqlTypeToUse = Types.VARCHAR;
				}
			}
			
			if(useSetObject) {
				pstmt.setObject(paramIndex, null);
			} else {
				pstmt.setNull(paramIndex, sqlTypeToUse);
			}
		} else {
			pstmt.setNull(paramIndex, sqlType);
		}
	}

	//ref org.springframework.jdbc.core.StatementCreatorUtils
	private static boolean isStringValue(Class<?> valueType) {
		//Consider any CharSequence (including StringBuffer and StringBuilder) as a String.                                                    
        return (CharSequence.class.isAssignableFrom(valueType) || StringWriter.class.isAssignableFrom(valueType));
	}
	
	//ref org.springframework.jdbc.core.StatementCreatorUtils
	//check whether the given value is a java.util.Date (but not one of the JDBC-specific subclasses).
	//private static boolean isDateValue(Class<?> valueType) {
	//	return (java.util.Date.class.isAssignableFrom(valueType) &&
	//			!(java.sql.Date.class.isAssignableFrom(valueType) ||
	//					java.sql.Time.class.isAssignableFrom(valueType) ||
	//					java.sql.Timestamp.class.isAssignableFrom(valueType)));
	//}

	private static boolean isCallPreparedStatementGetParameterMetaData() {
		final Boolean b = _callPreparedStatementGetParameterMetaData.get();
		return (b == null) ? true : b;
	}
	
	//讓連續 setNull() 動作之第 2 次後的速度快一些
	private static void setCallPreparedStatementGetParameterMetaData(final boolean value) {
		_callPreparedStatementGetParameterMetaData.set(value);
	}
}
