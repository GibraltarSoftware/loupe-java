package com.onloupe.appenders.log4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

import com.onloupe.core.logging.Log;
import com.onloupe.core.logging.ThreadInfo;
import com.onloupe.core.util.LogSystems;
import com.onloupe.model.log.LogMessageSeverity;

public class LoupeLog4jAppender extends AppenderSkeleton {
	
	private static final String LOG_SYSTEM = LogSystems.LOG4J;
	private static final int SKIP_FRAMES = 5;
	private static final Set<String> EXCLUSIONS = new HashSet<>(Arrays.asList("org.apache.log4j"));
	
	private UUID appenderRef = null;
	private boolean failed;

	@Override
	protected void append(LoggingEvent event) {
		if (failed || event == null || Level.OFF.equals(event.getLevel()))
			return;
		
		if (appenderRef == null) {
			try {
				appenderRef = Log.startAppender(this.getClass().getName());
			} catch (IOException e) {
				System.err.print("Loupe failed to start: " + e.getMessage());
				failed = true;
				return;
			}
		}
		
		// the event only provides the thread name. Identifying the rest based on the name is more
		// expensive than just digging up the thread itself.
		ThreadInfo threadInfo = new ThreadInfo();
		
		Throwable throwable = event.getThrowableInformation() != null ? event.getThrowableInformation().getThrowable()
				: null;
		
		StackTraceElement source = null;
		
		if (event.locationInformationExists()) {
			// try to get a stack trace element from the location information.
			try {
				source = new StackTraceElement(event.getLocationInformation().getClassName(),
						event.getLocationInformation().getMethodName(), event.getLocationInformation().getFileName(),
						Integer.parseInt(event.getLocationInformation().getLineNumber()));
			} catch (Exception e) {
				// forget it. we'll determine this down stream.
			} 
		}
		
		Log.write(mapSeverity(event.getLevel()), throwable, source, SKIP_FRAMES, EXCLUSIONS, threadInfo, LOG_SYSTEM,
				event.getLoggerName(), event.getRenderedMessage(), null);

	}
	
	

	@Override
	public void close() {
		Log.shutdownAppender(appenderRef, false);
	}

	@Override
	public boolean requiresLayout() {
		return false;
	}
	
	private LogMessageSeverity mapSeverity(Level level) {
		if (level != null) {
			switch (level.toInt()) {
			case Level.FATAL_INT:
				return LogMessageSeverity.CRITICAL;
			case Level.ERROR_INT:
				return LogMessageSeverity.ERROR;
			case Level.WARN_INT:
				return LogMessageSeverity.WARNING;
			case Level.INFO_INT:
				return LogMessageSeverity.INFORMATION;
			case Level.DEBUG_INT:
			case Level.TRACE_INT:
				return LogMessageSeverity.VERBOSE;
			}
		}
		return LogMessageSeverity.UNKNOWN;
	}
}
