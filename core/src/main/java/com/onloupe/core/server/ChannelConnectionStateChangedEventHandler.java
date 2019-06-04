package com.onloupe.core.server;

// TODO: Auto-generated Javadoc
/**
 * Event handler for the connection state changed event.
 */
@FunctionalInterface
public interface ChannelConnectionStateChangedEventHandler {
	
	/**
	 * Invoke.
	 *
	 * @param sender the sender
	 * @param e the e
	 */
	void invoke(Object sender, ChannelConnectionStateChangedEventArgs e);
}