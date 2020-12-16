package com.test.commons.util;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.support.incrementer.AbstractColumnMaxValueIncrementer;
import org.springframework.jdbc.support.incrementer.AbstractDataFieldMaxValueIncrementer;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;

/**
 * 供取得資料庫 table 之 sequence 欄位值的工具.<br>
 * 通常用在使用 JdbcTemplate 進行 SQL insert 時, 取得類 auto-increment 欄位最新值之用.
 * <p>
 * 使用本工具的條件:
 * <ol>
 * <li>全 AP 使用同一種資料庫. 例如跨 Oracle 與 Postgres 的情形, 本工具即不適用
 * <li>nextKey(String), nextKeyString(String) 只適用於預設資料庫(data source); 
 * 		取其他資料庫的 sequence 新值時, 須改用 nextKey(DataSource, String), nextKeyString(DataSource, String)
 * <li>本工具需登錄於 Spring context 中或在 AP 初始化階段預先 new instance, 
 * 		以便對本工具的 incrementerClassName(見下面的說明)及 dataSource, columnName 屬性設值
 * <li>incrementerClassName 屬性值可為:
 * 		<ul>
 * 			<li>Oracle: org.springframework.jdbc.support.incrementer.OracleSequenceMaxValueIncrementer
 * 			<li>PostgreSQL: org.springframework.jdbc.support.incrementer.PostgreSQLSequenceMaxValueIncrementer
 * 			<li>MySQL: org.springframework.jdbc.support.incrementer.MySQLMaxValueIncrementer
 * 			<li>DB2: org.springframework.jdbc.support.incrementer.DB2SequenceMaxValueIncrementer
 * 			<li>DB2 Mainframe: org.springframework.jdbc.support.incrementer.DB2MainframeSequenceMaxValueIncrementer
 * 			<li>Derby: org.springframework.jdbc.support.incrementer.DerbyMaxValueIncrementer
 * 			<li>H2: org.springframework.jdbc.support.incrementer.H2SequenceMaxValueIncrementer
 * 			<li>Hsql: org.springframework.jdbc.support.incrementer.HsqlMaxValueIncrementer
 * 			<li>SQL Server: org.springframework.jdbc.support.incrementer.SqlServerMaxValueIncrementer
 * 			<li>Sybase: org.springframework.jdbc.support.incrementer.SybaseMaxValueIncrementer
 * 		</ul>
 * </ol>
 * 
 * 在自訂類別中使用例:
 * <pre>
 *   DataFieldMaxValueIncrementer incrementer =  //自預設 data source 取
 * 		DBSequenceUtil.getIncrementer("my_incrementer");
 *   long newId = DBSequenceUtil.nextKey();
 *   //或 String newId = DBSequenceUtil.nextKeyString();
 * </pre>
 * 或者明確指定取值的來源 data source:
 * <pre>
 *   {@literal @}Resource(name="dataSourceV2")
 *   private DataSource dataSourceV2; //以注入方式設置 data source
 *   ...
 *   DataFieldMaxValueIncrementer incrementer =
 * 		DBSequenceUtil.getIncrementer(dataSourceV2, "my_incrementer");
 *   long newId = DBSequenceUtil.nextKey();
 * </pre>
 * 
 * 相當於人工執行如下類似的 SQL 指令(以 Oracle 為例):
 * <pre>
 *   (已執行過 SQL 指令: create sequence mytable_sequence;)
 *   select mytable_sequence.nextval from dual;
 * </pre>
 * <p>
 * depend on: Spring framework
 */
public class DBSequenceUtil implements ApplicationContextAware {
    private static final Logger log = LoggerFactory.getLogger(DBSequenceUtil.class);
    private static ApplicationContext _applicationContext;
    private static DataSource _dataSource;
    private static Class<AbstractDataFieldMaxValueIncrementer> _incrementerClass;
    private static String _columnName; //sequence table 裡的欄位名

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        _applicationContext = applicationContext;
    }
    
    /** 指定 data source(只能設定一次). */
    public void setDataSource(DataSource dataSource) {
        if(_dataSource == null)
            _dataSource = dataSource;
        else
        	log.warn("the property 'dataSource' can only be set once, ignore setting");
    }
    
    /** 指定 DataFieldMaxValueIncrementer 的實作類別之全名(只能設定一次). */
    @SuppressWarnings("unchecked")
    public void setIncrementerClassName(String incrementerClassName) {
        try {
            if(_incrementerClass == null)
                _incrementerClass = (Class<AbstractDataFieldMaxValueIncrementer>)Class.forName(incrementerClassName);
            else
            	log.warn("the property 'incrementerClassName' can only be set once, ignore setting");
        } catch(ClassNotFoundException ce) {
            throw new RuntimeException(ce);
        }
    }
    
    /** 指定 sequence table 裡的欄位名, 針對不提供 sequence 物件的資料庫(如 SQL Server 之類, 只能設定一次) */
    public void setColumnName(String columnName) {
        if(_columnName == null)
            _columnName = columnName;
        else
        	log.warn("the property 'columnName' can only be set once, ignore setting");
    }
    
    /**
     * @param dataSource
     * @param incrementerName Name the name of the sequence/table to use
     */
    protected static DataFieldMaxValueIncrementer getIncrementer(DataSource dataSource, String incrementerName) {
        try {
        	if(_incrementerClass == null) {
        		initInSpringContext(); //嘗試叫起 Spring context 中的本元件
        		if(_incrementerClass == null)
        			throw new RuntimeException("the DBSequenceUtil component's property 'incrementerClassName' not specified properly");
        	}
        	
            AbstractDataFieldMaxValueIncrementer o = (AbstractDataFieldMaxValueIncrementer)_incrementerClass.newInstance();
            o.setDataSource(dataSource);
            o.setIncrementerName(incrementerName);
            if(_columnName != null && AbstractColumnMaxValueIncrementer.class.isAssignableFrom(_incrementerClass)) //例: SQL Server
                ((AbstractColumnMaxValueIncrementer)o).setColumnName(_columnName);
            return o;
        } catch(InstantiationException ie) {
            throw new RuntimeException(ie);
        } catch(IllegalAccessException ie) {
            throw new RuntimeException(ie);
        }
    }
    
    /**
     * 自預設 dataSource 取 sequence 新值來源.
     * @param incrementerName Name the name of the sequence/table to use
     */
    protected static DataFieldMaxValueIncrementer getIncrementer(String incrementerName) {
    	return getIncrementer(getDefaultDataSource(), incrementerName);
    }
    
    protected static DataSource getDefaultDataSource() {
    	if(_dataSource == null) {
    		initInSpringContext(); //嘗試叫起 Spring context 中的本元件
	    	if(_dataSource == null)
	    		throw new RuntimeException("the DBSequenceUtil component's property 'dataSource' not specified properly");
    	}
    	return _dataSource;
    }
    
    /**
     * 當呼叫 getDataSource() 取不到 dataSource 物件時, 嘗試從 Spring context "喚醒" 已登錄在 Spring context 中的 dataSource 元件.
     */
    protected static boolean initInSpringContext() {
    	try {
	    	if(_applicationContext == null)
	    		throw new IllegalStateException("DBSequenceUtil not yet registered in Spring context or not registered as: lazy-init=\"false\"");
	    	_applicationContext.getBean(DBSequenceUtil.class);
	    	return true;
    	} catch(NoSuchBeanDefinitionException e) {
    		return false;
    	}
    }
    
    /**
     * 取下一 sequence 值.
     * @param dataSource
     * @param sequenceName sequence 物件/table 名
     */
    public static long nextKey(DataSource dataSource, String sequenceName) {
        long v = getIncrementer(dataSource, sequenceName).nextLongValue();
        if(log.isDebugEnabled())
        	log.debug("new sequence value of " + sequenceName + " = " + v);
        return v;
    }
    
    /**
     * 自預設 data source 取新 sequence 值.
     * @param sequenceName sequence 物件/table 名
     */
    public static long nextKey(String sequenceName) {
        long v = getIncrementer(sequenceName).nextLongValue();
        if(log.isDebugEnabled())
        	log.debug("new sequence value of " + sequenceName + " = " + v);
        return v;
    }
    
    /**
     * 取下一 sequence 值.
     * @param sequenceName sequence 物件/table 名
     */
    public static String nextKeyString(DataSource dataSource, String sequenceName) {
        String v = getIncrementer(dataSource, sequenceName).nextStringValue();
        if(log.isDebugEnabled())
        	log.debug("new sequence value of " + sequenceName + " = " + v);
        return v;
    }
    
    /**
     * 自預設 data source 取新 sequence 值.
     * @param sequenceName sequence 物件/table 名
     */
    public static String nextKeyString(String sequenceName) {
        String v = getIncrementer(sequenceName).nextStringValue();
        if(log.isDebugEnabled())
        	log.debug("new sequence value of " + sequenceName + " = " + v);
        return v;
    }
}
