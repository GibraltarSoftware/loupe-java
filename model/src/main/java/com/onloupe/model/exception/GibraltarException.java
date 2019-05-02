package com.onloupe.model.exception;

/** 
 Represents errors that occur within Gibraltar during application execution.
 
 This is a base class for any new Exception types we define and for generic exceptions we
 generate.  Custom Exception types defined by Gibraltar should try to derive from GibraltarException
 so that they could be caught as this base type.  This may not currently be consistent, however.
 For more information, see the root class, Exception.
*/
public class GibraltarException extends RuntimeException
{
	// This is a dummy wrapper around generic exceptions (for now)

	// This also has the problem that although Common\Agent is accessible to all of Gibraltar code,
	// we hide it from view in Gibraltar.dll, thus preventing customer code from being able to reference it!
	// This will have to be reconsidered at some point.  However, we likely want to replace most or all
	// exceptions with our ErrorNotifier system, because we don't want Gibraltar.dll to break vital customer
	// applications just because they made some mistake in accessing our code.  So they likely won't see
	// any of these exceptions, anyway.

	private static final long serialVersionUID = -6573650378658323044L;

	/** 
	 Initializes a new instance of the GibraltarException class.
	 
	 This constructor initializes the Message property of the new instance to a system-supplied
	 message that describes the error and takes into account the current system culture.
	 For more information, see the base constructor in Exception.
	*/
	public GibraltarException()
	{
		// Just the base default constructor, except...
	}

	/** 
	 Initializes a new instance of the GibraltarException class with a specified error message.
	 
	 @param message The error message string.
	 This constructor initializes the Message property of the new instance using the
	 message parameter.  The InnerException property is left as a null reference.
	 For more information, see the base constructor in Exception.
	*/
	public GibraltarException(String message)
	{
		super(message);
		// Just the base constructor, except...
	}

	/** 
	 Initializes a new instance of the GibraltarException class with a specified error message
	 and a reference to the inner exception that is the cause of this exception.
	 
	 @param message The error message string.
	 @param innerException The exception that is the cause of the current exception, or a
	 null reference if no inner exception is specified.
	 An exception that is thrown as a direct result of a previous exception should include
	 a reference to the previous exception in the innerException parameter.
	 For more information, see the base constructor in Exception.
	*/
	public GibraltarException(String message, RuntimeException innerException)
	{
		super(message, innerException);
		// Just the base constructor, except...
	}

	public GibraltarException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public GibraltarException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}
	
	
}