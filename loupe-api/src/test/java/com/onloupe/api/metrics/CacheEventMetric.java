package com.onloupe.api.metrics;

import com.onloupe.agent.metrics.SummaryFunction;
import com.onloupe.agent.metrics.annotation.EventMetricClass;
import com.onloupe.agent.metrics.annotation.EventMetricValue;

/** 
 Log event metrics using a single object
*/
@EventMetricClass(namespace = "GibraltarSample", categoryName = "Database.Engine", counterName = "Cache - Declarative", caption = "Simple Cache", description = "Performance metrics for the database engine.")
public class CacheEventMetric
{
	public CacheEventMetric(int pagesLoaded)
	{
		setPages(pagesLoaded);
		setSize(pagesLoaded * 8192);
	}

	private int _Pages;

	@EventMetricValue(name = "pages", summaryFunction = SummaryFunction.AVERAGE, caption = "Pages", description = "Total number of pages in cache.")
	public final int getPages()
	{
		return _Pages;
	}
	private void setPages(int value)
	{
		_Pages = value;
	}

	private int _Size;

	@EventMetricValue(name = "size", summaryFunction = SummaryFunction.AVERAGE, unitCaption = "Bytes", caption = "Cache Size", description = "Total number of bytes used by pages in cache.")
	public final int getSize()
	{
		return _Size;
	}
	private void setSize(int value)
	{
		_Size = value;
	}
}