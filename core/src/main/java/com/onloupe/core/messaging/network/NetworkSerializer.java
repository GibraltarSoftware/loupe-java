package com.onloupe.core.messaging.network;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import com.onloupe.core.util.IOUtils;
import com.onloupe.core.util.PacketHeader;


/**
 * Used to serialize network packets across a TCP socket.
 */
public class NetworkSerializer implements Closeable {
	
	/** The lock. */
	private final Object lock = new Object();
	
	/** The output stream. */
	private final PipedOutputStream outputStream = new PipedOutputStream();
	
	/** The input stream. */
	private final PipedInputStream inputStream = new PipedInputStream();
	
	/** The read buffer. */
	private final BufferedInputStream readBuffer;

	/**
	 * Instantiates a new network serializer.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public NetworkSerializer() throws IOException {
		inputStream.connect(outputStream);
		readBuffer = new BufferedInputStream(inputStream);
	}

	/**
	 * The unused data that has been provided to the serializer.
	 *
	 * @return the unused data
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final byte[] getUnusedData() throws IOException {
		synchronized (lock) {
			byte[] output = null;

			if (readBuffer.available() > 0) {
				output = new byte[readBuffer.available()];
				readBuffer.mark(output.length);
				readBuffer.read(output, 0, output.length);
				readBuffer.reset(); // set it back so people can get the buffer again.
			}

			return output;
		}
	}

	/**
	 * Indicates if there is any unused data in the network serializer.
	 *
	 * @return the have unused data
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final boolean getHaveUnusedData() throws IOException {
		synchronized (lock) {
			return readBuffer.available() > 0;

		}
	}

	/**
	 * Add more information to the serializer stream.
	 *
	 * @param buffer the buffer
	 * @param length the length
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final void appendData(byte[] buffer, int length) throws IOException {
		synchronized (lock) {
			outputStream.write(buffer, 0, length);
		}
	}

	/**
	 * Read the next network packets in the buffer.
	 *
	 * @return A complete packet or null if there isn't enough data to make a packet
	 *         Since packets may be spread across multiple packets the serializer
	 *         keeps a buffer of any unused bytes for the next read. This means a
	 *         Read call may return zero or one network packets
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final NetworkMessage readNext() throws IOException // make it wait
	{
		synchronized (this.lock) {
			// now lets figure out if we have one or more packets
			NetworkMessage nextPacket = null;
			PacketHeader header = NetworkMessage.readHeader(this.readBuffer);
			if (header != null && (this.readBuffer.available() >= header.getPacketLength())) {
				// we have enough data to read a packet
				nextPacket = NetworkMessage.read(this.readBuffer);
			}

			return nextPacket;
		}
	}

	/**
	 * Performs application-defined tasks associated with freeing, releasing, or
	 * resetting unmanaged resources.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final void close() throws IOException {
		IOUtils.closeQuietly(readBuffer);
		IOUtils.closeQuietly(inputStream);
		IOUtils.closeQuietly(outputStream);
	}
}