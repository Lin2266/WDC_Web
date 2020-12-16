package com.test.commons.spring;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.test.commons.util.DBRowMapper;
import com.test.commons.util.DBRowMappingPostProcessing;
import com.test.commons.util.internal.AbstractBeanRowMapper;

/**
 * 用來將 ResultSet 查詢結果的每一筆結合至對應的 Java Bean.
 * Java Bean 中的 field 或 property 至少要搭配 annotation: javax.persistence.Column 
 * 來標明該 field/property 所對應的 table 欄位, 
 * 如果 table 欄位名與 bean field/property 不同者(不計大小寫), 要加上 name 參數指定 table 欄位名, 如: @Column(name="...").
 * <p>
 * 同一 BeanRowMapper 可在不同次查詢(同一個 SQL)中重複使用(指呼叫使用 mapRow(ResultSet, int) 的場合), 
 * 但每次查詢完畢後需呼叫 reset() 才可再次使用.
 * <p>
 * 
 * @param <T> 裝載每一筆紀錄的 Java Bean class
 * @see javax.persistence.Column
 */
public class BeanRowMapper<T> extends AbstractBeanRowMapper<T> implements DBRowMapper<T> {
    /**
     * @param clazz 用來裝載每一筆 table 資料的 Java Bean
     */
    public BeanRowMapper(Class<?> clazz) {
    	init(clazz);
    }
    
    /**
     * @param clazz 用來裝載每一筆 table 資料的 Java Bean
     * @param postProcessing 定義每筆 clazz 物件被裝載資料完畢後, 欲接著後續處理的動作(例如修改物件中某些屬性值)
     */
    public BeanRowMapper(Class<?> clazz, DBRowMappingPostProcessing<T> postProcessing) {
    	init(clazz, postProcessing);
    }
    
    /**
     * 將單筆的查詢結果置入一個 bean 的屬性中
     * @param rs
     * @param rowNum 單筆資料所在的列數(無作用)
     * @return 裝載一筆資料的一個 Java Bean 物件
     */
    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        return mapRow(rs);
    }
}
