package com.onloupe.core.server;

import org.apache.http.StatusLine;

/**
 * An exception thrown to indicate a connection failure on the web channel.
 */
public class WebChannelConnectFailureException extends WebChannelException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Create a new connection failure exception
	 */
	public WebChannelConnectFailureException(String message, StatusLine responseStatus, java.net.URI requestUri) {
		super(message, responseStatus, requestUri);

	}

	/**
	 * Create a new connection failure exception
	 */
	public WebChannelConnectFailureException(String message) {
		super(message);

	}
}