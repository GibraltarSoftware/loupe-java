package com.onloupe.core.server;

/**
 * The event arguments for the connection state changed event
 */
public class ChannelConnectionStateChangedEventArgs {
	public ChannelConnectionStateChangedEventArgs(ChannelConnectionState state) {
		setState(state);
	}

	/**
	 * The current connection state
	 */
	private ChannelConnectionState state = ChannelConnectionState.values()[0];

	public final ChannelConnectionState getState() {
		return this.state;
	}

	private void setState(ChannelConnectionState value) {
		this.state = value;
	}
}