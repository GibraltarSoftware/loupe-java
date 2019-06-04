package com.onloupe.core.messaging;

import java.time.OffsetDateTime;

import com.onloupe.core.serialization.IPacket;

// TODO: Auto-generated Javadoc
/**
 * This interface is required to be a publishable packet.
 */
public interface IMessengerPacket extends IPacket {
	
	/**
	 * Gets the sequence.
	 *
	 * @return the sequence
	 */
	long getSequence();

	/**
	 * Sets the sequence.
	 *
	 * @param value the new sequence
	 */
	void setSequence(long value);

	/**
	 * Gets the timestamp.
	 *
	 * @return the timestamp
	 */
	OffsetDateTime getTimestamp();

	/**
	 * Sets the timestamp.
	 *
	 * @param value the new timestamp
	 */
	void setTimestamp(OffsetDateTime value);
}