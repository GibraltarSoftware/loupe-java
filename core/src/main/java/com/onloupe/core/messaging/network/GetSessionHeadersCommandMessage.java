package com.onloupe.core.messaging.network;

import java.io.InputStream;
import java.io.OutputStream;

import com.onloupe.model.system.Version;

/**
 * Command for retrieving the list of sesion headers
 */
public class GetSessionHeadersCommandMessage extends NetworkMessage {
	/**
	 * create a new session headers command message
	 */
	public GetSessionHeadersCommandMessage() {
		setTypeCode(NetworkMessageTypeCode.GET_SESSION_HEADERS);
		setVersion(new Version(1, 0));
	}

	@Override
	protected void onWrite(OutputStream outputStream) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onRead(InputStream inputStream) {
		// TODO Auto-generated method stub

	}

}