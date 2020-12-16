package com.test.commons.util;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class JspUtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testCreateUniqueTempRealDir() {
		System.out.println("testCreateUniqueTempRealDir(): " + JspUtil.createUniqueTempRealDir().getAbsolutePath());
	}

}
