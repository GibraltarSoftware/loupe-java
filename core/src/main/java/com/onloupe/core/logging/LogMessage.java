package com.onloupe.core.logging;

import java.util.Set;

import com.onloupe.model.log.LogMessageSeverity;

/**
 * An intermediary class to log a Gibraltar log message including an XML details
 * string.
 * 
 * This class knows how to formulate our most advanced log message format
 * including an XML details string. Importantly, it knows how to acquire
 * information about the source of a log message from the current call stack,
 * and acts as its own IMessageSourceProvider when handing it off to the central
 * Log. Thus, this object must be created while still within the same call stack
 * as the origination of the log message. Used internally by our external
 * Gibraltar log API.
 */
public class LogMessage extends LogMessageBase {

	public LogMessage(LogMessageSeverity severity, String logSystem, String categoryName, int skipFrames, String description, Object... args) {
		this(severity, logSystem, categoryName, skipFrames, null, description,  args);
	}
	
	public LogMessage(LogMessageSeverity severity, String logSystem, String categoryName, int skipFrames,
			String caption, String description, Object... args) {
		this(severity, LogWriteMode.QUEUED, logSystem, categoryName, skipFrames + 1, null, caption, description,
				args);
	}
	
	/**
	 * Creates a DetailLogMessage object with default LogWriteMode behavior and an
	 * XML details string.
	 * 
	 * This constructor creates a DetailLogMessage with the default LogWriteMode
	 * behavior (Queued) and a specified XML details string (which may be null).
	 * 
	 * @param severity     The severity of the log message.
	 * @param logSystem    The name of the logging system the message was issued
	 *                     through, such as "Trace" or "Gibraltar".
	 * @param categoryName The logging category or application subsystem category
	 *                     that the log message is associated with, such as "Trace",
	 *                     "Console", "Exception", or the logger name in Log4Net.
	 * @param skipFrames   The number of stack frames to skip over to find the first
	 *                     candidate to be identified as the source of the log
	 *                     message.
	 * @param detailsXml   Optional. An XML document with extended details about the
	 *                     message. Can be null.
	 * @param caption      A single line display caption.
	 * @param description  Optional. A multi-line description to use which can be a
	 *                     format string for the arguments. Can be null.
	 * @param args         Optional additional args to match up with the formatting
	 *                     string.
	 */
	public LogMessage(LogMessageSeverity severity, String logSystem, String categoryName, int skipFrames,
			String detailsXml, String caption, String description, Object... args) {
		this(severity, LogWriteMode.QUEUED, logSystem, categoryName, skipFrames + 1, detailsXml, caption, description,
				args);
	}

	/**
	 * Creates a DetailLogMessage object with specified LogWriteMode behavior and an
	 * XML details string.
	 * 
	 * This constructor creates a DetailLogMessage with specified LogWriteMode
	 * behavior (queue-and-return or wait-for-commit) and XML details string (which
	 * may be null).
	 * 
	 * @param severity     The severity of the log message.
	 * @param writeMode    Whether to queue-and-return or wait-for-commit.
	 * @param logSystem    The name of the logging system the message was issued
	 *                     through, such as "Trace" or "Gibraltar".
	 * @param categoryName The logging category or application subsystem category
	 *                     that the log message is associated with, such as "Trace",
	 *                     "Console", "Exception", or the logger name in Log4Net.
	 * @param skipFrames   The number of stack frames to skip over to find the first
	 *                     candidate to be identified as the source of the log
	 *                     message.
	 * @param detailsXml   Optional. An XML document with extended details about the
	 *                     message. Can be null.
	 * @param caption      A single line display caption.
	 * @param description  Optional. A multi-line description to use which can be a
	 *                     format string for the arguments. Can be null.
	 * @param args         Optional additional args to match up with the formatting
	 *                     string.
	 */
	public LogMessage(LogMessageSeverity severity, LogWriteMode writeMode, String logSystem, String categoryName,
			int skipFrames, String detailsXml, String caption, String description, Object... args) {
		this(severity, writeMode, logSystem, categoryName, skipFrames + 1, null, false, detailsXml, caption,
				description, args);
	}

	public LogMessage(LogMessageSeverity severity, LogWriteMode writeMode, String logSystem, String categoryName,
			int skipFrames, String caption, String description, Object... args) {
		this(severity, writeMode, logSystem, categoryName, skipFrames + 1, null, false, null, caption,
				description, args);
	}

	public LogMessage(LogMessageSeverity severity, LogWriteMode writeMode, String logSystem, String categoryName,
			int skipFrames, Throwable exception, String description, Object... args) {
		this(severity, writeMode, logSystem, categoryName, skipFrames, exception, null, null, false, null, null, description, args);
	}

	public LogMessage(LogMessageSeverity severity, LogWriteMode writeMode, String logSystem, String categoryName,
			int skipFrames, Throwable exception, ThreadInfo threadInfo, boolean attributeToException, String caption,
			String description, Object... args) {
		this(severity, writeMode, logSystem, categoryName, skipFrames, exception, threadInfo, null, attributeToException, null, caption, description, args);
	}
	
	public LogMessage(LogMessageSeverity severity, LogWriteMode writeMode, String logSystem, String categoryName,
			int skipFrames, Throwable exception, boolean attributeToException, String detailsXml, String caption,
			String description, Object... args) {
		this(severity, writeMode, logSystem, categoryName, skipFrames, exception, null, null, attributeToException, detailsXml, caption, description, args);
	}
	
	public LogMessage(LogMessageSeverity severity, LogWriteMode writeMode, String logSystem, String categoryName,
			int skipFrames, Throwable exception, ThreadInfo threadInfo, boolean attributeToException, String detailsXml, String caption,
			String description, Object... args) {
		this(severity, writeMode, logSystem, categoryName, skipFrames, detailsXml, caption, description, args);
	}

	public LogMessage(LogMessageSeverity severity, String logSystem, String categoryName,
			int skipFrames, Throwable exception, Set<String> exclusions, ThreadInfo threadInfo, boolean attributeToException, String caption,
			String description, Object... args) {
		this(severity, LogWriteMode.QUEUED, logSystem, categoryName, skipFrames, exception, threadInfo, attributeToException, null, caption, description, args);
	}

	/**
	 * Creates a DetailLogMessage object with specified LogWriteMode behavior,
	 * Exception object, and XML details string.
	 * 
	 * This constructor creates a DetailLogMessage with specified LogWriteMode
	 * behavior (queue-and-return or wait-for-commit), a specified Exception object
	 * to attach, and XML details string (which may be null).
	 * 
	 * @param severity             The severity of the log message.
	 * @param writeMode            Whether to queue-and-return or wait-for-commit.
	 * @param logSystem            The name of the logging system the message was
	 *                             issued through, such as "Trace" or "Gibraltar".
	 * @param categoryName         The logging category or application subsystem
	 *                             category that the log message is associated with,
	 *                             such as "Trace", "Console", "Exception", or the
	 *                             logger name in Log4Net.
	 * @param skipFrames           The number of stack frames to skip over to find
	 *                             the first candidate to be identified as the
	 *                             source of the log message.
	 * @param detailsXml           Optional. An XML document with extended details
	 *                             about the message. Can be null.
	 * @param exception            An exception associated with this log message (or
	 *                             null for none).
	 * @param attributeToException True if the call stack from where the exception
	 *                             was thrown should be used for log message
	 *                             attribution
	 * @param caption              A single line display caption.
	 * @param description          Optional. A multi-line description to use which
	 *                             can be a format string for the arguments. Can be
	 *                             null.
	 * @param args                 Optional additional args to match up with the
	 *                             formatting string.
	 */
	public LogMessage(LogMessageSeverity severity, LogWriteMode writeMode, String logSystem, String categoryName,
			int skipFrames, Throwable exception, ThreadInfo threadInfo, Set<String> exclusions, boolean attributeToException, String detailsXml, String caption,
			String description, Object... args) {
		super(severity, logSystem, categoryName, skipFrames + 1, false, attributeToException, exception, threadInfo, exclusions);
		setWriteMode(writeMode);
		setException(exception);
		setDetailsXml(detailsXml);

		setCaption(caption); // Allow null, or should we force it to string.Empty? Null will split it within
								// Description.
		setDescription(description);
		setMessageArgs(args);
	}
	
	public LogMessage(LogMessageSeverity severity, LogWriteMode writeMode, String logSystem, String categoryName,
			StackTraceElement element, Throwable exception, boolean attributeToException, String detailsXml, String caption,
			String description, Object... args) {
		this(severity, writeMode, logSystem, categoryName, element, exception, null, attributeToException, detailsXml, caption, description, args);
	}
	
	public LogMessage(LogMessageSeverity severity, String logSystem, String categoryName,
			StackTraceElement element, Throwable exception, ThreadInfo threadInfo, boolean attributeToException, String caption,
			String description, Object... args) {
		this(severity, LogWriteMode.QUEUED, logSystem, categoryName, element, exception, attributeToException, null, caption, description, args);
	}
	
	public LogMessage(LogMessageSeverity severity, LogWriteMode writeMode, String logSystem, String categoryName,
			StackTraceElement element, Throwable exception, ThreadInfo threadInfo, boolean attributeToException, String detailsXml, String caption,
			String description, Object... args) {
		super(severity, logSystem, categoryName, element, false, attributeToException, exception, threadInfo, null);
		setWriteMode(writeMode);
		setException(exception);
		setDetailsXml(detailsXml);

		setCaption(caption); // Allow null, or should we force it to string.Empty? Null will split it within
								// Description.
		setDescription(description);
		setMessageArgs(args);
	}
}