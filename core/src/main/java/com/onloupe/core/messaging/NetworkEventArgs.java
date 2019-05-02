package com.onloupe.core.messaging;

/**
 * Event arguments for NetworkWriter events
 */
public class NetworkEventArgs {
	/**
	 * Create a new network event arguments object
	 * 
	 * @param description
	 */
	public NetworkEventArgs(String description) {
		setDescription(description);
	}

	/**
	 * An extended description of the cause of the event
	 */
	private String description;

	public final String getDescription() {
		return this.description;
	}

	private void setDescription(String value) {
		this.description = value;
	}
}