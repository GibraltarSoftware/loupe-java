package com.onloupe.core.serialization.monitor;

import com.onloupe.agent.metrics.EventMetricDefinition;
import com.onloupe.core.serialization.FieldType;
import com.onloupe.core.serialization.ICachedPacket;
import com.onloupe.core.serialization.IPacket;
import com.onloupe.core.serialization.PacketDefinition;
import com.onloupe.core.serialization.SerializedPacket;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.data.EventMetricValueTrend;

import java.util.List;
import java.util.UUID;

/**
 * A serializable event value definition. Provides metadata for one value
 * associated with an event
 */
public class EventMetricValueDefinitionPacket extends GibraltarCachedPacket implements ICachedPacket, IDisplayable {
	private String name;
	private String caption;
	private String description;
	private String unitCaption;
	private EventMetricValueTrend defaultTrend = EventMetricValueTrend.AVERAGE;
	private UUID eventDefinitionPacketId;

	/**
	 * Creates an event metric definition packet for the provided event metric
	 * information
	 * 
	 * @param definition  The event metric definition for this value.
	 * @param name        The unique name of this event value within the definition.
	 * @param type        The simple type of the data being stored in this value.
	 * @param caption     The end-user display caption for this value
	 * @param description The end-user description for this value.
	 */
	public EventMetricValueDefinitionPacket(EventMetricDefinitionPacket definition, String name, java.lang.Class type,
			String caption, String description) {
		super(false);
		this.eventDefinitionPacketId = definition.getID();
		setID(UUID.randomUUID());
		this.name = name;
		setType(type);
		this.caption = caption;
		this.description = description;
	}

	/**
	 * The default way that individual samples will be aggregated to create a
	 * graphable trend.
	 */
	public final EventMetricValueTrend getDefaultTrend() {
		return this.defaultTrend;
	}

	public final void setDefaultTrend(EventMetricValueTrend value) {
		this.defaultTrend = value;
	}

	/**
	 * The unique name for this value within the event definition.
	 */
	public final String getName() {
		return this.name;
	}

	/**
	 * The end-user display caption for this value.
	 */
	@Override
	public final String getCaption() {
		return this.caption;
	}

	public final void setCaption(String value) {
		this.caption = value == null ? getName() : value.trim();
	}

	/**
	 * The end-user description for this value.
	 */
	@Override
	public final String getDescription() {
		return this.description;
	}

	public final void setDescription(String value) {
		this.description = value == null ? null : value.trim();
	}

	/**
	 * The original type of all data recorded for this value.
	 */
	private java.lang.Class type;

	public final java.lang.Class getType() {
		return this.type;
	}

	/**
	 * The simple type of all data recorded for this value.
	 */
	private java.lang.Class serializedType;

	public final java.lang.Class getSerializedType() {
		return this.serializedType;
	}

	private void setSerializedType(java.lang.Class value) {
		this.serializedType = value;
	}

	/**
	 * The units of measure for the data captured with this value (if numeric)
	 */
	public final String getUnitCaption() {
		return this.unitCaption;
	}

	public final void setUnitCaption(String value) {
		this.unitCaption = value == null ? null : value.trim();
	}

	/**
	 * Indicates whether the current object is equal to another object of the same
	 * type.
	 * 
	 * @return true if the current object is equal to the <paramref name="other" />
	 *         parameter; otherwise, false.
	 * 
	 * @param other An object to compare with this object.
	 */
	@Override
	public boolean equals(Object other) {
		// use our type-specific override
		return equals(
				other instanceof EventMetricValueDefinitionPacket ? (EventMetricValueDefinitionPacket) other : null);
	}

	/**
	 * Determines if the provided object is identical to this object.
	 * 
	 * @param other The object to compare this object to
	 * @return True if the objects represent the same data.
	 */
	public final boolean equals(EventMetricValueDefinitionPacket other) {
		// Careful - can be null
		if (other == null) {
			return false; // since we're a live object we can't be equal.
		}

		return ((this.name.equals(other.name)) && (getType() == other.getType())
				&& (this.caption.equals(other.caption)) && (this.description.equals(other.description))
				&& (this.defaultTrend == other.defaultTrend)
				&& (this.eventDefinitionPacketId.equals(other.eventDefinitionPacketId))
				&& (this.unitCaption.equals(other.unitCaption)) && super.equals(other));
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
	 * @return an int representing the hash code calculated for the contents of this
	 *         object
	 * 
	 */
	@Override
	public int hashCode() {
		int myHash = super.hashCode(); // Fold in hash code for inherited base type

		myHash ^= this.eventDefinitionPacketId.hashCode(); // Fold in hash code for GUID
		if (this.name != null) {
			myHash ^= this.name.hashCode(); // Fold in hash code for string Name
		}
		if (this.caption != null) {
			myHash ^= this.caption.hashCode(); // Fold in hash code for string Caption
		}
		if (this.description != null) {
			myHash ^= this.description.hashCode(); // Fold in hash code for string Description
		}
		if (this.unitCaption != null) {
			myHash ^= this.unitCaption.hashCode(); // Fold in hash code for string UnitCaption
		}

		if (getType() != null) {
			myHash ^= getType().hashCode(); // Fold in hash code for Type member
		}

		// Not bothering with ...Trend member?

		return myHash;
	}

	/**
	 * The unique Id of the definition of this event value.
	 */
	public final UUID getDefinitionId() {
		return this.eventDefinitionPacketId;
	}

	private static final int SERIALIZATION_VERSION = 1;

	/**
	 * The list of packets that this packet depends on.
	 * 
	 * @return An array of IPackets, or null if there are no dependencies.
	 */
	@Override
	public final List<IPacket> getRequiredPackets() {
		// the majority of packets have no dependencies
		return super.getRequiredPackets();
	}

	@Override
	public void writePacketDefinition(PacketDefinition definition) {
		super.writePacketDefinition(definition.getParentIPacket());

		definition.setVersion(SERIALIZATION_VERSION);

		definition.getFields().add("name", FieldType.STRING);
		definition.getFields().add("valueType", FieldType.STRING);
		definition.getFields().add("caption", FieldType.STRING);
		definition.getFields().add("description", FieldType.STRING);
		definition.getFields().add("defaultTrend", FieldType.INT);
		definition.getFields().add("eventDefinitionPacketId", FieldType.GUID);
		definition.getFields().add("unitCaption", FieldType.STRING);
	}

	@Override
	public final void writeFields(PacketDefinition definition, SerializedPacket packet) {
		super.writeFields(definition.getParentIPacket(), packet.getParentIPacket());

		packet.setField("name", this.name);
		packet.setField("valueType", TypeUtils.getNetType(getSerializedType()));
		packet.setField("caption", this.caption);
		packet.setField("description", this.description);
		packet.setField("defaultTrend",
				this.defaultTrend != null ? this.defaultTrend.getValue() : EventMetricValueTrend.COUNT);
		packet.setField("eventDefinitionPacketId", this.eventDefinitionPacketId);
		packet.setField("unitCaption", this.unitCaption);
	}

	@Override
	public final void readFields(PacketDefinition definition, SerializedPacket packet) {
		throw new UnsupportedOperationException("Deserialization of agent data is not supported");
	}

	/**
	 * Translate provided type to the effective serializable type
	 * 
	 * @param originalType
	 */
	private void setType(java.lang.Class originalType) {
		this.type = originalType;
		setSerializedType(EventMetricDefinition.isTrendableValueType(originalType) ? originalType : String.class);
	}
}