package com.onloupe.core.messaging.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.onloupe.core.data.BinarySerializer;
import com.onloupe.model.system.Version;

/**
 * Indicates the live view session for the specified session Id be terminated
 */
public class LiveViewStopCommandMessage extends NetworkMessage {
	private UUID channelId;
	private UUID sessionId;

	public LiveViewStopCommandMessage() {
		setTypeCode(NetworkMessageTypeCode.LIVE_VIEW_STOP_COMMAND);
		setVersion(new Version(1, 0));
	}

	/**
	 * Create a command to stop the specified live view channel
	 */
	public LiveViewStopCommandMessage(UUID channelId, UUID sessionId) {
		this();
		setChannelId(channelId);
		setSessionId(sessionId);
	}

	/**
	 * The channel Id of the viewer
	 */
	public final UUID getChannelId() {
		return this.channelId;
	}

	public final void setChannelId(UUID value) {
		this.channelId = value;
	}

	/**
	 * The session Id that is being viewed
	 */
	public final UUID getSessionId() {
		return this.sessionId;
	}

	public final void setSessionId(UUID value) {
		this.sessionId = value;
	}

	/**
	 * Write the packet to the stream
	 * 
	 * @throws IOException
	 */
	@Override
	protected void onWrite(OutputStream stream) throws IOException {
		stream.write(BinarySerializer.serializeValue(this.channelId));
		stream.write(BinarySerializer.serializeValue(this.sessionId));
	}

	/**
	 * Read packet data from the stream
	 * 
	 * @throws IOException
	 */
	@Override
	protected void onRead(InputStream stream) throws IOException {
		this.channelId = BinarySerializer.deserializeUUIDValue(stream);
		this.sessionId = BinarySerializer.deserializeUUIDValue(stream);
	}
}