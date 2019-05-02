package com.onloupe.core.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import com.onloupe.core.util.FileUtils;
import com.onloupe.core.util.SystemUtils;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.exception.GibraltarException;

/**
 * Determines the correct physical paths to use for various Gibraltar scenarios
 */
public final class PathManager {
	/**
	 * The subfolder of the selected path used for the repository
	 */
	public static final String REPOSITORY_FOLDER = "Repository";

	/**
	 * The subfolder of the selected path used for local session log collection
	 */
	public static final String COLLECTION_FOLDER = "Local Logs";

	/**
	 * The subfolder of the selected path used for discovery information
	 */
	public static final String DISCOVERY_FOLDER = "Discovery";

	/**
	 * Determine the best path of the provided type for the current user
	 * 
	 * @param pathType The path type to retrieve a path for
	 * @return The best accessible path of the requested type. The common
	 *         application data folder is used if usable then the local application
	 *         data folder as a last resort.
	 */
	public static String findBestPath(PathType pathType) {
		return findBestPath(pathType, null);
	}

	/**
	 * Determine the best path of the provided type for the current user
	 * 
	 * @param pathType      The path type to retrieve a path for
	 * @param preferredPath The requested full path to use if available.
	 * @return The best accessible path of the requested type. If the preferred path
	 *         is usable it is used, otherwise the common application data folder is
	 *         used then the local application data folder as a last resort.
	 */
	public static String findBestPath(PathType pathType, String preferredPath) {
		String bestPath = null;

		// first, if they provided an override path we'll start with that.
		if (TypeUtils.isNotBlank(preferredPath)) {
			bestPath = preferredPath;
			if (!pathIsUsable(bestPath)) {
				// the override path is no good, ignore it.
				bestPath = null;
			}
		}

		if (TypeUtils.isBlank(bestPath)) {
			String pathFolder = pathTypeToFolderName(pathType);

			// First, we want to try to use the all users data directory if this is not the
			// user-repository.
			if (pathType != PathType.REPOSITORY) {
				bestPath = createPath(getCommonApplicationDataPath(), pathFolder);
			}

			// Did we get a good path? If not go to the user's folder.
			if (TypeUtils.isBlank(bestPath)) {
				// nope, we need to switch to the user's LOCAL app data path as our first
				// backup. (not appdata - that may be part of a roaming profile)
				bestPath = createPath(getLocalApplicationDataPath(), pathFolder);
			}
			
			// if all else fails ...
			if (TypeUtils.isBlank(bestPath)) {
				bestPath = SystemUtils.getJavaIoTmpDir().toString();
			}
		}

		return bestPath;
	}

	/**
	 * Find the full path for the provided subfolder name within a special folder,
	 * and make sure it's usable (return null if fails).
	 * 
	 * @return The full path to the requested folder if it is usable, null
	 *         otherwise.
	 */
	private static String createPath(Path basePath, String folderName) {
		String bestPath = basePath != null ? computePath(basePath, folderName) : null;

		return pathIsUsable(bestPath) ? bestPath : null;
	}

	/**
	 * Compute the full path for the provided subfolder name within a special
	 * folder.
	 * 
	 * @return The full path to the requested folder, which may or may not exist.
	 */
	public static String computePath(Path basePath, String folderName) {
		String bestPath = null;

		try {
			bestPath = basePath.resolve("Gibraltar").resolve(folderName).toString();
		} catch (Exception e) {
			if (SystemUtils.isInDebugMode()) {
				e.printStackTrace();
			}
		}
		
		return bestPath;
	}

	/**
	 * Determines if the provided full path is usable for the current user
	 * 
	 * @param path
	 * @return True if the path is usable, false otherwise The path is usable if the
	 *         current user can access the path, create files and write to existing
	 *         files.
	 */
	public static boolean pathIsUsable(String path) {
		//I suppose valid paths are neither null nor blank... so let us start here.
		if (TypeUtils.isBlank(path))
			return false;
		
		// I suck. I can't figure out a way to easily check if we can create a file and
		// write to it other than to... create a file and try to write to it.
		boolean pathIsWritable = true;

		File directoryPath = new File(path);
		
		// create a random file name that won't already exist.
		File file = Paths.get(path).resolve(UUID.randomUUID().toString() + ".txt").toFile();
		
		try {
			// first, we have to make sure the directory exists.
			if (!directoryPath.isDirectory()) {
				// it doesn't - we'll need to create it AND sent the right permissions on it.
				directoryPath.mkdirs();
			}

			try (OutputStreamWriter testFile = new OutputStreamWriter(new FileOutputStream(file))) {
				// OK, we can CREATE a file, can we WRITE to it?
				testFile.write("This is a test file created by Loupe to verify that the directory is writable."
						+ System.lineSeparator());
				testFile.flush();
			}

			// we've written it and closed it, now open it again.
			try (InputStreamReader testFile = new InputStreamReader(new FileInputStream(file))) {
				testFile.read();
			}

			// no exception there, we're good to go. we'll delete it in a minute outside of
			// our pass/fail handler.
		} catch (java.lang.Exception e) {
			// if we can't do it, it's not writable for some reason.
			pathIsWritable = false;
		}

		FileUtils.safeDeleteFile(file);
		return pathIsWritable;
	}

	private static String pathTypeToFolderName(PathType pathType) {
		String pathFolder;
		switch (pathType) {
		case COLLECTION:
			pathFolder = COLLECTION_FOLDER;
			break;
		case REPOSITORY:
			pathFolder = REPOSITORY_FOLDER;
			break;
		case DISCOVERY:
			pathFolder = DISCOVERY_FOLDER;
			break;
		default:
			throw new GibraltarException("The current path type is unknown, indicating a programming error.");
		}

		return pathFolder;
	}

	private static Path getLocalApplicationDataPath() {
		if (SystemUtils.isWindows()) {
			return SystemUtils.getWindowsLocalApplicationDataFolder();
		}
		
		if (SystemUtils.isLinux()) {
			return SystemUtils.getLinuxLocalApplicationDataFolder();
		}
		
		return SystemUtils.getJavaIoTmpDir();
	}

	private static Path getCommonApplicationDataPath() {
		if (SystemUtils.isWindows()) {
			return SystemUtils.getWindowsCommonApplicationDataFolder();
		}
		
		if (SystemUtils.isLinux()) {
			return SystemUtils.getLinuxCommonApplicationLogFolder();
		}
		
		return SystemUtils.getJavaIoTmpDir();
	}
}