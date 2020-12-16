package com.test.commons.spring;

import java.lang.annotation.Annotation;
import java.util.*;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopeMetadata;
import org.springframework.context.annotation.ScopeMetadataResolver;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;

import com.test.commons.annotation.BackingBean;

/**
 * 由 Spring AnnotationScopeMetadataResolver 改寫而來, 以便能處理 BackingBean annotation 的 scope. 
 * (class: org.springframework.context.annotation.AnnotationScopeMetadataResolver).<br/>
 * 
 * 由本 framework 所控制 scope 可有如下種類:<ul>
 * <li>singleton: 在 spring context 持續存在的期間, 只有一個 bean instance 存在且不會消失
 * <li>prototype: 每自 spring context 取 bean 時, 即創建一新的 bean instance, 執行完畢後, spring 不特別留存之
 * <li>reqeust: 相當於 servlet request scope (for spring web context)
 * <li>session: 相當於 servlet session scope (for spring web context)
 * </ul>
 * <p>
 * depend on: Spring framework
 * 
 * @author Mark Fisher
 * @author Juergen Hoeller
 */
public class AnnotationScopeMetadataResolver2 implements ScopeMetadataResolver {
    private final String backingBeanAnnotationType = BackingBean.class.getName(); //自定義 annotation
    private final ScopedProxyMode defaultProxyMode;
    private Class<? extends Annotation> scopeAnnotationType = Scope.class; //Spring 內建

    /**
     * Create a new instance of the <code>BackingBeanScopeResolver</code> class.
     * @see #BackingBeanScopeResolver(ScopedProxyMode)
     * @see ScopedProxyMode#NO
     */
    public AnnotationScopeMetadataResolver2() {
        this.defaultProxyMode = ScopedProxyMode.NO;
    }
    
    /**
     * Create a new instance of the <code>AnnotationScopeMetadataResolver</code> class.
     * @param scopedProxyMode the desired scoped-proxy mode
     */
    public AnnotationScopeMetadataResolver2(ScopedProxyMode defaultProxyMode) {
        Assert.notNull(defaultProxyMode, "'defaultProxyMode' must not be null");
        this.defaultProxyMode = defaultProxyMode;
    }
    
    /**
     * Set the type of annotation that is checked for by this
     * {@link AnnotationScopeMetadataResolver}.
     * @param scopeAnnotationType the target annotation type
     */
    public void setScopeAnnotationType(Class<? extends Annotation> scopeAnnotationType) {
        Assert.notNull(scopeAnnotationType, "'scopeAnnotationType' must not be null");
        this.scopeAnnotationType = scopeAnnotationType;
    }
    
    @Override
    public ScopeMetadata resolveScopeMetadata(final BeanDefinition definition) {
        final ScopeMetadata metadata = new ScopeMetadata();
        if(definition instanceof AnnotatedBeanDefinition) {
            final AnnotatedBeanDefinition annDef = (AnnotatedBeanDefinition) definition;
            final AnnotationMetadata amd = annDef.getMetadata();
            //AnnotationAttributes attributes = AnnotationConfigUtils.attributesFor(annDef.getMetadata(), this.scopeAnnotationType); //AnnotationConfigUtils.attributesFor() 是 default scope, 無法在此直接呼叫
            
            //取 Spring 內建的 @Scope
            Map<String, Object> attributes = amd.getAnnotationAttributes(this.scopeAnnotationType.getName()); //@Scope
            
            if(attributes != null) { //遇 Spring 內建的 annotation 修飾的 Bean (以 @Scope annotation 為優先考慮)
            	String scope = (String)attributes.get("value");
                
                metadata.setScopeName(scope);
                ScopedProxyMode proxyMode = (ScopedProxyMode)attributes.get("proxyMode"); //例如: 遇含 @Transaction 的 Spring bean, 要動態建立 proxy class
                
                if(proxyMode == null || proxyMode == ScopedProxyMode.DEFAULT || //明訂不建立 proxy class 者
                		"request".equals(scope) || "session".equals(scope)) { //customized: 屬於 request/session scope 的 bean 仍強制不用 proxy(假設不會有 default Spring bean 反過來呼叫 request/session scoped Spring bean 的情形)
                    proxyMode = this.defaultProxyMode;
                }
                metadata.setScopedProxyMode(proxyMode);
            } else if((attributes = amd.getAnnotationAttributes(this.backingBeanAnnotationType)) != null) { //遇自定義的 @BackingBean
                metadata.setScopeName("singleton");
                
                //metadata.setScopedProxyMode(ScopedProxyMode.TARGET_CLASS); //Spring 3+ 會替 request/session scope bean 產生 proxy class (需配合 CGlib)
                metadata.setScopedProxyMode(ScopedProxyMode.NO); //不需考慮在一般 Spring bean 裡注入以 @BackingBean 修飾的 backing bean (scope=request/session)的場合(不需產生 proxy class)
            }
        }
        return metadata;
    }
}
