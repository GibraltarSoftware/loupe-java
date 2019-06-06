package com.onloupe.core.data;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

import com.onloupe.agent.SessionSummary;
import com.onloupe.core.serialization.IPacket;
import com.onloupe.core.serialization.PacketWriter;
import com.onloupe.core.util.IOUtils;
import com.onloupe.model.session.SessionStatus;


/**
 * The Class GLFWriter.
 */
public class GLFWriter implements Closeable {
	
	/** The Constant BUFFER_FLUSH_THRESHOLD. */
	private static final int BUFFER_FLUSH_THRESHOLD = 16 * 1024;

	/** The file channel. */
	private FileChannel fileChannel;
	
	/** The output stream. */
	private DataOutputStream outputStream;
	
	/** The packet writer. */
	private PacketWriter packetWriter;
	
	/** The file header. */
	private FileHeader fileHeader;
	
	/** The session summary. */
	private SessionSummary sessionSummary;
	
	/** The session header. */
	private SessionHeader sessionHeader;
	
	/** The auto flush. */
	private boolean autoFlush;
	
	/** The previous buffer size. */
	private int previousBufferSize;

	/**
	 * Initialize the GLF writer for the provided session which has already been
	 * recorded.
	 *
	 * @param file           The file stream to write the session file into (should
	 *                       be empty)
	 * @param sessionSummary This constructor is designed for use with sessions that
	 *                       have already been completed and closed.
	 * @throws NoSuchMethodException the no such method exception
	 * @throws SecurityException the security exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public GLFWriter(RandomAccessFile file, SessionSummary sessionSummary)
			throws NoSuchMethodException, SecurityException, IOException {
		this(file, sessionSummary, FileHeader.defaultMajorVersion, FileHeader.defaultMinorVersion);
	}

	/**
	 * Initialize the GLF writer for the provided session which has already been
	 * recorded.
	 *
	 * @param file           The file stream to write the session file into (should
	 *                       be empty)
	 * @param sessionSummary the session summary
	 * @param majorVersion   Major version of the serialization protocol
	 * @param minorVersion   Minor version of the serialization protocol This
	 *                       constructor is designed for use with sessions that have
	 *                       already been completed and closed.
	 * @throws NoSuchMethodException the no such method exception
	 * @throws SecurityException the security exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public GLFWriter(RandomAccessFile file, SessionSummary sessionSummary, int majorVersion, int minorVersion)
			throws NoSuchMethodException, SecurityException, IOException {
		this(file, sessionSummary, 1, null, majorVersion, minorVersion);
	}

	/**
	 * Initialize the GLF writer for storing information about the current live
	 * session.
	 *
	 * @param file           The file stream to write the session file into (should
	 *                       be empty)
	 * @param sessionSummary the session summary
	 * @param fileSequence the file sequence
	 * @param fileStartTime  Used during initial collection to indicate the real
	 *                       time this file became the active file. The file header
	 *                       is configured with a copy of the session summary,
	 *                       assuming that we're about to make a copy of the
	 *                       session. For live data collection the caller should
	 *                       supply the file start time to reflect the true time
	 *                       period covered by this file.
	 * @throws NoSuchMethodException the no such method exception
	 * @throws SecurityException the security exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public GLFWriter(RandomAccessFile file, SessionSummary sessionSummary, int fileSequence,
			OffsetDateTime fileStartTime) throws NoSuchMethodException, SecurityException, IOException {
		this(file, sessionSummary, fileSequence, fileStartTime, FileHeader.defaultMajorVersion,
				FileHeader.defaultMinorVersion);
	}

	/**
	 * Initialize the GLF writer for storing information about the current live
	 * session.
	 *
	 * @param file           The file stream to write the session file into (should
	 *                       be empty)
	 * @param sessionSummary the session summary
	 * @param fileSequence the file sequence
	 * @param fileStartTime  Used during initial collection to indicate the real
	 *                       time this file became the active file.
	 * @param majorVersion   Major version of the serialization protocol
	 * @param minorVersion   Minor version of the serialization protocol The file
	 *                       header is configured with a copy of the session
	 *                       summary, assuming that we're about to make a copy of
	 *                       the session. For live data collection the caller should
	 *                       supply the file start time to reflect the true time
	 *                       period covered by this file.
	 * @throws NoSuchMethodException the no such method exception
	 * @throws SecurityException the security exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public GLFWriter(RandomAccessFile file, SessionSummary sessionSummary, int fileSequence,
			OffsetDateTime fileStartTime, int majorVersion, int minorVersion)
			throws NoSuchMethodException, SecurityException, IOException {
		this.fileChannel = file.getChannel();
		this.sessionSummary = sessionSummary;
		
		// initialize the stream with the file header and session header
		this.fileHeader = new FileHeader(majorVersion, minorVersion);
		this.sessionHeader = new SessionHeader(sessionSummary);

		// There are two variants of the GLF format: One for a whole session, one for a
		// session fragment.
		if (fileStartTime != null) {
			this.sessionHeader.setFileId(UUID.randomUUID());
			this.sessionHeader.setFileSequence(fileSequence);
			this.sessionHeader.setFileStartDateTime(fileStartTime);
			this.sessionHeader.setFileEndDateTime(this.sessionHeader.getEndDateTime());

			// by default, this is the last file - it won't be if we open another.
			this.sessionHeader.setIsLastFile(true);
		}

		// we need to know how big the session header will be (it's variable sized)
		// before we can figure out the data offset.
		byte[] sessionHeader = this.sessionHeader.rawData();

		// where are we going to start our data block?
		this.fileHeader.setDataOffset(FileHeader.HEADER_SIZE + sessionHeader.length);

		this.fileChannel.write(ByteBuffer.wrap(this.fileHeader.rawData()));
		this.fileChannel.write(ByteBuffer.wrap(sessionHeader));

		this.outputStream = new DataOutputStream(new GZIPOutputStream(Channels.newOutputStream(this.fileChannel), true));

		this.packetWriter = new PacketWriter(this.outputStream, majorVersion, minorVersion);
	}

	/**
	 * Write.
	 *
	 * @param packet the packet
	 * @throws Exception the exception
	 */
	public void write(IPacket packet) throws Exception {
		this.packetWriter.write(packet);

		int currentBufferSize = this.outputStream.size();
		if (this.autoFlush || ((currentBufferSize - previousBufferSize) >= BUFFER_FLUSH_THRESHOLD)) {
			previousBufferSize = currentBufferSize;
			flush();
		}

	}

	/**
	 * Gets the auto flush.
	 *
	 * @return the auto flush
	 */
	public final boolean getAutoFlush() {
		return this.autoFlush;
	}

	/**
	 * Sets the auto flush.
	 *
	 * @param value the new auto flush
	 */
	public final void setAutoFlush(boolean value) {
		this.autoFlush = value;
	}

	/**
	 * Flush.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final void flush() throws IOException {
		updateSessionHeader();
		this.packetWriter.getOutputStream().flush();
	}

	/**
	 * Update the session file with the latest session summary information.
	 *
	 * @param sourceFile the source file
	 * @param updateFile the update file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void updateSessionHeader(GLFReader sourceFile, RandomAccessFile updateFile) throws IOException {
		long originalPosition = updateFile.getFilePointer();
		try {
			updateFile.seek(FileHeader.HEADER_SIZE);
			byte[] header = sourceFile.getSessionHeader().rawData();
			updateFile.write(header, 0, header.length);
		} finally {
			updateFile.seek(originalPosition); // move back to wherever it was
		}
	}

	/**
	 * Gets the session header.
	 *
	 * @return the session header
	 */
	public final SessionHeader getSessionHeader() {
		return this.sessionHeader;
	}

	/**
	 * Update the session file with the latest session summary information.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void updateSessionHeader() throws IOException {
		// The file includes up through now (after all, we're flushing it to disk)
		this.sessionHeader.setEndDateTime(this.sessionSummary.getEndDateTime()); // this is ONLY changing what we
																					// write out
		// in the file & the index, not the main
		// packet...

		if (this.sessionHeader.getHasFileInfo()) {
			this.sessionHeader.setFileEndDateTime(this.sessionSummary.getEndDateTime()); // we convert to UTC
																							// during
			// serialization, we want local
			// time.
		}

		// session status updates are tricky... We don't want the stream to reflect
		// running
		// once closed, but we do want to allow other changes.
		if ((this.sessionSummary.getStatus() == SessionStatus.CRASHED)
				|| (this.sessionSummary.getStatus() == SessionStatus.NORMAL)) {
			this.sessionHeader.setStatusName(this.sessionSummary.getStatus().toString());
		}

		// plus we want the latest statistics
		this.sessionHeader.setMessageCount(this.sessionSummary.getMessageCount());
		this.sessionHeader.setCriticalCount(this.sessionSummary.getCriticalCount());
		this.sessionHeader.setErrorCount(this.sessionSummary.getErrorCount());
		this.sessionHeader.setWarningCount(this.sessionSummary.getWarningCount());

		byte[] header = this.sessionHeader.rawData();
		this.fileChannel.write(ByteBuffer.wrap(header), FileHeader.HEADER_SIZE);
	}

	/**
	 * Performs application-defined tasks associated with freeing, releasing, or
	 * resetting managed resources.
	 * 
	 * 
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public final void close() throws IOException {
		// Call the underlying implementation
		close(false); // Ah, we didn't have a clean end, so don't set the last-file marker
	}

	/**
	 * Close.
	 *
	 * @param isLastFile the is last file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void close(boolean isLastFile) throws IOException {
		// set our session end date in our header - this doesn't imply that we're clean
		// or anything, just that this is the end.
		if (this.sessionSummary.getStatus() == SessionStatus.CRASHED) {
			this.sessionHeader.setStatusName(SessionStatus.CRASHED.toString());
		} else {
			this.sessionHeader.setStatusName(SessionStatus.NORMAL.toString());
		}

		this.sessionHeader.setIsLastFile(isLastFile);

		flush();

		// and we create our own PacketWriter, so handle that, too
		IOUtils.closeQuietly(this.packetWriter);
		IOUtils.closeQuietly(this.outputStream);
	}
}