package com.onloupe.core.serialization.monitor;

import com.onloupe.core.serialization.FieldType;
import com.onloupe.core.serialization.IPacket;
import com.onloupe.core.serialization.PacketDefinition;
import com.onloupe.core.serialization.SerializedPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Base object for all metric sample packets
 * 
 * A metric sample packet is the persistable form of a single metric sample.
 * This is the base class; inherit from either SampledMetricSamplePacket for a
 * sampled metric or EventMetricSamplePacket for an event metric, or a further
 * downstream object as appropriate.
 */
public abstract class MetricSamplePacket extends GibraltarPacket implements IPacket, IDisplayable {
	private MetricPacket metricPacket;
	private UUID id;
	private UUID metricId;

	/**
	 * Create a new metric sample for the provided metric.
	 * 
	 * @param metric The metric this sample applies to
	 */
	protected MetricSamplePacket(MetricPacket metricPacket) {
		if (metricPacket == null) {
			throw new NullPointerException("metricPacket");
		}

		setID(UUID.randomUUID());
		this.metricPacket = metricPacket;

		setPersisted(false);
	}

	/**
	 * The globally unique Id if this metric sample packet.
	 */
	public final UUID getID() {
		return this.id;
	}

	private void setID(UUID value) {
		this.id = value;
	}

	/**
	 * The display caption of the metric this sample is for.
	 */
	@Override
	public String getCaption() {
		return this.metricPacket.getCaption();
	}

	/**
	 * The description of the metric this sample is for.
	 */
	@Override
	public String getDescription() {
		return this.metricPacket.getDescription();
	}

	/**
	 * The unique Id of the metric we are associated with.
	 */
	public final UUID getMetricId() {
		return this.metricId;
	}

	private void setMetricId(UUID value) {
		this.metricId = value;
	}

	/**
	 * The performance counter metric packet this sample is for.
	 */
	public final MetricPacket getMetricPacket() {
		return this.metricPacket;
	}

	public final void setMetricPacket(MetricPacket value) {
		// make sure the packet has the same Guid as our current GUID so the user isn't
		// pulling a funny one
		if (value == null) {
			throw new NullPointerException("value");
		}

		if (getMetricId() != null && (!getMetricId().equals(value.getID()))) {
			throw new IllegalArgumentException(
					"The provided metric packet doesn't have the same ID as the metric packet ID already stored. This indicates the data would be inconsistent.");
		}

		this.metricPacket = value;

		if (getMetricId() != null) {
			// we are getting the packet set and our ID is empty (shouldn't actually happen,
			// but we've guarded for that, so make it work)
			setMetricId(this.metricPacket.getID());
		}
	}

	/**
	 * Indicates whether the metric packet has been written to the log stream yet.
	 */
	private boolean persisted;

	public final boolean getPersisted() {
		return this.persisted;
	}

	private void setPersisted(boolean value) {
		this.persisted = value;
	}

	@Override
	public String toString() {
		return String.format("%1$tc: %2$s", getTimestamp(), getCaption());
	}

	public final int compareTo(MetricSamplePacket other) {
		// First do a quick match on Guid. this is the only case we want to return zero
		// (an exact match)
		if (getID().equals(other.getID())) {
			return 0;
		}

		// now we want to sort by our nice increasing sequence #
		int compareResult = Long.compare(getSequence(), other.getSequence());

		assert compareResult != 0; // no way we should ever get an equal at this point.

		return compareResult;
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
		return equals(other instanceof MetricSamplePacket ? (MetricSamplePacket) other : null);
	}

	/**
	 * Determines if the provided object is identical to this object.
	 * 
	 * @param other The object to compare this object to
	 * @return True if the objects represent the same data.
	 */
	public final boolean equals(MetricSamplePacket other) {
		// Careful - can be null
		if (other == null) {
			return false; // since we're a live object we can't be equal.
		}

		return ((getID().equals(other.getID()))
				&& (((this.metricPacket == null) && (other.getMetricPacket() == null))
						|| (((this.metricPacket != null) && (other.getMetricPacket() != null))
								&& (this.metricPacket.getID().equals(other.getMetricPacket().getID()))))
				&& (super.equals(other)));
		// Bug: (?) Should Equals also be comparing on MetricID field?
		// Note: I wonder if we should be digging into MetricPacket.ID fields directly
		// like this
		// or if it would be better to invoke _MetricPacket.Equals(other.MetricPacket)
		// (but less efficient?)
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

		myHash ^= this.id.hashCode(); // Fold in hash code for GUID field ID
		if (this.metricPacket != null) {
			myHash ^= this.metricPacket.hashCode(); // Fold in hash code for the MetricPacket member
		}

		// Other fields aren't used in Equals, so we must not use them in hash code
		// calculation

		return myHash;
	}

	// We need to explicitly implement this interface because we don't want to
	// override the IPacket implementation,
	// we want to have our own distinct implementatino because the packet
	// serialization methods know to recurse object
	// structures looking for the interface.

	private static final int SERIALIZATION_VERSION = 1;

	/**
	 * The list of packets that this packet depends on.
	 * 
	 * @return An array of IPackets, or null if there are no dependencies.
	 */
	@Override
	public List<IPacket> getRequiredPackets() {
		List<IPacket> requiredPackets = super.getRequiredPackets();

		// we depend on our metric
		assert this.metricPacket != null;
		requiredPackets.add(this.metricPacket);
		return requiredPackets;
	}

	@Override
	public void writePacketDefinition(PacketDefinition definition) {
		super.writePacketDefinition(definition.getParentIPacket());

		definition.setVersion(SERIALIZATION_VERSION);

		definition.getFields().add("Id", FieldType.GUID);
		definition.getFields().add("metricPacketId", FieldType.GUID);
	}

	@Override
	public void writeFields(PacketDefinition definition, SerializedPacket packet) {
		super.writeFields(definition.getParentIPacket(), packet.getParentIPacket());

		packet.setField("Id", this.id);
		packet.setField("metricPacketId", this.metricPacket.getID());

		// and now we HAVE persisted
		setPersisted(true);

	}

	@Override
	public void readFields(PacketDefinition definition, SerializedPacket packet) {
		throw new UnsupportedOperationException("Deserialization of agent data is not supported");
	}

}