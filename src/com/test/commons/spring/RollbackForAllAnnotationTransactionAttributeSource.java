package com.test.commons.spring;

import java.lang.reflect.AnnotatedElement;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.DelegatingTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;

/**
 * 用以取代 Spring 內建的 AnnotationTransactionAttributeSource 元件, 
 * 使 annotation Transactional 也能作用於非 checked exception.
 * <p/>
 * 參考自: 
 * <ul>
 * 		<li>http://stackoverflow.com/questions/3701376/rollback-on-every-checked-exception-whenever-i-say-transactional
 * 		<li>http://stackoverflow.com/questions/8316747/using-custom-annotationtransactionattributesource-with-txannotation-driven
 * </ul>
 */
@SuppressWarnings("serial")
public class RollbackForAllAnnotationTransactionAttributeSource extends AnnotationTransactionAttributeSource {
	
	@Override
	protected TransactionAttribute determineTransactionAttribute(AnnotatedElement ae) {
		TransactionAttribute target = super.determineTransactionAttribute(ae);
		if(target == null) {
			return null;
		} else {
			return new DelegatingTransactionAttribute(target) {
				@Override
				public boolean rollbackOn(Throwable ex) {
					return true;
				}
			};
		}
	}
}
