package com.onloupe.core.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Generates and parses the binary header used at the start of a Gibraltar Log
 * File
 */
public class FileHeader {
	/**
	 * The number of bytes used by the header
	 */
	public static final int HEADER_SIZE = 20;

	/**
	 * The unique sequence of bytes at the start of the header that identify a
	 * binary file as a GLF
	 */
	public static final long GLFTYPE_CODE = 0x79474c460d0a1a0aL; // modeled after PNG, except "GLF" substituted for
																	// "PNG"

	/**
	 * Default value for serialization protocol major version
	 * 
	 * 
	 * Normally, you'd expect this to be a constant. However, for testing purposes
	 * it's convenient to be able to change it back and forth.
	 * 
	 */
	public static short defaultMajorVersion = 2;

	/**
	 * Default value for serialization protocol minor version
	 */
	public static short defaultMinorVersion = 2;

	private long _TypeCode;
	private short _MajorVersion;
	private short _MinorVersion;
	private int _DataOffset;
	private int _DataChecksum;

	/**
	 * Create a new empty file header
	 */
	public FileHeader() {
		this(defaultMajorVersion, defaultMinorVersion);
	}

	/**
	 * Create a new empty file header
	 */
	public FileHeader(int majorVersion, int minorVersion) {
		// set our defaults to proper GLF format
		this._TypeCode = GLFTYPE_CODE;
		this._MajorVersion = (short) majorVersion;
		this._MinorVersion = (short) minorVersion;
	}

	/**
	 * Create a new header from the provided byte array
	 * 
	 * @param data The byte array must have at least as many bytes as indicated by
	 *             the Header Size.
	 * @throws IOException 
	 */
	public FileHeader(byte[] data) throws IOException {
		// we need the input data buffer to be the right size to interpret.
		if (data == null) {
			throw new NullPointerException("data");
		}

		if (data.length < HEADER_SIZE) {
			throw new IllegalArgumentException("The provided header buffer is too short to be a valid header.");
		}

		ByteArrayInputStream rawData = new ByteArrayInputStream(data);
		this._TypeCode = BinarySerializer.deserializeLong(rawData);
		this._MajorVersion = BinarySerializer.deserializeShort(rawData);
		this._MinorVersion = BinarySerializer.deserializeShort(rawData);
		this._DataOffset = BinarySerializer.deserializeInt(rawData);
		this._DataChecksum = BinarySerializer.deserializeInt(rawData);
	}

	/**
	 * Export the file header into a raw data array
	 * 
	 * @return
	 */
	public final byte[] rawData() {
		ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
		buffer.put(BinarySerializer.serializeValue(this._TypeCode));
		buffer.put(BinarySerializer.serializeValue(this._MajorVersion));
		buffer.put(BinarySerializer.serializeValue(this._MinorVersion));
		buffer.put(BinarySerializer.serializeValue(this._DataOffset));
		buffer.put(BinarySerializer.serializeValue(this._DataChecksum));

		// we should have exactly filled our header to size.
		assert buffer.remaining() == 0;

		return buffer.array();
	}

	/**
	 * The type code set in the file
	 */
	public final long getTypeCode() {
		return this._TypeCode;
	}

	public final void setTypeCode(long value) {
		this._TypeCode = value;
	}

	/**
	 * The major version of the file
	 */
	public final short getMajorVersion() {
		return this._MajorVersion;
	}

	public final void setMajorVersion(short value) {
		this._MajorVersion = value;
	}

	/**
	 * The minor version of the file
	 */
	public final short getMinorVersion() {
		return this._MinorVersion;
	}

	public final void setMinorVersion(short value) {
		this._MinorVersion = value;
	}

	/**
	 * The offset in the stream from the start of the file header to the start of
	 * the data section
	 */
	public final int getDataOffset() {
		return this._DataOffset;
	}

	public final void setDataOffset(int value) {
		this._DataOffset = value;
	}

	/**
	 * A checksum of the file header
	 */
	public final int getDataChecksum() {
		return this._DataChecksum;
	}

	public final void setDataChecksum(int value) {
		this._DataChecksum = value;
	}

	/**
	 * True if the header is valid. Always returns true.
	 * 
	 * @return
	 */
	public final boolean isValid() {
		return true; // yeah, until we implement checksums we won't do it
	}

	/**
	 * Indicates if the supplied file version supports the Computer Id field.
	 */
	public static boolean supportsComputerId(int majorVersion, int minorVersion) {
		return ((majorVersion > 2) || ((majorVersion == 2) && (minorVersion > 0)));
	}

	/**
	 * Indicates if the supplied file version supports the Environment and Promotion
	 * fields.
	 */
	public static boolean supportsEnvironmentAndPromotion(int majorVersion, int minorVersion) {
		return (majorVersion > 1);
	}

	/**
	 * Indicates if the binary stream supports fragments or only single-stream
	 * transfer (the pre-3.0 format)
	 */
	public static boolean supportsFragments(int majorVersion, int minorVersion) {
		return (majorVersion > 1);
	}
}