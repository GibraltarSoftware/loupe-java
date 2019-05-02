package com.onloupe.core.serialization.monitor;

import com.onloupe.core.metrics.MetricDefinition;
import com.onloupe.core.serialization.FieldType;
import com.onloupe.core.serialization.IPacket;
import com.onloupe.core.serialization.PacketDefinition;
import com.onloupe.core.serialization.SerializedPacket;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.SampleType;
import com.onloupe.model.metric.MetricSampleInterval;

import java.util.List;

/**
 * Defines a metric that has been captured. Specific metrics extend this class.
 * Each time a metric is captured, a MetricSample is recorded.
 */
public class MetricDefinitionPacket extends GibraltarCachedPacket implements IPacket, IDisplayable {
	/**
	 * A global default sampling interval for display if no-one attempts to override
	 * it
	 */
	private static final MetricSampleInterval DEFAULT_INTERVAL = MetricSampleInterval.MINUTE;

	// our metric definition data (this gets written out)

	private String name;
	private MetricSampleInterval interval;
	private String metricTypeName;
	private String categoryName;
	private String counterName;
	private boolean persisted;
	private boolean isLive;
	private SampleType sampleType;
	private String caption;
	private String description;

	// internal tracking information (this does NOT get written out)
	private boolean readOnly;

	/**
	 * Create a new metric definition packet.
	 * 
	 * At any one time there should only be one metric definition with a given
	 * combination of metric type, category, and counter name. These values together
	 * are used to correlate metrics between sessions.
	 * 
	 * @param metricTypeName The unique metric type
	 * @param categoryName   The name of the category with which this definition is
	 *                       associated.
	 * @param counterName    The name of the definition within the category.
	 * @param sampleType     The type of data sampling done for this metric.
	 * @param description    A description of what is tracked by this metric,
	 *                       suitable for end-user display.
	 */
	public MetricDefinitionPacket(String metricTypeName, String categoryName, String counterName, SampleType sampleType,
			String description) {
		this(metricTypeName, categoryName, counterName, sampleType);
		setDescription(description);
	}

	/**
	 * Create a new metric definition packet.
	 * 
	 * At any one time there should only be one metric definition with a given
	 * combination of metric type, category, and counter name. These values together
	 * are used to correlate metrics between sessions.
	 * 
	 * @param metricTypeName The unique metric type
	 * @param categoryName   The name of the category with which this definition is
	 *                       associated.
	 * @param counterName    The name of the definition within the category.
	 * @param sampleType     The type of data sampling done for this metric.
	 */
	public MetricDefinitionPacket(String metricTypeName, String categoryName, String counterName,
			SampleType sampleType) {
		super(false);
		// verify our input
		if ((TypeUtils.isBlank(metricTypeName)) || (TypeUtils.isBlank(metricTypeName.trim()))) {
			throw new NullPointerException("metricTypeName");
		}
		if ((TypeUtils.isBlank(categoryName)) || (TypeUtils.isBlank(categoryName.trim()))) {
			throw new NullPointerException("categoryName");
		}
		if ((TypeUtils.isBlank(counterName)) || (TypeUtils.isBlank(counterName.trim()))) {
			throw new NullPointerException("counterName");
		}

		// we require a type, category, and counter name, which is checked by GetKey.
		setMetricTypeName(metricTypeName.trim());
		setCategoryName(categoryName.trim());
		setCounterName(counterName.trim());
		setSampleType(sampleType);

		setName(MetricDefinition.getKey(metricTypeName, categoryName, counterName)); // generate the name
		this.caption = String.format("%1$s - %2$s", categoryName, counterName); // make an
		// attempt to
		// generate a
		// plausible
		// caption

		setInterval(DEFAULT_INTERVAL);

		setPersisted(false); // we haven't been written to the log yet.

		setIsLive(true); // and we're live - if we were from another source, this constructor wouldn't
							// have been called.
	}

	/**
	 * The name of the metric definition being captured.
	 * 
	 * The name is for comparing the same definition in different sessions. They
	 * will have the same name but not the same Id.
	 */
	public final String getName() {
		return this.name;
	}

	private void setName(String value) {
		this.name = value;
	}

	/**
	 * A short display string for this metric definition, suitable for end-user
	 * display.
	 */
	@Override
	public final String getCaption() {
		// if we're null, we're going to use the Name property instead
		if (TypeUtils.isBlank(this.caption)) {
			// we call our own set here.
			setCaption(getName());
		}
		return this.caption;
	}

	public final void setCaption(String value) {
		// We want to get rid of any leading/trailing white space, but make sure they
		// aren't setting us to a null object
		if (TypeUtils.isBlank(value)) {
			this.caption = value;
		} else {
			this.caption = value.trim();
		}
	}

	/**
	 * A description of what is tracked by this metric, suitable for end-user
	 * display.
	 */
	@Override
	public final String getDescription() {
		return this.description;
	}

	public final void setDescription(String value) {
		// We want to get rid of any leading/trailing white space, but make sure they
		// aren't setting us to a null object
		if (TypeUtils.isBlank(value)) {
			this.description = value;
		} else {
			this.description = value.trim();
		}
	}

	/**
	 * The recommended default display interval for graphing.
	 */
	public final MetricSampleInterval getInterval() {
		return this.interval;
	}

	public final void setInterval(MetricSampleInterval value) {
		this.interval = value;
	}

	/**
	 * The internal metric type of this metric definition
	 * 
	 * Metric types distinguish different metric capture libraries from each other,
	 * ensuring that we can correctly correlate the same metric between sessions and
	 * not require category names to be globally unique. If you are creating a new
	 * metric, pick your own metric type that will uniquely idenify your library or
	 * namespace.
	 */
	public final String getMetricTypeName() {
		return this.metricTypeName;
	}

	private void setMetricTypeName(String value) {
		this.metricTypeName = value;
	}

	/**
	 * The category of this metric for display purposes. Category is the top
	 * displayed hierarchy.
	 */
	public final String getCategoryName() {
		return this.categoryName;
	}

	private void setCategoryName(String value) {
		this.categoryName = value;
	}

	/**
	 * The display name of this metric (unique within the category name).
	 */
	public final String getCounterName() {
		return this.counterName;
	}

	private void setCounterName(String value) {
		this.counterName = value;
	}

	/**
	 * Indicates whether the metric packet has been written to the log stream yet.
	 */
	public final boolean getPersisted() {
		return this.persisted;
	}

	private void setPersisted(boolean value) {
		this.persisted = value;
	}

	/**
	 * Indicates if the definition (and all metrics associated with it) are
	 * read-only or can be read/write.
	 * 
	 * If a metric definition is read-only, all metrics associated with it are
	 * read-only, however it's possible for some child objects to be read-only even
	 * if a definition is not. When read only, no new metrics can be added however
	 * display values can be changed.
	 */
	public final boolean isReadOnly() {
		return this.readOnly;
	}

	public final void setIsReadOnly(boolean value) {
		// this is really a latch
		if (value) {
			this.readOnly = true;
		}
	}

	/**
	 * Indicates if the definition is part of the current live metric definitino
	 * collection
	 * 
	 * The same process can be recording metrics and reading metrics from a data
	 * source such as a file. This flag indiciates whether this metric definition is
	 * for playback purposes (it represents previously recorded data) or is part of
	 * the active metric capture capability of the current process.
	 */
	public final boolean isLive() {
		return this.isLive;
	}

	private void setIsLive(boolean value) {
		this.isLive = value;
	}

	/**
	 * The sample type of the metric. Indicates whether the metric represents
	 * discrete events or a continuous value.
	 */
	public final SampleType getSampleType() {
		return this.sampleType;
	}

	private void setSampleType(SampleType value) {
		this.sampleType = value;
	}

	/**
	 * Compares the current object with another object of the same type.
	 * 
	 * @param other The object to compare this object with.
	 * @return Zero if the objects are equal, less than zero if this object is less
	 *         than the other, more than zero if this object is more than the other.
	 */
	public final int compareTo(MetricDefinitionPacket other) {
		// quick identity comparison based on guid
		if (getID().equals(other.getID())) {
			return 0;
		}

		// Now we try to stort by name. We already guard against uniqueness
		int compareResult = getName().compareToIgnoreCase(other.getName());

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
		return equals(other instanceof MetricDefinitionPacket ? (MetricDefinitionPacket) other : null);
	}

	/**
	 * Determines if the provided object is identical to this object.
	 * 
	 * @param other The object to compare this object to
	 * @return True if the objects represent the same data.
	 */
	public final boolean equals(MetricDefinitionPacket other) {
		// Careful - can be null
		if (other == null) {
			return false; // since we're a live object we can't be equal.
		}

		return ((getMetricTypeName().equals(other.getMetricTypeName()))
				&& (getCategoryName().equals(other.getCategoryName()))
				&& (getCounterName().equals(other.getCounterName())) && (getSampleType() == other.getSampleType())
				&& (getCaption().equals(other.getCaption())) && (getDescription().equals(other.getDescription()))
				&& (getInterval() == other.getInterval()) && (super.equals(other)));
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

		if (this.metricTypeName != null) {
			myHash ^= this.metricTypeName.hashCode(); // Fold in hash code for string MetricTypeName
		}
		if (this.categoryName != null) {
			myHash ^= this.categoryName.hashCode(); // Fold in hash code for string CategoryName
		}
		if (this.counterName != null) {
			myHash ^= this.counterName.hashCode(); // Fold in hash code for string CounterName
		}
		if (this.caption != null) {
			myHash ^= this.caption.hashCode(); // Fold in hash code for string Caption
		}
		if (this.description != null) {
			myHash ^= this.description.hashCode(); // Fold in hash code for string Description
		}

		// Note: Name is not checked in Equals, so it can't be in hash, but Name is
		// constructed from other fields anyway
		// Not bothering with SampleType and ...Interval members

		return myHash;
	}

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

	@Override
	public void writePacketDefinition(PacketDefinition definition) {
		super.writePacketDefinition(definition.getParentIPacket());

		definition.setVersion(SERIALIZATION_VERSION);

		definition.getFields().add("MetricTypeName", FieldType.STRING);
		definition.getFields().add("CategoryName", FieldType.STRING);
		definition.getFields().add("CounterName", FieldType.STRING);
		definition.getFields().add("SampleType", FieldType.INT);
		definition.getFields().add("Caption", FieldType.STRING);
		definition.getFields().add("Description", FieldType.STRING);
		definition.getFields().add("Interval", FieldType.INT);
	}

	@Override
	public void writeFields(PacketDefinition definition, SerializedPacket packet) {
		super.writeFields(definition.getParentIPacket(), packet.getParentIPacket());

		packet.setField("MetricTypeName", this.metricTypeName);
		packet.setField("CategoryName", this.categoryName);
		packet.setField("CounterName", this.counterName);
		packet.setField("SampleType", this.sampleType.getValue());
		packet.setField("Caption", this.caption);
		packet.setField("Description", this.description);
		packet.setField("Interval", this.interval.getValue());

		// and now we HAVE persisted
		setPersisted(true);
	}

	@Override
	public void readFields(PacketDefinition definition, SerializedPacket packet) {
		throw new UnsupportedOperationException("Deserialization of agent data is not supported");
	}

}