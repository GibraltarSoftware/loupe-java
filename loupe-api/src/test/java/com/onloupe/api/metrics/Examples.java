package com.onloupe.api.metrics;

import org.junit.jupiter.api.Test;

import com.onloupe.api.LoupeTestsBase;

public class Examples extends LoupeTestsBase
{

	@Test
	public final void recordSampledMetric()
	{
		SampledMetricExample.recordCacheMetric(1);
		SampledMetricExample.recordCacheMetric(2);
		SampledMetricExample.recordCacheMetric(3);
		SampledMetricExample.recordCacheMetric(4);
	}

	@Test
	public final void recordSampledMetricShortestCode()
	{
		SampledMetricExample.recordCacheMetricShortestCode(1);
		SampledMetricExample.recordCacheMetricShortestCode(2);
		SampledMetricExample.recordCacheMetricShortestCode(3);
		SampledMetricExample.recordCacheMetricShortestCode(4);
	}

	@Test
	public final void recordSampledMetricByObject()
	{
		SampledMetricExample.recordCacheMetricByObject(1);
		SampledMetricExample.recordCacheMetricByObject(2);
		SampledMetricExample.recordCacheMetricByObject(3);
		SampledMetricExample.recordCacheMetricByObject(4);
	}

	@Test
	public final void recordEventMetric()
	{
		EventMetricExample.recordCacheMetric(1);
		EventMetricExample.recordCacheMetric(2);
		EventMetricExample.recordCacheMetric(3);
		EventMetricExample.recordCacheMetric(4);
	}

	@Test
	public final void recordEventMetricByObject()
	{
		EventMetricExample.recordCacheMetricByObject(1);
		EventMetricExample.recordCacheMetricByObject(2);
		EventMetricExample.recordCacheMetricByObject(3);
		EventMetricExample.recordCacheMetricByObject(4);
	}
}