package com.test.commons.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;

/**
 * 令所有標以 annotation Transactional 的 bean 改交給 RollbackForAllAnnotationTransactionAttributeSource 控制.
 * 本元件需登錄於 Spring 登錄檔中.
 * <p/>
 * 參考自: 
 * <ul>
 * 		<li>http://stackoverflow.com/questions/3701376/rollback-on-every-checked-exception-whenever-i-say-transactional
 * 		<li>http://stackoverflow.com/questions/8316747/using-custom-annotationtransactionattributesource-with-txannotation-driven
 * </ul>
 * @see #com.tatung.commons.spring.RollbackForAllAnnotationTransactionAttributeSource
 */
public class RollbackForAllBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
	private static final String ANNOTATION_TRANSACTION_ATTRIBUTE_SOURCE = RollbackForAllAnnotationTransactionAttributeSource.class.getName(); //extends AnnotationTransactionAttributeSource

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory factory) throws BeansException {
		String[] names = factory.getBeanNamesForType(AnnotationTransactionAttributeSource.class);

		for(String name : names) {
			BeanDefinition bd = factory.getBeanDefinition(name);
			bd.setBeanClassName(ANNOTATION_TRANSACTION_ATTRIBUTE_SOURCE);
		}
	}
}
