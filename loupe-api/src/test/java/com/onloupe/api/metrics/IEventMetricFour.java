package com.onloupe.api.metrics;

import com.onloupe.agent.metrics.SummaryFunction;
import com.onloupe.agent.metrics.annotation.EventMetricClass;
import com.onloupe.agent.metrics.annotation.EventMetricValue;

@EventMetricClass(namespace = "EventMetricsByAttributesTests", categoryName = "Attributes.Event Metric Data", counterName = "IEventMetricFour", caption = "Event metric Four", description = "Fourth event metric defined on an interface.")
public interface IEventMetricFour
{
	@EventMetricValue(name = "string", summaryFunction = SummaryFunction.COUNT, caption = "String", description = "Data of type String")
	String getString();
}