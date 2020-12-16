package com.test.commons.util;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.util.*;
import org.apache.commons.beanutils.PropertyUtils;

/**
 * 協助組合不定條件的 SQL 語句的工具.<br>
 * 使用者需在程式中靜態引入, 如:<pre><code>
 * <b>import static</b> com.tatung.commons.util.SqlAssembler.*;
 * <b>import</b> com.tatung.commons.util.SqlAssembler;
 * </code></pre>
 * 
 * 使用例:<br>
 * 依據取值對象物件是否含物件 args 之屬性/key 而產生如下可能的 SQL:<pre><code>
 * select z.a, z.b, z.c from spam z where z.a=? and z.b=? and (z.c &gt; ? or z.d &lt; ?) and z.e is not null order by z.a
 * select z.a, z.b, z.c from spam z where z.a=? and (z.c &gt; ? or z.d &lt; ?) and z.e !=? order by z.a
 * select z.a, z.b, z.c from spam z where z.a=? and z.b=? and z.d &lt; ? and z.e  !=? order by z.a
 * select z.a, z.b, z.c from spam z where z.a=? and z.b=? and z.c &gt; ? and z.e  !=? order by z.a
 * select z.a, z.b, z.c from spam z where z.a=? and z.b=? and (z.c &gt; ? or z.d &lt; ?) order by z.a
 * select z.a, z.b, z.c from spam z where z.b=? and (z.c &gt; ? or z.d &lt; ?) and z.e  !=? order by z.a
 * ...(各種條件的組合)
 * </code></pre>
 * 
 * Java code: <pre style="border: 1px solid gray"><code>
 * SqlAssembler sql = new <b>SqlAssembler</b>("select z.a, z.b, z.c from spam z").<b>where(</b>
 *         eq("z.a", args.get("col1")) //此例中假設 args 為一 Map 物件
 *         .and(eq("z.b", args.get("col2")))
 *         .and(gt("z.c", args.get("col3")).or(lt("z.d", args.get("col4"))))
 *         .and(notEq("z.e", args.get("col5")))
 * <b>)</b>.raw("order by z.a");
 * //當 args 有內含被指定的「屬性名稱/key」時, 才會出現該條件.
 * //當「屬性值」為 null 時, eq("xxx", ...) 原本預定要生成 "<b>xxx=?</b>" 條件, 將會變成 "<b>xxx is null</b>" 條件.
 * log.debug("sql=" + sql); //印出 debug 訊息
 * String s = sql.<b>getSql</b>(); //取得 sql 句
 * Object[] values = sql.<b>getValues</b>(); //取得與 "?" 佔位符號對應的值
 * </code></pre>
 */
public final class SqlAssembler {
    private List<String> expressions; //放置含 JDBC PreparedStatemnt 占位符號的 SQL 字串
    private List<Object> values1; //與佔位符號對應的值(用於放置設值於 SqlAssembler 建構式內 SQL 敘述的 value) 
    private List<Object> values2; //與佔位符號對應的值(主要用於放置如 where 條件, set 設值等狀況的值. 當以 SqlAssempler 組合 sub-query 時, 會將內層 query 的 value 統統歸入 values2)
    private boolean assembleSetting; //正在組合 update SQL 中的 set xxx=? 句中
    private String cache; //暫存已產生的 SQL 句
    
    public SqlAssembler() {
        this.expressions = new LinkedList<String>(); //不會頻繁循讀取, 但可能常有插入的情形
        this.values2 = new LinkedList<Object>();
    }
    
    /**
     * 含 SQL 敘述片斷的建構式.
     * @param statement SQL 句之片斷.
     */
    public SqlAssembler(final String statement) {
        this();
        if(statement != null && statement.length() != 0)
        	this.expressions.add(statement);
    }
    
    /**
     * 含 SQL 敘述片斷的建構式, 且 SQL 片斷中含 JDBC 設值的佔位符號.
     * <br>
     * 例:
     * <pre style="border: 1px solid gray"><code>
     * SqlAssembler sql = new SqlAssembler("select aaa, ? from foobar", bbb)
     *     .where(...);
     * </code></pre>
     * @param statement SQL 句之片斷
     * @param values 個數要與 statement 中的佔位符號個數一樣
     */
    public SqlAssembler(final String statement, final Object ... values) {
    	this(statement);
    	if(values != null && values.length != 0)
    		this.values1 = Arrays.asList(values);
    }

    /** 插入任意字串 */
    public SqlAssembler raw(final String s) {
    	if(s != null && s.length() != 0) {
			clearCache();
			this.expressions.add(s);
		}
        return this;
    }

    /**
     * 對 SQL update 述句, 插入含占位符號的設值式.(例: "set xxx=?"). 例:<pre style="border: 1px solid gray"><code>
     * new SqlAssembler("update spam")
     *     .set("a", xxx).set("b", xxx).set("c", xxx)
     *     .where(eq("z", zzz).and(eq("d", xxx)));
     * </code></pre>
     * 可能得到:<pre style="border: 1px solid gray"><code>
     * update spam set a =?, b =?, c =? where (z is null and d=?)
     * </code></pre>
     * @param columnName  欲設值的 table 欄位名
     * @param value  與設值式之占位符號對應的值
     */
    public SqlAssembler set(final String columnName, final Object value) {
    	if(columnName != null && columnName.length() != 0) {
			clearCache();
			
	        if(this.assembleSetting) {
	            add(this.expressions, ",", columnName, "=?");
	        } else {
	            add(this.expressions, "set", columnName, "=?");
	            this.assembleSetting = true;
	        }
	        this.values2.add(value);
    	}
        return this;
    }
    
    /**
     * 對 SQL update 述句, 插入含占位符號的設值式, 但遇 null 值者則不設值.(例: "set xxx=?")
     * @see #set(String, Object)
     * @param columnName  欲設值的 table 欄位名
     * @param value  與設值式之占位符號對應的值
     */
    public SqlAssembler setNN(final String columnName, final Object value) {
    	if(value == null)
    		return this;
		clearCache();
    	return set(columnName, value);
    }
    
    /** 
     * 插入 having 條件式. 例:<pre style="border: 1px solid gray"><code>
     * new SqlAssembler("select c, sum(d) from abc").where(
     *     eq("a", xxx).and("b", xxx)
     * ).raw("group by c")
     * ).having(
     *     gt("sum(d)", xxx)
     * );
     * </code></pre>
     * 得到:<pre style="border: 1px solid gray"><code>
     * select c, sum(d) from abc where (a=? and b=?) group by c having sum(d) &gt; ?
     * </code></pre>
     * 注意: 如果 having() 在執行時沒有內含任何條件時, 則產生的 SQL 句會略去此 having 條件.
     */
    public SqlAssembler having(final Condition having) {
        if(having != null && having.getConditions().size() != 0) {
			clearCache();
			
            if(having.getConditions().size() > 1)
                add(addAll(add(this.expressions, "having ("), having.getConditions()), ")");
            else
                addAll(add(this.expressions, "having"), having.getConditions());
            this.values2.addAll(having.getValues());
        }
        return this;
    }
    
    /** 插入 where 條件. 如果執行時期此 where() 沒有內含任何條件時, 最後產生的 SQL 句中將略去 where 敘述. */
    public SqlAssembler where(final Condition conditions) {
        if(conditions != null && conditions.getConditions().size() != 0) {
			clearCache();
			
            if(conditions.getConditions().size() > 1)
                add(addAll(add(this.expressions, "where ("), conditions.getConditions()), ")");
            else
                addAll(add(this.expressions, "where"), conditions.getConditions());
            this.values2.addAll(conditions.getValues());
        }
        return this;
    }
    
    /** 
     * 插入 on 條件(用於 xxx outer join yyy on … 之類的敘述). 例:<pre style="border: 1px solid gray"><code>
     * new SqlAssembler("select a.* from foo a join")
     *     .subquery(new SqlAssembler("select col1 from bar").where(eqNN("col2", xxx)))
     *     .raw("as b")
     *     .on(
     *         condition("a.col1=b.col1")
     *         .and(eqNN("a.col3", xxx))
     *     ).where(eqNN("a.col3", xxx));
     * </code></pre>
     * 得到:<pre style="border: 1px solid gray"><code>
     * select a.* from foo a join (select col1 from bar where col2=?) as b 
     * on (a.col1=b.col1 and a.col3=?)
     * where a.col3=?
     * </code></pre>
     * 注意: 如果執行時期此 on() 沒有內含任何條件時, 最後產生的 SQL 句中將略去 on 敘述.
     */
    public SqlAssembler on(final Condition conditions) {
        if(conditions != null && conditions.getConditions().size() != 0) {
			clearCache();
			
            if(conditions.getConditions().size() > 1)
                add(addAll(add(this.expressions, "on ("), conditions.getConditions()), ")");
            else
                addAll(add(this.expressions, "on"), conditions.getConditions());
            this.values2.addAll(conditions.getValues());
        }
        return this;
    }
    
    /**
     * 插入子查詢述句. 例:<pre style="border: 1px solid gray"><code>
     * new SqlAssembler("select a.* from foo a join")
     *     .subquery(new SqlAssembler("select col1 from bar").where(eqNN("col2", xxx)))
     *     .raw("as b on a.col1=b.col1")
     *     .where(eqNN("a.col3", xxx));
     * </code></pre>
     * 得到:<pre style="border: 1px solid gray"><code>
     * select a.* from foo a join (select col1 from bar where col2=?) as b
     * on a.col1=b.col1
     * where a.col3=?
     * </code></pre>
     */
    public SqlAssembler subquery(final SqlAssembler subquery) {
        if(subquery != null && subquery.getExpressions().size() != 0) {
			clearCache();
			
            add(addAll(add(this.expressions, "("), subquery.getExpressions()), ")");
            this.values2.addAll(subquery.getValues2());
        }
        return this;
    }
    
    /** 
     * 與另一 SQL 述句聯集(SQL union, 結果不重覆).<pre style="border: 1px solid gray"><code>
     * new SqlAssembler("select a from spam").where(
     *     eq("a", xxx).and(gt("b", xxx))
     * ).union(
     *     new SqlAssembler("select a from eggs").where(
     *         eq("a", args.get("a")).and(gt("bb", args.get("b")).or(lt("cc", args.get("c"))))
     *     )
     * ).union(
     *     new SqlAssembler("select a from bar")
     * );
     * </code></pre>
     * 得到如下 SQL:<pre style="border: 1px solid gray"><code>
     * (
     *   (
     *     select a from spam where (a=? and b &gt;?)
     *   ) union (
     *     select a from eggs where (a=? and (bb &gt;? or cc &lt;?))
     *   )
     * ) union (
     *   select a from bar
     * )
     * </code></pre>
     */
    public SqlAssembler union(final SqlAssembler sql) {
        if(sql != null && sql.getExpressions().size() != 0) {
			clearCache();
			
        	if(this.expressions.size() != 0) {
        		this.expressions.add(0, "(");
        		this.expressions.add(")");
        	}
            add(addAll(add(this.expressions, "union ("), sql.getExpressions()), ")");
            this.values2.addAll(sql.getValues2());
        }
        return this;
    }
    
    /** 
     * 與另一 SQL 述句聯集(SQL union all, 結果可能重覆, 用例類似 union() 的說明)
     * @see #union(SqlAssembler) 
     */
    public SqlAssembler unionAll(final SqlAssembler sql) {
        if(sql != null && sql.getExpressions().size() != 0) {
			clearCache();
			
        	if(this.expressions.size() != 0) {
        		this.expressions.add(0, "(");
        		this.expressions.add(")");
        	}
            add(addAll(add(this.expressions, "union all ("), sql.getExpressions()), ")");
            this.values2.addAll(sql.getValues2());
        }
        return this;
    }
    
    /** 
     * 與另一 SQL 交集(SQL intersect, 結果不重覆, 用例類似 union() 的說明)
     * @see #union(SqlAssembler)
     */
    public SqlAssembler intersect(final SqlAssembler sql) {
        if(sql != null && sql.getExpressions().size() != 0) {
			clearCache();
			
        	if(this.expressions.size() != 0) {
        		this.expressions.add(0, "(");
        		this.expressions.add(")");
        	}
            add(addAll(add(this.expressions, "intersect ("), sql.getExpressions()), ")");
            this.values2.addAll(sql.getValues2());
        }
        return this;
    }
    
    /** 
     * 與另一 SQL 差集; 前句 SQL 查詢結果, 扣除後句結果中出現的相同資料(SQL minus, 結果不重覆, 用例類似 union() 的說明)
     * @see #union(SqlAssembler)
     */
    public SqlAssembler minus(final SqlAssembler sql) {
        if(sql != null && sql.getExpressions().size() != 0) {
			clearCache();
			
        	if(this.expressions.size() != 0) {
        		this.expressions.add(0, "(");
        		this.expressions.add(")");
        	}
            add(addAll(add(this.expressions, "minus ("), sql.getExpressions()), ")");
            this.values2.addAll(sql.getValues2());
        }
        return this;
    }

    /**
     * 將另一個 SqlAssembler 物件合併進來. 例如: <pre style="border: 1px solid gray"><code>
     * SqlAssembler sql = new SqlAssembler("select * from spam");
     * SqlAssembler sql2 = new SqlAssembler()
     *     .where(eqNN("a", args1.getA()).and(eqNN("b", args1.getB())).and(eqNN("c", args1.getC())));
     * sql.combine(sql2);
     * </code></pre>
     * 得到:<pre style="border: 1px solid gray"><code>
     * select * from spam where (a=? and b=? and c=?)
     * </code></pre>
     */
    public SqlAssembler combine(final SqlAssembler sql) {
    	if(sql != null && sql.getExpressions().size() != 0) {
			clearCache();
			
    		this.expressions.addAll(sql.getExpressions());
            this.values2.addAll(sql.getValues2());
    	}
    	return this;
    }
    
    /** 取得最終含占位符號的 SQL 述句. 連續呼叫的過程中, 本 SqlAssembler 物件狀態改變者, 須先呼叫 {@link #clearCache()} 清除暫存資訊. */
    public String getSql() {
        if(this.cache != null)
            return this.cache;
        StringBuilder sb = new StringBuilder();
        String prev = null;
        for(String s : this.expressions) {
            if(!s.startsWith(")") && !s.startsWith(",") && prev != null && !prev.endsWith("(")) //右括弧前、左括弧後、逗點前 不用加空白
                sb.append(" ");
            sb.append(s);
            prev = s;
        }
        this.cache = sb.toString();
        return this.cache;
    }

    private List<String> getExpressions() {
        return this.expressions;
    }
    
    /** 取得最終與 SQL 述句中的占位符號對應的值. */
    public Object[] getValues() {
        return getValues2().toArray(new Object[values2.size()]);
    }
    
    //用於將 sub-query 的 values 收集歸於外層的 values 中
    private List<Object> getValues2() {
    	if(this.values1 == null) //values1 應該是 readonly 的
    		return this.values2;
    	final List<Object> values = new ArrayList<Object>(this.values1.size() + this.values2.size()); //為了保險, 還是別動 values2 的內容
    	values.addAll(this.values1);
    	values.addAll(this.values2);
    	return values;
    }

    /** 列印如下型式的除錯用字串: sql=…; args=[xx, yy, zz, …] */
    @Override
    public String toString() {
        if(this.getValues() != null && this.getValues().length != 0)
            return new StringBuilder().append(getSql()).append("; args=").append(StrUtil.join(", ", this.getValues())).toString();
        else
        	return getSql();
    }
    
	//清除已暫存的 SQL 資訊
    private void clearCache() {
        this.cache = null;
    }
	
    /**
     * (<b>需 static import</b>)
     * 產生 "columnName = &#063;" 條件, 並指定其值(值為  null 者, 產生為 "columnName is null").
     * @param columnName  SQL 中的欄位名
     * @param value  欄位值, 型態為 String/Integer/Long/Byte/Short/Float/Double/byte[]/Boolean/BigDecimal/Blob/Clob,
     *        或 java.util.Date/java.sql.Timestamp/java.sql.Date/java.sql.Time 資料型態
     */
    public static Condition eq(final String columnName, final Object value) {
        return new Condition(columnName, Condition.RELATION_EQUAL, true, value, (String)null);
    }

    /**
     * (<b>需 static import</b>)
     * 產生 "columnName = (子 SQL)" 條件.
     * @param columnName  SQL 中的欄位名
     * @param subquery  放置子 SQL 句的物件
     */
    public static Condition eq(final String columnName, final SqlAssembler subquery) {
        if(subquery == null)
            return eq(columnName, (Object)null);
        return new Condition(columnName, Condition.RELATION_EQUAL, subquery);
    }
    
    /**
     * (<b>需 static import</b>)
     * 產生 "columnName = &#063;" 條件, 並指定其值(值為  null 者, 略過本條件).
     * @param columnName  SQL 中的欄位名
     * @param value  欄位值, 型態為 String/Integer/Long/Byte/Short/Float/Double/byte[]/Boolean/BigDecimal/Blob/Clob,
     *        或 java.util.Date/java.sql.Timestamp/java.sql.Date/java.sql.Time 資料型態
     */
    public static Condition eqNN(final String columnName, final Object value) {
    	if(value == null)
    		return new Condition();
        return new Condition(columnName, Condition.RELATION_EQUAL, false, value, (String)null);
    }
    
    /**
     * (<b>需 static import</b>)
     * 產生 "columnName != &#063;" 條件, 並指定其值(值為  null 者, 產生為 "columnName is not null").
     * @param columnName  SQL 中的欄位名
     * @param value  欄位值, 型態為 String/Integer/Long/Byte/Short/Float/Double/byte[]/Boolean/BigDecimal/Blob/Clob,
     *        或 java.util.Date/java.sql.Timestamp/java.sql.Date/java.sql.Time 資料型態
     */
    public static Condition notEq(final String columnName, final Object value) {
        return new Condition(columnName, Condition.RELATION_NOT_EQUAL, true, value, (String)null);
    }

    /**
     * (<b>需 static import</b>)
     * 產生 "columnName != (子 SQL)" 條件.
     * @param columnName  SQL 中的欄位名
     * @param subquery  放置子 SQL 句的物件
     */
    public static Condition notEq(final String columnName, final SqlAssembler subquery) {
        if(subquery == null)
            return notEq(columnName, (Object)null);
        return new Condition(columnName, Condition.RELATION_NOT_EQUAL, subquery);
    }
    
    /**
     * (<b>需 static import</b>)
     * 產生 "columnName != &#063;" 條件, 並指定其值(值為  null 者, 略過本條件).
     * @param columnName  SQL 中的欄位名
     * @param value  欄位值, 型態為 String/Integer/Long/Byte/Short/Float/Double/byte[]/Boolean/BigDecimal/Blob/Clob,
     *        或 java.util.Date/java.sql.Timestamp/java.sql.Date/java.sql.Time 資料型態
     */
    public static Condition notEqNN(final String columnName, final Object value) {
    	if(value == null)
    		return new Condition();
        return new Condition(columnName, Condition.RELATION_NOT_EQUAL, false, value, (String)null);
    }
    
    /**
     * (<b>需 static import</b>)
     * 產生 "columnName &lt; &#063;" 條件, 並指定其值.
     * @param columnName  SQL 中的欄位名
     * @param value  欄位值, 型態為 String/Integer/Long/Byte/Short/Float/Double/byte[]/Boolean/BigDecimal/Blob/Clob,
     *        或 java.util.Date/java.sql.Timestamp/java.sql.Date/java.sql.Time 資料型態
     */
    public static Condition lt(final String columnName, final Object value) {
        return new Condition(columnName, Condition.RELATION_LESS_THAN, true, value, (String)null);
    }

    /**
     * (<b>需 static import</b>)
     * 產生 "columnName &lt; (子 SQL)" 條件.
     * @param columnName  SQL 中的欄位名
     * @param subquery  放置子 SQL 句的物件
     */
    public static Condition lt(final String columnName, final SqlAssembler subquery) {
        if(subquery == null)
            return lt(columnName, (Object)null);
        return new Condition(columnName, Condition.RELATION_LESS_THAN, subquery);
    }
    
    /**
     * (<b>需 static import</b>)
     * 產生 "columnName &lt; &#063;" 條件, 並指定其值(值為  null 者, 略過本條件).
     * @param columnName  SQL 中的欄位名
     * @param value  欄位值, 型態為 String/Integer/Long/Byte/Short/Float/Double/byte[]/Boolean/BigDecimal/Blob/Clob,
     *        或 java.util.Date/java.sql.Timestamp/java.sql.Date/java.sql.Time 資料型態
     */
    public static Condition ltNN(final String columnName, final Object value) {
    	if(value == null)
    		return new Condition();
        return new Condition(columnName, Condition.RELATION_LESS_THAN, false, value, (String)null);
    }
    
    /**
     * (<b>需 static import</b>)
     * 產生 "columnName &lt;= &#063;" 條件, 並指定其值.
     * @param columnName  SQL 中的欄位名
     * @param value  欄位值, 型態為 String/Integer/Long/Byte/Short/Float/Double/byte[]/Boolean/BigDecimal/Blob/Clob,
     *        或 java.util.Date/java.sql.Timestamp/java.sql.Date/java.sql.Time 資料型態
     */
    public static Condition ltEq(final String columnName, final Object value) {
        return new Condition(columnName, Condition.RELATION_LESS_THAN_OR_EQUAL, true, value, (String)null);
    }

    /**
     * (<b>需 static import</b>)
     * 產生 "columnName &lt;= (子 SQL)" 條件.
     * @param columnName  SQL 中的欄位名
     * @param subquery  放置子 SQL 句的物件
     */
    public static Condition ltEq(final String columnName, final SqlAssembler subquery) {
        if(subquery == null)
            return ltEq(columnName, (Object)null);
        return new Condition(columnName, Condition.RELATION_LESS_THAN_OR_EQUAL, subquery);
    }
    
    /**
     * (<b>需 static import</b>)
     * 產生 "columnName &lt;= &#063;" 條件, 並指定其值(值為  null 者, 略過本條件).
     * @param columnName  SQL 中的欄位名
     * @param value  欄位值, 型態為 String/Integer/Long/Byte/Short/Float/Double/byte[]/Boolean/BigDecimal/Blob/Clob,
     *        或 java.util.Date/java.sql.Timestamp/java.sql.Date/java.sql.Time 資料型態
     */
    public static Condition ltEqNN(final String columnName, final Object value) {
    	if(value == null)
    		return new Condition();
        return new Condition(columnName, Condition.RELATION_LESS_THAN_OR_EQUAL, false, value, (String)null);
    }
    
    /**
     * (<b>需 static import</b>)
     * 產生 "columnName &gt; &#063;" 條件, 並指定其值.
     * @param columnName  SQL 中的欄位名
     * @param value  欄位值, 型態為 String/Integer/Long/Byte/Short/Float/Double/byte[]/Boolean/BigDecimal/Blob/Clob,
     *        或 java.util.Date/java.sql.Timestamp/java.sql.Date/java.sql.Time 資料型態
     */
    public static Condition gt(final String columnName, final Object value) {
        return new Condition(columnName, Condition.RELATION_GREATER_THAN, true, value, (String)null);
    }

    /**
     * (<b>需 static import</b>)
     * 產生 "columnName &gt; (子 SQL)" 條件.
     * @param columnName  SQL 中的欄位名
     * @param subquery  放置子 SQL 句的物件
     */
    public static Condition gt(final String columnName, final SqlAssembler subquery) {
        if(subquery == null)
            return gt(columnName, (Object)null);
        return new Condition(columnName, Condition.RELATION_GREATER_THAN, subquery);
    }
    
    /**
     * (<b>需 static import</b>)
     * 產生 "columnName &gt; &#063;" 條件, 並指定其值(值為  null 者, 略過本條件).
     * @param columnName  SQL 中的欄位名
     * @param value  欄位值, 型態為 String/Integer/Long/Byte/Short/Float/Double/byte[]/Boolean/BigDecimal/Blob/Clob,
     *        或 java.util.Date/java.sql.Timestamp/java.sql.Date/java.sql.Time 資料型態
     */
    public static Condition gtNN(final String columnName, final Object value) {
    	if(value == null)
    		return new Condition();
        return new Condition(columnName, Condition.RELATION_GREATER_THAN, false, value, (String)null);
    }
    
    /**
     * (<b>需 static import</b>)
     * 產生 "columnName &gt;= &#063;" 條件, 並指定其值.
     * @param columnName  SQL 中的欄位名
     * @param value  欄位值, 型態為 String/Integer/Long/Byte/Short/Float/Double/byte[]/Boolean/BigDecimal/Blob/Clob,
     *        或 java.util.Date/java.sql.Timestamp/java.sql.Date/java.sql.Time 資料型態
     */
    public static Condition gtEq(String columnName, Object value) {
        return new Condition(columnName, Condition.RELATION_GREATER_THAN_OR_EQUAL, true, value, (String)null);
    }

    /**
     * (<b>需 static import</b>)
     * 產生 "columnName &gt;= (子 SQL)" 條件.
     * @param columnName  SQL 中的欄位名
     * @param subquery  放置子 SQL 句的物件
     */
    public static Condition gtEq(final String columnName, final SqlAssembler subquery) {
        if(subquery == null)
            return gtEq(columnName, (Object)null);
        return new Condition(columnName, Condition.RELATION_GREATER_THAN_OR_EQUAL, subquery);
    }
    
    /**
     * (<b>需 static import</b>)
     * 產生 "columnName &gt;= &#063;" 條件, 並指定其值(值為  null 者, 略過本條件).
     * @param columnName  SQL 中的欄位名
     * @param value  欄位值, 型態為 String/Integer/Long/Byte/Short/Float/Double/byte[]/Boolean/BigDecimal/Blob/Clob,
     *        或 java.util.Date/java.sql.Timestamp/java.sql.Date/java.sql.Time 資料型態
     */
    public static Condition gtEqNN(final String columnName, final Object value) {
    	if(value == null)
    		return new Condition();
        return new Condition(columnName, Condition.RELATION_GREATER_THAN_OR_EQUAL, false, value, (String)null);
    }
    
    /**
     * (<b>需 static import</b>)
     * 產生 "columnName in (&#063;,&#063;,…)" 條件, 並指定其值;
     * 若整個 values 陣列為 null 者, <b>忽略本條件</b>; 若 values 陣列長度為 0 者, <b>視為條件不成立</b>.
     * @param columnName  SQL 中的欄位名
     * @param values  欄位值, 型態為 String/Integer/Long/Byte/Short/Float/Double/byte[]/Boolean/BigDecimal/Blob/Clob,
     *        或 java.util.Date/java.sql.Timestamp/java.sql.Date/java.sql.Time 資料型態
     */
    public static Condition in(final String columnName, final Object ... values) {
    	if(values == null)
    		return new Condition();
    	if(values.length == 0)
    		return condition("1=0"); //條件不成立
        return new Condition(columnName, Condition.RELATION_IN, true, values, (String[])null);
    }

    /**
     * (<b>需 static import</b>)
     * 產生 "columnName in (子 SQL)" 條件. 例: <pre style="border: 1px solid gray"><code>
     * new SqlAssembler("select a, b, c from spam").where(
     *     eq("a", xxx).and(gt("b", xxx)).and(ltNN("z", xxx))
     *     .and(in("c",
     *         new SqlAssembler("select c from foo").where(between("to_char(d,'yyyymmdd')", xxx, xxx))
     *     ))
     * );
     * </code></pre>
     * 得到: <pre style="border: 1px solid gray"><code>
     * select a, b, c from spam where (a=? and b &gt;? 
     *     and c in (
     *         select c from foo where to_char(d,'yyyymmdd') between ? and ?))
     * </code></pre>
     * @param columnName  SQL 中的欄位名
     * @param subquery  放置子 SQL 句的物件, null 者則忽略本條件
     */
    public static Condition in(final String columnName, final SqlAssembler subquery) {
        if(subquery == null)
            return new Condition();
        return new Condition(columnName, Condition.RELATION_IN, subquery);
    }
    
    /**
     * (<b>需 static import</b>)
     * 產生 "columnName in (&#063;,&#063;,…)" 條件, 並指定其值(其中某成員值為  null 者, 略過本成員值); 
     * 若整個 values 陣列為 null 者, <b>忽略本條件</b>; 若剔除 null 成員後的 values 陣列長度為 0 者, <b>視為條件不成立</b>.
     * @param columnName  SQL 中的欄位名
     * @param values  欄位值, 型態為 String/Integer/Long/Byte/Short/Float/Double/byte[]/Boolean/BigDecimal/Blob/Clob,
     *        或 java.util.Date/java.sql.Timestamp/java.sql.Date/java.sql.Time 資料型態
     */
    public static Condition inNN(final String columnName, final Object ... values) {
    	if(values == null)
    		return new Condition(); //忽略本條件
		if(values.length == 0)
    		return condition("1=0"); //條件不成立
        return new Condition(columnName, Condition.RELATION_IN, false, values, (String[])null);
    }
    
    /**
     * (<b>需 static import</b>)
     * 產生 "columnName not in (&#063;,&#063;,…)" 條件, 並指定其值, 若整個 values 陣列為 null 或 values 陣列長度為 0 者, <b>略過本條件</b>.
     * @param columnName  SQL 中的欄位名
     * @param values  欄位值, 型態為 String/Integer/Long/Byte/Short/Float/Double/byte[]/Boolean/BigDecimal/Blob/Clob,
     *        或 java.util.Date/java.sql.Timestamp/java.sql.Date/java.sql.Time 資料型態
     */
    public static Condition notIn(final String columnName, final Object ... values) {
    	if(values == null || values.length == 0)
    		return new Condition();
        return new Condition(columnName, Condition.RELATION_NOT_IN, true, values, (String[])null);
    }

    /**
     * (<b>需 static import</b>)
     * 產生 "columnName not in (子 SQL)" 條件.
     * @param columnName  SQL 中的欄位名
     * @param subquery  放置子 SQL 句的物件. null 者則忽略本條件
     */
    public static Condition notIn(final String columnName, final SqlAssembler subquery) {
        if(subquery == null)
            return new Condition();
        return new Condition(columnName, Condition.RELATION_NOT_IN, subquery);
    }
    
    /**
     * (<b>需 static import</b>)
     * 產生 "columnName not in (&#063;,&#063;,…)" 條件, 並指定其值(其中成員值為  null 者, 略過本成員值),
     * 若整個 values 陣列為 null, 或剔除 null 成員後的 values 陣列長度為 0 者, <b>略過本條件</b>.
     * @param columnName  SQL 中的欄位名
     * @param values  欄位值, 型態為 String/Integer/Long/Byte/Short/Float/Double/byte[]/Boolean/BigDecimal/Blob/Clob,
     *        或 java.util.Date/java.sql.Timestamp/java.sql.Date/java.sql.Time 資料型態
     */
    public static Condition notInNN(final String columnName, final Object ... values) {
    	if(values == null || values.length == 0)
    		return new Condition();
        return new Condition(columnName, Condition.RELATION_NOT_IN, false, (values == null) ? new Object[0] : values, (String[])null);
    }
    
    /**
     * (<b>需 static import</b>)
     * 產生 "columnName between &#063; and &#063;" 條件, 並指定其值
     * @param columnName  SQL 中的欄位名
     * @param value1  欄位值, 型態為 String/Integer/Long/Byte/Short/Float/Double/byte[]/Boolean/BigDecimal/Blob/Clob,
     *        或 java.util.Date/java.sql.Timestamp/java.sql.Date/java.sql.Time 資料型態
     * @param value2  欄位值, 型態為 String/Integer/Long/Byte/Short/Float/Double/byte[]/Boolean/BigDecimal/Blob/Clob,
     *        或 java.util.Date/java.sql.Timestamp/java.sql.Date/java.sql.Time 資料型態
     */
    public static Condition between(final String columnName, final Object value1, final Object value2) {
        return new Condition(columnName, Condition.RELATION_BETWEEN, true, new Object[] { value1, value2 }, (String[])null);
    }
    
    /**
     * (<b>需 static import</b>)
     * 產生 "columnName not between &#063; and &#063;" 條件, 並指定其值
     * @param columnName  SQL 中的欄位名
     * @param value1  欄位值, 型態為 String/Integer/Long/Byte/Short/Float/Double/byte[]/Boolean/BigDecimal/Blob/Clob,
     *        或 java.util.Date/java.sql.Timestamp/java.sql.Date/java.sql.Time 資料型態
     * @param value2  欄位值, 型態為 String/Integer/Long/Byte/Short/Float/Double/byte[]/Boolean/BigDecimal/Blob/Clob,
     *        或 java.util.Date/java.sql.Timestamp/java.sql.Date/java.sql.Time 資料型態
     */
    public static Condition notBetween(final String columnName, final Object value1, final Object value2) {
        return new Condition(columnName, Condition.RELATION_NOT_BETWEEN, true, new Object[] { value1, value2 }, (String[])null);
    }
    
    /**
     * (<b>需 static import</b>)
     * 產生 "columnName like &#063;" 條件, 並指定其值(該值需自行安置 SQL like 算式所需的萬用字元(如 '%'); 值為  null 者, 略過本條件). 例: <pre style="border: 1px solid gray"><code>
     * if(...)
     *    value3 = x + "<b>%</b>"; //value3 值打算用於模糊比對欄位值
     * ...
     * new SqlAssembler("select * from abc").where(
     *     eqNN("a", value1)
     *     .and(eq("b", value2))
     *     .and(<b>likeNN</b>("f", value3))
     * );
     * </code></pre>
     * 得到 SQL: <pre style="border: 1px solid gray"><code>
     * select * from abc where (a=? and b=? and f <b>like</b> ?)
     * //或者當 value3=null 時:
     * select * from abc where (a=? and b=?)
     * </code></pre>
     * @param columnName  SQL 中的欄位名
     * @param value  欄位值
     */
    public static Condition likeNN(final String columnName, final String value) {
        return new Condition(columnName, Condition.RELATION_LIKE, false, value, (String)null);
    }
    
    /**
     * (<b>需 static import</b>)
     * 產生 "columnName not like &#063;" 條件, 並指定其值(該值需自行安置 SQL like 算式所需的萬用字元(如 '%'); 值為  null 者, 略過本條件).
     * @see #likeNN(String, String)
     * @param columnName  SQL 中的欄位名
     * @param value  欄位值
     */
    public static Condition notLikeNN(final String columnName, final String value) {
        return new Condition(columnName, Condition.RELATION_NOT_LIKE, false, value, (String)null);
    }
    
    /**
     * (<b>需 static import</b>)
     * 產生 "exists (子 SQL)" 條件.<pre style="border: 1px solid gray"><code>
     * new SqlAssembler("select * from abc").where(
     *     eq("a", xxx)
     *     .and(exists(
     *         new SqlAssembler("select * from def").where(eq("c", xxx))
     *     ))
     * );
     * </code></pre>
     * 得到:<pre style="border: 1px solid gray"><code>
     * select * from abc where (a=? and exists (select * from def where c=?))
     * </code></pre>
     * 又例: <pre style="border: 1px solid gray"><code>
     * new SqlAssembler("select * from abc").where(
     *     exists(
     *         new SqlAssembler("select * from def").where(eq("c", xxx))
     *     )
     * );
     * </code></pre>
     * 得到:<pre style="border: 1px solid gray"><code>
     * select * from abc where exists (select * from def where c=?)
     * </code></pre>
     * @param subquery  放置子 SQL 句的物件
     */
    public static Condition exists(final SqlAssembler subquery) {
        return new Condition((String)null, Condition.RELATION_EXISTS, subquery);
    }
    
    /**
     * (<b>需 static import</b>)
     * 產生 "not exists (子 SQL)" 條件.
     * @see #exists(SqlAssembler)
     * @param subquery  放置子 SQL 句的物件
     */
    public static Condition notExists(final SqlAssembler subquery) {
        return new Condition((String)null, Condition.RELATION_NOT_EXISTS, subquery);
    }
    
    /**
     * (<b>需 static import</b>)
     * 產生任意的含 "&#063;" 佔位符號的一個條件式, 並指定其值. 例:<pre style="border: 1px solid gray"><code>
     * new SqlAssembler("select p.a, p.b, q.c from spam p, eggs q").where(
     *     condition("p.a &gt; ? or p.b != ?", xxx, xxx)
     * );
     * </code></pre>
     * 得到: <pre style="border: 1px solid gray"><code>
     * select p.a, p.b, q.c from spam p, eggs q where p.a &gt; ? or p.b != ?
     * </code></pre>
     * @param condition  一個任意 SQL 條件式
     * @param values  欄位值, 型態為 String/Integer/Long/Byte/Short/Float/Double/byte[]/Boolean/BigDecimal/Blob/Clob,
     *        或 java.util.Date/java.sql.Timestamp/java.sql.Date/java.sql.Time 資料型態
     */
    public static Condition condition(final String condition, final Object ... values) {
        return new Condition(condition, Condition.RELATION_UNKNOWN, true, values, (String[])null);
    }

    /**
     * (<b>需 static import</b>)
     * 產生一個或一組條件式(不含 "&#063;" 佔位符號), 以括弧圍住. 
     * 例:<pre style="border: 1px solid gray"><code>
     * new SqlAssembler("select p.a, p.b, q.c from spam p, eggs q").where(
     *     gt("p.a", xxx)
     *     .and(eqNN("p.b", xxx))
     *     .and(condition("p.b is null or p.b='A'"))
     * );
     * </code></pre>
     * 得到: <pre style="border: 1px solid gray"><code>
     * select p.a, p.b, q.c from spam p, eggs q where (
     * p.a &gt; ? and p.b=? and (p.b is null or p.b='A'))
     * </code></pre>
     * @param condition  一個任意 SQL 條件式
     */
    public static Condition condition(final String condition) {
        return new Condition(condition, Condition.RELATION_UNKNOWN, true, null, (String[])null);
    }
    
    /**
     * (<b>需 static import</b>)
     * 將現成的條件式用括弧包圍起來(可用於 where 條件後的第一組條件, 無法 and/or 圍起來時). 例: <pre style="border: 1px solid gray"><code>
     * new SqlAssembler("select a, b, c from spam").where(
     *     condition(<b>eqNN("a", xxx).or(eqNN("b", xxx))</b>).and(gtNN("c", xxx)).and(ltNN("d", xxx))
     * );
     * </code></pre>
     * 得到: <pre style="border: 1px solid gray"><code>
     * select a, b, c from spam where (<b>(a=? or b=?)</b> and c &gt;? and d &lt;?)
     * </code></pre>
     * @param conditions
     * @return
     */
    public static Condition condition(final Condition conditions) {
    	return new Condition(conditions);
    }
    
    /**
     * (<b>需 static import</b>)
     * 產生任意的含<b>一個</b> "&#063;" 佔位符號的一個條件式, 並指定其值, 且值不為 null 時才成立此條件式. 例: <pre style="border: 1px solid gray"><code>
     * new SqlAssembler("select * from test").where(
     *     eqNN("col1", xxx)
     *     .and(conditionNN("col2 like ?", xxx))
     * );
     * </code></pre>
     * 得到: <pre style="border: 1px solid gray"><code>
     * select * from test where (col1=? and col2 like ?)
     * </code></pre>
     * @param condition  一個任意 SQL 條件式
     * @param value  欄位值, 型態為 String/Integer/Long/Byte/Short/Float/Double/byte[]/Boolean/BigDecimal/Blob/Clob,
     *        或 java.util.Date/java.sql.Timestamp/java.sql.Date/java.sql.Time 資料型態
     */
    public static Condition conditionNN(final String condition, final Object value) {
        return new Condition(condition, Condition.RELATION_UNKNOWN, false, value, (String)null);
    }
    
    /** (<b>需 static import</b>) 在 where/having 條件句中插入<b>任意字串</b>. */
    public static Condition raws(String s) {
        return new Condition(s);
    }

    //
    //inner class "Condition"
    //
    
    /**
     * 用於存放存在於 where 或 having 子句中的 "一項" 關係式
     */
    public static class Condition {
        static final int RELATION_UNKNOWN = 0; //使用者自行指定
        static final int RELATION_EQUAL = 1; //a=?
        static final int RELATION_NOT_EQUAL = 2; //a!=?
        static final int RELATION_GREATER_THAN = 3; //a>?
        static final int RELATION_GREATER_THAN_OR_EQUAL = 4; //a>=?
        static final int RELATION_LESS_THAN = 5; //a<?
        static final int RELATION_LESS_THAN_OR_EQUAL = 6; //a<=?
        static final int RELATION_IN = 7; //a in (...)
        static final int RELATION_NOT_IN = 8; //a not in (...)
        static final int RELATION_BETWEEN = 9; //a between (..., ...)
        static final int RELATION_NOT_BETWEEN = 10; //a not between (..., ...)
        static final int RELATION_LIKE = 11; //a like (...)
        static final int RELATION_NOT_LIKE = 12; //a not like (...)
        static final int RELATION_EXISTS = 13; //exists (...)
        static final int RELATION_NOT_EXISTS = 14; //not exists (...)
        
        private List<String> conditions;
        private List<Object> values;
        
        public Condition() {
        	this.conditions = new LinkedList<String>();
            this.values = new LinkedList<Object>();
        }
        
        /** 直接將參數 raw 值字串, 不作任何改變地嵌入 SQL 句中. */
        Condition(final String raw) {
            this();
            if(raw != null && raw.length() != 0)
            	this.conditions.add(raw);
        }
        
        /** 用以表示如 =, !=, like, … 等簡單關係式 */
        Condition(final String columnName, final int relationType, final boolean allowNullValue, 
        		final Object vo, final String propertyName) {
        	this();
        	
        	if(columnName != null && columnName.length() != 0) {
	            Object[] value = (propertyName != null) ? retrieveValue(vo, propertyName) : new Object[] { vo }; //不指定 propertyName 者, 直接把 args 視為欄位值
	            
	            if(value.length != 0) {
	                if(value[0] == null) {
	                    if(allowNullValue) {
	                        StringBuilder condition = new StringBuilder().append(columnName);
	                        
	                        switch(relationType) {
	                            case RELATION_UNKNOWN: break; //value=null
	                            case RELATION_EQUAL: condition.append(" is null"); break;
	                            case RELATION_NOT_EQUAL: condition.append(" is not null"); break;
	                            case RELATION_GREATER_THAN: throw new IllegalArgumentException("null value not allowed for '>' operation");
	                            case RELATION_GREATER_THAN_OR_EQUAL: throw new IllegalArgumentException("null value not allowed for '>=' operation");
	                            case RELATION_LESS_THAN: throw new IllegalArgumentException("null value not allowed for '<' operation");
	                            case RELATION_LESS_THAN_OR_EQUAL: throw new IllegalArgumentException("null value not allowed for '<=' operation");
	                            case RELATION_LIKE: throw new IllegalArgumentException("null value not allowed for 'like' operation");
	                            case RELATION_NOT_LIKE: throw new IllegalArgumentException("null value not allowed for 'not like' operation");
	                            default: throw new IllegalArgumentException("unknown relation: " + relationType);
	                        }
	                        this.conditions.add(condition.toString());
	                    }
	                } else {
	                    this.values.add(value[0]);
	                    StringBuilder condition = new StringBuilder().append(columnName);
	                    
	                    switch(relationType) {
	                        case RELATION_UNKNOWN: break;
	                        case RELATION_EQUAL: condition.append("=?"); break;
	                        case RELATION_NOT_EQUAL: condition.append(" !=?"); break;
	                        case RELATION_GREATER_THAN: condition.append(" >?"); break;
	                        case RELATION_GREATER_THAN_OR_EQUAL: condition.append(" >=?"); break;
	                        case RELATION_LESS_THAN: condition.append(" <?"); break;
	                        case RELATION_LESS_THAN_OR_EQUAL: condition.append(" <=?"); break;
	                        case RELATION_LIKE: condition.append(" like ?"); break;
	                        case RELATION_NOT_LIKE: condition.append(" not like ?"); break;
	                        default: throw new IllegalArgumentException("unknown relation: " + relationType);
	                    }
	                    this.conditions.add(condition.toString());
	                }
	            }
            }
        }
        
        /** 用以表示如 in, not in, between, not between 或不明的 關係式 */
        Condition(final String columnName, final int relationType, final boolean allowNullValue, final Object vo, 
        		final String[] propertyNames) {
        	this.conditions = new LinkedList<String>();
        	if(columnName == null || columnName.length() == 0) {
        		this.values = new LinkedList<Object>();
        		return;
        	}
        	
        	final boolean nullable;
            if(relationType == RELATION_UNKNOWN || relationType == RELATION_BETWEEN || relationType == RELATION_NOT_BETWEEN)
                nullable = true;
            else
            	nullable = allowNullValue;
            
            //不需設欄位值者
            if(vo == null) {
                this.conditions.add(columnName);
                this.values = new LinkedList<Object>();
                return;
            }
            
            //propertyNames == null 者, vo 即是 Object[]
            this.values = (propertyNames != null) ? retrieveValues(vo, propertyNames, nullable) : asList((Object[])vo, nullable);
            if((relationType == RELATION_BETWEEN || relationType == RELATION_NOT_BETWEEN) && values.size() != 2) //between 和 not between 一定要配兩個 value 欄位
                throw new IllegalArgumentException("number of values doesn't match BETWEEN relation");
            if(this.values.size() == 0) {
            	if(relationType == RELATION_IN) //in (...) 無值可配者, 視為本條件不成立
            		this.conditions.add("1=0");
                return; //無條件
            }
            
            //以下 value 欄位數不為 0, for: between, not between, in, not in...
            final StringBuilder condition = new StringBuilder().append(columnName);
            
            switch(relationType) {
                case RELATION_UNKNOWN: break;
                case RELATION_IN: condition.append(" in (").append(prepareSqlParameters(this.values.size())).append(")"); break;
                case RELATION_NOT_IN: condition.append(" not in (").append(prepareSqlParameters(this.values.size())).append(")"); break;
                case RELATION_BETWEEN: condition.append(" between ? and ?"); break;
                case RELATION_NOT_BETWEEN: condition.append(" not between ? and ?"); break;
                default: throw new IllegalArgumentException("unknown relation: " + relationType);
            }
            this.conditions.add(condition.toString());
        }
        
        Condition(final Condition condition) {
        	if(condition == null) {
        		this.conditions = new LinkedList<String>();
        		this.values = new LinkedList<Object>();
        		return;
        	}
        	
        	this.values = condition.getValues();
        	if(condition.getConditions().size() == 0) {
        		this.conditions = condition.getConditions();
        		return;
        	}
        	
        	if(this.conditions == null)
        		this.conditions = new LinkedList<String>();
        	
        	if(condition.getConditions().size() > 1)
        		this.conditions.add("(");
        	this.conditions.addAll(condition.getConditions());
        	if(condition.getConditions().size() > 1)
        		this.conditions.add(")");
        }

        /** 含子查詢 SQL 句的關係式 */
        Condition(final String columnName, final int relationType, final SqlAssembler subquery) {
        	this.conditions = new LinkedList<String>();
        	
        	if(columnName == null || columnName.length() == 0) {
        		this.values = new LinkedList<Object>();
        		return;
        	}
        	
            if(subquery == null) {
            	this.conditions.add("");
                this.values = new LinkedList<Object>();
            } else {
            	final StringBuilder condition = new StringBuilder();
	            if(columnName != null)
	            	condition.append(columnName);
	            
	            switch(relationType) {
	                case RELATION_UNKNOWN: break;
	                case RELATION_EQUAL: condition.append(" ="); break;
	                case RELATION_NOT_EQUAL: condition.append(" !="); break;
	                case RELATION_GREATER_THAN: condition.append(" >"); break;
	                case RELATION_GREATER_THAN_OR_EQUAL: condition.append(" >="); break;
	                case RELATION_LESS_THAN: condition.append(" <"); break;
	                case RELATION_LESS_THAN_OR_EQUAL: condition.append(" <="); break;
	                case RELATION_IN: condition.append(" in"); break;
	                case RELATION_NOT_IN: condition.append(" not in"); break;
	                case RELATION_EXISTS: condition.append(" exists"); break;
	                case RELATION_NOT_EXISTS: condition.append(" not exists"); break;
	                default: throw new IllegalArgumentException("unknown relation: " + relationType);
	            }
	            condition.append(" (").append(subquery.getSql()).append(")");
	            this.conditions.add(condition.toString());
	            this.values = subquery.getValues2();
            }
        }
        
        /** 
         * 以 and 連結二關係式.<br>
         * 例:
         * <pre style="border: 1px solid gray"><code>
         * SqlAssenbler sql = new SqlAssembler("select * from foo").where(
         *         eq("a_field", x).<b>and</b>(eq("b_field, y))
         *     );
         * 
         * 產生 SQL 字串:
         * 
         * select * from foo where (a_field=? <b>and</b> (b_field=?))
         * //x 變數值對應至第一個佔位符 "?", y 變數值對應至第二個佔位符 "?"
         * </code></pre>
         */
        public Condition and(final Condition condition) {
            if(condition != null && condition.getConditions().size() != 0) {
                if(this.conditions.size() > 0) {
                    add(addAll(add(this.conditions, "and ("), condition.getConditions()), ")");
                } else {
                    add(addAll(add(this.conditions, "("), condition.getConditions()), ")");
                }
                this.values.addAll(condition.getValues());
            }
            return this;
        }
        
        /** 
         * 以 and 連結二關係式 (等同 {@link #and(Condition) and(SqlAssempler.condition(cond))} 的簡化寫法).<br>
         * 例:
         * <pre style="border: 1px solid gray"><code>
         * SqlAssenbler sql = new SqlAssembler("select * from foo").where(
         *         eq("a_field", x)
         *         .and(<b>condition("b_field is not null and b_field != 0")</b>)
         *     );
         * 
         * 可簡化為:
         * 
         * SqlAssenbler sql = new SqlAssembler("select * from foo").where(
         *         eq("a_field", x)
         *         .and(<b>"b_field is not null and b_field != 0"</b>)
         *     );
         * </code></pre>
         */
        public Condition and(final String cond) {
        	return and(SqlAssembler.condition(cond));
        }
        
        /** 
         * 以 and 連結二關係式 (等同 {@link #and(Condition) and(SqlAssempler.condition(cond, values[0], values[1], ...))} 的簡化寫法).<br>
         * 例:
         * <pre style="border: 1px solid gray"><code>
         * SqlAssenbler sql = new SqlAssembler("select * from foo").where(
         *         eq("a_field", x)
         *         .and(<b>condition("b_field is not null and b_field != ?", y)</b>)
         *     );
         * 
         * 可簡化為:
         * 
         * SqlAssenbler sql = new SqlAssembler("select * from foo").where(
         *         eq("a_field", x)
         *         .and(<b>"b_field is not null and b_field != ?", y</b>)
         *     );
         * </code></pre>
         */
        public Condition and(final String cond, final Object ... values) {
        	return and(SqlAssembler.condition(cond, values));
        }
        
        /**
         * 以 or 連結二關係式.<br>
         * 例:
         * <pre style="border: 1px solid gray"><code>
         * SqlAssenbler sql = new SqlAssembler("select * from foo").where(
         *         eq("a_field", x).<b>or</b>(eq("b_field, y))
         *     );
         * 
         * 產生 SQL 字串:
         * 
         * select * from foo where (a_field=? <b>or</b> (b_field=?))
         * //x 變數值對應至第一個佔位符 "?", y 變數值對應至第二個佔位符 "?"
         * </code></pre>
         */
        public Condition or(final Condition condition) {
            if(condition != null && condition.getConditions().size() != 0) {
                if(this.conditions.size() > 0) {
                    add(addAll(add(this.conditions, "or ("), condition.getConditions()), ")");
                } else {
                    add(addAll(add(this.conditions, "("), condition.getConditions()), ")");
                }
                this.values.addAll(condition.getValues());
            }
            return this;
        }
        
        /** 
         * 以 or 連結二關係式 (等同 {@link #or(Condition) or(SqlAssembler.condition(cond))} 的簡化寫法).<br>
         * 例:
         * <pre style="border: 1px solid gray"><code>
         * SqlAssenbler sql = new SqlAssembler("select * from foo").where(
         *         eq("a_field", x)
         *         .or(<b>condition("b_field is not null and b_field != 0")</b>)
         *     );
         * 
         * 可簡化為:
         * 
         * SqlAssenbler sql = new SqlAssembler("select * from foo").where(
         *         eq("a_field", x)
         *         .or(<b>"b_field is not null and b_field != 0"</b>)
         *     );
         * </code></pre>
         */
        public Condition or(final String cond) {
        	return or(SqlAssembler.condition(cond));
        }
        
        /** 
         * 以 or 連結二關係式 (等同 {@link #or(Condition) or(SqlAssembler.condition(cond, values[0], values[1], ...))} 的簡化寫法).<br>
         * 例:
         * <pre style="border: 1px solid gray"><code>
         * SqlAssenbler sql = new SqlAssembler("select * from foo").where(
         *         eq("a_field", x)
         *         .or(<b>condition("b_field is not null and b_field != ?", y)</b>)
         *     );
         * 
         * 可簡化為:
         * 
         * SqlAssenbler sql = new SqlAssembler("select * from foo").where(
         *         eq("a_field", x)
         *         .or(<b>"b_field is not null and b_field != ?", y</b>)
         *     );
         * </code></pre>
         */
        public Condition or(final String cond, final Object ... values) {
        	return or(SqlAssembler.condition(cond, values));
        }
        
        /** 
         * 插入任意字串(不會在最後產生的 SQL 句中, 額外以括弧把 s 字串圍起來).<br>
         * 例:
         * <pre style="border: 1px solid gray"><code>
         * SqlAssenbler sql = new SqlAssembler("select * from foo").where(
         *         eq("a_field", x)
         *     ).raw("order by b_field");
         * 
         * 產生 SQL 字串:
         * 
         * select * from foo where (a_field=?) order by b_field
         * </code></pre>
         */
        public Condition raw(final String s) {
        	if(s != null && s.length() != 0)
        		this.conditions.add(s);
            return this;
        }
        
        final List<String> getConditions() {
            return this.conditions;
        }
        
        final List<Object> getValues() {
            return this.values;
        }
    }
    
    //args 可能是 Map 或 JavaBean 或 SQL type value
    @SuppressWarnings("unchecked")
    private static Object[] retrieveValue(final Object args, final String propertyName) { //不傳回 Object 而傳回 Object[] 是為了控制是否有值 (null 值也是值) 
        if(args == null)
            return new Object[] { null };
        
        //args 是 Map 者, 取其 key-value
        if(args instanceof Map) {
            if(propertyName == null)
                throw new RuntimeException("SqlAssembler: no given key for the input map-arguments");
            Map<String, Object> args1 = (Map<String, Object>)args;
            return args1.containsKey(propertyName) ? new Object[] { args1.get(propertyName) } : new Object[0]; //無此 key, 略過本條件
        }
        
        //args 是 SQL 之資料型態者, 直接視為值而傳回
        Class<? extends Object> type = null;
        if((type = args.getClass()).equals(String.class) || type.equals(Integer.class) || type.equals(Long.class) || 
                type.equals(Byte.class) || type.equals(Short.class) || type.equals(Float.class) || 
                type.equals(Double.class) || type.equals(byte[].class) || type.equals(Boolean.class) || 
                type.equals(BigDecimal.class) || type.equals(Blob.class) || type.equals(Clob.class) || 
                type.equals(java.util.Date.class) || type.equals(java.sql.Timestamp.class) || type.equals(java.sql.Date.class) || 
                type.equals(java.sql.Time.class)) {
            return new Object[] { args };
        }
        
        //args 是 JavaBean 者, 取其屬性及值
        if(propertyName == null)
            throw new RuntimeException("SqlHelper: no property name given for the input bean-arguments");
        if(!PropertyUtils.isReadable(args, propertyName)) //無此屬性, 略過本條件
            return new Object[0];
        try {
            return new Object[] { PropertyUtils.getSimpleProperty(args, propertyName) }; 
        } catch(Throwable t) {
            throw new IllegalArgumentException(t); 
        }
    }
    
    //args 可能是 Map 或 JavaBean
    @SuppressWarnings("unchecked")
    private static List<Object> retrieveValues(final Object args, final String[] propertyNames, final boolean valueNullable) {
        List<Object> values = new LinkedList<Object>();
        
        if(args instanceof Map) { //args 是 Map 者, 取其 key-value
            Map<String, Object> args1 = (Map<String, Object>)args;
            for(int i = 0; i < propertyNames.length; i++) {
                String k = propertyNames[i];
                if(!args1.containsKey(k)) //略過不存在的 key
                    continue;
                Object v = args1.get(k);
                if(v != null || valueNullable)
                    values.add(v);
            }
        } else { //args 是 JavaBean 者, 取其屬性及值
            for(int i = 0; i < propertyNames.length; i++) {
                String propertyName = propertyNames[i];
                if(!PropertyUtils.isReadable(args, propertyName)) //略過不存在的屬性名
                    continue;
                Object v = null;
                try {
                    v = PropertyUtils.getSimpleProperty(args, propertyName); 
                } catch(Throwable t) {
                    throw new IllegalArgumentException(t);
                }
                if(v != null || valueNullable)
                    values.add(v);
            }
        }
        return values;
    }

    //values != null
    private static List<Object> asList(final Object[] values, final boolean allowNullValue) {
    	List<Object> ret = new LinkedList<Object>();
    	if(allowNullValue) {
    		ret.addAll(Arrays.asList(values));
    		return ret;
    	}
    	for(int i = 0; i < values.length; i++) {
    		if(values[i] != null)
    			ret.add(values[i]);
    	}
    	return ret;
    }
    
    //依待給的值的數量準備 SQL 中的占位符表示句
    private static String prepareSqlParameters(final int valuesQuantity) {
    	return (valuesQuantity <= 0) ? "" :
    		(valuesQuantity == 1) ? "?" : (StrUtil.repeat("?,", valuesQuantity - 1) + "?");
    }
    
    private static List<String> add(final List<String> list, final String ... ss) {
        for(String s : ss)
            list.add(s);
        return list;
    }
    
    private static List<String> addAll(final List<String> list, final List<String> toBeAdded) {
        list.addAll(toBeAdded);
        return list;
    }
}
