package com.onloupe.api.metrics;

import com.onloupe.agent.metrics.SampledMetric;
import com.onloupe.agent.metrics.SampledMetricDefinition;
import com.onloupe.agent.metrics.SamplingType;
import com.onloupe.agent.metrics.annotation.SampledMetricClass;
import com.onloupe.agent.metrics.annotation.SampledMetricDivisor;
import com.onloupe.agent.metrics.annotation.SampledMetricInstanceName;
import com.onloupe.agent.metrics.annotation.SampledMetricValue;
import com.onloupe.api.Loupe;
import com.onloupe.api.LoupeTestsBase;
import com.onloupe.core.logging.LogWriteMode;
import com.onloupe.core.util.OutObject;
import com.onloupe.core.util.TimeConversion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

public class SampledMetricsByAttributesTests extends LoupeTestsBase
{
	private static final int MESSAGES_PER_TEST = 10000;

	/** 
	 A simple example of sampled metric attribute usage.
	 
	 <p>The sampled metric counters defined here will track accumulated value and "temperature".</p>
	 <p>Unlike the EventMetric attribute, we only specify metricsSystem and categoryName in the SampledMetric
	 attribute.  Then multiple sampled metric counters can be defined on this class by specifying the remaining
	 necessary parameters in SampledMetricValue attributes.  All sampled metric counters defined by attributes
	 in this class will share this metricsSystem and categoryName.  If mixing sampled metrics with different
	 category names is really needed, it can be done with advanced usage tricks like defining metrics on interfaces,
	 much like with defining multiple event metrics on the same object.</p>
	*/
	@SampledMetricClass(namespace = "Gibraltar Sampled Metrics",  categoryName = "Example.SampledMetric.Attributes")
	private static class UserSampledAttributedClass implements Closeable
	{
		private String _InstanceName;

		// We're dealing with plain numbers, no units, so we just use null for the required unitCaption parameter.
		// Caption and Description are optional, but *strongly* recommended for understandable analysis results.
		// SamplingType.RawCount means that this value directly specifies the data to graph for this counter.
		@SampledMetricValue(counterName = "accumulator", samplingType = SamplingType.RAW_COUNT, caption="Accumulator", description="The accumulated total of increments and decrements")
		private int _Accumulator; // It's even legal to use private field members as a sampled metric counter.

		private int _MagnitudesAccumulator;
		private int _MagnitudesCount;
		private int _OperationCount;

		private static final float ROOM_TEMPERATURE = 20; // 20 C (68 F), our air-conditioned "room temperature".
		private float _TemperatureDelta; // How far above "room temperature" the device temperature was after last operation.
		private LocalDateTime _LastOperation = TimeConversion.MIN.toLocalDateTime(); // When the most recent opperation occurred, for temperature simulation.
		private boolean _EndThread; // A flag to tell our polling thread when to stop.
		private Thread _PollingThread;

		public UserSampledAttributedClass(String instanceName)
		{
			_InstanceName = instanceName;
			_Accumulator = 0;
			_TemperatureDelta = 0;
			_LastOperation = LocalDateTime.now();
			_MagnitudesAccumulator = 0;
			_MagnitudesCount = 0;
			_OperationCount = 0;
			_PollingThread = null;
		}

		public final void startPolling(TestReporter reporter)
		{
			if (getIsPolling())
			{
				return; // Already polling, don't make another one.
			}

			// Now we'll create a background thread to do our sample polling.  This is just an example approach for
			// our isolated example, but in real usage it may make more sense to have one external polling thread
			// handling all of your sampled metric sampling, rather than create a thread for each instance!
			_PollingThread = new Thread()
			{
			public void run()
			{
				try {
					samplePollingThreadStart(reporter);
				} catch (InterruptedException e) {
					reporter.publishEntry("Exception: " + e.getMessage());
				}
			}
			};
			_PollingThread.setName(String.format("Example polling thread (%1$s)", (_InstanceName != null) ? _InstanceName : "null"));

			_EndThread = false;
			_PollingThread.start(); // Start it up!
		}

		public final boolean getIsPolling()
		{
			return _PollingThread != null;
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
		@SampledMetricInstanceName
		public final String getInstanceName()
		{
			return _InstanceName;
		}

		/** 
		 The temperature of the device, as degrees Celsius.
		 
		 <p>We're using this to simulate a temperature as if it were perhaps read from some measurement
		 device.  But we'll fake it with math.</p>
		 <p>We're doing this math without side-effects, so we'll use a property to read it.  And we're measuring
		 temperature in degrees Celsius, so we'll use that for the units caption.
		</p>
		*/
		@SampledMetricValue(counterName = "temperature", samplingType = SamplingType.RAW_COUNT, unitCaption = "Degrees C", caption = "Device Temperature", description = "The temperature measured for the device.")
		public final float getTemperature()
		{
				// Cools towards room temperature over time.  We're greatly accelerating how fast it could actually cool.
			Duration coolingTime = Duration.between(_LastOperation, LocalDateTime.now());
			double coolingFactor = Math.exp(-(TimeUnit.MINUTES.toSeconds(coolingTime.toMinutes())));
			float currentTemperatureDelta = (float)(_TemperatureDelta * coolingFactor);

			return ROOM_TEMPERATURE + currentTemperatureDelta;
		}

		/** 
		 Perform an increment or decrement operation by a specified (positive or negative) delta.
		 
		 @param delta The amount to increment (positive) or decrement (negative) by.
		*/
		public final void applyDelta(int delta)
		{
			_Accumulator += delta;
			_TemperatureDelta = getTemperature() + 0.1f; // We're greatly exaggerating the temperature increase from one operation.
			_LastOperation = LocalDateTime.now();

			_MagnitudesAccumulator += (delta < 0) ? -delta : delta; // Add the magnitude of change.
			_MagnitudesCount++;
			// This seems redundant, but it gets read separately from the MagnitudesCount,
			// so we have to track it as a separate value.
			_OperationCount++;
		}

		/** 
		 The numerator portion of the average magnitude (absolute value) of operations.
		 
		 <p>This value tracks the total magnitude of operations applied since the previous sample.
		 When divided by the count of such operations, this produces an average as the value tracked by this
		 counter.  To accomplish this, we use the SamplingType.IncrementalFraction to designate that we're tracking
		 the incremental value since the previous sample (then reset to 0) and that we also need a second value
		 as the divisor portion, in order to complete actual metric counter.  See the MagnitudeDivisor() method
		 for this other value.</p>
		 <p>Because we're just dealing with raw numbers here, we'll use null for the required units caption
		 parameter.  And because this resets the value, we're using a method rather than a property to discourage
		 extraneous reads (such as in the debugger).</p>
		 @return The total of the absolute values of deltas applied since the previous sample.
		*/
		@SampledMetricValue(counterName = "averageMagnitude", samplingType = SamplingType.INCREMENTAL_FRACTION, caption="Average Magnitude", description="This tracks the average absolute value of operations applied.")
		public final int averageMagnitude()
		{
			int magnitudes = _MagnitudesAccumulator;
			_MagnitudesAccumulator = 0; // Reset average for next sample interval.
			return magnitudes;
		}

		/** 
		 The divisor portion of the average magnitude (absolute value) of operations.
		 
		 <p>This is the second part of the averageMagnitude counter, which we designate with the
		 SampledMetricDivisor attribute.  The counter name specified in this attribute must match the one we
		 use in the SampledMetricValue attribute for the counter that this goes with.</p>
		 <p>Because the SampledMetricValue attribute specifies SamplingType.IncrementalFraction, both data
		 values for this counter must behave as "Incremental", meaning they track the "increment" since the
		 previous sample (then reset to 0).  In this case both values happen to be integers, but all sampled
		 metrics convert data values to double (double-precision floating point), and the two members read for
		 a "Fraction" type counter do not need to have the same numeric type.  As with the numerator portion,
		 we're using a method because it has the side-effect of resetting the count, but in general the two members
		 used for a "Fraction" type do not need to be the same member type or the same access level.</p>
		 @return The number of operations applied since the previous sample.
		*/
		@SampledMetricDivisor(counterName = "averageMagnitude")
		public final int magnitudeDivisor()
		{
			int divisor = _MagnitudesCount;
			_MagnitudesCount = 0; // Reset count for next sample interval.
			return divisor;
		}

		/** 
		 The number of operations applied.
		 
		 This counter tracks the number of operations in a sampling interval.  Notice that we actually
		 never reset the value, however, so our sampling type is SamplingType.TotalCount.  This means that we
		 return the total count, but the analysis will calculate the delta for the start and end of a sampling
		 interval when displaying and thus graph the "rate" of operations.  This also saves us from having to reset
		 the value upon sampling, which can be less risky than the "Incremental" types.  This could be interesting
		 to compare against the "temperature" graph to see how temperature varies with operation rate.
		*/
		@SampledMetricValue(counterName = "operationCount", samplingType = SamplingType.TOTAL_COUNT, caption = "Operation Count", description = "This tracks the number of operations performed in a given interval.")
		public final int getOperationCount()
		{
			return _OperationCount;
		}

		/** 
		 Dispose of this instance and end the polling thread.
		 
		 This is the Microsoft-recommended pattern for IDisposable.  The finalizer (done for us by
		 default) will call Dispose(false), and our Dispose() will call Dispose(true).  This puts the disposal
		 logic in one place to handle both cases and allows for all inheritance levels to cleanup as well.
		*/
		public final void close() throws IOException
		{
			dispose(true);
		}

		protected void dispose(boolean disposing) // Or override, if we had a base class implementing it also.
		{
			//base.Dispose(disposing); // But we have no base, so this doesn't exist.

			if (disposing)
			{
				SampledMetric.write(this); // Write one last sample when we're disposed.
			}

			_EndThread = true; // This needs to be done by the finalizer as well as when disposed.
		}

		private void samplePollingThreadStart(TestReporter reporter) throws InterruptedException
		{
			reporter.publishEntry(String.format("Example polling thread (%s) started", (_InstanceName != null) ? _InstanceName : "null"));

			while (_EndThread == false)
			{
				SampledMetric.write(this); // Write a sample of all sampled metrics defined by attributes on this object.

				Thread.sleep(100); // Sleep for 0.1 seconds before sampling again.
			}

			reporter.publishEntry(String.format("Example polling thread (%s) ending", (_InstanceName != null) ? _InstanceName : "null"));
			_PollingThread = null; // Exiting thread, mark us as no longer polling.
		}
	}

	/** 
	 Several alternatives for example client code to utilize sampled metrics through reflection with attributes.
	 * @throws InterruptedException 
	 * @throws IOException 
	*/
	@Test
	public final void sampledAttributeReflectionExample(TestReporter reporter) throws InterruptedException, IOException
	{
		//
		// General example usage.
		//

		// Optional registration in advance (time-consuming reflection walk to find attributes the first time).

		// Either of these two approaches will dig into base types and all interfaces seen at the top level.
		SampledMetric.register(UserSampledAttributedClass.class); // Can be registered from the Type itself.
		// Or...
		UserSampledAttributedClass anyUserSampledAttributedInstance = new UserSampledAttributedClass("any-prototype");
		SampledMetric.register(anyUserSampledAttributedInstance); // Can register from a live instance (gets its Type automatically).

		// Elsewhere...

		UserSampledAttributedClass userSampledAttributedInstance = new UserSampledAttributedClass("automatic-instance-name"); // then...

		// To sample all valid sampled metrics defined by attributes (at all inheritance levels) for a data object instance:

		// This will determine the instance name automatically from the member marked with the EventMetricInstanceName attribute.
		// Null will be used as the instance name (the "default instance") if the attribute is not found for a particular metric.
		SampledMetric.write(userSampledAttributedInstance); // The recommended typical usage.

		// Or... To specify a different fallback instance name if an EventMetricInstanceName attribute isn't found:
		SampledMetric.write(userSampledAttributedInstance, "fallback-instance-if-not-assigned");



		//
		// Specific example usage for example class above.
		// Generate some meaningful data in the log to look at.
		//

		int[] testDataArray = new int[] {1, 5, 3, -4, 2, 7, -3, -2, 9, 4, -5, -1, 3, -7, 2, 4, -2, 8, 10, -4, 2};

		// Using the "default" instance here.  This will Dispose it for us when it exits the block.
		try (UserSampledAttributedClass realUsageInstance = new UserSampledAttributedClass(null))
		{
			SampledMetric.register(realUsageInstance); // Registering from the live object also registers the metric instance.
			realUsageInstance.startPolling(reporter); // Start polling thread for this one.

			for (int dataValue : testDataArray)
			{
				realUsageInstance.applyDelta(dataValue); // This method also fires off an event metric sample for us.

				Thread.sleep(50 + (5 * dataValue)); // Sleep for a little while to space out the data, not entirely evenly for this example.
			}
		}

		Thread.sleep(1000); // Give it some time to complete.
	}

	@Test
	public final void testEventAttributeReflection()
	{
		SampledMetric.register(UserSampledObject.class);

		UserSampledObject sampledObject = new UserSampledObject(25, 100);

		SampledMetricDefinition.write(sampledObject);

		SampledMetricDefinition sampledMetricDefinition;
		OutObject<SampledMetricDefinition> tempOutSampledMetricDefinition = new OutObject<SampledMetricDefinition>();
		Assertions.assertTrue(SampledMetricDefinition.tryGetValue(UserSampledObject.class, "IncrementalCount", tempOutSampledMetricDefinition));
		sampledMetricDefinition = tempOutSampledMetricDefinition.argValue;

		sampledObject.setValue(35, 90);
		sampledMetricDefinition.writeSample(sampledObject);
	}

	@Test
	public final void performanceTest(TestReporter reporter)
	{
		//first, lets get everything to flush so we have our best initial state.
		Loupe.endFile("Preparing for Performance Test");
		Loupe.information(LogWriteMode.WAIT_FOR_COMMIT, "Test.Agent.Metrics.Performance", "Preparing for Test", "Flushing queue");

		SampledMetric.register(UserSampledObject.class);

		UserSampledObject sampledObject = new UserSampledObject(25, 100);

		//now that we know it's flushed everything, lets do our timed loop.
		OffsetDateTime startTime = OffsetDateTime.now(ZoneOffset.UTC);
		for (int curMessage = 0; curMessage < MESSAGES_PER_TEST; curMessage++)
		{
			SampledMetricDefinition.write(sampledObject);
		}
		OffsetDateTime messageEndTime = OffsetDateTime.now(ZoneOffset.UTC);

		//one wait for commit message to force the buffer to flush.
		Loupe.information(LogWriteMode.WAIT_FOR_COMMIT, "Test.Agent.Metrics.Performance", "Waiting for Samples to Commit", null);

		//and store off our time
		OffsetDateTime endTime = OffsetDateTime.now(ZoneOffset.UTC);

		Duration duration = Duration.between(startTime, endTime);

		reporter.publishEntry(String.format(
				"Sampled Metrics by Attribute Test Completed in %sms .  %s messages were written at an average duration of %dms per message.  The flush took %dms.",
				duration.toMillis(), MESSAGES_PER_TEST, (duration.toMillis()) / MESSAGES_PER_TEST,
				Duration.between(messageEndTime, endTime).toMillis()));

	}
}