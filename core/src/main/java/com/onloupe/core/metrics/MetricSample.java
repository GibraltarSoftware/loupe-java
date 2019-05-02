package com.onloupe.core.metrics;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.onloupe.core.logging.Log;
import com.onloupe.core.serialization.monitor.MetricSamplePacket;

/**
 * A single raw sample of a metric.
 * 
 * 
 * Individual samples represent a single data point and may not be directly
 * useful without manipulation. For example, if the underlying metric is
 * intended to be the rate of an event, the individual raw samples will need to
 * be used to calculate the rate instead of being used directly.
 * 
 */
public abstract class MetricSample {
	private Metric metric;
	private MetricSamplePacket metricSamplePacket;

	/**
	 * Create a new metric sample for the provided metric and raw sample packet
	 * 
	 * The metric sample is automatically added to the samples collection of the
	 * provided metric object.
	 * 
	 * @param metric       The specific metric this sample relates to
	 * @param samplePacket The raw sample packet to wrap.
	 */
	public MetricSample(Metric metric, MetricSamplePacket samplePacket) {
		// if we didn't get a metric or metric sample, we're toast.
		if (metric == null) {
			throw new NullPointerException("metric");
		}

		if (samplePacket == null) {
			throw new NullPointerException("samplePacket");
		}

		this.metric = metric;
		this.metricSamplePacket = samplePacket;

		// we may need to correct the sample packet - this is due to the order objects
		// are rehydrated in.
		if (this.metricSamplePacket.getMetricPacket() == null) {
			this.metricSamplePacket.setMetricPacket(this.metric.getPacket());
		}
	}

	/**
	 * The unique id of this sample
	 */
	public UUID getId() {
		return this.metricSamplePacket.getID();
	}

	/**
	 * The metric this sample relates to.
	 */
	public Metric getMetric() {
		return this.metric;
	}

	/**
	 * The increasing sequence number of all sample packets for this metric to be
	 * used as an absolute order sort.
	 */
	public long getSequence() {
		return this.metricSamplePacket.getSequence();
	}

	/**
	 * The exact date and time the metric was captured.
	 */
	public OffsetDateTime getTimestamp() {
		return this.metricSamplePacket.getTimestamp();
	}

	/**
	 * The raw value of the metric.
	 */
	public abstract double getValue();

	/**
	 * Write this sample to the current process log if it hasn't been written
	 * already
	 * 
	 * If the sample has not been written to the log yet, it will be written. If it
	 * has been written, subsequent calls to this method are ignored.
	 */
	public final void write() {
		if (!metricSamplePacket.getPersisted()) {
			try {
				Log.write(this);
			} catch (InterruptedException e) {
				// do nothing
			}
		}
	}

	/**
	 * Compare this metric sample with another to determine if they are the same or
	 * how they should be sorted relative to each other.
	 * 
	 * MetricSample instances are sorted by their Sequence number property.
	 * 
	 * @param other
	 * @return 0 for an exact match, otherwise the relationship between the two for
	 *         sorting.
	 */
	public final int compareTo(MetricSample other) {
		// for performance reasons we've duped the key check here.
		return (new Long(this.metricSamplePacket.getSequence())).compareTo(other.getSequence());
	}

	/**
	 * Determines if the provided object is identical to this object.
	 * 
	 * @param other The object to compare this object to
	 * @return True if the objects represent the same data.
	 */
	public final boolean equals(MetricSample other) {
		// Careful, it could be null; check it without recursion
		if (other == null) {
			return false; // Since we're a live object we can't be equal to a null instance.
		}

		// look at the metric packets to let them make the call
		return this.metricSamplePacket.equals(other.getPacket());
	}

	/**
	 * Determines if the provided object is identical to this object.
	 * 
	 * @param obj The object to compare this object to
	 * @return True if the other object is also a MetricSample and represents the
	 *         same data.
	 */
	@Override
	public boolean equals(Object obj) {
		MetricSample otherMetricSample = obj instanceof MetricSample ? (MetricSample) obj : null;

		return equals(otherMetricSample); // Just have type-specific Equals do the check (it even handles null)
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
	 * @return An int representing the hash code calculated for the contents of this
	 *         object.
	 * 
	 */
	@Override
	public int hashCode() {
		int myHash = this.metricSamplePacket.hashCode(); // Equals just defers to the MetricSamplePacket

		return myHash;
	}

	/**
	 * The raw metric sample packet
	 */
	public MetricSamplePacket getPacket() {
		return this.metricSamplePacket;
	}
}