package com.onloupe.api.metrics;

import com.onloupe.agent.metrics.SamplingType;
import com.onloupe.agent.metrics.annotation.SampledMetricClass;
import com.onloupe.agent.metrics.annotation.SampledMetricValue;

/** 
 Log sampled metrics using a single object
*/
@SampledMetricClass(namespace = "GibraltarSample", categoryName = "Database.Engine")
public class CacheSampledMetric
{
	public CacheSampledMetric(int pagesLoaded)
	{
		setPages(pagesLoaded);
		setSize(pagesLoaded * 8192); //VistaDB.Engine.Core.IO.DiskPage.PageSize;
	}

	private int _Pages;

	@SampledMetricValue(counterName = "pages", samplingType = SamplingType.RAW_COUNT, caption = "Pages in Cache", description = "Total number of pages in cache")
	public final int getPages()
	{
		return _Pages;
	}
	private void setPages(int value)
	{
		_Pages = value;
	}

	private int _Size;

	@SampledMetricValue(counterName = "size", samplingType = SamplingType.RAW_COUNT, unitCaption = "Bytes", caption = "Cache Size", description = "Total number of bytes used by pages in cache")
	public final int getSize()
	{
		return _Size;
	}
	private void setSize(int value)
	{
		_Size = value;
	}
}