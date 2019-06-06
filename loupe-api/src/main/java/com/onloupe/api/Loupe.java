package com.onloupe.api;

import com.onloupe.agent.IMessageSourceProvider;
import com.onloupe.agent.SessionCriteria;
import com.onloupe.agent.SessionSummary;
import com.onloupe.agent.logging.MessageSourceProvider;
import com.onloupe.configuration.AgentConfiguration;
import com.onloupe.core.logging.LogMessage;
import com.onloupe.core.logging.LogMessage;
import com.onloupe.core.logging.Log;
import com.onloupe.core.logging.LogWriteMode;
import com.onloupe.core.logging.LogMessage;
import com.onloupe.core.logging.ThreadInfo;
import com.onloupe.core.metrics.MetricDefinitionCollection;
import com.onloupe.core.util.LogSystems;
import com.onloupe.model.log.LogMessageSeverity;
import com.onloupe.model.session.ISessionSummary;
import com.onloupe.model.session.SessionStatus;
import com.onloupe.model.system.Version;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;


/**
 * This static class is the primary API for logging with the Loupe Agent.
 * 
 * <p>
 * This Log class provides the API for directly logging to Loupe and for
 * receiving log messages from logging systems such as log4net. Messages sent
 * directly to Loupe will not go through System.Diagnostics.Trace and will not
 * be seen by trace listeners or by other logging systems, but can take direct
 * advantage of Loupe's logging features.
 * </p>
 * <p>
 * The logging API provides different groups of methods for different levels of
 * simplicity verses flexibility.
 * </p>
 * <ul> <li><strong>Trace Methods:</strong> Designed to mirror
 * the Trace class built into .NET, these provide the simplest API and are a
 * direct substitute for existing calls that use the Trace method (simply change
 * the class name from System.Diagnostics.Trace to Gibraltar.Agent.Log)</li>
 * <li><strong>Severity Methods:</strong> A method for each Loupe severity
 * from Critical (the most severe) to Verbose (the least). These provide a full
 * featured API for logging directly to Loupe as part of your
 * application.</li> <li><strong>Write Methods:</strong> Used to forward log
 * messages into the Loupe Agent from an external logging system or logging
 * aggregation class. These expose the most capability but are generally
 * unnecessary outside of the message forwarding scenario.</li> </ul>
 * <p>
 * <strong>Trace Methods</strong>
 * </p>
 * <p>
 * The various Trace methods provide a quick way to record a log message at a
 * chosen severity level with the fewest arguments to manage. These methods
 * include direct replacements for Trace.TraceInformation, Trace.TraceWarning,
 * and Trace.TraceError, as well as a Trace() call (replacing the use of
 * Trace.Write and Trace.WriteLine for logging Verbose messages).
 * </p>
 * <p>
 * In addition to the direct replacement calls for the Trace API an additional
 * TraceCritical Method method
 * was added for logging fatal errors.
 * </p>
 * <p>
 * Each of these methods also provides an overload which accepts an Exception
 * object as the first parameter. By providing the exception object with the
 * method, extended information about the exception is recorded which can
 * significantly improve the utility of the log information without requiring it
 * to be included in the message.
 * </p>
 * <p>
 * When using Trace exclusively, it's recommended that you include a call to
 * Trace.Close at the very end of your application's execution. This will ensure
 * that all Trace Listeners are shut down correctly, and the Agent will use this
 * to record that the session closed normally and start its shutdown procedure
 * by automatically calling Log.EndSession.
 * </p>
 * <p>
 * For more information, see <a href="Logging_Trace.html">Developer's Reference
 * - Logging - Using with Trace</a>.
 * </p>
 * <p>
 * <strong>Severity Methods</strong>
 * </p>
 * <p>
 * The Severity Methods (named after each severity level) provide the most
 * commonly-needed features of Loupe's logging capability. In order from most to
 * least severe, these are:
 * </p>
 * <ul> <li>
 * Log.Critical
 * </li> <li>
 * Log.Error </li>
 * <li>
 * Log.Warning
 * </li> <li> Log.Information </li> <li>
 * Log.Verbose
 * </li> </ul>
 * <p>
 * Each of these methods in their simplest form takes Category, Caption, and
 * Description instead of just a single Message to take best advantage of
 * Loupe's ability to group similar messages for analysis and reporting.
 * Additional overloads allow an Exception object to be specified (regardless of
 * severity) and allow the message to be committed to disk in the session file
 * before the thread's execution continues.
 * </p>
 * <p>
 * For more advanced usage, each Severity method has a corresponding Detail
 * method that supports recording an XML document string with details for more
 * sophisticated examination. This information can be formatted in the Loupe
 * Analyst to provide end users with extended drill-in data about a particular
 * situation. Because the logging data is highly compressed (typically 80
 * percent or more for strings over 5kb), it's safe to record XML documents
 * without overwhelming the session files.
 * </p>
 * <p>
 * For more information, see <a href="Logging_DirectLogging.html">Developer's
 * Reference - Logging Directly to Loupe</a>.
 * </p>
 * <p>
 * <strong>Write Method</strong>
 * </p>
 * <p>
 * If you are already using a different logging system than Trace or the Loupe
 * Agent you can forward messages from it into the Agent by using the Write
 * method. The two overloads of the Write method are designed to support both
 * full featured external log systems that can capture extended information,
 * origin information for the log message, and even override the user identity.
 * </p>
 * <p>
 * Another common scenario supported by Write is an existing application with a
 * central class that all logging is being routed through. The
 *  Log.Write method is
 * designed to support this easily while still allowing you to take advantage of
 * the safe formatting and origin determination capabilities of the Loupe Agent.
 * </p>
 * <p>
 * For more information, see
 * <a href="Logging_ExternalLogSystems.html">Developer's Reference - Logging -
 * Using with External Log Systems</a>.
 * </p>
 * <p>
 * <strong>Starting a Session</strong>
 * </p>
 * <p>
 * The Log object will attempt to start the first time it is used, or any time a
 * call is made to StartSession. When it
 * starts, it will raise its Log.Initializing
 * event to allow for configuration overrides to be done in code and for the
 * startup sequence to be canceled. If the startup is canceled, all API
 * functions continue to work but no Agent functionality is available. This is a
 * high speed mode that allows any agent overhead to be removed from the process
 * without altering the control flow or recompiling the application.
 * </p>
 * <p>
 * <strong>Ending a Session</strong>
 * </p>
 * <p>
 * It's a best practice at the end of your application's normal execution path
 * to include a call to Log.EndSession. This
 * performs several functions:
 * </p>
 * <ol> <li>It marks the session as ending normally.
 * Regardless of how the process exits after EndSession is called, it will not
 * be considered crashed.</li> <li>All queued information is flushed to disk
 * and all subsequent write requests are handled as WaitForCommit requests to
 * ensure that no messages are lost.</li> <li>Various internal changes are
 * made to ensure that the process will exit quickly. If no EndSession call is
 * made, the Agent may keep the process alive even if it normally would have
 * exited.</li> </ol>
 * <p>
 * You can safely call EndSession multiple times.
 * </p>
 * <p>
 * <strong>Configuring the Agent</strong>
 * </p>
 * <p>
 * The agent can be configured in the application configuration file, through
 * code, or both. To configure the agent in code you must subscribe to the
 * Log.Initializing event before the agent is
 * started and then manipulate the Agent
 * configuration object and its child objects. If any configuration was supplied
 * in the application configuration file that will have already been loaded into
 * the configuration objects when the event is raised.
 * </p>
 * 
 * @see <a href="Logging_Trace.html">Developer's Reference Logging - Using with Trace</a>
 * @see <a href="Logging_ExternalLogSystems.html">Developer's Reference Logging - Using with External Log Systems</a>
 * @see <a href="Logging_DirectLogging.html">Developer's Reference Logging - Using Loupe as a Log System</a>
 */
public final class Loupe {
	/**
	 * The file extension (without period) for a Loupe Package File.
	 */
	public static final String PACKAGE_EXTENSION = "Log.PackageExtension";

	/** The Constant THIS_LOG_SYSTEM. */
	private static final String THIS_LOG_SYSTEM = "Loupe";
	
	/** The Constant CATEGORY. */
	private static final String CATEGORY = "Log.Category";
	
	/** The Constant EXCEPTION_CATEGORY. */
	private static final String EXCEPTION_CATEGORY = "Log.ExceptionCategory";
	
	/** The Constant QUEUED. */
	private static final LogWriteMode QUEUED = LogWriteMode.QUEUED;

	/** The Constant metricDefinitions. */
	// Create a wrapped session summary so it's available to users of the agent.
	private static final MetricDefinitionCollection metricDefinitions = Log.getMetrics();

	static {
		// make sure that we put logging in silent mode - we're the agent!
		Log.setSilentMode(true);
	}

	/**
	 * Indicates if the agent should package &amp; send sessions for the current
	 * application after this session exits.
	 * 
	 * 
	 * When true the system will automatically spawn the packager to send all unsent
	 * sessions for the current application. This is only supported if the packager
	 * is enabled and configured to submit sessions via Loupe Server and/or to send
	 * packages via email. Loupe Server will be used by preference if available, but
	 * email can be used as a fall-back option. If sessions can't be sent on exit,
	 * the property can still be set but will stay false. No exception will be
	 * thrown.
	 *
	 * @return the send sessions on exit
	 */
	public static boolean getSendSessionsOnExit() {
		return Log.getSendSessionsOnExit();
	}

	/**
	 * Sets the send sessions on exit.
	 *
	 * @param value the new send sessions on exit
	 */
	private static void setSendSessionsOnExit(boolean value) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		Log.setSendSessionsOnExit(value); // Jump to the method (not property) for correct source attribution.
	}

	/**
	 * The version information for the Loupe Agent.
	 *
	 * @return the agent version
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static Version getAgentVersion() throws IOException {
		return Log.getAgentVersion();
	}

	//
	// VERBOSE
	//

	/**
	 * Write a categorized Verbose message directly to the Loupe log.
	 * 
	 * <p>
	 * This method provides basic Loupe logging features for typical use. Loupe
	 * supports a separate caption and description in log messages in order to
	 * provide better analysis capability. Log messages can be grouped by their
	 * captions even while their full descriptions differ, so for more useful
	 * matching we don't provide format processing on the caption argument, only on
	 * the description argument.
	 * </p>
	 * <p>
	 * The caption and description arguments tolerate null and empty strings (e.g. a
	 * simple one-line message caption with no further description needed). A null
	 * caption will cause the message caption to be extracted from the description
	 * after format processing (comparable to using the Trace...() methods which
	 * don't take a separate caption argument). A valid string caption argument,
	 * including an empty string, will be taken as the intended caption; an empty
	 * caption string is thus possible, but not recommended.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
	 * </p>
	 * 
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void verbose(String category, String caption, String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.VERBOSE, THIS_LOG_SYSTEM, category, 1,
				caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a categorized Verbose message directly to the Loupe log with an
	 * attached Exception.
	 * 
	 * <p>
	 * This method provides basic Loupe logging features for typical use. Loupe
	 * supports a separate caption and description in log messages in order to
	 * provide better analysis capability. Log messages can be grouped by their
	 * captions even while their full descriptions differ, so for more useful
	 * matching we don't provide format processing on the caption argument, only on
	 * the description argument.
	 * </p>
	 * <p>
	 * The caption and description arguments tolerate null and empty strings (e.g. a
	 * simple one-line message caption with no further description needed). A null
	 * caption will cause the message caption to be extracted from the description
	 * after format processing (comparable to using the Trace...() methods which
	 * don't take a separate caption argument). A valid string caption argument,
	 * including an empty string, will be taken as the intended caption; an empty
	 * caption string is thus possible, but not recommended.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
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
	 * @param exception   An Exception object to attach to this log message.
	 * @param threadInfo the thread info
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void verbose(Throwable exception, ThreadInfo threadInfo, String category, String caption, String description,
			Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.VERBOSE, QUEUED, THIS_LOG_SYSTEM, category,
				1, exception, threadInfo, false, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a categorized Verbose message directly to the Loupe log, specifying
	 * Queued or WaitForCommit behavior.
	 * 
	 * <p>
	 * This method provides basic Loupe logging features for typical use. Loupe
	 * supports a separate caption and description in log messages in order to
	 * provide better analysis capability. Log messages can be grouped by their
	 * captions even while their full descriptions differ, so for more useful
	 * matching we don't provide format processing on the caption argument, only on
	 * the description argument.
	 * </p>
	 * <p>
	 * The caption and description arguments tolerate null and empty strings (e.g. a
	 * simple one-line message caption with no further description needed). A null
	 * caption will cause the message caption to be extracted from the description
	 * after format processing (comparable to using the Trace...() methods which
	 * don't take a separate caption argument). A valid string caption argument,
	 * including an empty string, will be taken as the intended caption; an empty
	 * caption string is thus possible, but not recommended.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
	 * </p>
	 * <p>
	 * The writeMode argument allows the caller to specify WaitForCommit behavior,
	 * which will not return until the message has been committed to the session
	 * file on disk. The Queued behavior used by default with other overloads of
	 * this method places the message on Loupe's central queue and then returns,
	 * allowing the current thread execution to continue while Loupe processes the
	 * queue on a separate thread.
	 * </p>
	 * 
	 * @param writeMode   Whether to queue-and-return or wait-for-commit.
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void verbose(LogWriteMode writeMode, String category, String caption, String description,
			Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.VERBOSE, LogWriteMode.forValue(writeMode),
				THIS_LOG_SYSTEM, category, 1, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a categorized Verbose message directly to the Loupe log with an
	 * attached Exception and specifying Queued or WaitForCommit behavior.
	 * 
	 * <p>
	 * This method provides basic Loupe logging features for typical use. Loupe
	 * supports a separate caption and description in log messages in order to
	 * provide better analysis capability. Log messages can be grouped by their
	 * captions even while their full descriptions differ, so for more useful
	 * matching we don't provide format processing on the caption argument, only on
	 * the description argument.
	 * </p>
	 * <p>
	 * The caption and description arguments tolerate null and empty strings (e.g. a
	 * simple one-line message caption with no further description needed). A null
	 * caption will cause the message caption to be extracted from the description
	 * after format processing (comparable to using the Trace...() methods which
	 * don't take a separate caption argument). A valid string caption argument,
	 * including an empty string, will be taken as the intended caption; an empty
	 * caption string is thus possible, but not recommended.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
	 * </p>
	 * <p>
	 * This overload also allows an Exception object to be attached to the log
	 * message. An Exception-typed null (e.g. from a variable of an Exception type)
	 * is allowed for the exception argument, but calls which do not have a possible
	 * Exception to attach should use an overload without an exception argument
	 * rather than pass a direct value of null, to avoid compiler ambiguity over the
	 * type of a simple null.
	 * </p>
	 * <p>
	 * The writeMode argument allows the caller to specify WaitForCommit behavior,
	 * which will not return until the message has been committed to the session
	 * file on disk. The Queued behavior used by default with other overloads of
	 * this method places the message on Loupe's central queue and then returns,
	 * allowing the current thread execution to continue while Loupe processes the
	 * queue on a separate thread.
	 * </p>
	 *
	 * @param exception   An Exception object to attach to this log message.
	 * @param threadInfo the thread info
	 * @param writeMode   Whether to queue-and-return or wait-for-commit.
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void verbose(Throwable exception, ThreadInfo threadInfo, LogWriteMode writeMode, String category, String caption,
			String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.VERBOSE, LogWriteMode.forValue(writeMode),
				THIS_LOG_SYSTEM, category, 1, exception, threadInfo, false, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a detailed Verbose message directly to the Loupe log.
	 * 
	 * <p>
	 * This method provides an advanced use of Loupe log messages to include an XML
	 * document (as a string) containing extended details about the message. Also
	 * see the Write() method for when XML details are not needed. This method is
	 * otherwise similar to Write().
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
	 * </p>
	 * 
	 * @param detailsXml  An XML document (as a string) with extended details about
	 *                    the message.
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void verboseDetail(String detailsXml, String category, String caption, String description,
			Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.VERBOSE, THIS_LOG_SYSTEM, category, 1,
				detailsXml, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a detailed Verbose message directly to the Loupe log with an attached
	 * Exception.
	 * 
	 * <p>
	 * This method provides an advanced use of Loupe log messages to include an XML
	 * document (as a string) containing extended details about the message.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
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
	 * @param exception   An Exception object to attach to this log message.
	 * @param detailsXml  An XML document (as a string) with extended details about
	 *                    the message.
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void verboseDetail(Throwable exception, String detailsXml, String category, String caption,
			String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.VERBOSE, QUEUED, THIS_LOG_SYSTEM,
				category, 1, exception, false, detailsXml, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a detailed Verbose message directly to the Loupe log, specifying Queued
	 * or WaitForCommit behavior.
	 * 
	 * <p>
	 * This method provides an advanced use of Loupe log messages to include an XML
	 * document (as a string) containing extended details about the message.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
	 * </p>
	 * 
	 * @param writeMode   Whether to queue-and-return or wait-for-commit.
	 * @param detailsXml  An XML document (as a string) with extended details about
	 *                    the message.
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void verboseDetail(LogWriteMode writeMode, String detailsXml, String category, String caption,
			String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.VERBOSE, LogWriteMode.forValue(writeMode),
				THIS_LOG_SYSTEM, category, 1, detailsXml, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a detailed Verbose message directly to the Loupe log with an optional
	 * attached Exception and specifying Queued or WaitForCommit behavior.
	 * 
	 * <p>
	 * This method provides an advanced use of Loupe log messages to include an XML
	 * document (as a string) containing extended details about the message.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
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
	 * @param exception   An Exception object to attach to this log message.
	 * @param writeMode   Whether to queue-and-return or wait-for-commit.
	 * @param detailsXml  An XML document (as a string) with extended details about
	 *                    the message.
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void verboseDetail(Throwable exception, LogWriteMode writeMode, String detailsXml,
			String category, String caption, String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.VERBOSE, LogWriteMode.forValue(writeMode),
				THIS_LOG_SYSTEM, category, 1, exception, false, detailsXml, caption, description, args);

		logMessage.publishToLog();
	}

	//
	// INFORMATION
	//

	/**
	 * Write a categorized Information message directly to the Loupe log.
	 * 
	 * <p>
	 * This method provides basic Loupe logging features for typical use. Loupe
	 * supports a separate caption and description in log messages in order to
	 * provide better analysis capability. Log messages can be grouped by their
	 * captions even while their full descriptions differ, so for more useful
	 * matching we don't provide format processing on the caption argument, only on
	 * the description argument.
	 * </p>
	 * <p>
	 * The caption and description arguments tolerate null and empty strings (e.g. a
	 * simple one-line message caption with no further description needed). A null
	 * caption will cause the message caption to be extracted from the description
	 * after format processing (comparable to using the Trace...() methods which
	 * don't take a separate caption argument). A valid string caption argument,
	 * including an empty string, will be taken as the intended caption; an empty
	 * caption string is thus possible, but not recommended.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
	 * </p>
	 * 
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void information(String category, String caption, String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.INFORMATION, THIS_LOG_SYSTEM, category, 1,
				caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a categorized Information message directly to the Loupe log with an
	 * attached Exception.
	 * 
	 * <p>
	 * This method provides basic Loupe logging features for typical use. Loupe
	 * supports a separate caption and description in log messages in order to
	 * provide better analysis capability. Log messages can be grouped by their
	 * captions even while their full descriptions differ, so for more useful
	 * matching we don't provide format processing on the caption argument, only on
	 * the description argument.
	 * </p>
	 * <p>
	 * The caption and description arguments tolerate null and empty strings (e.g. a
	 * simple one-line message caption with no further description needed). A null
	 * caption will cause the message caption to be extracted from the description
	 * after format processing (comparable to using the Trace...() methods which
	 * don't take a separate caption argument). A valid string caption argument,
	 * including an empty string, will be taken as the intended caption; an empty
	 * caption string is thus possible, but not recommended.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
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
	 * @param exception   An Exception object to attach to this log message.
	 * @param threadInfo the thread info
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void information(Throwable exception, ThreadInfo threadInfo, String category, String caption, String description,
			Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.INFORMATION, QUEUED, THIS_LOG_SYSTEM,
				category, 1, exception, threadInfo, false, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a categorized Information message directly to the Loupe log, specifying
	 * Queued or WaitForCommit behavior.
	 * 
	 * <p>
	 * This method provides basic Loupe logging features for typical use. Loupe
	 * supports a separate caption and description in log messages in order to
	 * provide better analysis capability. Log messages can be grouped by their
	 * captions even while their full descriptions differ, so for more useful
	 * matching we don't provide format processing on the caption argument, only on
	 * the description argument.
	 * </p>
	 * <p>
	 * The caption and description arguments tolerate null and empty strings (e.g. a
	 * simple one-line message caption with no further description needed). A null
	 * caption will cause the message caption to be extracted from the description
	 * after format processing (comparable to using the Trace...() methods which
	 * don't take a separate caption argument). A valid string caption argument,
	 * including an empty string, will be taken as the intended caption; an empty
	 * caption string is thus possible, but not recommended.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
	 * </p>
	 * <p>
	 * The writeMode argument allows the caller to specify WaitForCommit behavior,
	 * which will not return until the message has been committed to the session
	 * file on disk. The Queued behavior used by default with other overloads of
	 * this method places the message on Loupe's central queue and then returns,
	 * allowing the current thread execution to continue while Loupe processes the
	 * queue on a separate thread.
	 * </p>
	 * 
	 * @param writeMode   Whether to queue-and-return or wait-for-commit.
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void information(LogWriteMode writeMode, String category, String caption, String description,
			Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.INFORMATION,
				LogWriteMode.forValue(writeMode), THIS_LOG_SYSTEM, category, 1, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a categorized Information message directly to the Loupe log with an
	 * attached Exception and specifying Queued or WaitForCommit behavior.
	 * 
	 * <p>
	 * This method provides basic Loupe logging features for typical use. Loupe
	 * supports a separate caption and description in log messages in order to
	 * provide better analysis capability. Log messages can be grouped by their
	 * captions even while their full descriptions differ, so for more useful
	 * matching we don't provide format processing on the caption argument, only on
	 * the description argument.
	 * </p>
	 * <p>
	 * The caption and description arguments tolerate null and empty strings (e.g. a
	 * simple one-line message caption with no further description needed). A null
	 * caption will cause the message caption to be extracted from the description
	 * after format processing (comparable to using the Trace...() methods which
	 * don't take a separate caption argument). A valid string caption argument,
	 * including an empty string, will be taken as the intended caption; an empty
	 * caption string is thus possible, but not recommended.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
	 * </p>
	 * <p>
	 * This overload also allows an Exception object to be attached to the log
	 * message. An Exception-typed null (e.g. from a variable of an Exception type)
	 * is allowed for the exception argument, but calls which do not have a possible
	 * Exception to attach should use an overload without an exception argument
	 * rather than pass a direct value of null, to avoid compiler ambiguity over the
	 * type of a simple null.
	 * </p>
	 * <p>
	 * The writeMode argument allows the caller to specify WaitForCommit behavior,
	 * which will not return until the message has been committed to the session
	 * file on disk. The Queued behavior used by default with other overloads of
	 * this method places the message on Loupe's central queue and then returns,
	 * allowing the current thread execution to continue while Loupe processes the
	 * queue on a separate thread.
	 * </p>
	 *
	 * @param exception   An Exception object to attach to this log message.
	 * @param threadInfo the thread info
	 * @param writeMode   Whether to queue-and-return or wait-for-commit.
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void information(Throwable exception, ThreadInfo threadInfo, LogWriteMode writeMode, String category, String caption,
			String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.INFORMATION,
				LogWriteMode.forValue(writeMode), THIS_LOG_SYSTEM, category, 1, exception,
				threadInfo, false, caption, description, args);

		logMessage.publishToLog();
	}
	
	/**
	 * Write a detailed Information message directly to the Loupe log.
	 * 
	 * <p>
	 * This method provides an advanced use of Loupe log messages to include an XML
	 * document (as a string) containing extended details about the message. Also
	 * see the Write() method for when XML details are not needed. This method is
	 * otherwise similar to Write().
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
	 * </p>
	 * 
	 * @param detailsXml  An XML document (as a string) with extended details about
	 *                    the message.
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void informationDetail(String detailsXml, String category, String caption, String description,
			Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.INFORMATION, THIS_LOG_SYSTEM, category, 1,
				detailsXml, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a detailed Information message directly to the Loupe log with an
	 * attached Exception.
	 * 
	 * <p>
	 * This method provides an advanced use of Loupe log messages to include an XML
	 * document (as a string) containing extended details about the message.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
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
	 * @param exception   An Exception object to attach to this log message.
	 * @param detailsXml  An XML document (as a string) with extended details about
	 *                    the message.
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void informationDetail(Throwable exception, String detailsXml, String category, String caption,
			String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.INFORMATION, QUEUED, THIS_LOG_SYSTEM,
				category, 1, exception, false, detailsXml, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a detailed Information message directly to the Loupe log, specifying
	 * Queued or WaitForCommit behavior.
	 * 
	 * <p>
	 * This method provides an advanced use of Loupe log messages to include an XML
	 * document (as a string) containing extended details about the message.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
	 * </p>
	 * 
	 * @param writeMode   Whether to queue-and-return or wait-for-commit.
	 * @param detailsXml  An XML document (as a string) with extended details about
	 *                    the message.
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void informationDetail(LogWriteMode writeMode, String detailsXml, String category, String caption,
			String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.INFORMATION,
				LogWriteMode.forValue(writeMode), THIS_LOG_SYSTEM, category, 1, detailsXml, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a detailed Information message directly to the Loupe log with an
	 * optional attached Exception and specifying Queued or WaitForCommit behavior.
	 * 
	 * <p>
	 * This method provides an advanced use of Loupe log messages to include an XML
	 * document (as a string) containing extended details about the message.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
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
	 * @param exception   An Exception object to attach to this log message.
	 * @param writeMode   Whether to queue-and-return or wait-for-commit.
	 * @param detailsXml  An XML document (as a string) with extended details about
	 *                    the message.
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void informationDetail(Throwable exception, LogWriteMode writeMode, String detailsXml,
			String category, String caption, String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.INFORMATION,
				LogWriteMode.forValue(writeMode), THIS_LOG_SYSTEM, category, 1, exception, false, detailsXml, caption,
				description, args);

		logMessage.publishToLog();
	}

	//
	// WARNING
	//

	/**
	 * Write a categorized Warning message directly to the Loupe log.
	 * 
	 * <p>
	 * This method provides basic Loupe logging features for typical use. Loupe
	 * supports a separate caption and description in log messages in order to
	 * provide better analysis capability. Log messages can be grouped by their
	 * captions even while their full descriptions differ, so for more useful
	 * matching we don't provide format processing on the caption argument, only on
	 * the description argument.
	 * </p>
	 * <p>
	 * The caption and description arguments tolerate null and empty strings (e.g. a
	 * simple one-line message caption with no further description needed). A null
	 * caption will cause the message caption to be extracted from the description
	 * after format processing (comparable to using the Trace...() methods which
	 * don't take a separate caption argument). A valid string caption argument,
	 * including an empty string, will be taken as the intended caption; an empty
	 * caption string is thus possible, but not recommended.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
	 * </p>
	 * 
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void warning(String category, String caption, String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.WARNING, THIS_LOG_SYSTEM, category, 1,
				caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a categorized Warning message directly to the Loupe log with an
	 * attached Exception.
	 * 
	 * <p>
	 * This method provides basic Loupe logging features for typical use. Loupe
	 * supports a separate caption and description in log messages in order to
	 * provide better analysis capability. Log messages can be grouped by their
	 * captions even while their full descriptions differ, so for more useful
	 * matching we don't provide format processing on the caption argument, only on
	 * the description argument.
	 * </p>
	 * <p>
	 * The caption and description arguments tolerate null and empty strings (e.g. a
	 * simple one-line message caption with no further description needed). A null
	 * caption will cause the message caption to be extracted from the description
	 * after format processing (comparable to using the Trace...() methods which
	 * don't take a separate caption argument). A valid string caption argument,
	 * including an empty string, will be taken as the intended caption; an empty
	 * caption string is thus possible, but not recommended.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
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
	 * @param exception   An Exception object to attach to this log message.
	 * @param threadInfo the thread info
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void warning(Throwable exception, ThreadInfo threadInfo, String category, String caption, String description,
			Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.WARNING, QUEUED, THIS_LOG_SYSTEM, category,
				1, exception, threadInfo, false, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a categorized Warning message directly to the Loupe log, specifying
	 * Queued or WaitForCommit behavior.
	 * 
	 * <p>
	 * This method provides basic Loupe logging features for typical use. Loupe
	 * supports a separate caption and description in log messages in order to
	 * provide better analysis capability. Log messages can be grouped by their
	 * captions even while their full descriptions differ, so for more useful
	 * matching we don't provide format processing on the caption argument, only on
	 * the description argument.
	 * </p>
	 * <p>
	 * The caption and description arguments tolerate null and empty strings (e.g. a
	 * simple one-line message caption with no further description needed). A null
	 * caption will cause the message caption to be extracted from the description
	 * after format processing (comparable to using the Trace...() methods which
	 * don't take a separate caption argument). A valid string caption argument,
	 * including an empty string, will be taken as the intended caption; an empty
	 * caption string is thus possible, but not recommended.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
	 * </p>
	 * <p>
	 * The writeMode argument allows the caller to specify WaitForCommit behavior,
	 * which will not return until the message has been committed to the session
	 * file on disk. The Queued behavior used by default with other overloads of
	 * this method places the message on Loupe's central queue and then returns,
	 * allowing the current thread execution to continue while Loupe processes the
	 * queue on a separate thread.
	 * </p>
	 * 
	 * @param writeMode   Whether to queue-and-return or wait-for-commit.
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void warning(LogWriteMode writeMode, String category, String caption, String description,
			Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.WARNING, LogWriteMode.forValue(writeMode),
				THIS_LOG_SYSTEM, category, 1, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a categorized Warning message directly to the Loupe log with an
	 * attached Exception and specifying Queued or WaitForCommit behavior.
	 * 
	 * <p>
	 * This method provides basic Loupe logging features for typical use. Loupe
	 * supports a separate caption and description in log messages in order to
	 * provide better analysis capability. Log messages can be grouped by their
	 * captions even while their full descriptions differ, so for more useful
	 * matching we don't provide format processing on the caption argument, only on
	 * the description argument.
	 * </p>
	 * <p>
	 * The caption and description arguments tolerate null and empty strings (e.g. a
	 * simple one-line message caption with no further description needed). A null
	 * caption will cause the message caption to be extracted from the description
	 * after format processing (comparable to using the Trace...() methods which
	 * don't take a separate caption argument). A valid string caption argument,
	 * including an empty string, will be taken as the intended caption; an empty
	 * caption string is thus possible, but not recommended.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
	 * </p>
	 * <p>
	 * This overload also allows an Exception object to be attached to the log
	 * message. An Exception-typed null (e.g. from a variable of an Exception type)
	 * is allowed for the exception argument, but calls which do not have a possible
	 * Exception to attach should use an overload without an exception argument
	 * rather than pass a direct value of null, to avoid compiler ambiguity over the
	 * type of a simple null.
	 * </p>
	 * <p>
	 * The writeMode argument allows the caller to specify WaitForCommit behavior,
	 * which will not return until the message has been committed to the session
	 * file on disk. The Queued behavior used by default with other overloads of
	 * this method places the message on Loupe's central queue and then returns,
	 * allowing the current thread execution to continue while Loupe processes the
	 * queue on a separate thread.
	 * </p>
	 *
	 * @param exception   An Exception object to attach to this log message.
	 * @param threadInfo the thread info
	 * @param writeMode   Whether to queue-and-return or wait-for-commit.
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void warning(Throwable exception, ThreadInfo threadInfo, LogWriteMode writeMode, String category, String caption,
			String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.WARNING, LogWriteMode.forValue(writeMode),
				THIS_LOG_SYSTEM, category, 1, exception, threadInfo, false, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a detailed Warning message directly to the Loupe log.
	 * 
	 * <p>
	 * This method provides an advanced use of Loupe log messages to include an XML
	 * document (as a string) containing extended details about the message. Also
	 * see the Write() method for when XML details are not needed. This method is
	 * otherwise similar to Write().
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
	 * </p>
	 * 
	 * @param detailsXml  An XML document (as a string) with extended details about
	 *                    the message.
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void warningDetail(String detailsXml, String category, String caption, String description,
			Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.WARNING, THIS_LOG_SYSTEM, category, 1,
				detailsXml, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a detailed Warning message directly to the Loupe log with an attached
	 * Exception.
	 * 
	 * <p>
	 * This method provides an advanced use of Loupe log messages to include an XML
	 * document (as a string) containing extended details about the message.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
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
	 * @param exception   An Exception object to attach to this log message.
	 * @param detailsXml  An XML document (as a string) with extended details about
	 *                    the message.
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void warningDetail(Throwable exception, String detailsXml, String category, String caption,
			String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.WARNING, QUEUED, THIS_LOG_SYSTEM,
				category, 1, exception, false, detailsXml, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a detailed Warning message directly to the Loupe log, specifying Queued
	 * or WaitForCommit behavior.
	 * 
	 * <p>
	 * This method provides an advanced use of Loupe log messages to include an XML
	 * document (as a string) containing extended details about the message.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
	 * </p>
	 * 
	 * @param writeMode   Whether to queue-and-return or wait-for-commit.
	 * @param detailsXml  An XML document (as a string) with extended details about
	 *                    the message.
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void warningDetail(LogWriteMode writeMode, String detailsXml, String category, String caption,
			String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.WARNING, LogWriteMode.forValue(writeMode),
				THIS_LOG_SYSTEM, category, 1, detailsXml, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a detailed Warning message directly to the Loupe log with an optional
	 * attached Exception and specifying Queued or WaitForCommit behavior.
	 * 
	 * <p>
	 * This method provides an advanced use of Loupe log messages to include an XML
	 * document (as a string) containing extended details about the message.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
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
	 * @param exception   An Exception object to attach to this log message.
	 * @param writeMode   Whether to queue-and-return or wait-for-commit.
	 * @param detailsXml  An XML document (as a string) with extended details about
	 *                    the message.
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void warningDetail(Throwable exception, LogWriteMode writeMode, String detailsXml,
			String category, String caption, String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.WARNING, LogWriteMode.forValue(writeMode),
				THIS_LOG_SYSTEM, category, 1, exception, false, detailsXml, caption, description, args);

		logMessage.publishToLog();
	}

	//
	// ERROR
	//

	/**
	 * Write a categorized Error message directly to the Loupe log.
	 * 
	 * <p>
	 * This method provides basic Loupe logging features for typical use. Loupe
	 * supports a separate caption and description in log messages in order to
	 * provide better analysis capability. Log messages can be grouped by their
	 * captions even while their full descriptions differ, so for more useful
	 * matching we don't provide format processing on the caption argument, only on
	 * the description argument.
	 * </p>
	 * <p>
	 * The caption and description arguments tolerate null and empty strings (e.g. a
	 * simple one-line message caption with no further description needed). A null
	 * caption will cause the message caption to be extracted from the description
	 * after format processing (comparable to using the Trace...() methods which
	 * don't take a separate caption argument). A valid string caption argument,
	 * including an empty string, will be taken as the intended caption; an empty
	 * caption string is thus possible, but not recommended.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
	 * </p>
	 * 
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void error(String category, String caption, String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.ERROR, THIS_LOG_SYSTEM, category, 1,
				caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a categorized Error message directly to the Loupe log with an attached
	 * Exception.
	 * 
	 * <p>
	 * This method provides basic Loupe logging features for typical use. Loupe
	 * supports a separate caption and description in log messages in order to
	 * provide better analysis capability. Log messages can be grouped by their
	 * captions even while their full descriptions differ, so for more useful
	 * matching we don't provide format processing on the caption argument, only on
	 * the description argument.
	 * </p>
	 * <p>
	 * The caption and description arguments tolerate null and empty strings (e.g. a
	 * simple one-line message caption with no further description needed). A null
	 * caption will cause the message caption to be extracted from the description
	 * after format processing (comparable to using the Trace...() methods which
	 * don't take a separate caption argument). A valid string caption argument,
	 * including an empty string, will be taken as the intended caption; an empty
	 * caption string is thus possible, but not recommended.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
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
	 * @param exception   An Exception object to attach to this log message.
	 * @param threadInfo the thread info
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void error(Throwable exception, ThreadInfo threadInfo, String category, String caption, String description,
			Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.ERROR, QUEUED, THIS_LOG_SYSTEM, category, 1,
				exception, threadInfo, false, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a categorized Error message directly to the Loupe log with an attached
	 * Exception.
	 * 
	 * <p>
	 * This method provides basic Loupe logging features for typical use. Loupe
	 * supports a separate caption and description in log messages in order to
	 * provide better analysis capability. Log messages can be grouped by their
	 * captions even while their full descriptions differ, so for more useful
	 * matching we don't provide format processing on the caption argument, only on
	 * the description argument.
	 * </p>
	 * <p>
	 * The caption and description arguments tolerate null and empty strings (e.g. a
	 * simple one-line message caption with no further description needed). A null
	 * caption will cause the message caption to be extracted from the description
	 * after format processing (comparable to using the Trace...() methods which
	 * don't take a separate caption argument). A valid string caption argument,
	 * including an empty string, will be taken as the intended caption; an empty
	 * caption string is thus possible, but not recommended.
	 * </p>
	 * <p>
	 * This overload also allows an Exception object to be attached to the log
	 * message. An Exception-typed null (e.g. from a variable of an Exception type)
	 * is allowed for the exception argument, but calls which do not have a possible
	 * Exception to attach should use an overload without an exception argument
	 * rather than pass a direct value of null, to avoid compiler ambiguity over the
	 * type of a simple null.
	 * </p>
	 * <p>
	 * If attributeToException is set to true the log message will be attributed to
	 * the location where the provided exception was thrown from instead of the
	 * caller of this method.
	 * </p>
	 *
	 * @param exception            An Exception object to attach to this log
	 *                             message.
	 * @param threadInfo the thread info
	 * @param attributeToException True to record this log message based on where
	 *                             the exception was thrown, not where this method
	 *                             was called
	 * @param category             The application subsystem or logging category
	 *                             that the log message is associated with, which
	 *                             supports a dot-delimited hierarchy (e.g. the
	 *                             logger name in log4net).
	 * @param caption              A simple single-line message caption. (Will not
	 *                             be processed for formatting.)
	 * @param description          Additional multi-line descriptive message (or may
	 *                             be null) which can be a format string followed by
	 *                             corresponding args.
	 * @param args                 A variable number of arguments referenced by the
	 *                             formatted description string (or no arguments to
	 *                             skip formatting).
	 */
	public static void error(Throwable exception, ThreadInfo threadInfo, boolean attributeToException, String category, String caption,
			String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.ERROR, QUEUED, THIS_LOG_SYSTEM, category, 1,
				exception, threadInfo, attributeToException, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a categorized Error message directly to the Loupe log, specifying
	 * Queued or WaitForCommit behavior.
	 * 
	 * <p>
	 * This method provides basic Loupe logging features for typical use. Loupe
	 * supports a separate caption and description in log messages in order to
	 * provide better analysis capability. Log messages can be grouped by their
	 * captions even while their full descriptions differ, so for more useful
	 * matching we don't provide format processing on the caption argument, only on
	 * the description argument.
	 * </p>
	 * <p>
	 * The caption and description arguments tolerate null and empty strings (e.g. a
	 * simple one-line message caption with no further description needed). A null
	 * caption will cause the message caption to be extracted from the description
	 * after format processing (comparable to using the Trace...() methods which
	 * don't take a separate caption argument). A valid string caption argument,
	 * including an empty string, will be taken as the intended caption; an empty
	 * caption string is thus possible, but not recommended.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
	 * </p>
	 * <p>
	 * The writeMode argument allows the caller to specify WaitForCommit behavior,
	 * which will not return until the message has been committed to the session
	 * file on disk. The Queued behavior used by default with other overloads of
	 * this method places the message on Loupe's central queue and then returns,
	 * allowing the current thread execution to continue while Loupe processes the
	 * queue on a separate thread.
	 * </p>
	 * 
	 * @param writeMode   Whether to queue-and-return or wait-for-commit.
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void error(LogWriteMode writeMode, String category, String caption, String description,
			Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.ERROR, LogWriteMode.forValue(writeMode),
				THIS_LOG_SYSTEM, category, 1, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a categorized Error message directly to the Loupe log with an attached
	 * Exception and specifying Queued or WaitForCommit behavior.
	 * 
	 * <p>
	 * This method provides basic Loupe logging features for typical use. Loupe
	 * supports a separate caption and description in log messages in order to
	 * provide better analysis capability. Log messages can be grouped by their
	 * captions even while their full descriptions differ, so for more useful
	 * matching we don't provide format processing on the caption argument, only on
	 * the description argument.
	 * </p>
	 * <p>
	 * The caption and description arguments tolerate null and empty strings (e.g. a
	 * simple one-line message caption with no further description needed). A null
	 * caption will cause the message caption to be extracted from the description
	 * after format processing (comparable to using the Trace...() methods which
	 * don't take a separate caption argument). A valid string caption argument,
	 * including an empty string, will be taken as the intended caption; an empty
	 * caption string is thus possible, but not recommended.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
	 * </p>
	 * <p>
	 * This overload also allows an Exception object to be attached to the log
	 * message. An Exception-typed null (e.g. from a variable of an Exception type)
	 * is allowed for the exception argument, but calls which do not have a possible
	 * Exception to attach should use an overload without an exception argument
	 * rather than pass a direct value of null, to avoid compiler ambiguity over the
	 * type of a simple null.
	 * </p>
	 * <p>
	 * The writeMode argument allows the caller to specify WaitForCommit behavior,
	 * which will not return until the message has been committed to the session
	 * file on disk. The Queued behavior used by default with other overloads of
	 * this method places the message on Loupe's central queue and then returns,
	 * allowing the current thread execution to continue while Loupe processes the
	 * queue on a separate thread.
	 * </p>
	 *
	 * @param exception   An Exception object to attach to this log message.
	 * @param threadInfo the thread info
	 * @param writeMode   Whether to queue-and-return or wait-for-commit.
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void error(Throwable exception, ThreadInfo threadInfo, LogWriteMode writeMode, String category, String caption,
			String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.ERROR, LogWriteMode.forValue(writeMode),
				THIS_LOG_SYSTEM, category, 1, exception, threadInfo, false, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a categorized Error message directly to the Loupe log with an attached
	 * Exception and specifying Queued or WaitForCommit behavior.
	 * 
	 * <p>
	 * This method provides basic Loupe logging features for typical use. Loupe
	 * supports a separate caption and description in log messages in order to
	 * provide better analysis capability. Log messages can be grouped by their
	 * captions even while their full descriptions differ, so for more useful
	 * matching we don't provide format processing on the caption argument, only on
	 * the description argument.
	 * </p>
	 * <p>
	 * The caption and description arguments tolerate null and empty strings (e.g. a
	 * simple one-line message caption with no further description needed). A null
	 * caption will cause the message caption to be extracted from the description
	 * after format processing (comparable to using the Trace...() methods which
	 * don't take a separate caption argument). A valid string caption argument,
	 * including an empty string, will be taken as the intended caption; an empty
	 * caption string is thus possible, but not recommended.
	 * </p>
	 * <p>
	 * This overload also allows an Exception object to be attached to the log
	 * message. An Exception-typed null (e.g. from a variable of an Exception type)
	 * is allowed for the exception argument, but calls which do not have a possible
	 * Exception to attach should use an overload without an exception argument
	 * rather than pass a direct value of null, to avoid compiler ambiguity over the
	 * type of a simple null.
	 * </p>
	 * <p>
	 * If attributeToException is set to true the log message will be attributed to
	 * the location where the provided exception was thrown from instead of the
	 * caller of this method.
	 * </p>
	 * <p>
	 * The writeMode argument allows the caller to specify WaitForCommit behavior,
	 * which will not return until the message has been committed to the session
	 * file on disk. The Queued behavior used by default with other overloads of
	 * this method places the message on Loupe's central queue and then returns,
	 * allowing the current thread execution to continue while Loupe processes the
	 * queue on a separate thread.
	 * </p>
	 *
	 * @param exception            An Exception object to attach to this log
	 *                             message.
	 * @param threadInfo the thread info
	 * @param attributeToException True to record this log message based on where
	 *                             the exception was thrown, not where this method
	 *                             was called
	 * @param writeMode            Whether to queue-and-return or wait-for-commit.
	 * @param category             The application subsystem or logging category
	 *                             that the log message is associated with, which
	 *                             supports a dot-delimited hierarchy (e.g. the
	 *                             logger name in log4net).
	 * @param caption              A simple single-line message caption. (Will not
	 *                             be processed for formatting.)
	 * @param description          Additional multi-line descriptive message (or may
	 *                             be null) which can be a format string followed by
	 *                             corresponding args.
	 * @param args                 A variable number of arguments referenced by the
	 *                             formatted description string (or no arguments to
	 *                             skip formatting).
	 */
	public static void error(Throwable exception, ThreadInfo threadInfo, boolean attributeToException, LogWriteMode writeMode,
			String category, String caption, String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.ERROR, LogWriteMode.forValue(writeMode),
				THIS_LOG_SYSTEM, category, 1, exception, threadInfo, attributeToException, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a detailed Error message directly to the Loupe log.
	 * 
	 * <p>
	 * This method provides an advanced use of Loupe log messages to include an XML
	 * document (as a string) containing extended details about the message. Also
	 * see the Write() method for when XML details are not needed. This method is
	 * otherwise similar to Write().
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
	 * </p>
	 * 
	 * @param detailsXml  An XML document (as a string) with extended details about
	 *                    the message.
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void errorDetail(String detailsXml, String category, String caption, String description,
			Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.ERROR, THIS_LOG_SYSTEM, category, 1,
				detailsXml, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a detailed Error message directly to the Loupe log with an attached
	 * Exception.
	 * 
	 * <p>
	 * This method provides an advanced use of Loupe log messages to include an XML
	 * document (as a string) containing extended details about the message.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
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
	 * @param exception   An Exception object to attach to this log message.
	 * @param detailsXml  An XML document (as a string) with extended details about
	 *                    the message.
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void errorDetail(Throwable exception, String detailsXml, String category, String caption,
			String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.ERROR, QUEUED, THIS_LOG_SYSTEM, category,
				1, exception, false, detailsXml, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a detailed Error message directly to the Loupe log with an attached
	 * Exception.
	 * 
	 * <p>
	 * This method provides an advanced use of Loupe log messages to include an XML
	 * document (as a string) containing extended details about the message.
	 * </p>
	 * <p>
	 * This overload also allows an Exception object to be attached to the log
	 * message. An Exception-typed null (e.g. from a variable of an Exception type)
	 * is allowed for the exception argument, but calls which do not have a possible
	 * Exception to attach should use an overload without an exception argument
	 * rather than pass a direct value of null, to avoid compiler ambiguity over the
	 * type of a simple null.
	 * </p>
	 * <p>
	 * If attributeToException is set to true the log message will be attributed to
	 * the location where the provided exception was thrown from instead of the
	 * caller of this method.
	 * </p>
	 * 
	 * @param exception            An Exception object to attach to this log
	 *                             message.
	 * @param attributeToException True to record this log message based on where
	 *                             the exception was thrown, not where this method
	 *                             was called
	 * @param detailsXml           An XML document (as a string) with extended
	 *                             details about the message.
	 * @param category             The application subsystem or logging category
	 *                             that the log message is associated with, which
	 *                             supports a dot-delimited hierarchy (e.g. the
	 *                             logger name in log4net).
	 * @param caption              A simple single-line message caption. (Will not
	 *                             be processed for formatting.)
	 * @param description          Additional multi-line descriptive message (or may
	 *                             be null) which can be a format string followed by
	 *                             corresponding args.
	 * @param args                 A variable number of arguments referenced by the
	 *                             formatted description string (or no arguments to
	 *                             skip formatting).
	 */
	public static void errorDetail(Throwable exception, boolean attributeToException, String detailsXml,
			String category, String caption, String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.ERROR, QUEUED, THIS_LOG_SYSTEM, category,
				1, exception, attributeToException, detailsXml, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a detailed Error message directly to the Loupe log, specifying Queued
	 * or WaitForCommit behavior.
	 * 
	 * <p>
	 * This method provides an advanced use of Loupe log messages to include an XML
	 * document (as a string) containing extended details about the message.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
	 * </p>
	 * 
	 * @param writeMode   Whether to queue-and-return or wait-for-commit.
	 * @param detailsXml  An XML document (as a string) with extended details about
	 *                    the message.
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void errorDetail(LogWriteMode writeMode, String detailsXml, String category, String caption,
			String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.ERROR, LogWriteMode.forValue(writeMode),
				THIS_LOG_SYSTEM, category, 1, detailsXml, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a detailed Error message directly to the Loupe log with an optional
	 * attached Exception and specifying Queued or WaitForCommit behavior.
	 * 
	 * <p>
	 * This method provides an advanced use of Loupe log messages to include an XML
	 * document (as a string) containing extended details about the message.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
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
	 * @param exception   An Exception object to attach to this log message.
	 * @param writeMode   Whether to queue-and-return or wait-for-commit.
	 * @param detailsXml  An XML document (as a string) with extended details about
	 *                    the message.
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void errorDetail(Throwable exception, LogWriteMode writeMode, String detailsXml,
			String category, String caption, String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.ERROR, LogWriteMode.forValue(writeMode),
				THIS_LOG_SYSTEM, category, 1, exception, false, detailsXml, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a detailed Error message directly to the Loupe log with an optional
	 * attached Exception and specifying Queued or WaitForCommit behavior.
	 * 
	 * <p>
	 * This method provides an advanced use of Loupe log messages to include an XML
	 * document (as a string) containing extended details about the message.
	 * </p>
	 * <p>
	 * This overload also allows an Exception object to be attached to the log
	 * message. An Exception-typed null (e.g. from a variable of an Exception type)
	 * is allowed for the exception argument, but calls which do not have a possible
	 * Exception to attach should use an overload without an exception argument
	 * rather than pass a direct value of null, to avoid compiler ambiguity over the
	 * type of a simple null.
	 * </p>
	 * <p>
	 * If attributeToException is set to true the log message will be attributed to
	 * the location where the provided exception was thrown from instead of the
	 * caller of this method.
	 * </p>
	 * 
	 * @param exception            An Exception object to attach to this log
	 *                             message.
	 * @param attributeToException True to record this log message based on where
	 *                             the exception was thrown, not where this method
	 *                             was called
	 * @param writeMode            Whether to queue-and-return or wait-for-commit.
	 * @param detailsXml           An XML document (as a string) with extended
	 *                             details about the message.
	 * @param category             The application subsystem or logging category
	 *                             that the log message is associated with, which
	 *                             supports a dot-delimited hierarchy (e.g. the
	 *                             logger name in log4net).
	 * @param caption              A simple single-line message caption. (Will not
	 *                             be processed for formatting.)
	 * @param description          Additional multi-line descriptive message (or may
	 *                             be null) which can be a format string followed by
	 *                             corresponding args.
	 * @param args                 A variable number of arguments referenced by the
	 *                             formatted description string (or no arguments to
	 *                             skip formatting).
	 */
	public static void errorDetail(Throwable exception, boolean attributeToException, LogWriteMode writeMode,
			String detailsXml, String category, String caption, String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.ERROR, LogWriteMode.forValue(writeMode),
				THIS_LOG_SYSTEM, category, 1, exception, attributeToException, detailsXml, caption, description, args);

		logMessage.publishToLog();
	}

	//
	// CRITICAL
	//

	/**
	 * Write a categorized Critical message directly to the Loupe log.
	 * 
	 * <p>
	 * This method provides basic Loupe logging features for typical use. Loupe
	 * supports a separate caption and description in log messages in order to
	 * provide better analysis capability. Log messages can be grouped by their
	 * captions even while their full descriptions differ, so for more useful
	 * matching we don't provide format processing on the caption argument, only on
	 * the description argument.
	 * </p>
	 * <p>
	 * The caption and description arguments tolerate null and empty strings (e.g. a
	 * simple one-line message caption with no further description needed). A null
	 * caption will cause the message caption to be extracted from the description
	 * after format processing (comparable to using the Trace...() methods which
	 * don't take a separate caption argument). A valid string caption argument,
	 * including an empty string, will be taken as the intended caption; an empty
	 * caption string is thus possible, but not recommended.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
	 * </p>
	 * 
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void critical(String category, String caption, String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.CRITICAL, THIS_LOG_SYSTEM, category, 1,
				caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a categorized Critical message directly to the Loupe log with an
	 * attached Exception.
	 * 
	 * <p>
	 * This method provides basic Loupe logging features for typical use. Loupe
	 * supports a separate caption and description in log messages in order to
	 * provide better analysis capability. Log messages can be grouped by their
	 * captions even while their full descriptions differ, so for more useful
	 * matching we don't provide format processing on the caption argument, only on
	 * the description argument.
	 * </p>
	 * <p>
	 * The caption and description arguments tolerate null and empty strings (e.g. a
	 * simple one-line message caption with no further description needed). A null
	 * caption will cause the message caption to be extracted from the description
	 * after format processing (comparable to using the Trace...() methods which
	 * don't take a separate caption argument). A valid string caption argument,
	 * including an empty string, will be taken as the intended caption; an empty
	 * caption string is thus possible, but not recommended.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
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
	 * @param exception   An Exception object to attach to this log message.
	 * @param threadInfo the thread info
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void critical(Throwable exception, ThreadInfo threadInfo, String category, String caption, String description,
			Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.CRITICAL, QUEUED, THIS_LOG_SYSTEM, category,
				1, exception, threadInfo, false, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a categorized Critical message directly to the Loupe log with an
	 * attached Exception.
	 * 
	 * <p>
	 * This method provides basic Loupe logging features for typical use. Loupe
	 * supports a separate caption and description in log messages in order to
	 * provide better analysis capability. Log messages can be grouped by their
	 * captions even while their full descriptions differ, so for more useful
	 * matching we don't provide format processing on the caption argument, only on
	 * the description argument.
	 * </p>
	 * <p>
	 * The caption and description arguments tolerate null and empty strings (e.g. a
	 * simple one-line message caption with no further description needed). A null
	 * caption will cause the message caption to be extracted from the description
	 * after format processing (comparable to using the Trace...() methods which
	 * don't take a separate caption argument). A valid string caption argument,
	 * including an empty string, will be taken as the intended caption; an empty
	 * caption string is thus possible, but not recommended.
	 * </p>
	 * <p>
	 * This overload also allows an Exception object to be attached to the log
	 * message. An Exception-typed null (e.g. from a variable of an Exception type)
	 * is allowed for the exception argument, but calls which do not have a possible
	 * Exception to attach should use an overload without an exception argument
	 * rather than pass a direct value of null, to avoid compiler ambiguity over the
	 * type of a simple null.
	 * </p>
	 * <p>
	 * If attributeToException is set to true the log message will be attributed to
	 * the location where the provided exception was thrown from instead of the
	 * caller of this method.
	 * </p>
	 *
	 * @param exception            An Exception object to attach to this log
	 *                             message.
	 * @param threadInfo the thread info
	 * @param attributeToException True to record this log message based on where
	 *                             the exception was thrown, not where this method
	 *                             was called
	 * @param category             The application subsystem or logging category
	 *                             that the log message is associated with, which
	 *                             supports a dot-delimited hierarchy (e.g. the
	 *                             logger name in log4net).
	 * @param caption              A simple single-line message caption. (Will not
	 *                             be processed for formatting.)
	 * @param description          Additional multi-line descriptive message (or may
	 *                             be null) which can be a format string followed by
	 *                             corresponding args.
	 * @param args                 A variable number of arguments referenced by the
	 *                             formatted description string (or no arguments to
	 *                             skip formatting).
	 */
	public static void critical(Throwable exception, ThreadInfo threadInfo, boolean attributeToException, String category,
			String caption, String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.CRITICAL, QUEUED, THIS_LOG_SYSTEM, category,
				1, exception, threadInfo, attributeToException, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a categorized Critical message directly to the Loupe log, specifying
	 * Queued or WaitForCommit behavior.
	 * 
	 * <p>
	 * This method provides basic Loupe logging features for typical use. Loupe
	 * supports a separate caption and description in log messages in order to
	 * provide better analysis capability. Log messages can be grouped by their
	 * captions even while their full descriptions differ, so for more useful
	 * matching we don't provide format processing on the caption argument, only on
	 * the description argument.
	 * </p>
	 * <p>
	 * The caption and description arguments tolerate null and empty strings (e.g. a
	 * simple one-line message caption with no further description needed). A null
	 * caption will cause the message caption to be extracted from the description
	 * after format processing (comparable to using the Trace...() methods which
	 * don't take a separate caption argument). A valid string caption argument,
	 * including an empty string, will be taken as the intended caption; an empty
	 * caption string is thus possible, but not recommended.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
	 * </p>
	 * <p>
	 * The writeMode argument allows the caller to specify WaitForCommit behavior,
	 * which will not return until the message has been committed to the session
	 * file on disk. The Queued behavior used by default with other overloads of
	 * this method places the message on Loupe's central queue and then returns,
	 * allowing the current thread execution to continue while Loupe processes the
	 * queue on a separate thread.
	 * </p>
	 * 
	 * @param writeMode   Whether to queue-and-return or wait-for-commit.
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void critical(LogWriteMode writeMode, String category, String caption, String description,
			Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.CRITICAL, LogWriteMode.forValue(writeMode),
				THIS_LOG_SYSTEM, category, 1, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a categorized Critical message directly to the Loupe log with an
	 * attached Exception and specifying Queued or WaitForCommit behavior.
	 * 
	 * <p>
	 * This method provides basic Loupe logging features for typical use. Loupe
	 * supports a separate caption and description in log messages in order to
	 * provide better analysis capability. Log messages can be grouped by their
	 * captions even while their full descriptions differ, so for more useful
	 * matching we don't provide format processing on the caption argument, only on
	 * the description argument.
	 * </p>
	 * <p>
	 * The caption and description arguments tolerate null and empty strings (e.g. a
	 * simple one-line message caption with no further description needed). A null
	 * caption will cause the message caption to be extracted from the description
	 * after format processing (comparable to using the Trace...() methods which
	 * don't take a separate caption argument). A valid string caption argument,
	 * including an empty string, will be taken as the intended caption; an empty
	 * caption string is thus possible, but not recommended.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
	 * </p>
	 * <p>
	 * This overload also allows an Exception object to be attached to the log
	 * message. An Exception-typed null (e.g. from a variable of an Exception type)
	 * is allowed for the exception argument, but calls which do not have a possible
	 * Exception to attach should use an overload without an exception argument
	 * rather than pass a direct value of null, to avoid compiler ambiguity over the
	 * type of a simple null.
	 * </p>
	 * <p>
	 * The writeMode argument allows the caller to specify WaitForCommit behavior,
	 * which will not return until the message has been committed to the session
	 * file on disk. The Queued behavior used by default with other overloads of
	 * this method places the message on Loupe's central queue and then returns,
	 * allowing the current thread execution to continue while Loupe processes the
	 * queue on a separate thread.
	 * </p>
	 *
	 * @param exception   An Exception object to attach to this log message.
	 * @param threadInfo the thread info
	 * @param writeMode   Whether to queue-and-return or wait-for-commit.
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void critical(Throwable exception, ThreadInfo threadInfo, LogWriteMode writeMode, String category, String caption,
			String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.CRITICAL,
				LogWriteMode.forValue(writeMode.getValue()), THIS_LOG_SYSTEM, category, 1, exception,
				threadInfo, false, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a categorized Critical message directly to the Loupe log with an
	 * attached Exception and specifying Queued or WaitForCommit behavior.
	 * 
	 * <p>
	 * This method provides basic Loupe logging features for typical use. Loupe
	 * supports a separate caption and description in log messages in order to
	 * provide better analysis capability. Log messages can be grouped by their
	 * captions even while their full descriptions differ, so for more useful
	 * matching we don't provide format processing on the caption argument, only on
	 * the description argument.
	 * </p>
	 * <p>
	 * The caption and description arguments tolerate null and empty strings (e.g. a
	 * simple one-line message caption with no further description needed). A null
	 * caption will cause the message caption to be extracted from the description
	 * after format processing (comparable to using the Trace...() methods which
	 * don't take a separate caption argument). A valid string caption argument,
	 * including an empty string, will be taken as the intended caption; an empty
	 * caption string is thus possible, but not recommended.
	 * </p>
	 * <p>
	 * This overload also allows an Exception object to be attached to the log
	 * message. An Exception-typed null (e.g. from a variable of an Exception type)
	 * is allowed for the exception argument, but calls which do not have a possible
	 * Exception to attach should use an overload without an exception argument
	 * rather than pass a direct value of null, to avoid compiler ambiguity over the
	 * type of a simple null.
	 * </p>
	 * <p>
	 * If attributeToException is set to true the log message will be attributed to
	 * the location where the provided exception was thrown from instead of the
	 * caller of this method.
	 * </p>
	 * <p>
	 * The writeMode argument allows the caller to specify WaitForCommit behavior,
	 * which will not return until the message has been committed to the session
	 * file on disk. The Queued behavior used by default with other overloads of
	 * this method places the message on Loupe's central queue and then returns,
	 * allowing the current thread execution to continue while Loupe processes the
	 * queue on a separate thread.
	 * </p>
	 *
	 * @param exception            An Exception object to attach to this log
	 *                             message.
	 * @param threadInfo the thread info
	 * @param attributeToException True to record this log message based on where
	 *                             the exception was thrown, not where this method
	 *                             was called
	 * @param writeMode            Whether to queue-and-return or wait-for-commit.
	 * @param category             The application subsystem or logging category
	 *                             that the log message is associated with, which
	 *                             supports a dot-delimited hierarchy (e.g. the
	 *                             logger name in log4net).
	 * @param caption              A simple single-line message caption. (Will not
	 *                             be processed for formatting.)
	 * @param description          Additional multi-line descriptive message (or may
	 *                             be null) which can be a format string followed by
	 *                             corresponding args.
	 * @param args                 A variable number of arguments referenced by the
	 *                             formatted description string (or no arguments to
	 *                             skip formatting).
	 */
	public static void critical(Throwable exception, ThreadInfo threadInfo, boolean attributeToException, LogWriteMode writeMode,
			String category, String caption, String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.CRITICAL,
				LogWriteMode.forValue(writeMode.getValue()), THIS_LOG_SYSTEM, category, 1, exception,
				threadInfo, attributeToException, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a detailed Critical message directly to the Loupe log.
	 * 
	 * <p>
	 * This method provides an advanced use of Loupe log messages to include an XML
	 * document (as a string) containing extended details about the message. Also
	 * see the Write() method for when XML details are not needed. This method is
	 * otherwise similar to Write().
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
	 * </p>
	 * 
	 * @param detailsXml  An XML document (as a string) with extended details about
	 *                    the message.
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void criticalDetail(String detailsXml, String category, String caption, String description,
			Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.CRITICAL, THIS_LOG_SYSTEM, category, 1,
				detailsXml, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a detailed Critical message directly to the Loupe log with an attached
	 * Exception.
	 * 
	 * <p>
	 * This method provides an advanced use of Loupe log messages to include an XML
	 * document (as a string) containing extended details about the message.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
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
	 * @param exception   An Exception object to attach to this log message.
	 * @param detailsXml  An XML document (as a string) with extended details about
	 *                    the message.
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void criticalDetail(Throwable exception, String detailsXml, String category, String caption,
			String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.CRITICAL, QUEUED, THIS_LOG_SYSTEM,
				category, 1, exception, false, detailsXml, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a detailed Critical message directly to the Loupe log with an attached
	 * Exception.
	 * 
	 * <p>
	 * This method provides an advanced use of Loupe log messages to include an XML
	 * document (as a string) containing extended details about the message.
	 * </p>
	 * <p>
	 * This overload also allows an Exception object to be attached to the log
	 * message. An Exception-typed null (e.g. from a variable of an Exception type)
	 * is allowed for the exception argument, but calls which do not have a possible
	 * Exception to attach should use an overload without an exception argument
	 * rather than pass a direct value of null, to avoid compiler ambiguity over the
	 * type of a simple null.
	 * </p>
	 * <p>
	 * If attributeToException is set to true the log message will be attributed to
	 * the location where the provided exception was thrown from instead of the
	 * caller of this method.
	 * </p>
	 * 
	 * @param exception            An Exception object to attach to this log
	 *                             message.
	 * @param attributeToException True to record this log message based on where
	 *                             the exception was thrown, not where this method
	 *                             was called
	 * @param detailsXml           An XML document (as a string) with extended
	 *                             details about the message.
	 * @param category             The application subsystem or logging category
	 *                             that the log message is associated with, which
	 *                             supports a dot-delimited hierarchy (e.g. the
	 *                             logger name in log4net).
	 * @param caption              A simple single-line message caption. (Will not
	 *                             be processed for formatting.)
	 * @param description          Additional multi-line descriptive message (or may
	 *                             be null) which can be a format string followed by
	 *                             corresponding args.
	 * @param args                 A variable number of arguments referenced by the
	 *                             formatted description string (or no arguments to
	 *                             skip formatting).
	 */
	public static void criticalDetail(Throwable exception, boolean attributeToException, String detailsXml,
			String category, String caption, String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.CRITICAL, QUEUED, THIS_LOG_SYSTEM,
				category, 1, exception, attributeToException, detailsXml, caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a detailed Critical message directly to the Loupe log, specifying
	 * Queued or WaitForCommit behavior.
	 * 
	 * <p>
	 * This method provides an advanced use of Loupe log messages to include an XML
	 * document (as a string) containing extended details about the message.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
	 * </p>
	 * 
	 * @param writeMode   Whether to queue-and-return or wait-for-commit.
	 * @param detailsXml  An XML document (as a string) with extended details about
	 *                    the message.
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void criticalDetail(LogWriteMode writeMode, String detailsXml, String category, String caption,
			String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.CRITICAL,
				LogWriteMode.forValue(writeMode.getValue()), THIS_LOG_SYSTEM, category, 1, detailsXml, caption,
				description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a detailed Critical message directly to the Loupe log with an optional
	 * attached Exception and specifying Queued or WaitForCommit behavior.
	 * 
	 * <p>
	 * This method provides an advanced use of Loupe log messages to include an XML
	 * document (as a string) containing extended details about the message.
	 * </p>
	 * <p>
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the Write() method in order to attribute the log
	 * message to their own outer callers.
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
	 * @param exception   An Exception object to attach to this log message.
	 * @param writeMode   Whether to queue-and-return or wait-for-commit.
	 * @param detailsXml  An XML document (as a string) with extended details about
	 *                    the message.
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy (e.g. the logger name in log4net).
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void criticalDetail(Throwable exception, LogWriteMode writeMode, String detailsXml,
			String category, String caption, String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.CRITICAL,
				LogWriteMode.forValue(writeMode.getValue()), THIS_LOG_SYSTEM, category, 1, exception, false, detailsXml,
				caption, description, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a detailed Critical message directly to the Loupe log with an optional
	 * attached Exception and specifying Queued or WaitForCommit behavior.
	 * 
	 * <p>
	 * This method provides an advanced use of Loupe log messages to include an XML
	 * document (as a string) containing extended details about the message.
	 * </p>
	 * <p>
	 * This overload also allows an Exception object to be attached to the log
	 * message. An Exception-typed null (e.g. from a variable of an Exception type)
	 * is allowed for the exception argument, but calls which do not have a possible
	 * Exception to attach should use an overload without an exception argument
	 * rather than pass a direct value of null, to avoid compiler ambiguity over the
	 * type of a simple null.
	 * </p>
	 * <p>
	 * If attributeToException is set to true the log message will be attributed to
	 * the location where the provided exception was thrown from instead of the
	 * caller of this method.
	 * </p>
	 * 
	 * @param exception            An Exception object to attach to this log
	 *                             message.
	 * @param attributeToException True to record this log message based on where
	 *                             the exception was thrown, not where this method
	 *                             was called
	 * @param writeMode            Whether to queue-and-return or wait-for-commit.
	 * @param detailsXml           An XML document (as a string) with extended
	 *                             details about the message.
	 * @param category             The application subsystem or logging category
	 *                             that the log message is associated with, which
	 *                             supports a dot-delimited hierarchy (e.g. the
	 *                             logger name in log4net).
	 * @param caption              A simple single-line message caption. (Will not
	 *                             be processed for formatting.)
	 * @param description          Additional multi-line descriptive message (or may
	 *                             be null) which can be a format string followed by
	 *                             corresponding args.
	 * @param args                 A variable number of arguments referenced by the
	 *                             formatted description string (or no arguments to
	 *                             skip formatting).
	 */
	public static void criticalDetail(Throwable exception, boolean attributeToException, LogWriteMode writeMode,
			String detailsXml, String category, String caption, String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.CRITICAL,
				LogWriteMode.forValue(writeMode.getValue()), THIS_LOG_SYSTEM, category, 1, exception,
				attributeToException, detailsXml, caption, description, args);

		logMessage.publishToLog();
	}
	
//////////////////////////////////////////////////new stuff

	/**
 * Critical.
 *
 * @param element the element
 * @param threadInfo the thread info
 * @param logSystem the log system
 * @param category the category
 * @param caption the caption
 */
public static void critical(StackTraceElement element, ThreadInfo threadInfo, String logSystem, String category,
			String caption) {
		critical(null, element, threadInfo, logSystem, category, caption);
	}

	/**
	 * Critical.
	 *
	 * @param element the element
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void critical(StackTraceElement element, ThreadInfo threadInfo, String logSystem, String category,
			String caption, String description, Object... args) {
		critical(null, element, threadInfo, logSystem, category, caption, description, args);
	}

	/**
	 * Critical.
	 *
	 * @param throwable the throwable
	 * @param element the element
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 */
	public static void critical(Throwable throwable, StackTraceElement element, ThreadInfo threadInfo,
			String logSystem, String category, String caption) {
		critical(throwable, element, threadInfo, logSystem, category, caption, null);
	}
	
	/**
	 * Critical.
	 *
	 * @param skipFrames the skip frames
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void critical(int skipFrames, ThreadInfo threadInfo, String logSystem,
			String category, String caption, String description, Object... args) {
		critical(skipFrames + 1, null, threadInfo, logSystem, category, caption, description, args);
	}
	
	/**
	 * Critical.
	 *
	 * @param skipFrames the skip frames
	 * @param exclusions the exclusions
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void critical(int skipFrames, Set<String> exclusions, ThreadInfo threadInfo, String logSystem,
			String category, String caption, String description, Object... args) {
		critical(null, skipFrames + 1, exclusions, threadInfo, logSystem, category, caption, description, args);
	}
	
	/**
	 * Critical.
	 *
	 * @param throwable the throwable
	 * @param skipFrames the skip frames
	 * @param exclusions the exclusions
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void critical(Throwable throwable, int skipFrames, Set<String> exclusions, ThreadInfo threadInfo, String logSystem,
			String category, String caption, String description, Object... args) {
		critical(throwable, null, skipFrames + 1, exclusions, threadInfo, logSystem, category, caption, description, args);
	}

	/**
	 * Critical.
	 *
	 * @param throwable the throwable
	 * @param element the element
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void critical(Throwable throwable, StackTraceElement element, ThreadInfo threadInfo, String logSystem, 
			String category, String caption, String description, Object... args) {
		critical(throwable, element, 1, null, threadInfo, logSystem, category, caption, description, args);
	}
	
	/**
	 * Critical.
	 *
	 * @param throwable the throwable
	 * @param element the element
	 * @param skipFrames the skip frames
	 * @param exclusions the exclusions
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 */
	public static void critical(Throwable throwable, StackTraceElement element, int skipFrames, Set<String> exclusions, 
			ThreadInfo threadInfo, String logSystem, String category, String caption) {
		critical(throwable, element, skipFrames + 1, exclusions, threadInfo, logSystem, category, caption, null);
	}
	
	/**
	 * Critical.
	 *
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
	public static void critical(Throwable throwable, StackTraceElement element, int skipFrames, Set<String> exclusions, 
			ThreadInfo threadInfo, String logSystem, String category, String caption, String description, Object... args) {
		write(LogMessageSeverity.CRITICAL, throwable, element, skipFrames + 1, exclusions, threadInfo, logSystem, category, caption, description, args);
	}
	
	/**
	 * Error.
	 *
	 * @param element the element
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 */
	public static void error(StackTraceElement element, ThreadInfo threadInfo, String logSystem, String category,
			String caption) {
		error(null, element, threadInfo, logSystem, category, caption);
	}

	/**
	 * Error.
	 *
	 * @param element the element
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void error(StackTraceElement element, ThreadInfo threadInfo, String logSystem, String category,
			String caption, String description, Object... args) {
		error(null, element, threadInfo, logSystem, category, caption, description, args);
	}

	/**
	 * Error.
	 *
	 * @param throwable the throwable
	 * @param element the element
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 */
	public static void error(Throwable throwable, StackTraceElement element, ThreadInfo threadInfo,
			String logSystem, String category, String caption) {
		error(throwable, element, threadInfo, logSystem, category, caption, null);
	}
	
	/**
	 * Error.
	 *
	 * @param skipFrames the skip frames
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void error(int skipFrames, ThreadInfo threadInfo, String logSystem,
			String category, String caption, String description, Object... args) {
		error(skipFrames + 1, null, threadInfo, logSystem, category, caption, description, args);
	}
	
	/**
	 * Error.
	 *
	 * @param skipFrames the skip frames
	 * @param exclusions the exclusions
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void error(int skipFrames, Set<String> exclusions, ThreadInfo threadInfo, String logSystem,
			String category, String caption, String description, Object... args) {
		error(null, skipFrames + 1, exclusions, threadInfo, logSystem, category, caption, description, args);
	}

	/**
	 * Error.
	 *
	 * @param throwable the throwable
	 * @param skipFrames the skip frames
	 * @param exclusions the exclusions
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 */
	public static void error(Throwable throwable, int skipFrames, Set<String> exclusions, ThreadInfo threadInfo, String logSystem,
			String category, String caption) {
		error(throwable, null, skipFrames + 1, exclusions, threadInfo, logSystem, category, caption, null);
	}
	
	/**
	 * Error.
	 *
	 * @param throwable the throwable
	 * @param skipFrames the skip frames
	 * @param exclusions the exclusions
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void error(Throwable throwable, int skipFrames, Set<String> exclusions, ThreadInfo threadInfo, String logSystem,
			String category, String caption, String description, Object... args) {
		error(throwable, null, skipFrames + 1, exclusions, threadInfo, logSystem, category, caption, description, args);
	}

	/**
	 * Error.
	 *
	 * @param throwable the throwable
	 * @param element the element
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void error(Throwable throwable, StackTraceElement element, ThreadInfo threadInfo, String logSystem, 
			String category, String caption, String description, Object... args) {
		error(throwable, element, 1, null, threadInfo, logSystem, category, caption, description, args);
	}
	
	/**
	 * Error.
	 *
	 * @param throwable the throwable
	 * @param element the element
	 * @param skipFrames the skip frames
	 * @param exclusions the exclusions
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 */
	public static void error(Throwable throwable, StackTraceElement element, int skipFrames, Set<String> exclusions, 
			ThreadInfo threadInfo, String logSystem, String category, String caption) {
		error(throwable, element, skipFrames + 1, exclusions, threadInfo, logSystem, category, caption, null);
	}
	
	/**
	 * Error.
	 *
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
	public static void error(Throwable throwable, StackTraceElement element, int skipFrames, Set<String> exclusions, 
			ThreadInfo threadInfo, String logSystem, String category, String caption, String description, Object... args) {
		write(LogMessageSeverity.ERROR, throwable, element, skipFrames + 1, exclusions, threadInfo, logSystem, category, caption, description, args);
	}
	
	/**
	 * Warn.
	 *
	 * @param category the category
	 * @param caption the caption
	 */
	public static void warn(String category, String caption) {
		warn(null, null, LogSystems.LOUPE, category, caption);
	}
	
	/**
	 * Warn.
	 *
	 * @param element the element
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 */
	public static void warn(StackTraceElement element, ThreadInfo threadInfo, String logSystem, String category,
			String caption) {
		warn(null, element, threadInfo, logSystem, category, caption);
	}

	/**
	 * Warn.
	 *
	 * @param element the element
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void warn(StackTraceElement element, ThreadInfo threadInfo, String logSystem, String category,
			String caption, String description, Object... args) {
		warn(null, element, threadInfo, logSystem, category, caption, description, args);
	}

	/**
	 * Warn.
	 *
	 * @param throwable the throwable
	 * @param element the element
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 */
	public static void warn(Throwable throwable, StackTraceElement element, ThreadInfo threadInfo,
			String logSystem, String category, String caption) {
		warn(throwable, element, threadInfo, logSystem, category, caption, null);
	}
	
	/**
	 * Warn.
	 *
	 * @param skipFrames the skip frames
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void warn(int skipFrames, ThreadInfo threadInfo, String logSystem,
			String category, String caption, String description, Object... args) {
		warn(skipFrames + 1, null, threadInfo, logSystem, category, caption, description, args);
	}
	
	/**
	 * Warn.
	 *
	 * @param skipFrames the skip frames
	 * @param exclusions the exclusions
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void warn(int skipFrames, Set<String> exclusions, ThreadInfo threadInfo, String logSystem,
			String category, String caption, String description, Object... args) {
		warn(null, skipFrames + 1, exclusions, threadInfo, logSystem, category, caption, description, args);
	}
	
	/**
	 * Warn.
	 *
	 * @param throwable the throwable
	 * @param skipFrames the skip frames
	 * @param exclusions the exclusions
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void warn(Throwable throwable, int skipFrames, Set<String> exclusions, ThreadInfo threadInfo, String logSystem,
			String category, String caption, String description, Object... args) {
		warn(throwable, null, skipFrames + 1, exclusions, threadInfo, logSystem, category, caption, description, args);
	}

	/**
	 * Warn.
	 *
	 * @param throwable the throwable
	 * @param element the element
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void warn(Throwable throwable, StackTraceElement element, ThreadInfo threadInfo, String logSystem, 
			String category, String caption, String description, Object... args) {
		warn(throwable, element, 1, null, threadInfo, logSystem, category, caption, description, args);
	}
	
	/**
	 * Warn.
	 *
	 * @param throwable the throwable
	 * @param element the element
	 * @param skipFrames the skip frames
	 * @param exclusions the exclusions
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 */
	public static void warn(Throwable throwable, StackTraceElement element, int skipFrames, Set<String> exclusions, 
			ThreadInfo threadInfo, String logSystem, String category, String caption) {
		warn(throwable, element, skipFrames + 1, exclusions, threadInfo, logSystem, category, caption, null);
	}
	
	/**
	 * Warn.
	 *
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
	public static void warn(Throwable throwable, StackTraceElement element, int skipFrames, Set<String> exclusions, 
			ThreadInfo threadInfo, String logSystem, String category, String caption, String description, Object... args) {
		write(LogMessageSeverity.WARNING, throwable, element, skipFrames + 1, exclusions, threadInfo, logSystem, category, caption, description, args);
	}

	/**
	 * Information.
	 *
	 * @param category the category
	 * @param caption the caption
	 */
	public static void information(String category, String caption) {
		information(null, null, LogSystems.LOUPE, category, caption);
	}
	
	/**
	 * Information.
	 *
	 * @param element the element
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 */
	public static void information(StackTraceElement element, ThreadInfo threadInfo, String logSystem, String category,
			String caption) {
		information(null, element, threadInfo, logSystem, category, caption);
	}

	/**
	 * Information.
	 *
	 * @param element the element
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void information(StackTraceElement element, ThreadInfo threadInfo, String logSystem, String category,
			String caption, String description, Object... args) {
		information(null, element, threadInfo, logSystem, category, caption, description, args);
	}

	/**
	 * Information.
	 *
	 * @param throwable the throwable
	 * @param element the element
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 */
	public static void information(Throwable throwable, StackTraceElement element, ThreadInfo threadInfo,
			String logSystem, String category, String caption) {
		information(throwable, element, threadInfo, logSystem, category, caption, null);
	}
	
	/**
	 * Information.
	 *
	 * @param skipFrames the skip frames
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void information(int skipFrames, ThreadInfo threadInfo, String logSystem,
			String category, String caption, String description, Object... args) {
		information(skipFrames + 1, null, threadInfo, logSystem, category, caption, description, args);
	}
	
	/**
	 * Information.
	 *
	 * @param skipFrames the skip frames
	 * @param exclusions the exclusions
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void information(int skipFrames, Set<String> exclusions, ThreadInfo threadInfo, String logSystem,
			String category, String caption, String description, Object... args) {
		information(null, skipFrames + 1, exclusions, threadInfo, logSystem, category, caption, description, args);
	}
	
	/**
	 * Information.
	 *
	 * @param throwable the throwable
	 * @param skipFrames the skip frames
	 * @param exclusions the exclusions
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void information(Throwable throwable, int skipFrames, Set<String> exclusions, ThreadInfo threadInfo, String logSystem,
			String category, String caption, String description, Object... args) {
		information(throwable, null, skipFrames + 1, exclusions, threadInfo, logSystem, category, caption, description, args);
	}

	/**
	 * Information.
	 *
	 * @param throwable the throwable
	 * @param element the element
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void information(Throwable throwable, StackTraceElement element, ThreadInfo threadInfo, String logSystem, 
			String category, String caption, String description, Object... args) {
		information(throwable, element, 1, null, threadInfo, logSystem, category, caption, description, args);
	}
	
	/**
	 * Information.
	 *
	 * @param throwable the throwable
	 * @param element the element
	 * @param skipFrames the skip frames
	 * @param exclusions the exclusions
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 */
	public static void information(Throwable throwable, StackTraceElement element, int skipFrames, Set<String> exclusions, 
			ThreadInfo threadInfo, String logSystem, String category, String caption) {
		information(throwable, element, skipFrames + 1, exclusions, threadInfo, logSystem, category, caption, null);
	}
	
	/**
	 * Information.
	 *
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
	public static void information(Throwable throwable, StackTraceElement element, int skipFrames, Set<String> exclusions, 
			ThreadInfo threadInfo, String logSystem, String category, String caption, String description, Object... args) {
		write(LogMessageSeverity.INFORMATION, throwable, element, skipFrames + 1, exclusions, threadInfo, logSystem, category, caption, description, args);
	}
	
	/**
	 * Verbose.
	 *
	 * @param category the category
	 * @param caption the caption
	 */
	public static void verbose(String category, String caption) {
		verbose(null, null, LogSystems.LOUPE, category, caption);
	}

	/**
	 * Verbose.
	 *
	 * @param element the element
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 */
	public static void verbose(StackTraceElement element, ThreadInfo threadInfo, String logSystem, String category,
			String caption) {
		verbose(null, element, threadInfo, logSystem, category, caption);
	}

	/**
	 * Verbose.
	 *
	 * @param element the element
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void verbose(StackTraceElement element, ThreadInfo threadInfo, String logSystem, String category,
			String caption, String description, Object... args) {
		verbose(null, element, threadInfo, logSystem, category, caption, description, args);
	}

	/**
	 * Verbose.
	 *
	 * @param throwable the throwable
	 * @param element the element
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 */
	public static void verbose(Throwable throwable, StackTraceElement element, ThreadInfo threadInfo, String logSystem,
			String category, String caption) {
		verbose(throwable, element, threadInfo, logSystem, category, caption, null);
	}

	/**
	 * Verbose.
	 *
	 * @param skipFrames the skip frames
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void verbose(int skipFrames, ThreadInfo threadInfo, String logSystem,
			String category, String caption, String description, Object... args) {
		verbose(skipFrames + 1, null, threadInfo, logSystem, category, caption, description, args);
	}
	
	/**
	 * Verbose.
	 *
	 * @param skipFrames the skip frames
	 * @param exclusions the exclusions
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void verbose(int skipFrames, Set<String> exclusions, ThreadInfo threadInfo, String logSystem,
			String category, String caption, String description, Object... args) {
		verbose(null, skipFrames + 1, exclusions, threadInfo, logSystem, category, caption, description, args);
	}
	
	/**
	 * Verbose.
	 *
	 * @param throwable the throwable
	 * @param skipFrames the skip frames
	 * @param exclusions the exclusions
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void verbose(Throwable throwable, int skipFrames, Set<String> exclusions, ThreadInfo threadInfo, String logSystem,
			String category, String caption, String description, Object... args) {
		verbose(throwable, null, skipFrames + 1, exclusions, threadInfo, logSystem, category, caption, description, args);
	}

	/**
	 * Verbose.
	 *
	 * @param throwable the throwable
	 * @param element the element
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void verbose(Throwable throwable, StackTraceElement element, ThreadInfo threadInfo, String logSystem, 
			String category, String caption, String description, Object... args) {
		verbose(throwable, element, 1, null, threadInfo, logSystem, category, caption, description, args);
	}
	
	/**
	 * Verbose.
	 *
	 * @param throwable the throwable
	 * @param element the element
	 * @param skipFrames the skip frames
	 * @param exclusions the exclusions
	 * @param threadInfo the thread info
	 * @param logSystem the log system
	 * @param category the category
	 * @param caption the caption
	 */
	public static void verbose(Throwable throwable, StackTraceElement element, int skipFrames, Set<String> exclusions, 
			ThreadInfo threadInfo, String logSystem, String category, String caption) {
		verbose(throwable, element, skipFrames + 1, exclusions, threadInfo, logSystem, category, caption, null);
	}
	
	/**
	 * Verbose.
	 *
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
	public static void verbose(Throwable throwable, StackTraceElement element, int skipFrames, Set<String> exclusions, 
			ThreadInfo threadInfo, String logSystem, String category, String caption, String description, Object... args) {
		write(LogMessageSeverity.VERBOSE, throwable, element, skipFrames + 1, exclusions, threadInfo, logSystem, category, caption, description, args);
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
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage;
		if (element != null) {
			logMessage = new LogMessage(severity, logSystem, category, element,
					throwable, threadInfo, false, caption, description, args);
		} else {
			logMessage = new LogMessage(severity, logSystem, category, skipFrames + 1,
					throwable, exclusions, threadInfo, false, caption, description, args);
		}

		logMessage.publishToLog();
	}

/////////////////////////////////////////////////////////////

	/**
	 * Write a complete log message directly to the Loupe log from a wrapper method
	 * or bridging logic, attributing the source of the message farther up the
	 * call-stack.
	 * 
	 * <p>
	 * This method provides an advanced use of Loupe log messages for use in wrapper
	 * methods and for bridging simple logging systems into Loupe. Also see
	 * Verbose
	 * and VerboseDetail
	 * and their other overloads and related methods for simpler usage of XML
	 * details when the other advanced hooks are not needed.
	 * </p>
	 * <p>
	 * This overload of Write() is provided as an API hook for simple wrapping
	 * methods which need to attribute a log message to their own outer callers
	 * rather than to the direct caller of this method. Passing a skipFrames of 0
	 * would designate the caller of this method as the originator; a skipFrames of
	 * 1 would designate the caller of the caller of this method as the originator,
	 * and so on. It will then extract information about the originator
	 * automatically based on the indicated stack frame. Bridge logic adapting from
	 * a logging system which already determines and provides information about the
	 * originator (such as log4net) into Loupe should use the other overload of
	 * Write,
	 * passing a customized IMessageSourceProvider.
	 * </p>
	 * <p>
	 * This method also requires explicitly selecting the LogWriteMode between
	 * Queued (the normal default, for optimal performance) and WaitForCommit (to
	 * help ensure critical information makes it to disk, e.g. before exiting the
	 * application upon return from this call). See the
	 * LogWriteMode enum for more information.
	 * </p>
	 * <p>
	 * This method also allows an optional Exception object to be attached to the
	 * log message (null for none). It can also include an optional XML document (as
	 * a string, or null for none) containing extended details about the message.
	 * </p>
	 * 
	 * @param severity    The log message severity.
	 * @param logSystem   The name of the originating log system (e.g. "Log4Net").
	 * @param skipFrames  The number of stack frames to skip back over to determine
	 *                    the original caller. (0 means the immediate caller of this
	 *                    method; 1 means their immediate caller, and so on.)
	 * @param exception   An Exception object to attach to this log message.
	 * @param writeMode   Whether to queue-and-return or wait-for-commit.
	 * @param detailsXml  An XML document (as a string) with extended details about
	 *                    the message. (May be null.)
	 * @param category    The application subsystem or logging category that the log
	 *                    message is associated with, which supports a dot-delimited
	 *                    hierarchy.
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	public static void write(LogMessageSeverity severity, String logSystem, int skipFrames, Exception exception,
			LogWriteMode writeMode, String detailsXml, String category, String caption, String description,
			Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		if (skipFrames < 0) {
			skipFrames = 0; // Make sure they don't pass us a negative, it would attribute it here to us.
		}

		LogMessage logMessage = new LogMessage(severity, LogWriteMode.forValue(writeMode.getValue()),
				logSystem, category, skipFrames + 1, exception, false, detailsXml, caption, description, args);

		logMessage.publishToLog();
	}
	
	/**
	 * Write.
	 *
	 * @param severity the severity
	 * @param logSystem the log system
	 * @param provider the provider
	 * @param username the username
	 * @param throwable the throwable
	 * @param writeMode the write mode
	 * @param detailsXml the details xml
	 * @param category the category
	 * @param caption the caption
	 * @param description the description
	 * @param args the args
	 */
	public static void write(LogMessageSeverity severity, String logSystem, IMessageSourceProvider provider, String username, Throwable throwable,
			LogWriteMode writeMode, String detailsXml, String category, String caption, String description,
			Object... args) {
		Log.writeMessage(severity, writeMode, logSystem, category, provider, username, throwable, null, detailsXml, caption, description, args);
	}

	/**
	 * Write a complete log message directly to the Loupe log from a wrapper method
	 * or bridging logic, attributing the source of the message farther up the
	 * call-stack.
	 * 
	 * <p>
	 * This method provides an advanced use of Loupe log messages for use in wrapper
	 * methods and for bridging simple logging systems into Loupe. Also see
	 * Verbose
	 * and VerboseDetail
	 * and their other overloads and related methods for simpler usage of XML
	 * details when the other advanced hooks are not needed.
	 * </p>
	 * <p>
	 * This overload of Write() is provided as an API hook for simple wrapping
	 * methods which need to attribute a log message to their own outer callers
	 * rather than to the direct caller of this method. Passing a skipFrames of 0
	 * would designate the caller of this method as the originator; a skipFrames of
	 * 1 would designate the caller of the caller of this method as the originator,
	 * and so on. It will then extract information about the originator
	 * automatically based on the indicated stack frame. Bridge logic adapting from
	 * a logging system which already determines and provides information about the
	 * originator (such as log4net) into Loupe should use the other overload of
	 * Write,
	 * passing a customized IMessageSourceProvider.
	 * </p>
	 * <p>
	 * This method also requires explicitly selecting the LogWriteMode between
	 * Queued (the normal default, for optimal performance) and WaitForCommit (to
	 * help ensure critical information makes it to disk, e.g. before exiting the
	 * application upon return from this call). See the
	 * LogWriteMode enum for more information.
	 * </p>
	 * <p>
	 * This method also allows an optional Exception object to be attached to the
	 * log message (null for none). It can also include an optional XML document (as
	 * a string, or null for none) containing extended details about the message.
	 * </p>
	 * <p>
	 * If attributeToException is set to true the log message will be attributed to
	 * the location where the provided exception was thrown from instead of the
	 * caller of this method.
	 * </p>
	 * 
	 * @param severity             The log message severity.
	 * @param logSystem            The name of the originating log system (e.g.
	 *                             "Log4Net").
	 * @param skipFrames           The number of stack frames to skip back over to
	 *                             determine the original caller. (0 means the
	 *                             immediate caller of this method; 1 means their
	 *                             immediate caller, and so on.)
	 * @param exception            An Exception object to attach to this log
	 *                             message.
	 * @param attributeToException True to record this log message based on where
	 *                             the exception was thrown, not where this method
	 *                             was called
	 * @param writeMode            Whether to queue-and-return or wait-for-commit.
	 * @param detailsXml           An XML document (as a string) with extended
	 *                             details about the message. (May be null.)
	 * @param category             The application subsystem or logging category
	 *                             that the log message is associated with, which
	 *                             supports a dot-delimited hierarchy.
	 * @param caption              A simple single-line message caption. (Will not
	 *                             be processed for formatting.)
	 * @param description          Additional multi-line descriptive message (or may
	 *                             be null) which can be a format string followed by
	 *                             corresponding args.
	 * @param args                 A variable number of arguments referenced by the
	 *                             formatted description string (or no arguments to
	 *                             skip formatting).
	 */
	public static void write(LogMessageSeverity severity, String logSystem, int skipFrames, Exception exception,
			boolean attributeToException, LogWriteMode writeMode, String detailsXml, String category, String caption,
			String description, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		if (skipFrames < 0) {
			skipFrames = 0; // Make sure they don't pass us a negative, it would attribute it here to us.
		}

		LogMessage logMessage = new LogMessage(severity, LogWriteMode.forValue(writeMode.getValue()),
				logSystem, category, skipFrames + 1, exception, attributeToException, detailsXml, caption, description,
				args);

		logMessage.publishToLog();
	}

	/**
	 * Record an unexpected exception to the log without displaying a user prompt.
	 * 
	 * 
	 * <p>
	 * This method provides an easy way to record an exception as a separate message
	 * which will be attributed to the code location which threw the exception
	 * rather than where this method was called from. The category will default to
	 * "Exception" if null, and the message will be formatted automatically based on
	 * the exception. The severity will be determined by the canContinue parameter:
	 * Critical for fatal errors (canContinue is false), Error for non-fatal errors
	 * (canContinue is true).
	 * </p>
	 * <p>
	 * This method is intended for use with top-level exception catching for errors
	 * not anticipated in a specific operation, but when it is not appropriate to
	 * alert the user because the error does not impact their work flow or will be
	 * otherwise handled gracefully within the application.
	 * </p>
	 * <p>
	 * For localized exception catching (e.g. anticipating exceptions when opening a
	 * file) we recommend logging an appropriate, specific log message with the
	 * exception attached. (See
	 * TraceError,
	 * Error, and
	 * Write and other such
	 * methods; the message need not be of Error severity.)
	 * </p>
	 * 
	 *  {@code //this option records the exception but does not display any user interface.  
	 * 	Log.RecordException(ex, "Exceptions", true);
	 * 	
	 * 	//this option records the exception and displays a user interface, optionally waiting for the user 
	 * 	//to decide to continue or exit before returning.
	 * 	Log.ReportException(ex, "Exceptions", true, true);}
	 *
	 * @param throwable   An exception object to record as a log message. This call
	 *                    is ignored if null.
	 * @param category    The application subsystem or logging category that the
	 *                    message will be associated with.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void recordException(Throwable throwable, String category) throws IOException {
		recordException(throwable, category, true);
	}

	/**
	 * Record an unexpected exception to the log without displaying a user prompt.
	 * 
	 * 
	 * <p>
	 * This method provides an easy way to record an exception as a separate message
	 * which will be attributed to the code location which threw the exception
	 * rather than where this method was called from. The category will default to
	 * "Exception" if null, and the message will be formatted automatically based on
	 * the exception. The severity will be determined by the canContinue parameter:
	 * Critical for fatal errors (canContinue is false), Error for non-fatal errors
	 * (canContinue is true).
	 * </p>
	 * <p>
	 * This method is intended for use with top-level exception catching for errors
	 * not anticipated in a specific operation, but when it is not appropriate to
	 * alert the user because the error does not impact their work flow or will be
	 * otherwise handled gracefully within the application.
	 * </p>
	 * <p>
	 * For localized exception catching (e.g. anticipating exceptions when opening a
	 * file) we recommend logging an appropriate, specific log message with the
	 * exception attached. (See
	 * TraceError,
	 * Error, and
	 * Write and other such
	 * methods; the message need not be of Error severity.)
	 * </p>
	 * 
	 *  {@code
	 * 	 //this option records the exception but does not display any user interface.
	 * 	 Log.RecordException(ex, "Exceptions", true);
	 * 
	 * 	 //this option records the exception and displays a user interface, optionally waiting for the user
	 * 	 //to decide to continue or exit before returning.
	 * 	 Log.ReportException(ex, "Exceptions", true, true);}
	 *
	 * @param throwable   An exception object to record as a log message. This call
	 *                    is ignored if null.
	 * @param category    The application subsystem or logging category that the
	 *                    message will be associated with.
	 * @param canContinue True if the application can continue after this call,
	 *                    false if this is a fatal error and the application should
	 *                    not continue.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void recordException(Throwable throwable, String category, boolean canContinue) throws IOException {
		if (throwable != null) {
			Log.recordException(new MessageSourceProvider(1), throwable, null, null, category, canContinue, false, false);
		}
	}

	/**
	 * Write a Verbose trace message directly to the Loupe log.
	 * 
	 * <p>
	 * Information about the current thread and calling method is automatically
	 * captured. The log message will be attributed to the immediate caller of this
	 * method. Wrapper implementations should instead use the Log.Write(...)
	 * overloads in order to attribute the log message to their own outer callers.
	 * </p>
	 * <p>
	 * The message will not be sent through System.Diagnostics.Trace and will not be
	 * seen by other trace listeners.
	 * </p>
	 * 
	 * @param format The string message to use, or a format string followed by
	 *               corresponding args.
	 * @param args   A variable number of arguments to insert into the formatted
	 *               message string.
	 */
	public static void traceVerbose(String format, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.VERBOSE, THIS_LOG_SYSTEM, CATEGORY, 1,
				format, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a Verbose trace message directly to the Loupe log, with an attached
	 * Exception.
	 * 
	 * <p>
	 * Information about the current thread and calling method is automatically
	 * captured. The log message will be attributed to the immediate caller of this
	 * method. Wrapper implementations should instead use the Log.Write(...)
	 * overloads in order to attribute the log message to their own outer callers.
	 * </p>
	 * <p>
	 * The message will not be sent through System.Diagnostics.Trace and will not be
	 * seen by other trace listeners.
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
	 * @param exception An Exception object to attach to this log message.
	 * @param format    The string message to use, or a format string followed by
	 *                  corresponding args.
	 * @param args      A variable number of arguments to insert into the formatted
	 *                  message string.
	 */
	public static void traceVerbose(Throwable exception, String format, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.VERBOSE, QUEUED, THIS_LOG_SYSTEM,
				CATEGORY, 1, exception, format, args);

		logMessage.publishToLog();
	}

	/**
	 * Write an Information trace message directly to the Loupe log.
	 * 
	 * Information about the current thread and calling method is automatically
	 * captured. The log message will be attributed to the immediate caller of this
	 * method. Wrapper implementations should instead use the Log.Write(...)
	 * overloads in order to attribute the log message to their own outer callers.
	 * <p>
	 * The message will not be sent through System.Diagnostics.Trace and will not be
	 * seen by other trace listeners.
	 * </p>
	 * 
	 * @param format The string message to use, or a format string followed by
	 *               corresponding args.
	 * @param args   A variable number of arguments to insert into the formatted
	 *               message string.
	 */
	public static void traceInformation(String format, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.INFORMATION, THIS_LOG_SYSTEM, CATEGORY, 1,
				format, args);

		logMessage.publishToLog();
	}

	/**
	 * Write an Information trace message directly to the Loupe log, with an
	 * attached Exception.
	 * 
	 * <p>
	 * Information about the current thread and calling method is automatically
	 * captured. The log message will be attributed to the immediate caller of this
	 * method. Wrapper implementations should instead use the Log.Write(...)
	 * overloads in order to attribute the log message to their own outer callers.
	 * </p>
	 * <p>
	 * The message will not be sent through System.Diagnostics.Trace and will not be
	 * seen by other trace listeners.
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
	 * @param exception An Exception object to attach to this log message.
	 * @param format    The string message to use, or a format string followed by
	 *                  corresponding args.
	 * @param args      A variable number of arguments to insert into the formatted
	 *                  message string.
	 */
	public static void traceInformation(Throwable exception, String format, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.INFORMATION, QUEUED, THIS_LOG_SYSTEM,
				CATEGORY, 1, exception, format, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a Warning trace message directly to the Loupe log.
	 * 
	 * <p>
	 * Information about the current thread and calling method is automatically
	 * captured. The log message will be attributed to the immediate caller of this
	 * method. Wrapper implementations should instead use the Log.Write(...)
	 * overloads in order to attribute the log message to their own outer callers.
	 * </p>
	 * <p>
	 * The message will not be sent through System.Diagnostics.Trace and will not be
	 * seen by other trace listeners.
	 * </p>
	 * 
	 * @param format The string message to use, or a format string followed by
	 *               corresponding args.
	 * @param args   A variable number of arguments to insert into the formatted
	 *               message string.
	 */
	public static void traceWarning(String format, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.WARNING, THIS_LOG_SYSTEM, CATEGORY, 1,
				format, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a Warning trace message directly to the Loupe log, with an attached
	 * Exception.
	 * 
	 * <p>
	 * Information about the current thread and calling method is automatically
	 * captured. The log message will be attributed to the immediate caller of this
	 * method. Wrapper implementations should instead use the Log.Write(...)
	 * overloads in order to attribute the log message to their own outer callers.
	 * </p>
	 * <p>
	 * The message will not be sent through System.Diagnostics.Trace and will not be
	 * seen by other trace listeners.
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
	 * @param exception An Exception object to attach to this log message.
	 * @param format    The string message to use, or a format string followed by
	 *                  corresponding args.
	 * @param args      A variable number of arguments to insert into the formatted
	 *                  message string.
	 */
	public static void traceWarning(Throwable exception, String format, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.WARNING, QUEUED, THIS_LOG_SYSTEM,
				CATEGORY, 1, exception, format, args);

		logMessage.publishToLog();
	}

	/**
	 * Write an Error trace message directly to the Loupe log.
	 * 
	 * <p>
	 * Information about the current thread and calling method is automatically
	 * captured. The log message will be attributed to the immediate caller of this
	 * method. Wrapper implementations should instead use the Log.Write(...)
	 * overloads in order to attribute the log message to their own outer callers.
	 * </p>
	 * <p>
	 * The message will not be sent through System.Diagnostics.Trace and will not be
	 * seen by other trace listeners.
	 * </p>
	 * 
	 * @param format The string message to use, or a format string followed by
	 *               corresponding args.
	 * @param args   A variable number of arguments to insert into the formatted
	 *               message string.
	 */
	public static void traceError(String format, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.ERROR, THIS_LOG_SYSTEM, CATEGORY, 1,
				format, args);

		logMessage.publishToLog();
	}

	/**
	 * Write an Error trace message directly to the Loupe log, with an attached
	 * Exception.
	 * 
	 * <p>
	 * Information about the current thread and calling method is automatically
	 * captured. The log message will be attributed to the immediate caller of this
	 * method. Wrapper implementations should instead use the Log.Write(...)
	 * overloads in order to attribute the log message to their own outer callers.
	 * </p>
	 * <p>
	 * The message will not be sent through System.Diagnostics.Trace and will not be
	 * seen by other trace listeners.
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
	 * @param exception An Exception object to attach to this log message.
	 * @param format    The string message to use, or a format string followed by
	 *                  corresponding args.
	 * @param args      A variable number of arguments to insert into the formatted
	 *                  message string.
	 */
	public static void traceError(Throwable exception, String format, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.ERROR, QUEUED, THIS_LOG_SYSTEM, CATEGORY,
				1, exception, format, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a Critical trace message directly to the Loupe log.
	 * 
	 * <p>
	 * Information about the current thread and calling method is automatically
	 * captured. The log message will be attributed to the immediate caller of this
	 * method. Wrapper implementations should instead use the Log.Write(...)
	 * overloads in order to attribute the log message to their own outer callers.
	 * </p>
	 * <p>
	 * The message will not be sent through System.Diagnostics.Trace and will not be
	 * seen by other trace listeners.
	 * </p>
	 * 
	 * @param format The string message to use, or a format string followed by
	 *               corresponding args.
	 * @param args   A variable number of arguments to insert into the formatted
	 *               message string.
	 */
	public static void traceCritical(String format, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.CRITICAL, THIS_LOG_SYSTEM, CATEGORY, 1,
				format, args);

		logMessage.publishToLog();
	}

	/**
	 * Write a Critical trace message directly to the Loupe log, with an attached
	 * Exception.
	 * 
	 * <p>
	 * Information about the current thread and calling method is automatically
	 * captured. The log message will be attributed to the immediate caller of this
	 * method. Wrapper implementations should instead use the Log.Write(...)
	 * overloads in order to attribute the log message to their own outer callers.
	 * </p>
	 * <p>
	 * The message will not be sent through System.Diagnostics.Trace and will not be
	 * seen by other trace listeners.
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
	 * @param exception An Exception object to attach to this log message.
	 * @param format    The string message to use, or a format string followed by
	 *                  corresponding args.
	 * @param args      A variable number of arguments to insert into the formatted
	 *                  message string.
	 */
	public static void traceCritical(Throwable exception, String format, Object... args) {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return;
		}

		LogMessage logMessage = new LogMessage(LogMessageSeverity.CRITICAL, QUEUED, THIS_LOG_SYSTEM,
				CATEGORY, 1, exception, format, args);

		logMessage.publishToLog();
	}

	/**
	 * End the current log file (but not the session) and open a new file to
	 * continue logging.
	 * 
	 * This method is provided to support user-initiated roll-over to a new log file
	 * (instead of waiting for an automatic maintenance roll-over) in order to allow
	 * the logs of an ongoing session up to that point to be collected and submitted
	 * for analysis without shutting down the subject application.
	 */
	public static void endFile() {
		Log.endFile(1, ""); // No reason declared, attribute it to our immediate caller.
	}

	/**
	 * End the current log file (but not the session) and open a new file to
	 * continue logging, specifying an optional reason.
	 * 
	 * This method is provided to support user-initiated roll-over to a new log file
	 * (instead of waiting for an automatic maintenance roll-over) in order to allow
	 * the logs of an ongoing session up to that point to be collected and submitted
	 * for analysis without shutting down the subject application.
	 * 
	 * @param reason An optionally-declared reason for invoking this operation (may
	 *               be null or empty).
	 */
	public static void endFile(String reason) {
		Log.endFile(1, reason); // Pass on reason, attribute it to our immediate caller.
	}

	/**
	 * End the current log file (but not the session) and open a new file to
	 * continue logging, specifying an optional reason and attributing the request
	 * farther back in the call stack.
	 * 
	 * This method is provided to support user-initiated roll-over to a new log file
	 * (instead of waiting for an automatic maintenance roll-over) in order to allow
	 * the logs of an ongoing session up to that point to be collected and submitted
	 * for analysis without shutting down the subject application.
	 * 
	 * @param skipFrames The number of stack frames to skip back over to determine
	 *                   the original caller. (0 means the immediate caller of this
	 *                   method; 1 means their immediate caller, and so on.)
	 * @param reason     An optionally-declared reason for invoking this operation
	 *                   (may be null or empty).
	 */
	public static void endFile(int skipFrames, String reason) {
		if (skipFrames < 0) {
			skipFrames = 0;
		}

		Log.endFile(skipFrames + 1, reason); // Pass on reason, attribute it farther back as specified.
	}

	/**
	 * Called at the end of the process execution cycle to indicate that the process
	 * shut down normally or explicitly crashed.
	 * 
	 * 
	 * <p>
	 * This will put the Loupe Agent into an ending state in which it will flush
	 * everything still in its queue and then switch to a background thread to
	 * process any further messages. All messages submitted after this call will
	 * block the submitting thread until they are committed to disk, so that any
	 * foreground thread still recording final items will be sure to get them
	 * through before they exit.
	 * </p>
	 * <p>
	 * In WinForms applications this method is called automatically when an
	 * ApplicationExit event is received. It is also called automatically when the
	 * Agent is registered as a Trace Listener and Trace.Close is called.
	 * </p>
	 * <p>
	 * If EndSession is never called, the log will reflect that the session must
	 * have crashed.
	 * </p>
	 * 
	 *  Used to explicitly set the session state for the current session
	 * and provide a reason. 
	 * 
	 * @param endingStatus   The explicit ending status to declare for this session,
	 *                       Normal or
	 *                       Crashed.
	 * @param sourceProvider An IMessageSourceProvider object which supplies the
	 *                       source information about this log message.
	 * @param reason         A simple reason to declare why the application is
	 *                       ending as Normal or as Crashed, or may be null.
	 */
	public static void shutdown(SessionStatus endingStatus, IMessageSourceProvider sourceProvider, String reason) {
		try {
			Log.shutdown((SessionStatus) endingStatus,
					(sourceProvider == null) ? null : sourceProvider, reason);
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	}

	/**
	 * Called at the end of the process execution cycle to indicate that the process
	 * shut down normally or explicitly crashed.
	 * 
	 * 
	 * <p>
	 * This will put the Loupe Agent into an ending state in which it will flush
	 * everything still in its queue and then switch to a background thread to
	 * process any further messages. All messages submitted after this call will
	 * block the submitting thread until they are committed to disk, so that any
	 * foreground thread still recording final items will be sure to get them
	 * through before they exit.
	 * </p>
	 * <p>
	 * In WinForms applications this method is called automatically when an
	 * ApplicationExit event is received. It is also called automatically when the
	 * Agent is registered as a Trace Listener and Trace.Close is called.
	 * </p>
	 * <p>
	 * If EndSession is never called, the log will reflect that the session must
	 * have crashed.
	 * </p>
	 * 
	 *  Used to explicitly set the session state for the current session
	 * and provide a reason. 
	 * 
	 * @param endingStatus The explicit ending status to declare for this session,
	 *                     Normal or
	 *                     Crashed.
	 * @param skipFrames   The number of stack frames to skip out to find the
	 *                     original caller.
	 * @param reason       A simple reason to declare why the application is ending
	 *                     as Normal or as Crashed, or may be null.
	 */
	public static void shutdown(SessionStatus endingStatus, int skipFrames, String reason) {
		try {
			Log.shutdown(endingStatus, skipFrames + 1, reason);
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	}

	/**
	 * Called at the end of the process execution cycle to indicate that the process
	 * shut down normally or explicitly crashed.
	 * 
	 * 
	 * <p>
	 * This will put the Loupe Agent into an ending state in which it will flush
	 * everything still in its queue and then switch to a background thread to
	 * process any further messages. All messages submitted after this call will
	 * block the submitting thread until they are committed to disk, so that any
	 * foreground thread still recording final items will be sure to get them
	 * through before they exit.
	 * </p>
	 * <p>
	 * In WinForms applications this method is called automatically when an
	 * ApplicationExit event is received. It is also called automatically when the
	 * Agent is registered as a Trace Listener and Trace.Close is called.
	 * </p>
	 * <p>
	 * If EndSession is never called, the log will reflect that the session must
	 * have crashed.
	 * </p>
	 * 
	 *  Used to explicitly set the session state for the current session
	 * and provide a reason. 
	 * 
	 * @param endingStatus The explicit ending status to declare for this session,
	 *                     Normal or
	 *                     Crashed.
	 * @param reason       A simple reason to declare why the application is ending
	 *                     as Normal or as Crashed, or may be null.
	 */
	public static void shutdown(SessionStatus endingStatus, String reason) {
		// A specified exit status attributed to our immediate caller with a specified
		// reason.
		try {
			Log.shutdown(endingStatus, 1, reason);
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	}

	/**
	 * Called at the end of the process execution cycle to indicate that the process
	 * shut down normally or explicitly crashed.
	 * 
	 * 
	 * <p>
	 * This will put the Loupe Agent into an ending state in which it will flush
	 * everything still in its queue and then switch to a background thread to
	 * process any further messages. All messages submitted after this call will
	 * block the submitting thread until they are committed to disk, so that any
	 * foreground thread still recording final items will be sure to get them
	 * through before they exit.
	 * </p>
	 * <p>
	 * In WinForms applications this method is called automatically when an
	 * ApplicationExit event is received. It is also called automatically when the
	 * Agent is registered as a Trace Listener and Trace.Close is called.
	 * </p>
	 * <p>
	 * If EndSession is never called, the log will reflect that the session must
	 * have crashed.
	 * </p>
	 * 
	 * Used to explicitly set the session state for the current session
	 * and provide a reason.
	 * 
	 * @param reason A simple reason to declare why the application is ending as
	 *               Normal, or may be null.
	 */
	public static void shutdown(String reason) {
		// A specified exit status attributed to our immediate caller with a specified
		// reason.
		try {
			Log.shutdown(SessionStatus.NORMAL, 1, reason);
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	}


	/**
	 * Called at the end of the process execution cycle to indicate that the process
	 * shut down normally.
	 * 
	 * 
	 * <p>
	 * This will put the Loupe Agent into an ending state in which it will flush
	 * everything still in its queue and then switch to a background thread to
	 * process any further messages. All messages submitted after this call will
	 * block the submitting thread until they are committed to disk, so that any
	 * foreground thread still recording final items will be sure to get them
	 * through before they exit.
	 * </p>
	 * <p>
	 * In WinForms applications this method is called automatically when an
	 * ApplicationExit event is received. It is also called automatically when the
	 * Agent is registered as a Trace Listener and Trace.Close is called.
	 * </p>
	 * <p>
	 * If EndSession is never called, the log will reflect that the session must
	 * have crashed.
	 * </p>
	 * 
	 * This overload will declare a
	 * Normal ending state with no explicit
	 * reason.
	 */
	public static void shutdown() {
		// A normal exit attributed to our immediate caller with no explicit reason.
		try {
			Log.shutdown(SessionStatus.NORMAL, 1, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	}
	
	/**
	 * Attempt to activate the agent.
	 * 
	 * If the agent is already active this call has no effect. When starting, the
	 * agent will raise an Initializing event which can be canceled. If it cancels
	 * then the session has not been started. All calls to the agent are safe
	 * whether it has been activated or not.
	 * @throws IOException Exception in case of failed startup
	 */
	public static void start() throws IOException {
		Log.start(null, 1, null);
	}

	/**
	 * Attempt to activate the agent.
	 * 
	 * @param reason A caption for the reason the session is starting, or null. If
	 *               the agent is already active this call has no effect. When
	 *               starting, the agent will raise an Initializing event which can
	 *               be canceled. If it cancels then the session has not been
	 *               started. All calls to the agent are safe whether it has been
	 *               activated or not.
	 * @throws IOException Exception in case of failed startup
	 */
	public static void start(String reason) throws IOException {
		Log.start(null, 1, reason);
	}

	/**
	 * Attempt to activate the agent.
	 * 
	 * @param skipFrames The number of stack frames to skip out to find the original
	 *                   caller.
	 * @param reason     A caption for the reason the session is starting, or null.
	 *                   If the agent is already active this call has no effect.
	 *                   When starting, the agent will raise an Initializing event
	 *                   which can be canceled. If it cancels then the session has
	 *                   not been started. All calls to the agent are safe whether
	 *                   it has been activated or not.
	 * @throws IOException Exception in case of failed startup
	 */
	public static void start(int skipFrames, String reason) throws IOException {
		Log.start(null, skipFrames + 1, reason);
	}

	/**
	 * Attempt to activate the agent.
	 *
	 * @param sourceProvider An IMessageSourceProvider object which supplies the
	 *                       source information about this log message. If the agent
	 *                       is already active this call has no effect. When
	 *                       starting, the agent will raise an Initializing event
	 *                       which can be canceled. If it cancels then the session
	 *                       has not been started. All calls to the agent are safe
	 *                       whether it has been activated or not.
	 * @param reason         A caption for the reason the session is starting, or
	 *                       null.
	 * @throws IOException Exception in case of failed startup
	 */
	public static void start(IMessageSourceProvider sourceProvider, String reason) throws IOException {
		Log.start(null, (sourceProvider == null) ? null : sourceProvider, reason);
	}

	/**
	 * Attempt to activate the agent.
	 * 
	 * @param configuration The Agent configuration to use instead of any
	 *                      configuration in the app.config file. If the agent is
	 *                      already active this call has no effect. When starting,
	 *                      the agent will raise an Initializing event which can be
	 *                      canceled. If it cancels then the session has not been
	 *                      started. All calls to the agent are safe whether it has
	 *                      been activated or not.
	 * @throws IOException Exception in case of failed startup or null configuration object
	 */
	public static void start(AgentConfiguration configuration) throws IOException {
		if (configuration == null) {
			throw new NullPointerException("configuration");
		}

		Log.start(configuration, 1, null);
	}

	/**
	 * Attempt to activate the agent.
	 * 
	 * @param configuration The Agent configuration to use instead of any
	 *                      configuration in the app.config file.
	 * @param reason        A caption for the reason the session is starting, or
	 *                      null. If the agent is already active this call has no
	 *                      effect. When starting, the agent will raise an
	 *                      Initializing event which can be canceled. If it cancels
	 *                      then the session has not been started. All calls to the
	 *                      agent are safe whether it has been activated or not.
	 * @throws IOException Exception in case of failed startup or null configuration object
	 */
	public static void start(AgentConfiguration configuration, String reason) throws IOException {
		if (configuration == null) {
			throw new NullPointerException("configuration");
		}

		Log.start(configuration, 1, reason);
	}

	/**
	 * Attempt to activate the agent.
	 * 
	 * @param configuration The Agent configuration to use instead of any
	 *                      configuration in the app.config file.
	 * @param skipFrames    The number of stack frames to skip out to find the
	 *                      original caller.
	 * @param reason        A caption for the reason the session is starting, or
	 *                      null. If the agent is already active this call has no
	 *                      effect. When starting, the agent will raise an
	 *                      Initializing event which can be canceled. If it cancels
	 *                      then the session has not been started. All calls to the
	 *                      agent are safe whether it has been activated or not.
	 * @throws IOException Exception in case of failed startup or null configuration object
	 */
	public static void start(AgentConfiguration configuration, int skipFrames, String reason) throws IOException {
		if (configuration == null) {
			throw new NullPointerException("configuration");
		}

		Log.start(configuration, skipFrames + 1, reason);
	}

	/**
	 * Attempt to activate the agent.
	 *
	 * @param configuration  The Agent configuration to use instead of any
	 *                       configuration in the app.config file.
	 * @param sourceProvider An IMessageSourceProvider object which supplies the
	 *                       source information about this log message. If the agent
	 *                       is already active this call has no effect. When
	 *                       starting, the agent will raise an Initializing event
	 *                       which can be canceled. If it cancels then the session
	 *                       has not been started. All calls to the agent are safe
	 *                       whether it has been activated or not.
	 * @param reason         A caption for the reason the session is starting, or
	 *                       null.
	 * @throws IOException Exception in case of failed startup or null configuration object
	 */
	public static void start(AgentConfiguration configuration, IMessageSourceProvider sourceProvider,
			String reason) throws IOException {
		if (configuration == null) {
			throw new NullPointerException("configuration");
		}

		Log.start(configuration, (sourceProvider == null) ? null : sourceProvider, reason);
	}


	/**
	 * Safely send sessions to the Loupe Server or via email. Only one send request
	 * will be processed at a time.
	 * 
	 * @param criteria The criteria to match for the sessions to send
	 * @return True if the send was processed, false if it was not due to
	 *         configuration or another active send
	 * 
	 *         <p>
	 *         This method uses the same logic to determine how to transport data as
	 *         SendSessionsOnExit. If a Loupe Server connection is configured and
	 *         the server can be contacted it will be used. Otherwise if packaging
	 *         via email is configured the package will be sent that way.
	 *         </p>
	 *         <p>
	 *         If there is no way to send the information (either due to
	 *         configuration or the server being unreachable) this method will
	 *         return false. Otherwise the method will return when it completes
	 *         sending.
	 *         </p>
	 *         <p>
	 *         If another send attempt is currently being processed this method will
	 *         complete immediately and return false. This prevents multiple
	 *         simultaneous send attempts from consuming resources.
	 *         </p>
	 * @throws IOException Exception in case of failure to send sessions.
	 * 
	 */
	public static Boolean sendSessions(SessionCriteria criteria) throws IOException {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return false;
		}

		return Log.sendSessions(Optional.of(criteria), null, false);
	}

	/**
	 * Safely send sessions to the Loupe Server or via email. Only one send request
	 * will be processed at a time.
	 * 
	 * @param sessionMatchPredicate A delegate to evaluate sessions and determine
	 *                              which ones to send.
	 * @return True if the send was processed, false if it was not due to
	 *         configuration or another active send
	 * 
	 *         <p>
	 *         This method uses the same logic to determine how to transport data as
	 *         SendSessionsOnExit. If a Loupe Server connection is configured and
	 *         the server can be contacted it will be used. Otherwise if packaging
	 *         via email is configured the package will be sent that way.
	 *         </p>
	 *         <p>
	 *         If there is no way to send the information (either due to
	 *         configuration or the server being unreachable) this method will
	 *         return false. Otherwise the method will return when it completes
	 *         sending.
	 *         </p>
	 *         <p>
	 *         If another send attempt is currently being processed this method will
	 *         complete immediately and return false. This prevents multiple
	 *         simultaneous send attempts from consuming resources.
	 *         </p>
	 * @throws IOException Exception in case of failure to send sessions.
	 * 
	 */
	public static Boolean sendSessions(java.util.function.Predicate<ISessionSummary> sessionMatchPredicate) throws IOException {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return false;
		}

		return Log.sendSessions(null, sessionMatchPredicate, false);
	}

	/**
	 * Safely send sessions asynchronously to the Loupe Server or via email. Only
	 * one send request will be processed at a time.
	 * 
	 * @param criteria The criteria to match for the sessions to send
	 * @return True if the send was processed, false if it was not due to
	 *         configuration or another active send
	 * 
	 *         <p>
	 *         This method uses the same logic to determine how to transport data as
	 *         SendSessionsOnExit. If a Loupe Server connection is configured and
	 *         the server can be contacted it will be used. Otherwise if packaging
	 *         via email is configured the package will be sent that way.
	 *         </p>
	 *         <p>
	 *         If there is no way to send the information (either due to
	 *         configuration or the server being unreachable) this method will
	 *         return false. Otherwise the method will return once it starts the
	 *         send process, which will complete asynchronously.
	 *         </p>
	 *         <p>
	 *         If another send attempt is currently being processed this method will
	 *         complete immediately and return false. This prevents multiple
	 *         simultaneous send attempts from consuming resources.
	 *         </p>
	 * @throws IOException Exception in case of failure to send sessions.
	 * 
	 */
	public static Boolean sendSessionsAsync(SessionCriteria criteria) throws IOException {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return false;
		}

		return Log.sendSessions(Optional.of(criteria), null, true);
	}

	/**
	 * Safely send sessions asynchronously to the Loupe Server or via email. Only
	 * one send request will be processed at a time.
	 * 
	 * @param sessionMatchPredicate A delegate to evaluate sessions and determine
	 *                              which ones to send.
	 * @return True if the send was processed, false if it was not due to
	 *         configuration or another active send
	 * 
	 *         <p>
	 *         This method uses the same logic to determine how to transport data as
	 *         SendSessionsOnExit. If a Loupe Server connection is configured and
	 *         the server can be contacted it will be used. Otherwise if packaging
	 *         via email is configured the package will be sent that way.
	 *         </p>
	 *         <p>
	 *         If there is no way to send the information (either due to
	 *         configuration or the server being unreachable) this method will
	 *         return false. Otherwise the method will return once it starts the
	 *         send process, which will complete asynchronously.
	 *         </p>
	 *         <p>
	 *         If another send attempt is currently being processed this method will
	 *         complete immediately and return false. This prevents multiple
	 *         simultaneous send attempts from consuming resources.
	 *         </p>
	 * @throws IOException Exception in case of failure to send sessions.
	 * 
	 */
	public static Boolean sendSessionsAsync(java.util.function.Predicate<ISessionSummary> sessionMatchPredicate) throws IOException {
		// don't do jack if we aren't initialized.
		if (!Log.isLoggingActive()) {
			return false;
		}

		return Log.sendSessions(null, sessionMatchPredicate, true);
	}

	/**
	 * Our one metric definition collection for capturing metrics in this process
	 * 
	 * 
	 * For performance reasons, it is important that there is only a single instance
	 * of a particular metric for any given process. This is managed automatically
	 * provided only this metrics collection is used. If there is a duplicate metric
	 * in the data stream, that information will be discarded when the log file is
	 * read (but there is no affect at runtime).
	 *
	 * @return the metric definitions
	 */
	public static MetricDefinitionCollection getMetricDefinitions() {
		return metricDefinitions;
	}
	
	/**
	 * The common information about the active log session. This is always safe even
	 * when logging is disabled.
	 *
	 * @return the session summary
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static SessionSummary getSessionSummary() throws IOException {
		return Log.getSessionSummary();
	}

}