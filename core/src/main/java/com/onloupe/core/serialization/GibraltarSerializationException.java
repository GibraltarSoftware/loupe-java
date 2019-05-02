package com.onloupe.core.serialization;

import com.onloupe.model.exception.GibraltarException;

/**
 * This is a base class for any new serialization Exception types we define and
 * for generic exceptions generated in Serialization.
 * 
 * Any generation of an ApplicationException in Serialization should probably
 * use this class instead.
 */
public class GibraltarSerializationException extends GibraltarException {
	// This is a dummy wrapper around Gibraltar exceptions (for now)

	private static final long serialVersionUID = 1L;

	/**
	 * Initializes a new instance of the GibraltarSerializationException class.
	 * 
	 * This contructor initializes the Message property of the new instance to a
	 * system-supplied message that describes the error and takes into account the
	 * current system culture. For more information, see the base constructor in
	 * Exception.
	 */
	public GibraltarSerializationException() {
		// Just the base default constructor
	}

	/**
	 * Initializes a new instance of the GibraltarSerializationException class with
	 * a specified error message.
	 * 
	 * @param message The error message string. This constructor initializes the
	 *                Message property of the new instance using the message
	 *                parameter. The InnerException property is left as a null
	 *                reference. For more information, see the base contructor in
	 *                Exception.
	 */
	public GibraltarSerializationException(String message) {
		super(message);
		// Just the base constructor
	}

	/**
	 * Initializes a new instance of the GibraltarSerializationException class with
	 * a specified error message.
	 * 
	 * @param message      The error message string.
	 * @param streamFailed Indicates if the entire stream is now considered corrupt
	 *                     and no further packets can be retrieved. This constructor
	 *                     initializes the Message property of the new instance
	 *                     using the message parameter. The InnerException property
	 *                     is left as a null reference. For more information, see
	 *                     the base contructor in Exception.
	 */
	public GibraltarSerializationException(String message, boolean streamFailed) {
		super(message);
		setStreamFailed(streamFailed);
	}

	/**
	 * Initializes a new instance of the GibraltarSerializationException class with
	 * a specified error message and a reference to the inner exception that is the
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
	public GibraltarSerializationException(String message, Throwable innerException) {
		super(message, innerException);
		// Just the base constructor
	}

	/**
	 * Initializes a new instance of the GibraltarSerializationException class with
	 * a specified error message and a reference to the inner exception that is the
	 * cause of this exception.
	 * 
	 * @param message        The error message string.
	 * @param innerException The exception that is the cause of the current
	 *                       exception, or a null reference if no inner exception is
	 *                       specified.
	 * @param streamFailed   Indicates if the entire stream is now considered
	 *                       corrupt and no further packets can be retrieved. An
	 *                       exception that is thrown as a direct result of a
	 *                       previous exception should include a reference to the
	 *                       previous exception in the innerException parameter. For
	 *                       more information, see the base constructor in
	 *                       Exception.
	 */
	public GibraltarSerializationException(String message, Throwable innerException, boolean streamFailed) {
		super(message, innerException);
		setStreamFailed(streamFailed);
	}

	/**
	 * Indicates if the exception is a stream error, so no further packets can be
	 * serialized
	 */
	private boolean streamFailed;

	public final boolean getStreamFailed() {
		return this.streamFailed;
	}

	private void setStreamFailed(boolean value) {
		this.streamFailed = value;
	}

}