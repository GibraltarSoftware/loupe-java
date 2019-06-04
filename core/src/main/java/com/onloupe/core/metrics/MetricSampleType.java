package com.onloupe.core.metrics;

// TODO: Auto-generated Javadoc
/**
 * Determines what the raw data for a given sampled metric is, and how it has to
 * be processed to produce final values.
 * 
 * In many cases it is necessary to store raw facts that are translated into the
 * final display value during the display process so that they work regardless
 * of time resolution. For example, to determine the percentage of processor
 * time used for an activity, you need to know a time interval to look across
 * (say per second, per hour, etc.), how many units of work were possible during
 * that interval (time slices of the processor) and how many were used by the
 * process. By specifying the TotalFraction type, the metric display system will
 * automatically inspect the raw and baseline values then translate them into a
 * percentage. This enumeration is conceptually similar to the Performance
 * Counter Type enumeration provided by the runtime, but has been simplified for
 * easier use.
 */
public enum MetricSampleType {
	/**
	 * Each sample value is in final form for display as of the timestamp of the
	 * sample.
	 */
	RAW_COUNT(0),

	/**
	 * Each sample value has the numerator and denominator of a fraction for display
	 * as of the timestamp of the sample.
	 */
	RAW_FRACTION(1),

	/**
	 * Each sample is the incremental change since the prior sample as of the
	 * timestamp of the sample.
	 */
	INCREMENTAL_COUNT(2),

	/**
	 * Each sample has the numerator and denominator expressed as the incremental
	 * change since the prior sample as of the timestamp of the sample.
	 */
	INCREMENTAL_FRACTION(3),

	/** Each sample value is the cumulative total up to the timestamp of the sample. */
	TOTAL_COUNT(4),

	/**
	 * Each sample value has the numerator and denominator expressed as the total up
	 * to the timestamp of the sample.
	 */
	TOTAL_FRACTION(5);

	/** The Constant SIZE. */
	public static final int SIZE = java.lang.Integer.SIZE;

	/** The int value. */
	private int intValue;
	
	/** The mappings. */
	private static java.util.HashMap<Integer, MetricSampleType> mappings;

	/**
	 * Gets the mappings.
	 *
	 * @return the mappings
	 */
	private static java.util.HashMap<Integer, MetricSampleType> getMappings() {
		if (mappings == null) {
			synchronized (MetricSampleType.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, MetricSampleType>();
				}
			}
		}
		return mappings;
	}

	/**
	 * Instantiates a new metric sample type.
	 *
	 * @param value the value
	 */
	private MetricSampleType(int value) {
		this.intValue = value;
		getMappings().put(value, this);
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public int getValue() {
		return this.intValue;
	}

	/**
	 * For value.
	 *
	 * @param value the value
	 * @return the metric sample type
	 */
	public static MetricSampleType forValue(int value) {
		return getMappings().get(value);
	}
}