package com.onloupe.core.messaging;


/**
 * Standard event handler delegate for the LocalSErverDiscoveryFile Event
 * arguments.
 */
@FunctionalInterface
public interface LocalServerDiscoveryFileEventHandler {
	
	/**
	 * Invoke.
	 *
	 * @param sender the sender
	 * @param e the e
	 */
	void invoke(Object sender, LocalServerDiscoveryFileEventArgs e);
}