package com.onloupe.agent.metrics;


/**
 * Indicates the default way to interpret multiple values for display purposes.
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

	/** The Constant SIZE. */
	public static final int SIZE = java.lang.Integer.SIZE;

	/** The int value. */
	private int intValue;
	
	/** The mappings. */
	private static java.util.HashMap<Integer, SummaryFunction> mappings;

	/**
	 * Gets the mappings.
	 *
	 * @return the mappings
	 */
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

	/**
	 * Instantiates a new summary function.
	 *
	 * @param value the value
	 */
	private SummaryFunction(int value) {
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
	 * @return the summary function
	 */
	public static SummaryFunction forValue(int value) {
		return getMappings().get(value);
	}
}