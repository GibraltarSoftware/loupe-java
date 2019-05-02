package com.onloupe.api.metrics;


/** 
 This is a user-provided data object which implements multiple events through inheritance and interfaces.
*/
public class UserMultipleEventObject extends UserEventObject implements IEventMetricOne, IEventMetricThree
{
	public UserMultipleEventObject(String instanceName)
	{
		super(instanceName);
		// Just rely on our base constructor.
	}

}