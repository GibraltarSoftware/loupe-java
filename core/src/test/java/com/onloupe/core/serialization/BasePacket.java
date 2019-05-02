package com.onloupe.core.serialization;

import com.onloupe.model.exception.GibraltarPacketVersionException;

import java.util.ArrayList;
import java.util.List;

public class BasePacket extends BaseObject implements IPacket {
	private String text;

	public BasePacket() {
	}

	public BasePacket(String text, int id) {
		super(id);
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	public void setText(String value) {
		this.text = value;
	}

	/**
	 * The list of packets that this packet depends on.
	 * 
	 * @return An array of IPackets, or null if there are no dependencies.
	 */
	@Override
	public List<IPacket> getRequiredPackets() {
		// We're a base packet and depend on nothing.
		return new ArrayList<>();
	}

	@Override
	public void writePacketDefinition(PacketDefinition definition) {
		//we're the lowest level.
		definition.setVersion(1);
		definition.getFields().add("ID", FieldType.INT);
		definition.getFields().add("text", FieldType.STRING);
	}

	/**
	 * Write out all of the fields for the current packet
	 * 
	 * @param definition The definition that was used to persist the packet.
	 * @param packet     The serialized packet to populate with data
	 */
	@Override
	public void writeFields(PacketDefinition definition, SerializedPacket packet) {
		packet.setField("ID", getId());
		packet.setField("text", this.text);
	}

	/**
	 * Read back the field values for the current packet.
	 * 
	 * @param definition The definition that was used to persist the packet.
	 * @param packet     The serialized packet to read data from
	 */
	@Override
	public void readFields(PacketDefinition definition, SerializedPacket packet) {
		switch (definition.getVersion()) {
		case 1:
			setId(packet.getField("ID", Integer.class));
			this.text = packet.getField("text", String.class);
			break;
		default:
			throw new GibraltarPacketVersionException(definition.getVersion());
		}
	}

	@Override
	public boolean equals(Object obj) {
		BasePacket other = obj instanceof BasePacket ? (BasePacket) obj : null;
		if (other == null) {
			return false;
		}

		if (!super.equals(obj)) {
			return false;
		}

		return other.text.equals(this.text);
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