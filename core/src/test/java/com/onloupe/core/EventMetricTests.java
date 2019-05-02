//package com.onloupe.core;
//
//import java.math.BigDecimal;
//import java.time.Duration;
//import java.time.LocalDateTime;
//import java.util.Locale;
//
//import org.junit.jupiter.api.Test;
//
//import com.onloupe.core.monitor.EventMetric;
//import com.onloupe.core.monitor.EventMetricDefinition;
//import com.onloupe.core.monitor.EventMetricSample;
//import com.onloupe.core.monitor.EventMetricValueDefinitionCollection;
//import com.onloupe.core.monitor.Log;
//import com.onloupe.core.monitor.LogWriteMode;
//import com.onloupe.core.monitor.MetricDefinition;
//import com.onloupe.core.util.TimeConversion;
//import com.onloupe.model.data.EventMetricValueTrend;
//import com.onloupe.model.log.LogMessageSeverity;
//
////C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
////ORIGINAL LINE: [TestFixture] public class EventMetricTests
//public class EventMetricTests {
//	/**
//	 * Ensures each of the metrics we test with are actually defined.
//	 */
//	public final void setup() {
//		MetricDefinition newMetricDefinition = Log.getMetrics().tryGetValue("EventMetricTests",
//				"Gibraltar.Monitor.Test", "Manual");
//
//		// Define an event metric manually (the long way)
//		if (newMetricDefinition == null) {
//			// Define an event metric manually (the hard way)
//			EventMetricDefinition newEventMetric = new EventMetricDefinition("EventMetricTests",
//					"Gibraltar.Monitor.Test", "Manual");
//
//			// we now have a minimal definition, but we probably want to add a few
//			// attributes to make it useful
//			// NOTE: This is designed to exactly match UserDataObject for convenience in
//			// analzing results.
//			EventMetricValueDefinitionCollection valueDefinition = newEventMetric.getValues();
//			valueDefinition.add("short_average", Short.class, "Short Average", "Data of type Short")
//					.setDefaultTrend(EventMetricValueTrend.AVERAGE);
//			valueDefinition.add("short_sum", Short.class, "Short Sum", "Data of type Short")
//					.setDefaultTrend(EventMetricValueTrend.SUM);
//			valueDefinition.add("short_runningaverage", Short.class, "Short Running Average", "Data of type Short")
//					.setDefaultTrend(EventMetricValueTrend.RUNNING_AVERAGE);
//			valueDefinition.add("short_runningsum", Short.class, "Short Running Sum", "Data of type Short")
//					.setDefaultTrend(EventMetricValueTrend.RUNNING_SUM);
//			valueDefinition.add("int_average", Integer.class, "Int Average", "Data of type Int")
//					.setDefaultTrend(EventMetricValueTrend.AVERAGE);
//			valueDefinition.add("int_sum", Integer.class, "Int Sum", "Data of type Int")
//					.setDefaultTrend(EventMetricValueTrend.SUM);
//			valueDefinition.add("long_average", Long.class, "Long Average", "Data of type Long")
//					.setDefaultTrend(EventMetricValueTrend.AVERAGE);
//			valueDefinition.add("long_sum", Long.class, "Long Sum", "Data of type Long")
//					.setDefaultTrend(EventMetricValueTrend.SUM);
//			valueDefinition.add("decimal_average", BigDecimal.class, "Decimal Average", "Data of type Decimal")
//					.setDefaultTrend(EventMetricValueTrend.AVERAGE);
//			valueDefinition.add("decimal_sum", BigDecimal.class, "Decimal Sum", "Data of type Decimal")
//					.setDefaultTrend(EventMetricValueTrend.SUM);
//			valueDefinition.add("double_average", Double.class, "Double Average", "Data of type Double")
//					.setDefaultTrend(EventMetricValueTrend.AVERAGE);
//			valueDefinition.add("double_sum", Double.class, "Double Sum", "Data of type Double")
//					.setDefaultTrend(EventMetricValueTrend.SUM);
//			valueDefinition.add("float_average", Float.class, "Float Average", "Data of type Float")
//					.setDefaultTrend(EventMetricValueTrend.AVERAGE);
//			valueDefinition.add("float_sum", Float.class, "Float Sum", "Data of type Float")
//					.setDefaultTrend(EventMetricValueTrend.SUM);
//			valueDefinition.add("timespan_average", Duration.class, "TimeSpan Average", "Data of type TimeSpan")
//					.setDefaultTrend(EventMetricValueTrend.AVERAGE);
//			valueDefinition.add("timespan_sum", Duration.class, "TimeSpan Sum", "Data of type TimeSpan")
//					.setDefaultTrend(EventMetricValueTrend.SUM);
//			valueDefinition
//					.add("timespan_runningaverage", Duration.class, "TimeSpan Running Average",
//							"Data of type TimeSpan represented as a running average.")
//					.setDefaultTrend(EventMetricValueTrend.RUNNING_AVERAGE);
//			valueDefinition
//					.add("timespan_runningsum", Duration.class, "TimeSpan Running Sum",
//							"Data of type TimeSpan represented as a running sum.")
//					.setDefaultTrend(EventMetricValueTrend.RUNNING_SUM);
//			valueDefinition.add("string", String.class, "String", "Data of type String");
//			valueDefinition.add("system.enum", UserDataEnumeration.class, "System.Enum", "Data of type System.Enum");
//
//			newEventMetric.setDefaultValue(newEventMetric.getValues().get("int_average"));
//			newEventMetric.register(); // Register it with the collection.
//		}
//
//		// And define a metric using reflection (not the same metric as above)
//		// UserDataObject myDataObject = new UserDataObject(null);
//		// EventMetric.AddOrGet(myDataObject);
//	}
//
//	@Test
//	public final void testRecordEventMetric() {
//		// Internally we want to make this comparable to the reflection test, just
//		// varying the part that use reflection.
//		EventMetric thisExperimentMetric = EventMetric.addOrGet("EventMetricTests", "Gibraltar.Monitor.Test", "Manual",
//				"RecordEventMetric");
//
//		// write out one sample
//		EventMetricSample newSample = thisExperimentMetric.createSample();
//		newSample.setValue("short_average", 1);
//		newSample.setValue("short_sum", 1);
//		newSample.setValue("short_runningaverage", 1);
//		newSample.setValue("short_runningsum", 1);
//		newSample.setValue("int_average", 1);
//		newSample.setValue("int_sum", 1);
//		newSample.setValue("long_average", 1);
//		newSample.setValue("long_sum", 1);
//		newSample.setValue("decimal_average", 1);
//		newSample.setValue("decimal_sum", 1);
//		newSample.setValue("double_average", 1);
//		newSample.setValue("double_sum", 1);
//		newSample.setValue("float_average", 1);
//		newSample.setValue("float_sum", 1);
//		newSample.setValue("timespan_average", TimeConversion.durationOfTicks(1));
//		newSample.setValue("timespan_sum", TimeConversion.durationOfTicks(1));
//		newSample.setValue("timespan_runningaverage", TimeConversion.durationOfTicks(1));
//		newSample.setValue("timespan_runningsum", TimeConversion.durationOfTicks(1));
//		newSample.setValue("string", String.format(Locale.getDefault(), "The current manual sample is %1$s", 1));
//		newSample.setValue("system.enum", UserDataEnumeration.forValue(1));
//		newSample.write(); // only now does it get written because we had to wait until you populated the
//							// metrics
//	}
//
//	@Test
//	public final void testRecordEventMetricPerformanceTest() {
//		// Internally we want to make this comparable to the reflection test, just
//		// varying the part that use reflection.
//		EventMetric thisExperimentMetric = EventMetric.addOrGet("EventMetricTests", "Gibraltar.Monitor.Test", "Manual",
//				"RecordEventMetricPerformanceTest");
//
//		// make this loupe Log with severity verbose
//		// and we're going to write out a BUNCH of samples
//
//		Log.write(LogMessageSeverity.VERBOSE, "", "Unit test recordEventMetricPerformanceTest()",
//				"Starting performance test");
//		LocalDateTime curTime = LocalDateTime.now(); // for timing how fast we are
//		for (int curSample = 0; curSample < 32000; curSample++) {
//			EventMetricSample newSample = thisExperimentMetric.createSample();
//			newSample.setValue("short_average", curSample);
//			newSample.setValue("short_sum", curSample);
//			newSample.setValue("short_runningaverage", curSample);
//			newSample.setValue("short_runningsum", curSample);
//			newSample.setValue("int_average", curSample);
//			newSample.setValue("int_sum", curSample);
//			newSample.setValue("long_average", curSample);
//			newSample.setValue("long_sum", curSample);
//			newSample.setValue("decimal_average", curSample);
//			newSample.setValue("decimal_sum", curSample);
//			newSample.setValue("double_average", curSample);
//			newSample.setValue("double_sum", curSample);
//			newSample.setValue("float_average", curSample);
//			newSample.setValue("float_sum", curSample);
//			newSample.setValue("timespan_average", TimeConversion.durationOfTicks(curSample));
//			newSample.setValue("timespan_sum", TimeConversion.durationOfTicks(curSample));
//			newSample.setValue("timespan_runningaverage", TimeConversion.durationOfTicks(curSample));
//			newSample.setValue("timespan_runningsum", TimeConversion.durationOfTicks(curSample));
//			newSample.setValue("string",
//					String.format(Locale.getDefault(), "The current manual sample is %1$s", curSample));
//			newSample.setValue("system.enum", UserDataEnumeration.forValue(curSample));
//
//			newSample.write(); // only now does it get written because we had to wait until you populated the
//								// metrics
//		}
//		Duration duration = Duration.between(curTime, LocalDateTime.now());
//		Log.write(LogMessageSeverity.VERBOSE, "", "Unit test recordEventMetricPerformanceTest()",
//				"Completed performance test in %s milliseconds for 32,000 samples", duration.toMillis());
//
//		Log.write(LogMessageSeverity.VERBOSE, LogWriteMode.WAIT_FOR_COMMIT, null, "Unit Tests",
//				"Event Metrics performance test flush", null);
//	}
//
//}