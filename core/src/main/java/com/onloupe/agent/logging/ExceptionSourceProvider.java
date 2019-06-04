package com.onloupe.agent.logging;

import com.onloupe.agent.IMessageSourceProvider;
import com.onloupe.core.CommonCentralLogic;

// TODO: Auto-generated Javadoc
/**
 * Serves as an IMessageSourceProvider to attribute a message to the code
 * location which threw an Exception.
 * 
 * This class looks at the
 * <see CREF="System.Diagnostics.StackTrace">StackTrace</see> of a thrown
 * Exception, rather than the current call stack, to attribute a message to the
 * code location which threw that Exception rather than to where the call is
 * made to log the Exception.
 */
public class ExceptionSourceProvider implements IMessageSourceProvider {
	
	/** The method name. */
	private String methodName;
	
	/** The class name. */
	private String className;
	
	/** The file name. */
	private String fileName;
	
	/** The line number. */
	private int lineNumber;

	/**
	 * Construct an ExceptionSourceProvider based on a provided Exception.
	 * 
	 * The first (closest) stack frame of the first (outer) Exception will be taken
	 * as the originator of a log message using this as its IMessageSourceProvider.
	 *
	 * @param throwable the throwable
	 */
	public ExceptionSourceProvider(Throwable throwable) {
		// We never skipped Gibraltar frames here, so go ahead with trustSkipFrames =
		// true to disable that check.
		MessageSourceProvider provider = CommonCentralLogic.findMessageSource(0, throwable);
		this.methodName = provider.getMethodName();
		this.className = provider.getClassName();
		this.fileName = provider.getFileName();
		this.lineNumber = provider.getLineNumber();
	}

	/**
	 * Instantiates a new exception source provider.
	 *
	 * @param _MethodName the method name
	 * @param _ClassName the class name
	 * @param _FileName the file name
	 * @param _LineNumber the line number
	 */
	public ExceptionSourceProvider(String _MethodName, String _ClassName, String _FileName, int _LineNumber) {
		super();
		this.methodName = _MethodName;
		this.className = _ClassName;
		this.fileName = _FileName;
		this.lineNumber = _LineNumber;
	}

	/**
	 * Should return the simple name of the method which issued the log message.
	 *
	 * @return the method name
	 */
	@Override
	public final String getMethodName() {
		return this.methodName;
	}

	/**
	 * Should return the full name of the class (with namespace) whose method issued
	 * the log message.
	 *
	 * @return the class name
	 */
	@Override
	public final String getClassName() {
		return this.className;
	}

	/**
	 * Should return the name of the file containing the method which issued the log
	 * message.
	 *
	 * @return the file name
	 */
	@Override
	public final String getFileName() {
		return this.fileName;
	}

	/**
	 * Should return the line within the file at which the log message was issued.
	 *
	 * @return the line number
	 */
	@Override
	public final int getLineNumber() {
		return this.lineNumber;
	}

}