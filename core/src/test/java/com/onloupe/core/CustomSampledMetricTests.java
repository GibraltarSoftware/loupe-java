// We deleted CustomSampledMetric. This needs to be deleted or started over.

//package com.onloupe.core;
//
//import java.util.Locale;
//
//import org.junit.jupiter.api.Test;
//
//import com.onloupe.core.monitor.CustomSampledMetric;
//import com.onloupe.core.monitor.CustomSampledMetricDefinition;
//import com.onloupe.core.monitor.Log;
//import com.onloupe.core.monitor.MetricSampleType;
//
//import junit.framework.Assert;
//
////C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
////ORIGINAL LINE: [TestFixture] public class CustomSampledMetricTests
//public class CustomSampledMetricTests {
//	@Test
//	public final void testGenerateMetricData() throws InterruptedException {
//		Log.trace("Defining new metric definitions");
//		// go ahead and register a few metrics
//		int curMetricDefinitionCount = Log.getMetrics().size();
//
//		CustomSampledMetricDefinition newMetric;
//		newMetric = new CustomSampledMetricDefinition(Log.getMetrics(), "GenerateMetricData", "Unit Test Data",
//				"IncrementalCount", MetricSampleType.INCREMENTAL_COUNT);
//		newMetric.setDescription("Unit test sampled metric using the incremental count calculation routine");
//
//		newMetric = new CustomSampledMetricDefinition(Log.getMetrics(), "GenerateMetricData", "Unit Test Data",
//				"IncrementalFraction", MetricSampleType.INCREMENTAL_FRACTION);
//		newMetric.setDescription(
//				"Unit test sampled metric using the incremental fraction calculation routine.  Rare, but fun.");
//
//		newMetric = new CustomSampledMetricDefinition(Log.getMetrics(), "GenerateMetricData", "Unit Test Data",
//				"TotalCount", MetricSampleType.TOTAL_COUNT);
//		newMetric.setDescription("Unit test sampled metric using the Total Count calculation routine.  Very common.");
//
//		newMetric = new CustomSampledMetricDefinition(Log.getMetrics(), "GenerateMetricData", "Unit Test Data",
//				"TotalFraction", MetricSampleType.TOTAL_FRACTION);
//		newMetric.setDescription(
//				"Unit test sampled metric using the Total Fraction calculation routine.  Rare, but rounds us out.");
//
//		newMetric = new CustomSampledMetricDefinition(Log.getMetrics(), "GenerateMetricData", "Unit Test Data",
//				"RawCount", MetricSampleType.RAW_COUNT);
//		newMetric.setDescription(
//				"Unit test sampled metric using the Raw Count calculation routine, which we will then average to create sample intervals.");
//
//		newMetric = new CustomSampledMetricDefinition(Log.getMetrics(), "GenerateMetricData", "Unit Test Data",
//				"RawFraction", MetricSampleType.RAW_FRACTION);
//		newMetric.setDescription(
//				"Unit test sampled metric using the Raw Fraction calculation routine.  Fraction types aren't common.");
//
//		// we should have added six new metric definitions
//		Assert.assertEquals(curMetricDefinitionCount + 6, Log.getMetrics().size());
//		// , "The number of registered metric definitions hasn't increased by the right
//		// amount, tending to mean that one or more metrics didn't register.");
//
//		// and lets go ahead and create new metrics for each definition
//		Log.trace("Defining new metrics");
//		new CustomSampledMetric(Log.getMetrics(), "GenerateMetricData", "Unit Test Data", "IncrementalCount", null);
//		new CustomSampledMetric(Log.getMetrics(), "GenerateMetricData", "Unit Test Data", "IncrementalFraction", null);
//		new CustomSampledMetric(Log.getMetrics(), "GenerateMetricData", "Unit Test Data", "TotalCount", null);
//		new CustomSampledMetric(Log.getMetrics(), "GenerateMetricData", "Unit Test Data", "TotalFraction", null);
//		new CustomSampledMetric(Log.getMetrics(), "GenerateMetricData", "Unit Test Data", "RawCount", null);
//		new CustomSampledMetric(Log.getMetrics(), "GenerateMetricData", "Unit Test Data", "RawFraction", null);
//
//		// and finally, lets go log some data!
//		Log.trace("And now lets log data");
//		CustomSampledMetric incrementalCountMetric = (CustomSampledMetric) Log.getMetrics()
//				.get("GenerateMetricData", "Unit Test Data", "IncrementalCount").getMetrics().get(0);
//		CustomSampledMetric incrementalFractionMetric = (CustomSampledMetric) Log.getMetrics()
//				.get("GenerateMetricData", "Unit Test Data", "IncrementalFraction").getMetrics().get(0);
//		CustomSampledMetric deltaCountMetric = (CustomSampledMetric) Log.getMetrics()
//				.get("GenerateMetricData", "Unit Test Data", "TotalCount").getMetrics().get(0);
//		CustomSampledMetric deltaFractionMetric = (CustomSampledMetric) Log.getMetrics()
//				.get("GenerateMetricData", "Unit Test Data", "TotalFraction").getMetrics().get(0);
//		CustomSampledMetric rawCountMetric = (CustomSampledMetric) Log.getMetrics()
//				.get("GenerateMetricData", "Unit Test Data", "RawCount").getMetrics().get(0);
//		CustomSampledMetric rawFractionMetric = (CustomSampledMetric) Log.getMetrics()
//				.get("GenerateMetricData", "Unit Test Data", "RawFraction").getMetrics().get(0);
//
//		// lets add 10 values, a few milliseconds apart
//		int curSamplePass = 0;
//		while (curSamplePass < 10) {
//			// We're putting in fairly bogus data, but it will produce a consistent output.
//			incrementalCountMetric.writeSample(curSamplePass * 20);
//			incrementalFractionMetric.writeSample(curSamplePass * 20, curSamplePass * 30);
//			deltaCountMetric.writeSample(curSamplePass * 20);
//			deltaFractionMetric.writeSample(curSamplePass * 20, curSamplePass * 30);
//			rawCountMetric.writeSample(curSamplePass);
//			rawFractionMetric.writeSample(curSamplePass / 10.0);
//
//			curSamplePass++;
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//				throw e;
//			}
//		}
//
//		Log.trace("Completed logging metric samples.");
//	}
//
//	@Test
//	public final void testReadMetricData() {
//
//	}
//
//	@Test
//	public final void testSimpleMetricUsage() throws InterruptedException {
//		Log.trace("Defining new metric definitions");
//		// create one sampled metric definition using the "make a definition for the
//		// current log set" override
//		CustomSampledMetricDefinition temperatureTracking = new CustomSampledMetricDefinition("SimpleMetricUsage",
//				"Temperature", "Experiment Temperature", MetricSampleType.RAW_COUNT);
//		temperatureTracking.setDescription(
//				"This is an example from iControl where we want to track the temperature of a reaction or some such thing.");
//
//		// create a set of METRICS (definition + metric) using the static add metric
//		// capability
//		Log.trace("defining new metrics");
//		CustomSampledMetric incrementalCountMetric = CustomSampledMetric.addOrGet("SimpleMetricUsage", "Unit Test Data",
//				"IncrementalCount", MetricSampleType.INCREMENTAL_COUNT, null);
//		CustomSampledMetric incrementalFractionMetric = CustomSampledMetric.addOrGet("SimpleMetricUsage",
//				"Unit Test Data", "IncrementalFraction", MetricSampleType.INCREMENTAL_FRACTION, null);
//		CustomSampledMetric deltaCountMetric = CustomSampledMetric.addOrGet("SimpleMetricUsage", "Unit Test Data",
//				"TotalCount", MetricSampleType.TOTAL_COUNT, null);
//		CustomSampledMetric deltaFractionMetric = CustomSampledMetric.addOrGet("SimpleMetricUsage", "Unit Test Data",
//				"TotalFraction", MetricSampleType.TOTAL_FRACTION, null);
//		CustomSampledMetric rawCountMetric = CustomSampledMetric.addOrGet("SimpleMetricUsage", "Unit Test Data",
//				"RawCount", MetricSampleType.RAW_COUNT, null);
//		CustomSampledMetric rawFractionMetric = CustomSampledMetric.addOrGet("SimpleMetricUsage", "Unit Test Data",
//				"RawFraction", MetricSampleType.RAW_FRACTION, null);
//
//		// lets add 10 values, a few milliseconds apart
//		Log.trace("And now lets log data");
//		int curSamplePass = 0;
//		while (curSamplePass < 10) {
//			// the temperature tracking one is set up so we can write to instances directly
//			// from a definition.
//			temperatureTracking.writeSample(String.format(Locale.getDefault(), "Experiment %1$s", 1), curSamplePass);
//			temperatureTracking.writeSample(String.format(Locale.getDefault(), "Experiment %1$s", 2), curSamplePass);
//			temperatureTracking.writeSample(String.format(Locale.getDefault(), "Experiment %1$s", 3), curSamplePass);
//			temperatureTracking.writeSample(String.format(Locale.getDefault(), "Experiment %1$s", 4), curSamplePass);
//			temperatureTracking.writeSample(String.format(Locale.getDefault(), "Experiment %1$s", 5), curSamplePass);
//			temperatureTracking.writeSample(String.format(Locale.getDefault(), "Experiment %1$s", 6), curSamplePass);
//
//			incrementalCountMetric.writeSample(curSamplePass * 20);
//			incrementalFractionMetric.writeSample(curSamplePass * 20, curSamplePass * 30);
//			deltaCountMetric.writeSample(curSamplePass * 20);
//			deltaFractionMetric.writeSample(curSamplePass * 20, curSamplePass * 30);
//			rawCountMetric.writeSample(curSamplePass);
//			rawFractionMetric.writeSample(curSamplePass / 10.0);
//
//			curSamplePass++;
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//				throw e;
//			}
//		}
//		Log.trace("Completed logging metric samples.");
//	}
//}