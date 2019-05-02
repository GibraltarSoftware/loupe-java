package com.onloupe.api.metrics;

import com.onloupe.agent.metrics.SamplingType;
import com.onloupe.agent.metrics.annotation.SampledMetricClass;
import com.onloupe.agent.metrics.annotation.SampledMetricDivisor;
import com.onloupe.agent.metrics.annotation.SampledMetricInstanceName;
import com.onloupe.agent.metrics.annotation.SampledMetricValue;

@SampledMetricClass(namespace = "UserSampledObject", categoryName = "Attributes.Unit Test Data")
public class UserSampledObject
{
	private int _PrimaryValue;
	private int _SecondaryValue;
	private String _InstanceName;

	public UserSampledObject()
	{
		_PrimaryValue = 0;
		_SecondaryValue = 1;
		_InstanceName = "Dummy instance";
	}

	public UserSampledObject(int primaryValue)
	{
		_PrimaryValue = primaryValue;
		_SecondaryValue = 1;
		_InstanceName = "Dummy instance";
	}

	public UserSampledObject(int primaryValue, int secondaryValue)
	{
		_PrimaryValue = primaryValue;
		_SecondaryValue = secondaryValue;
		_InstanceName = "Dummy instance";
	}

	public UserSampledObject(String instanceName)
	{
		_PrimaryValue = 0;
		_SecondaryValue = 1;
		_InstanceName = instanceName;
	}

	public UserSampledObject(String instanceName, int primaryValue)
	{
		_PrimaryValue = primaryValue;
		_SecondaryValue = 1;
		_InstanceName = instanceName;
	}

	public UserSampledObject(String instanceName, int primaryValue, int secondaryValue)
	{
		_PrimaryValue = primaryValue;
		_SecondaryValue = secondaryValue;
		_InstanceName = instanceName;
	}

	public final void setValue(int primaryValue)
	{
		_PrimaryValue = primaryValue;
		_SecondaryValue = 1;
	}

	public final void setValue(int primaryValue, int secondaryValue)
	{
		_PrimaryValue = primaryValue;
		_SecondaryValue = secondaryValue;
	}

	public final void setInstanceName(String instanceName)
	{
		_InstanceName = instanceName;
	}

	@SampledMetricValue(counterName = "IncrementalCount", samplingType = SamplingType.INCREMENTAL_COUNT, description="Unit test sampled metric using the incremental count calculation routine")
	@SampledMetricValue(counterName = "IncrementalFraction", samplingType = SamplingType.INCREMENTAL_FRACTION, description = "Unit test sampled metric using the incremental fraction calculation routine.  Rare, but fun.")
	@SampledMetricValue(counterName = "TotalCount", samplingType = SamplingType.TOTAL_COUNT, description = "Unit test sampled metric using the Total Count calculation routine.  Very common.")
	@SampledMetricValue(counterName = "TotalFraction", samplingType = SamplingType.TOTAL_FRACTION, description = "Unit test sampled metric using the Total Fraction calculation routine.  Rare, but rounds us out.")
	@SampledMetricValue(counterName = "RawCount", samplingType = SamplingType.RAW_COUNT, description = "Unit test sampled metric using the Raw Count calculation routine, which we will then average to create sample intervals.")
	@SampledMetricValue(counterName = "RawFraction", samplingType = SamplingType.RAW_FRACTION, description = "Unit test sampled metric using the Raw Fraction calculation routine.  Fraction types aren't common.")
	public final int getPrimaryValue()
	{
		return _PrimaryValue;
	}
	public final void setPrimaryValue(int value)
	{
		_PrimaryValue = value;
	}

	@SampledMetricDivisor(counterName = "IncrementalFraction")
	@SampledMetricDivisor(counterName = "TotalFraction")
	@SampledMetricDivisor(counterName = "RawFraction")
	public final int getSecondaryValue()
	{
		return _SecondaryValue;
	}
	public final void setSecondaryValue(int value)
	{
		_SecondaryValue = value;
	}

	@SampledMetricInstanceName
	public final String getInstanceName()
	{
		return _InstanceName;
	}
}