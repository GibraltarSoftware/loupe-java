package com.onloupe.core.server;

import java.util.UUID;

/**
 * Information used in the CredentialsRequired event.
 */
public class CredentialsRequiredEventArgs {
	/**
	 * Create a new event data object
	 */
	public CredentialsRequiredEventArgs(String endpointUri, UUID repositoryId, boolean authFailed,
			IWebAuthenticationProvider authenticationProvider) {
		setEndpointUri(endpointUri);
		setRepositoryId(repositoryId);
		setAuthenticationProvider(authenticationProvider);
		setCancel(false);
		setAuthenticationFailed(authFailed);
	}

	/**
	 * The server being connected to
	 */
	private String endpointUri;

	public final String getEndpointUri() {
		return this.endpointUri;
	}

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

	public final UUID getRepositoryId() {
		return this.repositoryId;
	}

	private void setRepositoryId(UUID value) {
		this.repositoryId = value;
	}

	/**
	 * Indicates if credentials are required because an authentication attempt
	 * failed.
	 */
	private boolean authenticationFailed;

	public final boolean getAuthenticationFailed() {
		return this.authenticationFailed;
	}

	private void setAuthenticationFailed(boolean value) {
		this.authenticationFailed = value;
	}

	/**
	 * An authentication provider with the credentials to use.
	 */
	private IWebAuthenticationProvider authenticationProvider;

	public final IWebAuthenticationProvider getAuthenticationProvider() {
		return this.authenticationProvider;
	}

	public final void setAuthenticationProvider(IWebAuthenticationProvider value) {
		this.authenticationProvider = value;
	}

	/**
	 * True to cancel a connection attempt.
	 */
	private boolean cancel;

	public final boolean getCancel() {
		return this.cancel;
	}

	public final void setCancel(boolean value) {
		this.cancel = value;
	}
}