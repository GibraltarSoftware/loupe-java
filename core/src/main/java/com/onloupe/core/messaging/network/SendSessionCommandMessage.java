package com.onloupe.core.messaging.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.onloupe.agent.SessionCriteria;
import com.onloupe.core.data.BinarySerializer;
import com.onloupe.model.system.Version;

/**
 * A command to have the agent send sessions to the server immediately
 */
public class SendSessionCommandMessage extends NetworkMessage {
	private UUID sessionId;
	private SessionCriteria criteria;

	public SendSessionCommandMessage() {
		setTypeCode(NetworkMessageTypeCode.SEND_SESSION);
		setVersion(new Version(1, 0));
	}

	/**
	 * Create a new send session command for the specified session id and criteria
	 * 
	 * @param sessionId
	 * @param criteria
	 */
	public SendSessionCommandMessage(UUID sessionId, SessionCriteria criteria) {
		this();
		setSessionId(sessionId);
		setCriteria(criteria);
	}

	/**
	 * The session Id to send
	 */
	public final UUID getSessionId() {
		return this.sessionId;
	}

	public final void setSessionId(UUID value) {
		this.sessionId = value;
	}

	/**
	 * The criteria to use to send the session
	 */
	public final SessionCriteria getCriteria() {
		return this.criteria;
	}

	public final void setCriteria(SessionCriteria value) {
		this.criteria = value;
	}

	/**
	 * Write the packet to the stream
	 * 
	 * @throws IOException
	 */
	@Override
	protected void onWrite(OutputStream stream) throws IOException {
		stream.write(BinarySerializer.serializeValue(this.sessionId));
		stream.write(BinarySerializer.serializeValue(this.criteria.getValue()));

	}

	/**
	 * Read packet data from the stream
	 * 
	 * @throws IOException
	 */
	@Override
	protected void onRead(InputStream stream) throws IOException {
		this.sessionId = BinarySerializer.deserializeUUIDValue(stream);
		this.criteria = SessionCriteria.forValue(BinarySerializer.deserializeInt(stream));
	}
}