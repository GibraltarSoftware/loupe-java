package com.onloupe.api.metrics;

import com.onloupe.agent.metrics.SummaryFunction;
import com.onloupe.agent.metrics.annotation.EventMetricClass;
import com.onloupe.agent.metrics.annotation.EventMetricValue;

@EventMetricClass(namespace = "EventMetricsByAttributesTests", categoryName = "Attributes.Event Metric Data", counterName = "IEventMetricThree", caption = "Event metric Three", description = "Third event metric defined on an interface.")
public interface IEventMetricThree extends IEventMetricFour
{
	@EventMetricValue(name = "double_average", summaryFunction = SummaryFunction.AVERAGE, caption = "Double Average", description = "Data of type Double")
	@EventMetricValue(name = "double_sum", summaryFunction = SummaryFunction.SUM, caption = "Double Sum", description = "Data of type Double")
	double getDouble();

	@EventMetricValue(name = "float_average", summaryFunction = SummaryFunction.AVERAGE, caption = "Float Average", description = "Data of type Float")
	@EventMetricValue(name = "float_sum", summaryFunction = SummaryFunction.SUM, caption = "Float Sum", description = "Data of type Float")
	float getFloat();
}