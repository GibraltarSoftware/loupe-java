package com.onloupe.core.messaging.network;

public enum NetworkMessageTypeCode {
	UNKNOWN(0), LIVE_VIEW_START_COMMAND(1), LIVE_VIEW_STOP_COMMAND(2), SEND_SESSION(3), SESSION_HEADER(4),
	GET_SESSION_HEADERS(5), REGISTER_ANALYST_COMMAND(6), REGISTER_AGENT_COMMAND(7), SESSION_CLOSED(8),
	PACKET_STREAM_START_COMMAND(9),

	/**
	 * Measures the clock drift and latency between two computers
	 */
	CLOCK_DRIFT(10);

	public static final int SIZE = java.lang.Integer.SIZE;

	private int intValue;
	private static java.util.HashMap<Integer, NetworkMessageTypeCode> mappings;

	private static java.util.HashMap<Integer, NetworkMessageTypeCode> getMappings() {
		if (mappings == null) {
			synchronized (NetworkMessageTypeCode.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, NetworkMessageTypeCode>();
				}
			}
		}
		return mappings;
	}

	private NetworkMessageTypeCode(int value) {
		this.intValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return this.intValue;
	}

	public static NetworkMessageTypeCode forValue(int value) {
		return getMappings().get(value);
	}
}