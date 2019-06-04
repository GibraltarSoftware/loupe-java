package com.onloupe.core.messaging.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.onloupe.agent.SessionCriteria;
import com.onloupe.core.data.BinarySerializer;
import com.onloupe.model.system.Version;

// TODO: Auto-generated Javadoc
/**
 * A command to have the agent send sessions to the server immediately.
 */
public class SendSessionCommandMessage extends NetworkMessage {
	
	/** The session id. */
	private UUID sessionId;
	
	/** The criteria. */
	private SessionCriteria criteria;

	/**
	 * Instantiates a new send session command message.
	 */
	public SendSessionCommandMessage() {
		setTypeCode(NetworkMessageTypeCode.SEND_SESSION);
		setVersion(new Version(1, 0));
	}

	/**
	 * Create a new send session command for the specified session id and criteria.
	 *
	 * @param sessionId the session id
	 * @param criteria the criteria
	 */
	public SendSessionCommandMessage(UUID sessionId, SessionCriteria criteria) {
		this();
		setSessionId(sessionId);
		setCriteria(criteria);
	}

	/**
	 * The session Id to send.
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
	public final void setSessionId(UUID value) {
		this.sessionId = value;
	}

	/**
	 * The criteria to use to send the session.
	 *
	 * @return the criteria
	 */
	public final SessionCriteria getCriteria() {
		return this.criteria;
	}

	/**
	 * Sets the criteria.
	 *
	 * @param value the new criteria
	 */
	public final void setCriteria(SessionCriteria value) {
		this.criteria = value;
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
		stream.write(BinarySerializer.serializeValue(this.criteria.getValue()));

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
		this.criteria = SessionCriteria.forValue(BinarySerializer.deserializeInt(stream));
	}
}