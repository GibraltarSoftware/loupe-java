package com.onloupe.core.serialization;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.onloupe.core.data.BinarySerializer;

public class BinarySerializationTests {
	
	private Random random = new Random();

	@Test
	public final void testGUIDSerialization() throws IOException {
		UUID expected = UUID.randomUUID();
		
		byte[] data = BinarySerializer.serializeValue(expected);
		UUID actual = BinarySerializer.deserializeUUIDValue(new ByteArrayInputStream(data));
		Assertions.assertEquals(expected, actual);
		
		expected = UUID.randomUUID();
		
		data = BinarySerializer.serializeValue(expected);
		actual = BinarySerializer.deserializeUUIDValue(ByteBuffer.wrap(data));
		Assertions.assertEquals(expected, actual);
	}
	
	@Test
	public final void testLongSerialization() throws IOException {
		byte[] zeroExpected = BinarySerializer.serializeValue(0L);
		long zeroActual = BinarySerializer.deserializeLong(new ByteArrayInputStream(zeroExpected));
		Assertions.assertEquals(0L, zeroActual);
		
		Iterator<Long> iterator = random.longs(100).iterator();
		
		while (iterator.hasNext()) {
			long expected = iterator.next();
			byte[] data = BinarySerializer.serializeValue(expected);
			long actual = BinarySerializer.deserializeLong(new ByteArrayInputStream(data));
			Assertions.assertEquals(expected, actual);
		}		
	}
	
	@Test
	public final void testIntSerialization() throws IOException {
		Iterator<Integer> iterator = random.ints(100).iterator();
		
		while (iterator.hasNext()) {
			int expected = iterator.next();
			byte[] data = BinarySerializer.serializeValue(expected);
			int actual = BinarySerializer.deserializeInt(new ByteArrayInputStream(data));
			Assertions.assertEquals(expected, actual);
		}		
	}
	
}
