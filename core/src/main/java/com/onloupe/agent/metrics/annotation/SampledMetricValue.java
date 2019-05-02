package com.onloupe.agent.metrics.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.onloupe.agent.metrics.SamplingType;

@Repeatable(SampledMetricValues.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface SampledMetricValue {
	String counterName() default "";
	SamplingType samplingType();
	String unitCaption() default "";
	String caption() default "";
	String description() default "";
}
