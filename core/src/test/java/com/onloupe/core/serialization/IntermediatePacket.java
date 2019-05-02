package com.onloupe.core.serialization;

public class IntermediatePacket extends BasePacket {
	public IntermediatePacket() {
	}

	public IntermediatePacket(String text, int id) {
		super(text, id);
	}

	@Override
	public boolean equals(Object obj) {
		IntermediatePacket other = obj instanceof IntermediatePacket ? (IntermediatePacket) obj : null;
		if (other == null) {
			return false;
		}

		return super.equals(obj);
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
		int myHash = super.hashCode(); // Fold in hash code for inherited BasePacket

		// No member variables at this level, so just use the base hash code

		return myHash;
	}
}