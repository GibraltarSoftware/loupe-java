package com.onloupe.model.exception;

import java.util.Locale;

/** 
 This exception indicates that an unexpected packet version was encountered.
 
 This exception occurs when processing a Gibraltar log packet with a version
 which is not understood by this version of Gibraltar.  This usually means that an older
 version of Gibraltar is trying to read a log stream generated by a newer and incompatible
 version of Gibraltar.  For more information, see the root class, Exception.
*/
public class GibraltarPacketVersionException extends GibraltarException
{

	private static final long serialVersionUID = 7713046978868701753L;

	/** 
	 Initializes a new instance of the GibraltarPacketVersionException class.
	 
	 This constructor initializes the Message property of the new instance to a system-supplied
	 message that describes the error and takes into account the current system culture.
	 For more information, see the base constructor in Exception.
	*/
	public GibraltarPacketVersionException()
	{
		// Just the base default constructor
	}

	/** 
	 Initializes a new instance of the GibraltarPacketVersionException class with a specified error message.
	 
	 @param message The error message string.
	 This constructor initializes the Message property of the new instance using the
	 message parameter.  The InnerException property is left as a null reference.
	 For more information, see the base constructor in Exception.
	*/
	public GibraltarPacketVersionException(String message)
	{
		super(message);
		// Just the base constructor
	}

	/** 
	 Initializes a new instance of the GibraltarPacketVersionException class with a specified error message
	 and a reference to the inner exception that is the cause of this exception.
	 
	 @param message The error message string.
	 @param innerException The exception that is the cause of the current exception, or a
	 null reference if no inner exception is specified.
	 An exception that is thrown as a direct result of a previous exception should include
	 a reference to the previous exception in the innerException parameter.
	 For more information, see the base constructor in Exception.
	*/
	public GibraltarPacketVersionException(String message, RuntimeException innerException)
	{
		super(message, innerException);
		// Just the base constructor
	}

	/** 
	 Initializes a new instance of the GibraltarPacketVersionException class to a standard
	 error message string given a supplied version parameter.
	 
	 @param version The unexpected version encountered
	 This is the preferred way to initialize this exception type, because
	 this constructor automatically formats the message string to be "Unexpected version: %s",
	 where %s is replaced with the provided version argument.
	*/
	public GibraltarPacketVersionException(int version)
	{
		this(String.format(Locale.ROOT, "Unexpected version: %1$s", version));
		// Just call the other constructor
	}
}