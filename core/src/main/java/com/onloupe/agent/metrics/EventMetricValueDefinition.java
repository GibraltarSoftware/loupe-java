package com.onloupe.agent.metrics;

import com.onloupe.core.serialization.PacketDefinition;
import com.onloupe.core.serialization.monitor.EventMetricValueDefinitionPacket;
import com.onloupe.model.data.EventMetricValueTrend;
import com.onloupe.model.metric.MemberType;


/**
 * Defines one value that can be associated with an event metric, created via
 * eventMetricDefinition.AddValue(...);
 */
public final class EventMetricValueDefinition {
	
	/** The definition. */
	private EventMetricDefinition definition;
	
	/** The packet. */
	private EventMetricValueDefinitionPacket packet;
	
	/** The trendable. */
	private boolean trendable;
	
	/** The bound. */
	private boolean bound;
	
	/** The member type. */
	private MemberType memberType;
	
	/** The member name. */
	private String memberName;


	/**
	 * Create a new API value definition from a provided API event metric definition
	 * and internal value definition.
	 *
	 * @param definition      The API event metric definition that owns this value
	 *                        definition
	 * @param packet the packet
	 */
	public EventMetricValueDefinition(EventMetricDefinition definition, EventMetricValueDefinitionPacket packet) {
		this.definition = definition;
		this.packet = packet;
		
		this.setTrendable(EventMetricDefinition.isTrendableValueType(this.packet.getType()));
	}

	/**
	 * The default way that individual samples will be aggregated to create a
	 * graphable summary.
	 *
	 * @return the summary function
	 */
	public SummaryFunction getSummaryFunction() {
		return SummaryFunction.forValue(this.getDefaultTrend().getValue());
	}
	
	/**
	 * Sets the summary function.
	 *
	 * @param value the new summary function
	 */
	public void setSummaryFunction(int value) {
		setDefaultTrend(EventMetricValueTrend.forValue(value));
	}	

	/**
	 * The default way that individual samples will be aggregated to create a
	 * graphable trend.
	 *
	 * @return the default trend
	 */
	public final EventMetricValueTrend getDefaultTrend() {
		return this.packet.getDefaultTrend();
	}

	/**
	 * Sets the default trend.
	 *
	 * @param value the new default trend
	 */
	public final void setDefaultTrend(EventMetricValueTrend value) {
		this.packet.setDefaultTrend(value);
	}

	/**
	 * The metric definition this value is associated with.
	 *
	 * @return the definition
	 */
	public EventMetricDefinition getDefinition() {
		return this.definition;
	}

	/**
	 * The unique name for this value within the event definition.
	 *
	 * @return the name
	 */
	public String getName() {
		return this.packet.getName();
	}

	/**
	 * The end-user display caption for this value.
	 *
	 * @return the caption
	 */
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
	 * The end-user description for this value.
	 *
	 * @return the description
	 */
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
	 * The simple type of all data recorded for this value.
	 *
	 * @return the type
	 */
	public java.lang.Class getType() {
		return this.packet.getType();
	}

	/**
	 * The simple type that all data recorded for this value will be serialized as.
	 *
	 * @return the serialized type
	 */
	public java.lang.Class getSerializedType() {
		return this.packet.getSerializedType();
	}

	/**
	 * Indicates whether this metric value column can be graphed by a mathematical
	 * summary (true), or only by count (false).
	 *
	 * @return true, if is numeric
	 */
	public boolean isNumeric() {
		return this.isTrendable();
	}

	/**
	 * The display caption for the units this value column represents (if numeric),
	 * or null for unit-less values (or non-numeric).
	 *
	 * @return the unit caption
	 */
	public String getUnitCaption() {
		return this.packet.getUnitCaption();
	}

	/**
	 * Sets the unit caption.
	 *
	 * @param value the new unit caption
	 */
	public void setUnitCaption(String value) {
		this.packet.setUnitCaption(value);
	}

	/**
	 * Indicates whether the value is configured for automatic collection through
	 * binding
	 * 
	 * If true, the other binding-related properties are available.
	 *
	 * @return true, if is bound
	 */
	public boolean isBound() {
		return bound;
	}

	/**
	 * Sets the bound.
	 *
	 * @param bound the new bound
	 */
	public void setBound(boolean bound) {
		this.bound = bound;
	}

	/**
	 * The type of member that this value is bound to (field, property or method)
	 * 
	 * This property is only valid if Bound is true.
	 *
	 * @return the member type
	 */
	public MemberType getMemberType() {
		return this.memberType;
	}

	/**
	 * Sets the member type.
	 *
	 * @param value the new member type
	 */
	public void setMemberType(MemberType value) {
		this.memberType = value;
	}

	/**
	 * The name of the member that this value is bound to.
	 * 
	 * This property is only valid if Bound is true.
	 *
	 * @return the member name
	 */
	public String getMemberName() {
		return this.memberName;
	}

	/**
	 * Sets the member name.
	 *
	 * @param value the new member name
	 */
	public void setMemberName(String value) {
		this.memberName = value;
	}

	/**
	 * Checks if is trendable.
	 *
	 * @return true, if is trendable
	 */
	public boolean isTrendable() {
		return trendable;
	}

	/**
	 * Sets the trendable.
	 *
	 * @param trendable the new trendable
	 */
	public void setTrendable(boolean trendable) {
		this.trendable = trendable;
	}
	
	/**
	 * Gets the packet.
	 *
	 * @return the packet
	 */
	public EventMetricValueDefinitionPacket getPacket() {
		return packet;
	}
	
	/**
	 * The index of this value in the arrays, once the definition is read-only.
	 */
	private int myIndex;

	/**
	 * Gets the my index.
	 *
	 * @return the my index
	 */
	public final int getMyIndex() {
		return this.myIndex;
	}

	/**
	 * Sets the my index.
	 *
	 * @param value the new my index
	 */
	public final void setMyIndex(int value) {
		this.myIndex = value;
	}
	

	/**
	 * Add a definition for this value to the packet definition.
	 *
	 * @param definition The packet definition to add our value definition to
	 */
	public final void addField(PacketDefinition definition) {
		definition.getFields().add(getName(), this.packet.getSerializedType());
	}
	
	/**
	 * Determines if the provided object is identical to this object.
	 * 
	 * @param other The object to compare this object to
	 * @return True if the objects represent the same data.
	 */
	public final boolean equals(EventMetricValueDefinition other) {
		// Careful - can be null
		if (other == null) {
			return false; // since we're a live object we can't be equal.
		}

		if (this == other || this.packet == other.getPacket()) {
			return true; // If the objects or the underlying packet are the same object, they're the
							// same.
		}

		// We're really just a type cast, refer to our base object
		return this.packet.getName().equals(other.getName());
	}
}