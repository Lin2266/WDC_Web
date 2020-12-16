package com.test.commons.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用以標明 View 層之 backing bean 中的 ajax 專用 action method.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Action //base action annotation
public @interface AjaxAction {
	/** 特別指定 response 的內容型態, 未指定者, 套用 AP 的總設定值. */
	String responseContentType() default "";
}
