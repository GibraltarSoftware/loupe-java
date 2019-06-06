package com.onloupe.core.serialization.monitor;

import com.onloupe.core.metrics.Metric;
import com.onloupe.core.metrics.MetricDefinition;
import com.onloupe.core.serialization.FieldType;
import com.onloupe.core.serialization.IPacket;
import com.onloupe.core.serialization.PacketDefinition;
import com.onloupe.core.serialization.SerializedPacket;
import com.onloupe.core.util.TypeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Defines a metric that has been captured. Specific metrics extend this class.
 * Each time a metric is captured, a MetricSample is recorded.
 */
public abstract class MetricPacket extends GibraltarCachedPacket
		implements IPacket, IPacketObjectFactory<Metric, MetricDefinition>, IDisplayable {
	
	/** The definition id. */
	// our metric definition data (this gets written out)
	private UUID definitionId;
	
	/** The instance name. */
	private String instanceName;

	/** The metric definition packet. */
	// internal tracking information (this does NOT get written out)
	private MetricDefinitionPacket metricDefinitionPacket; // we just persist the ID when we get around to this.
	
	/** The name. */
	private String name;
	
	/** The persisted. */
	private boolean persisted;

	/**
	 * Create a new metric packet with the specified unique name.
	 * 
	 * At any one time there should only be one metric with a given name. This name
	 * is used to correlate metrics between sessions.
	 * 
	 * @param metricDefinitionPacket The metric definition to create a metric
	 *                               instance for.
	 * @param instanceName           The name of the metric instance, or an empty
	 *                               string ("") to create a default instance.
	 */
	protected MetricPacket(MetricDefinitionPacket metricDefinitionPacket, String instanceName) {
		super(false);
		// verify our input. instance name can be null or an empty string; we'll
		// coalesce all those cases to null
		if (metricDefinitionPacket == null) {
			throw new NullPointerException("metricDefinitionPacket");
		}

		setDefinitionId(metricDefinitionPacket.getID()); // it's really important we set this and not rely on people
															// just picking up the metric packet for some of our other
															// code

		setInstanceName(TypeUtils.trimToNull(instanceName));

		// process setting our definition through the common routine. This has to be
		// AFTER we set our definition ID and instance name above.
		setDefinitionPacket(metricDefinitionPacket);

		setPersisted(false); // we haven't been written to the log yet.
	}

	/**
	 * The unique Id of the metric definition.
	 *
	 * @return the definition id
	 */
	public final UUID getDefinitionId() {
		return this.definitionId;
	}

	/**
	 * Sets the definition id.
	 *
	 * @param value the new definition id
	 */
	private void setDefinitionId(UUID value) {
		this.definitionId = value;
	}

	/**
	 * The name of the metric being captured.
	 * 
	 * The name is for comparing the same metric in different sessions. They will
	 * have the same name but not the same Id.
	 *
	 * @return the name
	 */
	public final String getName() {
		return this.name;
	}

	/**
	 * Sets the name.
	 *
	 * @param value the new name
	 */
	private void setName(String value) {
		this.name = value;
	}

	/**
	 * A short display string for this metric packet.
	 *
	 * @return the caption
	 */
	@Override
	public String getCaption() {
		// if our caller didn't override us, we're going to do a best effort caption
		// generation.
		String caption;

		// If there is no instance name, just use the name of the definition (this is
		// common - default instances won't have a name)
		if (TypeUtils.isBlank(getInstanceName())) {
			caption = this.metricDefinitionPacket.getCaption();
		} else {
			// If there is an instance name, prepend the caption definition if available.
			if (TypeUtils.isBlank(this.metricDefinitionPacket.getCaption())) {
				caption = getInstanceName();
			} else {
				caption = String.format("%1$s - %2$s", this.metricDefinitionPacket.getCaption(), getInstanceName());
			}
		}

		return caption;
	}

	/**
	 * The metric definition's description.
	 *
	 * @return the description
	 */
	@Override
	public String getDescription() {
		return this.metricDefinitionPacket.getDescription();
	}

	/**
	 * The metric instance name (unique within the counter name). May be null or
	 * empty if no instance name is required.
	 *
	 * @return the instance name
	 */
	public final String getInstanceName() {
		return this.instanceName;
	}

	/**
	 * Sets the instance name.
	 *
	 * @param value the new instance name
	 */
	private void setInstanceName(String value) {
		this.instanceName = value;
	}

	/**
	 * Indicates whether the metric packet has been written to the log stream yet.
	 *
	 * @return the persisted
	 */
	public final boolean getPersisted() {
		return this.persisted;
	}

	/**
	 * Sets the persisted.
	 *
	 * @param value the new persisted
	 */
	private void setPersisted(boolean value) {
		this.persisted = value;
	}

	/**
	 * Compare this object to another to determine sort order.
	 *
	 * @param other the other
	 * @return the int
	 */
	public final int compareTo(MetricPacket other) {
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
	 * @param other An object to compare with this object.
	 * @return true if the current object is equal to the 
	 *         parameter; otherwise, false.
	 */
	@Override
	public boolean equals(Object other) {
		// use our type-specific override
		return equals(other instanceof MetricPacket ? (MetricPacket) other : null);
	}

	/**
	 * Determines if the provided object is identical to this object.
	 * 
	 * @param other The object to compare this object to
	 * @return True if the objects represent the same data.
	 */
	public final boolean equals(MetricPacket other) {
		// Careful - can be null
		if (other == null) {
			return false; // since we're a live object we can't be equal.
		}

		return ((getInstanceName().equals(other.getInstanceName()))
				&& (getDefinitionId().equals(other.getDefinitionId())) && (super.equals(other)));
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

		if (this.instanceName != null) {
			myHash ^= this.instanceName.hashCode(); // Fold in hash code for InstanceName
		}
		myHash ^= this.definitionId.hashCode(); // Fold in hash code for DefinitionID

		return myHash;
	}

	/**
	 * The current metric definition packet. Setting to null is not allowed.
	 *
	 * @return the definition packet
	 */
	public final MetricDefinitionPacket getDefinitionPacket() {
		return this.metricDefinitionPacket;
	}

	/**
	 * Sets the definition packet.
	 *
	 * @param value the new definition packet
	 */
	private void setDefinitionPacket(MetricDefinitionPacket value) {
		if (value == null) {
			throw new NullPointerException("value");
		}

		// we have to already have a definition ID, and it better match
		if (!getDefinitionId().equals(value.getID())) {
			throw new IndexOutOfBoundsException(
					"The definition packet object provided is not the same as the definition of this metric packet based on comparing Id's");
		}

		this.metricDefinitionPacket = value;
		setName(MetricDefinition.getKey(this.metricDefinitionPacket.getMetricTypeName(),
				this.metricDefinitionPacket.getCategoryName(), this.metricDefinitionPacket.getCounterName(),
				getInstanceName())); // generate the name
	}

	// We need to explicitly implement this interface because we don't want to
	// override the IPacket implementation,
	// we want to have our own distinct implementation because the packet
	// serialization methods know to recurse object
	// structures looking for the interface.

	/** The Constant SERIALIZATION_VERSION. */
	private static final int SERIALIZATION_VERSION = 1;

	/**
	 * The list of packets that this packet depends on.
	 * 
	 * @return An array of IPackets, or null if there are no dependencies.
	 */
	@Override
	public List<IPacket> getRequiredPackets() {
		List<IPacket> requiredPackets = super.getRequiredPackets();

		// a metric depends on its metric definition
		assert this.metricDefinitionPacket != null;
		requiredPackets.add(this.metricDefinitionPacket);
		return requiredPackets;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.monitor.GibraltarCachedPacket#writePacketDefinition(com.onloupe.core.serialization.PacketDefinition)
	 */
	@Override
	public void writePacketDefinition(PacketDefinition definition) {
		super.writePacketDefinition(definition.getParentIPacket());

		definition.setVersion(SERIALIZATION_VERSION);

		definition.getFields().add("instanceName", FieldType.STRING);
		definition.getFields().add("definitionId", FieldType.GUID);
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.monitor.GibraltarCachedPacket#writeFields(com.onloupe.core.serialization.PacketDefinition, com.onloupe.core.serialization.SerializedPacket)
	 */
	@Override
	public void writeFields(PacketDefinition definition, SerializedPacket packet) {
		super.writeFields(definition.getParentIPacket(), packet.getParentIPacket());

		packet.setField("instanceName", this.instanceName);
		packet.setField("definitionId", this.definitionId);

		// and now we HAVE persisted
		setPersisted(true);
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.monitor.GibraltarCachedPacket#readFields(com.onloupe.core.serialization.PacketDefinition, com.onloupe.core.serialization.SerializedPacket)
	 */
	@Override
	public void readFields(PacketDefinition definition, SerializedPacket packet) {
		throw new UnsupportedOperationException("Deserialization of agent data is not supported");
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.monitor.IPacketObjectFactory#getDataObject(java.lang.Object)
	 */
	@Override
	public Metric getDataObject(MetricDefinition optionalParent) {
		// we don't implement this; our derived class always should.
		throw new UnsupportedOperationException();
	}
}