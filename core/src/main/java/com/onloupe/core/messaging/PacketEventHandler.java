package com.onloupe.core.messaging;


/**
 * The Interface PacketEventHandler.
 */
@FunctionalInterface
public interface PacketEventHandler {
	
	/**
	 * Invoke.
	 *
	 * @param sender the sender
	 * @param e the e
	 */
	void invoke(Object sender, PacketEventArgs e);
}