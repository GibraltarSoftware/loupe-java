package com.onloupe.core.messaging;

import java.time.OffsetDateTime;

import com.onloupe.core.serialization.IPacket;

/**
 * This interface is required to be a publishable packet
 */
public interface IMessengerPacket extends IPacket {
	long getSequence();

	void setSequence(long value);

	OffsetDateTime getTimestamp();

	void setTimestamp(OffsetDateTime value);
}