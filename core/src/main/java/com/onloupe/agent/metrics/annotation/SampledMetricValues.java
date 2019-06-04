package com.onloupe.agent.metrics.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// TODO: Auto-generated Javadoc
/**
 * The Interface SampledMetricValues.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface SampledMetricValues {
	
	/**
	 * Value.
	 *
	 * @return the sampled metric value[]
	 */
	SampledMetricValue[] value();
}
