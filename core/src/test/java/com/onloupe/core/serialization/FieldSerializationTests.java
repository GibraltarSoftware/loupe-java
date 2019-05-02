package com.onloupe.core.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FieldSerializationTests {
	private static void testCheckInt(int expectedValue, int expectedSize)
			throws NoSuchMethodException, SecurityException, IOException {
		IFieldWriter writer = new FieldWriter(new ByteArrayOutputStream(), 2, 2);
		writer.write(expectedValue);

		IFieldReader reader = new FieldReader(writer.toArray());
		int actualValue = reader.readInt();

		Assertions.assertEquals(expectedValue, actualValue, "Expected deserialized value ");
		if (expectedSize > 0) {
			Assertions.assertEquals(expectedSize, writer.getLength(),
					String.format("Unexpected size for %s ", expectedValue));
		}
	}

	private static void testCheckLong(long expectedValue, int expectedSize)
			throws NoSuchMethodException, SecurityException, IOException {
		IFieldWriter writer = new FieldWriter();
		writer.write(expectedValue);

		IFieldReader reader = new FieldReader(writer.toArray());
		long actualValue = reader.readLong();

		Assertions.assertEquals(expectedValue, actualValue, "Expected deserialized value ");
		if (expectedSize > 0) {
			Assertions.assertEquals(expectedSize, writer.getLength(),
					String.format("Unexpected size for %s ", expectedValue));
		}
	}

	private static void testCheckDouble(double expectedValue, int expectedSize)
			throws IOException, NoSuchMethodException, SecurityException {
		IFieldWriter writer = new FieldWriter();
		writer.write(expectedValue);

		IFieldReader reader = new FieldReader(writer.toArray());
		double actualValue = reader.readDouble();

		Assertions.assertEquals(expectedValue, actualValue, "Expected deserialized value ");
		Assertions.assertEquals(expectedSize, writer.getLength(),
				String.format("Unexpected size for %s ", expectedValue));
	}

	@Test
	public final void testCheckString() throws IOException, NoSuchMethodException, SecurityException {
		IFieldWriter writer = new FieldWriter();
		writer.write((String) null);
		Assertions.assertEquals(2, writer.getLength(), "Incorrect position");

		writer.write("");
		Assertions.assertEquals(3, writer.getLength(), "Incorrect position");

		writer.write(" ");
		Assertions.assertEquals(5, writer.getLength(), "Incorrect position");

		writer.write("Hello");
		Assertions.assertEquals(11, writer.getLength(), "Incorrect position");

		writer.write("Hello");
		Assertions.assertEquals(17, writer.getLength(), "Incorrect position");

		writer.write("Hi");
		Assertions.assertEquals(20, writer.getLength(), "Incorrect position");

		IFieldReader reader = new FieldReader(writer.toArray());
		Assertions.assertEquals(null, reader.readString(), "Strings do not match.");
		Assertions.assertEquals("", reader.readString(), "Strings do not match.");
		Assertions.assertEquals(" ", reader.readString(), "Strings do not match.");
		Assertions.assertEquals("Hello", reader.readString(), "Strings do not match.");
		Assertions.assertEquals("Hello", reader.readString(), "Strings do not match.");
		Assertions.assertEquals("Hi", reader.readString(), "Strings do not match.");
	}

	@Test
	public final void testCheckInts() throws NoSuchMethodException, SecurityException, IOException {
		for (int i = 0; i < 1024 * 1024; i++) {
			testCheckInt(i, -1);
			testCheckLong(i, -1);
		}
	}

	/**
	 * For optimized int32 smaller values take less space
	 * 
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IOException
	 */
	@Test
	public final void testCheckInt32() throws NoSuchMethodException, SecurityException, IOException {
		// 0x00000000 - 0x0000003F (0 to 63) takes 1 byte
		// 0x00000040 - 0x00001FFF (64 to 8,191) takes 2 bytes
		// 0x00002000 - 0x000FFFFF (8,192 to 1,048,575) takes 3 bytes
		// 0x00100000 - 0x07FFFFFF (1,048,576 to 134,217,727) takes 4 bytes
		// 0x08000000 - 0x7FFFFFFF (134,217,728 to 2,147,483,647) takes 5 bytes
		//
		// 0x80000000 - 0xF8000000 (-2,147,483,648 to -134,217,728) takes 5 bytes
		// 0xF8000001 - 0xFFF00000 (-134,217,727 to -1,048,576) takes 4 bytes
		// 0xFFF00001 - 0xFFFFE000 (-1,048,575 to -8192) takes 3 bytes
		// 0xFFFFE001 - 0xFFFFFFC0 (-8191 to -64) takes 2 bytes
		// 0xFFFFFFC1 - 0xFFFFFFFF (-63 to -1) takes 1 byte
		testCheckInt(0, 1);
		testCheckInt(63, 1);

		testCheckInt(64, 2);
		testCheckInt(8191, 2);

		testCheckInt(8192, 3);
		testCheckInt(1048575, 3);

		testCheckInt(1048576, 4);
		testCheckInt(134217727, 4);

		testCheckInt(134217728, 5);
		testCheckInt(2147483647, 5); // int.MaxValue
		testCheckInt(-2147483648, 5); // int.MinValue
		testCheckInt(-134217728, 5);

		testCheckInt(-134217727, 4);
		testCheckInt(-1048576, 4);

		testCheckInt(-1048575, 3);
		testCheckInt(-8192, 3);

		testCheckInt(-8191, 2);
		testCheckInt(-64, 2);

		testCheckInt(-63, 1);
		testCheckInt(-1, 1);
	}

	/**
	 * For optimized Int64 smaller values take less space
	 * 
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IOException
	 */
	@Test
	public final void testCheckInt64() throws NoSuchMethodException, SecurityException, IOException {
		// 0x00000000 - 0x0000003F (0 to 63) takes 1 byte
		// 0x00000040 - 0x00001FFF (64 to 8,191) takes 2 bytes
		// 0x00002000 - 0x000FFFFF (8,192 to 1,048,575) takes 3 bytes
		// 0x00100000 - 0x07FFFFFF (1,048,576 to 134,217,727) takes 4 bytes
		// 0x08000000 - 0x7FFFFFFF (134,217,728 to 2,147,483,647) takes 5 bytes
		//
		// 0x80000000 - 0xF8000000 (-2,147,483,648 to -134,217,728) takes 5 bytes
		// 0xF8000001 - 0xFFF00000 (-134,217,727 to -1,048,576) takes 4 bytes
		// 0xFFF00001 - 0xFFFFE000 (-1,048,575 to -8192) takes 3 bytes
		// 0xFFFFE001 - 0xFFFFFFC0 (-8191 to -64) takes 2 bytes
		// 0xFFFFFFC1 - 0xFFFFFFFF (-63 to -1) takes 1 byte
		//
		testCheckLong(0, 1);
		testCheckLong(63, 1);

		testCheckLong(64, 2);
		testCheckLong(8191, 2);

		testCheckLong(8192, 3);
		testCheckLong(1048575, 3);

		testCheckLong(1048576, 4);
		testCheckLong(134217727, 4);

		testCheckLong(134217728, 5);
		testCheckLong(2147483647, 5); // int.MaxValue
		testCheckLong(-2147483648, 5); // int.MinValue
		testCheckLong(-134217728, 5);

		testCheckLong(-134217727, 4);
		testCheckLong(-1048576, 4);

		testCheckLong(-1048575, 3);
		testCheckLong(-8192, 3);

		testCheckLong(-8191, 2);
		testCheckLong(-64, 2);

		testCheckLong(-63, 1);
		testCheckLong(-1, 1);
		testCheckLong(0x7fffffffffffffffL, 10);
		testCheckLong(-0x7fffffffffffffffL + 1, 10);
	}

	@Test
	public final void testCheckDouble() throws IOException, NoSuchMethodException, SecurityException {
		testCheckDouble(0, 1);
		testCheckDouble(1, 2);
		testCheckDouble(2, 1);
		testCheckDouble(5, 2);
		testCheckDouble(10, 2);
		testCheckDouble(100, 3);
		testCheckDouble(1234, 3);
		testCheckDouble(9876543210.0, 7);
		testCheckDouble(0.9876543210, 9);
		testCheckDouble(0.123, 9);
		testCheckDouble(0.125, 2);
		testCheckDouble(2.5, 2);
		testCheckDouble(0.25, 2);
		testCheckDouble(3.14, 9);
		testCheckDouble(0.99999, 9);
		testCheckDouble(Double.MAX_VALUE, 9);
		testCheckDouble(-Double.MAX_VALUE, 9);
	}

	@Test
	public final void testCheckTimeSpan() throws NoSuchMethodException, SecurityException, IOException {
		Random random = new Random();
		Iterator<Long> testValues = random.longs(50).iterator();
		while (testValues.hasNext()) {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			FieldWriter writer = new FieldWriter(buffer, 2, 2);

			Duration expected = Duration.ofNanos(Math.abs(testValues.next()));
			writer.write(expected);

			IFieldReader reader = new FieldReader(writer.toArray());
			Duration actual = reader.readDuration();

			// the serializer converts to ticks, so we need to round the last two decimal
			// places to zero.
			long expectedRoundedNanos = BigDecimal.valueOf(expected.toNanos()).movePointLeft(2).longValue() * 100;
			Assertions.assertEquals(expectedRoundedNanos, actual.toNanos(), "Durations are not equal.");
		}
	}

	@Test
	public final void testCheckDateTime() throws NoSuchMethodException, SecurityException, IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		IFieldWriter writer = new FieldWriter(buffer, 2, 2);

		LocalDateTime referenceTime = LocalDateTime.now(); // we convert to UTC during serialization, we want local
															// time.
		writer.write(referenceTime);
		Assertions.assertEquals(12, buffer.size());
		// add one tick
		writer.write(referenceTime.plusNanos(100));
		Assertions.assertEquals(16, buffer.size());
		writer.write(referenceTime.plus(50, ChronoUnit.MILLIS));
		Assertions.assertEquals(22, buffer.size());
		writer.write(referenceTime.plusHours(1));
		Assertions.assertEquals(31, buffer.size());
		writer.write(referenceTime.plusDays(1));
		Assertions.assertEquals(40, buffer.size());

		IFieldReader reader = new FieldReader(new ByteArrayInputStream(writer.toArray()));
		Assertions.assertEquals(referenceTime, reader.readDateTime(), "Dates are not equal.");
		Assertions.assertEquals(referenceTime.plusNanos(100), reader.readDateTime(), "Dates are not equal.");
		Assertions.assertEquals(referenceTime.plus(50, ChronoUnit.MILLIS), reader.readDateTime(),
				"Dates are not equal.");
		Assertions.assertEquals(referenceTime.plusHours(1), reader.readDateTime(), "Dates are not equal.");
		Assertions.assertEquals(referenceTime.plusDays(1), reader.readDateTime(), "Dates are not equal.");
	}

	@Test
	public final void testCheckGuid() throws IOException, NoSuchMethodException, SecurityException {
		for (UUID expected : new UUID[] { UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID() }) {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			IFieldWriter writer = new FieldWriter(buffer, 2, 2);
			writer.write(expected);

			IFieldReader reader = new FieldReader(writer.toArray());
			UUID actual = reader.readGuid();
			Assertions.assertEquals(expected, actual, "Expected GUID ");
		}
	}
	
	@Test
	public final void testStringArrayEncoding() throws Exception {
		String[] same = new String[] {"foo", "foo", "foo"};
		String[] different = new String[] {"foo", "bar", "baz"};
		String[] reverse = new String[] {"foo", "foo", "bar", "baz", "bar", "bar"};
		
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		IFieldWriter writer = new FieldWriter(buffer, 2, 2);
		writer.write(same);
		writer.write(different);
		writer.write(reverse);
		
		IFieldReader reader = new FieldReader(writer.toArray());
		Assertions.assertTrue(Arrays.equals(same, reader.readStringArray()), "Same array does not match read value.");
		Assertions.assertTrue(Arrays.equals(different, reader.readStringArray()), "Different array does not match read value.");
		Assertions.assertTrue(Arrays.equals(reverse, reader.readStringArray()), "Reverse array does not match read value.");
	}

}