package com.onloupe.agent.metrics.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.onloupe.agent.metrics.SummaryFunction;

// TODO: Auto-generated Javadoc
/**
 * The Interface EventMetricValue.
 */
@Repeatable(EventMetricValues.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface EventMetricValue {
	
	/**
	 * Name.
	 *
	 * @return the string
	 */
	String name();
	
	/**
	 * Unit caption.
	 *
	 * @return the string
	 */
	String unitCaption() default "";
	
	/**
	 * Summary function.
	 *
	 * @return the summary function
	 */
	SummaryFunction summaryFunction();
	
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
	
	/**
	 * Default value.
	 *
	 * @return true, if successful
	 */
	boolean defaultValue() default false;
}
