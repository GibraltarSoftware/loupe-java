package com.onloupe.core.serialization;

import com.onloupe.core.util.TimeConversion;
import com.onloupe.model.exception.GibraltarPacketVersionException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// This is modeled on LogPacket, but modified to contain a subpacket of SubPacket
public class RootPacket implements IPacket {
	private LocalDateTime _TimeStamp = TimeConversion.MIN.toLocalDateTime();
	private long _ThreadId;
	private String _Caption;
	private SubPacket _SubPacket;

	public RootPacket(String caption) {
		this._TimeStamp = LocalDateTime.now(); // we convert to UTC during serialization, we want local time.
		this._ThreadId = Thread.currentThread().getId();
		this._Caption = caption;
		this._SubPacket = new SubPacket(caption, (int) this._ThreadId);
	}

	public RootPacket() {
	}

	public final LocalDateTime getTimeStamp() {
		return this._TimeStamp;
	}

	public final long getThreadId() {
		return this._ThreadId;
	}

	public final String getCaption() {
		return this._Caption;
	}

	public final SubPacket getSubPacket() {
		return this._SubPacket;
	}

	/**
	 * The list of packets that this packet depends on.
	 * 
	 * @return An array of IPackets, or null if there are no dependencies.
	 */
	@Override
	public final List<IPacket> getRequiredPackets() {
		// We're a base packet and depend on nothing.
		return new ArrayList<>();
	}

	@Override
	public void writePacketDefinition(PacketDefinition definition) {
		definition.setVersion(1);
		definition.getFields().add("timestamp", FieldType.DATE_TIME);
		definition.getFields().add("threadId", FieldType.LONG);
		definition.getFields().add("caption", FieldType.STRING);

		PacketDefinition subPacket = new PacketDefinition(this._SubPacket.getClass().getSimpleName());
		((IPacket) this._SubPacket).writePacketDefinition(subPacket);
		definition.getSubPackets().add(subPacket);
	}

	/**
	 * Write out all of the fields for the current packet
	 * 
	 * @param definition The definition that was used to persist the packet.
	 * @param packet     The serialized packet to populate with data
	 */
	@Override
	public final void writeFields(PacketDefinition definition, SerializedPacket packet) {
		packet.setField("timestamp", this._TimeStamp);
		packet.setField("threadId", this._ThreadId);
		packet.setField("caption", this._Caption);
		((IPacket) this._SubPacket).writeFields(definition, packet);
	}

	/**
	 * Read back the field values for the current packet.
	 * 
	 * @param definition The definition that was used to persist the packet.
	 * @param packet     The serialized packet to read data from
	 */
	@Override
	public final void readFields(PacketDefinition definition, SerializedPacket packet) {
		switch (definition.getVersion()) {
		case 1:
			this._TimeStamp = packet.getField("timestamp", LocalDateTime.class);
			this._ThreadId = packet.getField("threadId", Integer.class);
			this._Caption = packet.getField("caption", String.class);
			this._SubPacket = new SubPacket(); // Need a valid but empty SubPacket to read into
			((IPacket) this._SubPacket).readFields(definition.getSubPackets().get(0), packet);
			break;
		default:
			throw new GibraltarPacketVersionException(definition.getVersion());
		}
	}

	@Override
	public boolean equals(Object obj) {
		return equals(obj instanceof RootPacket ? (RootPacket) obj : null);
	}

	public final boolean equals(RootPacket other) {
		// Careful - can be null
		if (other == null) {
			return false; // since we're a live object we can't be equal.
		}

		if (!this._TimeStamp.equals(other._TimeStamp)) {
			return false;
		}
		if (this._ThreadId != other._ThreadId) {
			return false;
		}
		if (!this._Caption.equals(other._Caption)) {
			return false;
		}
		if (!this._SubPacket.equals(other._SubPacket)) {
			return false;
		}
		return true;
	}

	/**
	 * Provides a representative hash code for objects of this type to spread out
	 * distribution in hash tables.
	 * 
	 * Objects which consider themselves to be Equal (a.Equals(b) returns true) are
	 * expected to have the same hash code. Objects which are not Equal may have the
	 * same hash code, but minimizing such overlaps helps with efficient operation
	 * of hash tables.
	 * 
	 * @return an int representing the hash code calculated for the contents of this
	 *         object
	 * 
	 */
	@Override
	public int hashCode() {
		int myHash = (int) this._ThreadId; // Fold in thread ID as a hash code for itself

		myHash ^= this._TimeStamp.hashCode(); // Fold in hash code for DateTime timestamp
		if (this._Caption != null) {
			myHash ^= this._Caption.hashCode(); // Fold in hash code for string caption
		}
		if (this._SubPacket != null) {
			myHash ^= this._SubPacket.hashCode(); // Fold in hash code for subpacket member
		}

		return myHash;
	}
}