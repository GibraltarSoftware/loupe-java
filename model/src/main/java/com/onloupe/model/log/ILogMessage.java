package com.onloupe.model.log;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.onloupe.model.data.IExceptionInfo;

/** 
 An interface for accessing log message data.
 
 Most string properties of this interface (except where otherwise noted) will not return null values.
*/
public interface ILogMessage
{

	/** 
	 @return A Guid identifying this log message, unique across all sessions.
	 */
	UUID getId();

	/** 
	 @return The sequence number assigned to this log message, unique within this session.
	*/
	long getSequence();

	/** 
	 @return The timestamp of this log message.
	*/
	OffsetDateTime getTimestamp();

	/** 	 
	 Severities have lower numerical values the more severe a message is,
	 with Critical = 1 and Verbose = 16 enabling numerical comparison to capture
	 a given severity and worse or better.  For example, Severity &lt; LogMessageSeverity.WARNING
	 will match Error and Critical. 
	 
	 @return The severity of this log message (from Critical to Verbose).
	*/
	LogMessageSeverity getSeverity();

	/** 
	 @return The log system which issued this log message.
	 
	 Internally, Loupe generally uses &quot;Gibraltar&quot; for its own messages as well as those
	 logged directly to the Log object, and &quot;Trace&quot; for messages captured via the .NET Trace subsystem.
	 You can use your own value by using the Log.Write methods which are designed to enable forwarding messages
	 from other log systems.
	*/
	String getLogSystem();

	/** 
	 @return The dot-delimited hierarchical category for this log message.
	*/
	String getCategoryName();

	/** 
	 @return The user name associated with this log message (often just the user who started the process).
	 
	 If user anonymization is enabled in configuration this will reflect the anonymous value.
	*/
	String getUserName();

	/** 
	 @return The simple caption string for this log message.
	*/
	String getCaption();

	/** 
	 @return The longer description for this log message.
	*/
	String getDescription();

	/** 
	 @return The optional details XML for this log message (as a string).  (Or null if none.)
	*/
	String getDetails();

	/** 
	 @return The name of the method which originated this log message, unless unavailable.  (Can be null or empty.)
	*/
	String getMethodName();

	/** 
	 @return The full name of the class containing this method which originated the log message, unless unavailable.  (Can be null or empty.)
	*/
	String getClassName();

	/** 
	 @return The full path to the file containing this definition of the method which originated the log message, if available.  (Can be null or empty.)
	*/
	String getFileName();

	/** 
	 @return The line number in the file at which the this message originated, if available.
	*/
	int getLineNumber();

	/** 
	 @return Whether or not this log message includes attached Exception information.
	*/
	boolean getHasException();

	/** 
	 @return The information about any Exception attached to this log message.  (Or null if none.)
	*/
	IExceptionInfo getException();

	/** 
	 * 	 This is not the operating system thread Id as managed threads do not necessarily
	 correspond to OS threads.
	@return The Managed Thread Id of the thread which originated this log message.
	*/
	long getThreadId();

	/** 
	 @return The name of the thread which originated this log message.
	*/
	String getThreadName();

	/** 
	 @return The application domain identifier of the app domain which originated this log message.
	*/
	int getDomainId();
	/** 
	 @return The friendly name of the app domain which originated this log message.
	*/
	String getDomainName();

	/** 
	 @return Indicates whether the thread which originated this log message is a Thread Pool thread.
	*/
	boolean isThreadPoolThread();

	/** 
	 @return Indicates if the log message has related thread information.  If false, some calls to thread information may throw exceptions.
	*/
	boolean getHasThreadInfo();

	/** 
	 @return Indicates if the class name and method name are available.
	*/
	boolean getHasMethodInfo();

	/** 
	 @return Indicates if the file name and line number are available.
	*/
	boolean getHasSourceLocation();
}