package com.onloupe.core.server;

import java.util.UUID;

import com.onloupe.core.server.data.DataConverter;
import com.onloupe.core.server.data.SessionsListXml;

// TODO: Auto-generated Javadoc
/**
 * Get the requested sessions for a client from the server.
 */
public class RequestedSessionsGetRequest extends WebChannelRequestBase {
	
	/**
	 * create a new request for the specified client.
	 *
	 * @param clientId the client id
	 */
	public RequestedSessionsGetRequest(UUID clientId) {
		super(true, false);
		setClientId(clientId);
	}

	/** The unique Id of this client. */
	private UUID clientId;

	/**
	 * Gets the client id.
	 *
	 * @return the client id
	 */
	public final UUID getClientId() {
		return this.clientId;
	}

	/**
	 * Sets the client id.
	 *
	 * @param value the new client id
	 */
	private void setClientId(UUID value) {
		this.clientId = value;
	}

	/**
	 * The list of sessions requested from the server.
	 */
	private SessionsListXml requestedSessions;

	/**
	 * Gets the requested sessions.
	 *
	 * @return the requested sessions
	 */
	public final SessionsListXml getRequestedSessions() {
		return this.requestedSessions;
	}

	/**
	 * Sets the requested sessions.
	 *
	 * @param value the new requested sessions
	 */
	private void setRequestedSessions(SessionsListXml value) {
		this.requestedSessions = value;
	}

	/**
	 * Implemented by inheritors to perform the request on the provided web client.
	 *
	 * @param connection the connection
	 * @throws Exception the exception
	 */
	@Override
	protected void onProcessRequest(IWebChannelConnection connection) throws Exception {
		byte[] requestedSessionsRawData = connection
				.downloadData(String.format("/Hub/Hosts/%1$s/RequestedSessions.xml", getClientId()));

		// even though it's a session list we can't actually deserialize it directly -
		// because we cant use XmlSerializer
		// since the types will not necessarily be public.
		setRequestedSessions(DataConverter.byteArrayToSessionsListXml(requestedSessionsRawData));
	}
}