package com.onloupe.core.server;

import com.onloupe.core.logging.Log;
import com.onloupe.core.logging.LogWriteMode;
import com.onloupe.model.log.LogMessageSeverity;

// TODO: Auto-generated Javadoc
/**
 * Client logger implementation for our core Loupe logging interface.
 */
public class ClientLogger implements IClientLogger {
	
	/* (non-Javadoc)
	 * @see com.onloupe.core.server.IClientLogger#getSilentMode()
	 */
	@Override
	public final boolean getSilentMode() {
		return Log.getSilentMode();
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.server.IClientLogger#write(com.onloupe.model.log.LogMessageSeverity, java.lang.String, java.lang.String, java.lang.String, java.lang.Object[])
	 */
	@Override
	public final void write(LogMessageSeverity severity, String category, String caption, String description,
			Object... args) {
		Log.write(severity, category, caption, description, args);
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.server.IClientLogger#write(com.onloupe.model.log.LogMessageSeverity, java.lang.Throwable, boolean, java.lang.String, java.lang.String, java.lang.String, java.lang.Object[])
	 */
	@Override
	public final void write(LogMessageSeverity severity, Throwable exception, boolean attributeToException,
			String category, String caption, String description, Object... args) {
		Log.write(severity, LogWriteMode.QUEUED, exception, attributeToException, category, caption, description, args);
	}
}