package com.test.commons.util;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface DBRowMappingPostProcessing<T> {
	void execute(ResultSet rs, T vo) throws SQLException;
}
