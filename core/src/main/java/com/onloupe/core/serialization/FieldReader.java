package com.onloupe.core.serialization;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.onloupe.core.data.FileHeader;
import com.onloupe.core.util.TimeConversion;
import com.onloupe.model.exception.GibraltarException;


/**
 * Provides low-level decompression of the basic data types we pass over the
 * wire.
 * 
 * This class consumes a compressed stream of bytes to be produced by
 * FieldWriter to reinstate the original stream of basic data types passed to
 * FiedWriter.
 */
public class FieldReader implements IFieldReader {
	
	/** The stream. */
	private DataInputStream stream;
	
	/** The stream state. */
	private PacketStreamState streamState;
	
	/** The encoding. */
	private Charset encoding;
	
	/** The major version. */
	private int majorVersion;
	
	/** The minor version. */
	private int minorVersion;

	/** The string array reader. */
	private final ArrayEncoder<String> stringArrayReader;

	/**
	 * Initialize a FieldReader to read the specified stream using the provided
	 * encoding for strings. Also, share state with the specified parent reader.
	 *
	 * @param inputStream  Data to be read
	 * @param streamState Stream State tracking object for the packet stream
	 * @param majorVersion Major version of the serialization protocol
	 * @param minorVersion Minor version of the serialization protocol
	 * @throws NoSuchMethodException the no such method exception
	 * @throws SecurityException the security exception
	 */
	public FieldReader(InputStream inputStream, PacketStreamState streamState, int majorVersion, int minorVersion)
			throws NoSuchMethodException, SecurityException {
		this.stream = new DataInputStream(inputStream);
		this.encoding = StandardCharsets.UTF_8;
		this.streamState = streamState;
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
		this.stringArrayReader = new ArrayEncoder<String>(String.class);
	}

	/**
	 * Initialize a FieldReader to read the specified stream using the provided
	 * encoding for strings. Also, share state with the specified parent reader.
	 *
	 * @param inputStream  Data to be read
	 * @param majorVersion Major version of the serialization protocol
	 * @param minorVersion Minor version of the serialization protocol
	 * @throws NoSuchMethodException the no such method exception
	 * @throws SecurityException the security exception
	 */
	public FieldReader(InputStream inputStream, int majorVersion, int minorVersion)
			throws NoSuchMethodException, SecurityException {
		this(inputStream, new PacketStreamState(), majorVersion, minorVersion);
	}

	/**
	 * Initialize a FieldReader to read the specified stream using the provided
	 * encoding for strings.
	 *
	 * @param inputStream Data to be read
	 * @throws NoSuchMethodException the no such method exception
	 * @throws SecurityException the security exception
	 */
	public FieldReader(InputStream inputStream) throws NoSuchMethodException, SecurityException {
		this(inputStream, new PacketStreamState(), FileHeader.defaultMajorVersion, FileHeader.defaultMinorVersion);
	}

	/**
	 * Initialize a FieldReader to read the specified data using the default
	 * encoding for strings.
	 *
	 * @param data Data to be read
	 * @throws NoSuchMethodException the no such method exception
	 * @throws SecurityException the security exception
	 */
	public FieldReader(byte[] data) throws NoSuchMethodException, SecurityException {
		this(new ByteArrayInputStream(data));
	}

	/**
	 * Contextual information tracked while serializing or deserializing for a whole stream.
	 *
	 * @return the stream state
	 */
	public PacketStreamState getStreamState() { return this.streamState;}

	/**
	 * Allows the stream being read by a FieldReader to be replaced without having
	 * to re-instance a new object.
	 *
	 * @param inputStream the input stream
	 */
	public final void replaceStream(InputStream inputStream) {
		this.stream = new DataInputStream(inputStream);
		this.streamState = new PacketStreamState();
		this.majorVersion = FileHeader.defaultMajorVersion;
		this.minorVersion = FileHeader.defaultMinorVersion;
	}

	/**
	 * Returns a UInt64 value from the stream without repositioning the stream.
	 *
	 * @return A UInt64 value.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public final long peekUInt64() throws IOException {
		this.stream.mark(Long.BYTES);
		try {
			return this.stream.readLong();
		} finally {
			this.stream.reset();
		}
	}

	/**
	 * Returns a bool value from the stream.
	 *
	 * @return A bool value.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public final boolean readBool() throws IOException {
		return readByte() != 0;
	}

	/**
	 * Read a string from the stream.
	 * 
	 * We optimize strings by maintaining a hash table of each unique string we have
	 * seen. Each string is sent with as an integer index into the table. When a new
	 * string is encountered, it's index is followed by the string value.
	 *
	 * @return Returns the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public final String readString() throws IOException {
		return readStringDirect();
	}

	/**
	 * Read an array of strings from the stream.
	 *
	 * @return Returns an array of string values
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public final String[] readStringArray() throws IOException {
		Object[] baseArray = this.stringArrayReader.read(this);
		return Arrays.copyOf(baseArray, baseArray.length, String[].class);
	}

	/**
	 * Returns an Int32 value from the stream.
	 *
	 * @return An Int32 value.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public final int readInt() throws IOException {
		byte firstByte = readByte();
		int result = firstByte & 0x3f;
		int bitShift = 6;
		if ((firstByte & 0x40) != 0) {
			while (true) {
				byte nextByte = readByte();
				result |= (nextByte & 0x7f) << bitShift;
				bitShift += 7;
				if ((nextByte & 0x80) == 0) {
					break;
				}
			}
		}
		if ((firstByte & 0x80) == 0) {
			return result;
		} else {
			return -result;
		}
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.IFieldReader#readPositiveInt()
	 */
	@Override
	public final int readPositiveInt() throws IOException {
		int result = 0;
		int bitShift = 0;
		while (true) {
			byte nextByte = readByte();
			result |= (nextByte & 0x7f) << bitShift;
			bitShift += 7;
			if ((nextByte & 0x80) == 0) {
				return result;
			}
		}
	}

	/**
	 * Returns an Int64 value from the stream.
	 *
	 * @return An Int64 value.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public final long readLong() throws IOException {
		byte firstByte = readByte();
		long result = firstByte & 0x3f;
		int bitShift = 6;
		if ((firstByte & 0x40) != 0) {
			while (true) {
				byte nextByte = readByte();
				result |= (long) (nextByte & 0x7f) << bitShift;
				bitShift += 7;
				if ((nextByte & 0x80) == 0) {
					break;
				}
			}
		}
		if ((firstByte & 0x80) == 0) {
			return result;
		} else {
			return -result;
		}
	}

	/**
	 * Returns a UInt64 value from the stream.
	 *
	 * @return A UInt64 value.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public final long readPositiveLong() throws IOException {
		long result = 0;
		int bitCount = 0;
		while (true) {
			byte nextByte = readByte();
			// Normally, we are reading 7 bits at a time.
			// But once we've read 8*7=56 bits, if we still
			// have more bits, there can at most be 8 bits
			// so we read all 8 bits for that last byte.
			if (bitCount < 56) {
				result |= ((long) nextByte & 0x7f) << bitCount;
				bitCount += 7;
				if ((nextByte & 0x80) == 0) {
					break;
				}
			} else {
				result |= ((long) nextByte & 0xff) << 56;
				break;
			}
		}
		return result;
	}

	/**
	 * Returns a double value from the stream.
	 *
	 * @return A double value.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public final double readDouble() throws IOException {
		long bits = 0;
		int bitCount = 0;
		while (true) {
			byte nextByte = readByte();
			// Normally, we are reading 7 bits at a time.
			// But once we've read 8*7=56 bits, if we still
			// have more bits, there can at most be 8 bits
			// so we read all 8 bits for that last byte.
			if (bitCount < 56) {
				bits = (bits << 7) | ((long) nextByte & 0x7f);
				bitCount += 7;
				if ((nextByte & 0x80) == 0) {
					bits <<= 64 - bitCount;
					break;
				}
			} else {
				bits = (bits << 8) | ((long) nextByte & 0xff);
				break;
			}
		}
		return Double.longBitsToDouble(bits);
	}

	/**
	 * Returns a Duration value from the stream.
	 *
	 * @return A double value.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public final Duration readDuration() throws IOException {
		return TimeConversion.durationOfTicks(readPositiveLong());
	}

	/**
	 * Returns a DateTime value from the stream.
	 *
	 * @return A DateTime value.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public final LocalDateTime readDateTime() throws IOException {
		OffsetDateTime trueDateTime = readDateTimeOffset();
		return trueDateTime.toLocalDateTime();
	}

	/**
	 * Returns a DateTimeOffset value from the stream.
	 *
	 * @return A DateTimeOffset value.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public final OffsetDateTime readDateTimeOffset() throws IOException {

		OffsetDateTime timestamp;
		DateTimeEncoding encoding;
		long deltaTicks;
		long factor;
		int offsetMinutes = readInt(); // the time zone offset the time was written in

		encoding = DateTimeEncoding.forValue(readByte());
		deltaTicks = readPositiveLong(); // easier to just read the next value in one place

		switch (encoding) {
		case RAW_TICKS:
			// timestamp by absolute ticks, but don't reset ReferenceTime
			timestamp = TimeConversion.fromEpochTicks(deltaTicks,
					ZoneOffset.ofTotalSeconds((int) TimeUnit.MINUTES.toSeconds(offsetMinutes)));
			return timestamp; // We're done
		case NEW_REFERENCE:
			// timestamp by absolute ticks, and also set ReferenceTime
			timestamp = TimeConversion.fromEpochTicks(deltaTicks,
					ZoneOffset.ofTotalSeconds((int) TimeUnit.MINUTES.toSeconds(offsetMinutes)));
			this.streamState.setReferenceTime(timestamp.toLocalDateTime());
			return timestamp; // We're done
		default:
			// if it's later or earlier ticks we do nothing - that's handled below and we
			// throw an exception there
			// if it isn't one of the ones we want.
			break;
		}

		// At this point encoding must be DateTimeEncoding.LaterTicksNet or higher,
		// so the 1's bit indicates the sign of the offset. Do the adjustment here.
		if ((encoding.getValue() & 0x01) != 0) {
			deltaTicks = -deltaTicks;
		}

		// the .NET implementation supported multiple factors but we don't use that
		// capability, so just use factor 1.
		switch (encoding) {
		case LATER_TICKS_NET:
		case EARLIER_TICKS_NET:
			factor = 1;
			break;
		default:
			throw new IndexOutOfBoundsException();
		}

		deltaTicks *= factor; // adjust offset by determined factor

		// create a new timestamp by using our offset from reference ticks and our new
		// time zone value.
		LocalDateTime referenceTime = this.streamState.getReferenceTime();
		if (referenceTime == null)
			throw new GibraltarSerializationException("No reference time has been set and a relative ticks value was serialized");

		timestamp = OffsetDateTime.of(
				LocalDateTime.from(referenceTime.plusNanos(Math.multiplyExact(deltaTicks, 100))),
				ZoneOffset.ofTotalSeconds((int) TimeUnit.MINUTES.toSeconds(offsetMinutes)));

		return timestamp;
	}

	/**
	 * Returns a Guid value from the stream.
	 *
	 * @return A Guid value.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public final UUID readGuid() throws IOException {
		byte[] netBytes = readBytes(16);
		byte[] javaBytes = new byte[16];

		javaBytes[0] = netBytes[3];
		javaBytes[1] = netBytes[2];
		javaBytes[2] = netBytes[1];
		javaBytes[3] = netBytes[0];
		javaBytes[4] = netBytes[5];
		javaBytes[5] = netBytes[4];
		javaBytes[6] = netBytes[7];
		javaBytes[7] = netBytes[6];

		for (int index = 8; index < 16; index++) {
			javaBytes[index] = netBytes[index];
		}

		// Sourced from UUID.java
		long msb = 0;
		long lsb = 0;
		for (int i = 7; i >= 0; i--)
			msb = (msb << 8) | (javaBytes[i] & 0xff);
		for (int i = 15; i >= 8; i--)
			lsb = (lsb << 8) | (javaBytes[i] & 0xff);
		//////

		return new UUID(msb, lsb);
	}

	/**
	 * Returns a field value from the stream.
	 *
	 * @return An object value holding a value (see FieldType.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public final Object readField() throws IOException {
		FieldType nextFieldType = readFieldType();
		return readField(nextFieldType);
	}

	/**
	 * Returns a field value from the stream for the provided field type.
	 *
	 * @param fieldType The field type of the next field in the stream to read
	 * @return An object with the value that was read.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public final Object readField(FieldType fieldType) throws IOException {
		switch (fieldType) {
		case BOOL:
			return readBool();
		case STRING:
			return readString();
		case INT:
			return readInt();
		case LONG:
			return readLong();
		case DOUBLE:
			return readDouble();
		case DURATION:
			return readDuration();
		case DATE_TIME:
			return readDateTime();
		case GUID:
			return readGuid();
		case DATE_TIME_OFFSET:
			return readDateTimeOffset();
		default:
			throw new GibraltarException(String.format(
					"There is no known field type for %1$s, this most likely indicates a corrupt file or serialization defect.",
					fieldType));
		}
	}

	/**
	 * Returns a FieldType enum value from the stream.
	 *
	 * @return A FieldType enum value
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final FieldType readFieldType() throws IOException {
		return FieldType.forValue(readInt());
	} // yep, not read positive.

	/**
	 * Read an array of FieldType enum values from the stream.
	 *
	 * @return Returns an array of FieldType enum values
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final FieldType[] readFieldTypeArray() throws IOException {
		int length = readPositiveInt();
		FieldType[] array = new FieldType[length];
		for (int i = 0; i < length; i++) {
			array[i] = readFieldType();
		}
		return array;
	}

	/**
	 * Helper method to read a single byte from the underlying stream
	 * 
	 * 
	 * NOTE: In DEBUG builds, this method will throw an exception if the a byte
	 * cannot be read (past end-of-file). Otherwise, it returns zero.
	 *
	 * @return The next byte in the stream.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private byte readByte() throws IOException {
		return this.stream.readByte();
	}

	/**
	 * Read bytes.
	 *
	 * @param length the length
	 * @return the byte[]
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private byte[] readBytes(int length) throws IOException {
		byte[] bytes = new byte[length];
		this.stream.read(bytes, 0, length);
		return bytes;
	}

	/**
	 * Helper method to read a string from the underlying stream.
	 * 
	 * NOTE: In DEBUG builds, this method will throw an exception if a valid string
	 * is not read completely. Otherwise, it returns null.
	 *
	 * @return A string read with the expected encoding
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private String readStringDirect() throws IOException {
		int length = readPositiveInt();

		// Handle the possibility of an empty string
		if (length == 0) {
			return "";
		}

		byte[] bytes = new byte[length];
		this.stream.read(bytes, 0, length);

		// Handle the possibility of a null string under _MajorVersion > 1
		if (length == 1 && bytes[0] == 0) {
			return null;
		}

		// The rest of this method handles a non-null, non-empty string to be interned
		// in the StringReference table

		String value = this.encoding.decode(ByteBuffer.wrap(bytes)).toString();

		return value;
	}
}