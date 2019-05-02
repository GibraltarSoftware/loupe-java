package com.onloupe.core.server.data;

public enum ClientRepositoryStatusXml {

	/**
	 * <remarks/>
	 */
	ACTIVE,

	/**
	 * <remarks/>
	 */
	PENDING,

	/**
	 * <remarks/>
	 */
	INACTIVE;

	public static final int SIZE = java.lang.Integer.SIZE;

	public int getValue() {
		return this.ordinal();
	}

	public static ClientRepositoryStatusXml forValue(int value) {
		return values()[value];
	}
}