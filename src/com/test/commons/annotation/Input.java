package com.test.commons.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用來接收 request parameters 並與 Java Bean field/property/method parameters 的 annotation.
 * <ul>
 * <li>name 屬性有指定者, 以 name 屬性值為優先, 其次才取 value 屬性值.
 * <li>name/value 屬性有值者, 以該值對應為 request parameter name.
 * <li>name/value 屬性未指定或為空者, 以 property name 對應為 request parameter name.
 * <li>當標注於 method parameter 時, name/value 不能全無值, 因 method parameter 在 compile 為 Java byte-code 後, 
 *     parameter name 即告消失, 無法用來對應 request parameter.
 * </ul>
 * 又, 依標注位置的不同:
 * <ul>
 * <li>標注於 Bean field 時: 使用該 field 對應的 property "setter" method 設值.
 * <li>標注於 Bean property 時: 透過該 property "setter" 設值.
 * <li>標注於 Bean method parameter 時: 直接作為 method parameter 值傳入.
 * </ul>
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Input {
	/** 對 GET request 參數值進行轉碼處理時, 所使用的預設編碼. */
	static String DEFAULT_REQUEST_PARAMETER_ENCODING_FOR_GET = "UTF-8";
	
	/** AP Server 的 uri encoding 預設值. */
	static String DEFAULT_URI_ENCODING = "ISO-8859-1";
	
    String value() default ""; //原有的
    
    /** HTTP request 中的參數名 */
    String name() default "";
}
