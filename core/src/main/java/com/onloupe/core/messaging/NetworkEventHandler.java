package com.onloupe.core.messaging;


/**
 * Delegate for handling NetworkWriter events.
 */
@FunctionalInterface
public interface NetworkEventHandler {
	
	/**
	 * Invoke.
	 *
	 * @param sender the sender
	 * @param e the e
	 */
	void invoke(Object sender, NetworkEventArgs e);
}