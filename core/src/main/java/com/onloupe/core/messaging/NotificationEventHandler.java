package com.onloupe.core.messaging;

/**
 * Handler type for a notification event.
 * 
 * @param sender The sender of this notification event.
 * @param e      The NotificationEventArgs.
 */
@FunctionalInterface
public interface NotificationEventHandler {
	void invoke(Object sender, NotificationEventArgs e);
}