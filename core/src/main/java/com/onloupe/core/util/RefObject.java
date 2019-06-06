package com.onloupe.core.util;


/**
 * The Class RefObject.
 *
 * @param <T> the generic type
 */
public final class RefObject<T>
{
	
	/** The arg value. */
	public T argValue;
	
	/**
	 * Instantiates a new ref object.
	 *
	 * @param refArg the ref arg
	 */
	public RefObject(T refArg)
	{
		argValue = refArg;
	}
}