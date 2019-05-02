package com.onloupe.core.serialization.monitor;

import com.onloupe.agent.metrics.EventMetricValueDefinitionCollection;
import com.onloupe.core.serialization.FieldType;
import com.onloupe.core.serialization.ICachedPacket;
import com.onloupe.core.serialization.IPacket;
import com.onloupe.core.serialization.PacketDefinition;
import com.onloupe.core.serialization.SerializedPacket;
import com.onloupe.model.SampleType;

import java.util.List;

/**
 * A serializable event metric definition. Provides metadata for metrics based
 * on events.
 */
public final class EventMetricDefinitionPacket extends MetricDefinitionPacket implements ICachedPacket, java.lang.Comparable<EventMetricDefinitionPacket> {
	private String defaultValueName;

	/**
	 * Creates an event metric definition packet for the provided event metric
	 * information
	 * 
	 * @param metricTypeName The unique metric type
	 * @param categoryName   The name of the category with which this definition is
	 *                       associated.
	 * @param counterName    The name of the definition within the category.
	 */
	public EventMetricDefinitionPacket(String metricTypeName, String categoryName, String counterName) {
		super(metricTypeName, categoryName, counterName, SampleType.EVENT);
	}

	/**
	 * The default value to display for this event metric. Typically this should be
	 * a trendable value.
	 */
	public String getDefaultValueName() {
		return this.defaultValueName;
	}

	public void setDefaultValueName(String value) {
		this.defaultValueName = value;
	}

	/**
	 * Compare this event metric definition packet with another.
	 * 
	 * @param other
	 * @return
	 */
	@Override
	public int compareTo(EventMetricDefinitionPacket other) {
		// we just gateway to our base object.
		return super.compareTo(other);
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
		return equals(other instanceof EventMetricDefinitionPacket ? (EventMetricDefinitionPacket) other : null);
	}

	/**
	 * Determines if the provided object is identical to this object.
	 * 
	 * @param other The object to compare this object to
	 * @return True if the objects represent the same data.
	 */
	public boolean equals(EventMetricDefinitionPacket other) {
		// We're really just a type cast, refer to our base object
		return super.equals(other);
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
		int myHash = super.hashCode(); // Equals defers to base, so just use hash code for inherited base type

		return myHash;
	}

	/**
	 * The collection of value definitions for this event metric
	 * 
	 * This is really a hack to allow the packet writer deep in the bowels of the
	 * system to find the metric value definitions to write out. We really need to
	 * refactor the model to get rid of this much coupling.
	 */
	private EventMetricValueDefinitionCollection metricValues;

	public EventMetricValueDefinitionCollection getMetricValues() {
		return this.metricValues;
	}

	public void setMetricValues(EventMetricValueDefinitionCollection value) {
		this.metricValues = value;
	}

	private static final int SERIALIZATION_VERSION = 1;

	/**
	 * The list of packets that this packet depends on.
	 * 
	 * @return An array of IPackets, or null if there are no dependencies.
	 */
	@Override
	public List<IPacket> getRequiredPackets() {
		// the majority of packets have no dependencies
		return super.getRequiredPackets();
	}

	@Override
	public void writePacketDefinition(PacketDefinition definition) {
		super.writePacketDefinition(definition.getParentIPacket());

		definition.setVersion(SERIALIZATION_VERSION);

		definition.getFields().add("DefaultValueName", FieldType.STRING);
	}

	@Override
	public void writeFields(PacketDefinition definition, SerializedPacket packet) {
		super.writeFields(definition.getParentIPacket(), packet.getParentIPacket());

		packet.setField("DefaultValueName", this.defaultValueName);
	}

	@Override
	public void readFields(PacketDefinition definition, SerializedPacket packet) {
		throw new UnsupportedOperationException("Deserialization of agent data is not supported");
	}

}