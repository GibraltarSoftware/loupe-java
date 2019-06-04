package com.onloupe.agent.metrics.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// TODO: Auto-generated Javadoc
/**
 * The Interface SampledMetricDivisor.
 */
@Repeatable(SampledMetricDivisors.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface SampledMetricDivisor {
	
	/**
	 * Counter name.
	 *
	 * @return the string
	 */
	String counterName();
}
