package com.onloupe.api.metrics;

import com.onloupe.agent.metrics.SampledMetric;
import com.onloupe.agent.metrics.SampledMetricDefinition;
import com.onloupe.agent.metrics.SamplingType;
import com.onloupe.core.util.OutObject;

/** 
 Sampled metric example
*/
public final class SampledMetricExample
{
	/** 
	 Snapshot cache metrics
	 
	 @param pagesLoaded
	*/
	public static void recordCacheMetric(int pagesLoaded)
	{
		SampledMetricDefinition pageMetricDefinition;

		//since sampled metrics have only one value per metric, we have to create multiple metrics (one for every value)
		OutObject<SampledMetricDefinition> tempOutPageMetricDefinition = new OutObject<SampledMetricDefinition>();
		if (SampledMetricDefinition.tryGetValue("GibraltarSample", "Database.Engine", "Cache Pages", tempOutPageMetricDefinition) == false)
		{
		pageMetricDefinition = tempOutPageMetricDefinition.argValue;
			//doesn't exist yet - add it in all of its glory.  This call is MT safe - we get back the object in cache even if registered on another thread.
			pageMetricDefinition = SampledMetricDefinition.register("GibraltarSample", "Database.Engine", "cachePages", SamplingType.RAW_COUNT, "Pages", "Cache Pages", "The number of pages in the cache");
		}
	else
	{
		pageMetricDefinition = tempOutPageMetricDefinition.argValue;
	}

		//now that we know we have the definitions, make sure we've defined the metric instances.
		SampledMetric pageMetric = SampledMetric.register(pageMetricDefinition, null);

		//now go ahead and write those samples....
		pageMetric.writeSample(pagesLoaded);

		//Continue for our second metric.
		SampledMetricDefinition sizeMetricDefinition;
		OutObject<SampledMetricDefinition> tempOutSizeMetricDefinition = new OutObject<SampledMetricDefinition>();
		if (SampledMetricDefinition.tryGetValue("GibraltarSample", "Database.Engine", "Cache Size", tempOutSizeMetricDefinition) == false)
		{
		sizeMetricDefinition = tempOutSizeMetricDefinition.argValue;
			//doesn't exist yet - add it in all of its glory  This call is MT safe - we get back the object in cache even if registered on another thread.
			sizeMetricDefinition = SampledMetricDefinition.register("GibraltarSample", "Database.Engine", "cacheSize", SamplingType.RAW_COUNT, "Bytes", "Cache Size", "The number of bytes used by pages in the cache");
		}
	else
	{
		sizeMetricDefinition = tempOutSizeMetricDefinition.argValue;
	}

		SampledMetric sizeMetric = SampledMetric.register(sizeMetricDefinition, null);
		sizeMetric.writeSample(pagesLoaded * 8196);
	}

	/** 
	 Snapshot cache metrics using fewest lines of code
	 
	 @param pagesLoaded
	*/
	public static void recordCacheMetricShortestCode(int pagesLoaded)
	{
		//Alternately, it can be done in a single line of code each, although somewhat less readable.  Note the WriteSample call after the Register call.
		SampledMetric.register("GibraltarSample", "Database.Engine", "cachePages", SamplingType.RAW_COUNT, "Pages", "Cache Pages", "The number of pages in the cache", null).writeSample(pagesLoaded);
		SampledMetric.register("GibraltarSample", "Database.Engine", "cacheSize", SamplingType.RAW_COUNT, "Bytes", "Cache Size", "The number of bytes used by pages in the cache", null).writeSample(pagesLoaded * 8196);
	}

	/** 
	 Record a snapshot cache metric using an object
	 
	 @param pagesLoaded
	*/
	public static void recordCacheMetricByObject(int pagesLoaded)
	{
		//by using an object with the appropriate attributes we can do it in one line - even though it writes multiple values.
		SampledMetric.write(new CacheSampledMetric(pagesLoaded));
	}
}