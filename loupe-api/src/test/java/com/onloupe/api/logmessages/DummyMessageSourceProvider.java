package com.onloupe.api.logmessages;

import com.onloupe.agent.IMessageSourceProvider;

public class DummyMessageSourceProvider implements IMessageSourceProvider
{
	public DummyMessageSourceProvider(String className, String methodName, String fileName, int lineNumber)
	{
		setClassName(className);
		setMethodName(methodName);
		setFileName(fileName);
		setLineNumber(lineNumber);
	}

	/** 
	 Should return the simple name of the method which issued the log message.
	*/
	private String methodName;
	public final String getMethodName()
	{
		return methodName;
	}
	private void setMethodName(String value)
	{
		methodName = value;
	}

	/** 
	 Should return the full name of the class (with namespace) whose method issued the log message.
	*/
	private String className;
	public final String getClassName()
	{
		return className;
	}
	private void setClassName(String value)
	{
		className = value;
	}

	/** 
	 Should return the name of the file containing the method which issued the log message.
	*/
	private String fileName;
	public final String getFileName()
	{
		return fileName;
	}
	private void setFileName(String value)
	{
		fileName = value;
	}

	/** 
	 Should return the line within the file at which the log message was issued.
	*/
	private int lineNumber;
	public final int getLineNumber()
	{
		return lineNumber;
	}
	private void setLineNumber(int value)
	{
		lineNumber = value;
	}
}