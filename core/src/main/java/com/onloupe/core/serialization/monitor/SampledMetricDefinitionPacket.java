package com.onloupe.core.serialization.monitor;

import com.onloupe.core.serialization.FieldType;
import com.onloupe.core.serialization.IPacket;
import com.onloupe.core.serialization.PacketDefinition;
import com.onloupe.core.serialization.SerializedPacket;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.SampleType;

import java.util.List;

/**
 * A serializable sampled metric definition. Provides metadata for metrics based
 * on sampled values.
 */
public abstract class SampledMetricDefinitionPacket extends MetricDefinitionPacket implements IPacket {
	private String unitCaption;

	/**
	 * Base implementation for creating a sampled metric definition packet
	 * 
	 * @param metricTypeName The unique metric type
	 * @param categoryName   The name of the category with which this definition is
	 *                       associated.
	 * @param counterName    The name of the definition within the category.
	 */
	protected SampledMetricDefinitionPacket(String metricTypeName, String categoryName, String counterName) {
		super(metricTypeName, categoryName, counterName, SampleType.SAMPLED);
	}

	/**
	 * Base implementation for creating a sampled metric definition packet
	 * 
	 * @param metricTypeName The unique metric type
	 * @param categoryName   The name of the category with which this definition is
	 *                       associated.
	 * @param counterName    The name of the definition within the category.
	 * @param unitCaption    The display caption for the calculated values captured
	 *                       under this metric.
	 * @param description    A description of what is tracked by this metric,
	 *                       suitable for end-user display.
	 */
	protected SampledMetricDefinitionPacket(String metricTypeName, String categoryName, String counterName,
			String unitCaption, String description) {
		super(metricTypeName, categoryName, counterName, SampleType.SAMPLED, description);
		setUnitCaption(unitCaption);
	}

	public final int compareTo(SampledMetricDefinitionPacket other) {
		// we just gateway to our base object.
		return super.compareTo(other);
	}

	/**
	 * The display caption for the calculated values captured under this metric.
	 */
	public final String getUnitCaption() {
		if (TypeUtils.isBlank(this.unitCaption)) {
			// A little odd; we're actually going to route this to our setter..
			setUnitCaption(onUnitCaptionGenerate());
		}

		return this.unitCaption;
	}

	public final void setUnitCaption(String value) {
		// We want to get rid of any leading/trailing white space, but make sure they
		// aren't setting us to a null object
		if (TypeUtils.isBlank(value)) {
			this.unitCaption = value;
		} else {
			this.unitCaption = value.trim();
		}
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
		return equals(other instanceof SampledMetricDefinitionPacket ? (SampledMetricDefinitionPacket) other : null);
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
	public final boolean equals(SampledMetricDefinitionPacket other) {
		// Careful - can be null
		if (other == null) {
			return false; // since we're a live object we can't be equal.
		}

		return ((getUnitCaption().equals(other.getUnitCaption())) && (super.equals(other)));
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

		if (this.unitCaption != null) {
			myHash ^= this.unitCaption.hashCode(); // Fold in hash code for string UnitCaption
		}

		return myHash;
	}

	/**
	 * Inheritors will need to implement this to calculate a unit caption when
	 * requested.
	 * 
	 * @return The caption to display for the units of value.
	 */
	protected abstract String onUnitCaptionGenerate();

	private static final int SERIALIZATION_VERSION = 1;

	/**
	 * The list of packets that this packet depends on.
	 * 
	 * @return An array of IPackets, or null if there are no dependencies.
	 */
	@Override
	public final List<IPacket> getRequiredPackets() {
		return super.getRequiredPackets();
	}

	@Override
	public void writePacketDefinition(PacketDefinition definition) {
		super.writePacketDefinition(definition.getParentIPacket());

		definition.setVersion(SERIALIZATION_VERSION);

		definition.getFields().add("unitCaption", FieldType.STRING);
	}

	@Override
	public void writeFields(PacketDefinition definition, SerializedPacket packet) {
		super.writeFields(definition.getParentIPacket(), packet.getParentIPacket());

		packet.setField("unitCaption", this.unitCaption);
	}

	@Override
	public void readFields(PacketDefinition definition, SerializedPacket packet) {
		throw new UnsupportedOperationException("Deserialization of agent data is not supported");
	}
}