package com.onloupe.core.server;


/**
 * The state of a web request.
 */
public enum WebRequestState {
	
	/** Not yet processed. */
	NEW(0),

	/**
	 * Completed successfully.
	 */
	COMPLETED(1),

	/** Canceled before it could be completed. */
	CANCELED(2),

	/**
	 * Attempted but generated an error.
	 */
	ERROR(3);

	/** The Constant SIZE. */
	public static final int SIZE = java.lang.Integer.SIZE;

	/** The int value. */
	private int intValue;
	
	/** The mappings. */
	private static java.util.HashMap<Integer, WebRequestState> mappings;

	/**
	 * Gets the mappings.
	 *
	 * @return the mappings
	 */
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

	/**
	 * Instantiates a new web request state.
	 *
	 * @param value the value
	 */
	private WebRequestState(int value) {
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
	 * @return the web request state
	 */
	public static WebRequestState forValue(int value) {
		return getMappings().get(value);
	}
}