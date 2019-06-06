package com.onloupe.core.data;


/**
 * The different path types that Gibraltar uses.
 */
public enum PathType {
	
	/** The place for the agent to record new session information. */
	COLLECTION(0),

	/** The session repository for sessions the user wants to keep. */
	REPOSITORY(1),

	/** The shared folder for inter-agent discovery (like for live sessions). */
	DISCOVERY(5);

	/** The Constant SIZE. */
	public static final int SIZE = java.lang.Integer.SIZE;

	/** The int value. */
	private int intValue;
	
	/** The mappings. */
	private static java.util.HashMap<Integer, PathType> mappings;

	/**
	 * Gets the mappings.
	 *
	 * @return the mappings
	 */
	private static java.util.HashMap<Integer, PathType> getMappings() {
		if (mappings == null) {
			synchronized (PathType.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, PathType>();
				}
			}
		}
		return mappings;
	}

	/**
	 * Instantiates a new path type.
	 *
	 * @param value the value
	 */
	private PathType(int value) {
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
	 * @return the path type
	 */
	public static PathType forValue(int value) {
		return getMappings().get(value);
	}
}