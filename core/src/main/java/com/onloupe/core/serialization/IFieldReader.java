package com.onloupe.core.serialization;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Standard interface for objects that can read individual fields
 */
public interface IFieldReader {
	/**
	 * Returns a UInt64 value from the stream without repositioning the stream
	 * 
	 * @return A UInt64 value.
	 * @throws IOException
	 */
	long peekUInt64() throws IOException;

	/**
	 * Returns a bool value from the stream.
	 * 
	 * @return A bool value.
	 * @throws IOException
	 */
	boolean readBool() throws IOException;

	/**
	 * Read a string from the stream.
	 * 
	 * We optimize strings by maintaining a hash table of each unique string we have
	 * seen. Each string is sent with as an integer index into the table. When a new
	 * string is encountered, it's index is followed by the string value.
	 * 
	 * 
	 * @return Returns the string
	 * @throws IOException
	 */
	String readString() throws IOException;

	/**
	 * Read an array of strings from the stream.
	 * 
	 * @return Returns an array of string values
	 * @throws IOException
	 */
	String[] readStringArray() throws IOException;

	/**
	 * Returns an Int32 value from the stream.
	 * 
	 * @return An Int32 value.
	 * @throws IOException
	 */
	int readInt() throws IOException;

	/**
	 * Returns an Int64 value from the stream.
	 * 
	 * @return An Int64 value.
	 * @throws IOException
	 */
	long readLong() throws IOException;

	/**
	 * Returns a double value from the stream.
	 * 
	 * @return A double value.
	 * @throws IOException
	 */
	double readDouble() throws IOException;

	/**
	 * Returns a Duration value from the stream.
	 * 
	 * @return A Duration value.
	 * @throws IOException
	 */
	Duration readDuration() throws IOException;

	/**
	 * Returns a DateTime value from the stream.
	 * 
	 * @return A DateTime value.
	 * @throws IOException
	 */
	LocalDateTime readDateTime() throws IOException;

	/**
	 * Returns a DateTimeOffset value from the stream.
	 * 
	 * @return A DateTimeOffset value.
	 * @throws IOException
	 */
	OffsetDateTime readDateTimeOffset() throws IOException;

	/**
	 * Returns a Guid value from the stream.
	 * 
	 * @return A Guid value.
	 * @throws IOException
	 */
	UUID readGuid() throws IOException;

	/**
	 * Returns a field value from the stream that was written as an object
	 * 
	 * @return An object value holding a value (see FieldType).
	 * @throws IOException
	 */
	Object readField() throws IOException;

	/**
	 * Returns a field value from the stream for the specified field type
	 * 
	 * @param fieldType The field type of the next field
	 * @return An object value holding a value (see FieldType).
	 * @throws IOException
	 */
	Object readField(FieldType fieldType) throws IOException;

	int readPositiveInt() throws IOException;

	long readPositiveLong() throws IOException;
}