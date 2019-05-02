package com.onloupe.model.system;

/** 
 The user tracking mode for an application
*/
public enum ApplicationUserMode
{
	/** 
	 User tracking is disabled for this application
	*/
	NONE(0),

	/** 
	 The application runs as a single user which is the process user
	*/
	SINGLE_USER(1),

	/** 
	 The application can impersonate many users
	*/
	MULTI_USER(2);

	public static final int SIZE = java.lang.Integer.SIZE;

	private int intValue;
	private static java.util.HashMap<Integer, ApplicationUserMode> mappings;
	private static java.util.HashMap<Integer, ApplicationUserMode> getMappings()
	{
		if (mappings == null)
		{
			synchronized (ApplicationUserMode.class)
			{
				if (mappings == null)
				{
					mappings = new java.util.HashMap<Integer, ApplicationUserMode>();
				}
			}
		}
		return mappings;
	}

	private ApplicationUserMode(int value)
	{
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static ApplicationUserMode forValue(int value)
	{
		return getMappings().get(value);
	}
}