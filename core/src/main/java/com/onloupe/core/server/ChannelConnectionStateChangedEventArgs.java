package com.onloupe.core.server;

// TODO: Auto-generated Javadoc
/**
 * The event arguments for the connection state changed event.
 */
public class ChannelConnectionStateChangedEventArgs {
	
	/**
	 * Instantiates a new channel connection state changed event args.
	 *
	 * @param state the state
	 */
	public ChannelConnectionStateChangedEventArgs(ChannelConnectionState state) {
		setState(state);
	}

	/** The current connection state. */
	private ChannelConnectionState state = ChannelConnectionState.values()[0];

	/**
	 * Gets the state.
	 *
	 * @return the state
	 */
	public final ChannelConnectionState getState() {
		return this.state;
	}

	/**
	 * Sets the state.
	 *
	 * @param value the new state
	 */
	private void setState(ChannelConnectionState value) {
		this.state = value;
	}
}