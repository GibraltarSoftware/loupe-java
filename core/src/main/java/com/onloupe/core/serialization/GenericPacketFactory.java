package com.onloupe.core.serialization;

import java.io.IOException;

// TODO: Auto-generated Javadoc
/**
 * Helper class used by PacketFactory to wrapper the creation of GenericPacket.
 */
public class GenericPacketFactory implements IPacketFactory {
	
	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.IPacketFactory#createPacket(com.onloupe.core.serialization.PacketDefinition, com.onloupe.core.serialization.IFieldReader)
	 */
	@Override
	public final IPacket createPacket(PacketDefinition definition, IFieldReader reader) throws IOException {
		GenericPacket packet = new GenericPacket(definition, reader);
		return packet;
	}
}