package com.onloupe.core.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 * The Class Multiplexer.
 */
public class Multiplexer {
		
	/** The await termination seconds. */
	private long awaitTerminationSeconds;
	
	/** The initialized. */
	private boolean initialized = false;
	
	/** The executor service. */
	private ScheduledExecutorService executorService;
	
	/** The multiplexer. */
	private static Multiplexer multiplexer;
	
	/**
	 * Instantiates a new multiplexer.
	 *
	 * @param awaitTerminationSeconds the await termination seconds
	 * @param threads the threads
	 */
	private Multiplexer(long awaitTerminationSeconds, int threads) {
		super();
		this.awaitTerminationSeconds = awaitTerminationSeconds;
		this.executorService = Executors.newScheduledThreadPool(threads, new GibraltarThreadFactory("Loupe Multiplexer"));
		this.initialized = true;
	}

	/**
	 * Initialize.
	 */
	public static void initialize() {
		initialize(30, 5);
	}
	
	/**
	 * Initialize.
	 *
	 * @param awaitTerminationSeconds the await termination seconds
	 * @param threads the threads
	 */
	public static void initialize(long awaitTerminationSeconds, int threads) {
		if (!isInitialized()) {
			multiplexer = new Multiplexer(awaitTerminationSeconds, threads);
		}
	}
	
	/**
	 * Run.
	 *
	 * @param runnable the runnable
	 */
	public static void run(Runnable runnable) {
		if (!isInitialized()) {
			initialize();
		}

		multiplexer.executorService.execute(runnable);
	}
	
	/**
	 * Schedule.
	 *
	 * @param runnable the runnable
	 * @param initialDelay the initial delay
	 * @param delay the delay
	 * @param unit the unit
	 * @return the scheduled future
	 */
	public static ScheduledFuture<?> schedule(Runnable runnable, long initialDelay, long delay, TimeUnit unit) {
		if (!isInitialized()) {
			initialize();
		}
		return multiplexer.executorService.scheduleWithFixedDelay(runnable, initialDelay, delay, unit);
	}

	/**
	 * Shutdown.
	 */
	public static void shutdown() {
		if (isInitialized()) {
			multiplexer.doShutdown();
		}
	}
	
	/**
	 * Checks if is initialized.
	 *
	 * @return true, if is initialized
	 */
	private static boolean isInitialized() {
		return multiplexer != null && multiplexer.initialized;
	}
	
	/**
	 * Do shutdown.
	 */
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
	
	/**
	 * Reset.
	 */
	public static void reset() {
		multiplexer = null;
	}
}
