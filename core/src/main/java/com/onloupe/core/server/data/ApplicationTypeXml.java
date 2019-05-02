package com.onloupe.core.server.data;

public enum ApplicationTypeXml {

	/**
	 * <remarks/>
	 */
	UNKNOWN,

	/**
	 * <remarks/>
	 */
	CONSOLE,

	/**
	 * <remarks/>
	 */
	WINDOWS,

	/**
	 * <remarks/>
	 */
	SERVICE,

	/**
	 * <remarks/>
	 */
	ASPNET;

	public static final int SIZE = java.lang.Integer.SIZE;

	public int getValue() {
		return this.ordinal();
	}

	public static ApplicationTypeXml forValue(int value) {
		return values()[value];
	}
}