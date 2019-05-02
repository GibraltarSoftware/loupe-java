package com.onloupe.appenders.logback;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.onloupe.core.logging.Log;
import com.onloupe.core.util.LogSystems;
import com.onloupe.model.log.LogMessageSeverity;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;

public class LoupeLogbackAppender extends AppenderBase<ILoggingEvent> {
	
	private static final String LOG_SYSTEM = LogSystems.LOGBACK;
	private static final int SKIP_FRAMES = 1;
	private static final Set<String> EXCLUSIONS = new HashSet<>(Arrays.asList("ch.qos.logback"));
	private UUID appenderRef;
	private boolean failed;

	@Override
	protected void append(ILoggingEvent event) {
		if (failed || event == null || Level.OFF.equals(event.getLevel()))
			return;
		
		Throwable throwable = null;
		if (event.getThrowableProxy() instanceof ThrowableProxy) {
			throwable = ((ThrowableProxy)event.getThrowableProxy()).getThrowable();
		}
		
		StackTraceElement source = null;
		if (event.getCallerData() != null && event.getCallerData().length > 0) {
			source = event.getCallerData()[0];
		}

		Log.write(mapSeverity(event.getLevel()), throwable, source, SKIP_FRAMES, EXCLUSIONS, null, LOG_SYSTEM,
				event.getLoggerName(), event.getMessage(), null);
	}
	
	@Override
	public void start() {
		super.start();
		try {
			appenderRef = Log.startAppender(this.getClass().getSimpleName());
		} catch (IOException e) {
			System.err.print("Loupe failed to start: " + e.getMessage());
			failed = true;
		}
	}
	
	@Override
	public void stop() {
		Log.shutdownAppender(appenderRef, false);
		super.stop();
	}
	
	private LogMessageSeverity mapSeverity(Level level) {
		if (level != null) {
			switch (level.levelInt) {
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
