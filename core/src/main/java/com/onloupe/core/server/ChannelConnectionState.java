package com.onloupe.core.server;

// TODO: Auto-generated Javadoc
/**
 * The status of the subscription connection.
 */
public enum ChannelConnectionState {
	
	/** The subscription is disconnected. */
	DISCONNECTED(0),

	/** The subscription is attempting to connect. */
	CONNECTING(1),

	/**
	 * The subscription is connected.
	 */
	CONNECTED(2),

	/** The subscription is actively transferring data. */
	TRANSFERING_DATA(3);

	/** The Constant SIZE. */
	public static final int SIZE = java.lang.Integer.SIZE;

	/** The int value. */
	private int intValue;
	
	/** The mappings. */
	private static java.util.HashMap<Integer, ChannelConnectionState> mappings;

	/**
	 * Gets the mappings.
	 *
	 * @return the mappings
	 */
	private static java.util.HashMap<Integer, ChannelConnectionState> getMappings() {
		if (mappings == null) {
			synchronized (ChannelConnectionState.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, ChannelConnectionState>();
				}
			}
		}
		return mappings;
	}

	/**
	 * Instantiates a new channel connection state.
	 *
	 * @param value the value
	 */
	private ChannelConnectionState(int value) {
		this.intValue = value;
		getMappings().put(value, this);
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public int getValue() {
		return this.intValue;
	}

	/**
	 * For value.
	 *
	 * @param value the value
	 * @return the channel connection state
	 */
	public static ChannelConnectionState forValue(int value) {
		return getMappings().get(value);
	}
}