package com.onloupe.core.serialization.monitor;


/**
 * A serializable sampled metric definition. Provides metadata for metrics based
 * on sampled values.
 */
public abstract class SampledMetricPacket extends MetricPacket {
	
	/**
	 * Instantiates a new sampled metric packet.
	 *
	 * @param metricDefinitionPacket the metric definition packet
	 * @param instanceName the instance name
	 */
	protected SampledMetricPacket(SampledMetricDefinitionPacket metricDefinitionPacket, String instanceName) {
		super(metricDefinitionPacket, instanceName);
	}

	/**
	 * Compare to.
	 *
	 * @param other the other
	 * @return the int
	 */
	public final int compareTo(SampledMetricPacket other) {
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
		return equals(other instanceof SampledMetricPacket ? (SampledMetricPacket) other : null);
	}

	/**
	 * Indicates whether the current object is equal to another object of the same
	 * type.
	 *
	 * @param other An object to compare with this object.
	 * @return true if the current object is equal to the 
	 *         parameter; otherwise, false.
	 */
	public final boolean equals(SampledMetricPacket other) {
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
}