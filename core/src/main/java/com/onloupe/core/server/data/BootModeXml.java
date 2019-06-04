package com.onloupe.core.server.data;

// TODO: Auto-generated Javadoc
/**
 * The Enum BootModeXml.
 */
public enum BootModeXml {

	/** The unknown. */
	UNKNOWN,

	/** The normal. */
	NORMAL,

	/** The failsafe. */
	FAILSAFE,

	/** The failsafewithnetwork. */
	FAILSAFEWITHNETWORK;

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
	 * @return the boot mode xml
	 */
	public static BootModeXml forValue(int value) {
		return values()[value];
	}
}