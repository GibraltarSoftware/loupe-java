package com.onloupe.core.server;

import org.apache.http.StatusLine;

/**
 * Thrown by the web channel when the server reports that the file was not
 * found..
 */
public class WebChannelFileNotFoundException extends WebChannelException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Create a new file not found exception
	 * 
	 * @param message
	 * @param innerException
	 * @param requestUri
	 */
	public WebChannelFileNotFoundException(String message, StatusLine responseStatus, java.net.URI requestUri) {
		super(message, responseStatus, requestUri);
	}
}