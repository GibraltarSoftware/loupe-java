package com.onloupe.core.messaging;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import com.onloupe.core.util.FileUtils;
import com.onloupe.core.util.SystemUtils;


/**
 * IP Configuration information for a live stream proxy running on the local
 * computer.
 */
public class LocalServerDiscoveryFile {
	
	/** A file matching filter for discovery files. */
	public static final FileFilter FILE_FILTER = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return FileUtils.getFileExtension(file.getName()).equalsIgnoreCase("gpd");
		}
	};

	/**
	 * Load the specified file as a local server discovery file.
	 *
	 * @param fileNamePath The fully qualified file to load
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public LocalServerDiscoveryFile(String fileNamePath) throws IOException {
		setFileNamePath(fileNamePath);
		try (FileChannel fileStream = FileChannel.open(Paths.get(fileNamePath), StandardOpenOption.READ)) {
			ByteBuffer buffer = fileStream.map(MapMode.READ_ONLY, 0, (Integer.BYTES * 3));
			setProcessId(buffer.getInt());
			setPublisherPort(buffer.getInt());
			setSubscriberPort(buffer.getInt());
		} catch (FileNotFoundException e) {
			throw e;
		}
	}

	/** The TCP port to publish information to (for agents). */
	private int publisherPort;

	/**
	 * Gets the publisher port.
	 *
	 * @return the publisher port
	 */
	public final int getPublisherPort() {
		return this.publisherPort;
	}

	/**
	 * Sets the publisher port.
	 *
	 * @param value the new publisher port
	 */
	private void setPublisherPort(int value) {
		this.publisherPort = value;
	}

	/** The TCP port for subscribers to get information from (for analyst). */
	private int subscriberPort;

	/**
	 * Gets the subscriber port.
	 *
	 * @return the subscriber port
	 */
	public final int getSubscriberPort() {
		return this.subscriberPort;
	}

	/**
	 * Sets the subscriber port.
	 *
	 * @param value the new subscriber port
	 */
	private void setSubscriberPort(int value) {
		this.subscriberPort = value;
	}

	/**
	 * The process Id of the socket proxy host.
	 */
	private int processId;

	/**
	 * Gets the process id.
	 *
	 * @return the process id
	 */
	public final int getProcessId() {
		return this.processId;
	}

	/**
	 * Sets the process id.
	 *
	 * @param value the new process id
	 */
	private void setProcessId(int value) {
		this.processId = value;
	}

	/**
	 * Indicates if the socket proxy host is still running.
	 *
	 * @return true, if is alive
	 */
	public final boolean isAlive() {
		boolean isAlive = (new File(getFileNamePath())).isFile();

		if (isAlive) {
			try (Socket socket = new Socket()) {
				// this will throw an exception if the socket is in use/unavailable.
				socket.bind(
						new InetSocketAddress(InetAddress.getLoopbackAddress().getHostAddress(), getPublisherPort()));
				isAlive = false;
			} catch (IOException e) {
				isAlive = true;
			} catch (Exception e) {
				if (SystemUtils.isInDebugMode()) {
					e.printStackTrace();
				}
			}
		}

		if (!isAlive) {
			// we should try to delete the file; this may be left over from a crashed
			// process..
			FileUtils.safeDeleteFile(getFileNamePath());
		}

		return isAlive;
	}

	/** The fully qualified file name and path for the discovery file. */
	private String fileNamePath;

	/**
	 * Gets the file name path.
	 *
	 * @return the file name path
	 */
	public final String getFileNamePath() {
		return this.fileNamePath;
	}

	/**
	 * Sets the file name path.
	 *
	 * @param value the new file name path
	 */
	private void setFileNamePath(String value) {
		this.fileNamePath = value;
	}
}