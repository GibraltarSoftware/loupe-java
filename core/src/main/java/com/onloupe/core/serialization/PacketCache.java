package com.onloupe.core.serialization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class PacketCache implements java.lang.Iterable<ICachedPacket> {
	private List<ICachedPacket> cache;
	private HashMap<UUID, Integer> index;

	public PacketCache() {
		this.cache = new ArrayList<ICachedPacket>();
		this.index = new HashMap<UUID, Integer>();
	}

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

	public final boolean contains(ICachedPacket packet) {
		return this.index.containsKey(packet.getID());
	}

	public final int getCount() {
		return this.cache.size();
	}

	public final void clear() {
		this.cache.clear();
		this.index.clear();
	}

	public final ICachedPacket get(int index) {
		return index >= 0 && index < this.cache.size() ? this.cache.get(index) : null;
	}

	public final ICachedPacket get(UUID id) {
		Integer index = this.index.get(id);
		if (index != null) {
			return this.cache.get(index);
		} else {
			return null;
		}
	}

	@Override
	public final Iterator<ICachedPacket> iterator() {
		return this.cache.iterator();
	}

}