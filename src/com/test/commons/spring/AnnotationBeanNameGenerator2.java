package com.test.commons.spring;

import java.util.*;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import com.test.commons.annotation.BackingBean;

/**
 * depend on: Spring framework
 */
public class AnnotationBeanNameGenerator2 extends AnnotationBeanNameGenerator {
    private static final String BACKING_BEAN_ANNOTATION_CLASSNAME = BackingBean.class.getName(); //有 name, path 屬性
    //private static final String SIMPLE_RESOURCE_ANNOTATION_CLASSNAME = SimpleResource.class.getName(); //for 自訂 RESTful bean annotation
    
    @Override
    public String generateBeanName(final BeanDefinition definition, final BeanDefinitionRegistry registry) {
        if(definition instanceof AnnotatedBeanDefinition) {
            String beanName = determineBeanNameFromAnnotation((AnnotatedBeanDefinition)definition, registry);
            if(StringUtils.hasText(beanName)) {
                //Explicit bean name found.
                return beanName;
            }
        }

        //Generate a unique default bean name.
        return buildDefaultBeanName(definition, registry);
    }

    /**
     * 自 AnnotationBeanNameGenerator.determineBeanNameFromAnnotation(AnnotatedBeanDefinition) 改寫而來.<br/>
     * 如果 annotation type 為 BackingBean 且具足 path 屬性, 以 path 屬性值為 bean alias name.
     * 無 path 屬性則使用預設的 value 屬性值 (value 屬性和 path 屬性不能同時指定)
     * @param annotatedDef the annotation-aware bean definition
     * @return the bean name, or <code>null</code> if none is found
     */
    protected String determineBeanNameFromAnnotation(final AnnotatedBeanDefinition annotatedDef, final BeanDefinitionRegistry registry) {
        AnnotationMetadata amd = annotatedDef.getMetadata();
        Set<String> types = amd.getAnnotationTypes();
        String beanName = null;
        
        for(String type : types) {
            //System.out.println("type=" + type);
        	//AnnotationAttributes attributes = AnnotationConfigUtils.attributesFor(amd, type); //AnnotationConfigUtils.attributesFor() 乃 default scope, 無法在此被呼叫
        	Map<String, Object> attributes = amd.getAnnotationAttributes(type);
        	
            if(attributes != null && isStereotypeWithNameValue(type, amd.getMetaAnnotationTypes(type), attributes)) { //Check whether the given annotation is a stereotype that is allowed to suggest a component name through its annotation value()
                String strValue = null;
                
                //在此插入對自訂 annotation 處理程序, 把被自訂 annotation 修飾的 bean 置入 Spring context(配以自訂的 id 及 alias)
                if(BACKING_BEAN_ANNOTATION_CLASSNAME.equals(type)) {
//                	String name = (String)attributes.get("name"); //用作 spring bean id
                	final String path = (String)attributes.get("path"); //用作 spring bean id
                	final String value = (String)attributes.get("value"); //視同 path
                	if((path == null || path.length() == 0) && (value == null || value.length() == 0))
                		throw new IllegalArgumentException("attribute 'path' or default attribute not specified");
                	if(path != null && path.length() != 0 && value != null && value.length() != 0)
                		throw new IllegalArgumentException("attribute 'path' or default attribute can not be specified simultaneously");
//                    if(name == null || name.length() == 0) //強制 backing-bean 要有 name 屬性
//                        throw new IllegalArgumentException("\"name\" attribute of the annotation " + BACKING_BEAN_ANNOTATION_CLASSNAME + " must not be empty (class=" + annotatedDef.getBeanClassName() + ")");
//                    if(path == null || path.length() == 0) //強制 backing-bean 要有 path 屬性
//                        throw new IllegalArgumentException("\"path\" attribute of the annotation " + BACKING_BEAN_ANNOTATION_CLASSNAME + " must not be empty (class=" + annotatedDef.getBeanClassName() + ")");
//                    registry.registerAlias(name, path); //以 path 為 alias name
                	
                    strValue = (path != null && path.length() != 0) ? path : value; //as bean name
                } else { //其他原 Spring 內建的 Annotation, 均取其 value 屬性值為 bean name (原來的取法)
                    Object value = attributes.get("value");
                    if(value instanceof String) {
                    	strValue = (String)value;
                    }
                }
                
                if(StringUtils.hasLength(strValue)) {
                    if(beanName != null && !strValue.equals(beanName)) {
                        throw new IllegalStateException("Stereotype annotations suggest inconsistent " +
                                "component names: '" + beanName + "' versus '" + strValue + "'");
                    }
                    beanName = strValue;
                }
            }
        }
        return beanName;
    }
}
