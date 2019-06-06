package com.onloupe.core.messaging.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.onloupe.core.data.BinarySerializer;
import com.onloupe.core.util.PacketHeader;
import com.onloupe.model.system.Version;


/**
 * A packet of data that can be serialized across the network.
 */
public abstract class NetworkMessage {
	
	/** The Constant BASE_PACKET_LENGTH. */
	private static final int BASE_PACKET_LENGTH = 16; // our fixed size when serialized

	/** The lock. */
	private final Object lock = new Object();

	/** The version. */
	private Version version;
	
	/** The type code. */
	private NetworkMessageTypeCode typeCode;
	
	/** The length. */
	private int length;

	/**
	 * The protocol version.
	 *
	 * @return the version
	 */
	public final Version getVersion() {
		synchronized (this.lock) {
			return this.version;
		}
	}

	/**
	 * Sets the version.
	 *
	 * @param value the new version
	 */
	public final void setVersion(Version value) {
		synchronized (this.lock) {
			this.version = value;
		}
	}

	/**
	 * The specific packet type code.
	 *
	 * @return the type code
	 */
	public final NetworkMessageTypeCode getTypeCode() {
		synchronized (this.lock) {
			return this.typeCode;
		}
	}

	/**
	 * Sets the type code.
	 *
	 * @param value the new type code
	 */
	public final void setTypeCode(NetworkMessageTypeCode value) {
		synchronized (this.lock) {
			this.typeCode = value;
		}
	}

	/**
	 * The number of bytes for the packet.
	 *
	 * @return the length
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final int getLength() throws IOException {
		synchronized (this.lock) {
			if (this.length == 0) {
				// we haven't calculated it yet... we have to serialize to find out.
				ByteArrayOutputStream test = new ByteArrayOutputStream();
				write(test);
				return test.size();
			}

			return this.length;
		}
	}
	
	

	/**
	 * Sets the length.
	 *
	 * @param length the new length
	 */
	public void setLength(int length) {
		synchronized(this.lock) {
			this.length = length;
		}
	}

	/**
	 * Peek at the byte data and see if there's a full packet header.
	 *
	 * @param inputStream the input stream
	 * @return the packet header
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static PacketHeader readHeader(InputStream inputStream) throws IOException {
		if (inputStream.available() >= BASE_PACKET_LENGTH) {
			inputStream.mark(BASE_PACKET_LENGTH);
			NetworkMessageTypeCode typeCode = NetworkMessageTypeCode
					.forValue(BinarySerializer.deserializeInt(inputStream));

			int majorVersion = BinarySerializer.deserializeInt(inputStream);
			int minorVersion = BinarySerializer.deserializeInt(inputStream);
			Version version = new Version(majorVersion, minorVersion);

			int packetLength = BinarySerializer.deserializeInt(inputStream);
			inputStream.reset();
			return new PacketHeader(packetLength, typeCode, version);
		} else {
			return null;
		}
	}

	/**
	 * Read the provided stream to create the packet.
	 *
	 * @param inputStream the input stream
	 * @return the network message
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static NetworkMessage read(InputStream inputStream) throws IOException {
		NetworkMessage newPacket;

		// read out the header, this gives us what we need to know what type of packet
		// to make.
		NetworkMessageTypeCode typeCode = NetworkMessageTypeCode.forValue(BinarySerializer.deserializeInt(inputStream));

		int majorVersion = BinarySerializer.deserializeInt(inputStream);
		int minorVersion = BinarySerializer.deserializeInt(inputStream);
		Version version = new Version(majorVersion, minorVersion);

		int packetLength = BinarySerializer.deserializeInt(inputStream);

		switch (typeCode) {
		case LIVE_VIEW_START_COMMAND:
			newPacket = new LiveViewStartCommandMessage();
			break;
		case LIVE_VIEW_STOP_COMMAND:
			newPacket = new LiveViewStopCommandMessage();
			break;
		case SEND_SESSION:
			newPacket = new SendSessionCommandMessage();
			break;
		case GET_SESSION_HEADERS:
			newPacket = new GetSessionHeadersCommandMessage();
			break;
		case REGISTER_AGENT_COMMAND:
			newPacket = new RegisterAgentCommandMessage();
			break;
		case REGISTER_ANALYST_COMMAND:
			newPacket = new RegisterAnalystCommandMessage();
			break;
		case SESSION_CLOSED:
			newPacket = new SessionClosedMessage();
			break;
		case SESSION_HEADER:
			newPacket = new SessionHeaderMessage();
			break;
		case PACKET_STREAM_START_COMMAND:
			newPacket = new PacketStreamStartCommandMessage();
			break;
		default:
			throw new IllegalStateException(
					"Unable to create network packet because it uses a type code that is unknown: " + typeCode);
		}

		newPacket.setLength(packetLength);
		newPacket.setTypeCode(typeCode);
		newPacket.setVersion(version);

		newPacket.onRead(inputStream);

		return newPacket;
	}

	/**
	 * Write the packet to the stream.
	 *
	 * @param stream The stream to write to
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final void write(OutputStream stream) throws IOException {
		synchronized (this.lock) {
			ByteArrayOutputStream ourPacketStream = new ByteArrayOutputStream();
			onWrite(ourPacketStream);

			stream.write(BinarySerializer.serializeValue(this.typeCode.getValue()));
			stream.write(BinarySerializer.serializeValue(this.version.getMajor()));
			stream.write(BinarySerializer.serializeValue(this.version.getMinor()));
			stream.write(BinarySerializer.serializeValue(ourPacketStream.size() + BASE_PACKET_LENGTH));
			stream.write(ourPacketStream.toByteArray());
			stream.flush();
		}
	}

	/**
	 * Write the packet to the stream.
	 *
	 * @param outputStream the output stream
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected abstract void onWrite(OutputStream outputStream) throws IOException;

	/**
	 * Read packet data from the stream.
	 *
	 * @param inputStream the input stream
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected abstract void onRead(InputStream inputStream) throws IOException;

	/**
	 * Inheritors must implement this to reflect their packet length as they read a
	 * packet plus the base length that came before.
	 * 
	 * At any time the remaining length is the Length property minus the BaseLength
	 * property.
	 *
	 * @return the base length
	 */
	protected int getBaseLength() {
		return BASE_PACKET_LENGTH;
	}

	/**
	 * Gets the content length.
	 *
	 * @return the content length
	 */
	// getContentLength would return _Length - Base_packet_length
	protected int getContentLength() {
		return this.length - BASE_PACKET_LENGTH;
	}
}