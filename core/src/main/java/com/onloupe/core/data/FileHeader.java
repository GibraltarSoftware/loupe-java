package com.onloupe.core.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;


/**
 * Generates and parses the binary header used at the start of a Gibraltar Log
 * File.
 */
public class FileHeader {
	
	/** The number of bytes used by the header. */
	public static final int HEADER_SIZE = 20;

	/** The unique sequence of bytes at the start of the header that identify a binary file as a GLF. */
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

	/** Default value for serialization protocol minor version. */
	public static short defaultMinorVersion = 2;

	/** The Type code. */
	private long _TypeCode;
	
	/** The Major version. */
	private short _MajorVersion;
	
	/** The Minor version. */
	private short _MinorVersion;
	
	/** The Data offset. */
	private int _DataOffset;
	
	/** The Data checksum. */
	private int _DataChecksum;

	/**
	 * Create a new empty file header.
	 */
	public FileHeader() {
		this(defaultMajorVersion, defaultMinorVersion);
	}

	/**
	 * Create a new empty file header.
	 *
	 * @param majorVersion the major version
	 * @param minorVersion the minor version
	 */
	public FileHeader(int majorVersion, int minorVersion) {
		// set our defaults to proper GLF format
		this._TypeCode = GLFTYPE_CODE;
		this._MajorVersion = (short) majorVersion;
		this._MinorVersion = (short) minorVersion;
	}

	/**
	 * Create a new header from the provided byte array.
	 *
	 * @param data The byte array must have at least as many bytes as indicated by
	 *             the Header Size.
	 * @throws IOException Signals that an I/O exception has occurred.
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
	 * Export the file header into a raw data array.
	 *
	 * @return the byte[]
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
	 * The type code set in the file.
	 *
	 * @return the type code
	 */
	public final long getTypeCode() {
		return this._TypeCode;
	}

	/**
	 * Sets the type code.
	 *
	 * @param value the new type code
	 */
	public final void setTypeCode(long value) {
		this._TypeCode = value;
	}

	/**
	 * The major version of the file.
	 *
	 * @return the major version
	 */
	public final short getMajorVersion() {
		return this._MajorVersion;
	}

	/**
	 * Sets the major version.
	 *
	 * @param value the new major version
	 */
	public final void setMajorVersion(short value) {
		this._MajorVersion = value;
	}

	/**
	 * The minor version of the file.
	 *
	 * @return the minor version
	 */
	public final short getMinorVersion() {
		return this._MinorVersion;
	}

	/**
	 * Sets the minor version.
	 *
	 * @param value the new minor version
	 */
	public final void setMinorVersion(short value) {
		this._MinorVersion = value;
	}

	/**
	 * The offset in the stream from the start of the file header to the start of
	 * the data section.
	 *
	 * @return the data offset
	 */
	public final int getDataOffset() {
		return this._DataOffset;
	}

	/**
	 * Sets the data offset.
	 *
	 * @param value the new data offset
	 */
	public final void setDataOffset(int value) {
		this._DataOffset = value;
	}

	/**
	 * A checksum of the file header.
	 *
	 * @return the data checksum
	 */
	public final int getDataChecksum() {
		return this._DataChecksum;
	}

	/**
	 * Sets the data checksum.
	 *
	 * @param value the new data checksum
	 */
	public final void setDataChecksum(int value) {
		this._DataChecksum = value;
	}

	/**
	 * True if the header is valid. Always returns true.
	 *
	 * @return true, if is valid
	 */
	public final boolean isValid() {
		return true; // yeah, until we implement checksums we won't do it
	}

	/**
	 * Indicates if the supplied file version supports the Computer Id field.
	 *
	 * @param majorVersion the major version
	 * @param minorVersion the minor version
	 * @return true, if successful
	 */
	public static boolean supportsComputerId(int majorVersion, int minorVersion) {
		return ((majorVersion > 2) || ((majorVersion == 2) && (minorVersion > 0)));
	}

	/**
	 * Indicates if the supplied file version supports the Environment and Promotion
	 * fields.
	 *
	 * @param majorVersion the major version
	 * @param minorVersion the minor version
	 * @return true, if successful
	 */
	public static boolean supportsEnvironmentAndPromotion(int majorVersion, int minorVersion) {
		return (majorVersion > 1);
	}

	/**
	 * Indicates if the binary stream supports fragments or only single-stream
	 * transfer (the pre-3.0 format)
	 *
	 * @param majorVersion the major version
	 * @param minorVersion the minor version
	 * @return true, if successful
	 */
	public static boolean supportsFragments(int majorVersion, int minorVersion) {
		return (majorVersion > 1);
	}
}