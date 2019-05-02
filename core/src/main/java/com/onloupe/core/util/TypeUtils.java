package com.onloupe.core.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TypeUtils {

	@SuppressWarnings("rawtypes")
	public static void getSuperInterfaces(Class clazz, Set<Class> superInterfaces) {
		for (Class interfaze : clazz.getInterfaces()) {
			superInterfaces.add(interfaze);
			getSuperInterfaces(interfaze, superInterfaces);
		}
	}

	public static boolean equals(String str1, String str2) {
		return str1 == null ? str2 == null : str1.equals(str2);
	}

	public static boolean isBlank(String str) {
		return CodeConversionHelpers.isNullOrEmpty(str) || CodeConversionHelpers.isNullOrWhiteSpace(str);
	}

	public static boolean isNotBlank(String str) {
		return !isBlank(str);
	}

	public static boolean startsWithIgnoreCase(String str, String prefix) {
		if (str == null || prefix == null)
			return str == prefix;

		if (prefix.length() > str.length())
			return false;

		return str.toUpperCase().startsWith(prefix.toUpperCase());
	}

	public static String mid(String str, int pos, final int len) {
		if (str == null) {
			return null;
		}
		if (len < 0 || pos > str.length()) {
			return "";
		}
		if (pos < 0) {
			pos = 0;
		}
		if (str.length() <= pos + len) {
			return str.substring(pos);
		}
		return str.substring(pos, pos + len);
	}

	public static boolean endsWithIgnoreCase(String str, String suffix) {
		if (str == null || suffix == null)
			return str == suffix;

		if (suffix.length() > str.length())
			return false;

		return str.toUpperCase().endsWith(suffix.toUpperCase());
	}

	public static String trimToEmpty(String str) {
		return str == null ? "" : str.trim();
	}

	public static String trimToNull(String str) {
		return isBlank(str) ? null : str.trim();
	}
	
	public static String trim(String str) {
		return str == null ? null : str.trim();
	}

	public static String strip(String str, final String stripChars) {
		if (CodeConversionHelpers.isNullOrEmpty(str)) {
			return str;
		}
		str = stripStart(str, stripChars);
		return stripEnd(str, stripChars);
	}

	public static String stripStart(final String str, final String stripChars) {
		int strLen;
		if (str == null || (strLen = str.length()) == 0) {
			return str;
		}
		int start = 0;
		if (stripChars == null) {
			while (start != strLen && Character.isWhitespace(str.charAt(start))) {
				start++;
			}
		} else if (stripChars.isEmpty()) {
			return str;
		} else {
			while (start != strLen && stripChars.indexOf(str.charAt(start)) != -1) {
				start++;
			}
		}
		return str.substring(start);
	}

	public static String stripEnd(final String str, final String stripChars) {
		int end;
		if (str == null || (end = str.length()) == 0) {
			return str;
		}

		if (stripChars == null) {
			while (end != 0 && Character.isWhitespace(str.charAt(end - 1))) {
				end--;
			}
		} else if (stripChars.isEmpty()) {
			return str;
		} else {
			while (end != 0 && stripChars.indexOf(str.charAt(end - 1)) != -1) {
				end--;
			}
		}
		return str.substring(0, end);
	}

	public static Throwable getRootCause(Throwable throwable) {
		final List<Throwable> list = new ArrayList<>();
		while (throwable != null && !list.contains(throwable)) {
			list.add(throwable);
			throwable = throwable.getCause();
		}
		return list.isEmpty() ? null : list.get(list.size() - 1);
	}

	public static String getRootCauseMessage(final Throwable th) {
		Throwable root = getRootCause(th);
		root = root == null ? th : root;
		return root.getMessage();
	}

	public static String getStackTrace(final Throwable throwable) {
		try (final StringWriter sw = new StringWriter(); final PrintWriter pw = new PrintWriter(sw, true)) {
			throwable.printStackTrace(pw);
			return sw.getBuffer().toString();
		} catch (Exception e) {
			return null;
		}
	}

	public static int safeInt(Object value) {
		if (value != null) {
			if (Integer.class.isAssignableFrom(value.getClass()))
				return (int) value;
			if (value instanceof Short)
				return ((Short) value).intValue();
			if (value instanceof Long)
				return ((Long) value).intValue();
			if (value instanceof Double)
				return ((Double) value).intValue();
			if (value instanceof Float)
				return ((Float) value).intValue();
			if (value instanceof BigDecimal)
				return ((BigDecimal) value).intValue();
			if (value instanceof BigInteger)
				return ((BigInteger) value).intValue();
		}
		return 0;
	}
	
	public static long safeLong(Object value) {		
		if (value != null) {
			if (Long.class.isAssignableFrom(value.getClass()))
				return (long) value;
			if (value instanceof Short)
				return ((Short) value).longValue();
			if (value instanceof Integer)
				return ((Integer) value).longValue();
			if (value instanceof Double)
				return ((Double) value).longValue();
			if (value instanceof Float)
				return ((Float) value).longValue();
			if (value instanceof BigDecimal)
				return ((BigDecimal) value).longValue();
			if (value instanceof BigInteger)
				return ((BigInteger) value).longValue();
		}
		return 0L;
	}
	
	public static double safeDouble(Object value) {		
		if (value != null) {
			if (Double.class.isAssignableFrom(value.getClass()))
				return (double) value;
			if (value instanceof Short)
				return ((Short) value).doubleValue();
			if (value instanceof Integer)
				return ((Integer) value).doubleValue();
			if (value instanceof Long)
				return ((Long) value).doubleValue();
			if (value instanceof Float)
				return ((Float) value).doubleValue();
			if (value instanceof BigDecimal)
				return ((BigDecimal) value).doubleValue();
			if (value instanceof BigInteger)
				return ((BigInteger) value).doubleValue();
		}
		return 0d;
	}

	public static UUID safeUUID(Object value) {
		return (value instanceof UUID) ? (UUID)value : new UUID(0,0);
	}
	
	/**
	 * This method takes a java type and returns the FQCN of the corresponding type
	 * in .NET. It is necessary for us to bind data types in a way that the .NET desktop
	 * and server side applications can understand and deserialize.
	 * 
	 * @param type
	 * @return
	 */
	public static String getNetType(Class type) {	
		if (type != null) {
			if (String.class.isAssignableFrom(type))
				return "System.String";
			if (Boolean.class.isAssignableFrom(type))
				return "System.Boolean";
			if (Short.class.isAssignableFrom(type))
				return "System.Int16";
			if (Integer.class.isAssignableFrom(type))
				return "System.Int32";
			if (Long.class.isAssignableFrom(type))
				return "System.Int64";
			if (Double.class.isAssignableFrom(type)
					|| Float.class.isAssignableFrom(type))
				return "System.Double";
			if (UUID.class.isAssignableFrom(type))
				return "System.Guid";
			if (String[].class.isAssignableFrom(type))
				return "System.String[]";
			if (Duration.class.isAssignableFrom(type))
				return "System.TimeSpan";
			if (LocalDateTime.class.isAssignableFrom(type))
				return "System.DateTime";
			if (OffsetDateTime.class.isAssignableFrom(type))
				return "System.DateTimeOffset";
		}
		throw new IndexOutOfBoundsException("Unsupported type " + type == null ? null : type.getName());
	}
}
