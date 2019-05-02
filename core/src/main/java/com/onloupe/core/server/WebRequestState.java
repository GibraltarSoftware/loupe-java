package com.onloupe.core.server;

/**
 * The state of a web request
 */
public enum WebRequestState {
	/**
	 * Not yet processed
	 */
	NEW(0),

	/**
	 * Completed successfully.
	 */
	COMPLETED(1),

	/**
	 * Canceled before it could be completed
	 */
	CANCELED(2),

	/**
	 * Attempted but generated an error.
	 */
	ERROR(3);

	public static final int SIZE = java.lang.Integer.SIZE;

	private int intValue;
	private static java.util.HashMap<Integer, WebRequestState> mappings;

	private static java.util.HashMap<Integer, WebRequestState> getMappings() {
		if (mappings == null) {
			synchronized (WebRequestState.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, WebRequestState>();
				}
			}
		}
		return mappings;
	}

	private WebRequestState(int value) {
		this.intValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return this.intValue;
	}

	public static WebRequestState forValue(int value) {
		return getMappings().get(value);
	}
}