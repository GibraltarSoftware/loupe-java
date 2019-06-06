package com.onloupe.agent;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import com.onloupe.configuration.ServerConfiguration;
import com.onloupe.core.data.FileTransportPackage;
import com.onloupe.core.data.RepositoryPublishClient;
import com.onloupe.core.data.SessionCriteriaPredicate;
import com.onloupe.core.data.SimplePackage;
import com.onloupe.core.logging.Log;
import com.onloupe.core.logging.LogWriteMode;
import com.onloupe.core.monitor.LocalRepository;
import com.onloupe.core.server.GibraltarNetworkException;
import com.onloupe.core.server.HubConnection;
import com.onloupe.core.server.HubConnectionStatus;
import com.onloupe.core.util.FileUtils;
import com.onloupe.core.util.IOUtils;
import com.onloupe.core.util.PackageStats;
import com.onloupe.core.util.TimeConversion;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.data.ISessionSummaryCollection;
import com.onloupe.model.exception.GibraltarException;
import com.onloupe.model.log.LogMessageSeverity;
import com.onloupe.model.session.ISessionSummary;


/**
 * Packages up sessions collected on the local computer and sends them via email
 * or file transport.
 */
public class Packager {
	
	/** The log category for the packager. */
	public static final String LOG_CATEGORY = "Loupe.Packager";

	/** The repository. */
	private LocalRepository repository;
	
	/** The Constant userListFormat. */
	private static final String[] userListFormat = new String[] { "Anonymous", "%s", "%s and %s", "%s, %s, et. al." };;
	
	/** The Constant userListMaxCount. */
	private static final int userListMaxCount = userListFormat.length - 1;;

	/**
	 * Create a new packager for the current process.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Packager() throws IOException {
		this(Log.getSessionSummary().getProduct(), Log.getSessionSummary().getApplication());
	}

	/**
	 * Create a new packager for the current process.
	 *
	 * @param productName the product name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Packager(String productName) throws IOException {
		this(productName, null);
	}

	/**
	 * Create a new packager for the current process.
	 *
	 * @param productName the product name
	 * @param applicationName the application name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Packager(String productName, String applicationName) throws IOException {
		this(productName, applicationName, Log.getConfiguration().getSessionFile().getFolder());
	}

	/**
	 * Create a new packager for the current process.
	 *
	 * @param productName the product name
	 * @param applicationName the application name
	 * @param repositoryFolder the repository folder
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Packager(String productName, String applicationName, String repositoryFolder) throws IOException {
		if (TypeUtils.isBlank(productName)) {
			throw new NullPointerException("productName");
		}

		setProductName(productName);
		setApplicationName(applicationName);
		setCaption(String.format("%s Logs", getProductName()));

		// now connect to the right local repository to package from.
		this.repository = new LocalRepository(productName, repositoryFolder);
	}

	/**
	 * The product name of the current running application this packager was
	 * initialized with.
	 */
	private String productName;

	/**
	 * Gets the product name.
	 *
	 * @return the product name
	 */
	public final String getProductName() {
		return this.productName;
	}

	/**
	 * Sets the product name.
	 *
	 * @param value the new product name
	 */
	private void setProductName(String value) {
		this.productName = value;
	}

	/**
	 * The name of the current running application this packager was initialized
	 * with.
	 */
	private String applicationName;

	/**
	 * Gets the application name.
	 *
	 * @return the application name
	 */
	public final String getApplicationName() {
		return this.applicationName;
	}

	/**
	 * Sets the application name.
	 *
	 * @param value the new application name
	 */
	private void setApplicationName(String value) {
		this.applicationName = value;
	}

	/** A caption for the resulting package. */
	private String caption;

	/**
	 * Gets the caption.
	 *
	 * @return the caption
	 */
	public final String getCaption() {
		return this.caption;
	}

	/**
	 * Sets the caption.
	 *
	 * @param value the new caption
	 */
	public final void setCaption(String value) {
		this.caption = value;
	}

	/**
	 * A description for the resulting package.
	 */
	private String description;

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public final String getDescription() {
		return this.description;
	}

	/**
	 * Sets the description.
	 *
	 * @param value the new description
	 */
	public final void setDescription(String value) {
		this.description = value;
	}

	/**
	 * Send to file.
	 *
	 * @param sessions the sessions
	 * @param markAsRead the mark as read
	 * @param fullFileNamePath the full file name path
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	//wrapper everything to this.
	public final void sendToFile(EnumSet<SessionCriteria> sessions, boolean markAsRead, String fullFileNamePath) throws IOException {
		// check for invalid arguments.
		if (TypeUtils.isBlank(fullFileNamePath)) {
			throw new NullPointerException("fullFileNamePath");
		}
		if (Paths.get(fullFileNamePath).getParent() == null) {
			throw new IndexOutOfBoundsException("The provided fullFileNamePath is not fully qualified");
		}

		actionSendToFile(sessions, markAsRead, fullFileNamePath);
	}

	/**
	 * Write the completed package to the provided full file name and path without
	 * extension.
	 *
	 * @param sessions         The set of match rules to apply to sessions to
	 *                         determine what to send.
	 * @param markAsRead       True to have every included session marked as read
	 *                         upon successful completion.
	 * @param fullFileNamePath The file name and path to write the final package to
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final void sendToFile(SessionCriteria sessions, boolean markAsRead, String fullFileNamePath)
			throws IOException {
		sendToFile(EnumSet.of(sessions), markAsRead, fullFileNamePath);
	}

	/**
	 * Write the completed package to the provided full file name and path without
	 * extension.
	 *
	 * @param sessionMatchPredicate A delegate to evaluate sessions and determine
	 *                              which ones to send.
	 * @param markAsRead            True to have every included session marked as
	 *                              read upon successful completion.
	 * @param fullFileNamePath      The file name and path to write the final
	 *                              package to
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final void sendToFile(java.util.function.Predicate<ISessionSummary> sessionMatchPredicate,
			boolean markAsRead, String fullFileNamePath) throws IOException {
		if (sessionMatchPredicate == null) {
			throw new NullPointerException("sessionMatchPredicate");
		}

		// check for invalid arguments.
		if (TypeUtils.isBlank(fullFileNamePath)) {
			throw new NullPointerException("fullFileNamePath");
		}

		if (Paths.get(fullFileNamePath).getParent() == null) {
			throw new IndexOutOfBoundsException("The provided fullFileNamePath is not fully qualified");
		}

		actionSendToFile(sessionMatchPredicate, markAsRead, fullFileNamePath);
	}

	/**
	 * Send the specified packages to our session data server as configured.
	 *
	 * @param sessionMatchPredicate    A delegate to evaluate sessions and determine
	 *                                 which ones to send.
	 * @param markAsRead               True to have every included session marked as
	 *                                 read upon successful completion.
	 * @param purgeSentSessions        True to have every included session removed
	 *                                 from the local repository upon successful
	 *                                 completion.
	 * @param overrideConfiguration    Indicates if any of the configuration
	 *                                 information provided on this call should be
	 *                                 used.
	 * @param useGibraltarService      Indicates if the Gibraltar Loupe Service
	 *                                 should be used instead of a private server
	 * @param customerName             The unique customer name when using the
	 *                                 Gibraltar Loupe Service
	 * @param server                   The full DNS name of the server where the
	 *                                 service is located. Only applies to a private
	 *                                 server.
	 * @param port                     An optional port number override for the
	 *                                 server. Only applies to a private server.
	 * @param useSsl                   Indicates if the connection should be
	 *                                 encrypted with SSL. Only applies to a private
	 *                                 server.
	 * @param applicationBaseDirectory The virtual directory on the host for the
	 *                                 private service. Only applies to a private
	 *                                 server.
	 * @param repository               The specific repository on the server for a
	 *                                 private server. Only applies to a private
	 *                                 server.
	 * @throws Exception the exception
	 * @exception GibraltarException The server couldn't be contacted or there was a
	 *                               communication error.
	 */
	public final void sendToServer(java.util.function.Predicate<ISessionSummary> sessionMatchPredicate,
			boolean markAsRead, boolean purgeSentSessions, boolean overrideConfiguration, boolean useGibraltarService,
			String customerName, String server, int port, boolean useSsl, String applicationBaseDirectory,
			String repository) throws Exception {
		if (sessionMatchPredicate == null) {
			throw new NullPointerException("sessionMatchPredicate");
		}

		ServerConfiguration connectionOptions = new ServerConfiguration();
		connectionOptions.setUseGibraltarService(useGibraltarService);
		connectionOptions.setCustomerName(customerName);
		connectionOptions.setServer(server);
		connectionOptions.setPort(port);
		connectionOptions.setUseSsl(useSsl);
		connectionOptions.setApplicationBaseDirectory(applicationBaseDirectory);
		connectionOptions.setRepository(repository);

		actionSendToServer(sessionMatchPredicate, markAsRead, purgeSentSessions, overrideConfiguration,
				connectionOptions);

	}

	/**
	 * Send the specified packages to our session data server as configured.
	 *
	 * @param sessions                 The set of match rules to apply to sessions
	 *                                 to determine what to send.
	 * @param markAsRead               True to have every included session marked as
	 *                                 read upon successful completion.
	 * @param purgeSentSessions        True to have every included session removed
	 *                                 from the local repository upon successful
	 *                                 completion.
	 * @param overrideConfiguration    Indicates if any of the configuration
	 *                                 information provided on this call should be
	 *                                 used.
	 * @param useGibraltarService      Indicates if the Gibraltar Loupe Service
	 *                                 should be used instead of a private server
	 * @param customerName             The unique customer name when using the
	 *                                 Gibraltar Loupe Service
	 * @param server                   The full DNS name of the server where the
	 *                                 service is located. Only applies to a private
	 *                                 server.
	 * @param port                     An optional port number override for the
	 *                                 server. Only applies to a private server.
	 * @param useSsl                   Indicates if the connection should be
	 *                                 encrypted with SSL. Only applies to a private
	 *                                 server.
	 * @param applicationBaseDirectory The virtual directory on the host for the
	 *                                 private service. Only applies to a private
	 *                                 server.
	 * @param repository               The specific repository on the server for a
	 *                                 private server. Only applies to a private
	 *                                 server.
	 * @throws Exception the exception
	 * @exception GibraltarException The server couldn't be contacted or there was a
	 *                               communication error
	 */
	public final void sendToServer(SessionCriteria sessions, boolean markAsRead, boolean purgeSentSessions,
			boolean overrideConfiguration, boolean useGibraltarService, String customerName, String server, int port,
			boolean useSsl, String applicationBaseDirectory, String repository) throws Exception {
		if (sessions == SessionCriteria.NONE) {
			return;
		}

		ServerConfiguration connectionOptions = new ServerConfiguration();
		connectionOptions.setUseGibraltarService(useGibraltarService);
		connectionOptions.setCustomerName(customerName);
		connectionOptions.setServer(server);
		connectionOptions.setPort(port);
		connectionOptions.setUseSsl(useSsl);
		connectionOptions.setApplicationBaseDirectory(applicationBaseDirectory);
		connectionOptions.setRepository(repository);

		actionSendToServer(sessions, markAsRead, purgeSentSessions, overrideConfiguration, connectionOptions);
	}
	
    /**
     * Send to server.
     *
     * @param sessions the sessions
     * @param markAsRead the mark as read
     * @throws Exception the exception
     */
    public void sendToServer(SessionCriteria sessions, boolean markAsRead) throws Exception
    {
        sendToServer(sessions, markAsRead, false, false, false, null, null, 0, false, null, null);
    }
    
    /**
     * Send to server.
     *
     * @param sessions the sessions
     * @param markAsRead the mark as read
     * @param customerName the customer name
     * @throws Exception the exception
     */
    public void sendToServer(SessionCriteria sessions, boolean markAsRead, String customerName) throws Exception
    {
        sendToServer(sessions, markAsRead, false, true, true, customerName, null, 0, false, null, null);
    }
    
    /**
     * Send to server.
     *
     * @param sessions the sessions
     * @param markAsRead the mark as read
     * @param server the server
     * @param port the port
     * @param useSsl the use ssl
     * @param applicationBaseDirectory the application base directory
     * @param repository the repository
     * @throws Exception the exception
     */
    public void sendToServer(SessionCriteria sessions, boolean markAsRead, String server, int port, boolean useSsl, String applicationBaseDirectory, String repository) throws Exception
    {
        sendToServer(sessions, markAsRead, false, true, false, null, server, port, useSsl, applicationBaseDirectory, repository);
    }

	/**
	 * Determines if it can correctly connect to the server and send data.
	 *
	 * @param overrideConfiguration Indicates if any of the configuration
	 *                              information provided on this call should be used
	 * @param serverConfiguration   The connection configuration to use if
	 *                              overriding the server configuration
	 * @return The hub status information for the specified configuration
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	public static HubConnectionStatus canSendToServer(boolean overrideConfiguration,
			ServerConfiguration serverConfiguration) throws IOException, Exception {
		HubConnectionStatus status;
		if (overrideConfiguration) {
			status = HubConnection.canConnect(serverConfiguration);
		} else {
			status = HubConnection.canConnect(Log.getConfiguration().getServer());
		}

		return status;
	}

	/**
	 * Determines if it can correctly connect to the server and send data.
	 *
	 * @return The hub status information for the specified configuration
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	public static HubConnectionStatus canSendToServer() throws IOException, Exception {
		return canSendToServer(false, null);
	}

	/**
	 * Get a dataset of all of the sessions that should be included in our package.
	 *
	 * @param sessionCriteria the session criteria
	 * @param hasProblemSessions the has problem sessions
	 * @return the i session summary collection
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected final ISessionSummaryCollection findPackageSessions(SessionCriteria sessionCriteria,
			Boolean hasProblemSessions) throws IOException {
		if (sessionCriteria == SessionCriteria.NONE) {
			// special case: All they asked for is none, which means, well... none.
			hasProblemSessions = false;
			return null;
		}

		hasProblemSessions = false;
		ISessionSummaryCollection packageSessions;

		// special case: if session criteria includes active session then we have to
		// split the file.
		// Go ahead and end the current file. We need to be sure that there is an up to
		// date file when the copy runs.
		if ((SessionCriteria.ACTIVE.getValue() & sessionCriteria.getValue()) == SessionCriteria.ACTIVE.getValue()) {
			Log.endFile("Creating Package including active session");
		}

		// run the maintenance merge to make sure we have the latest sessions.
		this.repository.refresh(false, true, EnumSet.of(sessionCriteria));

		// find all of the sessions so we can subset it downstream.
		packageSessions = this.repository
				.find((new SessionCriteriaPredicate(getProductName(), getApplicationName(), EnumSet.of(sessionCriteria))));
		if (!Log.getSilentMode()) {
			Log.write(LogMessageSeverity.VERBOSE, LOG_CATEGORY, "Sessions list loaded",
					"There are %d matching sessions in the collection repository.", packageSessions.size());
		}

		return packageSessions;
	}

	/**
	 * Get a dataset of all of the sessions that should be included in our package.
	 *
	 * @param sessionPredicate the session predicate
	 * @param hasProblemSessions the has problem sessions
	 * @return the i session summary collection
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected final ISessionSummaryCollection findPackageSessions(
			java.util.function.Predicate<ISessionSummary> sessionPredicate, Boolean hasProblemSessions)
			throws IOException {
		if (sessionPredicate == null) {
			// special case: All they asked for is none, which means, well... none.
			hasProblemSessions = false;
			return null;
		}

		hasProblemSessions = false;
		ISessionSummaryCollection packageSessions;

		// Go ahead and end the current file - we will assume the caller may want it. We
		// need to be sure that there is an up to date file when the copy runs.
		Log.endFile("Creating Package including active session");

		// run the maintenance merge to make sure we have the latest sessions.
		this.repository.refresh(false, true);

		// find all of the sessions so we can subset it downstream.
		packageSessions = this.repository.find(sessionPredicate);
		if (!Log.getSilentMode()) {
			Log.write(LogMessageSeverity.VERBOSE, LOG_CATEGORY, "Sessions list loaded",
					"There are %d matching sessions in the collection repository.", packageSessions.size());
		}

		return packageSessions;
	}

	/**
	 * Creates a transportable package of the selected sessions in the local
	 * collection repository.
	 * 
	 * Multi-thread safe.
	 *
	 * @param selectedSessions the selected sessions
	 * @param maxPackageSizeBytes the max package size bytes
	 * @param hasProblemSessions the has problem sessions
	 * @param packagingState the packaging state
	 * @param destinationFileNamePath the destination file name path
	 * @return the simple package
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private SimplePackage createTransportablePackage(ISessionSummaryCollection selectedSessions,
			int maxPackageSizeBytes, Boolean hasProblemSessions, PackagingState packagingState,
			String destinationFileNamePath) throws IOException {
		packagingState.setIsComplete(true); // when we detect that we must break into two packages we set this false.
		hasProblemSessions = false;

		File temporaryFile;
		if (TypeUtils.isNotBlank(destinationFileNamePath)) {
			temporaryFile = new File(destinationFileNamePath);
		} else {
			// calculate a temporary file to put the package in and make our package.
			temporaryFile = Files.createTempFile("package-", ".zip").toFile();
		}

		// it just made a file in that location, best to blow it away before we go
		// further (get temp file name creates a 0 byte file)
		FileUtils.safeDeleteFile(temporaryFile);

		SimplePackage newPackage = new SimplePackage(temporaryFile);

		// we merge sessions one by one because we have to check the size of the package
		// after each.
		int sessionsMergedInPackage = 0;
		long sessionsSizeBytes = 0;
		boolean foundStartSession = (packagingState.getLastSessionId() == null);
		boolean foundStartFile = (packagingState.getLastFileId() == null);
		List<String> userNameList = new ArrayList<String>(userListMaxCount); // List of the first 0-3 user names found.

		for (ISessionSummary session : selectedSessions) {
			// wait, have we hit our start session yet?
			if (!foundStartSession) {
				// if this is our matching start session id then this is the last one we sent,
				// so we'll start on the next one.
				if (session.getId().equals(packagingState.getLastSessionId())) {
					foundStartSession = true;
				}
			} else {
				List<UUID> fragments = this.repository.getSessionFileIds(session.getId());

				for (UUID fileId : fragments) {
					if (!foundStartFile) {
						// if this fragment is the last file we sent then note we found it
						// so we'll start on the next one.
						if (fileId.equals(packagingState.getLastFileId())) {
							foundStartFile = true;
						}
					} else {
						// we should include this session, and then see how large we are.
						
						RandomAccessFile sessionFile = null;
						try {
							// get the session as a stream so we can check its size. We may have a cached
							// stream from the last iteration.
							if ((packagingState.getNextSessionId() != null)
									&& (packagingState.getNextSessionId().equals(session.getId()))
									&& (packagingState.getNextSessionFile() != null)) {
								sessionFile = packagingState.getNextSessionFile();
								packagingState.setNextSessionId(null);
								packagingState.setNextSessionStream(null);
							} else {
								if (packagingState.getNextSessionFile() != null) {
									// huh. a mismatched stream - that means we have an algorithm error.
									if (!Log.getSilentMode()) {
										Log.write(LogMessageSeverity.WARNING, LOG_CATEGORY,
												"Packaging Session State is out of Sync",
												"We carried forward a package session state but the NextSessionId didn't match the next session, so we'll clean up the unused stream");
									}
									packagingState.setNextSessionStream(null);
								}

								try {
									sessionFile = this.repository.loadSessionFile(session.getId(), fileId);
								} catch (RuntimeException ex) {
									if (!Log.getSilentMode()) {
										Log.write(LogMessageSeverity.VERBOSE, LogWriteMode.QUEUED, ex, LOG_CATEGORY,
												"Session to be packaged is not in the repository.", ex.getMessage());
									}
								}
							}

							// there are cases where we won't get a stream back - typically because there's
							// a problem with it or it's still in use
							if (sessionFile == null) {
								if (!Log.getSilentMode()) {
									Log.write(LogMessageSeverity.VERBOSE, LOG_CATEGORY,
											"Session was unexpectedly not available to be included in the package",
											"The repository wasn't able to load the session for inclusion into the package even though it should be available.  Session Id: %s",
											session.getId().toString());
								}
							} else {
								String sessionUserName = null;
								long sessionLength = sessionFile.length();

								// Check if we're going to exceed our maximum length by adding this session...
								if ((sessionLength + sessionsSizeBytes) > maxPackageSizeBytes) {
									// two ways we can handle this: If this is the FIRST session in the package then
									// we have a special case: The session can not fit in a package.
									// Otherwise, we'll end this package and start another.

									if (sessionsMergedInPackage == 0) {
										// we have nothing to lose really: Try to merge the session in case it will
										// compress down to fit.
										newPackage.addSession(sessionFile);
										packagingState.setLastSessionId(session.getId()); // and if we stop on the next
																							// loop then this is the
																							// session we ended on.
										packagingState.setLastFileId(fileId); // and this is the fragment that we ended
																				// on.
										sessionsMergedInPackage++;

										// we're going to exceed our length.. or ARE we?
										sessionsSizeBytes = newPackage.length();

										// if we still are too big then it's curtains for this fragment, it's just not
										// safe to send it. Reset the package and keep going on to the next session
										if (sessionsSizeBytes > maxPackageSizeBytes) {
											if (!Log.getSilentMode()) {
												Log.write(LogMessageSeverity.WARNING, LOG_CATEGORY,
														"Session is too large to fit in a package",
														"The current session has %d bytes of session data, when packaged is %d bytes which would exceed the max of %d bytes.  This session will be skipped.",
														sessionLength, sessionsSizeBytes, maxPackageSizeBytes);
											}

											// dump this package and reset so we're ready for a new one.
											newPackage.close();
											FileUtils.safeDeleteFile(temporaryFile);
											
											newPackage = new SimplePackage(temporaryFile);
											sessionsMergedInPackage = 0;
											sessionsSizeBytes = 0;

											// and super special case: We need to dump the stream too because nothing is
											// going to do that later.
											// sessionStream.Dispose(); // It's already disposed from when we added it
											// to the package.
										} else {
											// We're actually keeping it, so check it's user name.
											sessionUserName = session.getUserName();
										}

									} else {
										if (!Log.getSilentMode()) {
											Log.write(LogMessageSeverity.VERBOSE, LOG_CATEGORY,
													"Package is projected to exceed max allowed size, no more sessions will be added.",
													"The current package size is %d bytes of session data, this new package is %d bytes which would exceed the max of %d bytes.",
													sessionsSizeBytes, sessionLength, maxPackageSizeBytes);
										}
										packagingState.setIsComplete(false); // we defaulted this to true at the top.

										// since we're going to have to stop for this package, but we don't want to lose
										// the work that went into getting the stream we need to store it all off.
										packagingState.setNextSessionId(session.getId());
										packagingState.setNextSessionStream(sessionFile); // Save it to try in the next
										// package.
										break;
									}
								} else {
									// go ahead and merge this session.
									newPackage.addSession(sessionFile); // This will copy and dispose the sessionStream
																		// before
									// returning.
									packagingState.setLastSessionId(session.getId()); // and if we stop on the next loop
																						// then this is the session we
																						// ended on.
									packagingState.setLastFileId(fileId); // and this is the fragment that we ended on.
									sessionsSizeBytes = newPackage.length();
									sessionsMergedInPackage++;
									sessionUserName = session.getUserName();
								}

								if (userNameList.size() < userListMaxCount
										&& TypeUtils.isNotBlank(sessionUserName)) {
									if (!userNameList.contains(sessionUserName)) {
										userNameList.add(sessionUserName);
									}
								}
							}
						} catch (Exception ex) {
							packagingState.setLastSessionId(session.getId()); // in case we have an error below we want
																				// to know we tried this one.
							packagingState.setLastFileId(fileId); // and this specific fragment.
							if (!Log.getSilentMode()) {
								Log.write(LogMessageSeverity.ERROR, LogWriteMode.QUEUED, ex, LOG_CATEGORY,
										"Unable to add selected session to transport package",
										"While attempting to add a session that was selected to the transport package an exception was thrown.  The session will be skipped.  Exception:\r\n%s",
										ex.getMessage());
							}
						} finally {
							IOUtils.closeQuietly(sessionFile);
						}
					}
				}
			}
		}

		// make sure the package actually HAS any sessions in it - it's possible that
		// each session we wanted to put in errored out
		// or was oversize or something.
		if (sessionsMergedInPackage == 0) {
			IOUtils.closeQuietly(newPackage);
			newPackage = null;

			if (!Log.getSilentMode()) {
				Log.write(LogMessageSeverity.VERBOSE, LOG_CATEGORY, "Suppressing empty working package",
						"No exportable sessions were identified in the last set of sessions to write out, so the package would have been empty.");
			}
		}

		if (newPackage != null) {
			// Update package metadata to reflect the best we know.

			// calculate a good caption & description
			String effectiveAppName = TypeUtils.isBlank(getApplicationName()) ? getProductName()
					: String.format("%s %s", getProductName(), getApplicationName());
			int userNameCount = Math.min(userNameList.size(), userListMaxCount);
			String effectiveUserName = String.format(userListFormat[userNameCount],
					userNameList.toArray(new Object[0]));

			PackageStats stats = newPackage.getStats();
			int sessions = stats.getSessions();
			int problemSessions = stats.getProblemSessions();

			// now we can figure out the right caption & description
			String sessionPlural = "s"; // Assume usually plural...
			String problemLabel = "a"; // Assume usually singular...
			if (problemSessions > 0) {
				if (problemSessions == 1) // We're only putting plural on problem session(s); session just says "Total".
				{
					sessionPlural = ""; // So we can borrow this for problem session(s).
				} else {
					problemLabel = "Multiple";
				}

				hasProblemSessions = true;
				newPackage.setCaption(String.format("%s Sessions from %s (%d Problem Session%s of %d Total)",
						effectiveAppName, effectiveUserName, problemSessions, sessionPlural, sessions));
				newPackage.setDescription(String.format(
						"!!!This Package Contains %s Problem Session%s!!!\r\n\r\nProduct: %s\r\nComputer: %s\r\nUser: %s\r\nTotal Sessions: %d\r\nProblem Sessions: %d.  A problem session has at least one error or has crashed.\r\nGenerated: %8$s\r\n",
						problemLabel, sessionPlural, effectiveAppName, Log.getSessionSummary().getHostName(),
						Log.getSessionSummary().getFullyQualifiedUserName(), sessions, problemSessions,
						OffsetDateTime.now().format(TimeConversion.CS_DATETIMEOFFSET_FORMAT)));
			} else {
				if (sessions == 1) {
					sessionPlural = "";
				}

				newPackage.setCaption(String.format("%s Sessions from %s (%d Session%s)", effectiveAppName,
						effectiveUserName, sessions, sessionPlural));
				newPackage.setDescription(String.format(
						"Product: %s\r\nComputer: %s\r\nUser: %s\r\nTotal Sessions: %d\r\nGenerated: %s\r\n",
						effectiveAppName, Log.getSessionSummary().getHostName(),
						Log.getSessionSummary().getFullyQualifiedUserName(), sessions, OffsetDateTime.now().format(TimeConversion.CS_DATETIMEOFFSET_FORMAT)));
			}

			if (!Log.getSilentMode()) {
				Log.write(LogMessageSeverity.VERBOSE, LOG_CATEGORY, "Completed creating working package",
						"Package is momentarily written to a temporary file.\r\nCaption: %s\r\nDescription: %s\r\nPackaging Complete: %s",
						newPackage.getCaption(), newPackage.getDescription(), packagingState.isComplete());
			}
		}

		return newPackage;
	}

	/**
	 * Log the request information for a send to server request.
	 *
	 * @param overrideConfiguration the override configuration
	 * @param markAsRead the mark as read
	 * @param purgeSentSessions the purge sent sessions
	 * @param serverConfiguration the server configuration
	 */
	private static void logSendToServer(boolean overrideConfiguration, boolean markAsRead, boolean purgeSentSessions,
			ServerConfiguration serverConfiguration) {
		StringBuilder message = new StringBuilder(1024);

		String hubConnectionParameters;
		if (!overrideConfiguration) {
			message.append("Sessions are being sent using application default Server settings." + "\r\n");
			hubConnectionParameters = "";
		} else {
			if (serverConfiguration.getUseGibraltarService()) {
				message.append("Sessions are being sent to the Loupe Service.\r\n" + "\r\n");
				hubConnectionParameters = "\r\nHub Customer: %s";
			} else {
				message.append("Sessions are being sent to a private Loupe Server.\r\n" + "\r\n");
				hubConnectionParameters = "\r\nServer: %2$s\r\nPort:  %3$s\r\nUse SSL:  %4$s\r\nApplication Base Directory:  %5$s\r\nRepository:  %6$s";
			}
		}

		if ((markAsRead) && (purgeSentSessions)) {
			message.append(
					"Sessions will be marked as read and removed from the local computer once confirmed by the server."
							+ "\r\n");
		} else if (markAsRead) {
			message.append("Sessions will be marked as read once confirmed by the server." + "\r\n");
		} else if (purgeSentSessions) {
			message.append("Sessions will be removed from the local computer once confirmed by the server." + "\r\n");
		}

		if (TypeUtils.isNotBlank(hubConnectionParameters)) {
			message.append(hubConnectionParameters);

			Log.write(LogMessageSeverity.INFORMATION, LOG_CATEGORY, "Sending sessions to Server", message.toString(), serverConfiguration.getCustomerName(),
					serverConfiguration.getServer(), serverConfiguration.getPort(), serverConfiguration.getUseSsl(), serverConfiguration.getApplicationBaseDirectory(),
					serverConfiguration.getRepository());
		} else {
			Log.write(LogMessageSeverity.INFORMATION, LOG_CATEGORY, "Sending sessions to Server", message.toString());
		}

	}

	/**
	 * Performs the actual packaging and storing of sessions in a file, safe for
	 * async calling.
	 *
	 * @param sessionCriteria the session criteria
	 * @param markAsRead the mark as read
	 * @param fullFileNamePath the full file name path
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void actionSendToFile(EnumSet<SessionCriteria> sessionCriteria, boolean markAsRead, String fullFileNamePath)
			throws IOException {

		// special case: if session criteria includes active session then we have to
		// split the file.
		// Go ahead and end the current file. We need to be sure that there is an up to
		// date file when the copy runs.
		if (sessionCriteria.contains(SessionCriteria.ACTIVE) || sessionCriteria.contains(SessionCriteria.ALL_SESSIONS)) {
			Log.endFile("Creating Package including active session");
		}

		// run the maintenance merge to make sure we have the latest sessions.
		this.repository.refresh(false, true, sessionCriteria);

		// find all of the sessions so we can subset it downstream.
		actionSendToFile(new SessionCriteriaPredicate(getProductName(), getApplicationName(), sessionCriteria),
				markAsRead, fullFileNamePath);
	}

	/**
	 * Performs the actual packaging and storing of sessions in a file, safe for
	 * async calling.
	 *
	 * @param sessionPredicate the session predicate
	 * @param markAsRead the mark as read
	 * @param fullFileNamePath the full file name path
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void actionSendToFile(Predicate<ISessionSummary> sessionPredicate, boolean markAsRead,
			String fullFileNamePath) throws IOException {
		if (!Log.getSilentMode()) {
			Log.write(LogMessageSeverity.VERBOSE, LOG_CATEGORY, "Starting asynchronous send to file", null);
		}

		String destinationFileNamePath = fullFileNamePath;

		Log.write(LogMessageSeverity.INFORMATION, LOG_CATEGORY, "Sending package to file",
				"When complete sessions will be in the file '%s'.%s", destinationFileNamePath,
				(markAsRead ? "If successful, sessions will be marked as read and not sent again." : null));

		// clean up the file name to make sure it has the right extension and no
		// goofiness we can't deal with.
		String currentExtension = null;
		String targetExtension = Log.PACKAGE_EXTENSION;
		destinationFileNamePath = destinationFileNamePath.trim();
		currentExtension = FileUtils.getFileExtension(destinationFileNamePath);

		if (TypeUtils.isBlank(currentExtension)) {
			// no existing extension, add it
			destinationFileNamePath = destinationFileNamePath.trim() + "." + targetExtension;
		} else if (!currentExtension.equalsIgnoreCase(targetExtension)) {
			// replace the existing extension.
			destinationFileNamePath = destinationFileNamePath.substring(0,
					destinationFileNamePath.length() - currentExtension.length()) + "." + targetExtension;
		}

		FileTransportPackage fileTransportPackage = null;
		try // so we can be sure we dispose the file transport package
		{
			ISessionSummaryCollection selectedSessions = findPackageSessions(sessionPredicate, false);

			// see if there's anything to actually package...
			if ((selectedSessions != null) && (selectedSessions.size() > 0)) {
				// what's the max package size? it's the free space of the drive we're writing
				// to or 2GB, whichever is less.
				Path path = Paths.get(destinationFileNamePath);
				int maxPackageSize = (int) Math.min(path.getRoot().toFile().getFreeSpace(), 2147483647); // one byte
				// below 2GB

				PackagingState packagingState = new PackagingState();

				// create our one transportable package. It'll get disposed when the file
				// transport package below gets disposed.
				SimplePackage newPackage = createTransportablePackage(selectedSessions, maxPackageSize, false,
						packagingState, destinationFileNamePath);

				// we may not get back a transportable package in some cases - like when all of
				// the remaining sessions are not exportable.
				if (newPackage != null) {
					fileTransportPackage = new FileTransportPackage(getProductName(), getApplicationName(), newPackage,
							destinationFileNamePath);
				}
			}

			if (fileTransportPackage == null) {
				// this is really a duplicate of the output a few clauses above
				if (!Log.getSilentMode()) {
					Log.write(LogMessageSeverity.VERBOSE, LOG_CATEGORY, "No Sessions to Send",
							"The packager process didn't find any sessions to send in the package based on the selection criteria.");
				}
			} else {
				if (!Log.getSilentMode()) {
					Log.write(LogMessageSeverity.VERBOSE, LOG_CATEGORY, "Writing file package to destination",
							"The final package is being written to '%s'", destinationFileNamePath);
				}
				fileTransportPackage.send();

				if (!Log.getSilentMode()) {
					Log.write(LogMessageSeverity.VERBOSE, LOG_CATEGORY, "Package Written to Disk",
							"The packager was able to write a package to %s", destinationFileNamePath);
				}

				if (markAsRead) {
					fileTransportPackage.markContentsAsRead(this.repository);
				}
			}
		} finally {
			// make sure we dispose the transport package so it disposes the inner package.
			if (fileTransportPackage != null) {
				IOUtils.closeQuietly(fileTransportPackage);
			}
		}

		if (!Log.getSilentMode()) {
			Log.write(LogMessageSeverity.VERBOSE, null, "Completed asynchronous package send to file", null);
		}
	}

	/**
	 * Performs the actual packaging and transmission of sessions via SDS, safe for
	 * async calling.
	 *
	 * @param sessionCriteria the session criteria
	 * @param markAsRead the mark as read
	 * @param purgeSentSessions the purge sent sessions
	 * @param connectionSpecified the connection specified
	 * @param serverConfiguration the server configuration
	 * @throws Exception the exception
	 */
	private void actionSendToServer(SessionCriteria sessionCriteria, boolean markAsRead, boolean purgeSentSessions,
			boolean connectionSpecified, ServerConfiguration serverConfiguration) throws Exception {
		if (sessionCriteria == SessionCriteria.NONE) {
			// special case: All they asked for is none, which means, well... none.
			return;
		}

		// special case: if session criteria includes active session then we have to
		// split the file.
		// Go ahead and end the current file. We need to be sure that there is an up to
		// date file when the copy runs.
		if ((SessionCriteria.ACTIVE.getValue() & sessionCriteria.getValue()) == SessionCriteria.ACTIVE.getValue()) {
			Log.endFile("Creating Package including active session");
		}

		// run the maintenance merge to make sure we have the latest sessions.
		this.repository.refresh(false, true, EnumSet.of(sessionCriteria));

		// find all of the sessions so we can subset it downstream.
		actionSendToServer(new SessionCriteriaPredicate(getProductName(), getApplicationName(), EnumSet.of(sessionCriteria)),
				markAsRead, purgeSentSessions, connectionSpecified, serverConfiguration);
	}

	/**
	 * Performs the actual packaging and transmission of sessions via SDS, safe for
	 * async calling.
	 *
	 * @param sessionPredicate the session predicate
	 * @param markAsRead the mark as read
	 * @param purgeSentSessions the purge sent sessions
	 * @param connectionSpecified the connection specified
	 * @param serverConfiguration the server configuration
	 * @throws Exception the exception
	 */
	private void actionSendToServer(Predicate<ISessionSummary> sessionPredicate, boolean markAsRead,
			boolean purgeSentSessions, boolean connectionSpecified, ServerConfiguration serverConfiguration)
			throws Exception {
		if (!Log.getSilentMode()) {
			Log.write(LogMessageSeverity.VERBOSE, LOG_CATEGORY, "Starting asynchronous send to web", null);
		}

		logSendToServer(connectionSpecified, markAsRead, purgeSentSessions, serverConfiguration);

		// before we waste any time, lets see if we're going to be successful.
		if (connectionSpecified) {
			// this is a little odd but we need to change exception types - this throws an
			// invalid operation exception, we need an argument exception.
			try {
				serverConfiguration.validate();
			} catch (IllegalStateException ex) {
				throw new IllegalArgumentException(ex.getMessage(), ex);
			}
		} else {
			// find our running configuration and validate that.
			Log.getConfiguration().getServer().validate();
		}

		ISessionSummaryCollection selectedSessions = findPackageSessions(sessionPredicate, false);

		// see if there's anything to actually package...
		if ((selectedSessions != null) && (selectedSessions.size() > 0)) {
			// now we connect to the server and send each of these sessions
			try (RepositoryPublishClient publishClient = connectionSpecified
					? new RepositoryPublishClient(this.repository, serverConfiguration)
					: new RepositoryPublishClient(this.repository);) {
				// try to connect. If we can't do that, there's no point.
				HubConnectionStatus status = publishClient.canConnect();
				if (!status.isValid()) {
					throw new GibraltarNetworkException(String.format("Unable to send sessions to server."
							+ "\r\n%s\r\nIt's possible that your Internet connection is unavailable or that software on your computer"
							+ " is blocking network traffic.\r\n\r\nVerify that you have an active Internet connection and that you don't"
							+ " have software on your computer that will block applications communicating to the Internet.",
							status.getMessage()));
				} else {
					for (ISessionSummary session : selectedSessions) {
						publishClient.uploadSession(session.getId(), 2, purgeSentSessions); // we give it a maximum of
																							// two retries before we
																							// give up on the
																							// connection.

						if (markAsRead) {
							LocalRepository localRepository = publishClient.getRepository();
							try {
								localRepository.setSessionsNew(Arrays.asList(session.getId()), false);
							} catch (RuntimeException ex) {
								if (!Log.getSilentMode()) {
									Log.write(LogMessageSeverity.WARNING, LogWriteMode.QUEUED, ex, LOG_CATEGORY,
											"Error marking an included session as read",
											"Unable to mark a session we successfully uploaded as read.  This won't prevent sessions from being sent.  Exception:\r\n%s",
											ex.getMessage());
								}
							}
						}
					}
				}
			} catch (IOException e) {
				throw e;
			}
		} else {
			if (!Log.getSilentMode()) {
				Log.write(LogMessageSeverity.VERBOSE, LOG_CATEGORY, "No Sessions to Send",
						"The packager process didn't find any sessions to send in the package based on the selection criteria.");
			}
		}

		if (!Log.getSilentMode()) {
			Log.write(LogMessageSeverity.VERBOSE, null, "Completed session transmission to server", null);
		}
	}

	/**
	 * The Class PackagingState.
	 */
	private static class PackagingState {
		/**
		 * The last session that we either packaged or attempted to package, and should
		 * start immediately after.
		 */
		private UUID lastSessionId;

		/**
		 * Gets the last session id.
		 *
		 * @return the last session id
		 */
		public UUID getLastSessionId() {
			return this.lastSessionId;
		}

		/**
		 * Sets the last session id.
		 *
		 * @param lastSessionId the new last session id
		 */
		public void setLastSessionId(UUID lastSessionId) {
			this.lastSessionId = lastSessionId;
		}

		/**
		 * The last session file that we either packaged or attempted to package, and
		 * should start immediately after.
		 */
		private UUID lastFileId;

		/**
		 * Gets the last file id.
		 *
		 * @return the last file id
		 */
		public UUID getLastFileId() {
			return this.lastFileId;
		}

		/**
		 * Sets the last file id.
		 *
		 * @param lastSessionId the new last file id
		 */
		public void setLastFileId(UUID lastSessionId) {
			this.lastFileId = lastSessionId;
		}

		/**
		 * The session to start the next package on. The NextSessionStream property will
		 * be set if this is set.
		 */
		private UUID nextSessionId;

		/**
		 * Gets the next session id.
		 *
		 * @return the next session id
		 */
		public UUID getNextSessionId() {
			return this.nextSessionId;
		}

		/**
		 * Sets the next session id.
		 *
		 * @param nextSessionId the new next session id
		 */
		public void setNextSessionId(UUID nextSessionId) {
			this.nextSessionId = nextSessionId;
		}

		/**
		 * A working session stream that could not be stored into the last package.
		 */
		private RandomAccessFile nextSessionFile;

		/**
		 * Gets the next session file.
		 *
		 * @return the next session file
		 */
		public RandomAccessFile getNextSessionFile() {
			return this.nextSessionFile;
		}

		/**
		 * Sets the next session stream.
		 *
		 * @param nextSessionFile the new next session stream
		 */
		public void setNextSessionStream(RandomAccessFile nextSessionFile) {
			this.nextSessionFile = nextSessionFile;
		}

		/** Indicates if all of the sessions have been packaged (packaging is therefore complete) or not. */
		private boolean complete;

		/**
		 * Checks if is complete.
		 *
		 * @return true, if is complete
		 */
		public final boolean isComplete() {
			return this.complete;
		}

		/**
		 * Sets the checks if is complete.
		 *
		 * @param value the new checks if is complete
		 */
		public final void setIsComplete(boolean value) {
			this.complete = value;
		}
	}

}