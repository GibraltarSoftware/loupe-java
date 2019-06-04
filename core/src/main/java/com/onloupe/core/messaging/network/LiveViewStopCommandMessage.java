package com.onloupe.core.messaging.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.onloupe.core.data.BinarySerializer;
import com.onloupe.model.system.Version;

// TODO: Auto-generated Javadoc
/**
 * Indicates the live view session for the specified session Id be terminated.
 */
public class LiveViewStopCommandMessage extends NetworkMessage {
	
	/** The channel id. */
	private UUID channelId;
	
	/** The session id. */
	private UUID sessionId;

	/**
	 * Instantiates a new live view stop command message.
	 */
	public LiveViewStopCommandMessage() {
		setTypeCode(NetworkMessageTypeCode.LIVE_VIEW_STOP_COMMAND);
		setVersion(new Version(1, 0));
	}

	/**
	 * Create a command to stop the specified live view channel.
	 *
	 * @param channelId the channel id
	 * @param sessionId the session id
	 */
	public LiveViewStopCommandMessage(UUID channelId, UUID sessionId) {
		this();
		setChannelId(channelId);
		setSessionId(sessionId);
	}

	/**
	 * The channel Id of the viewer.
	 *
	 * @return the channel id
	 */
	public final UUID getChannelId() {
		return this.channelId;
	}

	/**
	 * Sets the channel id.
	 *
	 * @param value the new channel id
	 */
	public final void setChannelId(UUID value) {
		this.channelId = value;
	}

	/**
	 * The session Id that is being viewed.
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
	 * Write the packet to the stream.
	 *
	 * @param stream the stream
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	protected void onWrite(OutputStream stream) throws IOException {
		stream.write(BinarySerializer.serializeValue(this.channelId));
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
		this.channelId = BinarySerializer.deserializeUUIDValue(stream);
		this.sessionId = BinarySerializer.deserializeUUIDValue(stream);
	}
}