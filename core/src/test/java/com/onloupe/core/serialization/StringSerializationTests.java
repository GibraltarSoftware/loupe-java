package com.onloupe.core.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringSerializationTests {

	@Test
	public final void testProtocolVersion2() throws IOException, NoSuchMethodException, SecurityException {
		// Test with protocol version 2.0
		FieldWriter writer = new FieldWriter(new ByteArrayOutputStream(), 2, 0);

		// Test that a null string takes only one byte
		writer.write((String) null);
		Assertions.assertEquals(2, writer.getLength(), "Expected position ");

		// Test that an empty string takes 2 bytes
		writer.write("");
		Assertions.assertEquals(3, writer.getLength(), "Expected position ");

		// Test that passing a single character string takes 2 bytes
		writer.write(" ");
		Assertions.assertEquals(5, writer.getLength(), "Expected position ");

		// Test that passing a two character string takes 3 bytes
		writer.write("Hi");
		Assertions.assertEquals(8, writer.getLength(), "Expected position ");

		// Test that 5 characters takes 6 bytes
		writer.write("Hello");
		Assertions.assertEquals(14, writer.getLength(), "Expected position ");

		// Test that a second occurrence still takes 6 bytes
		writer.write("Hello");
		Assertions.assertEquals(20, writer.getLength(), "Expected position ");

		// Likewise, a second occurrence of an earlier string still takes a much space
		writer.write("Hi");
		Assertions.assertEquals(23, writer.getLength(), "Expected position ");

		// Verify that the strings read back are the same as those written
		IFieldReader reader = new FieldReader(new ByteArrayInputStream(writer.toArray()), 2, 0);
		Assertions.assertNull(reader.readString());
		Assertions.assertEquals("", reader.readString());
		Assertions.assertEquals(" ", reader.readString());
		Assertions.assertEquals("Hi", reader.readString());
		Assertions.assertEquals("Hello", reader.readString());
		Assertions.assertEquals("Hello", reader.readString());
		Assertions.assertEquals("Hi", reader.readString());
	}
}