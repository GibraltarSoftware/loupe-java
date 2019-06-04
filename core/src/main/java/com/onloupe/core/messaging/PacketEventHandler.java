package com.onloupe.core.messaging;

// TODO: Auto-generated Javadoc
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