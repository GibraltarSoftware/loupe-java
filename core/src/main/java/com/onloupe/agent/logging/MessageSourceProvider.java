package com.onloupe.agent.logging;

import java.util.Set;

import com.onloupe.agent.IMessageSourceProvider;
import com.onloupe.core.CommonCentralLogic;


/**
 * A basic class to determine the source of a log message and act as an
 * IMessageSourceProvider.
 * 
 * This class knows how to acquire information about the source of a log message
 * from the current call stack, and acts as a IMessageSourceProvider to use when
 * handing off a log message to the central Log. Thus, this object must be
 * created while still within the same call stack as the origination of the log
 * message. Used internally by our Log.EndFile() method and ExceptionListener
 * (etc).
 */
public class MessageSourceProvider implements IMessageSourceProvider {
	
	/** The method name. */
	private String methodName;
	
	/** The class name. */
	private String className;
	
	/** The file name. */
	private String fileName;
	
	/** The line number. */
	private int lineNumber;

	/**
	 * Parameterless constructor for derived classes.
	 */
	public MessageSourceProvider() {
		this.methodName = null;
		this.className = null;
		this.fileName = null;
		this.lineNumber = 0;
	}
	
	/**
	 * Instantiates a new message source provider.
	 *
	 * @param element the element
	 */
	public MessageSourceProvider(StackTraceElement element) {
		// protecting ourselves here.
		if (element != null) {
			this.methodName = element.getMethodName();
			this.className = element.getClassName();
			this.fileName = element.getFileName();
			this.lineNumber = element.getLineNumber();
		}
	}	

	/**
	 * Creates a MessageSourceProvider object to be used as an
	 * IMessageSourceProvider.
	 * 
	 * @param className  The full name of the class (with namespace) whose method
	 *                   issued the log message.
	 * @param methodName The simple name of the method which issued the log message.
	 *                   This constructor is used only for the convenience of the
	 *                   Log class when it needs to generate an
	 *                   IMessageSoruceProvider for construction of
	 *                   internally-generated packets without going through the
	 *                   usual direct PublishToLog() mechanism.
	 */
	public MessageSourceProvider(String className, String methodName) {
		this.methodName = methodName;
		this.className = className;
		this.fileName = null;
		this.lineNumber = 0;
	}

	/**
	 * Creates a MessageSourceProvider object to be used as an
	 * IMessageSourceProvider.
	 * 
	 * @param className  The full name of the class (with namespace) whose method
	 *                   issued the log message.
	 * @param methodName The simple name of the method which issued the log message.
	 * @param fileName   The name of the file containing the method which issued the
	 *                   log message.
	 * @param lineNumber The line within the file at which the log message was
	 *                   issued. This constructor is used only for the convenience
	 *                   of the Log class when it needs to generate an
	 *                   IMessageSoruceProvider for construction of
	 *                   internally-generated packets without going through the
	 *                   usual direct PublishToLog() mechanism.
	 */
	public MessageSourceProvider(String className, String methodName, String fileName, int lineNumber) {
		this.methodName = methodName;
		this.className = className;
		this.fileName = fileName;
		this.lineNumber = lineNumber;
	}

	/**
	 * Creates a MessageSourceProvider object to be used as an
	 * IMessageSourceProvider.
	 * 
	 * This constructor is used only for the convenience of the Log class when it
	 * needs to generate an IMessageSoruceProvider for construction of
	 * internally-generated packets without going through the usual direct
	 * PublishToLog() mechanism.
	 *
	 * @param skipFrames  The number of stack frames to skip over to find the first
	 *                    candidate to be identified as the source of the log
	 *                    message.
	 */
	public MessageSourceProvider(int skipFrames) {
		MessageSourceProvider provider = CommonCentralLogic.findMessageSource(skipFrames + 1, null, null);

		this.lineNumber = provider.getLineNumber();
		this.fileName = provider.getFileName();
		this.methodName = provider.getMethodName();
		this.className = provider.getClassName();
	}

	/**
	 * Instantiates a new message source provider.
	 *
	 * @param skipFrames the skip frames
	 * @param throwable the throwable
	 */
	public MessageSourceProvider(int skipFrames, Throwable throwable) {
		MessageSourceProvider provider = CommonCentralLogic.findMessageSource(skipFrames + 1, throwable);

		this.lineNumber = provider.getLineNumber();
		this.fileName = provider.getFileName();
		this.methodName = provider.getMethodName();
		this.className = provider.getClassName();
	}
	
	/**
	 * Instantiates a new message source provider.
	 *
	 * @param skipFrames the skip frames
	 * @param exclusions the exclusions
	 */
	public MessageSourceProvider(int skipFrames, Set<String> exclusions) {
		MessageSourceProvider provider = CommonCentralLogic.findMessageSource(skipFrames + 1, exclusions);

		this.lineNumber = provider.getLineNumber();
		this.fileName = provider.getFileName();
		this.methodName = provider.getMethodName();
		this.className = provider.getClassName();
	}
	
	/**
	 * Instantiates a new message source provider.
	 *
	 * @param skipFrames the skip frames
	 * @param throwable the throwable
	 * @param exclusions the exclusions
	 */
	public MessageSourceProvider(int skipFrames, Throwable throwable, Set<String> exclusions) {
		MessageSourceProvider provider = CommonCentralLogic.findMessageSource(skipFrames + 1, throwable, exclusions);

		this.lineNumber = provider.getLineNumber();
		this.fileName = provider.getFileName();
		this.methodName = provider.getMethodName();
		this.className = provider.getClassName();
	}
	
	/**
	 * The simple name of the method which issued the log message.
	 *
	 * @return the method name
	 */
	@Override
	public final String getMethodName() {
		return this.methodName;
	}

	/**
	 * The full name of the class (with namespace) whose method issued the log
	 * message.
	 *
	 * @return the class name
	 */
	@Override
	public final String getClassName() {
		return this.className;
	}

	/**
	 * The name of the file containing the method which issued the log message.
	 *
	 * @return the file name
	 */
	@Override
	public final String getFileName() {
		return this.fileName;
	}

	/**
	 * The line within the file at which the log message was issued.
	 *
	 * @return the line number
	 */
	@Override
	public final int getLineNumber() {
		return this.lineNumber;
	}
}