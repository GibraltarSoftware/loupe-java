package com.onloupe.model.data;

/**
 * An interface which provides recorded information about an Exception.
 */
public interface IExceptionInfo {
	/**
	 * The full name of the type of the Exception.
	 */
	String getTypeName();

	/**
	 * The Message string of the Exception.
	 */
	String getMessage();

	/**
	 * A formatted string describing the source of an Exception.
	 */
	String getSource();

	/**
	 * A string dump of the Exception stack trace information.
	 */
	String getStackTrace();

	/**
	 * The information about this exception's inner exception (or null if none).
	 */
	IExceptionInfo getInnerException();
}