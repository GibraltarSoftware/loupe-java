package com.onloupe.model.data;


/**
 * An interface which provides recorded information about an Exception.
 */
public interface IExceptionInfo {
	
	/**
	 * The full name of the type of the Exception.
	 *
	 * @return the type name
	 */
	String getTypeName();

	/**
	 * The Message string of the Exception.
	 *
	 * @return the message
	 */
	String getMessage();

	/**
	 * A formatted string describing the source of an Exception.
	 *
	 * @return the source
	 */
	String getSource();

	/**
	 * A string dump of the Exception stack trace information.
	 *
	 * @return the stack trace
	 */
	String getStackTrace();

	/**
	 * The information about this exception's inner exception (or null if none).
	 *
	 * @return the inner exception
	 */
	IExceptionInfo getInnerException();
}