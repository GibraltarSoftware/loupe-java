package com.onloupe.api.metrics;

import com.onloupe.agent.metrics.SamplingType;
import com.onloupe.agent.metrics.annotation.SampledMetricClass;
import com.onloupe.agent.metrics.annotation.SampledMetricInstanceName;
import com.onloupe.agent.metrics.annotation.SampledMetricValue;

@SampledMetricClass(namespace = "SimpleMetricUsage", categoryName = "Temperature")
public class UserInstancedObject
{
	private static int _BaseTemperature = 10;
	private int _InstanceNumber;

	public UserInstancedObject(int instanceNumber)
	{
		_InstanceNumber = instanceNumber;
	}

	public static void setTemperature(int baseTemperature)
	{
		_BaseTemperature = baseTemperature;
	}

	@SampledMetricInstanceName
	public final String getMetricInstanceName()
	{
		return String.format("Experiment %1$s", _InstanceNumber);
	}

	@SampledMetricValue(counterName = "Experiment Temperature", samplingType = SamplingType.RAW_COUNT, description="This tracks the temperature of the various experiments.")
	public final int getTemperature()
	{
		return _BaseTemperature + _InstanceNumber;
	}
}