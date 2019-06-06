package com.onloupe.core.server.data;


/**
 * The Enum ClientRepositoryStatusXml.
 */
public enum ClientRepositoryStatusXml {

	/** The active. */
	ACTIVE,

	/** The pending. */
	PENDING,

	/** The inactive. */
	INACTIVE;

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
	 * @return the client repository status xml
	 */
	public static ClientRepositoryStatusXml forValue(int value) {
		return values()[value];
	}
}