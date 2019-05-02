package com.onloupe.api.metrics;

import com.onloupe.agent.metrics.SummaryFunction;
import com.onloupe.agent.metrics.annotation.EventMetricClass;
import com.onloupe.agent.metrics.annotation.EventMetricValue;

@EventMetricClass(namespace = "EventMetricsByAttributesTests", categoryName = "Attributes.Event Metric Data", counterName = "IEventMetricOne", caption = "Event metric One", description = "First event metric defined on an interface.")
public interface IEventMetricOne extends IEventMetricThree
{

	@EventMetricValue(name = "short_average", summaryFunction = SummaryFunction.AVERAGE, caption = "Short Average", description = "Data of type Short")
	@EventMetricValue(name = "short_sum", summaryFunction = SummaryFunction.SUM, caption = "Short Sum", description = "Data of type Short")
	short getShort();

	@EventMetricValue(name = "int_average", summaryFunction = SummaryFunction.AVERAGE, caption = "Int Average", description = "Data of type Int", defaultValue = true)
	@EventMetricValue(name = "int_sum", summaryFunction = SummaryFunction.SUM, caption = "Int Sum", description = "Data of type Int")
	int getInt();

	@EventMetricValue(name = "long_average", summaryFunction = SummaryFunction.AVERAGE, caption = "Long Average", description = "Data of type Long")
	@EventMetricValue(name = "long_sum", summaryFunction = SummaryFunction.SUM, caption = "Long Sum", description = "Data of type Long")
	long getLong();
}