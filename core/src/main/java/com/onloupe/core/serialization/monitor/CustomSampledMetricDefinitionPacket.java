package com.onloupe.core.serialization.monitor;

import com.onloupe.core.metrics.MetricSampleType;
import com.onloupe.core.serialization.FieldType;
import com.onloupe.core.serialization.IPacket;
import com.onloupe.core.serialization.PacketDefinition;
import com.onloupe.core.serialization.SerializedPacket;

/**
 * The serializeable representation of a custom sampled metric
 */
public class CustomSampledMetricDefinitionPacket extends SampledMetricDefinitionPacket implements IPacket {
	private MetricSampleType metricSampleType;

	/**
	 * Create a new custom sampled metric definition packet from the provided
	 * information
	 * 
	 * Definition packets are the lightweight internals used for persistence.
	 * 
	 * @param metricTypeName   The unique metric type
	 * @param categoryName     The name of the category with which this definition
	 *                         is associated.
	 * @param counterName      The name of the definition within the category.
	 * @param metricSampleType The specific unit representation of the data being
	 *                         captured for this metric
	 */
	public CustomSampledMetricDefinitionPacket(String metricTypeName, String categoryName, String counterName,
			MetricSampleType metricSampleType) {
		super(metricTypeName, categoryName, counterName);
		this.metricSampleType = metricSampleType;
	}

	/**
	 * Create a new custom sampled metric definition packet from the provided
	 * information
	 * 
	 * Definition packets are the lightweight internals used for persistence.
	 * 
	 * @param metricTypeName   The unique metric type
	 * @param categoryName     The name of the category with which this definition
	 *                         is associated.
	 * @param counterName      The name of the definition within the category.
	 * @param metricSampleType The specific unit representation of the data being
	 *                         captured for this metric
	 * @param unitCaption      The display caption for the calculated values
	 *                         captured under this metric.
	 * @param description      A description of what is tracked by this metric,
	 *                         suitable for end-user display.
	 */
	public CustomSampledMetricDefinitionPacket(String metricTypeName, String categoryName, String counterName,
			MetricSampleType metricSampleType, String unitCaption, String description) {
		super(metricTypeName, categoryName, counterName, unitCaption, description);
		this.metricSampleType = metricSampleType;
	}

	/**
	 * Compares this object to the provided comparison object
	 * 
	 * @param other
	 * @return Zero if objects are the same object, -1 or 1 to indicate relative
	 *         order (see CompareTo for more information)
	 */
	public final int compareTo(CustomSampledMetricDefinitionPacket other) {
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
		return equals(other instanceof CustomSampledMetricDefinitionPacket ? (CustomSampledMetricDefinitionPacket) other
				: null);
	}

	/**
	 * Determines if the provided object is identical to this object.
	 * 
	 * @param other The object to compare this object to
	 * @return True if the objects represent the same data.
	 */
	public final boolean equals(CustomSampledMetricDefinitionPacket other) {
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
	 * The intended method of interpreting the sampled counter value.
	 * 
	 * The counter type determines what math needs to be run to determine the
	 * correct value when comparing two samples.
	 */
	public final MetricSampleType getMetricSampleType() {
		return this.metricSampleType;
	}

	protected final void setMetricSampleType(MetricSampleType value) {
		this.metricSampleType = value;
	}

	/**
	 * Generate a display caption for the supplied sample metric type
	 * 
	 * @param metricSampleType The sample metric type to make a caption for
	 * @return An end-user display caption
	 */
	public static String sampledMetricTypeCaption(MetricSampleType metricSampleType) {
		String returnVal;

		switch (metricSampleType) {
		case TOTAL_COUNT:
			returnVal = "Count of Items";
			break;
		case TOTAL_FRACTION:
			returnVal = "Percentage";
			break;
		case INCREMENTAL_COUNT:
			returnVal = "Count of Items";
			break;
		case INCREMENTAL_FRACTION:
			returnVal = "Percentage";
			break;
		case RAW_COUNT:
			returnVal = "Count of Items";
			break;
		case RAW_FRACTION:
			returnVal = "Percentage";
			break;
		default:
			throw new IndexOutOfBoundsException("metricSampleType");
		}

		return returnVal;
	}

	/**
	 * Calculates the unit caption as required by the base object.
	 * 
	 * @return The caption to display for the units of value.
	 */
	@Override
	protected String onUnitCaptionGenerate() {
		// make a string description of our counter type
		return sampledMetricTypeCaption(getMetricSampleType());
	}

	private static final int SERIALIZATION_VERSION = 1;

	@Override
	public void writePacketDefinition(PacketDefinition definition) {
		super.writePacketDefinition(definition.getParentIPacket());

		definition.setVersion(SERIALIZATION_VERSION);

		definition.getFields().add("metricSampleType", FieldType.INT);
	}

	@Override
	public final void writeFields(PacketDefinition definition, SerializedPacket packet) {
		super.writeFields(definition.getParentIPacket(), packet.getParentIPacket());

		packet.setField("metricSampleType", this.metricSampleType.getValue());
	}

	@Override
	public final void readFields(PacketDefinition definition, SerializedPacket packet) {
		throw new UnsupportedOperationException("Deserialization of agent data is not supported");
	}

}