package com.onloupe.core.server.data;


/**
 * The Enum ApplicationTypeXml.
 */
public enum ApplicationTypeXml {

	/** The unknown. */
	UNKNOWN,

	/** The console. */
	CONSOLE,

	/** The windows. */
	WINDOWS,

	/** The service. */
	SERVICE,

	/** The aspnet. */
	ASPNET;

	/** The Constant SIZE. */
	public static final int SIZE = java.lang.Integer.SIZE;

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public int getValue() {
		return this.ordinal();
	}

	/**
	 * For value.
	 *
	 * @param value the value
	 * @return the application type xml
	 */
	public static ApplicationTypeXml forValue(int value) {
		return values()[value];
	}
}