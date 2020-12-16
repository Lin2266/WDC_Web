package com.test.commons.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Action //base action annotation
public @interface Rest {
	/** HTTP GET method, 用於查詢 */
	String METHOD_GET = "GET";
	/** HTTP POST method, 用於新增 */
	String METHOD_POST = "POST";
	/** HTTP PUT method, 用於修改 */
	String METHOD_PUT = "PUT";
	/** HTTP DELETE method, 用於刪除 */
	String METHOD_DELETE = "DELETE";
	/** 不限 HTTP method */
	String METHOD_ANY = "any";
	
	/** resource URL 中代表 operation 的名字 */
	String name() default "";
	
	/** 
	 * HTTP request method. 可為: METHOD_GET, METHOD_POST, METHOD_PUT, METHOD_DELETE, 或 METHOD_ANY(不限方式, 預設)
	 * @see #METHOD_GET
	 * @see #METHOD_POST
	 * @see #METHOD_PUT
	 * @see #METHOD_DELETE
	 * @see #METHOD_ANY
	 */
	String method() default METHOD_ANY;
	
	/** 特別指定 response 的內容型態, 未指定者, 套用 AP 的總設定值. */
	String responseContentType() default "";
}
