package com.onloupe.agent.metrics;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.onloupe.core.NameValuePair;
import com.onloupe.core.logging.Log;
import com.onloupe.core.serialization.monitor.CustomSampledMetricPacket;
import com.onloupe.core.serialization.monitor.CustomSampledMetricSamplePacket;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.metric.MemberType;

// TODO: Auto-generated Javadoc
/**
 * The Class SampledMetric.
 */
public final class SampledMetric {
	
	/** The metric definition. */
	private SampledMetricDefinition metricDefinition;
	
	/** The packet. */
	private CustomSampledMetricPacket packet;

	/**
	 * Create a new API custom sampled metric object from the provided API custom
	 * sampled metric definition and internal custom sampled metric.
	 * 
	 * The new metric will automatically be added to the metric definition's metrics
	 * collection.
	 *
	 * @param definition The API custom sampled metric definition for the metric
	 *                   instance.
	 * @param packet the packet
	 */
	public SampledMetric(SampledMetricDefinition definition, CustomSampledMetricPacket packet) {
		// verify and store off our input
		if (definition == null) {
			throw new NullPointerException("definition");
		}

		if (packet == null) {
			throw new NullPointerException("packet");
		}

		// one last safety check: The definition and the packet better agree.
		if (!definition.getId().equals(packet.getDefinitionId())) {
			throw new IndexOutOfBoundsException(
					"The provided metric packet has a different definition Id than the provide metric definition.");
		}

		
		this.metricDefinition = definition;
		this.packet = packet;
		
		this.metricDefinition.getMetrics().put(packet.getInstanceName(), this);
	}

	/**
	 * Registers all sampled metric definitions defined by attributes on the
	 * provided object or Type, and registers metric instances where
	 * SampledMetricInstanceName attribute is also found or a non-null fall-back is
	 * specified.
	 * <p>
	 * This call ensures that the time-consuming reflection scan of all members
	 * looking for attributes across the entire inheritance of an object instance or
	 * Type has been done (e.g. outside of a critical path) so that the first call
	 * to Write(userDataObject) will not have to do that work within a critical
	 * path. Results are cached internally, so redundant calls to this method will
	 * not repeat the scan for types already scanned (including as part of a
	 * different top-level type).
	 * </p>
	 * <p>
	 * If a live object is given (not just a Type) then the member(s) bound as
	 * [SampledMetricInstanceName] will be queried and used to also register a
	 * sampled metric instance with the returned name, to save that step as well,
	 * although this step is much quicker. If a Type is given instead of a live
	 * object, it can not be queried for instance name(s) and will only register the
	 * sampled metric definitions. Metric instances will still be created as needed
	 * when sampling a userDataObject, automatically.
	 * </p>
	 * <p>
	 * If fallbackInstanceName is null, only instances which specify an instance
	 * name in the live object will be registered (and returned). With a valid
	 * string for fall-back instance name (including string.Empty for the "default
	 * instance"), a sampled metric will be registered and returned (barring errors)
	 * for each definition found. The instance indicated by the binding in the
	 * object will always be used by preference over the fall-back instance name
	 * parameter, even if the instance name member returns a null.
	 * </p>
	 *
	 * @param userDataObject       An object or Type defining sampled metrics via
	 *                             attributes on itself or on its base types or
	 *                             interfaces.
	 * @param fallbackInstanceName The instance name to fall back on if a definition
	 *                             does not specify an instance name binding (may be
	 *                             null).
	 * @return And array of all sampled metric instances found or created (one per
	 *         definition) based on the instance name binding and optional
	 *         fallbackInstanceName.
	 */
	public static SampledMetric[] registerAll(Object userDataObject, String fallbackInstanceName) {
		// we need a live object, not a null object or we'll fail
		if (userDataObject == null) {
			throw new NullPointerException("userDataObject");
		}

		// Register all of the event metric definitions it contains, object or Type:
		SampledMetricDefinition[] definitions = SampledMetricDefinition.registerAll(userDataObject);

		List<SampledMetric> metricsList = new ArrayList<SampledMetric>();

		if (!(userDataObject instanceof java.lang.Class)) {
			// They gave us a live object, not just a Type, so see if there are metric
			// instances we can register.

			// We'll cache the instance name for efficiency when multiple definitions in a
			// row have the same BoundType.
			java.lang.Class boundType = null;
			String instanceName = null;

			for (SampledMetricDefinition definition : definitions) {
				if (definition.isBound() && definition.getNameBound()) {
					// We are bound, so BoundType won't be null. Initial null value won't match, so
					// we'll look it up.
					if (definition.getBoundType() != boundType) {
						// New bound type, we need to look up the instance name bound for sampled
						// metrics on that Type.
						String tempVar = definition.invokeInstanceNameBinding(userDataObject);
						instanceName = (tempVar != null) ? tempVar : fallbackInstanceName;
						// A null return means it didn't have an instance name binding or couldn't read
						// it, so we'll
						// use the specified fallbackInstanceName instead. If the instance name member
						// returned null,
						// this call will actually give us string.Empty, so we won't override it. If
						// this call and
						// fallbackInstanceName are both null, we won't register the instance.
						boundType = definition.getBoundType();
					}

					if (instanceName != null) // null means it didn't find one, so we won't register an instance.
					{
						// In case there's an error in registration of one metric, we don't want to stop
						// the rest.
						try {
							// An empty string (meaning the found value was null or empty) will be
							// registered (same as null).
							metricsList.add(register(definition, instanceName));
						}
						// ReSharper disable EmptyGeneralCatchClause
						catch (java.lang.Exception e) {
							// ReSharper restore EmptyGeneralCatchClause
						}
					}
				}
			}
		}

		return metricsList.toArray(new SampledMetric[0]);
	}

	/**
	 * Pre-registers all sampled metric definitions defined by attributes on the
	 * provided object or Type, and registers metric instances where
	 * SampledMetricInstanceName attribute is also found.
	 * 
	 * 
	 * <p>
	 * This call ensures that the reflection scan of all members looking for
	 * attributes across the entire inheritance of an object instance or Type has
	 * been done (e.g. outside of a critical path) so that the first call to
	 * Write can be as fast as possible. Results are
	 * cached internally, so redundant calls to this method will not repeat the scan
	 * for types already scanned (including as part of a different top-level type).
	 * </p>
	 * <p>
	 * If a live object is given (not just a Type) then the member(s) decorated with
	 * a SampledMetricInstanceNameAttribute
	 * Class will be queried and used to also register a sampled metric
	 * instance with the returned name.
	 * </p>
	 * <p>
	 * If a Type is given instead of a live object, it can't be queried for instance
	 * name(s) and will only register the sampled metric definitions. Metric
	 * instances will still be automatically created as needed when writing a
	 * metricDataObject.
	 * </p>
	 * 
	 *  For examples, see the Sampled
	 * Metric class overview. 
	 *
	 * @param metricData An object or Type defining sampled metrics via attributes
	 *                   on itself or on its base types or interfaces.
	 */
	public static void register(Object metricData) {
		registerAll(metricData, null); // Register all definitions, but with no fall-back for unspecified instances.
	}

	/**
	 * Creates a new metric instance from the provided definition information, or
	 * returns any existing instance if found.
	 * 
	 * <p>
	 * This call is designed to be safe in multithreaded environments. If two
	 * threads attempt to register the same metric at the same time, the first will
	 * register the metric and the second (and all subsequent calls to Register with
	 * the same three part key) will return the same object.
	 * </p>
	 * <p>
	 * If the Metric Definition doesn't exist, it will be created. If the Sampled
	 * Metric doesn't exist, it will be created.
	 * </p>
	 * <p>
	 * If a metric definition does exist with the same 3-part Key but is not a
	 * sampled metric an exception will be thrown. This is one of the only times
	 * that an exception can be thrown by the Loupe Agent.
	 * </p>
	 * 
	 *  For examples, see the Sampled
	 * Metric class overview. 
	 *
	 * @param metricsSystem The metrics capture system label.
	 * @param categoryName  The name of the category with which this metric is
	 *                      associated.
	 * @param counterName   The name of the metric definition within the category.
	 * @param samplingType  The sampling type of this sampled metric counter.
	 * @param unitCaption   A displayable caption for the units this metric samples,
	 *                      or null for unit-less values.
	 * @param metricCaption A displayable caption for this sampled metric counter.
	 * @param description   An extended end-user description of this sampled metric
	 *                      counter.
	 * @param instanceName  The unique name of this instance within the metric's
	 *                      collection (may be null).
	 * @return the sampled metric
	 */
	public static SampledMetric register(String metricsSystem, String categoryName, String counterName,
			SamplingType samplingType, String unitCaption, String metricCaption, String description,
			String instanceName) {
		SampledMetricDefinition metricDefinition = SampledMetricDefinition.register(metricsSystem, categoryName,
				counterName, samplingType, unitCaption, metricCaption, description);

		// Then just forward into our call that requires the definition to be specified
		return register(metricDefinition, instanceName);
	}

	/**
	 * Creates a new metric instance from the provided definition information, or
	 * returns any existing instance if found.
	 * 
	 * 
	 * <p>
	 * If the Sampled Metric doesn't exist, it will be created.
	 * </p>
	 * <p>
	 * This call is designed to be safe in multithreaded environments. If two
	 * threads attempt to register the same metric at the same time, the first will
	 * register the metric and the second (and all subsequent calls to Register with
	 * the same three part key) will return the same object.
	 * </p>
	 *
	 * @param definition   The metric definition for the metric instance.
	 * @param instanceName The unique name of this instance within the metric's
	 *                     collection (may be null).
	 * @return The event metric object for the specified event metric instance.
	 *          For examples, see the Sampled
	 *         Metric class overview. 
	 */
	public static SampledMetric register(SampledMetricDefinition definition, String instanceName) {
		if (definition == null) {
			// Uh-oh. AddOrGet() gave us a non-CustomSampledMetricDefinition?
			return null;
		}

		return definition.addOrGetMetric(instanceName);
	}

	/**
	 * Write a metric sample with the provided data immediately, for non-fraction
	 * sampling types.
	 * 
	 * Sampled metrics using any fraction sampling type should instead use an
	 * overload providing both values.  For examples, see the
	 * Sampled Metric class overview. 
	 * 
	 * @param rawValue The raw data value.
	 */
	public void writeSample(double rawValue) {
		// Create a new custom sampled metric and write it out to the log
		Log.write(new CustomSampledMetricSamplePacket(packet, rawValue));
	}

	/**
	 * Write a metric sample with the provided data immediately, for non-fraction
	 * sampling types.
	 * 
	 * Sampled metrics using any fraction sampling type should instead use an
	 * overload providing both values.  For examples, see the
	 * Sampled Metric class overview. 
	 * 
	 * @param rawValue     The raw data value.
	 * @param rawTimestamp The exact date and time the raw value was determined.
	 */
	public void writeSample(double rawValue, OffsetDateTime rawTimestamp) {
		// Create a new custom sampled metric and write it out to the log
		Log.write(new CustomSampledMetricSamplePacket(packet, rawValue, rawTimestamp));
	}

	/**
	 * Write a metric sample with the provided data immediately, for fraction
	 * sampling types.
	 * 
	 * Sampled metrics using a non-fraction sampling type should instead use an
	 * overload taking a single data values.  For examples, see the
	 * Sampled Metric class overview. 
	 * 
	 * @param rawValue  The raw data value.
	 * @param baseValue The divisor entry of this sample.
	 */
	public void writeSample(double rawValue, double baseValue) {
		// Create a new custom sampled metric and write it out to the log
		Log.write(new CustomSampledMetricSamplePacket(packet, rawValue, baseValue));
	}

	/**
	 * Write a metric sample with the provided data immediately, for fraction
	 * sampling types.
	 * 
	 * Sampled metrics using a non-fraction sampling type should instead use an
	 * overload taking a single data values.  For examples, see the
	 * Sampled Metric class overview. 
	 * 
	 * @param rawValue     The raw data value.
	 * @param baseValue    The divisor entry of this sample.
	 * @param rawTimestamp The exact date and time the raw value was determined.
	 */
	public void writeSample(double rawValue, double baseValue, OffsetDateTime rawTimestamp) {
		// Create a new custom sampled metric and write it out to the log
		Log.write(new CustomSampledMetricSamplePacket(packet, rawValue, baseValue, rawTimestamp));
	}

	/**
	 * Write a sampled metric sample for this sampled metric instance using the
	 * provided data object.
	 * 
	 * The provided user data object must be assignable to the bound type which
	 * defined this sampled metric via attributes.  For examples, see the
	 * Sampled Metric class overview. 
	 *
	 * @param metricData The object to retrieve metric values from.
	 */
	public void writeSample(Object metricData) {
		if (metricData == null) {
			throw new NullPointerException("metricData");
		}

		if (!getDefinition().isBound()) {
			throw new IllegalArgumentException(
					"This sampled metric's definition is not bound to sample automatically from a user data object.  WriteSample(...) must be given the data values directly.");
		}

		java.lang.Class userDataType = metricData.getClass();
		if (!getDefinition().getBoundType().isAssignableFrom(userDataType)) {
			throw new IllegalArgumentException(String.format(Locale.ROOT,
					"The provided user data object type (%1$s) is not assignable to this sampled metric's bound type (%2$s) and can not be sampled automatically for this metric instance.",
					userDataType, getDefinition().getBoundType()));
		}

		try {
			// Get the numerator value...
			NameValuePair<MemberType> dataBinding = getDefinition().getDataBinding();

			// This should throw an exception if dataBinding isn't valid, so we'll bail the
			// whole thing.
			Double numerator = null;
			if (dataBinding.getValue().equals(MemberType.FIELD)) {
				Field field = userDataType.getDeclaredField(dataBinding.getName());
				field.setAccessible(true);
				numerator = field.getDouble(metricData);
			} else if (dataBinding.getValue().equals(MemberType.METHOD)) {
				Method method = userDataType.getMethod(dataBinding.getName());
				method.setAccessible(true);
				numerator = (double)method.invoke(metricData);
			}

			if (SampledMetricDefinition.requiresDivisor(getSamplingType())) {
				NameValuePair<MemberType> divisorBinding = getDefinition().getDivisorBinding();

				// This should throw an exception if divisorBinding isn't valid, so we'll bail
				// the whole thing.
				Double rawDivisor = null;
				if (divisorBinding.getValue().equals(MemberType.FIELD)) {
					Field field = userDataType.getDeclaredField(divisorBinding.getName());
					field.setAccessible(true);
					rawDivisor = field.getDouble(metricData);
				} else if (divisorBinding.getValue().equals(MemberType.METHOD)) {
					Method method = userDataType.getMethod(divisorBinding.getName());
					method.setAccessible(true);
					rawDivisor = (double)method.invoke(metricData);
				}
				
				writeSample(numerator, rawDivisor); // Write the pair of values.
			} else {
				writeSample(numerator); // Write the single data value.
			}
		} catch (java.lang.Exception e) {

			// We can't write this sample if we got an error reading the data.
		}
	}

	/**
	 * Write sampled metric samples for all sampled metrics defined on the provided
	 * data object by attributes.
	 * 
	 * The provided user data object must be assignable to the bound type which
	 * defined this sampled metric via attributes.  For examples, see the
	 * Sampled Metric class overview. 
	 *
	 * @param metricData           The object to retrieve both metric values and
	 *                             definition from
	 * @param fallbackInstanceName The instance name to fall back on if a definition
	 *                             does not specify an instance name binding (may be
	 *                             null).
	 */
	public static void write(Object metricData, String fallbackInstanceName) {
		// We have to force a null fall-back instance name into a valid string to force
		// all metrics to be sampled.
		SampledMetric[] allMetrics = registerAll(metricData,
				(fallbackInstanceName != null) ? fallbackInstanceName : "");

		for (SampledMetric metric : allMetrics) {
			try {
				metric.writeSample(metricData);
			}
			// ReSharper disable EmptyGeneralCatchClause
			catch (java.lang.Exception e) {
				// ReSharper restore EmptyGeneralCatchClause
			}
		}

		return;
	}

	/**
	 * Write sampled metric samples for all sampled metrics defined on the provided
	 * data object by attributes.
	 * 
	 * The provided user data object must be assignable to the bound type which
	 * defined this sampled metric via attributes.  For examples, see the
	 * Sampled Metric class overview. 
	 * 
	 * @param metricData The object to retrieve both metric values and definition
	 *                   from
	 */
	public static void write(Object metricData) {
		// The real logic is in SampledMetricDefinition.
		write(metricData, "");
	}

	/**
	 * The definition of this sampled metric.
	 *
	 * @return the definition
	 */
	public SampledMetricDefinition getDefinition() {
		return this.metricDefinition;
	}

	/**
	 * The unique Id of this sampled metric instance. This can reliably be used as a
	 * key to refer to this item, within the same session which created it.
	 * 
	 * 
	 * The Id is limited to a specific session, and thus identifies a consistent
	 * unchanged definition. The Id can <b>not</b> be used to identify a definition
	 * across different sessions, which could have different actual definitions due
	 * to changing user code. See the Key property to identify
	 * a metric definition across different sessions.
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
	 * The category of this metric for display purposes. Displayed as a dot (.)
	 * delimited hierarchal display.
	 * 
	 * 
	 * You can create arbitrarily deep categorization by using periods (.) to
	 * separate category levels. For example, the category "Database.Query.Search"
	 * will be parsed to create a three-level category {Database, Query, Search}.
	 * You can have spaces in the category name.
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
	 * The intended method of interpreting the sampled counter value.
	 * 
	 * 
	 * <p>
	 * Depending on how your application can conveniently aggregate data, select the
	 * matching sampling type. For example, consider a metric designed to record
	 * disk utilization in bytes / second. This can be done by:
	 * </p>
	 * <list type="number"> <item> Recording with each sample the total number of
	 * bytes written from the start of the process to the current point. This would
	 * use the Total Count Sampling Type. </item>
	 * <item> Recording with each sample the number of bytes written since the last
	 * sample. This would use the IncrementalCount Sampling
	 * Type. </item> <item> Recording with each sample the bytes per second
	 * since the last sample. This would use the RawCount
	 * Sampling Type. </item> </list>
	 * <p>
	 * <strong>Fraction Sampling Formats</strong>
	 * </p>
	 * <p>
	 * When you want to record a metric that represents a percentage, such as
	 * percent utilization, it's often easiest to record the individual metric
	 * samples with both parts of the fraction used to derive the percentage. For
	 * example, consider a metric designed to record percent disk utilization (as a
	 * percentage of working time). This can be done by:
	 * </p>
	 * <list type="number"> <item> Recording with each sample the total number of
	 * ticks spent writing to disk as the value and the total number of ticks spent
	 * servicing requests as the base value. This would use the TotalFraction
	 * Sampling Type. </item> <item> Recording with
	 * each sample the number of ticks spent writing to disk since the last sample
	 * as the value and the number of ticks spent servicing client requests since
	 * the last sample as the base value. This would use the IncrementalFraction
	 * Sampling Type. </item> <item> Recording with
	 * each sample the number of ticks spent writing per second as the value and the
	 * number of ticks spent servicing client requests per second as the base value.
	 * This would use the RawFraction Sampling Type.
	 * </item> </list>
	 * <p>
	 * The advantage of the fractional sampling types over simply doing the division
	 * yourself is primarily the additional safety aspects built into Loupe (such as
	 * division by zero protection) and automatic, accurate extrapolation to
	 * different sampling intervals (such as when samples are recorded once per
	 * second but you want to view them on a longer interval)
	 * </p>
	 *
	 * @return the sampling type
	 * @see SamplingType SamplingType Enumeration
	 */
	public SamplingType getSamplingType() {
		return this.metricDefinition.getSamplingType();
	}

	/**
	 * The display caption for the units this metric's values represent, or null for
	 * unit-less values.
	 * 
	 * 
	 * <p>
	 * Unit caption is used in the Analyst during charting and graphing to allow
	 * metrics that share the same units to be displayed on the same axis.
	 * Comparison is case insensitive, but otherwise done as a normal string
	 * compare.
	 * </p>
	 * <p>
	 * Normally unit captions do not include aggregation text, such as Average, Min
	 * or Max to support the best axis grouping.
	 * </p>
	 *
	 * @return the unit caption
	 */
	public String getUnitCaption() {
		return this.metricDefinition.getUnitCaption();
	}

	/**
	 * Gets the instance name for this sampled metric.
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