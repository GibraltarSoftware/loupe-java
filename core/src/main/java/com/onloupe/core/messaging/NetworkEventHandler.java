package com.onloupe.core.messaging;

/**
 * Delegate for handling NetworkWriter events
 */
@FunctionalInterface
public interface NetworkEventHandler {
	void invoke(Object sender, NetworkEventArgs e);
}