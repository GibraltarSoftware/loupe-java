package com.onloupe.agent.network;

import java.net.URI;

import org.apache.http.client.HttpClient;

// TODO: Auto-generated Javadoc
/**
 *  
 *  Basic Authentication credentials for authenticating with the server.
 */
public final class BasicAuthenticationProvider implements IServerAuthenticationProvider
{
	
	/**
	 *  
	 * 	 Create a new instance of the HTTP Basic Authentication Provider with the specified username and password
	 * 	 
	 *
	 * @param userName the user name
	 * @param password the password
	 */
	public BasicAuthenticationProvider(String userName, String password)
	{
		setUserName(userName);
		setPassword(password);
	}

	/**   	 The user name to use for basic authentication. */
	private String userName;
	
	/**
	 * Gets the user name.
	 *
	 * @return the user name
	 */
	public String getUserName()
	{
		return userName;
	}
	
	/**
	 * Sets the user name.
	 *
	 * @param value the new user name
	 */
	public void setUserName(String value)
	{
		userName = value;
	}

	/**   	 The password to use for basic authentication. */
	private String password;
	
	/**
	 * Gets the password.
	 *
	 * @return the password
	 */
	public String getPassword()
	{
		return password;
	}
	
	/**
	 * Sets the password.
	 *
	 * @param value the new password
	 */
	public void setPassword(String value)
	{
		password = value;
	}

	/**
	 *  <inheritdoc />.
	 *
	 * @return true, if is authenticated
	 */
	public boolean isAuthenticated()
	{
			//we don't need to pre-authenticate to get a token so we say yes.
		return true;
	}

	/**
	 *  <inheritdoc />.
	 *
	 * @return the logout is supported
	 */
	public boolean getLogoutIsSupported()
	{
		return false;
	}

	/**
	 *  <inheritdoc />.
	 *
	 * @param entryUri the entry uri
	 * @param client the client
	 * @param resourceUrl the resource url
	 * @param requestSupportsAuthentication the request supports authentication
	 */
	public void preProcessRequest(java.net.URI entryUri, HttpClient client, String resourceUrl, boolean requestSupportsAuthentication)
	{
		// Currrently do nothing, because our http client can handle this.
	}
	
	/* (non-Javadoc)
	 * @see com.onloupe.agent.network.IServerAuthenticationProvider#login(java.net.URI, org.apache.http.client.HttpClient)
	 */
	@Override
	public void login(URI entryUri, HttpClient client) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see com.onloupe.agent.network.IServerAuthenticationProvider#logout(java.net.URI, org.apache.http.client.HttpClient)
	 */
	@Override
	public void logout(URI entryUri, HttpClient client) {
		// TODO Auto-generated method stub
		
	}

	
}