package com.onloupe.core.messaging;

// TODO: Auto-generated Javadoc
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