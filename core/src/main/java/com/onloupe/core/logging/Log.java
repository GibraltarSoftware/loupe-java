package com.onloupe.core.logging;

import com.onloupe.agent.IMessageSourceProvider;
import com.onloupe.agent.Packager;
import com.onloupe.agent.SessionCriteria;
import com.onloupe.agent.SessionSummary;
import com.onloupe.agent.logging.ExceptionSourceProvider;
import com.onloupe.agent.logging.MessageSourceProvider;
import com.onloupe.configuration.AgentConfiguration;
import com.onloupe.configuration.PackagerConfiguration;
import com.onloupe.configuration.ServerConfiguration;
import com.onloupe.core.CommonCentralLogic;
import com.onloupe.core.data.RepositoryPublishEngine;
import com.onloupe.core.messaging.CommandPacket;
import com.onloupe.core.messaging.FileMessenger;
import com.onloupe.core.messaging.IMessengerPacket;
import com.onloupe.core.messaging.MessagingCommand;
import com.onloupe.core.messaging.Notifier;
import com.onloupe.core.messaging.Publisher;
import com.onloupe.core.metrics.MetricDefinitionCollection;
import com.onloupe.core.metrics.MetricSample;
import com.onloupe.core.monitor.Listener;
import com.onloupe.core.monitor.LocalRepository;
import com.onloupe.core.monitor.ResourceMonitor;
import com.onloupe.core.monitor.UserResolutionNotifier;
import com.onloupe.core.serialization.monitor.LogMessagePacket;
import com.onloupe.core.serialization.monitor.MetricSamplePacket;
import com.onloupe.core.serialization.monitor.SessionClosePacket;
import com.onloupe.core.server.ClientLogger;
import com.onloupe.core.server.HubConnection;
import com.onloupe.core.util.CodeConversionHelpers;
import com.onloupe.core.util.IOUtils;
import com.onloupe.core.util.LogSystems;
import com.onloupe.core.util.Multiplexer;
import com.onloupe.core.util.SystemUtils;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.log.LogMessageSeverity;
import com.onloupe.model.session.ISessionSummary;
import com.onloupe.model.session.SessionStatus;
import com.onloupe.model.system.Version;

import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

// TODO: Auto-generated Javadoc
/**
 * Handles interfacing with a single log file for the purpose of writing log
 * messages.
 */
public final class Log {
	/**
	 * The file extension (without period) for a Gibraltar Log File. Used internally
	 * to Gibraltar.
	 */
	public static final String LOG_EXTENSION = FileMessenger.LOG_EXTENSION;

	/**
	 * The file extension (without period) for a Gibraltar Package File.
	 */
	public static final String PACKAGE_EXTENSION = FileMessenger.PACKAGE_EXTENSION;

	/**
	 * A standard file filter for standard file dialogs that allows selection of
	 * packages and logs.
	 */
	public static final String FILE_FILTER = "Package File(*." + PACKAGE_EXTENSION + ")|*." + PACKAGE_EXTENSION
			+ "|Log File (*." + LOG_EXTENSION + ")|*." + LOG_EXTENSION + "|All Files (*.*)|*.*";

	/**
	 * A standard file filter for standard file dialogs that allows selection of
	 * logs.
	 */
	public static final String FILE_FILTER_LOGS_ONLY = "Log File (*." + LOG_EXTENSION + ")|*." + LOG_EXTENSION
			+ "|All Files (*.*)|*.*";

	/**
	 * A standard file filter for standard file dialogs that allows selection of
	 * packages.
	 */
	public static final String FILE_FILTER_PACKAGES_ONLY = "Package File(*." + PACKAGE_EXTENSION + ")|*."
			+ PACKAGE_EXTENSION + "|All Files (*.*)|*.*";
	
	/** The category for trace messages. */
	public static final String CATEGORY = "Trace";

	/**
	 * The default category name, replacing a null or empty category.
	 */
	private static final String GENERAL_CATEGORY = "General";

	/**
	 * The default category name for a dedicated Exception message.
	 */
	public static final String EXCEPTION_CATEGORY = "System.Exception";

	/** The Constant LINE_BREAK_STRING. */
	public static final String LINE_BREAK_STRING = "\r\n";

	/** The session start info. */
	private static SessionSummary sessionStartInfo;
	
	/** The publisher. */
	private static Publisher publisher; // Our one and only publisher
	
	/** The publish engine. */
	private static RepositoryPublishEngine publishEngine; // We have zero or one publish engine
	
	/** The active packager. */
	private static Packager activePackager; // PROTECTED BY LOCK
	
	/** The repository. */
	private static LocalRepository repository;
	
	/** The resource monitor. */
	private static ResourceMonitor resourceMonitor;

	/** The metric definitions. */
	private static MetricDefinitionCollection metricDefinitions = new MetricDefinitionCollection();

	/** The t current thread info. */
	private static ThreadLocal<ThreadInfo> tCurrentThreadInfo = new ThreadLocal<>(); // ThreadInfo for the current thread, for efficiency.

	/** The send sessions on exit. */
	private static boolean sendSessionsOnExit; // protected by syncObject

	/** The running configuration. */
	private static AgentConfiguration runningConfiguration;

	/** The Constant syncObject. */
	private static final Object syncObject = new Object(); // the general lock for the log object.
	
	/** The Constant initializingLock. */
	private static final Object initializingLock = new Object();
	
	/** The Constant notifierLock. */
	private static final Object notifierLock = new Object(); // used for initializing Notifier instances.

	/** The initialized. */
	private volatile static boolean initialized; // protected by being volatile
	
	/** The initialization never attempted. */
	private volatile static boolean initializationNeverAttempted = true; // protected by being volatile
	
	/** The initializing. */
	private volatile static boolean initializing; // PROTECTED BY INITIALIZING and volatile
	
	/** The explicit start session called. */
	private volatile static boolean explicitStartSessionCalled; // protected by being volatile
	
	/** The shutdown requested. */
	private static boolean shutdownRequested;

	/** The message alert notifier. */
	private static Notifier messageAlertNotifier; // PROTECTED BY NOTIFIERLOCK (weak check outside lock allowed)
	
	/** The message notifier. */
	private static Notifier messageNotifier; // PROTECTED BY NOTIFIERLOCK (weak check outside lock allowed)
	
	/** The user resolution notifier. */
	private static UserResolutionNotifier userResolutionNotifier;

	// A thread-specific static flag for each thread to identify if this thread is
	/** The t thread is initializer. */
	// the current initialize
	private static ThreadLocal<Boolean> tThreadIsInitializer = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			// TODO Auto-generated method stub
			return Boolean.FALSE;
		}
	};
	
	/** The Constant appenderRefs. */
	private static final Map<UUID, String> appenderRefs = new HashMap<UUID, String>();

	/**
	 * A temporary flag to tell us whether to invoke a Debugger.Break() when
	 * Log.DebugBreak() is called.
	 * 
	 * True enables breakpointing, false disables. This should probably be replaced
	 * with an enum to support multiple modes, assuming the basic usage works out.
	 *
	 * @return the break point enable
	 */
	public static boolean getBreakPointEnable() {
		return CommonCentralLogic.getBreakPointEnable();
	}

	/**
	 * Sets the break point enable.
	 *
	 * @param value the new break point enable
	 */
	public static void setBreakPointEnable(boolean value) {
		CommonCentralLogic.setBreakPointEnable(value);
	}

	/**
	 * Indicates if the logging system should be running in silent mode (for example
	 * when running in the agent).
	 * 
	 * Pass-through to the setting in CommonFileTools.
	 *
	 * @return the silent mode
	 */
	public static boolean getSilentMode() {
		return CommonCentralLogic.getSilentMode();
	}

	/**
	 * Sets the silent mode.
	 *
	 * @param value the new silent mode
	 */
	public static void setSilentMode(boolean value) {
		CommonCentralLogic.setSilentMode(value);
	}

	
	/**
	 * Gets the metric definitions.
	 *
	 * @return the metric definitions
	 */
	public static MetricDefinitionCollection getMetricDefinitions() {
		return metricDefinitions;
	}

	/**
	 * Indicates if logging is active, performing initialization if necessary.
	 *
	 * @return True if logging is active, false if it isn't at this time. The very
	 *         first time this is used it will attempt to start the logging system
	 *         even if it hasn't already been started. If that call is canceled
	 *         through our Initializing event then it will return false. After the
	 *         first call it will indicate if logging is currently initialized and
	 *         not attempt to initialize.
	 */
	public static boolean isLoggingActive() {
		if (getInitialized()) {
			return true; // this is our fastest case - we're up and running, nothing more to say.
		}

		// The behavior here isn't obvious: We go in if initialization has never been
		// done OR is being
		// done for the first time right now. That ensures consistency between threads.
		if (initializationNeverAttempted) {
			try {
				initialized = initialize(null);
			} catch (IOException e) {
				return false;
			}
		}

		return getInitialized();
	}

	/**
	 * Indicates if the log system has been initialized and is operational
	 * 
	 * Once true it will never go false, however if false it may go true at any
	 * time.
	 *
	 * @return the initialized
	 */
	public static boolean getInitialized() {
		return initialized;
	}

	/**
	 * Attempt to initialize the log system. If it is already initialized it will
	 * return immediately.
	 *
	 * @param configuration Optional. A default configuration to start with instead
	 *                      of the configuration file.
	 * @return True if the initialization has completed (on this call or prior),
	 *         false if a re-entrant call returns to avoid deadlocks and infinite
	 *         recursion. If calling initialization from a path that may have
	 *         started with the trace listener, you must set suppressTraceInitialize
	 *         to true to guarantee that the application will not deadlock or throw
	 *         an unexpected exception.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static boolean initialize(AgentConfiguration configuration) throws IOException {
		// NOTE TO MAINTAINERS: THIS CLASS RELIES ON THE INITIALIZER VARIABLES BEING
		// VOLATILE TO AVOID LOCKS

		if (tThreadIsInitializer.get()) {
			return false; // this is a re-entrant call, return before we try to get any locks to ensure no
							// deadlocks.
		}

		// ARE we initialized?
		if (initialized) {
			return true; // Initialization has already been run before, so report it complete.
		}

		// OK, not initialized yet - either we're initializing or we need to stall until
		// we're initialized.
		boolean performInitialization = false;
		synchronized (initializingLock) // still need this lock to be sure we're the one and only thread that attempts
										// to do initialization.
		{
			if (!initializing) {
				// since it isn't kicking over yet we need to start that process...
				initializing = true;
				performInitialization = true;
			}

			initializingLock.notifyAll();
		}

		if (performInitialization) {
			tThreadIsInitializer.set(true); // so if we wander around and re-enter initialize we won't block.

			// we have to be sure that any initialization failure won't damage our lock
			// state which would deadlock logging
			try {
				initialized = onInitialize(configuration);
			} finally {
				tThreadIsInitializer.set(false);
				initializationNeverAttempted = false; // we no longer want people to call into us. this should be set
														// before we release the waiting threads waiting on
														// Initializing.

				// and we are no longer trying to initialize.
				synchronized (initializingLock) {
					initializing = false;

					initializingLock.notifyAll();
				}

				// and set the logger down to the server connection now that it's valid
				HubConnection.setLogger(new ClientLogger());
			}
		} else {
			// we need to stall until the initialization status is determinate - either
			// we're initialized or not.

			// Careful, don't block if we're called from a critical thread or we could
			// deadlock.
			if (!Publisher.queryThreadMustNotBlock()) {
				// it's initializing, but not yet complete - we need to stall until it is.
				synchronized (initializingLock) {
					while (initializing) // the status is still indeterminate.
					{
						try {
							initializingLock.wait();
						} catch (InterruptedException e) {
							// do nothing
						}
					}

					initializingLock.notifyAll();
				}
			}
		}

		return initialized; // Initialization is now done, report completion.
	}

	
	/**
	 * The running publisher configuration. This is always safe even when logging is
	 * disabled.
	 *
	 * @return the configuration
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static AgentConfiguration getConfiguration() throws IOException {
		ensureSummaryIsAvailable();

		return runningConfiguration;
	}

	/**
	 * The common information about the active log session. This is always safe even
	 * when logging is disabled.
	 *
	 * @return the session summary
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static SessionSummary getSessionSummary() throws IOException {
		ensureSummaryIsAvailable();

		return sessionStartInfo;
	}

	/**
	 * Get the official Error Alert Notifier instance. Will create it if it doesn't
	 * already exist.
	 *
	 * @return the message alert notifier
	 */
	public static Notifier getMessageAlertNotifier() {
		if (messageAlertNotifier == null) {
			synchronized (notifierLock) // Must get the lock to make sure only one thread can try this at a time.
			{
				if (messageAlertNotifier == null) // Double-check that it's actually still null.
				{
					messageAlertNotifier = new Notifier(LogMessageSeverity.WARNING, "Message Alert");
				}
			}
		}

		return messageAlertNotifier; // It's never altered again once created, so we can just read it now.
	}

	/**
	 * Get the official Notifier instance that returns all messages. Will create it
	 * if it doesn't already exist.
	 *
	 * @return the message notifier
	 */
	public static Notifier getMessageNotifier() {
		if (messageNotifier == null) {
			synchronized (notifierLock) // Must get the lock to make sure only one thread can try this at a time.
			{
				if (messageNotifier == null) // Double-check that it's actually still null.
				{
					messageNotifier = new Notifier(LogMessageSeverity.VERBOSE, "Messages", false);
				}
			}
		}

		return messageNotifier; // It's never altered again once created, so we can just read it now.
	}

	/**
	 * Get the official user resolution notifier instance. Will create it if it
	 * doesn't already exist.
	 *
	 * @return the user resolution notifier
	 */
	public static UserResolutionNotifier getUserResolutionNotifier() {
		if (userResolutionNotifier == null) {
			synchronized (notifierLock) // Must get the lock to make sure only one thread can try this at a time.
			{
				if (userResolutionNotifier == null) // Double-check that it's actually still null.
				{
					userResolutionNotifier = new UserResolutionNotifier(
							runningConfiguration.getPublisher().getEnableAnonymousMode());
				}
			}
		}

		return userResolutionNotifier; // It's never altered again once created, so we can just read it now.
	}

	/**
	 * The current process's collection repository.
	 *
	 * @return the repository
	 */
	public static LocalRepository getRepository() {
		// when valid this should have been set up during initialization, which is done
		// explicitly.
		return repository;
	}

	/**
	 * Indicates if we have sufficient configuration information to automatically
	 * send packages while running (via email or server).
	 * 
	 * This checks whether there is sufficient configuration to submit sessions
	 * using the current configuration.
	 *
	 * @param message the message
	 * @return true, if successful
	 */
	public static boolean canSendSessions(String message) {
		if (!getInitialized()) {
			message = "Gibraltar is not currently enabled";
			return false;
		}

		if (message == null) {
			message = "";
		}

		boolean goodToGo = false;

		// if neither mode are enabled supply that message.
		PackagerConfiguration packager = runningConfiguration.getPackager();

		if (!packager.getAllowServer() && !packager.getAllowEmail()) {
			message = "Neither email or server packaging is allowed with the current packager configuration";
		} else {
			if (packager.getAllowEmail() && (isEmailSubmissionConfigured(message))) // only test if allowed
			{
				goodToGo = true;
			} else if (packager.getAllowServer() && (isHubSubmissionConfigured(message))) // only test if allowed
			{
				goodToGo = true;
			}
		}

		return goodToGo;
	}

	/**
	 * Indicates if we have sufficient configuration information to automatically
	 * send packages upon exit (via email or server).
	 * 
	 * This checks whether there is sufficient configuration to submit sessions
	 * through the packager upon exit. It also checks that the packager executable
	 * can be found.
	 *
	 * @param message the message
	 * @return true, if successful
	 */
	public static boolean canSendSessionsOnExit(String message) {
		boolean goodToGo = canSendSessions(message);

		// we also check that the executable is around since we have to fire up packager
		// externally.
		if (goodToGo) {
			goodToGo = canFindPackager(message);
		}

		return goodToGo;
	}

	/**
	 * Ensure all messages have been written completely.
	 */
	public static void flush() {
		IMessengerPacket commandPacket = new CommandPacket(MessagingCommand.FLUSH);
		write(new IMessengerPacket[] { commandPacket }, LogWriteMode.WAIT_FOR_COMMIT);
	}

	/**
	 * Indicates if we have sufficient configuration information to automatically
	 * send packages by email submission.
	 *
	 * @param message the message
	 * @return Does not check if email submission is allowed
	 */
	public static boolean isEmailSubmissionConfigured(String message) {
		if (!getInitialized()) {
			message = "Gibraltar is not currently enabled";
			return false;
		}

		boolean goodToGo = true;
		if (message == null) {
			message = "";
		}

		// Do we appear to have sufficient configuration?
		PackagerConfiguration packager = runningConfiguration.getPackager();
		if (TypeUtils.isBlank(packager.getDestinationEmailAddress())) {
			message += "No destination email address was provided for the packager.\r\n";
			goodToGo = false;
		}

		// ToDo: Consider having it also check the outgoing email server config (which
		// is more than just ours).

		return goodToGo;
	}

	/**
	 * Indicates if we have sufficient configuration information to automatically
	 * send packages to a Loupe Server.
	 * 
	 * This checks whether there is sufficient configuration to submit sessions
	 * through a server. It does NOT check whether the packager is configured to
	 * allow submission through a server, because they may also be sent directly
	 * from Agent without using the packager.
	 *
	 * @param message the message
	 * @return true, if is hub submission configured
	 */
	public static boolean isHubSubmissionConfigured(String message) {
		if (!initialized) {
			message = "Gibraltar is not currently enabled";
			return false;
		}

		boolean goodToGo = true;
		if (message == null) {
			message = "";
		}

		// Do we appear to have sufficient configuration?
		ServerConfiguration server = runningConfiguration.getServer();
		if (!server.getEnabled()) {
			message += "Server configuration is missing or disabled.\r\n";
			goodToGo = false; // Can't use it if it's disabled.
		} else if (server.getUseGibraltarService()) {
			// Using the Loupe Service requires a customer name.
			if (TypeUtils.isBlank(server.getCustomerName())) {
				message += "No customer name was provided for the Loupe Service.\r\n";
				goodToGo = false; // Can't use Loupe Service if no customer name is configured.
			}
		} else {
			// Using a private server requires a server name and a port that is not negative
			// (0 means default).
			if (TypeUtils.isBlank(server.getServer())) {
				message += "No server name was provided for the server.\r\n";
				goodToGo = false; // Can't use a private server if no server name is configured.
			} else if (server.getPort() < 0) {
				message += "An invalid server port was configured.\r\n";
				goodToGo = false; // Can't use a private server if the port is not valid.
			}
		}

		return goodToGo;
	}

	/**
	 * Indicates if the packager executable is available where this process can find
	 * it.
	 *
	 * @param message the message
	 * @return true, if successful
	 */
	public static boolean canFindPackager(String message) {
		if (message == null) {
			message = "";
		}

		// Is the packager executable available?
		String packagerFileNamePath = getPackagerFileNamePath();
		boolean goodToGo = (new File(packagerFileNamePath)).isFile();

		if (!goodToGo) {
			message += "The packager utility could not be found in the same directory as the application.\r\n";
		}

		return goodToGo;
	}

	/**
	 * Indicates if the agent should package &amp; send sessions for the current
	 * application after this session exits.
	 * 
	 * When true the system will automatically
	 *
	 * @return the send sessions on exit
	 */
	public static boolean getSendSessionsOnExit() {
		synchronized (syncObject) {
			syncObject.notifyAll();

			return sendSessionsOnExit;
		}
	}

	/**
	 * Indicates if the StartSession API method was ever explicitly called.
	 * 
	 * If StartSession was not explicitly called then an ApplicationExit event will
	 * implicitly call EndSession for easy Gibraltar drop-in support. If
	 * StartSession was explicitly called then we expect the client to make a
	 * corresponding explicit EndSession call, and the Agent's ApplicationExit
	 * handler will not call EndSession.
	 *
	 * @return the explicit start session called
	 */
	public static boolean getExplicitStartSessionCalled() {
		return explicitStartSessionCalled;
	}

	/**
	 * Our one metric definition collection for capturing metrics in this process
	 * 
	 * 
	 * For performance reasons, it is important that there is only a single instance
	 * of a particular metric for any given process. This is managed automatically
	 * provided only this metrics collection is used. If there is a duplicate metric
	 * in the data stream, that information will be discarded when the log file is
	 * read (but there is no effect at runtime).
	 *
	 * @return the metrics
	 */
	public static MetricDefinitionCollection getMetrics() {
		return metricDefinitions;
	}

	/**
	 * Reports whether EndSession() has been called to formally end the session.
	 *
	 * @return true, if is session ending
	 */
	public static boolean isSessionEnding() {
		return CommonCentralLogic.isSessionEnding();
	}

	/**
	 * Reports whether EndSession() has completed flushing the end-session command
	 * to the log.
	 *
	 * @return true, if is session ended
	 */
	public static boolean isSessionEnded() {
		return CommonCentralLogic.isSessionEnded();
	}
	
	/**
	 * The version information for the Gibraltar Agent.
	 *
	 * @return the agent version
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static Version getAgentVersion() throws IOException {
		ensureSummaryIsAvailable();

		return sessionStartInfo.getAgentVersion();
	}

	/**
	 * Record the provided set of metric samples to the log.
	 * 
	 * When sampling multiple metrics at the same time, it is faster to make a
	 * single write call than multiple calls.
	 *
	 * @param samples A list of metric samples to record.
	 * @throws InterruptedException the interrupted exception
	 */
	public static void write(ArrayList<MetricSample> samples) throws InterruptedException {
		if (!initialized) {
			return;
		}

		if (samples == null || samples.isEmpty()) {
			return;
		}

		IMessengerPacket[] packetArray = new IMessengerPacket[samples.size()]; // An array to hold the batch of packets.
		// Now iterate over each sample, putting them into our array.
		int index = 0;
		for (MetricSample curSample : samples) {
			packetArray[index] = curSample.getPacket();
			index++;
		}

		publisher.publish(packetArray, false);
	}

	/**
	 * Record the provided metric sample to the log.
	 * 
	 * Most applications should use another object or the appropriate log method on
	 * this object to create log information instead of manually creating log
	 * packets and writing them here. This functionality is primarily for internal
	 * support of the various log listeners that support third party log systems.
	 *
	 * @param sample the sample
	 * @throws InterruptedException the interrupted exception
	 */
	public static void write(MetricSample sample) throws InterruptedException {
		if (!initialized) {
			return;
		}

		if (sample == null) {
			return;
		}

		// We must wrap the packet as an array here, the underlying publisher method now
		// takes them as a batch.
		publisher.publish(new MetricSamplePacket[] { sample.getPacket() }, false);
	}
	
	/**
	 * Write.
	 *
	 * @param severity the severity
	 * @param throwable the throwable
	 * @param element the element
	 * @param skipFrames the skip frames
	 * @param exclusions the exclusions
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void write(LogMessageSeverity severity, Throwable throwable, StackTraceElement element, int skipFrames, Set<String> exclusions, 
			ThreadInfo threadInfo, String logSystem, String category, String caption, String description, Object... args) {
		if (element != null) {
			write(severity, LogWriteMode.QUEUED, logSystem, throwable, false, threadInfo, element, category, caption, description, args);
		} else {
			write(severity, LogWriteMode.QUEUED, logSystem, throwable, false, threadInfo, skipFrames, category, caption,
					description, args);
		}
	}
	
	/**
	 * Write.
	 *
	 * @param severity the severity
	 * @param writeMode the write mode
	 * @param logSystem the log system
	 * @param throwable the throwable
	 * @param attributeToException the attribute to exception
	 * @param threadInfo the thread info
	 * @param element the element
	 * @param category the category
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void write(LogMessageSeverity severity, LogWriteMode writeMode, String logSystem, Throwable throwable,
			boolean attributeToException, ThreadInfo threadInfo, StackTraceElement element, String category, String caption, String description, Object... args) {
		if (!initialized) {
			return;
		}

		// skipFrames = 1 to skip out to our caller instead of right here.
		LogMessage logMessage = new LogMessage(severity, writeMode, logSystem, category, element, throwable, threadInfo,
				attributeToException, null, caption, description, args);
		logMessage.publishToLog(); // tell the SimpleLogMessage to publish itself (back through us).		
	}


	/**
	 * Write a log message directly to the Gibraltar log with an attached Exception
	 * and specifying Queued or WaitForCommit behavior.
	 * 
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the WriteMessage() method in order to attribute
	 * the log message to their own outer callers.
	 * </p>
	 * <p>
	 * This overload also allows an Exception object to be attached to the log
	 * message. An Exception-typed null (e.g. from a variable of an Exception type)
	 * is allowed for the exception argument, but calls which do not have a possible
	 * Exception to attach should use an overload without an exception argument
	 * rather than pass a direct value of null, to avoid compiler ambiguity over the
	 * type of a simple null.
	 * </p>
	 *
	 * @param severity             The log message severity.
	 * @param writeMode            Whether to queue-and-return or wait-for-commit.
	 * @param logSystem the log system
	 * @param throwable the throwable
	 * @param attributeToException True if the call stack from where the exception
	 *                             was thrown should be used for log message
	 *                             attribution
	 * @param threadInfo the thread info
	 * @param skipFrames the skip frames
	 * @param category             The category for this log message.
	 * @param caption              A simple single-line message caption. (Will not
	 *                             be processed for formatting.)
	 * @param description          Additional multi-line descriptive message (or may
	 *                             be null) which can be a format string followed by
	 *                             corresponding args.
	 * @param args                 A variable number of arguments referenced by the
	 *                             formatted description string (or no arguments to
	 *                             skip formatting).
	 */
	public static void write(LogMessageSeverity severity, LogWriteMode writeMode, String logSystem, Throwable throwable,
			boolean attributeToException, ThreadInfo threadInfo, int skipFrames, String category, String caption, String description, Object... args) {
		if (!initialized) {
			return;
		}

		// skipFrames = 1 to skip out to our caller instead of right here.
		LogMessage logMessage = new LogMessage(severity, writeMode, logSystem, category, skipFrames + 1, throwable, threadInfo,
				attributeToException, null, caption, description, args);
		logMessage.publishToLog(); // tell the SimpleLogMessage to publish itself (back through us).		
	}

	/**
	 * Write.
	 *
	 * @param severity the severity
	 * @param writeMode the write mode
	 * @param throwable the throwable
	 * @param attributeToException the attribute to exception
	 * @param skipFrames the skip frames
	 * @param category the category
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void write(LogMessageSeverity severity, LogWriteMode writeMode, Throwable throwable,
			boolean attributeToException, int skipFrames, String category, String caption, String description, Object... args) {
		write(severity, writeMode, LogSystems.GIBRALTAR, throwable, attributeToException, null, skipFrames, category, caption, description, args);
	}
	
	/**
	 * Write.
	 *
	 * @param severity the severity
	 * @param category the category
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void write(LogMessageSeverity severity, String category, String caption, String description,
			Object... args) {
		write(severity, LogWriteMode.QUEUED, null, false, 1, category, caption, description, args);
	}
	
	/**
	 * Write.
	 *
	 * @param severity the severity
	 * @param writeMode the write mode
	 * @param throwable the throwable
	 * @param category the category
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void write(LogMessageSeverity severity, LogWriteMode writeMode, Throwable throwable, String category,
			String caption, String description, Object... args) {
		write(severity, writeMode, throwable, false, 1, category, caption, description, args);
	}
	
	/**
	 * Write.
	 *
	 * @param severity the severity
	 * @param writeMode the write mode
	 * @param throwable the throwable
	 * @param attributeToException the attribute to exception
	 * @param category the category
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void write(LogMessageSeverity severity, LogWriteMode writeMode, Throwable throwable,
			boolean attributeToException, String category, String caption, String description, Object... args) {
		write(severity, writeMode, throwable, attributeToException, 1, category, caption, description, args);
	}
	
	/**
	 * Write message.
	 *
	 * @param severity the severity
	 * @param writeMode the write mode
	 * @param logSystem the log system
	 * @param categoryName the category name
	 * @param sourceProvider the source provider
	 * @param userName the user name
	 * @param throwable the throwable
	 * @param threadInfo the thread info
	 * @param detailsXml the details xml
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void writeMessage(LogMessageSeverity severity, LogWriteMode writeMode, String logSystem,
			String categoryName, IMessageSourceProvider sourceProvider, String userName, Throwable throwable,
			ThreadInfo threadInfo, String detailsXml, String caption, String description, Object... args) {
		if (!initialized) {
			return;
		}

		IMessengerPacket packet = makeLogPacket(severity, logSystem, categoryName, sourceProvider, userName, throwable,
				threadInfo, detailsXml, caption, description, args);
		// write the assembled packet to our queue
		write(new IMessengerPacket[] { packet }, writeMode);

	}
	
	/**
	 * Write message.
	 *
	 * @param severity the severity
	 * @param writeMode the write mode
	 * @param skipFrames the skip frames
	 * @param throwable the throwable
	 * @param detailsXml the details xml
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void writeMessage(LogMessageSeverity severity, LogWriteMode writeMode, int skipFrames,
			Throwable throwable, String detailsXml, String caption, String description, Object... args) {
		if (!initialized) {
			return;
		}

		if (skipFrames < 0) {
			skipFrames = 0; // Less than 0 is illegal (it would mean us!), correct it to designate our
							// immediate caller.
		}

		LogMessage logMessage = new LogMessage(severity, writeMode, LogSystems.GIBRALTAR, CATEGORY, skipFrames + 1,
				throwable, false, detailsXml, caption, description, args);
		logMessage.publishToLog(); // tell the DetailLogMessage to publish itself (back through us).
	}
	
	/**
	 * Write message.
	 *
	 * @param severity the severity
	 * @param writeMode the write mode
	 * @param skipFrames the skip frames
	 * @param throwable the throwable
	 * @param attributeToException the attribute to exception
	 * @param detailsXml the details xml
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void writeMessage(LogMessageSeverity severity, LogWriteMode writeMode, int skipFrames,
			Throwable throwable, boolean attributeToException, String detailsXml, String caption, String description,
			Object... args) {
		if (!initialized) {
			return;
		}

		if (skipFrames < 0) {
			skipFrames = 0; // Less than 0 is illegal (it would mean us!), correct it to designate our
							// immediate caller.
		}

		LogMessage logMessage = new LogMessage(severity, writeMode, LogSystems.GIBRALTAR, CATEGORY, skipFrames + 1,
				throwable, attributeToException, detailsXml, caption, description, args);
		logMessage.publishToLog(); // tell the DetailLogMessage to publish itself (back through us).
	}
	
	/**
	 * Publish the provided raw packet to the stream.
	 * 
	 * This functionality is primarily for internal support of the various log
	 * listeners that support third party log systems. This overload uses the
	 * default LogWriteMode.Queued. To specify wait-for-commit behavior, use the
	 * overload with a LogWriteMode argument.
	 *
	 * @param packet The log packet to write
	 */
	public static void write(IMessengerPacket packet) {
		// we explicitly are not checking initialized because we are part of the
		// initialize.
		if (publisher == null) {
			return;
		}

		// Wrap the packet as an array and pass it off to our more-general overload.
		write(new IMessengerPacket[] { packet }, LogWriteMode.QUEUED); // Use normal Queued mode by
																		// default.
	}

	/**
	 * Publish a batch of raw packets to the stream, specifying the LogWriteMode to
	 * use.
	 * 
	 * This functionality is primarily for internal support of the various log
	 * listeners that support third party log systems.
	 *
	 * @param packetArray An array of the log packets to write.
	 * @param writeMode   Whether to queue-and-return or wait-for-commit.
	 */
	public static void write(IMessengerPacket[] packetArray, LogWriteMode writeMode) {
		// we explicitly are not checking initialized because we are part of the
		// initialize.
		if (publisher == null) {
			return;
		}

		if (packetArray == null || packetArray.length == 0) {
			return;
		}

		// before we publish, are these log messages? if so we have to count them
		for (IMessengerPacket packet : packetArray) {
			LogMessagePacket logPacket = packet instanceof LogMessagePacket ? (LogMessagePacket) packet : null;
			if (logPacket != null) {
				sessionStartInfo.updateMessageStatistics(logPacket);
			}
		}

		try {
			if (writeMode == LogWriteMode.WAIT_FOR_COMMIT) {
				publisher.publish(packetArray, true);
			} else {
				publisher.publish(packetArray, false);
			}
		} catch (InterruptedException e) {
			// do nothing
			if (SystemUtils.isInDebugMode()) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Trace.
	 *
	 * @param str the str
	 * @param args the args
	 */
	public static void trace(String str, Object... args) {
		trace(null, str, args);
	}

	/**
	 * Trace.
	 *
	 * @param t the t
	 * @param str the str
	 * @param args the args
	 */
	public static void trace(Throwable t, String str, Object... args) {
		write(LogMessageSeverity.VERBOSE, t, null, 1, null, null, LogSystems.GIBRALTAR, CATEGORY, null, str, args);
	}
	
	/**
	 * Record an unexpected Exception to the Gibraltar central log, formatted
	 * automatically.
	 * 
	 * <p>
	 * This method provides an easy way to record an Exception as a separate message
	 * which will be attributed to the code location which threw the Exception
	 * rather than where this method was called from. The category will default to
	 * "Exception" if null, and the message will be formatted automatically based on
	 * the Exception. The severity will be determined by the canContinue parameter:
	 * Critical for fatal errors (canContinue is false), Error for non-fatal errors
	 * (canContinue is true).
	 * </p>
	 * <p>
	 * This method is intended for use with top-level exception catching for errors
	 * not anticipated in a specific operation, but when it is not appropriate to
	 * alert the user because the error does not impact their work flow or will be
	 * otherwise handled gracefully within the application. For unanticipated errors
	 * which disrupt a user activity, see the
	 * <see CREF="ReportException">ReportException</see> method.
	 * </p>
	 *
	 * @param sourceProvider An IMessageSourceProvider object which supplies the
	 *                       source information about this log message (NOT the
	 *                       exception source information).
	 * @param throwable      An Exception object to record as a log message. This
	 *                       call is ignored if null.
	 * @param threadInfo the thread info
	 * @param detailsXml     Optional. An XML document with extended details about
	 *                       the exception. Can be null.
	 * @param category       The application subsystem or logging category that the
	 *                       message will be associated with.
	 * @param canContinue    True if the application can continue after this call,
	 *                       false if this is a fatal error and the application can
	 *                       not continue after this call.
	 * @param reporting      True if the error will also be reported to the user.
	 *                       (private use)
	 * @param blocking       True if reporting to user and waiting for user
	 *                       response; otherwise should be false. (private use)
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void recordException(IMessageSourceProvider sourceProvider, Throwable throwable, ThreadInfo threadInfo, String detailsXml,
			String category, boolean canContinue, boolean reporting, boolean blocking) throws IOException {
		if (!initialized) {
			return;
		}

		if (throwable == null) {
			return;
		}

		LogMessageSeverity severity = canContinue ? LogMessageSeverity.ERROR : LogMessageSeverity.CRITICAL;

		ExceptionSourceProvider exceptionSourceProvider = new ExceptionSourceProvider(throwable);

		IMessageSourceProvider finalSourceProvider;

		String recorded;
		String recordedLocation = "";
		if (exceptionSourceProvider.getClassName() == null) {
			finalSourceProvider = sourceProvider;
			recorded = ""; // Already have the recording location in the fSP, don't add it to description.
		} else {
			finalSourceProvider = exceptionSourceProvider;
			// Source will be attributed to the exception origin, so add the reporting
			// location to the description.
			recorded = String.format("Reported by: %1$s.%2$s\r\n", sourceProvider.getClassName(),
					sourceProvider.getMethodName());
			if (sourceProvider.getFileName() != null) {
				recordedLocation = String.format("Location: Line %1$s of file '%2$s'\r\n",
						sourceProvider.getLineNumber(), sourceProvider.getFileName());
			}
		}

		if (TypeUtils.isBlank(category)) {
			category = EXCEPTION_CATEGORY;
		}

		String caption = ((throwable.getMessage() != null) ? throwable.getMessage() : "").trim();
		if (TypeUtils.isBlank(caption)) {
			caption = throwable.getClass().getSimpleName();
		}

		String reportString = canContinue
				? "\r\nThis non-fatal error %s be reported to the user%s, then execution may continue.\r\n"
				: "\r\nThis fatal error %s be reported to the user%s, then the application will exit.\r\n";

		reportString = String.format(reportString, (reporting ? "will" : "will not"),
				reporting ? (blocking ? " and wait for their response" : " without waiting on their response") : "");

		writeMessage(severity, LogWriteMode.WAIT_FOR_COMMIT, LogSystems.GIBRALTAR, category, finalSourceProvider, null,
				throwable, threadInfo, detailsXml, null, "%s\r\nException type: %s\r\n%s %s %s", caption,
				throwable.getClass().getName(), recorded, recordedLocation, reportString);

		if (!canContinue) {
			getSessionSummary().setStatus(SessionStatus.CRASHED);
		}
	}
	
	/**
	 * Record exception.
	 *
	 * @param skipFrames the skip frames
	 * @param throwable the throwable
	 * @param threadInfo the thread info
	 * @param detailsXml the details xml
	 * @param category the category
	 * @param canContinue the can continue
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void recordException(int skipFrames, Throwable throwable, ThreadInfo threadInfo, String detailsXml, String category,
			boolean canContinue) throws IOException {
		recordException(null, throwable, threadInfo, detailsXml, category, canContinue, false, false);
	}
	
	/**
	 * Record exception.
	 *
	 * @param sourceProvider the source provider
	 * @param throwable the throwable
	 * @param threadInfo the thread info
	 * @param detailsXml the details xml
	 * @param category the category
	 * @param canContinue the can continue
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void recordException(IMessageSourceProvider sourceProvider, Throwable throwable, ThreadInfo threadInfo, String detailsXml,
			String category, boolean canContinue) throws IOException {
		recordException(sourceProvider, throwable, threadInfo, detailsXml, category, canContinue, false, false);
	}

	/**
	 * Record exception.
	 *
	 * @param skipFrames the skip frames
	 * @param throwable the throwable
	 * @param detailsXml the details xml
	 * @param category the category
	 * @param canContinue the can continue
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void recordException(int skipFrames, Throwable throwable, String detailsXml, String category,
			boolean canContinue) throws IOException {
		recordException(skipFrames, throwable, null, detailsXml, category, canContinue);
	}
	
	/**
	 * End the current log file (but not the session) and open a new file to
	 * continue logging.
	 * 
	 * This method is provided to support user-initiated roll-over to a new log file
	 * (instead of waiting for an automatic maintenance roll-over) in order to allow
	 * the logs of an ongoing session up to that point to be collected and submitted
	 * (or opened in the viewer) for analysis without shutting down the subject
	 * application.
	 */
	public static void endFile() {
		if (!initialized) {
			return;
		}

		endFile(1, ""); // No reason declared, attribute it to our immediate caller.
	}

	/**
	 * End the current log file (but not the session) and open a new file to
	 * continue logging.
	 * 
	 * This method is provided to support user-initiated roll-over to a new log file
	 * (instead of waiting for an automatic maintenance roll-over) in order to allow
	 * the logs of an ongoing session up to that point to be collected and submitted
	 * (or opened in the viewer) for analysis without shutting down the subject
	 * application.
	 * 
	 * @param reason An optionally-declared reason for invoking this operation (may
	 *               be null or empty).
	 */
	public static void endFile(String reason) {
		if (!initialized) {
			return;
		}

		endFile(1, reason); // Pass on reason, attribute it to our immediate caller.
	}

	/**
	 * End the current log file (but not the session) and open a new file to
	 * continue logging.
	 * 
	 * This method is provided to support user-initiated roll-over to a new log file
	 * (instead of waiting for an automatic maintenance roll-over) in order to allow
	 * the logs of an ongoing session up to that point to be collected and submitted
	 * (or opened in the viewer) for analysis without shutting down the subject
	 * application.
	 * 
	 * @param skipFrames The number of stack frames to skip out to find the original
	 *                   caller.
	 * @param reason     An optionally-declared reason for invoking this operation
	 *                   (may be null or empty).
	 */
	public static void endFile(int skipFrames, String reason) {
		if (!initialized) {
			return;
		}

		if (skipFrames < 0) // Sanity check, in case we decide to make this overload public also...
		{
			skipFrames = 0; // Illegal skipFrames value, attribute it to our immediate caller instead.
		}

		// Remember to increment skipFrames as we pass it down a new stack frame level.
		IMessageSourceProvider sourceProvider = new MessageSourceProvider(skipFrames + 1);

		final String endFormat = "Current log file ending by request%s%s";
		final String newFormat = "New log file opened by request%s%s";
		final String noReasonTerminator = ".";
		final String reasonDelimiter = ": ";
		String formatArg0;
		String formatArg1;

		if (TypeUtils.isBlank(reason)) {
			formatArg0 = noReasonTerminator;
			formatArg1 = "";
		} else {
			formatArg0 = reasonDelimiter;
			formatArg1 = reason;
		}

		// Make a packet to mark the end of the current log file and why it ended there.
		IMessengerPacket endPacket = makeLogPacket(LogMessageSeverity.INFORMATION, LogSystems.GIBRALTAR, CATEGORY,
				sourceProvider, "", null, null, null, String.format(endFormat, formatArg0, formatArg1), null);

		// Make a command packet to trigger the actual file close.
		IMessengerPacket commandPacket = new CommandPacket(MessagingCommand.CLOSE_FILE);

		// Make a packet to force a new file open, mark why it rolled over, and key off
		// of for completion.
		IMessengerPacket newPacket = makeLogPacket(LogMessageSeverity.INFORMATION, LogSystems.GIBRALTAR, CATEGORY,
				sourceProvider, "", null, null, null, String.format(newFormat, formatArg0, formatArg1), null);

		// Now send them as a batch to enforce back-to-back processing, and wait for the
		// last one to commit to disk.
		write(new IMessengerPacket[] { endPacket, commandPacket, newPacket }, LogWriteMode.WAIT_FOR_COMMIT);
	}

	/**
	 * Called at the end of the process execution cycle to indicate that the process
	 * shut down normally or explicitly crashed.
	 * 
	 * <p>
	 * This will put the Gibraltar log into an ending state in which it will flush
	 * everything still in its queue and then switch to a background thread to
	 * process any further log messages. All log messages submitted after this call
	 * will block the submitting thread until they are committed to disk, so that
	 * any foreground thread still logging final items will be sure to get them
	 * through before they exit. This is called automatically when an
	 * ApplicationExit event is received, and can also be called directly (such as
	 * if that event would not function).
	 * </p>
	 * <p>
	 * If EndSession is never called, the log will reflect that the session must
	 * have crashed.
	 * </p>
	 *
	 * @param endingStatus   The explicit ending status to declare for this session,
	 *                       <see cref="SessionStatus.Normal">Normal</see> or
	 *                       <see cref="SessionStatus.Crashed">Crashed</see>.
	 * @param sourceProvider An IMessageSourceProvider object which supplies the
	 *                       source information about this log message.
	 * @param reason         A simple reason to declare why the application is
	 *                       ending as Normal or as Crashed, or may be null.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void shutdown(SessionStatus endingStatus, IMessageSourceProvider sourceProvider, String reason)
			throws IOException {
		if (!initialized) {
			return;
		}

		if (endingStatus.getValue() < SessionStatus.NORMAL.getValue() || publisher == null) {
			return;
		}

		IMessengerPacket exitReason = null;
		synchronized (syncObject) {
			if (!isSessionEnding()) {
				// first time in - we need to kick off the packager if we're set to send on
				// exit.
				if (sendSessionsOnExit) {
					sendSessionData(); // we want this to be async so no need to wait.
				}

				shutdownPublishEngine(); // self-checks if the publish engine doesn't exist and is therefore "shutdown"
			}

			CommonCentralLogic.declareSessionIsEnding(); // Flag that the session will be marked as ending.

			SessionStatus oldStatus = getSessionSummary().getStatus();

			// Status can only progress one-way: Running -> Normal -> Crashed. Never
			// backwards.
			if (endingStatus.getValue() > oldStatus.getValue()) {
				getSessionSummary().setStatus(endingStatus);

				boolean normalEnd = (endingStatus == SessionStatus.NORMAL);

				final String captionFormat = "Session ending %s%s%s";
				final String noReasonTerminator = ".";
				final String reasonDelimiter = ": ";
				String state;
				String formatArg1;
				String formatArg2;

				if (TypeUtils.isBlank(reason)) {
					formatArg1 = noReasonTerminator;
					formatArg2 = "";
				} else {
					formatArg1 = reasonDelimiter;
					formatArg2 = reason;
				}

				String descriptionFormat;
				if (normalEnd) {
					state = "normally";
					descriptionFormat = "Session state changed from %s to %s.\r\n"
							+ "Any further EndSession calls will not be reported unless declaring the session crashed.";
				} else {
					state = "as crashed";
					descriptionFormat = "Session state changed from %s to %s.\r\n"
							+ "Any further EndSession calls will not be reported since sessions declared crashed can not be set back to normal.";
				}

				exitReason = makeLogPacket(LogMessageSeverity.VERBOSE, LogSystems.GIBRALTAR, CATEGORY, sourceProvider, null,
						null, null, null, String.format(captionFormat, state, formatArg1, formatArg2), descriptionFormat,
						oldStatus, endingStatus);
			}

			syncObject.notifyAll();
		}

		// Mark the session as a normal/crashed exit and tell the messaging system that
		// the application is exiting.
		// This must be done outside the lock because we block until its done and we
		// could deadlock!
		List<IMessengerPacket> batch = new ArrayList<IMessengerPacket>();
		batch.add(new SessionClosePacket(getSessionSummary().getStatus()));
		if (exitReason != null) {
			batch.add(exitReason);
		}
		batch.add(new CommandPacket(MessagingCommand.SHUTDOWN, getSessionSummary().getStatus()));

		// now that we've created our batch write them out.
		write(batch.toArray(new IMessengerPacket[0]), LogWriteMode.WAIT_FOR_COMMIT);
		
		// reset statics in preparation for a possible restart
		reset();
	}
	
	/**
	 * Unregister an appender with Loupe. If requestShutdown is true, Loupe
	 * will attempt to shut down if no other appenders are currently active,
	 * otherwise Loupe will shut down after the last active appender has.
	 *
	 * @param appenderRef the appender ref
	 * @param requestShutdown the request shutdown
	 */
	public static void shutdownAppender(UUID appenderRef, boolean requestShutdown) {
		String name = appenderRefs.remove(appenderRef);
		if (name != null) {
			write(LogMessageSeverity.INFORMATION, CATEGORY,
					"Shutting down appender, id:" + appenderRef.toString() + ". name: " + name, null);
			
			shutdownRequested = shutdownRequested || requestShutdown;
			if (appenderRefs.isEmpty() && shutdownRequested) {
				try {
					Log.shutdown(SessionStatus.NORMAL, 1, null);
				} catch (IOException e) {
					
				}
			}
		}
	}


	/**
	 * Called at the end of the process execution cycle to indicate that the process
	 * shut down normally or explicitly crashed.
	 * 
	 * <p>
	 * This will put the Gibraltar log into an ending state in which it will flush
	 * everything still in its queue and then switch to a background thread to
	 * process any further log messages. All log messages submitted after this call
	 * will block the submitting thread until they are committed to disk, so that
	 * any foreground thread still logging final items will be sure to get them
	 * through before they exit. This is called automatically when an
	 * ApplicationExit event is received, and can also be called directly (such as
	 * if that event would not function).
	 * </p>
	 * <p>
	 * If EndSession is never called, the log will reflect that the session must
	 * have crashed.
	 * </p>
	 *
	 * @param endingStatus The explicit ending status to declare for this session,
	 *                     <see cref="SessionStatus.Normal">Normal</see> or
	 *                     <see cref="SessionStatus.Crashed">Crashed</see>.
	 * @param skipFrames   The number of stack frames to skip out to find the
	 *                     original caller.
	 * @param reason       A simple reason to declare why the application is ending
	 *                     as Normal or as Crashed, or may be null.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void shutdown(SessionStatus endingStatus, int skipFrames, String reason)
			throws IOException {
		if (!initialized) {
			return;
		}

		shutdown(endingStatus, new MessageSourceProvider(skipFrames + 1), reason);
	}
	
	/**
	 * Shutdown.
	 */
	public static void shutdown() {
		try {
			shutdown(SessionStatus.NORMAL, 1, "Beginning shutdown of Loupe");
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	}
	
	/**
	 * Register an appender with Loupe, and start it up if not already running.
	 *
	 * @param appenderName the appender name
	 * @return A UUID that will be used to track the appender.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static UUID startAppender(String appenderName) throws IOException {
		// no null nonsense.
		if (TypeUtils.isBlank(appenderName))
			throw new NullPointerException("name must not be null.");

		try {						
			UUID appenderRef = UUID.randomUUID();
			Log.start(null, 1, "Initializing, appender detected. id: " + appenderRef.toString() + ". name: " + appenderName);

			write(LogMessageSeverity.INFORMATION, CATEGORY,
					"Registering appender, id:" + appenderRef.toString() + ". name: " + appenderName, null);

			appenderRefs.put(appenderRef, appenderName);
			return appenderRef;
		} catch (IOException e) {
			if (SystemUtils.isInDebugMode()) {
				e.printStackTrace();
			}
			
			throw e;
		}
	}
	
	/**
	 * Start.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void start() throws IOException {
		start(null, 1, "Initializing, will attempt configuration resolution.");
	}
	
	/**
	 * Start.
	 *
	 * @param configuration the configuration
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void start(AgentConfiguration configuration) throws IOException {
		if (configuration == null) {
			throw new NullPointerException("configuration");
		}

		start(configuration, 1, "Initializing with provided configuration.");
	}

	/**
	 * Called to activate the logging system. If it is already active then this has
	 * no effect.
	 *
	 * @param configuration Optional. An initial default configuration to use
	 *                      instead of the configuration file.
	 * @param skipFrames the skip frames
	 * @param reason the reason
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void start(AgentConfiguration configuration, int skipFrames, String reason)
			throws IOException {
		explicitStartSessionCalled = true;

		// if we're already initialized then there's nothing more to do so just return.
		if (initialized) {
			return;
		}

		start(configuration, new MessageSourceProvider(skipFrames + 1), reason);
	}

	/**
	 * Called to activate the logging system. If it is already active then this has
	 * no effect.
	 *
	 * @param configuration  Optional. An initial default configuration to use
	 *                       instead of the configuration file.
	 * @param sourceProvider the source provider
	 * @param reason the reason
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void start(AgentConfiguration configuration, IMessageSourceProvider sourceProvider,
			String reason) throws IOException {
		explicitStartSessionCalled = true;

		// if we're already initialized then there's nothing more to do so just return.
		if (getInitialized()) {
			return;
		}

		// otherwise we try to initialize and log a message.
		initialize(configuration);

		assert(sessionStartInfo != null);

		if (initialized) {
			if (TypeUtils.isBlank(reason)) {
				reason = "Session started";
			}

			writeMessage(LogMessageSeverity.INFORMATION, LogWriteMode.QUEUED, LogSystems.GIBRALTAR, CATEGORY, sourceProvider,
					null, null, null, null, reason, null);
		}
	}

	/**
	 * Send sessions using packager.
	 *
	 * @param criteria              Optional. A session criteria to use
	 * @param sessionMatchPredicate Optional. A session match predicate to use
	 * @param asyncSend the async send
	 * @return True if the send was processed, false if it was not due to
	 *         configuration or another active send Either a criteria or
	 *         sessionMatchPredicate must be provided
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static Boolean sendSessions(Optional<SessionCriteria> criteria,
			java.util.function.Predicate<ISessionSummary> sessionMatchPredicate, boolean asyncSend) throws IOException {
		if (!criteria.isPresent() && (sessionMatchPredicate == null)) {
			write(LogMessageSeverity.INFORMATION, Packager.LOG_CATEGORY,
					"Send session command ignored due to no criteria specified",
					"A session match predicate wasn't provided so nothing would be selected to send, skipping the send.");
			return false;
		}

		boolean result = false;
		Packager newPackager = null;
		synchronized (syncObject) {
			if (activePackager == null) {
				// we aren't doing a package, lets create a new one and process with it
				newPackager = new Packager();
				activePackager = newPackager; // this claims our spot
			}

			syncObject.notifyAll();
		}

		if (newPackager == null) {
			// someone else is sending
			write(LogMessageSeverity.INFORMATION, Packager.LOG_CATEGORY,
					"Send session command ignored due to ongoing send",
					"There is already a packager session send going on for the current application so this second request will be ignored to prevent interference.");
		} else {
			try {
				String message = null;
				String tempRefMessage = new String(message);
				if (isHubSubmissionConfigured(tempRefMessage) && (Packager.canSendToServer().isValid())) {
					message = tempRefMessage;
					// TODO: Kendall: We no longer have this event handler, will need to use another
					// strategy.
					// we DON'T release the active packager here, we do it in the event handler.
					ServerConfiguration config = runningConfiguration.getServer();

					if (criteria.isPresent()) {
						newPackager.sendToServer(criteria.get(), true, config.getPurgeSentSessions(), false, false,
								null, null, 0, false, null, null);
					} else {
						newPackager.sendToServer(sessionMatchPredicate, true, config.getPurgeSentSessions(), false,
								false, null, null, 0, false, null, null);
					}

					result = true;
				} else {
					message = tempRefMessage;
					// we can't send.
					write(LogMessageSeverity.INFORMATION, Packager.LOG_CATEGORY,
							"Send session command ignored due to configuration",
							"Either the current configuration doesn't support server or email submission or the server is not available.");

					// no good, dispose and clear the packager.
					synchronized (syncObject) {
						activePackager = null;

						syncObject.notifyAll();
					}
				}
			} catch (java.lang.Exception e) {
				// ReSharper restore EmptyGeneralCatchClause
				// That should never throw an exception, but it would not be good if it killed
				// NotifyDispatchMain() and
				// skipped setting the next notify time. So we'll swallow any exceptions here.
			}

		}

		return result;
	}

	/**
	 * Set the SendSessionsOnExit setting. (Should only be called through the
	 * SendSessionsOnExit property in Monitor.Log or Agent.Log.)
	 *
	 * @param value the new send sessions on exit
	 */
	public static void setSendSessionsOnExit(boolean value) {
		boolean valueChanged = false;
		boolean suppressedSet = false;
		String suppressMessage = "";

		// We work with the session boolean in a lock to be fully MT safe, but we don't
		// want to log in that lock
		// to ensure we can't deadlock, so we have to save our options to log after we
		// release the lock.
		synchronized (syncObject) {
			if (value != getSendSessionsOnExit()) {
				// Can only cancel it if it hasn't been launched yet: i.e. not
				// already exiting.
				if (!value && !isSessionEnding()) {
					valueChanged = true;
					sendSessionsOnExit = false;
				// Otherwise, we can't cancel it, so ignore the change (won't log anything,
				// either).
				} else {
					// before we just put it to true, we better make sure we CAN send.
					String tempRefSuppressMessage = new String(suppressMessage);
					if (canSendSessionsOnExit(tempRefSuppressMessage)) {
						suppressMessage = tempRefSuppressMessage;
						valueChanged = true;
						sendSessionsOnExit = true;
						if (isSessionEnding()) {
							sendSessionData(); // Already in exit mode, we need to kick off the after-exit packager.
						}

						// Otherwise, it will be fired off when EndSession() is called if the property
						// is still true.
					} else {
						suppressMessage = tempRefSuppressMessage;
						// we're suppressing the attempt - we'll need to log that.
						suppressedSet = true;
					}
				}
			}

			syncObject.notifyAll();
		}

		// now that we're not in the sync lock, go ahead and log what the deal is.
		if (valueChanged) {
			String caption = value ? "Session Data will be sent on exit" : "Session Data will not be sent on exit";
			String description = value ? "Session data will be submitted after the session exits."
					: "Session data will no longer be submitted after the session exits because the option was cleared.";
			writeMessage(LogMessageSeverity.INFORMATION, LogWriteMode.QUEUED, 2, null, null, caption, description);
		}

		if (suppressedSet) {
			writeMessage(LogMessageSeverity.WARNING, LogWriteMode.QUEUED, 2, null, null,
					"Unable to Send Session Data on Exit",
					"A request was made to send session data after the session exits, but the current configuration can't support it.\r\n%s",
					suppressMessage);
		}
	}

	/**
	 * Create a complete log message WITHOUT sending it to the Gibraltar central
	 * log.
	 * 
	 * This method is used internally to construct a complete LogMessagePacket,
	 * which can then be bundled with other packets (in an array) to be submitted to
	 * the log as a batch. This method ONLY supports being invoked on the same
	 * thread which is originating the log message.
	 *
	 * @param severity       The severity enum value of the log message.
	 * @param logSystem      The name of the originating log system, such as
	 *                       "Trace", "Log4Net", or "Gibraltar".
	 * @param category       The application subsystem or logging category that the
	 *                       log message is associated with, which can be a
	 *                       dot-delimited hierarchy (e.g. the logger name in
	 *                       log4net).
	 * @param sourceProvider An IMessageSourceProvider object which supplies the
	 *                       source information about this log message.
	 * @param userName       The effective username associated with the execution
	 *                       task which issued the log message.
	 * @param throwable      An Exception object attached to this log message, or
	 *                       null if none.
	 * @param threadInfo the thread info
	 * @param detailsXml     Optional. An XML document with extended details about
	 *                       the message. Can be null.
	 * @param caption        A single line display caption.
	 * @param description    Optional. A multi-line description to use which can be
	 *                       a format string for the arguments. Can be null.
	 * @param args           A variable number of arguments to insert into the
	 *                       formatted description string.
	 * @return the i messenger packet
	 */
	public static IMessengerPacket makeLogPacket(LogMessageSeverity severity, String logSystem, String category,
			IMessageSourceProvider sourceProvider, String userName, Throwable throwable, ThreadInfo threadInfo, String detailsXml,
			String caption, String description, Object... args) {
		if (!initialized) {
			return null;
		}

		LogMessagePacket packet = new LogMessagePacket();
		
		// if the log system does not provide thread info, we will attempt to capture it.
		if (threadInfo == null) {
			threadInfo = getCurrentThreadInfo();
		}
		
		packet.setThreadInfoPacket(threadInfo.getPacket()); // Set the ThreadInfoPacket property in the LogMessagePacket
		packet.setThreadIndex(threadInfo.getThreadIndex()); // This is how we actually identify the ThreadInfo we mean!
		packet.setThreadId(threadInfo.getThreadId()); // This is used by older code to identify the thread, but it's not
														// unique!
		
		// Some sanity-checks against null arguments.
		if (TypeUtils.isBlank(logSystem)) {
			logSystem = "Unknown";
		}

		if (TypeUtils.isBlank(category)) {
			category = GENERAL_CATEGORY;
		}

		if (runningConfiguration.getPublisher().getEnableAnonymousMode()) {
			userName = ""; // For now blank all user name data in anonymous mode.
		} else {
			// TODO: resolve user identity

			if (TypeUtils.isBlank(userName)) {
				// Get from session info.
				userName = sessionStartInfo.getFullyQualifiedUserName();
			}

		}

		String formattedDescription;
		if (args == null || args.length == 0) {
			// Since we aren't calling SafeFormat(), we have to protect against a null
			// message ourselves here.
			formattedDescription = (description != null) ? description : "";
		} else {
			// Note: This will handle the case of a null message, so let it have the
			// original message to report.
			formattedDescription = CommonCentralLogic.safeFormat(Locale.getDefault(), description, args);
		}

		if (description == null) {
			description = ""; // Must be a legal string.
		}

		if (description.length() > 0) {
			char lastChar = description.charAt(description.length() - 1);
			if (lastChar != '\n' && lastChar != '\r') {
				description += LINE_BREAK_STRING; // Make sure Description ends in a line break, unless it's
													// empty.
			}
		}

		String[] descriptionLines = description.split("\\r?\\n");
		int lineCount = descriptionLines.length;

		if (caption == null) {
			// Need to extract the Caption, leave off line-break and trim trailing
			// whitespace.
			caption = TypeUtils.trimToEmpty(descriptionLines[0]);

			// Now re-join the Description with optimal line break strings, including the
			// one at the end automatically.
			description = (lineCount > 1)
					? CodeConversionHelpers.join(LINE_BREAK_STRING, descriptionLines, 1, lineCount - 1)
					: "";
		} else {
			// Caption is already a valid string, so we don't extract it from description,
			// just trim trailing whitespace.
			caption = caption.trim();

			// And just normalize the Description's line breaks, including the one at the
			// end automatically.
			description = CodeConversionHelpers.join(LINE_BREAK_STRING, descriptionLines);
		}

		packet.setSeverity(severity);
		packet.setLogSystem(logSystem);
		packet.setCategoryName(category);
		packet.setUserName(userName);
		packet.setCaption(caption);
		packet.setDescription(formattedDescription);
		packet.setDetails(detailsXml);
		packet.setException(throwable);
		packet.setSourceInfo(sourceProvider);

		return packet;
	}

	/**
	 * Gets the current thread info.
	 *
	 * @return the current thread info
	 */
	private static ThreadInfo getCurrentThreadInfo() {
		if (!initialized) {
			return null;
		}

		// see if we already have a thread info object for the requested thread Id
		ThreadInfo curThreadInfo = tCurrentThreadInfo.get();

		if (curThreadInfo == null) {
			// we don't have it, go and create it. We rely on still being on the
			// thread in question so we can get OUR thread information
			tCurrentThreadInfo.set(new ThreadInfo()); //this captures and calculates hte information for the current thread.
			curThreadInfo = tCurrentThreadInfo.get();
		}

		return curThreadInfo;
	}

	/**
	 * Indicates if the calling thread is part of the log initialization process.
	 *
	 * @return the thread is initializer
	 */
	public static boolean getThreadIsInitializer() {
		return tThreadIsInitializer.get();
	}

	/**
	 * Sets the thread is initializer.
	 *
	 * @param value the new thread is initializer
	 */
	public static void setThreadIsInitializer(boolean value) {
		Log.tThreadIsInitializer.set(value);
	}

	/**
	 * Get the full file name and path to where the packager would need to be for us
	 * to use it.
	 *
	 * @return the packager file name path
	 */
	// TODO need more research... how to spin up another java process
	private static String getPackagerFileNamePath() {
		// assume the packager assembly is in our directory, what path would that be?
//		String proposedPath = Log.class.GetTypeInfo().Assembly.CodeBase;
//
//		// get rid of the standard code base prefix if it's there.
//		if (TypeUtils.startsWithIgnoreCase(proposedPath, "file:")) {
//			proposedPath = proposedPath.substring(5);
//
//			// now remove all backslashes at this point, since they can't mean a UNC path.
//			proposedPath = CodeConversionHelpers.trimStart(proposedPath, '\\', '/');
//		}
//
//		proposedPath = (new File(proposedPath)).getParent();
//
//		// the packager we're looking for depends on the version of .NET
//		String packagerExeName;
//		if (getSessionSummary().getRuntimeVersion().getMajor() >= 4) {
//			packagerExeName = "Gibraltar.Packager.NET40.exe";
//		} else {
//			packagerExeName = "Gibraltar.Packager.exe";
//		}
//
//		String packagerFileNamePath = Paths.get(proposedPath).resolve(packagerExeName).toString();
//
//		return packagerFileNamePath;
		return null;
	}

	/**
	 * Attempt to create a process to send the data for the current application
	 * using the packager.
	 */
	private static void sendSessionData() {
//		try
//		{
//			String packagerFileNamePath = getPackagerFileNamePath();
//
//			ProcessStartInfo packagerStartInfo = new ProcessStartInfo(packagerFileNamePath);
//			packagerStartInfo.CreateNoWindow = true;
//			packagerStartInfo.UseShellExecute = false;
//			packagerStartInfo.WorkingDirectory = (new File(packagerFileNamePath)).getParent();
//
//			//we need our process Id to signal the packager to wait for us to exit.
//			int ourPid = Process.GetCurrentProcess().Id;
//			StringBuilder argumentString = new StringBuilder(2048);
//
//			//there are two ways to build the command:  either we're sending to the server or to email.
//			String message = ""; //we don't use it, but we have to send it
//			String tempRefMessage = new String(message);
//			if ((isHubSubmissionConfigured(tempRefMessage)) && (Packager.canSendToServer().getIsValid()))
//			{
//				message = tempRefMessage;
//				argumentString.append(String.format("/s /w \"%1$s\" /m server /p \"%2$s\" /a \"%3$s\" ", ourPid, sessionStartInfo.getProduct(), sessionStartInfo.getApplication()));
//
//				//now see if we're sending customer or server information.
//				ServerConfiguration server = runningConfiguration.getServer();
//				if (server.getUseGibraltarService())
//				{
//					argumentString.append(String.format("/customer \"%1$s\" ", server.getCustomerName()));
//				}
//				else
//				{
//					argumentString.append(String.format("/server \"%1$s\" ", server.getServer()));
//
//					if (server.getUseSsl())
//					{
//						argumentString.append("/ssl \"true\" ");
//					}
//
//					if (server.getPort() != 0)
//					{
//						argumentString.append(String.format("/port \"%1$s\" ", server.getPort()));
//					}
//
//					if (TypeUtils.isBlank(server.getApplicationBaseDirectory()) == false)
//					{
//						argumentString.append(String.format("/directory \"%1$s\" ", server.getApplicationBaseDirectory()));
//					}
//
//					if (TypeUtils.isBlank(server.getRepository()) == false)
//					{
//						argumentString.append(String.format("/repository \"%1$s\" ", server.getRepository()));
//					}
//				}
//
//				//and determine if we should purge sent sessions...
//				if (server.getPurgeSentSessions())
//				{
//					argumentString.append("/purgeSentSessions \"true\" ");
//				}
//			}
//			else
//			{
//				message = tempRefMessage;
//
//				argumentString.append(String.format("/s /w \"%1$s\" /m email /p \"%2$s\" /a \"%3$s\" /d \"%4$s\" ", ourPid, sessionStartInfo.getProduct(), sessionStartInfo.getApplication(), runningConfiguration.getPackager().getDestinationEmailAddress()));
//
//				if (!TypeUtils.isBlank(runningConfiguration.getPackager().getFromEmailAddress()))
//				{
//					argumentString.append(String.format(" /f \"%1$s\" ", runningConfiguration.getPackager().getFromEmailAddress()));
//				}
//			}
//
//			//finally, did they override the file path we're supposed to use?
//			if (TypeUtils.isBlank(runningConfiguration.getSessionFile().getFolder()) == false)
//			{
//				argumentString.append(String.format("/folder \"%1$s\" ", runningConfiguration.getSessionFile().getFolder()));
//			}
//
//			packagerStartInfo.Arguments = argumentString.toString();
//
//			Process.Start(packagerStartInfo); //and we don't care what happens so once we've launched it we are outta here.
//		}
//		catch (RuntimeException ex)
//		{
//			
//		}
	}

	/**
	 * Normalize caption description.
	 *
	 * @param caption the caption
	 * @param description the description
	 */
	private static void normalizeCaptionDescription(String caption, String description) {
		if (description == null) {
			description = ""; // Must be a legal string.
		}

		if (description.length() > 0) {
			char lastChar = description.charAt(description.length() - 1);
			if (lastChar != '\n' && lastChar != '\r') {
				description += LINE_BREAK_STRING; // Make sure Description ends in a line break, unless it's
													// empty.
			}
		}

		String[] descriptionLines = description.split("\\r?\\n");
		int lineCount = descriptionLines.length;

		if (caption == null) {
			// Need to extract the Caption, leave off line-break and trim trailing
			// whitespace.
			caption = TypeUtils.trimToEmpty(descriptionLines[0]);

			// Now re-join the Description with optimal line break strings, including the
			// one at the end automatically.
			description = (lineCount > 1)
					? CodeConversionHelpers.join(LINE_BREAK_STRING, descriptionLines, 1, lineCount - 1)
					: "";
		} else {
			// Caption is already a valid string, so we don't extract it from description,
			// just trim trailing whitespace.
			caption = caption.trim();

			// And just normalize the Description's line breaks, including the one at the
			// end automatically.
			description = CodeConversionHelpers.join(LINE_BREAK_STRING, descriptionLines);
		}
	}

	/**
	 * Perform the critical central initialization and indicate if we should be
	 * active or not.
	 *
	 * @param configuration the configuration
	 * @return True if initialization was completed and logging can now commence,
	 *         false otherwise.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static boolean onInitialize(AgentConfiguration configuration) throws IOException {
		// make sure that we don't ever run again once we're initialized, that would be
		// bad.
		if (initialized) {
			return true;
		}
		
		Multiplexer.initialize();

		// Find out if we are going to be able to initialize or not.
		boolean suppressInitialize = false;
//		InitializingEventHandler tempEvent = (Object sender, LogInitializingEventArgs e) -> initializing.invoke(sender,
//				e);
//
		AgentConfiguration initialConfiguration = (configuration != null) ? configuration : new AgentConfiguration();
//		if (tempEvent != null) {
//			// we need to see if our callers will let us initialize.
//			LogInitializingEventArgs eventArgs = new LogInitializingEventArgs(initialConfiguration);
//			try {
//				tempEvent.invoke(null, eventArgs);
//			} catch (RuntimeException ex) {
//				// treat a fail as a cancel, so there's really nothing to do here - can't log
//				// it.
//				eventArgs.setCancel(true);
//			}
//
//			suppressInitialize = eventArgs.getCancel();
//		}

		if (suppressInitialize) {
			return false; // we are not going to start up, so we stay shut down.
		}

		// Now that we aren't going to cancel, go ahead and store this as our running
		// configuration and complete the initialization.

		// sanitize the configuration
		try {
			initialConfiguration.sanitize();
		} catch (RuntimeException ex) {
		}

		runningConfiguration = initialConfiguration;

		// if we're in debug mode then force the central silent mode option.
		if (runningConfiguration.getPublisher().getEnableDebugMode()) {
			Log.setSilentMode(false);
		}

		sessionStartInfo = new SessionSummary(runningConfiguration);

		if (runningConfiguration.getSessionFile().getEnabled()) {
				repository = new LocalRepository(sessionStartInfo.getProduct(),
						runningConfiguration.getSessionFile().getFolder());

		}

		// initialize our publisher
		String sessionName = String.format("%1$s %2$s %3$s %4$s", sessionStartInfo.getProduct(),
				sessionStartInfo.getApplication(), sessionStartInfo.getApplicationVersion(),
				OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")));

		publisher = new Publisher(sessionName, runningConfiguration, sessionStartInfo);

		resourceMonitor = new ResourceMonitor();
		resourceMonitor.startMonitors();
		
		// record our session start info right now so we're sure it's the first packet
		// we have.
		write(sessionStartInfo.getPacket());

		// initialize the listener architecture.
		Listener.initialize(runningConfiguration);

		// and we need to load up the session publisher if it's enabled.
		startPublishEngine(); // this checks to see if it can start based on configuration

		// and now we're initialized!
		return true;
	}

	/**
	 * Ensure summary is available.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static void ensureSummaryIsAvailable() throws IOException {
		// make sure we aren't in a race condition starting to initialize...
		if ((!initialized) && ((runningConfiguration == null) || (sessionStartInfo == null))) {
			// it's initializing, but not yet complete - we need to stall until it is.
			synchronized (initializingLock) {
				while (initializing) // the status is still indeterminate.
				{
					try {
						initializingLock.wait();
					} catch (InterruptedException e) {

					}
				}

				// OK, right now we HAVE the initializing lock so no thread can sneak in and
				// have us damage what it's up to.
				runningConfiguration = new AgentConfiguration();
				sessionStartInfo = new SessionSummary(runningConfiguration);

				initializingLock.notifyAll();
			}
		}
	}

	/**
	 * If the configuration allows publishing then starts our one publish engine,
	 * creating it if necessary.
	 */
	private static void startPublishEngine() {
		try {
			// we try to keep the lock for the shortest time period we can.
			RepositoryPublishEngine publishEngine = null;
			synchronized (syncObject) {
				if ((runningConfiguration.getServer().getEnabled())
						&& (runningConfiguration.getServer().getAutoSendSessions())) {
					if (publishEngine == null) {
						publishEngine = new RepositoryPublishEngine(publisher, runningConfiguration);
					}

					if (runningConfiguration.getServer().getAutoSendOnError()) {
						Notifier notifier = getMessageAlertNotifier(); // poking this creates our
																		// background threads.
					}
				}

				syncObject.notifyAll();
			}

			if (publishEngine != null) {
				publishEngine.start();
			}
		} catch (RuntimeException ex) {

		}
	}

	/**
	 * Shutdown the publish engine if it exists without waiting for it to complete.
	 */
	private static void shutdownPublishEngine() {
		RepositoryPublishEngine localPublishEngine;
		synchronized (syncObject) {
			localPublishEngine = publishEngine;
			syncObject.notifyAll();
		}

		if (localPublishEngine != null) {
			localPublishEngine.stop(false);
		}
	}
	
	/**
	 * Reset.
	 */
	private static void reset() {
		Multiplexer.shutdown();
		Multiplexer.reset();
		
		if (resourceMonitor != null) {
			IOUtils.closeQuietly(resourceMonitor);
			resourceMonitor = null;
		}
		
		if (publisher != null) {
			IOUtils.closeQuietly(publisher);
			publisher = null;
		}
		Publisher.reset();
		
		if (userResolutionNotifier != null) {
			IOUtils.closeQuietly(userResolutionNotifier);
			userResolutionNotifier = null;
		}
		UserResolutionNotifier.reset();
		
		tCurrentThreadInfo = new ThreadLocal<>();
		
		tThreadIsInitializer = new ThreadLocal<Boolean>() {
			@Override
			protected Boolean initialValue() {
				// TODO Auto-generated method stub
				return Boolean.FALSE;
			}
		};
		
		
		activePackager = null;		
		repository = null;
		sessionStartInfo = null;
		initialized = false;
		initializing = false;
		initializationNeverAttempted = false;
		explicitStartSessionCalled = false;
		shutdownRequested = false;
		
		metricDefinitions = new MetricDefinitionCollection();
		runningConfiguration = null;
		
		if (appenderRefs != null && !appenderRefs.isEmpty()) {
			appenderRefs.clear();
		}
		
		CommonCentralLogic.declareSessionHasEnded();
	}
}