package com.onloupe.core.serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.onloupe.core.data.FileHeader;
import com.onloupe.core.util.TimeConversion;
import com.onloupe.core.util.TypeUtils;

/**
 * Provides low-level compression of the basic data types we pass over the wire.
 * 
 * This class produces a compressed stream of bytes to be consumed by
 * FieldReader which will reinstate the original stream of basic data types.
 */
public class FieldWriter implements IFieldWriter {
	private ByteArrayOutputStream buffer;
	private int majorVersion;
	private int minorVersion;
	private LocalDateTime referenceTime;

	private final ArrayEncoder<String> stringArrayWriter;

	/**
	 * Initialize a FieldWriter to write to the specified stream using the provided
	 * encoding for strings.
	 * 
	 * @param buffer       Buffer to write data into
	 * @param majorVersion Major version of the serialization protocol
	 * @param minorVersion Minor version of the serialization protocol
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public FieldWriter(ByteArrayOutputStream buffer, int majorVersion, int minorVersion)
			throws NoSuchMethodException, SecurityException {
		this.buffer = buffer;
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
		this.stringArrayWriter = new ArrayEncoder<String>(String.class);
	}

	/**
	 * Initialize a FieldWriter to write to the specified stream using the provided
	 * encoding for strings.
	 * 
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public FieldWriter() throws NoSuchMethodException, SecurityException {
		this(new ByteArrayOutputStream(), FileHeader.defaultMajorVersion, FileHeader.defaultMinorVersion);
	}

	/**
	 * Write an object to the stream as its serializable type
	 * 
	 * @param value The object (or boxed integral value) to write.
	 */
	@Override
	public final void write(Object value) throws Exception {
		// but what type are we going to use?
		FieldType serializedType = PacketDefinition.getSerializableType(value.getClass());
		write(serializedType.getValue()); // cast to Int32 to match ReadFieldType()

		write(value, serializedType);
	}

	/**
	 * Write an object to the stream as its serializable type
	 * 
	 * @param value     The object (or boxed integral value) to write.
	 * @param fieldType The field type to write the value out as.
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@Override
	public final void write(Object value, FieldType fieldType) throws Exception {
		switch (fieldType) {
		case BOOL:
			write(value != null ? (boolean)value : false);
			break;
		case STRING:
			write((String)value);
			break;
		case STRING_ARRAY:
			write((String[])value);
			break;
		case INT:
			write(TypeUtils.safeInt(value));
			break;
		case LONG:
			write(TypeUtils.safeLong(value));
			break;
		case DOUBLE:
			write(TypeUtils.safeDouble(value));
			break;
		case DURATION:
			write((Duration)value);
			break;
		case DATE_TIME:
			write((LocalDateTime)value);
			break;
		case DATE_TIME_OFFSET:
			write((OffsetDateTime)value);
			break;
		case GUID:
			write(TypeUtils.safeUUID(value));
			break;
		default:
			throw new IndexOutOfBoundsException("value");
		}
	}

	/**
	 * Write a bool to the stream.
	 * 
	 * @return A bool value.
	 */
	@Override
	public final void write(boolean value) {
		writeByte(value ? (byte) 1 : (byte) 0);
	}

	/**
	 * Write a string to the stream.
	 *
	 */
	@Override
	public final void write(String value) throws IOException {
		if (value == null) {
			writeByte((byte) 1);
			writeByte((byte) 0);
		} else if (value.length() == 0) {
			writeByte((byte) 0);
		} else {
			byte[] bytes = value.getBytes(java.nio.charset.StandardCharsets.UTF_8); //we always serialize as UTF-8.
			writePositive(bytes.length);
			writeBytes(bytes);
		}
	}

	/**
	 * Write an array of string to the stream.
	 * 
	 * @return An array of string values.
	 * @throws IOException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@Override
	public final void write(String[] array) throws Exception {
		this.stringArrayWriter.write(array, this);
	}

	/**
	 * Stores a 32-bit signed value into the stream using 7-bit encoding.
	 * 
	 * The value is written 7 bits at a time (starting with the least-significant
	 * bits) until there are no more bits to write. The eighth bit of each byte
	 * stored is used to indicate whether there are more bytes following this one.
	 * 
	 */
	@Override
	public final void write(int value) {
		byte firstByte;
		if (value < 0) {
			// If the value is negative, set the sign bit and negate the value
			// to optimize 7-bit encoding
			firstByte = (byte) 0x80;
			value = -value;
		} else {
			firstByte = 0;
		}

		// include the first 6 bits of the value in the first byte
		firstByte |= (byte) (value & 0x3f);
		value >>>= 6;
		if (value == 0) {
			// if the value is in the range [-63..63] we only need to write one byte
			writeByte(firstByte);
		} else {
			// In this case we need to write at least 2 bytes. The second bit of
			// the first byte is used to indicate that more bytes follow.
			firstByte |= 0x40;
			writeByte(firstByte);

			while (value >= 0x80) {
				writeByte((byte) (value | 0x80));
				value >>>= 7;
			}
			writeByte((byte) value);
		}
	}

	/**
	 * Stores a 64-bit signed value into the stream using 7-bit encoding.
	 * 
	 * The value is written 7 bits at a time (starting with the least-significant
	 * bits) until there are no more bits to write. The eighth bit of each byte
	 * stored is used to indicate whether there are more bytes following this one.
	 * 
	 * 
	 * @param value The Int64 value to encode.
	 */
	@Override
	public final void write(long value) {
		byte firstByte;
		if (value < 0) {
			// If the value is negative, set the sign bit and negate the value
			// to optimize 7-bit encoding
			firstByte = (byte) 0x80;
			value = -value;
		} else {
			firstByte = 0;
		}

		firstByte |= (byte) (value & 0x3f);
		value >>>= 6;
		if (value == 0) {
			// if the value is in the range [-32..31] we only need to write one byte
			writeByte(firstByte);
		} else {
			// In this case we need to write at least 2 bytes. The second bit of
			// the first byte is used to indicate that more bytes follow.
			firstByte |= 0x40;
			writeByte(firstByte);

			while (value >= 0x80L) {
				writeByte((byte) (value | 0x80));
				value >>>= 7;
			}
			writeByte((byte) value);
		}
	}

	/**
	 * Stores a 32-bit unsigned value into the stream using 7-bit encoding.
	 * 
	 * The value is written 7 bits at a time (starting with the least-significant
	 * bits) until there are no more bits to write. The eighth bit of each byte
	 * stored is used to indicate whether there are more bytes following this one.
	 * 
	 * 
	 * @param value The int value to encode.
	 */
	@Override
	public final void writePositive(int value) {
		while (value >= 0x80) {
			writeByte((byte) (value | 0x80));
			value >>>= 7;
		}
		writeByte((byte) value);
	}

	/**
	 * Stores a 32-bit unsigned value into the stream using 7-bit encoding.
	 * 
	 * The value is written 7 bits at a time (starting with the least-significant
	 * bits) until there are no more bits to write. The eighth bit of each byte
	 * stored is used to indicate whether there are more bytes following this one.
	 * 
	 * 
	 * @param value The long value to encode.
	 */
	@Override
	public final void writePositive(long value) {
		while (value >= 0x80) {
			writeByte((byte) (value | 0x80));
			value >>>= 7;
		}
		writeByte((byte) value);
	}

	/**
	 * Efficiently encodes a packet length as a variable length byte array using
	 * 7-bit encoding
	 * 
	 * @param length Packet length to be encoded
	 * @return Returns a MemoryStream containing the encoded length
	 */
	public static ByteBuffer writeLength(long length) {
		ByteBuffer buffer = ByteBuffer.allocate(9);

		while (length >= 0x80) {
			buffer.put(((byte) (length | 0x80)));
			length >>>= 7;
		}
		buffer.put((byte) length);

		return buffer;
	}

	/**
	 * Stores a 64-bit double value int the stream in the fewest bytes possible.
	 * 
	 * For many common numbers the bit representation of a double includes lots of
	 * trailing zeros. This creates an opportunity to optimize these values in a
	 * similar way to how we optimize UInt64. The difference is just that in this
	 * case we are interested in the high-order bits whereas with UInt64 we are
	 * interested in the low order bits.
	 * 
	 */
	@Override
	public final void write(double value) throws IOException {
		// First off, convert to bits to make bit-twiddling possible
		long bits = Double.doubleToLongBits(value);

		// For zero, we only need to send a single byte
		if (bits == 0) {
			writeByte((byte) 0);
		} else {
			// We're done if either their are no more bits to send or we've written 8 bytes
			for (int byteCount = 0; byteCount < 8 && bits != 0; byteCount++) {
				// Grab the leftmost 7 bits
				long maskedBits = bits & 0xFE00000000000000L; // KM: This is now signed, I'm hoping it still works right
																// in binary.
				byte nextByte = (byte) (maskedBits >>> 57);
				bits <<= 7;

				// set the high order bit within the byte if more bits still to process
				if (bits != 0) {
					nextByte |= 0x80;
				}

				writeByte(nextByte);
			}
			// After writing 8 7-bit values, we've written 56 bits. So, if
			// we have bits left, we have 8 bits at most, so let's write them.
			if (bits != 0) {
				writeByte((byte) (bits >>> 56));
			}
		}
	}

	/**
	 * Stores a Duration value to the stream
	 */
	@Override
	public final void write(Duration value) {
		writePositive(TimeConversion.durationInTicks(value));
	}

	/**
	 * Stores a DateTime value to the stream
	 */
	@Override
	public final void write(LocalDateTime value) {
		// write it out as a date time offset so we get time offset information
		write(OffsetDateTime.of(value, ZoneId.systemDefault().getRules().getOffset(value)));
	}

	/**
	 * Stores a DateTime value to the stream
	 */
	@Override
	public final void write(OffsetDateTime value) {
		// write out the time zone offset for this date in minutes (because there are
		// some partial hour time zones)
		write((int) TimeUnit.SECONDS.toMinutes(value.getOffset().getTotalSeconds()));

		// On first write, we store the reference time, thereafter,
		// we store DateTime as offset to the reference time
		if (this.referenceTime == null) {
			this.referenceTime = value.toLocalDateTime();
			writeByte((byte) DateTimeEncoding.NEW_REFERENCE.getValue()); // Tell it to set ReferenceTime from this
			writePositive(TimeConversion.epochTicks(value)); // need to take long
		} else {
			Duration delta = Duration.between(this.referenceTime, value.toLocalDateTime());
			long deltaTicks = TimeConversion.durationInTicks(delta);
			if (delta.isNegative()) {
				writeByte((byte) DateTimeEncoding.EARLIER_TICKS_NET.getValue()); // earlier than ReferenceTime
				writePositive(-deltaTicks); // convert negative to absolute value and cast unsigned
			} else {
				writeByte((byte) DateTimeEncoding.LATER_TICKS_NET.getValue()); // later than ReferenceTime
				writePositive(deltaTicks); // confirmed to be non-negative, safe to cast unsigned
			}
		}
	}

	/**
	 * Stores a DateTime value to the stream
	 * 
	 * 
	 */
	public final void writeTimestamp(LocalDateTime value) // change to Timestamp value if we make it?
	{
		// We always use UTC time for consistency
		OffsetDateTime timestamp = value.atOffset(ZoneOffset.UTC);

		// On first write, we store the reference time, thereafter,
		// we store DateTime as offset to the reference time
		if (this.referenceTime == null) {
			this.referenceTime = value;
			write(Duration.ZERO);
			write(TimeConversion.epochTicks(timestamp));
		} else {
			Duration delta = Duration.between(this.referenceTime, timestamp);
			write(TimeConversion.durationInTicks(delta));

			// if we got no delta then we have to gratuitously write out a zero to make sure
			// that
			// we read this back on the other side, because it's going to think we just
			// specified
			// a new reference.
			if (delta.isZero() || delta.isNegative()) {
				write(0L);
			}
		}
	}

	/**
	 * Stores a 128-bit Guid value to the stream
	 */
	@Override
	public final void write(UUID value) throws IOException {
		// .NET's binary format for GUID's is little endian, and done in segments, so we
		// have to exactly emulate that.
		byte[] net = new byte[16];

		// Based on
		// https://stackoverflow.com/questions/5745512/how-to-read-a-net-guid-into-a-java-uuid
		byte[] javaLeastSignificantBytes = toBytes(value.getLeastSignificantBits());
		for (int i = 8; i < 16; i++) {
			net[i] = javaLeastSignificantBytes[i - 8];
		}

		// and now do the swaps that are in different order...
		byte[] javaMostSignificantBytes = toBytes(value.getMostSignificantBits());
		net[3] = javaMostSignificantBytes[0];
		net[2] = javaMostSignificantBytes[1];
		net[1] = javaMostSignificantBytes[2];
		net[0] = javaMostSignificantBytes[3];
		net[5] = javaMostSignificantBytes[4];
		net[4] = javaMostSignificantBytes[5];
		net[6] = javaMostSignificantBytes[7];
		net[7] = javaMostSignificantBytes[6];

		writeBytes(net);
	}

	/**
	 * This is a helper method for unit testing. It only works for the case the
	 * underlying stream is a MemoryStream.
	 * 
	 * @return The stream data as a byte array
	 */
	@Override
	public final byte[] toArray() {
		return this.buffer.toByteArray();
	}

	/**
	 * Helper method to write a single byte to the underlying stream.
	 * 
	 * @param value byte to be written
	 */
	private void writeByte(byte value) {
		this.buffer.write(value);
	}

	/**
	 * Helper method to write a single byte to the underlying stream.
	 * 
	 * @param values byte array to be written
	 */
	private void writeBytes(byte[] values) throws IOException {
		this.buffer.write(values);
	}

	private byte[] toBytes(long value) {
		// FROM:
		// https://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java/29132118#29132118
		return new byte[] { (byte) value, (byte) (value >> 8), (byte) (value >> 16), (byte) (value >> 24),
				(byte) (value >> 32), (byte) (value >> 40), (byte) (value >> 48), (byte) (value >> 56) };
	}

	/**
	 * Number of valid bytes in the underlying ByteArrayOutputStream. Not seekable.
	 * 
	 * @return
	 */
	@Override
	public int getLength() {
		return this.buffer.size();
	}
}