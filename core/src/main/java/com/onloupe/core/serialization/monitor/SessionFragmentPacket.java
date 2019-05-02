package com.onloupe.core.serialization.monitor;

import com.onloupe.core.serialization.FieldType;
import com.onloupe.core.serialization.IPacket;
import com.onloupe.core.serialization.PacketDefinition;
import com.onloupe.core.serialization.SerializedPacket;
import com.onloupe.core.util.TimeConversion;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class SessionFragmentPacket extends GibraltarCachedPacket implements IPacket {
	private OffsetDateTime fileStartDateTime;
	private OffsetDateTime fileEndDateTime;
	private boolean lastFile;

	public final OffsetDateTime getFileStartDateTime() {
		return this.fileStartDateTime;
	}

	public final void setFileStartDateTime(OffsetDateTime value) {
		this.fileStartDateTime = value;
		if (getTimestamp().equals(TimeConversion.MIN)) {
			setTimestamp(value);
		}
	}

	public final OffsetDateTime getFileEndDateTime() {
		return this.fileEndDateTime;
	}

	public final void setFileEndDateTime(OffsetDateTime value) {
		this.fileEndDateTime = value;
	}

	public final boolean isLastFile() {
		return this.lastFile;
	}

	public final void setIsLastFile(boolean value) {
		this.lastFile = value;
	}

	/**
	 * Create a new session file packet for the provided FileID.
	 * 
	 * @param _FileID
	 */
	public SessionFragmentPacket(UUID _FileID) {
		super(_FileID, true);
	}

	/**
	 * Used during rehydration
	 */
	public SessionFragmentPacket() {
		super(false);
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
		return equals(other instanceof SessionSummaryPacket ? (SessionSummaryPacket) other : null);
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
	public final boolean equals(SessionFragmentPacket other) {
		// Careful - can be null
		if (other == null) {
			return false; // since we're a live object we can't be equal.
		}

		return (getFileStartDateTime().equals(other.getFileStartDateTime())
				&& getFileEndDateTime().equals(other.getFileEndDateTime()) && (isLastFile() == other.isLastFile())
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

		myHash ^= this.fileStartDateTime.hashCode(); // Fold in hash code for DateTimeOffset member start time
		myHash ^= this.fileEndDateTime.hashCode(); // Fold in hash code for DateTimeOffset member end time

		// Not bothering with bool member IsLastFile

		return myHash;
	}

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

	/**
	 * Populate a definition for this packet.
	 * 
	 */
	@Override
	public void writePacketDefinition(PacketDefinition definition) {
		super.writePacketDefinition(definition.getParentIPacket());

		definition.setVersion(SERIALIZATION_VERSION);

		definition.getFields().add("FileStartDateTime", FieldType.DATE_TIME_OFFSET);
		definition.getFields().add("FileEndDateTime", FieldType.DATE_TIME_OFFSET);
		definition.getFields().add("IsLastFile", FieldType.BOOL);
	}

	/**
	 * Write out all of the fields for the current packet
	 * 
	 * @param definition The definition that was used to perisist the packet.
	 * @param packet     The serialized packet to populate with data
	 */
	@Override
	public final void writeFields(PacketDefinition definition, SerializedPacket packet) {
		super.writeFields(definition.getParentIPacket(), packet.getParentIPacket());

		packet.setField("FileStartDateTime", this.fileStartDateTime);
		packet.setField("FileEndDateTime", this.fileEndDateTime);
		packet.setField("IsLastFile", this.lastFile);
	}

	/**
	 * Read back the field values for the current packet.
	 * 
	 * @param definition The definition that was used to perisist the packet.
	 * @param packet     The serialized packet to read data from
	 */
	@Override
	public final void readFields(PacketDefinition definition, SerializedPacket packet) {
		throw new UnsupportedOperationException("Deserialization of agent data is not supported");
	}
}