package com.onloupe.core.metrics;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * A single display-ready metric value.
 * 
 * 
 * This is the complementary object to a Metric Sample. A Sample is a raw value
 * that may require multiple samples to determine a display ready value.
 * 
 */
public class MetricValue {
	private long sequence;
	private double value;
	private OffsetDateTime timeStamp;

	/**
	 * The exact date and time the metric was captured.
	 */
	public OffsetDateTime getTimestamp() {
		return this.timeStamp;
	}

	/**
	 * The date and time the metric was captured in the effective time zone.
	 */
	public final LocalDateTime getLocalTimestamp() {
		return getTimestamp().toLocalDateTime();
	}

	/**
	 * The value of the metric.
	 */
	public final double getValue() {
		return this.value;
	}

	/**
	 * The value of the metric multiplied by 100 to handle raw percentage display
	 * 
	 * This value is scaled by 100 even if the underlying metric is not a percentage
	 */
	public final double getPercentageValue() {
		return this.value * 100;
	}

	/**
	 * The increasing sequence number of all sample packets for this metric to be
	 * used as an absolute order sort.
	 */
	public final long getSequence() {
		return this.sequence;
	}

	/**
	 * Compare this metric value to another for the purpose of sorting them in time.
	 * 
	 * MetricValue instances are sorted by their Sequence number property.
	 * 
	 * @param other The MetricValue object to compare this object to.
	 * @return An int which is less than zero, equal to zero, or greater than zero
	 *         to reflect whether this MetricValue should sort as being less-than,
	 *         equal to, or greater-than the other MetricValue, respectively.
	 */
	public final int compareTo(MetricValue other) {
		// we are all about the sequence number baby!
		return (new Long(this.sequence)).compareTo(other.getSequence());
	}

	/**
	 * Determines if the provided MetricValue object is identical to this object.
	 * 
	 * @param other The MetricValue object to compare this object to.
	 * @return True if the Metric Value objects represent the same data.
	 */
	public final boolean equals(MetricValue other) {
		// Careful, it could be null; check it without recursion
		if (other == null) {
			return false; // Since we're a live object we can't be equal to a null instance.
		}

		// they are equal if they have the same sequence and value
		return ((getValue() == other.getValue()) && (getSequence() == other.getSequence()));
	}

	/**
	 * Determines if the provided object is identical to this object.
	 * 
	 * @param obj The object to compare this object to
	 * @return True if the other object is also a MetricValue and represents the
	 *         same data.
	 */
	@Override
	public boolean equals(Object obj) {
		MetricValue otherMetricValue = obj instanceof MetricValue ? (MetricValue) obj : null;

		return equals(otherMetricValue); // Just have type-specific Equals do the check (it even handles null)
	}

}