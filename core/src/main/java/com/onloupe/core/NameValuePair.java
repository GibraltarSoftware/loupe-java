package com.onloupe.core;

/**
 * Simple class that contains a name and corresponding value
 * 
 * NameValuePairs are compared to each other by Name for sorting purposes.
 */
@Deprecated
public class NameValuePair<TValue> implements IName {
	/**
	 * The name of the item
	 */
	private String name;

	@Override
	public final String getName() {
		return this.name;
	}

	public final void setName(String value) {
		this.name = value;
	}

	/**
	 * The value of the item
	 */
	private TValue value;

	public final TValue getValue() {
		return this.value;
	}

	public final void setValue(TValue value) {
		this.value = value;
	}

	/**
	 * Default constructor used to initialize the class
	 * 
	 * No Remarks
	 */
	public NameValuePair() {
		setName("");
		setValue(null);
	}

	/**
	 * Default constructor used to initialize the class
	 * 
	 * @param name  The name of the item
	 * @param value The value of the corresponding item No Remarks
	 */
	public NameValuePair(String name, TValue value) {
		setName(name);
		setValue(value);
	}

	/**
	 * Returns a System.String that represents current System.Object
	 * 
	 * @return Returns a System.String that represents current System.Object No
	 *         Remarks
	 */
	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Compares this object with the passed in object, if it is an INameValuePair.
	 * 
	 * @param obj The other object that is to be compared with this instance.
	 * @return A value that is less than, equal to, or greater than zero.
	 */
	public final int compareTo(Object obj) {
		if (obj == null) {
			return Integer.MAX_VALUE; // We're not null, so we're greater than any null.
		}

		IName otherPair = obj instanceof IName ? (IName) obj : null;
		if (otherPair == null) {
			return Integer.MIN_VALUE; // Invalid comparison, we can't compare against unknown types.
		}

		return compareTo(otherPair);
	}

	/**
	 * Compares this IName with another of any data type.
	 * 
	 * @param other The other IName that is to be compared with this instance.
	 * @return A value that is less than, equal to, or greater than zero.
	 */
	public final int compareTo(IName other) {
		if (other == null) {
			return Integer.MAX_VALUE; // We're not null, so we're greater than any null.
		}

		return getName().compareTo(other.getName());
	}

	/**
	 * Compares this NameValuePair&lt;TValue&gt; with another with the same data
	 * type.
	 * 
	 * @param other The other NameValuePair&lt;TValue&gt; that is to be compared
	 *              with this instance.
	 * @return A value that is less than, equal to, or greater than zero.
	 */
	public final int compareTo(NameValuePair<TValue> other) {
		return compareTo((IName) other); // Just use the broader type-cast comparison.
	}

}