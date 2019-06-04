package com.onloupe.agent.metrics.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// TODO: Auto-generated Javadoc
/**
 * The Interface SampledMetricDivisors.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface SampledMetricDivisors {
	
	/**
	 * Value.
	 *
	 * @return the sampled metric divisor[]
	 */
	SampledMetricDivisor[] value();
}
