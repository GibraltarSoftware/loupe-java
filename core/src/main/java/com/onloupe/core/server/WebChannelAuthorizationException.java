package com.onloupe.core.server;

import org.apache.http.StatusLine;


/**
 * Thrown by the web channel when it is unable to authenticate to the remote
 * server.
 */
public class WebChannelAuthorizationException extends WebChannelException {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Create a new authorization exception.
	 *
	 * @param message the message
	 * @param responseStatus the response status
	 * @param requestUri the request uri
	 */
	public WebChannelAuthorizationException(String message, StatusLine responseStatus, java.net.URI requestUri) {
		super(message, responseStatus, requestUri);
	}

	/**
	 * Instantiates a new web channel authorization exception.
	 *
	 * @param message the message
	 */
	public WebChannelAuthorizationException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

}