package com.onloupe.core.messaging.network;

import java.io.InputStream;
import java.io.OutputStream;

import com.onloupe.model.system.Version;

// TODO: Auto-generated Javadoc
/**
 * Command for retrieving the list of sesion headers.
 */
public class GetSessionHeadersCommandMessage extends NetworkMessage {
	
	/**
	 * create a new session headers command message.
	 */
	public GetSessionHeadersCommandMessage() {
		setTypeCode(NetworkMessageTypeCode.GET_SESSION_HEADERS);
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