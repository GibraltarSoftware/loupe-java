package com.onloupe.core.server;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import com.onloupe.model.system.Version;

/**
 * Information about the capabilities and status of a server repository
 */
public class HubRepository {
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

	private Optional<OffsetDateTime> expirationDt = Optional.empty();

	public final Optional<OffsetDateTime> getExpirationDt() {
		return this.expirationDt;
	}

	private void setExpirationDt(Optional<OffsetDateTime> value) {
		this.expirationDt = value;
	}

	private Optional<UUID> serverRepositoryId = Optional.empty();

	public final Optional<UUID> getServerRepositoryId() {
		return this.serverRepositoryId;
	}

	private void setServerRepositoryId(Optional<UUID> value) {
		this.serverRepositoryId = value;
	}

	private Version protocolVersion;

	public final Version getProtocolVersion() {
		return this.protocolVersion;
	}

	private void setProtocolVersion(Version value) {
		this.protocolVersion = value;
	}

	private String publicKey;

	public final String getPublicKey() {
		return this.publicKey;
	}

	private void setPublicKey(String value) {
		this.publicKey = value;
	}

	private NetworkConnectionOptions agentLiveStreamOptions;

	public final NetworkConnectionOptions getAgentLiveStreamOptions() {
		return this.agentLiveStreamOptions;
	}

	private void setAgentLiveStreamOptions(NetworkConnectionOptions value) {
		this.agentLiveStreamOptions = value;
	}

	private NetworkConnectionOptions clientLiveStreamOptions;

	public final NetworkConnectionOptions getClientLiveStreamOptions() {
		return this.clientLiveStreamOptions;
	}

	private void setClientLiveStreamOptions(NetworkConnectionOptions value) {
		this.clientLiveStreamOptions = value;
	}

	/**
	 * Indicates if the server supports file fragments or just a single stream per
	 * session
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