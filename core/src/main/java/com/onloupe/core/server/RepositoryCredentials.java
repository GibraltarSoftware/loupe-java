package com.onloupe.core.server;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;

import com.onloupe.core.util.IOUtils;
import com.onloupe.core.util.TypeUtils;

/**
 * Authentication credentials for a repository to a shared data service.
 */
public final class RepositoryCredentials implements IWebAuthenticationProvider {
	/**
	 * The prefix for the authorization header for this credential type
	 */
	public static final String AUTHORIZATION_PREFIX = "Gibraltar-Repository";

	/**
	 * The HTTP Request header identifying the client repository
	 */
	public static final String CLIENT_REPOSITORY_HEADER = "X-Gibraltar-Repository";

	public static final String AUTHORIZATION_HEADER = "Authorization";

	private final Object lock = new Object();

	private String accessToken; // PROTECTED BY LOCK

	/**
	 * Create a new set of repository credentials
	 * 
	 * @param repositoryId     The owner Id to specify to the server (for example
	 *                         repository Id)
	 * @param keyContainerName The name of the key container to retrieve the private
	 *                         key from
	 * @param useMachineStore  True to use the machine store instead of the user
	 *                         store for the digital certificate
	 */
	public RepositoryCredentials(UUID repositoryId, String keyContainerName, boolean useMachineStore) {
		if (repositoryId == null) {
			throw new IndexOutOfBoundsException("The supplied repository Id is an empty guid, which can't be right.");
		}

		setRepositoryId(repositoryId);
		setKeyContainerName(keyContainerName);
		setUseMachineStore(useMachineStore);
	}

	/**
	 * True to use the machine store instead of the user store for the digital
	 * certificate
	 */
	private boolean useMachineStore;

	public boolean getUseMachineStore() {
		return this.useMachineStore;
	}

	private void setUseMachineStore(boolean value) {
		this.useMachineStore = value;
	}

	/**
	 * The name of the key container to retrieve the private key from
	 */
	private String keyContainerName;

	public String getKeyContainerName() {
		return this.keyContainerName;
	}

	private void setKeyContainerName(String value) {
		this.keyContainerName = value;
	}

	/**
	 * The owner Id to specify to the server (for example repository Id)
	 */
	private UUID repositoryId;

	public UUID getRepositoryId() {
		return this.repositoryId;
	}

	private void setRepositoryId(UUID value) {
		this.repositoryId = value;
	}

	/**
	 * Indicates if the authentication provider believes it has authenticated with
	 * the channel
	 * 
	 * If false then no logout will be attempted, and any request that requires
	 * authentication will cause a login attempt without waiting for an
	 * authentication failure.
	 */
	@Override
	public boolean isAuthenticated() {
		boolean isAuthenticated = false;

		// we have to always use a lock when handling the access token.
		synchronized (this.lock) {
			isAuthenticated = (TypeUtils.isNotBlank(this.accessToken));
			this.lock.notifyAll();
		}

		return isAuthenticated;
	}

	/**
	 * indicates if the authentication provider can perform a logout
	 */
	@Override
	public boolean logoutIsSupported() {
		return false;
	}

	/**
	 * Perform a login on the supplied channel
	 * 
	 * @param channel
	 * @param client
	 * @throws Exception
	 */
	@Override
	public void login(WebChannel channel, HttpClient client) throws Exception {
		// we need to get the access token for our repository id
		String requestUrl = String.format("Repositories/%1$s/AccessToken.bin", getRepositoryId());

		HttpResponse accessTokenResponse = client.execute(new HttpGet(requestUrl));

		if (accessTokenResponse != null) {
			synchronized (this.lock) {
				// and here we WOULD decrypt the access token if it was encrypted
				this.accessToken = IOUtils.copyToString(accessTokenResponse.getEntity().getContent());
				this.lock.notifyAll();
			}
		}
	}

	/**
	 * Perform a logout on the supplied channel
	 * 
	 * @param channel
	 * @param client
	 */
	@Override
	public void logout(WebChannel channel, HttpClient client) {
		// we have to always use a lock when handling the access token.
		synchronized (this.lock) {
			this.accessToken = null;
			this.lock.notifyAll();
		}
	}

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
	 * @throws URISyntaxException
	 */
	@Override
	public void preProcessRequest(WebChannel channel, CloseableHttpClient client, HttpRequestBase request,
			String resourceUrl, boolean requestSupportsAuthentication) throws URISyntaxException {
		// figure out the effective relative URL.
		String fullUrl = resourceUrl;
		if (channel.getEntryURI() != null) {
			fullUrl = channel.getEntryURI() + resourceUrl;
		}

		java.net.URI clientUri;
		clientUri = new java.net.URI(fullUrl);
		// we're doing sets not adds to make sure we overwrite any existing value.
		if (requestSupportsAuthentication) {
			request.addHeader(AUTHORIZATION_HEADER,
					AUTHORIZATION_PREFIX + ": " + calculateHash(clientUri.getPath() + "?" + clientUri.getQuery()));
			request.addHeader(CLIENT_REPOSITORY_HEADER, getRepositoryId().toString());
		} else {
			// remove our repository header.
			request.removeHeaders(CLIENT_REPOSITORY_HEADER);
		}

	}

	/**
	 * Calculates the effective hash given the provided salt text.
	 * 
	 * @param saltText
	 * @return
	 */
	private String calculateHash(String saltText) {
		byte[] buffer;

		// we have to always use a lock when handling the access token.
		synchronized (this.lock) {
			buffer = StandardCharsets.UTF_8.encode(this.accessToken + saltText).array();
			this.lock.notifyAll();
		}

		return Base64.getEncoder().encodeToString(DigestUtils.sha1Hex(buffer).getBytes());
	}

}