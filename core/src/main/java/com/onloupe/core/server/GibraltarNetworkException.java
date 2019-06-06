package com.onloupe.core.server;

import com.onloupe.model.exception.GibraltarException;


/**
 * Exceptions related to network operations.
 */
public class GibraltarNetworkException extends GibraltarException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Initializes a new instance of the GibraltarNetworkException class.
	 * 
	 * This constructor initializes the Message property of the new instance to a
	 * system-supplied message that describes the error and takes into account the
	 * current system culture. For more information, see the base constructor in
	 * Exception.
	 */
	public GibraltarNetworkException() {
		// Just the base default constructor
	}

	/**
	 * Initializes a new instance of the GibraltarNetworkException class with a
	 * specified error message.
	 * 
	 * @param message The error message string. This constructor initializes the
	 *                Message property of the new instance using the message
	 *                parameter. The InnerException property is left as a null
	 *                reference. For more information, see the base constructor in
	 *                Exception.
	 */
	public GibraltarNetworkException(String message) {
		super(message);
		// Just the base constructor
	}

	/**
	 * Instantiates a new gibraltar network exception.
	 *
	 * @param t the t
	 */
	public GibraltarNetworkException(Throwable t) {
		super(t);
		// Just the base constructor
	}

	/**
	 * Instantiates a new gibraltar network exception.
	 *
	 * @param message the message
	 * @param t the t
	 */
	public GibraltarNetworkException(String message, Throwable t) {
		super(message, t);
		// Just the base constructor
	}

	/**
	 * Initializes a new instance of the GibraltarNetworkException class with a
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
	public GibraltarNetworkException(String message, RuntimeException innerException) {
		super(message, innerException);
		// Just the base constructor
	}

}