package com.onloupe.core.messaging.network;


/**
 * The Enum NetworkMessageTypeCode.
 */
public enum NetworkMessageTypeCode {
	
	/** The unknown. */
	UNKNOWN(0), 
 /** The live view start command. */
 LIVE_VIEW_START_COMMAND(1), 
 /** The live view stop command. */
 LIVE_VIEW_STOP_COMMAND(2), 
 /** The send session. */
 SEND_SESSION(3), 
 /** The session header. */
 SESSION_HEADER(4),
	
	/** The get session headers. */
	GET_SESSION_HEADERS(5), 
 /** The register analyst command. */
 REGISTER_ANALYST_COMMAND(6), 
 /** The register agent command. */
 REGISTER_AGENT_COMMAND(7), 
 /** The session closed. */
 SESSION_CLOSED(8),
	
	/** The packet stream start command. */
	PACKET_STREAM_START_COMMAND(9),

	/** Measures the clock drift and latency between two computers. */
	CLOCK_DRIFT(10);

	/** The Constant SIZE. */
	public static final int SIZE = java.lang.Integer.SIZE;

	/** The int value. */
	private int intValue;
	
	/** The mappings. */
	private static java.util.HashMap<Integer, NetworkMessageTypeCode> mappings;

	/**
	 * Gets the mappings.
	 *
	 * @return the mappings
	 */
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

	/**
	 * Instantiates a new network message type code.
	 *
	 * @param value the value
	 */
	private NetworkMessageTypeCode(int value) {
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
	 * @return the network message type code
	 */
	public static NetworkMessageTypeCode forValue(int value) {
		return getMappings().get(value);
	}
}