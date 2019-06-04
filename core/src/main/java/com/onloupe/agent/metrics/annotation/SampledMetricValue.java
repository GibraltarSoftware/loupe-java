package com.onloupe.agent.metrics.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.onloupe.agent.metrics.SamplingType;

// TODO: Auto-generated Javadoc
/**
 * The Interface SampledMetricValue.
 */
@Repeatable(SampledMetricValues.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface SampledMetricValue {
	
	/**
	 * Counter name.
	 *
	 * @return the string
	 */
	String counterName() default "";
	
	/**
	 * Sampling type.
	 *
	 * @return the sampling type
	 */
	SamplingType samplingType();
	
	/**
	 * Unit caption.
	 *
	 * @return the string
	 */
	String unitCaption() default "";
	
	/**
	 * Caption.
	 *
	 * @return the string
	 */
	String caption() default "";
	
	/**
	 * Description.
	 *
	 * @return the string
	 */
	String description() default "";
}
