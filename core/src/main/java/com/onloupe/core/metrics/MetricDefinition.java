package com.onloupe.core.metrics;

import java.util.UUID;

import com.onloupe.core.serialization.monitor.IDisplayable;
import com.onloupe.core.serialization.monitor.MetricDefinitionPacket;
import com.onloupe.core.serialization.monitor.TextParse;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.SampleType;
import com.onloupe.model.metric.MetricSampleInterval;

/**
 * The definition of a single metric that has been captured.
 * 
 * 
 * Individual metrics capture a stream of values for a metric definition which
 * can then be displayed and manipulated.
 * 
 */
public class MetricDefinition implements IDisplayable {
	private MetricDefinitionCollection definitions;
	private MetricDefinitionPacket packet;
	private MetricCollection metrics;

	private final Object lock = new Object();

	private String[] categoryNames; // the parsed array of the category name hierarchy, period delimited

	public MetricDefinition() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Create a new metric definition.
	 * 
	 * At any one time there should only be one metric definition with a given
	 * combination of metric type, category, and counter name. These values together
	 * are used to correlate metrics between sessions. The metric definition will
	 * <b>not</b> be automatically added to the provided collection.
	 * 
	 * @param definitions  The definitions dictionary this definition is a part of
	 * @param metricType   The unique metric type
	 * @param categoryName The name of the category with which this definition is
	 *                     associated.
	 * @param counterName  The name of the definition within the category.
	 * @param sampleType   The type of data sampling done for this metric.
	 */
	public MetricDefinition(MetricDefinitionCollection definitions, String metricType, String categoryName,
			String counterName, SampleType sampleType) {
		this(definitions, new MetricDefinitionPacket(metricType, categoryName, counterName, sampleType));
	}

	/**
	 * Create a new metric definition from the provided metric definition packet.
	 * 
	 * At any one time there should only be one metric definition with a given
	 * combination of metric type, category, and counter name. These values together
	 * are used to correlate metrics between sessions. The metric definition will
	 * <b>not</b> be automatically added to the provided collection.
	 * 
	 * @param definitions The definitions dictionary this definition is a part of
	 * @param packet      The packet to create a definition from.
	 */
	public MetricDefinition(MetricDefinitionCollection definitions, MetricDefinitionPacket packet) {
		// make sure our definitions dictionary isn't null
		if (definitions == null) {
			throw new NullPointerException("definitions");
		}

		// make sure our packet isn't null
		if (packet == null) {
			throw new NullPointerException("packet");
		}

		this.definitions = definitions;
		this.packet = packet;

		// and create our metric dictionary
		// ReSharper disable DoNotCallOverridableMethodsInConstructor
		this.metrics = onMetricDictionaryCreate();
		// ReSharper restore DoNotCallOverridableMethodsInConstructor

		// finally, auto-add ourself to the definition
		// _Definitions.Add(this); // Commented out for new EventMetricDefinition
		// protocol with Register().
	}

	/**
	 * The unique Id of this metric definition packet. This can reliably be used as
	 * a key to refer to this item.
	 * 
	 * The key can be used to compare the same definition across different instances
	 * (e.g. sessions). This Id is always unique to a particular instance.
	 */
	public final UUID getId() {
		return this.packet.getID();
	}

	/**
	 * The name of the metric definition being captured.
	 * 
	 * The name is for comparing the same definition in different sessions. They
	 * will have the same name but not the same Id.
	 */
	public final String getName() {
		return this.packet.getName();
	}

	/**
	 * A short display string for this metric definition, suitable for end-user
	 * display.
	 */
	@Override
	public final String getCaption() {
		return this.packet.getCaption();
	}

	public final void setCaption(String value) {
		this.packet.setCaption(value);
	}

	/**
	 * A description of what is tracked by this metric, suitable for end-user
	 * display.
	 */
	@Override
	public final String getDescription() {
		return this.packet.getDescription();
	}

	public final void setDescription(String value) {
		this.packet.setDescription(value);
	}

	/**
	 * The recommended default display interval for graphing.
	 */
	public final MetricSampleInterval getInterval() {
		return this.packet.getInterval();
	}

	protected final void setInterval(MetricSampleInterval value) {
		this.packet.setInterval(value);
	}

	/**
	 * The internal metric type of this metric definition
	 * 
	 * Metric types distinguish different metric capture libraries from each other,
	 * ensuring that we can correctly correlate the same metric between sessions and
	 * not require category names to be globally unique. If you are creating a new
	 * metric, pick your own metric type that will uniquely identify your library or
	 * namespace.
	 */
	public final String getMetricTypeName() {
		return this.packet.getMetricTypeName();
	}

	/**
	 * The definitions collection that contains this definition.
	 * 
	 * This parent pointer should be used when walking from an object back to its
	 * parent instead of taking advantage of the static metrics definition
	 * collection to ensure your application works as expected when handling data
	 * that has been loaded from a database or data file. The static metrics
	 * collection is for the metrics being actively captured in the current process,
	 * not for metrics that are being read or manipulated.
	 */
	public final MetricDefinitionCollection getDefinitions() {
		return this.definitions;
	}

	/**
	 * The set of metrics that use this definition.
	 * 
	 * All metrics with the same definition are of the same object type.
	 */
	public MetricCollection getMetrics() {
		return this.metrics;
	}

	/**
	 * The category of this metric for display purposes. This can be a period
	 * delimited string to represent a variable height hierarchy
	 */
	public final String getCategoryName() {
		return this.packet.getCategoryName();
	}

	/**
	 * An array of the individual category names within the specified category name
	 * which is period delimited.
	 */
	public final String[] getCategoryNames() {
		// have we parsed it yet? We don't want to do this every time, it ain't cheap.
		if (this.categoryNames == null) {
			// no.
			this.categoryNames = TextParse.splitStringWithTrim(getCategoryName(), new char[] { '-', '_', ' ' });
		}

		return this.categoryNames;
	}

	/**
	 * The display name of this metric (unique within the category name).
	 */
	public final String getCounterName() {
		return this.packet.getCounterName();
	}

	/**
	 * The sample type of the metric. Indicates whether the metric represents
	 * discrete events or a continuous value.
	 */
	public final SampleType getSampleType() {
		return this.packet.getSampleType();
	}

	/**
	 * Compares this MetricDefinition to another MetricDefinition to determine sort
	 * order
	 * 
	 * MetricDefinition instances are sorted by their Name property.
	 * 
	 * @param other The MetricDefinition to compare this MetricDefinition against
	 * @return An int which is less than zero, equal to zero, or greater than zero
	 *         to reflect whether this MetricDefinition should sort as being
	 *         less-than, equal to, or greater-than the other MetricDefintion,
	 *         respectively.
	 */
	public final int compareTo(MetricDefinition other) {
		// our packet knows what to do.
		return this.packet.compareTo(other.getPacket());
	}

	/**
	 * Determines if the provided MetricDefinition object is identical to this
	 * object.
	 * 
	 * @param other The MetricDefinition object to compare this object to
	 * @return True if the objects represent the same data.
	 */
	public final boolean equals(MetricDefinition other) {
		// Careful, it could be null; check it without recursion
		if (other == null) {
			return false; // Since we're a live object we can't be equal to a null instance.
		}

		// they are the same if their GUID's match
		return (getId().equals(other.getId()));
	}

	/**
	 * Determines if the provided object is identical to this object.
	 * 
	 * @param obj The object to compare this object to
	 * @return True if the other object is also a MetricDefinition and represents
	 *         the same data.
	 */
	@Override
	public boolean equals(Object obj) {
		MetricDefinition otherMetricDefinition = obj instanceof MetricDefinition ? (MetricDefinition) obj : null;

		return equals(otherMetricDefinition); // Just have type-specific Equals do the check (it even handles null)
	}

	/**
	 * Provides a representative hash code for objects of this type to spread out
	 * distribution in hash tables.
	 * 
	 * Objects which consider themselves to be Equal (a.Equals(b) returns true) are
	 * expected to have the same hash code. Objects which are not Equal may have the
	 * same hash code, but minimizing such overlaps helps with efficient operation
	 * of hash tables.
	 * 
	 * @return An int representing the hash code calculated for the contents of this
	 *         object.
	 * 
	 */
	@Override
	public int hashCode() {
		int myHash = getId().hashCode(); // The ID is all that Equals checks!

		return myHash;
	}

	/**
	 * Indicates if the definition is part of the current live metric definition
	 * collection
	 * 
	 * The same process can be recording metrics and reading metrics from a data
	 * source such as a file. This flag indicates whether this metric definition is
	 * for playback purposes (it represents previously recorded data) or is part of
	 * the active metric capture capability of the current process.
	 */
	public final boolean isLive() {
		return this.packet.isLive();
	}

	/**
	 * Indicates if the definition can be changed.
	 * 
	 * If a metric definition is read-only, that means the definition can't be
	 * changed in a way that would invalidate metrics or metric samples recorded
	 * with it. Display-only values (such as captions and descriptions) can always
	 * be changed, and new metrics can always be added to a metric definition.
	 */
	public final boolean isReadOnly() {
		return this.packet.isReadOnly();
	}

	public final void setIsReadOnly(boolean value) {
		this.packet.setIsReadOnly(value);
	}

	/**
	 * Set this metric definition to be read-only and lock out further changes,
	 * allowing it to be instantiated and sampled.
	 */
	public void setReadOnly() {
		setIsReadOnly(true);
	}

	/**
	 * Object Change Locking object.
	 */
	public final Object getLock() {
		return this.lock;
	}

	/**
	 * Invoked by the base class to allow inheritors to provide derived
	 * implementations
	 * 
	 * If you wish to provide a derived class for the metric dictionary in your
	 * derived metric, use this method to create and return your derived object.
	 * This is used during object construction, so implementations should treat it
	 * as a static method.
	 * 
	 * @return The MetricCollection-compatible object.
	 */
	protected MetricCollection onMetricDictionaryCreate() {
		return new MetricCollection(this);
	}

	/**
	 * Calculate the string key for a metric definition.
	 * 
	 * @param metric The existing metric object to generate a string key for
	 * @return The unique string key for this item
	 */
	public static String getKey(Metric metric) {
		// make sure the metric object isn't null
		if (metric == null) {
			throw new NullPointerException("metric");
		}

		// We are explicitly NOT passing the instance name here - we want the key of the
		// DEFINITION.
		return getKey(metric.getMetricTypeName(), metric.getCategoryName(), metric.getCounterName());
	}

	/**
	 * Calculate the string key for a metric definition.
	 * 
	 * @param metricDefinition The existing metric definition object to generate a
	 *                         string key for
	 * @return The unique string key for this item
	 */
	public static String getKey(MetricDefinition metricDefinition) {
		// make sure the metric definition object isn't null
		if (metricDefinition == null) {
			throw new NullPointerException("metricDefinition");
		}

		return getKey(metricDefinition.getMetricTypeName(), metricDefinition.getCategoryName(),
				metricDefinition.getCounterName());
	}

	/**
	 * Calculate the string key for a metric.
	 * 
	 * @param metricDefinition The existing metric definition object to generate a
	 *                         string key for
	 * @param instanceName     The name of the performance counter category
	 *                         instance, or an empty string (""), if the category
	 *                         contains a single instance.
	 * @return The unique string key for this item
	 */
	public static String getKey(MetricDefinition metricDefinition, String instanceName) {
		// make sure the metric definition object isn't null
		if (metricDefinition == null) {
			throw new NullPointerException("metricDefinition");
		}

		return getKey(metricDefinition.getMetricTypeName(), metricDefinition.getCategoryName(),
				metricDefinition.getCounterName(), instanceName);
	}

	/**
	 * Calculate the string key for a metric definition.
	 * 
	 * @param metricTypeName The unique metric type
	 * @param categoryName   The name of the performance counter category
	 *                       (performance object) with which this performance
	 *                       counter is associated.
	 * @param counterName    The name of the performance counter.
	 * @return The unique string key for this item
	 * @exception ArgumentNullException The provided metricsSystem, categoryName, or
	 *                                  counterName was null.
	 */
	public static String getKey(String metricTypeName, String categoryName, String counterName) {
		return getKey(metricTypeName, categoryName, counterName, null);
	}

	/**
	 * Calculate the string key for a metric.
	 * 
	 * @param metricTypeName The unique metric type
	 * @param categoryName   The name of the performance counter category
	 *                       (performance object) with which this performance
	 *                       counter is associated.
	 * @param counterName    The name of the performance counter.
	 * @param instanceName   The name of the performance counter category instance,
	 *                       or an empty string (""), if the category contains a
	 *                       single instance.
	 * @return The unique string key for this item
	 * @exception ArgumentNullException The provided metricsSystem, categoryName, or
	 *                                  counterName was null.
	 */
	public static String getKey(String metricTypeName, String categoryName, String counterName, String instanceName) {
		String key;

		if (TypeUtils.isBlank(metricTypeName)) {
			throw new NullPointerException("metricTypeName");
		}

		if (TypeUtils.isBlank(categoryName)) {
			throw new NullPointerException("categoryName");
		}

		if (TypeUtils.isBlank(counterName)) {
			throw new NullPointerException("counterName");
		}

		// we assemble the key by appending the parts of the name of the counter
		// together. We have to guard for a NULL or EMPTY instance name
		if ((TypeUtils.isBlank(instanceName)) || (TypeUtils.isBlank(instanceName.trim()))) {
			// there is no instance name - just the first two parts
			key = String.format("%1$s~%2$s~%3$s", metricTypeName.trim(), categoryName.trim(), counterName.trim());
		} else {
			key = String.format("%1$s~%2$s~%3$s~%4$s", metricTypeName.trim(), categoryName.trim(), counterName.trim(),
					instanceName.trim());
		}

		return key;
	}

	/**
	 * Takes an instance name or complete metric name and normalizes it to a metric
	 * name so it can be used to look up a metric
	 * 
	 * @param metricDefinition The metric definition to look for metrics within
	 * @param metricKey        The instance name or complete metric name
	 * @return
	 */
	public static String normalizeKey(MetricDefinition metricDefinition, String metricKey) {
		String returnVal;
		String trueMetricKey;

		boolean prependDefinitionName = false;

		// Did we get a null? If we got a null, we know we need to pre-pend the
		// definition (and it isn't safe to do any more testing)
		if (metricKey == null) {
			prependDefinitionName = true;
			trueMetricKey = null;
		} else {
			// trim the input for subsequent testing to see what we get
			trueMetricKey = metricKey.trim();

			if (TypeUtils.isBlank(trueMetricKey)) {
				// we know we need to pre-pend the definition name
				prependDefinitionName = true;
			} else {
				// OK, a true key is a full name, so see if the key we got STARTS with our
				// definition name
				if (trueMetricKey.length() < metricDefinition.getName().length()) {
					// the key we got is shorter than the length of the metric definition name, so
					// it can't include the metric definition name.
					prependDefinitionName = true;
				} else {
					// now check the start of the string to see what we get
					if (!trueMetricKey.startsWith(metricDefinition.getName())) {
						// they aren't the same at least as long as the metric definition name is, so we
						// assume we need to pre-pend.
						prependDefinitionName = true;
					}
				}
			}
		}

		// If the value we got was just the instance name, we need to put the metric
		// definition's key in front of it.
		if (prependDefinitionName) {
			returnVal = getKey(metricDefinition, trueMetricKey);
		} else {
			returnVal = trueMetricKey;
		}

		return returnVal;
	}

	/**
	 * The underlying packet
	 */
	public MetricDefinitionPacket getPacket() {
		return this.packet;
	}
}