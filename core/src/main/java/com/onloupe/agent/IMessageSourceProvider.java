package com.onloupe.agent;

/**
 * An interface by which conversion classes can provide the details of the
 * source of a log message.
 * 
 * Unavailable fields may return null.
 */
public interface IMessageSourceProvider {
	// Note: We don't support passing the originating threadId and rely on receiving
	// log messages still on the same thread.

	/**
	 * Should return the simple name of the method which issued the log message.
	 */
	String getMethodName();

	/**
	 * Should return the full name of the class (with namespace) whose method issued
	 * the log message.
	 */
	String getClassName();

	/**
	 * Should return the name of the file containing the method which issued the log
	 * message.
	 */
	String getFileName();

	/**
	 * Should return the line within the file at which the log message was issued.
	 */
	int getLineNumber();

	// ToDo: Assembly and method Signature info?

}