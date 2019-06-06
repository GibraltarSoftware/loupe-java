package com.onloupe.core.serialization;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;


/**
 * Standard interface for objects that can write individual serialized fields.
 */
public interface IFieldWriter {

	/**
	 * Write an object to the stream as its serializable type.
	 *
	 * @param value The object (or boxed integral value) to write.
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	void write(Object value) throws IOException, Exception;

	/**
	 * Write an object to the stream as its serializable type.
	 *
	 * @param value     The object (or boxed integral value) to write.
	 * @param fieldType The field type to write the value out as.
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	void write(Object value, FieldType fieldType) throws IOException, Exception;

	/**
	 * Write a bool to the stream.
	 *
	 * @param value the value
	 */
	void write(boolean value);

	/**
	 * Write a string to the stream.
	 *
	 * @param value the value
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	void write(String value) throws IOException;

	/**
	 * Write an array of string to the stream.
	 *
	 * @param array the array
	 * @throws Exception the exception
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	void write(String[] array) throws Exception;

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
	void write(long value);

	/**
	 * Stores a 32-bit unsigned value into the stream using 7-bit encoding.
	 * 
	 * The value is written 7 bits at a time (starting with the least-significant
	 * bits) until there are no more bits to write. The eighth bit of each byte
	 * stored is used to indicate whether there are more bytes following this one.
	 * 
	 * 
	 * @param value The UInt32 value to encode.
	 */
	void write(int value);

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
	void writePositive(int value);

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
	void writePositive(long value);

	/**
	 * Stores a 64-bit double value int the stream in the fewest bytes possible.
	 * 
	 * For many common numbers the bit representation of a double includes lots of
	 * trailing zeros. This creates an opportunity to optimize these values in a
	 * similar way to how we optimize UInt64. The difference is just that in this
	 * case we are interested in the high-order bits whereas with UInt64 we are
	 * interested in the low order bits.
	 *
	 * @param value the value
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	void write(double value) throws IOException;

	/**
	 * Stores a Duration value to the stream.
	 *
	 * @param value the value
	 */
	void write(Duration value);

	/**
	 * Stores a DateTime value to the stream.
	 *
	 * @param value the value
	 */
	void write(LocalDateTime value);

	/**
	 * Stores a DateTime value to the stream.
	 *
	 * @param value the value
	 */
	void write(OffsetDateTime value);

	/**
	 * Stores a 128-bit Guid value to the stream.
	 *
	 * @param value the value
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	void write(UUID value) throws IOException;

	/**
	 * This is a helper method for unit testing. It only works for the case the
	 * underlying stream is a MemoryStream.
	 * 
	 * @return The stream data as a byte array
	 */
	byte[] toArray();

	/**
	 * Number of valid bytes in the underlying ByteArrayOutputStream. Not seekable.
	 *
	 * @return the length
	 */
	public int getLength();
}