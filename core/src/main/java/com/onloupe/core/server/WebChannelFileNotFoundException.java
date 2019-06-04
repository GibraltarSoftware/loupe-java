package com.onloupe.core.server;

import org.apache.http.StatusLine;

// TODO: Auto-generated Javadoc
/**
 * Thrown by the web channel when the server reports that the file was not
 * found..
 */
public class WebChannelFileNotFoundException extends WebChannelException {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Create a new file not found exception.
	 *
	 * @param message the message
	 * @param responseStatus the response status
	 * @param requestUri the request uri
	 */
	public WebChannelFileNotFoundException(String message, StatusLine responseStatus, java.net.URI requestUri) {
		super(message, responseStatus, requestUri);
	}
}