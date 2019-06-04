package com.onloupe.core.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// TODO: Auto-generated Javadoc
/**
 * A class to provide common wrappers and direct access to low-level file calls.
 */
public final class FileUtils {
	/**
	 * Attempt to open a FileStream while avoiding exceptions.
	 * 
	 * @param fileName     The full-path file name to create or open.
	 * @param options Standard RWS/D
	 * @return A random access file, or null upon failure.
	 */
	public static RandomAccessFile openRandomAccessFile(String fileName, String options) {
		RandomAccessFile randomAccessFile = null;

		try {
			File file = new File(fileName);
			if (file != null && file.isFile()) {
				randomAccessFile = new RandomAccessFile(file, options); // What we're wrapping anyway.
			}
		} catch (SecurityException | IllegalArgumentException | IOException e) {
			randomAccessFile = null;
		}

		return randomAccessFile;
	}

	/**
	 * Delete a file with no exception being thrown. Uses DeleteFile method if not
	 * running under Mono.
	 *
	 * @param file the file
	 * @return true, if successful
	 */
	public static boolean safeDeleteFile(File file) {
		if (file == null)
			return false;
		
	    File[] contents = file.listFiles();
	    if (contents != null) {
	        for (File f : contents) {
	            if (!Files.isSymbolicLink(f.toPath())) {
	                safeDeleteFile(f);
	            }
	        }
	    }
	    return file.delete();
	}

	/**
	 * Safe delete file.
	 *
	 * @param fileName the file name
	 * @return true, if successful
	 */
	public static boolean safeDeleteFile(String fileName) {
		return safeDeleteFile(new File(fileName));
	}
	
	/**
	 * Get a persistent lock on a file without opening it.
	 *
	 * @param fileName     The full-path file name to create or open.
	 * @param mode the mode
	 * @param shared the shared
	 * @return the file lock
	 */
	public static FileLock getFileLock(String fileName, String mode, boolean shared) {
		try (RandomAccessFile file = new RandomAccessFile(new File(fileName), mode)) {
			return file.getChannel().lock(0, Long.MAX_VALUE, shared);
		} catch (java.lang.Exception e) {
			return null;
		}
	}
	
	/**
	 * Gets the base file name.
	 *
	 * @param fileName the file name
	 * @return the base file name
	 */
	public static String getBaseFileName(String fileName) {
		if (TypeUtils.isBlank(fileName))
			return null;
		
	    int index = fileName.lastIndexOf('.');
	    return (index == -1) ? fileName : fileName.substring(0, index);
	}
	
	/**
	 * Gets the file extension.
	 *
	 * @param fileName the file name
	 * @return the file extension
	 */
	public static String getFileExtension(String fileName) {
		if (TypeUtils.isBlank(fileName))
			return null;
		
	    int index = fileName.lastIndexOf(".");
	    return (index == -1) ? null : fileName.substring(index);
	}
	
	/**
	 * Checks if is file newer.
	 *
	 * @param file the file
	 * @param reference the reference
	 * @return true, if is file newer
	 */
	public static boolean isFileNewer(final File file, final File reference) {
		if (file == null || reference == null)
			return false;

		if (!file.exists() || !reference.exists())
			return false;
		
		return file.lastModified() > reference.lastModified();
	}
	
	/**
	 * Gets the path.
	 *
	 * @param path the path
	 * @return the path
	 */
	public static Path getPath(String path) {		
		try {
			return Paths.get(path);
		} catch (Exception e) {
			return null;
		}
	}
}