package com.onloupe.core.server;

import java.net.URISyntaxException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;

// TODO: Auto-generated Javadoc
/**
 * Implemented to provide custom authentication for a web channel.
 */
public interface IWebAuthenticationProvider {
	
	/**
	 * Indicates if the authentication provider believes it has authenticated with
	 * the channel
	 * 
	 * If false then no logout will be attempted, and any request that requires
	 * authentication will cause a login attempt without waiting for an
	 * authentication failure.
	 *
	 * @return true, if is authenticated
	 */
	boolean isAuthenticated();

	/**
	 * indicates if the authentication provider can perform a logout.
	 *
	 * @return true, if successful
	 */
	boolean logoutIsSupported();

	/**
	 * Perform a login on the supplied channel.
	 *
	 * @param channel The channel object
	 * @param client  A web client object to use to perform login operations.
	 * @throws Exception the exception
	 */
	void login(WebChannel channel, HttpClient client) throws Exception;

	/**
	 * Perform a logout on the supplied channel.
	 *
	 * @param channel The channel object
	 * @param client  A web client object to use to perform logout operations.
	 */
	void logout(WebChannel channel, HttpClient client);

	/**
	 * Perform per-request authentication processing.
	 *
	 * @param channel                       The channel object
	 * @param client                        The web client that is about to be used
	 *                                      to execute the request. It can't be used
	 *                                      by the authentication provider to make
	 *                                      requests.
	 * @param request                       The request that is about to be sent
	 * @param resourceUrl                   The resource URL (with query string)
	 *                                      specified by the client.
	 * @param requestSupportsAuthentication Indicates if the request being processed
	 *                                      supports authentication or not. If the
	 *                                      request doesn't support authentication,
	 *                                      it's a best practice to not provide any
	 *                                      authentication information.
	 * @throws URISyntaxException the URI syntax exception
	 */
	void preProcessRequest(WebChannel channel, CloseableHttpClient client, HttpRequestBase request, String resourceUrl,
			boolean requestSupportsAuthentication) throws URISyntaxException;
}