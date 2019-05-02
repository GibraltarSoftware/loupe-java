package com.onloupe.core.server;

import java.util.UUID;

import com.onloupe.core.server.data.DataConverter;
import com.onloupe.core.server.data.SessionStatusXml;
import com.onloupe.core.server.data.SessionXml;

/**
 * Uploads a session XML document to the endpoint of the web channel
 */
public class SessionHeaderUploadRequest extends WebChannelRequestBase {
	/**
	 * Create a new session header upload request.
	 * 
	 * @param sessionHeader
	 * @param clientId
	 */
	public SessionHeaderUploadRequest(SessionXml sessionHeader, UUID clientId) {
		super(true, false);
		setClientId(clientId);
		setSessionHeader(sessionHeader);
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
	 * The session header to be uploaded.
	 */
	private SessionXml sessionHeader;

	public final SessionXml getSessionHeader() {
		return this.sessionHeader;
	}

	private void setSessionHeader(SessionXml value) {
		this.sessionHeader = value;
	}

	/**
	 * Implemented by inheritors to perform the request on the provided web client.
	 * 
	 * @param connection
	 * @throws Exception
	 */
	@Override
	protected void onProcessRequest(IWebChannelConnection connection) throws Exception {
		String strRequestUrl = String.format("/Hub/Hosts/%s/Sessions/%s/session.xml", getClientId(),
				getSessionHeader().getid());

		assert getSessionHeader().getsessionDetail().getstatus() != SessionStatusXml.RUNNING;
		assert getSessionHeader().getsessionDetail().getstatus() != SessionStatusXml.UNKNOWN;

		// we can't encode using XmlSerializer because it will only work with public
		// types, and we
		// aren't public if we get ILMerged into something.
		byte[] encodedXml = DataConverter.sessionXmlToByteArray(getSessionHeader());

		connection.uploadData(strRequestUrl, "text/xml", encodedXml);
	}
}