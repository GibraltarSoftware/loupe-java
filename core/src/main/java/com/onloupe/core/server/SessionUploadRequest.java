package com.onloupe.core.server;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPut;

import com.onloupe.core.NameValuePair;
import com.onloupe.core.data.InterprocessLock;
import com.onloupe.core.data.InterprocessLockManager;
import com.onloupe.core.data.RepositoryPublishClient;
import com.onloupe.core.logging.Log;
import com.onloupe.core.logging.LogWriteMode;
import com.onloupe.core.monitor.LocalRepository;
import com.onloupe.core.util.IOUtils;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.log.LogMessageSeverity;

// TODO: Auto-generated Javadoc
/**
 * A web channel request to upload a session file or full session stream.
 */
public class SessionUploadRequest extends WebChannelRequestBase implements Closeable {

	/** The Constant SESSION_TEMP_FOLDER. */
	private static final String SESSION_TEMP_FOLDER = "Session_Upload";

	/** The Constant SINGLE_PASS_CUTOFF_BYTES. */
	private static final int SINGLE_PASS_CUTOFF_BYTES = 300000; // about 300k.
	
	/** The Constant DEFAULT_SEGMENT_SIZE_BYTES. */
	private static final int DEFAULT_SEGMENT_SIZE_BYTES = 100000; // about 100k

	/** The initialized. */
	private boolean initialized;
	
	/** The perform cleanup. */
	private boolean performCleanup;
	
	/** The bytes written. */
	private int bytesWritten;
	
	/** The temp session progress file name path. */
	private String tempSessionProgressFileNamePath; // the transfer tracking file.

	/** The session transport lock. */
	private InterprocessLock sessionTransportLock;
	
	/** The delete temporary files on dispose. */
	private boolean deleteTemporaryFilesOnDispose;

	/**
	 * Create a new session upload request.
	 *
	 * @param clientId the client id
	 * @param repository the repository
	 * @param sessionId the session id
	 * @param fileId the file id
	 * @param purgeSessionOnSuccess Indicates if the session should be purged from
	 *                              the repository once it has been sent
	 *                              successfully.
	 */
	public SessionUploadRequest(UUID clientId, LocalRepository repository, UUID sessionId, UUID fileId,
			boolean purgeSessionOnSuccess) {
		super(true, false);
		setClientId(clientId);
		setRepository(repository);
		setSessionId(sessionId);
		setFileId(fileId);
		setPurgeSessionOnSuccess(purgeSessionOnSuccess);
		this.initialized = false;
	}

	/**
	 * Initialize the upload request and underlying session data for transport.
	 *
	 * @return True if the session has been initialized and this is the only upload
	 *         request trying to process this data. If the session isn't already
	 *         being transported to this endpoint then a lock will be set for
	 *         transport. This request must be disposed to ensure this lock is
	 *         released in a timely manner.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final boolean prepareSession() throws IOException {
		initialize();

		return (this.sessionTransportLock != null);
	}

	/** The repository the session upload request is coming from. */
	private LocalRepository repository;

	/**
	 * Gets the repository.
	 *
	 * @return the repository
	 */
	public final LocalRepository getRepository() {
		return this.repository;
	}

	/**
	 * Sets the repository.
	 *
	 * @param value the new repository
	 */
	private void setRepository(LocalRepository value) {
		this.repository = value;
	}

	/** The unique id to use for the client sending the session. */
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
	 * The unique id of the session being sent.
	 */
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
	private void setSessionId(UUID value) {
		this.sessionId = value;
	}

	/**
	 * Optional. The unique id of the file within the session being sent.
	 */
	private UUID fileId;

	/**
	 * Gets the file id.
	 *
	 * @return the file id
	 */
	public final UUID getFileId() {
		return this.fileId;
	}

	/**
	 * Sets the file id.
	 *
	 * @param value the new file id
	 */
	public final void setFileId(UUID value) {
		this.fileId = value;
	}

	/**
	 * Indicates if the session should be purged from the repository once it has
	 * been sent successfully.
	 */
	private boolean purgeSessionOnSuccess;

	/**
	 * Gets the purge session on success.
	 *
	 * @return the purge session on success
	 */
	public final boolean getPurgeSessionOnSuccess() {
		return this.purgeSessionOnSuccess;
	}

	/**
	 * Sets the purge session on success.
	 *
	 * @param value the new purge session on success
	 */
	private void setPurgeSessionOnSuccess(boolean value) {
		this.purgeSessionOnSuccess = value;
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
		// we have to get rid of the lock if we have it.
		if (this.sessionTransportLock != null) {
			IOUtils.closeQuietly(this.sessionTransportLock);
			this.sessionTransportLock = null;
		}

		// and flush any temporary files we have if they aren't in a persistent
		// repository.
		if (this.deleteTemporaryFilesOnDispose) {
			safeDeleteTemporaryData();
		}
	}

	/**
	 * Perform cleanup.
	 *
	 * @param connection the connection
	 */
	private void performCleanup(IWebChannelConnection connection) {
		try {
			// we're going to upload zero bytes as a delete to the right URL.
			connection.deleteData(generateResourceUri());
		} catch (Exception ex) {

		}
	}

	/**
	 * Implemented by inheritors to perform the request on the provided web client.
	 *
	 * @param connection the connection
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws URISyntaxException the URI syntax exception
	 */
	@Override
	protected void onProcessRequest(IWebChannelConnection connection) throws IOException, URISyntaxException {
		if (!initialized) {
			initialize();
		}

		if (this.sessionTransportLock == null) {
			throw new IllegalStateException("The session is currently being transported by another process.");
		}

		// if we might have left a file fragment on the server we need to send a delete
		// call to remove any partial file
		if (this.performCleanup) {
			this.performCleanup = false; // even if we fail, don't try again.
			performCleanup(connection);
		}

		// find the prepared session file
		RandomAccessFile sessionFile = getRepository().loadSessionFile(getSessionId(), getFileId());

		// calculate our SHA1 Hash...
		List<NameValuePair<String>> additionalHeaders = new ArrayList<NameValuePair<String>>();
		try {
			byte[] bytes = new byte[(int) sessionFile.length()];
			sessionFile.readFully(bytes);
			
			//Regex to capitalize the sha1 header.
			additionalHeaders
					.add(new NameValuePair<String>(HubConnection.SHA1_HASH_HEADER,
							DigestUtils.sha1Hex(bytes).replaceAll("..(?!$)", "$0-")));

			// now back up the stream to the beginning so we can send the actual data.
			sessionFile.seek(0);
		} catch (Exception ex) {
			if (!Log.getSilentMode()) {
				Log.write(LogMessageSeverity.ERROR, LogWriteMode.QUEUED, ex, RepositoryPublishClient.LOG_CATEGORY,
						"Unable to calculate hash for session file due to " + ex.getClass() + " exception.",
						"The upload will proceed but without the hash to check the accuracy of the upload.\r\nException: %s\r\n%s\r\n",
						ex.getClass().getName(), ex.getMessage());
			}
		}

		// if it's SMALL we just put the whole thing up as a single action.
		if (sessionFile.length() < SINGLE_PASS_CUTOFF_BYTES) {
			byte[] sessionData = new byte[(int) sessionFile.length()];
			sessionFile.readFully(sessionData);
			connection.uploadData(generateResourceUri(), HttpPut.METHOD_NAME, BINARY_CONTENT_TYPE, sessionData,
					additionalHeaders, null);
		} else {
			// we need to do a segmented post operation. Note that we may be restarting a
			// request after an error, so don't reset
			// our bytes written.
			sessionFile.seek(this.bytesWritten);
			int restartCount = 0;
			byte[] sessionData = new byte[DEFAULT_SEGMENT_SIZE_BYTES];
			while (this.bytesWritten < sessionFile.length()) {
				int remaining = (int) sessionFile.length() - (int) sessionFile.getFilePointer();
				// Read the next segment which is either our segment size or the last fragment
				// of the file, exactly sized.
				if (remaining < sessionData.length) {
					// we're at the last block - resize our buffer down.
					sessionData = new byte[remaining];
				}
				sessionFile.read(sessionData, 0, sessionData.length);

				boolean isComplete = remaining < 1;
				String requestUrl = String.format("%s?Start=%d&Complete=%s&FileSize=%d", generateResourceUri(),
						this.bytesWritten, isComplete, sessionFile.length());

				boolean restartTransfer = false;
				try {
					connection.uploadData(requestUrl, BINARY_CONTENT_TYPE, sessionData, additionalHeaders);
				} catch (WebChannelException ex) {
					StatusLine responseStatus = ex.getResponseStatus();

					// get the inner web response to figure out exactly what the deal is.
					if (responseStatus.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
						if (!Log.getSilentMode()) {
							Log.write(LogMessageSeverity.ERROR, LogWriteMode.QUEUED, ex,
									RepositoryPublishClient.LOG_CATEGORY,
									"Server exchange error, we will assume client and server are out of sync.",
									"The server returned a Bad Request Error (400) which generally means there is either a session-specific transfer problem that may be resolved by restarting the transfer from zero or an internal server problem.\r\nException: %s",
									ex);
						}

						if (restartCount < 4) {
							restartTransfer = true;
							restartCount++;
						}
					}

					if (!restartTransfer) {
						// we didn't find a reason to restart the transfer, we need to let the exception
						// fly.
						throw ex;
					}
				}

				if (restartTransfer) {
					// if we experience this type of Server-level transport error, assume there's
					// some out of sync condition and start again.
					performCleanup(connection);
					sessionFile.seek(0);
					this.bytesWritten = 0;
				} else {
					// and now that we've written the bytes and not gotten an exception we can mark
					// these bytes as done!
					this.bytesWritten = (int) sessionFile.getFilePointer();
					updateProgressTrackingFile();
				}
			}
		}

		// and since we're now good & done... clean up our temp stuff.
		safeDeleteTemporaryData();

		// finally, if we are supposed to purge a session once we sent we need to give
		// that a shot.
		if (getPurgeSessionOnSuccess()) {
			safePurgeSession();
		}
	}

	/**
	 * Load progress tracking file.
	 *
	 * @return the int
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private int loadProgressTrackingFile() throws IOException {
		try (RandomAccessFile file = new RandomAccessFile(this.tempSessionProgressFileNamePath, "r")) {
			return file.readInt();
		} catch (IOException e) {
			throw e; 
		}
	}

	/**
	 * Update progress tracking file.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void updateProgressTrackingFile() throws IOException {
		try (RandomAccessFile file = new RandomAccessFile(this.tempSessionProgressFileNamePath, "rw")) {
			file.write(this.bytesWritten);
		} catch (IOException e) {
			throw e;
		}
	}

	/**
	 * Safe purge session.
	 */
	private void safePurgeSession() {
		try {
			getRepository().remove(getSessionId(), getFileId()); // this will just remove the one file, not
			// the whole session.
		} catch (Exception ex) {

		}
	}

	/**
	 * Removes all of the temporary data used to transfer the session without
	 * allowing exceptions to propagate on failure.
	 */
	private void safeDeleteTemporaryData() {
		safeDeleteFile(this.tempSessionProgressFileNamePath);
		this.deleteTemporaryFilesOnDispose = false; // because we already did.
	}

	/**
	 * Safe delete file.
	 *
	 * @param fileNamePath the file name path
	 */
	private static void safeDeleteFile(String fileNamePath) {
		if (TypeUtils.isBlank(fileNamePath)) {
			return;
		}

		try {
			(new File(fileNamePath)).delete();
		} catch (RuntimeException ex) {

		}
	}

	/**
	 * Generate resource uri.
	 *
	 * @return the string
	 */
	private String generateResourceUri() {
		return String.format("/Hub/Hosts/%1$s/Sessions/%2$s/Files/%3$s.zip", getClientId(), getSessionId(),	getFileId());
	}

	/**
	 * The temporary path to put all of the transfer information for this session.
	 *
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private String generateTemporarySessionPath() throws IOException {
		// find the right temporary directory for us...
		// if this is a real repository, we'll use a persistent path.
		// otherwise we'll use something truly temporary and have to clean up after
		// ourselves.
		String tempDirectory;
		if (getRepository() != null) {
			tempDirectory = Paths.get(getRepository().getTempPath()).resolve(SESSION_TEMP_FOLDER).toString();
		} else {
			try {
				tempDirectory = Files.createTempDirectory("temp_").toString();
			} catch (IOException e) {
				throw e;
			}
			(new File(tempDirectory)).delete(); // we just want it as a directory, not a file.
			this.deleteTemporaryFilesOnDispose = true;
		}

		// make damn sure it exists.
		(new File(tempDirectory)).mkdirs();

		return tempDirectory;
	}

	/**
	 * The file name (without extension) for this session.
	 *
	 * @return the string
	 */
	private String generateTemporarySessionFileName() {
		// the path needs to be generated reliably, but uniquely.
		return String.format("%1$s_%2$s_%3$s", getSessionId(), getClientId(), getFileId());
	}

	/**
	 * The full file name and path (without extension) for the transfer information
	 * for this session.
	 *
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private String generateTemporarySessionFileNamePath() throws IOException {
		return Paths.get(generateTemporarySessionPath()).resolve(generateTemporarySessionFileName()).toString();
	}

	/**
	 * Initialize.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void initialize() throws IOException {
		if (this.initialized) {
			return;
		}

		// we need to grab a lock on this session to prevent it from being transported
		// to the same endpoint at the same time.
		String sessionWorkingFileNamePath = generateTemporarySessionFileNamePath();

		// we aren't going to do Using - we keep the lock!
		if (this.sessionTransportLock == null) // if we are retrying to initialize after a failure we may already have
												// it.
		{
			this.sessionTransportLock = InterprocessLockManager.getInstance().lock(this,
					generateTemporarySessionPath(), generateTemporarySessionFileName(), 0, true);
		}

		// we aren't waiting to see if we can get the lock - if anyone else has it they
		// must be transferring the session.
		if (this.sessionTransportLock != null) {
			// Lets figure out if we're restarting a previous send or starting a new one.
			this.tempSessionProgressFileNamePath = sessionWorkingFileNamePath + ".txt";

			this.bytesWritten = 0;
			if ((new File(this.tempSessionProgressFileNamePath)).isFile()) {
				// load up the existing transfer state.
				try {
					this.bytesWritten = loadProgressTrackingFile();
				} catch (java.lang.Exception e) {
					// oh well, assume no progress.
					this.performCleanup = true;

					safeDeleteFile(this.tempSessionProgressFileNamePath);
				}
			} else {
				// make sure we didn't get started, but not finish, writing our temp file.
				safeDeleteFile(this.tempSessionProgressFileNamePath);
			}

			this.initialized = true;
		}
	}
}