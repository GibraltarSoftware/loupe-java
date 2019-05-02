package com.onloupe.core.serialization;

public class BaseObject {
	private int id;

	public BaseObject() {
	}

	public BaseObject(int id) {
		this.id = id;
	}

	public final int getId() {
		return this.id;
	}

	public final void setId(int value) {
		this.id = value;
	}

	@Override
	public boolean equals(Object obj) {
		BaseObject other = obj instanceof BaseObject ? (BaseObject) obj : null;
		if (other == null) {
			return false;
		}

		return other.id == this.id;
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
		int myHash = (int) this.id; // Just use the only member variable, an int, as our hash code

		return myHash;
	}
}