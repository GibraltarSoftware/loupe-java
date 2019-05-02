package com.onloupe.core.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Multiplexer {
		
	private long awaitTerminationSeconds;
	private boolean initialized = false;
	private ScheduledExecutorService executorService;
	
	private static Multiplexer multiplexer;
	
	private Multiplexer(long awaitTerminationSeconds, int threads) {
		super();
		this.awaitTerminationSeconds = awaitTerminationSeconds;
		this.executorService = Executors.newScheduledThreadPool(threads, new GibraltarThreadFactory("Loupe Multiplexer"));
		this.initialized = true;
	}

	public static void initialize() {
		initialize(30, 5);
	}
	
	public static void initialize(long awaitTerminationSeconds, int threads) {
		if (!isInitialized()) {
			multiplexer = new Multiplexer(awaitTerminationSeconds, threads);
		}
	}
	
	public static void run(Runnable runnable) {
		if (!isInitialized()) {
			initialize();
		}

		multiplexer.executorService.execute(runnable);
	}
	
	public static ScheduledFuture<?> schedule(Runnable runnable, long initialDelay, long delay, TimeUnit unit) {
		if (!isInitialized()) {
			initialize();
		}
		return multiplexer.executorService.scheduleWithFixedDelay(runnable, initialDelay, delay, unit);
	}

	public static void shutdown() {
		if (isInitialized()) {
			multiplexer.doShutdown();
		}
	}
	
	private static boolean isInitialized() {
		return multiplexer != null && multiplexer.initialized;
	}
	
	private void doShutdown() {
		if (initialized) {
			try {
				if (executorService != null && !executorService.isShutdown()) {
					executorService.shutdown();
					if (!executorService.awaitTermination(awaitTerminationSeconds, TimeUnit.SECONDS)) {
						executorService.shutdownNow();
					}
				}
			} catch (InterruptedException e) {
				// nothing to do. we're shutting down anyway.
			}
			initialized = false;
		}
	}
	
	public static void reset() {
		multiplexer = null;
	}
}
