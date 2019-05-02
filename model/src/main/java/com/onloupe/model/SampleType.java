package com.onloupe.model;

/** 
 The method of data collection done by the metric
 
 
 Metrics are either sampled or event oriented.  Sampled metrics are well defined for all
 time between the start and end of the metric, meaning they have a value.  This value's accuracy compared to
 reality is dependent on the frequency of sampling (the Sample Interval).  For example, all Windows performance
 counter metrics are sampled metrics.
 Event metrics are undefined between occurrences.  For example, an IIS server log represents a set of event
 metric values - each one is an event that has additional information worth tracking, but the value is undefined
 between events.
 
*/
public enum SampleType
{
	/** 
	 Metric values are contiguous samples of the measured value
	*/
	SAMPLED(0),

	/** 
	 Metric values are isolated events with additional information.
	*/
	EVENT(1);

	public static final int SIZE = java.lang.Integer.SIZE;

	private int intValue;
	private static java.util.HashMap<Integer, SampleType> mappings;
	private static java.util.HashMap<Integer, SampleType> getMappings()
	{
		if (mappings == null)
		{
			synchronized (SampleType.class)
			{
				if (mappings == null)
				{
					mappings = new java.util.HashMap<Integer, SampleType>();
				}
			}
		}
		return mappings;
	}

	private SampleType(int value)
	{
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static SampleType forValue(int value)
	{
		return getMappings().get(value);
	}
}