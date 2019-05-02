package com.onloupe.core.serialization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FieldDefinitionCollection {
	private Map<String, FieldDefinition> definitionsByName = new HashMap<String, FieldDefinition>();
	private Map<String, Integer> indexByName = new HashMap<String, Integer>();
	private List<FieldDefinition> definitionsList = new ArrayList<FieldDefinition>();
	private final Object lock = new Object(); // MT Safety lock
	private boolean locked; // true if we can no longer be modified.

	public final boolean getLocked() {
		return this.locked;
	}

	public final void setLocked(boolean value) {
		// act as a latch - once set can't be unset
		if (value) {
			this.locked = value;
		}
	}

	/**
	 * Returns an enumerator that iterates through the collection.
	 * 
	 * @return A <see cref="T:System.Collections.Generic.IEnumerator`1" /> that can
	 *         be used to iterate through the collection.
	 * 
	 *         <filterpriority>1</filterpriority>
	 */
	public final Iterator<FieldDefinition> iterator() {
		return this.definitionsList.iterator();
	}

	public final void add(String fieldName, java.lang.Class type) {
		add(fieldName, PacketDefinition.getSerializableType(type));
	}

	public final void add(String fieldName, FieldType fieldType) {
		FieldDefinition field = new FieldDefinition(fieldName, fieldType);
		add(field);
	}

	/**
	 * Adds an item to the
	 * <see cref="T:System.Collections.Generic.ICollection`1" />.
	 * 
	 * @param item The object to add to the
	 *             <see cref="T:System.Collections.Generic.ICollection`1" />.
	 * @return
	 * @exception T:System.NotSupportedException The <see cref=
	 *                                           "T:System.Collections.Generic.ICollection`1"
	 *                                           /> is read-only.
	 */
	public final boolean add(FieldDefinition item) {
		synchronized (this.lock) {
			// make sure it isn't already here by name
			if (this.definitionsByName.containsKey(item.getName())) {
				throw new IllegalArgumentException("There is already a field definition with the provided name.");
			}

			// otherwise we just add it.
			this.definitionsByName.put(item.getName(), item);
			this.definitionsList.add(item);
			this.indexByName.put(item.getName(), this.definitionsList.size() - 1); // always the most recently added
																						// item
		}
		return true;
	}

	/**
	 * Removes all items from the
	 * <see cref="T:System.Collections.Generic.ICollection`1" />.
	 * 
	 * @exception T:System.NotSupportedException The <see cref=
	 *                                           "T:System.Collections.Generic.ICollection`1"
	 *                                           /> is read-only.
	 */
	public final void clear() {
		synchronized (this.lock) {
			this.definitionsByName.clear();
			this.definitionsList.clear();
			this.indexByName.clear();
		}
	}

	/**
	 * Determines whether the
	 * <see cref="T:System.Collections.Generic.ICollection`1" /> contains a specific
	 * value.
	 * 
	 * @return true if <paramref name="item" /> is found in the
	 *         <see cref="T:System.Collections.Generic.ICollection`1" />; otherwise,
	 *         false.
	 * 
	 * @param item The object to locate in the
	 *             <see cref="T:System.Collections.Generic.ICollection`1" />.
	 */
	public final boolean contains(Object objectValue) {
		FieldDefinition item = (FieldDefinition) objectValue;
		return this.definitionsByName.containsKey(item.getName());
	}

	/**
	 * Determines whether the FieldDefinitionCollection contains a FieldDefinition
	 * for the specified fieldName.
	 * 
	 * @param fieldName The name of the field of interest.
	 * @return True if found, false if not.
	 */
	public final boolean containsKey(String fieldName) {
		return this.definitionsByName.containsKey(fieldName);
	}

	/**
	 * Copies the elements of the
	 * <see cref="T:System.Collections.Generic.ICollection`1"/> to an
	 * <see cref="T:System.Array"/>, starting at a particular
	 * <see cref="T:System.Array"/> index.
	 * 
	 * @param array      The one-dimensional <see cref="T:System.Array"/> that is
	 *                   the destination of the elements copied from
	 *                   <see cref="T:System.Collections.Generic.ICollection`1"/>.
	 *                   The <see cref="T:System.Array"/> must have zero-based
	 *                   indexing.
	 * @param arrayIndex The zero-based index in <paramref name="array"/> at which
	 *                   copying begins.
	 * @exception T:System.ArgumentNullException       <paramref name="array"/> is
	 *                                                 null.
	 * @exception T:System.ArgumentOutOfRangeException <paramref name="arrayIndex"/>
	 *                                                 is less than 0.
	 * @exception T:System.ArgumentException           <paramref name="array"/> is
	 *                                                 multidimensional. -or-
	 *                                                 <paramref name="arrayIndex"/>
	 *                                                 is equal to or greater than
	 *                                                 the length of
	 *                                                 <paramref name="array"/>.
	 *                                                 -or- The number of elements
	 *                                                 in the source <see cref=
	 *                                                 "T:System.Collections.Generic.ICollection`1"/>
	 *                                                 is greater than the available
	 *                                                 space from
	 *                                                 <paramref name="arrayIndex"/>
	 *                                                 to the end of the destination
	 *                                                 <paramref name="array"/>.
	 *                                                 -or- Type cannot be cast
	 *                                                 automatically to the type of
	 *                                                 the destination
	 *                                                 <paramref name="array"/>.
	 * 
	 */
	public final void copyTo(FieldDefinition[] array, int arrayIndex) {
		System.arraycopy(this.definitionsList, 0, array, arrayIndex, this.definitionsList.size());
	}

	/**
	 * Removes the first occurrence of a specific object from the
	 * <see cref="T:System.Collections.Generic.ICollection`1" />.
	 * 
	 * @return true if <paramref name="item" /> was successfully removed from the
	 *         <see cref="T:System.Collections.Generic.ICollection`1" />; otherwise,
	 *         false. This method also returns false if <paramref name="item" /> is
	 *         not found in the original
	 *         <see cref="T:System.Collections.Generic.ICollection`1" />.
	 * 
	 * @param item The object to remove from the
	 *             <see cref="T:System.Collections.Generic.ICollection`1" />.
	 * @exception T:System.NotSupportedException The <see cref=
	 *                                           "T:System.Collections.Generic.ICollection`1"
	 *                                           /> is read-only.
	 */
	public final boolean remove(Object objectValue) {
		FieldDefinition item = (FieldDefinition) objectValue;
		boolean itemRemoved = false;
		synchronized (this.lock) {
			// just try to remove it from the two collections, you never know.
			if (this.definitionsList.remove(item)) {
				itemRemoved = true;
			}

			if (this.definitionsByName.remove(item.getName()) != null) {
				itemRemoved = true;
			}

			// We don't remove field definitions much (if ever), but if we did, it we need
			// to make
			// sure the index lookup is updated. And since this is a rare operation, let's
			// just rebuild the index
			this.indexByName.clear();
			int index = 0;
			for (FieldDefinition fieldDefinition : this.definitionsList) {
				this.indexByName.put(fieldDefinition.getName(), index++);
			}
		}

		return itemRemoved;
	}

	/**
	 * Gets the number of elements contained in the
	 * <see cref="T:System.Collections.Generic.ICollection`1" />.
	 * 
	 * @return The number of elements contained in the
	 *         <see cref="T:System.Collections.Generic.ICollection`1" />.
	 * 
	 */
	public final int size() {
		return this.definitionsList.size();
	}

	/**
	 * Gets a value indicating whether the
	 * <see cref="T:System.Collections.Generic.ICollection`1" /> is read-only.
	 * 
	 * @return true if the <see cref="T:System.Collections.Generic.ICollection`1" />
	 *         is read-only; otherwise, false.
	 * 
	 */
	public final boolean isReadOnly() {
		return this.locked;
	}

	/**
	 * Determines the index of a specific item in the
	 * <see cref="T:System.Collections.Generic.IList`1" />.
	 * 
	 * @return The index of <paramref name="item" /> if found in the list;
	 *         otherwise, -1.
	 * 
	 * @param item The object to locate in the
	 *             <see cref="T:System.Collections.Generic.IList`1" />.
	 */
	public final int indexOf(FieldDefinition item) {
		return this.definitionsList.indexOf(item);
	}

	/**
	 * Determines the index of a FieldDefinition by its specified fieldName.
	 * 
	 * @param fieldName The name of the field of interest.
	 * @return The index of the FieldDefinition with the specified fieldName, or -1
	 *         if not found.
	 */
	public final int indexOf(String fieldName) {
		try {
			return this.indexByName.get(fieldName);
		} catch (RuntimeException e) {
			return -1;
		}
	}

	/**
	 * Inserts an item to the <see cref="T:System.Collections.Generic.IList`1" /> at
	 * the specified index.
	 * 
	 * @param index The zero-based index at which <paramref name="item" /> should be
	 *              inserted.
	 * @param item  The object to insert into the
	 *              <see cref="T:System.Collections.Generic.IList`1" />.
	 * @exception T:System.ArgumentOutOfRangeException <paramref name="index" /> is
	 *                                                 not a valid index in the
	 *                                                 <see cref=
	 *                                                 "T:System.Collections.Generic.IList`1"
	 *                                                 />.
	 * @exception T:System.NotSupportedException       The <see cref=
	 *                                                 "T:System.Collections.Generic.IList`1"
	 *                                                 /> is read-only.
	 */
	public final void add(int index, FieldDefinition item) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Removes the <see cref="T:System.Collections.Generic.IList`1" /> item at the
	 * specified index.
	 * 
	 * @param index The zero-based index of the item to remove.
	 * @return
	 * @exception T:System.ArgumentOutOfRangeException <paramref name="index" /> is
	 *                                                 not a valid index in the
	 *                                                 <see cref=
	 *                                                 "T:System.Collections.Generic.IList`1"
	 *                                                 />.
	 * @exception T:System.NotSupportedException       The <see cref=
	 *                                                 "T:System.Collections.Generic.IList`1"
	 *                                                 /> is read-only.
	 */
	public final FieldDefinition remove(int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Gets the element at the specified index.
	 * 
	 * @return The element at the specified index.
	 * 
	 * @param index The zero-based index of the element to get or set.
	 * @exception T:System.ArgumentOutOfRangeException <paramref name="index" /> is
	 *                                                 not a valid index in the
	 *                                                 <see cref=
	 *                                                 "T:System.Collections.Generic.IList`1"
	 *                                                 />.
	 * @exception T:System.NotSupportedException       The property is set and the
	 *                                                 <see cref=
	 *                                                 "T:System.Collections.Generic.IList`1"
	 *                                                 /> is read-only.
	 */
	public final FieldDefinition get(int index) {
		return this.definitionsList.get(index);
	}

	public final FieldDefinition set(int index, FieldDefinition value) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Gets the element at the specified index.
	 * 
	 * @return The element at the specified index.
	 * 
	 * @param fieldName The name of the field to get.
	 * @exception T:System.ArgumentOutOfRangeException <paramref name="fieldName" />
	 *                                                 is not a valid name in the
	 *                                                 <see cref=
	 *                                                 "T:System.Collections.Generic.IList`1"
	 *                                                 />.
	 * @exception T:System.NotSupportedException       The property is set and the
	 *                                                 <see cref=
	 *                                                 "T:System.Collections.Generic.IList`1"
	 *                                                 /> is read-only.
	 */
	public final FieldDefinition get(String fieldName) {
		return this.definitionsByName.get(fieldName);
	}

	public final void set(String fieldName, FieldDefinition value) {
		throw new UnsupportedOperationException();
	}

}