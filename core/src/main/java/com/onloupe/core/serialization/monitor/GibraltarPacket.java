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

public abstract class GibraltarPacket implements IMessengerPacket {
	private long sequence;
	private OffsetDateTime timeStamp;

	/**
	 * The increasing sequence number of all packets for this session to be used as
	 * an absolute order sort.
	 */
	@Override
	public final long getSequence() {
		return this.sequence;
	}

	@Override
	public final void setSequence(long value) {
		this.sequence = value;
	}

	@Override
	public final OffsetDateTime getTimestamp() {
		return this.timeStamp;
	}

	@Override
	public final void setTimestamp(OffsetDateTime value) {
		this.timeStamp = value;
	}

	/**
	 * Indicates whether the current object is equal to another object of the same
	 * type.
	 * 
	 * @return true if the current object is equal to the <paramref name="other" />
	 *         parameter; otherwise, false.
	 * 
	 * @param other An object to compare with this object.
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
	 * @return true if the current object is equal to the <paramref name="other" />
	 *         parameter; otherwise, false.
	 * 
	 * @param other An object to compare with this object.
	 */
	public final boolean equals(GibraltarPacket other) {
		// Careful - can be null
		if (other == null) {
			return false; // since we're a live object we can't be equal.
		}

		return ((getSequence() == other.getSequence()) && getTimestamp().equals(other.getTimestamp()));
	}

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

	@Override
	public void writePacketDefinition(PacketDefinition definition) {
		definition.setVersion(VERSION);
		definition.getFields().add("Sequence", (new Long(this.sequence)).getClass());
		definition.getFields().add("TimeStamp", this.timeStamp.getClass());
	}

	@Override
	public void writeFields(PacketDefinition definition, SerializedPacket packet) {
		packet.setField("Sequence", this.sequence);
		packet.setField("TimeStamp", this.timeStamp);
	}

	@Override
	public void readFields(PacketDefinition definition, SerializedPacket packet) {
		throw new UnsupportedOperationException("Deserialization of agent data is not supported");
	}

	protected void readFieldsFast(IFieldReader reader) throws IOException {
		this.sequence = reader.readLong();
		this.timeStamp = reader.readDateTimeOffset();
	}
}