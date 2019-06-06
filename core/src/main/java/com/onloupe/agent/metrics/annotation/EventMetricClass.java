package com.onloupe.agent.metrics.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * The Interface EventMetricClass.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface EventMetricClass {
	
	/**
	 * Namespace.
	 *
	 * @return the string
	 */
	String namespace();
	
	/**
	 * Category name.
	 *
	 * @return the string
	 */
	String categoryName();
	
	/**
	 * Counter name.
	 *
	 * @return the string
	 */
	String counterName();
	
	/**
	 * Caption.
	 *
	 * @return the string
	 */
	String caption();
	
	/**
	 * Description.
	 *
	 * @return the string
	 */
	String description();
}
