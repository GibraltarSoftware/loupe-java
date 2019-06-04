package com.onloupe.core.messaging.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.onloupe.core.data.BinarySerializer;
import com.onloupe.model.system.Version;

// TODO: Auto-generated Javadoc
/**
 * Indicates that the identified session has been closed.
 */
public class SessionClosedMessage extends NetworkMessage {
	
	/** The session id. */
	private UUID sessionId;

	/**
	 * Instantiates a new session closed message.
	 */
	public SessionClosedMessage() {
		setTypeCode(NetworkMessageTypeCode.SESSION_CLOSED);
		setVersion(new Version(1, 0));
	}

	/**
	 * Create a new session closed message for the specified session id.
	 *
	 * @param sessionId the session id
	 */
	public SessionClosedMessage(UUID sessionId) {
		this();
		setSessionId(sessionId);
	}

	/**
	 * Gets the session id.
	 *
	 * @return the session id
	 */
	public final UUID getSessionId() {
		return this.sessionId;
	}

	/**
	 * Sets the session id.
	 *
	 * @param value the new session id
	 */
	private void setSessionId(UUID value) {
		this.sessionId = value;
	}

	/**
	 * Write the packet to the stream.
	 *
	 * @param stream the stream
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	protected void onWrite(OutputStream stream) throws IOException {
		stream.write(BinarySerializer.serializeValue(this.sessionId));
	}

	/**
	 * Read packet data from the stream.
	 *
	 * @param stream the stream
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	protected void onRead(InputStream stream) throws IOException {
		this.sessionId = BinarySerializer.deserializeUUIDValue(stream);
	}
}