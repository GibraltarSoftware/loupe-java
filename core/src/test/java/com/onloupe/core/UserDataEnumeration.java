package com.onloupe.core;

/**
 * This is a standin for any user defined data enumeration (not in our normal
 * libraries)
 */
public enum UserDataEnumeration {
	/**
	 * The experiment completed successfully
	 */
	SUCCESS,

	/**
	 * The experiment was not completed because the user canceled it
	 */
	CANCEL,

	/**
	 * The experiment was terminated early because of a communication failure
	 */
	QUIT;

	public static final int SIZE = java.lang.Integer.SIZE;

	public int getValue() {
		return this.ordinal();
	}

	public static UserDataEnumeration forValue(int value) {
		return values()[value];
	}
}