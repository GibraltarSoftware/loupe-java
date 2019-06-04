package com.onloupe.core.messaging;

import com.onloupe.configuration.IMessengerConfiguration;
import com.onloupe.configuration.SessionFileConfiguration;
import com.onloupe.core.FileSystemTools;
import com.onloupe.core.data.GLFWriter;
import com.onloupe.core.data.InterprocessLock;
import com.onloupe.core.data.InterprocessLockManager;
import com.onloupe.core.data.RepositoryMaintenance;
import com.onloupe.core.logging.Log;
import com.onloupe.core.logging.LogWriteMode;
import com.onloupe.core.monitor.LocalRepository;
import com.onloupe.core.util.IOUtils;
import com.onloupe.core.util.Multiplexer;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.exception.DirectoryNotFoundException;
import com.onloupe.model.log.LogMessageSeverity;
import com.onloupe.model.session.SessionStatus;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

// TODO: Auto-generated Javadoc
/**
 * The Class FileMessenger.
 */
public class FileMessenger extends MessengerBase {
	
	/** The Constant LOG_CATEGORY. */
	private static final String LOG_CATEGORY = MessengerBase.LOG_CATEGORY + ".File Messenger";

	/** The Constant LOG_EXTENSION. */
	public static final String LOG_EXTENSION = "glf";
	
	/** The Constant PACKAGE_EXTENSION. */
	public static final String PACKAGE_EXTENSION = "glp";
	
	/** The Constant SESSION_LOCK_FOLDER_NAME. */
	public static final String SESSION_LOCK_FOLDER_NAME = "RunningSessions";

	/** The Constant _RandomGenerator. */
	private static final Random _RandomGenerator = new Random(); // static is important so multiple instances created
																	// close together get different values

	/** The closed. */
																	private boolean closed;
	
	/** The repository folder. */
	private String repositoryFolder;
	
	/** The session lock folder. */
	private String sessionLockFolder;
	
	/** The current session file. */
	private int currentSessionFile;
	
	/** The file expiration. */
	private LocalDateTime fileExpiration = LocalDateTime.MIN;

	/** The current file. */
	private RandomAccessFile currentFile;
	
	/** The current serializer. */
	private GLFWriter currentSerializer;
	
	/** The maintainer. */
	private RepositoryMaintenance maintainer;
	
	/** The session file lock. */
	private InterprocessLock sessionFileLock;

	/** The max local disk usage. */
	private int maxLocalDiskUsage;
	
	/** The max local file age. */
	private int maxLocalFileAge;
	
	/** The max file size bytes. */
	private long maxFileSizeBytes;
	
	/** The max log duration seconds. */
	private long maxLogDurationSeconds;
	
	/** The repository maintenance enabled. */
	private boolean repositoryMaintenanceEnabled;
	
	/** The repository maintenance requested. */
	private boolean repositoryMaintenanceRequested;
	
	/** The repository maintenance scheduled date time. */
	private OffsetDateTime repositoryMaintenanceScheduledDateTime; // once maintenance has been requested, when we will
																	// do it.

	/**
																	 * Instantiates a new file messenger.
																	 */
																	public FileMessenger() {
		super("File", true);

	}

	/**
	 * Creates the appropriate start of a session file name for a
	 * product/application.
	 *
	 * @param productName the product name
	 * @param applicationName the application name
	 * @return the string
	 */
	public static String sessionFileNamePrefix(String productName, String applicationName) {
		return FileSystemTools.sanitizeFileName(String.format("%1$s %2$s", productName, applicationName));
	}

	/**
	 * Inheritors should override this method to implement custom Command handling
	 * functionality
	 * 
	 * Code in this method is protected by a Queue Lock. This method is called with
	 * the Message Dispatch thread exclusively. Some commands (Shutdown, Flush) are
	 * handled by MessengerBase and redirected into specific method calls.
	 *
	 * @param command              The MessagingCommand enum value of this command.
	 * @param state the state
	 * @param writeThrough         Whether write-through (synchronous) behavior was
	 *                             requested.
	 * @param maintenanceModeRequested the maintenance mode requested
	 * @return Specifies whether maintenance mode has been requested and the type (source) of that request.
	 */
	@Override
	protected MaintenanceModeRequest onCommand(MessagingCommand command, Object state, boolean writeThrough, MaintenanceModeRequest maintenanceModeRequested) {
		if (command == MessagingCommand.CLOSE_FILE) {
			// This command is for us! It means issue maintenance mode to close and roll
			// over to a new file.
			return MaintenanceModeRequest.EXPLICIT;
		}
		return maintenanceModeRequested;
	}

	/**
	 * Inheritors should override this method to implement custom initialize
	 * functionality.
	 * 
	 * This method will be called exactly once before any call to OnFlush or OnWrite
	 * is made. Code in this method is protected by a Thread Lock. This method is
	 * called with the Message Dispatch thread exclusively.
	 *
	 * @param configuration the configuration
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	protected void onInitialize(IMessengerConfiguration configuration) throws IOException {
		// do our first time initialization
		setCaption("Standard File Messenger");
		setDescription(
				"Messenger implementation that writes messages to files through a buffer.  Supports synchronous and asynchronous messaging.");

		// try to up cast the configuration to our specific configuration type
		SessionFileConfiguration fileConfiguration = (SessionFileConfiguration) configuration;

		// If the max file size is unbounded (zero or less) then we want 1GB.
		this.maxFileSizeBytes = fileConfiguration.getMaxFileSize() < 1 ? 1024 : fileConfiguration.getMaxFileSize();
		this.maxFileSizeBytes = this.maxFileSizeBytes * 1048576; // the configured value is in MB, we use bytes for
																	// faster
		// comparisons

		this.maxLogDurationSeconds = fileConfiguration.getMaxFileDuration() * 60; // the configured value is in
																					// minutes, we
																					// use
																					// seconds for consistency

		this.repositoryMaintenanceEnabled = fileConfiguration.getEnableFilePruning();
		this.maxLocalDiskUsage = fileConfiguration.getMaxLocalDiskUsage();
		this.maxLocalFileAge = fileConfiguration.getMaxLocalFileAge();

		// what are the very best folders for us to use?
		this.repositoryFolder = LocalRepository.calculateRepositoryPath(
				getPublisher().getSessionSummary().getProduct(), fileConfiguration.getFolder());
		this.sessionLockFolder = Paths.get(this.repositoryFolder).resolve(SESSION_LOCK_FOLDER_NAME).toString();

		// we also have to be sure the path exists now.
		FileSystemTools.ensurePathExists(this.repositoryFolder);
		FileSystemTools.ensurePathExists(this.sessionLockFolder);

		// Since we update the index during a flush, and the index update is about as
		// bad as a flush we look at both together.
		setAutoFlush(true);
		setAutoFlushInterval(
				Math.min(fileConfiguration.getAutoFlushInterval(), fileConfiguration.getIndexUpdateInterval()));

		// If we aren't able to initialize our log folder, throw an exception
		if (TypeUtils.isBlank(this.repositoryFolder)) {
			throw new DirectoryNotFoundException("No log folder could be determined, so the file messenger can't log.");
		}

		scheduleRepositoryMaintenance(0, 0);

		getSessionFileLock();
	}

	/**
	 * Inheritors should override this method to implement custom Exit functionality
	 * 
	 * Code in this method is protected by a Queue Lock. This method is called with
	 * the Message Dispatch thread exclusively.
	 */
	@Override
	protected void onExit() {
		// we want to switch into the appropriate exit mode; we don't want to leave it
		// running now
		// even if we close abruptly.
		if (this.currentSerializer != null) {
			this.currentSerializer.getSessionHeader()
					.setStatusName(this.currentSerializer.getSessionHeader().toString());
		}
	}

	/**
	 * Inheritors should override this method to implement custom flush
	 * functionality.
	 * 
	 * Code in this method is protected by a Queue Lock. This method is called with
	 * the Message Dispatch thread exclusively.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	protected void onFlush() throws IOException {
		// push the serializer to flush to disk
		if (this.currentSerializer != null) {
			// The order of these two operations is related in a non-obvious way: flushing
			// the current
			// serializer updates the session header we write to the index, so it must be
			// done first.
			this.currentSerializer.flush();
		}

		// and do repository maintenance if it was requested. It won't be requested if
		// maintenance is disabled.
		// This is over here to ensure we DO it eventually but we can do it on a lazy
		// schedule,
		// and don't bother if we're Exiting (includes closing) or it's dangerous (we're
		// in a debugger)
		if ((this.repositoryMaintenanceRequested && !getExiting())
				&& (OffsetDateTime.now().isAfter(this.repositoryMaintenanceScheduledDateTime))) {
			this.repositoryMaintenanceRequested = false;

			// do we actually have a maintainer? If not create it now.
			if (this.maintainer == null) {
				// initialize the repository maintenance object with our configuration.
				try {
					this.maintainer = new RepositoryMaintenance(this.repositoryFolder,
							getPublisher().getSessionSummary().getProduct(),
							getPublisher().getSessionSummary().getApplication(), this.maxLocalFileAge,
							this.maxLocalDiskUsage, !Log.getSilentMode());
				} catch (RuntimeException ex) {

					if (!Log.getSilentMode()) {
						Log.write(LogMessageSeverity.ERROR, LogWriteMode.QUEUED, ex, LOG_CATEGORY,
								"Unable to initialize repository maintenance",
								"While attempting to initialize the repository maintenance class in the file messenger an exception was thrown:\r\n%s",
								ex.getMessage());
					}
				}
			}

			// and only continue if we did create a good maintainer.
			if (this.maintainer != null) {
				Multiplexer.run(() -> FileMessenger.this.maintainer.performMaintenance());
			}
		}
	}

	/**
	 * Inheritors should override this to implement a periodic maintenance
	 * capability
	 * 
	 * Maintenance is invoked by a return value from the OnWrite method. When
	 * invoked, this method is called and all log messages are buffered for the
	 * duration of the maintenance period. Once this method completes, normal log
	 * writing will resume. During maintenance, any queue size limit is ignored.
	 * This method is not called with any active locks to allow messages to continue
	 * to queue during maintenance. This method is called with the Message Dispatch
	 * thread exclusively.
	 *
	 * @throws Exception the exception
	 * @throws SecurityException the security exception
	 */
	@Override
	protected void onMaintenance() throws Exception {
		// close the existing file and open a new one. We rely on OpenFile doing both.
		openFile();

		// and if repository maintenance is enabled, kick that off as well.
		scheduleRepositoryMaintenance(0, 0);
	}

	/**
	 * Inheritors should override this method to implement custom Close
	 * functionality
	 * 
	 * Code in this method is protected by a Queue Lock. This method is called with
	 * the Message Dispatch thread exclusively.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	protected void onClose() throws IOException {
		if (!closed) {
			closeFile(true); // closes the file and serializer safely
			closed = true;
		}
	}

	/**
	 * Get the unique lock for the active session, to be held until the session
	 * exits.
	 *
	 * @return the session file lock
	 */
	private void getSessionFileLock() {
		if (this.sessionFileLock != null) {
			return;
		}

		try {
			this.sessionFileLock = InterprocessLockManager.getInstance().lock(this, this.sessionLockFolder,
					Log.getSessionSummary().getId().toString(), 0, true);

			if (this.sessionFileLock == null) {
				Log.write(LogMessageSeverity.INFORMATION, LOG_CATEGORY,
						"Loupe Agent unable to get the unique lock for a new session",
						"The Loupe Agent's FileMessenger was not able to lock this active session as Running.  "
								+ "This could interfere with efficiently distinguishing whether this session has crashed or is still running.");
			}
		} catch (Exception ex) // we don't want failure to get the session file lock to be fatal...
		{
			if (!Log.getSilentMode()) {
				Log.write(LogMessageSeverity.INFORMATION, LogWriteMode.QUEUED, ex, true, LOG_CATEGORY,
						"Loupe Agent unable to get the unique lock for a new session due to "
								+ TypeUtils.getRootCauseMessage(ex.getCause()),
						"The Loupe Agent's FileMessenger was not able to lock this active session as Running.  "
								+ "This could interfere with efficiently distinguishing whether this session has crashed or is still running.");
			}
		}
	}

	/**
	 * Release the unique lock for the active session, to be called when the
	 * FileMessenger gets disposed.
	 * 
	 */
	private void releaseSessionFileLock() {
		if (this.sessionFileLock == null) {
			return;
		}

		try {
			this.sessionFileLock.close();
			this.sessionFileLock = null;
		} catch (Exception ex) {
			Log.write(LogMessageSeverity.INFORMATION, LogWriteMode.QUEUED, ex, LOG_CATEGORY,
					"Loupe Agent got an error while releasing the unique lock for this session",
					"The Loupe Agent's FileMessenger was not able to properly release the lock as this session exits.  "
							+ "This will likely take care of itself as the process exits, but an exception here is unexpected and unusual.");

			if (this.sessionFileLock != null && this.sessionFileLock.isDisposed()) {
				this.sessionFileLock = null;
			}
		}
	}

	/**
	 * Schedule repository maintenance to happen at the next opportunity if
	 * maintenance is enabled.
	 *
	 * @param minDelaySec the min delay sec
	 * @param maxDelaySec the max delay sec
	 */
	private void scheduleRepositoryMaintenance(int minDelaySec, int maxDelaySec) {
		if (!this.repositoryMaintenanceEnabled || getExiting()) {
			return; // nothing to do
		}

		int repositoryMaintenanceDelay = (maxDelaySec == 0) ? 0
				: _RandomGenerator.nextInt((maxDelaySec - minDelaySec) + 1);

		// now we have to make sure we don't move out the current maintenance time if
		// it's already set.
		OffsetDateTime proposedMaintenanceTime = OffsetDateTime.now().plusSeconds(repositoryMaintenanceDelay);
		if (this.repositoryMaintenanceRequested) {
			this.repositoryMaintenanceScheduledDateTime = (proposedMaintenanceTime
					.isBefore(this.repositoryMaintenanceScheduledDateTime)) ? proposedMaintenanceTime
							: this.repositoryMaintenanceScheduledDateTime;
		} else {
			this.repositoryMaintenanceScheduledDateTime = proposedMaintenanceTime;
			this.repositoryMaintenanceRequested = true;
		}
	}

	/**
	 * Close file.
	 *
	 * @param isLastFile the is last file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void closeFile(boolean isLastFile) throws IOException {
		// close any existing serializer
		if (this.currentSerializer != null) {
			try {
				// The order of these two operations is related in a non-obvious way: closing
				// the current
				// serializer updates the session header we write to the index, so it must be
				// done first.
				this.currentSerializer.close(isLastFile);

				// Now update our index information with the final session header info.
				if (!isLastFile) {
					// we need to keep our state as being running, Close just changed it.
					this.currentSerializer.getSessionHeader().setStatusName(SessionStatus.RUNNING.toString());
				}
			} finally {
				this.currentSerializer = null;
			}
		}

		// close any existing file stream
		if (this.currentFile != null) {
			IOUtils.closeQuietly(this.currentFile);
		}

		// And if it's the last file, release our unique lock for this session.
		if (isLastFile) {
			releaseSessionFileLock();
		}
	}

	/**
	 * Open a new output file.
	 * 
	 * Any existing file will be closed.
	 *
	 * @throws Exception the exception
	 * @throws SecurityException the security exception
	 */
	private void openFile() throws Exception {
		// clear the existing file pointer to make sure if we fail, it's gone.
		// we also rely on this to distinguish adding a new file to an existing stream.
		closeFile(false);

		// increment our session file counter since we're going to open a new file
		this.currentSessionFile++;

		// Calculate our candidate file name (with path) based on what we know.
		Path folder = Paths.get(this.repositoryFolder);
		String fileNamePath = folder.resolve(makeFileName()).toString();

		// now double check that the candidate path is unique
		fileNamePath = FileSystemTools.makeFileNamePathUnique(fileNamePath);

		// we now have a unique file name, create the file.
		FileSystemTools.ensurePathExists(folder.toString());
		this.currentFile = FileSystemTools.createRandomAccessFile(fileNamePath, "rwd");

		// and open a serializer on it
		this.currentSerializer = new GLFWriter(this.currentFile, getPublisher().getSessionSummary(),
				this.currentSessionFile, OffsetDateTime.now());

		// write out every header packet to the stream
		ICachedMessengerPacket[] headerPackets = getPublisher().getHeaderPackets();
		if (headerPackets != null) {
			for (ICachedMessengerPacket packet : headerPackets) {
				this.currentSerializer.write(packet);
			}
		}

		// and set a time for us to do our next index update.
		this.fileExpiration = LocalDateTime.now().plusSeconds(this.maxLogDurationSeconds);
	}

	/**
	 * Make file name.
	 *
	 * @return the string
	 */
	private String makeFileName() {
		String fileName = String.format("%s_%s-%s.%s",
				sessionFileNamePrefix(getPublisher().getSessionSummary().getProduct(),
						getPublisher().getSessionSummary().getApplication()),
				getPublisher().getSessionSummary().getStartDateTime().format(
						DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")),
				this.currentSessionFile, LOG_EXTENSION);

		return FileSystemTools.sanitizeFileName(fileName);
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.messaging.MessengerBase#onWrite(com.onloupe.core.messaging.IMessengerPacket, boolean, com.onloupe.core.messaging.MessengerBase.MaintenanceModeRequest)
	 */
	@Override
	protected MaintenanceModeRequest onWrite(IMessengerPacket packet, boolean writeThrough, MaintenanceModeRequest maintenanceModeRequested) throws Exception {
		// Do we have a serializer opened?
		if (this.currentSerializer == null) {
			// we do not. we need to open a file.
			openFile();
		}

		// now write to the file
		this.currentSerializer.write(packet);

		if (writeThrough) {
			onFlush();
		}

		// and do we need to request maintenance?
		if ((this.currentFile.length() > this.maxFileSizeBytes)
				|| LocalDateTime.now().isAfter(this.fileExpiration)) {
			return MaintenanceModeRequest.REGULAR;
		}

		return maintenanceModeRequested;
	}
}