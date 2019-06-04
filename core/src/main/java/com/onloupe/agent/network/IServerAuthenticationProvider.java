package com.onloupe.agent.network;

import org.apache.http.client.HttpClient;

// TODO: Auto-generated Javadoc
/**
 *  
 *  Implemented to provide custom authentication for a web channel.
 */
public interface IServerAuthenticationProvider
{
	
	/**
	 *  
	 * 	 Indicates if the authentication provider believes it has authenticated with the channel
	 * 	 
	 * 	 If false then no logout will be attempted, and any request that requires authentication will
	 * 	 cause a login attempt without waiting for an authentication failure.
	 *
	 * @return true, if is authenticated
	 */
	boolean isAuthenticated();

	/**
	 *  
	 * 	 indicates if the authentication provider can perform a logout.
	 *
	 * @return the logout is supported
	 */
	boolean getLogoutIsSupported();

	/** 
	 Perform a login on the supplied channel
	 
	 @param entryUri The entry URL of the server
	 @param client A web client object to use to perform login operations.
	*/
	void login(java.net.URI entryUri, HttpClient client);

	/** 
	 Perform a logout on the supplied channel
	 
	 @param entryUri The entry URL of the server
	 @param client A web client object to use to perform logout operations.
	*/
	void logout(java.net.URI entryUri, HttpClient client);

	/** 
	 Perform per-request authentication processing.
	 
	 @param entryUri The entry URL of the server
	 @param client The web client that is about to be used to execute the request.  It can't be used by the authentication provider to make requests.
	 @param resourceUrl The resource URL (with query string) specified by the client.
	 @param requestSupportsAuthentication Indicates if the request being processed supports authentication or not.
	 If the request doesn't support authentication, it's a best practice to not provide any authentication information.
	*/
	void preProcessRequest(java.net.URI entryUri, HttpClient client, String resourceUrl, boolean requestSupportsAuthentication);
}