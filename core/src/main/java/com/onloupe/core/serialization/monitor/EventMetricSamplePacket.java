package com.onloupe.core.serialization.monitor;

import com.onloupe.agent.metrics.EventMetric;
import com.onloupe.agent.metrics.EventMetricValueDefinition;
import com.onloupe.agent.metrics.EventMetricValueDefinitionCollection;
import com.onloupe.core.serialization.FieldDefinition;
import com.onloupe.core.serialization.IDynamicPacket;
import com.onloupe.core.serialization.IPacket;
import com.onloupe.core.serialization.PacketDefinition;
import com.onloupe.core.serialization.SerializedPacket;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * One raw data sample of an event metric
 * 
 * A metric sample packet must be explicitly logged to be recorded, although
 * when it is logged does not affect any of its data (timestamps and other
 * information are captured during construction).
 */
public class EventMetricSamplePacket extends MetricSamplePacket implements IDynamicPacket, java.lang.Comparable<EventMetricSamplePacket> {
	
	/** The value definitions. */
	private EventMetricValueDefinitionCollection valueDefinitions;

	/**
	 * Create an event metric sample packet for live data collection.
	 *
	 * @param metric The metric this sample is for
	 */
	public EventMetricSamplePacket(EventMetric metric) {
		super(metric.getPacket());
		// create a new sample values collection the correct size of our metric's values
		// collection
		setValues(new Object[metric.getDefinition().getValues().getEventMetricValueDefinitions().size()]);
		this.valueDefinitions = metric.getDefinition().getValues();

		// and set our default dynamic type name based on our metric definition. It
		// isn't clear to me
		// that there's really a contract that it won't be changed by the serializer, so
		// we allow it to be
		setDynamicTypeName(metric.getDefinition().getName());
	}

	/**
	 * Compare this object to another to determine sort order.
	 *
	 * @param other the other
	 * @return the int
	 */
	@Override
	public final int compareTo(EventMetricSamplePacket other) {
		// we just gateway to our base object.
		return super.compareTo(other);
	}

	/**
	 * Indicates whether the current object is equal to another object of the same
	 * type.
	 *
	 * @param other An object to compare with this object.
	 * @return true if the current object is equal to the <paramref name="other" />
	 *         parameter; otherwise, false.
	 */
	@Override
	public boolean equals(Object other) {
		// use our type-specific override
		return equals(other instanceof EventMetricSamplePacket ? (EventMetricSamplePacket) other : null);
	}

	/**
	 * Determines if the provided object is identical to this object.
	 * 
	 * @param other The object to compare this object to
	 * @return True if the objects represent the same data.
	 */
	public final boolean equals(EventMetricSamplePacket other) {
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
	 * The values related to this event metric sample.
	 */
	private Object[] values;

	/**
	 * Gets the values.
	 *
	 * @return the values
	 */
	public final Object[] getValues() {
		return this.values;
	}

	/**
	 * Sets the values.
	 *
	 * @param value the new values
	 */
	private void setValues(Object[] value) {
		this.values = value;
	}

	// We need to explicitly implement this interface because we don't want to
	// override the IPacket implementation,
	// we want to have our own distinct implementation because the packet
	// serialization methods know to recurse object
	// structures looking for the interface.

	/** The Constant SERIALIZATION_VERSION. */
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

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.monitor.MetricSamplePacket#writePacketDefinition(com.onloupe.core.serialization.PacketDefinition)
	 */
	@Override
	public void writePacketDefinition(PacketDefinition definition) {
		super.writePacketDefinition(definition.getParentIPacket());

		definition.setVersion(SERIALIZATION_VERSION);

		// now we need to write out the definition of each of our values to our type.
		// we're a dynamic packet so we can have a different definition for our
		// declaring type each time.
		for (EventMetricValueDefinition valueDefinition : this.valueDefinitions.getEventMetricValueDefinitions()) {
			valueDefinition.addField(definition);
		}
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.monitor.MetricSamplePacket#writeFields(com.onloupe.core.serialization.PacketDefinition, com.onloupe.core.serialization.SerializedPacket)
	 */
	@Override
	public final void writeFields(PacketDefinition definition, SerializedPacket packet) {
		super.writeFields(definition.getParentIPacket(), packet.getParentIPacket());

		// iterate our array, writing out each value.
		for (int valueIndex = 0; valueIndex < definition.getFields().size(); valueIndex++) {
			FieldDefinition fieldDefinition = definition.getFields().get(valueIndex);
			packet.setField(fieldDefinition.getName(), getValues()[valueIndex]);
		}
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.monitor.MetricSamplePacket#readFields(com.onloupe.core.serialization.PacketDefinition, com.onloupe.core.serialization.SerializedPacket)
	 */
	@Override
	public final void readFields(PacketDefinition definition, SerializedPacket packet) {
		throw new UnsupportedOperationException("Deserialization of agent data is not supported");
	}

	/** The dynamic type name. */
	private String dynamicTypeName;

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.IDynamicPacket#getDynamicTypeName()
	 */
	@Override
	public final String getDynamicTypeName() {
		return this.dynamicTypeName;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.IDynamicPacket#setDynamicTypeName(java.lang.String)
	 */
	@Override
	public final void setDynamicTypeName(String value) {
		this.dynamicTypeName = value;
	}

}