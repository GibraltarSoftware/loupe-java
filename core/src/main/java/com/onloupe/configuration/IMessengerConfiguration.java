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
	 *
	 * @return the force synchronous
	 */
	boolean getForceSynchronous();

	/**
	 * Sets the force synchronous.
	 *
	 * @param value the new force synchronous
	 */
	void setForceSynchronous(boolean value);

	/**
	 * The maximum number of queued messages waiting to be processed by the
	 * messenger
	 * 
	 * Once the total number of messages waiting to be processed exceeds the maximum
	 * queue length the messenger will switch to a synchronous mode to catch up.
	 * This will not cause the client to experience synchronous logging behavior
	 * unless the publisher queue is also filled.
	 *
	 * @return the max queue length
	 */
	int getMaxQueueLength();

	/**
	 * Sets the max queue length.
	 *
	 * @param value the new max queue length
	 */
	void setMaxQueueLength(int value);

	/**
	 * When false, the messenger is disabled even if otherwise configured.
	 * 
	 * This allows for explicit disable/enable without removing the existing
	 * configuration or worrying about the default configuration.
	 *
	 * @return the enabled
	 */
	boolean getEnabled();

	/**
	 * Sets the enabled.
	 *
	 * @param value the new enabled
	 */
	void setEnabled(boolean value);
}