package com.onloupe.model.data;

public enum ProcessorArchitecture {

	/**
	 * 
	 */
	UNKNOWN,

	/**
	 * 
	 */
	X86,

	/**
	 * 
	 */
	AMD64,

	/**
	 * 
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
