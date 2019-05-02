package com.onloupe.core.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
	 * @param fileName
	 * @return
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

	public static boolean safeDeleteFile(String fileName) {
		return safeDeleteFile(new File(fileName));
	}
	
	/**
	 * Get a persistent lock on a file without opening it.
	 * 
	 * @param fileName     The full-path file name to create or open.
	 * @param creationMode An action to take on files that exist and do not exist
	 * @param fileAccess   Desired access to the object, which can be read, write,
	 *                     or both
	 * @param fileShare    The sharing mode of an object, which can be read, write,
	 *                     both, or none
	 * @return
	 */
	public static FileLock getFileLock(String fileName, String mode, boolean shared) {
		try (RandomAccessFile file = new RandomAccessFile(new File(fileName), mode)) {
			return file.getChannel().lock(0, Long.MAX_VALUE, shared);
		} catch (java.lang.Exception e) {
			return null;
		}
	}
	
	public static String getBaseFileName(String fileName) {
		if (TypeUtils.isBlank(fileName))
			return null;
		
	    int index = fileName.lastIndexOf('.');
	    return (index == -1) ? fileName : fileName.substring(0, index);
	}
	
	public static String getFileExtension(String fileName) {
		if (TypeUtils.isBlank(fileName))
			return null;
		
	    int index = fileName.lastIndexOf(".");
	    return (index == -1) ? null : fileName.substring(index);
	}
	
	public static boolean isFileNewer(final File file, final File reference) {
		if (file == null || reference == null)
			return false;

		if (!file.exists() || !reference.exists())
			return false;
		
		return file.lastModified() > reference.lastModified();
	}
	
	public static Path getPath(String path) {		
		try {
			return Paths.get(path);
		} catch (Exception e) {
			return null;
		}
	}
}