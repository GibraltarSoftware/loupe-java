package com.onloupe.api.metrics;

import com.onloupe.agent.metrics.EventMetric;
import com.onloupe.agent.metrics.EventMetricDefinition;
import com.onloupe.agent.metrics.SummaryFunction;
import com.onloupe.agent.metrics.annotation.EventMetricClass;
import com.onloupe.agent.metrics.annotation.EventMetricInstanceName;
import com.onloupe.agent.metrics.annotation.EventMetricValue;
import com.onloupe.api.Loupe;
import com.onloupe.api.LoupeTestsBase;
import com.onloupe.core.logging.LogWriteMode;
import com.onloupe.core.util.OutObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

public class EventMetricsByAttributesTests extends LoupeTestsBase
{
	
	private final AtomicLong interlocked = new AtomicLong();
	
	private enum OperationType
	{
		NONE(0),
		PLUS(1),
		MINUS(2);

		public static final int SIZE = java.lang.Integer.SIZE;

		private int intValue;
		private static java.util.HashMap<Integer, OperationType> mappings;
		private static java.util.HashMap<Integer, OperationType> getMappings()
		{
			if (mappings == null)
			{
				synchronized (OperationType.class)
				{
					if (mappings == null)
					{
						mappings = new java.util.HashMap<Integer, OperationType>();
					}
				}
			}
			return mappings;
		}

		private OperationType(int value)
		{
			intValue = value;
			getMappings().put(value, this);
		}

		public int getValue()
		{
			return intValue;
		}

		public static OperationType forValue(int value)
		{
			return getMappings().get(value);
		}
	}

	/** 
	 A simple example of event metric attribute usage.
	 
	 The event metric defined here will track increments and decrements.
	*/
	@EventMetricClass(namespace = "Gibraltar Event Metrics", 
			categoryName = "Example.EventMetric.Attributes", counterName = "UserEventClass", 
			caption = "Example event metric", description = "An example of defining an event metric via attributes.")
	private class UserEventAttributedClass
	{
		private String _InstanceName;

		// We're dealing with plain numbers, no units, so we just use null for the required unitCaption parameter.
		// Caption and Description are optional, but *strongly* recommended for understandable analysis results.
		// And this will be the primary data column to graph for this event metric, so we mark it as the default.
		@EventMetricValue(name = "delta", summaryFunction = SummaryFunction.RUNNING_SUM, 
				defaultValue = true, caption = "Delta", description = "The positive or negative (or zero) effect of the operation.")
		private int _Delta; // It's even legal to use private field members as a value column.

		public UserEventAttributedClass(String instanceName)
		{
			_InstanceName = instanceName;
			_Delta = 0;
		}

		/** 
		 An optional member to be automatically queried for the instance name to use for this event metric on this data object instance.
		 
		 The name of this property isn't important, but for this simple example the obvious name works well.
		 Fields (member variables) and zero-argument methods are other options, but only one member in the class
		 should be marked with the [EventMetricInstanceName] attribute.  It can also be left out entirely and either
		 be specified in code or default to null (the "default instance").  This attribute is handy when different
		 instances of your class will record to separate metric instances, as allowed for in this example.  Any other
		 type will be converted to a string (ToString()) unless it is a null (a numeric zero is not considered a null).
		*/
		@EventMetricInstanceName
		public final String getInstanceName()
		{
			return _InstanceName;
		}

		/** 
		 The OperationType enum value for the most recent operation.
		 
		 Because this is not a recognized numeric type, the value collected will automatically be converted
		 to a string in each event metric sample.  (Can cast an enum to an int to keep it as a numeric value, but
		 the enum label would then be lost.)  We must specify a summary function, but since this is a non-numeric
		 data type, SummaryFunction.Count is the only meaningful choice.  Units are also meaningless for non-numeric
		 data types, so we use null for the required unitCaption parameter.
		*/
		@EventMetricValue(name = "operation", summaryFunction = SummaryFunction.COUNT, 
				defaultValue = true, caption = "Operation", description = "The operation performed.")
		public final OperationType getOperation()
		{
			OperationType operation;
			if (_Delta < 0)
			{
				operation = OperationType.MINUS;
			}
			else if (_Delta > 0)
			{
				operation = OperationType.PLUS;
			}
			else
			{
				operation = OperationType.NONE;
			}

			return operation;
		}

		/** 
		 Perform an increment or decrement operation by a specified (positive or negative) delta.
		 
		 @param delta The amount to increment (positive) or decrement (negative) by.
		*/
		public final void applyDelta(int delta)
		{
			_Delta = delta;

			EventMetric.write(this);
		}

		/** 
		 Read the magnitude (absolute value) of the delta for the latest operation.
		 
		 This is an example of a zero-argument method, which can also be marked with an EventMetricValue
		 attribute to be included as another value column.  Only methods with no arguments are supported, and it
		 must, of course, return a value (not void).
		 @return The amount of increment, or the negative of the amount of decrement.
		*/
		@EventMetricValue(name = "magnitude", summaryFunction = SummaryFunction.AVERAGE, 
				defaultValue = true, caption = "Magnitude", description = "The magnitude (absolute value) of the delta submitted for the operation.")
		public final int magnitudeOfChange()
		{
			return (_Delta < 0) ? -_Delta : _Delta;
		}
	}

	private final Object _SyncLock = new Object();
	private volatile long _ThreadCounter;
	private volatile boolean _ThreadFailed;

	/** 
	 Several alternatives for example client code to utilize event metrics through reflection with attributes.
	 * @throws InterruptedException 
	*/
	@Test
	public final void eventAttributeReflectionExample() throws InterruptedException
	{
		
		Loupe.information("Unit Tests.Metrics.EventMetric.Reflection", "Starting eventAttributeReflectionExample");

		//
		// General example usage.
		//

		// Optional registration in advance (time-consuming reflection walk to find attributes the first time).

		// Either of these two approaches will dig into base types and all interfaces seen at the top level.
		EventMetric.register(UserEventAttributedClass.class); // Can be registered from the Type itself.
		// Or...
		UserEventAttributedClass anyUserEventAttributedInstance = new UserEventAttributedClass("any-prototype");
		EventMetric.register(anyUserEventAttributedInstance); // Can register from a live instance (gets its Type automatically).

		// Elsewhere...

		UserEventAttributedClass userEventAttributedInstance = new UserEventAttributedClass("automatic-instance-name"); // then...

		// To sample all valid event metrics defined by attributes (at all inheritance levels) for a data object instance:

		// This will determine the instance name automatically from the member marked with the EventMetricInstanceName attribute.
		// Null will be used as the instance name (the "default instance") if the attribute is not found for a particular metric.
		EventMetric.write(userEventAttributedInstance); // The recommended typical usage.

		// Or... To specify a different fallback instance name if an EventMetricInstanceName attribute isn't found:
		EventMetric.write(userEventAttributedInstance, "fallback-instance-if-not-assigned");



		//
		// Specific example usage for example class above.
		// Generate some meaningful data in the log to look at.
		//

		UserEventAttributedClass realUsageInstance = new UserEventAttributedClass(null); // Using the "default" instance here.
		EventMetric.register(realUsageInstance); // Registering from the live object also registers the metric instance.

		int[] testDataArray = new int[] {1, 5, 3, -4, 2, 7, -3, -2, 9, 4, -5, -1, 3, -7, 2, 4, -2, 8, 10, -4, 2};

		for (int dataValue : testDataArray)
		{
			realUsageInstance.applyDelta(dataValue); // This method also fires off an event metric sample for us.

			Thread.sleep(100 + (10 * dataValue)); // Sleep for a little while to space out the data, not entirely evenly for this example.
		}

		UserEventAttributedClass reverseInstance = new UserEventAttributedClass("Reverse example"); // Spaces are legal, too.
		// We've already registered the event metric definition above in several places,
		// and we'll just let this metric instance be registered when we first sample it.

		for (int i = testDataArray.length - 1; i >= 0; i--)
		{
			// We're just running through the example data in reverse order for a second example.
			reverseInstance.applyDelta(testDataArray[i]);

			Thread.sleep(100 + (10 * testDataArray[i])); // Space out the data over time a bit.
		}
	}

	@Test
	public final void recordEventMetricReflection()
	{
		Loupe.information("Unit Tests.Metrics.EventMetric.Reflection", "Starting recordEventMetricReflection");

		UserEventObject myDataObject = new UserEventObject(null);
		EventMetric.register(myDataObject);

		EventMetricDefinition metricDefinition;
		OutObject<EventMetricDefinition> tempOutMetricDefinition = new OutObject<EventMetricDefinition>();
		Assertions.assertTrue(EventMetricDefinition.tryGetValue(UserEventObject.class, tempOutMetricDefinition));
		metricDefinition = tempOutMetricDefinition.argValue;

		EventMetricDefinition.write(myDataObject);

		// Now try it with inheritance and interfaces in the mix.

		UserMultipleEventObject bigDataObject = new UserMultipleEventObject(null);
		EventMetric.register(bigDataObject);
		// There's no event at the top level, so this lookup should fail.
		OutObject<EventMetricDefinition> tempOutMetricDefinition2 = new OutObject<EventMetricDefinition>();
		Assertions.assertFalse(EventMetricDefinition.tryGetValue(UserMultipleEventObject.class, tempOutMetricDefinition2));
		metricDefinition = tempOutMetricDefinition2.argValue;
		// Now check for interfaces...
		OutObject<EventMetricDefinition> tempOutMetricDefinition3 = new OutObject<EventMetricDefinition>();
		Assertions.assertTrue(EventMetricDefinition.tryGetValue(IEventMetricOne.class, tempOutMetricDefinition3));
		metricDefinition = tempOutMetricDefinition3.argValue;
		OutObject<EventMetricDefinition> tempOutMetricDefinition5 = new OutObject<EventMetricDefinition>();
		Assertions.assertTrue(EventMetricDefinition.tryGetValue(IEventMetricThree.class, tempOutMetricDefinition5));
		metricDefinition = tempOutMetricDefinition5.argValue;
		OutObject<EventMetricDefinition> tempOutMetricDefinition6 = new OutObject<EventMetricDefinition>();
		Assertions.assertTrue(EventMetricDefinition.tryGetValue(IEventMetricFour.class, tempOutMetricDefinition6));
		metricDefinition = tempOutMetricDefinition6.argValue;

		// And sample all of them on the big object with a single call...

		EventMetric.write(bigDataObject);
	}

	@Test
	public final void recordEventMetricReflectionPerformanceTest(TestReporter reporter)
	{
		Loupe.information("Unit Tests.Metrics.EventMetric.Reflection", "Starting recordEventMetricReflectionPerformanceTest");

		UserEventObject myDataObject = new UserEventObject("Performance Test");

		//We have to limit ourselves to 32000 samples to stay within short.
		final short sampleCount = 32000;
		short curSample;

		//warm up the object just to get rid of first hit performance
		EventMetric.register(myDataObject);
		EventMetric.write(myDataObject);

		//and we're going to write out a BUNCH of samples
		reporter.publishEntry("Starting reflection performance test");

		LocalDateTime curTime = LocalDateTime.now();
		for (curSample = 0; curSample < sampleCount; curSample++)
		{
			//we have a LOT of numbers we need to set to increment this object.
			myDataObject.setValues(curSample); //sets all of the numerics

			//and write it out again just for kicks
			EventMetric.write(myDataObject);
		}

		Duration duration = Duration.between(curTime, LocalDateTime.now());
		reporter.publishEntry(String.format("Completed reflection performance test in %s milliseconds for %d samples",
				duration.toMillis(), curSample));

		Loupe.verbose(LogWriteMode.WAIT_FOR_COMMIT, "Test.Agent.Metrics.EventMetric.Attributes", "Event Metrics performance test flush", null);
	}

	@Test
	public final void recordEventMetricReflectionDataRangeTest(TestReporter reporter)
	{
		Loupe.information("Unit Tests.Metrics.EventMetric.Reflection", "Starting recordEventMetricReflectionDataRangeTest");
		UserEventObject myDataObject = new UserEventObject("Data Range Test");

		//warm up the object just to get rid of first hit performance
		EventMetric.register(myDataObject);
		EventMetric.write(myDataObject);

		//and we're going to write out a BUNCH of samples
		reporter.publishEntry("Starting reflection data range test");
		LocalDateTime curTime = LocalDateTime.now();

		//We have to limit ourselves to 32000 samples to stay within short.
		for (short curSample = 0; curSample < 32000; curSample++)
		{
			//we have a LOT of numbers we need to set to increment this object.
			myDataObject.setValues(curSample, (short)32000); //sets all of the numerics

			//and write it out again just for kicks
			EventMetric.write(myDataObject);
		}
		Duration duration = Duration.between(curTime, LocalDateTime.now());
		reporter.publishEntry(String.format("Completed reflection data range test in %s milliseconds for 32,000 samples", duration.toMillis()));

		Loupe.information(LogWriteMode.WAIT_FOR_COMMIT, "Unit Tests.Metrics.EventMetric.Reflection", "Flushing recordEventMetricReflectionDataRangeTest", null);
	}

	/** 
	 Create a set of data points distributed over a reasonable range of time to show how events over time display.
	 * @throws InterruptedException 
	*/
	@Test
	public final void prettySampleDataOverTimeTest() throws InterruptedException
	{
		Loupe.information("Unit Tests.Metrics.EventMetric.Reflection", "Starting prettySampleDataOverTimeTest");

		UserEventObject myDataObject = new UserEventObject("Pretty Data");
		EventMetric.register(myDataObject);

		//do a set of 20 samples with a gap between each.
		for (short curSample = 0; curSample < 20; curSample++)
		{
			myDataObject.setValues(curSample);
			EventMetric.write(myDataObject);

			//now sleep for a little to make a nice gap.  This has to be >> 16 ms to show a reasonable gap.
			Thread.sleep(200);
		}
	}

	/** 
	 Deliberately attempt to register the same metric simultaneously on multiple threads to test threadsafety.
	 * @throws InterruptedException 
	*/
	@Test
	public final void eventMetricThreadingCollisionTest(TestReporter reporter) throws InterruptedException
	{
		Loupe.information("Unit Tests.Metrics.EventMetric.Reflection", "Starting EventMetric threading collision test");
		final int threadCount = 9;

		int loopCount;
		synchronized (_SyncLock)
		{
			_ThreadFailed = false;
			_ThreadCounter = 0;

			for (int i = 1; i <= threadCount; i++)
			{
				Thread newThread = new Thread()
				{
				public void run()
				{
					synchronizedMetricRegistration(reporter);
				}
				};
				newThread.setName("Sync thread " + i);
				newThread.start();
			}

			loopCount = 0;
			while (_ThreadCounter < threadCount)
			{
				Thread.sleep(100);
				loopCount++;
				if (loopCount > 40)
				{
					break;
				}
			}

			Thread.sleep(2000);
			reporter.publishEntry("Releasing SyncLock");
			_SyncLock.notifyAll();
		}

		loopCount = 0;
		while (_ThreadCounter > 0)
		{
			Thread.sleep(100);
			loopCount++;
			if (loopCount > 40)
			{
				break;
			}
		}

		Thread.sleep(100);
		if (_ThreadCounter > 0)
		{
			reporter.publishEntry("Not all threads finished before timeout");
		}

		if (_ThreadFailed)
		{
			Assertions.fail("At least one thread got an exception");
		}
	}

	private void synchronizedMetricRegistration(TestReporter reporter)
	{
		String name = Thread.currentThread().getName();
		reporter.publishEntry(String.format("%s started", name));
		UserEventCollisionClass userObject = new UserEventCollisionClass(name);

		try
		{
			_ThreadCounter = interlocked.incrementAndGet();
			synchronized (_SyncLock)
			{
				// Do nothing, just release it immediately.
			}

			EventMetric.register(userObject);
			reporter.publishEntry(String.format("%s completed registration of event metric", name));

			userObject.applyDelta(Thread.currentThread().getId());
		}
		catch (RuntimeException ex)
		{
			_ThreadFailed = true;
			reporter.publishEntry(String.format("%s got %s: %s", name, ex.getClass().getSimpleName(), ex.getMessage()));
		}

		_ThreadCounter = interlocked.decrementAndGet();
	}

	/** 
	 A simple example of event metric attribute usage.
	 
	 The event metric defined here will track increments and decrements.
	*/
	@EventMetricClass(namespace = "Gibraltar Event Metrics", 
			categoryName = "Example.EventMetric.Attributes", counterName = "ThreadingCollision", 
			caption = "Example event metric", description = "A practical test case for threading collision of registration.")
	private static class UserEventCollisionClass
	{
		private String _InstanceName;

		// We're dealing with plain numbers, no units, so we just use null for the required unitCaption parameter.
		// Caption and Description are optional, but *strongly* recommended for understandable analysis results.
		// And this will be the primary data column to graph for this event metric, so we mark it as the default.
		@EventMetricValue(name = "delta", summaryFunction = SummaryFunction.RUNNING_SUM, 
				defaultValue = true, caption = "Delta", description = "The positive or negative (or zero) effect of the operation.")
		private long _Delta; // It's even legal to use private field members as a value column.

		public UserEventCollisionClass(String instanceName)
		{
			_InstanceName = instanceName;
			_Delta = 0;
		}

		/** 
		 An optional member to be automatically queried for the instance name to use for this event metric on this data object instance.
		 
		 The name of this property isn't important, but for this simple example the obvious name works well.
		 Fields (member variables) and zero-argument methods are other options, but only one member in the class
		 should be marked with the [EventMetricInstanceName] attribute.  It can also be left out entirely and either
		 be specified in code or default to null (the "default instance").  This attribute is handy when different
		 instances of your class will record to separate metric instances, as allowed for in this example.  Any other
		 type will be converted to a string (ToString()) unless it is a null (a numeric zero is not considered a null).
		*/
		@com.onloupe.agent.metrics.annotation.EventMetricInstanceName
		public final String getInstanceName()
		{
			return _InstanceName;
		}

		/** 
		 The OperationType enum value for the most recent operation.
		 
		 Because this is not a recognized numeric type, the value collected will automatically be converted
		 to a string in each event metric sample.  (Can cast an enum to an int to keep it as a numeric value, but
		 the enum label would then be lost.)  We must specify a summary function, but since this is a non-numeric
		 data type, SummaryFunction.Count is the only meaningful choice.  Units are also meaningless for non-numeric
		 data types, so we use null for the required unitCaption parameter.
		*/
		@EventMetricValue(name = "operation", summaryFunction = SummaryFunction.COUNT, 
				defaultValue = true, caption = "Operation", description = "The operation performed.")
		public final OperationType getOperation()
		{
			OperationType operation;
			if (_Delta < 0)
			{
				operation = OperationType.MINUS;
			}
			else if (_Delta > 0)
			{
				operation = OperationType.PLUS;
			}
			else
			{
				operation = OperationType.NONE;
			}

			return operation;
		}

		/** 
		 Perform an increment or decrement operation by a specified (positive or negative) delta.
		 
		 @param delta The amount to increment (positive) or decrement (negative) by.
		*/
		public final void applyDelta(long delta)
		{
			_Delta = delta;

			EventMetric.write(this);
		}

		/** 
		 Read the magnitude (absolute value) of the delta for the latest operation.
		 
		 This is an example of a zero-argument method, which can also be marked with an EventMetricValue
		 attribute to be included as another value column.  Only methods with no arguments are supported, and it
		 must, of course, return a value (not void).
		 @return The amount of increment, or the negative of the amount of decrement.
		*/
		@EventMetricValue(name = "magnitude", summaryFunction = SummaryFunction.AVERAGE, 
				defaultValue = true, caption = "Magnitude", description = "The magnitude (absolute value) of the delta submitted for the operation.")
		public final long magnitudeOfChange()
		{
			return (_Delta < 0) ? -_Delta : _Delta;
		}
	}
}