package com.onloupe.core.server.data;

public enum BootModeXml {

	/**
	 * <remarks/>
	 */
	UNKNOWN,

	/**
	 * <remarks/>
	 */
	NORMAL,

	/**
	 * <remarks/>
	 */
	FAILSAFE,

	/**
	 * <remarks/>
	 */
	FAILSAFEWITHNETWORK;

	public static final int SIZE = java.lang.Integer.SIZE;

	public int getValue() {
		return this.ordinal();
	}

	public static BootModeXml forValue(int value) {
		return values()[value];
	}
}