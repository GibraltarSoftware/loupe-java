package com.onloupe.agent.metrics;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.onloupe.agent.metrics.annotation.SampledMetricClass;
import com.onloupe.agent.metrics.annotation.SampledMetricDivisor;
import com.onloupe.agent.metrics.annotation.SampledMetricDivisors;
import com.onloupe.agent.metrics.annotation.SampledMetricInstanceName;
import com.onloupe.agent.metrics.annotation.SampledMetricValue;
import com.onloupe.agent.metrics.annotation.SampledMetricValues;
import com.onloupe.core.NameValuePair;
import com.onloupe.core.logging.Log;
import com.onloupe.core.metrics.IMetricDefinition;
import com.onloupe.core.metrics.MetricDefinitionCollection;
import com.onloupe.core.metrics.MetricSampleType;
import com.onloupe.core.serialization.monitor.CustomSampledMetricDefinitionPacket;
import com.onloupe.core.serialization.monitor.CustomSampledMetricPacket;
import com.onloupe.core.util.OutObject;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.SampleType;
import com.onloupe.model.metric.MemberType;


/**
 * The definition of a user-defined sampled metric
 * 
 * Custom Sampled Metrics are the simplest form of Sampled Metrics, not
 * requiring the developer to derive their own classes to encapsulate a sampled
 * metric. Review if this class can serve your needs before you create your own
 * custom set of classes derived from SampledMetric (or derive from this class)
 */
public final class SampledMetricDefinition implements IMetricDefinition {
	
	/** The lock. */
	private final Object lock = new Object();
	
	/** The packet. */
	private CustomSampledMetricDefinitionPacket packet;

    /** The requires multiple samples. */
    private boolean requiresMultipleSamples;
    
    /** The bound. */
    private boolean bound;
    
    /** The bound type. */
    private Class boundType;
    
    /** The name bound. */
    private boolean nameBound;
    
    /** The name member name. */
    private String nameMemberName;
    
    /** The name member type. */
    private MemberType nameMemberType;
    
	/** The metrics. */
	private Map<String, SampledMetric> metrics = new HashMap<String, SampledMetric>();

	/** The Constant supportedDataTypes. */
	private static final java.lang.Class[] supportedDataTypes = new Class[] { Double.class, Float.class,
			BigDecimal.class, Long.class, Integer.class, Short.class, LocalDateTime.class, OffsetDateTime.class,
			Duration.class, int.class, long.class, short.class, float.class, double.class };

	/** The Constant definitions. */
	private static final MetricDefinitionCollection definitions = Log.getMetricDefinitions();
	
	/** The Constant dataTypeSupported. */
	private static final Map<java.lang.Class, Boolean> dataTypeSupported = initTypeSupportedDictionary();
	
	/** The Constant inheritanceMap. */
	// Array of all inherited types (that have attributes), by type.
	private static final Map<java.lang.Class, Class[]> inheritanceMap = new HashMap<java.lang.Class, Class[]>();
	
	/** The Constant definitionsMap. */
	// LOCKED List of definitions by specific bound type.
	private static final Map<java.lang.Class, List<SampledMetricDefinition>> definitionsMap = new HashMap<java.lang.Class, List<SampledMetricDefinition>>();
	
	/** The Constant dictionaryLock. */
	private static final Object dictionaryLock = new Object(); // Lock for the DefinitionMap dictionary.

	/**
	 * Inits the type supported dictionary.
	 *
	 * @return the hash map
	 */
	private static HashMap<java.lang.Class, Boolean> initTypeSupportedDictionary() {
		// We need to initialize our type-supported dictionary up front....
		HashMap<java.lang.Class, Boolean> dataTypeSupported = new HashMap<java.lang.Class, Boolean>(
				supportedDataTypes.length);
		for (java.lang.Class type : supportedDataTypes) {
			dataTypeSupported.put(type, true);
		}
		return dataTypeSupported;
	}

	/**
	 * Create a new metric definition for the active log.
	 * 
	 * At any one time there should only be one metric definition with a given
	 * combination of metric type, category, and counter name. These values together
	 * are used to correlate metrics between sessions. The metric definition will
	 * automatically be added to the provided collection.
	 * 
	 * @param metricsSystem The metrics capture system label.
	 * @param categoryName  The name of the category with which this definition is
	 *                      associated.
	 * @param counterName   The name of the definition within the category.
	 * @param samplingType  The type of data captured for each metric under this
	 *                      definition.
	 * @param unitCaption   A displayable caption for the units this metric's values
	 *                      represent, or null for unit-less values.
	 * @param metricCaption A displayable caption for this sampled metric counter.
	 * @param description   A description of what is tracked by this metric,
	 *                      suitable for end-user display.
	 */
	private SampledMetricDefinition(String metricsSystem, String categoryName, String counterName,
			SamplingType samplingType, String unitCaption, String metricCaption, String description) {
		this.packet = new CustomSampledMetricDefinitionPacket(metricsSystem, categoryName, counterName,
				MetricSampleType.forValue(samplingType.getValue()), unitCaption, description);
		this.packet.setCaption(metricCaption);
		this.requiresMultipleSamples = sampledMetricTypeRequiresMultipleSamples(samplingType);
		definitions.add(this);
		setReadOnly();
	}

	/**
	 * Find or create a sampled metric definition from the specified parameters.
	 *
	 * @param metricsSystem The metrics capture system label.
	 * @param categoryName  The name of the category with which this definition is
	 *                      associated.
	 * @param counterName   The name of the definition within the category.
	 * @param samplingType  The sampling type of the sampled metric counter.
	 * @param unitCaption   A displayable caption for the units this metric's values
	 *                      represent, or null for unit-less values.
	 * @param metricCaption A displayable caption for this sampled metric counter.
	 * @param description   An extended end-user description of this sampled metric
	 *                      counter. If a metric definition does not already exist
	 *                      for the specified metrics system, category name, and
	 *                      counter name, a new sampled metric definition will be
	 *                      created from the provided parameters. If one already
	 *                      exists then it will be checked for compatibility. A
	 *                      sampled metric defined with the same SamplingType will
	 *                      be considered compatible, otherwise an ArgumentException
	 *                      will be thrown. Each distinct metric definition (all
	 *                      sampled metrics and event metrics) must have a distinct
	 *                      Key (the metrics system, category, and counter name).
	 *                      Multiple metric instances can then be created (each with
	 *                      its own instance name) from the same metric definition.
	 * 
	 *                      @throws NullPointerException The
	 *                      provided metricsSystem, categoryName, or counterName was
	 *                      null.
	 *                      @throws IllegalArgumentException There is
	 *                      already a metric definition for the same key, but it is
	 *                      not a sampled metric.&lt;br /&gt;-or-&lt;br /&gt; There
	 *                      is already a sampled metric definition for the same key
	 *                      but it uses an incompatible sampling type.
	 * @return the sampled metric definition
	 */
	public static SampledMetricDefinition register(String metricsSystem, String categoryName, String counterName,
			SamplingType samplingType, String unitCaption, String metricCaption, String description) {
		SampledMetricDefinition officialDefinition;
		boolean newCreation;

		// We need to lock the collection while we check for an existing definition and
		// maybe add a new one to it.
		synchronized (Log.getMetricDefinitions().getLock()) {
			IMetricDefinition rawDefinition;
			OutObject<IMetricDefinition> tempOutRawDefinition = new OutObject<IMetricDefinition>();
			if (!Log.getMetricDefinitions().tryGetValue(metricsSystem, categoryName, counterName,
					tempOutRawDefinition)) {
				rawDefinition = tempOutRawDefinition.argValue;
				// There isn't already one by that Key. Great! Make one from our parameters.
				newCreation = true;
				officialDefinition = new SampledMetricDefinition(metricsSystem, categoryName, counterName, samplingType,
						unitCaption, metricCaption, description);
			} else {
				rawDefinition = tempOutRawDefinition.argValue;
				// Oooh, we found one already registered. We'll want to do some checking on
				// this, but outside the lock.
				newCreation = false;
				officialDefinition = rawDefinition instanceof SampledMetricDefinition
						? (SampledMetricDefinition) rawDefinition
						: null;
			}
		} // End of collection lock

		if (officialDefinition == null) {
			throw new IllegalArgumentException(String.format(Locale.ROOT,
					"There is already a metric definition for the same metrics system (%1$s), category name (%2$s), and counter name (%3$s), but it is not a sampled metric.",
					metricsSystem, categoryName, counterName));
		} else if (!newCreation) {
			// There was one other than us, make sure it's compatible with us. Just check
			// SamplingType.
			if (officialDefinition.getSamplingType() != samplingType) {
				throw new IllegalArgumentException(String.format(Locale.ROOT,
						"There is already a sampled metric definition for the same metrics system (%1$s), category name (%2$s), and counter name (%3$s), but it is not compatible; "
								+ "it defines sampling type as %4$s rather than %5$s.",
						metricsSystem, categoryName, counterName, officialDefinition.getSamplingType(), samplingType));
			}

			// If the SamplingType matches, then we're okay to return the official one.
		}
		// Otherwise, we just made this one, so we're all good.

		return officialDefinition;
	}

	/**
	 * Find or create multiple sampled metrics definitions (defined via
	 * SampledMetric attributes) for the provided object or Type.
	 * 
	 * The provided Type or the GetType() of the provided object instance will be
	 * scanned for SampledMetric attributes on itself and any of its interfaces to
	 * identify a list of sampled metrics defined for instances of that type,
	 * creating them as necessary by scanning its members for SampledMetricValue
	 * attributes. Inheritance will be followed into base types, along with all
	 * interfaces inherited to the top level. This method will not throw exceptions,
	 * so a null argument will return an empty array, as will an argument which does
	 * not define any valid sampled metrics. Also see AddOrGet() to find or create
	 * sampled metrics definitions for a specific Type, without digging into
	 * inheritance or interfaces of that Type.
	 *
	 * @param userInstanceObject A Type or an instance defining sampled metrics by
	 *                           attributes on itself and/or its interfaces and/or
	 *                           inherited types.
	 * @return An array of zero or more sampled metric definitions found for the
	 *         provided object or Type.
	 */
	public static SampledMetricDefinition[] registerAll(Object userInstanceObject) {
		ArrayList<SampledMetricDefinition> definitions = new ArrayList<SampledMetricDefinition>();

		if (userInstanceObject != null) {
			// Either they gave us a Type, or we need to get the type of the object instance
			// they gave us.
			java.lang.Class userObjectType = ((userInstanceObject instanceof java.lang.Class
					? (java.lang.Class) userInstanceObject
					: null) != null)
							? (userInstanceObject instanceof java.lang.Class ? (java.lang.Class) userInstanceObject
									: null)
							: userInstanceObject.getClass();

			SampledMetricDefinition[] metricDefinitions;
			java.lang.Class[] inheritanceArray;
			boolean foundIt;
			synchronized (inheritanceMap) // Apparently Dictionaries are not internally threadsafe.
			{
				inheritanceArray = inheritanceMap.get(userObjectType);
			}
			if (inheritanceArray != null) {
				// We've already scanned this type, so use the cached array of types.
				for (java.lang.Class inheritedType : inheritanceArray) {
					try {
						metricDefinitions = registerGroup(inheritedType, null);
					} catch (java.lang.Exception e) {
						metricDefinitions = null;
					}
					if (metricDefinitions != null) {
						definitions.addAll(Arrays.asList(metricDefinitions)); // Add them to the list if found.
					}
				}
			} else {
				// New top-level type, we have to scan its inheritance.
				List<java.lang.Class> inheritanceList = new ArrayList<java.lang.Class>(); // List of all the inherited
																							// types we find with
																							// attributes on them.

				// First, see if the main type they gave us defines a sampled metric group
				// (metricsSystem and categoryName).
				if (userObjectType.isAnnotationPresent(SampledMetricClass.class)) {
					try {
						inheritanceList.add(userObjectType); // Add the top level Type to our list of types.
						metricDefinitions = registerGroup(userObjectType, null);
					} catch (java.lang.Exception e2) {
						metricDefinitions = null;
					}
					if (metricDefinitions != null) {
						definitions.addAll(Arrays.asList(metricDefinitions)); // Add them to the list if found.
					}
				}

				// Now check all of its interfaces.
				// Now check all of its interfaces.
				Set<Class> interfaces = new HashSet<Class>();
				for (java.lang.Class interfc : userObjectType.getInterfaces()) {
					interfaces.add(interfc);
					TypeUtils.getSuperInterfaces(interfc, interfaces);
				}
				
				for (java.lang.Class interfc : interfaces) {
					if (interfc.isAnnotationPresent(SampledMetricClass.class)) {
						// We found an interface with the right Attribute, get its group of definitions.
						try {
							inheritanceList.add(interfc); // Add the interface to our list of types.
							metricDefinitions = registerGroup(interfc, null);
						} catch (java.lang.Exception e3) {
							metricDefinitions = null;
						}
						if (metricDefinitions != null) {
							definitions.addAll(Arrays.asList(metricDefinitions)); // Add them to the list if found.
						}
					}
				}

				// And finally, drill down it's inheritance... unless it's an interface (which
				// will have a null base type).
				java.lang.Class baseObjectType = userObjectType.getSuperclass();

				// The IsInterface check shouldn't be needed, but just in case, we want to be
				// sure we don't cause a duplicate.
				while (baseObjectType != null && baseObjectType != Object.class
						&& !baseObjectType.isInterface()) {
					// See if an ancestor Type defines a group of sampled metrics.
					if (baseObjectType.isAnnotationPresent(SampledMetricClass.class)) {
						try {
							inheritanceList.add(baseObjectType); // Add the inherited base to our list of types.
							metricDefinitions = registerGroup(baseObjectType, null);
						} catch (java.lang.Exception e4) {
							metricDefinitions = null;
						}
						if (metricDefinitions != null) {
							definitions.addAll(Arrays.asList(metricDefinitions)); // Add them to the list if found.
						}
					}

					// No need to check its interfaces, we already got all of them from the top
					// level.

					baseObjectType = baseObjectType.getSuperclass(); // Get the next deeper Type.
				}

				// Now, remember the list of attributed types we found in this walk.
				synchronized (inheritanceMap) // Apparently Dictionaries are not internally threadsafe.
				{
					inheritanceMap.put(userObjectType, inheritanceList.toArray(new java.lang.Class[0]));
				}
			}
		}

		// And finally, return the full list of definitions we found.
		// If they gave us a null, we'll just return an empty array.
		return definitions.toArray(new SampledMetricDefinition[0]);
	}

	/**
	 * Find or create sampled metric definitions from SampledMetric and
	 * SampledMetricValue attributes on a specific Type.
	 * 
	 * The provided type must have a SampledMetric attribute and can have one or
	 * more fields, properties or zero-argument methods with SampledMetricValue
	 * attributes defined. This method creates metric definitions but does not
	 * create specific metric instances, so it does not require a live object. If
	 * the sampled metric definition already exists, it is just returned and no
	 * exception is thrown. If the provided type is not suitable to create sampled
	 * metrics from because it is missing the appropriate attributes or the
	 * attributes have been miss-defined, an ArgumentException will be thrown.
	 * Inheritance and interfaces will <b>not</b> be searched, so the provided Type
	 * must directly define sampled metrics, but valid objects of a type assignable
	 * to the specified bound Type of this definition <b>can</b> be sampled from
	 * these specific sampled metric definitions. Also see AddOrGetDefinitions() to
	 * find and return all definitions in the inheritance chain of a type or object.
	 *
	 * @param userObjectType A specific Type with attributes defining a group of
	 *                       sampled metrics.
	 * @return The group of sampled metric definitions (as an array) determined by
	 *         attributes on the given Type.
	 */
	public static SampledMetricDefinition[] registerType(java.lang.Class userObjectType) {
		if (userObjectType == null) {
			throw new NullPointerException("A valid Type must be provided.");
		}
		return registerGroup(userObjectType, ""); // Search single Type only, no inheritance.
	}

	/**
	 * Find or create a single sampled metric definition (by counter name) from
	 * SampledMetric and SampledMetricValue attributes on a specific Type.
	 * 
	 * The provided type must have a SampledMetric attribute and can have one or
	 * more fields, properties or zero-argument methods with SampledMetricValue
	 * attributes defined. This method creates a metric definition but does not
	 * create a specific metric instance, so it does not require a live object. If
	 * the sampled metric definition already exists, it is just returned and no
	 * exception is thrown. If the provided type is not suitable to create an
	 * sampled metric from because it is missing the appropriate attribute or the
	 * attribute has been miss-defined, an ArgumentException will be thrown.
	 * Inheritance and interfaces will <b>not</b> be searched, so the provided Type
	 * must directly define an sampled metric, but valid objects of a type
	 * assignable to the specified bound Type of this definition <b>can</b> be
	 * sampled from this specific sampled metric definition. Also see
	 * AddOrGetDefinitions() to find and return an array of definitions.
	 *
	 * @param userObjectType A specific Type with attributes defining sampled
	 *                       metrics including the specified counter name.
	 * @param counterName    The counterName of a specific sampled metric to find or
	 *                       create under the SampledMetric attribute on the
	 *                       specified Type.
	 * @return The single sampled metric definition selected by counter name and
	 *         determined by attributes on the given Type.
	 */
	public static SampledMetricDefinition register(java.lang.Class userObjectType, String counterName) {
		if (userObjectType == null) {
			throw new NullPointerException("A valid Type must be provided.");
		}
		if (TypeUtils.isBlank(counterName)) {
			throw new NullPointerException(
					"A valid counter name must be specified to select a single sampled metric definition, or use the overload without a counterName parameter.");
		}

		SampledMetricDefinition[] definitions = registerGroup(userObjectType, counterName); // Specific counter.
		return (definitions.length > 0) ? definitions[0] : null;
	}

	/*
	 * /// <summary> /// Find or create multiple sampled metrics definitions
	 * (defined via SampledMetric attributes) for the provided object or Type. ///
	 * </summary> /// <remarks>The provided Type or the GetType() of the provided
	 * object instance will be scanned for SampledMetric /// attributes on itself
	 * and any of its interfaces to identify a list of sampled metrics defined for
	 * instances of /// that type, creating them as necessary by scanning its
	 * members for SampledMetricValue attributes. Inheritance /// will be followed
	 * into base types, along with all interfaces inherited to the top level. This
	 * method may throw /// exceptions, so a null argument will return an empty
	 * array, as will an argument which does not define any /// valid sampled
	 * metrics.</remarks>
	 */

	/**
	 * Find or create sampled metric definition from SampledMetric and
	 * SampledMetricValue attributes on a specific Type.
	 * 
	 * The provided type must have a SampledMetric attribute and can have one or
	 * more fields, properties or zero-argument methods with SampledMetricValue
	 * attributes defined. This method creates a metric definition but does not
	 * create a specific metric instance, so it does not require a live object. If
	 * the sampled metric definition already exists, it is just returned and no
	 * exception is thrown. If the provided type is not suitable to create sampled
	 * metrics from because it is missing the appropriate attributes or the
	 * attributes have been miss-defined, an ArgumentException will be thrown.
	 * Inheritance and interfaces will <b>not</b> be searched, so the provided Type
	 * must directly define an sampled metric, but valid objects of a type
	 * assignable to the specified bound Type of this definition <b>can</b> be
	 * sampled from these specific sampled metric definitions.
	 *
	 * @param userObjectType A specific Type with attributes defining an sampled
	 *                       metric.
	 * @param counterName    The counterName of a specific sampled metric to find or
	 *                       create, string.Empty to return the entire group of
	 *                       sampled metric definitions unless there are errors, or
	 *                       null to swallow errors.
	 * @return The single sampled metric definition determined by attributes on the
	 *         given Type.
	 */
	public static SampledMetricDefinition[] registerGroup(java.lang.Class userObjectType, String counterName) {
		if (userObjectType == null) {
			return new SampledMetricDefinition[0]; // Return an empty array; This is already checked in the public
													// wrappers.
		}

		List<SampledMetricDefinition> definitions = definitionsMap.get(userObjectType);

		// Check if we've scanned this specific Type before. We need the lock...
		synchronized (dictionaryLock) {
			if (definitions == null) {
				// We haven't scanned this Type before, start a new list.
				definitions = new ArrayList<SampledMetricDefinition>();

				// In this internal catch-all, counterName may be empty or null or a specific
				// counter name.
				// All errors must be swallowed (but logged) if counterName is null.

				// Check if it defines a group at this specific level, no inheritance search, no
				// interfaces search.
				if (!userObjectType.isAnnotationPresent(SampledMetricClass.class)) {
					if (counterName == null) {
						return definitions.toArray(new SampledMetricDefinition[0]); // Swallow all errors. Return empty
																					// array.
					}
					// Sorry, Attribute not found
					throw new IllegalArgumentException(
							"The specified Type does not have a SampledMetric attribute, so it can't be used to define sampled metrics.");
				}

				// OK, now waltz off and get the attribute we want.
				SampledMetricClass sampledMetricAnnotation = (SampledMetricClass) userObjectType
						.getAnnotation(SampledMetricClass.class);

				// Verify that the sampled metric attribute that we got is valid
				if (sampledMetricAnnotation == null) {
					if (counterName == null) {
						return definitions.toArray(new SampledMetricDefinition[0]); // Swallow all errors. Return empty
																					// array.
					}
					throw new IllegalArgumentException(
							"The specified Type does not have a usable SampledMetric attribute, so it can't be used to define sampled metrics.");
				}

				// make sure the user didn't do any extraordinary funny business
				String metricsSystem = sampledMetricAnnotation.namespace();
				if (TypeUtils.isBlank(metricsSystem)) {
					if (counterName == null) {
						return definitions.toArray(new SampledMetricDefinition[0]); // Swallow all errors. Return empty
																					// array.
					}
					throw new IllegalArgumentException(
							"The specified Type's SampledMetric attribute has an empty metric namespace which is not allowed, so no metrics can be defined under it.");
				}

				String metricCategoryName = sampledMetricAnnotation.categoryName();
				if (TypeUtils.isBlank(metricCategoryName)) {
					if (counterName == null) {
						return definitions.toArray(new SampledMetricDefinition[0]); // Swallow all errors. Return empty
																					// array.
					}
					throw new IllegalArgumentException(
							"The specified Type's SampledMetric attribute has an empty metric category name which is not allowed, so no metrics can be defined under it.");
				}

				// Now reflect all of the field/property/methods in the type so we can inspect
				// them for attributes.
				List<AccessibleObject> members = new ArrayList<AccessibleObject>();
				members.addAll(Arrays.asList(userObjectType.getFields()));
				members.addAll(Arrays.asList(userObjectType.getMethods()));

				// These will apply to every sampled metric in the logical group on this Type.
				NameValuePair<MemberType> instanceNameBinding = null;

				// We need to collect the mapping of divisors for later.
				Map<String, AccessibleObject> divisors = new HashMap<String, AccessibleObject>();

				for (AccessibleObject curMember : members) {
					MemberType curMemberType;
					String curMemberName;
					if (curMember instanceof Field) {
						curMemberType = MemberType.FIELD;
						curMemberName = ((Field)curMember).getName();
					} else {
						curMemberType = MemberType.METHOD;
						curMemberName = ((Method)curMember).getName();
					}
					// and what can we get from our little friend?
					if (curMember.isAnnotationPresent(SampledMetricInstanceName.class)) {
						// have we already bound our name?
						if (instanceNameBinding != null) {
							// yes, so report a duplicate name warning
						} else {
							// nope, we're good, so remember our binding information
							instanceNameBinding = new NameValuePair<MemberType>(curMemberName, curMemberType);
						}
					}

					if (curMember.isAnnotationPresent(SampledMetricDivisor.class)
							|| curMember.isAnnotationPresent(SampledMetricDivisors.class)) {
						// they have at least one sampled metric divisor attribute, go get all of them
						// we get back an array of objects just in case there are any non-CLS compliant
						// attributes defined, which there never are.
						SampledMetricDivisor[] curMemberValueAttributes =  curMember.getAnnotationsByType(SampledMetricDivisor.class);

						for (SampledMetricDivisor curDivisorAttribute : curMemberValueAttributes) {
							// cast it and test
							if (curDivisorAttribute != null) {
								IMetricDefinition existingMetricDefinition;
								String divisorCounterName = curDivisorAttribute.counterName();
								if (TypeUtils.isBlank(divisorCounterName)) {
								} else {
									OutObject<IMetricDefinition> tempOutExistingMetricDefinition = new OutObject<IMetricDefinition>();
									if (Log.getMetricDefinitions().tryGetValue(metricsSystem, metricCategoryName,
											divisorCounterName, tempOutExistingMetricDefinition)) {
										existingMetricDefinition = tempOutExistingMetricDefinition.argValue;
										SampledMetricDefinition sampledMetricDefinition = existingMetricDefinition instanceof SampledMetricDefinition
												? (SampledMetricDefinition) existingMetricDefinition
												: null;
										if (sampledMetricDefinition == null) {
											// Uh-oh, the definition already exists, but it isn't a sampled metric.
											// This is only a warning because this attribute just gets ignored by the
											// existing metric,
											// And they'll get a real error below if there's actually a
											// SampledMetricValue attribute for this counter name.
										} else if (!sampledMetricDefinition.isBound()
												|| sampledMetricDefinition.getBoundType() != userObjectType) {
											// Uh-oh, the definition already exists, but it isn't bound to this Type!
										} else if (!bindingMatchesMember(sampledMetricDefinition.getDivisorBinding(),
												curMember)) {
											// Uh-oh, the definition already exists, but it isn't bound to this member!
										}

										// Otherwise, the existing definition is correctly bound to this divisor
										// already. We're good here.
										// If it's already bound, we must have scanned this before and this was the
										// first matching
										// divisor attribute for the counter name, so mark us as the match.
										divisors.put(divisorCounterName, curMember); // Remember this member for later.
									} else {
										existingMetricDefinition = tempOutExistingMetricDefinition.argValue;
										if (divisors.containsKey(divisorCounterName)) {
											// They already had one with that counter name!
										} else {
											divisors.put(divisorCounterName, curMember); // Remember this member for
																							// later.
										}
									}
								}
							}
						}
					}
				}

				// We had to scan every member first for the instance name so we could bind it
				// in every sampled metric.
				// Otherwise we would have problems if it wasn't found before the sampled
				// metrics. Now go back and scan again.

				for (AccessibleObject curMember : members) {
					MemberType curMemberType;
					String curMemberName;
					if (curMember instanceof Field) {
						curMemberType = MemberType.FIELD;
						curMemberName = ((Field)curMember).getName();
					} else {
						curMemberType = MemberType.METHOD;
						curMemberName = ((Method)curMember).getName();
					}
					
					// Look for SampledMetricValue attributes to actually defined sampled metric
					// counters.
					if (curMember.isAnnotationPresent(SampledMetricValue.class) ||
							curMember.isAnnotationPresent(SampledMetricValues.class)) {
						// What type of value does this member give? It'll be the same for every value
						// attribute on it!
						java.lang.Class curType = getTypeOfMember(curMember);

						// they have at least one sampled metric value attribute, go get all of them
						// we get back an array of objects just in case there are any non-CLS compliant
						// attributes defined, which there never are.
						SampledMetricValue[] curMemberValueAttributes = curMember.getAnnotationsByType(SampledMetricValue.class);

						for (SampledMetricValue curValueAttribute : curMemberValueAttributes) {
							// cast it and test
							if (curValueAttribute != null) {
								// apply defaults (because this is the only place to get the name of the marked
								// member)
								String metricCounterName = TypeUtils.trim(curValueAttribute.counterName());

								// First time we've seen this Type, scan the whole thing even if they only
								// wanted one.

								SamplingType samplingType = curValueAttribute.samplingType();
								// We use a lock because we need to have the check and the add (which happens as
								// part of the new metric definition) happen as a single event.
								Object metricDefinitionsLock = Log.getMetricDefinitions().getLock();
								synchronized (metricDefinitionsLock) {
									// System.Threading.Enter(metricDefinitionsLock);

									IMetricDefinition rawMetricDefinition;
									OutObject<IMetricDefinition> tempOutRawMetricDefinition = new OutObject<IMetricDefinition>();
									if (Log.getMetricDefinitions().tryGetValue(metricsSystem, metricCategoryName,
											metricCounterName, tempOutRawMetricDefinition)) {
										rawMetricDefinition = tempOutRawMetricDefinition.argValue;
										SampledMetricDefinition sampledMetricDefinition = rawMetricDefinition instanceof SampledMetricDefinition
												? (SampledMetricDefinition) rawMetricDefinition
												: null;
										if (sampledMetricDefinition == null) {
											// Uh-oh, the definition already exists, but it isn't a sampled metric!
										} else if (!sampledMetricDefinition.isBound()
												|| sampledMetricDefinition.getBoundType() != userObjectType) {
											// Uh-oh, the definition already exists, but it isn't bound to this Type!
										} else if (!bindingMatchesMember(sampledMetricDefinition.getDataBinding(),
												curMember)) {
											// Uh-oh, the definition already exists, but it isn't bound to this member!
										} else {
											definitions.add(sampledMetricDefinition); // Found one! Add it to our list.
										}
									} else {
										rawMetricDefinition = tempOutRawMetricDefinition.argValue;
										if (curType == null) // Warn about an unreadable property.
										{
										} else if (curType == void.class) // Warn about a void method.
										{
										} else if (!isValidDataType(curType)) {
										} else {
											AccessibleObject divisorInfo;
											if (divisors.containsKey(metricCounterName)) {
												divisorInfo = divisors.get(metricCounterName);
												// We found a divisor attribute for this counter name earlier.
												// Does this counter actually need one?
												if (!requiresDivisor(samplingType)) {
													// It doesn't use one. Warn the user.
													divisorInfo = null;
												}

												// Otherwise, we leave divisorInfo valid, so we bind it below.
											} else {
												divisorInfo = null; // Didn't find any, make sure it's marked invalid.
												if (requiresDivisor(samplingType)) {
													// Uh-oh! We need a divisor but they didn't specify one.
												}
											}

											// Now that we have the info for a new sampled metric definition
											// and passed all the checks, we need to create it.
											SampledMetricDefinition newMetricDefinition = new SampledMetricDefinition(
													metricsSystem, metricCategoryName, metricCounterName, samplingType,
													curValueAttribute.unitCaption(), curValueAttribute.caption(),
													curValueAttribute.description());
											newMetricDefinition.setBoundType(userObjectType);
											newMetricDefinition.setIsBound(true);

											newMetricDefinition.setDataBinding(curMember);

											if (divisorInfo != null) {
												newMetricDefinition.setDivisorBinding(divisorInfo);
											}

											if (instanceNameBinding != null) {
												// ToDo: Push NVP binding handle through for instanceName binding
												// instead of separate properties.
												newMetricDefinition.setNameMemberType(instanceNameBinding.getValue());
												newMetricDefinition.setNameMemberName(instanceNameBinding.getName());
												newMetricDefinition.setNameBound(true);
											}

											// ToDo: Set it read-only and add to collection, following EventMetric
											// model?

											definitions.add(newMetricDefinition); // Add it to our list.
										}
									}
								} // end of metric definitions lock

							} // end check that this SampledMetricValue attribute is valid
						} // end foreach loop over SampledMetricValue attributes on a given member
					} // end check for SampledMetricValue attribute

				} // end of foreach loop over members

				// Now we need to remember this list of definitions, to save time on future
				// lookups.
				definitionsMap.put(userObjectType, definitions);
			} // end of definition map dictionary-lookup-failed.

			// If we found an entry, definitions was set for us by the TryGetValue(), so
			// we're good.

		} // end of dictionary lock

		// We scanned for all of them (the first time), but did they ask for just one?
		if (TypeUtils.isNotBlank(counterName)) {
			SampledMetricDefinition[] mappedDefinitions = definitions.toArray(new SampledMetricDefinition[0]); // Save a
																												// snapshot
																												// to
																												// loop
																												// over...
			definitions = new ArrayList<SampledMetricDefinition>(); // Now reset the list; they only want one.
			for (SampledMetricDefinition definition : mappedDefinitions) {
				if (definition.getCounterName().equals(counterName)) {
					definitions.add(definition); // Add the right one to the list.
				}
			}
		}

		return definitions.toArray(new SampledMetricDefinition[0]); // Return a copy of what we found (they can't change
																	// our internal cached list).
	}

	/**
	 * Checks the provided Type against the list of recognized numeric types and
	 * special types supported for SampledMetric data.
	 * 
	 * Sampled metrics require inherently numeric samples, so only data with a
	 * numeric Type or of a recognized Type which can be converted to a numeric
	 * value in a standard way can be sampled for a sampled metric counter.
	 * Supported numeric .NET types include: Double, Single, Decimal, Int64, UInt64,
	 * Int32, UInt32, Int16, and UInt16. The common time representation types:
	 * DateTime, DateTimeOffset, and TimeSpan are also supported by automatically
	 * taking their Ticks value. All sampled metric data samples are converted to a
	 * Double (double-precision floating point) value when sampled.
	 * 
	 * @param userDataType The typeof(SomeSpecificType) or dataValue.GetType() to
	 *                     check.
	 * @return True if Loupe supports sampled metric data samples with that Type,
	 *         otherwise false.
	 */
	public static boolean isValidDataType(java.lang.Class userDataType) {
		return dataTypeSupported.containsKey(userDataType);
	}

	/**
	 * Checks the provided SamplingType enum value to determine if that sampling
	 * type requires two values per sample.
	 * 
	 * Sampled metrics sample either single values (*Count sampling types) or pairs
	 * of values (*Fraction sampling types), determined by their sampling type. This
	 * method distinguishes between the two scenarios. The *Fraction sampling types
	 * record a numerator and a divisor for each sample, so when defining sampled
	 * metric counters with SampledMetric and SampledMetricValue attributes, these
	 * sampling types require a corresponding SampledMetricDivisor attribute for the
	 * same counter name. The *Count sampling types only record a single value for
	 * each sample, so they do not need a divisor to be specified.
	 * 
	 * @param samplingType A SamplingType enum value to check.
	 * @return True if the given sampling type requires a second value for each
	 *         sample as the divisor.
	 */
	public static boolean requiresDivisor(SamplingType samplingType) {
		boolean required;
		switch (samplingType) {
		case RAW_FRACTION:
		case INCREMENTAL_FRACTION:
		case TOTAL_FRACTION:
			required = true;
			break;
		default:
			required = false;
			break;
		}

		return required;
	}

	/**
	 * Determine the readable Type for a field, property, or method.
	 * 
	 * This method assumes that only MemberType of Field, Property, or Method will
	 * be given. A method with void return type will return typeof(void), and
	 * properties with no get accessor will return null. This does not currently
	 * check method signature info for the zero-argument requirement.
	 * 
	 * @param member The MemberInfo of a Field, Property, or Method member.
	 * @return The Type of value which can be read from the field, property, or
	 *         method.
	 */
	private static java.lang.Class getTypeOfMember(AccessibleObject member) {
		java.lang.Class readType;
		if (member instanceof Method) {
			// For methods, it's the return value type.
			readType = ((java.lang.reflect.Method) member).getReturnType();
		} else if (member instanceof Field) {
			// For fields, it's the field type... They can always be read (through
			// reflection, that is).
			java.lang.reflect.Field fieldInfo = (java.lang.reflect.Field) member;
			readType = fieldInfo.getType();
		} else {
			// Nothing else is supported; return null.
			readType = null;
		}

		return readType;
	}

	/**
	 * Check that the specified binding matches the specified member, assuming the
	 * same declaring type.
	 * 
	 * @param binding A NameValuePair&lt;MemberType&gt; representing the name and
	 *                member type binding to check.
	 * @param member  The MemberInfo of the member to check against.
	 * @return True if the binding matches the given member, false if the binding
	 *         (or member) is null or does not match.
	 */
	private static boolean bindingMatchesMember(NameValuePair<MemberType> binding, AccessibleObject member) {
		String name = null;
		Class memberType = null;
		if (MemberType.FIELD.equals(binding.getValue())) {
			Field field = (Field)member;
			name = field.getName();
			memberType = Field.class;
		} else if (MemberType.METHOD.equals(binding.getValue())) {
			Method method = (Method)member;
			name = method.getName();
			memberType = Method.class;
		}
		return (binding != null && member != null && memberType.isInstance(member)
				&& binding.getName().equals(name));
	}

	/**
	 * The intended method of interpreting the sampled counter value.
	 *
	 * @return the sampling type
	 */
	public SamplingType getSamplingType() {
		return SamplingType.forValue(this.packet.getMetricSampleType().getValue());
	}

	/**
	 * Indicates whether a final value can be determined from just one sample or if
	 * two comparable samples are required.
	 *
	 * @return the requires multiple samples
	 */
	public boolean getRequiresMultipleSamples() {
		return this.requiresMultipleSamples;
	}

	/**
	 * Indicates if this definition is configured to retrieve its information
	 * directly from an object.
	 * 
	 * When true, metric instances and samples can be generated from a live object
	 * of the same type that was used to generate the data binding. It isn't
	 * necessary that the same object be used, just that it be a compatible type to
	 * the original type used to establish the binding.
	 *
	 * @return true, if is bound
	 */
	public boolean isBound() {
		return this.bound;
	}

	/**
	 * Sets the checks if is bound.
	 *
	 * @param value the new checks if is bound
	 */
	public void setIsBound(boolean value) {
		this.bound = value;
	}

	/**
	 * When bound, indicates the exact interface or object type that was bound.
	 * 
	 * When creating new metrics or metric samples, this data type must be provided
	 * in bound mode.
	 *
	 * @return the bound type
	 */
	public java.lang.Class getBoundType() {
		return this.boundType;
	}

	/**
	 * Sets the bound type.
	 *
	 * @param value the new bound type
	 */
	public void setBoundType(java.lang.Class value) {
		this.boundType = value;
	}

	/**
	 * The set of custom sampled metrics that use this definition.
	 * 
	 * All metrics with the same definition are of the same object type.
	 *
	 * @return the metrics
	 */
	protected Map<String, SampledMetric> getMetrics() {
		return metrics;
	}
	
	/**
	 * Retrieves the specified metric instance, or creates it if it doesn't exist.
	 *
	 * @param instanceName the instance name
	 * @return The custom sampled metric object.
	 */
	public SampledMetric addOrGetMetric(String instanceName) {
		// Find the right metric sample instance, creating it if we have to.
		SampledMetric ourMetric;
		
		if (TypeUtils.isBlank(instanceName)) {
			instanceName = null;
		}
		
		// This must be protected in a multi-threaded environment
		synchronized (lock) {
			ourMetric = metrics.get(instanceName);
			if (ourMetric == null) {
				ourMetric = new SampledMetric(this, new CustomSampledMetricPacket(packet, instanceName));
				metrics.put(instanceName, ourMetric);
			}
		}

		// and return the metric
		return ourMetric;
	}

	/**
	 * Write a metric sample to the specified metric instance with the provided data
	 * immediately, creating the metric if it doesn't exist.
	 * 
	 * 
	 * <p>
	 * The sample is immediately written to the log. If you are sampling multiple
	 * metrics at the same time, it is faster to create each of the samples and
	 * write them with one call to Log.Write instead of writing them out
	 * individually. To do this, you can use CreateMetricSample.
	 * </p>
	 * <p>
	 * Custom metrics using a sample type of AverageFraction and DeltaFraction
	 * should not use this method because they require a base value as well as a raw
	 * value.
	 * </p>
	 * 
	 * @param instanceName The instance name to use, or blank or null for the
	 *                     default metric.
	 * @param rawValue     The raw data value
	 */
	public void writeSample(String instanceName, double rawValue) {
		// Find the right metric sample instance, creating it if we have to.
		SampledMetric ourMetric = addOrGetMetric(instanceName);

		// now that we have the right metric object, its time to go ahead and create the
		// sample.
		ourMetric.writeSample(rawValue);
	}

	/**
	 * Write a metric sample to the specified metric instance with the provided data
	 * immediately, creating the metric if it doesn't exist.
	 * 
	 * 
	 * <p>
	 * The sample is immediately written to the log. If you are sampling multiple
	 * metrics at the same time, it is faster to create each of the samples and
	 * write them with one call to Log.Write instead of writing them out
	 * individually. To do this, you can use CreateMetricSample.
	 * </p>
	 * <p>
	 * Custom metrics using a sample type of AverageFraction and DeltaFraction
	 * should not use this method because they require a base value as well as a raw
	 * value.
	 * </p>
	 * 
	 * @param instanceName The instance name to use, or blank or null for the
	 *                     default metric.
	 * @param rawValue     The raw data value
	 * @param rawTimeStamp The exact date and time the raw value was determined
	 */
	public void writeSample(String instanceName, double rawValue, OffsetDateTime rawTimeStamp) {
		// Find the right metric sample instance, creating it if we have to.
		SampledMetric ourMetric = addOrGetMetric(instanceName);

		// now that we have the right metric object, its time to go ahead and create the
		// sample.
		ourMetric.writeSample(rawValue, rawTimeStamp);
	}

	/**
	 * Write a metric sample to the specified metric instance with the provided data
	 * immediately, creating the metric if it doesn't exist.
	 * 
	 * The sample is immediately written to the log. If you are sampling multiple
	 * metrics at the same time, it is faster to create each of the samples and
	 * write them with one call to Log.Write instead of writing them out
	 * individually. To do this, you can use CreateMetricSample
	 * 
	 * @param instanceName The instance name to use, or blank or null for the
	 *                     default metric.
	 * @param rawValue     The raw data value
	 * @param baseValue    The reference value to compare against for come counter
	 *                     types
	 */
	public void writeSample(String instanceName, double rawValue, double baseValue) {
		// Find the right metric sample instance, creating it if we have to.
		SampledMetric ourMetric = addOrGetMetric(instanceName);

		// now that we have the right metric object, its time to go ahead and create the
		// sample.
		ourMetric.writeSample(rawValue, baseValue);
	}

	/**
	 * Write a metric sample to the specified metric instance with the provided data
	 * immediately, creating the metric if it doesn't exist.
	 * 
	 * The sample is immediately written to the log. If you are sampling multiple
	 * metrics at the same time, it is faster to create each of the samples and
	 * write them with one call to Log.Write instead of writing them out
	 * individually. To do this, you can use CreateMetricSample
	 *
	 * @param instanceName The instance name to use, or blank or null for the
	 *                     default metric.
	 * @param rawValue     The raw data value
	 * @param baseValue    The reference value to compare against for come counter
	 *                     types
	 * @param rawTimeStamp The exact date and time the raw value was determined
	 */
	public void writeSample(String instanceName, double rawValue, double baseValue, OffsetDateTime rawTimeStamp) {
		// Find the right metric sample instance, creating it if we have to.
		SampledMetric ourMetric = addOrGetMetric(instanceName);

		// now that we have the right metric object, its time to go ahead and create the
		// sample.
		ourMetric.writeSample(rawValue, baseValue, rawTimeStamp);
	}

	/**
	 * Write a metric sample to the specified instance of this metric definition
	 * using the provided data object.
	 * 
	 * 
	 * <p>
	 * This overload may only be used if this metric definition was created by
	 * SampledMetric and SampledMetricValue attributes on a particular Type (class,
	 * struct, or interface), and only for userDataObjects of a type assignable to
	 * that bound type for this definition. Also see the static WriteAllSamples()
	 * method.
	 * </p>
	 * <p>
	 * The provided instanceName parameter will override any instance name binding
	 * set for this definition with a SampledMetricInstanceName attribute (this
	 * method overload ignores the instance name binding). The specified metric
	 * instance is created if it does not already exist. See the other overloads
	 * taking a userDataObject as the first parameter to use the bound member to
	 * determine a metric instance name from the user data object automatically,
	 * with an optional fall-back instance name.
	 * </p>
	 *
	 * @param instanceName The instance name to use, or null or empty for the
	 *                     default metric instance.
	 * @param metricData   A data object to sample, compatible with the binding type
	 *                     of this definition.
	 */
	public void writeSample(String instanceName, Object metricData) {
		if (metricData == null) {
			throw new NullPointerException("metricData");
		}

		if (!isBound()) {
			throw new IllegalArgumentException(
					"This sampled metric definition is not bound to sample automatically from a user data object.  A different overload must be used to specify the data value(s) directly.");
		}

		java.lang.Class userDataType = metricData.getClass();
		if (!getBoundType().isAssignableFrom(userDataType)) {
			throw new IllegalArgumentException(String.format(Locale.ROOT,
					"The provided user data object type (%1$s) is not assignable to this sampled metric's bound type (%2$s) and can not be sampled automatically for this metric definition.",
					userDataType, getBoundType()));
		}

		SampledMetric metricInstance = SampledMetric.register(this, instanceName); // Get the particular instance
																					// specified.
		metricInstance.writeSample(metricData); // And write a sample from the provided data object.
	}

	/**
	 * Write a metric sample to an automatically-determined instance of this metric
	 * definition using the provided data object, with a fall-back instance name.
	 * 
	 * 
	 * <p>
	 * This overload may only be used if this metric definition was created by
	 * SampledMetric and SampledMetricValue attributes on a particular Type (class,
	 * struct, or interface), and only for userDataObjects of a type assignable to
	 * that bound type for this definition.
	 * </p>
	 * <p>
	 * The metric instance name will be obtained from the member which was marked
	 * with the SampledMetricInstanceName attribute. If none is bound, the instance
	 * name parameter will be used as a fall-back. The determined metric instance
	 * will be created if it does not already exist.
	 * </p>
	 *
	 * @param metricData           A data object to sample, compatible with the
	 *                             binding type of this definition.
	 * @param fallbackInstanceName The instance name to fall back on if this
	 *                             definition does not specify an instance name
	 *                             binding (may be null).
	 */
	public void writeSample(Object metricData, String fallbackInstanceName) {
		if (metricData == null) {
			throw new NullPointerException("metricData");
		}

		if (!isBound()) {
			throw new IllegalArgumentException(
					"This sampled metric definition is not bound to sample automatically from a user data object.  A different overload must be used to specify the data value(s) directly.");
		}

		java.lang.Class userDataType = metricData.getClass();
		if (!getBoundType().isAssignableFrom(userDataType)) {
			throw new IllegalArgumentException(String.format(Locale.ROOT,
					"The provided user data object type (%1$s) is not assignable to this sampled metric's bound type (%2$s) and can not be sampled automatically for this metric definition.",
					userDataType, getBoundType()));
		}

		String autoInstanceName = fallbackInstanceName; // Use the fall-back instance unless we find a specific instance
														// name.

		if (getNameBound()) {
			String tempVar = invokeInstanceNameBinding(metricData);
			autoInstanceName = (tempVar != null) ? tempVar : fallbackInstanceName; // Use fall-back on errors.
		}

		if (TypeUtils.isBlank(autoInstanceName)) {
			autoInstanceName = null; // Convert empty string back to null.
		}

		// Now use our other overload with the instance name we just grabbed (or the
		// default we set first).
		writeSample(autoInstanceName, metricData);
	}

	/**
	 * Write a metric sample to an automatically-determined instance of this metric
	 * definition using the provided data object.
	 * 
	 * 
	 * <p>
	 * This overload may only be used if this metric definition was created by
	 * SampledMetric and SampledMetricValue attributes on a particular Type (class,
	 * struct, or interface), and only for userDataObjects of a type assignable to
	 * that bound type for this definition.
	 * </p>
	 * <p>
	 * The metric instance name will be obtained from the member which was marked
	 * with the SampledMetricInstanceName attribute. If none is bound, the default
	 * instance will be used (a null instance name). The determined metric instance
	 * will be created if it does not already exist. See the overloads with an
	 * instanceName parameter to specify a particular metric instance name.
	 * </p>
	 *
	 * @param metricData A data object to sample, compatible with the binding type
	 *                   of this definition.
	 */
	public void writeSample(Object metricData) {
		writeSample(metricData, null);
	}

	/**
	 * Use the instance name binding for this definition to query the instance name
	 * of a given user data object.
	 * 
	 * If not bound, this method returns null.
	 * 
	 * @param metricData A live object instance (does not work on a Type).
	 * @return The instance name determined by the binding query.
	 */
	public String invokeInstanceNameBinding(Object metricData) {
		java.lang.Class userDataType = metricData.getClass();
		if (!getBoundType().isAssignableFrom(userDataType)) {
			return null; // Doesn't match the bound type, can't invoke it.
		}

		// ToDo: Change instance name binding to use NVP (or a new Binding class).
		NameValuePair<MemberType> nameBinding = new NameValuePair<MemberType>(getNameMemberName(), getNameMemberType());

		String autoInstanceName = null;
		try {
			Object rawName = null;
			if (getNameMemberType().equals(MemberType.FIELD)) {
				Field field = userDataType.getDeclaredField(getNameMemberName());
				field.setAccessible(true);
				rawName = field.get(metricData);
			} else if (getNameMemberType().equals(MemberType.METHOD)) {
				Method method = userDataType.getMethod(getNameMemberName());
				method.setAccessible(true);
				rawName = method.invoke(metricData);
			}

			if (rawName == null || rawName.getClass() == String.class) {
				autoInstanceName = rawName instanceof String ? (String) rawName : null; // Null, or an actual
																						// string. We're cool with
																						// either.
			} else {
				autoInstanceName = rawName.toString(); // Convert it to a string, because that's what we need.
			}

			if (autoInstanceName == null) {
				autoInstanceName = ""; // Use this to report a valid "default instance" result.
			}
		} catch (java.lang.Exception e) {
			autoInstanceName = null; // Null reports a failure case.
		}

		return autoInstanceName;
	}

	/**
	 * Sample every sampled metric defined by SampledMetric and SampledMetricValue
	 * attributes on the provided data object at any interface or inheritance level.
	 * 
	 * @param metricData           A user data object defining sampled metrics by
	 *                             attributes on itself or its interfaces or any
	 *                             inherited type.
	 * @param fallbackInstanceName The instance name to fall back on if a given
	 *                             definition does not specify an instance name
	 *                             binding (may be null).
	 */
	public static void write(Object metricData, String fallbackInstanceName) {
		// Actual logic is in SampledMetric class.
		SampledMetric.write(metricData, fallbackInstanceName);
	}

	/**
	 * Sample every sampled metric defined by SampledMetric and SampledMetricValue
	 * attributes on the provided data object at any interface or inheritance level.
	 * 
	 * @param metricData A user data object defining sampled metrics by attributes
	 *                   on itself or its interfaces or any inherited type.
	 */
	public static void write(Object metricData) {
		// Actual logic is in SampledMetric class.
		SampledMetric.write(metricData, "");
	}

	/**
	 * Indicates whether two samples are required to calculate a metric value or
	 * not.
	 * 
	 * 
	 * Many sample types require multiple samples to determine an output value
	 * because they work with the change between two points.
	 *
	 * @param samplingType the sampling type
	 * @return true, if successful
	 */
	public static boolean sampledMetricTypeRequiresMultipleSamples(SamplingType samplingType) {
		boolean multipleRequired;

		// based purely on the counter type, according to Microsoft documentation
		switch (samplingType) {
		case RAW_FRACTION:
		case RAW_COUNT:
			// these just require one sample
			multipleRequired = false;
			break;
		default:
			// everything else requires more than one sample
			multipleRequired = true;
			break;
		}

		return multipleRequired;
	}

	/**
	 * Indicates if there is a binding for metric instance name.
	 * 
	 * When true, the Name Member Name and Name Member Type properties are
	 * available.
	 *
	 * @return the name bound
	 */
	public boolean getNameBound() {
		return this.nameBound;
	}

	/**
	 * Sets the name bound.
	 *
	 * @param value the new name bound
	 */
	public void setNameBound(boolean value) {
		this.nameBound = value;
	}

	/**
	 * The name of the member to invoke to determine the metric instance name.
	 * 
	 * This property is only valid when NameBound is true.
	 *
	 * @return the name member name
	 */
	public String getNameMemberName() {
		return this.nameMemberName;
	}

	/**
	 * Sets the name member name.
	 *
	 * @param value the new name member name
	 */
	public void setNameMemberName(String value) {
		this.nameMemberName = value;
	}

	/**
	 * The type of the member to be invoked to determine the metric instance name
	 * (field, method, or property)
	 * 
	 * This property is only valid when NameBound is true.
	 *
	 * @return the name member type
	 */
	public MemberType getNameMemberType() {
		return this.nameMemberType;
	}

	/**
	 * Sets the name member type.
	 *
	 * @param value the new name member type
	 */
	public void setNameMemberType(MemberType value) {
		this.nameMemberType = value;
	}

	/**
	 * Get the lock object for this sampled metric definition.
	 *
	 * @return the lock
	 */
	public Object getLock() {
		return this.lock;
	}

	/**
	 * Set the binding for the primary sampling data value (numerator);.
	 *
	 * @param member The MemberInfo of the member to bind to.
	 */
	private void setDataBinding(AccessibleObject member) {
		MemberType memberType;
		String memberName;
		if (member instanceof Field) {
			memberType = MemberType.FIELD;
			memberName = ((Field)member).getName();
		} else {
			memberType = MemberType.METHOD;
			memberName = ((Method)member).getName();			
		}
		setDataBinding(new NameValuePair<MemberType>(memberName, memberType));
	}

	/**
	 * Contains a name-value pair of data member name and MemberType, or null if not
	 * bound.
	 */
	private NameValuePair<MemberType> dataBinding;

	/**
	 * Gets the data binding.
	 *
	 * @return the data binding
	 */
	public NameValuePair<MemberType> getDataBinding() {
		return this.dataBinding;
	}

	/**
	 * Sets the data binding.
	 *
	 * @param value the new data binding
	 */
	private void setDataBinding(NameValuePair<MemberType> value) {
		this.dataBinding = value;
	}

	/**
	 * Indicates whether the value is configured for automatic collection through
	 * binding.
	 * 
	 * If true, the other binding-related properties are available.
	 *
	 * @return the data bound
	 */
	public boolean getDataBound() {
		return (getDataBinding() != null);
	}

	/**
	 * The type of member that this value is bound to (field, property or method).
	 * 
	 * This property is only valid if Bound is true.
	 *
	 * @return the data member type
	 */
	public MemberType getDataMemberType() {
		return getDataBound() ? getDataBinding().getValue() : MemberType.UNBOUND;
	}

	/**
	 * The name of the member that this value is bound to.
	 * 
	 * This property is only valid if Bound is true.
	 *
	 * @return the data member name
	 */
	public String getDataMemberName() {
		return getDataBound() ? getDataBinding().getName() : null;
	}

	/**
	 * Set the binding for the secondary sampling data value (divisor);.
	 *
	 * @param member The MemberInfo of the member to bind to.
	 */
	private void setDivisorBinding(AccessibleObject member) {
		MemberType memberType;
		String memberName;
		if (member instanceof Field) {
			memberType = MemberType.FIELD;
			memberName = ((Field)member).getName();
		} else {
			memberType = MemberType.METHOD;
			memberName = ((Method)member).getName();			
		}
		setDivisorBinding(new NameValuePair<MemberType>(memberName, memberType));
	}

	/**
	 * Contains a name-value pair of divisor member name and MemberType, or null if
	 * not bound.
	 */
	private NameValuePair<MemberType> divisorBinding;

	/**
	 * Gets the divisor binding.
	 *
	 * @return the divisor binding
	 */
	public NameValuePair<MemberType> getDivisorBinding() {
		return this.divisorBinding;
	}

	/**
	 * Sets the divisor binding.
	 *
	 * @param value the new divisor binding
	 */
	private void setDivisorBinding(NameValuePair<MemberType> value) {
		this.divisorBinding = value;
	}

	/**
	 * Indicates whether the divisor is configured for automatic collection through
	 * binding.
	 * 
	 * If true, the other binding-related properties are available.
	 *
	 * @return the divisor bound
	 */
	public boolean getDivisorBound() {
		return (getDivisorBinding() != null);
	}

	/**
	 * The type of member that the divisor is bound to (field, property or method).
	 * 
	 * This property is only valid if Bound is true.
	 *
	 * @return the divisor member type
	 */
	public MemberType getDivisorMemberType() {
		return getDivisorBound() ? getDivisorBinding().getValue() : MemberType.UNBOUND;
	}

	/**
	 * The name of the member that the divisor is bound to.
	 * 
	 * This property is only valid if Bound is true.
	 *
	 * @return the divisor member name
	 */
	public String getDivisorMemberName() {
		return getDivisorBound() ? getDivisorBinding().getName() : null;
	}

	/**
	 * The unique Id of this sampled metric definition. This can reliably be used as
	 * a key to refer to this item, within the same session which created it.
	 * 
	 * The Id is limited to a specific session, and thus identifies a consistent
	 * unchanged definition. The Id can <b>not</b> be used to identify a definition
	 * across different sessions, which could have different actual definitions due
	 * to changing user code. See the Key property to identify a metric definition
	 * across different sessions.
	 *
	 * @return the id
	 */
	@Override
	public UUID getId() {
		return this.packet.getID();
	}

	/**
	 * The three-part key of the metric definition being captured, as a single
	 * string.
	 * 
	 * The Key is the combination of metrics capture system label, category name,
	 * and counter name to uniquely identify a specific metric definition. It can
	 * also identify the same definition across different sessions.
	 *
	 * @return the key
	 */
	@Override
	public String getKey() {
		return this.packet.getName();
	}

	/**
	 * A short display string for this metric definition, suitable for end-user
	 * display.
	 *
	 * @return the caption
	 */
	@Override
	public String getCaption() {
		return this.packet.getCaption();
	}
	// internal set { _WrappedDefinition.Caption = value; }

	/**
	 * A description of what is tracked by this metric, suitable for end-user
	 * display.
	 *
	 * @return the description
	 */
	@Override
	public String getDescription() {
		return this.packet.getDescription();
	}
	// internal set { _WrappedDefinition.Description = value; }

	/**
	 * The recommended default display interval for graphing.
	 *
	 * @return the interval
	 */
	@Override
	public SamplingInterval getInterval() {
		return SamplingInterval.forValue(this.packet.getInterval().getValue());
	}

	/**
	 * The display caption for the units this metric's values represent, or null for
	 * unit-less values.
	 *
	 * @return the unit caption
	 */
	public String getUnitCaption() {
		return this.packet.getUnitCaption();
	}

	/**
	 * The metric capture system label under which this metric definition was
	 * created.
	 * 
	 * This label distinguishes metrics defined and captured by different libraries
	 * from each other, ensuring that metrics defined by different development
	 * groups will fall under separate namespaces and not require category names to
	 * be globally unique across third party libraries linked by an application.
	 * Pick your own label which will uniquely identify your library or namespace.
	 *
	 * @return the metrics system
	 */
	@Override
	public String getMetricsSystem() {
		return this.packet.getMetricTypeName();
	}

	/**
	 * The category of this metric for display purposes. This can be a period
	 * delimited string to represent a variable height hierarchy.
	 *
	 * @return the category name
	 */
	@Override
	public String getCategoryName() {
		return this.packet.getCategoryName();
	}

	/**
	 * The display name of this metric (unique within the category name).
	 *
	 * @return the counter name
	 */
	@Override
	public String getCounterName() {
		return this.packet.getCounterName();
	}

	/**
	 * The sample type of the metric. Indicates whether the metric represents
	 * discrete events or a continuous value.
	 *
	 * @return the sample type
	 */
	@Override
	public SampleType getSampleType() {
		return this.packet.getSampleType();
	}

	/**
	 * Indicates that this sampled metric definition has been registered and can not
	 * be altered (always true for sampled metric definitions).
	 *
	 * @return true, if is read only
	 */
	@Override
	public boolean isReadOnly() {
		return this.packet.isReadOnly();
	}
	
	/**
	 * Set this metric definition to be read-only and lock out further changes,
	 * allowing it to be instantiated and sampled.
	 */
	public void setReadOnly() {
		synchronized (getLock()) {
			this.packet.setIsReadOnly(true);
		}
	}

	/**
	 * Determines whether the collection of all metric definitions contains an
	 * element with the specified key.
	 * 
	 * @param id The metric definition Id to locate in the collection
	 * @return true if the collection contains an element with the key; otherwise,
	 *         false.
	 */
	public static boolean containsKey(UUID id) {
		// gateway to our inner dictionary
		return definitions.containsKey(id);
	}

	/**
	 * Determines whether the collection of all metric definitions contains an
	 * element with the specified key.
	 *
	 * @param key The Key of the event metric definition to check (composed of the
	 *            metrics system, category name, and counter name combined as a
	 *            single string).
	 * @return true if the collection contains an element with the key; otherwise,
	 *         false.
	 */
	public static boolean containsKey(String key) {
		// protect ourself from a null before we do the trim (or we'll get an odd user
		// the error won't understand)
		if (TypeUtils.isBlank(key)) {
			throw new NullPointerException("key");
		}

		// gateway to our alternate inner dictionary
		return definitions.containsKey(key.trim());
	}

	/**
	 * Determines whether the collection of all metric definitions contains an
	 * element with the specified key.
	 *
	 * @param metricsSystem The metrics capture system label.
	 * @param categoryName  The name of the category with which this definition is
	 *                      associated.
	 * @param counterName   The name of the definition within the category.
	 * @return true if the collection contains an element with the key; otherwise,
	 *         false.
	 */
	public static boolean containsKey(String metricsSystem, String categoryName, String counterName) {
		// gateway to our alternate inner dictionary
		return definitions.containsKey(metricsSystem, categoryName, counterName);
	}

	/**
	 * Retrieve a SampledMetricDefinition by its Id, if present. (Throws an
	 * ArgumentException if the Id resolves to an EventMetricDefinition instead.)
	 * 
	 * This method looks in the collection of registered metric definitions for the
	 * specified Id key. If it is not found, the output is set to null and the
	 * method returns false. If the Id key is found and resolves to a
	 * SampledMetricDefinition, it is stored in the value output parameter and the
	 * method returns true. If the Id key is found but is not a
	 * SampledMetricDefinition, an ArgumentException is thrown to signal a usage
	 * inconsistency in your code.
	 * 
	 * @param id    The Id of the sampled metric definition to get.
	 * @param value The output variable to receive the SampledMetricDefinition
	 *              object if found (null if not).
	 * @return False if no metric definition is registered with the given Id, true
	 *         if a SampledMetricDefinition is registered with the given Id, or
	 *         throws an exception if the registered definition is not a
	 *         SampledMetricDefinition.
	 */
	public static boolean tryGetValue(UUID id, OutObject<SampledMetricDefinition> value) {
		IMetricDefinition definition;

		// gateway to our internal collection TryGetValue()
		OutObject<IMetricDefinition> tempOutDefinition = new OutObject<IMetricDefinition>();
		boolean foundValue = definitions.tryGetValue(id, tempOutDefinition);
		definition = tempOutDefinition.argValue;
		value.argValue = foundValue
				? definition instanceof SampledMetricDefinition ? (SampledMetricDefinition) definition : null
				: null;
		if (foundValue && value.argValue == null) {
			// Uh-oh, we found one but it didn't resolve to an EventMetricDefinition!
			throw new IllegalArgumentException(String.format(Locale.ROOT,
					"The metric definition found by Id (%1$s) is not a sampled metric definition.", id));
		}
		return foundValue;
	}

	/**
	 * Retrieve a SampledMetricDefinition by its combined three-part Key string, if
	 * present.
	 * 
	 * This method looks in the collection of registered metric definitions for the
	 * specified Key. If it is not found, the output is set to null and the method
	 * returns false. If the Id key is found and resolves to a
	 * SampledMetricDefinition, it is stored in the value output parameter and the
	 * method returns true. If the Id key is found but is not a
	 * SampledMetricDefinition, an ArgumentException is thrown to signal a usage
	 * inconsistency in your code.
	 *
	 * @param key   The Key of the sampled metric definition to get (composed of the
	 *              metrics system, category name, and counter name combined as a
	 *              single string).
	 * @param value The output variable to receive the SampledMetricDefinition
	 *              object if found (null if not).
	 * @return False if no metric definition is registered with the given Key, true
	 *         if a SampledMetricDefinition is registered with the given Key, or
	 *         throws an exception if the registered definition is not a
	 *         SampledMetricDefinition.
	 */
	public static boolean tryGetValue(String key, OutObject<SampledMetricDefinition> value) {
		// protect ourself from a null before we do the trim (or we'll get an odd user
		// the error won't understand)
		if (TypeUtils.isBlank(key)) {
			throw new NullPointerException("key");
		}

		IMetricDefinition definition;

		// gateway to our inner dictionary try get value
		OutObject<IMetricDefinition> tempOutDefinition = new OutObject<IMetricDefinition>();
		boolean foundValue = definitions.tryGetValue(key.trim(), tempOutDefinition);
		definition = tempOutDefinition.argValue;
		value.argValue = foundValue
				? definition instanceof SampledMetricDefinition ? (SampledMetricDefinition) definition : null
				: null;
		if (foundValue && value.argValue == null) {
			// Uh-oh, we found one but it didn't resolve to an EventMetricDefinition!
			throw new IllegalArgumentException(String.format(Locale.ROOT,
					"The metric definition found by Key \"%1$s\" is not a sampled metric definition.", key));
		}
		return foundValue;
	}

	/**
	 * Retrieve a SampledMetricDefinition by its three key strings (metrics system,
	 * category name, and counter name), if present.
	 * 
	 * This method looks in the collection of registered metric definitions for the
	 * specified 3-part key. If it is not found, the output is set to null and the
	 * method returns false. If the Id key is found and resolves to a
	 * SampledMetricDefinition, it is stored in the value output parameter and the
	 * method returns true. If the Id key is found but is not a
	 * SampledMetricDefinition, an ArgumentException is thrown to signal a usage
	 * inconsistency in your code.
	 *
	 * @param metricsSystem The metrics capture system label of the definition to
	 *                      look up.
	 * @param categoryName  The name of the category with which the definition is
	 *                      associated.
	 * @param counterName   The name of the definition within the category.
	 * @param value         The output variable to receive the
	 *                      SampledMetricDefinition object if found (null if not).
	 * @return False if no metric definition is registered with the given Key, true
	 *         if a SampledMetricDefinition is registered with the given Key, or
	 *         throws an exception if the registered definition is not a
	 *         SampledMetricDefinition.
	 */
	public static boolean tryGetValue(String metricsSystem, String categoryName, String counterName,
			OutObject<SampledMetricDefinition> value) {
		IMetricDefinition definition;

		// gateway to our inner dictionary try get value
		OutObject<IMetricDefinition> tempOutDefinition = new OutObject<IMetricDefinition>();
		boolean foundValue = definitions.tryGetValue(metricsSystem, categoryName, counterName, tempOutDefinition);
		definition = tempOutDefinition.argValue;
		value.argValue = foundValue
				? definition instanceof SampledMetricDefinition ? (SampledMetricDefinition) definition : null
				: null;
		if (foundValue && value.argValue == null) {
			// Uh-oh, we found one but it didn't resolve to an EventMetricDefinition!
			throw new IllegalArgumentException(String.format(Locale.ROOT,
					"The metric definition found by metrics system (%1$s) category name (%2$s) counter name (%3$s) is not a sampled metric definition.",
					metricsSystem, categoryName, counterName));
		}
		return foundValue;
	}

	/**
	 * Find an existing sampled metric definition previously registered via
	 * SampledMetric and SampledMetricValue attributes on a specific Type, by its
	 * counter name.
	 * 
	 * This method overload can obtain a previously registered
	 * SampledMetricDefinition created through SampledMetric and SampledMetricValue
	 * attributes, by specifying the Type containing those attributes. If the
	 * specified Type does not have a SampledMetric attribute defined, or if the
	 * Type has a SampledMetric attribute but has not been registered (e.g. by a
	 * call to SampledMetricDefinition.Register(userObjectType)), then false is
	 * returned (with out value set to null). If a sampled metric defined by
	 * attributes on that Type has been successfully registered, then true is
	 * returned (with the registered SampledMetricDefinition stored in the out
	 * value). If the metric definition found by the 3-part Key used in the
	 * SampledMetric attribute (along with the specified counter name) is not a
	 * sampled metric (e.g. an event metric definition was registered with that
	 * Key), then an ArgumentException is thrown to signal your programming mistake.
	 * Inheritance and interfaces will <b>not</b> be searched, so the specified Type
	 * must directly define the sampled metric, but valid objects of a type
	 * assignable to the specified bound Type of this definition <b>can</b> be
	 * sampled from the specific sampled metric definition found.
	 *
	 * @param userObjectType A specific Type with attributes defining one or more
	 *                       sampled metrics.
	 * @param counterName    The counter name of the desired individual sampled
	 *                       metric definition defined by attributes on the
	 *                       specified Type.
	 * @param value          The output variable to receive the
	 *                       SampledMetricDefinition object if found (null if not).
	 * @return False if no SampledMetric attribute is found on the specified Type,
	 *         or if no metric definition is registered with the 3-part Key found in
	 *         that attribute (combined with the specified counter name), true if a
	 *         SampledMetricDefinition is registered with the given Key, or throws
	 *         an exception if the registered definition found is not a
	 *         SampledMetricDefinition.
	 */
	public static boolean tryGetValue(java.lang.Class userObjectType, String counterName,
			OutObject<SampledMetricDefinition> value) {
		value.argValue = null; // In case we don't find it.
		if (userObjectType == null) {
			throw new NullPointerException("userObjectType");
		}

		if (counterName == null) {
			throw new NullPointerException("counterName");
		}

		counterName = counterName.trim(); // Trim any whitespace around it.
		if (TypeUtils.isBlank(counterName)) {
			throw new NullPointerException(
					"The specified counter name is empty which is not allowed, so no metrics can be found for it.");
		}

		List<SampledMetricDefinition> definitionList;
		boolean foundValue = false; // Haven't found the actual definition yet.
		// We shouldn't need a lock because we aren't changing the dictionary, just
		// doing a single read check.
		synchronized (dictionaryLock) // But apparently Dictionaries are not internally threadsafe.
		{
			definitionList = definitionsMap.get(userObjectType);
			if (definitionList != null && !definitionList.isEmpty()) {
				SampledMetricDefinition[] definitionArray = definitionList.toArray(new SampledMetricDefinition[0]);
				for (SampledMetricDefinition definition : definitionArray) {
					if (definition.getCounterName().equals(counterName)) {
						value.argValue = definition; // Hey, we found it!
						foundValue = true; // Report success!
						break; // Stop looking through the array.
					}
				}
			}
		}

		if (!foundValue) // If we didn't find it on the known list for that type...
		{
			SampledMetricClass sampledMetricAnnotation = null;
			if (userObjectType.isAnnotationPresent(SampledMetricClass.class)) {
				sampledMetricAnnotation = (SampledMetricClass) userObjectType
						.getAnnotation(SampledMetricClass.class);
			}

			if (sampledMetricAnnotation != null) {
				String metricsSystem = sampledMetricAnnotation.namespace();
				String categoryName = sampledMetricAnnotation.categoryName();

				IMetricDefinition definition;

				// gateway to our inner dictionary try get value
				OutObject<IMetricDefinition> tempOutDefinition = new OutObject<IMetricDefinition>();
				foundValue = definitions.tryGetValue(metricsSystem, categoryName, counterName, tempOutDefinition);
				definition = tempOutDefinition.argValue;
				value.argValue = foundValue
						? definition instanceof SampledMetricDefinition ? (SampledMetricDefinition) definition : null
						: null;
				if (foundValue && value.argValue == null) {
					// Uh-oh, we found one but it didn't resolve to a SampledMetricDefinition!
					throw new IllegalArgumentException(String.format(Locale.ROOT,
							"The metric definition registered for metrics system (%1$s) and category name (%2$s) from SampledMetric attribute on %4$s, and specified counter name (%3$s) is not a sampled metric definition.",
							metricsSystem, categoryName, counterName, userObjectType.getSimpleName()));
				}
			}
			// Otherwise we already pre-set value to null, and foundvalue is still false, so
			// we'll report the failure.
		}
		// Otherwise we found a valid definition in our Type-to-definition map, so we've
		// output that and report success.

		return foundValue;
	}
	

	/**
	 * Determines if the provided object is identical to this object.
	 *
	 * @param other the other
	 * @return True if the other object is also a MetricDefinition and represents
	 *         the same data.
	 */
	@Override
	public boolean equals(Object other) {
		// Careful, it could be null; check it without recursion
		if (other == null) {
			return false; // Since we're a live object we can't be equal to a null instance.
		}

		if (!(other instanceof SampledMetricDefinition))
			return false;
		
		SampledMetricDefinition otherDef = (SampledMetricDefinition)other;
		
		// they are the same if their GUID's match
		return (getId().equals(otherDef.getId()));
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.metrics.IMetricDefinition#getName()
	 */
	@Override
	public String getName() {
		return this.packet.getName();
	}
	
	/**
	 * Creates builder to build {@link builder}.
	 *
	 * @param metricsSystem the metrics system
	 * @param categoryName the category name
	 * @param counterName the counter name
	 * @return created builder
	 */
	public static Builder builder(String metricsSystem, String categoryName, String counterName) {
		return new Builder(metricsSystem, categoryName, counterName);
	}
	/**
	 * Builder to build {@link builder}.
	 */
	public static final class Builder {
		
		/** The metrics system. */
		private String metricsSystem;
		
		/** The category name. */
		private String categoryName;
		
		/** The counter name. */
		private String counterName;
		
		/** The sampling type. */
		private SamplingType samplingType;
		
		/** The unit caption. */
		private String unitCaption;
		
		/** The metric caption. */
		private String metricCaption;
		
		/** The description. */
		private String description;

		/**
		 * Instantiates a new builder.
		 *
		 * @param metricsSystem the metrics system
		 * @param categoryName the category name
		 * @param counterName the counter name
		 */
		private Builder(String metricsSystem, String categoryName, String counterName) {
			this.metricsSystem = metricsSystem;
			this.categoryName = categoryName;
			this.counterName = counterName;
		}

		/**
		 * Sampling type.
		 *
		 * @param samplingType the sampling type
		 * @return the builder
		 */
		public Builder samplingType(SamplingType samplingType) {
			this.samplingType = samplingType;
			return this;
		}

		/**
		 * Unit caption.
		 *
		 * @param unitCaption the unit caption
		 * @return the builder
		 */
		public Builder unitCaption(String unitCaption) {
			this.unitCaption = unitCaption;
			return this;
		}

		/**
		 * Metric caption.
		 *
		 * @param metricCaption the metric caption
		 * @return the builder
		 */
		public Builder metricCaption(String metricCaption) {
			this.metricCaption = metricCaption;
			return this;
		}

		/**
		 * Description.
		 *
		 * @param description the description
		 * @return the builder
		 */
		public Builder description(String description) {
			this.description = description;
			return this;
		}

		/**
		 * Builds the.
		 *
		 * @return the sampled metric definition
		 */
		public SampledMetricDefinition build() {
			return register(metricsSystem, categoryName, counterName, samplingType, unitCaption, metricCaption,
					description);
		}
	}
}