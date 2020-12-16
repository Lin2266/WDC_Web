package com.test.commons.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用以標明 View 層之 backing bean 中的預設 action method.
 * 即 JSP 網址中未指定 action method 參數時, 系統將自動執行由本 annotation 標注的 method, 
 * 但不會發生導頁行為, 且同一 backing bean 最多只能有一個本 annotation 標注的 method.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Action //base action annotation
public @interface DefaultAction {}
