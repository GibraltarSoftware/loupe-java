package com.onloupe.agent.network;

import java.net.URI;

import org.apache.http.client.HttpClient;

/** 
 Basic Authentication credentials for authenticating with the server
*/
public final class BasicAuthenticationProvider implements IServerAuthenticationProvider
{
	/** 
	 Create a new instance of the HTTP Basic Authentication Provider with the specified username and password
	 
	 @param userName
	 @param password
	*/
	public BasicAuthenticationProvider(String userName, String password)
	{
		setUserName(userName);
		setPassword(password);
	}

	/** 
	 The user name to use for basic authentication
	*/
	private String userName;
	public String getUserName()
	{
		return userName;
	}
	public void setUserName(String value)
	{
		userName = value;
	}

	/** 
	 The password to use for basic authentication
	*/
	private String password;
	public String getPassword()
	{
		return password;
	}
	public void setPassword(String value)
	{
		password = value;
	}

	/** <inheritdoc />
	*/
	public boolean isAuthenticated()
	{
			//we don't need to pre-authenticate to get a token so we say yes.
		return true;
	}

	/** <inheritdoc />
	*/
	public boolean getLogoutIsSupported()
	{
		return false;
	}

	/** <inheritdoc />
	*/
	public void preProcessRequest(java.net.URI entryUri, HttpClient client, String resourceUrl, boolean requestSupportsAuthentication)
	{
		// Currrently do nothing, because our http client can handle this.
	}
	
	@Override
	public void login(URI entryUri, HttpClient client) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void logout(URI entryUri, HttpClient client) {
		// TODO Auto-generated method stub
		
	}

	
}