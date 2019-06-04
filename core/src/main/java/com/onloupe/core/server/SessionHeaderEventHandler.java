package com.onloupe.core.server;

// TODO: Auto-generated Javadoc
/**
 * Delegate for handling session header events.
 */
@FunctionalInterface
public interface SessionHeaderEventHandler {
	
	/**
	 * Invoke.
	 *
	 * @param sender the sender
	 * @param e the e
	 */
	void invoke(Object sender, SessionHeaderEventArgs e);
}