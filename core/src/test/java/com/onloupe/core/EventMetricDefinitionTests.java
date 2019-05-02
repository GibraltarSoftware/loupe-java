package com.onloupe.core;

import java.math.BigDecimal;
import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.onloupe.agent.metrics.EventMetricDefinition;
import com.onloupe.agent.metrics.EventMetricValueDefinitionCollection;
import com.onloupe.model.data.EventMetricValueTrend;

public class EventMetricDefinitionTests {
	/**
	 * Register metric definitions the long hand way to prove the underlying API
	 * works.
	 * 
	 * This test is coupled with other tests in this batch, so don't go changing the
	 * strings willy nilly
	 */
	@Test
	public final void testRegisterEventMetrics() {
		// Define an event metric manually (the hard way)
		EventMetricDefinition newEventMetric = EventMetricDefinition.builder("EventMetricTests", "Gibraltar.Monitor.Test",
				"Manual").build();

		// we now have a minimal definition, but we probably want to add a few
		// attributes to make it useful
		// NOTE: This is designed to exactly match UserDataObject for convenience in
		// analyzing results.
		EventMetricValueDefinitionCollection valueDefinition = newEventMetric.getValues();
		valueDefinition.add("short_average", Short.class, "Short Average", "Data of type Short")
				.setDefaultTrend(EventMetricValueTrend.AVERAGE);
		valueDefinition.add("short_sum", Short.class, "Short Sum", "Data of type Short")
				.setDefaultTrend(EventMetricValueTrend.SUM);
		valueDefinition.add("short_runningaverage", Short.class, "Short Running Average", "Data of type Short")
				.setDefaultTrend(EventMetricValueTrend.RUNNING_AVERAGE);
		valueDefinition.add("short_runningsum", Short.class, "Short Running Sum", "Data of type Short")
				.setDefaultTrend(EventMetricValueTrend.RUNNING_SUM);
		valueDefinition.add("int_average", Integer.class, "Int Average", "Data of type Int")
				.setDefaultTrend(EventMetricValueTrend.AVERAGE);
		valueDefinition.add("int_sum", Integer.class, "Int Sum", "Data of type Int")
				.setDefaultTrend(EventMetricValueTrend.SUM);
		valueDefinition.add("long_average", Long.class, "Long Average", "Data of type Long")
				.setDefaultTrend(EventMetricValueTrend.AVERAGE);
		valueDefinition.add("long_sum", Long.class, "Long Sum", "Data of type Long")
				.setDefaultTrend(EventMetricValueTrend.SUM);
		valueDefinition.add("decimal_average", BigDecimal.class, "Decimal Average", "Data of type Decimal")
				.setDefaultTrend(EventMetricValueTrend.AVERAGE);
		valueDefinition.add("decimal_sum", BigDecimal.class, "Decimal Sum", "Data of type Decimal")
				.setDefaultTrend(EventMetricValueTrend.SUM);
		valueDefinition.add("double_average", Double.class, "Double Average", "Data of type Double")
				.setDefaultTrend(EventMetricValueTrend.AVERAGE);
		valueDefinition.add("double_sum", Double.class, "Double Sum", "Data of type Double")
				.setDefaultTrend(EventMetricValueTrend.SUM);
		valueDefinition.add("float_average", Float.class, "Float Average", "Data of type Float")
				.setDefaultTrend(EventMetricValueTrend.AVERAGE);
		valueDefinition.add("float_sum", Float.class, "Float Sum", "Data of type Float")
				.setDefaultTrend(EventMetricValueTrend.SUM);
		valueDefinition.add("timespan_average", Duration.class, "TimeSpan Average", "Data of type TimeSpan")
				.setDefaultTrend(EventMetricValueTrend.AVERAGE);
		valueDefinition.add("timespan_sum", Duration.class, "TimeSpan Sum", "Data of type TimeSpan")
				.setDefaultTrend(EventMetricValueTrend.SUM);
		valueDefinition
				.add("timespan_runningaverage", Duration.class, "TimeSpan Running Average",
						"Data of type TimeSpan represented as a running average.")
				.setDefaultTrend(EventMetricValueTrend.RUNNING_AVERAGE);
		valueDefinition
				.add("timespan_runningsum", Duration.class, "TimeSpan Running Sum",
						"Data of type TimeSpan represented as a running sum.")
				.setDefaultTrend(EventMetricValueTrend.RUNNING_SUM);
		valueDefinition.add("string", String.class, "String", "Data of type String");
		valueDefinition.add("system.enum", UserDataEnumeration.class, "System.Enum", "Data of type System.Enum");

		newEventMetric.setDefaultValue(newEventMetric.getValues().get("int_average"));
		newEventMetric = newEventMetric.register(); // Register it with the collection.

		// Create this instance of that definition
		newEventMetric.addOrGet("This Experiment");
	}
}