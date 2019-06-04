package com.onloupe.core.messaging;

import com.onloupe.core.serialization.ICachedPacket;

// TODO: Auto-generated Javadoc
/**
 * This interface is required to be a publishable cached packet.
 */
public interface ICachedMessengerPacket extends ICachedPacket, IMessengerPacket {
	// we're primarily here really as a way of associating a messenger packet with
	// ICachedPacket in one blow.

	/**
	 * Indicates if this packet is part of the session header and should be
	 * presented with other header packets.
	 *
	 * @return true, if is header
	 */
	boolean isHeader();
}