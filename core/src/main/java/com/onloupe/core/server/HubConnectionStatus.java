package com.onloupe.core.server;

import com.onloupe.configuration.ServerConfiguration;

// TODO: Auto-generated Javadoc
/**
 * The Class HubConnectionStatus.
 */
public class HubConnectionStatus {
	
	/**
	 * Instantiates a new hub connection status.
	 *
	 * @param configuration the configuration
	 * @param isValid the is valid
	 * @param status the status
	 * @param message the message
	 */
	public HubConnectionStatus(ServerConfiguration configuration, boolean isValid, HubStatus status, String message) {
		this(configuration, null, null, isValid, status, message);
	}

	/**
	 * Instantiates a new hub connection status.
	 *
	 * @param configuration the configuration
	 * @param channel the channel
	 * @param repository the repository
	 * @param isValid the is valid
	 * @param status the status
	 * @param message the message
	 */
	public HubConnectionStatus(ServerConfiguration configuration, WebChannel channel, HubRepository repository,
			boolean isValid, HubStatus status, String message) {
		setConfiguration(configuration);
		setChannel(channel);
		setRepository(repository);
		setStatus(status);
		setMessage(message);
		setIsValid(isValid);
	}

	/** The configuration. */
	private ServerConfiguration configuration;

	/**
	 * Gets the configuration.
	 *
	 * @return the configuration
	 */
	public final ServerConfiguration getConfiguration() {
		return this.configuration;
	}

	/**
	 * Sets the configuration.
	 *
	 * @param value the new configuration
	 */
	private void setConfiguration(ServerConfiguration value) {
		this.configuration = value;
	}

	/** The repository. */
	private HubRepository repository;

	/**
	 * Gets the repository.
	 *
	 * @return the repository
	 */
	public final HubRepository getRepository() {
		return this.repository;
	}

	/**
	 * Sets the repository.
	 *
	 * @param value the new repository
	 */
	private void setRepository(HubRepository value) {
		this.repository = value;
	}

	/**
	 * The hub status of the final hub connected to.
	 */
	private HubStatus status;

	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	public final HubStatus getStatus() {
		return this.status;
	}

	/**
	 * Sets the status.
	 *
	 * @param value the new status
	 */
	private void setStatus(HubStatus value) {
		this.status = value;
	}

	/** An end-user display message providing feedback on why a connection is not available. */
	private String message;

	/**
	 * Gets the message.
	 *
	 * @return the message
	 */
	public final String getMessage() {
		return this.message;
	}

	/**
	 * Sets the message.
	 *
	 * @param value the new message
	 */
	private void setMessage(String value) {
		this.message = value;
	}

	/**
	 * True if the configuration is valid and the server is available, false
	 * otherwise.
	 */
	private boolean valid;

	/**
	 * Checks if is valid.
	 *
	 * @return true, if is valid
	 */
	public final boolean isValid() {
		return this.valid;
	}

	/**
	 * Sets the checks if is valid.
	 *
	 * @param value the new checks if is valid
	 */
	private void setIsValid(boolean value) {
		this.valid = value;
	}

	/** The channel that was connected. */
	private WebChannel channel;

	/**
	 * Gets the channel.
	 *
	 * @return the channel
	 */
	public final WebChannel getChannel() {
		return this.channel;
	}

	/**
	 * Sets the channel.
	 *
	 * @param value the new channel
	 */
	private void setChannel(WebChannel value) {
		this.channel = value;
	}
}