package com.onloupe.core.server;

import com.onloupe.model.exception.GibraltarException;


/**
 * Thrown when an operation times out.
 */
public class GibraltarTimeoutException extends GibraltarException {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Initializes a new instance of the GibraltarTimeoutException class.
	 * 
	 * This constructor initializes the Message property of the new instance to a
	 * system-supplied message that describes the error and takes into account the
	 * current system culture. For more information, see the base constructor in
	 * Exception.
	 */
	public GibraltarTimeoutException() {
		// Just the base default constructor
	}

	/**
	 * Initializes a new instance of the GibraltarTimeoutException class with a
	 * specified error message.
	 * 
	 * @param message The error message string. This constructor initializes the
	 *                Message property of the new instance using the message
	 *                parameter. The InnerException property is left as a null
	 *                reference. For more information, see the base constructor in
	 *                Exception.
	 */
	public GibraltarTimeoutException(String message) {
		super(message);
		// Just the base constructor
	}

	/**
	 * Initializes a new instance of the GibraltarTimeoutException class with a
	 * specified error message and a reference to the inner exception that is the
	 * cause of this exception.
	 * 
	 * @param message        The error message string.
	 * @param innerException The exception that is the cause of the current
	 *                       exception, or a null reference if no inner exception is
	 *                       specified. An exception that is thrown as a direct
	 *                       result of a previous exception should include a
	 *                       reference to the previous exception in the
	 *                       innerException parameter. For more information, see the
	 *                       base constructor in Exception.
	 */
	public GibraltarTimeoutException(String message, RuntimeException innerException) {
		super(message, innerException);
		// Just the base constructor
	}
}