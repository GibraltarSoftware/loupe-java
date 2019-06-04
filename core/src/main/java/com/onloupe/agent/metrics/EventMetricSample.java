package com.onloupe.agent.metrics;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import com.onloupe.core.logging.Log;
import com.onloupe.core.serialization.monitor.EventMetricSamplePacket;
import com.onloupe.core.util.OutObject;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.log.LogMessageSeverity;

// TODO: Auto-generated Javadoc
/**
 * One sample of a Event metric
 * 
 * Specific Event metrics will have a derived implementation of this class,
 * however clients should work with this interface when feasible to ensure
 * compatibility with any Event metric implementation.
 */
public final class EventMetricSample {
	
	/** The Constant LOG_CATEGORY. */
	private static final String LOG_CATEGORY = "Loupe";

	/** The metric. */
	private EventMetric metric;
	
	/** The packet. */
	private EventMetricSamplePacket packet;

	/**
	 * Create a new API event metric sample object for the provided metric and
	 * internal event metric sample.
	 * 
	 * The metric sample is NOT? automatically added to the samples collection of
	 * the provided metric object.
	 * 
	 * @param metric       The metric object this sample applies to.
	 * @param packet The internal metric sample.
	 */
	protected EventMetricSample(EventMetric metric, EventMetricSamplePacket packet) {
		// and now that we've been created, make sure our metric definition set is
		// locked.
		// metric.Definition.IsReadOnly = true; // ToDo: Double-check that this is set
		// within internal sample.

		// Cache the Event-typed objects we passed to our general metric sample base, so
		// we don't have to cast them.
		this.metric = metric;
		this.packet = packet;

		metric.getDefinition().setReadOnly();
	}

	/**
	 * Set a value in this sample by its value column name.
	 * 
	 * The value must be defined as part of the event metric definition associated
	 * with this sample or an exception will be thrown. The data type must also be
	 * compatible with the data type configured on the event metric definition or no
	 * data will be recorded.
	 * 
	 * @param name  The unique name of the value being recorded (must match a value
	 *              name in the metric definition).
	 * @param value The value to be recorded.
	 */
	public void setValue(String name, Object value) {
		// make sure we got a name
		if (TypeUtils.isBlank(name)) {
			throw new NullPointerException("name");
		}

		// look up the value in the definition so we can find its offset into the array
		EventMetricValueDefinition curValueDefinition;

		OutObject<EventMetricValueDefinition> tempOutCurValueDefinition = new OutObject<EventMetricValueDefinition>();
		if (!getMetric().getDefinition().getValueCollection().tryGetValue(name, tempOutCurValueDefinition)) {
			curValueDefinition = tempOutCurValueDefinition.argValue;
			if (!Log.getSilentMode()) {
				// trace log and return, nothing we can do.
				Log.write(LogMessageSeverity.WARNING, Log.CATEGORY,
						"Unable to add metric value because the value definition could not be found.",
						"Unable to add metric value to the current sample because there is no value definition named %s for metric definition %s",
						getMetric().getDefinition().getKey(), name);
			}
			return;
		} else {
			curValueDefinition = tempOutCurValueDefinition.argValue;
		}

		// now use our overload that takes value to go from here.
		setValue(curValueDefinition, value);
	}

	/**
	 * Records a value to the values array of this sample given its value
	 * definition.
	 * 
	 * The value must be defined as part of the event metric definition associated
	 * with this sample or an exception will be thrown. The data type must also be
	 * compatible with the data type configured on the event metric definition or no
	 * data will be recorded. If called more than once for the same value, the prior
	 * value will be replaced.
	 * 
	 * @param valueDefinition The metric value definition object of the value to be
	 *                        recorded.
	 * @param value           The value to be recorded.
	 */
	public final void setValue(EventMetricValueDefinition valueDefinition, Object value) {
		// make sure we got a value definition
		if (valueDefinition == null) {
			throw new NullPointerException("valueDefinition");
		}

		// look up the numerical index in the collection so we know what offset to put
		// it in the array at
		int valueIndex = getMetric().getDefinition().getValues().getList().indexOf(valueDefinition);

		// if we didn't find it, we're hosed
		if (valueIndex < 0) {
			if (!Log.getSilentMode()) {
				Log.write(LogMessageSeverity.WARNING, LOG_CATEGORY,
						"Unable to add metric value to the current sample due to missing value definition",
						"There is no value definition named %2$s for metric definition %s",
						getMetric().getDefinition().getName(), valueDefinition.getName());
			}
//			throw new IndexOutOfBoundsException("valueDefinition: " + valueDefinition.getName());
		}

		// coerce it into the right type
		Object storedValue;
		if (value == null) {
			// you can always store a null. And we can't really check it more, so there.
			storedValue = null;
		} else {
			// is it close enough to what we're expecting?
			if (valueDefinition.isTrendable()) {
				if (EventMetricDefinition.isTrendableValueType(value.getClass())) {
					storedValue = value;
				} else {
					// no, it should be trendable and it isn't. store null.
					storedValue = null;
				}
			} else {
				// we don't care what it is because we're going to coerce it to a string.
				storedValue = value.toString();
			}
		}

		// now write out the value to the correct spot in the array
		packet.getValues()[valueIndex] = storedValue;
	}

	/**
	 * The Event metric this sample is for.
	 *
	 * @return the metric
	 */
	public EventMetric getMetric() {
		return this.metric;
	}

	/**
	 * The raw value of this metric. Depending on the metric definition, this may be
	 * meaningless and instead a calculation may need to be performed.
	 *
	 * @return the value
	 */
	public double getValue() {
		double value;

		// There are two possible values: Either we have a default numerical value
		// assigned and can
		// return it in raw form, or we will return 1 (we are a count of 1)
		EventMetricValueDefinition defaultValueDefinition = getMetric().getDefinition().getDefaultValue();

		if (defaultValueDefinition == null) {
			// no default value defined, return one
			value = 1; // we are automatically a count of one, that way if someone sums a set of
						// instances they get a count
		} else {
			// We need to read the object value from our values collection. It could be
			// null, it could be of a different type....

			// If it isn't trendable, etc. we're going to return it as null
			if (defaultValueDefinition.isTrendable()) {
				// We have a default value so we're going to return what it has - either null or
				// a numerical value
				int valueIndex = getMetric().getDefinition().getValues().getList().indexOf(defaultValueDefinition);

				assert valueIndex >= 0; // it has to be because we got the object above, so I'm only doing an assert

				// all trendable values are castable
				if (getValues()[valueIndex] == null) {
					// Lets translate all cases of null into NaN since we aren't defined as Double?
					value = Double.NaN;
				} else {
					// use our get effective value routine since it has any conversion overrides we
					// need
					value = getEffectiveValue(valueIndex);
				}
			} else {
				value = Double.NaN;
			}
		}

		return value;
	}
	
	/**
	 * Get the effective value, substituting zero for null.
	 *
	 * @param valueIndex The numeric index of the value to retrieve
	 * @return the effective value
	 */
	public final double getEffectiveValue(int valueIndex) {
		double returnVal;

		// get the raw value - it could be any type, could be null.
		Object rawValue = getValues()[valueIndex];

		// Now start handling it depending on what it is and what type it is,
		// occasionally we have to mess with the data more explicitly

		// if it's null, we return zero.
		if (rawValue == null) {
			returnVal = 0;
		} else if (rawValue instanceof Duration) {
			returnVal = ((Duration) rawValue).toMillis();
		} else if ((rawValue instanceof OffsetDateTime) || (rawValue instanceof LocalDateTime)) {
			// no direct conversion to double.
			returnVal = 1; // we basically have to count the number of occurrences, we can't convert
							// to/from
		} else {
			// We are going to do a conversion, not a cast, to double because we may lose
			// precision, etc.
			returnVal = (double) rawValue;
		}

		return returnVal;
	}

	// ToDo: Additional GetValue overrides to query individual values by name,
	// index(?).

	/**
	 * The array of values associated with this sample. Any value may be a null
	 * object.
	 *
	 * @return the values
	 */
	public final Object[] getValues() {
		return packet.getValues();
	}

	/*
	 * /// <summary> /// The increasing sequence number of all sample packets for
	 * this metric to be used as an absolute order sort. /// </summary> public long
	 * Sequence { get { return _WrappedSample.Sequence; } }
	 * 
	 * 
	 * /// <summary> /// The exact date and time the metric was captured. ///
	 * </summary> public DateTimeOffset Timestamp { get { return
	 * _WrappedSample.Timestamp; } }
	 */

	/**
	 * Write this sample to the current process log if it hasn't been written
	 * already
	 * 
	 * If the sample has not been written to the log yet, it will be written. If it
	 * has been written, subsequent calls to this method are ignored.
	 */
	public void write() {
		if (!this.packet.getPersisted()) {
			Log.write(packet);
		}	
	}

	/**
	 * Determines if the provided object is identical to this object.
	 * 
	 * @param obj The object to compare this object to
	 * @return True if the other object is also a MetricSample and represents the
	 *         same data.
	 */
	@Override
	public boolean equals(Object obj) {
		EventMetricSample otherMetricSample = obj instanceof EventMetricSample ? (EventMetricSample) obj : null;

		return equals(otherMetricSample); // Just have type-specific Equals do the check (it even handles null)
	}

}