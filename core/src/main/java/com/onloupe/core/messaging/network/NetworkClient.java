package com.onloupe.core.messaging.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Observable;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import com.onloupe.core.logging.Log;
import com.onloupe.core.logging.LogWriteMode;
import com.onloupe.core.serialization.IPacket;
import com.onloupe.core.serialization.PacketReader;
import com.onloupe.core.serialization.PacketWriter;
import com.onloupe.core.server.GibraltarNetworkException;
import com.onloupe.core.server.NetworkConnectionOptions;
import com.onloupe.core.util.OutObject;
import com.onloupe.core.util.SystemUtils;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.log.LogMessageSeverity;

// TODO: Auto-generated Javadoc
/**
 * The base class for different network clients that talk to a server.
 */
public abstract class NetworkClient extends Observable implements Closeable {
	
	/** The log category used for network client operations. */
	public static final String LOG_CATEGORY = "Loupe.Network.Client";
	
	/** The Constant SECURITY_PROTOCOLS. */
	private static final String[] SECURITY_PROTOCOLS = new String[] {"SSLv2Hello", "TLSv1", "TLSv1.1", "TLSv1.2"};

	/** The Constant NETWORK_READ_BUFFER_LENGTH. */
	private static final int NETWORK_READ_BUFFER_LENGTH = 10240;

	/** The options. */
	private NetworkConnectionOptions options;
	
	/** The retry connections. */
	private boolean retryConnections;
	
	/** The major version. */
	private int majorVersion;
	
	/** The minor version. */
	private int minorVersion;
	
	/** The lock. */
	private final Object lock = new Object();

	/** The single socket. */
	private boolean singleSocket;
	
	/** The connected. */
	private boolean connected = false; // PROTECTED BY LOCK
	
	/** The connection failed. */
	private boolean connectionFailed = false; // PROTECTED BY LOCK
	
	/** The closed. */
	private volatile boolean closed; // PROTECTED BY LOCK //Volatile so we can peek at it.
	
	/** The has corrupt data. */
	private boolean hasCorruptData;
	
	/** The packets lost count. */
	private int packetsLostCount;
	
	/** The status string. */
	private String statusString;

	// these serializers are conversation-specific and have to be replaced every
	/** The network serializer. */
	// time we connect.
	private NetworkSerializer networkSerializer;
	
	/** The network input stream. */
	private BufferedInputStream networkInputStream;
	
	/** The network output stream. */
	private BufferedOutputStream networkOutputStream;

	/** The background reader. */
	private Thread backgroundReader;

	/** The packet reader. */
	private PacketReader packetReader;
	
	/** The packet writer. */
	private PacketWriter packetWriter;
	
	/** The tcp client. */
	private Socket tcpClient; // only used when we were given a specific TCP client, not when we connect
								// ourselves.
	
	/** The ssl context. */
								private SSLContext sslContext;
	
	/** The ssl socket factory. */
	private SSLSocketFactory sslSocketFactory;

	/**
	 * Create a new network client to the specified endpoint.
	 *
	 * @param options the options
	 * @param retryConnections If true then network connections will automatically
	 *                         be retried (instead of the client being considered
	 *                         failed)
	 * @param majorVersion     Major version of the serialization protocol
	 * @param minorVersion     Minor version of the serialization protocol
	 */
	protected NetworkClient(NetworkConnectionOptions options, boolean retryConnections, int majorVersion,
			int minorVersion) {
		if (options == null) {
			throw new NullPointerException("options");
		}

		synchronized (this.lock) // since we promptly access these variables from another thread, I'm adding this
									// as paranoia to ensure they get synchronized.
		{
			this.options = options;
			this.retryConnections = retryConnections;
			this.majorVersion = majorVersion;
			this.minorVersion = minorVersion;
			
			if (options.getUseSsl()) {
					try {
						sslContext = SSLContext.getInstance("TLS");
						sslContext.init(null, null, null);
						sslSocketFactory = sslContext.getSocketFactory();
					} catch (Exception e) {
						if (SystemUtils.isInDebugMode()) {
							e.printStackTrace();
						}
					} 
				}			
			}

			calculateStateMessage(null); // initializes to default
	}

	/**
	 * Create a new network client using the existing socket.
	 * 
	 * @param socket    Already connected TCP Socket
	 * @param majorVersion Major version of the serialization protocol
	 * @param minorVersion Minor version of the serialization protocol
	 */
	protected NetworkClient(Socket socket, int majorVersion, int minorVersion) {
		synchronized (this.lock) // since we promptly access these variables from another thread, I'm adding this
									// as paranoia to ensure they get synchronized.
		{
			this.singleSocket = true;
			this.tcpClient = socket;
			this.retryConnections = false; // when we're given the client, we can't retry connections.
			this.majorVersion = majorVersion;
			this.minorVersion = minorVersion;
		}

		calculateStateMessage(null); // initializes to default
	}

	/**
	 * Start the network client.
	 */
	public final void start() {
		initialize();
	}

	/**
	 * Stop reading from the network and prepare to exit.
	 */
	@Override
	public final void close() {
		try {
			actionOnClosed();
		} catch (Exception ex) {
			try {
				Log.recordException(0, ex, null, LOG_CATEGORY, true);
			} catch (IOException e) {
				// do nothing
			}
		}
		
		disposeMembers();
	}

	/**
	 * Indicates if the remote viewer is currently connected.
	 *
	 * @return true, if is connected
	 */
	public final boolean isConnected() {
		return this.connected;
	}

	/**
	 * Indicates if the writer experienced a network failure.
	 *
	 * @return true, if successful
	 */
	public final boolean connectionFailed() {
		return this.connectionFailed;
	}

	/**
	 * Indicates if the writer was explicitly closed.
	 *
	 * @return true, if is closed
	 */
	public final boolean isClosed() {
		return this.closed;
	}

	/**
	 * Indicates whether a session had errors during rehydration and has lost some
	 * packets.
	 *
	 * @return the checks for corrupt data
	 */
	public final boolean getHasCorruptData() {
		return this.hasCorruptData;
	}

	/**
	 * Indicates how many packets were lost due to errors in rehydration.
	 *
	 * @return the packets lost count
	 */
	public final int getPacketsLostCount() {
		return this.packetsLostCount;
	}

	/**
	 * Get a copy of the network connection options used by this client.
	 *
	 * @return the network connection options
	 */
	public final NetworkConnectionOptions cloneOptions() {
		return this.options.clone();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.statusString;
	}

	/**
	 * Can retry.
	 *
	 * @return true, if successful
	 */
	protected abstract boolean canRetry();

	/**
	 * Retry delay.
	 *
	 * @return the integer
	 */
	protected abstract Integer retryDelay();

	/**
	 * Implemented to complete the protocol connection.
	 *
	 * @return True if a connection was successfully established, false otherwise.
	 * @throws NoSuchMethodException the no such method exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected abstract boolean connect() throws NoSuchMethodException, IOException;

	/**
	 * Implemented to transfer data on an established connection.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected abstract void transferData() throws IOException;

	/**
	 * Called when a valid connection is being administratively closed.
	 */
	protected void onClose() {
	}

	/**
	 * Allows derived classes to register all of the packet factories they need when
	 * creating a new packet reader.
	 *
	 * @param packetReader the packet reader
	 */
	protected void onPacketFactoryRegister(PacketReader packetReader) {
	}

	/**
	 * The network connection options used to connect to the server.
	 *
	 * @return the options
	 */
	public final NetworkConnectionOptions getOptions() {
		return this.options;
	}

	/**
	 * Transmit the provided network message to the server.
	 *
	 * @param message the message
	 */
	protected final void sendMessage(NetworkMessage message) {
		synchronized (this.lock) {
			if (this.networkOutputStream != null) {
				try {
					message.write(this.networkOutputStream);
				} catch (Exception ex) {
					throw new GibraltarNetworkException(
							"Unable to send message, usually because the network connection was lost.", ex);
				}
			}
		}
	}

	/**
	 * Transmit the provided serialized packet to the server.
	 *
	 * @param packet the packet
	 */
	protected final void sendPacket(IPacket packet) {
		synchronized (this.lock) {
			Exception sendException = null;
			if (this.networkOutputStream != null) {
				try (Socket socket = getTcpClient()) {
					// make sure we have a packet serializer, if not hopefully this is the first
					// time :)
					if (this.packetWriter == null) {
						this.packetWriter = new PacketWriter(this.networkOutputStream, this.majorVersion,
								this.minorVersion);
					}

					// since the packet writer goes directly to the network stream we have to do our
					// full network exception handling.
					this.packetWriter.write(packet);
				} catch (SocketException ex) {
					// most likely the socket is no good any more.
					actionSocketFailed(ex); // throws an exception
				} catch (IOException ex) {
					// the doc indicates you'll often get an IO exception that WRAPS a socket
					// exception.
					if ((ex.getCause() != null) && (ex.getCause() instanceof SocketException)) {
						// most likely the socket is no good any more.
						actionSocketFailed(ex); // throws an exception
					} else {
						sendException = ex;
					}
				} catch (Exception ex) {
					throw new GibraltarNetworkException(
							"Unable to send packet, usually because the network connection was lost.", sendException);
				}
			}
		}
	}

	/**
	 * Read the next network packet from the pipe. Blocks until a packet is detected
	 * or the connection fails.
	 *
	 * @return the network message
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected final NetworkMessage readNetworkMessage() throws IOException
	{
		if (networkSerializer == null)
		{
			networkSerializer = new NetworkSerializer();
		}

		boolean socketClosed = false;
		NetworkMessage nextPacket;

		do
		{
			//see if we have another packet in the buffer...
			nextPacket = networkSerializer.readNext();

			if (nextPacket == null)
			{
				//go into a blocking wait on the socket..  we'll loop until we get the whole buffer into the stream.
				OutObject<byte[]> tempOutBuffer = new OutObject<byte[]>();
				int newDataLength = readSocket(tempOutBuffer);

				if (newDataLength < 0)
				{
					//this is the signal that the other end shut down the pipe.
					socketClosed = true;
				}
				else
				{
					networkSerializer.appendData(tempOutBuffer.argValue, newDataLength);
				}
			}
		} while ((socketClosed == false) && (nextPacket == null));

		return nextPacket;
	}
	
	/**
	 * Read socket.
	 *
	 * @param buffer the buffer
	 * @return the int
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private int readSocket(OutObject<byte[]> buffer) throws IOException
	{
		//go into a blocking wait on the socket..  we'll loop until we get the whole buffer into the stream.
		try {
			byte[] bytes = new byte[NETWORK_READ_BUFFER_LENGTH];
			int readBytes = networkInputStream.read(bytes, 0, bytes.length);
			buffer.argValue = bytes;
			return readBytes;
		} catch (Exception e) {
			if (SystemUtils.isInDebugMode()) {
				e.printStackTrace();
			}
			
			// Return close signal. Either the stream has been nullified by an exiting process,
			// or a socket exception was raised (for instance, by the client abruptly exiting).
			return -1;
		}
	}

	/**
	 * Called to shut down the client due to a command from the server.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected final void remoteClose() throws IOException {
		synchronized (this.lock) {
			if (!Log.getSilentMode()) {
				Log.write(LogMessageSeverity.INFORMATION, LOG_CATEGORY, "Live view session ending at remote request",
						"We have received an indication from the other end of the connection that it is time to end the live view session.");
			}
			close();
		}
	}

	/**
	 * Initialize.
	 */
	private void initialize() {
		synchronized (this.lock) {
			if (this.backgroundReader == null) {

				// and start our reader thread.
				this.backgroundReader = new Thread() {
					@Override
					public void run() {
						asyncNetworkStreamMain();
					}
				};
				
				this.backgroundReader.setName("Loupe Network Client Reader");
				this.backgroundReader.start();
			}
		}
	}

	/**
	 * The main method of the background thread for reading from the stream.
	 */
	private void asyncNetworkStreamMain() {
		try {
			int previousFailures = 0;
			while (!this.closed) {
				// Create a new TCP Client. If we're in single socket mode & this is the second
				// time, it'll fail.
				try (Socket socket = getTcpClient()) {
					this.tcpClient = socket;

					calculateStateMessage(this.tcpClient);

					// Here we should authenticate when we add support for that to Loupe.
					// TODO BUG: Add Authentication

					synchronized (this.lock) {
						this.networkInputStream = new BufferedInputStream(this.tcpClient.getInputStream());
						this.networkOutputStream = new BufferedOutputStream(this.tcpClient.getOutputStream());
					}

					boolean connected = connect();
					calculateStateMessage(this.tcpClient);

					if (connected) {
						// since we've successfully connected reset the previous failure count.
						previousFailures = 0;

						// let our monitors know we're connected
						actionOnConnected();

						// and now we're ready to pass control to our downstream objects.
						transferData();
					}
				} catch (GibraltarNetworkException ex) {
					// in this case we've already handled the critical state issues, we just need to
					// exit the loop and roll around again.
					if (!Log.getSilentMode()) {
						Log.write(LogMessageSeverity.VERBOSE, LogWriteMode.QUEUED, ex, LOG_CATEGORY,
								"Recovering from network failure",
								"The TCP Socket has been disposed and now we will prepare to reconnect.\r\nPrevious Failures: %d\r\nConnection Info: %s",
								previousFailures, this.options);
					}
					
					if (SystemUtils.isInDebugMode()) {
						ex.printStackTrace();
					}
				} catch (Exception ex) {
					if (!Log.getSilentMode()) {
						Log.write(LogMessageSeverity.WARNING, LogWriteMode.QUEUED, ex, LOG_CATEGORY,
								"Unexpected exception while working with network stream",
								"We will clear the current state and attempt to re-establish (if allowed)\r\n%s\r\nException: %s",
								this, ex.getMessage());
					}
					
					if (SystemUtils.isInDebugMode()) {
						ex.printStackTrace();
					}
				} finally {
					calculateStateMessage(null);

					if (!this.retryConnections) {
						// this is going to be fatal, so kill it.
						close();
					}

					disposeMembers();
				}

				// now we need to stall for a moment while connecting
				if (!this.closed) {
					int retryDelayMs = 0;
					if (canRetry() && retryDelay() != null) {
						retryDelayMs = retryDelay();
					} else {
						if (previousFailures == 0) {
							// we always want to retry immediately on the first failure.
						} else if (previousFailures < 5) {
							// first five failures just wait half a second.
							retryDelayMs = 500;
						} else if (previousFailures < 13) // 8 + the previous 5
						{
							retryDelayMs = 15000;
						} else {
							retryDelayMs = 60000;
						}
					}

					if (!canRetry()) {
						close();
					}

					else {
						previousFailures++;
						Thread.sleep(retryDelayMs);
					}
				}
			}
		} catch (RuntimeException | InterruptedException ex) {
			Log.write(LogMessageSeverity.ERROR, LogWriteMode.QUEUED, new GibraltarNetworkException(ex), LOG_CATEGORY,
					"Background network reader thread failed",
					"The thread is exiting due to an exception and the client will effectively be dead.\r\nException: %s",
					ex.getMessage());
		} finally {
			calculateStateMessage(null);
			// in this position we're exiting the thread, so we need to release our pointer
			// to it too.
			this.backgroundReader = null;
		}
	}

	/**
	 * Release and dispose all of the connection-specific resources.
	 */
	private void disposeMembers() {
		synchronized (this.lock) {
			safeDispose(this.packetWriter);
			this.packetWriter = null;

			this.networkInputStream = null;
			this.networkOutputStream = null;

			safeDispose(this.networkSerializer);
			this.networkSerializer = null;

			safeDispose(this.packetReader);
			this.packetReader = null;
		}
	}

	/**
	 * Safe dispose.
	 *
	 * @param disposableObject the disposable object
	 */
	private static void safeDispose(Closeable disposableObject) {
		if (disposableObject != null) {
			try {
				disposableObject.close();
			} catch (RuntimeException | IOException ex) // this is what makes it safe...
			{
				try {
					Log.recordException(0, new GibraltarNetworkException(ex), null, LOG_CATEGORY, true);
				} catch (IOException e) {
					// do nothing
				}
			}
		}
	}

	/**
	 * Handles terminal socket failures.
	 *
	 * @param ex the ex
	 */
	private void actionSocketFailed(Exception ex) {
		synchronized (this.lock) {
			if (this.connected) {
				// since we were connected we're transitioning to failed.
				if (!Log.getSilentMode()) {
					Log.write(LogMessageSeverity.WARNING, LogWriteMode.QUEUED, new GibraltarNetworkException(ex),
							LOG_CATEGORY, "Network Socket Failed",
							"We received an exception from the socket when performing an operation and will assume it has failed.\r\n%s\r\nException: %s",
							this.statusString, ex.getMessage());
				}
			}
		}

		// we always throw the exception to force the call stack to unwind.
		throw new GibraltarNetworkException("The network socket has failed", ex);
	}

	/**
	 * sets our status to connected and fires the appropriate event.
	 * 
	 * This is separate from the overrideable OnConnected to ensure our critical
	 * state management gets done even if inheritor messes up.
	 */
	private void actionOnConnected() {
		synchronized (this.lock) {
			if (!this.connected) {
				this.connected = true;
			}
		}
	}

	/**
	 * sets our status to disconnected and fires the appropriate event.
	 * 
	 * This is separate from the overrideable OnDisconnected to ensure our critical
	 * state management gets done even if inheritor messes up.
	 *
	 * @throws Exception the exception
	 */
	private void actionOnClosed() throws Exception {
		boolean performClose = false;

		try {
			OutputStream writerStream = null;
			InputStream readerStream = null;

			// we can get into a deadlock here: we have to peek if another thread is already
			// closing
			if (!this.closed) {
				synchronized (this.lock) {
					if (!this.closed) {
						performClose = true;
						this.closed = true; // so any other thread will know we're closed.
						writerStream = this.networkOutputStream;
						readerStream = this.networkInputStream;
					}
				}
			}

			if (!performClose) {
				return; // we are already closed, forget about it!
			}

			if (writerStream != null || readerStream != null) {
				onClose();

				this.retryConnections = false; // even if it was true, we don't want to do it now..

				if ((this.tcpClient != null) && (!this.tcpClient.isClosed())) {
					this.tcpClient.close();
				}
			}
		} catch (IOException e) {
			throw e;
		} finally {
			// lets force these events to be in the right order.
			onClose();
		}
	}

	/**
	 * Calculate state message.
	 *
	 * @param socket the socket
	 */
	private void calculateStateMessage(Socket socket) {
		String state = null;

		// ideally we want to know where we connected exactly
		if (socket != null) {
			try {
				if (socket.isConnected()) {
					InetAddress localAddress = socket.getLocalAddress();
					InetAddress remoteAddress = socket.getInetAddress();
					state = String.format("%sNetwork Client from %s:%d to %s", this.options.getUseSsl() ? "Encrypted " : "", localAddress.getHostAddress(),
							socket.getLocalPort(), remoteAddress.getHostAddress());
				}
			} catch (java.lang.Exception e) {
				if (SystemUtils.isInDebugMode()) {
					e.printStackTrace();
				}
			}
		}

		if (TypeUtils.isBlank(state)) {
			if (this.options != null) {
				state = String.format("%3$sNetwork Client to %s:%d (Not connected)", this.options.getHostName(),
						this.options.getPort(), this.options.getUseSsl() ? "Encrypted " : "");
			} else {
				state = "Network Client (Not connected)";
			}
		}

		this.statusString = state;
	}

	/**
	 * Get a new TCP Client,if possible.
	 *
	 * @return the tcp client
	 */
	private Socket getTcpClient() {
		Socket newClient = null;
		RuntimeException innerException = null;
		if (this.singleSocket) {
			newClient = this.tcpClient;
			this.tcpClient = null; // it can only be used once
		} else {
			try {
				if (this.options.getUseSsl()) {
					SSLSocket sslSocket = (SSLSocket)this.sslSocketFactory.createSocket(this.options.getHostName(),
							this.options.getPort());
					sslSocket.setEnabledProtocols(SECURITY_PROTOCOLS);
					newClient = sslSocket;
				} else {
					newClient = new Socket(this.options.getHostName(), this.options.getPort());
				}
			} catch (SocketException ex) {
				innerException = new GibraltarNetworkException(ex);
			} catch (IOException ex) {
				// the doc indicates you'll often get an IO exception that WRAPS a socket
				// exception.
				if ((ex.getCause() != null) && (ex.getCause() instanceof SocketException)) {
					innerException = new GibraltarNetworkException(ex.getCause());
				} else {
					throw new GibraltarNetworkException(ex); // in this case we're going to let the RAW exception get
																// sent out instead of a network exception ex; // in
																// this case we're going to let the RAW exception get
																// sent out instead of a network exception
				}
			}
		}

		if (newClient == null || !newClient.isConnected()) {
			throw new GibraltarNetworkException("There is no connection available", innerException);
		}

		return newClient;
	}

}