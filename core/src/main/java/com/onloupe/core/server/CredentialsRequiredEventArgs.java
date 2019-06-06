package com.onloupe.core.server;

import java.util.UUID;


/**
 * Information used in the CredentialsRequired event.
 */
public class CredentialsRequiredEventArgs {
	
	/**
	 * Create a new event data object.
	 *
	 * @param endpointUri the endpoint uri
	 * @param repositoryId the repository id
	 * @param authFailed the auth failed
	 * @param authenticationProvider the authentication provider
	 */
	public CredentialsRequiredEventArgs(String endpointUri, UUID repositoryId, boolean authFailed,
			IWebAuthenticationProvider authenticationProvider) {
		setEndpointUri(endpointUri);
		setRepositoryId(repositoryId);
		setAuthenticationProvider(authenticationProvider);
		setCancel(false);
		setAuthenticationFailed(authFailed);
	}

	/** The server being connected to. */
	private String endpointUri;

	/**
	 * Gets the endpoint uri.
	 *
	 * @return the endpoint uri
	 */
	public final String getEndpointUri() {
		return this.endpointUri;
	}

	/**
	 * Sets the endpoint uri.
	 *
	 * @param value the new endpoint uri
	 */
	private void setEndpointUri(String value) {
		this.endpointUri = value;
	}

	/**
	 * The repository being connected to
	 * 
	 * In extraordinary cases - like authentication is required to the server
	 * configuration page - this will be an empty GUID.
	 */
	private UUID repositoryId;

	/**
	 * Gets the repository id.
	 *
	 * @return the repository id
	 */
	public final UUID getRepositoryId() {
		return this.repositoryId;
	}

	/**
	 * Sets the repository id.
	 *
	 * @param value the new repository id
	 */
	private void setRepositoryId(UUID value) {
		this.repositoryId = value;
	}

	/**
	 * Indicates if credentials are required because an authentication attempt
	 * failed.
	 */
	private boolean authenticationFailed;

	/**
	 * Gets the authentication failed.
	 *
	 * @return the authentication failed
	 */
	public final boolean getAuthenticationFailed() {
		return this.authenticationFailed;
	}

	/**
	 * Sets the authentication failed.
	 *
	 * @param value the new authentication failed
	 */
	private void setAuthenticationFailed(boolean value) {
		this.authenticationFailed = value;
	}

	/**
	 * An authentication provider with the credentials to use.
	 */
	private IWebAuthenticationProvider authenticationProvider;

	/**
	 * Gets the authentication provider.
	 *
	 * @return the authentication provider
	 */
	public final IWebAuthenticationProvider getAuthenticationProvider() {
		return this.authenticationProvider;
	}

	/**
	 * Sets the authentication provider.
	 *
	 * @param value the new authentication provider
	 */
	public final void setAuthenticationProvider(IWebAuthenticationProvider value) {
		this.authenticationProvider = value;
	}

	/**
	 * True to cancel a connection attempt.
	 */
	private boolean cancel;

	/**
	 * Gets the cancel.
	 *
	 * @return the cancel
	 */
	public final boolean getCancel() {
		return this.cancel;
	}

	/**
	 * Sets the cancel.
	 *
	 * @param value the new cancel
	 */
	public final void setCancel(boolean value) {
		this.cancel = value;
	}
}