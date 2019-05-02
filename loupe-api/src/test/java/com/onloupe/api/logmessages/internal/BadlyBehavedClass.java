package com.onloupe.api.logmessages.internal;

public class BadlyBehavedClass
{

	public final void methodThatThrowsException()
	{
		throw new IllegalStateException("This is just so we can check the call stack");
	}
}