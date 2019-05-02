package com.onloupe.api.metrics;

import com.onloupe.agent.metrics.EventMetric;
import com.onloupe.agent.metrics.EventMetricDefinition;
import com.onloupe.agent.metrics.EventMetricSample;
import com.onloupe.agent.metrics.SummaryFunction;
import com.onloupe.core.util.OutObject;
import com.onloupe.core.util.RefObject;

/** 
 Write event metrics using different approaches
*/
public final class EventMetricExample
{
	/** 
	 Record an event metric using a programmatic declaration
	 
	 @param pagesLoaded
	*/
	public static void recordCacheMetric(int pagesLoaded)
	{
		EventMetricDefinition cacheMetric;

		//so we can be called multiple times we want to see if the definition already exists.
		OutObject<EventMetricDefinition> tempOutCacheMetric = new OutObject<EventMetricDefinition>();
		if (EventMetricDefinition.tryGetValue("GibraltarSample", "Database.Engine", "Cache", tempOutCacheMetric) == false)
		{
		cacheMetric = tempOutCacheMetric.argValue;
			cacheMetric = EventMetricDefinition.builder("GibraltarSample", "Database.Engine", "Cache").build();

			//add the values (that are part of the definition)
			cacheMetric.addValue("pages", Integer.class, SummaryFunction.AVERAGE, "Pages", "Pages in Cache", "Total number of pages in cache");
			cacheMetric.addValue("size", Integer.class, SummaryFunction.AVERAGE, "Bytes", "Cache Size", "Total number of bytes used by pages in cache");

			//and now that we're done, we need to register this definition.  This locks the definition
			//and makes it go live.  Note that it's based by ref because if another thread registered the same metric, we'll get the
			//registered object (whoever one the race), not necessarily the one we've just created to pass in.
			RefObject<EventMetricDefinition> tempRefCacheMetric = new RefObject<EventMetricDefinition>(cacheMetric);
			EventMetricDefinition.register(tempRefCacheMetric);
		cacheMetric = tempRefCacheMetric.argValue;
		}
	else
	{
		cacheMetric = tempOutCacheMetric.argValue;
	}

		//Now we can get the specific metric we want to record samples under (this is an instance of the definition)
		EventMetric cacheEventMetric = EventMetric.register(cacheMetric, null);

		//now go ahead and write that sample.
		EventMetricSample newSample = cacheEventMetric.createSample();
		newSample.setValue("pages", pagesLoaded);
		newSample.setValue("size", pagesLoaded * 8196);
		newSample.write();
	}

	/** 
	 Record an event metric using an object
	 
	 @param pagesLoaded
	*/
	public static void recordCacheMetricByObject(int pagesLoaded)
	{
		CacheEventMetric sample = new CacheEventMetric(pagesLoaded);
		EventMetric.write(sample);
	}
}