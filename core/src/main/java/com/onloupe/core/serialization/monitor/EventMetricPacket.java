package com.onloupe.core.serialization.monitor;

import com.onloupe.core.serialization.ICachedPacket;
import com.onloupe.core.serialization.IPacket;
import com.onloupe.core.serialization.PacketDefinition;
import com.onloupe.core.serialization.SerializedPacket;

import java.util.ArrayList;
import java.util.List;


/**
 * The serializeable representation of a custom sampled metric.
 */
public class EventMetricPacket extends MetricPacket implements ICachedPacket {
	/**
	 * Create a new event metric packet for the provided metric definition and a
	 * specific instance.
	 * 
	 * @param metricDefinitionPacket The metric definition packet that defines this
	 *                               metric
	 * @param instanceName           The unique instance name of this metric or null
	 *                               for the default instance.
	 */
	public EventMetricPacket(EventMetricDefinitionPacket metricDefinitionPacket, String instanceName) {
		super(metricDefinitionPacket, instanceName);
	}

	/**
	 * Compare this event metric packet to another to determine sort order.
	 *
	 * @param other the other
	 * @return the int
	 */
	public final int compareTo(EventMetricPacket other) {
		// we just gateway to our base object.
		return super.compareTo(other);
	}

	/**
	 * Indicates whether the current object is equal to another object of the same
	 * type.
	 *
	 * @param other An object to compare with this object.
	 * @return true if the current object is equal to the 
	 *         parameter; otherwise, false.
	 */
	@Override
	public boolean equals(Object other) {
		// use our type-specific override
		return equals(other instanceof EventMetricPacket ? (EventMetricPacket) other : null);
	}

	/**
	 * Determines if the provided object is identical to this object.
	 * 
	 * @param other The object to compare this object to
	 * @return True if the objects represent the same data.
	 */
	public final boolean equals(EventMetricPacket other) {
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

	/** The Constant SERIALIZATION_VERSION. */
	private static final int SERIALIZATION_VERSION = 1;

	/**
	 * The list of packets that this packet depends on.
	 * 
	 * @return An array of IPackets, or null if there are no dependencies.
	 */
	@Override
	public final List<IPacket> getRequiredPackets() {
		List<IPacket> requiredPackets = super.getRequiredPackets();

		// we need to add in required packets for the metric definition values. If we don't, they will never get written out.
		EventMetricDefinitionPacket metricDefinitionPacket = (EventMetricDefinitionPacket) getDefinitionPacket();
		for (int curValueIndex = 0; curValueIndex < metricDefinitionPacket.getMetricValues().getCount(); curValueIndex++) {
			requiredPackets.add(metricDefinitionPacket.getMetricValues().get(curValueIndex).getPacket());
		}

		return requiredPackets;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.monitor.MetricPacket#writePacketDefinition(com.onloupe.core.serialization.PacketDefinition)
	 */
	@Override
	public void writePacketDefinition(PacketDefinition definition) {
		super.writePacketDefinition(definition.getParentIPacket());

		definition.setVersion(SERIALIZATION_VERSION);
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.monitor.MetricPacket#writeFields(com.onloupe.core.serialization.PacketDefinition, com.onloupe.core.serialization.SerializedPacket)
	 */
	@Override
	public final void writeFields(PacketDefinition definition, SerializedPacket packet) {
		super.writeFields(definition.getParentIPacket(), packet.getParentIPacket());
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.monitor.MetricPacket#readFields(com.onloupe.core.serialization.PacketDefinition, com.onloupe.core.serialization.SerializedPacket)
	 */
	@Override
	public final void readFields(PacketDefinition definition, SerializedPacket packet) {
		// we only exist to do the required packet thing
		throw new UnsupportedOperationException("Deserialization of agent data is not supported");
	}
}