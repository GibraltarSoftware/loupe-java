package com.onloupe.core.util;

import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class SystemUtils.
 */
public class SystemUtils {
	
	/** The Constant MEGABYTE. */
	private static final BigDecimal MEGABYTE = BigDecimal.valueOf(1024L * 1024L);
	
	/** The Constant runtimeManager. */
	private static final RuntimeMXBean runtimeManager = ManagementFactory.getRuntimeMXBean();
	
	/** The Constant inDebugMode. */
	private static final boolean inDebugMode;
	
	/** The Constant propertyCache. */
	private static final Map<String, String> propertyCache = new HashMap<String, String>();
	
	/** The Constant environmentPropertyCache. */
	private static final Map<String, String> environmentPropertyCache = new HashMap<String, String>();
	
	static {
		boolean debugModeArgPresent;
		try {
			debugModeArgPresent = runtimeManager.getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
		} catch (Exception e) {
			debugModeArgPresent = false;
		}
		
		inDebugMode = debugModeArgPresent;
	}
	
	/**
	 * Gets the system environment property.
	 *
	 * @param key the key
	 * @return the system environment property
	 */
	public static String getSystemEnvironmentProperty(String key) {
		String value = null;
		if (TypeUtils.isNotBlank(key)) {
			if (!environmentPropertyCache.containsKey(key)) {
				try {
					value = System.getenv(key);
					environmentPropertyCache.put(key, value);
				} catch (Exception e) {
					if (isInDebugMode()) {
						e.printStackTrace();
					}
				}
			} else {
				value = environmentPropertyCache.get(key);
			} 
		}
		return value;
	}
	
	/**
	 * Gets the system property.
	 *
	 * @param key the key
	 * @return the system property
	 */
	public static String getSystemProperty(String key) {
		String value = null;
		if (TypeUtils.isNotBlank(key)) {
			if (!propertyCache.containsKey(key)) {
				try {
					value = System.getProperty(key);
					propertyCache.put(key, value);
				} catch (Exception e) {
					if (isInDebugMode()) {
						e.printStackTrace();
					}
				}
			} else {
				value = propertyCache.get(key);
			} 
		}
		return value;
	}

	/**
	 * Gets the linux common application log folder.
	 *
	 * @return the linux common application log folder
	 */
	public static Path getLinuxCommonApplicationLogFolder() {
		// /var/log is the dedicated location for system logging, but several
		// third-party utilities use it too.
		return FileUtils.getPath("/var/log");
    }

	/**
	 * Gets the linux local application data folder.
	 *
	 * @return the linux local application data folder
	 */
	public static Path getLinuxLocalApplicationDataFolder() {
		// fairly commonplace for apps running on nix to spawn hidden local folders
		// in the user home dir.
		
		Path path = getUserHome();
		
		// if we can't resolve the user home, we'll drop logs in the default java tmp dir.
		return path != null ? path.resolve(".logs") : getJavaIoTmpDir();
    }
	
	/**
	 * Gets the windows common application data folder.
	 *
	 * @return the windows common application data folder
	 */
	public static Path getWindowsCommonApplicationDataFolder() {
		return FileUtils.getPath(getSystemEnvironmentProperty("ALLUSERSPROFILE"));
    }

	/**
	 * Gets the windows local application data folder.
	 *
	 * @return the windows local application data folder
	 */
	public static Path getWindowsLocalApplicationDataFolder() {
		return FileUtils.getPath(getSystemEnvironmentProperty("LOCALAPPDATA"));
    }
	
	/**
	 * Gets the user home.
	 *
	 * @return the user home
	 */
	public static Path getUserHome() {
		return FileUtils.getPath(getSystemProperty("user.home"));
    }

	/**
	 * Gets the java io tmp dir.
	 *
	 * @return the java io tmp dir
	 */
	public static Path getJavaIoTmpDir() {
		return FileUtils.getPath(getSystemProperty("java.io.tmpdir"));
	}

	/**
	 * Gets the java home.
	 *
	 * @return the java home
	 */
	public static Path getJavaHome() {
		return FileUtils.getPath(getSystemProperty("java.home"));
	}
	
	/**
	 * Gets the user name.
	 *
	 * @return the user name
	 */
	public static String getUserName() {
		return getSystemProperty("user.name");
	}

	/**
	 * Gets the os arch.
	 *
	 * @return the os arch
	 */
	public static String getOsArch() {
		return getSystemProperty("os.arch");
	}

	/**
	 * Gets the os name.
	 *
	 * @return the os name
	 */
	public static String getOsName() {
		return getSystemProperty("os.name");
	}

	/**
	 * Gets the os version.
	 *
	 * @return the os version
	 */
	public static String getOsVersion() {
		return getSystemProperty("os.version");
	}

	/**
	 * Gets the user timezone.
	 *
	 * @return the user timezone
	 */
	public static String getUserTimezone() {
		return getSystemProperty("user.timezone");
	}

	/**
	 * Gets the java version.
	 *
	 * @return the java version
	 */
	public static String getJavaVersion() {
		return getSystemProperty("java.version");
	}
	
	/**
	 * Best effort to identify if the host OS is Windows.
	 *
	 * @return true, if is windows
	 */
	public static boolean isWindows() {
		return TypeUtils.startsWithIgnoreCase(getOsName(), "Windows");
	}

	/**
	 * Best effort to identify if the host OS is Linux.
	 *
	 * @return true, if is linux
	 */
	public static boolean isLinux() {
		return TypeUtils.startsWithIgnoreCase(getOsName(), "Linux");
	}

	/**
	 * Best effort to identify if the host OS is Mac OS X.
	 *
	 * @return true, if is mac os X
	 */
	public static boolean isMacOsX() {
		return TypeUtils.startsWithIgnoreCase(getOsName(), "Mac OS X");
	}

	/**
	 * Get total JVM memory in MB.
	 *
	 * @return the total memory
	 */
	public static int getTotalMemory() {
		try {
			return BigDecimal.valueOf(Runtime.getRuntime().totalMemory()).divide(MEGABYTE).intValue();
		} catch (Exception e) {
			if (isInDebugMode()) {
				e.printStackTrace();
			}
			
			return 0;
		}
	}

	/**
	 * Get total JVM memory in MB.
	 *
	 * @return the used memory
	 */
	public static int getUsedMemory() {
		try {
			return (BigDecimal.valueOf(Runtime.getRuntime().totalMemory())
					.subtract(BigDecimal.valueOf(Runtime.getRuntime().freeMemory())))
					.divide(MEGABYTE).intValue();
		} catch (Exception e) {
			if (isInDebugMode()) {
				e.printStackTrace();
			}
			
			return 0;
		}
	}

	/**
	 * Get the default screen device data, if possible.
	 * 
	 * We might (and usually will) not be able to get this. Headless java will not provide it 
	 * and other errors are possible.
	 *
	 * @return the display mode
	 */
	public static DisplayMode getDisplayMode() {
		if (GraphicsEnvironment.isHeadless())
			return null;

		return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
	}
	
	/**
	 * Checks if is in debug mode.
	 *
	 * @return true, if is in debug mode
	 */
	public static boolean isInDebugMode() {
		return inDebugMode;
	}
}
