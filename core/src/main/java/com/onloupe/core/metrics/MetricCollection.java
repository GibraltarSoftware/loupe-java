package com.onloupe.core.metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// TODO: Auto-generated Javadoc
/**
 * A collection of metrics, keyed by their unique ID and name
 * 
 * A metric has a unique ID to identify a particular instance of the metric
 * (associated with one session) and a name that is unique within a session but
 * is designed for comparison of the same metric between sessions.
 */
public class MetricCollection {
	
	/** The dictionary by name. */
	private final Map<String, Metric> dictionaryByName = new HashMap<String, Metric>();
	
	/** The dictionary. */
	private final Map<UUID, Metric> dictionary = new HashMap<UUID, Metric>();
	
	/** The list. */
	private final List<Metric> list = new ArrayList<Metric>();
	
	/** The lock. */
	private final Object lock = new Object();
	
	/** The metric definition. */
	private MetricDefinition metricDefinition;

	/**
	 * Create a new metric dictionary for the provided definition.
	 * 
	 * This dictionary is created automatically by the Metric Definition during its
	 * initialization.
	 *
	 * @param metricDefinition the metric definition
	 */
	public MetricCollection(MetricDefinition metricDefinition) {
		if (metricDefinition == null) {
			throw new NullPointerException("metricDefinition");
		}
		this.metricDefinition = metricDefinition;
	}

	/**
	 * Object Change Locking object.
	 *
	 * @return the lock
	 */
	public final Object getLock() {
		return this.lock;
	}

	/**
	 * Determines whether the collection contains an element with the specified key.
	 * 
	 * @param key The key to locate in the collection
	 * @return true if the collection contains an element with the key; otherwise,
	 *         false.
	 */
	public final boolean containsKey(UUID key) {
		synchronized (this.lock) {
			// gateway to our inner dictionary
			return this.dictionary.containsKey(key);
		}
	}

	/**
	 * Determines whether the collection contains an element with the specified key.
	 * 
	 * @param key The metric name to locate in the collection
	 * @return true if the collection contains an element with the key; otherwise,
	 *         false.
	 */
	public final boolean containsKey(String key) {
		// we do a few cute tricks to normalize the key before checking it to be
		// tolerant of what users do
		String trueKey = MetricDefinition.normalizeKey(this.metricDefinition, key);

		synchronized (this.lock) {
			// gateway to our alternate inner dictionary
			return this.dictionaryByName.containsKey(trueKey);
		}
	}

	/**
	 * The metric definition that owns this dictionary, meaning every metric is a
	 * specific instance of this metric definition.
	 *
	 * @return the definition
	 */
	public MetricDefinition getDefinition() {
		return this.metricDefinition;
	}

	/**
	 * Retrieve an item from the collection by its key if present. If not present,
	 * the default value of the object is returned.
	 *
	 * @param key The key of the value to get.
	 * @return the metric
	 */
	public Metric tryGetValue(UUID key) {
		synchronized (this.lock) {
			// gateway to our inner dictionary try get value
			return this.dictionary.get(key);
		}
	}

	/**
	 * Retrieve an item from the collection by its key if present. If not present,
	 * the default value of the object is returned.
	 *
	 * @param key The metric name to locate in the collection
	 * @return the metric
	 */
	public Metric tryGetValue(String key) {
		// we do a few cute tricks to normalize the key before checking it to be
		// tolerant of what users do
		String trueKey = MetricDefinition.normalizeKey(this.metricDefinition, key);

		synchronized (this.lock) {
			// gateway to our inner dictionary try get value
			return this.dictionaryByName.get(trueKey);
		}
	}

	/**
	 * Returns the zero-based index of the specified metric within the dictionary.
	 * 
	 * @param item A metric object to find the index of
	 * @return The zero-based index of an item if found, a negative number if not
	 *         found.
	 */
	public final int indexOf(Metric item) {
		synchronized (this.lock) {
			return this.list.indexOf(item);
		}
	}

	/**
	 * Inserting objects by index is not supported because the collection is sorted.
	 * This method is implemented only for IList interface support and will throw an
	 * exception if called.
	 *
	 * @param index the index
	 * @param item the item
	 */
	public final void insert(int index, Metric item) {
		// we don't support setting an object by index; we are sorted.
		throw new UnsupportedOperationException();
	}

	/**
	 * Removing objects by index is not supported because the collection is always
	 * read only. This method is implemented only for IList interface support and
	 * will throw an exception if called.
	 *
	 * @param index the index
	 */
	public final void removeAt(int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Retrieve metric packet by numeric index in collection.
	 *
	 * @param index the index
	 * @return the metric
	 */
	public Metric get(int index) {
		synchronized (this.lock) {
			return this.list.get(index);
		}
	}

	/**
	 * Retrieve metric object by its Id.
	 *
	 * @param id the id
	 * @return the metric
	 */
	public Metric get(UUID id) {
		synchronized (this.lock) {
			return this.dictionary.get(id);
		}
	}

	/**
	 * Retrieve metric object by its name.
	 *
	 * @param key the key
	 * @return the metric
	 */
	public Metric get(String key) {
		// we do a few cute tricks to normalize the key before checking it to be
		// tolerant of what users do
		String trueKey = MetricDefinition.normalizeKey(this.metricDefinition, key);

		synchronized (this.lock) {
			return this.dictionaryByName.get(trueKey);
		}
	}

	/**
	 * Add the supplied Metric item to this collection.
	 * 
	 * Metrics automatically add themselves when they are created, so it isn't
	 * necessary (and will produce errors) to manually add them.
	 *
	 * @param item The new Metric item to add to this collection
	 * @return true, if successful
	 */
	public final boolean add(Metric item) {
		// we really don't want to support this method, but we have to for
		// ICollection<T> compatibility. So we're going to ruthlessly
		// verify that the metric object was created correctly.

		if (item == null) {
			throw new NullPointerException("A metric item must be provided to add it to the collection.");
		}

		// make sure the metric is for the right definition, namely our definition.
		if (!item.getDefinition().equals(this.metricDefinition)) {
			throw new IndexOutOfBoundsException(
					"The provided metric item is not related to the metric definition that owns this metrics collection.");
		}

		// we're about to modify the collection, get a lock. We don't want the lock to
		// cover the changed event since
		// we really don't know how long that will take, and it could be deadlock prone.
		synchronized (this.lock) {
			// make sure we don't already have it
			if (this.dictionary.containsKey(item.getId())) {
				throw new IllegalArgumentException("The specified metric item is already in the collection.");
			}

			if (this.dictionaryByName.containsKey(item.getName())) {
				throw new IllegalArgumentException("A metric item for the same metric is already in the collection.");
			}

			// add it to all of our collections, and to the definition metric cache.
			this.dictionary.put(item.getId(), item);
			this.dictionaryByName.put(item.getName(), item);
			this.list.add(item);
			this.metricDefinition.getDefinitions().addMetric(item);
			return true;
		}

	}

	/**
	 * Indicates whether the specified metric object is contained in this
	 * dictionary.
	 *
	 * @param item The non-null object to look for.
	 * @return true, if successful
	 */
	public final boolean contains(Metric item) {
		synchronized (this.lock) {
			// here we are relying on the fact that the comment object implements
			// IComparable sufficiently to guarantee uniqueness
			return this.list.contains(item);
		}
	}

	/**
	 * Copies the entire contents of the dictionary into the provided array starting
	 * at the specified index.
	 * 
	 * The provided array must be large enough to contain the entire contents of
	 * this dictionary starting with the specified index.
	 * 
	 * @param array      The existing array to copy the dictionary into
	 * @param arrayIndex The zero-based index to start copying from.
	 */
	public final void copyTo(Metric[] array, int arrayIndex) {
		synchronized (this.lock) {
			for (Metric def : this.list) {
				array[arrayIndex] = def;
				arrayIndex++;
			}
		}
	}

	/**
	 * Copy the entire collection of metric instances into a new array.
	 * 
	 * @return A new array containing all of the metric instances in this
	 *         collection.
	 */
	public final Metric[] toArray() {
		synchronized (this.lock) {
			return this.list.toArray(new Metric[0]);
		}
	}

	/**
	 * The number of items in the dictionary.
	 *
	 * @return the count
	 */
	public final int getCount() {
		synchronized (this.lock) {
			return this.list.size();
		}
	}

	/**
	 * Indicates whether the dictionary is read-only (meaning no new metrics can be
	 * added) or not.
	 *
	 * @return true, if is read only
	 */
	public final boolean isReadOnly() {
		return true;
	}

	/**
	 * Removing objects is not supported.
	 * 
	 * This method is implemented only for ICollection interface support and will
	 * throw an exception if called.
	 *
	 * @param item The Metric item to remove.
	 * @return true, if successful
	 */
	public final boolean remove(Metric item) {
		throw new UnsupportedOperationException();
	}

}