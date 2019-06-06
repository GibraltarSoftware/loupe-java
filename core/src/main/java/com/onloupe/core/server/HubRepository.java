package com.onloupe.core.server;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import com.onloupe.model.system.Version;


/**
 * Information about the capabilities and status of a server repository.
 */
public class HubRepository {
	
	/**
	 * Instantiates a new hub repository.
	 *
	 * @param expirationDt the expiration dt
	 * @param serverRepositoryId the server repository id
	 * @param protocolVersion the protocol version
	 * @param publicKey the public key
	 * @param agentOptions the agent options
	 * @param clientOptions the client options
	 */
	public HubRepository(Optional<OffsetDateTime> expirationDt, Optional<UUID> serverRepositoryId,
			Version protocolVersion, String publicKey, NetworkConnectionOptions agentOptions,
			NetworkConnectionOptions clientOptions) {
		setExpirationDt(expirationDt);
		setServerRepositoryId(serverRepositoryId);
		setProtocolVersion(protocolVersion);
		setPublicKey(publicKey);
		setAgentLiveStreamOptions(agentOptions);
		setClientLiveStreamOptions(clientOptions);
	}

	/** The expiration dt. */
	private Optional<OffsetDateTime> expirationDt = Optional.empty();

	/**
	 * Gets the expiration dt.
	 *
	 * @return the expiration dt
	 */
	public final Optional<OffsetDateTime> getExpirationDt() {
		return this.expirationDt;
	}

	/**
	 * Sets the expiration dt.
	 *
	 * @param value the new expiration dt
	 */
	private void setExpirationDt(Optional<OffsetDateTime> value) {
		this.expirationDt = value;
	}

	/** The server repository id. */
	private Optional<UUID> serverRepositoryId = Optional.empty();

	/**
	 * Gets the server repository id.
	 *
	 * @return the server repository id
	 */
	public final Optional<UUID> getServerRepositoryId() {
		return this.serverRepositoryId;
	}

	/**
	 * Sets the server repository id.
	 *
	 * @param value the new server repository id
	 */
	private void setServerRepositoryId(Optional<UUID> value) {
		this.serverRepositoryId = value;
	}

	/** The protocol version. */
	private Version protocolVersion;

	/**
	 * Gets the protocol version.
	 *
	 * @return the protocol version
	 */
	public final Version getProtocolVersion() {
		return this.protocolVersion;
	}

	/**
	 * Sets the protocol version.
	 *
	 * @param value the new protocol version
	 */
	private void setProtocolVersion(Version value) {
		this.protocolVersion = value;
	}

	/** The public key. */
	private String publicKey;

	/**
	 * Gets the public key.
	 *
	 * @return the public key
	 */
	public final String getPublicKey() {
		return this.publicKey;
	}

	/**
	 * Sets the public key.
	 *
	 * @param value the new public key
	 */
	private void setPublicKey(String value) {
		this.publicKey = value;
	}

	/** The agent live stream options. */
	private NetworkConnectionOptions agentLiveStreamOptions;

	/**
	 * Gets the agent live stream options.
	 *
	 * @return the agent live stream options
	 */
	public final NetworkConnectionOptions getAgentLiveStreamOptions() {
		return this.agentLiveStreamOptions;
	}

	/**
	 * Sets the agent live stream options.
	 *
	 * @param value the new agent live stream options
	 */
	private void setAgentLiveStreamOptions(NetworkConnectionOptions value) {
		this.agentLiveStreamOptions = value;
	}

	/** The client live stream options. */
	private NetworkConnectionOptions clientLiveStreamOptions;

	/**
	 * Gets the client live stream options.
	 *
	 * @return the client live stream options
	 */
	public final NetworkConnectionOptions getClientLiveStreamOptions() {
		return this.clientLiveStreamOptions;
	}

	/**
	 * Sets the client live stream options.
	 *
	 * @param value the new client live stream options
	 */
	private void setClientLiveStreamOptions(NetworkConnectionOptions value) {
		this.clientLiveStreamOptions = value;
	}

	/**
	 * Indicates if the server supports file fragments or just a single stream per
	 * session.
	 *
	 * @return the supports file fragments
	 */
	public final boolean getSupportsFileFragments() {
		if (getProtocolVersion().compareTo(HubConnection.hub30ProtocolVersion) >= 0) // we introduced file fragments in
																						// 1.2
		{
			return true;
		}
		return false;
	}

	/**
	 * Indicates if the server supports the API for log events, etc.
	 *
	 * @return the supports server api
	 */
	public final boolean getSupportsServerApi() {
		if (getProtocolVersion().compareTo(HubConnection.hub38ProtocolVersion) >= 0) // we introduced file fragments in
																						// 1.3
		{
			return true;
		}
		return false;
	}
}