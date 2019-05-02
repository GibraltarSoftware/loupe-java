package com.onloupe.core.serialization.monitor;

import com.onloupe.core.serialization.FieldType;
import com.onloupe.core.serialization.IPacket;
import com.onloupe.core.serialization.PacketDefinition;
import com.onloupe.core.serialization.SerializedPacket;
import com.onloupe.model.session.SessionStatus;

import java.util.List;
import java.util.UUID;

/**
 * Marks the ending status of a session.
 */
public class SessionClosePacket extends GibraltarPacket implements IPacket, java.lang.Comparable<SessionClosePacket> {

	private UUID id;
	private SessionStatus endingStatus;

	public SessionClosePacket() {
		// we aren't a cacheable packet so we have our own GUID
		setId(UUID.randomUUID());
		setEndingStatus(SessionStatus.NORMAL);
	}

	public SessionClosePacket(SessionStatus endingStatus) {
		// we aren't a cacheable packet so we have our own GUID
		setId(UUID.randomUUID());
		setEndingStatus(endingStatus);
	}

	public final UUID getId() {
		return this.id;
	}

	private void setId(UUID value) {
		this.id = value;
	}

	public final SessionStatus getEndingStatus() {
		return this.endingStatus;
	}

	public final void setEndingStatus(SessionStatus value) {
		this.endingStatus = value;
	}

	@Override
	public String toString() {
		return String.format("Session Close: Status is %1$s", this.endingStatus);
	}

	@Override
	public final int compareTo(SessionClosePacket other) {
		// First do a quick match on Guid. this is the only case we want to return zero
		// (an exact match)
		if (getId().equals(other.getId())) {
			return 0;
		}

		// now we want to sort by our nice increasing sequence #
		int compareResult = Long.compare(getSequence(), other.getSequence());

		assert compareResult != 0; // no way we should ever get an equal at this point.

		return compareResult;
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
		return equals(other instanceof SessionClosePacket ? (SessionClosePacket) other : null);
	}

	/**
	 * Determines if the provided object is identical to this object.
	 * 
	 * @param other The object to compare this object to
	 * @return True if the objects represent the same data.
	 */
	public final boolean equals(SessionClosePacket other) {
		// Careful - can be null
		if (other == null) {
			return false; // since we're a live object we can't be equal.
		}

		return ((getId().equals(other.getId())) && (getEndingStatus() == other.getEndingStatus())
				&& (super.equals(other)));
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
		int myHash = super.hashCode(); // Fold in hash code for inherited base type

		myHash ^= getId().hashCode(); // Fold in hash code for GUID
		myHash ^= getEndingStatus().hashCode(); // Fold in hash code for EndingStatus enum

		return myHash;
	}

	/**
	 * The current serialization version
	 * 
	 * 
	 * <p>
	 * Version 1: Added Id and EndingStatus field to previously empty packet.
	 * </p>
	 * 
	 */
	private static final int SERIALIZATION_VERSION = 1;

	/**
	 * The list of packets that this packet depends on.
	 * 
	 * @return An array of IPackets, or null if there are no dependencies.
	 */
	@Override
	public final List<IPacket> getRequiredPackets() {
		return super.getRequiredPackets();
	}

	@Override
	public void writePacketDefinition(PacketDefinition definition) {
		super.writePacketDefinition(definition.getParentIPacket());

		definition.setVersion(SERIALIZATION_VERSION);

		definition.getFields().add("Id", FieldType.GUID);
		definition.getFields().add("Status", FieldType.INT);
	}

	@Override
	public final void writeFields(PacketDefinition definition, SerializedPacket packet) {
		super.writeFields(definition.getParentIPacket(), packet.getParentIPacket());

		packet.setField("Id", this.id);
		packet.setField("Status", this.endingStatus.getValue());
	}

	@Override
	public final void readFields(PacketDefinition definition, SerializedPacket packet) {
		throw new UnsupportedOperationException("Deserialization of agent data is not supported");
	}
}