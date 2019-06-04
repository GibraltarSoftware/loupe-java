package com.onloupe.core.logging;

import java.util.Set;

import com.onloupe.agent.IMessageSourceProvider;
import com.onloupe.agent.logging.ExceptionSourceProvider;
import com.onloupe.agent.logging.MessageSourceProvider;
import com.onloupe.core.util.LogSystems;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.log.LogMessageSeverity;

// TODO: Auto-generated Javadoc
/**
 * Base class for log message template classes.
 * 
 * This class knows how to translate from a simple logging API like Trace into
 * our more all-encompassing Gibraltar Log collector. Importantly, it knows how
 * to acquire information about the source of a log message from the current
 * call stack, and acts as its own IMessageSourceProvider when handing it off to
 * the central Log. Thus, this object must be created while still within the
 * same call stack as the origination of the log message. Used internally by our
 * Trace Listener and external Gibraltar log API.
 */
public abstract class LogMessageBase {
	
	/** The severity. */
	private LogMessageSeverity severity;
	
	/** The log system. */
	private String logSystem;
	
	/** The category name. */
	private String categoryName;
	
	/** The message source provider. */
	private IMessageSourceProvider messageSourceProvider;

	/** The caption. */
	private String caption;
	
	/** The description. */
	private String description;
	
	/** The message args. */
	private Object[] messageArgs;
	
	/** The details xml. */
	private String detailsXml;
	
	/** The throwable. */
	private Throwable throwable;
	
	/** The thread info. */
	private ThreadInfo threadInfo;
	
	/** The write mode. */
	private LogWriteMode writeMode = LogWriteMode.values()[0]; // queued-and-return or wait-for-commit
	
	/** The attribute to exception. */
	private boolean attributeToException;

	/**
	 * Base constructor for log message template classes.
	 * 
	 * @param severity     The severity of the log message.
	 * @param logSystem    The name of the logging system the message was issued
	 *                     through, such as "Trace" or "Gibraltar".
	 * @param categoryName The logging category or application subsystem category
	 *                     that the log message is associated with, such as "Trace",
	 *                     "Console", "Exception", or the logger name in Log4Net.
	 */
	protected LogMessageBase(LogMessageSeverity severity, String logSystem, String categoryName) {
		this.severity = severity;
		this.logSystem = logSystem;
		this.categoryName = categoryName;
	}

	/**
	 * Base constructor for log message template classes where the message should be
	 * attributed to the exception.
	 *
	 * @param severity             The severity of the log message.
	 * @param logSystem            The name of the logging system the message was
	 *                             issued through, such as "Trace" or "Gibraltar".
	 * @param categoryName         The logging category or application subsystem
	 *                             category that the log message is associated with,
	 *                             such as "Trace", "Console", "Exception", or the
	 *                             logger name in Log4Net.
	 * @param skipFrames           The number of stack frames to skip over to find
	 *                             the first candidate to be identified as the
	 *                             source of the log message.
	 * @param localOrigin          True if logging a message originating in
	 *                             Gibraltar code. False if logging a message from
	 *                             the client application.
	 * @param attributeToException True if the call stack from where the exception
	 *                             was thrown should be used for log message
	 *                             attribution
	 * @param throwable the throwable
	 * @param threadInfo the thread info
	 * @param exclusions the exclusions
	 */
	protected LogMessageBase(LogMessageSeverity severity, String logSystem, String categoryName, int skipFrames,
			boolean localOrigin, boolean attributeToException, Throwable throwable, ThreadInfo threadInfo, Set<String> exclusions) {
		this.severity = severity;
		this.logSystem = logSystem;
		this.categoryName = categoryName;
		this.throwable = throwable;
		this.threadInfo = threadInfo;
		
		if (attributeToException && throwable != null) {
			// try to use the exception as the source provider..
			ExceptionSourceProvider exceptionSourceProvider = new ExceptionSourceProvider(throwable);
			if (TypeUtils.isNotBlank(exceptionSourceProvider.getClassName())) {
				// yep, we found something.
				this.messageSourceProvider = exceptionSourceProvider;
			}
		}

		if (this.messageSourceProvider == null) {
			this.messageSourceProvider = new MessageSourceProvider(skipFrames + 1, exclusions);
		}
	}

	/**
	 * Instantiates a new log message base.
	 *
	 * @param severity the severity
	 * @param logSystem the log system
	 * @param categoryName the category name
	 * @param element the element
	 * @param localOrigin the local origin
	 * @param attributeToException the attribute to exception
	 * @param throwable the throwable
	 */
	protected LogMessageBase(LogMessageSeverity severity, String logSystem, String categoryName, StackTraceElement element,
			boolean localOrigin, boolean attributeToException, Throwable throwable) {
		this(severity, logSystem, categoryName, element, localOrigin, attributeToException, throwable, null);
	}
	
	/**
	 * Instantiates a new log message base.
	 *
	 * @param severity the severity
	 * @param logSystem the log system
	 * @param categoryName the category name
	 * @param element the element
	 * @param localOrigin the local origin
	 * @param attributeToException the attribute to exception
	 * @param throwable the throwable
	 * @param exclusions the exclusions
	 */
	protected LogMessageBase(LogMessageSeverity severity, String logSystem, String categoryName, StackTraceElement element,
			boolean localOrigin, boolean attributeToException, Throwable throwable, Set<String> exclusions) {
		this(severity, logSystem, categoryName, element, localOrigin, attributeToException, throwable, null, exclusions);
	}
	
	/**
	 * Instantiates a new log message base.
	 *
	 * @param severity the severity
	 * @param logSystem the log system
	 * @param categoryName the category name
	 * @param element the element
	 * @param localOrigin the local origin
	 * @param attributeToException the attribute to exception
	 * @param throwable the throwable
	 * @param threadInfo the thread info
	 * @param exclusions the exclusions
	 */
	protected LogMessageBase(LogMessageSeverity severity, String logSystem, String categoryName, StackTraceElement element,
			boolean localOrigin, boolean attributeToException, Throwable throwable, ThreadInfo threadInfo, Set<String> exclusions) {
		this.severity = severity;
		this.logSystem = logSystem;
		this.categoryName = categoryName;
		this.throwable = throwable;
		this.threadInfo = threadInfo;
		
		if (attributeToException && throwable != null) {
			// try to use the exception as the source provider..
			ExceptionSourceProvider exceptionSourceProvider = new ExceptionSourceProvider(throwable);
			if (TypeUtils.isNotBlank(exceptionSourceProvider.getClassName())) {
				// yep, we found something.
				this.messageSourceProvider = exceptionSourceProvider;
			}
		}

		if (this.messageSourceProvider == null) {
			if (element != null) {
				// if we receive a stack trace element from the client, let's use it.
				this.messageSourceProvider = new MessageSourceProvider(element);
			} else {
				// we did not receive a stack trace element from the client, so let's find it.
				if (LogSystems.LOG4J.equals(logSystem)) {
					// log4j generally adds 5 frames. there is room for enrichment of the
					// LogSystems class in the future.
					this.messageSourceProvider = new MessageSourceProvider(5, throwable, exclusions);
				}
			}
		}
	}

	/**
	 * The severity of the log message.
	 *
	 * @return the severity
	 */
	public final LogMessageSeverity getSeverity() {
		return this.severity;
	}

	/**
	 * The name of the logging system the message was issued through, such as
	 * "Trace" or "Gibraltar".
	 *
	 * @return the log system
	 */
	public final String getLogSystem() {
		return this.logSystem;
	}

	/**
	 * The logging category or application subsystem category that the log message
	 * is associated with, such as "Trace", "Console", "Exception", or the logger
	 * name in Log4Net.
	 *
	 * @return the category name
	 */
	public final String getCategoryName() {
		return this.categoryName;
	}

	/**
	 * A single line display caption. It will not be format-expanded.
	 *
	 * @return the caption
	 */
	public final String getCaption() {
		return this.caption;
	}

	/**
	 * Sets the caption.
	 *
	 * @param value the new caption
	 */
	protected final void setCaption(String value) {
		this.caption = value;
	}

	/**
	 * Optional. A multiline description to use which can be a format string for for
	 * the arguments. Can be null.
	 *
	 * @return the description
	 */
	public final String getDescription() {
		return this.description;
	}

	/**
	 * Sets the description.
	 *
	 * @param value the new description
	 */
	protected final void setDescription(String value) {
		this.description = value;
	}

	/**
	 * Optional additional args to match up with the formatting string.
	 *
	 * @return the message args
	 */
	public final Object[] getMessageArgs() {
		return this.messageArgs;
	}

	/**
	 * Sets the message args.
	 *
	 * @param value the new message args
	 */
	protected final void setMessageArgs(Object[] value) {
		this.messageArgs = value;
	}

	/**
	 * Optional. An XML document with extended details about the message. Can be
	 * null.
	 *
	 * @return the details xml
	 */
	public final String getDetailsXml() {
		return this.detailsXml;
	}

	/**
	 * Sets the details xml.
	 *
	 * @param value the new details xml
	 */
	protected final void setDetailsXml(String value) {
		this.detailsXml = value;
	}

	/**
	 * An exception associated with this log message (or null for none).
	 *
	 * @return the exception
	 */
	public final Throwable getException() {
		return this.throwable;
	}

	/**
	 * Sets the exception.
	 *
	 * @param value the new exception
	 */
	protected final void setException(Throwable value) {
		this.throwable = value;
	}

	/**
	 * Record this log message based on where the exception was thrown, not where
	 * this method was called.
	 *
	 * @return the attribute to exception
	 */
	public final boolean getAttributeToException() {
		return this.attributeToException;
	}

	/**
	 * Sets the attribute to exception.
	 *
	 * @param value the new attribute to exception
	 */
	protected final void setAttributeToException(boolean value) {
		this.attributeToException = value;
	}

	/**
	 * Whether to queue-and-return or wait-for-commit.
	 *
	 * @return the write mode
	 */
	public final LogWriteMode getWriteMode() {
		return this.writeMode;
	}

	/**
	 * Sets the write mode.
	 *
	 * @param value the new write mode
	 */
	protected final void setWriteMode(LogWriteMode value) {
		this.writeMode = value;
	}

	/**
	 * This static helper method looks through an array of objects (eg. the param
	 * args) for the first Exception.
	 * 
	 * @param args An array of objects which might or might not contain an
	 *             Exception.
	 * @return The first element of the array which is an Exception (or derived from
	 *         Exception), or null if none is found.
	 */
	protected static Exception firstException(Object[] args) {
		if (args == null) {
			return null;
		}
		for (int i = 0; i < args.length; i++) {
			Exception exception = args[i] instanceof Exception ? (Exception) args[i] : null;
			if (exception != null) {
				return exception;
			}
		}
		return null;
	}

	/**
	 * Publish this SimpleLogMessage to the Gibraltar central log.
	 */
	public final void publishToLog() {
		// We pass a null for the user name so that Log.WriteMessage() will figure it
		// out for itself.
		Log.writeMessage(this.severity, this.writeMode, this.logSystem, this.categoryName,
				this.messageSourceProvider, null, this.throwable, this.threadInfo, this.detailsXml, this.caption, this.description,
				this.messageArgs);
	}
}