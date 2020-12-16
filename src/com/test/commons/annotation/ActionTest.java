package com.test.commons.annotation;

import static org.junit.Assert.*;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Test;

public class ActionTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testAnnotations() throws Exception {
		Method method = ActionTest.class.getMethod("test", new Class<?>[0]);
		assertNotNull(method.getAnnotation(Ann2.class));
		assertNull(method.getAnnotation(Ann1.class));
		assertFalse(method.isAnnotationPresent(Ann1.class));
	}
	
	@Test
	public void testAnnotations1() throws Exception {
		Method test1 = ActionTest.class.getMethod("test1", new Class<?>[0]);
		assertTrue(test1.getAnnotation(Action.class).annotationType().isAnnotationPresent(Action.class));
		
		Method test2 = ActionTest.class.getMethod("test2", new Class<?>[0]);
		assertTrue(test2.getAnnotation(AjaxAction.class).annotationType().isAnnotationPresent(Action.class));
	}
	
	@Ann2
	public void test() {}
	
	@Action
	public void test1() {}
	
	@AjaxAction
	public void test2() {}

	@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Ann1 {}
	
	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Ann1
	public @interface Ann2 {}
}
