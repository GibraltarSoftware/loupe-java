package com.onloupe.core.serialization;

import java.util.ArrayList;
import java.util.List;

// The concept of a DynamicPacket is that some instance objects may have a
// different (i.e. dynamic) number of fields from other instances.
// This may be needed for EventMetrics.  To accommodate this, we need a way
// to associate a packet definition with each variation of the number of fields
// and each packet instance must somehow be associated with the corresponding
// packet definition.  We accomplish this by implementing the IDynamicPacket
// interface.  The DynamicTypeName is then used to allow for these mappings.
public class DynoPacket implements IDynamicPacket {
	private String[] _Strings;
	private int[] _Ints;
	private String _DynamicTypeName;

	/**
	 * A default constructor is necessary to properly implement IPacket
	 */
	public DynoPacket() {
	}

	public DynoPacket(int stringCount, int intCount) {
		this._Strings = new String[stringCount];
		this._Ints = new int[intCount];
		for (int i = 0; i < Math.max(stringCount, intCount); i++) {
			if (i < stringCount) {
				this._Strings[i] = "String " + (i + 1);
			}
			if (i < intCount) {
				this._Ints[i] = i + 1;
			}
		}
	}

	@Override
	public final String getDynamicTypeName() {
		return this._DynamicTypeName;
	}

	@Override
	public final void setDynamicTypeName(String value) {
		this._DynamicTypeName = value;
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

		FieldDefinitionCollection fields = definition.getFields();
		for (int i = 0; i < this._Strings.length; i++) {
			fields.add("String " + (i + 1), FieldType.STRING);
		}
		for (int i = 0; i < this._Ints.length; i++) {
			fields.add("Int " + (i + 1), FieldType.INT);
		}
	}

	/**
	 * Write out all of the fields for the current packet
	 * 
	 * @param definition The definition that was used to persist the packet.
	 * @param packet     The serialized packet to populate with data
	 */
	@Override
	public final void writeFields(PacketDefinition definition, SerializedPacket packet) {
		for (int i = 0; i < this._Strings.length; i++) {
			packet.setField("String " + (i + 1), this._Strings[i]);
		}
		for (int i = 0; i < this._Ints.length; i++) {
			packet.setField("Int " + (i + 1), this._Ints[i]);
		}
	}

	/**
	 * Read back the field values for the current packet.
	 * 
	 * @param definition The definition that was used to persist the packet.
	 * @param packet     The serialized packet to read data from
	 */
	@Override
	public final void readFields(PacketDefinition definition, SerializedPacket packet) {
		// In order for the NMock tests to work right, the number and type
		// of fields read must match the PacketDefinition exactly
		int stringCount = 0;
		for (FieldDefinition fieldDefinition : definition) {
			if (fieldDefinition.getFieldType() != FieldType.STRING) {
				break;
			}
			stringCount++;
		}
		int intCount = definition.getFields().size() - stringCount;
		this._Strings = new String[stringCount];
		this._Ints = new int[intCount];
		for (int i = 0; i < this._Strings.length; i++) {
			this._Strings[i] = packet.getField("String " + (i + 1), String.class);
		}

		for (int i = 0; i < this._Ints.length; i++) {
			this._Ints[i] = packet.getField("Int " + (i + 1), Integer.class);
		}
	}

	@Override
	public boolean equals(Object obj) {
		return equals(obj instanceof DynoPacket ? (DynoPacket) obj : null);
	}

	/**
	 * Indicates whether the current object is equal to another object of the same
	 * type.
	 * 
	 * 
	 * @return true if the current object is equal to the other parameter;
	 *         otherwise, false.
	 * 
	 * 
	 * @param other An object to compare with this object.
	 */
	public final boolean equals(DynoPacket other) {
		// Careful - can be null
		if (other == null) {
			return false; // since we're a live object we can't be equal.
		}

		if (this._Strings.length != other._Strings.length) {
			return false;
		}

		for (int i = 0; i < this._Strings.length; i++) {
			if (!this._Strings[i].equals(other._Strings[i])) {
				return false;
			}
		}

		if (this._Ints.length != other._Ints.length) {
			return false;
		}

		for (int i = 0; i < this._Ints.length; i++) {
			if (this._Ints[i] != other._Ints[i]) {
				return false;
			}
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
		int myHash = this._Strings.length ^ this._Ints.length; // Different array lengths make them not Equal, fold them
																// in

		for (int i = 0; i < this._Strings.length; i++) {
			if (this._Strings[i] != null) {
				myHash ^= this._Strings[i].hashCode(); // Fold in hash code for each string in the array
			}
		}

		for (int i = 0; i < this._Ints.length; i++) {
			myHash ^= this._Ints[i]; // Fold in each int (as a hash code for itself) in the array
		}

		return myHash;
	}
}