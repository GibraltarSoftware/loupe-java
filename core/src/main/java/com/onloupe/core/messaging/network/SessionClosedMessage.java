package com.onloupe.core.messaging.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.onloupe.core.data.BinarySerializer;
import com.onloupe.model.system.Version;

/**
 * Indicates that the identified session has been closed.
 */
public class SessionClosedMessage extends NetworkMessage {
	private UUID sessionId;

	public SessionClosedMessage() {
		setTypeCode(NetworkMessageTypeCode.SESSION_CLOSED);
		setVersion(new Version(1, 0));
	}

	/**
	 * Create a new session closed message for the specified session id
	 * 
	 * @param sessionId
	 */
	public SessionClosedMessage(UUID sessionId) {
		this();
		setSessionId(sessionId);
	}

	public final UUID getSessionId() {
		return this.sessionId;
	}

	private void setSessionId(UUID value) {
		this.sessionId = value;
	}

	/**
	 * Write the packet to the stream
	 * 
	 * @throws IOException
	 */
	@Override
	protected void onWrite(OutputStream stream) throws IOException {
		stream.write(BinarySerializer.serializeValue(this.sessionId));
	}

	/**
	 * Read packet data from the stream
	 * 
	 * @throws IOException
	 */
	@Override
	protected void onRead(InputStream stream) throws IOException {
		this.sessionId = BinarySerializer.deserializeUUIDValue(stream);
	}
}