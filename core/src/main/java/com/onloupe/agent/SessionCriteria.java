package com.onloupe.agent;

/**
 * Selection criteria for session inclusion in a session package
 */
public enum SessionCriteria {
	/**
	 * Default. Includes no sessions.
	 */
	NONE(0), COMPLETED(1), NEW(2), CRASHED(4), CRITICAL(8), ERROR(16), WARNING(32), ACTIVE(64), ALL_SESSIONS(65);

	/**
	 * Include all sessions including the session for the current process regardless
	 * of whether they've been sent before.
	 */
	public static final int SIZE = java.lang.Integer.SIZE;

	private int intValue;
	private static java.util.HashMap<Integer, SessionCriteria> mappings;

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

	private SessionCriteria(int value) {
		this.intValue = value;
		synchronized (SessionCriteria.class) {
			getMappings().put(value, this);
		}
	}

	public int getValue() {
		return this.intValue;
	}

	public static SessionCriteria forValue(int value) {
		synchronized (SessionCriteria.class) {
			return getMappings().get(value);
		}
	}
}