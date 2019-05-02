package com.onloupe.api.metrics;

import com.onloupe.agent.metrics.EventMetric;
import com.onloupe.agent.metrics.EventMetricDefinition;
import com.onloupe.agent.metrics.EventMetricSample;
import com.onloupe.agent.metrics.SampledMetric;
import com.onloupe.agent.metrics.SampledMetricDefinition;
import com.onloupe.agent.metrics.SamplingType;
import com.onloupe.agent.metrics.SummaryFunction;
import com.onloupe.api.Loupe;
import com.onloupe.api.LoupeTestsBase;
import com.onloupe.core.logging.LogWriteMode;
import com.onloupe.core.util.OutObject;
import com.onloupe.core.util.RefObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

public class PerformanceTestsMetrics extends LoupeTestsBase
{
	private static final int LOOPS_PER_SAMPLED_TEST = 10000;
	private static final int LOOPS_PER_EVENT_TEST = 60000;

	private static final int MESSAGES_PER_SAMPLED_LOOP = 6;
	private static final int MESSAGES_PER_EVENT_LOOP = 1;
	private static final int VALUES_PER_EVENT_MESSAGE = 3;

	@BeforeAll
	public static final void metricsPerformanceSetUp(TestReporter reporter)
	{
		// Start a new session file so it won't do maintenance in the middle of our tests.
		Loupe.endFile("Preparing for Performance Test");
		reporter.publishEntry("Session File wrapped to new segment.");
	}

	@Test
	public final void sampledMetricsByMethodsPerformanceTest(TestReporter reporter)
	{
		Loupe.traceVerbose("Registering new sampled metric definitions");
		//go ahead and register a few metrics
		//int curMetricDefinitionCount = AgentLog.MetricDefinitions.Count;

		SampledMetricDefinition incrementalCountDefinition = SampledMetricDefinition.register("PerformanceTestsMetrics", "Performance.SampledMetrics.Methods", "IncrementalCount", SamplingType.INCREMENTAL_COUNT, null, "Incremental Count", "Unit test sampled metric using the incremental count calculation routine.");

		SampledMetricDefinition incrementalFractionDefinition = SampledMetricDefinition.register("PerformanceTestsMetrics", "Performance.SampledMetrics.Methods", "IncrementalFraction", SamplingType.INCREMENTAL_FRACTION, null, "Incremental Fraction", "Unit test sampled metric using the incremental fraction calculation routine.  Rare, but fun.");

		SampledMetricDefinition totalCountDefinition = SampledMetricDefinition.register("PerformanceTestsMetrics", "Performance.SampledMetrics.Methods", "TotalCount", SamplingType.TOTAL_COUNT, null, "Total Count", "Unit test sampled metric using the Total Count calculation routine.  Very common.");

		SampledMetricDefinition totalFractionDefinition = SampledMetricDefinition.register("PerformanceTestsMetrics", "Performance.SampledMetrics.Methods", "TotalFraction", SamplingType.TOTAL_FRACTION, null, "Total Fraction", "Unit test sampled metric using the Total Fraction calculation routine.  Rare, but rounds us out.");

		SampledMetricDefinition rawCountDefinition = SampledMetricDefinition.register("PerformanceTestsMetrics", "Performance.SampledMetrics.Methods", "RawCount", SamplingType.RAW_COUNT, null, "Raw Count", "Unit test sampled metric using the Raw Count calculation routine, which we will then average to create sample intervals.");

		SampledMetricDefinition rawFractionDefinition = SampledMetricDefinition.register("PerformanceTestsMetrics", "Performance.SampledMetrics.Methods", "RawFraction", SamplingType.RAW_FRACTION, null, "Raw Fraction", "Unit test sampled metric using the Raw Fraction calculation routine.  Fraction types aren't common.");

		//we should have added six new metric definitions
		//Assert.AreEqual(curMetricDefinitionCount + 6, AgentLog.MetricDefinitions.Count, "The number of registered metric definitions hasn't increased by the right amount, tending to mean that one or more metrics didn't register.");

		// These should never be null, but let's check to confirm.
		assert incrementalCountDefinition != null;
		assert incrementalFractionDefinition != null;
		assert totalCountDefinition != null;
		assert totalFractionDefinition != null;
		assert rawCountDefinition != null;
		assert rawFractionDefinition != null;

		reporter.publishEntry("Sampled metric definitions registered by methods.");

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
		OffsetDateTime startTime = OffsetDateTime.now();
		for (int curMessage = 0; curMessage < LOOPS_PER_SAMPLED_TEST; curMessage++)
		{
			//We're putting in fairly bogus data, but it will produce a consistent output.
			incrementalCountMetric.writeSample(20);
			incrementalFractionMetric.writeSample(20, 30);
			totalCountMetric.writeSample(20);
			totalFractionMetric.writeSample(20, 30);
			rawCountMetric.writeSample(20);
			rawFractionMetric.writeSample(20, 30);
		}
		OffsetDateTime messageEndTime = OffsetDateTime.now();

		//one wait for commit message to force the buffer to flush.
		Loupe.information(LogWriteMode.WAIT_FOR_COMMIT, "Test.Agent.Metrics.Performance", "Waiting for Samples to Commit", null);

		//and store off our time
		OffsetDateTime endTime = OffsetDateTime.now();

		Duration testDuration = Duration.between(startTime, endTime);
		Duration loopDuration = Duration.between(startTime, messageEndTime);
		final int messagesPerTest = LOOPS_PER_SAMPLED_TEST * MESSAGES_PER_SAMPLED_LOOP;

		reporter.publishEntry(String.format("Sampled Metrics by Methods Test committed {0:N0} samples in {1:F3} ms (average {2:F4} ms per message).  Average loop time {3:F4} ms ({4} samples per loop) and final flush time {5:F3} ms.", messagesPerTest, testDuration.toMillis(), (testDuration.toMillis() / messagesPerTest), (loopDuration.toMillis() / LOOPS_PER_SAMPLED_TEST), MESSAGES_PER_SAMPLED_LOOP, Duration.between(messageEndTime, endTime).toMillis()));
	}

	@Test
	public final void sampledMetricsByAttributesPerformanceTest(TestReporter reporter)
	{
		SampledMetric.register(UserPerformanceObject.class);
		reporter.publishEntry("Sampled metrics registered by attributes.");

		UserPerformanceObject sampledObject = new UserPerformanceObject("AttributesPerformanceTest", 25, 100);

		//first, lets get everything to flush so we have our best initial state.
		Loupe.information(LogWriteMode.WAIT_FOR_COMMIT, "Test.Agent.Metrics.Performance", "Preparing for Test", "Flushing queue");

		//now that we know it's flushed everything, lets do our timed loop.
		OffsetDateTime startTime = OffsetDateTime.now(ZoneOffset.UTC);
		for (int curMessage = 0; curMessage < LOOPS_PER_SAMPLED_TEST; curMessage++)
		{
			SampledMetricDefinition.write(sampledObject);
		}
		OffsetDateTime messageEndTime = OffsetDateTime.now(ZoneOffset.UTC);

		//one wait for commit message to force the buffer to flush.
		Loupe.information(LogWriteMode.WAIT_FOR_COMMIT, "Test.Agent.Metrics.Performance", "Waiting for Samples to Commit", null);

		//and store off our time
		OffsetDateTime endTime = OffsetDateTime.now(ZoneOffset.UTC);

		Duration testDuration = Duration.between(startTime, endTime);
		Duration loopDuration = Duration.between(startTime, messageEndTime);
		final int messagesPerTest = LOOPS_PER_SAMPLED_TEST * MESSAGES_PER_SAMPLED_LOOP;

		reporter.publishEntry(String.format(
				"Sampled Metrics by Attributes Test committed {0:N0} samples in {1:F3} ms (average {2:F4} ms per message).  Average loop time {3:F4} ms ({4} samples per loop) and final flush time {5:F3} ms.",
				messagesPerTest, testDuration.toMillis(), (testDuration.toMillis() / messagesPerTest),
				(loopDuration.toMillis() / LOOPS_PER_SAMPLED_TEST), MESSAGES_PER_SAMPLED_LOOP,
				Duration.between(messageEndTime, endTime).toMillis()));
	}

	@Test
	public final void eventMetricsByMethodsPerformanceTest(TestReporter reporter) throws Exception
	{
		EventMetricDefinition eventDefinition;
		OutObject<EventMetricDefinition> tempOutEventDefinition = new OutObject<EventMetricDefinition>();
		if (false == EventMetricDefinition.tryGetValue("PerformanceTestsMetrics", "Performance.EventMetrics.Methods", "UserEvent", tempOutEventDefinition))
		{
			eventDefinition = EventMetricDefinition
					.builder("PerformanceTestsMetrics", "Performance.EventMetrics.Methods", "UserEvent")
					.caption("User Event").description("Unit test event metric with typical data.")
					.addValue("fileName", String.class, SummaryFunction.COUNT, null, "File name",
							"The name of the file")
					.addValue("operation", UserFileOperation.class, SummaryFunction.COUNT, null, "Operation",
							"The type of file operation being performed.")
					.addValue("duration", Duration.class, SummaryFunction.AVERAGE, "ms", "Duration",
							"The duration for this file operation.")
					.build();
			
			RefObject<EventMetricDefinition> tempRefEventDefinition = new RefObject<EventMetricDefinition>(eventDefinition);
			EventMetricDefinition.register(tempRefEventDefinition, "duration");
		eventDefinition = tempRefEventDefinition.argValue;
		}
	else
	{
		eventDefinition = tempOutEventDefinition.argValue;
	}

		assert eventDefinition != null;
		assert eventDefinition.isReadOnly();

		reporter.publishEntry("Event metric definition registered by methods.");

		EventMetric eventMetric = EventMetric.register(eventDefinition, "MethodsPerformanceTest");

		assert eventMetric != null;

		String fileName = "C:\\Dummy\\File\\Name.txt";
		OffsetDateTime operationStart = OffsetDateTime.now(ZoneOffset.UTC);
		OffsetDateTime operationEnd = operationStart.plus(1234, ChronoUnit.MILLIS);

		//first, lets get everything to flush so we have our best initial state.
		Loupe.information(LogWriteMode.WAIT_FOR_COMMIT, "Test.Agent.Metrics.Performance", "Preparing for Test", "Flushing queue");

		//now that we know it's flushed everything, lets do our timed loop.
		OffsetDateTime startTime = OffsetDateTime.now(ZoneOffset.UTC);
		for (int curMessage = 0; curMessage < LOOPS_PER_EVENT_TEST; curMessage++)
		{
			EventMetricSample eventSample = eventMetric.createSample();
			eventSample.setValue("fileName", fileName);
			eventSample.setValue("operation", UserFileOperation.WRITE);
			eventSample.setValue("duration", Duration.between(operationStart, operationEnd));
			eventSample.write();
		}
		OffsetDateTime messageEndTime = OffsetDateTime.now(ZoneOffset.UTC);

		//one wait for commit message to force the buffer to flush.
		Loupe.information(LogWriteMode.WAIT_FOR_COMMIT, "Test.Agent.Metrics.Performance", "Waiting for Samples to Commit", null);

		//and store off our time
		OffsetDateTime endTime = OffsetDateTime.now(ZoneOffset.UTC);

		Duration testDuration = Duration.between(startTime, endTime);
		Duration loopDuration = Duration.between(startTime, messageEndTime);
		final int messagesPerTest = LOOPS_PER_EVENT_TEST * MESSAGES_PER_EVENT_LOOP;

		reporter.publishEntry(String.format(
				"Event Metrics by Methods Test committed {0:N0} events in {1:F3} ms (average {2:F4} ms per message).  Average loop time {3:F4} ms ({4} values per message) and final flush time {5:F3} ms.",
				messagesPerTest, testDuration.toMillis(), (testDuration.toMillis() / messagesPerTest),
				(loopDuration.toMillis() / LOOPS_PER_EVENT_TEST), VALUES_PER_EVENT_MESSAGE,
				Duration.between(messageEndTime, endTime).toMillis()));

	}

	@Test
	public final void eventMetricsByAttributesPerformanceTest(TestReporter reporter)
	{
		EventMetric.register(UserPerformanceObject.class);
		reporter.publishEntry("Event metrics registered by attributes.");

		UserPerformanceObject eventObject = new UserPerformanceObject("AttributesPerformanceTest");
		OffsetDateTime operationStart = OffsetDateTime.now(ZoneOffset.UTC);
		OffsetDateTime operationEnd = operationStart.plus(1234, ChronoUnit.MILLIS);
		eventObject.setEventData("C:\\Dummy\\File\\Name.txt", UserFileOperation.WRITE, operationStart, operationEnd);

		//first, lets get everything to flush so we have our best initial state.
		Loupe.information(LogWriteMode.WAIT_FOR_COMMIT, "Test.Agent.Metrics.Performance", "Preparing for Test", "Flushing queue");

		//now that we know it's flushed everything, lets do our timed loop.
		OffsetDateTime startTime = OffsetDateTime.now(ZoneOffset.UTC);
		for (int curMessage = 0; curMessage < LOOPS_PER_EVENT_TEST; curMessage++)
		{
			EventMetricDefinition.write(eventObject);
		}
		OffsetDateTime messageEndTime = OffsetDateTime.now(ZoneOffset.UTC);

		//one wait for commit message to force the buffer to flush.
		Loupe.information(LogWriteMode.WAIT_FOR_COMMIT, "Test.Agent.Metrics.Performance", "Waiting for Samples to Commit", null);

		//and store off our time
		OffsetDateTime endTime = OffsetDateTime.now(ZoneOffset.UTC);

		Duration testDuration = Duration.between(startTime, endTime);
		Duration loopDuration = Duration.between(startTime, messageEndTime);
		final int messagesPerTest = LOOPS_PER_EVENT_TEST * MESSAGES_PER_EVENT_LOOP;

		reporter.publishEntry(String.format(
				"Event Metrics by Attributes Test committed {0:N0} events in {1:F3} ms (average {2:F4} ms per message).  Average loop time {3:F4} ms ({4} values per message) and final flush time {5:F3} ms.",
				messagesPerTest, testDuration.toMillis(), (testDuration.toMillis() / messagesPerTest),
				(loopDuration.toMillis() / LOOPS_PER_EVENT_TEST), VALUES_PER_EVENT_MESSAGE,
				Duration.between(messageEndTime, endTime).toMillis()));
	}
}