package com.onloupe.core.server;

import org.apache.http.StatusLine;

public class WebChannelMethodNotAllowedException extends WebChannelException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WebChannelMethodNotAllowedException(String message, StatusLine responseStatus, java.net.URI requestUri) {
		super(message, responseStatus, requestUri);
	}

}