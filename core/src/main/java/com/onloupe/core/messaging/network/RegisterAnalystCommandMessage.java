package com.onloupe.core.messaging.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.onloupe.core.data.BinarySerializer;
import com.onloupe.model.system.Version;

/**
 * Sent by a Desktop to register itself with the remote server
 */
public class RegisterAnalystCommandMessage extends NetworkMessage {
	private String userName;
	private UUID repositoryId;

	public RegisterAnalystCommandMessage() {
		setTypeCode(NetworkMessageTypeCode.REGISTER_ANALYST_COMMAND);
		setVersion(new Version(1, 0));
	}

	/**
	 * Create a new registration for the specified client repository id and user
	 * name
	 * 
	 * @param repositoryId
	 * @param userName
	 */
	public RegisterAnalystCommandMessage(UUID repositoryId, String userName) {
		this();
		this.repositoryId = repositoryId;
		this.userName = userName;
	}

	/**
	 * The user running Analyst
	 */
	public final String getUserName() {
		return this.userName;
	}

	/**
	 * The unique client repository id of the Analyst
	 */
	public final UUID getRepositoryId() {
		return this.repositoryId;
	}

	/**
	 * Write the packet to the stream
	 * 
	 * @throws IOException
	 */
	@Override
	protected void onWrite(OutputStream stream) throws IOException {
		stream.write(BinarySerializer.serializeValue(this.repositoryId));
		stream.write(BinarySerializer.serializeValue(this.userName));
	}

	/**
	 * Read packet data from the stream
	 * 
	 * @throws IOException
	 */
	@Override
	protected void onRead(InputStream stream) throws IOException {
		this.repositoryId = BinarySerializer.deserializeUUIDValue(stream);
		this.userName = BinarySerializer.deserializeStringValue(stream);
	}
}