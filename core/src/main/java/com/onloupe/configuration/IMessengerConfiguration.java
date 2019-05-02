package com.onloupe.configuration;

/**
 * Minimal configuration information for each messenger.
 */
public interface IMessengerConfiguration {

	/**
	 * When true, the messenger will treat all write requests as write-through
	 * requests.
	 * 
	 * This overrides the write through request flag for all published requests,
	 * acting as if they are set true. This will slow down logging and change the
	 * degree of parallelism of multithreaded applications since each log message
	 * will block until it is committed to every configured messenger.
	 */
	boolean getForceSynchronous();

	void setForceSynchronous(boolean value);

	/**
	 * The maximum number of queued messages waiting to be processed by the
	 * messenger
	 * 
	 * Once the total number of messages waiting to be processed exceeds the maximum
	 * queue length the messenger will switch to a synchronous mode to catch up.
	 * This will not cause the client to experience synchronous logging behavior
	 * unless the publisher queue is also filled.
	 */
	int getMaxQueueLength();

	void setMaxQueueLength(int value);

	/**
	 * When false, the messenger is disabled even if otherwise configured.
	 * 
	 * This allows for explicit disable/enable without removing the existing
	 * configuration or worrying about the default configuration.
	 */
	boolean getEnabled();

	void setEnabled(boolean value);
}