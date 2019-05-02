package com.onloupe.core.messaging;

/**
 * Standard event handler delegate for the LocalSErverDiscoveryFile Event
 * arguments
 * 
 * @param sender
 * @param e
 */
@FunctionalInterface
public interface LocalServerDiscoveryFileEventHandler {
	void invoke(Object sender, LocalServerDiscoveryFileEventArgs e);
}