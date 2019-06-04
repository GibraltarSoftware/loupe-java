package com.onloupe.core.serialization;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import com.onloupe.core.data.FileHeader;
import com.onloupe.core.util.IOUtils;
import com.onloupe.core.util.TypeUtils;

// TODO: Auto-generated Javadoc
/**
 * Reads a packet data stream, recreating the packets it contains.
 */
public class PacketReader implements IPacketReader, Closeable {
	
	/** The stream. */
	private DataInputStream stream;
	
	/** The input is read only. */
	private boolean inputIsReadOnly;
	
	/** The reader. */
	private final FieldReader reader;
	
	/** The cached types. */
	private PacketDefinitionList cachedTypes;
	
	/** The packet factory. */
	private PacketFactory packetFactory;
	
	/** The release stream. */
	private boolean releaseStream; // indicate whether we need to release _Stream upon Dispose()
	
	/** The closed. */
	private boolean closed;
	
	/** The major version. */
	private int majorVersion;
	
	/** The minor version. */
	private int minorVersion;

	/**
	 * Initialize a PacketReader to read the specified stream using the provided
	 * encoding for strings.
	 *
	 * @param inputStream     Data to be read
	 * @param inputIsReadOnly Indicates if the input can be assumed fixed in length
	 * @throws NoSuchMethodException the no such method exception
	 * @throws SecurityException the security exception
	 */
	public PacketReader(InputStream inputStream, boolean inputIsReadOnly)
			throws NoSuchMethodException, SecurityException {
		this(inputStream, inputIsReadOnly, FileHeader.defaultMajorVersion, FileHeader.defaultMinorVersion);
	}

	/**
	 * Initialize a PacketReader to read the specified stream using the provided
	 * encoding for strings.
	 *
	 * @param inputStream     Data to be read
	 * @param inputIsReadOnly Indicates if the input can be assumed fixed in length
	 * @param majorVersion    Major version of the serialization protocol
	 * @param minorVersion    Minor version of the serialization protocol
	 * @throws NoSuchMethodException the no such method exception
	 * @throws SecurityException the security exception
	 */
	public PacketReader(InputStream inputStream, boolean inputIsReadOnly, int majorVersion, int minorVersion)
			throws NoSuchMethodException, SecurityException {
		this.stream = new DataInputStream(inputStream);
		this.inputIsReadOnly = inputIsReadOnly;
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;

		// _ReleaseStream = false; // _Stream was passed in to us, don't release it upon
		// Dispose()!
		// (false by default) If we were invoked from another constructor, they will
		// overwrite _ReleaseStream correctly
		this.reader = new FieldReader(this.stream, majorVersion, minorVersion);
		this.cachedTypes = new PacketDefinitionList();
		this.packetFactory = new PacketFactory();
	}

	/**
	 * Initialize a PacketReader to read the specified data using the default
	 * encoding for strings.
	 *
	 * @param data Data to be read
	 * @throws NoSuchMethodException the no such method exception
	 * @throws SecurityException the security exception
	 */
	public PacketReader(byte[] data) throws NoSuchMethodException, SecurityException {
		this(new ByteArrayInputStream(data), true);
		this.releaseStream = true;
	}

	/**
	 * Indicates if there are any more packets available on the current stream.
	 *
	 * @return the data available
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final boolean getDataAvailable() throws IOException {
		if (this.stream.available() <= 0) {
			return false; // we're EOF so
		}

		int packetSize = (int) this.reader.peekUInt64();

		// there is only data available if our stream is long enough to contain a whole
		// packet
		return (this.stream.available() >= packetSize);
	}

	/**
	 * Read and return the next IPacket from the stream.
	 *
	 * @return the i packet
	 * @throws NoSuchMethodException the no such method exception
	 * @throws SecurityException the security exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InstantiationException the instantiation exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	@Override
	public final IPacket read() throws NoSuchMethodException, SecurityException, IOException, IllegalAccessException, InstantiationException, InvocationTargetException {
		int packetSize = (int) this.reader.readPositiveLong();

		// if the packet size is less than one, that's obviously wrong
		if (packetSize < 1) {
			throw new GibraltarSerializationException(
					"The size of the next packet is smaller than 1 byte or negative, which can't be correct.  The packet stream is corrupted.",
					true);
		}

		byte[] buffer = new byte[packetSize];
		this.stream.read(buffer, 0, packetSize);

		IFieldReader bufferReader = new FieldReader(new ByteArrayInputStream(buffer), this.reader.getStreamState(), this.majorVersion,
				this.minorVersion);

		PacketDefinition definition;
		int typeIndex = bufferReader.readPositiveInt();
		if (typeIndex >= this.cachedTypes.getCount()) {
			definition = PacketDefinition.readPacketDefinition(bufferReader);
			if (TypeUtils.isBlank(definition.getTypeName())) {
				// we're hosed... we won't be able to parse this packet.
				throw new GibraltarSerializationException(
						"The type name of the definition is null, which can't be correct.  The packet stream is corrupted.",
						true);
			}

			this.cachedTypes.add(definition);
			this.cachedTypes.commit();
		} else {
			definition = this.cachedTypes.get(typeIndex);
		}

		IPacketFactory factory = this.packetFactory.getPacketFactory(definition.getTypeName());
		IPacket packet = factory.createPacket(definition, bufferReader);

		return packet;
	}

	/**
	 * Read and return the next IPacket from the stream.
	 *
	 * @param packetStream the packet stream
	 * @return the i packet
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InstantiationException the instantiation exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	public final IPacket readPacket(InputStream packetStream) throws IOException, IllegalAccessException, InstantiationException, InvocationTargetException {
		this.reader.replaceStream(packetStream);

		PacketDefinition definition;
		int typeIndex = this.reader.readPositiveInt();
		if (typeIndex >= this.cachedTypes.getCount()) {
			definition = PacketDefinition.readPacketDefinition(this.reader);
			if (TypeUtils.isBlank(definition.getTypeName())) {
				// we're hosed... we won't be able to parse this packet.
				throw new GibraltarSerializationException(
						"The type name of the definition is null, which can't be correct.  The packet stream is corrupted.",
						true);
			}

			this.cachedTypes.add(definition);
			this.cachedTypes.commit();
		} else {
			definition = this.cachedTypes.get(typeIndex);
		}
		definition.setPacketCount(definition.getPacketCount() + 1);
		definition.setPacketSize(definition.getPacketSize() + packetStream.available());

		IPacketFactory factory = this.packetFactory.getPacketFactory(definition.getTypeName());
		IPacket packet = factory.createPacket(definition, this.reader);

		return packet;
	}

	/**
	 * Returns a summary of packet count and size for each packet type
	 * 
	 * 
	 * The returned list is sorted using the default sort implied by the
	 * PacketTypeStorageSummary.CompareTo method.
	 *
	 * @return the storage summary
	 */
	public final ArrayList<PacketTypeStorageSummary> getStorageSummary() {
		ArrayList<PacketTypeStorageSummary> summary = new ArrayList<PacketTypeStorageSummary>();
		for (PacketDefinition cachedType : this.cachedTypes) {
			PacketTypeStorageSummary packetSummary = new PacketTypeStorageSummary(cachedType);
			summary.add(packetSummary);
		}

		return summary;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.IPacketReader#registerType(java.lang.Class)
	 */
	@Override
	public final void registerType(java.lang.Class type) {
		this.packetFactory.registerType(type);
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.IPacketReader#registerFactory(java.lang.String, com.onloupe.core.serialization.IPacketFactory)
	 */
	@Override
	public final void registerFactory(String typeName, IPacketFactory factory) {
		this.packetFactory.registerFactory(typeName, factory);
	}

	/**
	 * Performs application-defined tasks associated with freeing, releasing, or
	 * resetting managed resources.
	 * 
	 * 
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public final void close() throws IOException {
		if (!this.closed) {
			this.closed = true;

			if (this.releaseStream) {
				IOUtils.closeQuietly(this.stream);
				this.releaseStream = false;
			}
		}
	}
}