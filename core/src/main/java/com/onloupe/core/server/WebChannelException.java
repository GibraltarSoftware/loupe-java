package com.onloupe.core.server;

import org.apache.http.StatusLine;

import com.onloupe.model.exception.GibraltarException;

/**
 * The base class for all exceptions thrown by the Web Channel
 */
public class WebChannelException extends GibraltarException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WebChannelException(String message) {
		super(message);
	}

	/**
	 * Create a new web channel exception
	 */
	public WebChannelException(String message, StatusLine responseStatus, java.net.URI requestUri) {
		super(message);
		setRequestUri(requestUri);
		setResponseStatus(responseStatus);
	}

	/**
	 * the url that was requested.
	 */
	private java.net.URI requestUri;

	public final java.net.URI getRequestUri() {
		return this.requestUri;
	}

	private void setRequestUri(java.net.URI value) {
		this.requestUri = value;
	}

	private StatusLine responseStatus;

	public StatusLine getResponseStatus() {
		return this.responseStatus;
	}

	public void setResponseStatus(StatusLine responseStatus) {
		this.responseStatus = responseStatus;
	}

}