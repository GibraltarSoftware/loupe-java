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
	 *
	 * @return the method name
	 */
	String getMethodName();

	/**
	 * Should return the full name of the class (with namespace) whose method issued
	 * the log message.
	 *
	 * @return the class name
	 */
	String getClassName();

	/**
	 * Should return the name of the file containing the method which issued the log
	 * message.
	 *
	 * @return the file name
	 */
	String getFileName();

	/**
	 * Should return the line within the file at which the log message was issued.
	 *
	 * @return the line number
	 */
	int getLineNumber();

	// ToDo: Assembly and method Signature info?

}