package com.onloupe.core.serialization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * The Class FieldDefinitionCollection.
 */
public class FieldDefinitionCollection {
	
	/** The definitions by name. */
	private Map<String, FieldDefinition> definitionsByName = new HashMap<String, FieldDefinition>();
	
	/** The index by name. */
	private Map<String, Integer> indexByName = new HashMap<String, Integer>();
	
	/** The definitions list. */
	private List<FieldDefinition> definitionsList = new ArrayList<FieldDefinition>();
	
	/** The lock. */
	private final Object lock = new Object(); // MT Safety lock
	
	/** The locked. */
	private boolean locked; // true if we can no longer be modified.

	/**
	 * Gets the locked.
	 *
	 * @return the locked
	 */
	public final boolean getLocked() {
		return this.locked;
	}

	/**
	 * Sets the locked.
	 *
	 * @param value the new locked
	 */
	public final void setLocked(boolean value) {
		// act as a latch - once set can't be unset
		if (value) {
			this.locked = value;
		}
	}

	/**
	 * Returns an enumerator that iterates through the collection.
	 * 
	 * @return A that can be used to iterate through the collection.
	 * 
	 */
	public final Iterator<FieldDefinition> iterator() {
		return this.definitionsList.iterator();
	}

	/**
	 * Adds the.
	 *
	 * @param fieldName the field name
	 * @param type the type
	 */
	public final void add(String fieldName, java.lang.Class type) {
		add(fieldName, PacketDefinition.getSerializableType(type));
	}

	/**
	 * Adds the.
	 *
	 * @param fieldName the field name
	 * @param fieldType the field type
	 */
	public final void add(String fieldName, FieldType fieldType) {
		FieldDefinition field = new FieldDefinition(fieldName, fieldType);
		add(field);
	}

	/**
	 * Adds an item to the collection.
	 *
	 * @param item The object to add to the collection
	 * @return true, if successful
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
	 * Removes all items from the collection.
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
	 * collection contains a specific
	 * value.
	 *
	 * @param objectValue the object value
	 * @return true if item is found in the
	 *         collection; otherwise,
	 *         false.
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
	 * Copies the elements of the collection to an array, starting at a particular
	 * array index.
	 *
	 * @param array      The one-dimensional array that is the destination of the
	 *                   elements copied from collection. The array must have
	 *                   zero-based indexing.
	 * @param arrayIndex The zero-based index in  at which
	 *                   copying begins.
	 */
	public final void copyTo(FieldDefinition[] array, int arrayIndex) {
		System.arraycopy(this.definitionsList, 0, array, arrayIndex, this.definitionsList.size());
	}

	/**
	 * Removes the first occurrence of a specific object from the
	 * collection.
	 *
	 * @param objectValue the object value
	 * @return true if item was successfully removed from the
	 *         collection; otherwise,
	 *         false. This method also returns false if item is
	 *         not found in the original
	 *         collection.
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
	 * collection.
	 * 
	 * @return The number of elements contained in the
	 *         collection.
	 * 
	 */
	public final int size() {
		return this.definitionsList.size();
	}

	/**
	 * Gets a value indicating whether the
	 * collection is read-only.
	 * 
	 * @return true if the collection
	 *         is read-only; otherwise, false.
	 * 
	 */
	public final boolean isReadOnly() {
		return this.locked;
	}

	/**
	 * Determines the index of a specific item in the
	 * collection.
	 *
	 * @param item The object to locate in the
	 *             collection.
	 * @return The index of item if found in the list;
	 *         otherwise, -1.
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
	 * Inserts an item to the collection at
	 * the specified index.
	 *
	 * @param index The zero-based index at which item should be
	 *              inserted.
	 * @param item  The object to insert into the
	 *              collection.
	 */
	public final void add(int index, FieldDefinition item) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Removes the collection item at the
	 * specified index.
	 *
	 * @param index The zero-based index of the item to remove.
	 * @return the field definition
	 */
	public final FieldDefinition remove(int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Gets the element at the specified index.
	 *
	 * @param index The zero-based index of the element to get or set.
	 * @return The element at the specified index.
	 */
	public final FieldDefinition get(int index) {
		return this.definitionsList.get(index);
	}

	/**
	 * Sets the.
	 *
	 * @param index the index
	 * @param value the value
	 * @return the field definition
	 */
	public final FieldDefinition set(int index, FieldDefinition value) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Gets the element at the specified index.
	 *
	 * @param fieldName The name of the field to get.
	 * @return The element at the specified index.
	 */
	public final FieldDefinition get(String fieldName) {
		return this.definitionsByName.get(fieldName);
	}

	/**
	 * Sets the.
	 *
	 * @param fieldName the field name
	 * @param value the value
	 */
	public final void set(String fieldName, FieldDefinition value) {
		throw new UnsupportedOperationException();
	}

}