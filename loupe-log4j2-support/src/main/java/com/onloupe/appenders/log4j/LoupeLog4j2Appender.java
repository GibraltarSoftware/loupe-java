package com.onloupe.appenders.log4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import com.onloupe.core.logging.Log;
import com.onloupe.core.logging.ThreadInfo;
import com.onloupe.core.util.LogSystems;
import com.onloupe.model.log.LogMessageSeverity;

@Plugin(name="LoupeLog4jAppender", category= Core.CATEGORY_NAME, elementType= Appender.ELEMENT_TYPE, printObject=true)
public final class LoupeLog4j2Appender extends AbstractAppender {

	private static final String LOG_SYSTEM = LogSystems.LOG4J;
	private static final int SKIP_FRAMES = 4;
	private static final Set<String> EXCLUSIONS = new HashSet<>(Arrays.asList("org.apache.log4j"));
	private UUID appenderRef;
	private boolean failed;

	@Override
	public void append(LogEvent event) {
		if (failed || event == null || Level.OFF.equals(event.getLevel()))
			return;

		Log.write(mapSeverity(event.getLevel()), event.getThrown(), event.getSource(), SKIP_FRAMES, EXCLUSIONS,
				new ThreadInfo(event.getThreadId(), event.getThreadName()), LOG_SYSTEM, event.getLoggerName(),
				event.getMessage() != null ? event.getMessage().getFormattedMessage() : null, null);
	}

	protected LoupeLog4j2Appender(String name) {
		super(name, null, null);
	}

    @PluginFactory
    public static LoupeLog4j2Appender createAppender(
      @PluginAttribute("name") String name) {
        return new LoupeLog4j2Appender(name);
    }
    
    @Override
    public void start() {
    	super.start();
    	try {
			appenderRef = Log.startAppender(super.getName());
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
		if (Level.FATAL.equals(level))
			return LogMessageSeverity.CRITICAL;
		if (Level.ERROR.equals(level))
			return LogMessageSeverity.ERROR;
		if (Level.WARN.equals(level))
			return LogMessageSeverity.WARNING;
		if (Level.INFO.equals(level))
			return LogMessageSeverity.INFORMATION;
		if (Level.DEBUG.equals(level) || Level.TRACE.equals(level))
			return LogMessageSeverity.VERBOSE;
		return LogMessageSeverity.UNKNOWN;
	}

}
