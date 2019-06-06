package com.onloupe.core.messaging.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.OffsetDateTime;

import com.onloupe.core.data.BinarySerializer;
import com.onloupe.core.data.SessionHeader;
import com.onloupe.model.system.Version;


/**
 * Contains the session summary information for a remote session.
 */
public class SessionHeaderMessage extends NetworkMessage {
	
	/** The session header. */
	private SessionHeader sessionHeader;
	
	/** The timestamp. */
	private OffsetDateTime timestamp;
	
	/** The clock drift. */
	private Duration clockDrift = Duration.ZERO;

	/**
	 * Instantiates a new session header message.
	 */
	public SessionHeaderMessage() {
		setTypeCode(NetworkMessageTypeCode.SESSION_HEADER);
		setVersion(new Version(1, 0));
	}

	/**
	 * Create a new session header message without any clock drift.
	 *
	 * @param sessionHeader the session header
	 */
	public SessionHeaderMessage(SessionHeader sessionHeader) {
		this(sessionHeader, Duration.ZERO);
	}

	/**
	 * Create a new session header message with the specified clock drift.
	 *
	 * @param sessionHeader the session header
	 * @param clockDrift the clock drift
	 */
	public SessionHeaderMessage(SessionHeader sessionHeader, Duration clockDrift) {
		this();
		this.sessionHeader = sessionHeader;
		this.clockDrift = clockDrift;
		this.timestamp = OffsetDateTime.now();
	}

	/**
	 * The current session header.
	 *
	 * @return the session header
	 */
	public final SessionHeader getSessionHeader() {
		return this.sessionHeader;
	}

	/**
	 * The timestamp of when the message was generated.
	 *
	 * @return the timestamp
	 */
	public final OffsetDateTime getTimestamp() {
		return this.timestamp;
	}

	/**
	 * The total amount of.
	 *
	 * @return the clock drift
	 */
	public final Duration getClockDrift() {
		return this.clockDrift;
	}

	/**
	 * Read packet data from the stream.
	 *
	 * @param stream the stream
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	protected void onRead(InputStream stream) throws IOException {
		byte[] bytes = new byte[getContentLength()];
		stream.read(bytes, 0, getContentLength());
		ByteBuffer buffer = ByteBuffer.wrap(bytes);

		this.timestamp = BinarySerializer.deserializeOffsetDateTimeValue(buffer);
		this.clockDrift = BinarySerializer.deserializeDuration(buffer);
		this.sessionHeader = new SessionHeader(buffer, buffer.remaining());
	}

	/**
	 * Write the packet to the stream.
	 *
	 * @param stream the stream
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	protected void onWrite(OutputStream stream) throws IOException {
		// this will automatically figure out its length...
		stream.write(BinarySerializer.serializeValue(this.timestamp));
		stream.write(BinarySerializer.serializeValue(this.clockDrift));
		byte[] rawData = this.sessionHeader.rawData();
		stream.write(rawData, 0, rawData.length);
	}
}