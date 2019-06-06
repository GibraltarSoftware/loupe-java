package com.onloupe.core.serialization;

import java.io.IOException;


/**
 * Implemented to support writing packets
 * 
 * Having everything use an interface allows us to support NMOCK.
 */
public interface IPacketWriter {
	
	/**
	 * Write the data needed to serialize the state of the packet.
	 *
	 * @param packet Object to be serialized, must implement IPacket
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	void write(IPacket packet) throws IOException, Exception;
}