package com.onloupe.core.server;

import java.io.IOException;
import java.net.InetAddress;

import com.onloupe.core.data.FileHeader;
import com.onloupe.core.data.SessionHeader;
import com.onloupe.core.logging.Log;
import com.onloupe.core.messaging.LocalServerDiscoveryFile;
import com.onloupe.core.messaging.NetworkMessenger;
import com.onloupe.core.messaging.network.LiveViewStartCommandMessage;
import com.onloupe.core.messaging.network.NetworkClient;
import com.onloupe.core.messaging.network.NetworkMessage;
import com.onloupe.core.messaging.network.RegisterAgentCommandMessage;
import com.onloupe.core.messaging.network.SendSessionCommandMessage;
import com.onloupe.core.messaging.network.SessionHeaderMessage;
import com.onloupe.model.log.LogMessageSeverity;

// TODO: Auto-generated Javadoc
/**
 * Communicates between an Agent and a Loupe Server.
 */
public class LiveSessionPublisher extends NetworkClient {
	
	/** The messenger. */
	private NetworkMessenger messenger;
	
	/** The discovery file. */
	private LocalServerDiscoveryFile discoveryFile;

	/** The lock. */
	private final Object lock = new Object();

	/**
	 * Create a new connection with the specified options.
	 *
	 * @param messenger the messenger
	 * @param options the options
	 */
	public LiveSessionPublisher(NetworkMessenger messenger, NetworkConnectionOptions options) {
		this(messenger, options, FileHeader.defaultMajorVersion, FileHeader.defaultMinorVersion);
	}

	/**
	 * Create a new connection with the specified options.
	 *
	 * @param messenger the messenger
	 * @param options the options
	 * @param majorVersion the major version
	 * @param minorVersion the minor version
	 */
	public LiveSessionPublisher(NetworkMessenger messenger, NetworkConnectionOptions options, int majorVersion,
			int minorVersion) {
		super(options, true, majorVersion, minorVersion);
		if (!Log.getSilentMode()) {
			Log.write(LogMessageSeverity.VERBOSE, LOG_CATEGORY, "New live sessions publisher being created",
					"Configuration:\r\n%s", options);
		}

		synchronized (this.lock) // since we promptly access these variables from another thread, I'm adding this
									// as paranoia to ensure they get synchronized.
		{
			this.messenger = messenger;
		}
	}

	/**
	 * Instantiates a new live session publisher.
	 *
	 * @param messenger the messenger
	 * @param discoveryFile the discovery file
	 */
	public LiveSessionPublisher(NetworkMessenger messenger, LocalServerDiscoveryFile discoveryFile) {
		this(messenger, new NetworkConnectionOptions(discoveryFile.getPublisherPort(),
				InetAddress.getLoopbackAddress().getHostAddress(), false));
		this.discoveryFile = discoveryFile;
	}

	/**
	 * Send a copy of the latest session summary information to the server.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final void sendSummary() throws IOException {
		SessionHeaderMessage headerPacket = new SessionHeaderMessage(new SessionHeader(Log.getSessionSummary()));
		sendMessage(headerPacket);
	}

	/**
	 * Implemented to complete the protocol connection.
	 *
	 * @return True if a connection was successfully established, false otherwise.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	protected boolean connect() throws IOException {
		// identify ourselves as a control channel
		RegisterAgentCommandMessage startCommandPacket = new RegisterAgentCommandMessage(
				Log.getSessionSummary().getId());
		sendMessage(startCommandPacket);

		sendSummary();

		return true;
	}

	/**
	 * Implemented to transfer data on an established connection.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	protected void transferData() throws IOException {
		// we only ever use the network serializer.
		NetworkMessage nextPacket = null;
		do {
			nextPacket = readNetworkMessage();

			if (nextPacket != null) {
				LiveViewStartCommandMessage viewStartCommand = nextPacket instanceof LiveViewStartCommandMessage
						? (LiveViewStartCommandMessage) nextPacket
						: null;
				SendSessionCommandMessage sendSessionCommand = nextPacket instanceof SendSessionCommandMessage
						? (SendSessionCommandMessage) nextPacket
						: null;

				if (viewStartCommand != null) {
					viewStartCommand.validate();
					// we need to initiate an outbound viewer to the same destination we point to.
					this.messenger.startLiveView(getOptions(), viewStartCommand.getRepositoryId(),
							viewStartCommand.getChannelId(), viewStartCommand.getSequenceOffset());
				} else if (sendSessionCommand != null) {
					// send to server baby!
					this.messenger.sendToServer((SendSessionCommandMessage) nextPacket);
				}
			}

		} while (nextPacket != null); // it will go to null when the connection closes
	}

	/**
	 * Allows a derived class to implement its own retry delay strategy.
	 *
	 * @return true if any retry should be attempted
	 */
	@Override
	protected boolean canRetry() {
		// make sure we're still alive..
		if (this.discoveryFile != null) {
			return this.discoveryFile.isAlive();
		}

		return true;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.messaging.network.NetworkClient#retryDelay()
	 */
	@Override
	protected Integer retryDelay() {
		// TODO Auto-generated method stub
		return null;
	}
}