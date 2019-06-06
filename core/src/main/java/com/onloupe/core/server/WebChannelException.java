package com.onloupe.core.server;

import org.apache.http.StatusLine;

import com.onloupe.model.exception.GibraltarException;


/**
 * The base class for all exceptions thrown by the Web Channel.
 */
public class WebChannelException extends GibraltarException {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new web channel exception.
	 *
	 * @param message the message
	 */
	public WebChannelException(String message) {
		super(message);
	}

	/**
	 * Create a new web channel exception.
	 *
	 * @param message the message
	 * @param responseStatus the response status
	 * @param requestUri the request uri
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

	/**
	 * Gets the request uri.
	 *
	 * @return the request uri
	 */
	public final java.net.URI getRequestUri() {
		return this.requestUri;
	}

	/**
	 * Sets the request uri.
	 *
	 * @param value the new request uri
	 */
	private void setRequestUri(java.net.URI value) {
		this.requestUri = value;
	}

	/** The response status. */
	private StatusLine responseStatus;

	/**
	 * Gets the response status.
	 *
	 * @return the response status
	 */
	public StatusLine getResponseStatus() {
		return this.responseStatus;
	}

	/**
	 * Sets the response status.
	 *
	 * @param responseStatus the new response status
	 */
	public void setResponseStatus(StatusLine responseStatus) {
		this.responseStatus = responseStatus;
	}

}