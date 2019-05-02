package com.onloupe.core.serialization;

import java.io.IOException;

/**
 * Helper class used by PacketFactory to wrapper the creation of GenericPacket
 */
public class GenericPacketFactory implements IPacketFactory {
	@Override
	public final IPacket createPacket(PacketDefinition definition, IFieldReader reader) throws IOException {
		GenericPacket packet = new GenericPacket(definition, reader);
		return packet;
	}
}