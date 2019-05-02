package com.onloupe.core.server;

/**
 * The status of the subscription connection
 */
public enum ChannelConnectionState {
	/**
	 * The subscription is disconnected
	 */
	DISCONNECTED(0),

	/**
	 * The subscription is attempting to connect
	 */
	CONNECTING(1),

	/**
	 * The subscription is connected.
	 */
	CONNECTED(2),

	/**
	 * The subscription is actively transferring data
	 */
	TRANSFERING_DATA(3);

	public static final int SIZE = java.lang.Integer.SIZE;

	private int intValue;
	private static java.util.HashMap<Integer, ChannelConnectionState> mappings;

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

	private ChannelConnectionState(int value) {
		this.intValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return this.intValue;
	}

	public static ChannelConnectionState forValue(int value) {
		return getMappings().get(value);
	}
}