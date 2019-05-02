package com.onloupe.api.metrics;

import java.time.Duration;
import java.time.OffsetDateTime;

import com.onloupe.agent.metrics.SamplingType;
import com.onloupe.agent.metrics.SummaryFunction;
import com.onloupe.agent.metrics.annotation.EventMetricClass;
import com.onloupe.agent.metrics.annotation.EventMetricInstanceName;
import com.onloupe.agent.metrics.annotation.EventMetricValue;
import com.onloupe.agent.metrics.annotation.SampledMetricClass;
import com.onloupe.agent.metrics.annotation.SampledMetricDivisor;
import com.onloupe.agent.metrics.annotation.SampledMetricInstanceName;
import com.onloupe.agent.metrics.annotation.SampledMetricValue;

@SampledMetricClass(namespace = "PerformanceTestsMetrics", categoryName = "Performance.SampledMetrics.Attributes")
@EventMetricClass(namespace = "PerformanceTestsMetrics", categoryName = "Performance.EventMetrics.Attributes", counterName = "UserEvent", caption="User Event", description="Unit test event metric with typical data.")
public class UserPerformanceObject
{
	private int _PrimaryValue;
	private int _SecondaryValue;
	private String _InstanceName;

	@EventMetricValue(name = "operation", summaryFunction = SummaryFunction.COUNT, caption="Operation", description="The type of file operation being performed.")
	private UserFileOperation _Operation; // We can even pull the value directly from a private field.

	private String _FileName;
	private OffsetDateTime _StartTime;
	private OffsetDateTime _EndTime;

	public UserPerformanceObject()
	{
		_PrimaryValue = 0;
		_SecondaryValue = 1;
		_InstanceName = "Dummy instance";
	}

	public UserPerformanceObject(int primaryValue)
	{
		_PrimaryValue = primaryValue;
		_SecondaryValue = 1;
		_InstanceName = "Dummy instance";
	}

	public UserPerformanceObject(int primaryValue, int secondaryValue)
	{
		_PrimaryValue = primaryValue;
		_SecondaryValue = secondaryValue;
		_InstanceName = "Dummy instance";
	}

	public UserPerformanceObject(String instanceName)
	{
		_PrimaryValue = 0;
		_SecondaryValue = 1;
		_InstanceName = instanceName;
	}

	public UserPerformanceObject(String instanceName, int primaryValue)
	{
		_PrimaryValue = primaryValue;
		_SecondaryValue = 1;
		_InstanceName = instanceName;
	}

	public UserPerformanceObject(String instanceName, int primaryValue, int secondaryValue)
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

	public final void setEventData(String fileName, UserFileOperation operation, OffsetDateTime start, OffsetDateTime end)
	{
		_FileName = fileName;
		_Operation = operation;
		_StartTime = start;
		_EndTime = end;
	}

	@EventMetricValue(name = "fileName", summaryFunction = SummaryFunction.COUNT, caption="File name", description="The name of the file.")
	public final String getFileName()
	{
		return _FileName;
	}

	// TimeSpan is sampled as its Ticks value but ultimately displayed in milliseconds.  We'll call the units "ms".
	// Also, this is the value to graph by default for this metric, and we'll recommend averaging it.
	@EventMetricValue(name = "duration", summaryFunction = SummaryFunction.AVERAGE, unitCaption = "ms", defaultValue=true, caption="Duration", description="The duration for this file operation.")
	public final Duration getDuration()
	{
		return Duration.between(_StartTime, _EndTime); // Compute duration from our start and end timestamps.
	}

	@SampledMetricValue(counterName = "IncrementalCount", samplingType = SamplingType.INCREMENTAL_COUNT, description = "Unit test sampled metric using the incremental count calculation routine")
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
	@EventMetricInstanceName
	public final String getInstanceName()
	{
		return _InstanceName;
	}
}