package com.onloupe.core.serialization;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.onloupe.core.util.IOUtils;

public class GZipTests {
	private static final int PACKET_COUNT = 10000;
	private static final int SYNCHRONOUS_TAIL = (int) (PACKET_COUNT * 0.9);

	// Test1 is the nominal case of a GZip stream opened and closed nicely
	@Test
	public final void test1() throws IOException {
		// Temp file will be automatically deleted as part of dispose
		try (TempFile tempFile = new TempFile()) {
			GZIPOutputStream gzipWriterStream = new GZIPOutputStream(
					Channels.newOutputStream(tempFile.file.getChannel()), true);

			// Write a bunch of packets to the stream
			for (int i = 0; i < PACKET_COUNT; i++) {
				Packet packet = new Packet(i);
				gzipWriterStream.write(packet.getBytes());
			}

			gzipWriterStream.flush();
			
			// Close the stream
			gzipWriterStream.close();

			byte[] bytes = Files.readAllBytes(tempFile.filePath);
			Assertions.assertEquals(25772, bytes.length);

			// Read back the data and verify that it is correct
			try (GZIPInputStream gzipReaderStream = new GZIPInputStream(new ByteArrayInputStream(bytes), 25772)) {
				for (int i = 0; i < PACKET_COUNT; i++) {
					Packet packet = new Packet(gzipReaderStream);
					Assertions.assertEquals(i, packet.number);
				}

				// Verify that we're at the end of the stream
				Assertions.assertEquals(-1, gzipReaderStream.read());
			}
		} catch (Exception e) {
			Assertions.fail(e);
		}
	}

	// Test2 always writes in Sync mode
	@Test
	public final void test2() throws IOException {
		// Temp file will be automatically deleted as part of dispose
		try (TempFile tempFile = new TempFile()) {
			GZIPOutputStream gzipWriterStream = new GZIPOutputStream(
					Channels.newOutputStream(tempFile.file.getChannel()), true);

			// Write a bunch of packets to the stream
			for (int i = 0; i < PACKET_COUNT; i++) {
				Packet packet = new Packet(i);
				gzipWriterStream.write(packet.getBytes(), 0, packet.getBytes().length);
				if ((i + 1) % 1000 == 0) {
					gzipWriterStream.flush();
				}
			}

			gzipWriterStream.flush();

			gzipWriterStream.close();

			byte[] bytes = Files.readAllBytes(tempFile.filePath);
			Assertions.assertEquals(23931, bytes.length);

			// Read back the data and verify that it is correct
			try (GZIPInputStream gzipReaderStream = new GZIPInputStream(new ByteArrayInputStream(bytes), 23931)) {
				for (int i = 0; i < PACKET_COUNT; i++) {
					Packet packet = new Packet(gzipReaderStream);
					Assertions.assertEquals(i, packet.number);
				}

				// Verify that we're at the end of the stream
				Assertions.assertEquals(-1, gzipReaderStream.read());
			} catch (Exception e) {
				Assertions.fail(e);
			}
		} catch (Exception e) {
			Assertions.fail(e);
		}
	}

	public static class TempFile implements Closeable {
		public Path filePath;
		public RandomAccessFile file;

		public TempFile() throws IOException {
			this.filePath = Files.createTempFile("gzipTest-", ".txt");
			this.file = new RandomAccessFile(this.filePath.toString(), "rwd");
		}

		public TempFile(String fileName) throws IOException {
			this(fileName, true);
		}

		public TempFile(String fileName, boolean write) throws IOException {
			this.filePath = Files.createTempDirectory("gzipTests").resolve(fileName);
			this.file = new RandomAccessFile(this.filePath.toString(), write ? "rwd" : "r");
		}

		@Override
		public final void close() throws IOException {
			IOUtils.closeQuietly(file);
			Files.delete(filePath);
		}
	}

	public static class Packet {
		public int number;
		public String value;

		public Packet(int number) {
			this.number = number;
			this.value = "DCBA >>>> " + String.format("%04d", number) + " <<<< ABCD\n";
		}

		public Packet(InputStream stream) throws IOException {
			byte[] buffer = new byte[25];
			stream.read(buffer, 0, 25);
			this.value = new String(buffer, StandardCharsets.UTF_8);
			this.number = Integer.parseInt(this.value.substring(10, 14));
		}

		public final byte[] getBytes() {
			return this.value.getBytes(StandardCharsets.UTF_8);
		}
	}
}