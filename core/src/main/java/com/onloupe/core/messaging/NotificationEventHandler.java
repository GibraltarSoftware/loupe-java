package com.onloupe.core.messaging;


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