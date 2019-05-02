package com.onloupe.core.server;

import java.io.IOException;

/**
 * An abstract implementation of a web request that simplifies making new
 * requests.
 */
public abstract class WebChannelRequestBase implements IWebRequest {
	/**
	 * The standard content type for GZip'd data.
	 */
	public static final String GZIP_CONTENT_TYPE = "application/gzip";

	/**
	 * The standard content type for raw binary data.
	 */
	public static final String BINARY_CONTENT_TYPE = "application/octet-stream";

	/**
	 * The standard content type for a zip file
	 */
	public static final String ZIP_CONTENT_TYPE = "application/zipfile";

	/**
	 * The standard content type for XML data
	 */
	protected static final String XML_CONTENT_TYPE = "text/xml";

	/**
	 * The standard content type for text data
	 */
	protected static final String TEXT_CONTENT_TYPE = "text/text";

	private boolean supportsAuthentication;
	private boolean requiresAuthentication;

	/**
	 * Create a new web channel request
	 * 
	 * @param supportsAuthentication
	 * @param requiresAuthentication
	 */
	protected WebChannelRequestBase(boolean supportsAuthentication, boolean requiresAuthentication) {
		this.supportsAuthentication = supportsAuthentication;
		this.requiresAuthentication = requiresAuthentication;
	}

	/**
	 * Indicates if the web request requires authentication (so the channel should
	 * authenticate before attempting the request)
	 */
	@Override
	public final boolean getRequiresAuthentication() {
		return this.requiresAuthentication;
	}

	/**
	 * Indicates if the web request supports authentication, so if the server
	 * requests credentials the request can provide them.
	 */
	@Override
	public final boolean getSupportsAuthentication() {
		return this.supportsAuthentication;
	}

	/**
	 * Perform the request against the specified web client connection.
	 * 
	 * @param connection
	 * @throws Exception
	 * @throws IOException
	 */
	@Override
	public final void processRequest(IWebChannelConnection connection) throws IOException, Exception {
		onProcessRequest(connection);
	}

	/**
	 * Implemented by inheritors to perform the request on the provided web client.
	 * 
	 * @param connection
	 * @throws IOException
	 * @throws Exception
	 */
	protected abstract void onProcessRequest(IWebChannelConnection connection) throws IOException, Exception;

}