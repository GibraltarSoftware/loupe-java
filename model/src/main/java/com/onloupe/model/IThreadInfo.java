package com.onloupe.model;

// TODO: Auto-generated Javadoc
/** 
 An interface for accessing information about a thread.
*/
public interface IThreadInfo
{


	/**
	 * Gets the thread id.
	 *
	 * @return the thread id
	 */
	long getThreadId();

	/**
	 *  
	 * 	 The name of the thread which originated this log message.
	 *
	 * @return the thread name
	 */
	String getThreadName();

	/**
	 *  
	 * 	 The application domain identifier of the app domain which originated this log message.
	 *
	 * @return the domain id
	 */
	int getDomainId();

	/**
	 *  
	 * 	 The friendly name of the app domain which originated this log message.
	 *
	 * @return the domain name
	 */
	String getDomainName();

	/**
	 *  
	 * 	 Indicates whether the thread which originated this log message is a background thread.
	 *
	 * @return true, if is background
	 */
	boolean isBackground();

	/**
	 *  
	 * 	 Indicates whether the thread which originated this log message is a threadpool thread.
	 *
	 * @return true, if is thread pool thread
	 */
	boolean isThreadPoolThread();
}