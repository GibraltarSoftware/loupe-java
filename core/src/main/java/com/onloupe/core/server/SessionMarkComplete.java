package com.onloupe.core.server;

import java.util.UUID;

import com.onloupe.core.server.data.DataConverter;
import com.onloupe.core.server.data.SessionXml;

/**
 * Informs the server that the session is complete (assuming it is a protocol
 * 1.2 or higher server)
 */
public class SessionMarkComplete extends WebChannelRequestBase {
	/**
	 * Create a new session header upload request.
	 * 
	 * @param sessionId
	 * @param clientId
	 */
	public SessionMarkComplete(UUID sessionId, UUID clientId) {
		super(true, false);
		setSessionId(sessionId);
		setClientId(clientId);
	}

	/**
	 * The unique Id of this client
	 */
	private UUID clientId;

	public final UUID getClientId() {
		return this.clientId;
	}

	private void setClientId(UUID value) {
		this.clientId = value;
	}

	/**
	 * The unique Id of the session that is complete
	 */
	private UUID sessionId;

	public final UUID getSessionId() {
		return this.sessionId;
	}

	private void setSessionId(UUID value) {
		this.sessionId = value;
	}

	/**
	 * Implemented by inheritors to perform the request on the provided web client.
	 * 
	 * @param connection
	 * @throws Exception
	 */
	@Override
	protected void onProcessRequest(IWebChannelConnection connection) throws Exception {
		String strRequestUrl = String.format("/Hub/Hosts/%1$s/Sessions/%2$s/session.xml", getClientId(),
				getSessionId());

		SessionXml sessionHeaderXml = new SessionXml();
		sessionHeaderXml.setid(getSessionId().toString());
		sessionHeaderXml.setisComplete(true);
		sessionHeaderXml.setisCompleteSpecified(true);

		// we can't encode using XmlSerializer because it will only work with public
		// types, and we
		// aren't public if we get ILMerged into something.
		byte[] encodedXml = DataConverter.sessionXmlToByteArray(sessionHeaderXml);

		connection.uploadData(strRequestUrl, "text/xml", encodedXml);
	}
}