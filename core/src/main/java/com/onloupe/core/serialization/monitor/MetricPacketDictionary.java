package com.onloupe.core.serialization.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A collection of performance counter metric packets, keyed by their unique ID.
 * This is the persistable form of a performance counter metric.
 */
public class MetricPacketDictionary {
	private final Map<String, MetricPacket> dictionaryByName = new HashMap<String, MetricPacket>();
	private final Map<UUID, MetricPacket> dictionary = new HashMap<UUID, MetricPacket>();
	private final List<MetricPacket> list = new ArrayList<MetricPacket>();
	private final Object lock = new Object();

	/**
	 * Add an existing metric packet object to our collection. It must be for the
	 * same analysis as this collection.
	 * 
	 * @param newMetricPacket The new metric object to add.
	 * @return
	 */
	public final boolean add(MetricPacket newMetricPacket) {
		// we really don't want to support this method, but we have to for
		// ICollection<T> compatibility. So we're going to ruthelessly
		// verify that the metric packet object was created correctly.

		if (newMetricPacket == null) {
			throw new NullPointerException("A metric packet object must be provided to add it to the collection.");
		}

		// we're about to modify the collection, get a lock. We don't want the lock to
		// cover the changed event since
		// we really don't know how long that will take, and it could be deadlock prone.
		synchronized (this.lock) {
			// make sure we don't already have it
			if (this.dictionary.containsKey(newMetricPacket.getID())) {
				throw new IllegalArgumentException(
						"There already exists a metric packet object in the collection for the specified Id.");
			}

			// make sure we don't already have it by its name
			if (this.dictionaryByName.containsKey(newMetricPacket.getName())) {
				throw new IllegalArgumentException(
						"There already exists a metric packet object in the collection with the specified name");
			}

			// add it to both lookup collections
			this.dictionary.put(newMetricPacket.getID(), newMetricPacket);
			this.dictionaryByName.put(newMetricPacket.getName(), newMetricPacket);
			this.list.add(newMetricPacket);
		}
		return true;
	}

	/**
	 * Determines whether the collection contains an element with the specified key.
	 * 
	 * @param key The key to locate in the collection
	 * @return true if the collection contains an element with the key; otherwise,
	 *         false.
	 */
	public final boolean containsKey(UUID key) {
		synchronized (this.lock) // Apparently Dictionaries are not internally threadsafe.
		{
			// gateway to our inner dictionary
			return this.dictionary.containsKey(key);
		}
	}

	/**
	 * Determines whether the collection contains an element with the specified key.
	 * 
	 * @param key The performance counter key to locate in the collection
	 * @return true if the collection contains an element with the key; otherwise,
	 *         false.
	 */
	public final boolean containsKey(String key) {
		synchronized (this.lock) // Apparently Dictionaries are not internally threadsafe.
		{
			// gateway to our alternate inner dictionary
			return this.dictionaryByName.containsKey(key);
		}
	}

	/**
	 * Retrieve an item from the collection by its key if present. If not present,
	 * the default value of the object is returned.
	 * 
	 * @param key   The key of the value to get.
	 * @param value When this method returns, contains the value associated with the
	 *              specified key, if the key is found; otherwise, the default value
	 *              for the type of the value parameter. This parameter is passed
	 *              uninitialized.
	 * @return true if the collection contains an element with the specified key;
	 *         otherwise false.
	 */
	public final MetricPacket tryGetValue(UUID key) {
		synchronized (this.lock) // Apparently Dictionaries are not internally threadsafe.
		{
			// gateway to our inner dictionary try get value
			return this.dictionary.get(key);
		}
	}

	/**
	 * Retrieve an item from the collection by its key if present. If not present,
	 * the default value of the object is returned.
	 * 
	 * @param key   The performance counter key to locate in the collection
	 * @param value When this method returns, contains the value associated with the
	 *              specified key, if the key is found; otherwise, the default value
	 *              for the type of the value parameter. This parameter is passed
	 *              uninitialized.
	 * @return true if the collection contains an element with the specified key;
	 *         otherwise false.
	 */
	public final boolean tryGetValue(String key, MetricPacket value) {
		synchronized (this.lock) // Apparently Dictionaries are not internally threadsafe.
		{
			// gateway to our inner dictionary try get value
			return this.dictionaryByName.containsKey(key);
		}
	}

	public final int indexOf(Object objectValue) {
		MetricPacket item = (MetricPacket) objectValue;
		synchronized (this.lock) // Apparently Lists are not internally threadsafe.
		{
			return this.list.indexOf(item);
		}
	}

	public final void add(int index, MetricPacket item) {
		// we don't support setting an object by index; we are sorted.
		throw new UnsupportedOperationException();
	}

	public final MetricPacket remove(int index) {
		// find the item at the requested location
		MetricPacket victim;
		synchronized (this.lock) // Apparently Lists are not internally threadsafe.
		{
			victim = this.list.get(index);
		}

		// and pass that to our normal remove method. Don't lock around this, it needs
		// to send an event outside the lock.
		remove(victim);
		return victim;
	}

	/**
	 * Retrieve performance counter metric packet object by numeric index in
	 * collection.
	 * 
	 * @param index
	 * @return
	 */
	public final MetricPacket get(int index) {
		synchronized (this.lock) // Apparently Lists are not internally threadsafe.
		{
			return this.list.get(index);
		}
	}

	public final MetricPacket set(int index, MetricPacket value) {
		// we don't want to support setting an object by index, we are sorted.
		throw new UnsupportedOperationException();
	}

	/**
	 * Retrieve metric packet object by its name
	 * 
	 * @param key
	 * @return
	 */
	public final MetricPacket get(String key) {
		synchronized (this.lock) // Apparently Dictionaries are not internally threadsafe.
		{
			return this.dictionaryByName.get(key);
		}
	}

	/**
	 * Retrieve performance counter metric packet object by its Id
	 * 
	 * @param ID
	 * @return
	 */
	public final MetricPacket get(UUID id) {
		synchronized (this.lock) // Apparently Dictionaries are not internally threadsafe.
		{
			return this.dictionary.get(id);
		}
	}

	public final void clear() {
		// Only do this if we HAVE something, since events are fired.
		int count;
		synchronized (this.lock) // We need the lock for checking the count; apparently Lists are not internally
									// threadsafe.
		{
			count = this.list.size(); // Save this to check outside the lock, too.
			if (count > 0) {
				// The collection isn't already clear, so clear it inside the lock.
				this.list.clear();
				this.dictionary.clear();
				this.dictionaryByName.clear();
			}
		}

	}

	public final boolean contains(Object objectValue) {
		MetricPacket item = (MetricPacket) objectValue;
		// here we are relying on the fact that the comment object implements
		// IComparable sufficiently to guarantee uniqueness
		synchronized (this.lock) // Apparently Lists are not internally threadsafe.
		{
			return this.list.contains(item);
		}
	}

	public final int size() {
		synchronized (this.lock) // Apparently Lists are not internally threadsafe.
		{
			return this.list.size();
		}
	}

	public final boolean isReadOnly() {
		return false;
	}

	/**
	 * Remove the specified victim comment. If the comment isn't in the collection,
	 * no exception is thrown.
	 * 
	 * @param victim The object to remove.
	 */
	public final boolean remove(Object objectValue) {
		MetricPacket victim = (MetricPacket) objectValue;
		boolean result = false;

		if (victim == null) {
			throw new NullPointerException("A victim object must be provided to remove it from the collection.");
		}

		// we're about to modify the collection, get a lock. We don't want the lock to
		// cover the changed event since
		// we really don't know how long that will take, and it could be deadlock prone.
		synchronized (this.lock) {
			// we have to remove it from both collections, and we better not raise an error
			// if not there.
			if (this.dictionary.containsKey(victim.getID())) {
				this.dictionary.remove(victim.getID());
				result = true; // we did remove something
			}

			if (this.dictionaryByName.containsKey(victim.getName())) {
				this.dictionaryByName.remove(victim.getName());
				result = true; // we did remove something
			}

			if (this.list.contains(victim)) {
				this.list.remove(victim);
				result = true; // we did remove something
			}
		}

		return result;
	}

}