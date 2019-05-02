package com.onloupe.agent.metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.onloupe.core.serialization.monitor.EventMetricValueDefinitionPacket;
import com.onloupe.core.util.OutObject;
import com.onloupe.core.util.TypeUtils;

/**
 * A collection of event values for the parent event metric definition.
 * 
 * This object is automatically created by the Event Metric Definition and is
 * accessible through the Values property.
 */
public final class EventMetricValueDefinitionCollection {
	private final Map<String, EventMetricValueDefinition> dictionary = new HashMap<String, EventMetricValueDefinition>();
	private final List<EventMetricValueDefinition> list = new ArrayList<EventMetricValueDefinition>();
	private EventMetricDefinition definition;


	public List<EventMetricValueDefinition> getList() {
		return list;
	}

	/**
	 * Create a new values dictionary for the specified metric definition
	 * 
	 * @param definition The parent metric definition object that will own this
	 *                   dictionary.
	 */
	public EventMetricValueDefinitionCollection(EventMetricDefinition definition) {
		this.definition = definition;
	}

	/**
	 * Create a new value definition with the supplied name and type. The name must
	 * be unique in this collection.
	 * 
	 * Internally, only simple type are supported. Any non-numeric,
	 * non-DateTimeOffset type will be converted to a string using the default
	 * ToString capability when it is recorded.
	 * 
	 * @param name The unique name for this value definition.
	 * @param type The simple type of this value.
	 * @return The newly created value definition.
	 */
	public EventMetricValueDefinition add(String name, java.lang.Class type) {
		// forward the call to our larger add method
		return add(name, type, SummaryFunction.COUNT, null, null, null);
	}

	/**
	 * Create a new value definition with the supplied name and type. The name must
	 * be unique in this collection.
	 * 
	 * Internally, only simple type are supported. Any non-numeric,
	 * non-DateTimeOffset type will be converted to a string using the default
	 * ToString capability when it is recorded.
	 * 
	 * @param name            The unique name for this value definition.
	 * @param type            The simple type of this value.
	 * @param summaryFunction The default way that individual samples of this value
	 *                        column can be aggregated to create a graphable
	 *                        summary. (Use SummaryFunction.Count for non-numeric
	 *                        types.)
	 * @param unitCaption     A displayable caption for the units this value
	 *                        represents, or null for unit-less values.
	 * @param caption         The end-user display caption for this value.
	 * @param description     The end-user description for this value.
	 * @return The newly created value definition.
	 * @exception ArgumentNullException The provided name or type are null.
	 * @exception ArgumentException     There is already a definition with the
	 *                                  provided name
	 */
	public EventMetricValueDefinition add(String name, java.lang.Class type, SummaryFunction summaryFunction,
			String unitCaption, String caption, String description) {
		synchronized (this.definition.getLock()) {
			// if we are read-only, you can't add a new value
			if (isReadOnly()) {
				throw new IllegalStateException("The collection is read-only");
			}

			// make sure we got everything we require
			if (TypeUtils.isBlank(name)) {
				throw new NullPointerException("name");
			}

			if (type == null) {
				throw new NullPointerException("type");
			}

			// make sure the name is unique
			if (containsKey(name)) {
				throw new IllegalArgumentException("There is already a value definition with the provided name.");
			}

			// create a new value definition
			EventMetricValueDefinitionPacket newPacket = new EventMetricValueDefinitionPacket(
					this.definition.getPacket(), name, type, caption, description);
			EventMetricValueDefinition newDefinition = new EventMetricValueDefinition(this.definition, newPacket);
			newDefinition.setCaption(caption);
			newDefinition.setUnitCaption(unitCaption);
			newDefinition.setDescription(description);
			newDefinition.setSummaryFunction(summaryFunction.getValue());
			
			list.add(newDefinition);
			dictionary.put(newDefinition.getName(), newDefinition);
			
			// and return the new object to our caller so the have the object we created
			// from their input.
			return newDefinition;
		}
	}

	/**
	 * The metric definition this value is associated with.
	 */
	public EventMetricDefinition getDefinition() {
		return this.definition;
	}

	/**
	 * Determines whether the collection contains an element with the specified key.
	 * 
	 * @param name The value name to locate in the collection
	 * @return True if the collection contains an element with the key; otherwise,
	 *         false.
	 */
	public boolean containsKey(String name) {
		// gateway to our alternate inner dictionary
		return this.dictionary.containsKey(name);
	}

	/**
	 * Retrieve an item from the collection by its key if present. If not present,
	 * the default value of the object is returned.
	 * 
	 * @param name  The key of the value to get.
	 * @param value When this method returns, contains the value associated with the
	 *              specified key, if the key is found; otherwise, the default value
	 *              for the type of the value parameter. This parameter is passed
	 *              uninitialized.
	 * @return true if the collection contains an element with the specified key;
	 *         otherwise false.
	 */
	public boolean tryGetValue(String name, OutObject<EventMetricValueDefinition> value) {
		if (isReadOnly()) // Don't need the lock once we're read-only.
		{
			// gateway to our inner dictionary try get value
			value.argValue = this.dictionary.get(name);
			return value.argValue != null;
		}

		synchronized (this.definition.getLock()) // But we do need the lock when it may still be changing.
		{
			// gateway to our inner dictionary try get value
			value.argValue = this.dictionary.get(name);
			return value.argValue != null;
		}
	}

	/**
	 * Retrieve metric object by its name
	 * 
	 * @param name
	 * @return
	 */
	public EventMetricValueDefinition get(String name) {
		synchronized (this.definition.getLock()) {
			return (this.dictionary.get(name));
		}
	}
	
	/**
	 * Retrieve the metric definition by numeric index in collection.
	 * 
	 * @param index
	 * @return
	 */
	public final EventMetricValueDefinition get(int index) {
		if (isReadOnly()) // Don't need the lock once we're read-only.
		{
			return this.list.get(index);
		}

		synchronized (this.definition.getLock()) // But we do need the lock when it may still be changing.
		{
			return this.list.get(index);
		}
	}
	
	//////////////////////////////////////////////////////

	/**
	 * Create a new value definition with the supplied name and type. The name must
	 * be unique in this collection
	 * 
	 * Internally, only simple type are supported. Any non-numeric,
	 * non-DateTimeOffset type will be converted to a string using the default
	 * ToString capability when it is recorded.
	 * 
	 * @param name        The unique name for this value definition
	 * @param type        The simple type of this value
	 * @param caption     The end-user display caption for this value
	 * @param description The end-user description for this value.
	 * @return The newly created value definition
	 */
	public final EventMetricValueDefinition add(String name, java.lang.Class type, String caption, String description) {
		synchronized (this.definition.getLock()) // Is this really needed? Can't hurt....
		{
			// if we are read-only, you can't add a new value
			if (isReadOnly()) {
				throw new UnsupportedOperationException("The collection is read-only");
			}

			// make sure we got everything we require
			if (TypeUtils.isBlank(name)) {
				throw new NullPointerException("name");
			}

			if (type == null) {
				throw new NullPointerException("type");
			}

			// make sure the name is unique
			if (this.dictionary.containsKey(name)) {
				throw new IllegalArgumentException("There is already a value definition with the provided name.");
			}

			// create a new value definition
			EventMetricValueDefinitionPacket newPacket = new EventMetricValueDefinitionPacket(
					this.definition.getPacket(), name, type, caption, description);
			EventMetricValueDefinition newDefinition = new EventMetricValueDefinition(this.definition, newPacket);

			// forward the call to our one true add method
			// add(newDefinition);

			// and return the new object to our caller so the have the object we created
			// from their input.
			return newDefinition;
		}
	}
	
	/////////////////////////////////////////////////////

	/**
	 * Add an existing value definition item to this collection
	 * 
	 * @param item An existing value definition item associated with our metric
	 *             definition
	 * @exception ArgumentNullException The provided item was null.
	 * @exception ArgumentException     The provided value definition item is not
	 *                                  associated with our metric definition -or-
	 *                                  there is already a value definition with the
	 *                                  same name as the provided item.
	 */
	public final boolean add(EventMetricValueDefinition item) {
		// we can't do the read-only check here because we use this method to add
		// existing objects during rehydration

		// make sure the input isn't null
		if (item == null) {
			throw new NullPointerException("item");
		}

		// make sure it's associated with our definition
		if (item.getDefinition() != this.definition) {
			throw new IllegalArgumentException(
					"The provided value definition item is not associated with our metric definition");
		}

		synchronized (this.definition.getLock()) {
			// and make sure it isn't a duplicate key
			if (this.dictionary.containsKey(item.getName())) {
				throw new IllegalArgumentException(
						String.format("There is already a value definition item with the name %1$s", item.getName()));
			}

			// and finally what the hell, go ahead and add it.
			this.list.add(item);
			this.dictionary.put(item.getName(), item);
		}
		return true;
	}

	/**
	 * Clearing objects is not supported.
	 * 
	 * This method is implemented only for ICollection interface support and will
	 * throw an exception if called.
	 * 
	 * @exception InvalidOperationException The definition has been committed and is
	 *                                      now read-only
	 */
	public void clear() {
		synchronized (this.definition.getLock()) {
			if (isReadOnly()) {
				throw new IllegalStateException("The collection is read-only");
			}

			this.dictionary.clear();
		}

		// and raise the event so our caller knows we're cleared
		// OnCollectionChanged(new
		// CollectionChangedEventArgs<EventMetricValueDefinitionCollection,
		// EventMetricValueDefinition>(this, null, CollectionAction.Cleared));
	}

	/**
	 * Indicates whether the collection already contains the specified definition
	 * object
	 * 
	 * Even if the object doesn't exist in the collection, if another object with
	 * the same key exists then an exception will be thrown if the supplied object
	 * is added to the collection. See Add for more information.
	 * 
	 * @param item The event metric value definition object to look for
	 * @return True if the object already exists in the collection, false otherwise
	 */
	public final boolean contains(EventMetricValueDefinition item) {
		// here we are relying on the fact that the comment object implements
		// IComparable sufficiently to guarantee uniqueness
		if (isReadOnly()) // Don't need the lock once we're read-only.
		{
			return this.list.contains(item);
		}

		synchronized (this.definition.getLock()) // But we do need the lock when it may still be changing.
		{
			return this.list.contains(item);
		}
	}

	/**
	 * Copy the current list of event metric value definitions into the provided
	 * array starting at the specified index.
	 * 
	 * The array must be large enough to handle the entire contents of this
	 * dictionary starting at the provided array index.
	 * 
	 * @param array      The array to copy into
	 * @param arrayIndex The index to start inserting at
	 */
	public final void copyTo(EventMetricValueDefinition[] array, int arrayIndex) {
		if (isReadOnly()) // Don't need the lock once we're read-only.
		{
			for (EventMetricValueDefinition def : this.list) {
				array[arrayIndex] = def;
				arrayIndex++;
			}
		} else {
			synchronized (this.definition.getLock()) // But we do need the lock when it may still be changing.
			{
				for (EventMetricValueDefinition def : this.list) {
					array[arrayIndex] = def;
					arrayIndex++;
				}
			}
		}
	}

	/**
	 * Copy the current list of event metric value definitions into a new array.
	 * 
	 * @return A new array containing all of the event metric value definitions in
	 *         this collection.
	 */

	public final EventMetricValueDefinition[] toArray() {
		if (isReadOnly()) // Don't need the lock once we're read-only.
		{
			return this.list.toArray(new EventMetricValueDefinition[0]);
		}

		synchronized (this.definition.getLock()) // But we do need the lock when it may still be changing.
		{
			return this.list.toArray(new EventMetricValueDefinition[0]);
		}
	}

	/**
	 * The number of items currently in the dictionary.
	 */
	public int size() {
		return this.dictionary.size();
	}

	/**
	 * The number of items currently in the dictionary.
	 */
	public int getCount() {
		return size();
	}
	
	/**
	 * Indicates if the dictionary is considered read only.
	 */
	public boolean isReadOnly() {
		return this.definition.isReadOnly();
	}

	// Note: Apparently this documentation is out of date? Remove apparently *is*
	// supported unless IsReadOnly.
	/**
	 * Removing objects is not supported.
	 * 
	 * This method is implemented only for ICollection interface support and will
	 * throw an exception if called.
	 * 
	 * @param item The EventMetricValueDefinition item to remove.
	 */
	public final boolean remove(EventMetricValueDefinition item) {
		boolean itemRemoved = false;

		synchronized (this.definition.getLock()) {
			if (isReadOnly()) {
				throw new UnsupportedOperationException("The collection is read-only");
			}

			// do a safe remove of the victim in the dictionary and list, if they are still
			// present
			// if they aren't, we have a problem
			if (this.dictionary.containsKey(item.getName())) {
				this.dictionary.remove(item.getName());
				itemRemoved = true;
			}

			if (this.list.contains(item)) {
				this.list.remove(item);
				itemRemoved = true;
			}
		}

		// and fire our event if there was really something to remove
//		if (itemRemoved) {
//			onCollectionChanged(
//					new CollectionChangedEventArgs<EventMetricValueDefinitionCollection, EventMetricValueDefinition>(
//							this, item, CollectionAction.REMOVED));
//		}

		return itemRemoved;
	}
	
	/**
	 * Scan the collection and mark each value with its index (only once IsReadOnly
	 * is true).
	 */
	public final void setAllIndex() {
		synchronized (this.definition.getLock()) {
			if (isReadOnly()) // Don't do it until the definition is actually locked.
			{
				int index = 0;
				for (EventMetricValueDefinition valueDefinition : this.list) {
					valueDefinition.setMyIndex(index); // Efficiently set the cached index for all value columns.
					index++;
				}
			}
		}
	}
	
	public List<EventMetricValueDefinition> getEventMetricValueDefinitions() {
		return this.list;
	}

	/*
	 * /// <summary> /// This method is called every time a collection change event
	 * occurs to allow inheritors to override the change event. /// </summary> ///
	 * <remarks>If overridden, it is important to call this base implementation to
	 * actually fire the event.</remarks> /// <param name="e"></param> private void
	 * OnCollectionChanged(CollectionChangedEventArgs<
	 * EventMetricValueDefinitionCollection, EventMetricValueDefinition> e) { //save
	 * the delegate field in a temporary field for thread safety
	 * EventHandler<CollectionChangedEventArgs<EventMetricValueDefinitionCollection,
	 * EventMetricValueDefinition>> tempEvent = CollectionChanged;
	 * 
	 * if (tempEvent != null) { tempEvent(this, e); } }
	 */

//	private void wrappedCollectionCollectionChanged(Object sender,
//			CollectionChangedEventArgs<EventMetricValueDefinitionCollection, EventMetricValueDefinition> e1) {
//		// We should already have the lock?
//		synchronized (this.definition.getLock()) {
//			// Invalidate our cache of the d values list, so it will be rebuilt
//			// on next access.
//			this.list = null;
//		}
//	}

}