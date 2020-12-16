package com.test.commons.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.stereotype.Component;

/**
 * 用來標明 View 層之 backing bean (code-behind 模式), 做為 Spring(或其他 framework) context 裡的 managed bean.
 *
 * <p>This annotation serves as a specialization of {@link Component @Component},
 * allowing for implementation classes to be autodetected through classpath scanning.
 *
 * @since 2007/11/15
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Component
public @interface BackingBean {
	/**
     * 同 {@link #path()} (無此 method 者, 無法被 Spring 接受).
     */
    String value() default "";
	
    /** backing bean 所對應的 URI 去結尾的 "&#46;jsp" 或 "&#46;ajax" 等. */
    String path() default "";
}
