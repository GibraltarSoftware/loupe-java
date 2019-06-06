package com.onloupe.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.IllegalFormatException;
import java.util.Iterator;

import com.onloupe.core.util.FileUtils;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.exception.LoupeFileSystemException;


/**
 * Common routines for manipulating files and directories that extend the .NET
 * framework
 */
public final class FileSystemTools {

	/**
	 * Sanitize the provided directory name by substituting a specified character
	 * for illegal values.
	 * 
	 * @param directoryName The name of the directory to sanitize.
	 * @param replaceChar   The character to substitute for illegal values, must be
	 *                      legal.
	 * @return The sanitized directory name.
	 */
	public static String sanitize(String directoryName, String replaceChar) {
		if (TypeUtils.isBlank(directoryName)) {
			throw new LoupeFileSystemException("No directory provided: " + directoryName);
		}

		return directoryName.replaceAll("[^a-zA-Z0-9\\._]+", replaceChar);
	}

	/**
	 * Ensures that the provided full file name and path is unique, and makes it
	 * unique if necessary.
	 *
	 * @param pathStr the path str
	 * @return A unique path based on the provided path.
	 * @throws Exception the exception
	 */
	public static String makeFileNamePathUnique(String pathStr) throws Exception {
		File fileInfo = new File(pathStr);
		if (!fileInfo.exists()) {
			return pathStr;
		}

		// break up the path into its constituent parts
		String folder = fileInfo.getParentFile().getAbsolutePath();
		String name = FileUtils.getBaseFileName(fileInfo.getName());
		String extension = FileUtils.getFileExtension(fileInfo.getName());

		// get the list of existing files in the current folder based on this file name
		try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(folder), name + "*" + extension)) {
			int startIndex = name.length() + 1;
			int maskLength = startIndex + extension.length() + 1;
			int counter = 0;

			Iterator<Path> paths = dirStream.iterator();
			while (paths.hasNext()) {
				Path path = paths.next();
				if (path.toFile().isFile()) {
					String fileName = path.getFileName().toString();
					int charCount = fileName.length() - maskLength;
					String suffix = fileName.substring(startIndex, startIndex + charCount);
					int thisCounter = Integer.parseInt(suffix);
					if (thisCounter > counter) {
						counter = thisCounter;
					}
				}
			}

			// generate a unique file path
			// we increment counter here because it currently lists the
			// largest counter value we've seen
			counter++;

			String uniqueFile = String.format("%s-%d%s", name, counter, extension);
			return Paths.get(folder).resolve(uniqueFile).toString(); // make sure we return a fully qualified path..
		} catch (IOException | NumberFormatException | IllegalFormatException e) {
			throw e;
		}
	}

	/**
	 * Ensure that the path to the provided fully qualified file name exists,
	 * creating it if necessary.
	 *
	 * @param fileNamePath A fully qualified file name and path.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void ensurePathExists(String fileNamePath) throws IOException {
		try {
			Files.createDirectories(Paths.get(fileNamePath));
		} catch (IOException e) {
			throw e;
		}
	}

	/**
	 * Checks the attributes on the file and clears read-only attributes.
	 *
	 * @param fileNamePath the file name path
	 */
	public static void makeFileWriteable(String fileNamePath) {
		File file = new File(fileNamePath);

		if (!file.exists())
			throw new LoupeFileSystemException("File not found: " + fileNamePath);

		if (!file.canWrite())
			file.setWritable(true);
	}

	/**
	 * Find out the size of the file specified.
	 *
	 * @param fileNamePath the file name path
	 * @return The file size in bytes or 0 if the file is not found.
	 */
	public static long getFileSize(String fileNamePath) {
		long fileSize = 0;
		if ((new File(fileNamePath)).isFile()) {
			File fileInfo = new File(fileNamePath);
			fileSize = fileInfo.length();
		}

		return fileSize;
	}

	/**
	 * Open a temporary file for write and return the open FileStream.
	 *
	 * @param deleteOnClose True to set the file delete on close, false to leave the
	 *                      file after close (caller must delete, rename, etc).
	 * @return An open read-write FileStream.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static File getTempFile(boolean deleteOnClose) throws IOException {
		File file;
		try {
			file = File.createTempFile("LOUPE_", ".tmp");
		} catch (IOException e) {
			throw e;
		}

		if (deleteOnClose)
			file.deleteOnExit();

		return file;
	}

	/**
	 * Open a temporary file for read and write and return the open FileStream which
	 * will delete-on-close.
	 *
	 * @return An open read-write FileStream which is set to delete on close.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static File getTempFile() throws IOException {
		return getTempFile(true);
	}

	/**
	 * Copy the content of a Stream into a temporary file opened for read and write
	 * and return the open FileStream which will delete-on-close.
	 *
	 * @param contentStream An open Stream to copy from its current Position to its
	 *                      end.
	 * @return An open read-write FileStream which is set to delete on close with a
	 *         copy of the contentStream.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static File copyStreamToTempFile(InputStream contentStream) throws IOException {
		File file = getTempFile();
		try {
			Files.copy(contentStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw e;
		}

		file.deleteOnExit();

		return file;
	}

	/**
	 * Pump the contents of one stream from its current Position into another stream
	 * at its current Position up to a maximum byte count.
	 * 
	 * @param source   The Stream to read from, starting at its current Position.
	 * @param dest     The Stream to write into, starting at its current Position.
	 * @param maxCount The maximum count of bytes to copy. Non-positive count will
	 *                 copy nothing.
	 * @return The total number of bytes copied.
	 */
	public static int contentPump(ByteBuffer source, ByteBuffer dest, int maxCount) {
		if (maxCount <= 0) {
			return 0;
		}

		byte[] destArray = new byte[maxCount];
		source.get(destArray, 0, maxCount);
		dest.put(destArray);
		return destArray.length;
	}

	/**
	 * Content pump.
	 *
	 * @param source the source
	 * @param dest the dest
	 * @return the int
	 */
	public static int contentPump(ByteBuffer source, ByteBuffer dest) {
		return contentPump(source, dest, source.capacity());
	}

	/**
	 * Pump the contents of one stream from its current Position into another stream
	 * at its current Position up to a maximum byte count.
	 *
	 * @param source The Stream to read from, starting at its current Position.
	 * @param dest   The Stream to write into, starting at its current Position.
	 * @return The total number of bytes copied.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static int contentPump(InputStream source, OutputStream dest) throws IOException {
		byte[] buffer = new byte[1024 * 8];
		int totalBytes = 0;
		int bytesRead = 0;
		while ((bytesRead = source.read(buffer)) > 0) {
			dest.write(buffer, 0, bytesRead);
			totalBytes += bytesRead;
		}

		return totalBytes;
	}

	/**
	 * Copy the entire contents of one stream into another, preserving the source
	 * Position.
	 * 
	 * @param source The Stream to read from Position 0, restoring its original
	 *               Position when completed.
	 * @param dest   The Stream to write into, which will be advanced by the number
	 *               of bytes written
	 * @return The total number of bytes copied.
	 */
	public static int contentCopy(ByteBuffer source, ByteBuffer dest) {
		return contentCopy(source, dest, false);
	}

	/**
	 * Copy the entire contents of one stream into another, preserving Position.
	 * 
	 * @param source                             The Stream to read from Position 0,
	 *                                           restoring its original Position
	 *                                           when completed.
	 * @param dest                               The Stream to write into which may
	 *                                           optionally be restored to its
	 *                                           original position
	 * @param resetDestinationToOriginalPosition True to reset the destination
	 *                                           stream back to its starting
	 *                                           position
	 * @return The total number of bytes copied.
	 */
	public static int contentCopy(ByteBuffer source, ByteBuffer dest, boolean resetDestinationToOriginalPosition) {
		int originalSourcePosition = source.position();
		int originalDestinationPosition = dest.position();
		source.position(0);

		int totalBytesCopied = contentPump(source, dest);

		source.position(originalSourcePosition);

		if (resetDestinationToOriginalPosition) {
			dest.position(originalDestinationPosition);
		}

		return totalBytesCopied;
	}

	/**
	 * Copy a file to a target location, replacing an existing file if the source is
	 * newer.
	 *
	 * @param sourceFileNamePath the source file name path
	 * @param targetFileNamePath the target file name path
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void copyIfNewer(String sourceFileNamePath, String targetFileNamePath) throws IOException {
		File source = new File(targetFileNamePath);
		File target = new File(targetFileNamePath);
		if (source.isFile() && target.isFile()) {
			if (FileUtils.isFileNewer(source, target)) {
				try {
					Files.copy(source.toPath(), target.toPath(), StandardCopyOption.COPY_ATTRIBUTES,
							StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					throw e;
				}
			}
		} else {
			try {
				Files.copy(source.toPath(), target.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
			} catch (IOException e) {
				throw e;
			}
		}
	}

	/**
	 * Sanitize file name.
	 *
	 * @param fileName the file name
	 * @return the string
	 */
	public static String sanitizeFileName(String fileName) {
		if (TypeUtils.isBlank(fileName))
			throw new LoupeFileSystemException("File name must not be blank.");

		return fileName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
	}

	/**
	 * Creates the random access file.
	 *
	 * @param filename the filename
	 * @param mode the mode
	 * @return the random access file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static RandomAccessFile createRandomAccessFile(String filename, String mode) throws IOException {
		try {
			File file = new File(filename);
			file.createNewFile();
			return new RandomAccessFile(file, mode);
		} catch (FileNotFoundException e) {
			throw e;
		}
	}
}