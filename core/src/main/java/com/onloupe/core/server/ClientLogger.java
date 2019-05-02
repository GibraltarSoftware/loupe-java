package com.onloupe.core.server;

import com.onloupe.core.logging.Log;
import com.onloupe.core.logging.LogWriteMode;
import com.onloupe.model.log.LogMessageSeverity;

/**
 * Client logger implementation for our core Loupe logging interface
 */
public class ClientLogger implements IClientLogger {
	@Override
	public final boolean getSilentMode() {
		return Log.getSilentMode();
	}

	@Override
	public final void write(LogMessageSeverity severity, String category, String caption, String description,
			Object... args) {
		Log.write(severity, category, caption, description, args);
	}

	@Override
	public final void write(LogMessageSeverity severity, Throwable exception, boolean attributeToException,
			String category, String caption, String description, Object... args) {
		Log.write(severity, LogWriteMode.QUEUED, exception, attributeToException, category, caption, description, args);
	}
}