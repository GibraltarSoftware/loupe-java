package com.onloupe.core.serialization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;


/**
 * The Class PacketCache.
 */
public class PacketCache implements java.lang.Iterable<ICachedPacket> {
	
	/** The cache. */
	private List<ICachedPacket> cache;
	
	/** The index. */
	private HashMap<UUID, Integer> index;

	/**
	 * Instantiates a new packet cache.
	 */
	public PacketCache() {
		this.cache = new ArrayList<ICachedPacket>();
		this.index = new HashMap<UUID, Integer>();
	}

	/**
	 * Adds the or get.
	 *
	 * @param packet the packet
	 * @return the int
	 */
	public final int addOrGet(ICachedPacket packet) {
		Integer index = this.index.get(packet.getID());
		if (index != null) {
			return index;
		}

		index = this.cache.size();
		this.cache.add(packet);
		this.index.put(packet.getID(), index);
		return index;
	}

	/**
	 * Contains.
	 *
	 * @param packet the packet
	 * @return true, if successful
	 */
	public final boolean contains(ICachedPacket packet) {
		return this.index.containsKey(packet.getID());
	}

	/**
	 * Gets the count.
	 *
	 * @return the count
	 */
	public final int getCount() {
		return this.cache.size();
	}

	/**
	 * Clear.
	 */
	public final void clear() {
		this.cache.clear();
		this.index.clear();
	}

	/**
	 * Gets the.
	 *
	 * @param index the index
	 * @return the i cached packet
	 */
	public final ICachedPacket get(int index) {
		return index >= 0 && index < this.cache.size() ? this.cache.get(index) : null;
	}

	/**
	 * Gets the.
	 *
	 * @param id the id
	 * @return the i cached packet
	 */
	public final ICachedPacket get(UUID id) {
		Integer index = this.index.get(id);
		if (index != null) {
			return this.cache.get(index);
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public final Iterator<ICachedPacket> iterator() {
		return this.cache.iterator();
	}

}