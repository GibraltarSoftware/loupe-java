package com.onloupe.model.system;

/** 
 The various levels of users in the system
*/
public enum UserType
{
	UNKNOWN(0), SYSTEM(1), VIRTUAL(2), REVIEWER(4), FULL(8), ADMINISTRATOR(16);

	/** 
	 Users that have accounts
	*/
	//PUBLIC STATIC FINAL _USER_TYPE _CAN_LOG_IN = NEW _USER_TYPE(4 | 8 | 16);

	/** 
	 Users that can access all application-specific features
	*/
	//PUBLIC STATIC FINAL _USER_TYPE _FULL_APPLICATION_ACCESS = NEW _USER_TYPE(8 | 16);
	
	public static final int SIZE = java.lang.Integer.SIZE;

	private int intValue;
	private static java.util.HashMap<Integer, UserType> mappings;
	private static java.util.HashMap<Integer, UserType> getMappings()
	{
		if (mappings == null)
		{
			synchronized (UserType.class)
			{
				if (mappings == null)
				{
					mappings = new java.util.HashMap<Integer, UserType>();
				}
			}
		}
		return mappings;
	}

	private UserType(int value)
	{
		intValue = value;
		synchronized (UserType.class)
		{
			getMappings().put(value, this);
		}
	}

	public int getValue()
	{
		return intValue;
	}

}