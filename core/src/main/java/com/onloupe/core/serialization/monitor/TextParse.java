package com.onloupe.core.serialization.monitor;

import java.util.Arrays;

import com.onloupe.core.util.TypeUtils;


/**
 * The Class TextParse.
 */
public final class TextParse {
	
	/**
	 * Splits a dot-delimited class name.
	 *
	 * @param className the class name
	 * @return the string[]
	 */
	public static String[] className(String className) {
		return splitStringWithTrim(className);
	}

	/**
	 * Split string with trim.
	 *
	 * @param source the source
	 * @return the string[]
	 */
	public static String[] splitStringWithTrim(String source) {
		return splitStringWithTrim(source, null);
	}

	/**
	 * Split string with trim.
	 *
	 * @param source the source
	 * @param additionalIgnoreCharacters the additional ignore characters
	 * @return the string[]
	 */
	public static String[] splitStringWithTrim(String source, char[] additionalIgnoreCharacters) {
		if (source == null) {
			return new String[0];
		}

		String[] sourceFragments = Arrays.asList(source.split(".")).stream()
				.filter(TypeUtils::isNotBlank).toArray(String[]::new);

		// now double check its perspective on empty entries: Remove any that are empty
		// when trimmed.
		int validFragmentsCount = sourceFragments.length; // by default all our valid names

		for (int curFragmentIndex = 0; curFragmentIndex < validFragmentsCount; curFragmentIndex++) {
			// Clean up this value, removing redundant demarcation characters and
			// leading/training whitespace
			String curCategoryName = sourceFragments[curFragmentIndex].trim();

			if (additionalIgnoreCharacters != null) {
				curCategoryName = TypeUtils.strip(curCategoryName, new String(additionalIgnoreCharacters));
			}

			// and if it is still not null or empty, just set it back. Otherwise, we need to
			// remove this string.
			if (TypeUtils.isBlank(curCategoryName)) {
				// move every remaining string up one to fill in the gap.
				for (int futureCategoryNameIndex = curFragmentIndex
						+ 1; futureCategoryNameIndex < sourceFragments.length; futureCategoryNameIndex++) {
					sourceFragments[futureCategoryNameIndex - 1] = sourceFragments[futureCategoryNameIndex];
				}

				// and the last one is now null, because we moved everything up.
				sourceFragments[sourceFragments.length - 1] = null;
				// we have one less valid name..
				validFragmentsCount--;
				// but we just moved something up into *our* spot so we need to recheck our
				// current position.
				curFragmentIndex--;
			} else {
				sourceFragments[curFragmentIndex] = curCategoryName;
			}
		}

		// do we need to shorten the array before we return it?
		if (validFragmentsCount < sourceFragments.length) {
			// yes, we must have found one or more names to be empty so we need to trim
			// them.
			String[] remainingFragments = new String[validFragmentsCount];

			// I admit it, this looks silly - but I couldn't find a built in way of getting
			// a limited # of the
			// array elements without doing my own iteration, so here it is.
			for (int curCategoryNameIndex = 0; curCategoryNameIndex < remainingFragments.length; curCategoryNameIndex++) {
				remainingFragments[curCategoryNameIndex] = sourceFragments[curCategoryNameIndex];
			}

			return remainingFragments;
		} else {
			// return the array as is - it's just fine.
			return sourceFragments;
		}
	}
}