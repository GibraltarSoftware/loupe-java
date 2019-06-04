package com.onloupe.core.serialization.monitor;

import com.onloupe.core.serialization.FieldType;
import com.onloupe.core.serialization.IPacket;
import com.onloupe.core.serialization.PacketDefinition;
import com.onloupe.core.serialization.SerializedPacket;

import java.time.OffsetDateTime;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The base class for a single sampled metric data sample.
 * 
 * A sampled metric sample packet must be explicitly logged to be recorded,
 * although when it is logged does not affect any of its data (timestamps and
 * other information are captured during construction). This is a base class and
 * can not be used directly. Instead, one of its inheritors will be used
 * depending on the particular metric type being logged.
 */
public abstract class SampledMetricSamplePacket extends MetricSamplePacket implements IPacket {
	
	/** The raw timestamp. */
	private OffsetDateTime rawTimestamp;
	
	/** The raw value. */
	private double rawValue;

	/**
	 * Create an incomplete sampled metric with just the metric packet
	 * 
	 * Before the sampled metric packet is valid, a raw value, counter time stamp,
	 * and counter type will need to be supplied.
	 * 
	 * @param packet The metric this sample is for
	 */
	protected SampledMetricSamplePacket(SampledMetricPacket packet) {
		super(packet);
	}

	/**
	 * Create a complete sampled metric packet
	 * 
	 * <p>
	 * Metrics using a sample type of AverageFraction and DeltaFraction should not
	 * use this method because they require a base value as well as a raw value.
	 * </p>
	 *
	 * @param packet   The metric this sample is for
	 * @param rawValue The raw data value
	 */
	protected SampledMetricSamplePacket(SampledMetricPacket packet, double rawValue) {
		super(packet);
		setRawValue(rawValue);

		// we will fill in the other items if they are missing, so we don't check them
		// for null.
		setRawTimestamp(OffsetDateTime.now()); // we convert to UTC during serialization, we want local time.
	}

	/**
	 * Create a complete sampled metric packet
	 * 
	 * <p>
	 * Metrics using a sample type of AverageFraction and DeltaFraction should not
	 * use this method because they require a base value as well as a raw value.
	 * </p>
	 *
	 * @param packet       The metric this sample is for
	 * @param rawValue     The raw data value
	 * @param rawTimeStamp The exact date and time the raw value was determined
	 */
	protected SampledMetricSamplePacket(SampledMetricPacket packet, double rawValue, OffsetDateTime rawTimeStamp) {
		super(packet);
		setRawValue(rawValue);

		// we will fill in the other items if they are missing, so we don't check them
		// for null.
		setRawTimestamp(rawTimeStamp);
	}

	/**
	 * Compares this sampled metric packet with another. See general CompareTo
	 * documentation for specifics.
	 *
	 * @param other the other
	 * @return the int
	 */
	public final int compareTo(SampledMetricSamplePacket other) {
		// we really are just forwarding to the default comparitor; we are just casting
		// types
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
		return equals(other instanceof SampledMetricSamplePacket ? (SampledMetricSamplePacket) other : null);
	}

	/**
	 * Indicates whether the current object is equal to another object of the same
	 * type.
	 *
	 * @param other An object to compare with this object.
	 * @return true if the current object is equal to the <paramref name="other" />
	 *         parameter; otherwise, false.
	 */
	public final boolean equals(SampledMetricSamplePacket other) {
		// Careful - can be null
		if (other == null) {
			return false; // since we're a live object we can't be equal.
		}

		return (getRawTimestamp().equals(other.getRawTimestamp()) && (getRawValue() == other.getRawValue())
				&& super.equals(other));
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

		myHash ^= (new Double(this.rawValue)).hashCode(); // Fold in hash code for double member RawValue
		myHash ^= this.rawTimestamp.hashCode(); // Fold in hash code for DateTimeOffset member RawTimeStamp

		return myHash;
	}

	/**
	 * The exact date and time the raw value was determined.
	 * 
	 * When doing some calculations it is essential to know when the raw value
	 * became the new value so a difference between it and a subsequent value is
	 * given the proper duration. For example, if you want to know bytes per second
	 * you need to know exactly when the underlying bytes metric was determined,
	 * which may not be when it was recorded to the log file.
	 *
	 * @return the raw timestamp
	 */
	public final OffsetDateTime getRawTimestamp() {
		return this.rawTimestamp;
	}

	/**
	 * Sets the raw timestamp.
	 *
	 * @param value the new raw timestamp
	 */
	protected final void setRawTimestamp(OffsetDateTime value) {
		this.rawTimestamp = value;
	}

	/**
	 * The raw value as it was sampled
	 * 
	 * The raw value generally can't be used directly but instead must be processed
	 * by comparing the raw values of two different samples and their time
	 * difference to determine the effective sampled metric value.
	 *
	 * @return the raw value
	 */
	public final double getRawValue() {
		return this.rawValue;
	}

	/**
	 * Sets the raw value.
	 *
	 * @param value the new raw value
	 */
	protected final void setRawValue(double value) {
		this.rawValue = value;
	}

	/** The Constant SERIALIZATION_VERSION. */
	private static final int SERIALIZATION_VERSION = 1;

	/**
	 * The list of packets that this packet depends on.
	 * 
	 * @return An array of IPackets, or null if there are no dependencies.
	 */
	@Override
	public List<IPacket> getRequiredPackets() {
		return super.getRequiredPackets();
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.monitor.MetricSamplePacket#writePacketDefinition(com.onloupe.core.serialization.PacketDefinition)
	 */
	@Override
	public void writePacketDefinition(PacketDefinition definition) {
		super.writePacketDefinition(definition.getParentIPacket());

		definition.setVersion(SERIALIZATION_VERSION);

		definition.getFields().add("rawTimeStamp", FieldType.DATE_TIME_OFFSET);
		definition.getFields().add("rawValue", FieldType.DOUBLE);
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.monitor.MetricSamplePacket#writeFields(com.onloupe.core.serialization.PacketDefinition, com.onloupe.core.serialization.SerializedPacket)
	 */
	@Override
	public void writeFields(PacketDefinition definition, SerializedPacket packet) {
		super.writeFields(definition.getParentIPacket(), packet.getParentIPacket());

		packet.setField("rawTimeStamp", this.rawTimestamp);
		packet.setField("rawValue", this.rawValue);
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.monitor.MetricSamplePacket#readFields(com.onloupe.core.serialization.PacketDefinition, com.onloupe.core.serialization.SerializedPacket)
	 */
	@Override
	public void readFields(PacketDefinition definition, SerializedPacket packet) {
		throw new UnsupportedOperationException("Deserialization of agent data is not supported");
	}
}