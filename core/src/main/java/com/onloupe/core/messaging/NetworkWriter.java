package com.onloupe.core.messaging;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

import com.onloupe.core.data.FileHeader;
import com.onloupe.core.logging.Log;
import com.onloupe.core.messaging.network.LiveViewStartCommandMessage;
import com.onloupe.core.messaging.network.LiveViewStopCommandMessage;
import com.onloupe.core.messaging.network.NetworkClient;
import com.onloupe.core.messaging.network.NetworkMessage;
import com.onloupe.core.messaging.network.PacketStreamStartCommandMessage;
import com.onloupe.core.messaging.network.SendSessionCommandMessage;
import com.onloupe.core.serialization.monitor.ApplicationUserPacket;
import com.onloupe.core.serialization.monitor.AssemblyInfoPacket;
import com.onloupe.core.serialization.monitor.ExceptionInfoPacket;
import com.onloupe.core.serialization.monitor.LogMessagePacket;
import com.onloupe.core.serialization.monitor.SessionSummaryPacket;
import com.onloupe.core.serialization.monitor.ThreadInfoPacket;
import com.onloupe.core.server.NetworkConnectionOptions;
import com.onloupe.model.log.LogMessageSeverity;

/**
 * Used by the agent to write session data to a network socket
 */
public class NetworkWriter extends NetworkClient {
	private final Object lock = new Object();
	private NetworkMessenger messenger;
	private long sequenceOffset;
	private UUID channelId;
	private UUID repositoryId;

	/**
	 * Create a new network writer for a remote server
	 */

	public NetworkWriter(NetworkMessenger messenger, NetworkConnectionOptions options, java.util.UUID repositoryId,
			java.util.UUID channelId) {
		this(messenger, options, repositoryId, channelId, 0);
	}

	public NetworkWriter(NetworkMessenger messenger, NetworkConnectionOptions options, UUID repositoryId,
			UUID channelId, long sequenceOffset) {
		this(messenger, options, repositoryId, channelId, sequenceOffset, FileHeader.defaultMajorVersion,
				FileHeader.defaultMinorVersion);
	}

	/**
	 * Create a new network writer for a remote server
	 */
	public NetworkWriter(NetworkMessenger messenger, NetworkConnectionOptions options, UUID repositoryId,
			UUID channelId, long sequenceOffset, int majorVersion, int minorVersion) {
		super(options, false, majorVersion, minorVersion);
		if (channelId == null) {
			throw new NullPointerException("channelId");
		}

		synchronized (this.lock) // since we promptly access these variables from another thread, I'm adding this
									// as paranoia to ensure they get synchronized.
		{
			this.messenger = messenger;
			this.repositoryId = repositoryId;
			this.channelId = channelId;
			this.sequenceOffset = sequenceOffset;
		}
	}

	/**
	 * Create a new network writer for a connected socket
	 */

	public NetworkWriter(NetworkMessenger messenger, Socket socket, java.util.UUID repositoryId,
			java.util.UUID channelId) {
		this(messenger, socket, repositoryId, channelId, 0);
	}

	public NetworkWriter(NetworkMessenger messenger, Socket socket, UUID repositoryId, UUID channelId,
			long sequenceOffset) {
		this(messenger, socket, repositoryId, channelId, sequenceOffset, FileHeader.defaultMajorVersion,
				FileHeader.defaultMinorVersion);
	}

	/**
	 * Create a new network writer for a connected socket
	 */
	public NetworkWriter(NetworkMessenger messenger, Socket socket, UUID repositoryId, UUID channelId,
			long sequenceOffset, int majorVersion, int minorVersion) {
		super(socket, majorVersion, minorVersion);
		if (channelId == null) {
			throw new NullPointerException("channelId");
		}

		synchronized (this.lock) // since we promptly access these variables from another thread, I'm adding this
									// as paranoia to ensure they get synchronized.
		{
			this.messenger = messenger;
			this.repositoryId = repositoryId;
			this.channelId = channelId;
			this.sequenceOffset = sequenceOffset;
		}
	}

	/**
	 * Write the provided packet to the client stream (synchronously)
	 * 
	 * @param packets Throws exceptions if there is a connection failure.
	 * @throws NoSuchMethodException
	 */
	public final void write(IMessengerPacket[] packets) throws NoSuchMethodException {
		synchronized (this.lock) {
			if ((connectionFailed()) || (isClosed())) {
				return;
			}

			for (IMessengerPacket packet : packets) {
				write(packet);
			}
		}
	}

	/**
	 * Write the provided packet to the client stream (synchronously)
	 * 
	 * @param packet
	 * @throws NoSuchMethodException
	 */
	public final void write(IMessengerPacket packet) throws NoSuchMethodException {
		synchronized (this.lock) {
			if ((connectionFailed()) || (isClosed())) {
				return;
			}

			// we don't send across all types - just a few we understand.
			if (canWritePacket(packet)) {
				sendPacket(packet);
			}
		}
	}

	/**
	 * Indicates if we can write the specified packet.
	 * 
	 * @param packet
	 * @return
	 */
	public static boolean canWritePacket(IMessengerPacket packet) {
		// we don't send across all types - just a few we understand.
		return ((packet instanceof LogMessagePacket) || (packet instanceof ApplicationUserPacket)
				|| (packet instanceof ExceptionInfoPacket) || (packet instanceof ThreadInfoPacket)
				|| (packet instanceof SessionSummaryPacket) || (packet instanceof AssemblyInfoPacket));
	}

	/**
	 * Implemented to complete the protocol connection
	 * 
	 * @return True if a connection was successfully established, false otherwise.
	 * @throws NoSuchMethodException
	 * @throws IOException
	 */
	@Override
	protected boolean connect() throws NoSuchMethodException, IOException {
		// because we're used inside of the writing side of a messenger we have to be
		// sure our thread doesn't block.
		Publisher.threadMustNotBlock();

		boolean connnected = false;

		// tell the other end who we are to start the conversation
		sendMessage(
				new LiveViewStartCommandMessage(this.repositoryId, Log.getSessionSummary().getId(), this.channelId));

		// and then wait to hear that we should start our packet stream.
		NetworkMessage nextPacket;
		do {
			nextPacket = readNetworkMessage();
			if (nextPacket != null) {
				if (nextPacket instanceof LiveViewStopCommandMessage) {
					// we are going to shut down the connection
					remoteClose();
				} else if (nextPacket instanceof PacketStreamStartCommandMessage) {
					if (!Log.getSilentMode()) {
						Log.write(LogMessageSeverity.INFORMATION, LOG_CATEGORY,
								"Packet Stream start command received from server",
								"Received the start command, now we will switch over to the gibraltar session stream data.\r\n%s",
								this);
					}

					// initialization is tetchy - we have to get the header, the cache, and be added
					// to the list in one blow
					// to be sure we get all of the packets we should.
					this.messenger.activateWriter(this, this.sequenceOffset);
					connnected = true;
				}
			}
		} while ((nextPacket != null) && !connnected);

		return connnected;
	}

	/**
	 * Implemented to transfer data on an established connection
	 * 
	 * @throws IOException
	 */
	@Override
	protected void transferData() throws IOException {
		NetworkMessage nextPacket;
		do {
			nextPacket = readNetworkMessage();

			if (nextPacket != null) {
				if (nextPacket instanceof LiveViewStopCommandMessage) {
					// time to end...
					remoteClose();
				} else if (nextPacket instanceof SendSessionCommandMessage) {
					// send to server baby!
					this.messenger.sendToServer((SendSessionCommandMessage) nextPacket);
				}
			}
		} while (nextPacket != null);
	}

	// TODO KM:  IT appears the .NET implementation this should be retriable using the default backoff.

	// TODO RKELLIHER Define...
	@Override
	protected boolean canRetry() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected Integer retryDelay() {
		// TODO Auto-generated method stub
		return null;
	}
}