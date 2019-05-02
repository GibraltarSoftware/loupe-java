package com.onloupe.api.metrics;

public enum UserFileOperation
{
	NONE,
	OPEN,
	CLOSE,
	READ,
	WRITE,
	APPEND,
	FLUSH;

	public static final int SIZE = java.lang.Integer.SIZE;

	public int getValue()
	{
		return this.ordinal();
	}

	public static UserFileOperation forValue(int value)
	{
		return values()[value];
	}
}