package com.onloupe.core.monitor;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.onloupe.agent.metrics.SampledMetric;
import com.onloupe.agent.metrics.SampledMetricDefinition;
import com.onloupe.agent.metrics.SamplingType;
import com.onloupe.core.util.Multiplexer;
import com.onloupe.core.util.SystemUtils;

public class ResourceMonitor implements Closeable {
	private ScheduledFuture<?> backgroundResourceMonitor;

	private SampledMetric usedMemoryMetric;
	private SampledMetric activeThreadsMetric;
	
	public ResourceMonitor() {
		SampledMetricDefinition usedMemoryDefinition = SampledMetricDefinition
				.builder("GibraltarSample", "System", "usedMemory").samplingType(SamplingType.RAW_COUNT)
				.unitCaption("UsedMemory").metricCaption("Used memory in MB").description("Used JVM Memory in MB")
				.build();
		usedMemoryMetric = SampledMetric.register(usedMemoryDefinition, null);

		SampledMetricDefinition activeThreadsDefinition = SampledMetricDefinition
				.builder("GibraltarSample", "System", "activeThreads").samplingType(SamplingType.RAW_COUNT)
				.unitCaption("ActiveThreads").metricCaption("Active threads in JVM")
				.description("Active threads in JVM").build();
		activeThreadsMetric = SampledMetric.register(activeThreadsDefinition, null);
		
		startMonitors();
	}
	
	public void startMonitors() {
		backgroundResourceMonitor = Multiplexer.schedule(new Thread() {
			@Override
			public void run() {
				writeBackgroundResourceMetrics();
			}
		}, 15, 15, TimeUnit.SECONDS);
	}
	
	public void stopMonitors() {
		backgroundResourceMonitor.cancel(true);
	}
	
	private void writeBackgroundResourceMetrics() {
		usedMemoryMetric.writeSample(SystemUtils.getUsedMemory());
		activeThreadsMetric.writeSample(Thread.activeCount());
	}

	@Override
	public void close() throws IOException {
		stopMonitors();
	}

}
