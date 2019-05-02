package com.onloupe.core.serialization;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.onloupe.core.util.IOUtils;

public class PacketSerializationTests {

	@Test
	public final void CheckWriting1Packet() throws Exception {
		WrapperPacket writtenPacket = new WrapperPacket("Test1", 100);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PacketWriter writer = new PacketWriter(outputStream);
		writer.write(writtenPacket);
		Assertions.assertEquals(108, outputStream.size(), "Expected value ");

		PacketReader reader = new PacketReader(outputStream.toByteArray());
		reader.registerType(WrapperPacket.class);
		IPacket readPacket = reader.read();
		Assertions.assertEquals(writtenPacket, readPacket);

		IOUtils.closeQuietly(writer);
		IOUtils.closeQuietly(reader);
	}

	@Test
	public final void CheckWriting2Packets() throws Exception {
		WrapperPacket packet1 = new WrapperPacket("Test1", 100);
		WrapperPacket packet2 = new WrapperPacket("Test2", 200);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PacketWriter writer = new PacketWriter(outputStream);
		writer.write(packet1);
		Assertions.assertEquals(108, outputStream.size(), "Expected value ");
		writer.write(packet2);
		Assertions.assertEquals(126, outputStream.size(), "Expected value ");

		PacketReader reader = new PacketReader(outputStream.toByteArray());
		reader.registerType(WrapperPacket.class);
		Assertions.assertEquals(packet1, reader.read());
		Assertions.assertEquals(packet2, reader.read());

		IOUtils.closeQuietly(writer);
		IOUtils.closeQuietly(reader);
	}

	@Test
	public final void testPacketCacheTest() throws Exception {

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PacketWriter writer = new PacketWriter(outputStream);

		LogPacket.write("message 1", writer);
		Assertions.assertEquals(141, outputStream.size(), "Expected value ");
		Thread.sleep(50);

		LogPacket.write("message 2", writer);
		Assertions.assertEquals(160, outputStream.size(), "Expected value ");
		Thread.sleep(50);

		LogPacket.write("message 3", writer);
		Assertions.assertEquals(179, outputStream.size(), "Expected value ");

		LogPacket.write("message 1", writer);
		Assertions.assertEquals(198, outputStream.size(), "Expected value ");

		PacketReader reader = new PacketReader(outputStream.toByteArray());
		reader.registerType(WrapperPacket.class);
		reader.registerType(LogPacket.class);
		reader.registerType(ThreadInfo.class);

		ThreadInfo threadInfo = (ThreadInfo) reader.read();
		LogPacket message1 = (LogPacket) reader.read();
		LogPacket message2 = (LogPacket) reader.read();
		LogPacket message3 = (LogPacket) reader.read();
		LogPacket message4 = (LogPacket) reader.read();

		long threadId = Thread.currentThread().getId();
		assert threadId == threadInfo.getThreadId();
		assert threadId == message1.getThreadId();
		assert "message 1".equals(message1.getCaption());
		Assertions.assertFalse(message1.getTimeStamp().isAfter(LocalDateTime.now()));

		assert message2.getThreadId() == threadId;
		assert "message 2".equals(message2.getCaption());
		Assertions.assertFalse(message1.getTimeStamp().isAfter(LocalDateTime.now()));

		assert message3.getThreadId() == threadId;
		assert "message 3".equals(message3.getCaption());
		Assertions.assertFalse(message1.getTimeStamp().isAfter(LocalDateTime.now()));

		assert "message 1".equals(message4.getCaption());
		Assertions.assertFalse(message1.getTimeStamp().isAfter(LocalDateTime.now()));

		IOUtils.closeQuietly(writer);
		IOUtils.closeQuietly(reader);
	}

}