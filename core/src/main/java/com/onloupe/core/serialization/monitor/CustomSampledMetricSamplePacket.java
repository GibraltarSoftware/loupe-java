package com.onloupe.core.serialization.monitor;

import com.onloupe.core.serialization.FieldType;
import com.onloupe.core.serialization.IPacket;
import com.onloupe.core.serialization.PacketDefinition;
import com.onloupe.core.serialization.SerializedPacket;

import java.time.OffsetDateTime;
import java.util.List;


/**
 * One raw data sample of a custom sampled metric
 * 
 * A metric sample packet must be explicitly logged to be recorded, although
 * when it is logged does not affect any of its data (timestamps and other
 * information are captured during construction).
 */
public class CustomSampledMetricSamplePacket extends SampledMetricSamplePacket implements IPacket, java.lang.Comparable<CustomSampledMetricSamplePacket> {
	
	/** The base value. */
	private double baseValue; // the (optional) base value to compare the raw value to (used in some metric
								// types)

	/**
								 * Create a complete custom sampled metric packet.
								 *
								 * @param packet   The metric this sample is for
								 * @param rawValue The raw data value
								 */
	public CustomSampledMetricSamplePacket(CustomSampledMetricPacket packet, double rawValue) {
		super(packet, rawValue);
		this.baseValue = 0;
	}

	/**
	 * Create a complete custom sampled metric packet.
	 *
	 * @param packet       The metric this sample is for
	 * @param rawValue     The raw data value
	 * @param rawTimeStamp The exact date and time the raw value was determined
	 */
	public CustomSampledMetricSamplePacket(CustomSampledMetricPacket packet, double rawValue, OffsetDateTime rawTimeStamp) {
		super(packet, rawValue, rawTimeStamp);
		this.baseValue = 0;
	}

	/**
	 * Create a complete custom sampled metric packet.
	 *
	 * @param packet    The metric this sample is for
	 * @param rawValue  The raw data value
	 * @param baseValue The reference value to compare against for come counter
	 *                  types
	 */
	public CustomSampledMetricSamplePacket(CustomSampledMetricPacket packet, double rawValue, double baseValue) {
		super(packet, rawValue);
		this.baseValue = baseValue;
	}

	/**
	 * Create a complete custom sampled metric packet.
	 *
	 * @param packet       The metric this sample is for
	 * @param rawValue     The raw data value
	 * @param baseValue    The reference value to compare against for come counter
	 *                     types
	 * @param rawTimeStamp The exact date and time the raw value was determined
	 */
	public CustomSampledMetricSamplePacket(CustomSampledMetricPacket packet, double rawValue, double baseValue,
			OffsetDateTime rawTimeStamp) {
		super(packet, rawValue, rawTimeStamp);
		this.baseValue = baseValue;
	}

	/**
	 * The base value as it was sampled
	 * 
	 * The base value is used with the raw value for certain counter types. For
	 * example, if you want to determine the percentage utilization, you need to
	 * know both how much capacity was used and how much was available. The base
	 * represents how much was available and the raw value how much was used in that
	 * scenario.
	 *
	 * @return the base value
	 */
	public final double getBaseValue() {
		return this.baseValue;
	}

	/**
	 * Sets the base value.
	 *
	 * @param value the new base value
	 */
	protected final void setBaseValue(double value) {
		this.baseValue = value;
	}

	/**
	 * Compare this custom sampled metric sample packet with another to determine if
	 * they are the same sample packet.
	 *
	 * @param other the other
	 * @return the int
	 */
	@Override
	public final int compareTo(CustomSampledMetricSamplePacket other) {
		// we really are just forwarding to the default comparitor; we are just casting
		// types
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
		return equals(
				other instanceof CustomSampledMetricSamplePacket ? (CustomSampledMetricSamplePacket) other : null);
	}

	/**
	 * Determines if the provided object is identical to this object.
	 * 
	 * @param other The object to compare this object to
	 * @return True if the objects represent the same data.
	 */
	public final boolean equals(CustomSampledMetricSamplePacket other) {
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
		// the majority of packets have no dependencies
		return super.getRequiredPackets();
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.monitor.SampledMetricSamplePacket#writePacketDefinition(com.onloupe.core.serialization.PacketDefinition)
	 */
	@Override
	public void writePacketDefinition(PacketDefinition definition) {
		super.writePacketDefinition(definition.getParentIPacket());

		definition.setVersion(SERIALIZATION_VERSION);

		definition.getFields().add("baseValue", FieldType.DOUBLE);
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.monitor.SampledMetricSamplePacket#writeFields(com.onloupe.core.serialization.PacketDefinition, com.onloupe.core.serialization.SerializedPacket)
	 */
	@Override
	public final void writeFields(PacketDefinition definition, SerializedPacket packet) {
		super.writeFields(definition.getParentIPacket(), packet.getParentIPacket());

		packet.setField("baseValue", this.baseValue);
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.monitor.SampledMetricSamplePacket#readFields(com.onloupe.core.serialization.PacketDefinition, com.onloupe.core.serialization.SerializedPacket)
	 */
	@Override
	public final void readFields(PacketDefinition definition, SerializedPacket packet) {
		throw new UnsupportedOperationException("Deserialization of agent data is not supported");
	}

}