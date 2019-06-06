package com.onloupe.core.server;

import java.io.Closeable;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;

import com.onloupe.configuration.ServerConfiguration;
import com.onloupe.core.server.data.ClientRepositoryXml;
import com.onloupe.core.server.data.DataConverter;
import com.onloupe.core.server.data.HubConfigurationXml;
import com.onloupe.core.server.data.HubStatusXml;
import com.onloupe.core.server.data.LiveStreamServerXml;
import com.onloupe.core.util.CodeConversionHelpers;
import com.onloupe.core.util.IOUtils;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.log.LogMessageSeverity;
import com.onloupe.model.system.Version;


/**
 * A web channel specifically designed to work with the Gibraltar Hub.
 */
public class HubConnection implements Closeable {
	
	/** The web request header to add for our hash. */
	public static final String SHA1_HASH_HEADER = "X-Gibraltar-Hash";

	/** The Constant LOG_CATEGORY. */
	public static final String LOG_CATEGORY = "Loupe.Repository.Hub";
	
	/** The Constant SDS_SERVER_NAME. */
	public static final String SDS_SERVER_NAME = "hub.gibraltarsoftware.com";
	
	/** The Constant SDS_ENTRY_PATH. */
	private static final String SDS_ENTRY_PATH = "/Customers/%s";

	/**
	 * The version number for the new Gibraltar 3.0 features
	 */
	public static final Version hub30ProtocolVersion = new Version(1, 2);

	/**
	 * The version number for the new Gibraltar 3.8 features
	 */
	public static final Version hub38ProtocolVersion = new Version(1, 4);

	/** The latest version of the protocol we understand. */
	public static final Version clientProtocolVersion = hub38ProtocolVersion;

	/** The lock. */
	private final Object lock = new Object();
	
	/** The channel lock. */
	private final Object channelLock = new Object();

	/** The root configuration. */
	// these are the root connection parameters from the configuration.
	private ServerConfiguration rootConfiguration;

	/** The test url. */
	private String testUrl;
	
	/** The current channel. */
	private WebChannel currentChannel; // the current hub we're connected to. //PROTECTED BY CHANNELLOCK
	
	/** The enable logging. */
	private boolean enableLogging; // PROTECTED BY LOCK

	/** The status lock. */
	// status information
	private final Object statusLock = new Object();
	
	/** The have tried to connect. */
	private volatile boolean haveTriedToConnect; // volatile instead of lock to avoid locks in locks
	
	/** The hub repository. */
	private HubRepository hubRepository; // PROTECTED BY STATUSLOCK
	
	/** The hub status. */
	private HubConnectionStatus hubStatus; // PROTECTED BY STATUSLOCK

	// Security information. if SupplyCredentials is set, then the other three items
	/** The use credentials. */
	// must be set.
	private boolean useCredentials; // PROTECTED BY LOCK
	
	/** The use repository credentials. */
	private boolean useRepositoryCredentials; // PROTECTED BY LOCK
	
	/** The client repository id. */
	private UUID clientRepositoryId; // PROTECTED BY LOCK
	
	/** The key container name. */
	private String keyContainerName; // PROTECTED BY LOCK
	
	/** The use machine store. */
	private boolean useMachineStore; // PROTECTED BY LOCK

	/**
	 * Create a new server connection using the provided configuration.
	 *
	 * @param configuration the configuration
	 */
	public HubConnection(ServerConfiguration configuration) {
		this.rootConfiguration = configuration;
	}

	/** The logger to use in this process. */
	private static IClientLogger logger;

	/**
	 * Gets the logger.
	 *
	 * @return the logger
	 */
	public static IClientLogger getLogger() {
		return logger;
	}

	/**
	 * Sets the logger.
	 *
	 * @param value the new logger
	 */
	public static void setLogger(IClientLogger value) {
		logger = value;
	}

	/**
	 * Indicates if logging for events on the web channel is enabled or not.
	 *
	 * @return the enable logging
	 */
	public final boolean getEnableLogging() {
		return this.enableLogging;
	}

	/**
	 * Sets the enable logging.
	 *
	 * @param value the new enable logging
	 */
	public final void setEnableLogging(boolean value) {
		synchronized (this.lock) {
			if (value != this.enableLogging) {
				this.enableLogging = value;

				// update the existing channel, if necessary.
				synchronized (this.channelLock) {
					if (this.currentChannel != null) {
						this.currentChannel.setEnableLogging(this.enableLogging);
					}

					this.channelLock.notifyAll();
				}
			}

			this.lock.notifyAll();
		}
	}

	/**
	 * Identify our relationship Id and credential configuration for communicating
	 * with the server.
	 *
	 * @param clientRepositoryId the client repository id
	 * @param useApiKey the use api key
	 * @param keyContainerName the key container name
	 * @param useMachineStore the use machine store
	 */
	public final void setCredentials(UUID clientRepositoryId, boolean useApiKey, String keyContainerName,
			boolean useMachineStore) {
		if (clientRepositoryId == null) {
			throw new NullPointerException("clientRepositoryId");
		}

		if (TypeUtils.isBlank(keyContainerName)) {
			throw new NullPointerException("keyContainerName");
		}

		synchronized (this.lock) {
			this.useCredentials = true;
			this.useRepositoryCredentials = useApiKey;
			this.clientRepositoryId = clientRepositoryId;
			this.keyContainerName = keyContainerName;
			this.useMachineStore = useMachineStore;

			this.lock.notifyAll();
		}
	}

	/**
	 * Attempts to connect to the server and returns information about the
	 * connection status.
	 * 
	 * This method will keep the connection if it is made, improving efficiency if
	 * you are then going to use the connection.
	 *
	 * @return True if the configuration is valid and the server is available, false
	 *         otherwise.
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	@SuppressWarnings("incomplete-switch")
	public final HubConnectionStatus canConnect() throws IOException, Exception {
		WebChannel currentChannel;
		synchronized (this.channelLock) {
			currentChannel = this.currentChannel;
			this.channelLock.notifyAll();
		}

		// if we have a current connection we'll need to see if we can keep using it
		if (currentChannel != null) {
			HubStatus status = HubStatus.MAINTENANCE;
			try {
				HubConfigurationGetRequest configurationGetRequest = new HubConfigurationGetRequest();
				currentChannel.executeRequest(configurationGetRequest, 1); // we'd like it to succeed, so we'll give it
																			// one retry

				// now, if we got back a redirect we need to go THERE to get the status.
				HubConfigurationXml configurationXml = configurationGetRequest.getConfiguration();
				if (!configurationXml.getredirectRequested()
						&& (configurationXml.getstatus() == HubStatusXml.AVAILABLE)) {
					// we can just keep using this connection, so lets do that.
					return new HubConnectionStatus(null, true, HubStatus.AVAILABLE, null);
				}

				status = HubStatus.forValue(configurationXml.getstatus().getValue()); // we define these to be equal.
			} catch (Exception ex) {
				if (!getLogger().getSilentMode()) {
					getLogger().write(LogMessageSeverity.INFORMATION, ex, false, LOG_CATEGORY,
							"Unable to get server configuration, connection will be assumed unavailable.",
							"Due to an exception we were unable to retrieve the server configuration.  We'll assume the server is in maintenance until we can succeed.  Exception: %s\r\n",
							ex.getMessage());
				}
			}

			// drop the connection - we might do better, unless we're already at the root.
			if (isRootHub(currentChannel.getHostName(), currentChannel.getPort(), currentChannel.isUseSSL(),
					currentChannel.getApplicationBaseDirectory())) {
				// we are the root - what we are is the best we are.
				String message = null;
				switch (status) {
				case EXPIRED:
					message = "your subscription is expired";
					break;
				case MAINTENANCE:
					message = "the repository is in maintenance";
					break;
				}
				return new HubConnectionStatus(null, false, status, message);
			}
		}

		// if we don't have a connection (either we didn't before or we just invalidated
		// the current connection) get a new one.
		HubConnectionStatus connectionStatus = connect();
		setCurrentChannel(connectionStatus.getChannel());

		// before we return, lets set our status to track what we just calculated.
		synchronized (this.statusLock) {
			this.hubStatus = new HubConnectionStatus(connectionStatus.getConfiguration(),
					connectionStatus.isValid(), connectionStatus.getStatus(), connectionStatus.getMessage());
			this.hubRepository = connectionStatus.getRepository();
		}

		return connectionStatus;
	}

	/**
	 * Attempts to connected to the specified hub and returns information about the
	 * connection status. The connection is then dropped.
	 *
	 * @param configuration The configuration to test
	 * @return The connection status information
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	public static HubConnectionStatus canConnect(ServerConfiguration configuration) throws IOException, Exception {
		HubConnectionStatus connectionStatus = connect(configuration);

		if (connectionStatus.getStatus() == HubStatus.AVAILABLE) {
			// wait, one last check - what about protocol?
			if (connectionStatus.getRepository().getProtocolVersion().compareTo(hub30ProtocolVersion) < 0) {
				return new HubConnectionStatus(configuration, false, HubStatus.MAINTENANCE,
						"The server is implementing an older, incompatible version of the hub protocol.");
			}
		}

		if (connectionStatus.getChannel() != null) {
			IOUtils.closeQuietly(connectionStatus.getChannel());
		}

		// we don't want to return the status we got because it has a real channel on
		// it.
		return new HubConnectionStatus(configuration, null, connectionStatus.getRepository(),
				connectionStatus.isValid(), connectionStatus.getStatus(), connectionStatus.getMessage());
	}

	/**
	 * Execute the provided request.
	 *
	 * @param newRequest the new request
	 * @param maxRetries The maximum number of times to retry the connection. Use -1
	 *                   to retry indefinitely.
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	public final void executeRequest(IWebRequest newRequest, int maxRetries) throws IOException, Exception {
		// make sure we have a channel
		WebChannel channel = getCurrentChannel(); // this throws exceptions when it can't connect and is thread safe.

		// if we have a channel and NOW get an exception, here is where we would recheck
		// the status of our connection.
		boolean retryAuthentication = false;
		boolean resetAndRetry = false;
		try {
			channel.executeRequest(newRequest, maxRetries);
		} catch (WebChannelAuthorizationException ex) {
			// request better credentials..
			getLogger().write(LogMessageSeverity.WARNING, ex, true, LOG_CATEGORY,
					"Requesting updated credentials for the server connection due to " + ex.getClass(),
					"We're going to assume the user can provide current credentials.\r\nDetails: %s", ex.getMessage());
			if (CachedCredentialsManager.updateCredentials(channel, this.clientRepositoryId, false)) {
				// they entered new creds.. lets give them a go.
				retryAuthentication = true;
			} else {
				// they canceled, lets call it.
				throw ex;
			}
		} catch (WebChannelConnectFailureException e) {
			// clear our current channel and try again if we're on a child server.
			if (!isRootHub(channel.getHostName(), channel.getPort(), channel.isUseSSL(),
					channel.getApplicationBaseDirectory())) {
				resetAndRetry = true;
			}
		}

		if (retryAuthentication) {
			executeRequest(newRequest, maxRetries);
		} else if (resetAndRetry) {
			resetChannel(); // safely clears the current channel and gets a fresh one if possible
		}
	}

	/**
	 * Create a new subscription to this hub for the supplied repository information
	 * and shared secret.
	 *
	 * @param repositoryXml the repository xml
	 * @return The client repository information retrieved from the server.
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	public final ClientRepositoryXml createSubscription(ClientRepositoryXml repositoryXml)
			throws IOException, Exception {
		ClientRepositoryUploadRequest request = new ClientRepositoryUploadRequest(repositoryXml);

		// we have to use distinct credentials for this so we have to swap the
		// credentials on the connection.
		WebChannel channel = getCurrentChannel();

		boolean retry;
		do {
			retry = false; // so we'll exit by default
			try {
				channel.executeRequest(request, 1);
			} catch (WebChannelAuthorizationException ex) {
				// request better credentials..
				getLogger().write(LogMessageSeverity.WARNING, ex, true, LOG_CATEGORY,
						"Requesting updated credentials for the server connection due to " + ex.getClass(),
						"We're going to assume the user can provide current credentials.\r\nDetails: %s",
						ex.getMessage());
				retry = CachedCredentialsManager.updateCredentials(channel, this.clientRepositoryId, true);

				if (!retry) {
					throw ex;
				}
			}
		} while (retry);

		return request.getResponseRepository();
	}

	/**
	 * Authenticate now (instead of waiting for a request to fail).
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	public final void authenticate() throws IOException, Exception {
		// get the current connection and authenticate it
		WebChannel channel = getCurrentChannel();
		channel.authenticate();
	}

	/**
	 * Indicates if the connection is currently authenticated.
	 * 
	 * False if no connection, connection doesn't support authentication, or
	 * connection is not authenticated.
	 *
	 * @return true, if is authenticated
	 */
	public final boolean isAuthenticated() {
		boolean isAuthenticated = false;

		synchronized (this.channelLock) {
			if ((this.currentChannel != null) && (this.currentChannel.getAuthenticationProvider() != null)) {
				isAuthenticated = this.currentChannel.getAuthenticationProvider().isAuthenticated();
			}

			this.channelLock.notifyAll();
		}

		return isAuthenticated;
	}

	/**
	 * Indicates if the connection is currently connected without attempting a new
	 * connection
	 * 
	 * False if no connection. Connection may fail at any time.
	 *
	 * @return true, if is connected
	 */
	public final boolean isConnected() {
		boolean isConnected = false;

		synchronized (this.channelLock) {
			if (this.currentChannel != null && this.currentChannel.getConnectionState() != null) {
				switch (this.currentChannel.getConnectionState()) {
				case CONNECTED:
				case TRANSFERING_DATA:
					isConnected = true;
					break;
				default:
					break;
				}
			}

			this.channelLock.notifyAll();
		}

		return isConnected;
	}

	/**
	 * Information about the remote repository
	 * 
	 * Returns null when no server can be contacted.
	 *
	 * @return the repository
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	public final HubRepository getRepository() throws IOException, Exception {
		ensureConnectAttempted();
		synchronized (this.statusLock) {
			return this.hubRepository;
		}
	}

	/**
	 * The current connection status.
	 *
	 * @return the status
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	public final HubConnectionStatus getStatus() throws IOException, Exception {
		ensureConnectAttempted();
		synchronized (this.statusLock) {
			return this.hubStatus;
		}
	}

	/**
	 * Reset the current connection and re-establish it, getting the latest hub
	 * configuration.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	public final void reconnect() throws IOException, Exception {
		resetChannel();
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
		setCurrentChannel(null);
	}

	/**
	 * Make sure we've at least tried to connect to the hub.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	private void ensureConnectAttempted() throws IOException, Exception {
		if (!haveTriedToConnect) {
			getCurrentChannel(); // this will try to connect.
		}
	}

	/**
	 * Gets the current channel.
	 *
	 * @return the current channel
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	private WebChannel getCurrentChannel() throws IOException, Exception {
		WebChannel currentChannel;
		synchronized (this.channelLock) {
			currentChannel = this.currentChannel;
			this.channelLock.notifyAll();
		}

		// CAREFUL: Between here and the end of this if do not access a property that
		// checks _HaveTriedToConnect because if so we get into a big bad loop.
		if (currentChannel == null) {
			try {
				// try to connect.
				HubConnectionStatus connectionStatus = connect();

				WebChannel newChannel = connectionStatus.getChannel();

				// before we return, lets set our status to track what we just calculated.
				synchronized (this.statusLock) {
					this.hubStatus = new HubConnectionStatus(connectionStatus.getConfiguration(),
							connectionStatus.isValid(), connectionStatus.getStatus(), connectionStatus.getMessage());
					this.hubRepository = connectionStatus.getRepository();
				}

				// if we couldn't connect we'll have a null channel (connect returns null)
				if (newChannel == null) {
					throw new WebChannelConnectFailureException(connectionStatus.getMessage());
				}

				// otherwise we need to bind up our events and release the existing - use our
				// setter for that
				setCurrentChannel(newChannel);
				currentChannel = newChannel;
			} finally {
				// whether we succeeded or failed, we tried.
				this.haveTriedToConnect = true;
			}
		}

		return currentChannel;
	}

	/**
	 * Sets the current channel.
	 *
	 * @param channel the new current channel
	 */
	private void setCurrentChannel(WebChannel channel) {
		synchronized (this.channelLock) {
			// are they the SAME? if so nothing to do
			if (channel == this.currentChannel) {
				return;
			}

			// otherwise, release any existing connection...
			if (this.currentChannel != null) {
				IOUtils.closeQuietly(this.currentChannel);
				this.currentChannel = null;

				this.haveTriedToConnect = false;
			}

			// and establish the new connection.
			if (channel != null) {
				this.currentChannel = channel;
			}

			this.channelLock.notifyAll();
		}
	}

	/**
	 * Get a test URL to access through a web browser.
	 *
	 * @return the end user test url
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	public final String getEndUserTestUrl() throws IOException, Exception {
		if (TypeUtils.isBlank(this.testUrl)) {
			String fullHubUrl;
			WebChannel channel = null;
			try {
				// first try to resolve it through a real connection to determine the effective
				// server based on redirection
				HubConnectionStatus connectionStatus = connect(this.rootConfiguration);

				// if we weren't able to connect fully we will have gotten a null channel;
				// create just a configured channel with the parameters.
				if (connectionStatus.getChannel() == null) {
					channel = createChannel(this.rootConfiguration);
				} else {
					channel = connectionStatus.getChannel();
				}

				fullHubUrl = channel.getEntryURI();
			} finally {
				if (channel != null) {
					IOUtils.closeQuietly(channel);
				}
			}

			// if this is a hub URL we need to pull off the HUB suffix to make it a valid
			// HTML URL.
			if (TypeUtils.isNotBlank(fullHubUrl)) {
				if (fullHubUrl.endsWith("HUB")) {
					fullHubUrl = CodeConversionHelpers.substring(fullHubUrl, 0, fullHubUrl.length() - 4); // -3 for HUB, -1 to
																								// offset length to
																								// start position
				} else if (fullHubUrl.endsWith("HUB/")) {
					fullHubUrl = CodeConversionHelpers.substring(fullHubUrl, 0, fullHubUrl.length() - 4); // -3 for HUB/, -1
																								// to offset length
																								// to start position
				}
			}
			this.testUrl = fullHubUrl;
		}

		return this.testUrl;
	}

	/**
	 * The URL to the server's version info structure.
	 *
	 * @return the update url
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	public final String getUpdateUrl() throws IOException, Exception {
		String testUrl = getEndUserTestUrl(); // we are relying on the implementation of this pointing to the base of
												// the tenant.
		return testUrl + "Hub/VersionInfo.ini";
	}

	/**
	 * Reset the stored channel and reconnect.
	 *
	 * @return the web channel
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	private WebChannel resetChannel() throws IOException, Exception {
		// force the channel to drop..
		setCurrentChannel(null);

		// and get a fresh one...
		return getCurrentChannel();
	}

	/**
	 * Connect to the hub (or another hub if the configured hub is redirecting).
	 *
	 * @return The last web channel it was able to connect to after processing
	 *         redirections, if that channel is available.
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	private HubConnectionStatus connect() throws IOException, Exception {
		HubConnectionStatus connectionStatus = connect(this.rootConfiguration);
		if (connectionStatus.getChannel() != null) {
			// copy our current settings into it.
			synchronized (this.lock) {
				connectionStatus.getChannel().setEnableLogging(this.enableLogging);

				if (this.useCredentials) {
					connectionStatus.getChannel()
							.setAuthenticationProvider(CachedCredentialsManager.getCredentials(
									connectionStatus.getChannel(), this.useRepositoryCredentials,
									this.clientRepositoryId, this.keyContainerName, this.useMachineStore));
				}

				this.lock.notifyAll();
			}
		}

		return connectionStatus;
	}

	/**
	 * Connects to the specified hub (or another hub if this hub is redirecting).
	 *
	 * @param configuration the configuration
	 * @return The last web channel it was able to connect to after processing
	 *         redirections.
	 */
	private static HubConnectionStatus connect(ServerConfiguration configuration) {
		WebChannel channel = null;
		boolean canConnect = true;
		HubStatus status = HubStatus.MAINTENANCE; // a reasonable default.
		String statusMessage = null;
		Optional<UUID> serverRepositoryId = Optional.empty();
		Optional<OffsetDateTime> expirationDt = Optional.empty();
		Version protocolVersion = new Version(0, 0);
		NetworkConnectionOptions agentLiveStream = null;
		NetworkConnectionOptions clientLiveStream = null;

		// first, is it a valid config? No point in trying to connect if it's a bum
		// config.
		HubConnectionStatus connectionStatus;
		try {
			configuration.validate();
		} catch (RuntimeException ex) {
			canConnect = false;
			statusMessage = "Invalid configuration: " + ex.getMessage();
			connectionStatus = new HubConnectionStatus(configuration, false, status, statusMessage);
			return connectionStatus;
		}

		// and now try to connect to the server
		try {
			channel = createChannel(configuration);
			HubConfigurationGetRequest configurationGetRequest = new HubConfigurationGetRequest();
			channel.executeRequest(configurationGetRequest, 1); // we'd like it to succeed, so we'll give it one retry

			HubConfigurationXml configurationXml = configurationGetRequest.getConfiguration();

			// now, if we got back a redirect we need to go THERE to get the status.
			if (configurationXml.getredirectRequested()) {
				// recursively try again.
				IOUtils.closeQuietly(channel);
				connectionStatus = connect(configuration);
			} else {
				// set the right status message
				status = HubStatus.forValue(configurationXml.getstatus().getValue());

				switch (status) {
				case AVAILABLE:
					break;
				case EXPIRED:
					statusMessage = "The Server's license has expired.  " + (configuration.getUseGibraltarService()
							? "You can reactivate your license in seconds at www.GibraltarSoftware.com."
							: "To renew your license, run the Administration tool on the Loupe Server.");
					break;
				case MAINTENANCE:
					statusMessage = "The Server is currently undergoing maintenance and can't process requests.";
					break;
				default:
					throw new IndexOutOfBoundsException("status");
				}

				if (configurationXml.getid() != null) {
					serverRepositoryId = Optional.of(UUID.fromString(configurationXml.getid()));
				}

				if (configurationXml.getexpirationDt() != null) {
					expirationDt = Optional.of(DataConverter.fromDateTimeOffsetXml(configurationXml.getexpirationDt()));
				}

				String publicKey = configurationXml.getpublicKey();

				if (TypeUtils.isNotBlank(configurationXml.getprotocolVersion())) {
					protocolVersion = new Version(configurationXml.getprotocolVersion());
				}

				LiveStreamServerXml liveStreamConfig = configurationXml.getliveStream();
				if (liveStreamConfig != null) {
					agentLiveStream = new NetworkConnectionOptions(liveStreamConfig.getagentPort(),
							channel.getHostName(), liveStreamConfig.getuseSsl());
					clientLiveStream = new NetworkConnectionOptions(liveStreamConfig.getclientPort(),
							channel.getHostName(), liveStreamConfig.getuseSsl());
				}

				// We've connected for sure, time to set up our connection status to return to
				// our caller with the full connection info
				connectionStatus = new HubConnectionStatus(configuration, channel, new HubRepository(expirationDt,
						serverRepositoryId, protocolVersion, publicKey, agentLiveStream, clientLiveStream), true,
						status, statusMessage);
			}
		} catch (WebChannelFileNotFoundException e) {
			canConnect = false;
			if (configuration.getUseGibraltarService()) {
				// we'll treat file not found (e.g. customer never existed) as expired to get
				// the right UI behavior.
				status = HubStatus.EXPIRED;
				statusMessage = "The specified customer name is not valid";
			} else {
				statusMessage = "The server does not support this service or the specified directory is not valid";
			}

			connectionStatus = new HubConnectionStatus(configuration, false, status, statusMessage);
		} catch (WebChannelException ex) {
			canConnect = false;
			StatusLine responseStatus = ex.getResponseStatus();
			statusMessage = responseStatus.getReasonPhrase(); // by default we'll use the detailed description we got
																// back from the web server.

			// we want to be somewhat more intelligent in our responses to decode what these
			// might MEAN.
			if (configuration.getUseGibraltarService()) {
				switch (responseStatus.getStatusCode()) {
				case HttpStatus.SC_NOT_FOUND:
				case HttpStatus.SC_BAD_REQUEST:
					status = HubStatus.EXPIRED;
					statusMessage = "The specified customer name is not valid";
					break;
				}
			} else {
				switch (responseStatus.getStatusCode()) {
				case HttpStatus.SC_NOT_FOUND:
					statusMessage = "No service could be found with the provided information";
					break;
				case HttpStatus.SC_BAD_REQUEST:
					statusMessage = "The server does not support this service or the specified directory is not valid";
					break;
				}
			}

			connectionStatus = new HubConnectionStatus(configuration, false, status, statusMessage);
		} catch (Exception ex) {
			canConnect = false;
			statusMessage = ex.getMessage();

			connectionStatus = new HubConnectionStatus(configuration, false, status, statusMessage);
		}

		// before we return make sure we clean up an errant channel if we don't need it.
		if (!canConnect && (channel != null)) {
			IOUtils.closeQuietly(channel);
			channel = null;
		}

		return connectionStatus;
	}

	/**
	 * Create a web channel to the specified server configuration. Low level
	 * primitive that does no redirection.
	 *
	 * @param configuration the configuration
	 * @return the web channel
	 */
	private static WebChannel createChannel(ServerConfiguration configuration) {
		WebChannel channel;

		if (configuration.getUseGibraltarService()) {
			channel = new WebChannel(getLogger(), true, SDS_SERVER_NAME,
					String.format(SDS_ENTRY_PATH, configuration.getCustomerName()), clientProtocolVersion);
		} else {
			// we need to create the right application base directory to get into Hub.
			String entryPath = effectiveApplicationBaseDirectory(configuration.getApplicationBaseDirectory(),
					configuration.getRepository());

			// and now we can actually create the channel! Yay!
			channel = new WebChannel(getLogger(), configuration.getUseSsl(), configuration.getServer(),
					configuration.getPort(), entryPath, clientProtocolVersion);
		}

		return channel;
	}

	/**
	 * Combines application base directory (if not null) and repository (if not
	 * null) into one merged path.
	 *
	 * @param applicationBaseDirectory the application base directory
	 * @param repository the repository
	 * @return the string
	 */
	private static String effectiveApplicationBaseDirectory(String applicationBaseDirectory, String repository) {
		String effectivePath = (applicationBaseDirectory != null) ? applicationBaseDirectory : "";

		if (TypeUtils.isNotBlank(effectivePath)) {
			// check for whether we need to Extension a slash.
			if (!effectivePath.endsWith("/")) {
				effectivePath += "/";
			}
		}

		if (TypeUtils.isNotBlank(repository)) {
			// we want a specific repository - which was created for Loupe Service so it
			// assumes everyone's a "customer". Oh well.
			effectivePath += String.format(SDS_ENTRY_PATH, repository);
		}

		return effectivePath;
	}

	/**
	 * Indicates if we're on the original configured server (the "root") or have
	 * been redirected.
	 *
	 * @param hostName the host name
	 * @param port the port
	 * @param useSsl the use ssl
	 * @param applicationBaseDirectory the application base directory
	 * @return true, if is root hub
	 */
	private boolean isRootHub(String hostName, int port, boolean useSsl, String applicationBaseDirectory) {
		boolean isSameHub = true;

		if (TypeUtils.isBlank(hostName)) {
			// can't be the same - invalid host
			isSameHub = false;
		} else {
			if (this.rootConfiguration.getUseGibraltarService()) {
				if (!hostName.equalsIgnoreCase(SDS_SERVER_NAME)) {
					// it's the wrong server.
					isSameHub = false;
				}

				String entryPath = String.format(SDS_ENTRY_PATH, this.rootConfiguration.getCustomerName());

				if (!entryPath.equals(applicationBaseDirectory)) {
					// it isn't the same customer
					isSameHub = false;
				}
			} else {
				// simpler - we're looking for an exact match on each item.
				if (!hostName.equalsIgnoreCase(this.rootConfiguration.getServer())
						|| (this.rootConfiguration.getPort() != port)
						|| (this.rootConfiguration.getUseSsl() != useSsl)) {
					// it's the wrong server.
					isSameHub = false;
				} else {
					// application base directory is more complicated - we have to take into account
					// if we have a repository set or not.
					String entryPath = effectiveApplicationBaseDirectory(
							this.rootConfiguration.getApplicationBaseDirectory(),
							this.rootConfiguration.getRepository());

					if (!entryPath.equals(applicationBaseDirectory)) {
						// it isn't the same repository
						isSameHub = false;
					}
				}
			}
		}

		return isSameHub;
	}

}