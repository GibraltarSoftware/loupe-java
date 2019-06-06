package com.onloupe.agent.metrics;


/**
 * Determines what the raw data for a given sampled metric is, and how it has to
 * be processed to produce final data point values for display.
 * 
 * 
 * <p>
 * In many cases it is necessary to store raw facts that are translated into the
 * final display value during the display process so that they work regardless
 * of time resolution.
 * </p>
 * <p>
 * For example, to determine the percentage of processor time used for an
 * activity, you need to know a time interval to look across (say per second,
 * per hour, etc.), how many units of work were possible during that interval
 * (time slices of the processor) and how many were used by the process. By
 * specifying the TotalFraction type, the metric display system will
 * automatically inspect the raw and baseline values then translate them into a
 * percentage.
 * </p>
 * <p>
 * For more information on how to design sampled metrics including picking a
 * Sampling Type, see <a href="Metrics_SampledMetricDesign.html">Developer's
 * Reference - Metrics - Designing Sampled Metrics</a>.
 * </p>
 * <p>
 * This enumeration is conceptually similar to the Performance Counter Type
 * enumeration provided by the runtime, but has been simplified for easier use.
 * </p>
 * 
 * @see "Designing Sampled Metrics"
 * @see "SampledMetricDefinition"
 * @see "SampledMetric"
 */
public enum SamplingType {
	/**
	 * Each sample is the raw value for display as this data point.
	 */
	RAW_COUNT(0),

	/**
	 * Each sample has the raw numerator and denominator of a fraction for display
	 * as the value for this data point.
	 */
	RAW_FRACTION(1),

	/**
	 * Each sample is the incremental change in the value for display as this data
	 * point.
	 */
	INCREMENTAL_COUNT(2),

	/**
	 * Each sample has the separate incremental changes to the numerator and
	 * denominator of the fraction for display as this data point.
	 */
	INCREMENTAL_FRACTION(3),

	/**
	 * Each sample is the cumulative total of display value data points.
	 */
	TOTAL_COUNT(4),

	/**
	 * Each sample has the separate cumulative totals of the numerators and
	 * denominators of fraction value data points.
	 */
	TOTAL_FRACTION(5);

	/** The Constant SIZE. */
	public static final int SIZE = java.lang.Integer.SIZE;

	/** The int value. */
	private int intValue;
	
	/** The mappings. */
	private static java.util.HashMap<Integer, SamplingType> mappings;

	/**
	 * Gets the mappings.
	 *
	 * @return the mappings
	 */
	private static java.util.HashMap<Integer, SamplingType> getMappings() {
		if (mappings == null) {
			synchronized (SamplingType.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, SamplingType>();
				}
			}
		}
		return mappings;
	}

	/**
	 * Instantiates a new sampling type.
	 *
	 * @param value the value
	 */
	private SamplingType(int value) {
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
	 * @return the sampling type
	 */
	public static SamplingType forValue(int value) {
		return getMappings().get(value);
	}
}