package com.onloupe.core.data;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.onloupe.core.FileSystemTools;
import com.onloupe.core.logging.Log;
import com.onloupe.core.monitor.SessionFileInfo;
import com.onloupe.core.util.PackageStats;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.exception.GibraltarException;
import com.onloupe.model.log.LogMessageSeverity;
import com.onloupe.model.session.SessionStatus;

// TODO: Auto-generated Javadoc
/**
 * A very simple implementation of the Package type for use within the agent
 * 
 * Unlike the full package implementation this form has no index and does not
 * merge session fragments.
 */
public class SimplePackage implements Closeable {
	
	/** The Constant LOG_CATEGORY. */
	private static final String LOG_CATEGORY = "Loupe.Repository.Package";
	
	/** The Constant FRAGMENTS_FOLDER. */
	private static final String FRAGMENTS_FOLDER = "SessionFragments";

	/** The Sessions. */
	private final Map<UUID, SessionFileInfo<ZipEntry>> _Sessions = new HashMap<UUID, SessionFileInfo<ZipEntry>>();

	/** The raw file. */
	private File rawFile;
	
	/** The zip output stream. */
	private ZipOutputStream zipOutputStream;

	/** The caption. */
	private String caption;
	
	/** The description. */
	private String description;

	/**
	 * Create a new, empty package.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public SimplePackage() throws IOException {
		// we are a new package, don't know what we are yet
		setCaption("New Package");

		if (!Log.getSilentMode()) {
			Log.write(LogMessageSeverity.INFORMATION, LOG_CATEGORY, "Creating new package.", null);
		}

		// assigning a temporary directory we'll extract everything into
		this.rawFile = Files.createTempFile("package-", ".zip").toFile();

		FileOutputStream rawFileStream = new FileOutputStream(this.rawFile);
		this.zipOutputStream = new ZipOutputStream(rawFileStream);
	}

	/**
	 * Instantiates a new simple package.
	 *
	 * @param destinationFileNamePath the destination file name path
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public SimplePackage(String destinationFileNamePath) throws IOException {
		this(new File(destinationFileNamePath));
	}

	/**
	 * Create a new, empty package written to the provided file (which will be
	 * overwritten).
	 *
	 * @param destinationFile the destination file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public SimplePackage(File destinationFile) throws IOException {
		// we are a new package, don't know what we are yet
		setCaption("New Package");

		if (!Log.getSilentMode()) {
			Log.write(LogMessageSeverity.INFORMATION, LOG_CATEGORY, "Creating new package.", null);
		}

		this.rawFile = destinationFile;
		this.rawFile.createNewFile();

		FileOutputStream rawFileStream = new FileOutputStream(this.rawFile);
		this.zipOutputStream = new ZipOutputStream(rawFileStream);
	}

	/**
	 * Performs application-defined tasks associated with freeing, releasing, or
	 * resetting unmanaged resources.
	 * 
	 * 
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public final void close() throws IOException {
		if (this.zipOutputStream != null) {
			StringBuilder commentBuilder = new StringBuilder();

			if (TypeUtils.isNotBlank(this.caption)) {
				commentBuilder.append(this.caption);
				commentBuilder.append("\r\n"); // we love our fully compatible with everything CRLF.
			}

			if (TypeUtils.isNotBlank(this.description)) {
				commentBuilder.append(this.description);
				commentBuilder.append("\r\n"); // we love our fully compatible with everything CRLF.
			}

			if (commentBuilder.length() > 0) {
				this.zipOutputStream.setComment(commentBuilder.toString());
			}

			this.zipOutputStream.flush();
			this.zipOutputStream.close();
		}
	}

	/**
	 * Adds the provided session fragment file to the package if it doesn't already
	 * exist.
	 *
	 * @param sessionFile the session file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final void addSession(RandomAccessFile sessionFile) throws IOException {
		GLFReader glfReader = new GLFReader(sessionFile);
		if (!glfReader.isSessionStream()) {
			throw new GibraltarException("The data stream provided is not a valid session data stream.");
		}

		if (!Log.getSilentMode()) {
			Log.write(LogMessageSeverity.VERBOSE, LOG_CATEGORY, "Stream is session file, attempting to load", null);
		}

		SessionHeader sessionHeader = glfReader.getSessionHeader();

		String zipFilePath = generateFragmentPath(sessionHeader.getFileId());

		// Add this stream to our zip archive
		ZipEntry fragmentEntry;

		fragmentEntry = new ZipEntry(zipFilePath);
		fragmentEntry.setSize(sessionFile.length());

		MappedByteBuffer buffer = sessionFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0,
				sessionFile.getChannel().size());
		CRC32 crc = new CRC32();
		crc.update(buffer);

		fragmentEntry.setCrc(crc.getValue());
		fragmentEntry.setMethod(ZipEntry.STORED);
		this.zipOutputStream.putNextEntry(fragmentEntry);

		FileSystemTools.contentPump(Channels.newInputStream(sessionFile.getChannel()), this.zipOutputStream); // actually
																												// pump
																												// the
																												// data
																												// from
																												// the
																												// input
																												// to
																												// the
																												// output
																												// stream.

		this.zipOutputStream.closeEntry();

		addSessionHeaderToIndex(sessionHeader, fragmentEntry);
	}

	/**
	 * The display caption for the package.
	 *
	 * @return the caption
	 */
	public final String getCaption() {
		return this.caption;
	}

	/**
	 * Sets the caption.
	 *
	 * @param value the new caption
	 */
	public final void setCaption(String value) {
		this.caption = value;
	}

	/**
	 * The end user display description for the package.
	 *
	 * @return the description
	 */
	public final String getDescription() {
		return this.description;
	}

	/**
	 * Sets the description.
	 *
	 * @param value the new description
	 */
	public final void setDescription(String value) {
		this.description = value;
	}

	/**
	 * Length.
	 *
	 * @return The length of the package in bytes
	 */
	public long length() {
		return this.rawFile.length();
	}

	/**
	 * The current full path to the package.
	 *
	 * @return the absolute path
	 */
	public final String getAbsolutePath() {
		return this.rawFile.getAbsolutePath();
	}

	/**
	 * Get summary statistics about the sessions in the repository.
	 *
	 * @return the stats
	 */
	public final PackageStats getStats() {
		int sessions = this._Sessions.size();
		int problemSessions = 0;
		int files = 0;

		for (SessionFileInfo<ZipEntry> session : this._Sessions.values()) {
			if ((session.getHeader().getErrorCount() > 0) || (session.getHeader().getCriticalCount() > 0)
					|| (session.getHeader().getStatusName().equals(SessionStatus.CRASHED.toString()))) {
				problemSessions++;
			}

			files += session.getFragments().size();
		}

		return new PackageStats(sessions, problemSessions, files, this.rawFile.length());
	}

	/**
	 * Get the set of all of the session headers in the package.
	 *
	 * @return the sessions
	 */
	public final List<SessionHeader> getSessions() {
		ArrayList<SessionHeader> sessionList = new ArrayList<SessionHeader>(this._Sessions.size());

		for (SessionFileInfo<ZipEntry> session : this._Sessions.values()) {
			sessionList.add(session.getHeader());
		}

		return sessionList;
	}

	/**
	 * Adds the session header to index.
	 *
	 * @param sessionHeader the session header
	 * @param sessionFragment the session fragment
	 */
	private void addSessionHeaderToIndex(SessionHeader sessionHeader, ZipEntry sessionFragment) {
		SessionFileInfo<ZipEntry> sessionFileInfo = this._Sessions.get(sessionHeader.getId());
		if (sessionFileInfo != null) {
			// add this file fragment to the existing session info
			sessionFileInfo.addFragment(sessionHeader, sessionFragment, true);
		} else {
			// create a new session file info - this is the first we've seen this session.
			sessionFileInfo = new SessionFileInfo<ZipEntry>(sessionHeader, sessionFragment, true);
			this._Sessions.put(sessionFileInfo.getId(), sessionFileInfo);
		}
	}

	/**
	 * Generate fragment path.
	 *
	 * @param fileId the file id
	 * @return the string
	 */
	private static String generateFragmentPath(UUID fileId) {
		return String.format("%s/%s.%s", FRAGMENTS_FOLDER, fileId, Log.LOG_EXTENSION);
	}
}