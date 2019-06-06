package com.onloupe.agent;


/**
 * Selection criteria for session inclusion in a session package.
 */
public enum SessionCriteria {
	/**
	 * Default. Includes no sessions.
	 */
	NONE(0), 
 /** The completed. */
 COMPLETED(1), 
 /** The new. */
 NEW(2), 
 /** The crashed. */
 CRASHED(4), 
 /** The critical. */
 CRITICAL(8), 
 /** The error. */
 ERROR(16), 
 /** The warning. */
 WARNING(32), 
 /** The active. */
 ACTIVE(64), 
 /** The all sessions. */
 ALL_SESSIONS(65);

	/**
	 * Include all sessions including the session for the current process regardless
	 * of whether they've been sent before.
	 */
	public static final int SIZE = java.lang.Integer.SIZE;

	/** The int value. */
	private int intValue;
	
	/** The mappings. */
	private static java.util.HashMap<Integer, SessionCriteria> mappings;

	/**
	 * Gets the mappings.
	 *
	 * @return the mappings
	 */
	private static java.util.HashMap<Integer, SessionCriteria> getMappings() {
		if (mappings == null) {
			synchronized (SessionCriteria.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, SessionCriteria>();
				}
			}
		}
		return mappings;
	}

	/**
	 * Instantiates a new session criteria.
	 *
	 * @param value the value
	 */
	private SessionCriteria(int value) {
		this.intValue = value;
		synchronized (SessionCriteria.class) {
			getMappings().put(value, this);
		}
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
	 * @return the session criteria
	 */
	public static SessionCriteria forValue(int value) {
		synchronized (SessionCriteria.class) {
			return getMappings().get(value);
		}
	}
}