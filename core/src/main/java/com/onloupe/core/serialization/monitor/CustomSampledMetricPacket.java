package com.onloupe.core.serialization.monitor;

import com.onloupe.core.metrics.Metric;
import com.onloupe.core.metrics.MetricDefinition;

/**
 * The serializeable representation of a custom sampled metric
 */
public class CustomSampledMetricPacket extends SampledMetricPacket
		implements IPacketObjectFactory<Metric, MetricDefinition> {
	/**
	 * Create a new custom sampled metric packet for the provided metric definition
	 * and a specific instance.
	 * 
	 * @param metricDefinitionPacket The metric definition packet that defines this
	 *                               metric
	 * @param instanceName           The unique instance name of this metric or null
	 *                               for the default instance.
	 */
	public CustomSampledMetricPacket(CustomSampledMetricDefinitionPacket metricDefinitionPacket, String instanceName) {
		super(metricDefinitionPacket, instanceName);
	}

	public final int compareTo(CustomSampledMetricPacket other) {
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
		return equals(other instanceof CustomSampledMetricPacket ? (CustomSampledMetricPacket) other : null);
	}

	/**
	 * Determines if the provided object is identical to this object.
	 * 
	 * @param other The object to compare this object to
	 * @return True if the objects represent the same data.
	 */
	public final boolean equals(CustomSampledMetricPacket other) {
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

}