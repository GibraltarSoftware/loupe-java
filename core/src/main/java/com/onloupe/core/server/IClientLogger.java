package com.onloupe.core.server;

import com.onloupe.model.log.LogMessageSeverity;


/**
 * HTTP Client logging interface.
 */
public interface IClientLogger {
	
	/**
	 * Indicates if only minimal logging should be performed.
	 *
	 * @return the silent mode
	 */
	boolean getSilentMode();

	/**
	 * Write a trace message directly to the Gibraltar log.
	 * 
	 * The log message will be attributed to the caller of this method. Wrapper
	 * methods should instead call the WriteMessage() method in order to attribute
	 * the log message to their own outer callers.
	 * 
	 * @param severity    The log message severity.
	 * @param category    The category for this log message.
	 * @param caption     A simple single-line message caption. (Will not be
	 *                    processed for formatting.)
	 * @param description Additional multi-line descriptive message (or may be null)
	 *                    which can be a format string followed by corresponding
	 *                    args.
	 * @param args        A variable number of arguments referenced by the formatted
	 *                    description string (or no arguments to skip formatting).
	 */
	void write(LogMessageSeverity severity, String category, String caption, String description, Object... args);

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
	 * @param exception            An Exception object to attach to this log
	 *                             message.
	 * @param attributeToException True if the call stack from where the exception
	 *                             was thrown should be used for log message
	 *                             attribution
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
	void write(LogMessageSeverity severity, Throwable exception, boolean attributeToException, String category,
			String caption, String description, Object... args);
}