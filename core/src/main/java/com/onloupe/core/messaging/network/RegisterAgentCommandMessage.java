package com.onloupe.core.messaging.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.onloupe.core.data.BinarySerializer;
import com.onloupe.model.system.Version;

/**
 * Sernt by an agent to register itself with the remote server or desktop
 */
public class RegisterAgentCommandMessage extends NetworkMessage {
	private UUID sessionId;

	public RegisterAgentCommandMessage() {
		setTypeCode(NetworkMessageTypeCode.REGISTER_AGENT_COMMAND);
		setVersion(new Version(1, 0));
	}

	/**
	 * Create a new agent registration message for the specified session Id
	 * 
	 * @param sessionId
	 */
	public RegisterAgentCommandMessage(UUID sessionId) {
		this();
		this.sessionId = sessionId;
	}

	/**
	 * The session Id identifying the agent
	 */
	public final UUID getSessionId() {
		return this.sessionId;
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