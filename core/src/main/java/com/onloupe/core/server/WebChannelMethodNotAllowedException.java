package com.onloupe.core.server;

import org.apache.http.StatusLine;

// TODO: Auto-generated Javadoc
/**
 * The Class WebChannelMethodNotAllowedException.
 */
public class WebChannelMethodNotAllowedException extends WebChannelException {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new web channel method not allowed exception.
	 *
	 * @param message the message
	 * @param responseStatus the response status
	 * @param requestUri the request uri
	 */
	public WebChannelMethodNotAllowedException(String message, StatusLine responseStatus, java.net.URI requestUri) {
		super(message, responseStatus, requestUri);
	}

}