package com.onloupe.core.data;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import com.onloupe.configuration.ServerConfiguration;
import com.onloupe.core.logging.Log;
import com.onloupe.core.logging.LogWriteMode;
import com.onloupe.core.monitor.LocalRepository;
import com.onloupe.core.monitor.SessionFileInfo;
import com.onloupe.core.server.HubConnection;
import com.onloupe.core.server.HubConnectionStatus;
import com.onloupe.core.server.HubRepository;
import com.onloupe.core.server.RequestedSessionsGetRequest;
import com.onloupe.core.server.SessionFilesGetRequest;
import com.onloupe.core.server.SessionHeaderUploadRequest;
import com.onloupe.core.server.SessionMarkComplete;
import com.onloupe.core.server.SessionUploadRequest;
import com.onloupe.core.server.data.DataConverter;
import com.onloupe.core.server.data.SessionFileXml;
import com.onloupe.core.server.data.SessionXml;
import com.onloupe.core.util.IOUtils;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.data.ISessionSummaryCollection;
import com.onloupe.model.log.LogMessageSeverity;
import com.onloupe.model.session.ISessionSummary;


/**
 * Publishes sessions from the specified repository to a remote destination
 * repository.
 */
public class RepositoryPublishClient implements Closeable {
	
	/** The Constant LOG_CATEGORY. */
	public static final String LOG_CATEGORY = "Loupe.Repository.Publish";

	/** The source repository. */
	private LocalRepository sourceRepository;
	
	/** The product name. */
	private String productName;
	
	/** The application name. */
	private String applicationName;
	
	/** The hub connection. */
	private HubConnection hubConnection;

	/** The active. */
	private volatile boolean active;
	
	/** The closed. */
	private volatile boolean closed;

	/**
	 * Create a new repository publish engine for the specified repository.
	 *
	 * @param source The repository to publish
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public RepositoryPublishClient(LocalRepository source) throws IOException {
		this(source, null, null, Log.getConfiguration().getServer());
		// everything was handled in the constructor overload we passed off to
	}

	/**
	 * Create a new repository publish engine for the specified repository.
	 * 
	 * @param source          The repository to publish
	 * @param productName     Optional. A product name to restrict operations to.
	 * @param applicationName Optional. An application name within a product to
	 *                        restrict operations to.
	 * @param configuration   The server connection information.
	 */
	public RepositoryPublishClient(LocalRepository source, String productName, String applicationName,
			ServerConfiguration configuration) {
		this(source, configuration);
		this.productName = productName;
		this.applicationName = applicationName;
	}

	/**
	 * Create a new repository publish engine for the specified repository.
	 * 
	 * @param source              The repository to publish
	 * @param serverConfiguration The configuration of the connection to the server
	 */
	public RepositoryPublishClient(LocalRepository source, ServerConfiguration serverConfiguration) {
		if (source == null) {
			throw new NullPointerException("source");
		}

		this.sourceRepository = source;

		this.hubConnection = new HubConnection(serverConfiguration);
	}

	/**
	 * The repository this publish engine is associated with.
	 *
	 * @return the repository
	 */
	public final LocalRepository getRepository() {
		return this.sourceRepository;
	}

	/**
	 * Indicates if this is the active repository publish engine for the specified
	 * repository.
	 *
	 * @return true, if is active
	 */
	public final boolean isActive() {
		return this.active;
	}

	/**
	 * Attempts to connect to the server and returns information about the
	 * connection status.
	 *
	 * @return True if the configuration is valid and the server is available, false
	 *         otherwise.
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	public final HubConnectionStatus canConnect() throws IOException, Exception {
		return this.hubConnection.canConnect();
	}

	/**
	 * Publish qualifying local sessions and upload any details requested by the
	 * server.
	 *
	 * @param purgeSentSessions Indicates if the session should be purged from the
	 *                          repository once it has been sent successfully.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final void publishSessions(boolean purgeSentSessions) throws IOException {
		if (this.active) {
			return; // we're already publishing, we can't queue more.
		}

		// we do the check for new sessions on the foreground thread since it won't
		// block.
		List<ISessionSummary> sessions = getSessions();

		if ((sessions != null) && (!sessions.isEmpty())) {
			// go ahead and use the threadpool to publish the sessions.
			this.active = true; // this gets set to false by the publish sessions routine when it's done.

			// retry until successful (-1)
			asyncPublishSessions(sessions, -1, purgeSentSessions);
		}

	}

	/**
	 * Send the specified session with details, even if other publishers are
	 * running.
	 *
	 * @param sessionId the session id
	 * @param maxRetries the max retries
	 * @param purgeSentSession Indicates if the session should be purged from the
	 *                         repository once it has been sent successfully. Throws
	 *                         an exception if it fails
	 * @throws Exception the exception
	 */
	public final void uploadSession(UUID sessionId, int maxRetries, boolean purgeSentSession) throws Exception {
		performSessionDataUpload(sessionId, maxRetries, purgeSentSession);
	}

	/**
	 * Performs application-defined tasks associated with freeing, releasing, or
	 * resetting unmanaged resources.
	 * 
	 * 
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public final void close() throws IOException {
		if (!this.closed) {
			IOUtils.closeQuietly(hubConnection);
			this.closed = true;
		}
	}

	/**
	 * Publish the latest session data and find out what sessions should be
	 * uploaded.
	 *
	 * @param sessions the sessions
	 * @param maxRetries the max retries
	 * @param purgeSentSessions the purge sent sessions
	 */
	private void asyncPublishSessions(List<ISessionSummary> sessions, int maxRetries, boolean purgeSentSessions) {
		try {

			if (sessions == null) {
				return;
			}

			// lets make sure the server is connectible.
			HubConnectionStatus status = this.hubConnection.canConnect();
			if (!status.isValid() && (maxRetries >= 0)) {
				// we are stopping right here because the server isn't there, so no point in
				// trying anything else.
			} else {
				// OK, now we've released the session information from RAM (all we wanted were
				// the GUID's anyway)
				// and we can send these one by one as long as the connection is up.
				for (ISessionSummary session : sessions) {
					// try to upload it.
					try {
						performSessionHeaderUpload(session);
					} catch (Exception ex) {

					}
				}

				// now find out what sessions they want us to upload
				List<UUID> requestedSessions = getRequestedSessions();

				for (UUID sessionId : requestedSessions) {
					// we want to try each, even if they fail.
					try {
						performSessionDataUpload(sessionId, maxRetries, purgeSentSessions);
					} catch (Exception ex) {

					}
				}
			}
		} catch (Exception ex) {

		} finally {
			this.active = false; // so others can go now.
		}
	}

	/**
	 * Find out what sessions the server wants details for.
	 *
	 * @return the requested sessions
	 */

	private List<UUID> getRequestedSessions() {
		RequestedSessionsGetRequest request = new RequestedSessionsGetRequest(this.sourceRepository.getId());

		try {
			this.hubConnection.executeRequest(request, 1);
		} catch (Exception ex) {

		}

		List<UUID> requestedSessions = new ArrayList<UUID>();

		if ((request.getRequestedSessions() != null) && (request.getRequestedSessions().getsessions() != null)
				&& (request.getRequestedSessions().getsessions().length > 0)) {
			for (SessionXml requestedSession : request.getRequestedSessions().getsessions()) {
				// we want to either queue the session to be sent (if not queued already) or
				// mark the session as complete on the server is no data is available.
				try {
					HubRepository serverRepository = this.hubConnection.getRepository();
					UUID sessionId = UUID.fromString(requestedSession.getid());
					if (this.sourceRepository.sessionDataExists(sessionId)) {
						// queue for transmission
						requestedSessions.add(sessionId);
					} else {
						if (serverRepository.getProtocolVersion().compareTo(HubConnection.hub30ProtocolVersion) < 0) {
							if (!Log.getSilentMode()) {
								Log.write(LogMessageSeverity.INFORMATION, LOG_CATEGORY,
										"Server requesting completed session that's no longer available",
										"There's no way for us to tell the server that it should stop asking for this session.\r\nSession Id: %s",
										sessionId.toString());
							}
						} else {
							// it's complete, there's nothing more we can give them.
							// KM: We can't assume there is no data - it could be in another local
							// repository, so we shouldn't do this.
							// PerformSessionMarkComplete(sessionId);
						}
					}
				} catch (Exception ex) {

				}
			}
		}

		return requestedSessions;
	}

	/**
	 * Find the list of all sessions that haven't been published yet and match our
	 * filter.
	 *
	 * @return the sessions
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private List<ISessionSummary> getSessions() throws IOException {
		// find the list of all sessions that haven't been published yet and match our
		// filter
		List<ISessionSummary> sessions = new ArrayList<ISessionSummary>();

		try {
			this.sourceRepository.refresh(); // we want a picture of the latest data as of the start of this process.

			ISessionSummaryCollection collection = this.sourceRepository.find(new Predicate<ISessionSummary>() {
				@Override
				public boolean test(ISessionSummary t) {
					return unsentSessionsPredicate(t);
				}
			});

			if (collection != null) {
				collection.iterator().forEachRemaining(sessions::add);
			}
		} catch (RuntimeException ex) {

		}

		return sessions;
	}

	/**
	 * A predicate filter for the repository to identify unsent, qualifying sessions.
	 *
	 * @param candidateSession the candidate session
	 * @return true, if successful
	 */
	private boolean unsentSessionsPredicate(ISessionSummary candidateSession) {
		boolean matchesPredicate = candidateSession.isNew();

		if (matchesPredicate) {
			matchesPredicate = candidateSession.getProduct().equalsIgnoreCase(this.productName);
		}

		if (matchesPredicate && !TypeUtils.isBlank(this.applicationName)) {
			matchesPredicate = candidateSession.getApplication().equalsIgnoreCase(this.applicationName);
		}

		return matchesPredicate;
	}

	/**
	 * Sends a session, either as a single stream or a set of fragments, to the
	 * server.
	 *
	 * @param sessionId the session id
	 * @param maxRetries        The maximum number of times to retry the session
	 *                          data upload.
	 * @param purgeSentSessions Indicates whether to purge sessions that have been
	 *                          successfully sent from the repository
	 * @return Throws an exception if the upload fails.
	 * @throws Exception the exception
	 */
	private void performSessionDataUpload(UUID sessionId, int maxRetries, boolean purgeSentSessions) throws Exception {
		this.sourceRepository.refresh(); // we want a picture of the latest data as of the start of this process.

		// this can get a little complicated: Do we use the new fragment-based protocol
		// or the old single session mode?
		HubRepository serverRepository = this.hubConnection.getRepository();
		Map<UUID, SessionFileXml> serverFiles = new HashMap<UUID, SessionFileXml>();
		if (serverRepository.getProtocolVersion().compareTo(HubConnection.hub30ProtocolVersion) < 0) {
			// we aren't compatible with this hub, its too old.
			throw new IOException("Unable to upload session to server as it doesn't support Loupe 3.0 protocol");
		}

		SessionFilesGetRequest sessionFilesRequest = new SessionFilesGetRequest(this.sourceRepository.getId(),
				sessionId);
		this.hubConnection.executeRequest(sessionFilesRequest, maxRetries);

		if (sessionFilesRequest.getFiles().getfiles() != null) {
			// since individual files are immutable we don't need to upload any file the
			// server already has.
			for (SessionFileXml sessionFileXml : sessionFilesRequest.getFiles().getfiles()) {
				serverFiles.put(UUID.fromString(sessionFileXml.getid()), sessionFileXml);
			}
		}

		// now we need to update each file they don't have.
		SessionFileInfo<File> sessionFileInfo = this.sourceRepository.loadSessionFiles(sessionId);

		for (File sessionFragment : sessionFileInfo.getFragments()) {
			// if they already have this one, skip it.
			SessionHeader fileHeader = LocalRepository.loadSessionHeader(sessionFragment.getPath());
			if (fileHeader == null) {
				break; // the file must be gone, it certainly isn't valid.
			}

			if (serverFiles.containsKey(fileHeader.getFileId())) {
				// skip this file. If we're supposed to be purging sent data then drop this
				// fragment.
				if (purgeSentSessions) {
					this.sourceRepository.remove(sessionId, fileHeader.getFileId());
				}
			} else {
				// ohhkay, lets upload this bad boy.
				performSessionFileUpload(sessionId, fileHeader.getFileId(), maxRetries,
						purgeSentSessions);
			}
		}

		if (!this.sourceRepository.sessionIsRunning(sessionId)) {
			// finally, mark this session as complete. We've sent all the data we have. But,
			// we won't fail if we can't.
			try {
				performSessionMarkComplete(sessionId);
			} catch (Exception ex) {
				if (!Log.getSilentMode()) {
					Log.write(LogMessageSeverity.WARNING, LogWriteMode.QUEUED, ex, true, LOG_CATEGORY,
							"Unable to inform server a session is complete due to "
									+ TypeUtils.getRootCause(ex),
							"The server may continue to ask for data for this session until we can tell it we have no more data.\r\nSession Id: %s\r\nException: %s",
							sessionId.toString(), ex.getMessage());
				}
			}
	}
	}

	/**
	 * Sends a merged session stream or a single session fragment file to the
	 * server.
	 *
	 * @param sessionId the session id
	 * @param fileId the file id
	 * @param maxRetries        The maximum number of times to retry the session
	 *                          data upload.
	 * @param purgeSentSessions Indicates whether to purge sessions that have been
	 *                          successfully sent from the repository
	 * @return Throws an exception if the upload fails.
	 * @throws Exception the exception
	 */
	private void performSessionFileUpload(UUID sessionId, UUID fileId, int maxRetries,
			boolean purgeSentSessions) throws Exception {
		try (SessionUploadRequest request = new SessionUploadRequest(this.sourceRepository.getId(),
				this.sourceRepository, sessionId, fileId, purgeSentSessions)) {
			// because upload request uses a multiprocess lock we put it in a using to
			// ensure it gets disposed.
			// explicitly prepare the session - this returns true if we got the lock meaning
			// no one else is actively transferring this session right now.
			if (!request.prepareSession()) {
				if (!Log.getSilentMode()) {
					Log.write(LogMessageSeverity.INFORMATION, LOG_CATEGORY,
							"Skipping sending session to server because another process already is transferring it",
							"We weren't able to get a transport lock on the session '%s' so we assume another process is currently sending it.",
							sessionId.toString());
				}
			} else {
				this.hubConnection.executeRequest(request, maxRetries);
			}
		} catch (Exception e) {
			throw e;
		}

	}

	/**
	 * Upload the session summary for one session.
	 *
	 * @param sessionSummary the session summary
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	private void performSessionHeaderUpload(ISessionSummary sessionSummary) throws IOException, Exception {
		SessionXml sessionSummaryXml = DataConverter.toSessionXml(sessionSummary);
		performSessionHeaderUpload(sessionSummaryXml);
	}

	/**
	 * Upload the session summary for one session.
	 *
	 * @param sessionSummary the session summary
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	private void performSessionHeaderUpload(SessionXml sessionSummary) throws IOException, Exception {
		UUID sessionId = UUID.fromString(sessionSummary.getid());

		assert !TypeUtils.isBlank(sessionSummary.getsessionDetail().getproductName());
		assert !TypeUtils.isBlank(sessionSummary.getsessionDetail().getapplicationName());
		assert !TypeUtils.isBlank(sessionSummary.getsessionDetail().getapplicationVersion());

		// we consider a session complete (since we're the source repository) with just
		// the header if there
		// is no session file.
		sessionSummary.getsessionDetail().setisComplete(!this.sourceRepository.sessionDataExists(sessionId));

		SessionHeaderUploadRequest uploadRequest = new SessionHeaderUploadRequest(sessionSummary,
				this.sourceRepository.getId());

		this.hubConnection.executeRequest(uploadRequest, -1);

		// and if we were successful (must have been - we got to here) then mark the
		// session as not being new any more.
		this.sourceRepository.setSessionsNew(Arrays.asList(sessionId), false);
	}

	/**
	 * Mark the specified session as being complete.
	 *
	 * @param sessionId the session id
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	private void performSessionMarkComplete(UUID sessionId) throws IOException, Exception {
		SessionMarkComplete uploadRequest = new SessionMarkComplete(sessionId, this.sourceRepository.getId());

		// get our web channel to upload this request for us.
		this.hubConnection.executeRequest(uploadRequest, -1);
	}
}