package com.onloupe.core.serialization.monitor;

import com.onloupe.core.messaging.IMessengerPacket;
import com.onloupe.core.serialization.IFieldReader;
import com.onloupe.core.serialization.IPacket;
import com.onloupe.core.serialization.PacketDefinition;
import com.onloupe.core.serialization.SerializedPacket;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class GibraltarPacket.
 */
public abstract class GibraltarPacket implements IMessengerPacket {
	
	/** The sequence. */
	private long sequence;
	
	/** The time stamp. */
	private OffsetDateTime timeStamp;

	/**
	 * The increasing sequence number of all packets for this session to be used as
	 * an absolute order sort.
	 *
	 * @return the sequence
	 */
	@Override
	public final long getSequence() {
		return this.sequence;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.messaging.IMessengerPacket#setSequence(long)
	 */
	@Override
	public final void setSequence(long value) {
		this.sequence = value;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.messaging.IMessengerPacket#getTimestamp()
	 */
	@Override
	public final OffsetDateTime getTimestamp() {
		return this.timeStamp;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.messaging.IMessengerPacket#setTimestamp(java.time.OffsetDateTime)
	 */
	@Override
	public final void setTimestamp(OffsetDateTime value) {
		this.timeStamp = value;
	}

	/**
	 * Indicates whether the current object is equal to another object of the same
	 * type.
	 *
	 * @param other An object to compare with this object.
	 * @return true if the current object is equal to the <paramref name="other" />
	 *         parameter; otherwise, false.
	 */
	@Override
	public boolean equals(Object other) {
		// use our type-specific override
		return equals(other instanceof GibraltarPacket ? (GibraltarPacket) other : null);
	}

	/**
	 * Indicates whether the current object is equal to another object of the same
	 * type.
	 *
	 * @param other An object to compare with this object.
	 * @return true if the current object is equal to the <paramref name="other" />
	 *         parameter; otherwise, false.
	 */
	public final boolean equals(GibraltarPacket other) {
		// Careful - can be null
		if (other == null) {
			return false; // since we're a live object we can't be equal.
		}

		return ((getSequence() == other.getSequence()) && getTimestamp().equals(other.getTimestamp()));
	}

	/** The Constant VERSION. */
	private static final int VERSION = 1;

	/**
	 * The list of packets that this packet depends on.
	 * 
	 * @return An array of IPackets, or null if there are no dependencies.
	 */
	@Override
	public List<IPacket> getRequiredPackets() {
		// we're a base and depend on nothing.
		return new ArrayList<>();
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.IPacket#writePacketDefinition(com.onloupe.core.serialization.PacketDefinition)
	 */
	@Override
	public void writePacketDefinition(PacketDefinition definition) {
		definition.setVersion(VERSION);
		definition.getFields().add("Sequence", (new Long(this.sequence)).getClass());
		definition.getFields().add("TimeStamp", this.timeStamp.getClass());
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.IPacket#writeFields(com.onloupe.core.serialization.PacketDefinition, com.onloupe.core.serialization.SerializedPacket)
	 */
	@Override
	public void writeFields(PacketDefinition definition, SerializedPacket packet) {
		packet.setField("Sequence", this.sequence);
		packet.setField("TimeStamp", this.timeStamp);
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.IPacket#readFields(com.onloupe.core.serialization.PacketDefinition, com.onloupe.core.serialization.SerializedPacket)
	 */
	@Override
	public void readFields(PacketDefinition definition, SerializedPacket packet) {
		throw new UnsupportedOperationException("Deserialization of agent data is not supported");
	}

	/**
	 * Read fields fast.
	 *
	 * @param reader the reader
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected void readFieldsFast(IFieldReader reader) throws IOException {
		this.sequence = reader.readLong();
		this.timeStamp = reader.readDateTimeOffset();
	}
}