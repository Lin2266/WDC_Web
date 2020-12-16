package com.test.commons.spring;

import java.lang.reflect.*;
import java.util.regex.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.util.ReflectionUtils;

/**
 * 擴充 Spring PropertySourcesPlaceholderConfigurer 元件功能, 
 * 除原 <code>org.springframework.context.support.PropertySourcesPlaceholderConfigurer</code> 功能以外,
 * 也能將內含的 properties 值以 getProperty() 等 method 取出.
 * <p>
 * 參考: <code>http://www.javaeye.com/topic/180941 (但本工具已不用於替 bean field/property 設值了)</code>
 * <br>
 * depend on: Spring framework
 * 
 * @author ahuaxuan(aaron zhang) 
 * @since 2008/04/07 
 */
public class AnnotationSupportedPropertyPlaceholderConfigurer
        extends PropertySourcesPlaceholderConfigurer 
        implements InitializingBean { //, BeanPostProcessorInitializingBean {
    private java.util.Properties pros; //指向 PropertySourcesPlaceholderConfigurer 內部所存的 properties 值以便能以程式方式讀出
    //private final Class<String> enableClass = String.class; //暫只容許把設定值注入 String field/property
    //private Class<?>[] enableClassList = { String.class };
    
    //public void setEnableClassList(Class<?>[] enableClassList) {
    //    this.enableClassList = enableClassList;
    //}

	//NOTE: 以下本來搭配自訂 annotation @Properties 用來設定 properties files 設定值給 bean field/property 之用, 現已由 @Value("${...}") 機制取代
    //@Override
    //public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    //    return bean;
    //}

    //NOTE: 以下本來搭配自訂 annotation @Properties 用來設定 properties files 設定值給 bean field/property 之用, 現已由 @Value("${...}") 機制取代
    //@Override
    //public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    //    //assign field value
    //    for(Field field : bean.getClass().getDeclaredFields()) {
    //        if(field.isAnnotationPresent(Properties.class)) {
    //            if(filterType(field.getType())) {
    //                Properties p = field.getAnnotation(Properties.class);
    //                
    //                try {
    //                    ReflectionUtils.makeAccessible(field);
    //                    field.set(bean, this.pros.getProperty(p.name()));
    //                } catch(Exception e) {
    //                    log.error(e.getMessage(), e);
    //                }
    //            }
    //        }
    //    }
    //    
    //    //assign property value
    //    for(Method method : bean.getClass().getMethods()) {
    //        if(method.isAnnotationPresent(Properties.class)) {
    //            Class<?>[] paramTypes = method.getParameterTypes();
    //            if(paramTypes.length == 1 && filterType(paramTypes[0])) {
    //                Properties p = method.getAnnotation(Properties.class);
    //                
    //                try {
    //                    method.invoke(bean, new Object[] { this.pros.getProperty(p.name()) });
    //                } catch(Exception e) {
    //                    log.error(e.getMessage(), e);
    //                }
    //            }
    //        }
    //    }
    //    
    //    return bean;
    //}

    @Override
    public void afterPropertiesSet() throws Exception {
        this.pros = this.mergeProperties();
    }
    
    //private boolean filterType(Class<?> type) {
    //    //if(type != null) {
    //    //    for(Class c : this.enableClassList) {
    //    //        if(c.equals(type))
    //    //            return true;
    //    //    }
    //    //    return false;
    //    //} else {
    //    //    return true;
    //    //}
    //    return (type == this.enableClass);
    //}

    /**
     * 取得由本元件讀取的 property 檔中的設定值
     * @param key
     * @return
     */
    public String getProperty(String key) {
        if(this.pros == null)
            return null;
        return this.pros.getProperty(key);
    }

    /**
     * 按 key 的樣式來取得符合樣式的設定資料的烤貝
     * @param keyPattern
     * @return
     */
    public java.util.Properties getPropertiesByPattern(String keyPattern) {
        java.util.Properties p = new java.util.Properties();
        Matcher matcher = Pattern.compile(keyPattern).matcher("");
        for(String key : this.pros.stringPropertyNames()) {
            if(matcher.reset(key).matches())
                p.setProperty(key, this.pros.getProperty(key));
        }
        return p;
    }
    
    /**
     * 按 key 之前置字串相同者, 取出符合的每筆設定.
     * @param prefix
     * @return
     */
    public java.util.Properties getPropertiesByKeyPrefix(String prefix) {
    	java.util.Properties p = new java.util.Properties();
    	for(String key : this.pros.stringPropertyNames()) {
    		if(key.startsWith(prefix))
    			p.setProperty(key, this.pros.getProperty(key));
    	}
    	return p;
    }
    
    @Override
    public String toString() {
        if(this.pros == null)
            return "null";
        return this.pros.toString();
    }
}
