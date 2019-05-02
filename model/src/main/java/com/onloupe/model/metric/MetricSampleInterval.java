package com.onloupe.model.metric;

/** 
 The requested interval between value samples.
*/
public enum MetricSampleInterval
{
	/** 
	 Use the interval as the data was recorded.
	*/
	DEFAULT(0),

	/** 
	 Use the interval as the data was recorded.
	*/
	SHORTEST(1),

	/** 
	 Use a sampling interval set in milliseconds
	*/
	MILLISECOND(2),

	/** 
	 Use a sampling interval set in seconds.
	*/
	SECOND(3),

	/** 
	 Use a sampling interval set in minutes.
	*/
	MINUTE(4),

	/** 
	 Use a sampling interval set in hours.
	*/
	HOUR(5),

	/** 
	 Use a sampling interval set in days.
	*/
	DAY(6),

	/** 
	 Use a sampling interval set in weeks.
	*/
	WEEK(7),

	/** 
	 Use a sampling interval set in months.
	*/
	MONTH(8);

	public static final int SIZE = java.lang.Integer.SIZE;

	private int intValue;
	private static java.util.HashMap<Integer, MetricSampleInterval> mappings;
	private static java.util.HashMap<Integer, MetricSampleInterval> getMappings()
	{
		if (mappings == null)
		{
			synchronized (MetricSampleInterval.class)
			{
				if (mappings == null)
				{
					mappings = new java.util.HashMap<Integer, MetricSampleInterval>();
				}
			}
		}
		return mappings;
	}

	private MetricSampleInterval(int value)
	{
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static MetricSampleInterval forValue(int value)
	{
		return getMappings().get(value);
	}
}