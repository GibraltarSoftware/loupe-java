package com.onloupe.agent.metrics;

/**
 * A suggested interval between value samples.
 */
public enum SamplingInterval {
	/**
	 * Use the interval as the data was recorded.
	 */
	DEFAULT(0),

	/**
	 * Use the interval as the data was recorded.
	 */
	SHORTEST(1),

	/**
	 * Use a sampling interval set in milliseconds
	 */
	MILLISECOND(2),

	/**
	 * Use a sampling interval set in seconds.
	 */
	SECOND(3),

	/**
	 * Use a sampling interval set in minutes.
	 */
	MINUTE(4),

	/**
	 * Use a sampling interval set in hours.
	 */
	HOUR(5),

	/**
	 * Use a sampling interval set in days.
	 */
	DAY(6),

	/**
	 * Use a sampling interval set in weeks.
	 */
	WEEK(7),

	/**
	 * Use a sampling interval set in months.
	 */
	MONTH(8);

	public static final int SIZE = java.lang.Integer.SIZE;

	private int intValue;
	private static java.util.HashMap<Integer, SamplingInterval> mappings;

	private static java.util.HashMap<Integer, SamplingInterval> getMappings() {
		if (mappings == null) {
			synchronized (SamplingInterval.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, SamplingInterval>();
				}
			}
		}
		return mappings;
	}

	private SamplingInterval(int value) {
		this.intValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return this.intValue;
	}

	public static SamplingInterval forValue(int value) {
		return getMappings().get(value);
	}
}