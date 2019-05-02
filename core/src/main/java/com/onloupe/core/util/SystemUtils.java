package com.onloupe.core.util;

import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class SystemUtils {
	private static final BigDecimal MEGABYTE = BigDecimal.valueOf(1024L * 1024L);
	private static final RuntimeMXBean runtimeManager = ManagementFactory.getRuntimeMXBean();
	private static final boolean inDebugMode;
	
	private static final Map<String, String> propertyCache = new HashMap<String, String>();
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

	public static Path getLinuxCommonApplicationLogFolder() {
		// /var/log is the dedicated location for system logging, but several
		// third-party utilities use it too.
		return FileUtils.getPath("/var/log");
    }

	public static Path getLinuxLocalApplicationDataFolder() {
		// fairly commonplace for apps running on nix to spawn hidden local folders
		// in the user home dir.
		
		Path path = getUserHome();
		
		// if we can't resolve the user home, we'll drop logs in the default java tmp dir.
		return path != null ? path.resolve(".logs") : getJavaIoTmpDir();
    }
	
	public static Path getWindowsCommonApplicationDataFolder() {
		return FileUtils.getPath(getSystemEnvironmentProperty("ALLUSERSPROFILE"));
    }

	public static Path getWindowsLocalApplicationDataFolder() {
		return FileUtils.getPath(getSystemEnvironmentProperty("LOCALAPPDATA"));
    }
	
	public static Path getUserHome() {
		return FileUtils.getPath(getSystemProperty("user.home"));
    }

	public static Path getJavaIoTmpDir() {
		return FileUtils.getPath(getSystemProperty("java.io.tmpdir"));
	}

	public static Path getJavaHome() {
		return FileUtils.getPath(getSystemProperty("java.home"));
	}
	
	public static String getUserName() {
		return getSystemProperty("user.name");
	}

	public static String getOsArch() {
		return getSystemProperty("os.arch");
	}

	public static String getOsName() {
		return getSystemProperty("os.name");
	}

	public static String getOsVersion() {
		return getSystemProperty("os.version");
	}

	public static String getUserTimezone() {
		return getSystemProperty("user.timezone");
	}

	public static String getJavaVersion() {
		return getSystemProperty("java.version");
	}
	
	/**
	 * Best effort to identify if the host OS is Windows.
	 * @return
	 */
	public static boolean isWindows() {
		return TypeUtils.startsWithIgnoreCase(getOsName(), "Windows");
	}

	/**
	 * Best effort to identify if the host OS is Linux.
	 * @return
	 */
	public static boolean isLinux() {
		return TypeUtils.startsWithIgnoreCase(getOsName(), "Linux");
	}

	/**
	 * Best effort to identify if the host OS is Mac OS X.
	 * @return
	 */
	public static boolean isMacOsX() {
		return TypeUtils.startsWithIgnoreCase(getOsName(), "Mac OS X");
	}

	/**
	 * Get total JVM memory in MB.
	 * @return
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
	 * @return
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
	 * @return
	 */
	public static DisplayMode getDisplayMode() {
		if (GraphicsEnvironment.isHeadless())
			return null;

		return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
	}
	
	public static boolean isInDebugMode() {
		return inDebugMode;
	}
}
