package com.onloupe.core.server;

import org.apache.http.StatusLine;

// TODO: Auto-generated Javadoc
/**
 * The Class WebChannelExpectationFailedException.
 */
public class WebChannelExpectationFailedException extends WebChannelException {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new web channel expectation failed exception.
	 *
	 * @param message the message
	 * @param responseStatus the response status
	 * @param requestUri the request uri
	 */
	public WebChannelExpectationFailedException(String message, StatusLine responseStatus, java.net.URI requestUri) {
		super(message, responseStatus, requestUri);
	}

}