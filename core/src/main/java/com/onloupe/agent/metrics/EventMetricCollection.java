package com.onloupe.agent.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.onloupe.core.util.OutObject;
import com.onloupe.core.util.TypeUtils;


/**
 * The collection of event metrics for a given event metric definition.
 */
public class EventMetricCollection {
    
    /** The dictionary by name. */
    private final Map<String, EventMetric> dictionaryByName = new HashMap<String, EventMetric>();
    
    /** The dictionary. */
    private final Map<UUID, EventMetric> dictionary = new HashMap<UUID, EventMetric>();
    
    /** The lock. */
    private final Object lock = new Object();
    
    /** The metric definition. */
    private final EventMetricDefinition metricDefinition;
    
	/**
	 * Create a new event metric dictionary for the provided definition.
	 * 
	 * This dictionary is created automatically by the Custom Sampled Metric
	 * Definition during its initialization.
	 * 
	 * @param metricDefinition The definition of the event metric to create a metric
	 *                         dictionary for
	 */
	public EventMetricCollection(EventMetricDefinition metricDefinition) {
		this.metricDefinition = metricDefinition;
	}

	/**
	 * Determines whether the collection contains an element with the specified key.
	 * 
	 * @param key The key to locate in the collection
	 * @return true if the collection contains an element with the key; otherwise,
	 *         false.
	 */
	public final boolean containsKey(UUID key) {
		// gateway to our inner dictionary
		return this.dictionary.containsKey(key);
	}

	/**
	 * Determines whether the collection contains an element with the specified key.
	 * 
	 * @param key The metric name to locate in the collection
	 * @return true if the collection contains an element with the key; otherwise,
	 *         false.
	 */
	public final boolean containsKey(String key) {
		// gateway to our alternate inner dictionary
		return this.dictionaryByName.containsKey(key);
	}

	/**
	 * Create a new metric object with the provided instance name and add it to the
	 * collection.
	 *
	 * @param instanceName The instance name to use, or blank or null for the
	 *                     default metric.
	 * @return The new metric object that was added to the collection
	 */
	public final EventMetric add(String instanceName) {
		synchronized (this.lock) {
			// Create a new metric object with the provided instance name (it will get added
			// to us automatically)
			EventMetric newMetric = new EventMetric(this.metricDefinition, instanceName);
			this.dictionary.put(newMetric.getId(), newMetric);
			this.dictionaryByName.put(newMetric.getInstanceName(), newMetric);
			// finally, return the newly created metric object to our caller
			return newMetric;
		}
	}

	/*
	 * /// <summary>Creates a new metric instance or returns an existing one by
	 * inspecting the provided object for EventMetricDefinition
	 * attributes.</summary> /// <remarks>If the metric doesn't exist, it will be
	 * created. /// If the metric definition does exist, but is not an Event Metric
	 * (or a derived class) an exception will be thrown. /// If the metric
	 * definition isn't bound to an object type, the default metric will be
	 * returned. /// The provided object must not be null and must be of the type
	 * the metric definition owning this dictionary is bound to.</remarks> ///
	 * <param name="userDataObject">The object to create a metric from.</param> ///
	 * <returns>The event metric object for the specified event metric
	 * instance.</returns> public EventMetric AddOrGet(object userDataObject) {
	 * EventMetric newMetric;
	 * 
	 * //we need a live object, not a null object or we'll fail if (userDataObject
	 * == null) { throw new ArgumentNullException("userDataObject"); }
	 * 
	 * //great. We now know a lot - namely that it has to have the right attributes,
	 * etc. to define a metric so we can //now go and find all of the information we
	 * need to create a new metric. string instanceName = null; if
	 * (Definition.NameBound) { //we don't even need to get it - we just care that
	 * it's defined. try { //To be righteous, we need to only invoke the member
	 * we're looking at BindingFlags methodBinding; switch
	 * (Definition.NameMemberType) { case MemberTypes.Field: methodBinding =
	 * BindingFlags.GetField; break; case MemberTypes.Method: methodBinding =
	 * BindingFlags.InvokeMethod; break; case MemberTypes.Property: methodBinding =
	 * BindingFlags.GetProperty; break; default: throw new
	 * ArgumentOutOfRangeException(); }
	 * 
	 * //invoke the bound instance name from the type our definition is associated
	 * with. This way if the object provided //has multiple implementations that are
	 * metric-enabled, we use the correct one. object rawValue =
	 * Definition.BoundType.InvokeMember(Definition.NameMemberName, methodBinding,
	 * null, userDataObject, null, Locale.ROOT);
	 * 
	 * //and the raw value is either null or something we're going to convert to a
	 * string if (rawValue == null) { instanceName = null; } else { instanceName =
	 * rawValue.ToString(); } } catch (Exception ex) { //just trace log this - we
	 * can continue, they'll just get the default instance until they fix their
	 * code. Trace.
	 * TraceWarning("Unable to retrieve the instance name to create a specific %s metric because an exception occurred while accessing the member {1}: {2}"
	 * , Definition.Key, Definition.NameMemberName, ex.ToString()); } }
	 * 
	 * //now that we have our instance name, we go ahead and see if there is already
	 * an instance with the right name or just add it lock (this.Lock) //make sure
	 * the try & add are atomic { if (TryGetValue(instanceName, out newMetric) ==
	 * false) { //there isn't one with the right name, we need to create it.
	 * newMetric = Add(instanceName); } }
	 * 
	 * //return what we got - we have an object one way or another, or we threw an
	 * exception. return newMetric; }
	 */

	/**
	 * The definition of all of the metrics in this collection.
	 *
	 * @return the definition
	 */
	public final EventMetricDefinition getDefinition() {
		return this.metricDefinition;
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
	public final boolean tryGetValue(UUID key, OutObject<EventMetric> value) {
		synchronized (this.lock) {
			// We are playing a few games to get native typing here.
			// Because it's an out value, we have to swap types around ourselves so we can
			// cast.
			return dictionary.containsKey(key);
		}
	}

	/**
	 * Retrieve an item from the collection by its key if present. If not present,
	 * the default value of the object is returned.
	 * 
	 * @param key   The metric name to locate in the collection
	 * @param value When this method returns, contains the value associated with the
	 *              specified key, if the key is found; otherwise, the default value
	 *              for the type of the value parameter. This parameter is passed
	 *              uninitialized.
	 * @return true if the collection contains an element with the specified key;
	 *         otherwise false.
	 */
	public final boolean tryGetValue(String key, OutObject<EventMetric> value) {
		synchronized (this.lock) {
			// We are playing a few games to get native typing here.
			// Because it's an out value, we have to swap types around ourselves so we can
			// cast.
			value.argValue = dictionaryByName.get(TypeUtils.trimToNull(key));
			return value.argValue != null;
		}
	}

	/**
	 * Retrieve event metric object by its Id.
	 *
	 * @param id the id
	 * @return the event metric
	 */
	public final EventMetric get(UUID id) {
		synchronized (this.lock) {
			return this.dictionary.get(id);
		}
	}

	/**
	 * Retrieve event metric object by its name.
	 *
	 * @param key the key
	 * @return the event metric
	 */
	public final EventMetric get(String key) {
		synchronized (this.lock) {
			return this.dictionaryByName.get(TypeUtils.trimToNull(key));
		}
	}

	/**
	 * Object Change Locking object.
	 *
	 * @return the lock
	 */
	public final Object getLock() {
		return this.lock;
	}

}