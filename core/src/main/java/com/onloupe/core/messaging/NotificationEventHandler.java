package com.onloupe.core.messaging;

// TODO: Auto-generated Javadoc
/**
 * Handler type for a notification event.
 * 
 */
@FunctionalInterface
public interface NotificationEventHandler {
	
	/**
	 * Invoke.
	 *
	 * @param sender the sender
	 * @param e the e
	 */
	void invoke(Object sender, NotificationEventArgs e);
}