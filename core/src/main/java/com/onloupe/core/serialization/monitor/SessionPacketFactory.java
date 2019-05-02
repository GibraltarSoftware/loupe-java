package com.onloupe.core.serialization.monitor;

import com.onloupe.core.serialization.IFieldReader;
import com.onloupe.core.serialization.IPacket;
import com.onloupe.core.serialization.IPacketFactory;
import com.onloupe.core.serialization.IPacketReader;
import com.onloupe.core.serialization.PacketDefinition;

public class SessionPacketFactory implements IPacketFactory {
	private String sessionStartInfoPacketType;
	private String sessionEndInfoPacketType;
	private String sessionFilePacketType;
	private String threadInfoPacketType;

	public SessionPacketFactory() {
		// resolve the names of all the types we want to be able to get packets for
		// this lets us do a faster switch in CreatePacket
		this.sessionStartInfoPacketType = SessionSummaryPacket.class.getName();
		this.sessionEndInfoPacketType = SessionClosePacket.class.getName();
		this.sessionFilePacketType = SessionFragmentPacket.class.getName();
		this.threadInfoPacketType = ThreadInfoPacket.class.getName();
	}

	/**
	 * This is the method that is invoked on an IPacketFactory to create an IPacket
	 * from the data in an IFieldReader given a specified PacketDefinition.
	 * 
	 * @param definition Definition of the fields expected in the next packet
	 * @param reader     Data stream to be read
	 * @return An IPacket corresponding to the PacketDefinition and the stream data
	 */
	@Override
	public final IPacket createPacket(PacketDefinition definition, IFieldReader reader) {
		IPacket packet;

		// what we create varies by what specific definition they're looking for
		if (definition.getTypeName().equals(this.threadInfoPacketType)) {
			packet = new ThreadInfoPacket();
		} else if (definition.getTypeName().equals(this.sessionStartInfoPacketType)) {
			packet = new SessionSummaryPacket();
		} else if (definition.getTypeName().equals(this.sessionEndInfoPacketType)) {
			packet = new SessionClosePacket();
		} else if (definition.getTypeName().equals(this.sessionFilePacketType)) {
			packet = new SessionFragmentPacket();
		} else {
			// crap, we don't know what to do here.
			throw new IndexOutOfBoundsException(
					"This packet factory doesn't understand how to create packets for the provided type.");
		}

		// this feels a little crazy, but you have to do your own read call here - we
		// aren't just creating the packet
		// object, we actually have to make the standard call to have it read data...
		definition.readFields(packet, reader);

		return packet;
	}

	/**
	 * Register the packet factory with the packet reader for all packet types it
	 * supports
	 * 
	 * @param packetReader
	 */
	public final void register(IPacketReader packetReader) {
		packetReader.registerFactory(this.sessionStartInfoPacketType, this);
		packetReader.registerFactory(this.sessionEndInfoPacketType, this);
		packetReader.registerFactory(this.sessionFilePacketType, this);
		packetReader.registerFactory(this.threadInfoPacketType, this);
	}
}