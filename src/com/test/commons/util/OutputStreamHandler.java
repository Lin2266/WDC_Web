package com.test.commons.util;

import java.io.IOException;
import java.io.OutputStream;

public interface OutputStreamHandler {
	/**
	 * @param out 資料輸出目的地
	 * @return 對 out 所輸出的資料長度(byte)
	 * @throws IOException
	 */
	long execute(OutputStream out) throws IOException;
}
