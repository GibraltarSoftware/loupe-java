package com.onloupe.core.serialization;

import com.onloupe.model.exception.GibraltarPacketVersionException;

import java.util.List;

public class DerivedPacket extends IntermediatePacket implements IPacket {
	private String text;

	public DerivedPacket() {
	}

	public DerivedPacket(String text, int id) {
		super("(" + text + ")", id);
		this.text = text;
	}

	public final String getText2() {
		return this.text;
	}

	public final void setText2(String value) {
		this.text = value;
	}

	@Override
	public boolean equals(Object obj) {
		DerivedPacket other = obj instanceof DerivedPacket ? (DerivedPacket) obj : null;
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
		int myHash = super.hashCode(); // Fold in hash code for inherited IntermediatePacket

		if (this.text != null) {
			myHash ^= this.text.hashCode(); // Fold in hash code for our string text member at this level
		}

		return myHash;
	}

	@Override
	public List<IPacket> getRequiredPackets() {
		return super.getRequiredPackets();
	}

	@Override
	public void writePacketDefinition(PacketDefinition definition) {
		super.writePacketDefinition(definition.getParentIPacket());

		definition.setVersion(2);
		definition.getFields().add("text", FieldType.STRING);
	}

	@Override
	public void writeFields(PacketDefinition definition, SerializedPacket packet) {
		super.writeFields(definition.getParentIPacket(), packet.getParentIPacket());

		packet.setField("text", this.text);
	}

	@Override
	public void readFields(PacketDefinition definition, SerializedPacket packet) {
		super.readFields(definition.getParentIPacket(), packet.getParentIPacket());

		switch (definition.getVersion()) {
		case 2:
			this.text = packet.getField("text", String.class);
			break;
		default:
			throw new GibraltarPacketVersionException(definition.getVersion());
		}
	}
}