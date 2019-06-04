package com.onloupe.agent.metrics;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.UUID;

import com.onloupe.core.logging.Log;
import com.onloupe.core.metrics.IMetricDefinition;
import com.onloupe.core.metrics.MetricDefinition;
import com.onloupe.core.metrics.MetricDefinitionCollection;
import com.onloupe.core.serialization.monitor.EventMetricPacket;
import com.onloupe.core.serialization.monitor.EventMetricSamplePacket;
import com.onloupe.core.util.OutObject;
import com.onloupe.core.util.SystemUtils;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.metric.MemberType;

// TODO: Auto-generated Javadoc
/**
 * The Class EventMetric.
 */
public final class EventMetric {
	
	/** The packet. */
	private EventMetricPacket packet;
	
	/** The metric definition. */
	private EventMetricDefinition metricDefinition;

	/**
	 * Creates a new event metric object from the metric definition looked up with
	 * the provided key information. The metric definition must already exist or an
	 * exception will be raised.
	 *
	 * @param collection the collection
	 * @param metricTypeName The unique metric type
	 * @param categoryName   The name of the category with which this definition is
	 *                       associated.
	 * @param counterName    The name of the definition within the category.
	 * @param instanceName   The unique name of this instance within the metric's
	 *                       collection.
	 */
	protected EventMetric(MetricDefinitionCollection collection, String metricTypeName, String categoryName, String counterName, String instanceName) {
		this((EventMetricDefinition) collection
				.get(MetricDefinition.getKey(metricTypeName, categoryName, counterName)), instanceName);
	}

	/**
	 * Create a new event metric object from the provided metric definition
	 * 
	 * The new metric will automatically be added to the metric definition's metrics
	 * collection.
	 * 
	 * @param definition   The metric definition for the metric instance
	 * @param instanceName The unique name of this instance within the metric's
	 *                     collection.
	 */
	public EventMetric(EventMetricDefinition definition, String instanceName) {
		this.metricDefinition = definition;
		this.packet = new EventMetricPacket(definition.getPacket(), instanceName);
	}

	/**
	 * Registers all event metric definitions defined by attributes on the provided
	 * object or Type, and registers metric instances where EventMetricInstanceName
	 * attribute is also found (with a live object).
	 * 
	 * @param metricData An object or Type defining event metrics via attributes on
	 *                   itself or on its base types or interfaces.
	 * 
	 *                   <p>
	 *                   This call ensures that the reflection scan of all members
	 *                   looking for attributes across the entire inheritance of an
	 *                   object instance or Type has been done (e.g. outside of a
	 *                   critical path) so that the first call to
	 *                   Write will not have to do
	 *                   that work within a critical path. Results are cached
	 *                   internally, so redundant calls to this method will not
	 *                   repeat the scan for types already scanned (including as
	 *                   part of a different top-level type).
	 *                   </p>
	 *                   <p>
	 *                   If a live object is given (not just a Type) then the
	 *                   member(s) decorated with an EventMetricInstanceNameAttribute
	 *                   Class will be queried and used to also register an
	 *                   event metric instance with the returned name
	 *                   </p>
	 *                   <p>
	 *                   If a Type is given instead of a live object, it can't be
	 *                   queried for instance name(s) and will only register the
	 *                   event metric definitions. Metric instances will still be
	 *                   created automatically as needed when Write is called.
	 *                   </p>
	 * 
	 * @see Write(object) Write Method
	 *      <exception caption="" cref="ArgumentNullException">Thrown if metricData
	 *      is null. <exception caption="" cref="ArgumentException">The specified
	 *      metricDataObjectType does not have an EventMetric attribute &lt;br /&gt;
	 *      &lt;br /&gt; -or- &lt;br /&gt; &lt;br /&gt; The specified Type does not
	 *      have a usable EventMetric attribute, so it can't be used to define an
	 *      event metric.&lt;br /&gt; &lt;br /&gt; -or- &lt;br /&gt; &lt;br /&gt;
	 *      The specified Type's EventMetric has an empty metric namespace which is
	 *      not allowed, so no metric can be defined.&lt;br /&gt; &lt;br /&gt; -or-
	 *      &lt;br /&gt; &lt;br /&gt; The specified Type's EventMetric has an empty
	 *      metric category name which is not allowed, so no metric can be
	 *      defined.&lt;br /&gt; &lt;br /&gt; -or- &lt;br /&gt; &lt;br /&gt; The
	 *      specified Type's EventMetric has an empty metric counter name which is
	 *      not allowed, so no metric can be defined.&lt;br /&gt; &lt;br /&gt; -or-
	 *      &lt;br /&gt; &lt;br /&gt; The specified Type's EventMetric attribute's
	 *      3-part Key is already used for a metric definition which is not an event
	 *      metric. <example> See the <see cref="EventMetric">EventMetric Class
	 *      Overview</see> for an example.
	 *      <code title="" description="" lang="neutral"></code></example>
	 */
	public static void register(Object metricData) {
		// we need a live object, not a null object or we'll fail
		if (metricData == null) {
			throw new NullPointerException("metricData");
		}

		// Register all of the event metric definitions it contains, object or Type:
		EventMetricDefinition[] definitions = EventMetricDefinition.registerAll(metricData);

		if (!(metricData instanceof java.lang.Class)) {
			// They gave us a live object, not just a Type, so see if there are metric
			// instances we can register.
			for (EventMetricDefinition definition : definitions) {
				if (definition.isBound() && definition.getNameBound()) {
					String instanceName = definition.invokeInstanceNameBinding(metricData);

					if (instanceName != null) // null means it didn't find one, so we won't register an instance.
					{
						// An empty string (meaning the found value was null or empty) will be
						// registered (same as null).
						register(definition, instanceName);
					}
				}
			}
		}
	}

	/**
	 * Return a registered event metric instance for the provided event metric
	 * definition.
	 * <p>
	 * If the provided event metric definition is an unregistered raw definition, it
	 * will be registered as a completed definition (or a matching registered event
	 * metric definition will be used in place of it), but an inability to
	 * successfully register the definition will result in an ArgumentException, as
	 * with calling the Register() method in EventMetricDefinition. Using a
	 * properly-registered definition is preferred.
	 * </p>
	 * <p>
	 * If an event metric with that instance name already exists for that registered
	 * definition, it will be returned. Otherwise, one will be created from that
	 * definition and returned.
	 * </p>
	 * 
	 * @param definition   The metric definition for the desired metric instance.
	 * @param instanceName The desired instance name (may be null for the default
	 *                     instance).
	 * @return The EventMetric object for the requested event metric instance.
	 */
	public static EventMetric register(EventMetricDefinition definition, String instanceName) {
		if (definition == null) {
			// Uh-oh. They gave us a non-EventMetricDefinition?
			return null;
		}

		EventMetricDefinition metricDefinition;
		synchronized (definition.getLock()) {
			if (!definition.isReadOnly()) {
				// Uh-oh. They gave us a raw event metric definition which wasn't registered.
				// But they're calling Register(), so they'd expect us to complete registration
				// for them in this call.
				metricDefinition = definition.register();
			} else {
				// Assume this is a registered definition. ToDo: Make sure they only get
				// IsReadOnly when actually registered.
				metricDefinition = definition;
			}
		}

		EventMetric eventMetric;
		EventMetricCollection metrics = metricDefinition.getMetrics();
		synchronized (metrics.getLock()) {
			OutObject<EventMetric> tempOutEventMetric = new OutObject<EventMetric>();
			if (!metrics.tryGetValue(instanceName, tempOutEventMetric)) {
				eventMetric = tempOutEventMetric.argValue;
				eventMetric = metrics.add(instanceName);
			} else {
				eventMetric = tempOutEventMetric.argValue;
			}
		}
		return eventMetric;
	}
	
	//////////////////////////////////////////////////////
	
	/**
	 * Creates a new metric instance or returns an existing one from the provided
	 * definition information, or returns any existing instance if found. If the
	 * metric definition doesn't exist, it will be created. If the metric doesn't
	 * exist, it will be created. If the metric definition does exist, but is not an
	 * Event Metric (or a derived class) an exception will be thrown.
	 * 
	 * @param definitions    The definitions dictionary this definition is a part of
	 * @param metricTypeName The unique metric type
	 * @param categoryName   The name of the category with which this definition is
	 *                       associated.
	 * @param counterName    The name of the definition within the category.
	 * @param instanceName   The unique name of this instance within the metric's
	 *                       collection.
	 * @return The event metric object for the specified event metric instance.
	 */
	public static EventMetric addOrGet(MetricDefinitionCollection definitions, String metricTypeName,
			String categoryName, String counterName, String instanceName) {
		// we must have a definitions collection, or we have a problem
		if (definitions == null) {
			throw new NullPointerException("definitions");
		}

		// we need to find the definition, adding it if necessary
		String definitionKey = MetricDefinition.getKey(metricTypeName, categoryName, counterName);
		EventMetricDefinition definition;

		OutObject<IMetricDefinition> tempOutDefinition = new OutObject<IMetricDefinition>();
		if (definitions.tryGetValue(definitionKey, tempOutDefinition)) {
			// if the metric definition exists, but is of the wrong type we have a problem.
			if (!(tempOutDefinition.argValue instanceof EventMetricDefinition)) {
				throw new IllegalArgumentException(
						"A metric already exists with the provided type, category, and counter name but it is not compatible with being an event metric.  Please use a different counter name.");
			}
			definition = (EventMetricDefinition)tempOutDefinition.argValue;
		} else {
			definition = (EventMetricDefinition)tempOutDefinition.argValue;
			// we didn't find one, make a new one
			definition = EventMetricDefinition.builder(metricTypeName, categoryName, counterName).build();
			definitions.add(definition); // Add it to the collection, no longer done in the constructor.
			// ToDo: Reconsider this implementation; putting incomplete event metric
			// definitions in the collection is not ideal,
			// and creating a metric from an empty event metric definition is fairly
			// pointless.
		}

		// now we have our definition, proceed to create a new metric if it doesn't
		// exist
		String metricKey = MetricDefinition.getKey(metricTypeName, categoryName, counterName, instanceName);
		EventMetric metric = null;

		// see if we can get the metric already. If not, we'll create it
		synchronized (definition.getLock()) // make sure the get & add are atomic
		{
			OutObject<EventMetric> tempEventMetric = new OutObject<EventMetric>();
			if (!definition.getMetrics().tryGetValue(metricKey, tempEventMetric)) {
				metric = definition.add(instanceName);
			}
		}

		return metric;
	}

	/**
	 * Creates a new metric instance or returns an existing one from the provided
	 * definition information, or returns any existing instance if found. If the
	 * metric definition doesn't exist, it will be created. If the metric doesn't
	 * exist, it will be created. If the metric definition does exist, but is not an
	 * Event Metric (or a derived class) an exception will be thrown. Definitions
	 * are looked up and added to the active logging metrics collection
	 * (Log.Metrics)
	 * 
	 * @param metricTypeName The unique metric type
	 * @param categoryName   The name of the category with which this definition is
	 *                       associated.
	 * @param counterName    The name of the definition within the category.
	 * @param instanceName   The unique name of this instance within the metric's
	 *                       collection.
	 * @return The event metric object for the specified event metric instance.
	 */
	public static EventMetric addOrGet(String metricTypeName, String categoryName, String counterName,
			String instanceName) {
		// just forward into our call that requires the definition to be specified
		return addOrGet(Log.getMetrics(), metricTypeName, categoryName, counterName, instanceName);
	}

	/**
	 * Creates a new metric instance or returns an existing one from the provided
	 * definition information, or returns any existing instance if found. If the
	 * metric definition doesn't exist, it will be created. If the metric doesn't
	 * exist, it will be created. If the metric definition does exist, but is not an
	 * Event Metric (or a derived class) an exception will be thrown. Definitions
	 * are looked up and added to the active logging metrics collection
	 * (Log.Metrics)
	 * 
	 * @param definition   The metric definition for the metric instance
	 * @param instanceName The unique name of this instance within the metric's
	 *                     collection.
	 * @return The event metric object for the specified event metric instance.
	 */
	public static EventMetric addOrGet(EventMetricDefinition definition, String instanceName) {
		// just forward into our call that requires the definition to be specified
		return addOrGet(Log.getMetrics(), definition.getPacket().getMetricTypeName(), definition.getCategoryName(),
				definition.getCounterName(), instanceName);
	}
	
	/////////////////////////////////////////////////////

	/**
	 * Create a new, empty metric sample for this event metric instance, ready to be
	 * filled out and written.
	 * 
	 * <p>
	 * This creates an empty sample for the current event metric instance, which
	 * needs to be filled out and written. Set the value columns by calling
	 * newSample.SetValue(...), and write it to the Loupe log by calling
	 * newSample.Write().
	 * </p>
	 * <p>
	 * To record samples for event metrics defined via attributes, call
	 * eventMetricInstance.WriteSample(userDataObject) or
	 * EventMetric.Write(userDataObject).
	 * </p>
	 * 
	 * @return The new metric sample object. <example> See the
	 *         <see cref="EventMetric">EventMetric Class Overview</see> for an
	 *         example. </example>
	 */
	public EventMetricSample createSample() {
		return new EventMetricSample(this, new EventMetricSamplePacket(this));
	}

	/**
	 * Create a new sample for this metric and populate it with data from the
	 * provided user data object. The caller must write this sample for it to be
	 * recorded.
	 * 
	 * 
	 * The provided user data object must be compatible with the object type used to
	 * initialize this event metric.
	 * 
	 * @param userDataObject The object to retrieve metric values from
	 * @return The new metric sample object
	 */
	public EventMetricSample createSample(Object userDataObject) {
		if (userDataObject == null) {
			throw new NullPointerException("userDataObject");
		}

		if (!getDefinition().isBound()) {
			throw new IllegalArgumentException(
					"This event metric's definition is not bound to sample automatically from a user data object.  CreateSample() and SetValue() must be used to specify the data values directly.");
		}

		java.lang.Class userDataType = userDataObject.getClass();
		if (!getDefinition().getBoundType().isAssignableFrom(userDataType)) {
			throw new IllegalArgumentException(String.format(Locale.ROOT,
					"The provided user data object type (%1$s) is not assignable to this event metric's bound type (%2$s) and can not be sampled automatically for this metric instance.",
					userDataType, getDefinition().getBoundType()));
		}

		EventMetricSample metricSample = createSample();

		for (EventMetricValueDefinition valueDefinition : getDefinition().getValueCollection().getList()) {
			if (!valueDefinition.isBound()) {
				continue; // Can't sample values that aren't bound.
			}

			try {
				// Get the numerator value...
				// ToDo: Change value definition to use NVP (or a new Binding class).
				Object rawData = null;
				if (valueDefinition.getMemberType().equals(MemberType.FIELD)) {
					Field field = userDataType.getDeclaredField(valueDefinition.getMemberName());
					field.setAccessible(true);
					rawData = field.get(userDataObject);
				} else if (valueDefinition.getMemberType().equals(MemberType.METHOD)) {
					Method method = userDataType.getMethod(valueDefinition.getMemberName());
					method.setAccessible(true);
					rawData = method.invoke(userDataObject);
				}

				metricSample.setValue(valueDefinition, rawData); // This will handle conversion as needed.
			} catch (java.lang.Exception e) {
				// We can't write this column if we got an error reading the data. Write the
				// sample without it?
				if (SystemUtils.isInDebugMode()) {
					e.printStackTrace();
				}
			}
		}

		return metricSample;
	}

	/**
	 * Write an event metric sample for this event metric instance using the
	 * provided data object.
	 * 
	 * The provided user data object must be assignable to the bound type which
	 * defined this event metric via attributes.
	 * 
	 * @param metricData The object to retrieve metric values from. <example> See
	 *                   the <see cref="EventMetric">EventMetric Class
	 *                   Overview</see> for an example. </example>
	 */
	public void writeSample(Object metricData) {
		// use our normal create sample method, but write it out immediately!
		createSample(metricData).write();
	}

	/**
	 * Write event metric samples for all event metrics defined on the provided data
	 * object by attributes.
	 * 
	 * The provided user data object must be assignable to the bound type which
	 * defined this event metric via attributes.
	 * 
	 * @param metricData           The object to retrieve both metric values and
	 *                             definition from
	 * @param fallbackInstanceName The instance name to fall back on if a definition
	 *                             does not specify an instance name binding (may be
	 *                             null). <example> See the
	 *                             <see cref="EventMetric">EventMetric Class
	 *                             Overview</see> for an example. </example>
	 */
	public static void write(Object metricData, String fallbackInstanceName) {
		// The real logic is in EventMetricDefinition.
		EventMetricDefinition.write(metricData, fallbackInstanceName);
	}

	/**
	 * Write event metric samples for all event metrics defined on the provided data
	 * object by attributes.
	 * 
	 * The provided user data object must be assignable to the bound type which
	 * defined this event metric via attributes.
	 * 
	 * @param metricData The object to retrieve both metric values and definition
	 *                   from <example> See the <see cref="EventMetric">EventMetric
	 *                   Class Overview</see> for an example. </example>
	 */
	public static void write(Object metricData) {
		// The real logic is in EventMetricDefinition.
		EventMetricDefinition.write(metricData, null);
	}

	/**
	 * Write a metric sample to the current process log if it hasn't been written
	 * already.
	 * 
	 * @param metricSample The metric sample to write.
	 */
	@Deprecated
	public static void write(EventMetricSample metricSample) {
		metricSample.write(); // If it does happen to call, bypassing the error flag, it can forward and work.
	}

	/**
	 * Write a metric sample to the current process log if it hasn't been written
	 * already.
	 * 
	 * @param metricSample The metric sample to write.
	 * @param str          A meaningless string to fit the overload.
	 */
	@Deprecated
	public static void write(EventMetricSample metricSample, String str) {
		metricSample.write(); // If it does happen to call, bypassing the error flag, it can forward and work.
	}

	/**
	 * This is a bogus overload to prevent incorrect usage of this method when
	 * attempting to write a metric sample.
	 * 
	 * @param metricSample The metric sample to be written.
	 */
	@Deprecated
	public void writeSample(EventMetricSample metricSample) {
		metricSample.write(); // If it does happen to call, bypassing the error flag, it can forward and work
								// (maybe).
	}

	/**
	 * Determines if the provided object is identical to this object.
	 * 
	 * @param other The object to compare this object to
	 * @return True if the objects represent the same data.
	 */
	/**
	 * Determines if the provided Metric object is identical to this object.
	 * 
	 * @param other The Metric object to compare this object to
	 * @return True if the objects represent the same data.
	 */
	public boolean equals(EventMetric other) {
		if (other == this) {
			return true; // ReferenceEquals means we're the same object, definitely equal.
		}

		// Careful, it could be null; check it without recursion
		if (other == null) {
			return false; // Since we're a live object we can't be equal to a null instance.
		}

		// they are the same if their Guid's match.
		return (getId().equals(other.getId()));
	}

	/**
	 * The definition of this event metric.
	 *
	 * @return the definition
	 */
	public EventMetricDefinition getDefinition() {
		return this.metricDefinition;
	}

	/**
	 * Gets the packet.
	 *
	 * @return the packet
	 */
	public EventMetricPacket getPacket() {
		return packet;
	}

	/**
	 * The unique Id of this event metric instance. This can reliably be used as a
	 * key to refer to this item, within the same session which created it.
	 * 
	 * The Id is limited to a specific session, and thus identifies a consistent
	 * unchanged definition. The Id can <b>not</b> be used to identify a definition
	 * across different sessions, which could have different actual definitions due
	 * to changing user code. See the Key property to identify a metric definition
	 * across different sessions.
	 *
	 * @return the id
	 */
	public UUID getId() {
		return this.packet.getID();
	}

	/**
	 * The four-part key of the metric instance being captured, as a single string.
	 * 
	 * The Key is the combination of metrics capture system label, category name,
	 * and counter name of the metric definition, along with the instance name, to
	 * uniquely identify a specific metric instance of a specific metric definition.
	 * It can also identify the same metric instance across different sessions.
	 *
	 * @return the key
	 */
	public String getKey() {
		return this.packet.getName();
	}

	/**
	 * A short caption of what the metric tracks, suitable for end-user display.
	 *
	 * @return the caption
	 */
	public String getCaption() {
		return this.packet.getCaption();
	}

	/**
	 * A description of what is tracked by this metric, suitable for end-user
	 * display.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return this.packet.getDescription();
	}

	/**
	 * The metrics capture system label of this metric definition.
	 *
	 * @return the metrics system
	 */
	public String getMetricsSystem() {
		return this.metricDefinition.getMetricsSystem();
	}

	/**
	 * The category of this metric for display purposes. Category is the top
	 * displayed hierarchy.
	 *
	 * @return the category name
	 */
	public String getCategoryName() {
		return this.metricDefinition.getCategoryName();
	}

	/**
	 * The display name of this metric (unique within the category name).
	 *
	 * @return the counter name
	 */
	public String getCounterName() {
		return this.metricDefinition.getCounterName();
	}

	/**
	 * Gets the instance name for this event metric.
	 *
	 * @return the instance name
	 */
	public String getInstanceName() {
		return this.packet.getInstanceName();
	}

	/**
	 * Indicates whether this is the default metric instance for this metric
	 * definition or not.
	 * 
	 * The default instance has a null instance name. This property is provided as a
	 * convenience to simplify client code so you don't have to distinguish empty
	 * strings or null.
	 *
	 * @return true, if is default
	 */
	public boolean isDefault() {
		return (TypeUtils.isBlank(this.packet.getInstanceName()));
	}

	
}