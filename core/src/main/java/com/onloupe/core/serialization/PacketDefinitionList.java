package com.onloupe.core.serialization;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * Helper class used by PacketRead and PacketWriter to maintain a cache of
 * PacketDefinition instances for used IPacket types.
 */
public final class PacketDefinitionList implements java.lang.Iterable<PacketDefinition> {
	
	/** The list. */
	private List<PacketDefinition> list;
	
	/** The committed list size. */
	private int committedListSize;

	/**
	 * Returns an empty list.
	 */
	public PacketDefinitionList() {
		this.list = new ArrayList<PacketDefinition>();
	}

	/**
	 * Gets the number of elements in the list.
	 *
	 * @return the count
	 */
	public int getCount() {
		return this.list.size();
	}

	/**
	 * Gets a particular item from the list.
	 * 
	 * @param index Zero-based index of the desired element
	 * @return Returns a PacketDefinition if a valid index is requested, otherwise
	 *         throws an exception.
	 */
	public PacketDefinition get(int index) {
		return this.list.get(index);
	}

	/**
	 * Gets the index of the corresponding PacketDefinition, if cached. Otherwise,
	 * returns -1.
	 *
	 * @param packet IPacket object for which a PacketDefinition may be cached
	 * @return the int
	 */
	public int indexOf(IPacket packet) {
		// Most packets have a static field structure. But some packets, such as EventMetrics, have a
		// dynamic set of fields determined at run-time. In this case, we want to cache a distinct
		// PacketDefinition for each set of field definitions associated with the type.
		// Classes that require this dynamic type capability should implement IDynamicPacket which
		// will define a DynamicTypeName field on the packet. This field will be automatically
		// serialized in the PacketDefinition and the DynamictTypeName is appended to the static
		// type name for purposes of indexing in this collection.
		String typeName = packet.getClass().getSimpleName();
		IDynamicPacket dynamicPacket;

		if (packet instanceof GenericPacket) {
			typeName = ((GenericPacket)packet).getPacketDefinition().getQualifiedTypeName();
		} else if ((dynamicPacket = packet instanceof IDynamicPacket ? (IDynamicPacket) packet : null) != null) {
			typeName += "+" + dynamicPacket.getDynamicTypeName();
		}

		return indexOf(typeName);
	}

	/**
	 * Gets the index of the corresponding PacketDefinition, if cached. Otherwise,
	 * returns -1.
	 *
	 * @param qualifiedTypeName Type name of the corresponding IPacket object for
	 *                          which a PacketDefinition may be cached
	 * @return the int
	 */
	public int indexOf(String qualifiedTypeName) {
		for (int index = 0; index < this.list.size(); index++) {
			if (this.list.get(index).getQualifiedTypeName().equals(qualifiedTypeName)) {
				return index;
			}
		}
		return -1;
	}

	/**
	 * Adds a PacketDefinition to the list.
	 * 
	 * @param item PacketDefinition to add
	 * @return Returns the index of the newly added item. If a PacketDefinition for
	 *         this type has already been added, an exception is raised.
	 * 
	 */
	public int add(PacketDefinition item) {
		// check if this packet definition is already registered
		int index = indexOf(item.getQualifiedTypeName());
		if (index >= 0) {
			// Something is wrong, the type should only be defined once
			// But, in release builds, we want to be forgiving, so simply accept the new
			// packet definition.
			this.list.set(index, item);
			return index;
		}

		// Add the item to the end of the list and return the index.
		this.list.add(item);
		return getCount() - 1;
	}

	/**
	 * This method is called after a packet is successfully written to "lock-in" any
	 * changes to state data.
	 */
	public void commit() {
		this.committedListSize = this.list.size();
	}

	/**
	 * This method is called if a packet write fails to undo any changes to state
	 * data that will not be available to the IPacketReader reading the stream.
	 */
	public void rollback() {
		if (this.list.size() > this.committedListSize) {			
			for (int i = this.list.size() - 1; i >= this.committedListSize; i--) {
				this.list.remove(i);
			}
		}
	}

	/**
	 * Returns an enumerator that iterates through the collection.
	 * 
	 * @return A <see cref="T:System.Collections.Generic.IEnumerator`1"/> that can
	 *         be used to iterate through the collection.
	 * 
	 *         <filterpriority>1</filterpriority>
	 */
	@Override
	public Iterator<PacketDefinition> iterator() {
		return this.list.iterator();
	}

	/**
	 * Returns an enumerator that iterates through a collection.
	 * 
	 * @return An <see cref="T:System.Collections.IEnumerator"/> object that can be
	 *         used to iterate through the collection.
	 * 
	 *         
	 */
	public Iterator<PacketDefinition> getEnumerator() {
		return ((java.lang.Iterable<PacketDefinition>) this).iterator();
	}
}