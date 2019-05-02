package com.onloupe.model;

/** 
 An interface for accessing information about a thread.
*/
public interface IThreadInfo
{

	/** 
	 The managed thread ID of the thread which originated this log message.
	*/
	long getThreadId();

	/** 
	 The name of the thread which originated this log message.
	*/
	String getThreadName();

	/** 
	 The application domain identifier of the app domain which originated this log message.
	*/
	int getDomainId();

	/** 
	 The friendly name of the app domain which originated this log message.
	*/
	String getDomainName();

	/** 
	 Indicates whether the thread which originated this log message is a background thread.
	*/
	boolean isBackground();

	/** 
	 Indicates whether the thread which originated this log message is a threadpool thread.
	*/
	boolean isThreadPoolThread();
}