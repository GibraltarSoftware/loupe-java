package com.onloupe.core.server;

/**
 * Event handler for the connection state changed event
 * 
 * @param sender
 * @param e
 */
@FunctionalInterface
public interface ChannelConnectionStateChangedEventHandler {
	void invoke(Object sender, ChannelConnectionStateChangedEventArgs e);
}