package com.onloupe.core.util;

import com.onloupe.core.messaging.network.NetworkMessageTypeCode;
import com.onloupe.model.system.Version;

public class PacketHeader {

	private Integer packetLength;
	private NetworkMessageTypeCode typeCode;
	private Version version;

	public PacketHeader(Integer packetLength, NetworkMessageTypeCode typeCode, Version version) {
		super();
		this.packetLength = packetLength;
		this.typeCode = typeCode;
		this.version = version;
	}

	public Integer getPacketLength() {
		return this.packetLength;
	}

	public void setPacketLength(Integer packetLength) {
		this.packetLength = packetLength;
	}

	public NetworkMessageTypeCode getTypeCode() {
		return this.typeCode;
	}

	public void setTypeCode(NetworkMessageTypeCode typeCode) {
		this.typeCode = typeCode;
	}

	public Version getVersion() {
		return this.version;
	}

	public void setVersion(Version version) {
		this.version = version;
	}

}
