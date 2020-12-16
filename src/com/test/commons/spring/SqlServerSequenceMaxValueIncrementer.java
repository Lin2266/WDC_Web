package com.test.commons.spring;

import javax.sql.DataSource;
import org.springframework.jdbc.support.incrementer.AbstractSequenceMaxValueIncrementer;

/**
 * SQL Server 2012+ 已支援 sequence 物件, 但 Springframework 的 SqlServerMaxValueIncrementer 仍是基於 bigint identity 型態實作的.
 * 在 Springframework 官方基於 sequence 物件的 MaxValueIncrementer 實作出現之前, 暫以本 class 頂著用.
 * <p>
 * NOTE:
 * <ul>
 *   <li>建立 sequence 物件, 至少需明確指定起始值, 否則會由數值 type 的最小值起始(預設 type 為 bigint). 例: "create sequence xxx start with 1"
 *   <li>取新值的 SQL 語法: "select next value for xxx"
 * </ul>
 */
public class SqlServerSequenceMaxValueIncrementer extends AbstractSequenceMaxValueIncrementer {
	//以下仿 org.springframework.jdbc.support.incrementer.PostgreSQLSequenceMaxValueIncrementer 實作
	
	/**
	 * Default constructor for bean property style usage.
	 * @see AbstractDataFieldMaxValueIncrementer#setDataSource
	 * @see AbstractDataFieldMaxValueIncrementer#setIncrementerName
	 */
	public SqlServerSequenceMaxValueIncrementer() {}
	
	/**
	 * Convenience constructor.
	 * @param dataSource the DataSource to use
	 * @param incrementerName the name of the sequence/table to use
	 */
	public SqlServerSequenceMaxValueIncrementer(DataSource dataSource, String incrementerName) {
		super(dataSource, incrementerName);
	}
	
	@Override
	protected String getSequenceQuery() {
		return "select next value for " + getIncrementerName();
	}
}
