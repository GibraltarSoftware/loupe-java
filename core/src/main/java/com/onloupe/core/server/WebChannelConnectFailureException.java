package com.onloupe.core.server;

import org.apache.http.StatusLine;


/**
 * An exception thrown to indicate a connection failure on the web channel.
 */
public class WebChannelConnectFailureException extends WebChannelException {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Create a new connection failure exception.
	 *
	 * @param message the message
	 * @param responseStatus the response status
	 * @param requestUri the request uri
	 */
	public WebChannelConnectFailureException(String message, StatusLine responseStatus, java.net.URI requestUri) {
		super(message, responseStatus, requestUri);

	}

	/**
	 * Create a new connection failure exception.
	 *
	 * @param message the message
	 */
	public WebChannelConnectFailureException(String message) {
		super(message);

	}
}