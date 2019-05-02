package com.onloupe.core.server;

import com.onloupe.configuration.ServerConfiguration;

public class HubConnectionStatus {
	public HubConnectionStatus(ServerConfiguration configuration, boolean isValid, HubStatus status, String message) {
		this(configuration, null, null, isValid, status, message);
	}

	public HubConnectionStatus(ServerConfiguration configuration, WebChannel channel, HubRepository repository,
			boolean isValid, HubStatus status, String message) {
		setConfiguration(configuration);
		setChannel(channel);
		setRepository(repository);
		setStatus(status);
		setMessage(message);
		setIsValid(isValid);
	}

	private ServerConfiguration configuration;

	public final ServerConfiguration getConfiguration() {
		return this.configuration;
	}

	private void setConfiguration(ServerConfiguration value) {
		this.configuration = value;
	}

	private HubRepository repository;

	public final HubRepository getRepository() {
		return this.repository;
	}

	private void setRepository(HubRepository value) {
		this.repository = value;
	}

	/**
	 * The hub status of the final hub connected to.
	 */
	private HubStatus status;

	public final HubStatus getStatus() {
		return this.status;
	}

	private void setStatus(HubStatus value) {
		this.status = value;
	}

	/**
	 * An end-user display message providing feedback on why a connection is not
	 * available
	 */
	private String message;

	public final String getMessage() {
		return this.message;
	}

	private void setMessage(String value) {
		this.message = value;
	}

	/**
	 * True if the configuration is valid and the server is available, false
	 * otherwise.
	 */
	private boolean valid;

	public final boolean isValid() {
		return this.valid;
	}

	private void setIsValid(boolean value) {
		this.valid = value;
	}

	/**
	 * The channel that was connected
	 */
	private WebChannel channel;

	public final WebChannel getChannel() {
		return this.channel;
	}

	private void setChannel(WebChannel value) {
		this.channel = value;
	}
}