package com.onloupe.api.metrics;

import com.onloupe.agent.metrics.SampledMetric;
import com.onloupe.agent.metrics.SampledMetricDefinition;
import com.onloupe.agent.metrics.SamplingType;
import com.onloupe.api.Loupe;
import com.onloupe.api.LoupeTestsBase;
import com.onloupe.core.logging.LogWriteMode;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;

public class SampledMetricsByMethodsTests extends LoupeTestsBase
{
	private static final int MESSAGES_PER_TEST = 10000;

	@Test
	public final void registrationConsistency(TestReporter reporter)
	{
		assert true;
		boolean question = SampledMetricDefinition.isValidDataType(Integer.class);
		assert question;
		question = SampledMetricDefinition.isValidDataType(this.getClass());
		assert!question;

		// Create a sampled metric definition to work with.
		SampledMetricDefinition createdDefinitionOne = SampledMetricDefinition.register("RegistrationConsistency", "Methods.Unit Test Data.Consistency", "Metric One", SamplingType.RAW_COUNT, null, "First metric", "The first sampled metric definition for this test.");
		Assertions.assertNotNull(createdDefinitionOne);

		// Get the same definition with different caption and description.
		SampledMetricDefinition obtainedDefinitionOne = SampledMetricDefinition.register("RegistrationConsistency", "Methods.Unit Test Data.Consistency", "Metric One", SamplingType.RAW_COUNT, null, "The first metric", "This is the first sampled metric definition for this test.");
		Assertions.assertNotNull(obtainedDefinitionOne);

		Assertions.assertSame(createdDefinitionOne, obtainedDefinitionOne, String.format("Second call to SampledMetricDefinition.Register() did not get the same definition object as the first. \"%s\" vs \"%s\".", createdDefinitionOne.getCaption(), obtainedDefinitionOne.getCaption()));
		Assertions.assertEquals("First metric", createdDefinitionOne.getCaption());
		Assertions.assertEquals("The first sampled metric definition for this test.", createdDefinitionOne.getDescription());

		// Get an instance from the definition.
		SampledMetric createdInstanceOneA = SampledMetric.register(createdDefinitionOne, "Instance One A");
		Assertions.assertNotNull(createdInstanceOneA);

		Assertions.assertSame(createdDefinitionOne, createdInstanceOneA.getDefinition(), String.format("Created instance does not point to the same definition object used to create it. \"%s\" vs \"%s\".", createdDefinitionOne.getCaption(), createdInstanceOneA.getDefinition().getCaption()));
		Assertions.assertEquals("Instance One A", createdInstanceOneA.getInstanceName());

		// Get the same instance the same way again.
		SampledMetric obtainedInstanceOneA = SampledMetric.register(createdDefinitionOne, "Instance One A");
		Assertions.assertNotNull(obtainedInstanceOneA);

		Assertions.assertSame(createdInstanceOneA, obtainedInstanceOneA, String.format("Second call to SampledMetric.Register() did not get the same metric instance object as the first. \"%s\" vs \"%s\".", createdInstanceOneA.getInstanceName(), obtainedInstanceOneA.getInstanceName()));

		// Get the same instance directly.
		obtainedInstanceOneA = SampledMetric.register("RegistrationConsistency", "Methods.Unit Test Data.Consistency", "Metric One", SamplingType.RAW_COUNT, null, "Metric #1", "This is metric definition #1 for this test.", "Instance One A");
		Assertions.assertNotNull(obtainedInstanceOneA);
		Assertions.assertNotNull(obtainedInstanceOneA.getDefinition());

		Assertions.assertSame(createdInstanceOneA, obtainedInstanceOneA, String.format("Third call to SampledMetric.Register() did not get the same metric instance object as the first. \"%s\" vs \"%s\".", createdInstanceOneA.getInstanceName(), obtainedInstanceOneA.getInstanceName()));

		// Get a second instance from the definition.
		SampledMetric createdInstanceOneB = SampledMetric.register(createdDefinitionOne, "Instance One B");
		Assertions.assertNotNull(createdInstanceOneB);
		Assertions.assertNotNull(createdInstanceOneB.getDefinition());

		Assertions.assertSame(createdDefinitionOne, createdInstanceOneB.getDefinition(), String.format("Created instance does not point to the same definition object used to create it. \"%s\" vs \"%s\".", createdDefinitionOne.getCaption(), createdInstanceOneB.getDefinition().getCaption()));
		Assertions.assertEquals("Instance One B", createdInstanceOneB.getInstanceName());

		Assertions.assertNotSame(createdInstanceOneA, createdInstanceOneB, String.format("Different metric instances should never be the same object."));
		Assertions.assertNotEquals(createdInstanceOneA, createdInstanceOneB, String.format("Different metric instances should not test as being equal. \"%s\" vs \"%s\".", createdInstanceOneA.getInstanceName(), createdInstanceOneB.getInstanceName()));

		Assertions.assertTrue(createdInstanceOneA.getKey().startsWith(createdDefinitionOne.getKey()), String.format("Instance Key does not start with definition Key. \"%s\" vs \"%s\".", createdInstanceOneA.getKey(), createdDefinitionOne.getKey()));
		Assertions.assertTrue(createdInstanceOneA.getKey().endsWith(createdInstanceOneA.getInstanceName()), String.format("Instance Key does not end with instance name. \"%s\" vs \"%s\".", createdInstanceOneA.getKey(), createdInstanceOneA.getInstanceName()));
		Assertions.assertTrue(createdInstanceOneB.getKey().startsWith(createdDefinitionOne.getKey()), String.format("Instance Key does not start with definition Key. \"%s\" vs \"%s\".", createdInstanceOneB.getKey(), createdDefinitionOne.getKey()));
		Assertions.assertTrue(createdInstanceOneB.getKey().endsWith(createdInstanceOneB.getInstanceName()), String.format("Instance Key does not end with instance name. \"%s\" vs \"%s\".", createdInstanceOneB.getKey(), createdInstanceOneB.getInstanceName()));

		// Try a different definition, directly to an instance first.
		SampledMetric createdInstanceTwoA = SampledMetric.register("RegistrationConsistency", "Methods.Unit Test Data.Consistency", "Metric Two", SamplingType.RAW_COUNT, null, "Second metric", "The second sampled metric definition for this test.", "Instance Two A");
		Assertions.assertNotNull(createdInstanceTwoA);

		// Check it's definition, created automatically.
		SampledMetricDefinition createdDefinitionTwo = createdInstanceTwoA.getDefinition();
		Assertions.assertNotNull(createdDefinitionTwo);

		// Get the same instance, with a different caption and description.
		SampledMetric obtainedInstanceTwoA = SampledMetric.register("RegistrationConsistency", "Methods.Unit Test Data.Consistency", "Metric Two", SamplingType.RAW_COUNT, null, "The second metric", "This is the second sampled metric definition for this test.", "Instance Two A");
		Assertions.assertNotNull(obtainedInstanceTwoA);
		Assertions.assertSame(createdDefinitionTwo, obtainedInstanceTwoA.getDefinition(), String.format("Instances of the same metric do not point to the same definition object. \"%s\" vs \"%s\".", createdDefinitionTwo.getCaption(), obtainedInstanceTwoA.getDefinition().getCaption()));

		// Get the same definition another way.
		SampledMetricDefinition obtainedDefinitionTwo = SampledMetricDefinition.register("RegistrationConsistency", "Methods.Unit Test Data.Consistency", "Metric Two", SamplingType.RAW_COUNT, null, "Metric #2", "This is metric definition #2 for this test.");
		Assertions.assertNotNull(obtainedDefinitionTwo);

		Assertions.assertSame(createdDefinitionTwo, obtainedDefinitionTwo, String.format("Call to SampledMetricDefinition.Register() did not get the same definition as the direct instances. \"%s\" vs \"%s\".", createdDefinitionTwo.getCaption(), obtainedDefinitionTwo.getCaption()));
		Assertions.assertEquals("Second metric", obtainedDefinitionTwo.getCaption());
		Assertions.assertEquals("The second sampled metric definition for this test.", obtainedDefinitionTwo.getDescription());

		Assertions.assertTrue(createdInstanceTwoA.getKey().startsWith(createdDefinitionTwo.getKey()), String.format("Instance Key does not start with definition Key. \"%s\" vs \"%s\".", createdInstanceTwoA.getKey(), createdDefinitionTwo.getKey()));
		Assertions.assertTrue(createdInstanceTwoA.getKey().endsWith(createdInstanceTwoA.getInstanceName()), String.format("Instance Key does not end with instance name. \"%s\" vs \"%s\".", createdInstanceTwoA.getKey(), createdInstanceTwoA.getInstanceName()));

		Assertions.assertNotSame(createdDefinitionOne, createdDefinitionTwo, "Different metric definitions should never be the same object.");
		Assertions.assertNotEquals(createdDefinitionOne, createdDefinitionTwo, String.format("Different metric definitions should not test as being equal. \"%s\" vs \"%s\".", createdDefinitionOne.getKey(), createdDefinitionTwo.getKey()));

		// Create instance from null, then from empty string.
		SampledMetric instanceOneNull = SampledMetric.register(createdDefinitionOne, null);
		SampledMetric instanceOneEmpty = SampledMetric.register(createdDefinitionOne, "");
		Assertions.assertNotNull(instanceOneNull);
		Assertions.assertNotNull(instanceOneEmpty);
		Assertions.assertSame(instanceOneNull, instanceOneEmpty, String.format("Null instance and empty instance are not the same metric object. %s vs %s.", (instanceOneNull.getInstanceName() == null) ? "(null)" : "\"" + instanceOneNull.getInstanceName() + "\"", (instanceOneEmpty.getInstanceName() == null) ? "(null)" : "\"" + instanceOneEmpty.getInstanceName() + "\""));

		// Create instance from empty string, then from null.
		SampledMetric instanceTwoEmpty = SampledMetric.register(createdDefinitionTwo, "");
		SampledMetric instanceTwoNull = SampledMetric.register(createdDefinitionTwo, null);
		Assertions.assertNotNull(instanceTwoEmpty);
		Assertions.assertNotNull(instanceTwoNull);
		Assertions.assertSame(instanceTwoEmpty, instanceTwoNull, String.format("Empty instance and null instance are not the same metric object. %s vs %s.", (instanceTwoEmpty.getInstanceName() == null) ? "(null)" : "\"" + instanceTwoEmpty.getInstanceName() + "\"", (instanceTwoNull.getInstanceName() == null) ? "(null)" : "\"" + instanceTwoNull.getInstanceName() + "\""));

		Assertions.assertTrue(instanceOneNull.isDefault());
		Assertions.assertTrue(instanceOneEmpty.isDefault());
		Assertions.assertTrue(instanceTwoEmpty.isDefault());
		Assertions.assertTrue(instanceTwoNull.isDefault());

		Assertions.assertNull(instanceOneNull.getInstanceName());
		Assertions.assertNull(instanceOneEmpty.getInstanceName());
		Assertions.assertNull(instanceTwoEmpty.getInstanceName());
		Assertions.assertNull(instanceTwoNull.getInstanceName());

		SampledMetricDefinition createdDefinitionThree = SampledMetricDefinition.register("RegistrationConsistency", "Methods.Unit Test Data.Consistency", "Metric Three", SamplingType.INCREMENTAL_COUNT, null, "Third metric", "An IncrementalCount metric.");
		Assertions.assertNotNull(createdDefinitionThree);
		SampledMetricDefinition obtainedDefinitionThree;
		try
		{
			obtainedDefinitionThree = SampledMetricDefinition.register("RegistrationConsistency", "Methods.Unit Test Data.Consistency", "Metric Three", SamplingType.RAW_COUNT, null, "Third metric", "A RawCount metric.");
			Assertions.assertNotNull(obtainedDefinitionThree); // This should never actually be executed.
		}
		catch (IllegalArgumentException ex)
		{
			reporter.publishEntry(String.format("SampledMetricDefinition.Register() with inconsistent sampling type threw expected exception.", ex));
			obtainedDefinitionThree = null;
		}
		Assertions.assertNull(obtainedDefinitionThree); // Confirm we went through the catch.

		SampledMetric createdInstanceThreeA;
		try
		{
			createdInstanceThreeA = SampledMetric.register("RegistrationConsistency", "Methods.Unit Test Data.Consistency", "Metric Three", SamplingType.TOTAL_COUNT, null, "Third metric", "A TotalCount metric.", "Instance Three A");
			Assertions.assertNotNull(createdInstanceThreeA); // This should never actually be executed.
		}
		catch (IllegalArgumentException ex)
		{
			reporter.publishEntry(String.format("SampledMetric.Register() with inconsistent sampling type threw expected exception.", ex));
			createdInstanceThreeA = null;
		}
		Assertions.assertNull(createdInstanceThreeA); // Confirm we went through the catch.

		obtainedDefinitionThree = SampledMetricDefinition.register("RegistrationConsistency", "Methods.Unit Test Data.Consistency", "Metric Three", SamplingType.INCREMENTAL_COUNT, "seconds", "Third metric", "An IncrementalCount of seconds metric.");
		Assertions.assertNotNull(obtainedDefinitionThree);
		//Assertions.IsNull(obtainedDefinitionThree.UnitCaption); // Bug: This is getting "Count of items" instead of null?

		SampledMetric createdInstanceFour = SampledMetric.register("RegistrationConsistency", "Methods.Unit Test Data.Consistency", "Metric Four", SamplingType.INCREMENTAL_FRACTION, "hits per minute", "Third metric", "An IncrementalCount of seconds metric.", null);
		Assertions.assertNotNull(createdInstanceFour);
		Assertions.assertEquals("hits per minute", createdInstanceFour.getUnitCaption());

		SampledMetricDefinition obtainedDefinitionFour = SampledMetricDefinition.register("RegistrationConsistency", "Methods.Unit Test Data.Consistency", "Metric Four", SamplingType.INCREMENTAL_FRACTION, "hits per hour", "Third metric", "An IncrementalCount of seconds metric.");
		Assertions.assertNotNull(obtainedDefinitionFour);
		Assertions.assertEquals("hits per minute", obtainedDefinitionFour.getUnitCaption());
	}

	@Test
	public final void basicMetricUsage() throws InterruptedException
	{
		Loupe.traceVerbose("Registering new sampled metric definitions");
		//go ahead and register a few metrics
		//int curMetricDefinitionCount = AgentLog.MetricDefinitions.Count;

		SampledMetricDefinition incrementalCountDefinition = SampledMetricDefinition.register("BasicMetricUsage", "Methods.Unit Test Data.Long", "IncrementalCount", SamplingType.INCREMENTAL_COUNT, null, "Incremental Count", "Unit test sampled metric using the incremental count calculation routine.");

		SampledMetricDefinition incrementalFractionDefinition = SampledMetricDefinition.register("BasicMetricUsage", "Methods.Unit Test Data.Long", "IncrementalFraction", SamplingType.INCREMENTAL_FRACTION, null, "Incremental Fraction", "Unit test sampled metric using the incremental fraction calculation routine.  Rare, but fun.");

		SampledMetricDefinition totalCountDefinition = SampledMetricDefinition.register("BasicMetricUsage", "Methods.Unit Test Data.Long", "TotalCount", SamplingType.TOTAL_COUNT, null, "Total Count", "Unit test sampled metric using the Total Count calculation routine.  Very common.");

		SampledMetricDefinition totalFractionDefinition = SampledMetricDefinition.register("BasicMetricUsage", "Methods.Unit Test Data.Long", "TotalFraction", SamplingType.TOTAL_FRACTION, null, "Total Fraction", "Unit test sampled metric using the Total Fraction calculation routine.  Rare, but rounds us out.");

		SampledMetricDefinition rawCountDefinition = SampledMetricDefinition.register("BasicMetricUsage", "Methods.Unit Test Data.Long", "RawCount", SamplingType.RAW_COUNT, null, "Raw Count", "Unit test sampled metric using the Raw Count calculation routine, which we will then average to create sample intervals.");

		SampledMetricDefinition rawFractionDefinition = SampledMetricDefinition.register("BasicMetricUsage", "Methods.Unit Test Data.Long", "RawFraction", SamplingType.RAW_FRACTION, null, "Raw Fraction", "Unit test sampled metric using the Raw Fraction calculation routine.  Fraction types aren't common.");

		//we should have added six new metric definitions
		//Assertions.AreEqual(curMetricDefinitionCount + 6, AgentLog.MetricDefinitions.Count, "The number of registered metric definitions hasn't increased by the right amount, tending to mean that one or more metrics didn't register.");

		// These should never be null, but let's check to confirm.
		assert incrementalCountDefinition != null;
		assert incrementalFractionDefinition != null;
		assert totalCountDefinition != null;
		assert totalFractionDefinition != null;
		assert rawCountDefinition != null;
		assert rawFractionDefinition != null;

		//and lets go ahead and create new metrics for each definition
		Loupe.traceVerbose("Obtaining default metric instances from each definition");

		SampledMetric incrementalCountMetric = SampledMetric.register(incrementalCountDefinition, null);
		SampledMetric incrementalFractionMetric = SampledMetric.register(incrementalFractionDefinition, null);
		SampledMetric totalCountMetric = SampledMetric.register(totalCountDefinition, null);
		SampledMetric totalFractionMetric = SampledMetric.register(totalFractionDefinition, null);
		SampledMetric rawCountMetric = SampledMetric.register(rawCountDefinition, null);
		SampledMetric rawFractionMetric = SampledMetric.register(rawFractionDefinition, null);

		// These should never be null, either, but let's check to confirm.
		assert incrementalCountMetric != null;
		assert incrementalFractionMetric != null;
		assert totalCountMetric != null;
		assert totalFractionMetric != null;
		assert rawCountMetric != null;
		assert rawFractionMetric != null;

		//and finally, lets go log some data!
		Loupe.traceVerbose("And now lets log data");

		//lets add 10 values, a few milliseconds apart
		int curSamplePass = 0;
		while (curSamplePass < 10)
		{
			//We're putting in fairly bogus data, but it will produce a consistent output.
			incrementalCountMetric.writeSample(curSamplePass * 20);
			incrementalFractionMetric.writeSample(curSamplePass * 20, curSamplePass * 30);
			totalCountMetric.writeSample(curSamplePass * 20);
			totalFractionMetric.writeSample(curSamplePass * 20, curSamplePass * 30);
			rawCountMetric.writeSample(curSamplePass);
			rawFractionMetric.writeSample(curSamplePass, 10.0);

			curSamplePass++;
			Thread.sleep(100);
		}

		Loupe.traceVerbose("Completed logging metric samples.");
	}

	@Test
	public final void simpleMetricUsage() throws InterruptedException
	{
		Loupe.traceVerbose("Registering new sampled metric definition");
		//create one sampled metric definition using the "make a definition for the current log set" override
		SampledMetricDefinition temperatureTracking = SampledMetricDefinition.register("SimpleMetricUsage", "Methods.Temperature", "Experiment Temperature", SamplingType.RAW_COUNT, null, "Temperature", "This is an example from iControl where we want to track the temperature of a reaction or some such thing.");

		//create a set of METRICS (definition + metric) using the static add metric capability
		Loupe.traceVerbose("Registering metric instances directly");

		SampledMetric incrementalCountMetric = SampledMetric.register("SimpleMetricUsage", "Methods.Unit Test Data.Direct", "IncrementalCount", SamplingType.INCREMENTAL_COUNT, null, "Incremental Count", "Unit test sampled metric using the incremental count calculation routine.", null);

		SampledMetric incrementalFractionMetric = SampledMetric.register("SimpleMetricUsage", "Methods.Unit Test Data.Direct", "IncrementalFraction", SamplingType.INCREMENTAL_FRACTION, null, "Incremental Fraction", "Unit test sampled metric using the incremental fraction calculation routine.  Rare, but fun.", null);

		SampledMetric totalCountMetric = SampledMetric.register("SimpleMetricUsage", "Methods.Unit Test Data.Direct", "TotalCount", SamplingType.TOTAL_COUNT, null, "Total Count", "Unit test sampled metric using the Total Count calculation routine.  Very common.", null);

		SampledMetric totalFractionMetric = SampledMetric.register("SimpleMetricUsage", "Methods.Unit Test Data.Direct", "TotalFraction", SamplingType.TOTAL_FRACTION, null, "Total Fraction", "Unit test sampled metric using the Total Fraction calculation routine.  Rare, but rounds us out.", null);

		SampledMetric rawCountMetric = SampledMetric.register("SimpleMetricUsage", "Methods.Unit Test Data.Direct", "RawCount", SamplingType.RAW_COUNT, null, "Raw Count", "Unit test sampled metric using the Raw Count calculation routine, which we will then average to create sample intervals.", null);

		SampledMetric rawFractionMetric = SampledMetric.register("SimpleMetricUsage", "Methods.Unit Test Data.Direct", "RawFraction", SamplingType.RAW_FRACTION, null, "Raw Fraction", "Unit test sampled metric using the Raw Fraction calculation routine.  Fraction types aren't common.", null);

		// These should never be null, but let's check to confirm.
		assert incrementalCountMetric != null;
		assert incrementalFractionMetric != null;
		assert totalCountMetric != null;
		assert totalFractionMetric != null;
		assert rawCountMetric != null;
		assert rawFractionMetric != null;

		Loupe.traceVerbose("And now lets log data");

		//lets add 10 values, a few milliseconds apart
		int curSamplePass = 0;
		while (curSamplePass < 10)
		{
			//the temperature tracking one is set up so we can write to instances directly from a definition.
			temperatureTracking.writeSample(String.format(Locale.getDefault(), "Experiment %1$s", 1), curSamplePass);
			temperatureTracking.writeSample(String.format(Locale.getDefault(), "Experiment %1$s", 2), curSamplePass);
			temperatureTracking.writeSample(String.format(Locale.getDefault(), "Experiment %1$s", 3), curSamplePass);
			temperatureTracking.writeSample(String.format(Locale.getDefault(), "Experiment %1$s", 4), curSamplePass);
			temperatureTracking.writeSample(String.format(Locale.getDefault(), "Experiment %1$s", 5), curSamplePass);
			temperatureTracking.writeSample(String.format(Locale.getDefault(), "Experiment %1$s", 6), curSamplePass);

			incrementalCountMetric.writeSample(curSamplePass * 20);
			incrementalFractionMetric.writeSample(curSamplePass * 20, curSamplePass * 30);
			totalCountMetric.writeSample(curSamplePass * 20);
			totalFractionMetric.writeSample(curSamplePass * 20, curSamplePass * 30);
			rawCountMetric.writeSample(curSamplePass);
			rawFractionMetric.writeSample(curSamplePass, 10.0);

			curSamplePass++;
			Thread.sleep(100);
		}
		Loupe.traceVerbose("Completed logging metric samples.");
	}


	@Test
	public final void performanceTest(TestReporter reporter)
	{
		// Start a new session file so it won't do maintenance in the middle of our test.
		Loupe.endFile("Preparing for Performance Test");

		Loupe.traceVerbose("Registering new sampled metric definitions");
		//go ahead and register a few metrics
		//int curMetricDefinitionCount = AgentLog.MetricDefinitions.Count;

		SampledMetricDefinition incrementalCountDefinition = SampledMetricDefinition.register("BasicMetricUsage", "Methods.Unit Test Data.Long", "IncrementalCount", SamplingType.INCREMENTAL_COUNT, null, "Incremental Count", "Unit test sampled metric using the incremental count calculation routine.");

		SampledMetricDefinition incrementalFractionDefinition = SampledMetricDefinition.register("BasicMetricUsage", "Methods.Unit Test Data.Long", "IncrementalFraction", SamplingType.INCREMENTAL_FRACTION, null, "Incremental Fraction", "Unit test sampled metric using the incremental fraction calculation routine.  Rare, but fun.");

		SampledMetricDefinition totalCountDefinition = SampledMetricDefinition.register("BasicMetricUsage", "Methods.Unit Test Data.Long", "TotalCount", SamplingType.TOTAL_COUNT, null, "Total Count", "Unit test sampled metric using the Total Count calculation routine.  Very common.");

		SampledMetricDefinition totalFractionDefinition = SampledMetricDefinition.register("BasicMetricUsage", "Methods.Unit Test Data.Long", "TotalFraction", SamplingType.TOTAL_FRACTION, null, "Total Fraction", "Unit test sampled metric using the Total Fraction calculation routine.  Rare, but rounds us out.");

		SampledMetricDefinition rawCountDefinition = SampledMetricDefinition.register("BasicMetricUsage", "Methods.Unit Test Data.Long", "RawCount", SamplingType.RAW_COUNT, null, "Raw Count", "Unit test sampled metric using the Raw Count calculation routine, which we will then average to create sample intervals.");

		SampledMetricDefinition rawFractionDefinition = SampledMetricDefinition.register("BasicMetricUsage", "Methods.Unit Test Data.Long", "RawFraction", SamplingType.RAW_FRACTION, null, "Raw Fraction", "Unit test sampled metric using the Raw Fraction calculation routine.  Fraction types aren't common.");

		//we should have added six new metric definitions
		//Assertions.AreEqual(curMetricDefinitionCount + 6, AgentLog.MetricDefinitions.Count, "The number of registered metric definitions hasn't increased by the right amount, tending to mean that one or more metrics didn't register.");

		// These should never be null, but let's check to confirm.
		assert incrementalCountDefinition != null;
		assert incrementalFractionDefinition != null;
		assert totalCountDefinition != null;
		assert totalFractionDefinition != null;
		assert rawCountDefinition != null;
		assert rawFractionDefinition != null;

		//and lets go ahead and create new metrics for each definition
		Loupe.traceVerbose("Obtaining default metric instances from each definition");

		SampledMetric incrementalCountMetric = SampledMetric.register(incrementalCountDefinition, null);
		SampledMetric incrementalFractionMetric = SampledMetric.register(incrementalFractionDefinition, null);
		SampledMetric totalCountMetric = SampledMetric.register(totalCountDefinition, null);
		SampledMetric totalFractionMetric = SampledMetric.register(totalFractionDefinition, null);
		SampledMetric rawCountMetric = SampledMetric.register(rawCountDefinition, null);
		SampledMetric rawFractionMetric = SampledMetric.register(rawFractionDefinition, null);

		// These should never be null, either, but let's check to confirm.
		assert incrementalCountMetric != null;
		assert incrementalFractionMetric != null;
		assert totalCountMetric != null;
		assert totalFractionMetric != null;
		assert rawCountMetric != null;
		assert rawFractionMetric != null;

		// Now, lets get everything to flush so we have our best initial state.
		Loupe.information(LogWriteMode.WAIT_FOR_COMMIT, "Test.Agent.Metrics.Performance", "Preparing for Test", "Flushing queue");

		//now that we know it's flushed everything, lets do our timed loop.
		OffsetDateTime startTime = OffsetDateTime.now(ZoneOffset.UTC);
		for (int curMessage = 0; curMessage < MESSAGES_PER_TEST; curMessage++)
		{
			//We're putting in fairly bogus data, but it will produce a consistent output.
			incrementalCountMetric.writeSample(20);
			incrementalFractionMetric.writeSample(20, 30);
			totalCountMetric.writeSample(20);
			totalFractionMetric.writeSample(20, 30);
			rawCountMetric.writeSample(20);
			rawFractionMetric.writeSample(10.0);
		}
		OffsetDateTime messageEndTime = OffsetDateTime.now(ZoneOffset.UTC);

		//one wait for commit message to force the buffer to flush.
		Loupe.information(LogWriteMode.WAIT_FOR_COMMIT, "Test.Agent.Metrics.Performance", "Waiting for Samples to Commit", null);

		//and store off our time
		OffsetDateTime endTime = OffsetDateTime.now(ZoneOffset.UTC);

		Duration testDuration = Duration.between(startTime, endTime);
		Duration loopDuration = Duration.between(startTime, messageEndTime);

		reporter.publishEntry(String.format(
				"Sampled Metrics by Method Test committed %s samples in %s ms (average %.4f ms per message).  Average loop time %.4f ms and final flush time %s ms.",
				MESSAGES_PER_TEST, testDuration.toMillis(),(double)(testDuration.toMillis()) / MESSAGES_PER_TEST,
				(double)loopDuration.toMillis() / MESSAGES_PER_TEST, Duration.between(messageEndTime, endTime).toMillis()));

	}
}