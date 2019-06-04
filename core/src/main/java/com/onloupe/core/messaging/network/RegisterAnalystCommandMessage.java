package com.onloupe.core.messaging.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.onloupe.core.data.BinarySerializer;
import com.onloupe.model.system.Version;

// TODO: Auto-generated Javadoc
/**
 * Sent by a Desktop to register itself with the remote server.
 */
public class RegisterAnalystCommandMessage extends NetworkMessage {
	
	/** The user name. */
	private String userName;
	
	/** The repository id. */
	private UUID repositoryId;

	/**
	 * Instantiates a new register analyst command message.
	 */
	public RegisterAnalystCommandMessage() {
		setTypeCode(NetworkMessageTypeCode.REGISTER_ANALYST_COMMAND);
		setVersion(new Version(1, 0));
	}

	/**
	 * Create a new registration for the specified client repository id and user
	 * name.
	 *
	 * @param repositoryId the repository id
	 * @param userName the user name
	 */
	public RegisterAnalystCommandMessage(UUID repositoryId, String userName) {
		this();
		this.repositoryId = repositoryId;
		this.userName = userName;
	}

	/**
	 * The user running Analyst.
	 *
	 * @return the user name
	 */
	public final String getUserName() {
		return this.userName;
	}

	/**
	 * The unique client repository id of the Analyst.
	 *
	 * @return the repository id
	 */
	public final UUID getRepositoryId() {
		return this.repositoryId;
	}

	/**
	 * Write the packet to the stream.
	 *
	 * @param stream the stream
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	protected void onWrite(OutputStream stream) throws IOException {
		stream.write(BinarySerializer.serializeValue(this.repositoryId));
		stream.write(BinarySerializer.serializeValue(this.userName));
	}

	/**
	 * Read packet data from the stream.
	 *
	 * @param stream the stream
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	protected void onRead(InputStream stream) throws IOException {
		this.repositoryId = BinarySerializer.deserializeUUIDValue(stream);
		this.userName = BinarySerializer.deserializeStringValue(stream);
	}
}