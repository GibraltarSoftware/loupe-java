package com.onloupe.core.server;

import com.onloupe.core.server.data.HubStatusXml;

/**
 * The current status of a server that is accessible over the network
 */
public enum HubStatus {
	/**
	 * The current status couldn't be determined.
	 */
	UNKNOWN(0),

	/**
	 * The server is accessible and operational.
	 */
	AVAILABLE(HubStatusXml.AVAILABLE.getValue()),

	/**
	 * The server has no license and should not be communicated with.
	 */
	EXPIRED(HubStatusXml.EXPIRED.getValue()),

	/**
	 * The server is currently undergoing maintenance and is not operational.
	 */
	MAINTENANCE(HubStatusXml.MAINTENANCE.getValue());

	public static final int SIZE = java.lang.Integer.SIZE;

	private int intValue;
	private static java.util.HashMap<Integer, HubStatus> mappings;

	private static java.util.HashMap<Integer, HubStatus> getMappings() {
		if (mappings == null) {
			synchronized (HubStatus.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, HubStatus>();
				}
			}
		}
		return mappings;
	}

	private HubStatus(int value) {
		this.intValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return this.intValue;
	}

	public static HubStatus forValue(int value) {
		return getMappings().get(value);
	}
}