package com.onloupe.core.messaging.network;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.onloupe.core.data.BinarySerializer;
import com.onloupe.model.system.Version;

/**
 * Requests a new live view stream
 */
public class LiveViewStartCommandMessage extends NetworkMessage {
	private final Object lock = new Object();

	private UUID repositoryId;
	private long sequenceOffset;
	private UUID sessionId;
	private UUID channelId;

	public LiveViewStartCommandMessage() {
		synchronized (this.lock) {
			setTypeCode(NetworkMessageTypeCode.LIVE_VIEW_START_COMMAND);
			setVersion(new Version(1, 0));
		}
	}

	/**
	 * Create a new message with the specified session id and optionally sequence
	 * offset
	 * 
	 * @param repositoryId   The unique Id of the client for all related activities
	 * @param channelId      A unique id for this request to identify a conversation
	 *                       pair
	 * @param sessionId      The session that is being requested to live view
	 * @param sequenceOffset The packet index to start at
	 */

	public LiveViewStartCommandMessage(java.util.UUID repositoryId, java.util.UUID sessionId,
			java.util.UUID channelId) {
		this(repositoryId, sessionId, channelId, 0);
	}

	public LiveViewStartCommandMessage(UUID repositoryId, UUID sessionId, UUID channelId, long sequenceOffset) {
		this();
		synchronized (this.lock) {
			this.repositoryId = repositoryId;
			this.sequenceOffset = sequenceOffset;
			this.sessionId = sessionId;
			this.channelId = channelId;
			validate();
		}
	}

	/**
	 * A unique id for this request to identify a conversation pair
	 */
	public final UUID getChannelId() {
		synchronized (this.lock) {
			return this.channelId;
		}
	}

	/**
	 * The last sequence number that was received previously to enable restart at
	 * the right point in the stream
	 */
	public final long getSequenceOffset() {
		synchronized (this.lock) {
			return this.sequenceOffset;
		}
	}

	/**
	 * The Id of the session to be viewed
	 */
	public final UUID getSessionId() {
		synchronized (this.lock) {
			return this.sessionId;
		}
	}

	/**
	 * The unique Id of the client for all related activities
	 */
	public final UUID getRepositoryId() {
		synchronized (this.lock) {
			return this.repositoryId;
		}
	}

	/**
	 * Verify the command is fully populated and
	 */
	public final void validate() {
		synchronized (this.lock) {
			if (getChannelId() == null) {
				throw new IllegalStateException("There is no channel Id specified");
			}
			if (getSessionId() == null) {
				throw new IllegalStateException("There is no session Id specified");
			}
		}

		// Repository Id is optional
	}

	/**
	 * Write the packet to the stream
	 * 
	 * @throws IOException
	 */
	@Override
	protected void onWrite(OutputStream stream) throws IOException {
		synchronized (this.lock) {
			stream.write(BinarySerializer.serializeValue(this.repositoryId));
			stream.write(BinarySerializer.serializeValue(this.sessionId));
			stream.write(BinarySerializer.serializeValue(this.channelId));
			stream.write(BinarySerializer.serializeValue(this.sequenceOffset));
		}
	}

	/**
	 * Read packet data from the stream
	 * 
	 * @throws IOException
	 */
	@Override
	protected void onRead(InputStream stream) throws IOException {
		synchronized (this.lock) {
			DataInputStream inputStream = new DataInputStream(stream);
			this.repositoryId = BinarySerializer.deserializeUUIDValue(inputStream);
			this.sessionId = BinarySerializer.deserializeUUIDValue(inputStream);
			this.channelId = BinarySerializer.deserializeUUIDValue(inputStream);
			this.sequenceOffset = inputStream.readLong();
		}
	}
}