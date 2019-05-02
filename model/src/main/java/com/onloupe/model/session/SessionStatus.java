package com.onloupe.model.session;

/** 
 The current known disposition of the session
*/
public enum SessionStatus
{
	/** 
	 The final status of the session isn't known
	*/
	UNKNOWN(0),

	/** 
	 The application is still running
	*/
	RUNNING(1),

	/** 
	 The application closed normally
	*/
	NORMAL(2),

	/** 
	 The application closed unexpectedly
	*/
	CRASHED(3);

	public static final int SIZE = java.lang.Integer.SIZE;

	private int intValue;
	private static java.util.HashMap<Integer, SessionStatus> mappings;
	private static java.util.HashMap<Integer, SessionStatus> getMappings()
	{
		if (mappings == null)
		{
			synchronized (SessionStatus.class)
			{
				if (mappings == null)
				{
					mappings = new java.util.HashMap<Integer, SessionStatus>();
				}
			}
		}
		return mappings;
	}

	private SessionStatus(int value)
	{
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static SessionStatus forValue(int value)
	{
		return getMappings().get(value);
	}
}