package com.onloupe.core.data;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import com.onloupe.core.serialization.FragmentStorageSummary;
import com.onloupe.core.util.FileUtils;

/**
 * Reads a GLF file
 */
public class GLFReader {
	private FileHeader fileHeader;
	private SessionHeader sessionHeader;
	private boolean sessionStream;

	/**
	 * Details about the storage required for this session fragment
	 */
	private FragmentStorageSummary fragmentStorageSummary;

	public final FragmentStorageSummary getFragmentStorageSummary() {
		return this.fragmentStorageSummary;
	}

	private void setFragmentStorageSummary(FragmentStorageSummary value) {
		this.fragmentStorageSummary = value;
	}

	/**
	 * Create a new GLF reader to operate on the provided stream. The GLFReader then
	 * owns the stream and will dispose it when disposed itself. (Use static
	 * GLFReader.IsGLF() to test a stream without giving it up.)
	 * 
	 * @param file
	 * @throws IOException
	 */
	public GLFReader(RandomAccessFile file) throws IOException {
		FileChannel channel = file.getChannel();
		this.fileHeader = check(channel.map(MapMode.READ_ONLY, 0, FileHeader.HEADER_SIZE));
		this.sessionStream = this.fileHeader != null;

		if (this.sessionStream) {
			// calculate the session header length
			int sessionHeaderLength = this.fileHeader.getDataOffset() - FileHeader.HEADER_SIZE;

			// read the session header into a buffer
			ByteBuffer sessionHeaderBuffer = channel.map(MapMode.READ_ONLY, FileHeader.HEADER_SIZE,
					sessionHeaderLength);

			// read the buffer into the array
			byte[] sessionHeader = new byte[sessionHeaderLength];
			sessionHeaderBuffer.get(sessionHeader);

			this.sessionHeader = new SessionHeader(sessionHeader);
			this.sessionHeader.setHasData(true); // since we're on a file

			setFragmentStorageSummary(new FragmentStorageSummary(this.sessionHeader.getFileStartDateTime(),
					this.sessionHeader.getFileEndDateTime(), file.length()));
		}
	}
	
	public boolean isGLF() {
		return sessionStream;
	}

	/**
	 * Indicates if the specified fileName is an existing, accessible, valid GLF
	 * file.
	 * 
	 * @param fileName The full path to the file in question.
	 * @return
	 */
	public static boolean isGLF(String fileName) {
		try (RandomAccessFile file = FileUtils.openRandomAccessFile(fileName, "r")) {
			return isGLF(file);
		} catch (java.lang.Exception e) {
			// We got an error just trying to verify it, so we surely won't be able to open
			// it for real.
			return false;
		}

	}

	public static boolean isGLF(RandomAccessFile file) throws IOException {
		// file must be at least as long as the header dimensions to proceed
		if (file == null || file.length() < FileHeader.HEADER_SIZE)
			return false;
		
		try (FileChannel channel = file.getChannel()) {
			return check(channel.map(MapMode.READ_ONLY, 0, FileHeader.HEADER_SIZE)) != null;
		}
	}

	/**
	 * Indicates if the provided file stream is for a GLF file.
	 * 
	 * @throws IOException
	 */
	private static FileHeader check(ByteBuffer buffer) throws IOException {
		// first is the stream even big enough?
		if (buffer == null) {
			return null;
		}

		// lets see if it has the right type code
		byte[] fileHeaderRawData = new byte[FileHeader.HEADER_SIZE];
		buffer.get(fileHeaderRawData);

		FileHeader sessionFileHeader = new FileHeader(fileHeaderRawData);

		if (sessionFileHeader.getTypeCode() == FileHeader.GLFTYPE_CODE) {
			return sessionFileHeader;
		}

		return null;
	}

	/**
	 * Returns the major version of the serialization protocol in use
	 */
	public final int getMajorVersion() {
		return this.fileHeader.getMajorVersion();
	}

	/**
	 * Returns the minor version of the serialization protocol in use
	 */
	public final int getMinorVersion() {
		return this.fileHeader.getMinorVersion();
	}

	/**
	 * Indicates if the stream provided to the GLFReader is a valid session stream.
	 */
	public final boolean isSessionStream() {
		return this.sessionStream;
	}

	/**
	 * The file header at the start of the stream.
	 */
	public final FileHeader getFileHeader() {
		return this.fileHeader;
	}

	/**
	 * The session header for the stream
	 */
	public final SessionHeader getSessionHeader() {
		return this.sessionHeader;
	}

}