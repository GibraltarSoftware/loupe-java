package com.onloupe.model.data;

public enum ProcessorArchitecture {

	/**
	 * <remarks/>
	 */
	UNKNOWN,

	/**
	 * <remarks/>
	 */
	X86,

	/**
	 * <remarks/>
	 */
	AMD64,

	/**
	 * <remarks/>
	 */
	IA64;

	public static final int SIZE = java.lang.Integer.SIZE;

	public int getValue() {
		return this.ordinal();
	}

	public static ProcessorArchitecture forValue(int value) {
		return values()[value];
	}
	
}
