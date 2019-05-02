package com.onloupe.model.system;

/** 
 Operating System Boot Mode
*/
public enum OSBootMode
{
	/** 
	 Normal Boot Mode
	*/
	NORMAL,

	/** 
	 Failsafe Boot Mode
	*/
	FAIL_SAFE,

	/** 
	 Failsafe With Network Boot Mode
	*/
	FAIL_SAFE_WITH_NETWORK;

	public static final int SIZE = java.lang.Integer.SIZE;

	public int getValue()
	{
		return this.ordinal();
	}

	public static OSBootMode forValue(int value)
	{
		return values()[value];
	}
}