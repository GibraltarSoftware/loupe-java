package com.onloupe.core.serialization;

import java.time.LocalDateTime;

// TODO: Auto-generated Javadoc
/**
 * Inter-field state information tracked while serializing or deserializing a packet stream
 *
 * To create the most efficient stream possible some data fields are serialized using relative values
 * or otherwise require prior state knowledge to serialize or deserialize.  This class tracks this information
 * for the life of a packet stream.
 */
public class PacketStreamState {
	
	/** The reference time. */
	private LocalDateTime referenceTime;

	/**
	 * Get the current reference time set in the reader for relative time operations (may be null).
	 *
	 * @return the reference time
	 */
	public LocalDateTime getReferenceTime() { return this.referenceTime; }

	/**
	 * Update the reference timestamp used for relative time operations.
	 *
	 * @param value the new reference time
	 */
	public void setReferenceTime(LocalDateTime value) { this.referenceTime = value; }
}
