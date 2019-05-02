package com.onloupe.agent.metrics;

/**
 * Indicates the default way to interpret multiple values for display purposes
 */
public enum SummaryFunction {
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
	private static java.util.HashMap<Integer, SummaryFunction> mappings;

	private static java.util.HashMap<Integer, SummaryFunction> getMappings() {
		if (mappings == null) {
			synchronized (SummaryFunction.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, SummaryFunction>();
				}
			}
		}
		return mappings;
	}

	private SummaryFunction(int value) {
		this.intValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return this.intValue;
	}

	public static SummaryFunction forValue(int value) {
		return getMappings().get(value);
	}
}