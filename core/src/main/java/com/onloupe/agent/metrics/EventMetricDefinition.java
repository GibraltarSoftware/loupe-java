package com.onloupe.agent.metrics;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Duration;
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

import com.onloupe.agent.metrics.annotation.EventMetricClass;
import com.onloupe.agent.metrics.annotation.EventMetricValue;
import com.onloupe.core.NameValuePair;
import com.onloupe.core.logging.Log;
import com.onloupe.core.metrics.IMetricDefinition;
import com.onloupe.core.metrics.Metric;
import com.onloupe.core.metrics.MetricDefinition;
import com.onloupe.core.metrics.MetricDefinitionCollection;
import com.onloupe.core.serialization.monitor.EventMetricDefinitionPacket;
import com.onloupe.core.util.OutObject;
import com.onloupe.core.util.RefObject;
import com.onloupe.core.util.SystemUtils;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.SampleType;
import com.onloupe.model.metric.MemberType;


/**
 * The definition of an event metric, which must be registered before any
 * specific event metric instance can be created or sampled.
 * 
 * 
 * <p>
 * Unlike sampled metrics which represent continuous values by sampling at
 * periodic intervals, event metrics have meaning only at discrete moments in
 * time when some "event" happens and records a sample to describe it.
 * </p>
 * <p>
 * Event metrics can define multiple values to be collected with each sample and
 * can include both numeric data types (recorded as their native type) and
 * strings (all non-numeric types are converted to strings). Numeric data
 * columns can then be processed later to be graphed like Sampled Metrics. Both
 * numeric and string data can be analyzed in a variety of ways to produce
 * charts. This makes event metrics a powerful instrument for analyzing your
 * application's behavior.
 * </p>
 * <p>
 * For more information Event Metrics, see
 * <a href="Metrics_EventMetricDesign.html">Developer's Reference - Metrics -
 * Designing Event Metrics</a>.
 * </p>
 * <p>
 * <strong>Defining Event Metrics</strong>
 * </p>
 * <p>
 * Event metrics can be defined either programmatically or declaratively with
 * attributes.
 * </p>
 * <p>
 * To define an event metric with attributes, apply the
 * EventMetric attribute to the source
 * code for any class, struct, or interface, and apply the
 * EventMetricValue attribute to
 * desired members to define the value columns. This approach provides a simple
 * and powerful way to design and collect event metrics for your application.
 * See the EventMetric Class Overview for an
 * example.
 * </p>
 * <p>
 * To define an event metric programmatically requires more coding, but allows
 * you to optimize the performance of recording event metrics and works in
 * environments where it isn't feasible to decorate a class with attributes. See
 * the EventMetric Class Overview for an example.
 * </p>
 * 
 */
public final class EventMetricDefinition implements IMetricDefinition {
	
	/** The Constant definitions. */
	private static final MetricDefinitionCollection definitions = Log.getMetricDefinitions();;

	/** The packet. */
	private EventMetricDefinitionPacket packet;
	
	/** The lock. */
	private final Object lock = new Object();

	/** The metrics. */
	private EventMetricCollection metrics;
	
	/** The metric values. */
	private EventMetricValueDefinitionCollection metricValues;
	
	/** The bound. */
	private boolean bound;
	
	/** The bound type. */
	private java.lang.Class boundType;
	
	/** The name bound. */
	private boolean nameBound;
	
	/** The name member name. */
	private String nameMemberName;
	
	/** The name member type. */
	private MemberType nameMemberType;

	/** The Constant inheritanceMap. */
	private static final Map<java.lang.Class, Class[]> inheritanceMap = new HashMap<java.lang.Class, Class[]>(); // Array
																													// of
																													// all
																													// inherited
																													// types
																													// (that
																													// have
																													// attributes),
																													// by
																													/** The Constant definitionMap. */
																													// type.
	private static final Map<java.lang.Class, EventMetricDefinition> definitionMap = new HashMap<java.lang.Class, EventMetricDefinition>(); // LOCKED
																																			// definition
																																			// by
																																			// specific
																																			// bound
																																			/** The Constant dictionaryLock. */
																																			// type.
	private static final Object dictionaryLock = new Object(); // Lock for the DefinitionMap dictionary.

	/**
	 * Create a new event metric definition for the active log.
	 * 
	 * At any one time there should only be one metric definition with a given
	 * combination of metric type, category, and counter name. These values together
	 * are used to correlate metrics between sessions. The metric definition will
	 * <b>not</b> be automatically added to the provided collection.
	 * 
	 * @param metricTypeName The unique metric type
	 * @param categoryName   The name of the category with which this definition is
	 *                       associated.
	 * @param counterName    The name of the definition within the category.
	 */
	private EventMetricDefinition(String metricTypeName, String categoryName, String counterName) {
		this.packet = new EventMetricDefinitionPacket(metricTypeName, categoryName, counterName);
		this.metrics = new EventMetricCollection(this);
		this.metricValues = new EventMetricValueDefinitionCollection(this);

		// and we need to set that to our packet, all part of our bogus reach-around to
		// make persistence work
		packet.setMetricValues(this.metricValues);
	}
	
	/**
	 * Create a new event metric object from the provided raw data packet.
	 *
	 * @param packet      The packet to create a definition from.
	 */
	private EventMetricDefinition(EventMetricDefinitionPacket packet) {
		// make sure our packet isn't null
		if (packet == null) {
			throw new NullPointerException("packet");
		}

		this.packet = packet;
		this.metrics = new EventMetricCollection(this);
		this.metricValues = new EventMetricValueDefinitionCollection(this);

		// and we need to set that to our packet, all part of our bogus reach-around to
		// make persistence work
		packet.setMetricValues(this.metricValues);
	}

	/**
	 * Create a new value column definition with the supplied name and type. The
	 * name must be unique within this definition.
	 * 
	 * Internally, only simple types are supported. Any non-numeric,
	 * non-DateTimeOffset type will be converted to a string using the default
	 * ToString capability when it is recorded.
	 * 
	 * @param name            The unique name for this value column definition.
	 * @param type            The simple type of this value (e.g. typeof(int) or
	 *                        typeof(string)).
	 * @param summaryFunction The default way that individual samples of this value
	 *                        column can be aggregated to create a graphable
	 *                        summary. (Use SummaryFunction.Count for non-numeric
	 *                        types.)
	 * @param unitCaption     A displayable caption for the units this value
	 *                        represents, or null for unit-less values.
	 * @param caption         The end-user display caption for this value column.
	 * @param description     The end-user description for this value column.
	 * @return The newly created value column definition.  See the
	 *         EventMetric Class Overview for an
	 *         example. 
	 */
	public EventMetricValueDefinition addValue(String name, java.lang.Class type, SummaryFunction summaryFunction,
			String unitCaption, String caption, String description) {
		// Error checking will be done by Values.Add(...), which is also publicly
		// available.

		// create a new value definition
		return getValueCollection().add(name, type, summaryFunction, unitCaption, caption, description);
	}

	/**
	 * Find or create multiple event metrics definitions (defined via EventMetric
	 * attributes) for the provided object or Type.
	 * 
	 * The provided Type or the GetType() of the provided object instance will be
	 * scanned for EventMetric attributes on itself and any of its interfaces to
	 * identify a list of event metrics defined for instances of that type, creating
	 * them as necessary by scanning its members for EventMetricValue attributes.
	 * Inheritance will be followed into base types, along with all interfaces
	 * inherited to the top level. This method will not throw exceptions, so a null
	 * argument will return an empty array, as will an argument which does not
	 * define any valid event metrics. Also see RegisterType(Type) to find or create
	 * a single event metric definition for a specific Type.
	 *
	 * @param metricData A Type or an instance defining event metrics by attributes
	 *                   on itself and/or its interfaces.
	 * @return An array of zero or more event metric definitions found for the
	 *         provided object or Type.  See the
	 *         EventMetric Class Overview for an
	 *         example. 
	 */
	public static EventMetricDefinition[] registerAll(Object metricData) {
		List<EventMetricDefinition> definitions = new ArrayList<EventMetricDefinition>();

		if (metricData != null) {
			// Either they gave us a Type, or we need to get the type of the object instance
			// they gave us.
			java.lang.Class userObjectType = ((metricData instanceof java.lang.Class ? (java.lang.Class) metricData
					: null) != null) ? (metricData instanceof java.lang.Class ? (java.lang.Class) metricData : null)
							: metricData.getClass();

			EventMetricDefinition metricDefinition;
			java.lang.Class[] inheritanceArray;
			boolean foundIt;
			synchronized (inheritanceMap) // Apparently Dictionaries aren't internally threadsafe.
			{
				inheritanceArray = inheritanceMap.get(userObjectType);
			}
			if (inheritanceArray != null) {
				// We've already scanned this type, so use the cached array of types.
				for (java.lang.Class inheritedType : inheritanceArray) {
					try {
						metricDefinition = registerType(inheritedType);
					} catch (Exception e) {
						if (SystemUtils.isInDebugMode()) {
							e.printStackTrace();
						}
						metricDefinition = null;
					}
					if (metricDefinition != null) {
						definitions.add(metricDefinition); // Add it to the list if found.
					}
				}
			} else {
				// New top-level type, we have to scan its inheritance.
				List<java.lang.Class> inheritanceList = new ArrayList<java.lang.Class>(); // List of all the
																								// inherited types we
																								// find with attributes
																								// on them.

				// First, see if the main type they gave us defines an event metric.
				if (userObjectType.isAnnotationPresent(EventMetricClass.class)) {
					try {
						inheritanceList.add(userObjectType); // Add the top level Type to our list of types.
						metricDefinition = registerType(userObjectType);
					} catch (Exception e) {
						if (SystemUtils.isInDebugMode()) {
							e.printStackTrace();
						}
						metricDefinition = null;
					}
					if (metricDefinition != null) {
						definitions.add(metricDefinition); // Add it to the list if found.
					}
				}

				// Now check all of its interfaces.
				Set<Class> interfaces = new HashSet<Class>();
				for (java.lang.Class interfc : userObjectType.getInterfaces()) {
					interfaces.add(interfc);
					TypeUtils.getSuperInterfaces(interfc, interfaces);
				}
				
				for (java.lang.Class interfc : interfaces) {
					if (interfc.isAnnotationPresent(com.onloupe.agent.metrics.annotation.EventMetricClass.class)) {
						// We found an interface with the right Attribute, get its definition.
						try {
							inheritanceList.add(interfc); // Add the interface to our list of types.
							metricDefinition = registerType(interfc);
						} catch (Exception e) {
							if (SystemUtils.isInDebugMode()) {
								e.printStackTrace();
							}
							metricDefinition = null;
						}
						if (metricDefinition != null) {
							definitions.add(metricDefinition); // Add it to the list if found.
						}
					}
				}

				// And finally, drill down it's inheritance...
				java.lang.Class baseObjectType = (userObjectType.isInterface()) ? null : userObjectType.getSuperclass();

				// ...unless it's an interface.
				while (baseObjectType != null && baseObjectType != Object.class
						&& !baseObjectType.isInterface()) {
					// See if an ancestor Type defines an event metric.
					if (baseObjectType.isAnnotationPresent(com.onloupe.agent.metrics.annotation.EventMetricClass.class)) {
						try {
							inheritanceList.add(baseObjectType); // Add the inherited base to our list of types.
							metricDefinition = registerType(baseObjectType);
						} catch (Exception e) {
							if (SystemUtils.isInDebugMode()) {
								e.printStackTrace();
							}
							metricDefinition = null;
						}
						if (metricDefinition != null) {
							definitions.add(metricDefinition); // Add it to the list if found.
						}
					}

					// No need to check its interfaces, we already got all of them from the top
					// level.

					baseObjectType = baseObjectType.getSuperclass(); // Get the next deeper Type.
				}

				// Now, remember the list of attributed types we found in this walk.
				synchronized (inheritanceMap) // Apparently Dictionaries aren't internally threadsafe.
				{
					inheritanceMap.put(userObjectType, inheritanceList.toArray(new java.lang.Class[0]));
				}
			}
		}

		// If they gave us a null, we'll just return an empty array.
		return definitions.toArray(new EventMetricDefinition[0]);
	}

	/**
	 * Determine the readable Type for a field, property, or method.
	 * 
	 * This method assumes that only MemberType of Field, Property, or Method will
	 * be given. A method with void return type will return typeof(void), and
	 * properties with no get accessor will return null. This does not currently
	 * check method signature info for the zero-argument requirement.
	 *
	 * @param memberInfo the member info
	 * @return The Type of value which can be read from the field, property, or
	 *         method.
	 */
	private static java.lang.Class getTypeOfMember(AccessibleObject memberInfo) {
		java.lang.Class readType = null;
		if (memberInfo instanceof Method) {
			// For methods, it's the return value type.
			readType = ((java.lang.reflect.Method) memberInfo).getReturnType();
		} else if (memberInfo instanceof Field) {
			// For fields, it's the field type... They can always be read (through
			// reflection, that is).
			readType = ((java.lang.reflect.Field) memberInfo).getType();
		}

		return readType;
	}

	/**
	 * Find or create an event metric definition from EventMetric and
	 * EventMetricValue attributes on a specific Type.
	 * 
	 * The provided type must have an EventMetric attribute and can have one or more
	 * fields, properties or zero-argument methods with EventMetricValue attributes
	 * defined. This method creates a metric definition but does not create a
	 * specific metric instance, so it does not require a live object. If the event
	 * metric definition already exists, it is just returned and no exception is
	 * thrown. If the provided type is not suitable to create an event metric from
	 * because it is missing the appropriate attribute or the attribute has been
	 * miss-defined, an ArgumentException will be thrown. Inheritance and interfaces
	 * will <b>not</b> be searched, so the provided Type must directly define an
	 * event metric, but valid objects of a type assignable to the specified bound
	 * Type of this definition <b>can</b> be sampled from this specific event metric
	 * definition. Also see AddOrGetDefinitions() to find and return an array of
	 * definitions.
	 *
	 * @param metricDataObjectType A specific Type with attributes defining an event
	 *                             metric.
	 * @return The single event metric definition determined by attributes on the
	 *         given Type.
	 */
	//THIS SCANS CLASS FIELDS AND METHODS FOR ATTRIBUTE/ANNOTATIONS
	public static EventMetricDefinition registerType(java.lang.Class metricDataObjectType) {
		// See if there is already a definition known on this Type.
		// If there is, we just want to return it and not do any more.
		EventMetricDefinition newMetricDefinition = definitionMap.get(metricDataObjectType);

		// ToDo: Need to overhaul error reporting, should log before throwing exceptions
		// in case they are caught internally.
		// And throwing exceptions may be pointless if they can never get here without
		// us catching exceptions and swallowing them.
		synchronized (dictionaryLock) {
			if (newMetricDefinition == null) {
				definitionMap.put(metricDataObjectType, null); // Pre-set to null in case of exception, so we don't scan
																// it again.

				// Check if it defines it at this specific level, no inheritance search, no
				// interfaces search.
				if (!metricDataObjectType.isAnnotationPresent(EventMetricClass.class)) {
					// Sorry, Attribute not found.
					throw new IllegalArgumentException(
							"The specified Type does not have an EventMetric attribute, so it can't be used to define an event metric.");
				}

				// OK, now waltz off and get the attribute we want.
				EventMetricClass eventMetricAnnotation = (EventMetricClass)metricDataObjectType.getAnnotation(EventMetricClass.class);
				// Try to cast it to the specific kind of attribute we need

				// Verify that the event metric attribute that we got is valid
				if (eventMetricAnnotation == null) {
					throw new IllegalArgumentException(
							"The specified Type does not have a usable EventMetric attribute, so it can't be used to define an event metric.");
				}

				// make sure the user didn't do any extraordinary funny business
				String metricsSystem = eventMetricAnnotation.namespace();
				if (TypeUtils.isBlank(metricsSystem)) {
					throw new IllegalArgumentException(
							"The specified Type's EventMetric has an empty metric namespace which is not allowed, so no metric can be defined.");
				}

				String metricCategoryName = eventMetricAnnotation.categoryName();
				if (TypeUtils.isBlank(metricCategoryName)) {
					throw new IllegalArgumentException(
							"The specified Type's EventMetric has an empty metric category name which is not allowed, so no metric can be defined.");
				}

				String metricCounterName = eventMetricAnnotation.counterName();
				if (TypeUtils.isBlank(metricCounterName)) {
					throw new IllegalArgumentException(
							"The specified Type's EventMetric has an empty metric counter name which is not allowed, so no metric can be defined.");
				}

				// See if there is already a definition with the specified keys.
				// If there is, we just want to return it and not do any more.

				// We use a lock because we need to have the check and the add (which we do at
				// the end) happen as a single event.
				// We'll just hold the collection lock the whole time since we don't have to
				// wait on arbitrary client code.
				synchronized (Log.getMetricDefinitions().getLock()) {
					IMetricDefinition existingMetricDefinition;
					OutObject<IMetricDefinition> tempOutExistingMetricDefinition = new OutObject<IMetricDefinition>();
					if (Log.getMetricDefinitions().tryGetValue(metricsSystem, metricCategoryName,
							metricCounterName, tempOutExistingMetricDefinition)) {
						existingMetricDefinition = tempOutExistingMetricDefinition.argValue;
						// eh, we already had a definition. We want to go no further.
						newMetricDefinition = existingMetricDefinition instanceof EventMetricDefinition
								? (EventMetricDefinition) existingMetricDefinition
								: null;
						if (newMetricDefinition == null) {
							throw new IllegalArgumentException(
									"The specified Type's EventMetric attribute's 3-part Key is already used for a metric definition which is not an event metric.");
						}
					} else {
						existingMetricDefinition = tempOutExistingMetricDefinition.argValue;

						// OK, now we know we'll be good.
						newMetricDefinition = new EventMetricDefinition(metricsSystem, metricCategoryName,
								metricCounterName);
						newMetricDefinition.setBoundType(metricDataObjectType);
						newMetricDefinition.setCaption(eventMetricAnnotation.caption());
						newMetricDefinition.setDescription(eventMetricAnnotation.description());

						// now that we have our new metric definition, do our level best to add the rest
						// of the information to it

						List<AccessibleObject> members = new ArrayList<AccessibleObject>();
						members.addAll(Arrays.asList(metricDataObjectType.getFields()));
						members.addAll(Arrays.asList(metricDataObjectType.getMethods()));

						// reflect all of the field/property/methods in the type so we can inspect them
						// for attributes

						for (AccessibleObject curMember : members) {
							MemberType curMemberType;
							if (curMember instanceof Field) {
								curMemberType = MemberType.FIELD;
							} else {
								curMemberType = MemberType.METHOD;
							}
							// and what can we get from our little friend?
							if (curMember.isAnnotationPresent(com.onloupe.agent.metrics.annotation.EventMetricInstanceName.class)) {
								// have we already bound our instance name?
								if (newMetricDefinition.getNameBound()) {
									// yes, so report a duplicate name warning
								} else {
									// nope, so lets go for it, set up our binding information
									if (MemberType.FIELD.equals(curMemberType)) {
										Field field = (Field)curMember;
										newMetricDefinition.setNameBound(true);
										newMetricDefinition.setNameMemberType(MemberType.FIELD);
										newMetricDefinition.setNameMemberName(field.getName());
									} else if (MemberType.METHOD.equals(curMemberType)) {
										Method method = (Method)curMember;
										newMetricDefinition.setNameBound(true);
										newMetricDefinition.setNameMemberType(MemberType.METHOD);
										newMetricDefinition.setNameMemberName(method.getName());
									}
								}
							}

							// What about mappings to values?
							if (curMember.isAnnotationPresent(com.onloupe.agent.metrics.annotation.EventMetricValue.class) ||
									curMember.isAnnotationPresent(com.onloupe.agent.metrics.annotation.EventMetricValues.class)) {
								// What type of value does this member give? It'll be the same for every value
								// attribute on it!
								java.lang.Class curType = getTypeOfMember(curMember);

								// they have at least one event metric value attribute, go get all of them
								// we get back an array of objects just in case there are any non-CLS compliant
								// attributes defined, which there never are.
								EventMetricValue[] curMemberValueAttributes = curMember.getAnnotationsByType(EventMetricValue.class);
								
								for (com.onloupe.agent.metrics.annotation.EventMetricValue curValueAttribute : curMemberValueAttributes) {
									String memberName = null;
									if (MemberType.FIELD.equals(curMemberType)) {
										memberName = ((Field)curMember).getName();
									} else if (MemberType.METHOD.equals(curMemberType)) {
										memberName = ((Method)curMember).getName();
									}
									
									// cast it and test
									if (curValueAttribute != null) {
										if (newMetricDefinition.getValueCollection()
												.containsKey(curValueAttribute.name())) // Warn about duplicates.
										{
										} else if (curType == null) // Warn about an unreadable property.
										{
										} else if (curType == void.class) // Warn about a void method.
										{
										} else {
											// We finally have validated everything and we're ready to set up the new
											// value.
											EventMetricValueDefinition newValue = newMetricDefinition
													.getValueCollection().add(curValueAttribute.name(), curType);
											// set up our binding information
											newValue.setBound(true);
											newValue.setMemberType(curMemberType);
											newValue.setMemberName(memberName);

											// now that we've added it, what else can we set?
											newValue.setUnitCaption(curValueAttribute.unitCaption());
											newValue.setSummaryFunction(curValueAttribute.summaryFunction().getValue());
											newValue.setCaption(curValueAttribute.caption());
											newValue.setDescription(curValueAttribute.description());

											// and finally, is this our default value?
											if ((newMetricDefinition.getDefaultValue() == null)
													&& (curValueAttribute.defaultValue())) {
												newMetricDefinition.setDefaultValue(newValue);
											}
										}
									}
								}
							} // End of if value attribute defined
						} // End of foreach over members

						// Indicate that the specified metric definition is a bound definition, and
						// register ourselves.
						newMetricDefinition.setIsBound(true);
						//TODO track this down. See: metricdefinition
						newMetricDefinition.setReadOnly(); // Mark the internal definition as
																					// completed.
						Log.getMetricDefinitions().add(newMetricDefinition);
					} // End of if Log.MetricDefinitions.TryGetValue ELSE
				} // End of LOCK on Log.MetricDefinitions

				definitionMap.put(metricDataObjectType, newMetricDefinition); // Remember it for next time.
			} // End of if DefinitionMap.TryGetValue == false

			// Otherwise, we read out the definition we found on this Type before, so we'll
			// just return it.
		} // End of LOCK on DefinitionMap

		return newMetricDefinition;
	}

	/**
	 * Register the referenced EventMetricDefinition template, or update the
	 * reference to the official definition if a compatible event metric definition
	 * already exists for the same 3-part Key.
	 * 
	 * This is the final step for creating a new event metric definition
	 * programmatically, after constructing a new EventMetricDefinition(...) and
	 * calling AddValue(...) as desired to define value columns. If a metric
	 * definition is already registered with the same Key, it will be checked for
	 * compatibility. An incompatible existing definition (e.g. a sampled metric, or
	 * missing value columns from the provided template) will result in an
	 * ArgumentException to signal your programming mistake; each different metric
	 * definition in an application session must have a unique 3-part Key. If a
	 * compatible existing event metric definition is found, the reference to the
	 * EventMetricDefinition will be updated to the registered definition. If no
	 * metric definition exists with the same 3-part key as the template, then the
	 * new definition will be officially registered and may be used as a valid
	 * definition. This approach ensures thread-safe creation of singular event
	 * metric definitions without the need for locking by your code.
	 *
	 * @param newDefinition A reference to an event metric definition template to be
	 *                      registered, and to receive the official registered event
	 *                      metric definition.
	 */
	public static void register(RefObject<EventMetricDefinition> newDefinition) {
		// ToDo: Consider copy-in/copy-out of newDefinition to protect against
		// pathological clients bypassing our sanity checks.
		EventMetricDefinition theDefinition = newDefinition.argValue;
		if (theDefinition == null) {
			throw new NullPointerException(
					"A null definition can not be registered nor used to look up a registered event metric definition.");
		}

		EventMetricDefinition registeredDefinition = theDefinition.register();
		// We don't overwrite newDefinition immediately, in case we get back a null, so
		// we can still inspect the
		// template in case of errors. Also, they can inspect it in a debugger after we
		// throw this exception...
		if (registeredDefinition == null || !registeredDefinition.isReadOnly()) {
			// Hmmm, this really should not happen if we've coded registration correctly.
			// Any errors should already throw exceptions above.
			throw new IllegalArgumentException(String.format(Locale.ROOT,
					"Unknown error registering new event metric definition: metrics system (%1$s), category name (%2$s), counter name (%3$s)",
					theDefinition.getMetricsSystem(), theDefinition.getCategoryName(), theDefinition.getCounterName()));
		}

		newDefinition.argValue = registeredDefinition; // Finally, update the reference to whatever the official
														// registration is.
	}

	/**
	 * Register the referenced EventMetricDefinition template, or update the
	 * reference to the official definition if a compatible event metric definition
	 * already exists for the same 3-part Key.
	 * 
	 * <p>
	 * This is the final step for creating a new event metric definition
	 * programmatically, after constructing a new EventMetricDefinition(...) and
	 * calling AddValue(...) as desired to define value columns. If a metric
	 * definition is already registered with the same Key, it will be checked for
	 * compatibility. An incompatible existing definition (e.g. a sampled metric, or
	 * missing value columns from the provided template) will result in an
	 * ArgumentException to signal your programming mistake; each different metric
	 * definition in an application session must have a unique 3-part Key. If a
	 * compatible existing event metric definition is found, the reference to the
	 * EventMetricDefinition will be updated to the registered definition. If no
	 * metric definition exists with the same 3-part key as the template, then the
	 * new definition will be officially registered and may be used as a valid
	 * definition. This approach ensures thread-safe creation of singular event
	 * metric definitions without the need for locking by your code.
	 * </p>
	 * <p>
	 * This overload allows a value column of the definition template to be
	 * designated as the default one to graph for this event metric. The specified
	 * name must match a value column name in the definition template or a
	 * KeyNotFoundException will be thrown (and the template will not be
	 * registered). The defaultValue parameter will overwrite any previous setting
	 * of the DefaultValue property of the event metric definition template. If the
	 * completed template is not ultimately used because a metric definition already
	 * exists with the same 3-part Key, then the defaultValue parameter will have no
	 * effect; a metric definition which is already registered can not be altered,
	 * to ensure consistency within the session log.
	 * </p>
	 * <p>
	 * Also see the overload directly taking an EventMetricValueDefinition as the
	 * defaultValue for an approach which may be less prone to mistakes.
	 * </p>
	 *
	 * @param newDefinition A reference to an event metric definition template to be
	 *                      registered, and to receive the official registered event
	 *                      metric definition.
	 * @param defaultValue  The name of a value column to designate as the default
	 *                      one to graph for this metric.
	 * @throws Exception the exception
	 */
	public static void register(RefObject<EventMetricDefinition> newDefinition, String defaultValue) throws Exception {
		EventMetricDefinition theDefinition = newDefinition.argValue;
		if (theDefinition == null) {
			throw new NullPointerException(
					"A null definition can not be registered nor used to look up a registered event metric definition.");
		}

		if (TypeUtils.isBlank(defaultValue)) {
			throw new NullPointerException(
					"The specified defaultValue name must be a legal value column name and thus may not be null or empty.");
		}

		// The definition should be held only by one thread until we're actually
		// registered, anyway, but just to be safe...
		// Lock the new definition so there can't be any other attempted changes while
		// we do this.
		synchronized (theDefinition.getLock()) {
			EventMetricValueDefinition defaultValueDefinition = theDefinition.getValueCollection().get(defaultValue);
			
			if (defaultValueDefinition == null) {
				throw new Exception(String.format(Locale.ROOT,
						"The specified defaultValue column name (%1$s) was not found in the definition.", defaultValue));
			}

			theDefinition.setDefaultValue(defaultValueDefinition); // Set the DefaultValue to the one identified.

			// And finally do the actual registration with our other overload.
			RefObject<EventMetricDefinition> tempRefTheDefinition = new RefObject<EventMetricDefinition>(theDefinition);
			register(tempRefTheDefinition);
			theDefinition = tempRefTheDefinition.argValue;
		}
		newDefinition.argValue = theDefinition;
	}

	/**
	 * Register the referenced EventMetricDefinition template, or update the
	 * reference to the official definition if a compatible event metric definition
	 * already exists for the same 3-part Key.
	 * 
	 * <p>
	 * This is the final step for creating a new event metric definition
	 * programmatically, after constructing a new EventMetricDefinition(...) and
	 * calling AddValue(...) as desired to define value columns. If a metric
	 * definition is already registered with the same Key, it will be checked for
	 * compatibility. An incompatible existing definition (e.g. a sampled metric, or
	 * missing value columns from the provided template) will result in an
	 * ArgumentException to signal your programming mistake; each different metric
	 * definition in an application session must have a unique 3-part Key. If a
	 * compatible existing event metric definition is found, the reference to the
	 * EventMetricDefinition will be updated to the registered definition. If no
	 * metric definition exists with the same 3-part key as the template, then the
	 * new definition will be officially registered and may be used as a valid
	 * definition. This approach ensures thread-safe creation of singular event
	 * metric definitions without the need for locking by your code.
	 * </p>
	 * <p>
	 * This overload allows an EventMetricValueDefinition to be designated as the
	 * default value column to graph for this event metric. When adding value
	 * columns to the definition template, the EventMetricValueDefinition returned
	 * by one of them can be saved to pass in this overload, for convenience. The
	 * defaultValue parameter will overwrite any previous setting of the
	 * DefaultValue property of the event metric definition template. If the
	 * completed template is not ultimately used because a metric definition already
	 * exists with the same 3-part Key, then the defaultValue parameter will have no
	 * effect; a metric definition which is already registered can not be altered,
	 * to ensure consistency within the session log.
	 * </p>
	 *
	 * @param newDefinition A reference to an event metric definition template to be
	 *                      registered, and to receive the official registered event
	 *                      metric definition.
	 * @param defaultValue  The definition of a value column in this event metric
	 *                      definition to designate as the default one to graph for
	 *                      this metric.
	 * @throws Exception the exception
	 */
	public static void register(RefObject<EventMetricDefinition> newDefinition,
			EventMetricValueDefinition defaultValue) throws Exception {
		EventMetricDefinition theDefinition = newDefinition.argValue;
		if (theDefinition == null) {
			throw new NullPointerException(
					"A null definition can not be registered nor used to look up a registered event metric definition.");
		}

		synchronized (theDefinition.getLock()) {
			if (defaultValue != null && (defaultValue.getDefinition() != theDefinition
					|| !theDefinition.getValueCollection().contains(defaultValue))) {
				throw new Exception(
						"The event metric value column definition specified is not associated with the specified event metric definition.");
			}

			theDefinition.setDefaultValue(defaultValue); // Set the DefaultValue to the one identified.

			// And finally do the actual registration with our other overload.
			RefObject<EventMetricDefinition> tempRefTheDefinition = new RefObject<EventMetricDefinition>(theDefinition);
			register(tempRefTheDefinition);
			theDefinition = tempRefTheDefinition.argValue;
		}
		newDefinition.argValue = theDefinition;
	}

	/**
	 * Register this instance as a completed definition and return the valid usable
	 * definition for this event metric.
	 * 
	 * This call is necessary to complete a new event metric definition (after calls
	 * to AddValue(...)) before it can be used, and it signifies that all desired
	 * value columns have been added to the definition. Only the first registration
	 * of a metric definition with a given Key (metrics system, category name, and
	 * counter name) will be effective and return the same definition object;
	 * subsequent calls (perhaps by another thread) will instead return the existing
	 * definition already registered. If a definition already registered with that
	 * Key can not be an event metric (e.g. a sampled metric is defined with that
	 * Key) or if this instance defined value columns not present as compatible
	 * value columns in the existing registered definition with that Key, then an
	 * ArgumentException will be thrown to signal your programming mistake.
	 * 
	 * @return The actual usable definition with the same metrics system, category
	 *         name, and counter name as this instance.  See the
	 *         EventMetric Class Overview for an
	 *         example. 
	 */
	public EventMetricDefinition register() {
		EventMetricDefinition officialDefinition;
		// We should be held only by one thread until we're actually registered, anyway,
		// but just to be safe...
		// Lock our own definition so there can't be any other attempted changes while
		// we do this.
		synchronized (getLock()) {

			// We need to lock the collection while we check for an existing definition and
			// maybe add this one to it.
			synchronized (Log.getMetricDefinitions().getLock()) {
				IMetricDefinition rawDefinition;
				OutObject<IMetricDefinition> tempOutRawDefinition = new OutObject<IMetricDefinition>();
				if (!Log.getMetricDefinitions().tryGetValue(getMetricsSystem(), getCategoryName(), getCounterName(),
						tempOutRawDefinition)) {
					rawDefinition = tempOutRawDefinition.argValue;
					// There isn't already one by that Key. Great! Register ourselves.
					this.setReadOnly(); // Mark the internal definition as completed.
					officialDefinition = this;
					Log.getMetricDefinitions().add(this);
				} else {
					rawDefinition = tempOutRawDefinition.argValue;
					// Oooh, we found one already registered. We'll want to do some checking on
					// this, but outside the lock.
					officialDefinition = rawDefinition instanceof EventMetricDefinition
							? (EventMetricDefinition) rawDefinition
							: null;
				}
			} // End of collection lock

			if (officialDefinition == null) {
				throw new IllegalArgumentException(String.format(Locale.ROOT,
						"There is already a metric definition for the same metrics system (%1$s), category name (%2$s), and counter name (%3$s), but it is not an event metric.",
						getMetricsSystem(), getCategoryName(), getCounterName()));
			} else if (this != officialDefinition) {
				// There was one other than us, make sure it's compatible with us.
				// It's read-only, so we don't need the definition lock for this check.
				EventMetricValueDefinitionCollection officialValues = officialDefinition.getValues();
				for (EventMetricValueDefinition ourValue : getValues().getList()) {
					EventMetricValueDefinition officialValue;
					OutObject<EventMetricValueDefinition> tempOutOfficialValue = new OutObject<EventMetricValueDefinition>();
					if (!officialValues.tryGetValue(ourValue.getName(), tempOutOfficialValue)) {
						officialValue = tempOutOfficialValue.argValue;
						// It doesn't have one of our value columns!
						throw new IllegalArgumentException(String.format(Locale.ROOT,
								"There is already an event metric definition for the same metrics system (%1$s), category name (%2$s), and counter name (%3$s), but it is not compatible; it does not define value column \"%4$s\".",
								getMetricsSystem(), getCategoryName(), getCounterName(), ourValue.getName()));
					} else {
						officialValue = tempOutOfficialValue.argValue;
						if (ourValue.getSerializedType() != ((EventMetricValueDefinition) officialValue)
								.getSerializedType()) {
							throw new IllegalArgumentException(String.format(Locale.ROOT,
									"There is already an event metric definition for the same metrics system (%1$s), category name (%2$s), and counter name (%3$s), but it is not compatible; "
											+ "it defines value column \"%4$s\" with type %5$s rather than type %6$s.",
									getMetricsSystem(), getCategoryName(), getCounterName(), ourValue.getName(),
									officialValue.getClass().getName(), ourValue.getType().getSimpleName()));
						}
					}
				}

				// We got through all the values defined in this instance? Then we're okay to
				// return the official one.
			}
			// Otherwise, it's just us, so we're all good.
		}

		return officialDefinition;
	}
	
	///////////////////////////////////////////////////////

	/**
	 * Adds the or get.
	 *
	 * @param instanceName the instance name
	 * @return the event metric
	 */
	public EventMetric addOrGet(String instanceName) {
		// now that we have our instance name, we go ahead and see if there is already
		// an instance with the right name or just add it
		// make sure the try & add are atomic

		synchronized (getLock()) {
			EventMetric metric = getMetrics().get(instanceName);
			EventMetric eventMetric;

			if (metric == null) {
				// there isn't one with the right name, we need to create it. It will add itself
				// to the metrics collection
				// in the constructor so we don't have to.
				eventMetric = add(instanceName);
			} else {
				eventMetric = metric;
			}

			return eventMetric;
		}
	}
	
	/**
	 * Adds the.
	 *
	 * @param instanceName the instance name
	 * @return the event metric
	 */
	public EventMetric add(String instanceName) {
		return this.getMetrics().add(TypeUtils.trimToNull(instanceName));
	}

	/**
	 * Creates a new metric definition from the provided information, or returns an
	 * existing matching definition if found. If the metric definition doesn't
	 * exist, it will be created. If the metric definition does exist, but is not a
	 * Custom Sampled Metric (or a derived class) an exception will be thrown.
	 * Definitions are looked up and added to the provided definitions dictionary.
	 *
	 * @param definitions    The definitions dictionary this definition is a part of
	 * @param metricTypeName The unique metric type
	 * @param categoryName   The name of the category with which this definition is
	 *                       associated.
	 * @param counterName    The name of the definition within the category.
	 * @return the event metric definition
	 */
	public static EventMetricDefinition addOrGet(MetricDefinitionCollection definitions, String metricTypeName,
			String categoryName, String counterName) {
		// we must have a definitions collection, or we have a problem
		if (definitions == null) {
			throw new NullPointerException("definitions");
		}

		// we need to find the definition, adding it if necessary
		String definitionKey = getKey(metricTypeName, categoryName, counterName);
		IMetricDefinition definition;

		// We need to grab a lock so our try get & the create are done as one lock.
		synchronized (definitions.getLock()) {
			OutObject<IMetricDefinition> tempOutDefinition = new OutObject<IMetricDefinition>();
			if (definitions.tryGetValue(definitionKey, tempOutDefinition)) {
				definition = tempOutDefinition.argValue;
			} else {
				definition = tempOutDefinition.argValue;
				// we didn't find one, make a new one
				definition = new EventMetricDefinition(metricTypeName, categoryName, counterName);
				definitions.add(definition); // Add it to the collection, no longer done in the constructor.
				// ToDo: Reconsider this implementation; putting incomplete event metric
				// definitions in the collection is not ideal.
			}
		}
		return (EventMetricDefinition) definition;
	}

	/**
	 * Creates a new metric definition from the provided information, or returns an
	 * existing matching definition if found. If the metric definition doesn't
	 * exist, it will be created. If the metric definition does exist, but is not an
	 * Event Metric an exception will be thrown. Definitions are looked up and added
	 * to the active logging metrics collection (Log.Metrics)
	 *
	 * @param metricTypeName The unique metric type
	 * @param categoryName   The name of the category with which this definition is
	 *                       associated.
	 * @param counterName    The name of the definition within the category.
	 * @return the event metric definition
	 */
	public static EventMetricDefinition addOrGet(String metricTypeName, String categoryName, String counterName) {
		// just forward into our call that requires the definition to be specified
		return addOrGet(Log.getMetrics(), metricTypeName, categoryName, counterName);
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
	
	///////////////////////////////////////////////////////
	
	/**
	 * The set of values defined for this metric definition
	 * 
	 * Any number of different values can be recorded along with each event to
	 * provide additional trends and filtering ability for later client analysis.
	 *
	 * @return the values
	 */
	public final EventMetricValueDefinitionCollection getValues() {
		return this.metricValues;
	}

	/**
	 * Indicates if this definition is configured to retrieve its information
	 * directly from an object.
	 * 
	 * When true, metric instances and samples can be defined from a live object of
	 * the same type that was used to generate the data binding. It isn't necessary
	 * that the same object be used, just that it be a compatible type to the
	 * original type used to establish the binding.
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
	 * The set of metrics that use this definition.
	 * 
	 * All metrics with the same definition are of the same object type.
	 *
	 * @return the metrics
	 */
	public EventMetricCollection getMetrics() {
		return this.metrics;
	}

	/**
	 * The set of values defined for this metric definition.
	 * 
	 * Any number of different values can be recorded along with each event to
	 * provide additional summarization and filtering ability for later client
	 * analysis.
	 *
	 * @return the value collection
	 */
	protected EventMetricValueDefinitionCollection getValueCollection() {
		return this.metricValues;
	}

	/**
	 * The set of values defined for this metric definition. (A snapshot array copy.
	 * AddValue() through this definition object.)
	 * 
	 * Any number of different values can be recorded along with each event to
	 * provide additional summarization and filtering ability for later client
	 * analysis. While the definition is being built (with
	 * AddValue the current set of value definitions can
	 * be examined as an array snapshot returned by this property. Changes to the
	 * array will only affect that copy.
	 *
	 * @return the value definitions
	 */
	public EventMetricValueDefinition[] getValueDefinitions() {
		return this.metricValues.toArray();
	}

	/**
	 * Indicates whether the provided object is a numeric type or can only be
	 * graphed by a SummaryFunction.Count.
	 * 
	 * @param type The type to be verified.
	 * @return True if the supplied type is mathematically graphable, false
	 *         otherwise.
	 */
	public static boolean isNumericValueType(java.lang.Class type) {
		// Just ask our internal class.
		return EventMetricDefinition.isTrendableValueType(type);
	}
	
	/**
	 * Indicates whether the provided object can be graphed as a trend.
	 * 
	 * @param type The type to be verified
	 * @return True if the supplied type is trendable, false otherwise.
	 */
	public static boolean isTrendableValueType(java.lang.Class type) {
		boolean trendable = false;

		// we're using Is so we can check for compatible types, not just base types.
		if ((type == Short.class) || (type == Short.class) || (type == Integer.class) || (type == Integer.class)
				|| (type == Long.class) || (type == Long.class) || (type == BigDecimal.class) || (type == Double.class)
				|| (type == Float.class)) {
			trendable = true;
		}
		// Now check object types
		else if ((type == OffsetDateTime.class) || (type == Duration.class)) {
			trendable = true;
		}

		return trendable;
	}

	/**
	 * Indicates whether the provided type can be stored as a value or not.
	 * 
	 * Most types can be stored, with the value of non-numeric types being the
	 * string representation of the type. Collections, arrays, and other such sets
	 * can't be stored as a single value.
	 * 
	 * @param type The type to be verified.
	 * @return True if the supplied type is supported, false otherwise.
	 */
	public static boolean isSupportedValueType(java.lang.Class type) {
		// Just ask our internal class.
		return EventMetricDefinition.isSupportedValueType(type);
	}

	/**
	 * The default value to display for this event metric. Typically this should be
	 * a trendable value.
	 *
	 * @return the default value
	 */
	public final EventMetricValueDefinition getDefaultValue() {
		return ((TypeUtils.isBlank(packet.getDefaultValueName())) ? null
				: getValues().get(packet.getDefaultValueName()));
	}
	
	/**
	 * Sets the default value.
	 *
	 * @param value the new default value
	 */
	public final void setDefaultValue(EventMetricValueDefinition value) {
		packet.setDefaultValueName(((value == null) ? null : getValues().get(value.getName()).getName()));
	}

	/**
	 * Write a metric sample to the specified instance of this event metric
	 * definition using the provided data object.
	 * 
	 * 
	 * <p>
	 * This overload may only be used if this metric definition was created by
	 * EventMetric and EventMetricValue attributes on a particular Type (class,
	 * struct, or interface), and only for userDataObjects of a type assignable to
	 * that bound type for this definition. Also see the static WriteAllSamples()
	 * method.
	 * </p>
	 * <p>
	 * The provided instanceName parameter will override any instance name binding
	 * set for this definition with an EventMetricInstanceName attribute (this
	 * method overload ignores the instance name binding). The specified metric
	 * instance is created if it does not already exist. See the other overloads
	 * taking a userDataObject as the first parameter to use the bound member to
	 * determine a metric instance name from the user data object automatically,
	 * with an optional fall-back instance name.
	 * </p>
	 * 
	 * @param instanceName   The instance name to use, or null or empty for the
	 *                       default metric instance.
	 * @param userDataObject A data object to sample, compatible with the binding
	 *                       type of this definition.
	 */
	public void writeSample(String instanceName, Object userDataObject) {
		if (userDataObject == null) {
			throw new NullPointerException("userDataObject");
		}

		boolean weAreRegistered = true;
		if (!isReadOnly()) {
			// Uh-oh, we're not actually a registered definition! Try to register ourselves.
			EventMetricDefinition registeredDefinition = register();

			if (registeredDefinition == null) {
				throw new IllegalArgumentException(String.format(Locale.ROOT,
						"Unknown error registering event metric definition: metrics system (%1$s), category name (%2$s), counter name (%3$s).",
						getMetricsSystem(), getCategoryName(), getCounterName()));
			}
			if (this != registeredDefinition) {
				weAreRegistered = false; // So we won't try to do this again below.
				registeredDefinition.writeSample(instanceName, userDataObject); // Have the registered one do it.
			}
		}

		if (weAreRegistered) {
			if (!isBound()) {
				throw new IllegalArgumentException(
						"This event metric definition is not bound to sample automatically from a user data object.  CreateSample() and SetValue() must be used to specify the data values directly.");
			}

			java.lang.Class userDataType = userDataObject.getClass();
			if (!getBoundType().isAssignableFrom(userDataType)) {
				throw new IllegalArgumentException(String.format(Locale.ROOT,
						"The provided user data object type (%1$s) is not assignable to this event metric's bound type (%2$s) and can not be sampled automatically for this metric definition.",
						userDataType, getBoundType()));
			}

			EventMetric metricInstance = EventMetric.register(this, instanceName); // Get the particular instance
																					// specified.
			metricInstance.writeSample(userDataObject); // And write a sample from the provided data object.
		}
	}

	/**
	 * Write a metric sample to an automatically-determined instance of this metric
	 * definition using the provided data object, with a fall-back instance name.
	 * 
	 * @param metricData           A data object to sample, compatible with the
	 *                             binding type of this definition.
	 * @param fallbackInstanceName The instance name to fall back on if this
	 *                             definition does not specify an instance name
	 *                             binding (may be null).
	 * 
	 *                             <p>
	 *                             This overload may only be used if this metric
	 *                             definition was created by EventMetric and
	 *                             EventMetricValue attributes on a particular Type
	 *                             (class, struct, or interface), and only for
	 *                             userDataObjects of a type assignable to that
	 *                             bound type for this definition.
	 *                             </p>
	 *                             <p>
	 *                             The metric instance name will be obtained from
	 *                             the member which was marked with the
	 *                             EventMetricInstanceName attribute. If none is
	 *                             bound, the instance name parameter will be used
	 *                             as a fall-back. The determined metric instance
	 *                             will be created if it does not already exist.
	 *                             </p>
	 * 
	 *                             @throws NullPointerException No metricData object was
	 *                             provided. 
	 *                             @throws IllegalArgumentException This event metric definition
	 *                             is not bound to sample automatically from a user
	 *                             data object. CreateSample() and SetValue() must
	 *                             be used to specify the data values
	 *                             directly.&lt;br /&gt; -or-&lt;br /&gt; The
	 *                             provided user data object is not assignable to
	 *                             this event metric's bound type and can not be
	 *                             sampled automatically for this metric definition.
	 *                              See the
	 *                             EventMetric Class
	 *                             Overview for an example.
	 */
	public void writeSample(Object metricData, String fallbackInstanceName) {
		if (metricData == null) {
			throw new NullPointerException("metricData");
		}

		boolean weAreRegistered = true;
		if (!isReadOnly()) {
			// Uh-oh, we're not actually a registered definition! Try to register ourselves.
			EventMetricDefinition registeredDefinition = register();

			if (registeredDefinition == null) {
				throw new IllegalArgumentException(String.format(Locale.ROOT,
						"Unknown error registering event metric definition: metrics system (%1$s), category name (%2$s), counter name (%3$s).",
						getMetricsSystem(), getCategoryName(), getCounterName()));
			}
			if (this != registeredDefinition) {
				weAreRegistered = false; // So we won't try to do this again below.
				registeredDefinition.writeSample(metricData, fallbackInstanceName); // Have the registered one do it.
			}
		}

		if (weAreRegistered) {
			if (!isBound()) {
				throw new IllegalArgumentException(
						"This event metric definition is not bound to sample automatically from a user data object.  CreateSample() and SetValue() must be used to specify the data values directly.");
			}

			java.lang.Class userDataType = metricData.getClass();
			if (!getBoundType().isAssignableFrom(userDataType)) {
				throw new IllegalArgumentException(String.format(Locale.ROOT,
						"The provided user data object type (%1$s) is not assignable to this event metric's bound type (%2$s) and can not be sampled automatically for this metric definition.",
						userDataType, getBoundType()));
			}

			String autoInstanceName = fallbackInstanceName; // Use the fall-back instance unless we find a specific
															// instance name.

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
	}

	/**
	 * Write a metric sample to an automatically-determined instance of this metric
	 * definition using the provided data object.
	 * 
	 * 
	 * <p>
	 * This overload may only be used if this metric definition was created by
	 * EventMetric and EventMetricValue attributes on a particular Type (class,
	 * struct, or interface), and only for userDataObjects of a type assignable to
	 * that bound type for this definition.
	 * </p>
	 * <p>
	 * The metric instance name will be obtained from the member which was marked
	 * with the EventMetricInstanceName attribute. If none is bound, the default
	 * instance will be used (a null instance name). The determined metric instance
	 * will be created if it does not already exist. See the overloads with an
	 * instanceName parameter to specify a particular metric instance name.
	 * </p>
	 * 
	 * @param metricData A data object to sample, compatible with the binding type
	 *                   of this definition.
	 *                   @throws IllegalArgumentException This event
	 *                   metric definition is not bound to sample automatically from
	 *                   a user data object. CreateSample() and SetValue() must be
	 *                   used to specify the data values directly.&lt;br /&gt;
	 *                   -or-&lt;br /&gt; The provided user data object is not
	 *                   assignable to this event metric's bound type and can not be
	 *                   sampled automatically for this metric definition. 
	 *                   See the EventMetric Class
	 *                   Overview for an example. 
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

			if (rawName == null || rawName instanceof String) {
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
	 * Sample every event metric defined by EventMetric and EventMetricValue
	 * attributes on the provided data object at any interface or inheritance level.
	 * 
	 * @param metricData           A user data object defining event metrics by
	 *                             attributes on itself or its interfaces or any
	 *                             inherited type.
	 * @param fallbackInstanceName The instance name to fall back on if a given
	 *                             definition does not specify an instance name
	 *                             binding (may be null).
	 *                             @throws IllegalArgumentException The specified
	 *                             metricDataObjectType does not have an EventMetric
	 *                             attribute &lt;br /&gt; &lt;br /&gt; -or- &lt;br
	 *                             /&gt; &lt;br /&gt; The specified Type does not
	 *                             have a usable EventMetric attribute, so it can't
	 *                             be used to define an event metric.&lt;br /&gt;
	 *                             &lt;br /&gt; -or- &lt;br /&gt; &lt;br /&gt; The
	 *                             specified Type's EventMetric has an empty metric
	 *                             namespace which is not allowed, so no metric can
	 *                             be defined.&lt;br /&gt; &lt;br /&gt; -or- &lt;br
	 *                             /&gt; &lt;br /&gt; The specified Type's
	 *                             EventMetric has an empty metric category name
	 *                             which is not allowed, so no metric can be
	 *                             defined.&lt;br /&gt; &lt;br /&gt; -or- &lt;br
	 *                             /&gt; &lt;br /&gt; The specified Type's
	 *                             EventMetric has an empty metric counter name
	 *                             which is not allowed, so no metric can be
	 *                             defined.&lt;br /&gt; &lt;br /&gt; -or- &lt;br
	 *                             /&gt; &lt;br /&gt; The specified Type's
	 *                             EventMetric attribute's 3-part Key is already
	 *                             used for a metric definition which is not an
	 *                             event metric.  See the
	 *                             EventMetric Class
	 *                             Overview for an example.
	 */
	public static void write(Object metricData, String fallbackInstanceName) {
		EventMetricDefinition[] allDefinitions = registerAll(metricData);

		for (EventMetricDefinition definition : allDefinitions) {
			try {
				definition.writeSample(metricData, fallbackInstanceName);
			}
			// ReSharper disable EmptyGeneralCatchClause
			catch (Exception e) {
				if (SystemUtils.isInDebugMode()) {
					e.printStackTrace();
				}
			}
		}

		return;
	}

	/**
	 * Sample every event metric defined by EventMetric and EventMetricValue
	 * attributes on the provided data object at any interface or inheritance level.
	 * 
	 * @param metricData A user data object defining event metrics by attributes on
	 *                   itself or its interfaces or any inherited type.
	 *                   @throws IllegalArgumentException The
	 *                   specified metricDataObjectType does not have an EventMetric
	 *                   attribute &lt;br /&gt; &lt;br /&gt; -or- &lt;br /&gt;
	 *                   &lt;br /&gt; The specified Type does not have a usable
	 *                   EventMetric attribute, so it can't be used to define an
	 *                   event metric.&lt;br /&gt; &lt;br /&gt; -or- &lt;br /&gt;
	 *                   &lt;br /&gt; The specified Type's EventMetric has an empty
	 *                   metric namespace which is not allowed, so no metric can be
	 *                   defined.&lt;br /&gt; &lt;br /&gt; -or- &lt;br /&gt; &lt;br
	 *                   /&gt; The specified Type's EventMetric has an empty metric
	 *                   category name which is not allowed, so no metric can be
	 *                   defined.&lt;br /&gt; &lt;br /&gt; -or- &lt;br /&gt; &lt;br
	 *                   /&gt; The specified Type's EventMetric has an empty metric
	 *                   counter name which is not allowed, so no metric can be
	 *                   defined.&lt;br /&gt; &lt;br /&gt; -or- &lt;br /&gt; &lt;br
	 *                   /&gt; The specified Type's EventMetric attribute's 3-part
	 *                   Key is already used for a metric definition which is not an
	 *                   event metric.  See the
	 *                   EventMetric Class Overview
	 *                   for an example. 
	 */
	public static void write(Object metricData) {
		write(metricData, null);
	}

	/**
	 * Object Change Locking object.
	 *
	 * @return the lock
	 */
	public Object getLock() {
		return this.lock;
	}

	
	/**
	 * Gets the packet.
	 *
	 * @return the packet
	 */
	protected EventMetricDefinitionPacket getPacket() {
		return packet;
	}

	/**
	 * Indicates if there is a binding for metric instance name.
	 * 
	 * When true, the Name Member Name and Name Member Type properties are
	 * available.
	 *
	 * @return the name bound
	 */
	protected boolean getNameBound() {
		return this.nameBound;
	}

	/**
	 * Sets the name bound.
	 *
	 * @param value the new name bound
	 */
	protected void setNameBound(boolean value) {
		this.nameBound = value;
	}

	/**
	 * The name of the member to invoke to determine the metric instance name.
	 * 
	 * This property is only valid when NameBound is true.
	 *
	 * @return the name member name
	 */
	protected String getNameMemberName() {
		return this.nameMemberName;
	}

	/**
	 * Sets the name member name.
	 *
	 * @param value the new name member name
	 */
	protected void setNameMemberName(String value) {
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
	protected MemberType getNameMemberType() {
		return this.nameMemberType;
	}

	/**
	 * Sets the name member type.
	 *
	 * @param value the new name member type
	 */
	protected void setNameMemberType(MemberType value) {
		this.nameMemberType = value;
	}

	/**
	 * The unique Id of this event metric definition. This can reliably be used as a
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
	@Override
	public UUID getId() {
		return this.packet.getID();
	}
	
	/**
	 * The name of the metric definition being captured.
	 * 
	 * The name is for comparing the same definition in different sessions. They
	 * will have the same name but not the same Id.
	 *
	 * @return the name
	 */
	public final String getName() {
		return this.packet.getName();
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

	/**
	 * Sets the caption.
	 *
	 * @param value the new caption
	 */
	public void setCaption(String value) {
		this.packet.setCaption(value);
	}

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

	/**
	 * Sets the description.
	 *
	 * @param value the new description
	 */
	public void setDescription(String value) {
		this.packet.setDescription(value);
	}

	/**
	 * The recommended default display interval for graphing.
	 *
	 * @return the interval
	 */
	//TODO refactor this when we pull up the model, use only agent types.
	@Override
	public SamplingInterval getInterval() {
		return SamplingInterval.forValue(this.packet.getInterval().getValue());
	}

	/*
	 * /// <summary> /// The definitions collection that contains this definition.
	 * /// </summary> /// <remarks>This parent pointer should be used when walking
	 * from an object back to its parent instead of taking /// advantage of the
	 * static metrics definition collection to ensure your application works as
	 * expected when handling /// data that has been loaded from a database or data
	 * file. The static metrics collection is for the metrics being /// actively
	 * captured in the current process, not for metrics that are being read or
	 * manipulated.</remarks> internal MetricDefinitionCollection Definitions { get
	 * { return Definitions; } }
	 */

	/**
	 * The metric capture system label under which this metric definition was
	 * created.
	 * 
	 * This label distinguish metrics defined and captured by different libraries
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
	 * Indicates whether this event metric definition is now read-only because it
	 * has been officially registered and can be used to create event metric
	 * instances.
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
			this.metricValues.setAllIndex();
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
	 * Retrieve an EventMetricDefinition by its Id, if present. (Throws an
	 * ArgumentException if the Id resolves to a SampledMetricDefinition instead.)
	 * 
	 * This method looks in the collection of registered metric definitions for the
	 * specified Id key. If it is not found, the output is set to null and the
	 * method returns false. If the Id key is found and resolves to an
	 * EventMetricDefinition, it is stored in the value output parameter and the
	 * method returns true. If the Id key is found but is not an
	 * EventMetricDefinition, an ArgumentException is thrown to signal a usage
	 * inconsistency in your code.
	 * 
	 * @param id    The Id of the event metric definition to get.
	 * @param value The output variable to receive the EventMetricDefinition object
	 *              if found (null if not).
	 * @return False if no metric definition is registered with the given Id, true
	 *         if an EventMetricDefinition is registered with the given Id, or
	 *         throws an exception if the registered definition is not an
	 *         EventMetricDefinition.
	 */
	public static boolean tryGetValue(UUID id, OutObject<EventMetricDefinition> value) {
		IMetricDefinition definition;

		// gateway to our internal collection TryGetValue()
		OutObject<IMetricDefinition> tempOutDefinition = new OutObject<IMetricDefinition>();
		boolean foundValue = definitions.tryGetValue(id, tempOutDefinition);
		definition = tempOutDefinition.argValue;
		value.argValue = foundValue
				? definition instanceof EventMetricDefinition ? (EventMetricDefinition) definition : null
				: null;
		if (foundValue && value.argValue == null) {
			// Uh-oh, we found one but it didn't resolve to an EventMetricDefinition!
			throw new IllegalArgumentException(String.format(Locale.ROOT,
					"The metric definition found by Id (%1$s) is not an event metric definition.", id));
		}
		return foundValue;
	}

	/**
	 * Retrieve an EventMetricDefinition by its combined three-part Key string, if
	 * present.
	 * 
	 * This method looks in the collection of registered metric definitions for the
	 * specified Id key. If it is not found, the output is set to null and the
	 * method returns false. If the Id key is found and resolves to an
	 * EventMetricDefinition, it is stored in the value output parameter and the
	 * method returns true. If the Id key is found but is not an
	 * EventMetricDefinition, an ArgumentException is thrown to signal a usage
	 * inconsistency in your code.
	 * 
	 * @param key   The Key of the event metric definition to get (composed of the
	 *              metrics system, category name, and counter name combined as a
	 *              single string).
	 * @param value The output variable to receive the EventMetricDefinition object
	 *              if found (null if not).
	 * @return False if no metric definition is registered with the given Key, true
	 *         if an EventMetricDefinition is registered with the given Key, or
	 *         throws an exception if the registered definition is not an
	 *         EventMetricDefinition.
	 */
	public static boolean tryGetValue(String key, OutObject<EventMetricDefinition> value) {
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
				? definition instanceof EventMetricDefinition ? (EventMetricDefinition) definition : null
				: null;
		if (foundValue && value.argValue == null) {
			// Uh-oh, we found one but it didn't resolve to an EventMetricDefinition!
			throw new IllegalArgumentException(String.format(Locale.ROOT,
					"The metric definition found by Key \"%1$s\" is not an event metric definition.", key));
		}
		return foundValue;
	}

	/**
	 * Retrieve an EventMetricDefinition by its three key strings (metrics system,
	 * category name, and counter name), if present.
	 * 
	 * This method looks in the collection of registered metric definitions for the
	 * specified Id key. If it is not found, the output is set to null and the
	 * method returns false. If the Id key is found and resolves to an
	 * EventMetricDefinition, it is stored in the value output parameter and the
	 * method returns true. If the Id key is found but is not an
	 * EventMetricDefinition, an ArgumentException is thrown to signal a usage
	 * inconsistency in your code.
	 * 
	 * @param metricsSystem The metrics capture system label of the definition to
	 *                      look up.
	 * @param categoryName  The name of the category with which the definition is
	 *                      associated.
	 * @param counterName   The name of the definition within the category.
	 * @param value         The output variable to receive the EventMetricDefinition
	 *                      object if found (null if not).
	 * @return False if no metric definition is registered with the given Key, true
	 *         if an EventMetricDefinition is registered with the given Key, or
	 *         throws an exception if the registered definition is not an
	 *         EventMetricDefinition.
	 */
	public static boolean tryGetValue(String metricsSystem, String categoryName, String counterName,
			OutObject<EventMetricDefinition> value) {
		IMetricDefinition definition;

		// gateway to our inner dictionary try get value
		OutObject<IMetricDefinition> tempOutDefinition = new OutObject<IMetricDefinition>();
		boolean foundValue = definitions.tryGetValue(metricsSystem, categoryName, counterName, tempOutDefinition);
		definition = tempOutDefinition.argValue;
		value.argValue = foundValue
				? definition instanceof EventMetricDefinition ? (EventMetricDefinition) definition : null
				: null;
		if (foundValue && value.argValue == null) {
			// Uh-oh, we found one but it didn't resolve to an EventMetricDefinition!
			throw new IllegalArgumentException(String.format(Locale.ROOT,
					"The metric definition found by metrics system (%1$s) category name (%2$s) counter name (%3$s) is not an event metric definition.",
					metricsSystem, categoryName, counterName));
		}
		return foundValue;
	}

	/**
	 * Find an existing event metric definition previously registered via
	 * EventMetric and EventMetricValue attributes on a specific Type.
	 * 
	 * This method overload can obtain a previously registered EventMetricDefinition
	 * created through EventMetric and EventMetricValue attributes, by specifying
	 * the Type containing those attributes. If the specified Type does not have an
	 * EventMetric attribute defined, or if the Type has an EventMetric attribute
	 * but has not been registered (e.g. by a call to
	 * EventMetricDefinition.Register(userObjectType)), then false is returned (with
	 * out value set to null). If an event metric defined by attributes on that Type
	 * has been successfully registered, then true is returned (with the registered
	 * EventMetricDefinition stored in the out value). If the metric definition
	 * found by the 3-part Key used in the EventMetric attribute is not an event
	 * metric (e.g. a sampled metric definition was registered with that Key), then
	 * an ArgumentException is thrown to signal your programming mistake.
	 * Inheritance and interfaces will <b>not</b> be searched, so the specified Type
	 * must directly define an event metric, but valid objects of a type assignable
	 * to the specified bound Type of this definition <b>can</b> be sampled from the
	 * specific event metric definition found.
	 * 
	 * @param metricDataObjectType A specific Type with attributes defining an event
	 *                             metric.
	 * @param value                The output variable to receive the
	 *                             EventMetricDefinition object if found (null if
	 *                             not).
	 * @return False if no EventMetric attribute is found on the specified Type, or
	 *         if no metric definition is registered with the 3-part Key found in
	 *         that attribute, true if an EventMetricDefinition is registered with
	 *         the given Key, or throws an exception if the registered definition
	 *         found is not an EventMetricDefinition.
	 */
	public static boolean tryGetValue(java.lang.Class metricDataObjectType, OutObject<EventMetricDefinition> value) {
		if (metricDataObjectType == null) {
			value.argValue = null;
			throw new NullPointerException("metricDataObjectType");
		}

		boolean foundValue;
		// We shouldn't need a lock here because we aren't changing the dictionary, just
		// doing a single read check.
		synchronized (dictionaryLock) // But apparently Dictionary may not be internally threadsafe, so we do need our
										// lock.
		{
			value.argValue = definitionMap.get(metricDataObjectType); 
			foundValue = value.argValue != null;
			// Fast lookup, for efficiency.
		}

		// We have to check for a possible null in the map, meaning we've seen that Type
		// but it couldn't register it.
		// We'll treat a null as a not-found case, and look for the attribute.
		if (!foundValue || value.argValue == null) {
			EventMetricClass eventMetricAnnotation = null;
			if (metricDataObjectType.isAnnotationPresent(EventMetricClass.class)) {
				eventMetricAnnotation = (EventMetricClass) metricDataObjectType.getAnnotation(EventMetricClass.class);
			}

			if (eventMetricAnnotation != null) {
				String metricsSystem = eventMetricAnnotation.namespace();
				String categoryName = eventMetricAnnotation.categoryName();
				String counterName = eventMetricAnnotation.counterName();

				IMetricDefinition definition;

				// gateway to our inner dictionary try get value
				OutObject<IMetricDefinition> tempOutDefinition = new OutObject<IMetricDefinition>();
				foundValue = definitions.tryGetValue(metricsSystem, categoryName, counterName, tempOutDefinition);
				definition = tempOutDefinition.argValue;
				value.argValue = foundValue
						? definition instanceof EventMetricDefinition ? (EventMetricDefinition) definition : null
						: null;
				if (foundValue && value.argValue == null) {
					// Uh-oh, we found one but it didn't resolve to an EventMetricDefinition!
					throw new IllegalArgumentException(String.format(Locale.ROOT,
							"The metric definition registered for metrics system (%1$s) category name (%2$s) counter name (%3$s) specified in EventMetric attribute on %4$s is not an event metric definition.",
							metricsSystem, categoryName, counterName, metricDataObjectType.getSimpleName()));
				}
			} else {
				foundValue = false;
				value.argValue = null;
			}
		}
		// else we found a valid definition in our Type-to-definition map, so we've
		// output that and report success.

		return foundValue;
	}
	
	/**
	 * Builder.
	 *
	 * @param metricTypeName the metric type name
	 * @param categoryName the category name
	 * @param counterName the counter name
	 * @return the builder
	 */
	public static Builder builder(String metricTypeName, String categoryName, String counterName) {
		return new Builder(metricTypeName, categoryName, counterName);
	}

	/**
	 * Builder.
	 *
	 * @param packet the packet
	 * @return the builder
	 */
	public static Builder builder(EventMetricDefinitionPacket packet) {
		return new Builder(packet);
	}
	
	/**
	 * The Class Builder.
	 */
	public static final class Builder {
		
		/** The definition. */
		private EventMetricDefinition definition;

		/**
		 * Instantiates a new builder.
		 *
		 * @param metricTypeName the metric type name
		 * @param categoryName the category name
		 * @param counterName the counter name
		 */
		private Builder(String metricTypeName, String categoryName, String counterName) {
			definition = new EventMetricDefinition(metricTypeName, categoryName, counterName);
		}

		/**
		 * Instantiates a new builder.
		 *
		 * @param packet the packet
		 */
		private Builder(EventMetricDefinitionPacket packet) {
			definition = new EventMetricDefinition(packet);
		}
		
		/**
		 * Default value.
		 *
		 * @param eventMetricValueDefinition the event metric value definition
		 * @return the builder
		 */
		public Builder defaultValue(EventMetricValueDefinition eventMetricValueDefinition) {
			definition.setDefaultValue(eventMetricValueDefinition);
			return this;
		}
		
		/**
		 * Adds the value.
		 *
		 * @param name the name
		 * @param type the type
		 * @param summaryFunction the summary function
		 * @param unitCaption the unit caption
		 * @param caption the caption
		 * @param description the description
		 * @return the builder
		 */
		public Builder addValue(String name, java.lang.Class type, SummaryFunction summaryFunction,
				String unitCaption, String caption, String description) {
			definition.addValue(name, type, summaryFunction, unitCaption, caption, description);
			return this;
		}
		
		/**
		 * Description.
		 *
		 * @param description the description
		 * @return the builder
		 */
		public Builder description(String description) {
			definition.setDescription(description);
			return this;
		}
		
		/**
		 * Caption.
		 *
		 * @param caption the caption
		 * @return the builder
		 */
		public Builder caption(String caption) {
			definition.getPacket().setCaption(caption);
			return this;
		}

		/**
		 * Builds the.
		 *
		 * @return the event metric definition
		 */
		public EventMetricDefinition build() {
			return definition;
		}
		
		/**
		 * Register.
		 *
		 * @return the event metric definition
		 */
		public EventMetricDefinition register() {
			return definition.register();
		}
	}
}