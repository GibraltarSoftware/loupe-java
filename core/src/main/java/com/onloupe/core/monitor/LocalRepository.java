package com.onloupe.core.monitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.onloupe.agent.SessionCriteria;
import com.onloupe.core.FileSystemTools;
import com.onloupe.core.data.GLFReader;
import com.onloupe.core.data.GLFWriter;
import com.onloupe.core.data.InterprocessLock;
import com.onloupe.core.data.InterprocessLockManager;
import com.onloupe.core.data.PathManager;
import com.onloupe.core.data.PathType;
import com.onloupe.core.data.RepositoryMaintenance;
import com.onloupe.core.data.SessionHeader;
import com.onloupe.core.logging.Log;
import com.onloupe.core.logging.LogWriteMode;
import com.onloupe.core.messaging.FileMessenger;
import com.onloupe.core.serialization.monitor.SessionSummaryCollection;
import com.onloupe.core.util.FileUtils;
import com.onloupe.core.util.IOUtils;
import com.onloupe.core.util.Multiplexer;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.data.ISessionSummaryCollection;
import com.onloupe.model.exception.DirectoryNotFoundException;
import com.onloupe.model.exception.GibraltarException;
import com.onloupe.model.exception.UnauthorizedAccessException;
import com.onloupe.model.log.LogMessageSeverity;
import com.onloupe.model.session.ISessionSummary;
import com.onloupe.model.session.SessionStatus;


/**
 * The local collection repository, a minimalistic repository.
 */
public class LocalRepository {
	
	/** The log category. */
	protected static final String LOG_CATEGORY = "Loupe.Local Repository";

	/** The Constant REPOSITORY_TEMP_FOLDER. */
	private static final String REPOSITORY_TEMP_FOLDER = "Temp";
	
	/** The Constant REPOSITORY_ARCHIVE_FOLDER. */
	public static final String REPOSITORY_ARCHIVE_FOLDER = "Archive";
	
	/** The Constant REPOSITORY_KEY_FILE. */
	public static final String REPOSITORY_KEY_FILE = "repository.gak";
	
	/** The Constant COMPUTER_KEY_FILE. */
	public static final String COMPUTER_KEY_FILE = "computer.gak";

	/** The lock. */
	private final Object lock = new Object();
	
	/** The queue lock. */
	private final Object queueLock = new Object();
	
	/** The refresh requests. */
	private final ConcurrentLinkedQueue<RefreshRequest> refreshRequests = new ConcurrentLinkedQueue<RefreshRequest>(); // protected
																														// by
	/** The caption. */
																														// QUEUELOCK
	private String caption;
	
	/** The repository path. */
	private String repositoryPath;
	
	/** The repository id. */
	private UUID repositoryId;
	
	/** The repository temp path. */
	private String repositoryTempPath;
	
	/** The session lock folder. */
	private String sessionLockFolder;

	/** The session cache. */
	private Map<UUID, SessionFileInfo<File>> sessionCache; // protected by LOCK
	
	/** The logging enabled. */
	private boolean loggingEnabled;
	
	/** The repository archive path. */
	private String repositoryArchivePath;
	
	/** The async refresh thread active. */
	private boolean asyncRefreshThreadActive; // protected by QUEUELOCK
	
	/**
	 * A single request to refresh our local cache of file information.
	 */
	private static class RefreshRequest {
		
		/**
		 * Instantiates a new refresh request.
		 *
		 * @param force the force
		 * @param sessionCriteria the session criteria
		 */
		public RefreshRequest(boolean force, EnumSet<SessionCriteria> sessionCriteria) {
			setCriteria(sessionCriteria);
			setForce(force);
			setTimestamp(LocalDateTime.now());
		}

		/** When the request was made. */
		private LocalDateTime timestamp = LocalDateTime.MIN;

		/**
		 * Gets the timestamp.
		 *
		 * @return the timestamp
		 */
		public final LocalDateTime getTimestamp() {
			return this.timestamp;
		}

		/**
		 * Sets the timestamp.
		 *
		 * @param value the new timestamp
		 */
		private void setTimestamp(LocalDateTime value) {
			this.timestamp = value;
		}

		/** What sessions should be covered by the request. */
		private EnumSet<SessionCriteria> criteria;

		/**
		 * Gets the criteria.
		 *
		 * @return the criteria
		 */
		public final EnumSet<SessionCriteria> getCriteria() {
			return this.criteria;
		}

		/**
		 * Sets the criteria.
		 *
		 * @param value the new criteria
		 */
		private void setCriteria(EnumSet<SessionCriteria> value) {
			this.criteria = value;
		}

		/** If a refresh should be forced even if we don't think the data is dirty. */
		private boolean force;

		/**
		 * Gets the force.
		 *
		 * @return the force
		 */
		public final boolean getForce() {
			return this.force;
		}

		/**
		 * Sets the force.
		 *
		 * @param value the new force
		 */
		private void setForce(boolean value) {
			this.force = value;
		}

	}

	/**
	 * Open a specific local repository.
	 *
	 * @param productName The product name for operations in this repository
	 * @throws IOException Signals that an I/O exception has occurred.
	 */

	public LocalRepository(String productName) throws IOException {
		this(productName, null);
	}

	/**
	 * Open a specific local repository.
	 *
	 * @param productName  The product name for operations in this repository
	 * @param overridePath The path to use instead of the default path for the
	 *                     repository
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public LocalRepository(String productName, String overridePath) throws IOException {
		
		this.caption = productName;

		// now, before we can use the product name we need to make sure it doesn't have
		// any illegal characters.
		this.repositoryPath = calculateRepositoryPath(productName, overridePath);
		this.repositoryTempPath = Paths.get(this.repositoryPath).resolve(REPOSITORY_TEMP_FOLDER).toString();
		this.sessionLockFolder = Paths.get(this.repositoryPath).resolve(FileMessenger.SESSION_LOCK_FOLDER_NAME)
				.toString();
		this.repositoryArchivePath = Paths.get(this.repositoryPath).resolve(REPOSITORY_ARCHIVE_FOLDER).toString();

		// we want the directories to exist, but we don't worry about permissions
		// because that should have already happened when the repository path was
		// calculated.
		try {
			(new File(this.repositoryTempPath)).mkdirs();
		} catch (RuntimeException ex) {

		}

		try {
			(new File(this.repositoryArchivePath)).mkdirs();
		} catch (RuntimeException ex) {

		}

		try (InterprocessLock lock = InterprocessLockManager.getInstance().lock(this, this.repositoryPath,
				RepositoryMaintenance.MUTIPROCESS_LOCK_NAME, 0, true)) {
			// if the repository doesn't have a readme file and our basic information,
			// create that.
			File file = Paths.get(this.repositoryPath).resolve("_ReadMe.txt").toFile();
			if (!file.isFile()) {
				try {
					Files.write(file.toPath(),
							"This directory contains log files.  You may delete log files (*.glf) safely, however renaming them is not recommended."
									.getBytes(),
							StandardOpenOption.CREATE);

				} catch (Exception ex) {
					if (!Log.getSilentMode()) {
						Log.write(LogMessageSeverity.WARNING, LogWriteMode.QUEUED, new GibraltarException(ex),
								LOG_CATEGORY, "Unable to create readme file in repository",
								"Path: %s\r\nException: %s", this.repositoryPath, ex.getMessage());
					}
				}
			}

			Path repositoryIdPath = Paths.get(this.repositoryPath).resolve(REPOSITORY_KEY_FILE);
			if (repositoryIdPath.toFile().isFile()) {
				// read back the existing repository id
				try {
					this.repositoryId = UUID
							.fromString(new String(Files.readAllBytes(repositoryIdPath), StandardCharsets.UTF_8));
				} catch (RuntimeException | IOException ex) {
					if (!Log.getSilentMode()) {
						Log.write(LogMessageSeverity.ERROR, LogWriteMode.QUEUED, new GibraltarException(ex),
								LOG_CATEGORY, "Unable to read repository Id, a new one will be created",
								"Path: %s\r\nException: %s", this.repositoryPath, ex.getMessage());
					}
				}
			}

			// create a new repository id
			if (this.repositoryId == null) {
				this.repositoryId = UUID.randomUUID();
				try {
					if (!repositoryIdPath.toFile().exists())
						repositoryIdPath.toFile().createNewFile();
					
					Files.write(repositoryIdPath,
							new String(this.repositoryId.toString().getBytes(), StandardCharsets.UTF_8).getBytes(),
							StandardOpenOption.TRUNCATE_EXISTING);
					Files.setAttribute(repositoryIdPath, "dos:hidden", true);
				} catch (RuntimeException | IOException ex) {
					if (!Log.getSilentMode()) {
						Log.write(LogMessageSeverity.ERROR, LogWriteMode.QUEUED, new GibraltarException(ex),
								LOG_CATEGORY,
								"Unable to store repository Id in repository.  This will lead to server integration challenges",
								"Path: %s\r\nException: %s", this.repositoryPath, ex.getMessage());
					}
				}
			}
		}
	}

	/**
	 * Calculate the best path for the log folder and the repository.
	 *
	 * @param productName the product name
	 * @return the string
	 */

	public static String CalculateRepositoryPath(String productName) {
		return calculateRepositoryPath(productName, null);
	}

	/**
	 * Calculate the best path for the log folder and the repository.
	 *
	 * @param productName the product name
	 * @param overridePath the override path
	 * @return the string
	 */
	public static String calculateRepositoryPath(String productName, String overridePath) {
		String repositoryFolder = PathManager.findBestPath(PathType.COLLECTION, overridePath);

		// now, we either just calculated a DEFAULT folder (which is just the base
		// directory) or a final one.
		if (TypeUtils.isBlank(overridePath)) {
			// we may need to adjust product name - we have to make sure it's valid for
			// being a directory.
			productName = FileSystemTools.sanitizeFileName(productName); // we use the more restrictive file name rules
																			// since we're doing just one directory
			repositoryFolder = Paths.get(repositoryFolder).resolve(productName).toString();
		}

		return repositoryFolder;
	}

	/**
	 * Calculate the best path for the default.
	 *
	 * @return the default repository path
	 */
	public static String getDefaultRepositoryPath() {
		return PathManager.findBestPath(PathType.COLLECTION, null);
	}

	/**
	 * A unique id for this repository.
	 *
	 * @return the id
	 */
	public final UUID getId() {
		return this.repositoryId;
	}

	/**
	 * Indicates if there are unsaved changes.
	 *
	 * @return true, if is dirty
	 */
	public final boolean isDirty() {
		return false;
	}

	/**
	 * Indicates if the repository is read only (sessions can't be added or
	 * removed).
	 *
	 * @return true, if is read only
	 */
	public final boolean isReadOnly() {
		return false;
	}

	/**
	 * The unique name for this repository (typically the file name or URI).
	 *
	 * @return the name
	 */
	public final String getName() {
		return this.repositoryPath;
	}

	/**
	 * A short end-user caption to display for the repository.
	 *
	 * @return the caption
	 */
	public final String getCaption() {
		return this.caption;
	}

	/**
	 * An extended end-user description of the repository.
	 *
	 * @return the description
	 */
	public final String getDescription() {
		return this.repositoryPath;
	}

	/**
	 * Indicates if the repository supports fragment files or not. Most do.
	 *
	 * @return the supports fragments
	 */
	public final boolean getSupportsFragments() {
		return true;
	}

	/**
	 * Retrieve the ids of the sessions files known locally for the specified
	 * session.
	 *
	 * @param sessionId the session id
	 * @return the session file ids
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final List<UUID> getSessionFileIds(UUID sessionId) throws IOException {
		List<UUID> sessionFileIds = new ArrayList<UUID>();
		synchronized (this.lock) {
			ensureCacheLoaded();

			SessionFileInfo<File> sessionFileInfo;
			if (this.sessionCache.containsKey(sessionId)) // we want the not found exception
			{
				sessionFileInfo = this.sessionCache.get(sessionId);
				for (File fragment : sessionFileInfo.getFragments()) {
					try {
						SessionHeader header = loadSessionHeader(fragment.getAbsolutePath());
						sessionFileIds.add(header.getFileId());
					} catch (Exception ex) {
						if (!Log.getSilentMode()) {
							Log.write(LogMessageSeverity.ERROR, LogWriteMode.QUEUED, ex, true, LOG_CATEGORY,
									"Unable to read session fragment file header due to " + ex.getClass(),
									"We will skip this file for the session.\r\nSession Id: %s\r\n%s", sessionId,
									ex.getMessage());
						}
					}
				}
			}
		}

		return sessionFileIds;
	}

	/**
	 * Indicates if the database should log operations to Gibraltar or not.
	 *
	 * @return true, if is logging enabled
	 */
	public final boolean isLoggingEnabled() {
		return this.loggingEnabled;
	}

	/**
	 * Sets the checks if is logging enabled.
	 *
	 * @param value the new checks if is logging enabled
	 */
	public final void setIsLoggingEnabled(boolean value) {
		this.loggingEnabled = value;
	}

	/**
	 * Get a generic stream for the contents of a session file.
	 *
	 * @param sessionId The unique Id of the session to retrieve the stream for.
	 * @param fileId    The unique Id of the session file to retrieve the stream
	 *                  for.
	 * @return A stream that should be immediately copied and then disposed. If no
	 *         file could be found with the provided Id an exception will be thrown.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final RandomAccessFile loadSessionFile(UUID sessionId, UUID fileId) throws IOException {
		RandomAccessFile file = tryLoadSessionFile(sessionId, fileId);
		if (file == null) {
			throw new IllegalStateException(
					"There is no session file with the Id " + fileId + " for session Id " + sessionId);
		}

		return file;
	}

	/**
	 * Try to get a stream pointing to a live file.
	 *
	 * @param sessionId the session id
	 * @param fileId the file id
	 * @return True if a file stream was found, false otherwise
	 * A stream that should be immediately copied and then disposed.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final RandomAccessFile tryLoadSessionFile(UUID sessionId, UUID fileId) throws IOException {
		ensureCacheLoaded();

		File fragmentFile = null;
		synchronized (this.lock) {
			SessionFileInfo<File> sessionFileInfo = this.sessionCache.get(sessionId);
			fragmentFile = findFragment(sessionFileInfo, fileId);

			if (sessionFileInfo == null || fragmentFile == null)
				return null;

			try {
				return new RandomAccessFile(fragmentFile, "r");
			} catch (FileNotFoundException ex) {
				// KM: Since this method is a "try" we will treat this as also returning null
				// without complaint.
				return null;
			}
		}
	}

	/**
	 * Find the session fragments in our local repository for the specified session
	 * Id.
	 *
	 * @param sessionId the session id
	 * @return the session file info
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final SessionFileInfo<File> loadSessionFiles(UUID sessionId) throws IOException {
		ensureCacheLoaded();

		synchronized (this.lock) {
			if (this.sessionCache.containsKey(sessionId)) {
				return this.sessionCache.get(sessionId);
			}

			return null;
		}
	}

	/**
	 * Perform an immediate, synchronous refresh.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final void refresh() throws IOException {
		refresh(false, true, EnumSet.of(SessionCriteria.ALL_SESSIONS));
	}

	/**
	 * Update the local repository with the latest information from the file system.
	 *
	 * @param async the async
	 * @param force the force
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final void refresh(boolean async, boolean force) throws IOException {
		refresh(async, force, EnumSet.of(SessionCriteria.ALL_SESSIONS));
	}

	/**
	 * Update the local repository with the latest information from the file system.
	 *
	 * @param async the async
	 * @param force the force
	 * @param sessionCriteria the session criteria
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final void refresh(boolean async, boolean force, EnumSet<SessionCriteria> sessionCriteria) throws IOException {
		if (async) {
			// because we want minimize any possibility in holding up the caller (which
			// could be the file system monitor)
			// we queue all requests and even use a dedicated lock to minimize contention
			synchronized (this.queueLock) {
				if (this.refreshRequests.size() < 10) // circuit breaker for extreme cases
				{
					this.refreshRequests.offer(new RefreshRequest(force, sessionCriteria));
				}

				if (!this.asyncRefreshThreadActive) {
					this.asyncRefreshThreadActive = true;
					Multiplexer.run(new Runnable() {
						@Override
						public void run() {
							asyncRefresh();
						}
					});
				}
			}
		} else {
			performRefresh(force, sessionCriteria);
		}
	}

	/**
	 * Remove a session from the repository and all folders by its Id.
	 *
	 * @param sessionId The unique Id of the session to be removed
	 * @return True if a session existed and was removed, false otherwise. If no
	 *         session is found with the specified Id then no exception is thrown.
	 *         Instead, false is returned. If a session is found and removed True is
	 *         returned. If there is a problem removing the specified session (and
	 *         it exists) then an exception is thrown. The session will be removed
	 *         from all folders that may reference it as well as user history and
	 *         preferences.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final boolean remove(UUID sessionId) throws IOException {
		synchronized (this.lock) {
			ensureCacheLoaded();

			SessionFileInfo<File> sessionFileInfo = null;
			if (!this.sessionCache.containsKey(sessionId)) {
				return false; // can't remove what ain't there.
			}

			sessionFileInfo = this.sessionCache.get(sessionId);

			// kill all of these files
			boolean fileRemoved = false;
			ArrayList<File> filesToRemove = new ArrayList<File>(sessionFileInfo.getFragments()); // since the collection
																									// could get
																									// modified as we
																									// go.
			for (File fragment : filesToRemove) {
				fileRemoved = FileUtils.safeDeleteFile(fragment) || fileRemoved;
			}
			return fileRemoved;
		}
	}

	/**
	 * Remove sessions from the repository and all folders by its Id.
	 *
	 * @param sessionIds An array of the unique Ids of the sessions to be removed
	 * @return True if a session existed and was removed, false otherwise. If no
	 *         sessions are found with the specified Ids then no exception is
	 *         thrown. Instead, false is returned. If at least one session is found
	 *         and removed True is returned. If there is a problem removing one or
	 *         more of the specified sessions (and it exists) then an exception is
	 *         thrown. The sessions will be removed from all folders that may
	 *         reference it as well as user history and preferences.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final boolean remove(List<UUID> sessionIds) throws IOException {
		boolean fileRemoved = false;
		for (UUID sessionId : sessionIds) {
			fileRemoved = remove(sessionId) || fileRemoved;
		}
		return fileRemoved;
	}

	/**
	 * Remove a session from the repository and all folders by its Id.
	 *
	 * @param sessionId The unique Id of the session to be removed
	 * @param fileId    The unique Id of the session fragment to be removed
	 * @return True if a session existed and was removed, false otherwise. If no
	 *         session is found with the specified Id then no exception is thrown.
	 *         Instead, false is returned. If a session is found and removed True is
	 *         returned. If there is a problem removing the specified session (and
	 *         it exists) then an exception is thrown. The session will be removed
	 *         from all folders that may reference it as well as user history and
	 *         preferences.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final boolean remove(UUID sessionId, UUID fileId) throws IOException {
		synchronized (this.lock) {
			ensureCacheLoaded();

			SessionFileInfo<File> sessionFileInfo = null;
			if (!this.sessionCache.containsKey(sessionId)) {
				return false; // can't remove what ain't there.
			}

			sessionFileInfo = this.sessionCache.get(sessionId);

			// now scan the files in the cache to see if the file they want is still there.
			File victim = null;
			for (File fragment : sessionFileInfo.getFragments()) {
				SessionHeader fileHeader = loadSessionHeader(fragment.getAbsolutePath());
				if ((fileHeader != null) && (fileHeader.getFileId().equals(fileId))) {
					victim = fragment;
					break;
				}
			}

			if (victim == null) {
				return false;
			}

			return FileUtils.safeDeleteFile(victim);
		}
	}

	/**
	 * Find if session data (more than just the header information) exists for a
	 * session with the provided Id.
	 *
	 * @param sessionId The unique Id of the session to be checked.
	 * @return True if the repository has at least some session data in the
	 *         repository, false otherwise.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final boolean sessionDataExists(UUID sessionId) throws IOException {
		synchronized (this.lock) {
			ensureCacheLoaded();
			return this.sessionCache.containsKey(sessionId);
		}
	}

	/**
	 * Find if session data (more than just the header information) exists for a
	 * session with the provided Id.
	 *
	 * @param sessionId The unique Id of the session to be checked.
	 * @param fileId    The unique Id of the session fragment to be checked.
	 * @return True if the repository has the indicated session fragment in the
	 *         repository, false otherwise.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final boolean sessionDataExists(UUID sessionId, UUID fileId) throws IOException {
		synchronized (this.lock) {
			ensureCacheLoaded();

			File fragmentFileInfo = null;

			SessionFileInfo<File> sessionFileInfo;
			if (this.sessionCache.containsKey(sessionId)) {
				sessionFileInfo = this.sessionCache.get(sessionId);
				fragmentFileInfo = findFragment(sessionFileInfo, fileId);
			}

			return (fragmentFileInfo != null);
		}
	}

	/**
	 * Find if a session exists with the provided Id.
	 *
	 * @param sessionId The unique Id of the session to be checked.
	 * @return True if the session exists in the repository, false otherwise.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final boolean sessionExists(UUID sessionId) throws IOException {
		synchronized (this.lock) {
			ensureCacheLoaded();
			return this.sessionCache.containsKey(sessionId);
		}
	}

	/**
	 * Find if the session is running with the provided Id.
	 *
	 * @param sessionId The unique Id of the session to be checked.
	 * @return True if the session exists in the repository and is running, false
	 *         otherwise.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final boolean sessionIsRunning(UUID sessionId) throws IOException {
		synchronized (this.lock) {
			ensureCacheLoaded();

			SessionFileInfo<File> sessionFileInfo;
			if (this.sessionCache.containsKey(sessionId)) {
				sessionFileInfo = this.sessionCache.get(sessionId);
				return sessionFileInfo.isRunning();
			} else {
				return false;
			}
		}
	}

	/**
	 * Set or clear the New flag for a sessions.
	 *
	 * @param sessionId The session to affect
	 * @param isNew     True to mark the sessions as new, false to mark them as not
	 *                  new.
	 * @return True if a session was changed, false otherwise.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final boolean setSessionNew(UUID sessionId, boolean isNew) throws IOException {
		boolean modifiedAnyFile = false;
		synchronized (this.lock) {
			ensureCacheLoaded();

			String destinationDirectory = isNew ? this.repositoryPath : this.repositoryArchivePath;

			modifiedAnyFile = setSessionNew(destinationDirectory, sessionId, isNew);

			invalidateCache();
		}

		return modifiedAnyFile;
	}

	/**
	 * Set or clear the New flag for a list of sessions.
	 *
	 * @param sessionIds The sessions to affect
	 * @param isNew      True to mark the sessions as new, false to mark them as not
	 *                   new.
	 * @return True if a session was changed, false otherwise.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final boolean setSessionsNew(List<UUID> sessionIds, boolean isNew) throws IOException {
		boolean modifiedAnyFile = false;
		synchronized (this.lock) {
			ensureCacheLoaded();

			String destinationDirectory = isNew ? this.repositoryPath : this.repositoryArchivePath;

			for (UUID sessionId : sessionIds) {
				modifiedAnyFile = setSessionNew(destinationDirectory, sessionId, isNew);
			}

			invalidateCache();
		}

		return modifiedAnyFile;
	}

	/**
	 * Retrieves all the sessions that match the conditions defined by the specified
	 * predicate.
	 *
	 * @param match The Predicate delegate
	 *              that defines the conditions of the sessions to search for.
	 * 
	 *              The Predicate is a
	 *              delegate to a method that returns true if the object passed to
	 *              it matches the conditions defined in the delegate. The sessions
	 *              of the repository are individually passed to the
	 *              Predicate delegate, moving
	 *              forward in the List, starting with the first session and ending
	 *              with the last session.
	 * @return A List containing all the sessions that match the conditions defined
	 *         by the specified predicate, if found; otherwise, an empty List.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final ISessionSummaryCollection find(java.util.function.Predicate<ISessionSummary> match)
			throws IOException {
		if (match == null) {
			throw new NullPointerException("match");
		}

		SessionSummaryCollection collection = new SessionSummaryCollection(this);

		synchronized (this.lock) {
			ensureCacheLoaded();

			for (SessionFileInfo<File> sessionFileInfo : this.sessionCache.values()) {
				if (match.test(sessionFileInfo.getHeader())) {
					collection.add(sessionFileInfo.getHeader());
				}
			}
		}

		return collection;
	}

	/**
	 * The set of all sessions in the repository.
	 * 
	 * <p>
	 * This contains the summary information. To load the full contents of a a
	 * session where local data files are available use the LoadSession method.
	 * </p>
	 * <p>
	 * The supplied collection is a binding list and supports update events for the
	 * individual sessions and contents of the repository.
	 * </p>
	 *
	 * @return the sessions
	 */
	public ISessionSummaryCollection getSessions() {
		throw new UnsupportedOperationException(
				"A general sessions collection isn't available in the raw local repository");
	}

	/**
	 * A temporary path within the repository that can be used to store working data.
	 *
	 * @return the temp path
	 */
	public final String getTempPath() {
		return this.repositoryTempPath;
	}

	/**
	 * Attempt to load the session header from the specified file, returning null if
	 * it can't be loaded.
	 *
	 * @param sessionFileNamePath The full file name &amp; path
	 * @return The session header, or null if it can't be loaded
	 */
	public static SessionHeader loadSessionHeader(String sessionFileNamePath) {
		SessionHeader header = null;

		try (RandomAccessFile sourceFile = new RandomAccessFile(new File(sessionFileNamePath), "r")) {
			GLFReader sourceGlfFile = new GLFReader(sourceFile);
			if (sourceGlfFile.isGLF()) {
				header = sourceGlfFile.getSessionHeader();
			}
		} catch (Exception ex) {
			if (!Log.getSilentMode()) {
				Log.write(LogMessageSeverity.WARNING, LogWriteMode.QUEUED, ex, LOG_CATEGORY,
						"Unexpected exception while attempting to load a session header",
						"While opening the file '%s' an exception was thrown reading the session header. This may indicate a corrupt file.\r\nException: %s\r\n",
						sessionFileNamePath, ex.getMessage());
			}
		}

		return header;
	}

	/**
	 * The path on disk to the repository.
	 *
	 * @return the repository path
	 */
	public final Path getRepositoryPath() {
		return this.repositoryPath != null ? Paths.get(this.repositoryPath) : null;
	}

	/**
	 * The path on disk to the repository session locks.
	 *
	 * @return the repository lock path
	 */
	protected final String getRepositoryLockPath() {
		return this.sessionLockFolder;
	}

	/**
	 * Called by the base class to refresh cached data.
	 *
	 * @param force the force
	 */
	protected void onRefresh(boolean force) {

	}

	/**
	 * The current session cache.
	 *
	 * @return the session cache
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected Map<UUID, SessionFileInfo<File>> getSessionCache() throws IOException {
		ensureCacheLoaded();
		return this.sessionCache;
	}

	/**
	 * Async refresh.
	 */
	private void asyncRefresh() {
		boolean exitSet = false;
		try {
			RefreshRequest request;
			do {
				request = null;
				synchronized (this.queueLock) {
					if (this.refreshRequests.isEmpty()) {
						// the queue is empty, lets explicitly bail and mark that we're doing so to
						// guarantee no race conditions with parties queuing.
						this.asyncRefreshThreadActive = false;
						exitSet = true;
						return;
					}

					request = this.refreshRequests.poll();
				}

				if (request != null) {
					performRefresh(request.getForce(), request.getCriteria());
				}

			} while (request != null);
		} catch (Exception ex) {
			if (!Log.getSilentMode()) {
				try {
					Log.recordException(0, ex, null, LOG_CATEGORY, true);
				} catch (IOException e) {
					// do nothing, we're hosed.
				}
			}
		} finally {
			if (!exitSet) {
				// we want to be really, really sure we don't leave the thread active option set
				// when we're no longer running even in ThreadAbort cases.
				synchronized (this.queueLock) {
					this.asyncRefreshThreadActive = false;
				}
			}
		}
	}

	/**
	 * Perform refresh.
	 *
	 * @param force the force
	 * @param sessionCriteria the session criteria
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void performRefresh(boolean force, EnumSet<SessionCriteria> sessionCriteria) throws IOException {
		if (force) {
			updateCache(sessionCriteria);
		} else {
			invalidateCache();
		}

		onRefresh(force);
	}

	/**
	 * Ensure cache loaded.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void ensureCacheLoaded() throws IOException {
		synchronized (this.lock) {
			if (this.sessionCache == null) {
				updateCache();
			}
		}
	}

	/**
	 * Invalidate cache.
	 */
	private void invalidateCache() {
		synchronized (this.lock) {
			this.sessionCache = null;
		}
	}

	/**
	 * Finds the specified file fragment in the provided session if it exists.
	 *
	 * @param sessionFileInfo the session file info
	 * @param fileId the file id
	 * @return the file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static File findFragment(SessionFileInfo<File> sessionFileInfo, UUID fileId) throws IOException {
		File file = null;
		for (File fileInfo : sessionFileInfo.getFragments()) {
			SessionHeader header = loadSessionHeader(fileInfo.getAbsolutePath());
			if ((header != null) && (header.getFileId().equals(fileId))) {
				file = fileInfo;
				break;
			}
		}
		return file;
	}

	/**
	 * Immediately update the cache from disk.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */

	protected final void updateCache() throws IOException {
		updateCache(EnumSet.of(SessionCriteria.ALL_SESSIONS));
	}

	/**
	 * Update cache.
	 *
	 * @param sessionCriteria the session criteria
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected final void updateCache(EnumSet<SessionCriteria> sessionCriteria) throws IOException {
		synchronized (this.lock) {
			Map<UUID, SessionFileInfo<File>> newSessionCache = loadSessions(sessionCriteria);
			this.sessionCache = newSessionCache;
		}
	}

	/**
	 * Scan the repository directory for all of the log files for this repository an
	 * build an index on the fly.
	 *
	 * @param sessionCriteria the session criteria
	 * @return A new index of the sessions in the folder
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private Map<UUID, SessionFileInfo<File>> loadSessions(EnumSet<SessionCriteria> sessionCriteria) throws IOException {
		if (this.loggingEnabled) {
			Log.write(LogMessageSeverity.VERBOSE, LOG_CATEGORY, "Creating index of local repository sessions",
					"Repository Folder: %s", this.repositoryPath);
		}

		HashMap<UUID, SessionFileInfo<File>> sessions = new HashMap<UUID, SessionFileInfo<File>>();
		HashMap<UUID, SessionFileInfo<File>> crashConversionCandidates = new HashMap<UUID, SessionFileInfo<File>>();

		// load up our index
		loadSessionsFromDirectory(this.repositoryPath, sessions, crashConversionCandidates, true);

		// optimization - for special case where we are only interested in new sessions
		// and active we ignore the archive.
		if (sessionCriteria.contains(SessionCriteria.COMPLETED)) {
			loadSessionsFromDirectory(this.repositoryArchivePath, sessions, crashConversionCandidates, false);
		}

		// We have to check sessions from the regular and archive paths to see if
		// they're running because
		// the true open session file won't be in any of these lists (it's locked)
		Map<UUID, Boolean> runningSessionCache = new HashMap<UUID, Boolean>();
		for (SessionFileInfo<File> sessionFileInfo : sessions.values()) {
			if ((!runningSessionCache.containsKey(sessionFileInfo.getId()))) {
				sessionFileInfo.setIsRunning(isSessionRunning(sessionFileInfo.getId()));
				runningSessionCache.put(sessionFileInfo.getId(), sessionFileInfo.isRunning());
			} else {
				sessionFileInfo.setIsRunning(false);
			}
		}

		// If our session load identified any running session files then perform crashed
		// session conversion
		for (SessionFileInfo<File> sessionFileInfo : crashConversionCandidates.values()) {
			checkAndPerformCrashedSessionConversion(sessionFileInfo);
		}

		if (this.loggingEnabled) {
			Log.write(LogMessageSeverity.VERBOSE, LOG_CATEGORY, "Finished creating index of local repository sessions",
					"Repository Folder: %s\r\nSessions found: %d", this.repositoryPath, sessions.size());
		}
		return sessions;
	}

	/**
	 * Load sessions from directory.
	 *
	 * @param directory the directory
	 * @param sessions the sessions
	 * @param crashConversionCandidates the crash conversion candidates
	 * @param isNew the is new
	 */
	private void loadSessionsFromDirectory(String directory, HashMap<UUID, SessionFileInfo<File>> sessions,
			HashMap<UUID, SessionFileInfo<File>> crashConversionCandidates, boolean isNew) {
		if (!(new File(directory)).isDirectory()) {
			return;
		}

		List<File> allSessionFiles = new ArrayList<File>();
		try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(directory),
				"*." + Log.LOG_EXTENSION)) {
			// even though we just pre-checked it may have been deleted between there and
			// here.
			for (Path path : dirStream) {
				File file = path.toFile();
				if (file.isFile()) {
					allSessionFiles.add(file);
				}
			}
		} catch (DirectoryNotFoundException e) {
			return;
		} catch (UnauthorizedAccessException e2) // if we are in-flight deleting the directory we'll get this.
		{
			return;
		} catch (IOException e1) {
			return;
		}

		for (File sessionFragment : allSessionFiles) {
			SessionHeader sessionHeader = loadSessionHeader(sessionFragment.getPath());

			if (sessionHeader == null) {
				if (this.loggingEnabled) {
					Log.write(LogMessageSeverity.INFORMATION, LOG_CATEGORY,
							"Skipping local repository fragment because the session header couldn't be loaded",
							"File: %s", sessionFragment.getName());
				}
			} else {
				SessionFileInfo<File> sessionFileInfo;
				if (sessions.containsKey(sessionHeader.getId())) {
					// add this file fragment to the existing session info
					sessionFileInfo = sessions.get(sessionHeader.getId());
					sessionFileInfo.addFragment(sessionHeader, sessionFragment, isNew);
				} else {
					// create a new session file info - this is the first we've seen this session.
					sessionFileInfo = new SessionFileInfo<File>(sessionHeader, sessionFragment, isNew);
					sessions.put(sessionFileInfo.getId(), sessionFileInfo);
				}

				// and if the session header thought it was running, we need to queue it for
				// potential crashed session conversion.
				if ((sessionHeader.getStatus() == SessionStatus.RUNNING)
						&& (!crashConversionCandidates.containsKey(sessionHeader.getId()))) {
					crashConversionCandidates.put(sessionHeader.getId(), sessionFileInfo);
				}
			}
		}
	}

	/**
	 * Indicates if the current session is running.
	 *
	 * @param sessionId the session id
	 * @return true, if is session running
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private boolean isSessionRunning(UUID sessionId) throws IOException {
		// this method is faster than actually acquiring the lock
		if (InterprocessLockManager.getInstance().queryLockAvailable(this, this.sessionLockFolder,
				sessionId.toString())) {
			return false;
		}

		return true;
	}

	/**
	 * Checks a running session to see if it should be converted to a crashed
	 * session, and if so converts it.
	 *
	 * @param session The full set of session information
	 * @return True if it was changed, false otherwise.
	 */
	private boolean checkAndPerformCrashedSessionConversion(SessionFileInfo<File> session) {
		boolean haveChanges = false;

		try {
			try (InterprocessLock sessionLock = getRunningSessionLock(session.getId())) {
				if (sessionLock == null) {
					return haveChanges; // It's still locked (thus still running), continue to the next running session.
				}

				boolean convertedCurrentSession = false;
				for (File fileFragment : session.getFragments()) {
					// And change each one to indicate that it's crashed.
					if (this.loggingEnabled) {
						Log.write(LogMessageSeverity.VERBOSE, LOG_CATEGORY, "Opening Session File",
								"Opening session %s using file '%s'.", session.getId(), fileFragment.getName());
					}
					RandomAccessFile sourceFile = null;
					GLFReader sourceGlfFile = null;

					try {
						if (fileFragment.exists() && fileFragment.canWrite()) {
							if (this.loggingEnabled) {
								Log.write(LogMessageSeverity.WARNING, LOG_CATEGORY, "Unable to Mark Session as Crashed",
										"Unable to completely convert session %s from being marked as running to crashed in repository at '%s' because the fragment '%s' could not be opened",
										session.getId(), this.repositoryPath, fileFragment.getName());
							}

							continue; // Otherwise, try the next fragment.
						}

						sourceFile = new RandomAccessFile(fileFragment, "rw");
						sourceGlfFile = new GLFReader(sourceFile);

						// update the GLF to crashed
						sourceGlfFile.getSessionHeader().setStatusName(SessionStatus.CRASHED.toString());
						GLFWriter.updateSessionHeader(sourceGlfFile, sourceFile);
						convertedCurrentSession = true;
					} catch (RuntimeException ex) {

						if (this.loggingEnabled) {
							Log.write(LogMessageSeverity.WARNING, LogWriteMode.QUEUED, ex, LOG_CATEGORY,
									"Unable to Mark Session as Crashed",
									"Unable to completely convert session %s from being marked as running to crashed in repository at '%s' because the fragment '%s' failed due to an exception:\r\n %s",
									session.getId(), this.repositoryPath, fileFragment.getName(), ex.getMessage());
						}
					} finally {
						// we have to dispose of our file writer
						IOUtils.closeQuietly(sourceFile);
					}
				}

				if (convertedCurrentSession) {
					// We've converted this session, so mark that we had changes...
					haveChanges = true;
					session.getHeader().setStatusName(SessionStatus.CRASHED.toString()); // we have to update this or
																							// they'll not see the
																							// current status.
					sessionLock.setDisposeProxyOnClose(true); // We've removed session from index, so we won't need that
																// lock again.
				}
			} catch (IOException e) {
				throw e;
			}
		} catch (Exception ex) {

			if (this.loggingEnabled) {
				Log.write(LogMessageSeverity.WARNING, LOG_CATEGORY, "Unable to Mark Session as Crashed",
						"Unable to completely convert a session from being marked as running to crashed in repository at %s due to an exception:\r\n %s",
						this.repositoryPath, ex.getMessage());
			}
		}

		return haveChanges;
	}

	/**
	 * Get the current running session lock.
	 *
	 * @param sessionId the session id
	 * @return Null if the lock couldn't be acquired, the InterprocessLock otherwise
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private InterprocessLock getRunningSessionLock(UUID sessionId) throws IOException {
		return InterprocessLockManager.getInstance().lock(this, this.sessionLockFolder, sessionId.toString(), 0, true);
	}

	/**
	 * Changes the new status of a single session.
	 *
	 * @param destinationDirectory the destination directory
	 * @param sessionId the session id
	 * @param isNew the is new
	 * @return true, if successful
	 */
	private boolean setSessionNew(String destinationDirectory, UUID sessionId, boolean isNew) {
		boolean modifiedAnyFile = false;
		SessionFileInfo<File> sessionFileInfo;
		if (this.sessionCache.containsKey(sessionId)) {
			sessionFileInfo = this.sessionCache.get(sessionId);
			for (File fragment : sessionFileInfo.getFragments()) {
				try {
					if (!destinationDirectory.equalsIgnoreCase(fragment.getParentFile().getName())) {
						// make sure there isn't a file already there (can happen in rare race
						// conditions)
						FileUtils.safeDeleteFile(Paths.get(destinationDirectory).resolve(fragment.getName()).toFile());

						// and then move the file to the new location.
						(new File(destinationDirectory)).mkdirs();

						Files.move(fragment.toPath(), Paths.get(destinationDirectory).resolve(fragment.getName()),
								StandardCopyOption.ATOMIC_MOVE);
						modifiedAnyFile = true;
					}
				} catch (FileNotFoundException ex) {
					if (!Log.getSilentMode()) {
						Log.write(LogMessageSeverity.INFORMATION, LogWriteMode.QUEUED, new GibraltarException(ex), true,
								LOG_CATEGORY,
								"While changing a session fragment file new state to " + isNew
										+ " the file was not found",
								"It's most likely already been moved to the appropriate status or was deleted.\r\nSession Id: %s\r\nFragment: %s\r\nException:\r\n%s: %s",
								sessionId, fragment.getName(), ex.getClass().getName(), ex.getMessage());
					}
				} catch (Exception ex) {
					if (!Log.getSilentMode()) {
						Log.write(LogMessageSeverity.WARNING, LogWriteMode.QUEUED, ex, true, LOG_CATEGORY,
								"Unable to update session fragment new state to " + isNew + " due to " + ex.getClass(),
								"It's most likely in use by another process, so we'll have another opportunity to get it later.\r\nSession Id: %s\r\nFragment: %s\r\nException:\r\n%s: %s",
								sessionId, fragment.getName(), ex.getClass().getName(), ex.getMessage());
					}
				}
			}

			// and if we moved it, drop it from the cache so we don't try to touch it again
			// this round (we'll dump the whole cache in a second)
			this.sessionCache.remove(sessionId);
		}

		return modifiedAnyFile;
	}

}