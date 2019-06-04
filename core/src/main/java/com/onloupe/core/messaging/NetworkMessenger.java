package com.onloupe.core.messaging;

import com.onloupe.configuration.IMessengerConfiguration;
import com.onloupe.configuration.NetworkViewerConfiguration;
import com.onloupe.configuration.ServerConfiguration;
import com.onloupe.core.logging.Log;
import com.onloupe.core.logging.LogWriteMode;
import com.onloupe.core.messaging.network.SendSessionCommandMessage;
import com.onloupe.core.server.HubConnection;
import com.onloupe.core.server.HubConnectionStatus;
import com.onloupe.core.server.HubStatus;
import com.onloupe.core.server.LiveSessionPublisher;
import com.onloupe.core.server.NetworkConnectionOptions;
import com.onloupe.core.util.IOUtils;
import com.onloupe.core.util.Multiplexer;
import com.onloupe.core.util.SystemUtils;
import com.onloupe.model.log.LogMessageSeverity;

import java.io.IOException;
import java.nio.file.StandardWatchEventKinds;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

// TODO: Auto-generated Javadoc
/**
 * The Class NetworkMessenger.
 */
public class NetworkMessenger extends MessengerBase implements Observer {
	
	/** The log category to use for messages in this messenger. */
	public static final String LOG_CATEGORY = "Loupe.Network.Messenger";

	/** The Constant DEFAULT_BUFFER_SIZE. */
	private static final int DEFAULT_BUFFER_SIZE = 1000;
	
	/** The Buffer size. */
	private int _BufferSize = DEFAULT_BUFFER_SIZE;

	// NOTE TO MAINTAINERS: Locking is granular based on the connection role on the
	/** The buffer. */
	// following collections, be warned!
	private final ConcurrentLinkedQueue<IMessengerPacket> buffer = new ConcurrentLinkedQueue<IMessengerPacket>(); // LOCKED
																													// BY
																													// ACTIVE
																													/** The pending clients. */
																													// CLIENTS
	private final ArrayList<NetworkWriter> pendingClients = new ArrayList<NetworkWriter>();
	
	/** The active clients. */
	private final ArrayList<NetworkWriter> activeClients = new ArrayList<NetworkWriter>();
	
	/** The dead clients. */
	private final ArrayList<NetworkWriter> deadClients = new ArrayList<NetworkWriter>();

	// if we allow local connections this is the index of the ones we're currently
	/** The local proxy connections. */
	// tracking.
	private final Map<String, LiveSessionPublisher> localProxyConnections = new HashMap<String, LiveSessionPublisher>(); // lock
																															// this
																															// for
																															// data
																															// integrity

	/** The discovery file monitor. */
																															private LocalServerDiscoveryFileMonitor discoveryFileMonitor;

	/** The active remote connection attempt. */
	private volatile boolean activeRemoteConnectionAttempt; // for making sure we only do one server connection attempt
																
																/** The hub connection. */
																// at a time
	private HubConnection hubConnection; // our one and only server connection, if enabled
	
	/** The client. */
	private LiveSessionPublisher client;
	
	/** The enable outbound. */
	private boolean enableOutbound;
	
	/** The connection options. */
	private NetworkConnectionOptions connectionOptions;
	
	/** The closed. */
	private boolean closed;

	/** The hub configuration expiration. */
	private OffsetDateTime hubConfigurationExpiration; // LOCKED BY LOCK
	
	/**
	 * Create a new network messenger.
	 */
	public NetworkMessenger() {
		super("Network", false);
	}

	/**
	 * The list of cached packets that should be in every stream before any other
	 * packet.
	 *
	 * @return the header packets
	 */
	public final ICachedMessengerPacket[] getHeaderPackets() {
		return getPublisher().getHeaderPackets();
	}

	/**
	 * Create a new outbound live viewer to the default server.
	 *
	 * @param repositoryId the repository id
	 * @param channelId the channel id
	 */

	public final void StartLiveView(java.util.UUID repositoryId, java.util.UUID channelId) {
		startLiveView(repositoryId, channelId, 0);
	}

	/**
	 * Start live view.
	 *
	 * @param repositoryId the repository id
	 * @param channelId the channel id
	 * @param sequenceOffset the sequence offset
	 */
	public final void startLiveView(UUID repositoryId, UUID channelId, long sequenceOffset) {
		if (this.connectionOptions != null) {
			startLiveView(this.connectionOptions, repositoryId, channelId, sequenceOffset);
		}
	}

	/**
	 * Create a new outbound live viewer to the default server.
	 *
	 * @param options the options
	 * @param repositoryId the repository id
	 * @param channelId the channel id
	 */

	public final void startLiveView(NetworkConnectionOptions options, java.util.UUID repositoryId,
			java.util.UUID channelId) {
		startLiveView(options, repositoryId, channelId, 0);
	}

	/**
	 * Start live view.
	 *
	 * @param options the options
	 * @param repositoryId the repository id
	 * @param channelId the channel id
	 * @param sequenceOffset the sequence offset
	 */
	public final void startLiveView(NetworkConnectionOptions options, UUID repositoryId, UUID channelId,
			long sequenceOffset) {

		if (channelId == null) {
			throw new NullPointerException("channelId");
		}

		// open an outbound pending connection.
		NetworkWriter newWriter = new NetworkWriter(this, options, repositoryId, channelId, sequenceOffset);
		newWriter.start();
		registerWriter(newWriter);
	}

	/**
	 * Send the matching sessions to the server.
	 *
	 * @param sendSessionCommand the send session command
	 */
	public final void sendToServer(SendSessionCommandMessage sendSessionCommand) {
		try {
			if (!Log.sendSessions(Optional.of(sendSessionCommand.getCriteria()), null, true)) // we love async!
			{
				// since we can't send sessions to the server we'll just roll over the log file
				// for local access.
				Log.endFile("Remote Live Sessions client requested the log file be rolled over");
			}
		} catch (Exception ex) {
			if (!Log.getSilentMode()) {
				Log.write(LogMessageSeverity.ERROR, LogWriteMode.QUEUED, ex, LOG_CATEGORY,
						"Failed to process send to server command",
						"An exception was thrown that prevents us from completing the command:\r\n%s",
						ex.getMessage());
			}
		}
	}

	/**
	 * Send the latest summary to the server.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final void sendSummary() throws IOException {
		// We don't want to use locks so we need to do a point-in-time copy and then
		// iterate through that copy.
		LiveSessionPublisher client = this.client;
		if (client != null) {
			sendSummary(client);
		}

		LiveSessionPublisher[] registeredClients;
		synchronized (this.localProxyConnections) {
			registeredClients = new LiveSessionPublisher[this.localProxyConnections.size()];
			LiveSessionPublisher[] localProxyConnections = this.localProxyConnections.values()
					.toArray(new LiveSessionPublisher[this.localProxyConnections.size()]);
			System.arraycopy(localProxyConnections, 0, registeredClients, 0, localProxyConnections.length);
		}

		for (LiveSessionPublisher liveSessionPublisher : registeredClients) {
			sendSummary(liveSessionPublisher);
		}
	}

	/**
	 * Send the latest summary to the specified publisher.
	 *
	 * @param publisher the publisher
	 */
	private void sendSummary(LiveSessionPublisher publisher) {
		try {
			if (publisher.isConnected()) {
				publisher.sendSummary();
			}
		} catch (Exception ex) {
			if (!Log.getSilentMode()) {
				Log.write(LogMessageSeverity.ERROR, LogWriteMode.QUEUED, ex, LOG_CATEGORY,
						"Failed to send summary to the server or other network proxy",
						"An exception was thrown that prevents us from sending the latest session summary to the server:\r\n"
								+ "Server or Proxy: %s\r\n%s Exception:\r\n%s",
						publisher, ex.getClass().getName(), ex.getMessage());
			}
		}
	}

	/**
	 * Activate writer.
	 *
	 * @param writer the writer
	 * @param sequenceOffset the sequence offset
	 */
	public final void activateWriter(NetworkWriter writer, long sequenceOffset) {
		// dump the queue to it....
		try {
			if (!Log.getSilentMode()) {
				Log.write(LogMessageSeverity.VERBOSE, LOG_CATEGORY, "New remote network viewer connection starting",
						"We will process the connection attempt and feed it our buffered data.\r\nRemote Endpoint: %s\r\nSequence Offset: {1:N0}",
						writer, sequenceOffset);
			}
			synchronized (this.activeClients) // we can't have a gap between starting to dump the buffer and the buffer
												// changing.
			{
				// write out every header packet to the stream
				ICachedMessengerPacket[] headerPackets = getHeaderPackets();
				if (headerPackets != null) {
					writer.write(headerPackets);
				}

				IMessengerPacket[] bufferContents = this.buffer.toArray(new IMessengerPacket[0]);

				if ((sequenceOffset > 0) && (bufferContents.length > 0)) {
					// they have all the packets up through the sequence offset so only later
					// packets
					if (bufferContents[0].getSequence() > sequenceOffset) {
						// All of our packets qualify because even the first one is after our offset. So
						// we just use bufferContents unmodified.
					} else if (bufferContents[bufferContents.length - 1].getSequence() <= sequenceOffset) {
						// *none* of our packets qualify, it's at the end of our buffer, so just clear
						// it.
						bufferContents = new IMessengerPacket[0];
					} else {
						// figure out exactly where in the buffer we should be.
						int firstPacketOffset = 0; // we know the zeroth packet should not be included because we
													// checked that above.
						for (int packetBufferIndex = bufferContents.length
								- 2; packetBufferIndex >= 0; packetBufferIndex--) // we iterate backwards because if
																					// they have any offset they're
																					// likely close to current.
						{
							if (bufferContents[packetBufferIndex].getSequence() <= sequenceOffset) {
								// This is the first packet we should *skip* so the first offset to take is up
								// one.
								firstPacketOffset = packetBufferIndex + 1;
							}
						}

						IMessengerPacket[] offsetBuffer = new IMessengerPacket[bufferContents.length
								- firstPacketOffset]; // inclusive

						// we've been trying unsuccessfully to isolate why we're getting an exception
						// that the destination array isn't long enough.
						try {
							System.arraycopy(bufferContents, firstPacketOffset, offsetBuffer, 0,
									bufferContents.length - firstPacketOffset);
							bufferContents = offsetBuffer;
						} catch (IllegalArgumentException ex) {
							Log.write(LogMessageSeverity.ERROR, LogWriteMode.QUEUED, ex, false, LOG_CATEGORY,
									"Unable to create offset buffer due to " + ex.getClass(),
									"Original Buffer Length: %d\r\nFirst Packet Offset: %d\r\nOffset Buffer Length: %d",
									bufferContents.length, firstPacketOffset, offsetBuffer.length);
						}
					}
				}

				if (bufferContents.length > 0) {
					writer.write(bufferContents);
				}

				// and mark it active if that succeeded
				if (!writer.connectionFailed()) {
					// note that it may have been previously registered so we need to be cautious
					// about this.
					if (!this.activeClients.contains(writer)) {
						this.activeClients.add(writer);
					}
				}
				// if it didn't succeed it should raise its failed event, and in turn we will
				// eventually dispose it in due course.
			}
		} catch (Exception ex) {
			if (!Log.getSilentMode()) {
				try {
					Log.recordException(0, ex, null, LOG_CATEGORY, true);
				} catch (IOException e) {
					// do nothing
				}
			}
		}
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
		setCaption("Network Viewer Messenger");
		setDescription("Messenger implementation that sends session data live over a TCP connection.");
		setOverflowMode(OverflowMode.DROP); // important so we don't block in this mode.

		// try to up cast the configuration to our specific configuration type
		NetworkViewerConfiguration messengerConfiguration = (NetworkViewerConfiguration) configuration;

		if (messengerConfiguration.getAllowLocalClients()) {
			// set up our local discovery file monitor so we will connect with local
			// proxies.
			this.discoveryFileMonitor = new LocalServerDiscoveryFileMonitor();
			this.discoveryFileMonitor.addObserver(this);
			this.discoveryFileMonitor.start();
		}

		if (messengerConfiguration.getAllowRemoteClients()) {
			// we need to monitor & keep a server configuration going.
			ServerConfiguration server = Log.getConfiguration().getServer();

			if (server.getEnabled()) {
				this.hubConnection = new HubConnection(server);
				this.enableOutbound = true;

				if (!Log.getSilentMode()) {
					Log.write(LogMessageSeverity.VERBOSE, LOG_CATEGORY,
							"Remote live view enabled, will be available once connected to server",
							"Server Configuration:\r\n%s", server);
				}
			}
		}

		setAutoFlush(true);
		setAutoFlushInterval(5);
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
		if (this.closed) {
			return;
		}

		if (this.discoveryFileMonitor != null) {
			this.discoveryFileMonitor.deleteObserver(this);
			this.discoveryFileMonitor.stop();
		}

		// move everything to the dead collection and then we unregister that guy (this
		// is what the iterators expect)
		synchronized (this.deadClients) {
			synchronized (this.activeClients) {
				for (NetworkWriter activeClient : this.activeClients) {
					if (!this.deadClients.contains(activeClient)) {
						this.deadClients.add(activeClient);
					}
				}
				this.activeClients.clear();
				this.buffer.clear();
			}

			synchronized (this.pendingClients) {
				for (NetworkWriter pendingClient : this.pendingClients) {
					if (!this.deadClients.contains(pendingClient)) {
						this.deadClients.add(pendingClient);
					}
				}
				this.pendingClients.clear();
			}
		}

		// now we can kill them all
		dropDeadConnections();

		// Now ditch our local proxies. We've already stopped the file monitor so new
		// ones shouldn't be showing up.
		// Despite that we don't like holding locks if we don't have to.
		ArrayList<LiveSessionPublisher> registeredProxies = null;
		synchronized (this.localProxyConnections) {
			if (!this.localProxyConnections.isEmpty()) {
				registeredProxies = new ArrayList<LiveSessionPublisher>(this.localProxyConnections.values());
				this.localProxyConnections.clear();
			}
		}

		if (registeredProxies != null) {
			for (LiveSessionPublisher localProxyConnection : registeredProxies) {
				localProxyConnection.close();
			}
		}

		if (this.client != null) {
			this.client.close();
			this.client = null;
		}

		if (this.hubConnection != null) {
			IOUtils.closeQuietly(this.hubConnection);
			this.hubConnection = null;
		}

		this.closed = true;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.messaging.MessengerBase#onCommand(com.onloupe.core.messaging.MessagingCommand, java.lang.Object, boolean, com.onloupe.core.messaging.MessengerBase.MaintenanceModeRequest)
	 */
	protected MaintenanceModeRequest onCommand(MessagingCommand command, Object state, boolean writeThrough, MaintenanceModeRequest maintenanceRequested) throws IOException {
		switch (command) {
		case OPEN_REMOTE_VIEWER:
			// open an outbound pending connection.
			NetworkConnectionOptions options = (NetworkConnectionOptions) state;
			startLiveView(options, null, UUID.randomUUID()); // this is a new channel we're opening.
			break;
		case SHUTDOWN:
			// close all of our outbound connections.
			onClose();
			break;
		default:
			break;
		}

		return maintenanceRequested;
	}

	/**
	 * Inheritors must override this method to implement their custom message
	 * writing functionality.
	 * 
	 * Code in this method is protected by a Queue Lock This method is called with
	 * the Message Dispatch thread exclusively.
	 *
	 * @param packet the packet
	 * @param writeThrough the write through
	 * @param maintenanceRequested the maintenance requested
	 * @return the maintenance mode request
	 */
	protected MaintenanceModeRequest onWrite(IMessengerPacket packet, boolean writeThrough, MaintenanceModeRequest maintenanceRequested) {
		if (this.closed) // we act like we're closed as soon as we receive exit mode, so we will still
		// get writes after that.
		{
			return maintenanceRequested;
		}

		if (NetworkWriter.canWritePacket(packet)) {
			synchronized (this.activeClients) // between caching and writing to the active clients we need to be
												// consistent.
			{
				// queue it for later clients
				cachePacket(packet);

				// send the packet to all our clients
				for (NetworkWriter activeClient : this.activeClients) {
					try {
						// if we run into a failed active client it's because it hasn't yet been pruned
						// from the active list,
						// so we need to go into maintenance
						if ((activeClient.connectionFailed()) || (activeClient.isClosed())) {
							maintenanceRequested = MaintenanceModeRequest.REGULAR;
						} else {
							activeClient.write(packet);
						}
					} catch (Exception ex) {

					}
				}
			}
		}

		return maintenanceRequested;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.messaging.MessengerBase#onFlush()
	 */
	@Override
	protected void onFlush() throws IOException {
		attemptRemoteConnectionAsync();

		sendSummary();
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.messaging.MessengerBase#onMaintenance()
	 */
	@Override
	protected void onMaintenance() throws Exception {
		dropDeadConnections();

		attemptRemoteConnectionAsync();
	}

	/**
	 * Cache packet.
	 *
	 * @param packet the packet
	 */
	private void cachePacket(IMessengerPacket packet) {
		// Make sure this is actually a message, not null.
		if (packet == null) {
			// Log.//debugBreak(); // This shouldn't happen, and we'd like to know if it is,
			// so stop here if
			// debugging.

			return; // Otherwise, just return; we don't want to throw exceptions.
		}

		synchronized (this.activeClients) // we are kept in sync with active client activity.
		{
			if (this._BufferSize > 0) {
				this.buffer.offer(packet);
			}

			while (this.buffer.size() > this._BufferSize) {
				this.buffer.poll(); // discard older excess.
			}
		}
	}

	/**
	 * Asynchronously verify that we are connected to a remote proxy if we should
	 * be.
	 */
	private void attemptRemoteConnectionAsync() {
		if (!this.enableOutbound) {
			return;
		}

		// If we already have a thread doing the remote connection attempt, don't start
		// another.
		if (this.activeRemoteConnectionAttempt) {
			return;
		}

		this.activeRemoteConnectionAttempt = true;
		
		Multiplexer.run(new Runnable() {
			@Override
			public void run() {
				asyncEnsureRemoteConnection();
			}
		});
	}

	/**
	 * Make sure we have an outbound proxy connection.
	 * 
	 * Intended for asynchronous execution from the thread pool.
	 * 
	 */
	private void asyncEnsureRemoteConnection() {
		// The outer try/finally is to guarantee in all possible cases we clear the
		// "we're running" flag.
		try {
			try {
				OffsetDateTime hubConfigurationExpiration;
				synchronized (this.hubConnection) {
					hubConfigurationExpiration = this.hubConfigurationExpiration;
				}

				NetworkConnectionOptions newLiveStreamOptions = null;
				if (!this.hubConnection.isConnected() || hubConfigurationExpiration.isBefore(OffsetDateTime.now())) {
					this.hubConnection.reconnect();
					OffsetDateTime connectionAttemptTime = OffsetDateTime.now();

					HubConnectionStatus status = this.hubConnection.getStatus();

					if (status.getStatus() == HubStatus.EXPIRED) {
						// if it's expired we're not going to check again for a while.
						if (status.getRepository() == null ? null : status.getRepository().getExpirationDt() == null) {
							// should never happen, but treat as our long term case.
							hubConfigurationExpiration = connectionAttemptTime.plusDays(1);
						} else {
							Duration expiredTimeframe = Duration.between(status.getRepository().getExpirationDt().get(),
									connectionAttemptTime);
							if (expiredTimeframe.toHours() < 24) {
								hubConfigurationExpiration = connectionAttemptTime.plusMinutes(15); // we'll check
																									// pretty fast for
																									// that first day.
							} else if (expiredTimeframe.toDays() < 4) {
								hubConfigurationExpiration = connectionAttemptTime.plusHours(6);
							} else {
								hubConfigurationExpiration = connectionAttemptTime.plusDays(1);
							}
						}

						if (!Log.getSilentMode()) {
							Log.write(LogMessageSeverity.VERBOSE, LOG_CATEGORY,
									"Loupe server status is expired so no remote live view possible.",
									"Will check the server configuration again at %s", hubConfigurationExpiration);
						}
					} else {
						// we always want to periodically recheck the configuration in case it has
						// changed anyway.
						// we want to coordinate to an exact hour point to provide some consistency to
						// worried users wondering when things will reconnect.
						hubConfigurationExpiration = connectionAttemptTime
								.plusMinutes(60 - connectionAttemptTime.getMinute()); // so we go to a flush hour.
						newLiveStreamOptions = this.hubConnection.getRepository() == null ? null
								: this.hubConnection.getRepository().getAgentLiveStreamOptions();

						if ((newLiveStreamOptions == null) && (!Log.getSilentMode())) {
							Log.write(LogMessageSeverity.INFORMATION, LOG_CATEGORY,
									"Remote live view not available due to server configuration",
									"The server is configured to have live view disabled so even though we have it enabled there is no live view.");
						}
					}

					synchronized (this.hubConnection) {
						this.hubConfigurationExpiration = hubConfigurationExpiration;
					}

					// if we got different options then we're going to have to drop & recreate the
					// client.
					if (newLiveStreamOptions != null && !newLiveStreamOptions.equals(this.connectionOptions)) {
						if (!Log.getSilentMode()) {
							Log.write(LogMessageSeverity.VERBOSE, LOG_CATEGORY,
									"Loupe server live view options are different than our running configuration so we will close the client.",
									"New configuration:\r\n%s", newLiveStreamOptions);
						}
						this.connectionOptions = newLiveStreamOptions;
						closeClient(this.client);
						this.client = null;
					}
				}
			} catch (Exception ex) {
				if (!Log.getSilentMode()) {
					Log.write(LogMessageSeverity.WARNING, LogWriteMode.QUEUED, ex, LOG_CATEGORY,
							"Remote viewer connection attempt failed",
							"While attempting to open our outbound connection to the proxy server an exception was thrown.  We will retry again later.\r\nException: %s",
							ex.getMessage());
				}
				
				if (SystemUtils.isInDebugMode()) {
					ex.printStackTrace();
				}
			}

			try {
				if ((this.client == null) && (this.connectionOptions != null)) {
					LiveSessionPublisher newClient = new LiveSessionPublisher(this, this.connectionOptions);
					newClient.start();
					this.client = newClient;
					sendSummary(this.client); // since we just connected, we want to immediately tell it about us.
				}
			} catch (Exception ex) {
				if (!Log.getSilentMode()) {
					Log.write(LogMessageSeverity.WARNING, LogWriteMode.QUEUED, ex, LOG_CATEGORY,
							"Remote viewer connection attempt failed",
							"While attempting to open our outbound connection to the proxy server an exception was thrown.  We will retry again later.\r\nException: %s",
							ex.getMessage());
				}
				
				if (SystemUtils.isInDebugMode()) {
					ex.printStackTrace();
				}
			}

		} finally {

			this.activeRemoteConnectionAttempt = false;
		}
	}

	/**
	 * Closes all outbound connections related to the current live agent client.
	 *
	 * @param deadClient the dead client
	 */
	private void closeClient(LiveSessionPublisher deadClient) {
		if (deadClient == null) {
			return;
		}

		// we are going to release every connection related to this client. They might
		// not all be - some might be local.
		NetworkConnectionOptions deadClientOptions = deadClient.getOptions();
		if (deadClientOptions != null) {
			synchronized (this.deadClients) {
				// move everything to the dead collection and then we unregister that guy (this
				// is what the iterators expect)
				synchronized (this.activeClients) {
					for (NetworkWriter activeClient : this.activeClients) {
						if ((activeClient.getOptions().equals(deadClientOptions))
								&& !this.deadClients.contains(activeClient)) {
							this.deadClients.add(activeClient);
						}
					}
				}

				synchronized (this.pendingClients) {
					for (NetworkWriter pendingClient : this.pendingClients) {
						if ((pendingClient.getOptions().equals(deadClientOptions))
								&& !this.deadClients.contains(pendingClient)) {
							this.deadClients.add(pendingClient);
						}
					}
					this.pendingClients.clear();
				}
			}

			// now we can kill them all
			dropDeadConnections();
		}

		deadClient.close();
	}

	/**
	 * Dispose any connections that we discovered are no longer valid.
	 * 
	 */
	private void dropDeadConnections() {
		NetworkWriter[] deadClients = null;
		synchronized (this.deadClients) {
			if (!this.deadClients.isEmpty()) {
				deadClients = new NetworkWriter[this.deadClients.size()];
				this.deadClients.toArray(deadClients);
			}

			this.deadClients.clear();
		}

		// now we start clearing them - outside of the lock since they may check that
		// themselves.
		if (deadClients != null) {
			for (NetworkWriter networkWriter : deadClients) {
				unregisterWriter(networkWriter);
			}
		}
	}

	/**
	 * Register a new writer for all events.
	 *
	 * @param newWriter the new writer
	 */
	private void registerWriter(NetworkWriter newWriter) {
		newWriter.addObserver(this);
		synchronized (this.pendingClients) {
			this.pendingClients.add(newWriter);
		}
	}

	/**
	 * Unregister the writer from all events and dispose it.
	 *
	 * @param writer the writer
	 */
	private void unregisterWriter(NetworkWriter writer) {
		writer.deleteObserver(this);

		synchronized (this.pendingClients) {
			this.pendingClients.remove(writer);
		}

		synchronized (this.activeClients) {
			this.activeClients.remove(writer);
		}

		// we are deliberately NOT removing it from the dead connection since that's
		// what we're iterating outside of here...

		writer.close();
	}

	/**
	 * Discovery file monitor on file changed.
	 *
	 * @param e the e
	 */
	private void discoveryFileMonitorOnFileChanged(LocalServerDiscoveryFileEventArgs e) {
		LiveSessionPublisher target;

		// this event *should* mean that we have a new proxy to connect to...
		synchronized (this.localProxyConnections) {
			target = this.localProxyConnections.get(e.getFile().getFileNamePath());
			if (target == null) {
				if (e.getFile().isAlive()) {
					target = new LiveSessionPublisher(this, e.getFile());
					target.start();
					this.localProxyConnections.put(e.getFile().getFileNamePath(), target);
				}
			}
		}
	}

	/**
	 * Discovery file monitor on file deleted.
	 *
	 * @param e the e
	 */
	private void discoveryFileMonitorOnFileDeleted(LocalServerDiscoveryFileEventArgs e) {
		LiveSessionPublisher victim;

		// this event *should* mean that we have to dump a proxy we were connected to...
		synchronized (this.localProxyConnections) {
			victim = this.localProxyConnections.get(e.getFile().getFileNamePath());
			if (victim != null) {
				this.localProxyConnections.remove(e.getFile().getFileNamePath());
			}
		}

		closeClient(victim);
	}

	/**
	 * Network writer closed.
	 *
	 * @param writer the writer
	 */
	private void networkWriterClosed(NetworkWriter writer) {
		if (!Log.getSilentMode()) {
			Log.write(LogMessageSeverity.VERBOSE, LOG_CATEGORY, "Remote network viewer connection closed",
					"Remote Endpoint: %s", writer);
		}

		// we can't afford to change the active clients collection since that's too
		// pivotal to performance.
		synchronized (this.deadClients) {
			if (!this.deadClients.contains(writer)) {
				this.deadClients.add(writer);
			}
		}
	}

	/**
	 * Network writer failed.
	 *
	 * @param writer the writer
	 */
	private void networkWriterFailed(NetworkWriter writer) {
		if (!Log.getSilentMode()) {
			Log.write(LogMessageSeverity.INFORMATION, LOG_CATEGORY, "Remote network viewer connection failed",
					"We will add it to our dead writers collection and it will get permanently removed in the next maintenance cycle.\r\nRemote Endpoint: %s",
					writer);
		}

		synchronized (this.deadClients) {
			if (!this.deadClients.contains(writer)) {
				this.deadClients.add(writer);
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof LocalServerDiscoveryFileMonitor) {
			LocalServerDiscoveryFileEventArgs args = (LocalServerDiscoveryFileEventArgs) arg;
			if (args.getKind() == StandardWatchEventKinds.ENTRY_CREATE
					|| args.getKind() == StandardWatchEventKinds.ENTRY_MODIFY) {
				discoveryFileMonitorOnFileChanged(args);
			} else if (args.getKind() == StandardWatchEventKinds.ENTRY_DELETE) {
					discoveryFileMonitorOnFileDeleted(args);

			}
		} else if (o instanceof NetworkWriter) {
			NetworkWriter writer = (NetworkWriter) o;
			writer.deleteObserver(this);

			if (writer.isClosed()) {
				networkWriterClosed(writer);
			} else if (writer.connectionFailed()) {
				networkWriterFailed(writer);
			}
		}
	}
}