package com.onloupe.model.data;

/**
 * Indicates the default way to interpret multiple values for display purposes
 */
public enum EventMetricValueTrend {
	/**
	 * Average all of the values within each sample range to determine the displayed
	 * value.
	 */
	AVERAGE(0),

	/**
	 * Add all of the values within each sample range to determine the displayed
	 * value.
	 */
	SUM(1),

	/**
	 * An average of all values up through the end of the sample range.
	 */
	RUNNING_AVERAGE(2),

	/**
	 * The sum of all values up through the end of the sample range.
	 */
	RUNNING_SUM(3),

	/**
	 * The number of values within each sample range.
	 */
	COUNT(4);

	public static final int SIZE = java.lang.Integer.SIZE;

	private int intValue;
	private static java.util.HashMap<Integer, EventMetricValueTrend> mappings;

	private static java.util.HashMap<Integer, EventMetricValueTrend> getMappings() {
		if (mappings == null) {
			synchronized (EventMetricValueTrend.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, EventMetricValueTrend>();
				}
			}
		}
		return mappings;
	}

	private EventMetricValueTrend(int value) {
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}

	public static EventMetricValueTrend forValue(int value) {
		return getMappings().get(value);
	}
}