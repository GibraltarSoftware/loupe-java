package com.onloupe.core.metrics;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.onloupe.core.serialization.monitor.IDisplayable;
import com.onloupe.core.serialization.monitor.MetricPacket;
import com.onloupe.core.serialization.monitor.MetricSamplePacket;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.SampleType;
import com.onloupe.model.metric.MetricSampleInterval;

/**
 * A single metric that has been captured. A metric is a single measured value
 * over time.
 * 
 * 
 * To display the data captured for this metric, use Calculate Values to
 * translate the raw captured data into displayable information.
 * 
 */
public abstract class Metric implements IDisplayable {
	private MetricDefinition metricDefinition;
	private MetricPacket packet;

	// these variables are not persisted but just used to manage our own state.
	// used when adding sampled metrics to ensure order.
	private AtomicLong sampleSequence = new AtomicLong();

	public Metric() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Create a new metric with the provided metric definition and metric packet.
	 * 
	 * Most derived classes will provide a more convenient implementation that will
	 * automatically create the correct metric packet instead of the caller having
	 * to first create it. The new metric will automatically be added to the metric
	 * definition's metrics collection.
	 * 
	 * @param definition The definition for this metric
	 * @param packet     The metric packet to use for this metric
	 */
	public Metric(MetricDefinition definition, MetricPacket packet) {
		// verify and store off our input
		if (definition == null) {
			throw new NullPointerException("definition");
		}

		if (packet == null) {
			throw new NullPointerException("packet");
		}

		// one last safety check: The definition and the packet better agree.
		if (!definition.getId().equals(packet.getDefinitionId())) {
			throw new IndexOutOfBoundsException(
					"The provided metric packet has a different definition Id than the provide metric definition.");
		}

		// and now that we know everything isn't null, go ahead and store things off
		this.metricDefinition = definition;
		this.packet = packet;

		// finally, add ourself to the metric definition's metrics collection
		this.metricDefinition.getMetrics().add(this);
	}

	/**
	 * The unique Id of this metric instance. This can reliably be used as a key to
	 * refer to this item.
	 * 
	 * The key can be used to compare the same metric across different instances
	 * (e.g. sessions). This Id is always unique to a particular instance.
	 */
	public final UUID getId() {
		return this.packet.getID();
	}

	/**
	 * The fully qualified name of the metric being captured.
	 * 
	 * The name is for comparing the same metric in different sessions. They will
	 * have the same name but not the same Id.
	 */
	public final String getName() {
		return this.packet.getName();
	}

	/**
	 * A short caption of what the metric tracks, suitable for end-user display.
	 */
	@Override
	public final String getCaption() {
		return this.packet.getCaption();
	}

	/**
	 * A description of what is tracked by this metric, suitable for end-user
	 * display.
	 */
	@Override
	public final String getDescription() {
		return this.packet.getDescription();
	}

	/**
	 * The definition of this metric object.
	 */
	public MetricDefinition getDefinition() {
		return this.metricDefinition;
	}

	/**
	 * The internal metric type of this metric definition
	 */
	public final String getMetricTypeName() {
		return getDefinition().getMetricTypeName();
	}

	/**
	 * The category of this metric for display purposes. Category is the top
	 * displayed hierarchy.
	 */
	public final String getCategoryName() {
		return getDefinition().getCategoryName();
	}

	/**
	 * Gets or sets an instance name for this performance counter.
	 */
	public final String getInstanceName() {
		return this.packet.getInstanceName();
	}

	/**
	 * Indicates whether this is the default metric instance for this metric
	 * definition or not.
	 * 
	 * The default instance has a null instance name. This property is provided as a
	 * convenience to simplify client code so you don't have to distinguish empty
	 * strings or null.
	 */
	public final boolean isDefault() {
		return (TypeUtils.isBlank(this.packet.getInstanceName()));
	}

	/**
	 * The sample type of the metric. Indicates whether the metric represents
	 * discrete events or a continuous value.
	 */
	public final SampleType getSampleType() {
		return getDefinition().getSampleType();
	}

	public String getCounterName() {
		return this.metricDefinition.getCounterName();
	}

	/**
	 * Compare this Metric to another Metric to determine sort order
	 * 
	 * Metric instances are sorted by their Name property.
	 * 
	 * @param other The Metric object to compare this Metric object against
	 * @return An int which is less than zero, equal to zero, or greater than zero
	 *         to reflect whether this Metric should sort as being less-than, equal
	 *         to, or greater-than the other Metric, respectively.
	 */
	public final int compareTo(Metric other) {
		// quick identity comparison based on guid
		if (other.getId().equals(this.packet.getID())) {
			return 0;
		}

		// Now we try to sort by name. We already guard against uniqueness
		int compareResult = this.packet.getName().compareToIgnoreCase(other.getName());

		return compareResult;
	}

	/**
	 * Determines if the provided Metric object is identical to this object.
	 * 
	 * @param other The Metric object to compare this object to
	 * @return True if the objects represent the same data.
	 */
	public final boolean equals(Metric other) {
		if (other == this) {
			return true; // ReferenceEquals means we're the same object, definitely equal.
		}

		// Careful, it could be null; check it without recursion
		if (other == null) {
			return false; // Since we're a live object we can't be equal to a null instance.
		}

		// they are the same if their Guid's match.
		return (getId().equals(other.getId()));
	}

	/**
	 * Determines if the provided object is identical to this object.
	 * 
	 * @param obj The object to compare this object to
	 * @return True if the other object is also a Metric and represents the same
	 *         data.
	 */
	@Override
	public boolean equals(Object obj) {
		Metric otherMetric = obj instanceof Metric ? (Metric) obj : null;

		return equals(otherMetric); // Just have type-specific Equals do the check (it even handles null)
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
		int myHash = getId().hashCode(); // The ID is all that Equals checks!

		return myHash;
	}

	/**
	 * Calculates the offset date from the provided baseline for the specified
	 * interval
	 * 
	 * 
	 * To calculate a backwards offset (the date that is the specified interval
	 * before the baseline) use a negative number of intervals. For example, -1
	 * intervals will give you one interval before the baseline.
	 * 
	 * @param baseline  The date and time to calculate an offset date and time from
	 * @param interval  The interval to add or subtract from the baseline
	 * @param intervals The number of intervals to go forward or (if negative)
	 *                  backwards
	 * @return
	 */
	public final OffsetDateTime calculateOffset(OffsetDateTime baseline, MetricSampleInterval interval, int intervals) {
		OffsetDateTime returnVal; // just so we're initialized with SOMETHING.
		int intervalCount = intervals;

		// since they aren't using shortest, we are going to use the intervals input
		// option which better not be zero or negative.
		if ((intervals == 0) && (interval != MetricSampleInterval.SHORTEST)) {
			throw new IndexOutOfBoundsException(
					"The number of intervals can't be zero if the interval isn't set to Shortest.");
		}

		switch (interval) {
		case DEFAULT: // use how the data was recorded
			if (getDefinition().getInterval() != MetricSampleInterval.DEFAULT) {
				returnVal = calculateOffset(baseline, getDefinition().getInterval(), intervalCount);
			} else {
				// default and ours is default - use second.
				returnVal = calculateOffset(baseline, MetricSampleInterval.SECOND, intervalCount);
			}
			break;
		case SHORTEST:
			// explicitly use the shortest value available, 16 milliseconds
			returnVal = baseline.plusNanos(TimeUnit.MILLISECONDS.toNanos(16)); // interval is ignored in the case of the
																				// "shortest" configuration
			break;
		case MILLISECOND:
			returnVal = baseline.plusNanos(TimeUnit.MILLISECONDS.toNanos(intervalCount));
			break;
		case SECOND:
			returnVal = baseline.plusSeconds(intervalCount);
			break;
		case MINUTE:
			returnVal = baseline.plusMinutes(intervalCount);
			break;
		case HOUR:
			returnVal = baseline.plusHours(intervalCount);
			break;
		case DAY:
			returnVal = baseline.plusDays(intervalCount);
			break;
		case WEEK:
			returnVal = baseline.plusWeeks(intervalCount);
			break;
		case MONTH:
			returnVal = baseline.plusMonths(intervalCount);
			break;
		default:
			throw new IndexOutOfBoundsException("interval");
		}

		return returnVal;
	}

	/**
	 * Calculates the amount we will "pull forward" a future sample by to fit it to
	 * our requested interval.
	 * 
	 * Tolerance allows for us to ignore small variations in exact timestamps for
	 * the purposes of fitting the best data.
	 * 
	 * @param interval
	 * @return
	 */
	public static Duration calculateOffsetTolerance(MetricSampleInterval interval) {
		Duration returnVal;

		switch (interval) {
		case DEFAULT:
		case SHORTEST:
		case MILLISECOND:
			// same as millisecond; we will use 1 clock tick
			returnVal = Duration.ofMillis(1);
			break;
		case SECOND:
			// 10 milliseconds
			returnVal = Duration.ofMillis(10);
			break;
		case MINUTE:
			// 2 seconds
			returnVal = Duration.ofSeconds(2);
			break;
		case HOUR:
			// 1 minute
			returnVal = Duration.ofMinutes(1);
			break;
		case DAY:
			// 30 minutes
			returnVal = Duration.ofMinutes(30);
			break;
		case WEEK:
			// 12 hours
			returnVal = Duration.ofHours(12);
			break;
		case MONTH:
			// two days
			returnVal = Duration.ofDays(2);
			break;
		default:
			throw new IndexOutOfBoundsException("interval");
		}

		return returnVal;
	}

	/**
	 * The underlying packet
	 */
	public MetricPacket getPacket() {
		return this.packet;
	}

	/**
	 * A unique, increasing sequence number each time it's called.
	 * 
	 * This method is thread-safe.
	 * 
	 * @return
	 */
	public final long getSampleSequence() {
		return this.sampleSequence.incrementAndGet();
	}

	/**
	 * Invoked when deserializing a metric sample to allow inheritors to provide
	 * derived implementations
	 * 
	 * If you wish to provide a derived class for metric samples in your derived
	 * metric, use this method to create and return your derived object to support
	 * the deserialization process. This is used during object construction, so
	 * implementations should treat it as a static method.
	 * 
	 * @param packet The metric sample packet being deserialized
	 * @return The metric sample-compatible object.
	 */
	public abstract MetricSample onMetricSampleRead(MetricSamplePacket packet);

}