package com.onloupe.core.data;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;

import com.onloupe.core.util.TimeConversion;
import com.onloupe.core.util.TypeUtils;

/**
 * Provides basic binary serialization for platform independent simple
 * serialization
 */
public final class BinarySerializer {

	/**
	 * Serialize a boolean value to a byte array with a single byte
	 * 
	 * @param hostValue
	 * @return
	 */
	public static byte[] serializeValue(boolean hostValue) {
		return hostValue ? new byte[] { 0x1 } : new byte[] { 0x0 };
	}

	/**
	 * Serialize a GUID to a 16 byte array
	 * 
	 * @param hostValue
	 * @return
	 */
	public static byte[] serializeValue(UUID hostValue) {
		byte[] javaUUID = ByteBuffer.allocate(Long.BYTES + Long.BYTES).putLong(hostValue.getMostSignificantBits())
				.putLong(hostValue.getLeastSignificantBits()).array();

		//We have to match .NET's byte ordering which was set long ago in the COM interop standard.
		byte[] dotNetGUID = Arrays.copyOf(javaUUID, javaUUID.length);

		//now re-arrange the first eight bytes to match .NET's DWORD-WORD-WORD little endian order.
		dotNetGUID[0] = javaUUID[3];
		dotNetGUID[1] = javaUUID[2];
		dotNetGUID[2] = javaUUID[1];
		dotNetGUID[3] = javaUUID[0];
		dotNetGUID[4] = javaUUID[5];
		dotNetGUID[5] = javaUUID[4];
		dotNetGUID[6] = javaUUID[7];
		dotNetGUID[7] = javaUUID[6];

		return dotNetGUID;
	}

	/**
	 * Serialize a string to a byte array
	 * 
	 * @param hostValue
	 * @return The byte array for the string Serializes the length in the first byte
	 *         then each character with one byte character encoding
	 */
	public static byte[] serializeValue(String hostValue) {
		// short circuit handling for nullstring
		if (hostValue == null) {
			return serializeValue(-1); // -1 magic value for null length string in 4 byte encoding
		}

		if (TypeUtils.isBlank(hostValue)) {
			return serializeValue(0); // zero length string in 4 byte encoding
		}

		// Get the UTF-8 encoded string
		byte[] rawValue = hostValue.getBytes(StandardCharsets.UTF_8);

		// but return it with the length as the first byte.
		ByteBuffer valueLengthBuffer = ByteBuffer.allocate(Integer.BYTES).putInt(rawValue.length);
		valueLengthBuffer.rewind();
		
		byte[] networkValue = new byte[Integer.BYTES + rawValue.length];
		valueLengthBuffer.get(networkValue, 0, Integer.BYTES);

		ByteBuffer valueBuffer = ByteBuffer.wrap(rawValue);
		valueBuffer.get(networkValue, Integer.BYTES, rawValue.length);
		return networkValue;
	}

	/**
	 * Serialize a date time to a byte array
	 * 
	 * @param hostValue
	 * @return Uses the date time offset encoding with the local time zone.
	 */
	public static byte[] serializeValue(LocalDateTime hostValue) {
		return serializeValue(OffsetDateTime.of(hostValue, ZoneOffset.UTC));
	}

	/**
	 * Serialize a date time and offset to a byte array.
	 * 
	 * @param hostValue
	 * @return Encodes the date time offset as a string in ISO 8601 standard
	 *         formatting
	 */
	public static byte[] serializeValue(OffsetDateTime hostValue) {
		// this is the ISO 8601 Standard format for date to string.
		return serializeValue(hostValue.format(TimeConversion.CS_DATETIMEOFFSET_FORMAT));
	}

	/**
	 * Serialize a Duration to a byte array
	 * 
	 * @param hostValue
	 * @return
	 */
	public static byte[] serializeValue(Duration hostValue) {
		return serializeValue(TimeConversion.durationInTicks(hostValue));
	}

	/**
	 * Create a network-byte-order array of the host value
	 * 
	 * @param hostValue The host value to be serialized
	 * @return A byte array of each byte of the value in network byte order
	 */
	public static byte[] serializeValue(long hostValue) {
		return ByteBuffer.allocate(Long.BYTES).putLong(hostValue).array();
	}

	/**
	 * Create a network-byte-order array of the host value
	 * 
	 * @param hostValue The host value to be serialized
	 * @return A byte array of each byte of the value in network byte order
	 */
	// bytebuffer & integer in java is always big endian, so this
	// should be okay.
	public static byte[] serializeValue(int hostValue) {
		return ByteBuffer.allocate(Integer.BYTES).putInt(hostValue).array();
	}

	/**
	 * Create a network-byte-order array of the host value
	 * 
	 * @param hostValue The host value to be serialized
	 * @return A byte array of each byte of the value in network byte order
	 */
	public static byte[] serializeValue(short hostValue) {
		return ByteBuffer.allocate(Short.BYTES).putShort(hostValue).array();
	}

	/**
	 * Deserialize a Duration value from the provided stream
	 * 
	 * @throws IOException
	 */
	public static Duration deserializeDuration(InputStream inputStream) throws IOException {
		DataInputStream stream = new DataInputStream(inputStream);
		return Duration.ofNanos(Math.multiplyExact(100, stream.readLong()));
	}

	public static Duration deserializeDuration(ByteBuffer buffer) {
		return Duration.ofNanos(Math.multiplyExact(100, buffer.getLong()));
	}

	/**
	 * Deserialize a date and time value from the provided stream
	 * 
	 * @throws IOException
	 */
	public static LocalDateTime deserializeLocalDateTime(InputStream inputStream) throws IOException {
		return deserializeOffsetDateTimeValue(inputStream).toLocalDateTime();
	}

	/**
	 * Deserialize a date time offset value from the provided buffer
	 * 
	 * @param buffer
	 */
	public static OffsetDateTime deserializeOffsetDateTimeValue(ByteBuffer buffer) {
		return OffsetDateTime.parse(deserializeStringValue(buffer), TimeConversion.CS_DATETIMEOFFSET_FORMAT);
	}

	/**
	 * Deserialize a date time offset value from the provided stream
	 * 
	 * @throws IOException
	 */
	public static OffsetDateTime deserializeOffsetDateTimeValue(InputStream inputStream) throws IOException {
		return OffsetDateTime.parse(deserializeStringValue(inputStream), TimeConversion.CS_DATETIMEOFFSET_FORMAT);
	}

	public static int deserializeInt(InputStream inputStream) throws IOException {
		int ch1 = inputStream.read();
		int ch2 = inputStream.read();
		int ch3 = inputStream.read();
		int ch4 = inputStream.read();
		if ((ch1 | ch2 | ch3 | ch4) < 0)
			throw new EOFException();
		return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
	}
	
	public static short deserializeShort(InputStream inputStream) throws IOException {
        byte[] curValue = new byte[Short.BYTES];
        inputStream.read(curValue, 0, curValue.length);
        return ByteBuffer.wrap(curValue).getShort();
	}

	public static long deserializeLong(InputStream inputStream) throws IOException {
        byte[] curValue = new byte[Long.BYTES];
        inputStream.read(curValue, 0, curValue.length);
        return ByteBuffer.wrap(curValue).getLong();
	}
	
	/**
	 * Deserialize a GUID value from the provided buffer
	 * 
	 * @param buffer
	 */
	public static UUID deserializeUUIDValue(ByteBuffer buffer) {
		byte[] dotNetMostSignificantBits = new byte[8];
		buffer.get(dotNetMostSignificantBits, 0, dotNetMostSignificantBits.length);
		byte[] leastSignificantBits = new byte[8];
		buffer.get(leastSignificantBits, 0, leastSignificantBits.length);

		return deserializeDotNetGUIDValue(dotNetMostSignificantBits, leastSignificantBits);
	}

	/**
	 * Deserialize a GUID value from the provided stream
	 * 
	 * @throws IOException
	 */
	public static UUID deserializeUUIDValue(InputStream inputStream) throws IOException {
		byte[] dotNetMostSignificantBits = new byte[8];
		inputStream.read(dotNetMostSignificantBits, 0, dotNetMostSignificantBits.length);
		byte[] leastSignificantBits = new byte[8];
		inputStream.read(leastSignificantBits, 0, leastSignificantBits.length);

		return deserializeDotNetGUIDValue(dotNetMostSignificantBits, leastSignificantBits);
	}

	private static UUID deserializeDotNetGUIDValue(byte[] mostSignificantBits, byte[] leastSignificantBits) {
		//now we have to reorder the most significant bits from the .NET format DWORD-WORD-WORD little endian
		//to the Java format.
		byte[] javaMostSignificantBits = new byte[8];
		javaMostSignificantBits[0] = mostSignificantBits[3];
		javaMostSignificantBits[1] = mostSignificantBits[2];
		javaMostSignificantBits[2] = mostSignificantBits[1];
		javaMostSignificantBits[3] = mostSignificantBits[0];
		javaMostSignificantBits[4] = mostSignificantBits[5];
		javaMostSignificantBits[5] = mostSignificantBits[4];
		javaMostSignificantBits[6] = mostSignificantBits[7];
		javaMostSignificantBits[7] = mostSignificantBits[6];

		return new UUID(ByteBuffer.wrap(javaMostSignificantBits).getLong(),
				ByteBuffer.wrap(leastSignificantBits).getLong());
	}

	/**
	 * Deserialize a boolean value from the provided stream
	 *
	 * @throws IOException
	 */
	public static boolean deserializeBooleanValue(InputStream inputStream) throws IOException {
		return inputStream.read() == 0 ? false : true;

	}

	/**
	 * Deserialize a boolean value from the provided buffer
	 * 
	 * @param buffer
	 */
	public static boolean deserializeBooleanValue(ByteBuffer buffer) {
		return buffer.get() == 0 ? false : true;
	}

	/**
	 * Deserialize a string value from the provided stream
	 * 
	 */
	public static String deserializeStringValue(ByteBuffer buffer) {
		// first get the length of the string
		Integer length = buffer.getInt();

		// now get the string, based on that length.
		if (length > 0) {
			byte[] curValue = new byte[length];
			buffer.get(curValue, 0, curValue.length);
			return new String(curValue, StandardCharsets.UTF_8);
		} else {
			return "";
		}

	}

	/**
	 * Deserialize a string value from the provided stream
	 * 
	 * @throws IOException
	 */
	public static String deserializeStringValue(InputStream inputStream) throws IOException {
		// first get the length of the string
		Integer length = deserializeInt(inputStream);

		// now get the string, based on that length.
		if (length > 0) {
			byte[] curValue = new byte[length];
			inputStream.read(curValue, 0, curValue.length);
			return new String(curValue, StandardCharsets.UTF_8);
		} else {
			return "";
		}
	}

	/**
	 * Calculate a CRC for the provided byte array
	 * 
	 * @param data
	 * @param length
	 * @return A 4 byte CRC value created by calculating an MD5 hash of the provided
	 *         byte array
	 */
	public static byte[] calculateCRC(byte[] data, int length) {
		if (length < data.length) {
			byte[] shortData = new byte[length];
			ByteBuffer.wrap(data).get(shortData, 0, length);
			data = shortData;
		}

		// Calculates the MD5 digest and returns the value as a 16 element byte[].
		byte[] crc = DigestUtils.md5(data);

		// This is a big bogus - I'm just going to take the first four bytes. We really
		// should
		// go find the true CRC32 algorithm
		return new byte[] { crc[0], crc[1], crc[2], crc[3] };
	}
}