package com.onloupe.core.serialization;

import java.util.UUID;

/**
 * Implemented on invariant packets that can be cached
 * 
 * This interface extends IPacket to handle packets that are referenced by
 * multiple packets and should only be serialized once.
 * 
 */
public interface ICachedPacket extends IPacket {
	/**
	 * The unique Id of the packet
	 */
	UUID getID();
}