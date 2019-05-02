package com.onloupe.api.metrics;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;

import com.onloupe.agent.metrics.SummaryFunction;
import com.onloupe.agent.metrics.annotation.EventMetricClass;
import com.onloupe.agent.metrics.annotation.EventMetricInstanceName;
import com.onloupe.agent.metrics.annotation.EventMetricValue;
import com.onloupe.core.util.TimeConversion;

/** 
 This is a user-provided data object, demonstrating reflection for event metrics.
*/
@EventMetricClass(namespace = "EventMetricsByAttributesTests", categoryName = "Attributes.Event Metric Data", counterName = "UserDataObject", caption = "Event metric via attributes", description = "Generic user data object used for testing event metrics with a trend for every numeric value type supported.")
public class UserEventObject
{
	private String _InstanceName;
	private short _Short;
	private int _Int;
	private long _Long;
	private double _Decimal;
	private double _Double;
	private float _Float;
	private Duration _Duration;
	private String _String;
	private UserDataEnumeration _Enum = UserDataEnumeration.values()[0];

	public UserEventObject(String instanceName)
	{
		_InstanceName = instanceName;
	}

	public final boolean isTrendableType(java.lang.Class type)
	{
		boolean trendable = false;

		//we're using Is so we can check for compatibile types, not just base types.
		if ((type == Short.class) || (type == Integer.class) || (type == Long.class) || (type == Double.class) || (type == Float.class))
		{
			trendable = true;
		}
		//Now check object types
		else if ((type == LocalDateTime.class) || (type == Duration.class))
		{
			trendable = true;
		}

		return trendable;
	}

	@EventMetricInstanceName
	public final String getInstanceName()
	{
		return _InstanceName;
	}

	@EventMetricValue(name = "short_average", summaryFunction = SummaryFunction.AVERAGE, caption = "Short Average", description = "Data of type Short.")
	@EventMetricValue(name = "short_sum", summaryFunction = SummaryFunction.SUM, caption = "Short Sum", description = "Data of type Short.")
	@EventMetricValue(name = "short_runningaverage", summaryFunction = SummaryFunction.RUNNING_AVERAGE, caption = "Short Running Average", description = "Data of type Short.")
	@EventMetricValue(name = "short_runningsum", summaryFunction = SummaryFunction.RUNNING_SUM, caption = "Short Running Sum", description = "Data of type Short.")
	public final short getShort()
	{
		return _Short;
	}
	public final void setShort(short value)
	{
		_Short = value;
	}

	@EventMetricValue(name = "int_average", summaryFunction = SummaryFunction.AVERAGE, defaultValue = true, caption = "Int Average", description = "Data of type Int.")
	@EventMetricValue(name = "int_sum", summaryFunction = SummaryFunction.SUM, caption = "Int Sum", description = "Data of type Int.")
	public final int getInt()
	{
		return _Int;
	}
	public final void setInt(int value)
	{
		_Int = value;
	}

	@EventMetricValue(name = "long_average", summaryFunction = SummaryFunction.AVERAGE, caption = "Long Average", description = "Data of type Long.")
	@EventMetricValue(name = "long_sum", summaryFunction = SummaryFunction.SUM, caption = "Long Sum", description = "Data of type Long.")
	public final long getLong()
	{
		return _Long;
	}
	public final void setLong(long value)
	{
		_Long = value;
	}

	@EventMetricValue(name = "decimal_average", summaryFunction = SummaryFunction.AVERAGE, caption = "Decimal Average", description = "Data of type Int.")
	@EventMetricValue(name = "decimal_sum", summaryFunction = SummaryFunction.SUM, caption = "Decimal Sum", description = "Data of type Decimal.")
	public final double getDecimal()
	{
		return _Decimal;
	}
	public final void setDecimal(double value)
	{
		_Decimal = value;
	}

	@EventMetricValue(name = "double_average", summaryFunction = SummaryFunction.AVERAGE, caption = "Double Average", description = "Data of type Double.")
	@EventMetricValue(name = "double_sum", summaryFunction = SummaryFunction.SUM, caption = "Double Sum", description = "Data of type Double.")
	public final double getDouble()
	{
		return _Double;
	}
	public final void setDouble(double value)
	{
		_Double = value;
	}

	@EventMetricValue(name = "float_average", summaryFunction = SummaryFunction.AVERAGE, caption = "Float Average", description = "Data of type Float.")
	@EventMetricValue(name = "float_sum", summaryFunction = SummaryFunction.SUM, caption = "Float Sum", description = "Data of type Float.")
	public final float getFloat()
	{
		return _Float;
	}
	public final void setFloat(float value)
	{
		_Float = value;
	}

	@EventMetricValue(name = "timespan_average", summaryFunction = SummaryFunction.AVERAGE, caption = "TimeSpan Average", description = "Data of type TimeSpan.")
	@EventMetricValue(name = "timespan_sum", summaryFunction = SummaryFunction.SUM, caption = "TimeSpan Sum", description = "Data of type TimeSpan.")
	@EventMetricValue(name = "timespan_runningaverage", summaryFunction = SummaryFunction.RUNNING_AVERAGE, caption = "TimeSpan Running Average", description = "Data of type TimeSpan.")
	@EventMetricValue(name = "timespan_runningsum", summaryFunction = SummaryFunction.RUNNING_SUM, caption = "TimeSpan Running Sum", description = "Data of type TimeSpan.")
	public final Duration getDuration()
	{
		return _Duration;
	}
	public final void setDuration(Duration value)
	{
		_Duration = value;
	}

	@EventMetricValue(name = "string", summaryFunction = SummaryFunction.COUNT, caption = "String", description = "Data of type String.")
	public final String getString()
	{
		return _String;
	}
	public final void setString(String value)
	{
		_String = value;
	}

	@EventMetricValue(name = "system.enum", summaryFunction = SummaryFunction.COUNT, caption = "System.Enum", description = "Data of type System.Enum.")
	public final UserDataEnumeration getEnum()
	{
		return _Enum;
	}
	public final void setEnum(UserDataEnumeration value)
	{
		_Enum = value;
	}


	public final void setValues(short sample)
	{
		//we just set each value to the provided value, much faster than calculating the range.
		_Short = sample;
		_Int = sample;
		_Long = sample;
		_Decimal = sample;
		_Double = sample;
		_Float = sample;
		_Duration = TimeConversion.durationOfTicks(sample);
		_Enum = UserDataEnumeration.forValue(sample);
		_String = String.format(Locale.getDefault(), "The Current Sample Value Is %s.", sample);
	}

	private static double interpolateValue(double minValue, double maxValue, short sample, short maxSamples)
	{
		if (sample < 0)
		{
			sample = 0;
		}

		if (sample > maxSamples)
		{
			sample = maxSamples;
		}

		double delta = maxValue - minValue;
		double interval = delta / maxSamples;
		double offset = interval * sample;
		double value = minValue + offset;

		return value;
	}

	public final void setValues(short sample, short maxSamples)
	{
		//we have to set each numeric value, and we want to demonstrate a range of values.
		_Short = (short)interpolateValue(Short.MIN_VALUE, Short.MAX_VALUE, sample, maxSamples);
		_Int = (int)interpolateValue(Integer.MIN_VALUE, Integer.MAX_VALUE, sample, maxSamples);
		_Long = (long)interpolateValue(Long.MIN_VALUE, Long.MAX_VALUE, sample, maxSamples);

		// We have to fudge these closer to 0, or else rounding into and out of double causes overflow on endpoints.
		double minDecimal = Double.MIN_NORMAL + Double.valueOf(7590000000000L);
		double maxDecimal = Double.MAX_VALUE - Double.valueOf(7590000000000L);

		_Decimal = interpolateValue(minDecimal, maxDecimal, sample, maxSamples);

		// More interesting endpoints than their own min and max.
		_Double = interpolateValue(Short.MIN_VALUE, Long.MAX_VALUE, sample, maxSamples);
		_Float = (float)interpolateValue(Short.MIN_VALUE, Long.MAX_VALUE, sample, maxSamples);

		// Negatives wouldn't be sensible, right?  So interpolate from 0 to the largest long instead.
		_Duration = TimeConversion.durationOfTicks((long)interpolateValue(0, Long.MAX_VALUE, sample, maxSamples));

		_Enum = UserDataEnumeration.forValue(sample);

		_String = String.format(Locale.getDefault(), "The Current Sample Value Is %s.", sample);
	}
}