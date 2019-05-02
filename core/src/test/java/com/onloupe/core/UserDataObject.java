package com.onloupe.core;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;

import com.onloupe.agent.metrics.annotation.EventMetricInstanceName;
import com.onloupe.core.util.TimeConversion;

/**
 * This is a user-provided data object, demonstrating reflection for event
 * metrics
 */
public class UserDataObject {
	private String instanceName;
	private short shortValue;
	private int intValue;
	private long longValue;
	private BigDecimal decimal = BigDecimal.ZERO;
	private double doubleValue;
	private float floatValue;
	private Duration duration;
	private String string;
	private UserDataEnumeration userData;

	public UserDataObject(String instanceName) {
		this.instanceName = instanceName;
	}

	public final boolean isTrendableType(java.lang.Class type) {
		boolean trendable = false;

		// we're using Is so we can check for compatibile types, not just base types.
		if ((type == Short.class) || (type == Integer.class) || (type == Long.class) || (type == BigDecimal.class)
				|| (type == Double.class) || (type == Float.class)) {
			trendable = true;
		}
		// Now check object types
		else if ((type == LocalDateTime.class) || (type == Duration.class)) {
			trendable = true;
		}

		return trendable;
	}

	@EventMetricInstanceName
	public final String getInstanceName() {
		return this.instanceName;
	}

	// [EventMetricValue("short_average", "Short Average", "Data of type Short",
	// DefaultTrend=EventMetricValueTrend.Average)]
	// [EventMetricValue("short_sum", "Short Sum", "Data of type Short",
	// DefaultTrend = EventMetricValueTrend.Sum)]
	// [EventMetricValue("short_runningaverage", "Short Running Average", "Data of
	// type Short", DefaultTrend = EventMetricValueTrend.RunningAverage)]
	// [EventMetricValue("short_runningsum", "Short Running Sum", "Data of type
	// Short", DefaultTrend = EventMetricValueTrend.RunningSum)]
	public final short getShort() {
		return this.shortValue;
	}

	public final void setShort(short value) {
		this.shortValue = value;
	}

	// [EventMetricValue("int_average", "Int Average", "Data of type Int",
	// DefaultTrend = EventMetricValueTrend.Average, IsDefaultValue = true)]
	// [EventMetricValue("int_sum", "Int Sum", "Data of type Int", DefaultTrend =
	// EventMetricValueTrend.Sum)]
	public final int getInt() {
		return this.intValue;
	}

	public final void setInt(int value) {
		this.intValue = value;
	}

	// [EventMetricValue("long_average", "Long Average", "Data of type Long",
	// DefaultTrend = EventMetricValueTrend.Average)]
	// [EventMetricValue("long_sum", "Long Sum", "Data of type Long", DefaultTrend =
	// EventMetricValueTrend.Sum)]
	public final long getLong() {
		return this.longValue;
	}

	public final void setLong(long value) {
		this.longValue = value;
	}

	// [EventMetricValue("decimal_average", "Decimal Average", "Data of type
	// Decimal", DefaultTrend = EventMetricValueTrend.Average)]
	// [EventMetricValue("decimal_sum", "Decimal Sum", "Data of type Decimal",
	// DefaultTrend = EventMetricValueTrend.Sum)]
	public final BigDecimal getDecimal() {
		return this.decimal;
	}

	public final void setDecimal(BigDecimal value) {
		this.decimal = value;
	}

	// [EventMetricValue("double_average", "Double Average", "Data of type Double",
	// DefaultTrend = EventMetricValueTrend.Average)]
	// [EventMetricValue("double_sum", "Double Sum", "Data of type Double",
	// DefaultTrend = EventMetricValueTrend.Sum)]
	public final double getDouble() {
		return this.doubleValue;
	}

	public final void setDouble(double value) {
		this.doubleValue = value;
	}

	// [EventMetricValue("float_average", "Float Average", "Data of type Float",
	// DefaultTrend = EventMetricValueTrend.Average)]
	// [EventMetricValue("float_sum", "Float Sum", "Data of type Float",
	// DefaultTrend = EventMetricValueTrend.Sum)]
	public final float getFloat() {
		return this.floatValue;
	}

	public final void setFloat(float value) {
		this.floatValue = value;
	}

	// [EventMetricValue("timespan_average", "TimeSpan Average", "Data of type
	// TimeSpan", DefaultTrend = EventMetricValueTrend.Average)]
	// [EventMetricValue("timespan_sum", "TimeSpan Sum", "Data of type TimeSpan",
	// DefaultTrend = EventMetricValueTrend.Sum)]
	// [EventMetricValue("timespan_runningaverage", "TimeSpan Running Average",
	// "Data of type TimeSpan represented as a running average.", DefaultTrend =
	// EventMetricValueTrend.RunningAverage)]
	// [EventMetricValue("timespan_runningsum", "TimeSpan Running Sum", "Data of
	// type TimeSpan represented as a running sum.", DefaultTrend =
	// EventMetricValueTrend.RunningSum)]
	public final Duration getTimeSpan() {
		return this.duration;
	}

	public final void setTimeSpan(Duration value) {
		this.duration = value;
	}
	/*
	 * public double TimeSpan { get { return _TimeSpan.TotalMilliseconds; } set {
	 * _TimeSpan = new TimeSpan((long)value); } }
	 */

	// [EventMetricValue("string", "String", "Data of type String")]
	public final String getString() {
		return this.string;
	}

	public final void setString(String value) {
		this.string = value;
	}

	// [EventMetricValue("system.enum", "System.Enum", "Data of type System.Enum (a
	// numeric enum, UserDataEnumeration")]
	public final UserDataEnumeration getEnum() {
		return this.userData;
	}

	public final void setEnum(UserDataEnumeration value) {
		this.userData = value;
	}

	public final void setValues(short sample) {
		// we just set each value to the provided value, much faster than calculating
		// the range.
		this.shortValue = sample;
		this.intValue = sample;
		this.longValue = sample;
		this.decimal = new BigDecimal(sample);
		this.doubleValue = sample;
		this.floatValue = sample;
		this.duration = TimeConversion.durationOfTicks(sample);
		this.userData = UserDataEnumeration.forValue(sample);
		this.string = String.format(Locale.getDefault(), "The Current Sample Value Is %s.", sample);
	}

	public final void setValues(short sample, short maxSamples) {
		// we have to set each numeric value, and we want to demonstrate a range of
		// values.
		this.shortValue = (short) (Short.MIN_VALUE + (((Short.MAX_VALUE - Short.MIN_VALUE) / maxSamples) * sample));

		this.intValue = Integer.MIN_VALUE + ((Integer.MAX_VALUE / (maxSamples / 2)) * (sample / 2))
				+ ((Integer.MAX_VALUE / (maxSamples / 2)) * (sample / 2));

		this.longValue = Long.MIN_VALUE + ((Long.MAX_VALUE / (maxSamples / 2)) * (sample / 2))
				+ ((Long.MAX_VALUE / (maxSamples / 2)) * (sample / 2));

		// TODO RKELLIHER review this...
		this.decimal = BigDecimal.valueOf(Long.MIN_VALUE).add(BigDecimal.valueOf(Long.MAX_VALUE)
				.divide((BigDecimal.valueOf(maxSamples).divide(BigDecimal.valueOf(2))))
				.multiply(((BigDecimal.valueOf(sample).divide(BigDecimal.valueOf(2)))).add(BigDecimal
						.valueOf(Long.MAX_VALUE).divide(((BigDecimal.valueOf(maxSamples).divide(BigDecimal.valueOf(2))))
								.multiply(((BigDecimal.valueOf(sample).divide(BigDecimal.valueOf(2)))))))));

		this.doubleValue = -Double.MAX_VALUE + ((Double.MAX_VALUE - -Double.MAX_VALUE) / maxSamples) * sample;

		this.floatValue = -Float.MAX_VALUE + ((Float.MAX_VALUE - -Float.MAX_VALUE) / maxSamples) * sample;

		this.duration = TimeConversion.durationOfTicks(this.longValue); // just use the long as a # of ticks.

		this.userData = UserDataEnumeration.forValue(sample);

		this.string = String.format(Locale.getDefault(), "The Current Sample Value Is %s.", sample);
	}
}