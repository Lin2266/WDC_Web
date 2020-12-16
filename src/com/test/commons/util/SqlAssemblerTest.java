package com.test.commons.util;

import static com.test.commons.util.SqlAssembler.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import javax.sql.DataSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import junit.framework.TestCase;

public class SqlAssemblerTest extends TestCase {
	private DataSource ds;
    private Map<String, Object> args;
    private ArgsBean args1;
    
    protected void setUp() throws Exception {
//    	final ApplicationContext context = new FileSystemXmlApplicationContext("web/WEB-INF/spring-main.xml");
//    	this.ds = (DataSource)context.getBean("dataSource");
    	
        this.args = new HashMap<String, Object>();
        this.args.put("a", "test1");
        this.args.put("b", 2);
        this.args.put("c", new java.util.Date());
        this.args.put("d", "20081210");
        this.args.put("e", null);
        
        this.args1 = new ArgsBean();
        this.args1.setA("test1");
        this.args1.setB(2);
        this.args1.setC(new java.util.Date());
        this.args1.setD(new java.util.Date());
        this.args1.setF("test%"); //for like operator
    }
    
    //MS SQL Server 官方 JDBC driver 之 PreparedStatement.getParameterMetaData() 對 SQL 敘述中的括弧的排法有怪異的限制
    public void xtestSQLServerPreparedStatementParameterMetaData() throws Exception {
    	Connection conn = null;
    	PreparedStatement pstmt = null;
    	ResultSet rs = null;
    	
    	try {
	    	System.out.println("testSQLServer()...");
	    	//String sql = "select * from test where aaa=? and bbb=? and ccc=?"; //correct
	    	
	    	//String sql = "select * from test where (aaa=? and bbb=? and ccc=?)"; //error
	    	//String sql = "select * from test where ( aaa=? and bbb=? and ccc=? )"; //correct
	    	
	    	//String sql = "select * from test where aaa=? and (bbb=? and ccc=?)"; //error
	    	//String sql = "select * from test where aaa=? and ( bbb=? and ccc=? )"; //correct
	    	
	    	//String sql = "select * from test where aaa=? and ( bbb=? or ccc=? )"; //error
	    	//String sql = "select * from test where aaa=? and ( bbb=? or ccc=? )"; //correct
	    	
	    	//String sql = "select * from test where ( aaa=? and ( bbb=? or ccc=? ))"; //correct
	    	//String sql = "select * from test where ( aaa=? and ( bbb=? and ( ccc=? )))"; //correct
	    	String sql = "select * from test where ( aaa=? and ( bbb=? and ( ?=ccc )))"; //error !!!
	    	
	    	//String sql = "select * from test where ( aaa=? and ( bbb=? and (( ccc ) = ? )))"; //error !!!
	    	//String sql = "select * from test where ( aaa=? and ( bbb=? and ( convert ( varchar ( 20 ), ccc, 111 ) =? ) ) )"; //error !!!
	    	
	    	//NOTE: 已知會使 pstmt.getParameterMetaData() 拋錯的場合:
	    	//1. 括弧旁邊要有其他非括弧, 非空白的字元
	    	//2. 欄位名放在等號右側
	    	//3. where 條件裡含函數
	    	//4. ....
	    	//工具中最好儘量避免使用 pstmt.getParameterMetaData()
	    	
	    	conn = this.ds.getConnection();
	    	pstmt = conn.prepareStatement(sql);
	    	
	    	System.out.println("  " + pstmt.getParameterMetaData());
	    	System.out.println("  " + pstmt.getParameterMetaData().getParameterTypeName(1));
	    	System.out.println("  " + pstmt.getParameterMetaData().getParameterTypeName(2));
	    	System.out.println("  " + pstmt.getParameterMetaData().getParameterTypeName(3));
	    	
	    	pstmt.close();
	    	pstmt = null;
	    	conn.close();
	    	conn = null;
    	} finally {
    		if(rs != null) try { rs.close(); } catch(Throwable t) {}
    		if(pstmt != null) try { pstmt.close(); } catch(Throwable t) {}
    		if(conn != null) try { conn.close(); } catch(Throwable t) {}
    	}
    }

    public void testToString1() {
        System.out.println("testToString1...........................");
        
        System.out.println("expected: select a, b, c from spam where a=? and b is null and (c > ? or d < ?) and e is not null");
        SqlAssembler sql = new SqlAssembler("select a, b, c from spam").where(
                eq("a", "foo").and(eq("b", null)).and(gt("c", 1).or(lt("d", 100))).and(notEq("e", (String)null))
        );
        System.out.println(sql.toString());
        
        System.out.println("\nexpected: select a, b, c from spam where a=? and (c >? or d <?) and e is not null"); //and b=? 但 b 略過 null 值條件
        sql = new SqlAssembler("select a, b, c from spam").where(
                eq("a", "foo").and(eqNN("b", null)).and(gt("c", 1).or(lt("d", 100))).and(notEq("e", (String)null))
        );
        System.out.println(sql.toString());

        System.out.println("\nexpected: select a, b, c from spam where a=?"); //and b=? and (c > ? or (d < ? and f=?)) and e != ? //但略過所有 null 值條件
        sql = new SqlAssembler("select a, b, c from spam").where(
                eqNN("a", "foo").and(eqNN("b", null)).and(gtNN("c", null).or(ltNN("d", null).and(eqNN("f", null)))).and(notEqNN("e", (String)null))
        );
        System.out.println(sql.toString());
        
        System.out.println("\nexpected: select a, b, c from spam"); //where a=? and b=? and (c > ? or (d < ? and f=?)) and e != ? //但略過所有 null 值條件
        sql = new SqlAssembler("select a, b, c from spam").where(
                eqNN("a", null).and(eqNN("b", null)).and(gtNN("c", null).or(ltNN("d", null).and(eqNN("f", null)))).and(notEqNN("e", (String)null))
        );
        System.out.println(sql.toString());
        
        System.out.println("\nexpected: select a, b, c from spam where ((a=? or b is null) and c > ? and d < ?)");
        sql = new SqlAssembler("select a, b, c from spam").where(
                condition(eq("a", "foo").or(eq("b", null))).and(gtNN("c", 1)).and(ltNN("d", 100))
        );
        System.out.println(sql.toString());
        
        assertTrue(true);
    }

//    public void testToString2() {
//        System.out.println("testToString2...........................");
//        
//        SqlAssembler sql = new SqlAssembler().raw("select a, b, c from spam").where(
//                eq("a", args, "a").and(gt("b", args, "b")).and(lt("c", args, "c"))
//        );
//        System.out.println(sql.toString());
//        
//        assertEquals(3, sql.getValues().length);
//        assertEquals(args.get("a"), sql.getValues()[0]);
//        assertEquals(args.get("b"), sql.getValues()[1]);
//        assertEquals(args.get("c"), sql.getValues()[2]);
//        
//        sql = new SqlAssembler().raw("select a, b, c from spam").where(
//                eq("a", args1, "a").and(gt("b", args1, "b")).and(lt("c", args1, "c"))
//        );
//        System.out.println(sql.toString());
//        
//        assertEquals(3, sql.getValues().length);
//        assertEquals(args1.getA(), sql.getValues()[0]);
//        assertEquals(args1.getB(), sql.getValues()[1]);
//        assertEquals(args1.getC(), sql.getValues()[2]);
//    }
    
    public void testToString3() {
        System.out.println("testToString3...........................");
        
        //select a, b, c from spam where a=? and b >? and c <? and e is null
        System.out.println("check if \"e is null\" condition exist:");
        SqlAssembler sql = new SqlAssembler("select a, b, c from spam").where(
                eq("a", args.get("a")).and(gt("b", args.get("b"))).and(lt("c", args.get("c"))).and(eq("e", args.get("e")))
        );
        System.out.println(sql.toString());
        assertEquals(3, sql.getValues().length);
        assertEquals(args.get("a"), sql.getValues()[0]);
        assertEquals(args.get("b"), sql.getValues()[1]);
        assertEquals(args.get("c"), sql.getValues()[2]);

        System.out.println("\nexpected: select a, b, c from spam where a=? and b >? and c <?"); //and e=?; //e 略過 null 值條件
        sql = new SqlAssembler("select a, b, c from spam").where(
                eq("a", args.get("a")).and(gt("b", args.get("b"))).and(lt("c", args.get("c"))).and(eqNN("e", args.get("e")))
        );
        System.out.println(sql.toString());
        assertEquals(3, sql.getValues().length);
        assertEquals(args.get("a"), sql.getValues()[0]);
        assertEquals(args.get("b"), sql.getValues()[1]);
        assertEquals(args.get("c"), sql.getValues()[2]);
    }
    
    //測不存在的屬性, 及帶 function 的欄位
    public void testToString4() {
        System.out.println("testToString4...........................");

        System.out.println("expected: select a, b, c from spam where a=? and b >? and z < ? and to_char(c, 'yyyymmdd') !=?"); //z 略過 null 條件 
        SqlAssembler sql = new SqlAssembler("select a, b, c from spam").where(
                eq("a", args.get("a")).and(gt("b", args.get("b"))).and(ltNN("z", args.get("z"))).and(notEq("to_char(c, 'yyyymmdd')", args.get("c")))
        );
        System.out.println(sql.toString());
        assertEquals(3, sql.getValues().length);
        assertEquals(args.get("a"), sql.getValues()[0]);
        assertEquals(args.get("b"), sql.getValues()[1]);
        assertEquals(args.get("c"), sql.getValues()[2]);
        
        System.out.println("\nexpected: select a, b, c from spam where a=? and b >? and to_char(c, 'yyyymmdd') !=?");
        sql = new SqlAssembler("select a, b, c from spam").where(
                eq("a", args1.getA()).and(gt("b", args1.getB())).and(notEq("to_char(c, 'yyyymmdd')", args1.getC()))
        );
        System.out.println(sql.toString());
        assertEquals(3, sql.getValues().length);
        assertEquals(args1.getA(), sql.getValues()[0]);
        assertEquals(args1.getB(), sql.getValues()[1]);
        assertEquals(args1.getC(), sql.getValues()[2]);
    }
    
    //union
    public void testToString5() {
        System.out.println("testToString5...........................");

        System.out.println("expected: (select a from spam where a=? and b > ? and c < ?) union " +
        		"(select a from eggs where a=? and bb > ? and cc < ?)");
        SqlAssembler sql = new SqlAssembler("select a from spam").where(
                eq("a", args.get("a")).and(gt("b", args.get("b"))).and(lt("c", args.get("c")))
        ).union(new SqlAssembler("select a from eggs").where(
                eq("a", args.get("a")).and(gt("bb", args.get("b")).or(lt("cc", args.get("c"))))
        ));
        System.out.println(sql.toString());
        assertEquals(6, sql.getValues().length);
        assertEquals(args.get("a"), sql.getValues()[0]);
        assertEquals(args.get("b"), sql.getValues()[1]);
        assertEquals(args.get("c"), sql.getValues()[2]);
        assertEquals(args.get("a"), sql.getValues()[3]);
        assertEquals(args.get("b"), sql.getValues()[4]);
        assertEquals(args.get("c"), sql.getValues()[5]);
        
        System.out.println("\nexpected: (select a from span where a=? and b > ? and c < ?) union " +
        		"(select a from eggs where a=? and bb > ? and cc < ?)");
        sql = new SqlAssembler("select a from spam").where(
                eq("a", args1.getA()).and(gt("b", args1.getB())).and(lt("c", args1.getC()))
        ).union(new SqlAssembler("select a from eggs").where(
                eq("a", args1.getA()).and(gt("bb", args1.getB()).or(lt("cc", args1.getC())))
        ));
        System.out.println(sql.toString());
        assertEquals(6, sql.getValues().length);
        assertEquals(args1.getA(), sql.getValues()[0]);
        assertEquals(args1.getB(), sql.getValues()[1]);
        assertEquals(args1.getC(), sql.getValues()[2]);
        assertEquals(args1.getA(), sql.getValues()[3]);
        assertEquals(args1.getB(), sql.getValues()[4]);
        assertEquals(args1.getC(), sql.getValues()[5]);
        
        System.out.println("\nexpected: (select a from foo) union all " +
        		"(select a from bar) union all " +
        		"(select a from eggs)");
        sql = new SqlAssembler("select a from foo")
        	.unionAll(new SqlAssembler("select a from bar"))
        	.unionAll(new SqlAssembler("select a from eggs"));
        System.out.println(sql.toString());
    }
    
    //update
    public void testToString6() {
        System.out.println("testToString6...........................");
    
        System.out.println("expected: update spam set a=?, b=?, c=? where z is null and d=?");
        SqlAssembler sql = new SqlAssembler("update spam").set("a", args.get("a")).set("b", 10).set("c", args.get("c"))
            .where(eq("z", args.get("z")).and(eq("d", args.get("d"))));
        System.out.println(sql.toString());
        assertEquals(4, sql.getValues().length);
        assertEquals(args.get("a"), sql.getValues()[0]);
        assertEquals(10, sql.getValues()[1]);
        assertEquals(args.get("c"), sql.getValues()[2]);
        assertEquals(args.get("d"), sql.getValues()[3]);

        System.out.println("\nexpected: update spam set a=?, b=?, c=? where d=?");
        sql = new SqlAssembler("update spam").set("a", args1.getA()).set("b", 10).set("c", args1.getC())
            .where(eq("d", args1.getD()));
        System.out.println(sql.toString());
        assertEquals(4, sql.getValues().length);
        assertEquals(args1.getA(), sql.getValues()[0]);
        assertEquals(10, sql.getValues()[1]);
        assertEquals(args1.getC(), sql.getValues()[2]);
        assertEquals(args1.getD(), sql.getValues()[3]);
        
        System.out.println("\nexpected: update spam set a =?, c =? where d=?"); //b =?, b 略去 null 值不 update
        sql = new SqlAssembler("update spam").set("a", args1.getA()).setNN("b", null).set("c", null)
            .where(eq("d", args1.getD()));
        System.out.println(sql.toString());
        assertEquals(3, sql.getValues().length);
        assertEquals(args1.getA(), sql.getValues()[0]);
        assertNull(sql.getValues()[1]);
        assertEquals(args1.getD(), sql.getValues()[2]);
    }
    
    //sub-query
    public void testAssembleSQL7() {
        System.out.println("testAssembleSQL7...........................");
     
        System.out.println("expected: select a, b, c from spam where a=? and b > ? and z < ? and c in " +
        		"(select c from foo where to_char(d, 'yyyymmdd') between ? and ?)");
        SqlAssembler sql = new SqlAssembler("select a, b, c from spam").where(
                eq("a", args.get("a")).and(gt("b", args.get("b"))).and(ltNN("z", args.get("z")))
                .and(in("c", 
                        new SqlAssembler("select c from foo").where(between("to_char(d,'yyyymmdd')", "20000101", "20081231"))
                ))
        );
        System.out.println(sql.toString());
        assertEquals(4, sql.getValues().length);
        assertEquals(args.get("a"), sql.getValues()[0]);
        assertEquals(args.get("b"), sql.getValues()[1]);
        assertEquals("20000101", sql.getValues()[2]);
        assertEquals("20081231", sql.getValues()[3]);
    }
    
    //raw string
    public void testAssembleSQL8() {
        System.out.println("testAssembleSQL8...........................");
        
        System.out.println("expected: select a, b, c from spam where a > b and c between 1 and 100");
        SqlAssembler sql = new SqlAssembler("select a, b, c from spam").where(
                raws("a > b").and(raws("c between 1 and 100"))
        );
        System.out.println(sql.toString());
    }
    
    //含 alias 名的 SQL
    public void testAssembleSQL9() {
        System.out.println("testAssembleSQL9...........................");
    
        System.out.println("expected: select p.a, p.b, q.c from spam p, eggs q where p.a=? and p.b > ? and q.c < ?");
        SqlAssembler sql = new SqlAssembler("select p.a, p.b, q.c from spam p, eggs q").where(
                eq("p.a", args.get("a")).and(gt("p.b", args.get("b"))).and(lt("q.c", args.get("c")))
        );
        System.out.println(sql.toString());
        assertEquals(3, sql.getValues().length);
        assertEquals(args.get("a"), sql.getValues()[0]);
        assertEquals(args.get("b"), sql.getValues()[1]);
        assertEquals(args.get("c"), sql.getValues()[2]);
        
        System.out.println("\n expected: select p.a, p.b, q.c from spam p, eggs q where p.a=? and p.b > ? and q.c < ?");
        sql = new SqlAssembler("select p.a, p.b, q.c from spam p, eggs q").where(
                eq("p.a", args1.getA()).and(gt("p.b", args1.getB())).and(lt("q.c", args1.getC()))
        );
        System.out.println(sql.toString());
        assertEquals(3, sql.getValues().length);
        assertEquals(args1.getA(), sql.getValues()[0]);
        assertEquals(args1.getB(), sql.getValues()[1]);
        assertEquals(args1.getC(), sql.getValues()[2]);
    }
    
    //人工檢查是否無 ? 佔位符號條件之前會多出 "and" 字樣
    public void testAssembleSQL10() {
        System.out.println("testAssembleSQL10...........................");
        
        System.out.println("expected: select p.a, p.b, q.c from spam p, eggs q where p.b is not null");
        SqlAssembler sql = new SqlAssembler("select p.a, p.b, q.c from spam p, eggs q").where(
                raws("p.b is not null")
        );
        System.out.println("check if not-necessary \"and\" string exist:");
        System.out.println(sql.toString());
        
        System.out.println("\nexpected: select p.a, p.b, q.c from spam p, eggs q where (p.a > ? or p.b != ?) and p.b is not null");
        sql = new SqlAssembler("select p.a, p.b, q.c from spam p, eggs q").where(
                condition("p.a > ? or p.b != ?", args.get("a"), args.get("b"))
                .and(condition("p.b is not null"))
        );
        System.out.println(sql.toString());
    }
    
    //特殊 SQL
    public void testAssembleSQL11() {
        System.out.println("testAssembleSQL11...........................");
        
        String name = "foo";
        Long system = 1L;
        System.out.println("expected: select a.id_, a.description_, c.sysid, c.sysname, d.name_, d.version_ from jbpm_processdefinition a join " +
        		"(select name_, max(version_) as version_ from jbpm_processdefinition where name_=? group by name_) " +
        		"as d on a.name_=d.name_ and a.version_=d.version_ left outer join tf_processdefinition b on a.id_=b.id_ left outer join tf_system c on b.system=c.id_ " +
        		"where b.system=?");
        SqlAssembler sql = new SqlAssembler("select a.id_, a.description_, c.sysid, c.sysname, d.name_, d.version_ from jbpm_processdefinition a join")
            .subquery(new SqlAssembler("select name_, max(version_) as version_ from jbpm_processdefinition").where(eqNN("name_", name)).raw("group by name_"))
            .raw("as d on a.name_=d.name_ and a.version_=d.version_ left outer join tf_processdefinition b on a.id_=b.id_ left outer join tf_system c on b.system=c.id_")
            .where(eqNN("b.system", system));
        System.out.println("check if the generated SQL statement correct:");
        System.out.println(sql.toString());
    }
    
    //特殊 SQL
    public void testAssembleSQL12() {
        System.out.println("testAssembleSQL12...........................");
        
        String name = null;
        Long system = null;
        SqlAssembler sql = new SqlAssembler("select a.id_, a.description_, c.sysid, c.sysname, d.name_, d.version_ from jbpm_processdefinition a join")
            .subquery(new SqlAssembler("select name_, max(version_) as version_ from jbpm_processdefinition").where(eqNN("name_", name)).raw("group by name_"))
            .raw("as d on a.name_=d.name_ and a.version_=d.version_ left outer join tf_processdefinition b on a.id_=b.id_ left outer join tf_system c on b.system=c.id_")
            .where(eqNN("b.system", system));
        System.out.println("check if the generated SQL statement correct:");
        System.out.println(sql.toString());
    }
    
    //特殊 SQL
    public void testAssembleSQL13() {
        System.out.println("testAssembleSQL13...........................");
        
        Map<String, Object> args = new HashMap<String, Object>();
        SqlAssembler sql = new SqlAssembler("select * from abc").where(
                eq("col1", args.get("prop1"))
                .and(eq("col2", args.get("prop2")))
                .and(eq("col3", args.get("prop3")))
                .and(raws("col4='aaa'"))
        );
        System.out.println(sql.toString());
    }
    
    //exists, not exists
    public void testAssembleSQL14() {
    	System.out.println("testAssembleSQL14...........................");
    	
    	SqlAssembler sql = new SqlAssembler("select * from abc x").where(
    			exists(
    					new SqlAssembler("select * from def y").where(eq("x.c", args1.getC()).and(condition("x.e=y.f")))
					)
			);
    	System.out.println(sql.toString());
    	
    	sql = new SqlAssembler("select * from abc").where(
    			eq("a", args1.getA())
    			.and(exists(
    					new SqlAssembler("select * from def").where(eq("c", args1.getC()))
					))
			);
    	System.out.println(sql.toString());
    }
    
    public void testAssembleSQL15() {
    	System.out.println("testAssembleSQL15...........................");
    	
    	System.out.println("expected: select a, ?, ? from spam where b=? and c > ? and d < ? and e is null");
    	SqlAssembler sql = new SqlAssembler("select a, ?, ? from spam", 123, "321").where(
    			eq("b", "值一").and(gt("c", "值二")).and(lt("d", "值三")).and(eq("e", null))
			);
    	System.out.println(sql.toString());
    	
    	System.out.println("\nexpected: select a, ?, ? from spam where b=? and c in (select m from foo where n=? and o is null)");
    	sql = new SqlAssembler("select a, ?, ? from spam", 123, 321).where(
    			eq("b", "值一").and(in("c",
    				new SqlAssembler("select m from foo").where(
    						eq("n", "值二").and(eq("o", null))
						)
				))
			);
    	System.out.println(sql.toString());
    }
    
    public void testCombine() {
    	System.out.println("testCombine...........................");
    	
    	SqlAssembler sql1 = new SqlAssembler("select * from spam");
    	SqlAssembler sql2 = new SqlAssembler().where(eqNN("a", args1.getA()).and(eqNN("b", args1.getB())).and(eqNN("c", args1.getC())));
    	System.out.println(sql1.combine(sql2));
    }
    
    public void testLike() {
    	System.out.println("testLike...........................");
    	SqlAssembler sql = new SqlAssembler("select * from abc").where(
    			eqNN("a", args1.getA())
    			.and(condition("b like 'zzz%'"))
    			.and(eq("c", args1.getC()).or(eq("d", args1.getD())))
		);
    	System.out.println(sql.toString());
    }
    
    public void testLike2() {
    	System.out.println("testLike2...........................");
    	int col1 = 2;
    	String col2 = "test%";
    	
    	SqlAssembler sql = new SqlAssembler("select * from test").where(
    			eqNN("col1", col1)
    			.and(conditionNN("col2 like ?", col2))
		);
    	System.out.println(sql.toString());
    	
    	//ApplicationContext springContext = new FileSystemXmlApplicationContext(new String[] { 
        //        "web/WEB-INF/spring-main.xml", "web/WEB-INF/spring-remoting.xml" });
        //JdbcTemplate jdbcTemplate = (JdbcTemplate)springContext.getBean("jdbcTemplate");
    	//jdbcTemplate.query(sql.getSql(), sql.getValues(), new RowCallbackHandler() {
		//	@Override
		//	public void processRow(ResultSet rs) throws SQLException {
		//		System.out.println("=>col1=" + rs.getInt("col1") + ", col2=" + rs.getString("col2"));
		//	}
		//});
    }
    
    public void testLike3() {
    	System.out.println("testLike3...........................");
    	SqlAssembler sql = new SqlAssembler("select * from abc").where(
    			eqNN("a", args1.getA())
    			.and(eq("b", args1.getB()))
    			.and(eqNN("c", args1.getC()).or(eqNN("d", args1.getD())))
    			.and(likeNN("f", args1.getF()))
		);
    	System.out.println(sql.toString());
    }
    
    public void testIn() {
    	System.out.println("testIn...........................");
    	String[] in1 = { "aaa", "bbb" };
    	Integer[] in2 = { 1, 3, 5 };
    	String[] in3 = {};
    	String [] in4 = null;
    	SqlAssembler sql = new SqlAssembler("select * from test_table").where(
    			in("a", (Object[])in1).and(in("b", (Object[])in2)).and(in("c", (Object[])in3)).and(in("d", (Object[])in4))
			);
    	System.out.println(sql.toString());
    	
    	sql = new SqlAssembler("select * from test_table").where(
    			inNN("a", (Object[])in1).and(inNN("b", (Object[])in2)).and(inNN("c", (Object[])in3)).and(inNN("d", (Object[])in4))
			);
    	System.out.println(sql.toString());
    	
    	sql = new SqlAssembler("select * from test_table").where(
    			in("a", new SqlAssembler("select m from test_table2"))
			);
    	System.out.println(sql.toString());
    }
    
    public void testSubquery() {
    	System.out.println("testSubquery()...........................");
    	String[] deviceIds = { "xyz", "123", "123.456.xxx" };
    	SqlAssembler sql = new SqlAssembler("select a.*, b.display_name as section_name, c.display_name as floor_name, " +
				"d.display_name as building_name from")
			.subquery(new SqlAssembler("select a.*, b.parent_seq as building_seq from")
				.subquery(new SqlAssembler("select a.*, b.parent_seq as floor_seq from")
					.subquery(new SqlAssembler("select a.pk_device_id, " +
						"(select a1.parent_seq from items_org_chart a1 left join items_org_chart_device b1 on a1.pk_chart_seq=b1.fk_chart_seq " +
						"where b1.fk_device_id=a.pk_device_id) as section_seq from items_device_info a")
						.where(in("pk_device_id", (Object[])deviceIds)))
					.raw("a left join items_org_chart b on a.section_seq=b.pk_chart_seq"))
				.raw("a left join items_org_chart b on a.floor_seq=b.pk_chart_seq"))
			.raw("a left join items_org_chart_building b on a.section_seq=b.fk_chart_seq " +
					"left join items_org_chart_building c on a.floor_seq=c.fk_chart_seq " +
					"left join items_org_chart_building d on a.building_seq=d.fk_chart_seq");
    	System.out.println(sql.toString());
    }
    
    public void testSubquery2() {
    	System.out.println("testSubquery2()...........................");
    	SqlAssembler sql = new SqlAssembler("select distinct a.*,")
			.subquery(new SqlAssembler("select sum(month_fee) from wt_project_fee_control").where(
					condition("fk_project=a.id_ and type=?", 0)
			)).raw("from wt_project a, wt_rule b")
			.where(
					condition("a.xxx=b.zzz and b.id_=?", 1)
			);
    	System.out.println("sql=" + sql);
    }
    
    public void testCondition() {
    	System.out.println("testCondition()...........................");
    	SqlAssembler sql = new SqlAssembler("select * from foo").where(
    			condition(
    				condition(eq("col1", 1).and(eq("col2", 2))
					).or(eq("col3", 3).and(eq("col4", 4)))
				).and(condition("col5=?", 5))
			);
    	System.out.println("sql=" + sql);
    }
    
    public void testCondition2() {
    	System.out.println("testCondition2()...........................");
    	SqlAssembler sql = new SqlAssembler("select * from foo").where(
    			eq("col1", "1")
    			.and(condition("col2 is not null"))
    			.and(lt("col3", 3))
			);
    	SqlAssembler sql2 = new SqlAssembler("select * from foo").where(
    			eq("col1", "1")
    			.and("col2 is not null") //簡化只含純字串關係式的 and(...), or(...)
    			.and(lt("col3", 3))
			);
    	System.out.println("sql=" + sql);
    	System.out.println("sql2=" + sql2);
    	assertEquals(sql.toString(), sql2.toString());
    }
    
    public void testCondition3() {
    	System.out.println("testCondition3()...........................");
    	SqlAssembler sql = new SqlAssembler("select * from foo").where(
    			eq("col1", "1")
    			.and(condition("col2 is not null and col2 != ?", 2))
    			.and(lt("col3", 3))
			);
    	SqlAssembler sql2 = new SqlAssembler("select * from foo").where(
    			eq("col1", "1")
    			.and("col2 is not null and col2 != ?", 2) //簡化只含純字串關係式的 and(...), or(...)
    			.and(lt("col3", 3))
			);
    	System.out.println("sql=" + sql);
    	System.out.println("sql2=" + sql2);
    	assertEquals(sql.toString(), sql2.toString());
    }
    
    public void testMisc() {
    	System.out.println("testMisc ...........................");
    	String[] deviceIds = { "aaa", "bbb" };
    	Date startDate = new Date();
    	Date endDate = new Date();
    	Integer period1 = 1;
    	Integer period2 = 96;
    	SqlAssembler sql = new SqlAssembler("select A.*,C.fk_pricingprogram_seq from items_meter_daily_96 A " +
    		    "join items_virtual_meter_detail B on A.fk_device_id=B.fk_device_id " +
    		    "join items_taipower_contract_capacity C on C.fk_virtual_meter_seq=B.fk_virtual_meter_seq").where(
    		    		in("A.fk_device_id", (Object[])deviceIds)
    		    		.and(gtEq("period_no", period1))
    		    		.and(ltEq("period_no", period2))
    		    		.and(condition("date(daily_date) >=?", startDate))
    		    		.and(condition("date(daily_date) <=?", endDate))
    		    );
    	System.out.println("sql=" + sql);
    }
    
    //寫成 private 的話, PropertyUtils 無法取取得 property values
    public class ArgsBean {
        private String a;
        private Integer b;
        private java.util.Date c;
        private java.util.Date d;
        private String e;
        private String f;
        
        public String getA() {
            return a;
        }
        
        public void setA(String a) {
            this.a = a;
        }
        
        public Integer getB() {
            return b;
        }
        
        public void setB(Integer b) {
            this.b = b;
        }
        
        public java.util.Date getC() {
            return c;
        }
        
        public void setC(java.util.Date c) {
            this.c = c;
        }
        
        public java.util.Date getD() {
            return d;
        }
        
        public void setD(java.util.Date d) {
            this.d = d;
        }
        
        public String getE() {
            return e;
        }

        public void setE(String e) {
            this.e = e;
        }

        public String getF() {
			return f;
		}

		public void setF(String f) {
			this.f = f;
		}

		@Override
		public String toString() {
			return "ArgsBean [a=" + a + ", b=" + b + ", c=" + c + ", d=" + d
					+ ", e=" + e + ", f=" + f + "]";
		}
    }
}
