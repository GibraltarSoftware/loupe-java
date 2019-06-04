package com.onloupe.core.server;

import com.onloupe.core.data.SessionHeader;

// TODO: Auto-generated Javadoc
/**
 * Event arguments for session header changes.
 */
public class SessionHeaderEventArgs {
	
	/**
	 * Create a new session header event arguments object.
	 *
	 * @param header the header
	 */
	public SessionHeaderEventArgs(SessionHeader header) {
		setSessionHeader(header);
	}

	/** The session header that was affected. */
	private SessionHeader sessionHeader;

	/**
	 * Gets the session header.
	 *
	 * @return the session header
	 */
	public final SessionHeader getSessionHeader() {
		return this.sessionHeader;
	}

	/**
	 * Sets the session header.
	 *
	 * @param value the new session header
	 */
	private void setSessionHeader(SessionHeader value) {
		this.sessionHeader = value;
	}
}