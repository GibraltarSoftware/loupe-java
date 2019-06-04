package com.onloupe.core.metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.onloupe.core.util.OutObject;
import com.onloupe.core.util.TypeUtils;

// TODO: Auto-generated Javadoc
/** 
 A collection of metric definitions, keyed by their unique Id and Key.
 
 
 A metric definition has a unique ID to identify a particular instance of the definition (associated with one session) 
 and a Key which is unique within a session but is designed for comparison of the same definition between sessions.
 
*/
public final class MetricDefinitionCollection
{
	
	/** The metric by id. */
	private final Map<UUID, Metric> metricById = new HashMap<UUID, Metric>();
	
	/** The dictionary by name. */
	private final Map<String, IMetricDefinition> dictionaryByName = new HashMap<String, IMetricDefinition>();
	
	/** The dictionary. */
	private final Map<UUID, IMetricDefinition> dictionary = new HashMap<UUID, IMetricDefinition>();
	
	/** The list. */
	private final List<IMetricDefinition> list = new ArrayList<IMetricDefinition>();
	
	/** The lock. */
	private final Object lock = new Object();

	/**
	 * Add a metric to the definition metric cache. Used by the MetricCollection
	 * base class to flatten the hierarchy.
	 * 
	 * @param newMetric The metric object to add to the cache.
	 */
	public void addMetric(Metric newMetric) {
		synchronized (this.lock) {
			this.metricById.put(newMetric.getId(), newMetric);
		}
	}

	/**
	 * Remove a metric to the definition metric cache. Used by the MetricCollection
	 * base class to flatten the hierarchy.
	 * 
	 * @param victimMetric The metric object to remove from the cache.
	 */
	public void removeMetric(Metric victimMetric) {
		synchronized (this.lock) {
			this.metricById.remove(victimMetric.getId());
		}
	}

	/**
	 * Object Change Locking object.
	 *
	 * @return the lock
	 */
	public Object getLock() {
		return this.lock;
	}


	/*
	/// <summary>
	/// Retrieve a metric given its unique Id.
	/// </summary>
	/// <param name="metricId">The unique Id of the metric to retrieve</param>
	/// <returns></returns>
	public EventMetric EventMetric(Guid metricId)
	{
	    // Have the internal definition collection look up the metric instance.
	    Metric metric = _WrappedCollection.Metric(metricId);

	    // Get its internal definition, and get its wrapper, which we track locally.
	    IMetricDefinition definition = (metric.Definition);
	    EventMetricDefinition eventDefinition = definition as EventMetricDefinition;
	    if (eventDefinition == null)
	        return null; // Wrong kind of metric definition!

	    EventMetric eventMetric = metric as EventMetric;

	    // Then ask the definition's metrics collection for the metric wrapper, which it tracks.
	    return eventDefinition.Metrics.(eventMetric);
	}

	/// <summary>
	/// Retrieve a metric given its unique Id.
	/// </summary>
	/// <param name="metricId">The unique Id of the metric to retrieve</param>
	/// <returns></returns>
	public SampledMetric SampledMetric(Guid metricId)
	{
	    // Have the internal definition collection look up the metric instance.
	    Metric metric = _WrappedCollection.Metric(metricId);

	    // Get its internal definition, and get its wrapper, which we track locally.
	    IMetricDefinition definition = (metric.Definition);
	    SampledMetricDefinition sampledDefinition = definition as SampledMetricDefinition;
	    if (sampledDefinition == null)
	        return null; // Wrong kind of metric definition!

	    CustomSampledMetric sampledMetric = metric as CustomSampledMetric;

	    // Then ask the definition's metrics collection for the metric wrapper, which it tracks.
	    return sampledDefinition.Metrics.(sampledMetric);
	}
	*/

	/**
	 * Determines whether the collection contains an element with the specified key.
	 * 
	 * @param key The key to locate in the collection
	 * @return true if the collection contains an element with the key; otherwise,
	 *         false.
	 */
	public boolean containsKey(UUID key) {
		synchronized (this.lock) {
			// gateway to our inner dictionary
			return this.dictionary.containsKey(key);
		}
	}


	/**
	 * Determines whether the collection contains an element with the specified key.
	 * 
	 * @param name The metric name to locate in the collection
	 * @return true if the collection contains an element with the key; otherwise,
	 *         false.
	 */
	public boolean containsKey(String name) {
		// protect ourself from a null before we do the trim (or we'll get an odd user
		// the error won't understand)
		if (TypeUtils.isBlank(name)) {
			throw new NullPointerException("name");
		}

		synchronized (this.lock) {
			// gateway to our alternate inner dictionary
			return this.dictionaryByName.containsKey(name.trim());
		}
	}

	/**
	 * Determines whether the collection contains an element with the specified key.
	 * 
	 * @param metricTypeName The unique metric type
	 * @param categoryName   The name of the category with which this definition is
	 *                       associated.
	 * @param counterName    The name of the definition within the category.
	 * @return true if the collection contains an element with the key; otherwise,
	 *         false.
	 */
	public boolean containsKey(String metricTypeName, String categoryName, String counterName) {
		// get the key for the provided values
		String key = MetricDefinition.getKey(metricTypeName, categoryName, counterName);

		synchronized (this.lock) {
			// gateway to our alternate inner dictionary
			return this.dictionaryByName.containsKey(key);
		}
	}

	/**
	 * Determines whether the collection contains an element with the specified key.
	 * 
	 * @param key The metric name to locate in the collection
	 * @return true if the collection contains an element with the key; otherwise,
	 *         false.
	 */
	public boolean containsMetricKey(UUID key) {
		synchronized (this.lock) {
			// gateway to our alternate inner dictionary
			return this.metricById.containsKey(key);
		}
	}
	/** 
	 Retrieve an item from the collection by its key if present.  If not present, the default value of the object is returned.
	 
	 @param id The metric definition Id of the value to get.
	 @param value When this method returns, contains the value associated with the specified key, if the key is found; otherwise, the default value for the type of the value parameter. This parameter is passed uninitialized.
	 @return true if the collection contains an element with the specified key; otherwise false.
	*/
	public boolean tryGetValue(UUID id, OutObject<IMetricDefinition> value)
	{

		synchronized (this.lock) {
			// gateway to our inner dictionary try get value
			value.argValue = this.dictionary.get(id);
			return value.argValue != null;
		}
	}

	/**
	 *  
	 * 	 Retrieve an item from the collection by its key if present.  If not present, the default value of the object is returned.
	 * 	 
	 *
	 * @param name The metric name to locate in the collection
	 * @param value When this method returns, contains the value associated with the specified key, if the key is found; otherwise, the default value for the type of the value parameter. This parameter is passed uninitialized.
	 * @return true if the collection contains an element with the specified key; otherwise false.
	 */
	public boolean tryGetValue(String name, OutObject<IMetricDefinition> value)
	{
		// protect ourself from a null before we do the trim (or we'll get an odd user
		// the error won't understand)
		if (TypeUtils.isBlank(name)) {
			throw new NullPointerException("name");
		}

		synchronized (this.lock) {
			// gateway to our inner dictionary try get value
			value.argValue = this.dictionaryByName.get(name.trim());
			return value.argValue != null;
		}
	}

	/**
	 *  
	 * 	 Retrieve an item from the collection by its key if present.  If not present, the default value of the object is returned.
	 * 	 
	 *
	 * @param metricsSystem The metrics capture system label.
	 * @param categoryName The name of the category with which this definition is associated.
	 * @param counterName The name of the definition within the category.
	 * @param value When this method returns, contains the value associated with the specified key, if the key is found; otherwise, the default value for the type of the value parameter. This parameter is passed uninitialized.
	 * @return true if the collection contains an element with the specified key; otherwise false.
	 */
	public boolean tryGetValue(String metricsSystem, String categoryName, String counterName, OutObject<IMetricDefinition> value)
	{
		// get the key for the provided values
		String key = MetricDefinition.getKey(metricsSystem, categoryName, counterName);

		synchronized (this.lock) {
			// gateway to our inner dictionary try get value
			value.argValue = (this.dictionaryByName.get(key));
			return value.argValue != null;
		}
	}

	/**
	 *  
	 * 	 Retrieve metric packet by numeric index in collection. 
	 * 	 
	 *
	 * @param index the index
	 * @return the i metric definition
	 */
	public IMetricDefinition get(int index)
	{
		synchronized (this.lock) {
			return this.list.get(index);
		}
	}

	/**
	 *  
	 * 	 Retrieve metric definition object by its Id.
	 * 	 
	 *
	 * @param id the id
	 * @return the i metric definition
	 */
	public IMetricDefinition get(UUID id)
	{
		synchronized (this.lock) {
			return this.dictionary.get(id);
		}
	}

	/**
	 *  
	 * 	 Retrieve metric definition object by its Key.
	 * 	 
	 *
	 * @param key the key
	 * @return the i metric definition
	 */
	public IMetricDefinition get(String key)
	{
		// protect ourself from a null before we do the trim (or we'll get an odd user
		// the error won't understand)
		if (TypeUtils.isBlank(key)) {
			throw new NullPointerException("name");
		}

		synchronized (this.lock) {
			return this.dictionaryByName.get(key.trim());
		}
	}

	/**
	 *  
	 * 	 Retrieve metric definition object by its metrics system, category, and counter names.
	 * 	 
	 *
	 * @param metricsSystem The metrics capture system label.
	 * @param categoryName The name of the category with which this definition is associated.
	 * @param counterName The name of the definition within the category.
	 * @return the i metric definition
	 */
	public IMetricDefinition get(String metricsSystem, String categoryName, String counterName)
	{
		// create the key from the parts we got
		String key = MetricDefinition.getKey(metricsSystem, categoryName, counterName);
		synchronized (this.lock) {
			return this.dictionaryByName.get(key);
		}
	}

	/**
	 *  
	 * 	 Add an existing IMetricDefinition item to this collection.
	 * 	 
	 * 	 If the supplied MetricDefinitin item is already in the collection, an exception will be thrown.
	 *
	 * @param item The new IMetricDefinition item to add.
	 * @return true, if successful
	 */
	public boolean add(IMetricDefinition item)
	{
		// we really don't want to support this method, but we have to for
		// ICollection<T> compatibility. So we're going to ruthlessly
		// verify that the metric object was created correctly.

		if (item == null) {
			throw new NullPointerException("item");
		}

		// we're about to modify the collection, get a lock. We don't want the lock to
		// cover the changed event since
		// we really don't know how long that will take, and it could be deadlock prone.
		synchronized (this.lock) {
			// make sure we don't already have it
			if (this.dictionary.containsKey(item.getId())) {
				throw new IllegalArgumentException(
						"The specified metric definition item is already in the collection.");
			}

			if (this.dictionaryByName.containsKey(item.getName())) {
				throw new IllegalArgumentException(
						"A metric definition item for the same metric is already in the collection.");
			}

			// add it to both lookup collections
			this.dictionary.put(item.getId(), item);
			this.dictionaryByName.put(item.getName(), item);
			return this.list.add(item);
		}		
	}

	/** 
	 Clearing objects is not supported.
	 
	 This method is implemented only for ICollection interface support and will throw an exception if called.
	*/
	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	/** 
	 Determines whether an element is in the collection.
	 
	 This method determines equality using the default equality comparer for the type of values in the list.  It performs
	 a linear search and therefore is an O(n) operation.
	 @param item The object to locate in the collection.
	 @return true if the item is found in the collection; otherwise false.
	*/
	public boolean contains(MetricDefinition item) {
		synchronized (this.lock) {
			// here we are relying on the fact that the comment object implements
			// IComparable sufficiently to guarantee uniqueness
			return this.list.contains(item);
		}
	}

	/** 
	 Copies the entire collection to a compatible one-dimensional array, starting at the specified index of the target array.
	 
	 Elements are copied to the array in the same order in which the enumerator iterates them from the collection.  The provided array 
	 must be large enough to contain the entire contents of this collection starting at the specified index.  This method is an O(n) operation.
	 @param array The one-dimensional array that is the destination of the elements copied from the collection.  The Array must have zero-based indexing.
	 @param arrayIndex The zero-based index in array at which copying begins.
	*/
	public void copyTo(IMetricDefinition[] array, int arrayIndex) {
		synchronized (this.lock) {
			for (IMetricDefinition def : this.list) {
				array[arrayIndex] = def;
				arrayIndex++;
			}
		}
	}

	/** 
	 Copy the entire collection of metric definitions into a new array.
	 
	 @return A new array containing all of the metric definitions in this collection.
	*/
	public MetricDefinition[] toArray()
	{
		synchronized (this.lock) {
			return this.list.toArray(new MetricDefinition[0]);
		}
	}

	/**
	 *  
	 * 	 The number of items currently in the collection.
	 *
	 * @return the int
	 */
	public int size()
	{
		return this.list.size();
	}

	/**
	 *  
	 * 	 Indicates if the collection is read only and therefore can't have items added or removed.
	 * 	 
	 * 	 This collection is never read-only, however removing items is not supported.
	 * 	 This property is required for ICollection compatibility
	 *
	 * @return true, if is read only
	 */
	public boolean isReadOnly()
	{
		return false;
	}


}