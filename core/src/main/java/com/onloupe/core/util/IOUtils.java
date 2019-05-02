package com.onloupe.core.util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class IOUtils {
	
	public static void closeQuietly(final Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (IOException e) {
			
		}
	}
	
	public static String copyToString(final InputStream inputStream) {
		try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
			return bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
		} catch (IOException e) {
			return null;
		}
	}
}
