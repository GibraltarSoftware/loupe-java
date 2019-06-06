package com.onloupe.core.serialization;


/**
 * Records a summary of packet count and aggregate size for one specific packet
 * type.
 */
public class PacketTypeStorageSummary implements java.lang.Comparable {
	
	/** Qualified type name from the related PacketDefinition. */
	private String qualifiedTypeName;

	/**
	 * Gets the qualified type name.
	 *
	 * @return the qualified type name
	 */
	public final String getQualifiedTypeName() {
		return this.qualifiedTypeName;
	}

	/**
	 * Sets the qualified type name.
	 *
	 * @param value the new qualified type name
	 */
	private void setQualifiedTypeName(String value) {
		this.qualifiedTypeName = value;
	}

	/** Short type name from the related PacketDefinition   In particular, there can be many instances of EventMetricSamplePacket that vary only by QualifiedTypeName. */
	private String typeName;

	/**
	 * Gets the type name.
	 *
	 * @return the type name
	 */
	public final String getTypeName() {
		return this.typeName;
	}

	/**
	 * Sets the type name.
	 *
	 * @param value the new type name
	 */
	private void setTypeName(String value) {
		this.typeName = value;
	}

	/** Number of packets of this type that were read. */
	private int packetCount;

	/**
	 * Gets the packet count.
	 *
	 * @return the packet count
	 */
	public final int getPacketCount() {
		return this.packetCount;
	}

	/**
	 * Sets the packet count.
	 *
	 * @param value the new packet count
	 */
	public final void setPacketCount(int value) {
		this.packetCount = value;
	}

	/**
	 * Total number of bytes of this packet type read from the file
	 * 
	 * 
	 * Packet sizes are collected as uncompressed bytes. But once all fragments have
	 * been read, the FileStorageSummary.Summarize method is called from Session to
	 * scale all the PacketSize values such that they represent compressed bytes.
	 * 
	 */
	private long packetSize;

	/**
	 * Gets the packet size.
	 *
	 * @return the packet size
	 */
	public final long getPacketSize() {
		return this.packetSize;
	}

	/**
	 * Sets the packet size.
	 *
	 * @param value the new packet size
	 */
	public final void setPacketSize(long value) {
		this.packetSize = value;
	}

	/**
	 * Returns the average number of bytes per packet (rounded up).
	 *
	 * @return the average packet size
	 */
	public final long getAveragePacketSize() {
		if (getPacketCount() <= 0) {
			return getPacketSize();
		}

		return (long) Math.ceil((double) getPacketSize() / getPacketCount());
	}

	/**
	 * Create a storage summary instance referencing a particualr PacketDefinition.
	 *
	 * @param packetDefinition the packet definition
	 */
	public PacketTypeStorageSummary(PacketDefinition packetDefinition) {
		setQualifiedTypeName(packetDefinition.getQualifiedTypeName());
		setTypeName(packetDefinition.getTypeName());
		setPacketCount(packetDefinition.getPacketCount());
		setPacketSize(packetDefinition.getPacketSize());
	}

	/**
	 * Default sort is descending by PacketCount within descending PacketSize.
	 *
	 * @param obj the obj
	 * @return the int
	 */
	@Override
	public final int compareTo(Object obj) {
		PacketTypeStorageSummary other = (PacketTypeStorageSummary) obj;

		if (getPacketSize() == other.getPacketSize()) {
			return other.getPacketCount() - getPacketCount();
		}

		return (int) (other.getPacketSize() - getPacketSize());
	}
}