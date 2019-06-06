package com.onloupe.core.util;

import com.onloupe.core.messaging.network.NetworkMessageTypeCode;
import com.onloupe.model.system.Version;


/**
 * The Class PacketHeader.
 */
public class PacketHeader {

	/** The packet length. */
	private Integer packetLength;
	
	/** The type code. */
	private NetworkMessageTypeCode typeCode;
	
	/** The version. */
	private Version version;

	/**
	 * Instantiates a new packet header.
	 *
	 * @param packetLength the packet length
	 * @param typeCode the type code
	 * @param version the version
	 */
	public PacketHeader(Integer packetLength, NetworkMessageTypeCode typeCode, Version version) {
		super();
		this.packetLength = packetLength;
		this.typeCode = typeCode;
		this.version = version;
	}

	/**
	 * Gets the packet length.
	 *
	 * @return the packet length
	 */
	public Integer getPacketLength() {
		return this.packetLength;
	}

	/**
	 * Sets the packet length.
	 *
	 * @param packetLength the new packet length
	 */
	public void setPacketLength(Integer packetLength) {
		this.packetLength = packetLength;
	}

	/**
	 * Gets the type code.
	 *
	 * @return the type code
	 */
	public NetworkMessageTypeCode getTypeCode() {
		return this.typeCode;
	}

	/**
	 * Sets the type code.
	 *
	 * @param typeCode the new type code
	 */
	public void setTypeCode(NetworkMessageTypeCode typeCode) {
		this.typeCode = typeCode;
	}

	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
	public Version getVersion() {
		return this.version;
	}

	/**
	 * Sets the version.
	 *
	 * @param version the new version
	 */
	public void setVersion(Version version) {
		this.version = version;
	}

}
