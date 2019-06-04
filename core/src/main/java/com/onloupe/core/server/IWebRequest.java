package com.onloupe.core.server;

import java.io.IOException;

// TODO: Auto-generated Javadoc
/**
 * The format of a web request provided to the GibraltarWebClient.
 */
public interface IWebRequest {
	
	/**
	 * Indicates if the web request requires authentication (so the channel should
	 * authenticate before attempting the request).
	 *
	 * @return the requires authentication
	 */
	boolean getRequiresAuthentication();

	/**
	 * Indicates if the web request supports authentication, so if the server
	 * requests credentials the request can provide them.
	 *
	 * @return the supports authentication
	 */
	boolean getSupportsAuthentication();

	/**
	 * Perform the request against the specified web client connection.
	 *
	 * @param connection the connection
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	void processRequest(IWebChannelConnection connection) throws IOException, Exception;
}