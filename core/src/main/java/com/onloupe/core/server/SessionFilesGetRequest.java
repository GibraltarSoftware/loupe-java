package com.onloupe.core.server;

import java.util.Optional;
import java.util.UUID;

import com.onloupe.core.server.data.DataConverter;
import com.onloupe.core.server.data.SessionFilesListXml;


/**
 * Get the list of session fragment files for a session.
 */
public class SessionFilesGetRequest extends WebChannelRequestBase {
	
	/**
	 * Create a new session headers request.
	 *
	 * @param sessionId the session id
	 */
	public SessionFilesGetRequest(UUID sessionId) {
		super(true, true);
		setSessionId(sessionId);
	}

	/**
	 * create a new request for the specified client and session.
	 *
	 * @param clientId the client id
	 * @param sessionId the session id
	 */
	public SessionFilesGetRequest(UUID clientId, UUID sessionId) {
		super(true, false);
		setClientId(Optional.ofNullable(clientId));
		setSessionId(sessionId);
	}

	/** The unique Id of this client when being used from an Agent. */
	private Optional<UUID> clientId = Optional.empty();

	/**
	 * Gets the client id.
	 *
	 * @return the client id
	 */
	public final Optional<UUID> getClientId() {
		return this.clientId;
	}

	/**
	 * Sets the client id.
	 *
	 * @param value the new client id
	 */
	private void setClientId(Optional<UUID> value) {
		this.clientId = value;
	}

	/** The unique Id of the session we want to get the existing files for. */
	private UUID sessionId;

	/**
	 * Gets the session id.
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

	/** The list of session files on the server. */
	private SessionFilesListXml files;

	/**
	 * Gets the files.
	 *
	 * @return the files
	 */
	public final SessionFilesListXml getFiles() {
		return this.files;
	}

	/**
	 * Sets the files.
	 *
	 * @param value the new files
	 */
	private void setFiles(SessionFilesListXml value) {
		this.files = value;
	}

	/**
	 * Implemented by inheritors to perform the request on the provided web client.
	 *
	 * @param connection the connection
	 * @throws Exception the exception
	 */
	@Override
	protected void onProcessRequest(IWebChannelConnection connection) throws Exception {
		String url;
		if (getClientId().isPresent()) {
			url = String.format("/Hub/Hosts/%1$s/%2$s", getClientId().get(), generateResourceUri());
		} else {
			url = String.format("/Hub/%1$s", generateResourceUri());
		}

		byte[] sessionFilesListRawData = connection.downloadData(url);

		// even though it's a session list we can't actually deserialize it directly -
		// because we cant use XmlSerializer
		// since the types will not necessarily be public.
		setFiles(DataConverter.byteArrayToSessionFilesListXml(sessionFilesListRawData));
	}

	/**
	 * Generate resource uri.
	 *
	 * @return the string
	 */
	private String generateResourceUri() {
		return String.format("Sessions/%1$s/Files.xml", getSessionId());
	}
}