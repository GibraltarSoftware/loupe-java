package com.onloupe.core.server;

import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

// TODO: Auto-generated Javadoc
/**
 * Manages the cached credentials for the current process
 * 
 * By retrieving repository credentials from the credential manager you ensure
 * coordination between all of the channels in the process. This eliminates
 * cases where two threads presenting alternate credentials cause
 * reauthentication which dramatically decreases efficiency of communication.
 */
public final class CachedCredentialsManager {
	
	/** The Constant gLock. */
	private static final Object gLock = new Object();
	
	/** The Constant gRequestLock. */
	private static final Object gRequestLock = new Object();

	// we have to cache using a case-sensitive comparison of the endpoint because
	/** The Constant gCachedCredentials. */
	// hashing is case-sensitive.
	private static final HashMap<CredentialCacheKey, IWebAuthenticationProvider> gCachedCredentials = new HashMap<CredentialCacheKey, IWebAuthenticationProvider>(); // PROTECTED
																																										// BY
																																										/** The Constant gCachedBlockedCredentials. */
																																										// LOCK
	private static final HashMap<String, String> gCachedBlockedCredentials = new HashMap<String, String>(); // PROTECTED
																											// BY LOCK

	/**
																											 * The Class CredentialCacheKey.
																											 */
																											private static class CredentialCacheKey {
		
		/** The hash code. */
		private int hashCode; // to avoid extra cost of calculation

		/**
		 * Instantiates a new credential cache key.
		 *
		 * @param entryUri the entry uri
		 * @param repositoryId the repository id
		 */
		public CredentialCacheKey(String entryUri, UUID repositoryId) {
			setEntryUri(entryUri);
			setRepositoryId(repositoryId);
			this.hashCode = getEntryUri().toLowerCase(Locale.ROOT).hashCode();
			this.hashCode = this.hashCode ^ getRepositoryId().hashCode();
		}

		/** The entry uri. */
		private String entryUri;

		/**
		 * Gets the entry uri.
		 *
		 * @return the entry uri
		 */
		public final String getEntryUri() {
			return this.entryUri;
		}

		/**
		 * Sets the entry uri.
		 *
		 * @param value the new entry uri
		 */
		private void setEntryUri(String value) {
			this.entryUri = value;
		}

		/** The repository id. */
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
		 * Equals.
		 *
		 * @param other the other
		 * @return true, if successful
		 */
		public final boolean equals(CredentialCacheKey other) {
			if (!getEntryUri().equalsIgnoreCase(other.getEntryUri())) {
				return false;
			}

			if (!getRepositoryId().equals(other.getRepositoryId())) {
				return false;
			}

			return true;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return this.hashCode;
		}
	}

	/**
	 * Get credentials for the specified URL target and repository information.
	 *
	 * @param targetChannel    The web channel representing the endpoint that the
	 *                         credentials are for
	 * @param useApiKey        True if an API key was used to originally set up the
	 *                         connection
	 * @param repositoryId     The owner Id to specify to the server (for example
	 *                         repository Id)
	 * @param keyContainerName The name of the key container to retrieve the private
	 *                         key from
	 * @param useMachineStore  True to use the machine store instead of the user
	 *                         store for the digital certificate
	 * @return If existing credentials are available they will be provided,
	 *         otherwise a new credentials object will be created and returned. This
	 *         method is Multithread safe.
	 */
	public static IWebAuthenticationProvider getCredentials(WebChannel targetChannel, boolean useApiKey,
			UUID repositoryId, String keyContainerName, boolean useMachineStore) {
		return getCredentials(getEntryUri(targetChannel), useApiKey, repositoryId, keyContainerName, useMachineStore);
	}

	/**
	 * Get credentials for the specified URL target and repository information.
	 *
	 * @param targetChannel    The web channel representing the endpoint that the
	 *                         credentials are for
	 * @param useApiKey        True if an API key was used to originally set up the
	 *                         connection
	 * @param repositoryId     The owner Id to specify to the server (for example
	 *                         repository Id)
	 * @param keyContainerName The name of the key container to retrieve the private
	 *                         key from
	 * @param useMachineStore  True to use the machine store instead of the user
	 *                         store for the digital certificate
	 * @return If existing credentials are available they will be provided,
	 *         otherwise null will be returned. This method is Multithread safe.
	 */
	public static IWebAuthenticationProvider getCachedCredentials(WebChannel targetChannel, boolean useApiKey,
			UUID repositoryId, String keyContainerName, boolean useMachineStore) {
		return getCachedCredentials(getEntryUri(targetChannel), useApiKey, repositoryId, keyContainerName,
				useMachineStore);
	}

	/**
	 * Determine the entry URI used for credential keys.
	 *
	 * @param channel the channel
	 * @return the entry uri
	 */
	public static String getEntryUri(WebChannel channel) {
		return getEntryUri(channel.getHostName());
	}

	/**
	 * Determine the entry URI used for credential keys.
	 *
	 * @param hostName the host name
	 * @return the entry uri
	 */
	public static String getEntryUri(String hostName) {
		return hostName.toLowerCase(Locale.ROOT); // hopefully that doesn't mess up Unicode host names...
	}

	/**
	 * Get credentials for the specified URL target and repository information.
	 *
	 * @param entryUri         The URI of the endpoint that the credentials are for
	 * @param useApiKey        True if an API key was used to originally set up the
	 *                         connection
	 * @param repositoryId     The owner Id to specify to the server (for example
	 *                         repository Id)
	 * @param keyContainerName The name of the key container to retrieve the private
	 *                         key from
	 * @param useMachineStore  True to use the machine store instead of the user
	 *                         store for the digital certificate
	 * @return If existing credentials are available they will be provided,
	 *         otherwise a new credentials object will be created and returned. This
	 *         method is Multithread safe.
	 */
	private static IWebAuthenticationProvider getCredentials(String entryUri, boolean useApiKey, UUID repositoryId,
			String keyContainerName, boolean useMachineStore) {
		IWebAuthenticationProvider credentials = getCachedCredentials(entryUri, useApiKey, repositoryId,
				keyContainerName, useMachineStore);

		if (credentials == null) {
			// we failed to get them above - so we need to ask the user (outside of our lock
			// so we don't block the world)
			credentials = requestUserCredentials(entryUri, repositoryId);
		}

		return credentials;
	}

	/**
	 * Gets the cached credentials.
	 *
	 * @param entryUri the entry uri
	 * @param useApiKey the use api key
	 * @param repositoryId the repository id
	 * @param keyContainerName the key container name
	 * @param useMachineStore the use machine store
	 * @return the cached credentials
	 */
	private static IWebAuthenticationProvider getCachedCredentials(String entryUri, boolean useApiKey,
			UUID repositoryId, String keyContainerName, boolean useMachineStore) {
		CredentialCacheKey cacheKey = new CredentialCacheKey(entryUri, repositoryId);

		IWebAuthenticationProvider credentials = null;
		synchronized (gLock) // gotta be MT safe!
		{
			gLock.notifyAll();

			if (!gCachedCredentials.containsKey(cacheKey)) {
				// we didn't find it - we need to go ahead and add new credentials
				if (useApiKey) {
					credentials = new RepositoryCredentials(repositoryId, keyContainerName, useMachineStore);
					gCachedCredentials.put(cacheKey, credentials);
				}
			} else {
				credentials = gCachedCredentials.get(cacheKey);
			}

			if (gCachedBlockedCredentials.containsKey(entryUri)) {
				// since they're blocked we repeat the original user's intent
				gLock.notifyAll();
				throw new WebChannelAuthorizationException("User declined to provide credentials");
			}
		}
		return credentials;
	}

	/**
	 * Request user credentials, coordinating between multiple threads looking for
	 * the same credentials.
	 * 
	 * Unlike Update, this will not re-prompt the user if they previously declined
	 * to provide credentials
	 *
	 * @param targetChannel the target channel
	 * @param repositoryId the repository id
	 * @return true, if successful
	 */
	public static boolean requestCredentials(WebChannel targetChannel, UUID repositoryId) {
		return requestCredentials(getEntryUri(targetChannel), repositoryId);
	}

	/**
	 * Request user credentials, coordinating between multiple threads looking for
	 * the same credentials.
	 * 
	 * Unlike Update, this will not re-prompt the user if they previously declined
	 * to provide credentials and it will assume any cached credentials work.
	 *
	 * @param entryUri the entry uri
	 * @param repositoryId the repository id
	 * @return true, if successful
	 */
	public static boolean requestCredentials(String entryUri, UUID repositoryId) {
		IWebAuthenticationProvider credentials = requestUserCredentials(entryUri, repositoryId);

		return (credentials != null);
	}

	/**
	 * Request user credentials, coordinating between multiple threads looking for
	 * the same credentials.
	 * 
	 * Unlike Update, this will not re-prompt the user if they previously declined
	 * to provide credentials and it will assume any cached credentials work.
	 *
	 * @param entryUri the entry uri
	 * @param repositoryId the repository id
	 * @return The new authentication provider
	 * @exception WebChannelAuthorizationException Thrown when no credentials were
	 *                                             provided
	 */
	private static IWebAuthenticationProvider requestUserCredentials(String entryUri, UUID repositoryId) {
		CredentialCacheKey cacheKey = new CredentialCacheKey(entryUri, repositoryId);

		IWebAuthenticationProvider authenticationProvider;

		// we only want one thread to pop up the UI to request authentication at a time.
		synchronized (gRequestLock) {
			// WAIT: Since we requested the lock someone may have put credentials in the
			// collection, so we have to check again.
			synchronized (gLock) {
				gLock.notifyAll();

				if (gCachedBlockedCredentials.containsKey(entryUri)) // someone may have just canceled.
				{
					throw new WebChannelAuthorizationException("User declined to provide credentials");
				}

				if (gCachedCredentials.containsKey(cacheKey)) {
					return gCachedCredentials.get(cacheKey); // yep, someone else got them.
				}
			}

			CredentialsRequiredEventArgs credentialEventArgs = new CredentialsRequiredEventArgs(entryUri, repositoryId,
					false, null);

			if (credentialEventArgs.getCancel()) {
				// if the user canceled we need to cache that so we don't keep pounding the
				// user.
				synchronized (gLock) {
					gCachedBlockedCredentials.put(entryUri, entryUri);

					gLock.notifyAll();
				}

				throw new WebChannelAuthorizationException("User declined to provide credentials");
			}

			if (credentialEventArgs.getAuthenticationProvider() == null) {
				throw new WebChannelAuthorizationException("No credentials are available for the specified server");
			}

			authenticationProvider = credentialEventArgs.getAuthenticationProvider();

			synchronized (gLock) {
				gCachedCredentials.put(cacheKey, authenticationProvider);

				gLock.notifyAll();
			}

			gRequestLock.notifyAll();
		}

		return authenticationProvider;
	}

	/**
	 * Attempt to re-query the credentials for the specified URI.
	 *
	 * @param targetChannel The web channel to update credentials for
	 * @param forceUpdate   True to force a requery to the user even if they
	 *                      previously canceled requesting credentials
	 * @return true, if successful
	 */
	public static boolean updateCredentials(WebChannel targetChannel, boolean forceUpdate) {
		if (targetChannel == null) {
			throw new NullPointerException("targetChannel");
		}

		return updateCredentials(getEntryUri(targetChannel), null, forceUpdate);
	}

	/**
	 * Attempt to re-query the credentials for the specified URI.
	 *
	 * @param targetChannel The web channel to update credentials for
	 * @param repositoryId  The owner Id to specify to the server (for example
	 *                      repository Id)
	 * @param forceUpdate   True to force a requery to the user even if they
	 *                      previously canceled requesting credentials
	 * @return true, if successful
	 */
	public static boolean updateCredentials(WebChannel targetChannel, UUID repositoryId, boolean forceUpdate) {
		if (targetChannel == null) {
			throw new NullPointerException("targetChannel");
		}

		if (repositoryId == null) {
			throw new NullPointerException("repositoryId");
		}

		return updateCredentials(getEntryUri(targetChannel), repositoryId, forceUpdate);
	}

	/**
	 * Attempt to re-query the credentials for the specified URI.
	 *
	 * @param entryUri     The entry URI to update credentials for
	 * @param repositoryId The owner Id to specify to the server (for example
	 *                     repository Id)
	 * @param forceUpdate  True to force a requery to the user even if they
	 *                     previously canceled requesting credentials
	 * @return True if the user provided updated credentials, false if they canceled
	 */
	public static boolean updateCredentials(String entryUri, UUID repositoryId, boolean forceUpdate) {
		CredentialCacheKey cacheKey = new CredentialCacheKey(entryUri, repositoryId);

		boolean newCredentialsProvided = false;
		synchronized (gLock) // if we have any in cache we need to update those, so in this case we stall
								// EVERYONE.
		{
			// WAIT: Since we requested the lock someone may have put credentials in the
			// collection, so we have to check again.
			gLock.notifyAll();

			if (!forceUpdate && (gCachedBlockedCredentials.containsKey(entryUri))) {
				return false;
			}

			IWebAuthenticationProvider credentials;
			credentials = gCachedCredentials.get(cacheKey);

			// we only want one thread to pop up the UI to request authentication at a time.
			synchronized (gRequestLock) {
				CredentialsRequiredEventArgs credentialEventArgs = new CredentialsRequiredEventArgs(entryUri,
						repositoryId, true, credentials);

				if (!credentialEventArgs.getCancel()) {
					if (credentialEventArgs.getAuthenticationProvider() == null) {
						throw new IllegalStateException("No credentials are available for the specified server");
					}

					newCredentialsProvided = true;

					gCachedBlockedCredentials.remove(entryUri); // if it was previously blocked, unblock it.

					gCachedCredentials.put(cacheKey, credentialEventArgs.getAuthenticationProvider()); // overwrite any
																										// existing
																										// value.
				}

				gRequestLock.notifyAll();
			}

			gLock.notifyAll();
		}

		return newCredentialsProvided;
	}

}