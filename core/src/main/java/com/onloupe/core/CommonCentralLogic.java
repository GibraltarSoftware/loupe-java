package com.onloupe.core;

import java.lang.reflect.Method;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.onloupe.agent.logging.MessageSourceProvider;
import com.onloupe.core.util.CodeConversionHelpers;
import com.onloupe.core.util.TypeUtils;

// TODO: Auto-generated Javadoc
/**
 * A static class to hold central logic for common file and OS operations needed
 * by various projects.
 */
public final class CommonCentralLogic {
	
	/** The silent mode. */
	private static boolean silentMode = false;
	
	/** The breakpoint enable. */
	volatile private static boolean breakpointEnable = false; // Can be changed in the debugger

	/** The g session ending. */
	// Basic log implementation.
	volatile private static boolean gSessionEnding; // Session end triggered. False until set to true.
	
	/** The g session ended. */
	volatile private static boolean gSessionEnded; // Session end completed. False until set to true.

	/**
	 * Indicates if the logging system should be running in silent mode (for example
	 * when running in the agent).
	 *
	 * @return the silent mode
	 */
	public static boolean getSilentMode() {
		return silentMode;
	}

	/**
	 * Sets the silent mode.
	 *
	 * @param value the new silent mode
	 */
	public static void setSilentMode(boolean value) {
		silentMode = value;
	}

	/**
	 * A temporary flag to tell us whether to invoke a Debugger.Break() when
	 * Log.DebugBreak() is called.
	 * 
	 * True enables breakpointing, false disables. This should probably be replaced
	 * with an enum to support multiple modes, assuming the basic usage works out.
	 *
	 * @return the break point enable
	 */
	public static boolean getBreakPointEnable() {
		return breakpointEnable;
	}

	/**
	 * Sets the break point enable.
	 *
	 * @param value the new break point enable
	 */
	public static void setBreakPointEnable(boolean value) {
		breakpointEnable = value;
	}

	/**
	 * Reports whether EndSession() has been called to formally end the session.
	 *
	 * @return true, if is session ending
	 */
	public static boolean isSessionEnding() {
		return gSessionEnding;
	}

	/**
	 * Reports whether EndSession() has completed flushing the end-session command
	 * to the log.
	 *
	 * @return true, if is session ended
	 */
	public static boolean isSessionEnded() {
		return gSessionEnded;
	}

	/**
	 * Sets the SessionEnding flag to true. (Can't be reversed once set.)
	 */
	public static void declareSessionIsEnding() {
		gSessionEnding = true;
	}

	/**
	 * Sets the SessionHasEnded flag to true. (Can't be reversed once set.)
	 */
	public static void declareSessionHasEnded() {
		gSessionEnding = true; // This must also be set to true before it can be ended.
		gSessionEnded = true;
	}

	/**
	 * Find message source.
	 *
	 * @return the message source provider
	 */
	public static MessageSourceProvider findMessageSource() {
		return findMessageSource(2);
	}
	
	/**
	 * Find message source.
	 *
	 * @param skipFrames the skip frames
	 * @return the message source provider
	 */
	public static MessageSourceProvider findMessageSource(int skipFrames) {
		return findMessageSource(skipFrames + 1, null, null);
	}

	/**
	 * Find message source.
	 *
	 * @param skipFrames the skip frames
	 * @param exclusions the exclusions
	 * @return the message source provider
	 */
	public static MessageSourceProvider findMessageSource(int skipFrames, Set<String> exclusions) {
		return findMessageSource(skipFrames + 1, null, exclusions);
	}
	
	/**
	 * Find message source.
	 *
	 * @param skipFrames the skip frames
	 * @param throwable the throwable
	 * @return the message source provider
	 */
	public static MessageSourceProvider findMessageSource(int skipFrames, Throwable throwable) {
		return findMessageSource(skipFrames + 1, throwable, null);
	}

	/**
	 * Extracts needed message source information from the current call stack.
	 * 
	 * This is used internally to perform the actual stack frame walk. Constructors
	 * for derived classes all call this method. This constructor also allows the
	 * caller to specify a log message as being of local origin, so Gibraltar stack
	 * frames will not be automatically skipped over when determining the originator
	 * for internally-issued log messages.
	 *
	 * @param skipFrames      The number of stack frames to skip over to find the
	 *                        first candidate to be identified as the source of the
	 *                        log message. (Should generally use 0 if exception
	 *                        parameter is not null.)
	 * @param throwable the throwable
	 * @param exclusions the exclusions
	 * @return The index of the stack frame chosen
	 */
	public static MessageSourceProvider findMessageSource(int skipFrames, Throwable throwable, Set<String> exclusions) {
		MessageSourceProvider provider = new MessageSourceProvider();
		
		if (exclusions == null) {
			exclusions = new HashSet<>();
		}
		
		exclusions.add("com.onloupe");

		try {
			StackTraceElement[] stackTrace;
			
			if (throwable != null) {
				stackTrace = throwable.getStackTrace();
				skipFrames = 0;
			} else {
				stackTrace = Thread.currentThread().getStackTrace();
				skipFrames = skipFrames + 2;
			}

			StackTraceElement selectedFrame = null;
			
			// not excluded, but low likelihood of being meaningful
			StackTraceElement firstTierCandidate = null;
			// excluded loupe internals or appender defined exclusions
			StackTraceElement secondTierCandidate = null;
			// excluded core system, last resort
			StackTraceElement thirdTierCandidate = null;

			// we don't want to walk more than 200 elements.
			int maxTraversalIndex = (stackTrace.length > 200) ? 200 : (stackTrace.length - 1);

			// start at length-1 because the first index is 0, walk backwards.
			for (int stackIndex = skipFrames; stackIndex < maxTraversalIndex; stackIndex++) {
				StackTraceElement stackTraceElement = stackTrace[stackIndex];

				// probably not necessary, but better safe.
				if (stackTraceElement == null)
					continue;

				String elementClassName = stackTraceElement.getClassName();
				String elementMethodName = stackTraceElement.getMethodName();
				
				// probably not necessary, but better safe.
				if (TypeUtils.isBlank(elementClassName) || TypeUtils.isBlank(elementMethodName))
					continue;
				
				if (TypeUtils.startsWithIgnoreCase(elementClassName, "java")
						|| TypeUtils.startsWithIgnoreCase(elementClassName, "sun")) {
					// this means we have drilled down into the core system packages,
					// so we probably missed our mark and will save this as a last resort.
					// since we want the innermost, we'll use the first reference.
					// low priority for selection.
					if (thirdTierCandidate == null) {
						thirdTierCandidate = stackTraceElement;
					}
				} else {
					// If no exclusion criteria matches, we can suppose
					// that this is the innermost execution of the client code.
					if (!isExcluded(elementClassName, exclusions)) {
						// these are not necessarily excluded but not necessarily useful, assign
						// high priority for selection.
						if (elementClassName.toLowerCase().contains("log") || elementClassName.toLowerCase().contains("slf")
							|| elementMethodName.equalsIgnoreCase("<init>") || elementMethodName.equalsIgnoreCase("<clinit>")) {
							if (firstTierCandidate == null) {
								firstTierCandidate = stackTraceElement;
							}
						} else {
							// This is our guy. End the loop.
							selectedFrame = stackTraceElement;
							break;
						}
					} else if (!TypeUtils.startsWithIgnoreCase(elementClassName, "com.onloupe")) {
						// this was excluded but is the top level non-system, non-loupe reference,
						// mid priority for selection.
						if (secondTierCandidate == null) {
							secondTierCandidate = stackTraceElement;
						}
					}
				}
			}

			if (selectedFrame == null) {
				// we were unable to positively identify client code, so
				// let's evaluate our other candidates by priority.
				selectedFrame = (firstTierCandidate != null) ? firstTierCandidate
						: (secondTierCandidate != null) ? secondTierCandidate : thirdTierCandidate;
			}

			provider = new MessageSourceProvider(selectedFrame);
		} catch (Exception e) {
			// do nothing, will return the base provider with default definitions.
		}

		return provider;
	}
	
	/**
	 * Checks if is excluded.
	 *
	 * @param fqcn the fqcn
	 * @param exclusions the exclusions
	 * @return true, if is excluded
	 */
	private static boolean isExcluded(String fqcn, Set<String> exclusions) {
		// no exclusions defined, therefore exclude nothing.
		if (exclusions == null || exclusions.isEmpty())
			return false;
		
		// no fqcn, therefore we can't use it.
		if (TypeUtils.isBlank(fqcn))
			return true;
		
		return exclusions.stream().anyMatch(exclusion -> TypeUtils.startsWithIgnoreCase(fqcn, exclusion));
	}

	/**
	 * Safely attempt to expand a format string with supplied arguments.
	 * 
	 * If the normal call to string.Format() fails, this method does its best to
	 * create a string (intended as a log message) error message containing the
	 * original format string and a representation of the args supplied, to attempt
	 * to preserve meaningful information despite the user's mistake.
	 *
	 * @param locale the locale
	 * @param format         The desired format string, as used by string.Format().
	 * @param args           An array of args, as used by string.Format() after the
	 *                       format string.
	 * @return The formatted string, or an error string containing best-effort
	 *         information.
	 */
	public static String safeFormat(Locale locale, String format, Object... args) {
		if (args == null || args.length == 0) {
			// No arguments were supplied, so the "format" string is returned without any
			// expansion.
			// Providing null or empty is also legal in this case, and we'll treat them both
			// as empty.
			return (format != null) ? format : ""; // Protect against a null, always return a valid string.
		}

		String resultString;
		RuntimeException formattingException = null;

		// If format is null, we want to get the exception from string.Format(), but we
		// don't want to pass in
		// an empty format string (which won't fail but will drop all of their
		// arguments).
		// So this is not the usual IsNullOrEmpty() check, it's null-or-not-empty that
		// we want here.
		if (format == null || format.length() > 0) {
			try {
				// ReSharper disable AssignNullToNotNullAttribute
				resultString = String.format(locale, format, args);
				// ReSharper restore AssignNullToNotNullAttribute
			} catch (RuntimeException ex) {
				// Catch all exceptions.
				formattingException = ex;
				resultString = null; // Signal a failure, so we can exit the catch block for further error handling.
			}
		} else {
			// They supplied arguments with an empty or null format string, so they won't
			// get any info!
			// We'll treat this as an error case, so they get the data from the args in our
			// error handling.
			resultString = null;
		}

		if (resultString == null) {
			// There was some formatting error, so we want to format an error string with
			// all the useful info we can.

			StringBuilder supportBuilder = new StringBuilder((format != null) ? format : ""); // For support people.
			Formatter formattedSupportBuilder = new Formatter(supportBuilder, locale);

			StringBuilder devBuilder = new StringBuilder(); // For developers.

			String formatString = reverseEscapes(format);

			// Add a blank line after the format string for support. We need a second line
			// break if there wasn't one already.
			if (TypeUtils.isBlank(format)) {
				// ToDo: Decide if we actually want the extra one in this case. It seems
				// unnecessary.
				formattedSupportBuilder.format("\r\n"); // There wasn't one already, so add the first linebreak...
			} else {
				char lastChar = format.charAt(format.length() - 1);
				if (lastChar != '\n' && lastChar != '\r') {
					formattedSupportBuilder.format("\r\n"); // Make sure this case ends with some kind of a linebreak...
				}
			}
			// The second line break will come at the start of the first Value entry.

			if (formattingException != null) {
				devBuilder.append(String.format(
						"\r\n\r\n\r\nError expanding message format with %1$s args supplied:\r\nException = ",
						args.length));
				devBuilder.append(safeToString(locale, formattingException, false));

				// Use formatString here rather than format because it has the quotes around it
				// and handles the null case.
				devBuilder.append(String.format(locale, "\r\nFormat string = %s", formatString));
			}

			// Now loop over the args provided. We need to add each entry to supportBuilder
			// and devBuilder.

			for (int i = 0; i < args.length; i++) {
				Object argI = args[i];

				formattedSupportBuilder.format(locale, "\r\nValue #%d: %s", i, safeToString(locale, argI, false));
				// Only doing devBuilder if we have an actual formatting Exception. Empty format
				// case doesn't bother.
				if (formattingException != null) {
					if (argI == null) {
						// We can't call GetType() from a null, can we? I think any original cast type
						// for the null is lost
						// by this point, so we can't report a type for it (other than "object"), so
						// just report it as a null.
						devBuilder.append(String.format(locale, "\r\nargs[%d] %s", i, safeToString(locale, argI, true)));
					} else {
						String typeName = argI.getClass().getName();
						devBuilder.append(String.format(locale, String.format("\r\nargs[%d] (%s) = %s", i, typeName,
								safeToString(locale, argI, true))));
					}
				}
			}

			supportBuilder.append(devBuilder); // Append the devBuilder section
			resultString = supportBuilder.toString();
			formattedSupportBuilder.close();
		}

		return resultString;
	}

	/** The Constant resolvedEscapes. */
	private static final char[] resolvedEscapes = new char[] { '\r', '\n', '\t', '\"', '\\' };
	
	/** The Constant literalEscapes. */
	private static final String[] literalEscapes = new String[] { "\\r", "\\n", "\\t", "\\\"", "\\\\" };
	
	/** The Constant escapeMap. */
	private static final HashMap<Character, String> escapeMap = initEscapeMap();

	/**
	 * Initializes the EscapeMap dictionary.
	 *
	 * @return the hash map
	 */
	private static HashMap<Character, String> initEscapeMap() {
		// Allocate and initialize our mapping of special resolved-escape characters to
		// corresponding string literals.
		int size = resolvedEscapes.length;
		HashMap<Character, String> escapeMap = new HashMap<Character, String>(size);

		for (int i = 0; i < size; i++) {
			escapeMap.put(resolvedEscapes[i], literalEscapes[i]);
		}
		return escapeMap;
	}

	/**
	 * Expand (some) special characters back to how they appear in string literals
	 * in source code.
	 * 
	 * This currently does nothing but return the original string.
	 * 
	 * @param format The string (e.g. a format string) to convert back to its
	 *               literal appearance.
	 * @return A string with embedded backslash escape codes to be displayed as in
	 *         source code.
	 */
	private static String reverseEscapes(String format) {
		if (format == null) {
			return "(null)";
		}

		StringBuilder builder = new StringBuilder("\"");
		int currentIndex = 0;

		while (currentIndex < format.length()) {
			String escapeString = null;
			int nextEscapeIndex = CodeConversionHelpers.indexOfAny(format, resolvedEscapes, currentIndex);

			if (nextEscapeIndex < 0) {
				// There aren't any more. We just need to copy the rest of the string.
				nextEscapeIndex = format.length(); // Pretend it's just past the end, so the math below works.
				// Leave escapeString as null, so we won't append anything for it below.
			} else {
				// We found one of our ResolvedEscapes. Which one?
				char escapeChar = format.charAt(nextEscapeIndex);
				escapeString = escapeMap.get(escapeChar);
				if (escapeString == null) {
					// It wasn't found in the map! Someone screwed up our mapping configuration, so
					// we have to punt.
					escapeString = CodeConversionHelpers.repeatChar(escapeChar, 1); // Copy the original char (1 time).
				}
			}

			int length = nextEscapeIndex - currentIndex; // How long is the substring up to the next escape char?

			if (length >= 0) {
				builder.append(TypeUtils.mid(format, currentIndex, length)); // Copy the string up to this point.
			}

			if (TypeUtils.isNotBlank(escapeString)) {
				builder.append(escapeString); // Replace the char with the corresponding string.
			}

			currentIndex = nextEscapeIndex + 1;
		}

		builder.append("\"");
		return builder.toString();
	}

	/**
	 * Try to expand an object to a string, handling exceptions which might occur.
	 *
	 * @param locale the locale
	 * @param forDisplay     The object for display into a string.
	 * @param reverseEscapes Whether to convert null and strings back to appearance
	 *                       as in code.
	 * @return The best effort at representing the given object as a string.
	 */
	private static String safeToString(Locale locale, Object forDisplay, boolean reverseEscapes) {
		StringBuilder builder = new StringBuilder();

		RuntimeException displayException = forDisplay instanceof RuntimeException ? (RuntimeException) forDisplay
				: null;
		RuntimeException expansionException = null;
		try {
			if (reverseEscapes && (forDisplay == null || forDisplay.getClass() == String.class)) {
				builder.append(reverseEscapes((String) forDisplay)); // Special handling of strings and nulls requested.
			} else if (displayException == null) {
				// forDisplay was not an exception, so do a generic format.
				builder.append(String.format(locale, "%s", forDisplay)); // Try to format the object by formatProvider.
			} else {
				// forDisplay was an exception type, use a helpful two-line format.
				builder.append(
						String.format(locale, "%s\r\nException Message ", displayException.getClass().getName()));
				// This is separate so that the text is set up in case Message throws an
				// exception here.
				builder.append(String.format(locale, "= %s", displayException.getMessage()));
			}
		} catch (RuntimeException ex) {
			// Catch all exceptions.
			expansionException = ex;
		}

		if (expansionException != null) {
			try {
				builder.append(String.format(locale, "<<<%s error converting to string>>> : ",
						expansionException.getClass().getName()));
				builder.append(expansionException.getMessage());
			} catch (java.lang.Exception e) {
				// An exception accessing the exception? Wow. That should not be possible. Well,
				// just punt.
				builder.append("<<<Error accessing exception message>>>");
			}
		}

		return builder.toString();
	}
}