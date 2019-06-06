package com.onloupe.core.serialization.monitor;

import com.onloupe.core.messaging.ICachedMessengerPacket;
import com.onloupe.core.serialization.FieldType;
import com.onloupe.core.serialization.IPacket;
import com.onloupe.core.serialization.PacketDefinition;
import com.onloupe.core.serialization.SerializedPacket;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * The Class GibraltarCachedPacket.
 */
public abstract class GibraltarCachedPacket implements ICachedMessengerPacket {
	
	/** The sequence. */
	private long sequence;
	
	/** The time stamp. */
	private OffsetDateTime timeStamp;
	
	/** The id. */
	private UUID id;
	
	/** The is header. */
	private boolean isHeader;

	/**
	 * Instantiates a new gibraltar cached packet.
	 *
	 * @param packetID the packet ID
	 * @param isHeader the is header
	 */
	protected GibraltarCachedPacket(UUID packetID, boolean isHeader) {
		setID(packetID);
		this.isHeader = isHeader;
	}

	/**
	 * Instantiates a new gibraltar cached packet.
	 *
	 * @param isHeader the is header
	 */
	protected GibraltarCachedPacket(boolean isHeader) {
		setID(UUID.randomUUID());
		this.isHeader = isHeader;
	}

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

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.ICachedPacket#getID()
	 */
	@Override
	public final UUID getID() {
		return this.id;
	}

	/**
	 * Sets the id.
	 *
	 * @param value the new id
	 */
	public final void setID(UUID value) {
		this.id = value;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.messaging.ICachedMessengerPacket#isHeader()
	 */
	@Override
	public final boolean isHeader() {
		return this.isHeader;
	}

	/**
	 * Indicates whether the current object is equal to another object of the same
	 * type.
	 *
	 * @param other An object to compare with this object.
	 * @return true if the current object is equal to the 
	 *         parameter; otherwise, false.
	 */
	@Override
	public boolean equals(Object other) {
		// use our type-specific override
		return equals(other instanceof GibraltarCachedPacket ? (GibraltarCachedPacket) other : null);
	}

	/**
	 * Indicates whether the current object is equal to another object of the same
	 * type.
	 *
	 * @param other An object to compare with this object.
	 * @return true if the current object is equal to the 
	 *         parameter; otherwise, false.
	 */
	public final boolean equals(GibraltarCachedPacket other) {
		// Careful - can be null
		if (other == null) {
			return false; // since we're a live object we can't be equal.
		}

		return ((getSequence() == other.getSequence()) && getTimestamp().equals(other.getTimestamp())
				&& (getID().equals(other.getID())));
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
		// We're a base and depend on nothing.
		return new ArrayList<>();
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.IPacket#writePacketDefinition(com.onloupe.core.serialization.PacketDefinition)
	 */
	@Override
	public void writePacketDefinition(PacketDefinition definition) {
		definition.setVersion(VERSION);
		definition.getFields().add("Sequence", FieldType.LONG);
		definition.getFields().add("TimeStamp", FieldType.DATE_TIME_OFFSET);
		definition.getFields().add("ID", FieldType.GUID);
		definition.getFields().add("IsHeader", FieldType.BOOL);
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.IPacket#writeFields(com.onloupe.core.serialization.PacketDefinition, com.onloupe.core.serialization.SerializedPacket)
	 */
	@Override
	public void writeFields(PacketDefinition definition, SerializedPacket packet) {
		assert this.timeStamp.getYear() > 1; // watch for timestamp being some variation of zero

		packet.setField("Sequence", this.sequence);
		packet.setField("TimeStamp", this.timeStamp);
		packet.setField("ID", this.id);
		packet.setField("IsHeader", this.isHeader);
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.IPacket#readFields(com.onloupe.core.serialization.PacketDefinition, com.onloupe.core.serialization.SerializedPacket)
	 */
	@Override
	public void readFields(PacketDefinition definition, SerializedPacket packet) {
		throw new UnsupportedOperationException("Deserialization of agent data is not supported");
	}
}