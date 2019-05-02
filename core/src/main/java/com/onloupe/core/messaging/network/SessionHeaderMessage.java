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
 * Contains the session summary information for a remote session
 */
public class SessionHeaderMessage extends NetworkMessage {
	private SessionHeader sessionHeader;
	private OffsetDateTime timestamp;
	private Duration clockDrift = Duration.ZERO;

	public SessionHeaderMessage() {
		setTypeCode(NetworkMessageTypeCode.SESSION_HEADER);
		setVersion(new Version(1, 0));
	}

	/**
	 * Create a new session header message without any clock drift
	 * 
	 * @param sessionHeader
	 */
	public SessionHeaderMessage(SessionHeader sessionHeader) {
		this(sessionHeader, Duration.ZERO);
	}

	/**
	 * Create a new session header message with the specified clock drift
	 * 
	 * @param sessionHeader
	 * @param clockDrift
	 */
	public SessionHeaderMessage(SessionHeader sessionHeader, Duration clockDrift) {
		this();
		this.sessionHeader = sessionHeader;
		this.clockDrift = clockDrift;
		this.timestamp = OffsetDateTime.now();
	}

	/**
	 * The current session header
	 */
	public final SessionHeader getSessionHeader() {
		return this.sessionHeader;
	}

	/**
	 * The timestamp of when the message was generated
	 */
	public final OffsetDateTime getTimestamp() {
		return this.timestamp;
	}

	/**
	 * The total amount of
	 */
	public final Duration getClockDrift() {
		return this.clockDrift;
	}

	/**
	 * Read packet data from the stream
	 * 
	 * @throws IOException
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
	 * Write the packet to the stream
	 * 
	 * @throws IOException
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