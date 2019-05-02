package com.onloupe.core.monitor;

import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.onloupe.configuration.AgentConfiguration;
import com.onloupe.configuration.ListenerConfiguration;
import com.onloupe.core.logging.Log;
import com.onloupe.core.serialization.monitor.AssemblyInfoPacket;
import com.onloupe.core.util.SystemUtils;
import com.onloupe.model.metric.MetricSampleInterval;

/**
 * The central listener that manages the configuration of the individual
 * listeners
 */
public final class Listener {
	private static final Object monitorThreadLock = new Object();
	private static final Object listenerLock = new Object();
	private static final Object configLock = new Object();

	private static AgentConfiguration agentConfiguration;
	private static ListenerConfiguration configuration; // the active listener configuration //LOCKED BY CONFIGLOCK
	private static boolean pendingConfigChange; // LOCKED BY CONFIGLOCK
	private static boolean initialized; // LOCKED BY CONFIGLOCK; (update only)
	

	private static Thread monitorThread; // LOCKED BY MONITORTHREADLOCK

	private static MetricSampleInterval samplingInterval = MetricSampleInterval.MINUTE;
	private static OffsetDateTime pollingStarted;
	private static boolean eventsInitialized;

	static {
		// create the background thread we need so we can respond to requests.
		createMonitorThread();
	}

	/**
	 * Apply the provided listener configuration
	 * 
	 * @param localAgentConfiguration
	 */
	public static void initialize(AgentConfiguration localAgentConfiguration) {
		ListenerConfiguration localListenerConfiguration = localAgentConfiguration.getListener();
		// get a configuration lock so we can update the configuration
		synchronized (configLock) {
			// and store the configuration; it's processed by the background thread.
			agentConfiguration = localAgentConfiguration; // Set the top config before the local Listener config.
			configuration = localListenerConfiguration; // Monitor thread looks for this to be non-null before proceeding.
			pendingConfigChange = true;

			// wait for our events to initialize always on our background thread
			while (!eventsInitialized) {
				try {
					configLock.wait(16);
				} catch (InterruptedException e) {
					// do nothing
				}
			}

			configLock.notifyAll();
		}
	}

	/**
	 * Indicates if the listeners have been initialized the first time yet.
	 */
	public static boolean getInitialized() {
		return initialized;
	}

	private static void createMonitorThread() {
		synchronized (monitorThreadLock) {
			monitorThread = new Thread() {
				@Override
				public void run() {
					monitorThreadMain();
				}
			};
			monitorThread.setName("Loupe Agent Monitor"); // name our thread so we can isolate it out of metrics and
															// such
			monitorThread.start();

			monitorThreadLock.notifyAll();
		}
	}

	private static void monitorThreadMain() {
		try {
			// First, we need to make sure we're initialized
			synchronized (configLock) {
				while (configuration == null) {
					try {
						configLock.wait(1000);
					} catch (InterruptedException e) {
						// do nothing
					}
				}

				configLock.notifyAll();
			}

			// we now have our first configuration - go for it. This interacts with Config
			// Log internally as it goes.
			updateMonitorConfiguration();
			
			// now we go into our wait process loop.
			pollingStarted = OffsetDateTime.now();
			while (!Log.isSessionEnding()) // Only do performance polling if we aren't shutting down.
			{
				// mark the start of our cycle
				OffsetDateTime previousPollStart = OffsetDateTime.now(ZoneOffset.UTC); // this realy should be UTC - we
																						// aren't storing it.

				// now we need to wait for the timer to expire, but the user can update it
				// periodically so we don't want to just
				// assume it is unchanged for the entire wait duration.
				OffsetDateTime targetNextPoll;
				do {
					long waitInterval;

					synchronized (listenerLock) {
						waitInterval = getTimerInterval(samplingInterval);
						listenerLock.notifyAll();
					}

					boolean configUpdated;

					if (waitInterval > 15000) {
						// the target next poll is exactly as you'd expect - the number of milliseconds
						// from the start of the previous poll.
						targetNextPoll = previousPollStart.plusNanos(TimeUnit.MILLISECONDS.toNanos(waitInterval));

						// but we want to wake up in 15 seconds to see if the user has changed their
						// mind.
						configUpdated = waitOnConfigUpdate(15000);
					} else {
						// we need to wait less than 15 seconds - pull out the time we burned since poll
						// start.
						long adjustedWaitInterval = Duration
								.between(OffsetDateTime.now(),
										previousPollStart.plusNanos(TimeUnit.MILLISECONDS.toNanos(waitInterval)))
								.toMillis();

						// but enforce a floor so we don't go crazy cycling.
						if (adjustedWaitInterval < 1000) {
							adjustedWaitInterval = 1000;
						}

						// set that to be our target next poll.
						targetNextPoll = previousPollStart
								.plusNanos(TimeUnit.MILLISECONDS.toNanos(adjustedWaitInterval));

						// and sleep that amount since the user won't have a chance to change their
						// mind.
						configUpdated = waitOnConfigUpdate(adjustedWaitInterval);
					}

					if (configUpdated) {
						// apply the update.
						updateMonitorConfiguration();
					}

				} while (targetNextPoll.isAfter(OffsetDateTime.now(ZoneOffset.UTC))
						&& !Log.isSessionEnding());
			}
		} catch (RuntimeException exception) {

		} finally {
			monitorThread = null; // We're out of the loop and about to exit the thread, so clear the thread
									// reference.
		}
	}

	/**
	 * wait upto the specified number of milliseconds for a configuration update.
	 * 
	 * @param maxWaitInterval
	 * @return
	 */
	private static boolean waitOnConfigUpdate(long maxWaitInterval) {
		boolean configUpdated;

		OffsetDateTime waitEndTime = OffsetDateTime.now().plusNanos(TimeUnit.MILLISECONDS.toNanos(maxWaitInterval));

		synchronized (configLock) {
			while ((waitEndTime.isAfter(OffsetDateTime.now(ZoneOffset.UTC)))
					&& (!pendingConfigChange || (configuration == null))) // don't have a config change -
																						// haven't waited as long as
																						// we're supposed to
			{
				try {
					configLock.wait(maxWaitInterval);
				} catch (InterruptedException e) {
					// do nothing
				}
			}

			configUpdated = ((pendingConfigChange) && (configuration != null));

			configLock.notifyAll();
		}

		return configUpdated;
	}

	private static void updateMonitorConfiguration() {
		ListenerConfiguration newConfiguration;

		Log.setThreadIsInitializer(true); // so if we wander back into Log.Initialize we won't block.

		// get the lock while we grab the configuration so we know it isn't changed out
		// under us
		synchronized (configLock) {
			newConfiguration = configuration;

			configLock.notifyAll();
		}

		synchronized (configLock) {
			eventsInitialized = true;

			configLock.notifyAll();
		}

		// and now apply this configuration to every polled listener.
		synchronized (configLock) {
			pendingConfigChange = false;
			initialized = true;

			configLock.notifyAll();
		}

		Log.setThreadIsInitializer(false);
		
		
		for (AssemblyInfoPacket assemblyInfoPacket : classpathResourcesToAssemblyInfo()) {
			Log.write(assemblyInfoPacket);
		}
	}
	
	private static Set<AssemblyInfoPacket> classpathResourcesToAssemblyInfo() {
		Set<AssemblyInfoPacket> assemblies = new HashSet<AssemblyInfoPacket>();

		try {
			Path javaHome = SystemUtils.getJavaHome();
			URLClassLoader classLoader = (URLClassLoader)ClassLoader.getSystemClassLoader();
			for (URL resource : classLoader.getURLs()) {
				try {
					Path path = Paths.get(resource.toURI());
					if (path != null) {
						assemblies.add(new AssemblyInfoPacket(path, true, path.startsWith(javaHome)));
					}
				} catch (URISyntaxException e) {
					// skip it.
				}
			}
		} catch (Exception e) {
			// forget it. we don't want to blow up here, not over this.
		}
		
		return assemblies;
	}

	/**
	 * Determines the number of milliseconds in the provided interval for the timer
	 * object.
	 * 
	 * The values Default and Shortest are automatically treated as Minute by this
	 * function, effectively making once a minute the system default.
	 * 
	 * @param referenceInterval The interval to calculate milliseconds for
	 * @return The number of milliseconds between timer polls
	 */
	private static long getTimerInterval(MetricSampleInterval referenceInterval) {
		// we have to convert the reference interval into the correct # of milliseconds
		long milliseconds = -1; // a safe choice because it means the timer will fire exactly once.

		switch (referenceInterval) {
		case DEFAULT:
		case SHORTEST:
		case MILLISECOND:
			// we won't go below once a second
			milliseconds = TimeUnit.SECONDS.toMillis(1);
			break;
		case MINUTE:
			milliseconds = TimeUnit.MINUTES.toMillis(1); // sorta by definition
			break;
		case SECOND:
			milliseconds = TimeUnit.SECONDS.toMillis(1); // sorta by definition
			break;
		case HOUR:
			milliseconds = TimeUnit.HOURS.toMillis(1);
			break;
		case DAY:
			milliseconds = 86400000; // get yer own calculator
			break;
		case WEEK:
			milliseconds = 604800000; // I mean who's going to do that, really. BTW: Just barely a 32 bit number.
			break;
		case MONTH:
			milliseconds = TimeUnit.DAYS.toMillis(OffsetDateTime.now().getMonth().maxLength());
			// now I'm just being a smartass.
			break;
		default:
			break;
		}

		// before we return: We poll artificially fast for the first few minutes and
		// first hour.
		long secondsPolling = TimeUnit.MILLISECONDS
				.toSeconds(Duration.between(pollingStarted, OffsetDateTime.now()).toMillis());
		if ((milliseconds > 5000) && (secondsPolling < 120)) {
			milliseconds = 5000;
		} else if ((milliseconds > 15000) && (secondsPolling < 3600)) {
			milliseconds = 15000;
		}

		return milliseconds;
	}
}