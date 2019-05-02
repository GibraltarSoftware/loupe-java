package com.onloupe.api.metrics;

import com.onloupe.agent.metrics.EventMetric;
import com.onloupe.agent.metrics.EventMetricDefinition;
import com.onloupe.agent.metrics.EventMetricSample;
import com.onloupe.agent.metrics.EventMetricValueDefinition;
import com.onloupe.agent.metrics.SummaryFunction;
import com.onloupe.api.Loupe;
import com.onloupe.api.LoupeTestsBase;
import com.onloupe.core.logging.LogWriteMode;
import com.onloupe.core.util.OutObject;
import com.onloupe.core.util.RefObject;
import com.onloupe.core.util.TimeConversion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

public class EventMetricsByMethodsTests extends LoupeTestsBase {
	private final Object _SyncLock = new Object();
	private volatile long _ThreadCounter;
	private volatile boolean _ThreadFailed;

	private AtomicLong interlocked = new AtomicLong();

	/**
	 * Ensures each of the metrics we test with are actually defined.
	 *
	 * @throws Exception
	 */
	@BeforeAll
	public static final void setup() throws Exception {
		// This is an example of how to create an event metric definition programatically.

		EventMetricDefinition newMetricDefinition;

		// Define an event metric manually (the long way).  First, see if it's already registered...
		OutObject<EventMetricDefinition> tempOutNewMetricDefinition = new OutObject<EventMetricDefinition>();
		if (EventMetricDefinition.tryGetValue("EventMetricTests", "Gibraltar.Monitor.Test", "Manual", tempOutNewMetricDefinition) == false) {
			newMetricDefinition = tempOutNewMetricDefinition.argValue;
			// It's not registered yet, so we need to fill out the template to define it.
			// Now we want to add a few value columns to make it useful.
			// NOTE:  This is designed to exactly match UserEventObject for convenience in analzing results.
			// The dummy data values we're using are unitless, so we'll just use null for the required unitCaption parameter.

			EventMetricDefinition newEventMetric = EventMetricDefinition.builder("EventMetricTests", "Gibraltar.Monitor.Test", "Manual")
			.addValue("short_average", Short.class, SummaryFunction.AVERAGE, null, "Short Average", "Data of type Short")
			.addValue("short_sum", Short.class, SummaryFunction.SUM, null, "Short Sum", "Data of type Short")
			.addValue("short_runningaverage", Short.class, SummaryFunction.RUNNING_AVERAGE, null, "Short Running Average", "Data of type Short")
			.addValue("short_runningsum", Short.class, SummaryFunction.RUNNING_SUM, null, "Short Running Sum", "Data of type Short")
			.addValue("int_sum", Integer.class, SummaryFunction.SUM, null, "Int Sum", "Data of type Int")
			.addValue("long_average", Long.class, SummaryFunction.AVERAGE, null, "Long Average", "Data of type Long")
			.addValue("long_sum", Long.class, SummaryFunction.SUM, null, "Long Sum", "Data of type Long")
			.addValue("decimal_average", Double.class, SummaryFunction.AVERAGE, null, "Decimal Average", "Data of type Decimal")
			.addValue("decimal_sum", Double.class, SummaryFunction.SUM, null, "Decimal Sum", "Data of type Decimal")
			.addValue("double_average", Double.class, SummaryFunction.AVERAGE, null, "Double Average", "Data of type Double")
			.addValue("double_sum", Double.class, SummaryFunction.SUM, null, "Double Sum", "Data of type Double")
			.addValue("float_average", Float.class, SummaryFunction.AVERAGE, null, "Float Average", "Data of type Float")
			.addValue("float_sum", Float.class, SummaryFunction.SUM, null, "Float Sum", "Data of type Float")
			.addValue("Duration_average", Duration.class, SummaryFunction.AVERAGE, null, "Duration Average", "Data of type Duration")
			.addValue("Duration_sum", Duration.class, SummaryFunction.SUM, null, "Duration Sum", "Data of type Duration")
			.addValue("Duration_runningaverage", Duration.class, SummaryFunction.RUNNING_AVERAGE, null, "Duration Running Average", "Data of type Duration represented as a running average.")
			.addValue("Duration_runningsum", Duration.class, SummaryFunction.RUNNING_SUM, null, "Duration Running Sum", "Data of type Duration represented as a running sum.")
			.addValue("string_type", String.class, SummaryFunction.COUNT, null, "String", "Data of type String")
			.addValue("system.enum", UserDataEnumeration.class, SummaryFunction.COUNT, null, "System.Enum", "Data of type System.Enum").build();

			// Pick an interesting value column as the default to be graphed for this metric.  We'll pass it below.
			EventMetricValueDefinition defaultValue = newEventMetric.addValue("int_average", Integer.class, SummaryFunction.AVERAGE, null, "Int Average", "Data of type Int");


			// Finally, register it with Gibraltar, and specify the default value column we saved above.
			RefObject<EventMetricDefinition> tempRefNewEventMetric = new RefObject<EventMetricDefinition>(newEventMetric);
			EventMetricDefinition.register(tempRefNewEventMetric, defaultValue);
			newEventMetric = tempRefNewEventMetric.argValue;
		} else {
			newMetricDefinition = tempOutNewMetricDefinition.argValue;
		}

		EventMetricDefinition metricDefinition;
		OutObject<EventMetricDefinition> tempOutMetricDefinition = new OutObject<EventMetricDefinition>();
		assert EventMetricDefinition.tryGetValue("EventMetricTests", "Gibraltar.Monitor.Test", "Manual", tempOutMetricDefinition);
		metricDefinition = tempOutMetricDefinition.argValue;
		assert metricDefinition != null;
	}

	@Test
	public final void recordEventMetric() {
		// Internally we want to make this comparable to the reflection test, just varying the part that uses reflection.
		EventMetricDefinition metricDefinition;
		OutObject<EventMetricDefinition> tempOutMetricDefinition = new OutObject<EventMetricDefinition>();
		assert EventMetricDefinition.tryGetValue("EventMetricTests", "Gibraltar.Monitor.Test", "Manual", tempOutMetricDefinition);
		metricDefinition = tempOutMetricDefinition.argValue;
		assert metricDefinition != null;

		EventMetric thisExperimentMetric = EventMetric.register(metricDefinition, "RecordEventMetric");
		assert thisExperimentMetric != null;

		// To write a sample manually, we must first create an empty sample for this event metric instance.
		EventMetricSample newSample = thisExperimentMetric.createSample();

		// Then we set the values.
		newSample.setValue("short_average", 1);
		newSample.setValue("short_sum", 1);
		newSample.setValue("short_runningaverage", 1);
		newSample.setValue("short_runningsum", 1);
		newSample.setValue("int_average", 1);
		newSample.setValue("int_sum", 1);
		newSample.setValue("long_average", 1);
		newSample.setValue("long_sum", 1);
		newSample.setValue("decimal_average", 1);
		newSample.setValue("decimal_sum", 1);
		newSample.setValue("double_average", 1);
		newSample.setValue("double_sum", 1);
		newSample.setValue("float_average", 1);
		newSample.setValue("float_sum", 1);
		newSample.setValue("Duration_average", TimeConversion.durationOfTicks(64));
		newSample.setValue("Duration_sum", TimeConversion.durationOfTicks(64));
		newSample.setValue("Duration_runningaverage", TimeConversion.durationOfTicks(64));
		newSample.setValue("Duration_runningsum", TimeConversion.durationOfTicks(64));
		newSample.setValue("string_type", String.format(Locale.getDefault(), "The current manual sample is %d", 1));
		newSample.setValue("system.enum", UserDataEnumeration.forValue(1));

		// And finally, tell the sample to write itself to the Gibraltar AgentLog.
		newSample.write();
	}

	@Test
	public final void recordEventMetricPerformanceTest(TestReporter reporter) {
		// Internally we want to make this comparable to the reflection test, just varying the part that uses reflection.
		EventMetricDefinition metricDefinition;
		OutObject<EventMetricDefinition> tempOutMetricDefinition = new OutObject<EventMetricDefinition>();
		assert EventMetricDefinition.tryGetValue("EventMetricTests", "Gibraltar.Monitor.Test", "Manual", tempOutMetricDefinition);
		metricDefinition = tempOutMetricDefinition.argValue;
		assert metricDefinition != null;

		EventMetric thisExperimentMetric = EventMetric.register(metricDefinition, "RecordEventMetricPerformanceTest");
		assert thisExperimentMetric != null;

		// We're going to write out a BUNCH of samples...
		reporter.publishEntry("Starting performance test");
		LocalDateTime curTime = LocalDateTime.now(); //for timing how fast we are
		int curSample;
		for (curSample = 0; curSample < 32000; curSample++) {
			EventMetricSample newSample = thisExperimentMetric.createSample();
			newSample.setValue("short_average", curSample);
			newSample.setValue("short_sum", curSample);
			newSample.setValue("short_runningaverage", curSample);
			newSample.setValue("short_runningsum", curSample);
			newSample.setValue("int_average", curSample);
			newSample.setValue("int_sum", curSample);
			newSample.setValue("long_average", curSample);
			newSample.setValue("long_sum", curSample);
			newSample.setValue("decimal_average", curSample);
			newSample.setValue("decimal_sum", curSample);
			newSample.setValue("double_average", curSample);
			newSample.setValue("double_sum", curSample);
			newSample.setValue("float_average", curSample);
			newSample.setValue("float_sum", curSample);
			newSample.setValue("Duration_average", TimeConversion.durationOfTicks(curSample));
			newSample.setValue("Duration_sum", TimeConversion.durationOfTicks(curSample));
			newSample.setValue("Duration_runningaverage", TimeConversion.durationOfTicks(curSample));
			newSample.setValue("Duration_runningsum", TimeConversion.durationOfTicks(curSample));
			newSample.setValue("string_type", String.format(Locale.getDefault(), "The current manual sample is %d", curSample));
			newSample.setValue("system.enum", UserDataEnumeration.forValue(curSample));

			newSample.write(); //only now does it get written because we had to wait until you populated the metrics
		}

		Duration duration = Duration.between(curTime, LocalDateTime.now());
		reporter.publishEntry(String.format("Completed performance test in %d milliseconds for %d samples",
				duration.toMillis(), curSample));

		Loupe.verbose(LogWriteMode.WAIT_FOR_COMMIT, "Test.Agent.Metrics.EventMetric.Methods", "Event Metrics performance test flush", null);
	}

	/**
	 * Deliberately attempt to register the same metric simultaneously on multiple threads to test threadsafety.
	 *
	 * @throws InterruptedException
	 */
	//@Test
//	public final void eventMetricThreadingCollisionTest(TestReporter reporter) throws InterruptedException
//	{
//		AgentLog.information("Unit Tests.Metrics.EventMetric.Reflection", "Starting EventMetric threading collision test", null);
//		final int threadCount = 9;
//
//		int loopCount;
//		synchronized (_SyncLock)
//		{
//			_ThreadFailed = false;
//			_ThreadCounter = 0;
//
//			for (int i = 1; i <= threadCount; i++)
//			{
//				Thread newThread = new Thread()
//				{
//				public void run()
//				{
//					synchronizedMetricRegistration(reporter);
//				}
//				};
//				newThread.setName("Sync thread " + i);
//				newThread.start();
//			}
//
//			loopCount = 0;
//			while (_ThreadCounter < threadCount)
//			{
//				Thread.sleep(100);
//				loopCount++;
//				if (loopCount > 40)
//				{
//					break;
//				}
//			}
//
//			Thread.sleep(2000);
//			reporter.publishEntry("Releasing SyncLock");
//			_SyncLock.notifyAll();
//		}
//
//		loopCount = 0;
//		while (_ThreadCounter > 0)
//		{
//			Thread.sleep(100);
//			loopCount++;
//			if (loopCount > 40)
//			{
//				break;
//			}
//		}
//
//		Thread.sleep(100);
//		if (_ThreadCounter > 0)
//		{
//			reporter.publishEntry("Not all threads finished before timeout");
//		}
//
//		if (_ThreadFailed)
//		{
//			Assertions.fail("At least one thread got an exception");
//		}
//	}
	private void synchronizedMetricRegistration(TestReporter reporter) {
		String name = Thread.currentThread().getName();
		reporter.publishEntry(String.format("%s started", name));
		EventMetricDefinition newDefinition = EventMetricDefinition
				.builder("EventMetricTests", "Gibraltar.Monitor.Test", "Sync")
				.addValue("delta", Double.class, SummaryFunction.RUNNING_SUM, "caption", "Delta", "The applied delta")
				.build();

		try {
			_ThreadCounter = interlocked.incrementAndGet();
			synchronized (_SyncLock) {
				// Do nothing, just release it immediately.
			}

			RefObject<EventMetricDefinition> tempRefNewDefinition = new RefObject<EventMetricDefinition>(newDefinition);
			EventMetricDefinition.register(tempRefNewDefinition);
			newDefinition = tempRefNewDefinition.argValue;

			EventMetric metric = EventMetric.register(newDefinition, name);

			reporter.publishEntry(String.format("%s completed registration of event metric", name));

			EventMetricSample sample = metric.createSample();
			sample.setValue("delta", Thread.currentThread().getId());
			sample.write();
		} catch (RuntimeException ex) {
			_ThreadFailed = true;
			reporter.publishEntry(String.format("%s got %s: %s", name, ex.getClass().getSimpleName(), ex.getMessage()));
		}

		_ThreadCounter = interlocked.decrementAndGet();
	}
}