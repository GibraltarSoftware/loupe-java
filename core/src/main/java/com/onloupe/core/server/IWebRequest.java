package com.onloupe.core.server;

import java.io.IOException;

/**
 * The format of a web request provided to the GibraltarWebClient
 */
public interface IWebRequest {
	/**
	 * Indicates if the web request requires authentication (so the channel should
	 * authenticate before attempting the request)
	 */
	boolean getRequiresAuthentication();

	/**
	 * Indicates if the web request supports authentication, so if the server
	 * requests credentials the request can provide them.
	 */
	boolean getSupportsAuthentication();

	/**
	 * Perform the request against the specified web client connection.
	 * 
	 * @param connection
	 * @throws Exception
	 * @throws IOException
	 */
	void processRequest(IWebChannelConnection connection) throws IOException, Exception;
}