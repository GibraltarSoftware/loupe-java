package com.onloupe.core.serialization;

import com.onloupe.core.data.FileHeader;
import com.onloupe.core.util.IOUtils;
import com.onloupe.core.util.SystemUtils;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class PacketWriter.
 */
public class PacketWriter implements IPacketWriter, Closeable {
	
	/** The output stream. */
	private OutputStream outputStream;
	
	/** The buffer. */
	private ByteArrayOutputStream buffer;
	
	/** The buffer writer. */
	private IFieldWriter bufferWriter;
	
	/** The cached types. */
	private PacketDefinitionList cachedTypes;
	
	/** The packet cache. */
	private PacketCache packetCache;

	/**
	 * Initialize a PacketWriter to read the specified stream using the provided
	 * encoding for strings.
	 *
	 * @param stream Destination for data written
	 * @throws NoSuchMethodException the no such method exception
	 * @throws SecurityException the security exception
	 */
	public PacketWriter(OutputStream stream) throws NoSuchMethodException, SecurityException {
		this(stream, FileHeader.defaultMajorVersion, FileHeader.defaultMinorVersion);
	}

	/**
	 * Initialize a PacketWriter to read the specified stream using the provided
	 * encoding for strings.
	 *
	 * @param stream       Destination for data written
	 * @param majorVersion Major version of the serialization protocol
	 * @param minorVersion Minor version of the serialization protocol
	 * @throws NoSuchMethodException the no such method exception
	 * @throws SecurityException the security exception
	 */
	public PacketWriter(OutputStream stream, int majorVersion, int minorVersion)
			throws NoSuchMethodException, SecurityException {
		this.outputStream = stream;
		this.buffer = new ByteArrayOutputStream();
		this.bufferWriter = new FieldWriter(this.buffer, majorVersion, minorVersion);
		this.cachedTypes = new PacketDefinitionList();
		this.packetCache = new PacketCache();
	}

	/**
	 * Write the data needed to serialize the state of the packet.
	 *
	 * @param packet Object to be serialized, must implement IPacket
	 * @throws Exception the exception
	 */
	@Override
	public final void write(IPacket packet) throws Exception {
		// Before we do anything - is this a cached packet that's already been written out?
		ICachedPacket cachedPacket = packet instanceof ICachedPacket ? (ICachedPacket) packet : null;
		if (cachedPacket != null) {
			// it is cacheable - is it in the cache?
			if (this.packetCache.contains(cachedPacket)) {
				// good to go, we're done.
				return;
			}
		}

		// First, we need to find out if there are any packets this guy depends on. If there are,
		// they have to be serialized out first. They may have been - they could be cached.
		// to do this, we'll need to get the definition.
		PacketDefinition previewDefinition;
		int previewTypeIndex = this.cachedTypes.indexOf(packet);
		if (previewTypeIndex < 0) {
			// we're going to get the definition, BUT we're not going to cache it yet.
			// This is because we recurse on our self if there are required packets, and if one of those
			// packets is our same type, IT has to write out the definition so that it's on the stream
			// before the packet itself.
			previewDefinition = PacketDefinition.createPacketDefinition(packet);
		} else {
			previewDefinition = this.cachedTypes.get(previewTypeIndex);
		}

		List<IPacket> requiredPackets = previewDefinition.getRequiredPackets(packet);

		for (IPacket requiredPacket : requiredPackets) {
			write(requiredPacket); // this will handle if it's a cached packet and shouldn't be written out.
		}

		// Begin our "transactional" phase
		try {
			// The first time a packet type is written, we send along a packet definition
			PacketDefinition definition;
			int typeIndex = this.cachedTypes.indexOf(packet);
			if (typeIndex < 0) {
				// Record that we've seen this type so we don't bother sending the
				// PacketDefinition again
				definition = PacketDefinition.createPacketDefinition(packet);
				typeIndex = this.cachedTypes.getCount();
				this.cachedTypes.add(definition);

				// Each packet always starts with a packet type index. And the first time a new
				// index is used, it is followed by the packet definition.
				this.bufferWriter.writePositive(typeIndex);
				definition.writeDefinition(this.bufferWriter);
			} else {
				// If this type has been written before, just send the type index
				this.bufferWriter.writePositive(typeIndex);
				definition = this.cachedTypes.get(typeIndex);
			}

			// if it's cacheable then we need to add it to our packet cache before we write it out
			if (definition.isCachable()) {
				// In the case of an ICachedPacket, we need to add it to the cache.
				// we'd have already bailed if it was in there.
				// Note: Use previous cast for efficiency, but we must recast here *if* packet gets reassigned above
				// Currently it does not get reassigned, so cachedPacket which we cast packet into above is still valid.
				this.packetCache.addOrGet(cachedPacket);
			}

			// Finally, and it really is a long journey, we ask the definition to write out
			// the individual fields for the packet
			definition.writeFields(packet, this.bufferWriter);
		} catch (Exception e) {
			if (SystemUtils.isInDebugMode()) {
				e.printStackTrace();
			}
			
			rollback();
			throw e;
		}

		// Write the data to the stream preceded by the length of this packet
		// NOTE: The logic below is careful to ensure that the length and payload is written in one call
		// This is necessary to ensure that the GZipStream writes the whole packet in edge cases
		// of writing the very last packet as an application is exiting.

		int payloadLength = this.buffer.size(); // get the actual length of the payload
		ByteBuffer encodedLength = FieldWriter.writeLength(payloadLength);
		int lengthLength = encodedLength.position();

		// the packetBytes array will contain the length and payload, in order
		byte[] packetBytes = new byte[lengthLength + payloadLength];

		encodedLength.rewind();
		encodedLength.get(packetBytes, 0, lengthLength); // write the length

		ByteBuffer payload = ByteBuffer.wrap(this.buffer.toByteArray());
		payload.get(packetBytes, lengthLength, payloadLength);

		this.outputStream.write(packetBytes, 0, packetBytes.length);
		this.buffer.reset();
		commit();
	}

	/**
	 * Commit.
	 */
	private void commit() {
		this.cachedTypes.commit();
	}

	/**
	 * Rollback.
	 */
	private void rollback() {
		this.cachedTypes.rollback();
	}

	/**
	 * Gets the output stream.
	 *
	 * @return the output stream
	 */
	public OutputStream getOutputStream() {
		return this.outputStream;
	}

	/* (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		IOUtils.closeQuietly(this.buffer);
		IOUtils.closeQuietly(this.outputStream);
	}

}