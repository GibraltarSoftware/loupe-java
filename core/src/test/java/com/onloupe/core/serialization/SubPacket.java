package com.onloupe.core.serialization;

import com.onloupe.model.exception.GibraltarPacketVersionException;

import java.util.ArrayList;
import java.util.List;

// This is modeled on BasePacket (may thus be redundant, but used for clarity)
public class SubPacket extends BaseObject implements IPacket {
	private String text;

	public SubPacket() {
	}

	public SubPacket(String text, int id) {
		super(id);
		this.text = text;
	}

	public final String getText() {
		return this.text;
	}

	public final void setText(String value) {
		this.text = value;
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
		definition.getFields().add("text", FieldType.STRING);
	}

	/**
	 * Write out all of the fields for the current packet
	 * 
	 * @param definition The definition that was used to persist the packet.
	 * @param packet     The serialized packet to populate with data
	 */
	@Override
	public final void writeFields(PacketDefinition definition, SerializedPacket packet) {
		packet.setField("text", this.text);
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
			this.text = packet.getField("text", String.class);
			break;
		default:
			throw new GibraltarPacketVersionException(definition.getVersion());
		}
	}


	@Override
	public boolean equals(Object obj) {
		SubPacket other = obj instanceof SubPacket ? (SubPacket) obj : null;
		if (other == null) {
			return false;
		}

		if (!super.equals(obj)) {
			return false;
		}

		return other.text.equals(this.text);
	}

	public final boolean equals(SubPacket other) {
		// Careful - can be null
		if (other == null) {
			return false; // since we're a live object we can't be equal.
		}

		if (!super.equals(other)) {
			return false;
		}
		if (!this.text.equals(other.text)) {
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
		int myHash = super.hashCode(); // Fold in hash code for inherited BaseObject

		if (this.text != null) {
			myHash ^= this.text.hashCode(); // Fold in hash code for string text member
		}

		return myHash;
	}
}