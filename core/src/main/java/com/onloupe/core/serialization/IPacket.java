package com.onloupe.core.serialization;

import java.util.List;


/**
 * This is the key interface objects implement to be serializable by Gibraltar.
 * 
 * 
 * To properly implement IPacket the class should also provide a default
 * constructor. This is needed to be compatible with the SimplePacketFactory
 * implementation of IPacketFactory.
 * 
 */
public interface IPacket {
	/**
	 * The list of packets that this packet depends on.
	 * 
	 * @return An array of IPackets, or null if there are no dependencies.
	 */
	List<IPacket> getRequiredPackets();

	/**
	 * Write the field definitions to the packet definition provided.
	 *
	 * @param blankDefinition the blank definition
	 */
	void writePacketDefinition(PacketDefinition blankDefinition);

	/**
	 * Write out all of the fields for the current packet.
	 *
	 * @param definition the definition
	 * @param packet     The serialized packet to populate with data
	 */
	void writeFields(PacketDefinition definition, SerializedPacket packet);

	/**
	 * Read back the field values for the current packet.
	 * 
	 * @param definition The definition that was used to persist the packet.
	 * @param packet     The serialized packet to read data from
	 */
	void readFields(PacketDefinition definition, SerializedPacket packet);
}