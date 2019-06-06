package com.onloupe.core.messaging.network;

import java.io.InputStream;
import java.io.OutputStream;

import com.onloupe.model.system.Version;


/**
 * Informs the receiver to start a new packet serializer for the subsequent
 * data.
 */
public class PacketStreamStartCommandMessage extends NetworkMessage {
	
	/**
	 * Create a new packet stream start message.
	 */
	public PacketStreamStartCommandMessage() {
		setTypeCode(NetworkMessageTypeCode.PACKET_STREAM_START_COMMAND);
		setVersion(new Version(1, 0));
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.messaging.network.NetworkMessage#onWrite(java.io.OutputStream)
	 */
	@Override
	protected void onWrite(OutputStream outputStream) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.messaging.network.NetworkMessage#onRead(java.io.InputStream)
	 */
	@Override
	protected void onRead(InputStream inputStream) {
		// TODO Auto-generated method stub

	}

}