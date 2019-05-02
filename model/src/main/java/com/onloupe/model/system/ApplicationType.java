package com.onloupe.model.system;

/** 
 The type of process the application was run as.
*/
public enum ApplicationType
{
	/** 
	 The application type couldn't be determined.
	*/
	UNKNOWN(0),

	/** 
	 A windows console application.  Can also include windows services running in console mode.
	*/
	CONSOLE(1),

	/** 
	 A Windows Smart Client application (a traditional windows application)
	*/
	WINDOWS(2),

	/** 
	 A Windows Service application.
	*/
	SERVICE(3),

	/** 
	 A Web Application running in the ASP.NET framework.
	*/
	ASP_NET(4);

	public static final int SIZE = java.lang.Integer.SIZE;

	private int intValue;
	private static java.util.HashMap<Integer, ApplicationType> mappings;
	private static java.util.HashMap<Integer, ApplicationType> getMappings()
	{
		if (mappings == null)
		{
			synchronized (ApplicationType.class)
			{
				if (mappings == null)
				{
					mappings = new java.util.HashMap<Integer, ApplicationType>();
				}
			}
		}
		return mappings;
	}

	private ApplicationType(int value)
	{
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static ApplicationType forValue(int value)
	{
		return getMappings().get(value);
	}
}