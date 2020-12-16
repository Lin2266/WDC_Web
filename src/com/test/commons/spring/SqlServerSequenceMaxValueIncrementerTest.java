package com.test.commons.spring;

import static org.junit.Assert.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.sql.DataSource;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * 測試前要確定 spring context 中的 dataSource 物件指向 SQL Server DB
 */
public class SqlServerSequenceMaxValueIncrementerTest {
	public static final String SEQUENCE_NAME = "sqlserver_sequence_max_value_incrementer";
	
	private DataSource dataSource;

	@Before
	public void setUp() throws Exception {
		ApplicationContext springContext = new FileSystemXmlApplicationContext("web/WEB-INF/spring-main.xml");
		this.dataSource = (DataSource)springContext.getBean("dataSource");
	}

	@Test
	public void testGetSequenceQuery() throws Exception {
		System.out.println("testGetSequenceQuery()...");
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try {
			conn = this.dataSource.getConnection();
			
			//create sequence object
			String sql = "create sequence " + SEQUENCE_NAME + " start with 1";
			System.out.println("  sql=" + sql);
			pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;
			
			//testing 測試對象
			System.out.println("  testing getting next value...");
			final SqlServerSequenceMaxValueIncrementer incrementer = new SqlServerSequenceMaxValueIncrementer(this.dataSource, SEQUENCE_NAME);
			assertEquals(1, incrementer.nextLongValue());
			assertEquals(2, incrementer.nextLongValue());
			
			//drop sequence object
			sql = "drop sequence " + SEQUENCE_NAME;
			System.out.println("  sql=" + sql);
			pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;
		} finally {
			if(pstmt != null) try { pstmt.close(); } catch(Throwable t) {}
			if(conn != null) try { conn.close(); } catch(Throwable t) {}
		}
	}

}
