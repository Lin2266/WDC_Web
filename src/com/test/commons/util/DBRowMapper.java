package com.test.commons.util;

import org.springframework.jdbc.core.RowMapper;

/**
 * 搭配 DBUtil 的, 定義處理每筆 ResultSet 記錄如何設值至 T 物件的介面.
 * (模仿 org.springframework.jdbc.core.RowMapper)
 */
public interface DBRowMapper<T> extends RowMapper<T> {} //在使用 Spring framework 的本環境中, 直接繼承 org.springframework.jdbc.core.RowMapper 即是
