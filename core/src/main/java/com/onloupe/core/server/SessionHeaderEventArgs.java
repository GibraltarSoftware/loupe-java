package com.onloupe.core.server;

import com.onloupe.core.data.SessionHeader;

/**
 * Event arguments for session header changes
 */
public class SessionHeaderEventArgs {
	/**
	 * Create a new session header event arguments object
	 * 
	 * @param header
	 */
	public SessionHeaderEventArgs(SessionHeader header) {
		setSessionHeader(header);
	}

	/**
	 * The session header that was affected
	 */
	private SessionHeader sessionHeader;

	public final SessionHeader getSessionHeader() {
		return this.sessionHeader;
	}

	private void setSessionHeader(SessionHeader value) {
		this.sessionHeader = value;
	}
}