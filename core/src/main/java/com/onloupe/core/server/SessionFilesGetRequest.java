package com.onloupe.core.server;

import java.util.Optional;
import java.util.UUID;

import com.onloupe.core.server.data.DataConverter;
import com.onloupe.core.server.data.SessionFilesListXml;

/**
 * Get the list of session fragment files for a session
 */
public class SessionFilesGetRequest extends WebChannelRequestBase {
	/**
	 * Create a new session headers request
	 */
	public SessionFilesGetRequest(UUID sessionId) {
		super(true, true);
		setSessionId(sessionId);
	}

	/**
	 * create a new request for the specified client and session.
	 */
	public SessionFilesGetRequest(UUID clientId, UUID sessionId) {
		super(true, false);
		setClientId(Optional.ofNullable(clientId));
		setSessionId(sessionId);
	}

	/**
	 * The unique Id of this client when being used from an Agent
	 */
	private Optional<UUID> clientId = Optional.empty();

	public final Optional<UUID> getClientId() {
		return this.clientId;
	}

	private void setClientId(Optional<UUID> value) {
		this.clientId = value;
	}

	/**
	 * The unique Id of the session we want to get the existing files for
	 */
	private UUID sessionId;

	public final UUID getSessionId() {
		return this.sessionId;
	}

	public final void setSessionId(UUID value) {
		this.sessionId = value;
	}

	/**
	 * The list of session files on the server
	 */
	private SessionFilesListXml files;

	public final SessionFilesListXml getFiles() {
		return this.files;
	}

	private void setFiles(SessionFilesListXml value) {
		this.files = value;
	}

	/**
	 * Implemented by inheritors to perform the request on the provided web client.
	 * 
	 * @param connection
	 * @throws Exception
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

	private String generateResourceUri() {
		return String.format("Sessions/%1$s/Files.xml", getSessionId());
	}
}