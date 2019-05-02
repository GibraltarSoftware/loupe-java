package com.onloupe.agent.metrics.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.onloupe.agent.metrics.SummaryFunction;

@Repeatable(EventMetricValues.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface EventMetricValue {
	String name();
	String unitCaption() default "";
	SummaryFunction summaryFunction();
	String caption();
	String description();
	boolean defaultValue() default false;
}
