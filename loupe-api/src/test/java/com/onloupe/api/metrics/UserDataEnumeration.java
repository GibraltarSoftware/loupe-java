package com.onloupe.api.metrics;

/** 
 This is a standin for any user defined data enumeration (not in our normal libraries)
*/
public enum UserDataEnumeration
{
	/** 
	 The experiment completed successfully
	*/
	SUCCESS(0),

	/** 
	 The experiment was not completed because the user canceled it
	*/
	CANCEL(1),

	/** 
	 The experiment was terminated early because of a communication failure
	*/
	QUIT(2);

	public static final int SIZE = java.lang.Integer.SIZE;

	private final int intValue;
	private UserDataEnumeration(int value)
	{
		intValue = value;
		getMappings().put(value, this);
	}
	
	public int getValue()
	{
		return this.ordinal();
	}

	private static java.util.HashMap<Integer, UserDataEnumeration> mappings;
	private static java.util.HashMap<Integer, UserDataEnumeration> getMappings()
	{
		if (mappings == null)
		{
			synchronized (UserDataEnumeration.class)
			{
				if (mappings == null)
				{
					mappings = new java.util.HashMap<Integer, UserDataEnumeration>();
				}
			}
		}
		return mappings;
	}
	
	public static UserDataEnumeration forValue(int value)
	{
		return getMappings().get(value);
	}
}